/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.karumien.cloud.sso.service;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

/**
 * Implementation of {@link LocalizationService} for translations.
 *
 * @author <a href="miroslav.svoboda@karumien.com">Miroslav Svoboda</a>
 * @since 1.0, 19. 8. 2019 12:58:57
 */
@Service
public class LocalizationServiceImpl implements LocalizationService {

    @Autowired
    private MessageSource messageSource;

    /**
     * {@inheritDoc}
     */
    @Override
    public String translate(String localeKey, Map<String, List<String>> attributes, Locale locale, String defaultTranslate) {
        String translate = null;
        if (attributes != null) {
            List<String> translates = attributes.get(ATTR_TRANSLATION + "[" + locale.getLanguage() + "]");
            if (!CollectionUtils.isEmpty(translates)) {
                translate = translates.get(0);
            }
            if (translate == null) {
                translates = attributes.get(ATTR_TRANSLATION);
                if (!CollectionUtils.isEmpty(translates)) {
                    translate = translates.get(0);
                }
            }
        }
        if (translate == null && localeKey != null) {
            translate = messageSource.getMessage(localeKey, null, locale);
            if (localeKey.equals(translate)) {
                translate = null;
            }
        }
        if (translate == null) {
            translate = defaultTranslate;
        }
        return translate;
    }

}
