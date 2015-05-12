package uk.co.k9topebble;

import static com.getpebble.android.kit.Constants.APP_UUID;
import static com.getpebble.android.kit.Constants.MSG_DATA;
import static com.getpebble.android.kit.Constants.TRANSACTION_ID;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;


import com.getpebble.android.kit.Constants;
import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.util.PebbleDictionary;
import com.getpebble.android.kit.util.PebbleTuple;

public class K9Reciever extends BroadcastReceiver {
	private static final String TAG = "K9Reciever"; 
	private static final String PTAG = "PEBBLE_DEBUG"; 
	
	private void printlog(final PebbleDictionary data)
	{
		String log = new String();

		Iterator<PebbleTuple> tupiter = data.iterator();
	      while(tupiter.hasNext()) {
	          PebbleTuple element = tupiter.next();
	          if (element.key != 0)
	          {
	        	  log = log + " " + element.value;
	          
	          }
	      }
	      Log.e(PTAG, log);
      		      
	}
    
    public void pebbleOperation(final Context context, final int transactionId, final PebbleDictionary data)
    {
    	Intent intent = new Intent(context, InboxService.class);
    	if (!data.contains(0))
    	{
    		Log.e(TAG, "Bad message seen! no command");
        	PebbleKit.sendNackToPebble(context, transactionId);
        	return;
    	}
    	switch (data.getUnsignedInteger(0).intValue())
    	{
    	case K9Defines.eMT_RequestStart:
    		// send data on the first 'n' emails in the inbox
    		// record the fact the app is running and send updates
        	PebbleKit.sendAckToPebble(context, transactionId);
        	intent.putExtra(K9Defines.COMMAND_EXTRA, K9Defines.COMMAND_START);
        	
        	// check protocol
        	if (data.contains(K9Defines.KEY_PROTOCOL_VERSION))
        	{
        		long protocol = data.getUnsignedInteger(K9Defines.KEY_PROTOCOL_VERSION);
        		if (protocol != K9Defines.PROTOCOL_VERSION)
        		{
                	PebbleDictionary out = new PebbleDictionary();
                	out.addUint8(0, (byte)K9Defines.eMT_ErrorMsg);
                	out.addString(3, "This watchapp is not supported");
            		PebbleKit.sendDataToPebble(context, K9Defines.PEBBLE_APP_UUID, out);
            		return;
        		}
        	}
        	
        	// send the config data
        	{
              	PebbleDictionary out = new PebbleDictionary();
            	out.addUint8(0, (byte)K9Defines.eMT_Config);
            	out.addUint8(K9Defines.KEY_INBOX_TEXT_SIZE, (byte) Preferences.inboxTextSize(context));
            	out.addUint8(K9Defines.KEY_BODY_TEXT_SIZE, (byte) Preferences.bodyTextSize(context));
        		PebbleKit.sendDataToPebble(context, K9Defines.PEBBLE_APP_UUID, out);
        	}
    		
        	context.startService(intent);
    		break;
    	case K9Defines.eMT_RequestStop:
    		PebbleKit.sendAckToPebble(context, transactionId);
    		// stop sending updates
        	intent.putExtra(K9Defines.COMMAND_EXTRA, K9Defines.COMMAND_STOP);
        	context.startService(intent);
    		break;
    	case K9Defines.eMT_RequestBody:
    	{
    		PebbleKit.sendAckToPebble(context, transactionId);
    		// lookup the body data and send it back
        	intent.putExtra(K9Defines.COMMAND_EXTRA, K9Defines.COMMAND_BODY);
        	String uuid = data.getString(1);
        	if (uuid != null)
        	{
        		intent.putExtra(K9Defines.UUID_EXTRA,uuid);
        		context.startService(intent);
        	}
    	}
    	break;
    	case K9Defines.eMT_RequestDelete:
    	{
    		PebbleKit.sendAckToPebble(context, transactionId);
    		// lookup the body data and send it back
    		intent.putExtra(K9Defines.COMMAND_EXTRA, K9Defines.COMMAND_DELETE);
    		String uuid = data.getString(1);
    		if (uuid != null)
    		{
    			intent.putExtra(K9Defines.UUID_EXTRA,uuid);
    			context.startService(intent);
    		}
    	}
    		break;
    	case K9Defines.eMT_SendImage:
    		PebbleKit.sendAckToPebble(context, transactionId);
    		// lookup the body data and send it back
        	intent.putExtra(K9Defines.COMMAND_EXTRA, K9Defines.COMMAND_IMAGE);
        	long pos = data.getUnsignedInteger(K9Defines.KEY_IMG_SIZE);
        	
        	byte[] imgData = data.getBytes(K9Defines.KEY_IMG_START);
        	if (imgData != null)
        	{
        		intent.putExtra(K9Defines.IMG_POS, (int)pos);
        		intent.putExtra(K9Defines.IMG_DATA,imgData);
        		context.startService(intent);
        	}
    		break;
    	case K9Defines.eMT_RequestLog:
    		PebbleKit.sendAckToPebble(context, transactionId);
    		Log.e(TAG, "log dump" + data.toJsonString());
    		printlog(data);
    		break;    		
    	default:
    		/// wasn't expecting this
    		Log.e(TAG, "Bad message seen! "  + data.getUnsignedInteger(0).intValue());
        	PebbleKit.sendNackToPebble(context, transactionId);
        	intent.putExtra(K9Defines.COMMAND_EXTRA, K9Defines.eMT_RequestStop);
        	context.startService(intent);
        	return;
    	}

    }
    
