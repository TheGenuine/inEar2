package de.reneruck.inear2.service;

import java.security.InvalidParameterException;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;
import de.reneruck.inear2.PlaylistFinishedException;
import de.reneruck.inear2.R;

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
	
			if (service.getApplication().getString(R.string.intent_play_pause).equals(action)) {
				Log.d(TAG, "Received Play-Pause action");
				play_pause();
			} else if (service.getApplication().getString(R.string.intent_next).equals(action)) {
				Log.d(TAG, "Next Track called");
				try {
					next();
				} catch (PlaylistFinishedException e) {
					Toast.makeText(context, R.string.toast_audiobook_is_finished, Toast.LENGTH_SHORT).show();
				}
			} else if (service.getApplication().getString(R.string.intent_previous).equals(action)) {
				Log.d(TAG, "Previous Track called");
				try {
					previous();
				} catch (PlaylistFinishedException e) {
					Toast.makeText(context, R.string.toast_audiobook_is_finished, Toast.LENGTH_SHORT).show();
				}
			} else if (service.getApplication().getString(R.string.intent_set_track).equals(action)) {
				Log.d(TAG, "Set Track called");
					int trackNr = intent.getIntExtra(PlaybackService.ACTION_SET_TRACK_NR, 0);
					service.setTrack(trackNr);
			} else if (Intent.ACTION_MEDIA_BUTTON.equals(action)) {
				Log.d(TAG, "Media Button Action");
				KeyEvent keyEvent = (KeyEvent) intent.getExtras().get(Intent.EXTRA_KEY_EVENT);
	            if (keyEvent.getAction() != KeyEvent.ACTION_DOWN)
	                return;

		            switch (keyEvent.getKeyCode()) {
		                case KeyEvent.KEYCODE_HEADSETHOOK:
		                	Log.d(TAG, "Media Button Action HEADSET HOOKED");
		                case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
		                	Log.d(TAG, "Media Button Action PLAY_PAUSE");
		                	play_pause();
		                	break;
		                case KeyEvent.KEYCODE_MEDIA_PLAY:
		                	Log.d(TAG, "Media Button Action PLAY");
		                	play();
		                	break;
		                case KeyEvent.KEYCODE_MEDIA_STOP:
		                	Log.d(TAG, "Media Button Action STOP");
		                case KeyEvent.KEYCODE_MEDIA_PAUSE:
		                	Log.d(TAG, "Media Button Action PAUSE");
		                	pause();
		                    break;
		                case KeyEvent.KEYCODE_MEDIA_NEXT:
		                	Log.d(TAG, "Media Button Action NEXT");
							try {
								next();
							} catch (PlaylistFinishedException e) {
								Log.d(TAG, "Playlist end reached");
							}
		                	break;
		                case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
		                	Log.d(TAG, "Media Button Action PREVIOUS");
		                    // TODO: ensure that doing this in rapid succession actually plays the
		                    // previous song
		                	try {
		                		previous();
		                	} catch (PlaylistFinishedException e) {
		                		Log.d(TAG, "Playlist end reached");
		                	}
		                	break;
		            }
			} else {
				Log.d(TAG, "No handler found for Action " + action); 
			}
		}
	}

	@Override
	public void play_pause() {
		if (isPlaying()) {
			pause();
		} else {
			play();
		}
	}

	private void play() {
		if(this.service != null) 
		{
			this.service.play();
		} else {
			Log.e(TAG, "No Playbackservice available");
		}
	}

	private void pause() {
		if(this.service != null) 
		{
			this.service.pause();
		} else {
			Log.e(TAG, "No Playbackservice available");
		}
	}

	@Override
	public void next() throws PlaylistFinishedException {
		if(this.service != null) 
		{
			this.service.next(false);
		} else {
			Log.e(TAG, "No Playbackservice available");
		}
	}

	@Override
	public void previous() throws PlaylistFinishedException {
		if(this.service != null) 
		{
			this.service.previous();
		} else {
			Log.e(TAG, "No Playbackservice available");
		}
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
