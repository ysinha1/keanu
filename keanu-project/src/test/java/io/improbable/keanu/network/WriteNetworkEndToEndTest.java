package io.improbable.keanu.network;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import org.junit.Before;
import org.junit.Test;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;

import io.improbable.keanu.vertices.ConstantVertex;
import io.improbable.keanu.vertices.dbl.DoubleVertex;
import io.improbable.keanu.vertices.dbl.nonprobabilistic.ConstantDoubleVertex;
import io.improbable.keanu.vertices.dbl.probabilistic.GaussianVertex;

public class WriteNetworkEndToEndTest {

    BayesianNetwork network;

    @Before
    public void createNetwork() {
        ConstantDoubleVertex mu = ConstantVertex.of(1.);
        ConstantDoubleVertex sigma = ConstantVertex.of(2.);
        GaussianVertex gaussianVertex = new GaussianVertex(mu, sigma);
        GaussianVertex gaussianVertex2 = new GaussianVertex(mu, sigma);
        DoubleVertex sum = gaussianVertex.plus(gaussianVertex2);
        network = new BayesianNetwork(sum.getConnectedGraph());
    }

    @Test
    public void youCanWriteANetwork() throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        OutputStream outStream = new PrintStream(byteArrayOutputStream, true, "UTF-8");
        network.write(outStream);
        String expected = Resources.toString(Resources.getResource("graphs/network-example.txt"), Charsets.UTF_8);
        assertThat(byteArrayOutputStream.toString().toString(), equalTo(expected));
    }
}
