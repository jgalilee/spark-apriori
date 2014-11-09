package com.jgalilee.spark.apriori

import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark.SparkConf
import org.apache.spark.rdd._
import org.apache.spark.broadcast._
import scala.collection.mutable
import collection.JavaConversions._

object JobDriver {

	val txnIdDelim = "\t"
	val txnItemDelim = " "
	var sc:SparkContext = null

	/**
	 * Step 1 - Load the transactions data into a cached RDD.
	 */
	def loadTxns(txnsIn:String, numPartitions:Int) = {
		// Performed only once at the start of the algorithm. Converts the result to
		// a set and places it back into a string.
		//
		// Doesn't keep it as a desearialized set as this exhibits worse performance
		// than converting it back from a string during Step 2.
		sc.textFile(txnsIn, numPartitions).map { l =>
			val lidx = l.indexOf(txnIdDelim)
			val k = l.substring(0, lidx)
			val v = l.substring(lidx + 1, l.length)
			v.split(txnItemDelim).distinct.mkString(txnItemDelim)
		}
	}

	/**
	 * Step 2 - Generate an RDD of all the candidate k itemsets.
	 */
	def findCandidates(txns:RDD[String], prevRules:Broadcast[Array[String]], k:Int, minSup:Int):RDD[String] = {
		txns.flatMap { items =>
			var cItems1:Array[Int] = items.split(txnItemDelim).map(_.toInt).sorted.toArray
			var combGen1 = new CombinationGenerator();
			var combGen2 = new CombinationGenerator();
			// Use mutable list buffer to protect against thrashing the GC.
			var candidates = scala.collection.mutable.ListBuffer.empty[(String,Int)]
			combGen1.reset(k,cItems1)
			while (combGen1.hasNext()) {
				var cItems2 = combGen1.next();
				var valid = true
				if (k > 1) {
					combGen2.reset(k-1,cItems2);
					while (combGen2.hasNext() && valid) {
						// Explicitly use Java library to prevent array being 'boxed' by the
						// Scala API.
						valid = prevRules.value.contains(java.util.Arrays.toString(combGen2.next()))
					}
				}
				if (valid) {
					candidates += Tuple2(java.util.Arrays.toString(cItems2),1)
				}
			}
			// Flatmap expects enumerable return type.
			candidates
		}.reduceByKey(_+_).filter(_._2 >= minSup).map { case (itemset, _) => itemset }
	}

	/**
	 * Run the Apriori algorithm until convergence.
	 */
	def run(txnsIn:String, maxIterations:Int, minSup:Int, output:String, partitions:Int) = {
		var k = 1
		var hasConverged = false
		// Step 1
		val txns = loadTxns(txnsIn, partitions).cache()
		var previousRules:Broadcast[Array[String]] = null
		while (k < maxIterations && !hasConverged) {
			printf("Starting Iteration %s\n", k)
			// Step 2. Calcualte the net set of candidates using the transformations.
			// Convert the result back into the array (expensive, but required), then
			// using the length of this array to check for convergence the algorithm
			// can be marked converged, or saved back to disk.
			var supportedRules = findCandidates(txns, previousRules, k, minSup)
			var tempPrevRules = supportedRules.toArray()
			var ruleCount = tempPrevRules.length
			if (0 == ruleCount) {
				hasConverged = true
			} else {
				// Action toArray has already 'materialized' the rules, so rather than
				// parrallelizing the data save it to disk. Discard the previous set of
				// rules.
				previousRules = sc.broadcast(tempPrevRules)
				supportedRules.saveAsTextFile(output + "/" + k)
				k += 1
			}
		}
		printf("Converged at Iteration %s\n", k)
	}

	def main(args: Array[String]) {
		if (args.length != 5) {
			Console.err.println("USAGE input max minsup output partitions")
			return
		}
		val conf = new SparkConf().setAppName("Spark Apriori")
		sc = new SparkContext(conf)
		run(args(0).toString,
			args(1).toInt,
			args(2).toInt,
			args(3).toString,
			args(4).toInt)
	}

}
