package com.karumien.cloud.sso.service;

import com.karumien.cloud.sso.api.model.PasswordPolicy;

/**
 * Password Generator Service
 *
 * @author <a href="miroslav.svoboda@karumien.com">Miroslav Svoboda</a>
 * @since 1.0, 3. 10. 2019 17:18:05
 */
public interface PasswordGeneratorService {

    String generate(PasswordPolicy policy);

}
