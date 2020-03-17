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

import com.karumien.cloud.sso.api.entity.AccountModule;
import com.karumien.cloud.sso.api.entity.AccountModuleID;

/**
 * Repository for operations on {@link AccountModule}.
 *
 * @author <a href="miroslav.svoboda@karumien.com">Miroslav Svoboda</a>
 * @since 1.0, 6. 11. 2019 11:20:57
 */
@Repository
public interface AccountModuleRepository extends JpaSpecificationExecutor<AccountModule>, JpaRepository<AccountModule, AccountModuleID> {

    @Query("select a.moduleId from AccountModule a where a.accountNumber = :accountNumber")
    List<String> findIdsByAccount(@Param("accountNumber") String accountNumber);

}
