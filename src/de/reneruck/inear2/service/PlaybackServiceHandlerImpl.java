package de.reneruck.inear2.service;

import java.security.InvalidParameterException;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.util.Log;

public class PlaybackServiceHandlerImpl extends Binder implements PlaybackServiceHandler {

	private static final String TAG = "InEar - PlaybackServiceHandler";

	private PlaybackService service;
	private BroadcastReceiver broadcastHandler;

	public PlaybackServiceHandlerImpl(PlaybackService service) {
		if (service != null) {
			this.service = service;
			this.broadcastHandler = new BroadcastHandler();
		} else {
			throw new InvalidParameterException("Parameter playbackService cannot be null");
		}
	}

	class BroadcastHandler extends BroadcastReceiver {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
	
			if (PlaybackService.ACTION_PLAY_PAUSE.equals(action)) {
				Log.d(TAG, "Received Play pause action");
				play_pause();
			} else if (PlaybackService.ACTION_NEXT.equals(action)) {
				Log.d(TAG, "Next Track called");
				next();
			} else if (PlaybackService.ACTION_PREVIOUS.equals(action)) {
				Log.d(TAG, "Previous Track called");
				previous();
			}
		}
	}

	@Override
	public void play_pause() {
		if (isPlaying()) {
			this.service.pause();
		} else {
			this.service.play();
		}
	}

	@Override
	public void next() {
		this.service.next();
	}

	@Override
	public void previous() {
		this.service.previous();
	}

	@Override
	public boolean isPlaying() {
		return this.service.isPlaying();
	}

	@Override
	public int getCurrentPlaybackPosition() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getDuration() {
		return this.service.getDuration();
	}

	public BroadcastReceiver getBroadcastHandler() {
		return this.broadcastHandler;
	}
}
