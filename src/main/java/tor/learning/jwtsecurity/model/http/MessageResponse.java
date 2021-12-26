package tor.learning.jwtsecurity.model.http;

public class MessageResponse {
    private String message;
    private Boolean success;

    public MessageResponse(String message, Boolean success) {
        this.message = message;
        this.success = success;
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
