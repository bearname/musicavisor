package ru.mikushov.musicadvisor.model;

public enum SearchType {
    ARTIST("artist"),
    ALBUM("album"),
    PLAYLIST("playlist"),
    TRACK("track");

    private final String value;

    SearchType(String value) {
        this.value= value;
    }

    public String getValue() {
        return value;
    }

    public static boolean contains(String s)
    {
        for(SearchType choice:values())
            if (choice.name().equals(s))
                return true;
        return false;
    }
}
