package project.zenith.model;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Data implements Serializable {

    private String td;
    private String name;
    private String status;
    private String posCode;
    private String type;

}
    