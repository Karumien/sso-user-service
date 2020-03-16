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

import com.karumien.cloud.sso.api.entity.GroupEntity;

/**
 * Repository for operations on {@link GroupEntity}.
 *
 * @author <a href="miroslav.svoboda@karumien.com">Miroslav Svoboda</a>
 * @since 1.0, 6. 11. 2019 11:20:57
 */
@Repository
public interface GroupEntityRepository extends JpaSpecificationExecutor<GroupEntity>, JpaRepository<GroupEntity, String> {

    /**
     * Search master groups by name
     * @param name grou name
     * @param realm realm name
     * @return {@link List} of {@link String} group IDs
     */
    @Query("select ge.id from GroupEntity ge where ge.name = :name and ge.realm = :realm and ge.parent is null")
    List<String> findGroupIdsByName(@Param("name") String name, @Param("realm") String realm);

}
