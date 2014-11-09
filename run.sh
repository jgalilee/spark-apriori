sbt package \
&& rm -rf final \
&& spark-submit \
  --class "com.jgalilee.spark.apriori.JobDriver" \
  --master local[4] \
  ./target/scala-2.10/spark-apriori_2.10-1.0.jar \
   transactions.txt 10 3 final 3 \
&& cat final/**/p*
