package de.reneruck.inear2.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;
import de.reneruck.inear2.AppContext;
import de.reneruck.inear2.Bookmark;
import de.reneruck.inear2.CurrentAudiobook;
import de.reneruck.inear2.PlayActivity;
import de.reneruck.inear2.PlaylistFinishedException;
import de.reneruck.inear2.R;
import de.reneruck.inear2.db.AsyncStoreBookmark;
import de.reneruck.inear2.db.DatabaseManager;

public class PlaybackService extends Service implements OnCompletionListener {
	
	public static final String ACTION_PLAY_PAUSE = "de.reneruck.inear.action.play_pause";
	public static final String ACTION_NEXT = "de.reneruck.inear.action.next";
	public static final String ACTION_PREVIOUS = "de.reneruck.inear.action.previous";
	public static final String ACTION_SET_TRACK = "de.reneruck.inear.action.set_track";
	public static final String ACTION_SET_TRACK_NR = "de.reneruck.inear.action.set_track.nr";
	
	private static final String TAG = "InEar - PlaybackService";
	
	private static final int play_pause = 1;
	private static final int next = 2;
	private static final int previous = 3;


	private MediaPlayer mediaPlayer;
	private AppContext appContext;
	private Notification notification;
	
	private int boundEntities = 0;
	private CurrentAudiobook bean;
	private PlaybackServiceHandlerImpl binder;
	private boolean foreground = false;
	
	@Override
	public void onCreate() {
		Log.d(TAG, "-- onCreate --");
		this.mediaPlayer = new MediaPlayer();
		this.mediaPlayer.setOnCompletionListener(this);
		this.appContext = (AppContext)getApplicationContext();
		initNotification();
		
		this.binder = new PlaybackServiceHandlerImpl(this);
		
		IntentFilter commandFilter = new IntentFilter();
		commandFilter.addAction(ACTION_PLAY_PAUSE);
		commandFilter.addAction(ACTION_NEXT);
		commandFilter.addAction(ACTION_PREVIOUS);
		commandFilter.addAction(ACTION_SET_TRACK);
		registerReceiver(this.binder.getBroadcastHandler(), commandFilter);
		
		super.onCreate();
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(TAG, "-- onStartCommand --");
		if(isNewAudiobook()) {
			this.bean = this.appContext.getCurrentAudiobookBean();
			if(this.bean != null && this.bean.getPlaylist().size() >= 1){
				if(!loadBookmark())  prepareMediaplayerToCurrentTrack();
			} else {
				Toast.makeText(getApplicationContext(), R.string.toast_no_valid_files_to_play, Toast.LENGTH_LONG).show();
			}
		}

		return super.onStartCommand(intent, flags, startId);
	}

	private boolean loadBookmark() {
		Bookmark bookmark = this.bean.getBookmark();
		if(bookmark != null) {
			Log.d(TAG, "Loading bookmark");
			this.bean.setCurrentTrack(bookmark.getTrackNumber());
			prepareMediaplayerToCurrentTrack();
			this.mediaPlayer.seekTo(bookmark.getPlaybackPosition());
			return true;
		}
		return false;
	}

