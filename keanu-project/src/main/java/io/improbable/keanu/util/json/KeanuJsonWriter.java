package io.improbable.keanu.util.json;

import java.io.IOException;
import java.io.OutputStream;

import org.nd4j.shade.jackson.databind.ObjectMapper;
import org.nd4j.shade.jackson.databind.module.SimpleModule;

import io.improbable.keanu.vertices.Vertex;
import io.improbable.keanu.vertices.VertexSerializer;

public class KeanuJsonWriter {

    private final ObjectMapper objectMapper;

    public KeanuJsonWriter() {
        objectMapper = new ObjectMapper();
        setUpObjectMapper();
    }

    private void setUpObjectMapper() {
        SimpleModule module = new SimpleModule();
        module.addSerializer(Vertex.class, new VertexSerializer());
        objectMapper.registerModule(module);
    }

    public void writeValue(OutputStream outputStream, Object object) throws IOException {
        objectMapper.writeValue(outputStream, object);
    }
}
