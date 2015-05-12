package uk.co.k9topebble;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.Html;

import com.getpebble.android.kit.util.PebbleDictionary;

/*
 * special cases for K9 for pure.
 * Only works in 'account' mode
 * has a 'read_flag' not unread.
 */

public class K9Integration {

	/*
	public static void _sendInboxToPebble(Context context, MessageQueue msgQ)
	{
		int keyvalue = 100;
		addMessage(context, msgQ, keyvalue, "email://verylonguuid1234512345", 
				"Test Sender <test@sender.co.uk",
				"Test subject, 1 2 3",
				true);
		keyvalue+=100;	
		addMessage(context, msgQ, keyvalue, "12345678901234567890", 
				"Example Sender <example@sender.co.uk",
				"Mauris et venenatis diam. Sed quis metus ut mi iaculis fermentum. Sed ut mauris posuere turpis duis.",
				true);
		keyvalue+=100;	
		addMessage(context, msgQ, keyvalue, "email://verylonguuidstringwithkeyvalue/" + keyvalue, 
				"Aliquam erat volutpat. Maecenas sapien erat metus.",
				"Mauris et venenatis diam. Sed quis metus ut mi iaculis fermentum. Sed ut mauris posuere turpis duis.",
				false);
		keyvalue+=100;	
		addMessage(context, msgQ, keyvalue, "email://verylonguuidstringwithkeyvalue/" + keyvalue, 
				"Praesent convallis felis eu pharetra orci aliquam.",
				"Cras nisl velit, vulputate vel ultricies et, suscipit sit amet felis. Nam adipiscing nulla eu metus.",
				false);
		keyvalue+=100;	
	}

	public static void sendInboxToPebble2(Context context, MessageQueue msgQ)
	{
		int keyvalue = 100;
		for (int i = 1; i < 30; i++)
		{
			String testMessage = "Test message " + i;
			String testSubject = "Test subject " + i;
			addMessage(context, msgQ, keyvalue, "email://verylonguuid1234512345", 
					testMessage,
					testSubject,
					(i % 1) == 0);
			keyvalue+=100;
		}
	}
	*/
	/*
	public static void sendDummyDataToPebble(Context context, MessageQueue msgQ)
	{
		int keyvalue = 100;
		addMessage(context, msgQ, keyvalue, "email://verylonguuid1234512345", 
				"123456789012345678901234567890",
				"Cras nisl velit, vulputate vel ultricies et, suscipit sit amet felis. Nam adipiscing nulla eu metus.",
				true);
		keyvalue+=100;	
		addMessage(context, msgQ, keyvalue, "12345678901234567890", 
				"12345678901234567890",
				"12345678901234567890123",
				true);
		keyvalue+=100;	
		addMessage(context, msgQ, keyvalue, "email://verylonguuid123456789012345", 
				"12345678901234567890123456789012345",
				"12345678901234567890",
				true);
		keyvalue+=100;	
		addMessage(context, msgQ, keyvalue, "email://verylonguuidstringwithkeyvalue/" + keyvalue, 
				"Aliquam erat volutpat. Maecenas sapien erat metus.",
				"Mauris et venenatis diam. Sed quis metus ut mi iaculis fermentum. Sed ut mauris posuere turpis duis.",
				false);
		keyvalue+=100;	
		addMessage(context, msgQ, keyvalue, "email://verylonguuidstringwithkeyvalue/" + keyvalue, 
				"Praesent convallis felis eu pharetra orci aliquam.",
				"Cras nisl velit, vulputate vel ultricies et, suscipit sit amet felis. Nam adipiscing nulla eu metus.",
				false);
		keyvalue+=100;	
	}
	 */

	static void k9error(MessageQueue msgQ, String msg)
	{
		if (msgQ == null)
		{
			return;
		}
		PebbleDictionary err = new PebbleDictionary();
		err.addUint8(K9Defines.KEY_COMMAND, (byte)K9Defines.eMT_ErrorMsg);
		err.addString(K9Defines.KEY_MESSAGE, msg);

		msgQ.addData(err);
	}
	
	public static void calculateInbox(Context context, ArrayList<emailRecord> emails)
	{
		ProviderAdaptationFactory factory = new ProviderAdaptationFactory();
		
		factory.getAdaption(context).calculateInbox(context, emails);
	}

