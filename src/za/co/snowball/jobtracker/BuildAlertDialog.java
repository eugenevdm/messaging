package za.co.snowball.jobtracker;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class BuildAlertDialog extends Activity {
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Dialog dialog = new Dialog(this);
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Select Color Mode");

		ListView modeList = new ListView(this);
		String[] stringArray = new String[] { "Bright Mode", "Normal Mode" };
		ArrayAdapter<String> modeAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1, stringArray);
		modeList.setAdapter(modeAdapter);

		builder.setView(modeList);
		dialog = builder.create();
		
		dialog.show();
		
	}

}
