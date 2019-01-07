package io.improbable.keanu.spark;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.sql.SparkSession;

import io.improbable.keanu.algorithms.NetworkSamples;
import io.improbable.keanu.algorithms.mcmc.MetropolisHastings;
import io.improbable.keanu.network.BayesianNetwork;
import io.improbable.keanu.util.io.JsonLoader;

public class SparkRunner implements Serializable {

    private final File savedModel;
    private final SparkSession session;
    private final int numPartitions;

    public SparkRunner(File savedModel, int numPartitions) {
        this.savedModel = savedModel;
        this.numPartitions = numPartitions;
        this.session = initSparkSession();
    }

    public void run() {
        try (JavaSparkContext jsc = new JavaSparkContext(session.sparkContext())) {

            JavaRDD<String> file = jsc.textFile(savedModel.getAbsolutePath());
            file = file.coalesce(1);

            JavaRDD<BayesianNetwork> net = file.mapPartitions(new ModelParser(numPartitions));
            net = net.repartition(numPartitions);

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

    class ModelParser implements FlatMapFunction<Iterator<String>, BayesianNetwork> {

        private final int numPartitions;

        public ModelParser(int numPartitions) {
            this.numPartitions = numPartitions;
        }

        public Iterator<BayesianNetwork> call(Iterator<String> lines) throws Exception {
            ArrayList<BayesianNetwork> bayesianNetworks = new ArrayList<>();
            String json = "";

            while (lines.hasNext()) {
                String line = lines.next();
                json += line;
            }

            for (int i = 0; i < numPartitions; i++) {
                JsonLoader loader = new JsonLoader();
                bayesianNetworks.add(loader.loadNetwork(json));
            }

            return bayesianNetworks.iterator();
        }
    }

    private SparkSession initSparkSession() {
        return SparkSession
            .builder()
            .appName("SparkRunner")
            .master("local[" + numPartitions + "]")
            .config("spark.driver.bindAddress", "127.0.0.1")
            .getOrCreate();
    }


}
