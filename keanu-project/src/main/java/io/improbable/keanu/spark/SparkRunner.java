package io.improbable.keanu.spark;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;

import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.sql.SparkSession;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Iterables;

import io.improbable.keanu.network.BayesianNetwork;
import io.improbable.keanu.network.NetworkLoader;
import io.improbable.keanu.util.io.JsonLoader;
import io.improbable.keanu.util.io.ProtobufLoader;
import scala.Tuple2;

public class SparkRunner {

    private final File savedModel;
    private final SparkSession session;

    public SparkRunner(File savedModel) {
        this.savedModel = savedModel;
        this.session = initSparkSession();
    }

    public void run() {
        try (JavaSparkContext jsc = new JavaSparkContext(session.sparkContext())) {
            
            JavaRDD<String> file = jsc.textFile(savedModel.getAbsolutePath());

            JavaRDD<BayesianNetwork> net = file.mapPartitions(new ParseJSON());

            int count = net.map(network -> {
                return network.getAllVertices().size();
            }).reduce((integer, integer2) -> integer + integer2);

            System.out.println(count);

        }
        session.close();
    }

    public static class ParseJSON implements FlatMapFunction<Iterator<String>, BayesianNetwork> {
        public Iterator<BayesianNetwork> call(Iterator<String> lines) throws Exception {
            ArrayList<BayesianNetwork> bayesianNetworks = new ArrayList<BayesianNetwork>();
            String json = "";
            while (lines.hasNext()) {
                String line = lines.next();
                json += line;
            }
            JsonLoader loader = new JsonLoader();
            bayesianNetworks.add(loader.loadNetwork(json));

            return bayesianNetworks.iterator();
        }
    }

    private SparkSession initSparkSession() {
        return SparkSession
            .builder()
            .appName("WordCount")
            .master("local")
            .config("spark.driver.bindAddress", "127.0.0.1")
            .getOrCreate();
    }


}
