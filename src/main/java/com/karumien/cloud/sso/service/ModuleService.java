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

    String ROLE_PREFIX = "MODULE_";

    ModuleInfo getModule(String moduleId);

    ModuleInfo createModule(ModuleInfo module);

    void deleteModule(String moduleId);

    void activateModules(List<String> modules, List<String> crmAccountId);
        
    void deactivateModules(List<String> modules, List<String> crmAccountId);

    List<ModuleInfo> getAccountModules(String crmAccountId);

    boolean isActiveModule(String moduleId, String crmAccountId);

    List<String> getAccountModulesSimple(String crmAccountId);

}
