package uk.co.k9topebble.ui;

import java.util.ArrayList;

import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.PebbleKit.FirmwareVersionInfo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import uk.co.k9topebble.InboxService;
import uk.co.k9topebble.K9Defines;
import uk.co.k9topebble.K9Integration;
import uk.co.k9topebble.K9Reciever;
import uk.co.k9topebble.MessageQueue;
import uk.co.k9topebble.Preferences;
import uk.co.k9topebble.R;
import uk.co.k9topebble.emailRecord;
 
public class MainActivity extends Activity {
 
    private static final int RESULT_SETTINGS = 1;
    private static final int RESULT_CLOSED = 2;
 
    int watchVersion = 1;
    
    Button btnInstallWatchApp;
    
    public int getVersion() {
        int v = 0;
        try {
            v = getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
        } catch (NameNotFoundException e) {
            // Huh? Really?
        }
        return v;
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
 
        showUserSettings();
        
		TextView txtVersion = (TextView) findViewById(R.id.txtVersion);
		
		btnInstallWatchApp = (Button) findViewById(R.id.btnInstall);
		
		btnInstallWatchApp.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

				String urlString = "http://www.k9topebble.co.uk/releases/" + getVersion() + "/K9ToPebble.pbw";
				if (watchVersion == 2)
				{
					//urlString = "pebble://appstore/52cc6ca9d76e998cf000002f";
					urlString = "http://www.k9topebble.co.uk/releases/" + getVersion() + "/K9ToPebbleSDK2.pbw";
				}
				
				Uri uri = Uri.parse(urlString);
				Intent intent = new Intent(Intent.ACTION_VIEW, uri);
				intent.setDataAndType(uri,"application/octet-stream");
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
			}
		});		
		
		

		try {
			String versionName = getPackageManager()
				    .getPackageInfo(getPackageName(), 0).versionName;
			String versionmsg = getString(R.string.version) + versionName;
			txtVersion.setText(versionmsg);
	        //testPermissionsTask task = new testPermissionsTask();
	        //task.execute();
			
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		/*IntentFilter filter = new IntentFilter();
		filter.addAction("com.getpebble.action.app.RECEIVE");
		filter.addAction("com.getpebble.action.app.RECEIVE_ACK");
		filter.addAction("com.getpebble.action.app.RECEIVE_NACK");
		filter.addAction("com.getpebble.action.app.PEBBLE_CONNECTED");
		filter.addAction("com.getpebble.action.app.PEBBLE_DISCONNECTED");

		K9Reciever reciever = new K9Reciever();
		registerReceiver(reciever, filter);*/

    }
    @Override
    public void onResume() {
        super.onResume();
 
        showUserSettings();
    } 
    @Override
    public void onStart() {
        super.onStart();
 
        showUserSettings();
        
        
        Intent serviceIntent = new Intent(this, InboxService.class);
    	serviceIntent.putExtra(K9Defines.COMMAND_EXTRA, K9Defines.COMMAND_INBOX_CHANGED);
    	startService(serviceIntent);        	
    	
    	//inboxRefreshTask task = new inboxRefreshTask(this);
    	//task.execute();

        ArrayList<emailRecord> m_emails;
    	m_emails = new ArrayList<emailRecord>();
    	K9Integration.calculateInbox(getApplicationContext(), m_emails);

    } 
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.settings, menu);
        return true;
    }
 
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_settings)
        {
            Intent i = new Intent(getApplicationContext(), UserSettingActivity.class);
            startActivityForResult(i, RESULT_SETTINGS);
        }
 
        return true;
    }
    
    public void onOptionsMenuClosed(Menu menu) {
        Intent i = new Intent(this, UserSettingActivity.class);

        startActivityForResult(i, RESULT_CLOSED);
        return;
    }
    
    protected boolean isAppInstalled(Context context)
    {
    	String appname = Preferences.inboxProvider(getApplicationContext());
        PackageManager pm = getPackageManager();
        boolean app_installed = false;
        try
        {
               pm.getPackageInfo(appname, PackageManager.GET_ACTIVITIES);
               app_installed = true;
        }
        catch (PackageManager.NameNotFoundException e)
        {
               app_installed = false;
        }
        return app_installed ;
    }
    
    protected boolean doesAppHavePermission(Context context)
    {
    	String appname = Preferences.inboxProvider(getApplicationContext());

   	    String permission = appname + ".permission.READ_MESSAGES";
   	    int res = context.checkCallingOrSelfPermission(permission);
		return (res == PackageManager.PERMISSION_GRANTED);            
    }
 
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.e("MainActivity", "OnActivityResult " + requestCode);
        switch (requestCode) {
        case RESULT_SETTINGS:
            /*showUserSettings();
        	inboxRefreshTask task = new inboxRefreshTask(this);
        	task.execute();*/
            break;
        case RESULT_CLOSED:
        	Preferences.commit(this);
            Intent serviceIntent = new Intent(this, InboxService.class);
        	serviceIntent.putExtra(K9Defines.COMMAND_EXTRA, K9Defines.COMMAND_INBOX_CHANGED);
        	startService(serviceIntent);      
    		/*SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
    		SharedPreferences.Editor editor = settings.edit();
    		editor.commit();
            showUserSettings();
        	inboxRefreshTask task = new inboxRefreshTask(this);
        	task.execute();*/            
            break;
            
        }
 
    }
 
    private void showUserSettings() {
        TextView settingsTextView = (TextView) findViewById(R.id.textUserSettings);
    	
    	StringBuilder builder = new StringBuilder();
        builder.append("<br>Change settings in settings menu.\n");
        builder.append("<br>Enabled: " + Preferences.enabled(getApplicationContext()));
        //builder.append("<br>Send body: " + Preferences.sendBody(getApplicationContext()));
        
        //builder.append("<br>Quiet time:" + Preferences.quietTime(getApplicationContext()));
        /*if (Preferences.quietTime(this))
        {
        	builder.append(
        			" " + 
        			getString(R.string.quiet_from) + ": " +
        			Preferences.quietTimeStart(getApplicationContext()) + 
        			" - " + 
        			getString(R.string.quiet_to) + ": " +
        			Preferences.quietTimeEnd(getApplicationContext()));
        }*/
        //builder.append("<br>Block notifications when phone silent: " + Preferences.silent(getApplicationContext()));
        //builder.append("<br>Inbox size: " + Preferences.inboxSize(getApplicationContext()));
        //builder.append("<br>Unread only? " + Preferences.unreadOnly(getApplicationContext()));
        builder.append("<br>Email app: " + Preferences.inboxProviderTitle(getApplicationContext()));
        if (!isAppInstalled(this))
        {
            builder.append("<br><font color=\"red\"> WARNING, the app " + 
            		Preferences.inboxProviderTitle(getApplicationContext()) + " is not installed </font>");
        }
        else
        {
            if (!doesAppHavePermission(getApplicationContext()))
            {
                builder.append("<br><font color=\"red\"> WARNING, do not have permission to read emails.  Please reinstall K9ToPebble. </font>");
            }

        }
        //if (PebbleKit.isWatchConnected(getBaseContext()))
        {
        	try
        	{
        		FirmwareVersionInfo fv = PebbleKit.getWatchFWVersion(getApplicationContext());
        		if (fv == null)
        		{
        			builder.append("<br>Watch not connected ");
        			btnInstallWatchApp.setEnabled(false);
        		}
        		else
        		{
        			builder.append("<br>Connected watch version " + fv.getMajor() + "." + fv.getMinor() + "." + fv.getPoint());
        			watchVersion = fv.getMajor();
        		}
        	}
        	catch (Exception e)
        	{
        		builder.append("<br>Connected with watch version 1");
        	}
        }
        //else
        {
        	//btnInstallWatchApp.setEnabled(false);
        	//builder.append("<br>Watch not connected ");
        }
        /*if (doesAppHavePermission(getApplicationContext()))
        {
        	String names[] = K9Integration.getAccounts(getApplicationContext());
        	if (names == null)
        	{
        		builder.append("<br><font color=\"red\"> WARNING, no accounts visisble, to make accounts visible add them to the unified folder. </font>");
        	}
        	else
        	{
        		builder.append("<br>Accounts: ");
        		for (int i = 0; i < names.length; i++)
        		{
        			builder.append(names[i] + " ");
        		}
        	}
        }*/
       // TextView settingsTextView = (TextView) findViewById(R.id.textUserSettings);
        settingsTextView.setText(Html.fromHtml(builder.toString()));

        /*
        boolean read = testPermissions();
        if (!read)
        {
            builder = new StringBuilder();
            builder.append("\nFailed to get permission to read mails! Please reinstall android app.");
            TextView errorTextView = (TextView) findViewById(R.id.textErrorMsg);
            errorTextView.setText(builder.toString());
        }*/
    }
    
    private class testPermissionsTask extends AsyncTask<Void, Void, Boolean> {
    	
        @Override
        protected Boolean doInBackground(Void... blank) {
        	boolean result = false;
          try
          {
        	  result =  testPermissions();
        	  
            } catch (Exception e) {
              e.printStackTrace();
            }
          
          return result;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (!result)
            {
                StringBuilder builder = new StringBuilder();
                builder.append("\nFailed to get permission to read mails! Please reinstall android app.");
                TextView errorTextView = (TextView) findViewById(R.id.textErrorMsg);
                errorTextView.setText(builder.toString());
            }

        }
      }

    
    
    private class inboxRefreshTask extends AsyncTask<Void, Void, Void> {

        Context context;

        public inboxRefreshTask(Context context) {
            this.context = context;
        }   	

        @Override
        protected Void doInBackground(Void... blank) {
            Log.e("MainActivity", "doInBackground ");

        	Intent serviceIntent = new Intent(this.context, InboxService.class);
        	serviceIntent.putExtra(K9Defines.COMMAND_EXTRA, K9Defines.COMMAND_INBOX_CHANGED);
        	startService(serviceIntent); 
        	return null;
        }

      }

    
    boolean testPermissions()
    {
    	Uri k9Uri = Uri.parse("content://com.fsck.k9.messageprovider/inbox_messages");


    	android.util.Log.e("MSG", "Start permission check");
    	try {
    		Cursor cur = getContentResolver().query(k9Uri, null, null, null, null);
    		if (cur != null)
    		{
    			cur.close();
    		}
    		android.util.Log.e("MSG", "Permissions ok");
			return true;
    	} catch (java.lang.SecurityException e) {
    		// TODO Auto-generated catch block
    		e.printStackTrace();
    		android.util.Log.e("MSG", "Problem with android app permissions, please reinstall");

    		return false;

    	}
    }
 
}