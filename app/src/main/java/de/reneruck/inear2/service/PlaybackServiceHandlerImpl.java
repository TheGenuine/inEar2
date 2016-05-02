package de.reneruck.inear2.service;

import java.security.InvalidParameterException;

import de.reneruck.inear2.exceptions.PlaylistFinishedException;
import de.reneruck.inear2.R;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.util.Log;
import android.widget.Toast;

public class PlaybackServiceHandlerImpl extends Binder implements PlaybackServiceHandler {

	private static final String TAG = "PlaybackServiceHandler";

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

			switch (action){
				case PlaybackService.ACTION_PLAY_PAUSE:
					Log.d(TAG, "Received Play pause action");
					play_pause();
					break;
				case PlaybackService.ACTION_NEXT:
					Log.d(TAG, "Next Track called");
					try {
						next();
					} catch (PlaylistFinishedException e) {
						Toast.makeText(context, R.string.toast_audiobook_is_finished, Toast.LENGTH_SHORT).show();
					}
					break;
				case PlaybackService.ACTION_PREVIOUS:
					Log.d(TAG, "Previous Track called");
					try {
						previous();
					} catch (PlaylistFinishedException e) {
						Toast.makeText(context, R.string.toast_audiobook_is_finished, Toast.LENGTH_SHORT).show();
					}
					break;
				case PlaybackService.ACTION_SET_TRACK:
					Log.d(TAG, "Set Track called");
					int trackNr = intent.getIntExtra(PlaybackService.ACTION_SET_TRACK_NR, 0);
					service.setTrack(trackNr);
					break;
				case PlaybackService.ACTION_DISMISS:
					Log.d(TAG, "Dismiss Action");
					play_pause();
                    stopService();
                    break;
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
		this.service.next(false);
	}

	@Override
	public void previous() throws PlaylistFinishedException {
		this.service.previous();
	}

    @Override
    public void stopService() {
        service.stopSelf();
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
