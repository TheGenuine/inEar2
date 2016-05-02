package de.reneruck.inear2;

import java.util.ArrayList;
import java.util.List;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.preference.PreferenceManager;
import android.util.Log;
import de.reneruck.inear2.db.DatabaseManager;
import de.reneruck.inear2.service.PlaybackService;


public class AppContext extends Application {

	private static final String TAG = "InEar - AppContext";

	private String audiobookBaseDir = "";

	private DatabaseManager databaseManager;
	private Settings settings;
	private AudioBook mCurrentAudioBook;
	private List<AudioBook> availableAudioBooks = new ArrayList<>();

	@Override
	public void onCreate() {
		super.onCreate();
		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
		this.databaseManager = new DatabaseManager(this);
		
		readSettings();
		registerForPreferenceChange();
	}
	
	public void readSettings() {
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
		this.audiobookBaseDir = sharedPref.getString("pref_base_dir", getString(R.string.pref_base_dir_default));
		this.settings = new Settings(sharedPref);
//		runFilescanner();
	}

	private void registerForPreferenceChange() {
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
		sharedPref.registerOnSharedPreferenceChangeListener(new OnSharedPreferenceChangeListener() {
			
			@Override
			public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
				if("pref_base_dir".equals(key)) {
					audiobookBaseDir = sharedPreferences.getString("pref_base_dir", getString(R.string.pref_base_dir_default));
//					runFilescanner();
				}
				if("pref_autoplay".equals(key)) {
					settings.setAutoplay(sharedPreferences.getBoolean("pref_autoplay", false));
				}
				if("pref_exclude_audiobook_files".equals(key)) {
					settings.setCreateNoMediaFile(sharedPreferences.getBoolean("pref_exclude_audiobook_files", true));
				}
			}
		});
	}
	
	public void setCurrentAudiobook(AudioBook currentAudioBook) {
		if(this.mCurrentAudioBook == null || !this.mCurrentAudioBook.equals(currentAudioBook))
		{
			this.mCurrentAudioBook = currentAudioBook;
			this.mCurrentAudioBook.loadStoredBookmark();
		} else {
			Log.d(TAG, currentAudioBook.getName() + " already loaded, nothing to do");
		}
	}

	public DatabaseManager getDatabaseManager() {
		return databaseManager;
	}

	public void setDatabaseManager(DatabaseManager databaseManager) {
		this.databaseManager = databaseManager;
	}

	public AudioBook getCurrentAudiobook() {
		return mCurrentAudioBook;
	}

	public Settings getSettings() {
		return settings;
	}

	public void setSettings(Settings settings) {
		this.settings = settings;
	}

	public boolean isPlaybackServiceRunning(){
        final ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        final List<RunningServiceInfo> services = activityManager.getRunningServices(Integer.MAX_VALUE);

        for (RunningServiceInfo runningServiceInfo : services) {
            if (runningServiceInfo.service.getClassName().equals(PlaybackService.class.getName())){
                return true;
            }
        }
        return false;
     }

	public void setAudiobookBaseDir(String audiobookBaseDir) {
		this.audiobookBaseDir = audiobookBaseDir;
	}

	public String getAudiobookBaseDir() {
		return this.audiobookBaseDir;
	}

	public List<AudioBook> getAvailableAudioBooks() {
		return availableAudioBooks;
	}

	public void setAvailableAudioBooks(List<AudioBook> availableAudioBooks) {
		this.availableAudioBooks = availableAudioBooks;
	}
}
