package io.improbable.keanu.spark;

import java.util.ArrayList;
import java.util.List;

import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.SparkSession;

public class WordCount {

    public static void main(String[] args) {

        SparkSession spark = SparkSession
            .builder()
            .appName("WordCount")
            .master("local")
            .config("spark.driver.bindAddress", "127.0.0.1")
            .getOrCreate();

        int n;
        JavaRDD<Integer> dataSet;
        try (JavaSparkContext jsc = new JavaSparkContext(spark.sparkContext())) {
            int slices = 2;
            n = 100000 * slices;
            List<Integer> l = new ArrayList<>(n);
            for (int i = 0; i < n; i++) {
                l.add(i);
            }

            dataSet = jsc.parallelize(l, slices);

            int count = dataSet.map(integer -> {
                double x = Math.random() * 2 - 1;
                double y = Math.random() * 2 - 1;
                return (x * x + y * y <= 1) ? 1 : 0;
            }).reduce((integer, integer2) -> integer + integer2);

            System.out.println("Pi is roughly " + 4.0 * count / n);

            spark.stop();
        }
    }

}
