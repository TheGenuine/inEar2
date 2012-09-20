package de.reneruck.inear2;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import de.reneruck.inear2.service.PlaybackService;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class PlaylistFragment extends Fragment implements PropertyChangeListener{
	
	private AppContext appContext;
	private List<String> currentPlaylist;
	private ListView playlistView;
	private PlaylistAdapter listAdapter;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.appContext = (AppContext) getActivity().getApplicationContext();
		this.appContext.getCurrentAudiobookBean().addPropertyChangeListener(this);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View inflated = inflater.inflate(R.layout.fragment_playlist, container);
		this.playlistView = (ListView) inflated.findViewById(R.id.playlist);
		setupListView();
		return inflated;
	}

	private void setupListView() {
		this.currentPlaylist = this.appContext.getCurrentAudiobookBean().getPlaylist();
		this.listAdapter = new PlaylistAdapter(this.appContext, R.layout.playlist_entry, currentPlaylist);
		this.playlistView.setAdapter(this.listAdapter);
		this.playlistView.setOnItemClickListener(this.onPlaylistItemListener);
		this.playlistView.setSelection(this.appContext.getCurrentAudiobookBean().getCurrentTrack());
	}
	
	private OnItemClickListener onPlaylistItemListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> adapter, View view, int pos, long id) {
			if(!"seperator".equals(view.getTag())){
				Intent i = new Intent(PlaybackService.ACTION_SET_TRACK);
				i.putExtra(PlaybackService.ACTION_SET_TRACK_NR, pos);
				appContext.sendBroadcast(i);
			}
		}
	};

	@Override
	public void propertyChange(PropertyChangeEvent event) {
		if("track".equals(event.getPropertyName()))
		{
			if(this.playlistView != null)
			{
				this.listAdapter.notifyDataSetChanged();
				this.playlistView.setSelection(this.appContext.getCurrentAudiobookBean().getCurrentTrack());
			}
		}
	}
}
