package project.zenith.model;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Login implements Serializable {
    private String result;
    private String message;
    private String uid;
    
    @Override
    public String toString() {
        return "result="+result+","
              +"message="+message+","
              +"uid="+uid;
    }
}