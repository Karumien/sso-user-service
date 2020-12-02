package com.karumien.cloud.sso.util;

import java.util.Set;

import javax.validation.constraints.Size;

import org.springframework.util.StringUtils;

import com.karumien.cloud.sso.exceptions.UnsupportedLocaleException;

public class ValidationUtil {
    private final static Set<String> supportedLocales = Set.of("at","bg","cs","de","dk","ee","en","es","fr","hu","it","lt","lv","nl","pl","pt","ro","ru","si","sk","sr","sv","tr");

    public static void validateLocale(@Size(max = 50) String locale) throws UnsupportedLocaleException {
		if(!StringUtils.isEmpty(locale) && !supportedLocales.contains(locale.toLowerCase())) {
			throw new UnsupportedLocaleException(locale);
		}
	}
}
