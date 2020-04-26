/*
 * Copyright (c) 2019-2029 Karumien s.r.o.
 *
 * Karumien s.r.o. is not responsible for defects arising from 
 * unauthorized changes to the source code.
 */
package com.karumien.cloud.sso.util;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;

/**
 * {@link PageRequest} and {@link Pageable} Utils.
 * @author <a href="viliam.litavec@karumien.com">Viliam Litavec</a>
 * @since 1.0, 26. 4. 2020 19:20:35 
 */
public class PageableUtils {
    
    public static Pageable getRequest(Integer page, Integer size, List<String> sort, List<String> available) {

        if (page == null) {
            page = 0;
        }
        
        if (size == null) {
            size = 10;
        }
        
        if (sort == null) {
            sort = new ArrayList<>();
        }
        
        List<Sort.Order> orders = new ArrayList<>();
        for (String orderBy : sort) {
            
            boolean desc = orderBy.endsWith(",DESC");
            String property = orderBy.indexOf(',') > 0 ? orderBy.substring(0, orderBy.indexOf(',')) : orderBy;

            if (available.contains(property)) {
                orders.add(desc ? Order.desc(property) : Order.asc(property));
            }
        }
        
        
        return PageRequest.of(page, size, Sort.by(orders));
    }

}
