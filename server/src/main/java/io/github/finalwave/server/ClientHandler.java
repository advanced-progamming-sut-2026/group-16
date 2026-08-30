package io.github.finalwave.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.NullNode;
import io.github.finalwave.network.JsonLineProtocol;
import io.github.finalwave.network.MessageEnvelope;
import io.github.finalwave.network.MessageTypes;
import io.github.finalwave.server.auth.LoginHandler;
import io.github.finalwave.server.auth.LoginService;
import io.github.finalwave.server.auth.RegisterHandler;
import io.github.finalwave.server.auth.ResumeHandler;
import io.github.finalwave.server.leaderboard.LeaderboardHandler;
import io.github.finalwave.server.matchmaking.ChallengeHandler;
import io.github.finalwave.server.matchmaking.MatchDirectoryHandler;
import io.github.finalwave.server.matchmaking.MatchRelayHandler;
import io.github.finalwave.server.presence.UserStatusHandler;
import io.github.finalwave.server.score.SubmitScoreHandler;
import io.github.finalwave.server.sync.ProgressSyncHandler;

import java.io.IOException;
import java.net.Socket;

public final class ClientHandler implements Runnable {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final Socket socket;
    private final ServerContext context;
    private final LoginService loginService;
    private RegisterHandler registerHandler;
    private LoginHandler loginHandler;
    private ProgressSyncHandler progressSyncHandler;
    private ResumeHandler resumeHandler;
    private LeaderboardHandler leaderboardHandler;
    private SubmitScoreHandler submitScoreHandler;
    private UserStatusHandler userStatusHandler;
    private ChallengeHandler challengeHandler;
    private MatchDirectoryHandler matchDirectoryHandler;
    private MatchRelayHandler matchRelayHandler;
    private JsonLineProtocol protocol;

    public ClientHandler(Socket socket, ServerContext context) {
        this.socket = socket;
        this.context = context;
        this.loginService = new LoginService(context);
    }

    @Override
    public void run() {
        registerHandler = new RegisterHandler(context, this);
        loginHandler = new LoginHandler(loginService, this);
        progressSyncHandler = new ProgressSyncHandler(context, this);
        resumeHandler = new ResumeHandler(context, this);
        leaderboardHandler = new LeaderboardHandler(context, this);
        submitScoreHandler = new SubmitScoreHandler(context, this);
        userStatusHandler = new UserStatusHandler(context, this);
        challengeHandler = new ChallengeHandler(context, this);
        matchDirectoryHandler = new MatchDirectoryHandler(context, this);
        matchRelayHandler = new MatchRelayHandler(context, this);
        String clientLabel = String.valueOf(socket.getRemoteSocketAddress());
        System.out.println("Client connected: " + clientLabel);
        try (socket) {
            protocol = new JsonLineProtocol(MAPPER, socket.getInputStream(), socket.getOutputStream());
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
            context.randomQueue().remove(this);
            context.matchRegistry().onDisconnect(this);
            context.sessionRegistry().unbind(this);
            System.out.println("Client handler finished: " + clientLabel);
        }
    }

    public synchronized void push(MessageEnvelope message) {
        JsonLineProtocol activeProtocol = protocol;
        if (activeProtocol == null || message == null) {
            return;
        }
        try {
            activeProtocol.send(message);
        } catch (IOException exception) {
            System.out.println("Push failed: " + exception.getMessage());
        }
    }

    public synchronized void push(String type, Object payload) {
        push(new MessageEnvelope(type, null, payload == null ? NullNode.getInstance() : MAPPER.valueToTree(payload)));
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
            context.randomQueue().remove(this);
            context.matchRegistry().onDisconnect(this);
            context.sessionRegistry().unbind(this);
            return new MessageEnvelope(MessageTypes.LOGOUT_OK, incoming.getRequestId(), null);
        }
        if (MessageTypes.RESUME.equals(type)) {
            return resumeHandler.handle(incoming);
        }
        if (MessageTypes.GET_LEADERBOARD.equals(type)) {
            return leaderboardHandler.handle(incoming);
        }
        if (MessageTypes.SUBMIT_SCORE.equals(type)) {
            return submitScoreHandler.handle(incoming);
        }
        if (MessageTypes.CHECK_USER_STATUS.equals(type)) {
            return userStatusHandler.handle(incoming);
        }
        if (MessageTypes.LIST_MATCH_USERS.equals(type)) {
            return matchDirectoryHandler.listUsers(incoming);
        }
        if (MessageTypes.MATCHMAKING_RESET.equals(type)) {
            return matchDirectoryHandler.resetMatchmaking(incoming);
        }
        if (MessageTypes.CHALLENGE_REQUEST.equals(type)) {
            return challengeHandler.handleRequest(incoming);
        }
        if (MessageTypes.CHALLENGE_RESPONSE.equals(type)) {
            return challengeHandler.handleResponse(incoming);
        }
        if (MessageTypes.JOIN_RANDOM_QUEUE.equals(type)) {
            return context.randomQueue().join(this, incoming);
        }
        if (MessageTypes.LEAVE_QUEUE.equals(type)) {
            return context.randomQueue().leave(this, incoming);
        }
        if (MessageTypes.MATCH_INPUT.equals(type)) {
            return matchRelayHandler.handleInput(incoming);
        }
        if (MessageTypes.MATCH_STATE.equals(type)) {
            return matchRelayHandler.handleState(incoming);
        }
        if (MessageTypes.MATCH_END.equals(type)) {
            return matchRelayHandler.handleEnd(incoming);
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
