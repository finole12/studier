package no.nith.nattogdag;


import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;

import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.format.DateFormat;
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
	private String delivered;
	private String returns;
	private MapActivity mapActivity;
	private Context context;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		stopID = getArguments().getString("stopID");
		user = getArguments().getString("user");
		password = getArguments().getString("password");
		mapActivity = (MapActivity) getActivity();
		context = getActivity();
		
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
	            	   delivered = deliveryText.getText().toString();
	            	   returns = returnsText.getText().toString();
	            	   
	            	   new sendReport().execute(user, password, stopID, delivered, returns);
	            	   
	            	   Log.d("Antall levert:", delivered);
	            	   Log.d("Antall retur:", returns);
	            	   Log.d("stopID:", stopID);
	            	   Log.d("user", user);
	            	   Log.d("password", password);
	            	   
	            	   
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
	
	public class sendReport extends AsyncTask<String, Void, String> {
		ProgressDialog pd;
		@Override
		protected void onPreExecute() {
			pd = new ProgressDialog(context);
			pd.setTitle("Sender rapport...");
			pd.setMessage("Vennligst vent.");
			pd.setCancelable(true);
			pd.setIndeterminate(true);
			pd.show();
		}
		@Override
		protected String doInBackground(String... params) {
			String user = params[0];
			String password = params[1];
			String stopID = params[2];
			String delivered = params[3];
			String returns = params[4];
			
			return Internet.sendDeliveryReport(user, password, stopID, delivered, returns);
		}
		
		@Override
		protected void onPostExecute(String result) {
			if (pd!=null) {
				pd.dismiss();
			}
			
			if(result.equals("1")) {
				Calendar cal = Calendar.getInstance();
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
				sdf.setTimeZone(TimeZone.getTimeZone("Europe/Oslo"));
				String dateTime = sdf.format(cal.getTime());
				
	     	   	mapActivity.disableMarker(dateTime, delivered, returns);
	     	   	
			    Toast.makeText(context, "Rapporten er lagret", 
						Toast.LENGTH_SHORT).show();
			} else {
				mapActivity.showErrorDialog(result);
			}
		}
	}
        	
}
