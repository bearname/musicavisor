package ru.mikushov.musicadvisor.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import ru.mikushov.musicadvisor.model.Music;

import java.io.*;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class JsonExportService implements ExportService {
    private final MusicService musicService;
    private static final Type MUSIC_TYPE = new TypeToken<List<Music>>() {}.getType();

    public JsonExportService(MusicService musicService) {
        this.musicService = musicService;
    }

    @Override
    public void export() {
        System.out.println("export");
        List<Music> newReleasesMusic = musicService.getFeaturedMusicList();
        try {
            String exportFileName = getExportFileName();
            try (Writer writer1 = new FileWriter(exportFileName)) {
                Gson gson1 = new GsonBuilder()
                        .setPrettyPrinting()
                        .create();
                gson1.toJson(newReleasesMusic, writer1);
                System.out.println("Success exported to file: " + exportFileName);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void load(String filename) {
        try {
            Gson gson = new Gson();
            System.out.println(filename);
            JsonReader reader = new JsonReader(new FileReader(filename));
            List<Music> musicList = gson.fromJson(reader, MUSIC_TYPE);
            musicList.forEach(System.out::println);
            musicService.addFeaturedMusic(musicList);
            System.out.println("Success import to file: " + filename);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private String getExportFileName() {
        return "C:" + File.pathSeparator + "Users" + File.pathSeparator + "mikha" + File.pathSeparator + "Desktop" + File.pathSeparator + "github" + File.pathSeparator + "musicadvisor" + File.pathSeparator + "dump-" + new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(new Date()) + ".json";
    }
}
