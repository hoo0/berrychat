package project.zenith.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import project.zenith.model.DeviceList;
import project.zenith.model.Login;
import project.zenith.service.ZenithService;

@Controller
@RequestMapping("/zenith/api")
class ApiController {
    
    @Autowired
    ZenithService zenithService;
    
	@GetMapping( "/login")
    @ResponseBody
	public Login doLogin(@RequestParam("id") String id, 
                         @RequestParam("password") String password) {
        
        Login login = zenithService.doLogin(id, password);
        return login;
	}

	@GetMapping( "/list")
    @ResponseBody
	public Object list() {
        
        Object response =  zenithService.doList();
        return response;
	}
    
	@GetMapping( "/list2")
    @ResponseBody
	public DeviceList list2() {
        
        DeviceList response =  zenithService.doList2();
        return response;
	}
    
	@GetMapping( "/status/{type}/{code}")
    @ResponseBody
	public Object status(@PathVariable("type") String type, 
                         @PathVariable("code") String code) {
        
        Object response =  zenithService.doStatus(type, code);
        return response;
	}
    
	@GetMapping( "/control/{type}/{code}/{action}")
    @ResponseBody
	public Object control(@PathVariable("type") String type, 
                          @PathVariable("code") String code,
                          @PathVariable("action") String action) {
        
        Object response =  zenithService.doControl(type, code, action);
        return response;
	}

}
