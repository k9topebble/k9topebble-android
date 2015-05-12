package uk.co.k9topebble;

import java.util.Calendar;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.preference.PreferenceManager;
import android.util.Log;

public class Preferences {
	private static final String TAG = "Preferences";
	
	private static SharedPreferences getShared(Context context)
	{
		return context.getApplicationContext().getSharedPreferences( 
				context.getApplicationContext().getPackageName() + "_preferences", Context.MODE_PRIVATE | Context.MODE_MULTI_PROCESS);
	}
	public static void commit(Context context)
	{
		SharedPreferences settings = getShared(context);
		SharedPreferences.Editor editor = settings.edit();
		editor.commit();		
	}

	public static boolean enabled(Context context)
	{
		SharedPreferences settings = getShared(context);
		return settings.getBoolean(context.getString(R.string.PREFS_ENABLED), true);
	}
	public static void enabled(Context context, boolean on)
	{
		SharedPreferences settings = getShared(context);
		SharedPreferences.Editor editor = settings.edit();
		editor.putBoolean(context.getString(R.string.PREFS_ENABLED), on);	
		Log.e(TAG, "Enabled state: " + on );
		editor.commit();
	}
	
	public static boolean sendBody(Context context)
	{
		SharedPreferences settings = getShared(context);
		return settings.getBoolean(context.getString(R.string.PREFS_BODY), true);
	}
	
	public static boolean quietTime(Context context)
	{
		SharedPreferences settings = getShared(context);
		return settings.getBoolean(context.getString(R.string.PREFS_QUIET), false);		
	}
	public static void quietTime(Context context, boolean on)
	{
		SharedPreferences settings = getShared(context);
		SharedPreferences.Editor editor = settings.edit();
		editor.putBoolean(context.getString(R.string.PREFS_QUIET), on);
		Log.e(TAG, "Quiet state: " + on );
		editor.commit();
	}
	public static void silentTime(Context context, boolean on) {
		SharedPreferences settings = getShared(context);
		SharedPreferences.Editor editor = settings.edit();
		editor.putBoolean(context.getString(R.string.PREFS_SILENT), on);
		Log.e(TAG, "Silent state: " + on );
		editor.commit();
	}

	public static int getHour(String time) {
		String[] pieces=time.split(":");

		return(Integer.parseInt(pieces[0]));
	}

	public static int getMinute(String time) {
		String[] pieces=time.split(":");

		return(Integer.parseInt(pieces[1]));
	}
	
	public static String quietTimeStart(Context context)
	{
		SharedPreferences settings = getShared(context);
		return settings.getString(context.getString(R.string.PREFS_START_TIME), "22:00");	
	}
	public static String quietTimeEnd(Context context)
	{
		SharedPreferences settings = getShared(context);

		return settings.getString(context.getString(R.string.PREFS_END_TIME), "7:00");	
	}
	public static String inboxProvider(Context context)
	{
		String inboxProviderTitle = inboxProviderTitle(context);
		if (inboxProviderTitle.equals(context.getString(R.string.com_fsck_k9)))
		{
			return "com.fsck.k9";
		} else if (inboxProviderTitle.equals(context.getString(R.string.com_kaitenmail)))
		{
			return "com.kaitenmail";

		} else if (inboxProviderTitle.equals(context.getString(R.string.com_kaitenmail_adsupported)))
		{
			return "com.kaitenmail.adsupported";
		}   
		else if (inboxProviderTitle.equals(context.getString(R.string.org_koxx_k9ForPureWidget)))
		{
			return "org.koxx.k9ForPureWidget";
		}   
		return "com.fsck.k9";
	}
	
	public static String inboxProviderTitle(Context context)
	{
		SharedPreferences settings = getShared(context);

		return settings.getString(context.getString(R.string.PREFS_INBOX_PROVIDER), 
				context.getString(R.string.com_fsck_k9));	
	}
	
	public static int quietTimeStartHour(Context context)
	{
		String time = quietTimeStart(context);
		if (time.length() > 1){
			return getHour(time);
		}
		return 22;
	}
	public static int quietTimeStartMinute(Context context)
	{
		String time = quietTimeStart(context);
		if (time.length() > 1){
			return getMinute(time);
		}
		return 0;
	}

