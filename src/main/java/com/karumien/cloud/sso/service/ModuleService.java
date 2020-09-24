/*
 * Copyright (c) 2019 Karumien s.r.o.
 *
 * Karumien s.r.o. is not responsible for defects arising from
 * unauthorized changes to the source code.
 */
package com.karumien.cloud.sso.service;

import java.util.Arrays;
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
    
    List<String> TLM_DEFAULT_ROLES = Arrays.asList(
        
        "ROLE_TLM00_D_IE","ROLE_TLM00_D_R","ROLE_TLM00_D_W","ROLE_TLM00_POI_IE","ROLE_TLM00_POI_R","ROLE_TLM00_POI_W",
        "ROLE_TLM00_USR_IE","ROLE_TLM00_USR_R","ROLE_TLM00_USR_W","ROLE_TLM00_V_IE","ROLE_TLM00_V_R","ROLE_TLM00_V_W",

        "ROLE_TLM01_DASH_R","ROLE_TLM01_DASH_W","ROLE_TLM01_FST_R","ROLE_TLM01_LM_R","ROLE_TLM01_EX_IE",
        "ROLE_TLM01_EX_R","ROLE_TLM01_EX_W",
        
        "ROLE_TLM02_BRD_R","ROLE_TLM02_ETA_R","ROLE_TLM02_OR_R","ROLE_TLM02_PLN_R","ROLE_TLM02_ROW_R","ROLE_TLM02_VP_R",
        "ROLE_TLM02_VP_W",
        
        "ROLE_TLM03_AN_R","ROLE_TLM03_JA_R","ROLE_TLM03_JA_W","ROLE_TLM03_LM_R","ROLE_TLM03_LT_R",
        
        "ROLE_TLM05_EX_IR","ROLE_TLM05_FC_R","ROLE_TLM05_FC_W","ROLE_TLM05_JAE_R","ROLE_TLM05_LM_R","ROLE_TLM05_LR_R",
        
        "ROLE_TLM06_AEI_R","ROLE_TLM06_AER_W","ROLE_TLM06_DRE_R",
        
        "ROLE_TLM07_CRD_R","ROLE_TLM07_CRD_W","ROLE_TLM07_TRD_R",
        
        "ROLE_TLM08_LMM_R","ROLE_TLM08_LMT_R",
        
        //deprecated roles
        "ROLE_TLM05_EX_IE", "ROLE_TLM05_EX_R", "ROLE_TLM05_EX_W",
        
        "ROLE_ADMIN","ROLE_DISPATCHER"
    );

    List<String> TLM_DEFAULT_ROLES_2020 = Arrays.asList(
        "ROLE_TLM", "ROLE_ADMIN","ROLE_DISPATCHER"
    );

    ModuleInfo getModule(String moduleId);

    ModuleInfo createModule(ModuleInfo module);

    void deleteModule(String moduleId);

    void activateModules(List<String> modules, List<String> accountNumber, Boolean applyRoles);
        
    void deactivateModules(List<String> modules, List<String> accountNumber);

    List<ModuleInfo> getAccountModules(String accountNumber);

    boolean isActiveModule(String moduleId, String accountNumber);

    List<String> getAccountModulesSimple(String accountNumber);

    List<ModuleInfo> getModules();

}
