package project.zenith.model;

import java.io.Serializable;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeviceList implements Serializable {
    private String result;
    private String message;
    private String uid;
    
    private List<DeviceListData> data;
    
    @Override
    public String toString() {
        return "result="+result+","
              +"message="+message;
    }
}
