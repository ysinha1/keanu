package io.improbable.keanu.vertices;

import java.io.IOException;

import org.nd4j.shade.jackson.core.JsonGenerationException;
import org.nd4j.shade.jackson.core.JsonGenerator;
import org.nd4j.shade.jackson.databind.SerializerProvider;
import org.nd4j.shade.jackson.databind.ser.std.StdSerializer;

public class VertexSerializer extends StdSerializer<Vertex>  {

    public VertexSerializer() {
        this(null);
    }

    public VertexSerializer(Class<Vertex> t) {
        super(t);
    }

    @Override
    public void serialize(Vertex vertex, JsonGenerator jsonGenerator, SerializerProvider serializerProvider)
        throws IOException, JsonGenerationException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeStringField("id", vertex.getId().toString());
        jsonGenerator.writeStringField("type", vertex.getClass().getSimpleName());
        jsonGenerator.writeFieldName("parents");
        jsonGenerator.writeStartArray();
        for (Object parent : vertex.getParents()) {
            jsonGenerator.writeObject((((Vertex)parent).getId().toString()));
        }
        jsonGenerator.writeEndArray();
        jsonGenerator.writeEndObject();
    }
}
