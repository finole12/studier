package no.nith.nattogdag;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

public class ResetDialogFragment extends DialogFragment {
	
	private MapActivity mapActivity;

	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		mapActivity = (MapActivity) getActivity();
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setMessage("Er du sikker på at du vil tilbakestille alle punktene til " +
				"\"ikke levert?\"")
        .setCancelable(true)
        .setPositiveButton("Ja", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                 mapActivity.resetMarkers();
            }
        })
        .setNegativeButton("Nei", null);
    
		
		return builder.create();
	}
	

}
