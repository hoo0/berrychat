package project.zenith.model;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StatusData implements Serializable {

    private String type;
    private String td;
    private String status;
    private int power;
    private int rm ;
    private int ht ;
    private int ct ;
    private int ac ;
    private int cc ;
    private int sc ;
    private int as ;
    private int ef ;
    private int rnt;
    private int rft;
    private int rt ;
}
