package ru.mikushov.musicadvisor.repository;

import ru.mikushov.musicadvisor.model.AccessToken;

import java.util.ArrayList;
import java.util.List;

public class AccessTokenRepository {
    private final List<AccessToken> accessTokenList = new ArrayList<>();

    public void addAccessToken(AccessToken token) {
        accessTokenList.add(token);
    }
}
