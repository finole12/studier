package no.nith.nattogdag;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;



import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

public class MainActivity extends Activity implements OnClickListener {
	

	private EditText brukernavnEditText;
	private EditText passordEditText;
	private Button okButton;
	private String user;
	private String password;
	private ProgressDialog pd;
	
	
	
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
			String authentication = null;
			BufferedReader in;
			try {
				URL url = new URL("https://nattogdagprosjekt-nith.rhcloud.com/NattogDag/" +
						"JsonServlet?user=" + user + "&password=" + password +
								"&command=getAuthentication");
				in = new BufferedReader(
				        new InputStreamReader(url.openStream(), "UTF-8"));
				Scanner scanner = new Scanner(in);
				// Uses the regex \A, which matches the beginning of input, telling Scanner 
				// to tokenize the entire stream.
				authentication = scanner.useDelimiter("\\A").next();
				scanner.close();
				in.close();
			} catch (Exception e) {
				Log.e("Error connecting", e.toString());
			} 
			
			
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
