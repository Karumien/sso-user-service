package com.karumien.cloud.sso.api.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum IdentityPropertyType {

    ID("id"),
    USERNAME("username"),
    EMAIL("email"),
    ATTR_CONTACT_NUMBER("contactNumber"),
    ATTR_ACCOUNT_NUMBER("accountNumber"),
    ATTR_NOTE("note"),
    ATTR_DRIVER_PIN("driverPin"),
    ATTR_BINARY_RIGHTS("binaryRights"),
    ATTR_PHONE("phone"),
    ATTR_LOCALE("locale"),
    ATTR_NAV4ID("nav4Id"),
    ATTR_BUSINESS_PRIORITY("businessPriority"),
    ATTR_HAS_CREDENTIALS("hasCredentials"),
    ATTR_LAST_LOGIN("lastLogin"),
    ATTR_LAST_LOGOUT("lastLogout"),
    ATTR_LAST_LOGIN_ERROR("lastLoginError");

    private String value;

}
