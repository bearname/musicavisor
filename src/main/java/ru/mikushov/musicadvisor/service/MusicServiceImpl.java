package ru.mikushov.musicadvisor.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import ru.mikushov.musicadvisor.controller.Command;
import ru.mikushov.musicadvisor.model.Album;
import ru.mikushov.musicadvisor.model.Artist;
import ru.mikushov.musicadvisor.model.Music;
import ru.mikushov.musicadvisor.repository.MusicRepository;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MusicServiceImpl implements MusicService {

    private final Map<String, String> apiRouter;
    private final MusicRepository featuredMusicRepository;

    public MusicServiceImpl(Map<String, String> apiRouter, MusicRepository featuredMusicRepository) {
        this.apiRouter = apiRouter;
        this.featuredMusicRepository = featuredMusicRepository;
    }

    public List<Album> getMusicAlbum(String accessToken)  {
        try {
            HttpResponse<String> response = getSpotifyInformation(Command.NEW, accessToken );
            JsonObject asJsonObject = JsonParser.parseString(response.body()).getAsJsonObject();
            JsonArray albums = asJsonObject.get("albums").getAsJsonObject().get("items").getAsJsonArray();

            return fillAlbumList(albums);
        } catch (Exception exception) {
            return new ArrayList<>();
        }
    }

    private HttpResponse<String> getSpotifyInformation(String command, String accessToken) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newBuilder().build();
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .header("Authorization", "Bearer " + accessToken)
                .uri(URI.create(apiRouter.get(command)))
                .GET()
                .build();
        return client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
//        return sendRequest(client, httpRequest);
    }

    private List<Album> fillAlbumList(JsonArray albums) {
        List<Album> albumList = new ArrayList<>();

        for (JsonElement albumElement : albums) {
            JsonObject albumObject = albumElement.getAsJsonObject();

            String name = albumObject.get("name").getAsString();
            JsonArray artists = albumObject.get("artists").getAsJsonArray();
            String albumUrl = albumObject.get("external_urls").getAsJsonObject().get("spotify").getAsString();
            Album album = new Album(name, fillArtistList(artists), albumUrl);
            System.out.println(album);
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

    public List<Music> getFeaturedMusic(String accessToken) {
        try {
            fillFeaturedMusicRepository(accessToken);
            return featuredMusicRepository.getAll();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();

            return new ArrayList<>();
        }
    }

    private void fillFeaturedMusicRepository(String accessToken) throws IOException, InterruptedException {
        HttpResponse<String> response = getSpotifyInformation(Command.FEATURED, accessToken);
        JsonObject asJsonObject = JsonParser.parseString(response.body()).getAsJsonObject();
        JsonArray musics = asJsonObject.get("playlists").getAsJsonObject().get("items").getAsJsonArray();

        parseJson(musics, featuredMusicRepository);
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
