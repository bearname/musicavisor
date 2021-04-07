package ru.mikushov.musicadvisor.service;

import com.google.gson.Gson;
import ru.mikushov.musicadvisor.model.Album;
import ru.mikushov.musicadvisor.model.Music;

import java.util.ArrayList;
import java.util.List;

public class JsonExportService implements ExportService {
    private final MusicService musicService;

    public JsonExportService(MusicService musicService) {
        this.musicService = musicService;
    }

    @Override
    public void export() {
        System.out.println("export");
        Gson gson = new Gson();
        List<Album> newReleasesMusic = musicService.getNewReleasesMusic();
        System.err.println(gson.toJson(newReleasesMusic));
//        newReleasesMusic.forEach(System.out::println);
    }

    @Override
    public void load(String filename) {

    }
}
