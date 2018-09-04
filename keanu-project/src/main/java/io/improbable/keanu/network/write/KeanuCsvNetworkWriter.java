package io.improbable.keanu.network.write;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;

import io.improbable.keanu.vertices.Vertex;

public class KeanuCsvNetworkWriter implements KeanuNetworkWriter {
    private final String separator;

    public KeanuCsvNetworkWriter(String separator) {
        this.separator = separator;
    }

    @Override
    public void write(OutputStream outputStream, List<? extends Vertex> vertices) throws IOException {
        Iterator<? extends Vertex> iterator = vertices.iterator();
        while (iterator.hasNext()) {
            Vertex vertex = iterator.next();
            vertex.write(outputStream);
            if (iterator.hasNext()) {
                outputStream.write(separator.getBytes());
            }
        }
    }
}
