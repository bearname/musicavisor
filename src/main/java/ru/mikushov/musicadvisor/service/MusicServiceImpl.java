package ru.mikushov.musicadvisor.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import ru.mikushov.musicadvisor.controller.Command;
import ru.mikushov.musicadvisor.model.Album;
import ru.mikushov.musicadvisor.model.AlbumCategory;
import ru.mikushov.musicadvisor.model.Artist;
import ru.mikushov.musicadvisor.model.Music;
import ru.mikushov.musicadvisor.repository.AlbumCategoryRepository;
import ru.mikushov.musicadvisor.repository.MemoryMusicRepository;
import ru.mikushov.musicadvisor.repository.MusicRepository;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static ru.mikushov.musicadvisor.controller.Command.PLAYLISTS;

public class MusicServiceImpl implements MusicService {

    private final Map<String, String> apiRouter;
    private final MusicRepository featuredMusicRepository;
    private final AlbumCategoryRepository albumCategoryRepository;
    private final MemoryMusicRepository categoryMusicRepository;

    public MusicServiceImpl(Map<String, String> apiRouter, MusicRepository featuredMusicRepository, AlbumCategoryRepository albumCategoryRepository, MemoryMusicRepository categoryMusicRepository) {
        this.apiRouter = apiRouter;
        this.featuredMusicRepository = featuredMusicRepository;
        this.albumCategoryRepository = albumCategoryRepository;
        this.categoryMusicRepository = categoryMusicRepository;
    }

    public List<Album> getMusicAlbum(String accessToken) {
        try {
            HttpResponse<String> response = getSpotifyInformation(Command.NEW, accessToken);
            JsonObject asJsonObject = JsonParser.parseString(response.body()).getAsJsonObject();
            JsonArray albums = asJsonObject.get("albums").getAsJsonObject().get("items").getAsJsonArray();

            return fillAlbumList(albums);
        } catch (Exception exception) {
            return new ArrayList<>();
        }
    }

    public List<Music> getFeaturedMusicList(String accessToken) {
        try {
            fillFeaturedMusicRepository(accessToken);
            return featuredMusicRepository.getAll();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();

            return new ArrayList<>();
        }
    }

    public List<AlbumCategory> getAlbumCategoryList(String accessToken) {
        HttpResponse<String> response;
        try {
            response = getSpotifyInformation(Command.CATEGORIES, accessToken);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }

        System.out.println(response.body());
        JsonObject asJsonObject = JsonParser.parseString(response.body()).getAsJsonObject();
        JsonArray categories = asJsonObject.get("categories").getAsJsonObject().get("items").getAsJsonArray();

        for (JsonElement category : categories) {

            JsonObject album = category.getAsJsonObject();

            String id = album.get("id").getAsString();
            String name = album.get("name").getAsString();
            albumCategoryRepository.add(new AlbumCategory(id, name));
        }

        return albumCategoryRepository.getAll();
    }

    public List<Music> getMusicByCategoryName(String categoryName, String accessToken) {
        final var albumCategory = albumCategoryRepository.findByName(categoryName);
        List<Music> musicList = new ArrayList<>();
        if (albumCategory != null) {
            try {
                updateApiRoutes(albumCategory.getId());
                HttpResponse<String> response = getSpotifyInformation(PLAYLISTS, accessToken);
                fillCategoryMusicRepository(response);
                musicList = categoryMusicRepository.getAll();
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }

        return musicList;
    }

    private HttpResponse<String> getSpotifyInformation(String command, String accessToken) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newBuilder().build();
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .header("Authorization", "Bearer " + accessToken)
                .uri(URI.create(apiRouter.get(command)))
                .GET()
                .build();

        return client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
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

    private void fillFeaturedMusicRepository(String accessToken) throws IOException, InterruptedException {
        HttpResponse<String> response = getSpotifyInformation(Command.FEATURED, accessToken);
        JsonObject asJsonObject = JsonParser.parseString(response.body()).getAsJsonObject();
        JsonArray musics = asJsonObject.get("playlists").getAsJsonObject().get("items").getAsJsonArray();

        saveMusicFromJsonArray(musics, featuredMusicRepository);
    }

    private void saveMusicFromJsonArray(JsonArray musics, MusicRepository musicRepository) {
        for (JsonElement musicJsonElement : musics) {
            JsonObject musicJsonObject = musicJsonElement.getAsJsonObject();

            String id = musicJsonObject.get("id").getAsString();
            String name = musicJsonObject.get("name").getAsString();
            String url = musicJsonObject.get("external_urls").getAsJsonObject().get("spotify").getAsString();
            musicRepository.add(new Music(id, name, url));
        }
    }

    private void fillCategoryMusicRepository(HttpResponse<String> response) {
        JsonObject asJsonObject = JsonParser.parseString(response.body()).getAsJsonObject();
        JsonArray musics = asJsonObject.get("playlists").getAsJsonObject().get("items").getAsJsonArray();
//        System.out.println(response.body());

        saveMusicFromJsonArray(musics, categoryMusicRepository);
    }

    private void updateApiRoutes(String albumCategoryId) {
        String url = "https://api.spotify.com/v1/browse/categories/" + albumCategoryId + "/playlists";
        apiRouter.put(PLAYLISTS, url);
    }
}
