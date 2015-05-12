package uk.co.k9topebble.ui;
import uk.co.k9topebble.Preferences;
import uk.co.k9topebble.R;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
 
public class UserSettingActivity extends PreferenceActivity /*implements Preference.OnPreferenceChangeListener*/{
 
    // add click listeners
    /*CheckBoxPreference pQuiet; 
    CheckBoxPreference pEnabled;
    TimePreference pStart; 
    TimePreference pEnd; 
    CheckBoxPreference pSilent;
    
	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		// update the time in the displays
		// enable and disable the options.
		
		boolean bEnabled = Preferences.enabled(preference.getContext());
		boolean bQuiet = Preferences.quietTime(preference.getContext());
		pQuiet.setSelectable(bEnabled);
		pStart.setSelectable(bEnabled && bQuiet);
		pEnd.setSelectable(bEnabled && bQuiet);
		pSilent.setSelectable(bEnabled);

		return true;
	}*/
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        addPreferencesFromResource(R.xml.settings);
        
        // add click listeners
        /*pEnabled = (CheckBoxPreference)findPreference(getString(R.string.PREFS_ENABLED));
        pQuiet = (CheckBoxPreference)findPreference(getString(R.string.PREFS_QUIET));
        pSilent = (CheckBoxPreference)findPreference(getString(R.string.PREFS_SILENT));
        pStart = (TimePreference)findPreference(getString(R.string.PREFS_START_TIME));
        pEnd = (TimePreference)findPreference(getString(R.string.PREFS_END_TIME));
        */
        /*pEnabled.setOnPreferenceChangeListener(this);
        pQuiet.setOnPreferenceChangeListener(this);
        pStart.setOnPreferenceChangeListener(this);
        pEnd.setOnPreferenceChangeListener(this);
        pSilent.setOnPreferenceChangeListener(this);
*/

    }
}