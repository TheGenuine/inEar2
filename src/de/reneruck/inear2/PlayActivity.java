package de.reneruck.inear2;

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
import de.reneruck.inear2.PlaybackService.MyBinder;

public class PlayActivity extends Activity{

	private static final String TAG = "InEar - PlayActivity";
	private AppContext appContext;
	private boolean bound;
	private SeekBar seekbar;
	private PlaybackService playbackService;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "-- OnCreate --");

        setContentView(R.layout.activity_play);
        this.appContext = (AppContext) getApplicationContext();

        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.d(TAG, "-- OnResume --");

		initializePlayControl();
		startAndBindToService();
	}

	private void initializePlayControl() {
    	ImageView bottonNext = (ImageView) findViewById(R.id.button_next);
    	bottonNext.setOnClickListener(this.nextButtonClickListener);

    	ImageView bottonPlay = (ImageView) findViewById(R.id.button_play);
    	bottonPlay.setOnClickListener(this.playButtonClickListener);

    	if(this.bound)
    	{
			((ImageView)findViewById(R.id.button_play)).setImageResource(android.R.drawable.ic_media_pause);

    	} else {
			((ImageView)findViewById(R.id.button_play)).setImageResource(android.R.drawable.ic_media_play);
    	}

    	ImageView bottonPrev = (ImageView) findViewById(R.id.button_prev);
    	bottonPrev.setOnClickListener(this.prevButtonClickListener);

		this.seekbar = (SeekBar) findViewById(R.id.seekBar1);
		this.seekbar.setOnSeekBarChangeListener(this.onSeekbarDragListener);
	}

	private ServiceConnection serviceConnection = new ServiceConnection() {
		@Override
		public void onServiceDisconnected(ComponentName name) {
			Log.d(TAG, "-- serviceDisconnected --");
			bound = false;
			playbackService = null;
		}
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			Log.d(TAG, "-- serviceConnected --");
			if(service != null && service instanceof MyBinder)
			{
				playbackService = ((MyBinder)service).getService();
			}
			bound = true;
		}
	};
	
	private void startAndBindToService() {
		Intent serviceIntent = new Intent(appContext, PlaybackService.class);
		startService(serviceIntent);
		bindService(serviceIntent, serviceConnection, 0);
	}

	private OnClickListener nextButtonClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			sendCommand(PlaybackService.ACTION_NEXT);
		}
	};

	private OnClickListener playButtonClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			sendCommand(PlaybackService.ACTION_PLAY_PAUSE);
		}
	};

	private OnClickListener prevButtonClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			sendCommand(PlaybackService.ACTION_PREVIOUS);
		}
	};

	private OnSeekBarChangeListener onSeekbarDragListener = new OnSeekBarChangeListener() {
		
		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
			// TODO Auto-generated method stub
			
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
        	break;
        case R.id.menu_playlist:
        	
        	break;
		}
		return true;
	}
	
	private void sendCommand(String action) {
		Intent i = new Intent(action);
		sendBroadcast(i);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		if(this.bound) {
			unbindService(this.serviceConnection);
		}
	};
}