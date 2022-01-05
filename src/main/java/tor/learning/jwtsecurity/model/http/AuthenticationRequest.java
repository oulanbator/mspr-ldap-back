package tor.learning.jwtsecurity.model.http;

public class AuthenticationRequest {
    private String username;
    private String password;
    private String twoFactorsTotp;

    public AuthenticationRequest() {
    }

    public AuthenticationRequest(String username, String password, String twoFactorsTotp) {
        this.username = username;
        this.password = password;
        this.twoFactorsTotp = twoFactorsTotp;
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

    public String getTwoFactorsTotp() {
        return twoFactorsTotp;
    }

    public void setTwoFactorsTotp(String twoFactorsTotp) {
        this.twoFactorsTotp = twoFactorsTotp;
    }
}
