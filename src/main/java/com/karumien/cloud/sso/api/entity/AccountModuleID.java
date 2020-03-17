/*
 * Copyright (c) 2019-2029 Karumien s.r.o.
 *
 * Karumien s.r.o. is not responsible for defects arising from 
 * unauthorized changes to the source code.
 */
package com.karumien.cloud.sso.api.entity;

import java.io.Serializable;

import javax.persistence.IdClass;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = { "moduleId", "accountNumber" })
@IdClass(AccountModuleID.class)
public class AccountModuleID implements Serializable {
    
    private static final long serialVersionUID = 1L;

    private String moduleId;
    private String accountNumber;
  
}

