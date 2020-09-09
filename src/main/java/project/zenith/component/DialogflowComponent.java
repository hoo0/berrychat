package project.zenith.component;

import com.google.actions.api.ActionRequest;
import com.google.actions.api.ActionResponse;
import com.google.actions.api.DialogflowApp;
import com.google.actions.api.ForIntent;
import com.google.actions.api.response.ResponseBuilder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DialogflowComponent extends DialogflowApp {

    @ForIntent("Trun On Light")
    public ActionResponse turnOnLight(ActionRequest request) {
        ResponseBuilder responseBuilder = getResponseBuilder(request)
            .add("메롱~ 불 안 켤거야");
        return responseBuilder.build();
    }

}