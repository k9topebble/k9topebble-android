package uk.co.k9topebble;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Random;

import com.getpebble.android.kit.util.PebbleDictionary;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.text.Html;
import android.util.Log;

public class InboxService extends Service {
	private static final String TAG = "InboxService";
	private MessageQueue m_msgQ = new MessageQueue();
	private byte[] imageData = new byte[168*19];
	private ArrayList<emailRecord> m_emails = null;
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	
	void start()
	{
		m_msgQ.reset();
		sendInboxToPebble(this, m_msgQ);
		//K9Integration.sendDummyDataToPebble(this, m_msgQ);
		m_msgQ.send(this, K9Defines.PEBBLE_APP_UUID);
	}
	
	void stop()
	{
		m_msgQ.reset();
		//stopSelf();
	}
	void body(String uuid)
	{
		sendPreview(this, m_msgQ, uuid);
		//K9Integration.sendPreview(this, uuid, m_msgQ);
		m_msgQ.send(this, K9Defines.PEBBLE_APP_UUID);
	}
	
	void delete(String uuid)
	{
		K9Integration.delete(this, uuid, m_msgQ);
		//K9Integration.sendPreview(this, uuid, m_msgQ);
		m_msgQ.send(this, K9Defines.PEBBLE_APP_UUID);
	}
	
	void deleteNotify(String uuid)
	{
		K9Integration.sendDeleteNotify(this, m_msgQ, uuid);
		//K9Integration.sendPreview(this, uuid, m_msgQ);
		m_msgQ.send(this, K9Defines.PEBBLE_APP_UUID);
	}
	
	void ack(int transactionId)
	{
		m_msgQ.ack(this, transactionId, K9Defines.PEBBLE_APP_UUID);
	}
	
	void nack(int transactionId)
	{
		m_msgQ.nack(this, transactionId, K9Defines.PEBBLE_APP_UUID);
	}
	
	public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        int command = -1;
        String uuid = null;
        int transactionId = 0;
        //android.os.Debug.waitForDebugger();
        if (intent != null) {
            Bundle bundle = intent.getExtras();
            //Retrieve your data using the name
            command = bundle.getInt(K9Defines.COMMAND_EXTRA, -1);
            uuid = bundle.getString(K9Defines.UUID_EXTRA);
            transactionId = bundle.getInt(K9Defines.TRANSACTION_EXTRA, 0);
            MyLogger.e(TAG, "intent bundle" + bundle);
        }	

        if (m_emails == null)
        {
        	MyLogger.e(TAG, "onStartCommand, emails not created yet");
        	m_emails = new ArrayList<emailRecord>();
        	K9Integration.calculateInbox(getApplicationContext(), m_emails);
        	if (K9Defines.COMMAND_INBOX_CHANGED == command)
        	{
        		return START_STICKY; // nothing more to do
        	}
     	
        }
        
