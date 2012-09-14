package de.reneruck.inear2;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.drm.DrmStore.Action;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.RemoteViews;

public class PlaybackService extends Service {
	
	public static final String ACTION_PLAY_PAUSE = "de.reneruck.inear.action.play_pause";
	public static final String ACTION_NEXT = "de.reneruck.inear.action.next";
	public static final String ACTION_PREVIOUS = "de.reneruck.inear.action.previous";

	private static final String TAG = "InEar - PlaybackService";
	
	private static final int play_pause = 1;
	private static final int next = 2;
	private static final int previous = 3;


	private MediaPlayer mediaPlayer;
	private AppContext appContext;
	private Notification notification;
	
	private int boundEntities = 0;
	private CurrentAudiobook bean;
	private MyBinder binder;
	private boolean foreground = false;
	
	@Override
	public void onCreate() {
		Log.d(TAG, "-- onCreate --");
		this.mediaPlayer = new MediaPlayer();
		this.appContext = (AppContext)getApplicationContext();
		initNotification();
		this.binder = new MyBinder();
		super.onCreate();
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(TAG, "-- onStartCommand --");

		if(this.mediaPlayer != null && !this.mediaPlayer.isPlaying()) {
			
			this.bean = this.appContext.getCurrentAudiobookBean();
			
			try {
				this.mediaPlayer.setDataSource(bean.getPlaylist().get(0));
				this.mediaPlayer.prepare();
			} catch (Exception e) {
				e.printStackTrace();
			}
	
	        IntentFilter commandFilter = new IntentFilter();
	        commandFilter.addAction(ACTION_PLAY_PAUSE);
	        commandFilter.addAction(ACTION_NEXT);
	        commandFilter.addAction(ACTION_PREVIOUS);
	        registerReceiver(receiver, commandFilter);
		}

		return super.onStartCommand(intent, flags, startId);
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
	
	private BroadcastReceiver receiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();

			if (ACTION_PLAY_PAUSE.equals(action)) {
				Log.d(TAG, "Received Play pause action");
				if (mediaPlayer.isPlaying()) {
					pause();
				} else {
					play();
				}
			} else if (ACTION_NEXT.equals(action)) {
				Log.d(TAG, "Next Track called");
			} else if (ACTION_PREVIOUS.equals(action))
				Log.d(TAG, "Previous Track called");
			{
			}
		}
	};
	
	private void pause() {
		if (this.boundEntities > 0) {
			mediaPlayer.pause();
		} else {
			stopSelf();
		}
	}
	
	private void play() {
		if(this.mediaPlayer != null) {
			this.mediaPlayer.start();
		}
	}

	public boolean isPlaying() {
		if(this.mediaPlayer != null) {
			return this.isPlaying();
		} else {
			return false;
		}
	}
	
	private void initNotification(){
		this.notification = new Notification.Builder(this)
		.setWhen(System.currentTimeMillis())
		.setSmallIcon(android.R.drawable.ic_media_play)
		.getNotification();
		
		RemoteViews contentView = new RemoteViews(getPackageName(), R.layout.notification_media_control);
		this.notification.contentView = contentView;
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
		contentView.setTextViewText(R.id.notification_current_track, this.bean.getCurrentTrackName());
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
		unregisterReceiver(receiver);
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
}
