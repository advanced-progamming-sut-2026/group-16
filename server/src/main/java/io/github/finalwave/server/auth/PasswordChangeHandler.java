package io.github.finalwave.server.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.finalwave.network.MessageEnvelope;
import io.github.finalwave.network.MessageTypes;
import io.github.finalwave.network.auth.ChangePasswordRequest;
import io.github.finalwave.network.auth.PasswordChangeFailPayload;
import io.github.finalwave.network.auth.PasswordChangeFailReason;
import io.github.finalwave.network.auth.PasswordChangeOkPayload;
import io.github.finalwave.network.auth.ResetPasswordRequest;
import io.github.finalwave.network.auth.SecurityQuestionLookupOkPayload;
import io.github.finalwave.network.auth.SecurityQuestionLookupRequest;
import io.github.finalwave.server.ClientHandler;

public final class PasswordChangeHandler {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final PasswordChangeService service;
    private final ClientHandler handler;

    public PasswordChangeHandler(PasswordChangeService service, ClientHandler handler) {
        this.service = service;
        this.handler = handler;
    }

    public MessageEnvelope handleReset(MessageEnvelope incoming) {
        try {
            ResetPasswordRequest request = MAPPER.treeToValue(incoming.getPayload(), ResetPasswordRequest.class);
            PasswordChangeService.Result result = service.reset(request);
            return passwordEnvelope(result, incoming.getRequestId(),
                    MessageTypes.RESET_PASSWORD_OK, MessageTypes.RESET_PASSWORD_FAIL);
        } catch (Exception exception) {
            return fail(MessageTypes.RESET_PASSWORD_FAIL, incoming.getRequestId(),
                    PasswordChangeFailReason.SERVER_ERROR);
        }
    }

    public MessageEnvelope handleChange(MessageEnvelope incoming) {
        try {
            ChangePasswordRequest request = MAPPER.treeToValue(incoming.getPayload(), ChangePasswordRequest.class);
            PasswordChangeService.Result result = service.change(request, handler);
            return passwordEnvelope(result, incoming.getRequestId(),
                    MessageTypes.CHANGE_PASSWORD_OK, MessageTypes.CHANGE_PASSWORD_FAIL);
        } catch (Exception exception) {
            return fail(MessageTypes.CHANGE_PASSWORD_FAIL, incoming.getRequestId(),
                    PasswordChangeFailReason.SERVER_ERROR);
        }
    }

    public MessageEnvelope handleLookup(MessageEnvelope incoming) {
        try {
            SecurityQuestionLookupRequest request =
                    MAPPER.treeToValue(incoming.getPayload(), SecurityQuestionLookupRequest.class);
            PasswordChangeService.Result result = service.lookupSecurityQuestion(
                    request == null ? null : request.getUsername(),
                    request == null ? null : request.getEmail());
            if (result.isSuccess() && result.lookupSuccessPayload() != null) {
                return new MessageEnvelope(
                        MessageTypes.SECURITY_QUESTION_LOOKUP_OK,
                        incoming.getRequestId(),
                        MAPPER.valueToTree(result.lookupSuccessPayload()));
            }
            return fail(MessageTypes.SECURITY_QUESTION_LOOKUP_FAIL, incoming.getRequestId(), result.failureReason());
        } catch (Exception exception) {
            return fail(MessageTypes.SECURITY_QUESTION_LOOKUP_FAIL, incoming.getRequestId(),
                    PasswordChangeFailReason.SERVER_ERROR);
        }
    }

    private MessageEnvelope passwordEnvelope(PasswordChangeService.Result result,
                                             String requestId,
                                             String okType,
                                             String failType) {
        if (result.isSuccess() && result.successPayload() != null) {
            PasswordChangeOkPayload payload = result.successPayload();
            return new MessageEnvelope(okType, requestId, MAPPER.valueToTree(payload));
        }
        return fail(failType, requestId, result.failureReason());
    }

    private MessageEnvelope fail(String type, String requestId, String reason) {
        return new MessageEnvelope(
                type,
                requestId,
                MAPPER.valueToTree(new PasswordChangeFailPayload(reason)));
    }
}
