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

import com.karumien.cloud.sso.api.entity.ClientEntity;

/**
 * Repository for operations on {@link ClientRepository}.
 *
 * @author <a href="ivo.smajstrla@karumien.com">Ivo Smajstrla</a>
 * @since 1.0, 8. 12. 2019 19:11:25 
 */
@Repository
public interface ClientRepository extends JpaSpecificationExecutor<ClientEntity>, JpaRepository<ClientEntity, String> {
    
    /**
     * Search client by id and realm.
     * 
     * @param realm realm of client
     * @param clientId client ID
     * @return {@link List} of credential IDs
     */
    @Query("select ce.id from ClientEntity ce where ce.realm = :realm and ce.clientId = :clientId")
    Optional<ClientEntity> findClientByRealmAndClientId(@Param("realm") String realm, @Param("clientId") String clientId);

}
