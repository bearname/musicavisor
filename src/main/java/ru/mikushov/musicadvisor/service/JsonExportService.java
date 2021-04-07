package ru.mikushov.musicadvisor.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import ru.mikushov.musicadvisor.model.Album;
import ru.mikushov.musicadvisor.model.Music;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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
        try {
            try (Writer writer1 = new FileWriter(getExportFileName())) {
                Gson gson1 = new GsonBuilder()
                        .setPrettyPrinting()
                        .create();
                gson1.toJson(newReleasesMusic, writer1);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void load(String filename) {

    }

    private String getExportFileName() {
        return "C:\\Users\\mikha\\Desktop\\github\\musicadvisor\\dump-" + new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(new Date()) + ".json";
    }
}
