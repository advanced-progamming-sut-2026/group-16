package io.github.finalwave.network.auth;

public final class ResetPasswordRequest {
    private String username;
    private String email;
    private String securityAnswer;
    private String newPassword;

    public ResetPasswordRequest() {
    }

    public ResetPasswordRequest(String username, String email, String securityAnswer, String newPassword) {
        this.username = username;
        this.email = email;
        this.securityAnswer = securityAnswer;
        this.newPassword = newPassword;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSecurityAnswer() {
        return securityAnswer;
    }

    public void setSecurityAnswer(String securityAnswer) {
        this.securityAnswer = securityAnswer;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}
