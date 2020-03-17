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
import javax.persistence.IdClass;
import javax.persistence.Table;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Activated Module on Entity.
 *
 * @author <a href="miroslav.svoboda@karumien.com">Miroslav Svoboda</a>
 * @since 1.0, 4. 10. 2019 10:43:25 
 */
@Entity
@Table(name = "PLUGIN_ACCOUNT_MODULE")
@Data
@EqualsAndHashCode(of = { "moduleId", "accountNumber" })
@IdClass(AccountModuleID.class)
public class AccountModule implements Serializable {
    
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "MODULE_ID", length = 36)
    private String moduleId;

    @Id
    @Column(name = "ACCOUNT_ID", length = 36)
    private String accountNumber;
  
}

