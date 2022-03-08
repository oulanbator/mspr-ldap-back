package epsi.mspr.ldapback.model.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class User {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String username;

    // TODO : delete this field ? 
    private String password;
    // TODO : delete this field ? 
    @Column(unique = true)
    private String email;

    private String twoFactorSecret;
    private boolean twoFactorVerified;

    private String userAgent;
    private String ipAddress;
    
    private int attempts = 0;
    private boolean isBlocked = false;

    public User() {
        this.twoFactorVerified = false;
    }

    public User(String username, String password, String email, String twoFactorSecret, 
                String userAgent, String ipAddress) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.twoFactorSecret = twoFactorSecret;
        this.twoFactorVerified = false;
        this.userAgent = userAgent;
        this.ipAddress = ipAddress;
        this.isBlocked = false;
        this.attempts = 0;
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
    
    public boolean isBlocked(){
        return this.isBlocked;
    }
    
    public void block(){
        this.isBlocked = true;
    }
    
    public void unBlock(){
        this.isBlocked = false;
        this.cleanAttempts();
    }
    public void cleanAttempts(){
        this.attempts = 0;
    }
    
    public int getAttempts(){
        return this.attempts;
    }
    
    public void addAttempts(){
        this.attempts++;
        if (this.attempts >= 5){
            this.block();
        }
    }
    
}
