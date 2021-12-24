package tor.learning.jwtsecurity.model;

public class TextMessageResponse {
    private String message;

    public TextMessageResponse() {
    }

    public TextMessageResponse(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
