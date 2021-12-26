package tor.learning.jwtsecurity.model.http;

public class AuthenticationResponse {
    private String data;
    private Boolean authSuccess;

    public AuthenticationResponse(Boolean authSuccess, String data) {
        this.authSuccess = authSuccess;
        this.data = data;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public Boolean getAuthSuccess() {
        return authSuccess;
    }

    public void setAuthSuccess(Boolean authSuccess) {
        this.authSuccess = authSuccess;
    }
}
