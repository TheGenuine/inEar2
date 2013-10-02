package de.reneruck.inear2;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import de.reneruck.inear2.file.FileScanner;

public class PlaylistAdapter extends BaseAdapter {

	private Context mContext;
	private ArrayList<String> mPlaylist;
	private int mRecource;

	private static LayoutInflater mInflater = null;

	public PlaylistAdapter(Context context, int recource, List<String> playlist) {
		mContext = context;
		mRecource = recource;
		mPlaylist = new ArrayList<String>(playlist);
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}
	
	@Override
	public int getCount() {
		return mPlaylist.size();
	}

	@Override
	public String getItem(int position) {
		return mPlaylist.get(position);
	}

	@Override
	public long getItemId(int position) {
		return mPlaylist.get(position).hashCode();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		TextView text;
		View view = convertView;
		String item = getItem(position);

		if(FileScanner.END_OF_CD.equals(item))
		{
			view = mInflater.inflate(R.layout.playlist_entry_seperator, parent, false);
		} else {
			
			if (convertView == null || convertView instanceof LinearLayout){
				view = mInflater.inflate(mRecource, null);
			} else {
				view = convertView;
			}
			text = (TextView) view;
						
			text.setBackgroundColor(Color.TRANSPARENT);
			text.setText(getItemText(item));
			
			int currentTrack = ((AppContext)this.mContext.getApplicationContext()).getCurrentAudiobookBean().getCurrentTrack();
			if(currentTrack == position)
			{
				text.setTypeface(null,Typeface.BOLD);
				text.setTextColor(Color.RED);
			} else {
				text.setTypeface(null,Typeface.NORMAL);
				text.setTextColor(Color.WHITE);
			}
		}
		return view;
	}

    private CharSequence getItemText(String item) {
    	String[] split = item.split(File.separator);
    	if(split.length > 1)
    	{
    		return split[split.length-1].replace(".mp3", " ").trim();
    	}
		return item;
	}
}