	private void prepareMediaplayerToCurrentTrack() {
		try {
			this.mediaPlayer.reset();
			this.mediaPlayer.setDataSource(this.bean.getPlaylist().get(this.bean.getCurrentTrack()));
			this.mediaPlayer.prepare();
			this.mediaPlayer.seekTo(0);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private boolean isNewAudiobook() {
		if(this.bean == null) {
			return true;
		} else {
			return !this.bean.equals(this.appContext.getCurrentAudiobookBean());
		}
	}

	private Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case play_pause:
				if(mediaPlayer.isPlaying())
				{
					pause();
				} else {
					play();
				}
				break;
			case next:
				break;
			case previous:
				break;
			default:
				break;
			}
		};
	};
	
	public void pause() {
		createOrUpdateBookmark();
		if (this.boundEntities > 0) {
			mediaPlayer.pause();
		} else {
			stopSelf();
		}
	}
	
	public void play() {
		if(this.mediaPlayer != null) {
			this.mediaPlayer.start();
		}
	}

	public boolean isPlaying() {
		if(this.mediaPlayer != null) {
			return this.mediaPlayer.isPlaying();
		} else {
			return false;
		}
	}
	
	public void previous() throws PlaylistFinishedException {
		if(this.bean != null)
		{
			this.bean.setPreviousTrack();
			boolean wasPlaying = this.mediaPlayer.isPlaying();
			prepareMediaplayerToCurrentTrack();
			if(wasPlaying) play();
		} else {
			Log.e(TAG, "No valid audiobook bean available");
		}
	}
	
	public void next(boolean wasPlaying) throws PlaylistFinishedException {
		if(this.bean != null)
		{
			this.bean.setNextTrack();
			prepareMediaplayerToCurrentTrack();
			if(wasPlaying) play();
		} else {
			Log.e(TAG, "No valid audiobook bean available");
		}
	}
	
	private void initNotification(){
		this.notification = new Notification.Builder(this)
		.setWhen(System.currentTimeMillis())
		.setSmallIcon(android.R.drawable.ic_media_play)
		.getNotification();
		
		RemoteViews contentView = new RemoteViews(getPackageName(), R.layout.notification_media_control);
		this.notification.contentView = contentView;
		this.notification.flags |= Notification.FLAG_ONGOING_EVENT;
		this.notification.flags |= Notification.FLAG_NO_CLEAR;
		
		setListeners();
	}
	
	private void toForeground() {
		Log.e(TAG, "-- ToForeground --");
		this.foreground = true;
		setNotificationContents();
		startForeground(1333, this.notification);
	}
	
	private void stopForeground() {
		Log.e(TAG, "-- stoppingForeground --");
		this.foreground = false;
		stopForeground(true);
	}

	private void setNotificationContents() {
		RemoteViews contentView = this.notification.contentView;
		if(mediaPlayer.isPlaying()) {
			contentView.setImageViewResource(R.id.notification_button_play, android.R.drawable.ic_media_pause);
		} else {
			contentView.setImageViewResource(R.id.notification_button_play, android.R.drawable.ic_media_play);
		}
//		contentView.setTextViewText(R.id.notification_current_track, this.bean.getCurrentTrackName());
	}
	
	private void setListeners() {
		RemoteViews contentView = this.notification.contentView;
		
		PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, new Intent(ACTION_PLAY_PAUSE), 0);
		contentView.setOnClickPendingIntent(R.id.notification_button_play, pendingIntent);
		
		PendingIntent pendingIntentNext = PendingIntent.getBroadcast(getApplicationContext(), 0, new Intent(ACTION_NEXT), 0);
		contentView.setOnClickPendingIntent(R.id.notification_button_next, pendingIntentNext);
		
		Intent intent = new Intent(Intent.ACTION_MAIN);
		intent.setClass(getApplicationContext(), PlayActivity.class);
		PendingIntent intentApplication = PendingIntent.getActivity(getApplicationContext(), 0, intent, Notification.FLAG_AUTO_CANCEL);
		contentView.setOnClickPendingIntent(R.id.notification_cover, intentApplication);
	}

	@Override
	public void onDestroy() {
		Log.d(TAG, "-- onDestroy --");
		unregisterReceiver(this.binder.getBroadcastHandler());
		this.mediaPlayer.release();
		this.mediaPlayer = null;
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
		if(this.mediaPlayer != null)
		{
			return this.mediaPlayer.getCurrentPosition();
		} else {
			return 0;
		}
	}
	
	public int getDuration() {
		if(this.mediaPlayer != null)
		{
			return this.mediaPlayer.getDuration();
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
			if(this.mediaPlayer.isPlaying()) {
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
		if(this.mediaPlayer != null && progress > 0 && progress < this.mediaPlayer.getDuration())
		{
			this.mediaPlayer.seekTo(progress);
		}
	}

	@Override
	public void onCompletion(MediaPlayer mediaPlayer) {
		Log.d(TAG, "Track finished");
		try {
			next(true);
		} catch (PlaylistFinishedException e) {
			Log.d(TAG, "End of Playlist reached");
//			this.bean.setCurrentTrack(0);
//			prepareMediaplayerToCurrentTrack();
		}
	}

	public void setTrack(int trackNr) {
		boolean wasPlaying = isPlaying();
		this.bean.setCurrentTrack(trackNr);
		prepareMediaplayerToCurrentTrack();
		if(wasPlaying) {
			play();
		}
	}

	private void createOrUpdateBookmark() {
		Log.d(TAG, "storing current track and playback position");
		if(this.bean.getBookmark() != null)
		{
			this.bean.getBookmark().setTrackNumber(this.bean.getCurrentTrack());
			this.bean.getBookmark().setPlaybackPosition(this.mediaPlayer.getCurrentPosition());
		} else {
			this.bean.setBookmark(new Bookmark(this.bean.getName(), this.bean.getCurrentTrack(), this.mediaPlayer.getCurrentPosition()));
		}
		storeBookmark();
	}

	private void storeBookmark() {
		DatabaseManager databaseManager = this.appContext.getDatabaseManager();
		if(databaseManager != null)
		{
			AsyncStoreBookmark storeBookmarkTask = new AsyncStoreBookmark(databaseManager);
			storeBookmarkTask.doInBackground(this.bean.getBookmark());
		} else {
			String string = getString(R.string.no_databasemanager);
			Toast.makeText(getApplicationContext(), string, Toast.LENGTH_LONG).show();
			Log.e(TAG, string);
		}
	}
}
