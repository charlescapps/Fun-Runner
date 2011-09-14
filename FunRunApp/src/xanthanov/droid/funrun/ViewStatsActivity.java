//Copyright (c) 2011 Charles L. Capps
//Released under MIT License

package xanthanov.droid.funrun; 

import xanthanov.droid.funrun.adapters.*; 
import xanthanov.droid.funrun.db.*; 
import xanthanov.droid.xantools.DroidLayout; 
import xanthanov.droid.xantools.DroidDialogs; 

import java.util.List; 
import java.sql.SQLException; 
import java.text.DateFormat; 
import java.text.SimpleDateFormat; 

import android.app.Activity; 
import android.app.Dialog; 
import android.app.AlertDialog; 
import android.os.Bundle; 
import android.widget.Gallery; 
import android.widget.AdapterView.OnItemClickListener; 
import android.widget.AdapterView; 
import android.widget.Toast; 
import android.widget.TextView; 
import android.widget.Button; 
import android.widget.ImageButton; 
import android.widget.LinearLayout; 
import android.widget.EditText; 
import android.view.View; 
import android.content.Context; 
import android.content.Intent; 
import android.content.res.Resources; 
import android.content.SharedPreferences; 
import android.content.DialogInterface; 

/**
*<h3>Activity for viewing old runs in a gallery view. </h3>
*<ul>
*<li>Has a &quot;Load Map&quot; button to view the run on a map -> ViewOldRunActivity. </li>
*<li>Uses Intent &quot;Extras&quot; to send the index of the run we're viewing to the ViewOldRunActivity.</li> 
*</ul>
*
*<h3>TODO</h3>
*<ul>
*<li>Figure out how to send any data type through an intent. Not really necessary but a good idea.</li>
*<li>Give an option to send a run to your email, or to post on Facebook (Google+?)</li>
*<li>Figure out how to customize the gallery view background more. May be difficult since it magically highlights when you touch it, etc.</li>
*<li>Obviously fix the disappearing arrow bug. Been lazy about this, it's probably just due to the state drawable.</li>
*</ul>
*
*@author Charles L. Capps
*
*@version 0.9b
**/

public class ViewStatsActivity extends Activity {

	private Gallery statsGallery; 
	private ViewStatsAdapter myAdapter; 
	private List<OldRun> oldRuns; 
	private FunRunReadOps dbReader; 
	private DroidLayout droidLay; 
	private Button returnButton; 
	private Button loadOnMapButton; 
	private ImageButton emailButton; 

	private Toast toastRunNumber; 

	private String DEFAULT_EMAIL; 

	public static final String RUN_INDEX_EXTRA = "RUN_INDEX_EXTRA"; 

	private static final int EMAIL_POPUP_ID = 0; 
	private static final int CHANGE_PREFS_POPUP_ID = 1; 
	private static final int EMAIL_SUCCESS_ID = 2; 

	private EditText emailText; 
	private TextView emailSuccessTitle; 
	private TextView emailSuccessText; 
	private SharedPreferences prefs; 

	private final DateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy h:mm aa"); 

	@Override
	public void onCreate(Bundle b) {
		super.onCreate(b); 
		setContentView(R.layout.view_stats); 

		prefs = android.preference.PreferenceManager.getDefaultSharedPreferences(this); 

		emailText = new EditText(this); 
		emailSuccessTitle = new TextView(this); 
		emailSuccessText = new TextView(this); 

		droidLay = new DroidLayout(this); 

		returnButton = (Button)findViewById(R.id.backToTitle); 
		loadOnMapButton = (Button)findViewById(R.id.loadRunOnMap); 
		emailButton = (ImageButton)findViewById(R.id.emailButton); 

		//Get the gallery from the layout 
		statsGallery = (Gallery) findViewById(R.id.statsGallery);
		dbReader = new FunRunReadOps(this); 
		try {
			oldRuns = dbReader.readOldRuns(); 
		}
		catch (SQLException e) {
			System.err.println("Error reading in old runs in ViewStatsActivity."); 
			e.printStackTrace(); 
			DroidDialogs.showPopup(this, "Error", "Error reading database of previous runs. Restart app and try again.", new AlertDialog.OnClickListener() {
				@Override
				public void onClick(android.content.DialogInterface dia, int id) {
					ViewStatsActivity.this.finish(); 
				}
			}); 
		}
		((FunRunApplication)getApplicationContext()).setOldRuns(oldRuns); 

		java.util.Collections.reverse(this.oldRuns); //Reverse so we see the newest runs first 
		myAdapter = new ViewStatsAdapter(this, oldRuns, statsGallery); 

		toastRunNumber = Toast.makeText(this, "Run " + (1)  + " of " + myAdapter.getCount(), Toast.LENGTH_SHORT); 

		statsGallery.setAdapter(myAdapter); 
		statsGallery.setOnItemClickListener(new OnItemClickListener() {
				public void onItemClick(AdapterView parent, View v, int position, long id) {
					toastRunNumber.setText("Run " + (position + 1)  + " of " + myAdapter.getCount()); 
					toastRunNumber.show(); 
				}
			});
		
		setupButtons(); 
		
	}

