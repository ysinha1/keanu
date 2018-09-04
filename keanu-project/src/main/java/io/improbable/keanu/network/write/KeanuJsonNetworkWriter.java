package io.improbable.keanu.network.write;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import io.improbable.keanu.util.json.KeanuJsonWriter;
import io.improbable.keanu.vertices.Vertex;

public class KeanuJsonNetworkWriter implements KeanuNetworkWriter {

    private final KeanuJsonWriter jsonWriter;

    public KeanuJsonNetworkWriter() {
        this(new KeanuJsonWriter());
    }

    public KeanuJsonNetworkWriter(KeanuJsonWriter jsonWriter) {
        this.jsonWriter = jsonWriter;
    }

    @Override
    public void write(OutputStream outputStream, List<? extends Vertex> vertices) throws IOException {
        jsonWriter.writeValue(outputStream, vertices);
    }
}
