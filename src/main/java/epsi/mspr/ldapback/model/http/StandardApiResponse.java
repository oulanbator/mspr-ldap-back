package epsi.mspr.ldapback.model.http;

import java.util.List;

public class StandardApiResponse {
    private String status;
    private int statusCode;
    private String message;
    private List<?> data;

    public StandardApiResponse() {
    }

    public StandardApiResponse(String status) {
        this.status = status;
    }

    public StandardApiResponse(String status, String message) {
        this.status = status;
        this.message = message;
    }

    public StandardApiResponse(String status, int statusCode, String message, List<?> data) {
        this.status = status;
        this.statusCode = statusCode;
        this.message = message;
        this.data = data;
    }

    public StandardApiResponse(String status, String message, List<?> data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<?> getData() {
        return data;
    }

    public void setData(List<?> data) {
        this.data = data;
    }
}