	public static int quietTimeEndHour(Context context)
	{
		String time = quietTimeEnd(context);
		if (time.length() > 1){
			return getHour(time);
		}
		return 7;
	}
	public static int quietTimeEndMinute(Context context)
	{
		String time = quietTimeEnd(context);
		if (time.length() > 1){
			return getMinute(time);
		}
		return 0;
	}

	public static boolean canSend(Context context)
	{
		SharedPreferences settings = getShared(context);
		boolean enabled = settings.getBoolean(context.getString(R.string.PREFS_ENABLED), true);
		boolean silent = settings.getBoolean(context.getString(R.string.PREFS_SILENT), true);
		Log.e(TAG, "Can send test");

		if (silent)
		{
			AudioManager am = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
			Log.e(TAG, "Silent enabled, audio mode " + am.getRingerMode());

			if (am.getRingerMode() == AudioManager.RINGER_MODE_SILENT)
			{
				Log.i("MyApp","Silent mode");
				return false;
			}
		}

		if (!enabled)
		{
			Log.e(TAG, "canSend - no, not enabled");
			return false;
		}
		boolean quiet = settings.getBoolean(context.getString(R.string.PREFS_QUIET), true);		
		if (!quiet)
		{
			Log.e(TAG, "canSend - yes, quiet not enabled");
			return true;
		}

		int sh = quietTimeStartHour(context);
		int sm = quietTimeStartMinute(context);
		int eh = quietTimeEndHour(context);
		int em = quietTimeEndMinute(context);

		// so, we are enabled but there is a quiet time.
		// need to figure out if we are in the quite zone

		// construct easier values to work with
		int start = (sh << 8) + sm;
		int end = (eh << 8) + em;
		Calendar c = Calendar.getInstance(); 
		int current = (c.get(Calendar.HOUR_OF_DAY) << 8) + c.get(Calendar.MINUTE);
		Log.e(TAG, "Current time " + c.get(Calendar.HOUR_OF_DAY) + ":" + c.get(Calendar.MINUTE));
		Log.e(TAG, "canSend - start: " +start + " end: " + end + " current: " + current);
		if (start > end)
		{
			/// typical case, the start time is later than the end time, eg 11pm to 7am
			if (current >= start)
			{
				return false;
			} else if (current <= end)
			{
				return false;
			} else
			{
				return true;
			}
		} else
		{
			// quiet time during the day?  ok then
			if ((current >= start) && (current <= end))
			{
				return false;
			}
			else
			{
				return true;
			}
		}
	}
	public static boolean silent(Context context) {
		SharedPreferences settings = getShared(context);
		return settings.getBoolean(context.getString(R.string.PREFS_SILENT), false);		
	}
	public static int inboxSize(Context context) {
		SharedPreferences settings = getShared(context);
		String inboxSize = settings.getString(context.getString(R.string.PREFS_INBOX_SIZE), "10");	
		return Integer.decode(inboxSize);
	}

	public static boolean unreadOnly(Context context)
	{
		SharedPreferences settings = getShared(context);
		return settings.getBoolean(context.getString(R.string.PREFS_UNREAD_ONLY), false);		
	}

	public static String topUUID(Context context)
	{
		SharedPreferences settings = getShared(context);
		return settings.getString(context.getString(R.string.PREFS_UUID), "");
	}
	public static void topUUID(Context context, String uuid)
	{
		SharedPreferences settings = getShared(context);
		SharedPreferences.Editor editor = settings.edit();
		editor.putString(context.getString(R.string.PREFS_UUID), uuid);	
		Log.e(TAG,  "Setting last seen UUID: " + uuid);
		editor.commit();
	}
	
	public static int inboxTextSize(Context context)
	{
		SharedPreferences settings = getShared(context);
		String inboxSize = settings.getString(context.getString(R.string.PREFS_INBOX_TEXT_SIZE), "0");
		return Integer.decode(inboxSize);
	}
	public static int bodyTextSize(Context context)
	{
		SharedPreferences settings = getShared(context);
		String bodySize = settings.getString(context.getString(R.string.PREFS_BODY_TEXT_SIZE), "0");
		return Integer.decode(bodySize);	
	}
}
