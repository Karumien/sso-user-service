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

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.RoleRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.karumien.cloud.sso.api.dto.GroupInfo;
import com.karumien.cloud.sso.api.model.ModuleInfo;
import com.karumien.cloud.sso.api.model.RightGroup;
import com.karumien.cloud.sso.api.model.RoleInfo;

import lombok.extern.slf4j.Slf4j;


/**
 * Implementation {@link AccountService} for Account Management.
 *
 * @author <a href="viliam.litavec@karumien.com">Viliam Litavec</a>
 * @since 1.0, 22. 8. 2019 18:59:57
 */
@Service
@Slf4j
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
    	return getSelfcareModulesFromResources().stream().map(rawModule -> convertToModuleInfo(rawModule)).collect(Collectors.toList()); 
    }

	private ModuleInfo convertToModuleInfo(GroupInfo rawModule) {
        ModuleInfo moduleInfo = new ModuleInfo();
        moduleInfo.setName(rawModule.getName());
        moduleInfo.setModuleId(rawModule.getModuleId());
        moduleInfo.setBusinessPriority(rawModule.getBusinessPriority());
        moduleInfo.setTranslation(localizationService.translate(
                moduleInfo.getModuleId() == null ? null : "module" + "." + moduleInfo.getModuleId().toLowerCase(), 
                		rawModule.getAttributes(), LocaleContextHolder.getLocale(), rawModule.getName()));
        
        moduleInfo.setGroups(rawModule.getGroups() == null ? null : rawModule.getGroups().stream().map(group -> convertToRightGroup(group)).collect(Collectors.toList()));
        return moduleInfo;
	}

	private RightGroup convertToRightGroup(GroupInfo group) {
		RightGroup rightGroup = new RightGroup();
        rightGroup.setName(group.getName());
        rightGroup.setGroupId(group.getModuleId());
        rightGroup.setServiceId(group.getServiceId());
        rightGroup.setBusinessPriority(group.getBusinessPriority());
        rightGroup.setTranslation(localizationService.translate(
                rightGroup.getGroupId() == null ? null : "group" + "." + rightGroup.getGroupId().toLowerCase(), 
                        group.getAttributes(), LocaleContextHolder.getLocale(), group.getName()));
        
        return rightGroup;
	}

	@Cacheable
    public List<GroupInfo> getSelfcareModulesFromResources() {
		try {
		    List<GroupInfo> modules = Arrays.asList(new ObjectMapper().readValue(new ClassPathResource("json/modulesInfo.json").getFile(), GroupInfo[].class));
		    return modules;
		} catch (Exception ex) {
		    log.error("Exception when reading modulesInfo resource JSON", ex);
		    return List.of();
		}
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
