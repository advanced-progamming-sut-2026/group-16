package io.github.finalwave.network.auth;

public final class SecurityQuestionLookupOkPayload {
    private int securityQuestionNumber;
    private String questionText;

    public SecurityQuestionLookupOkPayload() {
    }

    public SecurityQuestionLookupOkPayload(int securityQuestionNumber, String questionText) {
        this.securityQuestionNumber = securityQuestionNumber;
        this.questionText = questionText;
    }

    public int getSecurityQuestionNumber() {
        return securityQuestionNumber;
    }

    public void setSecurityQuestionNumber(int securityQuestionNumber) {
        this.securityQuestionNumber = securityQuestionNumber;
    }

    public String getQuestionText() {
        return questionText;
    }

    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }
}
