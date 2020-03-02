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

import com.karumien.cloud.sso.api.entity.CredentialEntity;
import com.karumien.cloud.sso.api.entity.UserEntity;

/**
 * Repository for operations on {@link UserEntity}.
 *
 * @author <a href="miroslav.svoboda@karumien.com">Miroslav Svoboda</a>
 * @since 1.0, 9. 12. 2019 19:11:25 
 */
@Repository
public interface CredentialRepository extends JpaSpecificationExecutor<UserEntity>, JpaRepository<CredentialEntity, String> {
    
    /**
     * Search credentials by user id and type.
     * 
     * @param userId user primary key
     * @param type credential type
     * @return {@link List} of credential IDs
     */
    @Query("select ce.id from CredentialEntity ce where ce.userId = :userId and ce.type = :type")
    List<String> findCredentialsByUserIdAndType(@Param("userId") String userId, @Param("type") String type);

}
