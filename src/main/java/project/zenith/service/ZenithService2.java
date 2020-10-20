package project.zenith.service;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import project.exception.JsonException;
import project.zenith.component.SessionInfo;
import project.zenith.model.DeviceList;
import project.zenith.model.Status;
import project.zenith.model.Login;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class ZenithService2 {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    
    @Autowired
    private Environment environment;
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
	private SessionInfo sessionInfo;
    
    private String makeUrl(String service, Map<String, Object> params) {
        String url = environment.getProperty("zenith.domain") + environment.getProperty("zenith."+service);        
		logger.debug("url = " + url);

        if (!"login".equals(service)) {
            if (params.get("uid") == null || "".equals(params.get("uid"))) {
                if (sessionInfo.getUid() == null || "".equals(sessionInfo.getUid())) {
                    throw new JsonException("session-expired", "session expired");
                }
                
                params.put("uid", sessionInfo.getUid()); //environment.getProperty("zenith.uid");
            }
            
            logger.debug("uid = " + params.get("uid"));
        }
        
        params.put("timestamp", String.valueOf(System.currentTimeMillis()));

        params.forEach((key, value) -> logger.debug("params = " + key + ":" + value));
        
        MultiValueMap multiValueMap = new LinkedMultiValueMap<String, Object>();
        multiValueMap.setAll(params);
        
        UriComponentsBuilder urlBuilder = UriComponentsBuilder.fromHttpUrl(url);
        url = urlBuilder.queryParams(multiValueMap).build().encode().toUri().toString();
        logger.debug("url = " + url);

        return url;   
    }
    
    public Object doStatus(String type, String code) {
        // ?uid={uid}&_={timestamp}&type={type}&code={code}&cmd=get
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("type", type);
        params.put("code", code);
        params.put("cmd", "get");
        
        String url = makeUrl("control", params);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");

        Object response = (new RestTemplate()).exchange(url, HttpMethod.GET, new HttpEntity<String>(headers), Object.class);
        return response;
    }
    
    public Login doLogin(String id, String password) {
        // ?_={timestamp}&loginID={id}&password={password}
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("loginID", id);
        params.put("password", password);
        
        String url = makeUrl("login", params);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");

        ResponseEntity<Login> response = (new RestTemplate()).exchange(url, HttpMethod.GET, new HttpEntity<String>(headers), Login.class);
        return response.getBody();
    }

    private Object doCommon(String service, Map<String, Object> params) {
        String url = makeUrl(service, params);
        
        // RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");
                
        Object response = (new RestTemplate()).exchange(url, HttpMethod.GET, new HttpEntity<String>(headers), String.class);
        return response;
    }
    
    public DeviceList doList() {
        // ?uid={uid}&_={timestamp}&type={type}
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("type", "list");
        
        String url = makeUrl("control", params);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");

        ResponseEntity<DeviceList> response = (new RestTemplate()).exchange(url, HttpMethod.GET, new HttpEntity<String>(headers), DeviceList.class);
        return response.getBody();
    }

    public Object doControl(Map<String, Object> params) {
        //uid check
        params.forEach((key, value) -> {
            logger.debug("params = " + key + ":" + value);
            
            if ("uid".equals(key)) {
                throw new JsonException("error-input", "input key error (uid)");
            }
        });
        
        return doCommon("control", params);
    }

    public Object doControl2(Map<String, Object> params) {
        return doCommon("control", params);
    }
    
    @Async
    public void doControlAsync(String type, String code, String action) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("type", type);
        params.put("code", code);
        params.put("action", action);
        params.put("uid", "0010203806");
        
        doControl2(params);
    }
    
}
