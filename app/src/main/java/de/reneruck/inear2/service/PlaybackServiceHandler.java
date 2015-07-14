package de.reneruck.inear2.service;

import de.reneruck.inear2.PlaylistFinishedException;


public interface PlaybackServiceHandler {

	public void play_pause();
	public void next() throws PlaylistFinishedException;
	public void previous() throws PlaylistFinishedException;
	
	public boolean isPlaying();
	public int getCurrentPlaybackPosition();
	public int getDuration();
	public void setPlaybackPosition(int progress);
}
