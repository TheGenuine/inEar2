package de.reneruck.inear2.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.session.MediaSession;
import android.media.session.PlaybackState;
import android.os.Binder;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;
import de.reneruck.inear2.AppContext;
import de.reneruck.inear2.models.AudioBook;
import de.reneruck.inear2.models.Bookmark;
import de.reneruck.inear2.PlayActivity;
import de.reneruck.inear2.exceptions.PlaylistFinishedException;
import de.reneruck.inear2.R;
import de.reneruck.inear2.db.AsyncStoreBookmark;
import de.reneruck.inear2.db.DatabaseManager;

public class PlaybackService extends Service implements OnCompletionListener {
	
	public static final String ACTION_PLAY_PAUSE = "de.reneruck.inear.action.play_pause";
	public static final String ACTION_NEXT = "de.reneruck.inear.action.next";
	public static final String ACTION_PREVIOUS = "de.reneruck.inear.action.previous";
	public static final String ACTION_SET_TRACK = "de.reneruck.inear.action.set_track";
	public static final String ACTION_DISMISS = "de.reneruck.inear.action.dismiss";

	public static final String ACTION_SET_TRACK_NR = "de.reneruck.inear.action.set_track.nr";

	private static final String TAG = "InEar - PlaybackService";
	private static final int play_pause = 1;
	private static final int next = 2;
	private static final int previous = 3;


	private MediaPlayer mMediaPlayer;
	private MediaSession mMediaSession;
    private AppContext mAppContext;
    private NotificationManager mNotificationManager;
    private AudioManager mAudioManager;

	private Notification notification;
	private int boundEntities = 0;
	private AudioBook currentAudioBook;
	private PlaybackServiceHandlerImpl binder;
	private boolean foreground = false;

	@Override
	public void onCreate() {
		Log.d(TAG, "-- onCreate --");
		this.mMediaPlayer = new MediaPlayer();
		this.mMediaPlayer.setOnCompletionListener(this);
		this.mAppContext = (AppContext) getApplicationContext();
        this.mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        this.binder = new PlaybackServiceHandlerImpl(this);

        setupMediaSession();
        setCommandFilters();

		super.onCreate();
	}

    private void setupMediaSession() {
        this.mMediaSession = new MediaSession(getApplicationContext(), TAG);

        mMediaSession.setFlags(MediaSession.FLAG_HANDLES_MEDIA_BUTTONS |
                MediaSession.FLAG_HANDLES_TRANSPORT_CONTROLS);

        mMediaSession.setCallback(new MediaSession.Callback() {
            @Override
            public void onPlay() {
                play();
            }

            @Override
            public void onPause() {
                pause();
            }

            @Override
            public void onSkipToNext() {
                try {
                    next(isPlaying());
                } catch (PlaylistFinishedException e) {
                    Log.d(TAG, "End of Playlist reached");
                }
            }

            @Override
            public void onSkipToPrevious() {
                try {
                    previous();
                } catch (PlaylistFinishedException e) {
                    Log.d(TAG, "End of Playlist reached");
                }
            }
        });

        mMediaSession.setActive(true);
        updateMediaSessionPlaybackState();
    }

    private void updateMediaSessionPlaybackState(){

        int state = isPlaying() ? PlaybackState.STATE_PLAYING : PlaybackState.STATE_PAUSED;

        PlaybackState playbackState = new PlaybackState.Builder()
                .setActions(
                            PlaybackState.ACTION_PLAY |
                            PlaybackState.ACTION_PLAY_PAUSE |
                            PlaybackState.ACTION_PAUSE |
                            PlaybackState.ACTION_SKIP_TO_NEXT |
                            PlaybackState.ACTION_SKIP_TO_PREVIOUS)
                .setState(state, this.mMediaPlayer.getCurrentPosition(), 1, SystemClock.elapsedRealtime())
                .build();
        mMediaSession.setPlaybackState(playbackState);
    }

    private void setCommandFilters() {
        IntentFilter commandFilter = new IntentFilter();
        commandFilter.addAction(ACTION_PLAY_PAUSE);
        commandFilter.addAction(ACTION_NEXT);
        commandFilter.addAction(ACTION_PREVIOUS);
        commandFilter.addAction(ACTION_SET_TRACK);
        registerReceiver(this.binder.getBroadcastHandler(), commandFilter);
    }

