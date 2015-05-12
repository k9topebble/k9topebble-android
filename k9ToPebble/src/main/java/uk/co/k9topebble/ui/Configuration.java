package uk.co.k9topebble.ui;

import java.util.Calendar;
import java.util.GregorianCalendar;

import uk.co.k9topebble.K9Defines;
import uk.co.k9topebble.R;

import uk.co.k9topebble.Preferences;

import android.net.Uri;
import android.os.Bundle;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.ToggleButton;

public class Configuration extends FragmentActivity {

	private Button btnChangeFromTime;
	private Button btnChangeToTime;
	private Button btnInstallWatchApp;
	private CheckBox chkBtnQuiet;
	private CheckBox chkBtnSilent;
	private ToggleButton btnEnabled;

	static final int TIME_FROM_DIALOG_ID = 999;
	static final int TIME_TO_DIALOG_ID = 998;

	public static class fromTime implements TimePickerDialog.OnTimeSetListener
	{
		Configuration m_parent;
		fromTime(Configuration parent)
		{
			m_parent = parent;
		}

		@Override
		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
			// TODO Auto-generated method stub
			//Preferences.quietTimeFrom(view.getContext(), hourOfDay, minute);
			m_parent.updateButtons();
		}
	}
	public static class toTime implements TimePickerDialog.OnTimeSetListener
	{
		Configuration m_parent;
		toTime(Configuration parent)
		{
			m_parent = parent;
		}

		@Override
		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
			// TODO Auto-generated method stub
			//Preferences.quietTimeTo(view.getContext(), hourOfDay, minute);
			m_parent.updateButtons();
		}
	}
	
	
	public static class TimePickerFragment extends DialogFragment {

		private int m_hour = 0;
		private int m_minute = 0;
		private OnTimeSetListener m_lis;
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			// Use the current time as the default values for the picker

			// Create a new instance of TimePickerDialog and return it
			return new TimePickerDialog(getActivity(), m_lis, m_hour, m_minute,
					DateFormat.is24HourFormat(getActivity()));
		}

		public void setTime(OnTimeSetListener lis, int hour, int minute)
		{
			m_lis = lis;
			m_hour = hour; 
			m_minute = minute;
		}

		
		public int getHour() {return m_hour;}

		public int getMinute() {return m_minute;}
	}
	

	public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
		//Preferences.quietTimeFrom(view.getContext(), hourOfDay, minute);
		updateButtons();
	}
	
	public void showTimePickerDialogFrom(View v) {
		TimePickerFragment newFragment = new TimePickerFragment();
		newFragment.setTime(new fromTime(this), Preferences.quietTimeStartHour(v.getContext()), Preferences.quietTimeStartMinute(v.getContext()));
	    newFragment.show(getSupportFragmentManager(), "timePickerFrom");
	}

	public void showTimePickerDialogTo(View v) {
		TimePickerFragment newFragment = new TimePickerFragment();
		newFragment.setTime(new toTime(this), Preferences.quietTimeEndHour(v.getContext()), Preferences.quietTimeEndMinute(v.getContext()));
		newFragment.show(getSupportFragmentManager(), "timePickerTo");
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_configuration);

		// update view

		btnChangeFromTime = (Button) findViewById(R.id.btnFrom);
		btnChangeToTime = (Button) findViewById(R.id.btnTo);
		chkBtnQuiet = (CheckBox) findViewById(R.id.chkBtnQuiet);
		chkBtnSilent = (CheckBox) findViewById(R.id.chkBtnSilent);
		btnEnabled = (ToggleButton) findViewById(R.id.btnEnabled);
		btnInstallWatchApp = (Button) findViewById(R.id.btnInstallWatchApp);
		TextView txtVersion = (TextView) findViewById(R.id.txtVersion);

		chkBtnQuiet.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Preferences.quietTime(getApplicationContext(), chkBtnQuiet.isChecked());
				updateButtons();
			}
		});
		chkBtnSilent.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Preferences.silentTime(getApplicationContext(), chkBtnSilent.isChecked());
				updateButtons();
			}
		});		btnEnabled.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Preferences.enabled(getApplicationContext(), btnEnabled.isChecked());
				updateButtons();
			}
		});
		btnInstallWatchApp.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

				Uri uri = Uri.parse(K9Defines.WATCHFACE_URL);
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
			
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		updateButtons();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.norification_details, menu);
		return true;
	}

	private void updateButtons()
	{

		GregorianCalendar cal = new GregorianCalendar();
		cal.set(Calendar.HOUR_OF_DAY, Preferences.quietTimeStartHour(this));
		cal.set(Calendar.MINUTE, Preferences.quietTimeStartMinute(this));
		btnChangeFromTime.setText(
				DateFormat.getTimeFormat(this/*Context*/).format(cal.getTime()));
	    		
		cal.set(Calendar.HOUR_OF_DAY, Preferences.quietTimeEndHour(this));
		cal.set(Calendar.MINUTE, Preferences.quietTimeEndMinute(this));
		btnChangeToTime.setText(
				DateFormat.getTimeFormat(this/*Context*/).format(cal.getTime()));

		chkBtnQuiet.setEnabled(Preferences.enabled(this));
		chkBtnQuiet.setChecked(Preferences.quietTime(this));
		chkBtnSilent.setChecked(Preferences.silent(this));
		btnChangeToTime.setEnabled(Preferences.quietTime(this));
		btnChangeFromTime.setEnabled(Preferences.quietTime(this));
	}
}
