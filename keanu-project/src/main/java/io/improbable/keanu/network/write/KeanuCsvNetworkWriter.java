package io.improbable.keanu.network.write;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import com.google.common.base.Joiner;

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
            write(outputStream, vertex);
            if (iterator.hasNext()) {
                outputStream.write(separator.getBytes());
            }
        }
    }

    private void write(OutputStream outputStream, Vertex<?> vertex) throws IOException {
        String line = String.format("%s|%s|%s",
            formatIdOf(vertex),
            vertex.getClass().getSimpleName(),
            vertex.getParents().stream().map(v -> formatIdOf(v)).collect(Collectors.toList()));
        outputStream.write(line.getBytes());
    }

    private static String formatIdOf(Vertex<?> v) {
        Object[] values = Arrays.stream(v.getId().getIdValues()).boxed().toArray();
        return Joiner.on(",").join(values);
    }
}
