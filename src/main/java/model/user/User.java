package model.user;

import util.HashUtil;

public class User {
    private long id;
    private String username;
    private String passwordHash;
    private String nickname;
    private String email;
    private Gender gender;
    private int securityQuestionId;
    private String securityAnswerHash;

    public User() {

    }

    public User(String username, String passwordHash, String nickname, String email, Gender gender) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.nickname = nickname;
        this.email = email;
        this.gender = gender;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public boolean authenticate(String password) {
        return passwordHash != null && passwordHash.equals(HashUtil.hashSHA256(password));
    }

    public boolean validateSecurityAnswer(String answer) {
        return securityAnswerHash != null && securityAnswerHash.equals(HashUtil.hashSHA256(answer.trim()));
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

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public int getSecurityQuestionId() {
        return securityQuestionId;
    }

    public void setSecurityQuestionId(int securityQuestionId) {
        this.securityQuestionId = securityQuestionId;
    }

    public String getSecurityAnswerHash() {
        return securityAnswerHash;
    }

    public void setSecurityAnswerHash(String securityAnswerHash) {
        this.securityAnswerHash = securityAnswerHash;
    }

    @Override
    public String toString() {
        return "User{" + "id=" + id + ", username='" + username + '\'' + ", passwordHash='" + passwordHash + '\'' + ", nickname='" + nickname + '\'' + ", email='" + email + '\'' + ", gender=" + gender + '}';
    }
}