        switch (command)
        {
        case K9Defines.COMMAND_START:
        	// start sending data to the watch
        	MyLogger.e(TAG, "onStartCommand: COMMAND_START transactionId: " + transactionId);
        	start();
        	break;
        default:
        case K9Defines.COMMAND_STOP:
        	// shutdown
        	MyLogger.e(TAG, "onStartCommand: COMMAND_STOP transactionId: " + transactionId);
        	stop();
        	break;
        case K9Defines.COMMAND_BODY:
        	// interrupt inbox delivery and start sending the body
        	MyLogger.e(TAG, "onStartCommand: COMMAND_BODY transactionId: " + transactionId +" UUID:" +uuid );
        	body(uuid);
        	break;
        case K9Defines.COMMAND_DELETE:
        	// interrupt inbox delivery and start sending the body
        	MyLogger.e(TAG, "onStartCommand: COMMAND_DELETE transactionId: " + transactionId);
        	delete(uuid);
        	break;
        case K9Defines.COMMAND_DELETE_NOTIFY:
        	// interrupt inbox delivery and start sending the body
        	MyLogger.e(TAG, "onStartCommand: COMMAND_DELETE_NOTIFY transactionId: " + transactionId);
        	deleteNotify(uuid);
        	break;
        case K9Defines.COMMAND_ACK:
        	// watch has the last message, send the next
        	if (K9Defines.DEBUG_ENABLED)Log.e(TAG, "onStartCommand: COMMAND_ACK transactionId: " + transactionId);
        	ack(transactionId);
        	break;
        case K9Defines.COMMAND_NACK:
        	// watch didn't get the last message, resend up to 3 times, then reset
        	if (K9Defines.DEBUG_ENABLED)Log.e(TAG, "onStartCommand: COMMAND_NACK transactionId: " + transactionId);
        	nack(transactionId);
        	break;
        case K9Defines.COMMAND_IMAGE:
            if (intent != null) {
                Bundle bundle = intent.getExtras();
                //Retrieve your data using the name
                int pos = bundle.getInt(K9Defines.IMG_POS);
                byte[] newImgData = bundle.getByteArray(K9Defines.IMG_DATA);
                for (int i= 0; i < newImgData.length; i++)
                {
                	imageData[pos + i] = newImgData[i];
                }
                if (pos + newImgData.length >= 168*18)
                {
                	if (K9Defines.DEBUG_ENABLED)Log.e(TAG, "bitmap complete");
                	generateBitmap();
                }
                else
                {
                	if (K9Defines.DEBUG_ENABLED)Log.e(TAG, "bitmap partial, now " + (pos + newImgData.length));
                }
                	
            }
            break;
        case K9Defines.COMMAND_INBOX_CHANGED:
        	if (K9Defines.DEBUG_ENABLED)Log.e(TAG, "onStartCommand, COMMAND_INBOX_CHANGED");
        	K9Integration.calculateInbox(this, m_emails);
        	break;
        case K9Defines.COMMAND_ACTIVATE:
        	if (K9Defines.DEBUG_ENABLED)Log.e(TAG, "onStartCommand, COMMAND_ACTIVATE");
        	break;
        }
        return START_STICKY;
	}
	
	void generateBitmap()
	{
		Bitmap bitmap = Bitmap.createBitmap(144, 168, Bitmap.Config.ARGB_8888);
		int offset = 0;
		for (int y = 0; y < 168; y++)
		{
			for (int x = 0; x < 18; x++)
			{
				int pixelblock = imageData[offset++] & 0xff;
				if (pixelblock != 0)
				{
					//Log.e("BITMAP", "non zero");
				}
				for (int p = 0; p < 8; p++)
				{
					int mask;
					switch (p)
					{
					case 0:
						mask =0x1;
						break;
					case 1:
						mask = 0x2;
						break;
					case 2:
						mask = 0x4;
						break;
					case 3:
						mask = 0x8;
						break;
					case 4:
						mask = 0x10;
						break;
					case 5:
						mask = 0x20;
						break;
					case 6:
						mask = 0x40;
						break;
					case 7:
						mask = 0x80;
						break;
					default:
						mask = 0;
						break;
					}
					//Log.e("BITMAP", "Pixel "  + (x*8+p) + ":" + y + " is " + Integer.toHexString(pixelblock) + " mask "+ Integer.toHexString(mask) + " result " + (pixelblock & mask));
					if ((pixelblock & mask) >= 1)
					{
						bitmap.setPixel(x*8+p, y, 0xffffffff);
					}
					else
					{
						bitmap.setPixel(x*8+p, y, 0xff000000);
					}
				}
			}
		}
		// should have something generated now, save it and 'send' it.
		
		String root = Environment.getExternalStorageDirectory().toString();
		File myDir = new File(root + "/saved_images");    
		myDir.mkdirs();
		Random generator = new Random();
		int n = 10000;
		n = generator.nextInt(n);
		String fname = "Image-"+ n +".png";
		File file = new File (myDir, fname);
		if (file.exists ()) file.delete (); 
		try {
		       FileOutputStream out = new FileOutputStream(file);
		       bitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
		       out.flush();
		       out.close();

		} catch (Exception e) {
		       e.printStackTrace();
		}
		
	}
	
	public void sendPreview(Context context, MessageQueue msgQ, String uri)
	{
		if (!m_emails.isEmpty())
		{
			for (int i = 0; i < m_emails.size(); i++)
			{
				emailRecord email = m_emails.get(i);
				if (email.m_URI.equals(uri))
				{
					MyLogger.i("Message",
							"URI match " +
							", SENDER: " + email.m_SENDER.length() +
							", SUBJECT: " + email.m_SUBJECT.length() +
							", UNREAD: " + email.m_UNREAD +
							", UUID: " + email.m_URI );
					if (email.m_PREVIEW != null)
					{
						String result = Html.fromHtml(email.m_PREVIEW).toString();
						K9Integration.addMessage(context, msgQ, uri, result);
						return;
					}
					else
					{
						MyLogger.e("Message", "There is no preview to read!");
					}
				}
			}
		}

		K9Integration.addMessage(context, msgQ, uri, "Problems with K9, please try again");
	}
	
	public void sendInboxToPebble(Context context, MessageQueue msgQ)
	{
		PebbleDictionary data = new PebbleDictionary();
		data.addUint8(0, (byte)K9Defines.eMT_Reset);
		msgQ.addData(data);    	

		if (!m_emails.isEmpty())
		{
	//		int maxSize = Preferences.inboxSize(context);
	//		boolean unreadOnly = Preferences.unreadOnly(context);

			int keyvalue = K9Defines.KEY_START;
			for (int i = 0; i < m_emails.size(); i++)
			{
				emailRecord email = m_emails.get(i);
				MyLogger.i("Message",
						", count: " + i + 
						", SENDER: " + email.m_SENDER.length() +
						", SUBJECT: " + email.m_SUBJECT.length() +
						", UNREAD: " + email.m_UNREAD +
						", UUID: " + email.m_URI );

				addMessage(context, msgQ, keyvalue, email.m_URI, email.m_SENDER, email.m_SUBJECT, email.m_UNREAD);
				keyvalue += K9Defines.KEY_INCREMENT;
			}
		}
		else
		{
			PebbleDictionary err = new PebbleDictionary();

			err.addUint8(K9Defines.KEY_COMMAND, (byte)K9Defines.eMT_ErrorMsg);
			err.addString(K9Defines.KEY_MESSAGE, "No emails in inbox");
			msgQ.addData(err);
		}

		data = new PebbleDictionary();
		data.addUint8(K9Defines.KEY_COMMAND, (byte)K9Defines.eMT_RequestPing);
		msgQ.addData(data);
	}


	public static void addMessage(Context context, MessageQueue msgQ, int keyvalue, String uuid, String sender, String subject, boolean unread)
	{
		if (sender.length() > K9Defines.MAX_SENDER_LENGTH)
			sender = sender.substring(0, K9Defines.MAX_SENDER_LENGTH);
		if (subject.length() > K9Defines.MAX_SUBJECT_LENGTH)
			subject = subject.substring(0, K9Defines.MAX_SUBJECT_LENGTH);
		if (uuid.length() > K9Defines.MAX_URL_LENGTH)
			uuid = uuid.substring(0, K9Defines.MAX_URL_LENGTH);
		int incsize = 4 + 1 + 2; // key + type + length
		int dictlength = 0;

		PebbleDictionary data = new PebbleDictionary();
		data.addUint8(K9Defines.KEY_COMMAND, (byte)K9Defines.eMT_Update);
		dictlength += incsize;
		dictlength += 4;

		data.addString(keyvalue + K9Defines.KEY_UUID_OFFSET, uuid);
		dictlength += incsize;
		dictlength += uuid.length();

		data.addUint8(K9Defines.KEY_COMMAND, (byte)K9Defines.eMT_Update);
		if (unread)
		{
			data.addUint8(keyvalue + K9Defines.KEY_UNREAD_OFFSET, (byte) 1);
		} 
		else
		{
			data.addUint8(keyvalue + K9Defines.KEY_UNREAD_OFFSET, (byte) 0);
		}
		dictlength += incsize;
		dictlength += 4;

		// can we squeeze in the sender?	
		if (dictlength + incsize + sender.length() > 110)
		{
			msgQ.addData(data);

			data = new PebbleDictionary();
			data.addUint8(K9Defines.KEY_COMMAND, (byte)K9Defines.eMT_Update);
			dictlength = incsize;
			dictlength += 4;
		}

		data.addString(keyvalue + K9Defines.KEY_SENDER_OFFSET, sender);
		dictlength += incsize;
		dictlength += sender.length();
		if (dictlength + incsize + subject.length() > 110)
		{
			msgQ.addData(data);

			data = new PebbleDictionary();
			data.addUint8(K9Defines.KEY_COMMAND, (byte)K9Defines.eMT_Update);
			dictlength = incsize;
			dictlength += 4;
		}
		data.addString(keyvalue + K9Defines.KEY_SUBJECT_OFFSET, subject);
		msgQ.addData(data);

	}

}
