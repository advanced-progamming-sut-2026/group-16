package io.github.finalwave.network;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

public final class JsonLineProtocol {
    private final ObjectMapper mapper;
    private final BufferedReader reader;
    private final BufferedWriter writer;

    public JsonLineProtocol(ObjectMapper mapper, InputStream input, OutputStream output) {
        this.mapper = mapper;
        this.reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8));
        this.writer = new BufferedWriter(new OutputStreamWriter(output, StandardCharsets.UTF_8));
    }

    public void send(MessageEnvelope message) throws IOException {
        writer.write(mapper.writeValueAsString(message));
        writer.newLine();
        writer.flush();
    }

    public MessageEnvelope receive() throws IOException {
        String line = reader.readLine();
        if (line == null || line.isBlank()) {
            return null;
        }
        return mapper.readValue(line, MessageEnvelope.class);
    }
}
