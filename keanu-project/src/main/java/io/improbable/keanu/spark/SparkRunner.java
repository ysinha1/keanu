package io.improbable.keanu.spark;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.sql.SparkSession;

import io.improbable.keanu.algorithms.NetworkSample;
import io.improbable.keanu.algorithms.NetworkSamples;
import io.improbable.keanu.algorithms.mcmc.MetropolisHastings;
import io.improbable.keanu.network.BayesianNetwork;
import io.improbable.keanu.util.io.JsonLoader;
import io.improbable.keanu.vertices.dbl.probabilistic.GaussianVertex;

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

            JavaRDD<NetworkSamples> netSamples = net.map(network -> {
                NetworkSamples posteriorSamples = MetropolisHastings.withDefaultConfig().getPosteriorSamples(
                    network,
                    network.getLatentVertices(),
                    5000
                );
                return posteriorSamples;
            });

            int count = netSamples.map(networkSamples -> {
                return networkSamples.size();
            }).reduce((i, i1) -> i + i1);

            System.out.println(count);

        }
        session.close();
    }

    public static class ParseJSON implements FlatMapFunction<Iterator<String>, BayesianNetwork> {
        public Iterator<BayesianNetwork> call(Iterator<String> lines) throws Exception {
            ArrayList<BayesianNetwork> bayesianNetworks = new ArrayList<>();
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
            .appName("SparkRunner")
            .master("local")
            .config("spark.driver.bindAddress", "127.0.0.1")
            .getOrCreate();
    }


}
