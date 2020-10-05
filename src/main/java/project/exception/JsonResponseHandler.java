package project.exception;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class JsonResponseHandler {

    @ExceptionHandler(JsonException.class)
    public ResponseDto jsonException(JsonException e) {
        return ResponseDto.of(e.getMessageCode(), e.getMessage());
    }

}