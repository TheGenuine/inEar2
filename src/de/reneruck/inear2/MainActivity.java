package de.reneruck.inear2;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import de.reneruck.inear2.R.animator;
import de.reneruck.inear2.settings.SettingsActivity;

public class MainActivity extends Activity {

	private static final String TAG = "InEar - Main";

	private AppContext appContext;

	private File audioBooksBaseDir;
	private List<String> audioBookTitles;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.appContext = (AppContext) getApplicationContext();
    }

    @Override
    protected void onResume() {
    	super.onResume();

    	this.audioBooksBaseDir = new File(this.appContext.getAudiobokkBaseDir());

    	if(this.audioBooksBaseDir != null && this.audioBooksBaseDir.exists())
    	{
    		getAllAudiobooks();
    		initializeAndshowLayout();
    	} else {
    		showNoEntriesFoundScreen();
    	}
    }

    private void showNoEntriesFoundScreen() {
		setContentView(R.layout.activity_main_no_entries);
		((TextView)findViewById(R.id.no_entries_path)).setText(this.appContext.getAudiobokkBaseDir());
	}

	private void initializeAndshowLayout() {
		setContentView(R.layout.activity_main);
		ListView audiobooksList = (ListView) findViewById(R.id.audiobooklist);
		ListAdapter listAdapter = new AudiobookListAdapter(this, audioBookTitles);
		audiobooksList.setAdapter(listAdapter);
		audiobooksList.setOnItemClickListener(this.audiobookItemClickListener);
		audiobooksList.invalidate();
	}

	private OnItemClickListener audiobookItemClickListener = new OnItemClickListener() {
    	@Override
    	public void onItemClick(AdapterView<?> arg0, View view, int pos, long id) {
    		appContext.setCurrentAudiobook(audioBookTitles.get(pos));
    		Intent i = new Intent(getApplicationContext(), PlayActivity.class);
    		startActivity(i);
        	overridePendingTransition(animator.activity_slide_in, animator.activity_slide_out);
    	}
	};

    private void getAllAudiobooks() {
    	this.audioBookTitles = Arrays.asList(this.audioBooksBaseDir.list());
	}

	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
        case android.R.id.home:
        	break;
        case R.id.menu_settings:
        	Intent i = new Intent(getApplicationContext(), SettingsActivity.class);
        	startActivity(i);
        	break;
		}
		return true;
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	 @Override
	public void onPause() {
		 super.onPause();
	};

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
}
