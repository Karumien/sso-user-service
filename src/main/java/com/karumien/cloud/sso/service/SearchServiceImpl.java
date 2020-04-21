/*
 * Copyright (c) 2019-2029 Karumien s.r.o.
 *
 * Karumien s.r.o. is not responsible for defects arising from 
 * unauthorized changes to the source code.
 */
package com.karumien.cloud.sso.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.karumien.cloud.sso.api.entity.AccountEntity;
import com.karumien.cloud.sso.api.entity.UserEntity;
import com.karumien.cloud.sso.api.model.AccountPropertyType;
import com.karumien.cloud.sso.api.model.IdentityPropertyType;
import com.karumien.cloud.sso.api.repository.AccountEntityRepository;
import com.karumien.cloud.sso.api.repository.CredentialRepository;
import com.karumien.cloud.sso.api.repository.GroupEntityRepository;
import com.karumien.cloud.sso.api.repository.RoleAttributeRepository;
import com.karumien.cloud.sso.api.repository.UserAttributeRepository;
import com.karumien.cloud.sso.api.repository.UserEntityRepository;
import com.karumien.cloud.sso.exceptions.AccountNotFoundException;

/**
 * Implementation of {@link SearchService}.
 *
 * @author <a href="miroslav.svoboda@karumien.com">Miroslav Svoboda</a>
 * @since 1.0, 4. 10. 2019 11:32:51 
 */
@Service
public class SearchServiceImpl implements SearchService {

    @Autowired
    private UserAttributeRepository userAttributeRepository;

    @Autowired
    private UserEntityRepository userEntityRepository;
    
    @Autowired
    private CredentialRepository credentialRepository;

    @Autowired
    private GroupEntityRepository groupEntityRepository;

    @Autowired
    private AccountEntityRepository accountEntityRepository;

    @Autowired
    private RoleAttributeRepository roleAttributeRepository;
    
    @Value("${keycloak.realm}")
    private String realm;
    
    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<String> findUserIdsByAttribute(IdentityPropertyType attribute, String value) {
        
        if (attribute == IdentityPropertyType.ID) {
            UserEntity user = userEntityRepository.findById(value).orElse(null);
            return user != null ? Arrays.asList(user.getUserId()) : new ArrayList<>();
        }

        if (attribute == IdentityPropertyType.ATTR_NOTE) {
            value = value.toLowerCase();
        }
        
        if (attribute == IdentityPropertyType.USERNAME) {
            return userEntityRepository.findUserIdsByUsername(realm, value.toLowerCase());
        }

        if (attribute == IdentityPropertyType.ATTR_HAS_CREDENTIALS) {
            return credentialRepository.findUserIdByCredentialsType("password");
        }

        if (attribute == IdentityPropertyType.EMAIL) {
            return userEntityRepository.findUserIdsByEmail(realm, value.toLowerCase());
        }
        
        return userAttributeRepository.findUserIdsByAttribute(attribute.getValue(), value);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<String> findAccountIdsByAttribute(AccountPropertyType attribute, String value) {
        
        if (StringUtils.hasText(value)) {
            
            switch (attribute) {
            case ATTR_ACCOUNT_NAME:
                return accountEntityRepository.findIdsByName(value);
            case ATTR_COMP_REG_NO:
                return accountEntityRepository.findIdsByCompRegNo(value);
            case ATTR_CONTACT_EMAIL:
                return accountEntityRepository.findIdsByContactEmail(value.toLowerCase());
            case ATTR_ACCOUNT_NUMBER:
                Optional<AccountEntity> account = accountEntityRepository.findById(value);
                if (account.isPresent()) {
                    return Arrays.asList(value);
                }
            default:
                break;
            }
        }
        
        return new ArrayList<>();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public boolean hasCredentials(String identityId) {
        return !credentialRepository.findCredentialsByUserIdAndType(identityId, "password").isEmpty();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public String getMasterGroupId(String masterGroup) {
        return groupEntityRepository.findGroupIdsByName(masterGroup, realm).stream()
            .findFirst().orElseThrow(() -> new AccountNotFoundException("NAME: " + masterGroup));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<String> findBinaryMaskForRole(String roleId) {
        return roleAttributeRepository.findBinaryMaskForRole(roleId).stream().findFirst();
    }
}
