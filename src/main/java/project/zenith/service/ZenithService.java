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
        // params.putAll(params);
        params.forEach((key, value) -> logger.debug("params = " + key + ":" + value));
        
        MultiValueMap multiValueMap = new LinkedMultiValueMap<String, String>();
        multiValueMap.setAll(params);
        
        UriComponentsBuilder urlBuilder = UriComponentsBuilder.fromHttpUrl(url);
        url = urlBuilder.queryParams(multiValueMap).build().encode().toUri().toString();
        logger.debug("url = " + url);

        return url;   
    }
    
    public Status doStatus(String type, String code) {
        // ?uid={uid}&_={timestamp}&type={type}&code={code}&cmd=get
        Map<String, String> params = new HashMap<String, String>();
        params.put("type", type);
        params.put("code", code);
        params.put("cmd", "get");
        
        String url = makeUrl("control", params);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");

        ResponseEntity<Status> response = (new RestTemplate()).exchange(url, HttpMethod.GET, new HttpEntity<String>(headers), Status.class);
        return response.getBody();
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

    private Object doCommon(String service, Map<String, String> params) {
        String url = makeUrl(service, params);
        
        // RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");
                
        Object response = (new RestTemplate()).exchange(url, HttpMethod.GET, new HttpEntity<String>(headers), String.class);
        return response;
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
    
    public Object doControl(String type, String code, String action) {
        return doControl(type, code, action, null, 0);
    }
    
    public Object doControl(String type, String code, String action, String uid, int value) {
        String power = "";
        if ("on".equals(action)) power = "100";
        else if ("off".equals(action)) power = "0";
        
        // ?uid=0010203806&_={{timestamp}}&type=light&code=lt03&cmd=set&power=100
        Map<String, String> params = new HashMap<String, String>();
        params.put("type" , type);
        params.put("code" , code);
        params.put("cmd"  , "set");
        params.put("power", power);
        
        if (uid != null) {
            params.put("uid", uid);
        }

        // ?uid={uid}&_={timestamp}&type={type}&code={code}&cmd=set&cmd2={cmd2}&power={power}
        if ("ac".equals(type)) {
            if ("on".equals(action)) {
                params.put("cmd2" , "power");
                params.put("power", "1");
            } else if ("off".equals(action)) {
                params.put("cmd2" , "power");
                params.put("power", "0");
            } else if ("change".equals(action)) {
                params.put("cmd2" , "condition");
                params.put("power", "1");
                
                params.put("state", "on");
                params.put("td"   , code);
                
                getStatusData(params);
                
                params.put("ct"   , String.valueOf(value));

            }
        }

        return doCommon("control", params);
    }
    
    private void getStatusData(Map<String, String> params) {
        Status status = doStatus(params.get("type"), params.get("code"));
        
        params.put("status", status.getData().getStatus());
        params.put("rm"    , String.valueOf(status.getData().getRm    ()));
        params.put("ht"    , String.valueOf(status.getData().getHt    ()));
        params.put("ct"    , String.valueOf(status.getData().getCt    ()));
        params.put("ac"    , String.valueOf(status.getData().getAc    ()));
        params.put("cc"    , String.valueOf(status.getData().getCc    ()));
        params.put("sc"    , String.valueOf(status.getData().getSc    ()));
        params.put("as"    , String.valueOf(status.getData().getAs    ()));
        params.put("ef"    , String.valueOf(status.getData().getEf    ()));
        params.put("rnt"   , String.valueOf(status.getData().getRnt   ()));
        params.put("rft"   , String.valueOf(status.getData().getRft   ()));
        params.put("rt"    , String.valueOf(status.getData().getRt    ()));
    }
    
    @Async
    public void doControlAsync(String type, String code, String action) {
        doControl(type, code, action, "0010203806", 0);
    }
    
}
