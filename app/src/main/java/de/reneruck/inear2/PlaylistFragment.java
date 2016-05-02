package de.reneruck.inear2;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import android.app.ListFragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import de.reneruck.inear2.service.PlaybackService;

public class PlaylistFragment extends ListFragment implements PropertyChangeListener{
	
	private AppContext appContext;
	private PlaylistAdapter listAdapter;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.appContext = (AppContext) getActivity().getApplicationContext();
		this.appContext.getCurrentAudiobook().addPropertyChangeListener(this);

		List<String> currentPlaylist = this.appContext.getCurrentAudiobook().getPlaylist();
		this.listAdapter = new PlaylistAdapter(getActivity(), R.layout.playlist_entry, currentPlaylist);
		setListAdapter(this.listAdapter);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View inflated = inflater.inflate(R.layout.fragment_playlist, container);
		return inflated;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		getListView().setSelection(this.appContext.getCurrentAudiobook().getCurrentTrack());
	}

	@Override
	public void onListItemClick(ListView l, View view, int position, long id) {
		if(!"seperator".equals(view.getTag())){
			Intent i = new Intent(PlaybackService.ACTION_SET_TRACK);
			i.putExtra(PlaybackService.ACTION_SET_TRACK_NR, position);
			appContext.sendBroadcast(i);
		}
	}
	
	@Override
	public void propertyChange(PropertyChangeEvent event) {
		if("track".equals(event.getPropertyName()))
		{
			if(getListView() != null)
			{
				this.listAdapter.notifyDataSetChanged();
				getListView().setSelection(this.appContext.getCurrentAudiobook().getCurrentTrack());
			}
		}
	}
}
