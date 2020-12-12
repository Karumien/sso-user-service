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
import javax.persistence.PostLoad;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.springframework.data.domain.Persistable;

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
public class AccountEntity implements Serializable, Persistable<String> {
    
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
    
    @Column(name = "CREATED", insertable = true, updatable = false)
    private LocalDateTime created = LocalDateTime.now();
   
    @Column(name = "UPDATED")
    private LocalDateTime updated;
   
    @Transient
    private boolean update;


	@Override
	public String getId() {
		return this.accountNumber;
	}

	@Override
	public boolean isNew() {
		return !this.update;
	}
	
	@PrePersist
    @PreUpdate
	@PostLoad
    void markUpdated() {
        this.update = true;
        this.updated = LocalDateTime.now();
    }
}

