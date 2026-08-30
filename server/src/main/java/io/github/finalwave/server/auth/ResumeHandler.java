package io.github.finalwave.server.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.finalwave.network.MessageEnvelope;
import io.github.finalwave.network.MessageTypes;
import io.github.finalwave.network.sync.ResumePayload;
import io.github.finalwave.network.sync.SyncFailPayload;
import io.github.finalwave.network.sync.SyncFailReason;
import io.github.finalwave.server.ClientHandler;
import io.github.finalwave.server.ServerContext;

public final class ResumeHandler {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final ResumeService resumeService;
    private final ClientHandler handler;

    public ResumeHandler(ServerContext context, ClientHandler handler) {
        this.resumeService = new ResumeService(context);
        this.handler = handler;
    }

    public MessageEnvelope handle(MessageEnvelope incoming) {
        try {
            ResumePayload request = MAPPER.treeToValue(incoming.getPayload(), ResumePayload.class);
            ResumeService.ResumeResult result = resumeService.resume(request, handler);
            if (result.isSuccess()) {
                return new MessageEnvelope(
                        MessageTypes.RESUME_OK,
                        incoming.getRequestId(),
                        MAPPER.valueToTree(result.successPayload())
                );
            }
            return new MessageEnvelope(
                    MessageTypes.RESUME_FAIL,
                    incoming.getRequestId(),
                    MAPPER.valueToTree(new SyncFailPayload(result.failureReason()))
            );
        } catch (Exception exception) {
            return new MessageEnvelope(
                    MessageTypes.RESUME_FAIL,
                    incoming.getRequestId(),
                    MAPPER.valueToTree(new SyncFailPayload(SyncFailReason.SERVER_ERROR))
            );
        }
    }
}
