package project.zenith.controller;

import java.util.concurrent.ExecutionException;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import project.zenith.component.DialogflowComponent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
class DialogflowController {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    
    @Autowired
    DialogflowComponent dialogflowComponent;
    
    @RequestMapping(value = "/zenith/api/dialogflow", method = RequestMethod.POST, produces = { "application/json" })
    String serveAction(@RequestBody String requestBody, @RequestHeader Map<String, String> headers) {
        
        logger.debug("dialogflow = " + requestBody);
        
        try {
            return dialogflowComponent.handleRequest(requestBody, headers).get();
        } catch (InterruptedException | ExecutionException e) {
            return handleError(e);
        }
    }
    
    private String handleError(Exception e) {
        e.printStackTrace();
        logger.error("Error in App.handleRequest ", e);
        return "Error handling the intent - " + e.getMessage();
    }
    
}
