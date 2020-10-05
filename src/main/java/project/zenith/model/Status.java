package project.zenith.model;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Status implements Serializable {
    private String result;
    private String message;
    private String uid;
    
    private StatusData data;
}
