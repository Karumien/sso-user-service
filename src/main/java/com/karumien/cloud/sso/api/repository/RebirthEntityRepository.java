/*
 * Copyright (c) 2019-2029 Karumien s.r.o.
 *
 * Karumien s.r.o. is not responsible for defects arising from
 * unauthorized changes to the source code.
 */
package com.karumien.cloud.sso.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.karumien.cloud.sso.api.entity.RebirthEntity;

/**
 * Repository for operations on {@link RebirthEntity}.
 *
 * @author <a href="miroslav.svoboda@karumien.com">Miroslav Svoboda</a>
 * @since 1.0, 6. 11. 2019 11:20:57
 */
@Repository
public interface RebirthEntityRepository extends JpaSpecificationExecutor<RebirthEntity>, JpaRepository<RebirthEntity, String> {
}
