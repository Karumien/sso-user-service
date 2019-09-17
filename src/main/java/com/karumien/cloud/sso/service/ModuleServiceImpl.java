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
package com.karumien.cloud.sso.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.ws.rs.NotFoundException;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RoleResource;
import org.keycloak.representations.idm.RoleRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.karumien.cloud.sso.api.model.ModuleInfo;
import com.karumien.cloud.sso.api.model.RoleInfo;
import com.karumien.cloud.sso.exceptions.AccountNotFoundException;
import com.karumien.cloud.sso.exceptions.ModuleNotFoundException;
import com.karumien.cloud.sso.exceptions.RoleNotFoundException;

/**
 * Implementation {@link ModuleService} for Module Management.
 *
 * @author <a href="viliam.litavec@karumien.com">Viliam Litavec</a>
 * @since 1.0,  22. 8. 2019 18:59:57
 */
@Service
public class ModuleServiceImpl implements ModuleService {

    @Value("${keycloak.realm}")
    private String realm;

    @Autowired
    private Keycloak keycloak;

    @Autowired
    private RoleService roleService;
    
    @Autowired
    private IdentityService identityService;
    
    @Autowired 
    private AccountService accountService;

    /**
     * {@inheritDoc}
     */
    @Override
    public ModuleInfo getModule(String moduleId) {
        try {
            return mapping(roleService.getRoleBaseOnId(getRoleName(moduleId)));
            //FIXME: why NotFoundException
        } catch (RoleNotFoundException | NotFoundException e) {
            throw new ModuleNotFoundException(moduleId);
        }
    }
    
    public Optional<RoleResource> findModule(String moduleId) {
        return roleService.findRoleResource(getRoleName(moduleId));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ModuleInfo createModule(ModuleInfo module) {
        
        RoleInfo role = new RoleInfo();
        role.setClientRole(false);
        role.setRoleId(getRoleName(module.getModuleId()));
        role.setDescription(module.getDescription());

        return mapping(roleService.createRole(role));
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteModule(String moduleId) {
        try{
            roleService.deleteRole(getRoleName(moduleId));
        } catch (RoleNotFoundException e) {
            throw new ModuleNotFoundException(moduleId);
        }
    }
    
    private ModuleInfo mapping(RoleInfo roleInfo) {
        
        if (! roleInfo.getRoleId().contains(ROLE_PREFIX)) {
            throw new IllegalStateException("Role " + roleInfo + " is not module");
        }
        
        //TODO viliam.litavec: Orika
        ModuleInfo module = new ModuleInfo();
        module.setModuleId(roleInfo.getRoleId().substring(ROLE_PREFIX.length()));
        module.setDescription(roleInfo.getDescription());
        return module;
    }

    private ModuleInfo mapping(RoleRepresentation roleRepresentation) {
        
        if (! roleRepresentation.getName().contains(ROLE_PREFIX)) {
            throw new IllegalStateException("Role " + roleRepresentation + " is not module");
        }
        
        //TODO viliam.litavec: Orika
        ModuleInfo module = new ModuleInfo();
        module.setModuleId(roleRepresentation.getName().substring(ROLE_PREFIX.length()));
        module.setDescription(roleRepresentation.getDescription());
        return module;
    }

    private String getRoleName(String moduleId) {
        return ROLE_PREFIX + moduleId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void activateModules(List<String> modules, List<String> crmAccountIds) {
        
        // TODO: transactional?
        List<RoleRepresentation> rolesToAdd = modules.stream().map(moduleId -> findModule(moduleId))
            .filter(module -> module.isPresent())
            .map(module -> module.get().toRepresentation())
            .collect(Collectors.toList());
        
        crmAccountIds.stream().map(crmAccountId ->  accountService.findGroupResource(crmAccountId))
            .filter(accountResource -> accountResource.isPresent())
            .forEach(accountResource -> accountResource.get().roles().realmLevel().add(rolesToAdd));

        crmAccountIds.stream().forEach(crmAccountId -> 
            accountService.getAccountIdentities(crmAccountId).forEach(identity -> identityService.refreshBinaryRoles(
                keycloak.realm(realm).users().get(identity.getIdentityId()).toRepresentation())));

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deactivateModules(List<String> modules, List<String> crmAccountIds) {
        
        // TODO: transactional?
        List<RoleRepresentation> rolesToRemove = modules.stream().map(moduleId -> findModule(moduleId))
            .filter(module -> module.isPresent())
            .map(module -> module.get().toRepresentation())
            .collect(Collectors.toList());
        
        crmAccountIds.stream().map(crmAccountId ->  accountService.findGroupResource(crmAccountId))
            .filter(accountResource -> accountResource.isPresent())
            .forEach(accountResource -> accountResource.get().roles().realmLevel().remove(rolesToRemove));
        
        crmAccountIds.stream().forEach(crmAccountId -> 
            accountService.getAccountIdentities(crmAccountId).forEach(identity -> identityService.refreshBinaryRoles(
                keycloak.realm(realm).users().get(identity.getIdentityId()).toRepresentation())));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ModuleInfo> getAccountModules(String crmAccountId) {
        return accountService.findGroupResource(crmAccountId)
            .orElseThrow(() -> new AccountNotFoundException(crmAccountId)).roles().realmLevel()
            .listAll().stream().filter(role -> role.getName().startsWith(ROLE_PREFIX))
            .map(role -> mapping(role)).collect(Collectors.toList());                
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getAccountModulesSimple(String crmAccountId) {
        List<ModuleInfo> info = getAccountModules(crmAccountId);
        return info.stream().map(module -> module.getModuleId()).collect(Collectors.toList());                
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isActiveModule(String moduleId, String crmAccountId) {
        //FIXME: performance
        return getAccountModules(crmAccountId).stream().filter(module -> module.getModuleId().equals(moduleId)).findAny().isPresent();
    }

}