	public static void sendDeleteNotify(Context context, MessageQueue msgQ, String uuid)
	{
		if (uuid.length() > K9Defines.MAX_URL_LENGTH)
			uuid = uuid.substring(0, K9Defines.MAX_URL_LENGTH);

		PebbleDictionary data = new PebbleDictionary();
		data.addString(K9Defines.KEY_URL, uuid);
		data.addUint8(K9Defines.KEY_COMMAND, (byte)K9Defines.eMT_ConfirmDelete);
		msgQ.addData(data);
	}

	
	public static void addMessage(Context context, MessageQueue msgQ, String uuid, String body)
	{
		int key = 10;
		short length = (short) body.length();
		LinkedList<PebbleDictionary> q = new LinkedList<PebbleDictionary>();
		while (body.length() > K9Defines.MAX_BODY_PACKET)
		{
			PebbleDictionary data = new PebbleDictionary();
			data.addUint8(K9Defines.KEY_COMMAND, (byte)K9Defines.eMT_Body);
			data.addString(key++, body.substring(0, K9Defines.MAX_BODY_PACKET));
			q.addFirst(data);	
			body = body.substring(K9Defines.MAX_BODY_PACKET);
		}
		PebbleDictionary data = new PebbleDictionary();
		data.addUint8(K9Defines.KEY_COMMAND, (byte)K9Defines.eMT_Body);
		data.addString(key++, body);
		q.addFirst(data);	

		data = new PebbleDictionary();
		data.addUint8(K9Defines.KEY_COMMAND, (byte)K9Defines.eMT_Body);
		data.addUint16(1, length);
		q.addFirst(data);

		// ok, got it all in reverse order, now push out
		for(Iterator<PebbleDictionary> itr = q.iterator();itr.hasNext();)  {
			msgQ.insertData(itr.next());
		}

		data = new PebbleDictionary();
		data.addUint8(K9Defines.KEY_COMMAND, (byte)K9Defines.eMT_RequestPing);
		msgQ.addData(data);


	}

	static public String getPreview(Context context, String url, String account, MessageQueue msgQ)
	{
		ProviderAdaptationFactory factory = new ProviderAdaptationFactory();

		return factory.getAdaption(context).getPreview(context, url, account, msgQ);
	} 

	static public void delete(Context context, String url, MessageQueue msgQ)
	{
		ProviderAdaptationFactory factory = new ProviderAdaptationFactory();

		factory.getAdaption(context).delete(context, url, msgQ);

	} 

	/*
	static public boolean uuidnew(Context context, String oldUUID, String newUUID)
	{
		if (newUUID.length() == 0)
		{
			return false;
		}
		if (oldUUID.length() == 0)
		{
			return true;
		}
		String[] mProjection =
			{
				K9Defines.MessageColumns.URI,
			};

		Cursor cur = getContent(context, mProjection, null);

		if (cur == null)
		{
			return true;
		}

		if (cur.getCount() > 0)
		{
			cur.moveToFirst();

			do {				
				String URI = cur.getString(0);

				boolean matchOld = oldUUID.equals(URI);
				boolean matchNew = newUUID.equals(URI);

				android.util.Log.i("MSG", "This UUID:" + URI + " - old:" + oldUUID + " newUUID: "+ newUUID + " matchold: " + matchOld + " matchNew: " + matchNew);

				if (matchNew == true)
				{
					cur.close();
					return true;
				}
				if (matchOld)
				{
					// older is newer
					cur.close();
					return false;
				}
			} while (cur.moveToNext() == true);
		}
		cur.close();
		return true;
	} 
*/

/*
	static private void _sendPreview(Context context, String url, MessageQueue msgQ)
	{
		String result = "Nulla facilisi. Etiam vel risus malesuada, pulvinar eros ac, porta ipsum. Suspendisse non mauris vel odio placerat vestibulum a eget nisl. Nullam gravida tempor erat, eu iaculis elit malesuada venenatis. Ut nec imperdiet ante. Aliquam at ante ultrices, viverra est vitae, consequat ante. Proin eu lorem ac enim pretium posuere nec viverra erat. Quisque pretium turpis lectus, et lobortis est facilisis in. In nec elit lacinia, hendrerit elit in, ullamcorper tellus. Morbi odio libero, dignissim at nibh sit amet, pharetra ultrices arcu. Proin diam nisl, placerat ac tortor eget, tempor volutpat quam. Fusce posuere lobortis sapien id laoreet. Ut luctus, libero quis dapibus blandit, est quam fringilla est, quis tincidunt arcu nulla at sapien.\n" +
				"Fusce eu ligula fermentum, tempus dui ac, tempus neque. Morbi fringilla lobortis dolor, ac dictum lorem bibendum in. Nullam sodales vehicula consectetur. Aenean vel orci ac nulla porttitor ultrices. Nulla consequat dapibus turpis, a tincidunt elit volutpat.";
		addMessage(context, msgQ, url, result);
	} */

	/*static private void sendPreview(Context context, String url, MessageQueue msgQ)
	{
		String result = getPreview(context, url, msgQ);
		// send in 100 byte chunks
		if (result != null)
		{
			addMessage(context, msgQ, url, result);
		}
		else
		{
			addMessage(context, msgQ, url, "Problems with K9, please try again");
		}
	} 
	*/
}
//PebbleKit.sendDataToPebble(context, K9Defines.PEBBLE_APP_UUID, data);
