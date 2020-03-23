/*
 * Copyright (c) 2019 Karumien s.r.o.
 *
 * Karumien s.r.o. is not responsible for defects arising from
 * unauthorized changes to the source code.
 */
package com.karumien.cloud.sso.service;

import java.util.List;

import com.karumien.cloud.sso.api.model.ModuleInfo;

/**
 * Service provides scenarios for Modules's management.
 *
 * @author <a href="viliam.litavec@karumien.com">Viliam Litavec</a>
 * @since 1.0, 10. 7. 2019 22:07:27
 */
public interface ModuleService {

    String MODULE_PREFIX = "MODULE_";

    ModuleInfo getModule(String moduleId);

    ModuleInfo createModule(ModuleInfo module);

    void deleteModule(String moduleId);

    void activateModules(List<String> modules, List<String> accountNumber);
        
    void deactivateModules(List<String> modules, List<String> accountNumber);

    List<ModuleInfo> getAccountModules(String accountNumber);

    boolean isActiveModule(String moduleId, String accountNumber);

    List<String> getAccountModulesSimple(String accountNumber);

    List<ModuleInfo> getModules();

}
