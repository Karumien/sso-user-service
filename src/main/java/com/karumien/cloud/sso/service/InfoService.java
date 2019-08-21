/*
 * Copyright (c) 2019 Karumien s.r.o.
 *
 * Karumien s.r.o. is not responsible for defects arising from
 * unauthorized changes to the source code.
 */
package com.karumien.cloud.sso.service;

import com.karumien.cloud.sso.api.model.VersionInfo;

/**
 * Service provides informations from Manifest.MF file.
 *
 * @author <a href="miroslav.svoboda@karumien.com">Miroslav Svoboda</a>
 * @since 1.0, 19. 8. 2019 12:58:57
 */
public interface InfoService {

    String IMPL_TITLE = "Implementation-Title";
    String IMPL_VERSION = "Implementation-Version";
    
    VersionInfo getVersionInfo();
    
}
