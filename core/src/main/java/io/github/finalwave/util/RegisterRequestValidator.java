package io.github.finalwave.util;

import io.github.finalwave.model.user.Gender;
import io.github.finalwave.model.user.SecurityQuestion;
import io.github.finalwave.network.auth.RegisterFailReason;
import io.github.finalwave.network.auth.RegisterRequest;

public final class RegisterRequestValidator {
    private RegisterRequestValidator() {
    }

    public static String validate(RegisterRequest request) {
        if (request == null) {
            return RegisterFailReason.INVALID_INPUT;
        }
        String username = trimToNull(request.getUsername());
        String password = request.getPassword();
        String nickname = trimToNull(request.getNickname());
        String email = trimToNull(request.getEmail());
        String genderText = trimToNull(request.getGender());

        if (username == null || password == null || nickname == null || email == null || genderText == null) {
            return RegisterFailReason.INVALID_INPUT;
        }
        if (!RegistrationValidator.isValidUsername(username)) {
            return RegisterFailReason.INVALID_USERNAME;
        }
        if (!RegistrationValidator.isStrongPassword(password)) {
            return RegisterFailReason.WEAK_PASSWORD;
        }
        if (!RegistrationValidator.isValidNickname(nickname)) {
            return RegisterFailReason.INVALID_NICKNAME;
        }
        if (!RegistrationValidator.isValidEmail(email)) {
            return RegisterFailReason.INVALID_EMAIL;
        }
        if (Gender.fromString(genderText) == null) {
            return RegisterFailReason.INVALID_GENDER;
        }
        Integer questionNumber = request.getSecurityQuestionNumber();
        String securityAnswer = request.getSecurityAnswer();
        if (questionNumber != null) {
            if (SecurityQuestion.fromNumber(questionNumber) == null) {
                return RegisterFailReason.INVALID_SECURITY_QUESTION;
            }
            if (securityAnswer == null || securityAnswer.isBlank()) {
                return RegisterFailReason.MISSING_SECURITY_ANSWER;
            }
        }
        return null;
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
