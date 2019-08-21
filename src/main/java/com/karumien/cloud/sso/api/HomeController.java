/*
 * Copyright (c) 2019-2029 Karumien s.r.o.
 *
 * Karumien s.r.o. is not responsible for defects arising from 
 * unauthorized changes to the source code.
 */
package com.karumien.cloud.sso.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.karumien.cloud.sso.api.model.VersionInfo;
import com.karumien.cloud.sso.service.InfoService;

import io.swagger.annotations.Api;

/**
 * Home redirection to swagger api documentation.
 *
 * @author <a href="miroslav.svoboda@karumien.com">Miroslav Svoboda</a>
 * @since 1.0, 15. 7. 2019 21:27:49
 */
@Controller
@Api(value = "Version Info Service", description = "REST API for Version Info Service", tags = { "Version Info Service" })
public class HomeController {
    
    @Autowired
    private InfoService infoService;

    @RequestMapping(value = "/")
    public String index() {
        return "redirect:swagger-ui.html";
    }
    
    @RequestMapping(value = "/version", method = RequestMethod.GET)
    public ResponseEntity<VersionInfo> getVersionInfo() {
        return new ResponseEntity<>(infoService.getVersionInfo(), HttpStatus.OK);
    }
}
