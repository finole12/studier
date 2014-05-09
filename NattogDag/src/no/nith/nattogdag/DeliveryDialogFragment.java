package no.nith.nattogdag;

import android.app.AlertDialog;
import android.app.Dialog;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.Toast;

public class DeliveryDialogFragment extends DialogFragment {
	
	private Dialog dialog;
	View parentView;
	private TextView addressTextView;
	private String stopID;
	private String user;
	private String password;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		stopID = getArguments().getString("stopID");
		user = getArguments().getString("user");
		password = getArguments().getString("password");
		
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
	    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
	    // Get the layout inflater
	    LayoutInflater inflater = getActivity().getLayoutInflater();
	    
	    // Inflate and set the layout for the dialog
	    // Pass null as the parent view because its going in the dialog layout
	    builder.setView(inflater.inflate(R.layout.dialog_signin, null))
	    // Add action buttons
	           .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
	               @Override
	               public void onClick(DialogInterface dialog, int id) {
	            	   Dialog f = (Dialog) dialog;
	            	   EditText deliveryText = (EditText) f.findViewById(R.id.delivery);
	            	   EditText returnsText = (EditText) f.findViewById(R.id.returns);
	            	   String delivered = deliveryText.getText().toString();
	            	   String returns = returnsText.getText().toString();
	            	   Internet.sendDeliveryReport(user, password, stopID, delivered, returns);
	            	   
	            	   Log.d("Antall levert:", delivered);
	            	   Log.d("Antall retur:", returns);
	            	   Log.d("stopID:", stopID);
	            	   Log.d("user", user);
	            	   Log.d("password", password);
	            	   
//	            	   MapActivity mapActivity = (MapActivity) getActivity();
//	            	   mapActivity.disableMarker();
	               }
	           })
	           .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
	               public void onClick(DialogInterface dialog, int id) {
	            	   DeliveryDialogFragment.this.getDialog().cancel();
	               }
	           });      
	   
	    
	    return builder.create();
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
				

//		dialog = getDialog();
//		
//		
//	    addressTextView = (TextView) dialog.findViewById(R.id.deliveryView);
//	    addressTextView.setText("Hallo");
//		    
//		    Toast.makeText(getActivity(), addressTextView.getText(), 
//					Toast.LENGTH_SHORT).show(); 
		    
		return super.onCreateView(inflater, container, savedInstanceState);
	}
	
//	@Override
//	public void onStart() {
//		
//		View view = getView();
//		    addressTextView = (TextView) view.findViewById(R.id.deliveryView);
//		    addressTextView.setText("Hallo");
//		    
//		super.onStart();
//	}
	
	public void setText(String stopAddress) {
		addressTextView.setText(stopAddress);
	}
        	
}
