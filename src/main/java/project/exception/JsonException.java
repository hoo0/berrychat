package project.exception;

public class JsonException extends RuntimeException {
    private String messageCode;
    
    public JsonException(String messageCode, String message) { 
        super(message);
        this.messageCode = messageCode;
    }
    
    public String getMessageCode() {
        return messageCode;
    }
}
