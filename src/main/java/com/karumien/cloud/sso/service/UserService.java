/*
 * Copyright (c) 2019 Karumien s.r.o.
 *
 * Karumien s.r.o. is not responsible for defects arising from
 * unauthorized changes to the source code.
 */
package com.karumien.cloud.sso.service;

import javax.validation.Valid;

import com.karumien.cloud.sso.api.model.UserBaseInfoDTO;

/**
 * Service for managing {@link PerformanceData} entity.
 *
 * @author <a href="viliam.litavec@karumien.com">Viliam Litavec</a>
 * @since 1.0, 10. 7. 2019 22:07:27
 */
public interface UserService {

    /**
     * Create user in target SSO.
     * 
     * @param user
     *            user specification
     * @return {@link UserBaseInfoDTO} changes after entity save
     */
    UserBaseInfoDTO createUser(@Valid UserBaseInfoDTO user);

    /**
     * Delete user in target SSO.
     * 
     * @param id unique user id
     */
    void deleteUser(String id);
}