    @Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(TAG, "-- onStartCommand --");
		if(isNewAudiobook()) {
			this.currentAudioBook = this.mAppContext.getCurrentAudiobook();
			if(this.currentAudioBook != null && this.currentAudioBook.getPlaylist().size() >= 1){
				if(!loadBookmark())  prepareMediaplayerToCurrentTrack();
			} else {
				Toast.makeText(getApplicationContext(), R.string.toast_no_valid_files_to_play, Toast.LENGTH_LONG).show();
			}
		}

		return super.onStartCommand(intent, flags, startId);
	}

	private boolean loadBookmark() {
		Bookmark bookmark = this.currentAudioBook.getBookmark();
		if(bookmark != null) {
			Log.d(TAG, "Loading bookmark");
			this.currentAudioBook.setCurrentTrack(bookmark.getTrackNumber());
			prepareMediaplayerToCurrentTrack();
			this.mMediaPlayer.seekTo(bookmark.getPlaybackPosition());
			return true;
		}
		return false;
	}

	private void prepareMediaplayerToCurrentTrack() {
		try {
			this.mMediaPlayer.reset();
			this.mMediaPlayer.setDataSource(this.currentAudioBook.getPlaylist().get(this.currentAudioBook.getCurrentTrack()));
			this.mMediaPlayer.prepare();
			this.mMediaPlayer.seekTo(0);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private boolean isNewAudiobook() {
        return this.currentAudioBook == null || !this.currentAudioBook.equals(this.mAppContext.getCurrentAudiobook());
    }

//	private Handler handler = new Handler() {
//
//		@Override
//		public void handleMessage(Message msg) {
//			switch (msg.what) {
//			case play_pause:
//				if(mMediaPlayer.isPlaying())
//				{
//					pause();
//				} else {
//					play();
//				}
//				break;
//			case next:
//				break;
//			case previous:
//				break;
//			default:
//				break;
//			}
//		};
//	};
	
	public void pause() {
		createOrUpdateBookmark();
        mMediaPlayer.pause();
        updateMediaSessionPlaybackState();
        updateNotification();
	}
	
	public void play() {
		if(this.mMediaPlayer != null) {
			this.mMediaPlayer.start();
            updateMediaSessionPlaybackState();
            updateNotification();
		}
	}

    private void updateNotification() {
        mNotificationManager.notify(1333, generateNotification());
    }

    public boolean isPlaying() {
        return this.mMediaPlayer != null && this.mMediaPlayer.isPlaying();
    }
	
	public void previous() throws PlaylistFinishedException {
		if(this.currentAudioBook != null)
		{
			this.currentAudioBook.setPreviousTrack();
			boolean wasPlaying = this.mMediaPlayer.isPlaying();
			prepareMediaplayerToCurrentTrack();
			if(wasPlaying) play();
		} else {
			Log.e(TAG, "No valid currentAudioBook currentAudioBook available");
		}
	}
	
	public void next(boolean wasPlaying) throws PlaylistFinishedException {
		if(this.currentAudioBook != null)
		{
			this.currentAudioBook.setNextTrack();
			prepareMediaplayerToCurrentTrack();
			if(wasPlaying) play();
		} else {
			Log.e(TAG, "No valid currentAudioBook currentAudioBook available");
		}
	}
	
	private Notification generateNotification(){

        Notification.Action previous_action = new Notification.Action.Builder(R.drawable.ic_stat_av_skip_previous, "", PendingIntent.getBroadcast(getApplicationContext(), 0, new Intent(ACTION_PREVIOUS), 0)).build();
        Notification.Action pause_action = new Notification.Action.Builder(R.drawable.ic_stat_av_pause, "", PendingIntent.getBroadcast(getApplicationContext(), 0, new Intent(ACTION_PLAY_PAUSE), 0)).build();
        Notification.Action play_action = new Notification.Action.Builder(R.drawable.ic_stat_av_play_arrow, "", PendingIntent.getBroadcast(getApplicationContext(), 0, new Intent(ACTION_PLAY_PAUSE), 0)).build();
        Notification.Action next_action = new Notification.Action.Builder(R.drawable.ic_stat_av_skip_next, "", PendingIntent.getBroadcast(getApplicationContext(), 0, new Intent(ACTION_NEXT), 0)).build();

        return new Notification.Builder(this.getApplicationContext())
				.setSmallIcon(isPlaying() ? android.R.drawable.ic_media_play : android.R.drawable.ic_media_pause)
                .setContentTitle(this.currentAudioBook.getName())
                .setContentText(this.currentAudioBook.getName() + " - " + this.currentAudioBook.getCurrentTrackName())
                .setWhen(System.currentTimeMillis())  // the time stamp
                .setDeleteIntent(PendingIntent.getBroadcast(getApplicationContext(), 0, new Intent(ACTION_DISMISS), 0))
                .setContentIntent(PendingIntent.getActivity(getApplicationContext(), 0, new Intent(getApplicationContext(),PlayActivity.class), 0))
                .addAction(previous_action)
                .addAction(isPlaying() ? pause_action : play_action)
                .addAction(next_action)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setTicker(this.currentAudioBook.getName() + " - " + this.currentAudioBook.getCurrentTrackName())
                .build();

//		this.notification.flags |= Notification.FLAG_NO_CLEAR;
	}
	
	private void toForeground() {
		Log.d(TAG, "-- ToForeground --");
		this.foreground = true;
		startForeground(1333, generateNotification());
	}
	
	private void stopForeground() {
		Log.d(TAG, "-- stoppingForeground --");
		this.foreground = false;
		stopForeground(true);
	}

	@Override
	public void onDestroy() {
		Log.d(TAG, "-- onDestroy --");
		unregisterReceiver(this.binder.getBroadcastHandler());
		this.mMediaSession.release();
		this.mMediaPlayer.release();
		this.mMediaPlayer = null;
		super.onDestroy();
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		Log.d(TAG, "-- onBind --");
		handleBind();
		return this.binder;
	}

	private void handleBind() {
		this.boundEntities++;
		if(this.foreground) {
			stopForeground();
		}
		Log.d(TAG, this.boundEntities + " entities are bound now");
	}
	
	@Override
	public void onRebind(Intent intent) {
		Log.d(TAG, "-- onRebind --");
		handleBind();
		super.onRebind(intent);
	}
	
	public int getCurrentPlaybackPosition() {
		if(this.mMediaPlayer != null)
		{
			return this.mMediaPlayer.getCurrentPosition();
		} else {
			return 0;
		}
	}
	
	public int getDuration() {
		if(this.mMediaPlayer != null)
		{
			return this.mMediaPlayer.getDuration();
		} else {
			return 0;
		}
	}
	
	@Override
	public boolean onUnbind(Intent intent) {
		Log.d(TAG, "-- onDestroy --");
		this.boundEntities--;
		Log.d(TAG, this.boundEntities + " entities are bound now");
		
		if(this.boundEntities <= 0)
		{
			if(this.mMediaPlayer.isPlaying()) {
				toForeground();
			} else {
				stopSelf();
			}
		}
		return true;
	}
	
	public class MyBinder extends Binder {
        public PlaybackService getService() {
            return PlaybackService.this;
        }
	}

	public void seekTo(int progress) {
		if(this.mMediaPlayer != null && progress > 0 && progress < this.mMediaPlayer.getDuration())
		{
			this.mMediaPlayer.seekTo(progress);
		}
	}

	@Override
	public void onCompletion(MediaPlayer mediaPlayer) {
		Log.d(TAG, "Track finished");
		try {
			next(true);
		} catch (PlaylistFinishedException e) {
			Log.d(TAG, "End of Playlist reached");
//			this.currentAudioBook.setCurrentTrack(0);
//			prepareMediaplayerToCurrentTrack();
		}
	}

	public void setTrack(int trackNr) {
		boolean wasPlaying = isPlaying();
		this.currentAudioBook.setCurrentTrack(trackNr);
		prepareMediaplayerToCurrentTrack();
		if(wasPlaying) {
			play();
		}
	}

	private void createOrUpdateBookmark() {
		Log.d(TAG, "storing current track and playback position");
		if(this.currentAudioBook.getBookmark() != null)
		{
			this.currentAudioBook.getBookmark().setTrackNumber(this.currentAudioBook.getCurrentTrack());
			this.currentAudioBook.getBookmark().setPlaybackPosition(this.mMediaPlayer.getCurrentPosition());
		} else {
			this.currentAudioBook.setBookmark(new Bookmark(this.currentAudioBook.getName(), this.currentAudioBook.getCurrentTrack(), this.mMediaPlayer.getCurrentPosition()));
		}
		storeBookmark();
	}

	private void storeBookmark() {
		DatabaseManager databaseManager = this.mAppContext.getDatabaseManager();
		if(databaseManager != null)
		{
			new AsyncStoreBookmark(databaseManager).execute(this.currentAudioBook.getBookmark());
		} else {
			String string = getString(R.string.no_databasemanager);
			Toast.makeText(getApplicationContext(), string, Toast.LENGTH_LONG).show();
			Log.w(TAG, string);
		}
	}
}