    @Override
	public void onReceive(Context context, Intent intent) {
    	String action = intent.getAction();
		Log.e(TAG, "Got an intent: " + action );
    	if (action.equals(Preferences.inboxProvider(context) +".intent.action.EMAIL_RECEIVED"))
    	{
    		K9EmailReceived(context, intent);
        	
        	Intent serviceIntent = new Intent(context, InboxService.class);
        	serviceIntent.putExtra(K9Defines.COMMAND_EXTRA, K9Defines.COMMAND_INBOX_CHANGED);
        	context.startService(serviceIntent);        	
    		return;
    	}
    	else if (action.equals(Preferences.inboxProvider(context) +".intent.action.EMAIL_DELETED"))
    	{
    		K9EmailDeleted(context, intent);

    		Intent serviceIntent = new Intent(context, InboxService.class);
        	serviceIntent.putExtra(K9Defines.COMMAND_EXTRA, K9Defines.COMMAND_INBOX_CHANGED);
        	context.startService(serviceIntent);        	
    		return;
    	} else if (action.equals(Constants.INTENT_APP_RECEIVE))
    	{
    		// is this message for me?
            final UUID receivedUuid = (UUID) intent.getSerializableExtra(APP_UUID);

            // Pebble-enabled apps are expected to be good citizens and only inspect broadcasts containing their UUID
            if (!K9Defines.PEBBLE_APP_UUID.equals(receivedUuid)) {
        		Log.e(TAG, "This message is not for me, it's for" + receivedUuid );
                return;
            }

            final int transactionId = intent.getIntExtra(TRANSACTION_ID, -1);
            final String jsonData = intent.getStringExtra(MSG_DATA);
            if (jsonData == null || jsonData.length() == 0) {
                return;
            }

            try {
                final PebbleDictionary data = PebbleDictionary.fromJson(jsonData);
                pebbleOperation(context, transactionId, data);
            } catch (JSONException e) {
                e.printStackTrace();
                PebbleKit.sendNackToPebble(context, transactionId);
                return;
            }
        } else if (action.equals(Constants.INTENT_APP_RECEIVE_ACK))
        {
        	final int transactionId = intent.getIntExtra(TRANSACTION_ID, -1);
        	//Application app = context.getApplication();
        	Intent serviceIntent = new Intent(context, InboxService.class);
        	serviceIntent.putExtra(K9Defines.COMMAND_EXTRA, K9Defines.COMMAND_ACK);
        	serviceIntent.putExtra(K9Defines.TRANSACTION_EXTRA, transactionId);
        	context.startService(serviceIntent);
        } else if (action.equals(Constants.INTENT_APP_RECEIVE_NACK))
        {
        	final int transactionId = intent.getIntExtra(TRANSACTION_ID, -1);
        	Intent serviceIntent = new Intent(context, InboxService.class);
        	serviceIntent.putExtra(K9Defines.COMMAND_EXTRA, K9Defines.COMMAND_NACK);
        	serviceIntent.putExtra(K9Defines.TRANSACTION_EXTRA, transactionId);
        	context.startService(serviceIntent);        	
        }
	}

