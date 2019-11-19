/*
 * Copyright (c) 2019-2029 Karumien s.r.o.
 *
 * Karumien s.r.o. is not responsible for defects arising from
 * unauthorized changes to the source code.
 */
package com.karumien.cloud.sso.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.util.CollectionUtils;

/**
 * Search Service for direct immutable access to KeyCloak DB for performance searching.
 * 
 * @author <a href="miroslav.svoboda@karumien.com">Miroslav Svoboda</a>
 * @since 1.0, 4. 10. 2019 11:31:52
 */
public interface SearchService {

    /**
     * Search Users by UserAttribute name and value.
     * 
     * @param attribute
     *            attribute name ie. contactNumber
     * @param value
     *            specific value of attribute
     * @return {@link List} of User's IDs
     */
    List<String> findUserIdsByAttribute(String attribute, String value);

    /**
     * Search Groups by UserAttribute name and value.
     * 
     * @param attribute
     *            attribute name ie. contactNumber
     * @param value
     *            specific value of attribute
     * @return {@link List} of User's IDs
     */
    List<String> findGroupIdsByAttribute(String attribute, String value);
    
    default Optional<String> getSimpleAttribute(Map<String, List<String>> attributes, String attrName) {
        if (CollectionUtils.isEmpty(attributes) || CollectionUtils.isEmpty(attributes.get(attrName))) {
            return Optional.empty();
        }
        return attributes.get(attrName).stream().findFirst();
    }
    
    default boolean containsAttribute(Map<String, List<String>> attributes, String attrName, Object value) {        
        if (CollectionUtils.isEmpty(attributes) || CollectionUtils.isEmpty(attributes.get(attrName))) {
            return false;
        }
        return attributes.get(attrName).contains(value);
    }
}
