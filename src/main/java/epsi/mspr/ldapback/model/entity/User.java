package epsi.mspr.ldapback.model.entity;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class User {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String username;

    @Column(unique = true)  // TODO : delete this field ?
    private String email;

    // 2FA
    private String twoFactorSecret;
    private String secretSalt;
    private boolean twoFactorVerified;

    // navigateur et IP
    private String userAgent = "";
    private String ipList = "";

    // brute force
    private boolean blocked;
    private int badCredentialsAttempts;


    public User() {
        this.twoFactorVerified = false;
    }

    public User(Long id, String username, String email, String twoFactorSecret, String secretSalt, boolean twoFactorVerified, String userAgent, String ipList, boolean blocked, int badCredentialsAttempts) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.twoFactorSecret = twoFactorSecret;
        this.secretSalt = secretSalt;
        this.twoFactorVerified = twoFactorVerified;
        this.userAgent = userAgent;
        this.ipList = ipList;
        this.blocked = blocked;
        this.badCredentialsAttempts = badCredentialsAttempts;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getTwoFactorSecret() {
        return twoFactorSecret;
    }

    public void setTwoFactorSecret(String twoFactorSecret) {
        this.twoFactorSecret = twoFactorSecret;
    }

    public boolean isTwoFactorVerified() {
        return twoFactorVerified;
    }

    public void setTwoFactorVerified(boolean twoFactorVerified) {
        this.twoFactorVerified = twoFactorVerified;
    }

    public String getAgentList() {
        return userAgent;
    }

    public void setAgentList(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getIpList() {
        return ipList;
    }

    public void setIpList(String ipList) {
        this.ipList = ipList;
    }

    public String getSecretSalt() {
        return secretSalt;
    }

    public void setSecretSalt(String secretSalt) {
        this.secretSalt = secretSalt;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public boolean isBlocked() {
        return blocked;
    }

    public void setBlocked(boolean blocked) {
        this.blocked = blocked;
    }

    public int getBadCredentialsAttempts() {
        return badCredentialsAttempts;
    }

    public void setBadCredentialsAttempts(int badCredentialsAttempts) {
        this.badCredentialsAttempts = badCredentialsAttempts;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", twoFactorSecret='" + twoFactorSecret + '\'' +
                ", twoFactorVerified=" + twoFactorVerified +
                ", userAgent='" + userAgent + '\'' +
                ", ipList='" + ipList + '\'' +
                '}';
    }
}
