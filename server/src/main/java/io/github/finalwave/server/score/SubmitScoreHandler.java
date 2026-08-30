package io.github.finalwave.server.score;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.finalwave.model.scoregame.ScoreGameSubmission;
import io.github.finalwave.model.user.User;
import io.github.finalwave.network.MessageEnvelope;
import io.github.finalwave.network.MessageTypes;
import io.github.finalwave.network.score.SubmitScoreFailPayload;
import io.github.finalwave.network.score.SubmitScoreOkPayload;
import io.github.finalwave.network.score.SubmitScoreRequest;
import io.github.finalwave.network.sync.SyncFailReason;
import io.github.finalwave.server.ClientHandler;
import io.github.finalwave.server.ServerContext;

public final class SubmitScoreHandler {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final ServerContext context;
    private final ClientHandler handler;

    public SubmitScoreHandler(ServerContext context, ClientHandler handler) {
        this.context = context;
        this.handler = handler;
    }

    public MessageEnvelope handle(MessageEnvelope incoming) {
        String username = context.sessionRegistry().usernameFor(handler).orElse(null);
        if (username == null) {
            return fail(incoming, SyncFailReason.AUTH_REQUIRED);
        }
        try {
            SubmitScoreRequest request = MAPPER.treeToValue(incoming.getPayload(), SubmitScoreRequest.class);
            if (request == null || request.getScore() < 0) {
                return fail(incoming, SyncFailReason.VALIDATION);
            }
            User user = context.database().getUser(username);
            if (user == null) {
                return fail(incoming, SyncFailReason.SERVER_ERROR);
            }
            ScoreGameSubmission.Result result = ScoreGameSubmission.apply(user, request.getScore());
            context.database().saveBestMeowPoint(user);
            SubmitScoreOkPayload payload = new SubmitScoreOkPayload();
            payload.setBestMeowPoint(result.bestMeowPoint());
            payload.setHasPlayed(result.hasPlayed());
            payload.setNewBest(result.newBest());
            return new MessageEnvelope(
                    MessageTypes.SUBMIT_SCORE_OK,
                    incoming.getRequestId(),
                    MAPPER.valueToTree(payload));
        } catch (Exception exception) {
            return fail(incoming, SyncFailReason.SERVER_ERROR);
        }
    }

    private static MessageEnvelope fail(MessageEnvelope incoming, String reason) {
        SubmitScoreFailPayload payload = new SubmitScoreFailPayload(reason);
        return new MessageEnvelope(
                MessageTypes.SUBMIT_SCORE_FAIL,
                incoming.getRequestId(),
                MAPPER.valueToTree(payload));
    }
}
