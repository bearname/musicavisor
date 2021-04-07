package ru.mikushov.musicadvisor.repository;

import ru.mikushov.musicadvisor.model.Music;

import java.util.List;

public interface MusicRepository {
    void add(Music music);

    List<Music> getAll();
}
