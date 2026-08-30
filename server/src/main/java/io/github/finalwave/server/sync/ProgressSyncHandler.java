package io.github.finalwave.server.sync;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.finalwave.network.MessageEnvelope;
import io.github.finalwave.network.MessageTypes;
import io.github.finalwave.network.sync.SyncFailPayload;
import io.github.finalwave.network.sync.SyncFailReason;
import io.github.finalwave.network.sync.UpdateAdventurePayload;
import io.github.finalwave.network.sync.UpdateBoostsPayload;
import io.github.finalwave.network.sync.UpdateGreenhousePotPayload;
import io.github.finalwave.network.sync.UpdateMatchSavePayload;
import io.github.finalwave.network.sync.UpdateMinigameStagesPayload;
import io.github.finalwave.network.sync.UpdateNewsPayload;
import io.github.finalwave.network.sync.UpdatePlantPayload;
import io.github.finalwave.network.sync.UpdateQuestProgressPayload;
import io.github.finalwave.network.sync.UpdateScoreGamePayload;
import io.github.finalwave.network.sync.UpdateSettingsPayload;
import io.github.finalwave.network.sync.UpdateWalletPayload;
import io.github.finalwave.network.sync.UnlockContentPayload;
import io.github.finalwave.server.ClientHandler;
import io.github.finalwave.server.ServerContext;

public final class ProgressSyncHandler {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final ServerContext context;
    private final ClientHandler handler;
    private final ServerProgressWriter writer;

    public ProgressSyncHandler(ServerContext context, ClientHandler handler) {
        this.context = context;
        this.handler = handler;
        this.writer = new ServerProgressWriter(context.database());
    }

    public MessageEnvelope handle(MessageEnvelope incoming) {
        String username = context.sessionRegistry().usernameFor(handler).orElse(null);
        if (username == null) {
            return fail(incoming, SyncFailReason.AUTH_REQUIRED);
        }
        try {
            return switch (incoming.getType()) {
                case MessageTypes.UPDATE_WALLET -> ok(
                        incoming,
                        MessageTypes.UPDATE_WALLET_OK,
                        writer.applyWallet(username, tree(incoming, UpdateWalletPayload.class))
                );
                case MessageTypes.UPDATE_PLANT -> ok(
                        incoming,
                        MessageTypes.UPDATE_PLANT_OK,
                        writer.applyPlant(username, tree(incoming, UpdatePlantPayload.class))
                );
                case MessageTypes.UPDATE_GREENHOUSE_POT -> ok(
                        incoming,
                        MessageTypes.UPDATE_GREENHOUSE_POT_OK,
                        writer.applyGreenhousePot(username, tree(incoming, UpdateGreenhousePotPayload.class))
                );
                case MessageTypes.UPDATE_BOOSTS -> ok(
                        incoming,
                        MessageTypes.UPDATE_BOOSTS_OK,
                        writer.applyBoosts(username, tree(incoming, UpdateBoostsPayload.class))
                );
                case MessageTypes.UNLOCK_CONTENT -> ok(
                        incoming,
                        MessageTypes.UNLOCK_CONTENT_OK,
                        writer.applyUnlock(username, tree(incoming, UnlockContentPayload.class))
                );
                case MessageTypes.UPDATE_QUEST_PROGRESS -> ok(
                        incoming,
                        MessageTypes.UPDATE_QUEST_PROGRESS_OK,
                        writer.applyQuestProgress(username, tree(incoming, UpdateQuestProgressPayload.class))
                );
                case MessageTypes.UPDATE_ADVENTURE -> ok(
                        incoming,
                        MessageTypes.UPDATE_ADVENTURE_OK,
                        writer.applyAdventure(username, tree(incoming, UpdateAdventurePayload.class))
                );
                case MessageTypes.UPDATE_MINIGAME_STAGES -> ok(
                        incoming,
                        MessageTypes.UPDATE_MINIGAME_STAGES_OK,
                        writer.applyMinigameStages(username, tree(incoming, UpdateMinigameStagesPayload.class))
                );
                case MessageTypes.UPDATE_MATCH_SAVE -> ok(
                        incoming,
                        MessageTypes.UPDATE_MATCH_SAVE_OK,
                        writer.applyMatchSave(username, tree(incoming, UpdateMatchSavePayload.class))
                );
                case MessageTypes.CLEAR_MATCH_SAVE -> {
                    writer.clearMatchSave(username);
                    yield ok(incoming, MessageTypes.CLEAR_MATCH_SAVE_OK, null);
                }
                case MessageTypes.UPDATE_NEWS -> ok(
                        incoming,
                        MessageTypes.UPDATE_NEWS_OK,
                        writer.applyNews(username, tree(incoming, UpdateNewsPayload.class))
                );
                case MessageTypes.UPDATE_SETTINGS -> ok(
                        incoming,
                        MessageTypes.UPDATE_SETTINGS_OK,
                        writer.applySettings(username, tree(incoming, UpdateSettingsPayload.class))
                );
                case MessageTypes.UPDATE_SCORE_GAME -> ok(
                        incoming,
                        MessageTypes.UPDATE_SCORE_GAME_OK,
                        writer.applyScoreGame(username, tree(incoming, UpdateScoreGamePayload.class))
                );
                default -> fail(incoming, SyncFailReason.VALIDATION);
            };
        } catch (SyncValidationException exception) {
            return fail(incoming, SyncFailReason.VALIDATION);
        } catch (RuntimeException exception) {
            return fail(incoming, SyncFailReason.SERVER_ERROR);
        }
    }

