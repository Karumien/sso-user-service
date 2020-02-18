package com.karumien.cloud.sso.api.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AccountPropertyType {

    ID("groupId"),
    NAME("name"),    
    ATTR_COMP_REG_NO("compRegNo"),
    ATTR_ACCOUNT_NAME("accountName"),
    ATTR_ACCOUNT_NUMBER("accountNumber"),
    ATTR_CONTACT_EMAIL("contactEmail"),
    ATTR_MODULE_ID("moduleId"),
    ATTR_RIGHT_GROUP_ID("groupId"),
    ATTR_SERVICE_ID("serviceId"),
    ATTR_NOTE("note"),
    ATTR_BUSINESS_PRIORITY("businessPriority");

    private String value;

}
