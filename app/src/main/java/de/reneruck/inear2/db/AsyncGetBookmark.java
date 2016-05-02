package de.reneruck.inear2.db;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import de.reneruck.inear2.AudioBook;
import de.reneruck.inear2.Bookmark;
import de.reneruck.inear2.ListenableAsyncTask;

public class AsyncGetBookmark extends ListenableAsyncTask<AudioBook, Void, Bookmark> {

	private DatabaseManager databaseManager;
	
	public AsyncGetBookmark(DatabaseManager databaseManager) {
		this.databaseManager = databaseManager;
	}

	@Override
	public Bookmark doInBackground(AudioBook... params) {
		Bookmark bookmark = null;
		if(params != null && params.length > 0)
		{
			String bookTitle = params[0].getName();
			DatabaseHelper dbHelper = this.databaseManager.getDbHelper();
			SQLiteDatabase readableDatabase = dbHelper.getReadableDatabase();
			
			Cursor query = readableDatabase.query(DbConfigs.TABLE_BOOKMARKS, new String[]{"*"}, DbConfigs.FIELD_BOOK_NAME + " = '" + bookTitle + "'", null, null, null, null);
			if (query.getCount() > 0) {
				query.moveToFirst();
				
				int id = query.getInt(query.getColumnIndex(DbConfigs.FIELD_BOOKMARK_ID));
				int trackNr = query.getInt(query.getColumnIndex(DbConfigs.FIELD_TRACK));
				int playbackPos = query.getInt(query.getColumnIndex(DbConfigs.FIELD_PLAYBACK_POS));
				
				bookmark = new Bookmark(bookTitle, trackNr, playbackPos);
				bookmark.setId(id);
			}
			query.close();
		}
		return bookmark;
	}

}