    private <T> T tree(MessageEnvelope incoming, Class<T> type) {
        try {
            return MAPPER.treeToValue(incoming.getPayload(), type);
        } catch (Exception exception) {
            throw new SyncValidationException("invalid payload");
        }
    }

    private MessageEnvelope ok(MessageEnvelope incoming, String type, Object payload) {
        return new MessageEnvelope(type, incoming.getRequestId(), MAPPER.valueToTree(payload));
    }

    private MessageEnvelope fail(MessageEnvelope incoming, String reason) {
        return new MessageEnvelope(
                failType(incoming.getType()),
                incoming.getRequestId(),
                MAPPER.valueToTree(new SyncFailPayload(reason))
        );
    }

    private static String failType(String requestType) {
        return switch (requestType) {
            case MessageTypes.UPDATE_WALLET -> MessageTypes.UPDATE_WALLET_FAIL;
            case MessageTypes.UPDATE_PLANT -> MessageTypes.UPDATE_PLANT_FAIL;
            case MessageTypes.UPDATE_GREENHOUSE_POT -> MessageTypes.UPDATE_GREENHOUSE_POT_FAIL;
            case MessageTypes.UPDATE_BOOSTS -> MessageTypes.UPDATE_BOOSTS_FAIL;
            case MessageTypes.UNLOCK_CONTENT -> MessageTypes.UNLOCK_CONTENT_FAIL;
            case MessageTypes.UPDATE_QUEST_PROGRESS -> MessageTypes.UPDATE_QUEST_PROGRESS_FAIL;
            case MessageTypes.UPDATE_ADVENTURE -> MessageTypes.UPDATE_ADVENTURE_FAIL;
            case MessageTypes.UPDATE_MINIGAME_STAGES -> MessageTypes.UPDATE_MINIGAME_STAGES_FAIL;
            case MessageTypes.UPDATE_MATCH_SAVE -> MessageTypes.UPDATE_MATCH_SAVE_FAIL;
            case MessageTypes.CLEAR_MATCH_SAVE -> MessageTypes.CLEAR_MATCH_SAVE_FAIL;
            case MessageTypes.UPDATE_NEWS -> MessageTypes.UPDATE_NEWS_FAIL;
            case MessageTypes.UPDATE_SETTINGS -> MessageTypes.UPDATE_SETTINGS_FAIL;
            case MessageTypes.UPDATE_SCORE_GAME -> MessageTypes.UPDATE_SCORE_GAME_FAIL;
            default -> MessageTypes.UPDATE_WALLET_FAIL;
        };
    }
}
