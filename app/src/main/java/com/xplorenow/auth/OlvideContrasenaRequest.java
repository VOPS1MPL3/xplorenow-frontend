package com.xplorenow.auth;

public class OlvideContrasenaRequest {
    private String email;

    public OlvideContrasenaRequest(String email) {
        this.email = email;
    }

    public String getEmail() { return email; }
}
