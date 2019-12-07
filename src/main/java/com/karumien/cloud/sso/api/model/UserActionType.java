package com.karumien.cloud.sso.api.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum UserActionType {

    UPDATE_PASSWORD("update-password"), 
    VERIFY_EMAIL("verify-email"), 
    UPDATE_PROFILE("update-profie"), 
    CONFIGURE_TOTP("configure-totp");
    
    private String value;
}
