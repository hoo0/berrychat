package project.service;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class ZenithService {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    
    @Autowired
    Environment environment;

    @Autowired
    RestTemplate restTemplate;
    
    public Object commonService(String serviceType,
                                 Map<String, String> params2) {
        String url = environment.getProperty("zenith.api.url."+serviceType);
        String uid = environment.getProperty("zenith.api.uid");
        long timestamp = System.currentTimeMillis();
		logger.debug("zenith.api.url."+serviceType + " = " + url);
        logger.debug("zenith.api.uid = " + uid);
        logger.debug("timestamp = " + timestamp);

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");
        
        /* http://182.223.25.170:8507/Api/Control.aspx?type={type]&uid={uid}&_={timestamp} */
        Map<String, String> params = new HashMap<String, String>();
        params.put("uid", uid);
        params.put("timestamp", String.valueOf(timestamp));
        params.putAll(params2);
        
        params.forEach((key, value) -> logger.debug("params = " + key + ":" + value));
        
        Object response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<String>(headers), String.class, params);
        // logger.debug("response = " + String.valueOf(response));
        return response;
    }

    public Object deviceList0() {

        String url = environment.getProperty("zenith.api.url.deviceList");
        String uid = environment.getProperty("zenith.api.uid");
        long timestamp = System.currentTimeMillis();
		logger.debug("zenith.api.url.deviceList = " + url);
        logger.debug("zenith.api.uid = " + uid);
        logger.debug("timestamp = " + timestamp);

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");
        // headers.setContentType(new MediaType("application","json",Charset.forName("UTF-8")));    //Response Header to UTF-8  
        
        /* http://182.223.25.170:8507/Api/Control.aspx?type={type]&uid={uid}&_={timestamp} */
        UriComponents builder = UriComponentsBuilder.fromHttpUrl(url)
            .queryParam("uid", uid)
            .queryParam("_", String.valueOf(timestamp))
            .build(false);    //자동으로 encode해주는 것을 막기 위해 false
        logger.debug("builder.toUriString() = " + builder.toUriString());
        
        Object response = restTemplate.exchange(builder.toUriString(), HttpMethod.GET, new HttpEntity<String>(headers), String.class);
        logger.debug("response = " + String.valueOf(response));
        return response;
    }
                                                    

    public Object deviceList() {
        Map<String, String> params = new HashMap<String, String>();
        params.put("type", "list");

        return commonService("deviceList", params);
    }
}