	@Override
	public void onStart() {
		super.onStart(); 
		grabPrefs(); 
	}

	private void setupButtons() {

		returnButton.setOnClickListener(new ClickReturn()); 
		if (myAdapter.isEmpty()) {
			loadOnMapButton.setEnabled(false); 
		}
		else {		
			loadOnMapButton.setOnClickListener(new ClickLoadOnMap()); 
		}

		emailButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					showDialog(EMAIL_POPUP_ID); 
					emailText.selectAll(); 
				}
			}); 
	}

	//Nested classes for buttons
	class ClickReturn implements View.OnClickListener {
		@Override
		public void onClick(View v) {
			ViewStatsActivity.this.finish(); 
		}
	}

	class ClickLoadOnMap implements View.OnClickListener {
		@Override
		public void onClick(View v) {
			Intent showMap = new Intent(ViewStatsActivity.this, ViewOldRunActivity.class); 
			showMap.putExtra(RUN_INDEX_EXTRA, myAdapter.getCurrentPosition()); 
			ViewStatsActivity.this.startActivity(showMap); 

		}
	}

	public void sendEmail(String email) {
		int position = statsGallery.getSelectedItemPosition(); 
		OldRun runToSend = oldRuns.get(position); 

		FunRunReadOps dbReader = new FunRunReadOps(this); 
		long totalPoints = 0; 

		try {
			totalPoints = dbReader.getTotalPoints(); 
		}
		catch (SQLException e) {
			System.err.println("ERROR getting total points from database."); 
			e.printStackTrace(); 
		}

		java.text.NumberFormat nf = java.text.NumberFormat.getInstance(); 
		String pointsText = nf.format(totalPoints); 

		final Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND); 
		emailIntent.setType("text/html"); 
		emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{email});
		emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Your Fun Run on " + dateFormat.format(runToSend.getRunDate()));  

		String htmlStr = "<b>Total lifetime points:</b>&nbsp;" + pointsText + "<br/>"; 
		htmlStr += "<h1 style=\"font-size:1.2em\"><u>Your Fun Run on " + dateFormat.format(runToSend.getRunDate()) + "</u></h1>";

		htmlStr += runToSend.toHtml(); 

		emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, android.text.Html.fromHtml(htmlStr)); 
		startActivity(Intent.createChooser(emailIntent, "Send email with:")); 

	}

	private void grabPrefs() {
		Resources res = getResources(); 

		String email_key = res.getString(R.string.email_pref); 
		String default_email = res.getString(R.string.default_email); 

		DEFAULT_EMAIL = prefs.getString(email_key, default_email); 

	}   

	@Override
	protected Dialog onCreateDialog(int id) {
		Dialog dialog;
		AlertDialog.Builder builder = new AlertDialog.Builder(this); 
		switch(id) {
			case EMAIL_POPUP_ID:
				// do the work to define the email Dialog
				emailText.setText(DEFAULT_EMAIL, TextView.BufferType.EDITABLE); 

				builder.setTitle("Enter e-mail address"); 
				builder.setView(emailText); 
				builder.setPositiveButton("Send", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						String email = emailText.getText().toString(); 
						if (!email.equals(DEFAULT_EMAIL)) {
							//Change default email preference to this email.
							showDialog(CHANGE_PREFS_POPUP_ID); 
						}
						sendEmail(email); 
					}
				}); 

				builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss(); 
					}

				}); 

				dialog= builder.create(); 

				break;
			case CHANGE_PREFS_POPUP_ID: 
 
				final String email = emailText.getText().toString(); 
				builder.setTitle("Change default email?");
				builder.setMessage("Would you like to change the default email to:\n" + email + "?");  
				builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						DEFAULT_EMAIL = email; 
						SharedPreferences.Editor prefEdit = prefs.edit(); 
						
						Resources res = getResources(); 
						String email_key = res.getString(R.string.email_pref); 
						prefEdit.putString(email_key, email); 
						prefEdit.commit(); 
						dialog.dismiss(); 
					}
				}); 
				builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss(); 
					}
				}); 

				dialog = builder.create(); 

				break; 

			case EMAIL_SUCCESS_ID: 
				builder.setCustomTitle(emailSuccessTitle); 
				builder.setView(emailSuccessText); 
				builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss(); 
					}
				}); 

				dialog = builder.create(); 

				break; 
			default:
				dialog = null;
		}
		return dialog;
	}

}
