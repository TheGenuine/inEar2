package de.reneruck.inear2.service;


public interface PlaybackServiceHandler {

	public void play_pause();
	public void next();
	public void previous();
	
	public boolean isPlaying();
	public int getCurrentPlaybackPosition();
	public int getDuration();
}
