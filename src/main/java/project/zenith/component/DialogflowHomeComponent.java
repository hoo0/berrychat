package project.zenith.component;

import com.google.actions.api.ActionRequest;
import com.google.actions.api.ActionResponse;
import com.google.actions.api.DialogflowApp;
import com.google.actions.api.ForIntent;
import com.google.actions.api.response.ResponseBuilder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import project.zenith.service.ZenithService;

@Component
public class DialogflowHomeComponent extends DialogflowApp {

    @Autowired
    ZenithService zenithService;
    
    @ForIntent("Turn On Light")
    public ActionResponse turnOnLight(ActionRequest request) {
        zenithService.doControlAsync("light", "lt03", "on");
        ResponseBuilder responseBuilder = getResponseBuilder(request).add("불을 켰다냥");
        return responseBuilder.build();
    }

    @ForIntent("Turn Off Light")
    public ActionResponse turnOffLight(ActionRequest request) {
        zenithService.doControlAsync("light", "lt03", "off");
        return getResponseBuilder(request).add("불을 껐다냥").build();
    }
    
    @ForIntent("Turn On AirCon1")
    public ActionResponse turnOnAirCon1(ActionRequest request) {
        zenithService.doControlAsync("ac", "ac01", "on");
        return getResponseBuilder(request).add("1번에어컨을 켰다냥").build();
    }

    @ForIntent("Turn Off AirCon1")
    public ActionResponse turnOffAirCon1(ActionRequest request) {
        zenithService.doControlAsync("ac", "ac01", "off");
        return getResponseBuilder(request).add("1번에어컨을 껐다냥").build();
    }
    
    @ForIntent("Turn On AirCon2")
    public ActionResponse turnOnAirCon2(ActionRequest request) {
        zenithService.doControlAsync("ac", "ac02", "on");
        return getResponseBuilder(request).add("2번에어컨을 켰다냥").build();
    }

    @ForIntent("Turn Off AirCon2")
    public ActionResponse turnOffAirCon2(ActionRequest request) {
        zenithService.doControlAsync("ac", "ac02", "off");
        return getResponseBuilder(request).add("2번에어컨을 껐다냥").build();
    }
    
    @ForIntent("Turn On AirCon All")
    public ActionResponse turnOnAirConAll(ActionRequest request) {
        zenithService.doControlAsync("ac", "ac01", "on");
        zenithService.doControlAsync("ac", "ac02", "on");
        return getResponseBuilder(request).add("에어컨을 모두 켰다냥").build();
    }

    @ForIntent("Turn Off AirCon All")
    public ActionResponse turnOffAirConAll(ActionRequest request) {
        zenithService.doControlAsync("ac", "ac01", "off");
        zenithService.doControlAsync("ac", "ac02", "off");
        return getResponseBuilder(request).add("에어컨을 모두 껐다냥").build();
    }

}