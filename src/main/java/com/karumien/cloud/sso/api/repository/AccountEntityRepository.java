/*
 * Copyright (c) 2019-2029 Karumien s.r.o.
 *
 * Karumien s.r.o. is not responsible for defects arising from
 * unauthorized changes to the source code.
 */
package com.karumien.cloud.sso.api.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.karumien.cloud.sso.api.entity.AccountEntity;

/**
 * Repository for operations on {@link AccountEntity}.
 *
 * @author <a href="miroslav.svoboda@karumien.com">Miroslav Svoboda</a>
 * @since 1.0, 6. 11. 2019 11:20:57
 */
@Repository
public interface AccountEntityRepository extends JpaSpecificationExecutor<AccountEntity>, JpaRepository<AccountEntity, String> {

    Optional<AccountEntity> findByCompRegNo(String compRegNo);

    @Query("select a.id from AccountEntity a where a.name like :value")
    List<String> findIdsByName(@Param("value") String value);

    @Query("select a.id from AccountEntity a where a.compRegNo like :value")
    List<String> findIdsByCompRegNo(@Param("value") String value);

    @Query("select a.id from AccountEntity a where a.contactEmail like :value")
    List<String> findIdsByContactEmail(@Param("value") String value);

    @Query(nativeQuery = true, value = "select locale from view_account_identities_locales where account_id = :accountNumber")
    List<String> getLocales(@Param("accountNumber") String accountNumber);
}
