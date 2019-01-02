package io.improbable.keanu.spark;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

import org.junit.Before;
import org.junit.Test;

import io.improbable.keanu.network.BayesianNetwork;
import io.improbable.keanu.network.NetworkSaver;
import io.improbable.keanu.util.io.ProtobufSaver;
import io.improbable.keanu.vertices.dbl.DoubleVertex;
import io.improbable.keanu.vertices.dbl.probabilistic.GaussianVertex;

public class SparkRunnerTest {

    @Before
    public void createProtobufFile() throws IOException {
        GaussianVertex a = new GaussianVertex(0, 1);
        GaussianVertex b = new GaussianVertex(5, 2);
        DoubleVertex c = a.plus(b);
        BayesianNetwork bayesianNetwork = new BayesianNetwork(c.getConnectedGraph());
        NetworkSaver saver = new ProtobufSaver(bayesianNetwork);
        FileOutputStream writer = new FileOutputStream("proto.pbf");
        saver.save(writer, true);
    }

    @Test
    public void canRun() {
        File file = new File("proto.pbf");
        SparkRunner runner = new SparkRunner(file);
        runner.run();
    }

}
