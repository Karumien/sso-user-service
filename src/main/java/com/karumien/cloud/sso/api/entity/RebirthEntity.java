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

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Group's Attribute Entity.
 *
 * @author <a href="miroslav.svoboda@karumien.com">Miroslav Svoboda</a>
 * @since 1.0, 4. 10. 2019 10:43:25 
 */
@Entity
@Table(name = "PLUGIN_REBIRTH")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "nav4Id")
public class RebirthEntity implements Serializable {
    
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "ID", length = 36)
    private String nav4Id;

    @Column(name = "VALUE", length = 3000)
    private String value;

    @Column(name = "PASSWORD", length = 512)
    private String password;

}

