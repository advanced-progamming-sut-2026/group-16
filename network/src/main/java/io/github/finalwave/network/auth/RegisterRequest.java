package io.github.finalwave.network.auth;

public final class RegisterRequest {
    private String username;
    private String password;
    private String nickname;
    private String email;
    private String gender;
    private Integer securityQuestionNumber;
    private String securityAnswer;

    public RegisterRequest() {
    }

    public RegisterRequest(String username, String password, String nickname, String email, String gender,
                           Integer securityQuestionNumber, String securityAnswer) {
        this.username = username;
        this.password = password;
        this.nickname = nickname;
        this.email = email;
        this.gender = gender;
        this.securityQuestionNumber = securityQuestionNumber;
        this.securityAnswer = securityAnswer;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public Integer getSecurityQuestionNumber() {
        return securityQuestionNumber;
    }

    public void setSecurityQuestionNumber(Integer securityQuestionNumber) {
        this.securityQuestionNumber = securityQuestionNumber;
    }

    public String getSecurityAnswer() {
        return securityAnswer;
    }

    public void setSecurityAnswer(String securityAnswer) {
        this.securityAnswer = securityAnswer;
    }
}
