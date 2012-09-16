package de.reneruck.inear2.service;

import java.security.InvalidParameterException;

import de.reneruck.inear2.PlaylistFinishedException;
import de.reneruck.inear2.R;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.util.Log;
import android.widget.Toast;

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
				try {
					next();
				} catch (PlaylistFinishedException e) {
					Toast.makeText(context, R.string.toast_audiobook_is_finished, Toast.LENGTH_SHORT).show();
				}
			} else if (PlaybackService.ACTION_PREVIOUS.equals(action)) {
				Log.d(TAG, "Previous Track called");
				try {
					previous();
				} catch (PlaylistFinishedException e) {
					Toast.makeText(context, R.string.toast_audiobook_is_finished, Toast.LENGTH_SHORT).show();
				}
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
	public void next() throws PlaylistFinishedException {
		this.service.next();
	}

	@Override
	public void previous() throws PlaylistFinishedException {
		this.service.previous();
	}

	@Override
	public boolean isPlaying() {
		return this.service.isPlaying();
	}

	@Override
	public int getCurrentPlaybackPosition() {
		return service.getCurrentPlaybackPosition();
	}

	@Override
	public int getDuration() {
		return this.service.getDuration();
	}

	public BroadcastReceiver getBroadcastHandler() {
		return this.broadcastHandler;
	}

	@Override
	public void setPlaybackPosition(int progress) {
		this.service.seekTo(progress);
	}
}
