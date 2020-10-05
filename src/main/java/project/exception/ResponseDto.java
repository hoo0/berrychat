package project.exception;

import java.io.Serializable;

public class ResponseDto implements Serializable {

    private String result;
    private String messageCode;
    private String message;

    public ResponseDto(String messageCode, String message) {
        this.result = "FAIL";
        this.messageCode = messageCode;
        this.message = message;
    }

    public String getResult() {
        return result;
    }

    public String getMessageCode() {
        return messageCode;
    }

    public String getMessage() {
        return message;
    }

    public static ResponseDto of(String messageCode, String message){
        return new ResponseDto(messageCode, message);
    }

}