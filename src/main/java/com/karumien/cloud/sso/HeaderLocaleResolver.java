/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.karumien.cloud.sso;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import org.springframework.context.annotation.Bean;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;


/**
 * Prepare locale from X-LOCALE header.
 *
 * @author <a href="miroslav.svoboda@karumien.com">Miroslav Svoboda</a>
 * @since 1.0, 18. 9. 2019 2:22:20
 */
@Component
public class HeaderLocaleResolver extends AcceptHeaderLocaleResolver implements WebMvcConfigurer {

    private static final List<Locale> SUPPORTED_LOCALES = Arrays.asList(new Locale("en"), new Locale("sk"), new Locale("cs"), 
            new Locale("de"), new Locale("fr"));
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Locale resolveLocale(HttpServletRequest request) {
        String headerLang = request.getHeader("x-locale");
        
        if (!StringUtils.hasText(headerLang)) {
            headerLang = request.getLocale() != null ? request.getLocale().getLanguage() : null;
        } 
    
        if (headerLang != null && headerLang.contains("_")) {
            headerLang = headerLang.substring(0,2);
        };
        
        return !StringUtils.hasText(headerLang) || Locale.lookup(Locale.LanguageRange.parse(headerLang), SUPPORTED_LOCALES) == null ?
                Locale.forLanguageTag("en") : 
                Locale.lookup(Locale.LanguageRange.parse(headerLang), SUPPORTED_LOCALES);
    }

    @Bean
    public ResourceBundleMessageSource messageSource() {
        ResourceBundleMessageSource rs = new ResourceBundleMessageSource();
        rs.setBasenames("i18n/messages");
        rs.setDefaultEncoding("UTF-8");
        rs.setUseCodeAsDefaultMessage(true);
        return rs;
    }

    @Bean
    public LocaleResolver localeResolver() {
        HeaderLocaleResolver headerLocaleResolver = new HeaderLocaleResolver();
        headerLocaleResolver.setDefaultLocale(Locale.forLanguageTag("en"));
        headerLocaleResolver.setSupportedLocales(SUPPORTED_LOCALES);
        return headerLocaleResolver;
    }
}
