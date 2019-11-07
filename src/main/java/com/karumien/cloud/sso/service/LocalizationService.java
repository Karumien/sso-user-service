/*
 * Copyright (c) 2019-2029 Karumien s.r.o.
 *
 * Karumien s.r.o. is not responsible for defects arising from
 * unauthorized changes to the source code.
 */
package com.karumien.cloud.sso.service;

import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Localization Service for translation support.
 * 
 * @author <a href="miroslav.svoboda@karumien.com">Miroslav Svoboda</a>
 * @since 1.0, 4. 10. 2019 11:31:52
 */
public interface LocalizationService {

    String ATTR_TRANSLATION = "translation";

    /**
     * Translate scenario uses custom attributes of entity, based on locale searched in i18n/attributes or uses
     * defaultTranslate.
     * 
     * @param localeKey
     *            key in i18n
     * @param attributes
     *            entity attributes
     * @param locale
     *            locale of user
     * @param defaultTranslate
     *            default translated value if not found in i18n/attributes
     * @return {@link String} translated text
     */
    String translate(String localeKey, Map<String, List<String>> attributes, Locale locale, String defaultTranslate);


}
