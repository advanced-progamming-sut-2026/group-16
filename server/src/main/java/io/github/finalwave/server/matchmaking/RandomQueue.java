package io.github.finalwave.server.matchmaking;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.finalwave.network.MessageEnvelope;
import io.github.finalwave.network.MessageTypes;
import io.github.finalwave.network.match.ChallengeFailReason;
import io.github.finalwave.network.match.JoinRandomQueueFailPayload;
import io.github.finalwave.network.match.JoinRandomQueueOkPayload;
import io.github.finalwave.server.ClientHandler;
import io.github.finalwave.server.ServerContext;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public final class RandomQueue {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final ServerContext context;
    private final ConcurrentLinkedQueue<ClientHandler> queue = new ConcurrentLinkedQueue<>();
    private final Map<ClientHandler, String> handlerUsernames = new ConcurrentHashMap<>();

    public RandomQueue(ServerContext context) {
        this.context = context;
    }

    public MessageEnvelope join(ClientHandler handler, MessageEnvelope incoming) {
        Optional<String> username = context.sessionRegistry().usernameFor(handler);
        if (username.isEmpty()) {
            return fail(incoming, ChallengeFailReason.NOT_LOGGED_IN);
        }
        if (context.matchRegistry().isBusy(handler)) {
            return fail(incoming, ChallengeFailReason.BUSY);
        }
        synchronized (this) {
            ClientHandler waiting = queue.poll();
            if (waiting == null) {
                if (!queue.contains(handler)) {
                    queue.offer(handler);
                    handlerUsernames.put(handler, username.get());
                }
                return ok(incoming, true);
            }
            if (waiting == handler) {
                return ok(incoming, true);
            }
            Optional<String> waitingUsername = context.sessionRegistry().usernameFor(waiting);
            handlerUsernames.remove(waiting);
            if (waitingUsername.isEmpty()) {
                if (!queue.contains(handler)) {
                    queue.offer(handler);
                    handlerUsernames.put(handler, username.get());
                }
                return ok(incoming, true);
            }
            context.matchRegistry().createRandomMatch(
                    waiting,
                    handler,
                    waitingUsername.get(),
                    username.get());
            return ok(incoming, false);
        }
    }

    public MessageEnvelope leave(ClientHandler handler, MessageEnvelope incoming) {
        remove(handler);
        return new MessageEnvelope(
                MessageTypes.LEAVE_QUEUE_OK,
                incoming.getRequestId(),
                MAPPER.valueToTree(Map.of()));
    }

    public void remove(ClientHandler handler) {
        queue.remove(handler);
        handlerUsernames.remove(handler);
    }

    private MessageEnvelope ok(MessageEnvelope incoming, boolean waiting) {
        JoinRandomQueueOkPayload payload = new JoinRandomQueueOkPayload(waiting);
        return new MessageEnvelope(
                MessageTypes.JOIN_RANDOM_QUEUE_OK,
                incoming.getRequestId(),
                MAPPER.valueToTree(payload));
    }

    private MessageEnvelope fail(MessageEnvelope incoming, ChallengeFailReason reason) {
        JoinRandomQueueFailPayload payload = new JoinRandomQueueFailPayload(reason);
        return new MessageEnvelope(
                MessageTypes.JOIN_RANDOM_QUEUE_FAIL,
                incoming.getRequestId(),
                MAPPER.valueToTree(payload));
    }
}
