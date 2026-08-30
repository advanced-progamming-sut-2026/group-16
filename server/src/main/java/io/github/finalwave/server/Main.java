package io.github.finalwave.server;

import io.github.finalwave.server.db.DatabaseConfig;
import io.github.finalwave.server.db.DatabaseSanityCheck;
import io.github.finalwave.server.db.ServerDatabase;
import io.github.finalwave.server.session.SessionRegistry;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public final class Main {
    private static final int DEFAULT_PORT = 5454;

    public static void main(String[] args) throws IOException {
        int port = resolvePort(args);
        DatabaseConfig.apply();
        System.out.println("Using database " + DatabaseConfig.resolvedUrl());
        ServerDatabase database = new ServerDatabase();
        database.initializeSchema();
        DatabaseSanityCheck.run(database);
        ServerContext context = new ServerContext(database, new SessionRegistry());
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server listening on port " + port);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                ClientHandler handler = new ClientHandler(clientSocket, context);
                Thread thread = new Thread(handler, "client-" + clientSocket.getRemoteSocketAddress());
                thread.setDaemon(true);
                thread.start();
            }
        }
    }

    private static int resolvePort(String[] args) {
        if (args.length > 0) {
            return Integer.parseInt(args[0]);
        }
        String envPort = System.getenv("PVZ_SERVER_PORT");
        if (envPort != null && !envPort.isBlank()) {
            return Integer.parseInt(envPort.trim());
        }
        return DEFAULT_PORT;
    }
}
