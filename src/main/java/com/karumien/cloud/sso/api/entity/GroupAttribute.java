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
 * Group's Attribute Entity.
 *
 * @author <a href="miroslav.svoboda@karumien.com">Miroslav Svoboda</a>
 * @since 1.0, 4. 10. 2019 10:43:25 
 */
@Entity
@Table(name = "GROUP_ATTRIBUTE")
@Data
@EqualsAndHashCode(of = "groupId")
@Immutable
public class GroupAttribute implements Serializable {
    
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "GROUP_ID", length = 36)
    private String groupId;

    @Column(name = "NAME", length = 255)
    private String name;

    @Column(name = "VALUE", length = 255)
    private String value;

}

