/*
 * Copyright (c) 2019 Karumien s.r.o.
 *
 * Karumien s.r.o. is not responsible for defects arising from
 * unauthorized changes to the source code.
 */
package com.karumien.cloud.sso.service;

import com.karumien.cloud.sso.api.entity.RebirthEntity;

/**
 * Service provides scenarios for Rebirth's management.
 *
 * @author <a href="viliam.litavec@karumien.com">Viliam Litavec</a>
 * @since 1.0, 16. 5. 2020 22:07:27
 */
public interface RebirthService {
    RebirthEntity createRebirth(RebirthEntity rebirth);
    RebirthEntity getRebirth(String nav4Id);
}
