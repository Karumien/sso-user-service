/*
 * Copyright (c) 2019-2029 Karumien s.r.o.
 *
 * Karumien s.r.o. is not responsible for defects arising from 
 * unauthorized changes to the source code.
 */
package com.karumien.cloud.sso.api.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.springframework.data.annotation.Immutable;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * User Entity.
 *
 * @author <a href="miroslav.svoboda@karumien.com">Miroslav Svoboda</a>
 * @since 1.0, 9. 12. 2019 17:22:25 
 */
@Entity
@Table(name = "CLIENT")
@Data
@EqualsAndHashCode(of = "id")
@Immutable
public class ClientEntity implements Serializable {
    
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "ID", length = 36)
    private String id;

    @Column(name = "CLIENT_ID", length = 255)
    private String clientId;
    
    @Column(name = "REALM_ID", length = 255)
    private String realm;
        
    @Column(name = "ROOT_URL", length = 255)
    private String rootUrl;

    @Column(name = "BASE_URL", length = 255)
    private String baseUrl;

    public String getRedirectUri() {

    	if (baseUrl == null) {
    		return rootUrl;
    	}
    	
    	if (rootUrl == null) {
    		return baseUrl;
    	}
    	
    	if (baseUrl.startsWith("/")) {
    		return rootUrl + baseUrl;
    	}
    	
    	return baseUrl;
    }
}

