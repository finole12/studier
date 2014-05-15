package no.nith.nattogdag;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends Activity implements OnClickListener {
	

	private EditText brukernavnEditText;
	private EditText passordEditText;
	private Button okButton;
	private String user;
	private String password;
	private ProgressDialog pd;
	private static final String SERVER_URL = "https://nattogdagprosjekt-nith.rhcloud.com/NattogDag/" +
			"JsonServlet";
	
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		brukernavnEditText = (EditText) findViewById(R.id.editText1);
		passordEditText = (EditText) findViewById(R.id.editText2);
		okButton = (Button) findViewById(R.id.mapButton);
		
		
		okButton.setOnClickListener(this);
		
	}
	


	@Override
	public void onClick(View view) {
		user = brukernavnEditText.getText().toString();
		password = passordEditText.getText().toString();
		new AuthorizeUser().execute(user, password);
		
	}
	
	class AuthorizeUser extends AsyncTask<String, Void, String> {
		@Override
		protected void onPreExecute() {
			pd = new ProgressDialog(MainActivity.this);
			pd.setTitle("Autoriserer bruker...");
			pd.setMessage("Vennligst vent.");
			pd.setCancelable(true);
			pd.setIndeterminate(true);
			pd.show();
		}
		
		@Override
		protected String doInBackground(String... params) {
			String user = params[0];
			String password = params[1];
			
			String authentication = Internet.sendPostRequest(SERVER_URL, user, password, 
					"getAuthentication");		
			
			String result = "";
			if (Boolean.parseBoolean(authentication)) {
				result = "ok";
				return result;
			} else {
				result = "Feil bruker eller passord";
				return result;
			}

			
		}
		
		@Override
		protected void onPostExecute(String result) {
			if (pd!=null) {
				pd.dismiss();
			}
			
			
			if(result.equals("ok")) {
				Intent intent = new Intent(MainActivity.this, MapActivity.class);
				intent.putExtra("user", user);
				intent.putExtra("password", password);
				startActivity(intent);
				brukernavnEditText.setText("");
				passordEditText.setText("");
				finish();
				
				
			} else {
				Context context = MainActivity.this;
				Toast.makeText(context, result, Toast.LENGTH_LONG).show();
			}
			
			
		}
		
	}


}
