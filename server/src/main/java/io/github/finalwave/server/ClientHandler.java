package io.github.finalwave.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.finalwave.network.JsonLineProtocol;
import io.github.finalwave.network.MessageEnvelope;
import io.github.finalwave.network.MessageTypes;
import io.github.finalwave.server.auth.LoginHandler;
import io.github.finalwave.server.auth.LoginService;
import io.github.finalwave.server.auth.RegisterHandler;
import io.github.finalwave.server.auth.ResumeHandler;
import io.github.finalwave.server.sync.ProgressSyncHandler;

import java.io.IOException;
import java.net.Socket;

public final class ClientHandler implements Runnable {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final Socket socket;
    private final ServerContext context;
    private final RegisterHandler registerHandler;
    private final LoginService loginService;
    private LoginHandler loginHandler;
    private ProgressSyncHandler progressSyncHandler;
    private ResumeHandler resumeHandler;

    public ClientHandler(Socket socket, ServerContext context) {
        this.socket = socket;
        this.context = context;
        this.registerHandler = new RegisterHandler(context.database());
        this.loginService = new LoginService(context);
    }

    @Override
    public void run() {
        loginHandler = new LoginHandler(loginService, this);
        progressSyncHandler = new ProgressSyncHandler(context, this);
        resumeHandler = new ResumeHandler(context, this);
        String clientLabel = String.valueOf(socket.getRemoteSocketAddress());
        System.out.println("Client connected: " + clientLabel);
        try (socket) {
            JsonLineProtocol protocol = new JsonLineProtocol(MAPPER, socket.getInputStream(), socket.getOutputStream());
            while (!socket.isClosed()) {
                MessageEnvelope incoming = protocol.receive();
                if (incoming == null) {
                    break;
                }
                MessageEnvelope response = handle(incoming);
                if (response != null) {
                    protocol.send(response);
                }
            }
        } catch (IOException exception) {
            System.out.println("Client disconnected: " + clientLabel + " (" + exception.getMessage() + ")");
        } finally {
            context.sessionRegistry().unbind(this);
            System.out.println("Client handler finished: " + clientLabel);
        }
    }

    private MessageEnvelope handle(MessageEnvelope incoming) {
        String type = incoming.getType();
        if (MessageTypes.PING.equals(type)) {
            return new MessageEnvelope(MessageTypes.PONG, incoming.getRequestId(), incoming.getPayload());
        }
        if (MessageTypes.REGISTER.equals(type)) {
            return registerHandler.handle(incoming);
        }
        if (MessageTypes.LOGIN.equals(type)) {
            return loginHandler.handle(incoming);
        }
        if (MessageTypes.LOGOUT.equals(type)) {
            context.sessionRegistry().unbind(this);
            return new MessageEnvelope(MessageTypes.LOGOUT_OK, incoming.getRequestId(), null);
        }
        if (MessageTypes.RESUME.equals(type)) {
            return resumeHandler.handle(incoming);
        }
        if (isSyncType(type)) {
            return progressSyncHandler.handle(incoming);
        }
        return null;
    }

    private static boolean isSyncType(String type) {
        return MessageTypes.UPDATE_WALLET.equals(type)
                || MessageTypes.UPDATE_PLANT.equals(type)
                || MessageTypes.UPDATE_GREENHOUSE_POT.equals(type)
                || MessageTypes.UPDATE_BOOSTS.equals(type)
                || MessageTypes.UNLOCK_CONTENT.equals(type)
                || MessageTypes.UPDATE_QUEST_PROGRESS.equals(type)
                || MessageTypes.UPDATE_ADVENTURE.equals(type)
                || MessageTypes.UPDATE_MINIGAME_STAGES.equals(type)
                || MessageTypes.UPDATE_MATCH_SAVE.equals(type)
                || MessageTypes.CLEAR_MATCH_SAVE.equals(type)
                || MessageTypes.UPDATE_NEWS.equals(type)
                || MessageTypes.UPDATE_SETTINGS.equals(type)
                || MessageTypes.UPDATE_SCORE_GAME.equals(type);
    }
}
