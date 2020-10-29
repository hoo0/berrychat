/*
 * Copyright 2012-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package project.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
class WelcomeController {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@GetMapping("/")
	public String welcome(HttpServletRequest request) {
        // logger.trace("Trace Level 테스트");
        // logger.debug("debug Level 테스트");
        // logger.info("info Level 테스트");
        // logger.warn("Warn Level 테스트");
        // logger.error("error Level 테스트");
        
        String URL = request.getRequestURL().toString();
        logger.info("URL=" + URL);
        
        if (URL.indexOf("://zenith2.duckdns.org") > 0 || URL.indexOf("://berrychat.run.goorm.io/") > 0) {
            return "redirect:/zenith/";
        }
		return "welcome";
	}

}
