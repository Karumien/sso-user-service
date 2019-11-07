/*
 * Copyright (c) 2019-2029 Karumien s.r.o.
 *
 * Karumien s.r.o. is not responsible for defects arising from 
 * unauthorized changes to the source code.
 */
package com.karumien.cloud.sso.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.karumien.cloud.sso.api.repository.GroupAttributeRepository;
import com.karumien.cloud.sso.api.repository.UserAttributeRepository;

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
    private GroupAttributeRepository groupAttributeRepository;

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<String> findUserIdsByAttribute(String attribute, String value) {
        return userAttributeRepository.findUserIdsByAttribute(attribute, value);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<String> findGroupIdsByAttribute(String attribute, String value) {
        return groupAttributeRepository.findGroupIdsByAttribute(attribute, value);
    }
}
