package io.improbable.keanu.network.write;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import io.improbable.keanu.vertices.Vertex;

public interface KeanuNetworkWriter {
    void write(OutputStream outputStream, List<? extends Vertex> vertices) throws IOException;
}
