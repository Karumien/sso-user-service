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

import com.karumien.cloud.sso.api.entity.RoleAttribute;

/**
 * Repository for operations on {@link RoleAttribute}.
 *
 * @author <a href="miroslav.svoboda@karumien.com">Miroslav Svoboda</a>
 * @since 1.0, 4. 10. 2019 11:20:57
 */
@Repository
public interface RoleAttributeRepository extends JpaSpecificationExecutor<RoleAttribute>, JpaRepository<RoleAttribute, String> {

    /**
     * Search Users by RoleAttribute name and value.
     * 
     * @param roleId
     *            specific Role ID
     * @return {@link List} of Roles attribute value
     */
    @Query("select ua.value from RoleAttribute ua where ua.name = 'binaryMask' and ua.roleId = :roleId")
    List<String> findBinaryMaskForRole(@Param("roleId") String roleId);

}
