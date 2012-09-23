package de.reneruck.inear2;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class EditAudiobookDialogFragment extends DialogFragment {

	private String audibookName;

	public EditAudiobookDialogFragment(String audibookName) {
		this.audibookName = audibookName;
	}

	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		setCancelable(true);
//		int style = DialogFragment.STYLE_NORMAL, theme = 0;
//		setStyle(style, theme);
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
		}   
	};

}
