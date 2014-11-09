# Spark Apriori

Brute force Apriori algorithm implementation using Spark. This algorithm does
not continue and build the association rules.

## Usage

*input max minsup output partitions*

```
spark-submit \
  --class "com.jgalilee.spark.apriori.JobDriver" \
  --master local[4] \
  ./target/scala-2.10/spark-apriori_2.10-1.0.jar \
  input/transactions.txt \
  10 \
  3 \
  output \
  3
```

* input - Path to the input transaction data.
* max  - Maximum number of iterations to run for.
* minsup - Minimum support candidate itemsets to be deemed frequent itemsets.
* output - Path to write the output for iteration n - i.e. output/n
* partitions - Number of partitions to use for the transaction dataset.

## Assumptions

Input data is assumed to be string representations in integer format -

tid \t 1 \s 2 \s ...

Where \t is a tab, and \s is a space. The number of duplicate items or order is
not important. This will be resolved during the map phase.

## Dependencies

* Spark 1.0.1
* Scala 2.10.4

## Licence

```
The MIT License (MIT)

Copyright (c) 2014 Jack Galilee

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
```
