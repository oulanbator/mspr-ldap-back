package tor.learning.jwtsecurity.model.http;

public class BooleanResponse {
    private Boolean response;

    public BooleanResponse(Boolean response) {
        this.response = response;
    }

    public Boolean getResponse() {
        return response;
    }

    public void setResponse(Boolean response) {
        this.response = response;
    }
}
