package de.reneruck.inear2.db;

public class DbConfigs {

	public static final String TABLE_BOOKMARKS = "Bookmarks";
	public static final String FIELD_BOOKMARK_ID = "id";
	public static final String FIELD_TRACK = "trackNr";
    public static final String FIELD_PLAYBACK_POS = "playbackPos";

    public static final String TABLE_AUDIOBOOKS = "Audiobooks";
    public static final String FIELD_BOOK_ID = "id";
    public static final String FIELD_BOOK_NAME = "audiobookName";

    public static final String TABLE_TRACKS = "Tracks";
    public static final String FIELD_TRACK_ID = "id";
	public static final String FIELD_TRACK_LOCATION = "location";
	public static final String FIELD_TRACK_LENGTH = "length";
	public static final String FIELD_AUDIOBOOK_ID ="audiobook_id";
    public static final String FIELD_TRACK_POS ="pos";
    public static final String FIELD_BOOK_PATH = "path";

    public static String databaseName = "inear.db";
	public static int databaseVersion = 2;
	
}
