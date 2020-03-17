/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.karumien.cloud.sso.service;

import java.util.List;
import java.util.stream.Collectors;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.NumberUtils;

import com.karumien.cloud.sso.api.model.ModuleInfo;
import com.karumien.cloud.sso.api.model.RightGroup;
import com.karumien.cloud.sso.api.model.RoleInfo;


/**
 * Implementation {@link AccountService} for Account Management.
 *
 * @author <a href="viliam.litavec@karumien.com">Viliam Litavec</a>
 * @since 1.0, 22. 8. 2019 18:59:57
 */
@Service
public class GroupServiceImpl implements GroupService {

    @Value("${keycloak.realm}")
    private String realm;

    @Autowired
    private Keycloak keycloak;
    
    @Autowired
    private SearchService searchService;
    
    @Autowired
    private RoleService roleService;

    @Autowired
    private LocalizationService localizationService;
    
    /**
     * {@inheritDoc}
     */
    @Override
    public List<ModuleInfo> getAccountHierarchy(String accountNumber) {
        //TODO: apply buyed services
        return keycloak.realm(realm).groups().group(searchService.getMasterGroupId(SELFCARE_GROUP)).toRepresentation()
            .getSubGroups().stream()
           .map(g -> mappingModule(g))
           .collect(Collectors.toList());               
    }
    
    private ModuleInfo mappingModule(GroupRepresentation group) {

        // TODO viliam: Orica
        ModuleInfo moduleInfo = new ModuleInfo();
        moduleInfo.setName(group.getName());
        moduleInfo.setModuleId(searchService.getSimpleAttribute(group.getAttributes(), ATTR_MODULE_ID).orElse(null));
        String businessPriority = searchService.getSimpleAttribute(group.getAttributes(), ATTR_BUSINESS_PRIORITY).orElse(null);
        if (businessPriority != null) {
            moduleInfo.setBusinessPriority(NumberUtils.parseNumber(businessPriority, Integer.class));
        }
        moduleInfo.setTranslation(localizationService.translate(
                moduleInfo.getModuleId() == null ? null : "module" + "." + moduleInfo.getModuleId().toLowerCase(), 
                        group.getAttributes(), LocaleContextHolder.getLocale(), group.getName()));
        
        moduleInfo.setGroups(group.getSubGroups().stream()
            .map(rg -> mappingRightGroup(rg))
            .collect(Collectors.toList()));
        return moduleInfo;
    }
    
    private RightGroup mappingRightGroup(GroupRepresentation group) {

        // TODO viliam: Orica
        RightGroup rightGroup = new RightGroup();
        rightGroup.setName(group.getName());
        rightGroup.setGroupId(searchService.getSimpleAttribute(group.getAttributes(), ATTR_RIGHT_GROUP_ID).orElse(null));
        rightGroup.setServiceId(searchService.getSimpleAttribute(group.getAttributes(), ATTR_SERVICE_ID).orElse(null));
        String businessPriority = searchService.getSimpleAttribute(group.getAttributes(), ATTR_BUSINESS_PRIORITY).orElse(null);
        if (businessPriority != null) {
            rightGroup.setBusinessPriority(NumberUtils.parseNumber(businessPriority, Integer.class));
        }
        rightGroup.setTranslation(localizationService.translate(
                rightGroup.getGroupId() == null ? null : "group" + "." + rightGroup.getGroupId().toLowerCase(), 
                        group.getAttributes(), LocaleContextHolder.getLocale(), group.getName()));
        
        return rightGroup;
    }

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<RoleInfo> getAccountRoles(String accountNumber) {
	    return roleService.getAccountRoles(keycloak.realm(realm).groups().group(searchService.getMasterGroupId(SELFCARE_GROUP)), false);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<RoleRepresentation> getAccountRolesRepresentation(String accountNumber) {
        return roleService.getAccountRolesRepresentation(keycloak.realm(realm).groups().group(searchService.getMasterGroupId(SELFCARE_GROUP)), false);
	}
	
    /**
     * {@inheritDoc}
     */
	@Override
	public List<String> getAccountRightsOfIdentity(String contactNumber) {
        return roleService.getIdentityRights(keycloak.realm(realm).groups().group(searchService.getMasterGroupId(SELFCARE_GROUP)), contactNumber);
	}
		
}
