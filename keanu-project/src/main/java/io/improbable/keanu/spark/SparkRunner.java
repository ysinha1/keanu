package io.improbable.keanu.spark;

import java.io.File;

import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.SparkSession;

public class SparkRunner {

    private final File savedModel;
    private final SparkSession session;

    public SparkRunner(File savedModel) {
        this.savedModel = savedModel;
        this.session = initSparkSession();
    }

    public void run() {
        try (JavaSparkContext jsc = new JavaSparkContext()) {
            jsc.textFile(savedModel.getPath());
        }
        session.close();
    }

    private SparkSession initSparkSession() {
        return SparkSession
            .builder()
            .appName("SparkRunner")
            .master("local")
            .config("spark.driver.bindAddress", "127.0.0.1")
            .config("spark.driver.allowMultipleContexts", true)
            .getOrCreate();
    }


}
