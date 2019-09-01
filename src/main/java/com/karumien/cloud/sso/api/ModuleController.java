/*
 * Copyright (c) 2019-2029 Karumien s.r.o.
 *
 * Karumien s.r.o. is not responsible for defects arising from 
 * unauthorized changes to the source code.
 */
package com.karumien.cloud.sso.api;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import com.karumien.cloud.sso.api.handler.ModulesApi;
import com.karumien.cloud.sso.api.model.ModuleInfo;
import com.karumien.cloud.sso.service.ModuleService;

import io.swagger.annotations.Api;

/**
 * REST Controller for Module Service (API).
 * 
 * @author <a href="miroslav.svoboda@karumien.com">Miroslav Svoboda</a>
 * @since 1.0, 18. 7. 2019 11:15:51 
 */
@RestController
@Api(value = "Module Service", description = "Management of Modules (Licences)", tags = { "Module Service" })
public class ModuleController implements ModulesApi {

    @Autowired
    private ModuleService moduleService;

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<ModuleInfo> getModule(String moduleId) {
        return new ResponseEntity<>(moduleService.getModule(moduleId), HttpStatus.OK);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<ModuleInfo> createModule(@Valid ModuleInfo module) {
        return new ResponseEntity<>(moduleService.createModule(module), HttpStatus.CREATED);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<Void> deleteModule(String moduleId) {
        moduleService.deleteModule(moduleId);
        return new ResponseEntity<>(HttpStatus.OK);
    }
        
}
