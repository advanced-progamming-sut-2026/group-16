package io.github.finalwave.server.leaderboard;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.finalwave.model.leaderboard.LeaderboardEntry;
import io.github.finalwave.model.leaderboard.LeaderboardQueryService;
import io.github.finalwave.network.MessageEnvelope;
import io.github.finalwave.network.MessageTypes;
import io.github.finalwave.network.leaderboard.GetLeaderboardFailPayload;
import io.github.finalwave.network.leaderboard.GetLeaderboardOkPayload;
import io.github.finalwave.network.leaderboard.LeaderboardRow;
import io.github.finalwave.network.sync.SyncFailReason;
import io.github.finalwave.server.ClientHandler;
import io.github.finalwave.server.ServerContext;

import java.util.ArrayList;
import java.util.List;

public final class LeaderboardHandler {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final ServerContext context;
    private final ClientHandler handler;

    public LeaderboardHandler(ServerContext context, ClientHandler handler) {
        this.context = context;
        this.handler = handler;
    }

    public MessageEnvelope handle(MessageEnvelope incoming) {
        String username = context.sessionRegistry().usernameFor(handler).orElse(null);
        if (username == null) {
            return fail(incoming, SyncFailReason.AUTH_REQUIRED);
        }
        try {
            List<LeaderboardEntry> entries = LeaderboardQueryService.loadAll(context.database().delegate());
            GetLeaderboardOkPayload payload = new GetLeaderboardOkPayload();
            payload.setEntries(toRows(entries));
            return new MessageEnvelope(
                    MessageTypes.GET_LEADERBOARD_OK,
                    incoming.getRequestId(),
                    MAPPER.valueToTree(payload));
        } catch (Exception exception) {
            return fail(incoming, SyncFailReason.SERVER_ERROR);
        }
    }

    private static List<LeaderboardRow> toRows(List<LeaderboardEntry> entries) {
        List<LeaderboardRow> rows = new ArrayList<>();
        if (entries == null) {
            return rows;
        }
        for (LeaderboardEntry entry : entries) {
            if (entry == null) {
                continue;
            }
            LeaderboardRow row = new LeaderboardRow();
            row.setUsername(entry.username());
            row.setProgressLabel(entry.progressLabel());
            row.setProgressSortKey(entry.progressSortKey());
            row.setMinigameCount(entry.minigameCount());
            row.setDailyQuestCount(entry.dailyQuestCount());
            row.setNonDailyQuestCount(entry.nonDailyQuestCount());
            row.setMyPoint(entry.bestScore());
            rows.add(row);
        }
        return rows;
    }

    private static MessageEnvelope fail(MessageEnvelope incoming, String reason) {
        GetLeaderboardFailPayload payload = new GetLeaderboardFailPayload(reason);
        return new MessageEnvelope(
                MessageTypes.GET_LEADERBOARD_FAIL,
                incoming.getRequestId(),
                MAPPER.valueToTree(payload));
    }
}
