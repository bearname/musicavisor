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

    public static boolean contains(String type) {

        for (SearchType searchType : SearchType.values()) {
            System.out.println(searchType.name().toLowerCase() + "'" + type + "'");
            System.out.println(searchType.name() == type);
            if (searchType.name() == type.toUpperCase()) {
                return true;
            }

//            if (searchType.getValue().equals(type)) {
//                return true;
//            }
        }

        return false;
    }
}
