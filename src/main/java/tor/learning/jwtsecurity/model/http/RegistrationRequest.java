package tor.learning.jwtsecurity.model.http;

public class RegistrationRequest {
    private String email;
    private String username;
    private String password;
    private Boolean twoFactorEnabled;

    public RegistrationRequest() {
    }

    public RegistrationRequest(String email, String username, String password, Boolean twoFactorEnabled) {
        this.email = email;
        this.username = username;
        this.password = password;
        this.twoFactorEnabled = twoFactorEnabled;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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

    public Boolean getTwoFactorEnabled() {
        return twoFactorEnabled;
    }

    public void setTwoFactorEnabled(Boolean twoFactorEnabled) {
        this.twoFactorEnabled = twoFactorEnabled;
    }
}
