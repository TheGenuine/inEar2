package de.reneruck.inear2;

import java.io.File;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class EditAudiobookDialogFragment extends DialogFragment {

	private String audibookName;

	public EditAudiobookDialogFragment() {
	}
	
	public EditAudiobookDialogFragment(String audibookName) {
		this.audibookName = audibookName;
	}

	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		setCancelable(true);
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(audibookName);
	    ListView list = new ListView(getActivity());
	    String[] stringArray = getActivity().getResources().getStringArray(R.array.array_dialog_entries);
		list.setAdapter(new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, 
	    		stringArray));
		list.setOnItemClickListener(this.onItemClickListener);
	    builder = new AlertDialog.Builder(getActivity());
	    builder.setNegativeButton("Cancel", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
	    builder.setView(list);

	    return builder.create();

	}

	private OnItemClickListener onItemClickListener = new OnItemClickListener() {
		public void onItemClick(AdapterView<?> adapter, View view, int pos, long id) {
			switch (pos) {
			case 0: // rename
				renameAudiobook();
				break;
			case 1: // reset playlist
				resetPlaylist();
				break;
			case 2: // delete
				FragmentTransaction ft = getFragmentManager().beginTransaction();
				final OnClickListener deleteListener = new OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						String path = ((AppContext)getActivity().getApplicationContext()).getAudiobokkBaseDir() + File.separator + audibookName;
						File f = new File(path);
						if(f.exists() && f.isDirectory())
						{
							Toast.makeText(getActivity(), "Deleting " + path, Toast.LENGTH_LONG).show();
						}
					}
				};
				ft.add(new DialogFragment(){
					public Dialog onCreateDialog(Bundle savedInstanceState) {
						return new AlertDialog.Builder(getActivity()).setTitle("Delete")
								.setMessage("Do you really want to delete this Audiobook")
								.setPositiveButton("yes", deleteListener)
								.setNegativeButton("no", new OnClickListener() {
									
									@Override
									public void onClick(DialogInterface dialog, int which) {
										dismiss();
									}
								}).create();
					}
				}, "confirm");
				ft.commit();
				break;
			}
		}
	};
	
	private void renameAudiobook() {
		dismiss();
	}
	
	private void resetPlaylist() {
		AppContext appContext = (AppContext) getActivity().getApplicationContext();
		File playlistFile = new File(appContext.getAudiobokkBaseDir() + File.separator + this.audibookName + File.separator + this.audibookName + ".m3u");
		if(playlistFile.exists()) {
			playlistFile.delete();
			appContext.runFilescanner();
		}
		dismiss();
	}   

}
