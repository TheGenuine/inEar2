package de.reneruck.inear2;

import java.util.ArrayList;
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
import de.reneruck.inear2.file.FileScanner;
import de.reneruck.inear2.settings.SettingsActivity;

public class MainActivity extends Activity {

	private static final String TAG = "InEar - Main";

	private AppContext mAppContext;
	private List<AudioBook> mAudioBooks = new ArrayList<>();

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.mAppContext = (AppContext) getApplicationContext();
    }

    @Override
    protected void onResume() {
    	super.onResume();
		this.mAudioBooks = this.mAppContext.getAvailableAudioBooks();

		if(this.mAudioBooks.isEmpty()){
			getAllAudiobooks();
			showLoadingScreen();
		} else {
			initializeAndShowLayout();
		}
    }

    private void showNoEntriesFoundScreen() {
		setContentView(R.layout.activity_main_no_entries);
		((TextView)findViewById(R.id.no_entries_path)).setText(this.mAppContext.getAudiobookBaseDir());
	}

	private void showLoadingScreen() {
		setContentView(R.layout.activity_main_loading);
	}

	private void initializeAndShowLayout() {
		setContentView(R.layout.activity_main);
		ListView audiobooksList = (ListView) findViewById(R.id.audiobooklist);
		ListAdapter listAdapter = new AudiobookListAdapter(this, mAudioBooks);
		audiobooksList.setAdapter(listAdapter);
		audiobooksList.setOnItemClickListener(this.audiobookItemClickListener);
		audiobooksList.invalidate();
	}

	private OnItemClickListener audiobookItemClickListener = new OnItemClickListener() {
    	@Override
    	public void onItemClick(AdapterView<?> arg0, View view, int pos, long id) {
    		mAppContext.setCurrentAudiobook(mAudioBooks.get(pos));
    		Intent i = new Intent(getApplicationContext(), PlayActivity.class);
    		startActivity(i);
        	overridePendingTransition(animator.activity_slide_in, animator.activity_slide_out);
    	}
	};

    private void getAllAudiobooks() {
		FileScanner fileScanner = new FileScanner(this.mAppContext);
		fileScanner.execute();
		fileScanner.listenWith(new ListenableAsyncTask.AsyncTaskListener<List<AudioBook>>() {
			@Override
			public void onPostExecute(List<AudioBook> audioBooks) {
				mAudioBooks = audioBooks;
				mAppContext.setAvailableAudioBooks(mAudioBooks);

				if(mAudioBooks.isEmpty()) {
					showNoEntriesFoundScreen();
				} else {
					initializeAndShowLayout();
				}
			}
		});
	}

	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return super.onCreateOptionsMenu(menu);
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
