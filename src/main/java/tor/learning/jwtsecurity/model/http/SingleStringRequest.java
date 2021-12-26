package tor.learning.jwtsecurity.model.http;

public class SingleStringRequest {
    private String data;

    public SingleStringRequest() {
    }

    public SingleStringRequest(String data) {
        this.data = data;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}
