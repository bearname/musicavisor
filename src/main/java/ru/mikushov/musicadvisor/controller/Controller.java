package ru.mikushov.musicadvisor.controller;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import ru.mikushov.musicadvisor.model.Music;
import ru.mikushov.musicadvisor.repository.MusicRepository;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public abstract class Controller {
    protected HttpResponse<String> sendRequest(HttpClient client, HttpRequest request) throws IOException, InterruptedException {
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    protected void parseJson(JsonArray musics, MusicRepository categoryMusicRepository) {
        for (JsonElement musicJsonElement : musics) {
            JsonObject musicJsonObject = musicJsonElement.getAsJsonObject();

            String id = musicJsonObject.get("id").getAsString();
            String name = musicJsonObject.get("name").getAsString();
            String url = musicJsonObject.get("external_urls").getAsJsonObject().get("spotify").getAsString();
            categoryMusicRepository.add(new Music(id, name, url));
        }
    }
}
