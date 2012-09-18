package de.reneruck.inear2;

import android.content.SharedPreferences;

public class Settings {

	private boolean createNoMediaFile;
	private boolean autoplay;
	
	public Settings(SharedPreferences sharedPref) {
		this.autoplay = sharedPref.getBoolean("pref_autoplay", true);
		this.createNoMediaFile = sharedPref.getBoolean("pref_create_nomedia", true); 
	}
	
	public boolean isCreateNoMediaFile() {
		return createNoMediaFile;
	}
	public boolean isAutoplay() {
		return autoplay;
	}

	public void setAutoplay(boolean autoplay) {
		this.autoplay = autoplay;
	}

	public void setCreateNoMediaFile(boolean createNoMediaFile) {
		this.createNoMediaFile = createNoMediaFile;
	}
	
}
