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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.karumien.cloud.sso.api.entity.UserEntity;
import com.karumien.cloud.sso.api.model.AccountPropertyType;
import com.karumien.cloud.sso.api.model.IdentityPropertyType;
import com.karumien.cloud.sso.api.repository.GroupAttributeRepository;
import com.karumien.cloud.sso.api.repository.UserAttributeRepository;
import com.karumien.cloud.sso.api.repository.UserEntityRepository;

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
    private GroupAttributeRepository groupAttributeRepository;

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

        if (attribute == IdentityPropertyType.ATTR_GLOBAL_EMAIL) {
            value = value.toLowerCase();
        }
        
        if (attribute == IdentityPropertyType.USERNAME) {
            return userEntityRepository.findUserIdsByUsername(realm, value.toLowerCase());
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
    public List<String> findGroupIdsByAttribute(AccountPropertyType attribute, String value) {

        if (attribute == AccountPropertyType.ATTR_CONTACT_EMAIL) {
            value = value.toLowerCase();
        }

        return groupAttributeRepository.findGroupIdsByAttribute(attribute.getValue(), value);
    }
}
