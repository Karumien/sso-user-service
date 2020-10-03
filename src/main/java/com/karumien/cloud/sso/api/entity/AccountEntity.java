/*
 * Copyright (c) 2019-2029 Karumien s.r.o.
 *
 * Karumien s.r.o. is not responsible for defects arising from 
 * unauthorized changes to the source code.
 */
package com.karumien.cloud.sso.api.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Group's Attribute Entity.
 *
 * @author <a href="miroslav.svoboda@karumien.com">Miroslav Svoboda</a>
 * @since 1.0, 4. 10. 2019 10:43:25 
 */
@Entity
@Table(name = "PLUGIN_ACCOUNT")
@Data
@EqualsAndHashCode(of = "accountNumber")
public class AccountEntity implements Serializable {
    
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "ID", length = 36)
    private String accountNumber;

    @Column(name = "NAME", length = 255)
    private String name;

    @Column(name = "COMP_REG_NO", length = 255)
    private String compRegNo;

    @Column(name = "CONTACT_EMAIL", length = 255)
    private String contactEmail;

    @Column(name = "NOTE", length = 1024)
    private String note;
    
    @Column(name = "LOCALE", length = 50)
    private String locale;
    
    @Column(name = "CREATED")
    private LocalDateTime created;
    
    @Column(name = "UPDATED")
    private LocalDateTime updated;
    
    @PrePersist 
    public void beforeCreate() {
    	created = LocalDateTime.now();
    	updated = LocalDateTime.now();
    }
    
    @PreUpdate 
    public void beforeUpdate() {
    	updated = LocalDateTime.now();
    }

}

