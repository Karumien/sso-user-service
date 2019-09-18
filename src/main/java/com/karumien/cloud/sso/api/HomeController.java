/*
 * Copyright (c) 2019-2029 Karumien s.r.o.
 *
 * Karumien s.r.o. is not responsible for defects arising from 
 * unauthorized changes to the source code.
 */
package com.karumien.cloud.sso.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

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
@Api(value = "Info Service", description = "Home Rewrite and Application Informations", tags = { "Info Service" })
public class HomeController {
    
    @Autowired
    private InfoService infoService;

    @GetMapping(value = "/")
    public String index() {
        return "redirect:swagger-ui.html";
    }
    
    @GetMapping(value = "/version")
    public ResponseEntity<VersionInfo> getVersion() {
        return new ResponseEntity<>(infoService.getVersionInfo(), HttpStatus.OK);
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @GetMapping(value = "/locale", produces = "text/plain")
    public ResponseEntity<Void> getLocale() {
        return new ResponseEntity(""+LocaleContextHolder.getLocale(), HttpStatus.OK);
    }
}
