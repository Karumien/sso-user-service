/*
 * Copyright (c) 2019-2029 Karumien s.r.o.
 *
 * Karumien s.r.o. is not responsible for defects arising from
 * unauthorized changes to the source code.
 */
package com.karumien.cloud.sso.api.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.karumien.cloud.sso.api.entity.UserAttribute;

/**
 * Repository for operations on {@link UserAttribute}.
 *
 * @author <a href="miroslav.svoboda@karumien.com">Miroslav Svoboda</a>
 * @since 1.0, 4. 10. 2019 11:20:57
 */
@Repository
public interface UserAttributeRepository extends JpaSpecificationExecutor<UserAttribute>, JpaRepository<UserAttribute, String> {

    /**
     * Search Users by UserAttribute name and value.
     * 
     * @param attribute
     *            name ie contactNumber
     * @param value
     *            specific value of attribute
     * @return {@link List} of User's IDs
     */
    @Query("select ua.user.id from UserAttribute ua where ua.name = :attribute and ua.value = :value")
    List<String> findUserIdsByAttribute(@Param("attribute") String attribute, @Param("value") String value);
    
    /**
     * Search Users by UserAttribute name and value.
     * 
     * @param attribute
     *            name ie contactNumber
     * @param value
     *            specific value of attribute
     * @param driver
     * 			  specific query for driver
     * @return {@link List} of User's IDs
     */
    @Query("select ua.user.id from UserAttribute ua where ua.name = :attribute and ua.value = :value and (:driver = true and ua.user.username like '%driver\\_%' or :driver = false and ua.user.username not like '%driver\\_%')")
    List<String> findUserIdsByAttribute(@Param("attribute") String attribute, @Param("value") String value, @Param("driver") boolean driver);

    /**
     * Search Users by UserAttribute name and value.
     * 
     * @param attribute
     *            name ie contactNumber
     * @param userId
     *            specific user by id
     * @return {@link List} of attribute values
     */
    @Query("select ua.value from UserAttribute ua where ua.name = :attribute and ua.user.id = :userId")
    List<String> findValueByAttributeOfUserId(@Param("attribute") String attribute, @Param("userId") String userId);

}
