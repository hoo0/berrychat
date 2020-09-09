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

import project.zenith.component.SessionInfo;
import project.zenith.model.DeviceList;
import project.zenith.model.Data;
import project.zenith.model.Login;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class ZenithService {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    
    @Autowired
    Environment environment;

    @Autowired
    RestTemplate restTemplate;
    
    @Autowired
	private SessionInfo sessionInfo;
    
    private String makeUrl(String service, Map<String, String> params) {
        String url = environment.getProperty("zenith.domain") + environment.getProperty("zenith."+service);        
		logger.debug("url = " + url);

        Map<String,String> params2 = new HashMap<String,String>();
        if (!"login".equals(service)) {
            String uid = sessionInfo.getUid(); //environment.getProperty("zenith.uid");
            logger.debug("uid = " + uid);
            
            params2.put("uid", uid);
        }
        params2.put("timestamp", String.valueOf(System.currentTimeMillis()));
        params2.putAll(params);
        params2.forEach((key, value) -> logger.debug("params = " + key + ":" + value));
        
        MultiValueMap multiValueMap = new LinkedMultiValueMap<String, String>();
        multiValueMap.setAll(params2);
        
        UriComponentsBuilder urlBuilder = UriComponentsBuilder.fromHttpUrl(url);
        url = urlBuilder.queryParams(multiValueMap).build().encode().toUri().toString();
        logger.debug("url = " + url);

        return url;   
    }
    
    private Object doCommon(String service, Map<String, String> params) {
        String url = makeUrl(service, params);
        
        // RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");
                
        Object response = (new RestTemplate()).exchange(url, HttpMethod.GET, new HttpEntity<String>(headers), String.class);
        return response;
    }

    public Login doLogin(String id, String password) {
        // ?_={timestamp}&loginID={id}&password={password}
        Map<String, String> params = new HashMap<String, String>();
        params.put("loginID", id);
        params.put("password", password);
        
        String url = makeUrl("login", params);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");

        ResponseEntity<Login> response = (new RestTemplate()).exchange(url, HttpMethod.GET, new HttpEntity<String>(headers), Login.class);
        return response.getBody();
    }

    public Object doList() {
        // ?uid={uid}&_={timestamp}&type={type}
        Map<String, String> params = new HashMap<String, String>();
        params.put("type", "list");

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");
        
        return doCommon("control", params);
    }
    
    public DeviceList doList2() {
        // ?uid={uid}&_={timestamp}&type={type}
        Map<String, String> params = new HashMap<String, String>();
        params.put("type", "list");
        
        String url = makeUrl("control", params);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");

        ResponseEntity<DeviceList> response = (new RestTemplate()).exchange(url, HttpMethod.GET, new HttpEntity<String>(headers), DeviceList.class);
        return response.getBody();
    }
    
    public Object doStatus(String type, String code) {
        // ?uid={uid}&_={timestamp}&type={type}&code={code}&cmd=get
        Map<String, String> params = new HashMap<String, String>();
        params.put("type", type);
        params.put("code", code);
        params.put("cmd", "get");

        return doCommon("control", params);
    }
    
    public Object doControl(String type, String code, String action) {
        String power = "";
        if ("on".equals(action)) power = "100";
        else if ("off".equals(action)) power = "0";
        
        // ?uid=0010203806&_={{timestamp}}&type=light&code=lt03&cmd=set&power=100
        Map<String, String> params = new HashMap<String, String>();
        params.put("type", type);
        params.put("code", code);
        params.put("cmd", "set");
        params.put("power", power);

        // ?uid={uid}&_={timestamp}&type={type}&code={code}&cmd=set&cmd2={cmd2}&power={power}
        if ("ac".equals(type)) {
            if ("on".equals(action)) {
                params.put("cmd2", "power");
                params.put("power", "1");
            } else if ("off".equals(action)) {
                params.put("cmd2", "power");
                params.put("power", "0");
            }
        }

        return doCommon("control", params);
    }
    
    @Async
    public void doControlAsync(String type, String code, String action) {
        doControl(type, code, action);
    }
}
