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

import com.karumien.cloud.sso.api.entity.UserEntity;

/**
 * Repository for operations on {@link UserEntity}.
 *
 * @author <a href="miroslav.svoboda@karumien.com">Miroslav Svoboda</a>
 * @since 1.0, 9. 12. 2019 19:11:25 
 */
@Repository
public interface UserEntityRepository extends JpaSpecificationExecutor<UserEntity>, JpaRepository<UserEntity, String> {

    /**
     * Search UserEntity by email.
     * 
     * @param realm
     *            for specific realm
     * @param email
     *            specific email
     * @return {@link List} of User's IDs
     */
    @Query("select ue.userId from UserEntity ue where ue.realm = :realm and ue.email = :email")
    List<String> findUserIdsByEmail(@Param("realm") String realm, @Param("email") String email);

    /**
     * Search UserEntity by username
     * 
     * @param realm
     *            for specific realm
     * @param username
     *            specific username
     * @return {@link List} of User's IDs
     */
    @Query("select ue.userId from UserEntity ue where ue.realm = :realm and ue.username = :username")
    List<String> findUserIdsByUsername(@Param("realm") String realm, @Param("username") String username);
}
