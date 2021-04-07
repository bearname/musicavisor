package ru.mikushov.musicadvisor.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import ru.mikushov.musicadvisor.controller.Command;
import ru.mikushov.musicadvisor.model.AlbumCategory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

public class MusicServiceImpl {

    private final Map<String, String> apiRouter;
    private final AuthenticationService authenticationService;

    public MusicServiceImpl(Map<String, String> apiRouter, AuthenticationService authenticationService) {
        this.apiRouter = apiRouter;
        this.authenticationService = authenticationService;
    }



}
