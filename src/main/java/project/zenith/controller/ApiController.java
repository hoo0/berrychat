package project.zenith.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import project.zenith.model.DeviceList;
import project.zenith.model.Login;
import project.zenith.model.Status;
import project.zenith.service.ZenithService;

@Controller
@RequestMapping("/zenith/api")
class ApiController {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    
    @Autowired
    ZenithService zenithService;
    
	@GetMapping("/login")
    @ResponseBody
	public Login doLogin(@RequestParam("id") String id, 
                         @RequestParam("password") String password) {
        return zenithService.doLogin(id, password);
	}

	// @GetMapping("/list")
	// @ResponseBody
	// public Object list() {
	// return zenithService.doList();
	// }
    
	@GetMapping("/list")
    @ResponseBody
	public DeviceList list2() {
        return zenithService.doList2();
	}
    
	@GetMapping("/status/{type}/{code}")
    @ResponseBody
	public Status status(@PathVariable("type") String type, 
                         @PathVariable("code") String code) {
        return zenithService.doStatus(type, code);
	}
    
	@GetMapping("/control/{type}/{code}/{action}")
    @ResponseBody
	public Object control(@PathVariable("type") String type, 
                          @PathVariable("code") String code,
                          @PathVariable("action") String action) {
        logger.debug(String.format("control : type=%s code=%s action=%s", type, code, action));
        return "control";//zenithService.doControl(type, code, action);
	}
    
	@GetMapping("/control/{type}/{code}/{action}/{value}")
    @ResponseBody
	public Object control2(@PathVariable("type") String type, 
                           @PathVariable("code") String code,
                           @PathVariable("action") String action,
                           @PathVariable("value") int value) {
        logger.debug(String.format("control2 : type=%s code=%s action=%s value=%d", type, code, action, value));
        return "control2";//zenithService.doControl(type, code, action, null, value);
	}
    
    
	@PostMapping("/control")
    @ResponseBody
	public Map<String, Object> control3(@RequestBody Map<String, Object> param) {
        
        logger.debug("param=" + param);
        return param;//zenithService.doControl(type, code, action);
	}

}
