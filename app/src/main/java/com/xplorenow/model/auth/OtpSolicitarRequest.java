package com.xplorenow.model.auth;

public class OtpSolicitarRequest {
    private String email;

    public OtpSolicitarRequest(String email) {
        this.email = email;
    }

    public String getEmail() { return email; }
}
