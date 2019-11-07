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

import com.karumien.cloud.sso.api.entity.GroupAttribute;

/**
 * Repository for operations on {@link GroupAttribute}.
 *
 * @author <a href="miroslav.svoboda@karumien.com">Miroslav Svoboda</a>
 * @since 1.0, 6. 11. 2019 11:20:57
 */
@Repository
public interface GroupAttributeRepository extends JpaSpecificationExecutor<GroupAttribute>, JpaRepository<GroupAttribute, String> {

    /**
     * Search Users by UserAttribute name and value.
     * 
     * @param attribute
     *            name ie accountNumber
     * @param value
     *            specific value of attribute
     * @return {@link List} of User's IDs
     */
    @Query("select ga.groupId from GroupAttribute ga where ga.name = :attribute and ga.value = :value")
    List<String> findGroupIdsByAttribute(@Param("attribute") String attribute, @Param("value") String value);

}
