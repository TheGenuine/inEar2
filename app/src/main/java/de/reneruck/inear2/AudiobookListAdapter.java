package de.reneruck.inear2;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class AudiobookListAdapter extends BaseAdapter {

	private Activity mActivity;
	private ArrayList<AudioBook> mData;

	private static LayoutInflater inflater = null;

	public AudiobookListAdapter(Activity activity, List<AudioBook> list) {
		mActivity = activity;
		mData = new ArrayList<>(list);
		inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	public int getCount() {
		return mData.size();
	}

	public Object getItem(int position) {
		return position;
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		View vi = convertView;

		if (convertView == null){
			vi = inflater.inflate(R.layout.audiobook_list_item, null);
		}

		TextView title = (TextView) vi.findViewById(R.id.title); // title
		TextView artist = (TextView) vi.findViewById(R.id.artist); // artist
																	// name
		TextView duration = (TextView) vi.findViewById(R.id.duration); // duration
		ImageView thumb_image = (ImageView) vi.findViewById(R.id.list_image);

		String audiobook = mData.get(position).getName();

		String[] split = audiobook.split("-");
		
		if(split.length > 1) {
			artist.setText(split[0].trim());
			title.setText(split[1].trim());
		} else {
			title.setText(split[0].trim());
		}
//		duration.setText();
		return vi;
	}
}
