package project.zenith.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import project.zenith.component.SessionInfo;
import project.zenith.model.Login;
import project.zenith.service.ZenithService;

@Controller
@RequestMapping("/zenith")
class ViewController {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    
    @Autowired
    ZenithService zenithService;
    
    @Autowired
	private SessionInfo sessionInfo;
    
    /* zenith(main) -> login -> zenith(main) -> control (set)
     */

	@GetMapping("")
	public String redirectToMain() {
        logger.debug("redirectToMain");
		return "redirect:/zenith/";
	}
    
	@GetMapping("/")
	public String main(Model model,
                       @RequestHeader Map<String, String> headers) {
        logger.debug("main");
        
        // headers.forEach((key, value) -> {
        //     logger.debug(String.format("Header '%s' = %s", key, value));
        // });
        logger.debug("Header.accept = " + headers.get("accept"));

        if (sessionInfo == null || StringUtils.isEmpty(sessionInfo.getUid())) { /* 로그인체크 */
            logger.debug("session is null : redirect:/zenith/login");
            return "redirect:/zenith/login";
        }

        logger.debug("sessionInfo.getUid() = " + sessionInfo.getUid());
        model.addAttribute("uid", sessionInfo.getUid());

        return "main";
	}

	@GetMapping("/login")
	public String login() {
        logger.error("login");
		return "login";
	}
    
	@PostMapping("/login")
	public String loginAction(Model model,
                              @RequestParam("id") String id, 
	                          @RequestParam("password") String password) throws Exception {
        logger.debug("loginAction");
        try {
            Login login = zenithService.doLogin(id, password);
            logger.debug("login = " + login.toString());
            
            if ("OK".equals(login.getResult())) {
                if (!StringUtils.isEmpty(login.getUid())) { /* 세션에 저장 */
                    sessionInfo.setUid(login.getUid());
                }
            } else {
                model.addAttribute("message", login.getMessage());
                return "login";
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("message", "로그인 중 오류가 발생했습니다.");
            return "login";
        }
        
        zenithService.doList();
        
		return "redirect:/zenith";
	}
    
	@GetMapping("/logout")
	public String logout() {
        logger.debug("logout");
        sessionInfo.setUid(null);
        
		return "redirect:/zenith";
	}
    
}
