package ru.mikushov.musicadvisor.service;

public interface AuthenticationService {
    void authenticate();
    boolean isAuthenticated();
    String getAccessToken();
}
