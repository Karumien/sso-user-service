package com.karumien.cloud.sso.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.validation.constraints.Size;

import org.springframework.util.StringUtils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ValidationUtil {
	
	public static final String DEFAULT_LOCALE = "en";
    public static final Set<String> SUPPORTED_LOCALES = Set.of("at","bg","cs","de","dk","ee",
    		DEFAULT_LOCALE,"es","fr","hu","it","lt","lv","nl","pl","pt","ro","ru","si","sk","sr","sv","tr");

    private static final Map<String, String> FIXED_LOCALES = new HashMap<>();
    static {
    
    	//de-at:at, da:dk, et:ee, sl:si 
    	FIXED_LOCALES.put("de_AT", "at");
    	FIXED_LOCALES.put("da", "sk");
    	FIXED_LOCALES.put("et", "ee");
    	FIXED_LOCALES.put("sl", "si");
    }
    
    public static String validateAndFixLocale(@Size(max = 10) String locale) {
    	
    	if (!StringUtils.hasText(locale)) {
			return DEFAULT_LOCALE;
		}
    	
    	// T012-315, TODO: T012-103
    	if (FIXED_LOCALES.containsKey(locale)) {
    		locale = FIXED_LOCALES.get(locale);
    	}
    	
    	if (!SUPPORTED_LOCALES.contains(locale)) {
			return DEFAULT_LOCALE;
		}

    	return locale;
	}
}
