package ru.mikushov.musicadvisor.infrostructure;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import ru.mikushov.musicadvisor.controller.Command;
import ru.mikushov.musicadvisor.model.Album;
import ru.mikushov.musicadvisor.model.AlbumCategory;
import ru.mikushov.musicadvisor.model.Artist;
import ru.mikushov.musicadvisor.model.Music;
import ru.mikushov.musicadvisor.service.AuthenticationService;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static ru.mikushov.musicadvisor.controller.Command.PLAYLISTS;

public class SpotifyClient {
    private final Map<String, String> apiRouter;
    private final AuthenticationService authenticationService;

    public SpotifyClient(Map<String, String> apiRouter, AuthenticationService authenticationService) {
        this.apiRouter = apiRouter;
        this.authenticationService = authenticationService;
        authenticationService.authenticate();
    }

    public List<Album> getNewReleaseMusic() {
        try {
            JsonArray albums = getJsonElements(Command.NEW, "albums");

            return fillAlbumList(albums);
        } catch (Exception exception) {
            exception.printStackTrace();
            return new ArrayList<>();
        }
    }

    public List<Music> getFeaturedPlaylists() {
        try {
            JsonArray playlists = getJsonElements(Command.FEATURED, "playlists");
            return parseJson(playlists);
        } catch (Exception exception) {
            exception.printStackTrace();
            return new ArrayList<>();
        }
    }

    public List<AlbumCategory> getAlbumCategories() {
        List<AlbumCategory> result = new ArrayList<>();
        try {
            JsonArray categories = getJsonElements(Command.CATEGORIES, "categories");
            for (JsonElement category : categories) {
                JsonObject album = category.getAsJsonObject();

                String id = album.get("id").getAsString();
                String name = album.get("name").getAsString();
                result.add(new AlbumCategory(id, name));
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }

        return result;
    }

    public List<Music> getMusicByCategory(AlbumCategory albumCategory) {
        try {
            updateApiRoutes(albumCategory.getId());
            JsonArray playlists = getJsonElements(PLAYLISTS, "playlists");
            return parseJson(playlists);
        } catch (Exception exception) {
            exception.printStackTrace();
            return new ArrayList<>();
        }
    }

    private List<Music> parseJson(JsonArray playlists) {
        List<Music> result = new ArrayList<>();
        for (JsonElement musicJsonElement : playlists) {
            JsonObject musicJsonObject = musicJsonElement.getAsJsonObject();

            String id = musicJsonObject.get("id").getAsString();
            String name = musicJsonObject.get("name").getAsString();
            String url = musicJsonObject.get("external_urls").getAsJsonObject().get("spotify").getAsString();
            result.add(new Music(id, name, url));
        }

        return result;
    }

    private JsonArray getJsonElements(String categories, String categories2) throws IOException, InterruptedException {
        HttpResponse<String> response = request(categories, authenticationService.getAccessToken());
        JsonObject asJsonObject = JsonParser.parseString(response.body()).getAsJsonObject();

        return asJsonObject.get(categories2).getAsJsonObject().get("items").getAsJsonArray();
    }

    private List<Album> fillAlbumList(JsonArray albums) {
        List<Album> albumList = new ArrayList<>();

        for (JsonElement albumElement : albums) {
            JsonObject albumObject = albumElement.getAsJsonObject();

            String name = albumObject.get("name").getAsString();
            JsonArray artists = albumObject.get("artists").getAsJsonArray();
            String albumUrl = albumObject.get("external_urls").getAsJsonObject().get("spotify").getAsString();
            Album album = new Album(name, fillArtistList(artists), albumUrl);
//            System.out.println(album);
            albumList.add(album);
        }

        return albumList;
    }

    private List<Artist> fillArtistList(JsonArray artists) {
        List<Artist> artistList = new ArrayList<>();

        for (JsonElement artist : artists) {
            String artistName = artist.getAsJsonObject().get("name").toString();
            String artistId = artist.getAsJsonObject().get("id").toString();
            String artistUrlOnSpotify = artist.getAsJsonObject().get("external_urls").getAsJsonObject().get("spotify").getAsString();

            artistList.add(new Artist(artistId, artistName, artistUrlOnSpotify));
        }

        return artistList;
    }

    private HttpResponse<String> request(String command, String accessToken) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newBuilder().build();
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .header("Authorization", "Bearer " + accessToken)
                .uri(URI.create(apiRouter.get(command)))
                .GET()
                .build();

        return client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
    }

    private void updateApiRoutes(String albumCategoryId) {
        String url = "https://api.spotify.com/v1/browse/categories/" + albumCategoryId + "/playlists";
        apiRouter.put(PLAYLISTS, url);
    }
}
