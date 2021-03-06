package de.reneruck.inear2;

import android.R.anim;
import android.app.ActionBar;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import de.reneruck.inear2.exceptions.PlaylistFinishedException;
import de.reneruck.inear2.service.PlaybackService;
import de.reneruck.inear2.service.PlaybackServiceHandler;

public class PlayActivity extends Activity{

	private static final String TAG = "InEar - PlayActivity";
	private AppContext appContext;
	private boolean isBound;
	private boolean isSeekbarUpdaterThreadRunning = false;
	private SeekBar seekbar;
	private PlaybackServiceHandler playbackServiceHandler;
	private Thread seekbarUpdaterThread;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "-- OnCreate --");

        setContentView(R.layout.activity_play);
        this.appContext = (AppContext) getApplicationContext();

        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        this.setTitle(this.appContext.getCurrentAudiobook().getName());
	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.d(TAG, "-- OnResume --");

		startAndBindToService();
	}

	private void initializePlayControl() {
    	ImageView bottonNext = (ImageView) findViewById(R.id.button_next);
    	bottonNext.setOnClickListener(this.nextButtonClickListener);

    	ImageView bottonPlay = (ImageView) findViewById(R.id.button_play);
    	bottonPlay.setOnClickListener(this.playButtonClickListener);

    	setPlaybackControllIcon();

    	ImageView bottonPrev = (ImageView) findViewById(R.id.button_prev);
    	bottonPrev.setOnClickListener(this.prevButtonClickListener);

		this.seekbar = (SeekBar) findViewById(R.id.seekBar1);
		this.seekbar.setOnSeekBarChangeListener(this.onSeekbarDragListener);
	}

	private ServiceConnection serviceConnection = new ServiceConnection() {
		@Override
		public void onServiceDisconnected(ComponentName name) {
			Log.d(TAG, "-- serviceDisconnected --");
			isBound = false;
			playbackServiceHandler = null;
		}
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			Log.d(TAG, "-- serviceConnected --");
			if(service != null && service instanceof PlaybackServiceHandler)
			{
				playbackServiceHandler = (PlaybackServiceHandler) service;
				isBound = true;
				initializePlayControl();
				setSeekbarMaxValue();
				setDurationIndicator();
				initSeekbarUpdaterThread();
			}
		}
	};
	
	private void initSeekbarUpdaterThread() {
		Log.d(TAG, "Initializing Seekbar Updater Thread");
		this.seekbarUpdaterThread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				while(isSeekbarUpdaterThreadRunning)
				{
					if(isBound)
					{
						seekbar.setProgress(playbackServiceHandler.getCurrentPlaybackPosition());
					}
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		});
		this.isSeekbarUpdaterThreadRunning = true;
		this.seekbarUpdaterThread.start();
	}
	
	
	private void setSeekbarMaxValue() {
		this.seekbar.setMax(this.playbackServiceHandler.getDuration());
	}
	
	private void setCurrentPlaytimeIndicator(int progress) {
		TextView currentTimeField = (TextView)findViewById(R.id.playback_current_time);
		int seconds = (int) (progress / 1000) % 60 ;
		int minutes = (int) ((progress / (1000*60)) % 60);	
		String secoundsString = seconds < 10 ? "0" + seconds : String.valueOf(seconds);
		String durationText = minutes + ":" + secoundsString;
		currentTimeField.setText(durationText);
	}
	
	private void setDurationIndicator() {
		if(this.isBound)
		{
			int duration = this.playbackServiceHandler.getDuration();
			TextView durationField = (TextView)findViewById(R.id.playback_max_time);
			int seconds = (int) (duration / 1000) % 60 ;
			int minutes = (int) ((duration / (1000*60)) % 60);		
			int hours = (int) ((duration / (1000*60*60)) % 60);		
			String secoundsString = seconds < 10 ? "0" + seconds : String.valueOf(seconds);
			String durationText = hours > 0 ? hours + ":" : "" + minutes + ":" + secoundsString;
			durationField.setText(durationText);
		}
	}
	
	private void startAndBindToService() {
		Intent serviceIntent = new Intent(appContext, PlaybackService.class);
		startService(serviceIntent);
		bindService(serviceIntent, serviceConnection, 0);
	}

	private OnClickListener nextButtonClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			try {
				playbackServiceHandler.next();
			} catch (PlaylistFinishedException e) {
				Log.d(TAG, "End of Playlist reached");
			}
		}
	};

	private OnClickListener playButtonClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			playbackServiceHandler.play_pause();
			setPlaybackControllIcon();
		}

	};
	
	private void setPlaybackControllIcon() {
		if (this.playbackServiceHandler.isPlaying()) {
			((ImageView) findViewById(R.id.button_play)).setImageResource(R.drawable.ic_play_stop);
		} else {
			((ImageView) findViewById(R.id.button_play)).setImageResource(R.drawable.ic_play_start);
		}
	}
	
	private OnClickListener prevButtonClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			try {
				playbackServiceHandler.previous();
			} catch (PlaylistFinishedException e) {
				Log.d(TAG, "End of Playlist reached");
			}
		}
	};

	private OnSeekBarChangeListener onSeekbarDragListener = new OnSeekBarChangeListener() {
		
		private boolean wasPlaying;

		@Override
		public void onProgressChanged(SeekBar seekbar, int progress, boolean fromUser) {
			seekbar.setProgress(progress);
			setCurrentPlaytimeIndicator(progress);
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
			if(isBound)
			{
				this.wasPlaying = playbackServiceHandler.isPlaying();
				if(this.wasPlaying)
				{
					playbackServiceHandler.play_pause();
				}
			}
		}

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			if(isBound)
			{
				playbackServiceHandler.setPlaybackPosition(seekBar.getProgress());
				if(this.wasPlaying)
				{
					playbackServiceHandler.play_pause();
				}
			}
		}
	};
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_play, menu);
        return true;
    }

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
        case android.R.id.home:
        	finish();
        	overridePendingTransition(anim.slide_in_left, anim.slide_out_right);
        	break;
        case R.id.menu_playlist:
        	
        	break;
		}
		return true;
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		overridePendingTransition(anim.slide_in_left, anim.slide_out_right);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		if(this.isBound) {
			unbindService(this.serviceConnection);
		}
		this.isSeekbarUpdaterThreadRunning = false;
		this.seekbarUpdaterThread = null;
	};
}
