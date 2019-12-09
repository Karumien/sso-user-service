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
@Table(name = "USER_ENTITY")
@Data
@EqualsAndHashCode(of = "userId")
@Immutable
public class UserEntity implements Serializable {
    
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "ID", length = 36)
    private String userId;

    @Column(name = "FIRST_NAME", length = 255)
    private String firstName;

    @Column(name = "LAST_NAME", length = 255)
    private String lastName;

    @Column(name = "USERNAME", length = 255)
    private String username;
    
    @Column(name = "REALM_ID", length = 255)
    private String realm;
    
    @Column(name = "EMAIL", length = 255)
    private String email;

    @Column(name = "EMAIL_VERIFIED")
    private Boolean emailVerified;
    
}