    public void K9EmailReceived(Context context, Intent intent) {
    	/*if (K9Integration.uuidnew(context, Preferences.topUUID(context), intent.getData().toString()) == false)
		{
			Log.e(TAG, "Not sending message, uuid is older than last good uuid: received: " +  intent.getData().toString()
					+ " - top: " + Preferences.topUUID(context));
			return;
		}*/
		
		// store this one as its now the top
		//Preferences.topUUID(context, intent.getData().toString());
		
		boolean send = Preferences.canSend(context);
		Log.e(TAG, "Got an intent, forward to pebble? " + send );
		
		if (!send)
		{
			return;
		}
		
        String account = intent.getStringExtra(Preferences.inboxProvider(context) + K9Defines.EXTRA_ACCOUNT_POST);
        //String folder = intent.getStringExtra(K9Defines.EXTRA_FOLDER);
        //Bundle extras = intent.getExtras();
        //Date sent = (Date)extras.get(K9Defines.EXTRA_SENT_DATE);
        String from = intent.getStringExtra(Preferences.inboxProvider(context) + K9Defines.EXTRA_FROM_POST);
        //String to = intent.getStringExtra(K9Defines.EXTRA_TO);
        //String cc = intent.getStringExtra(K9Defines.EXTRA_CC);
        //String bcc = intent.getStringExtra(K9Defines.EXTRA_BCC);
        String subject = intent.getStringExtra(Preferences.inboxProvider(context) + K9Defines.EXTRA_SUBJECT_POST);
        
        if (K9Defines.DEBUG_ENABLED)Log.e(TAG, intent.getAction());
        if (K9Defines.DEBUG_ENABLED)Log.e(TAG, "Got data " + intent.getData());
        if (account != null)
        {
        	Log.e(TAG, "Got account " + account);
        }
        /*if (folder != null)
        {
        	Log.e(TAG, "Got folder " + folder);
        }
        if (sent != null)
        {
        	Log.e(TAG, "Got sent " + sent);
        }*/
        if (from != null)
        {
        	Log.e(TAG, "Got from " + from);
        }
        /*if (to != null)
        {
        	Log.e(TAG, "Got to " + to);
        }
        if (cc != null)
        {
        	Log.e(TAG, "Got cc " + cc);
        }
        if (bcc != null)
        {
        	Log.e(TAG, "Got bcc " + bcc);
        }
        if (subject != null)
        {
        	Log.e(TAG, "Got subject " + subject);
        }*/
        
        Boolean body = Preferences.sendBody(context);
        String preview = null;
        if (body)
        {
        	preview = K9Integration.getPreview(context, intent.getData().toString(), account, null);
        }
		final Intent i = new Intent("com.getpebble.action.SEND_NOTIFICATION");

		final Map<String, String> data = new HashMap<String, String>();
		if (from != null)
		{
			data.put("title", from);
		}
		if (subject != null)
		{
			if (preview != null)
			{
				subject += ":-" + preview;
			}
			if (subject.length() > 200)
			{
				subject.substring(0, 200);
			}
			data.put("body", subject);
		}
		final JSONObject jsonData = new JSONObject(data);
		final String notificationData = new JSONArray().put(jsonData).toString();

		i.putExtra("messageType", "PEBBLE_ALERT");
		i.putExtra("sender", "MyAndroidApp");
		i.putExtra("notificationData", notificationData);

		Log.d(TAG, "About to send a modal alert to Pebble: " + notificationData);
		context.sendBroadcast(i);
	}

    public void K9EmailDeleted(Context context, Intent intent) {
        
        if (K9Defines.DEBUG_ENABLED)Log.e(TAG, "Got data " + intent.getData());

		// lookup the body data and send it back
        Intent serviceIntent = new Intent(context, InboxService.class);
        serviceIntent.putExtra(K9Defines.COMMAND_EXTRA, K9Defines.COMMAND_DELETE_NOTIFY);
        serviceIntent.putExtra(K9Defines.UUID_EXTRA,intent.getData().toString());
    	context.startService(serviceIntent);
		
	}
    

}
