package uk.co.k9topebble;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

import com.getpebble.android.kit.util.PebbleDictionary;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.TreeMultimap;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.Html;

public class ProviderAdaptationFactory {

	public abstract class ProviderAdaptation
	{
		public abstract void calculateInbox(Context context, ArrayList<emailRecord> emails);

		public void k9error(MessageQueue msgQ, String msg)
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
		public Cursor getContent(Context context, String[] mProjection, String UUIDs, MessageQueue msgQ)
		{
			String URIString = "content://" + Preferences.inboxProvider(context.getApplicationContext()) + ".messageprovider/inbox_messages/" ;
			if (UUIDs != null)
			{
				URIString += UUIDs;
			}

			Uri k9Uri = Uri.parse(URIString);

			android.util.Log.d("MSG", "Start read from " + k9Uri);
			for (int i = 0; i < 2; i++)
			{
				try {
					Cursor cur = context.getContentResolver().query(k9Uri, mProjection, null, null, null);
					if (cur != null)
					{
						android.util.Log.d("MSG", "Got cursor, returning");
						return cur;
					}
				} catch (java.lang.SecurityException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();

					android.util.Log.e("MSG", "Permissions fail");
					k9error(msgQ, "Permissions problem, reinstall android app!");
					return null;
				}
				try {
					Thread.sleep(500);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				android.util.Log.e("MSG", "Failed to get cursor!");
			}
			if (msgQ != null)
			{
				k9error(msgQ, "Problems communicating with K9, please try again later");
			}
			android.util.Log.e("MSG", "exiting with no email data!");
			return null;
		}

		public String getAccountUUID(Context context, String name)
		{
			Uri k9Uri = Uri.parse("content://" + Preferences.inboxProvider(context.getApplicationContext()) + ".messageprovider/accounts");

			android.util.Log.d("MSG", "Start read");
			for (int i = 0; i < 2; i++)
			{
				try {
					Cursor cur = context.getContentResolver().query(k9Uri, null, null, null, null);
					if (cur != null)
					{
						android.util.Log.d("MSG", "Got cursor, returning");
						if (cur.getCount() > 0)
						{
							//"accountNumber", "accountName", "accountUuid"
							cur.moveToFirst();
							do {				
								String accountNumber = cur.getString(0);
								String accountName = cur.getString(1);
								String accountUuid = cur.getString(2);
								MyLogger.i("Calculate",
										", accountNumber: " + accountNumber 
										+ ", accountName: " + accountName
										+ ", accountUuid: " + accountUuid
										);
								if (name.contentEquals(accountName))
								{
									cur.close();
									return accountUuid;
								}
							} while ((cur.moveToNext() == true));
						}
						cur.close();

						return "";
					}
				} catch (java.lang.SecurityException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();

					android.util.Log.e("MSG", "Permissions fail");
					return null;
				}
				try {
					Thread.sleep(500);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				android.util.Log.e("MSG", "Failed to get cursor!");
			}
			android.util.Log.e("MSG", "exiting with no email data!");
			return null;
		}

		public String getAccountUUID(Context context, Long num)
		{
			Uri k9Uri = Uri.parse("content://" + Preferences.inboxProvider(context.getApplicationContext()) + ".messageprovider/accounts");

			android.util.Log.d("MSG", "Start read");
			for (int i = 0; i < 2; i++)
			{
				try {
					Cursor cur = context.getContentResolver().query(k9Uri, null, null, null, null);
					if (cur != null)
					{
						android.util.Log.d("MSG", "Got cursor, returning");
						if (cur.getCount() > 0)
						{
							//"accountNumber", "accountName", "accountUuid"
							cur.moveToFirst();
							do {				
								Long accountNumber = cur.getLong(0);
								String accountName = cur.getString(1);
								String accountUuid = cur.getString(2);
								MyLogger.i("Calculate",
										", accountNumber: " + accountNumber 
										+ ", accountName: " + accountName
										+ ", accountUuid: " + accountUuid
										);
								if (num == accountNumber)
								{
									cur.close();
									return accountUuid;
								}
							} while ((cur.moveToNext() == true));
						}
						cur.close();

						return "";
					}
				} catch (java.lang.SecurityException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();

					android.util.Log.e("MSG", "Permissions fail");
					return null;
				}
				try {
					Thread.sleep(500);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				android.util.Log.e("MSG", "Failed to get cursor!");
			}
			android.util.Log.e("MSG", "exiting with no email data!");
			return null;
		}
		
		
		public abstract void delete(Context context, String url, MessageQueue msgQ);
	
		
		public abstract String getPreview(Context context, String url, String account, MessageQueue msgQ);
	}

	
	public ProviderAdaptation getAdaption(Context context)
	{
		String inboxProviderTitle = Preferences.inboxProviderTitle(context);
		if (inboxProviderTitle.equals(context.getString(R.string.org_koxx_k9ForPureWidget)))
		{
			return new k9PureAdaptation();
		}   
		return new k9Adaptation();
	}


	private class k9Adaptation extends ProviderAdaptation
	{
		public k9Adaptation()
		{
		}
		
		public String getPreview(Context context, String url, String account, MessageQueue msgQ)	
		{
			String result = null;
			String[] mProjection =
				{
					K9Defines.MessageColumns.PREVIEW,
					K9Defines.MessageColumns.URI,
				};

			Cursor cur = getContent(context, mProjection, null, msgQ);

			if (cur == null)
			{
				return null;
			}

			if (cur.getCount() > 0)
			{
				cur.moveToFirst();

				do {				
					String PREVIEW = cur.getString(0);
					String URI = cur.getString(1);

					boolean match = url.equals(URI);
					//android.util.Log.i("Message", 
					//		 ", PREVIEW: " + PREVIEW +
					//		 ", URI: " + URI +
					//		 ", match? " + match
					//		 );

					if (match == true)
					{
						result = PREVIEW;
						break;
					}

				} while (cur.moveToNext() == true);
			}
			cur.close();


			// send in 100 byte chunks
			if (result != null && result.length() > 0)
			{
				return Html.fromHtml(result).toString();
			}
			return "";
		} 
	

		public void calculateInbox(Context context, ArrayList<emailRecord> emails)
		{
			emails.clear();

			String[] mProjection =
				{
					K9Defines.MessageColumns.SENDER,   
					K9Defines.MessageColumns.SUBJECT,
					K9Defines.MessageColumns.UNREAD,
					K9Defines.MessageColumns.URI,
					K9Defines.MessageColumns.PREVIEW
				};

			Cursor cur = getContent(context, mProjection, null, null);

			if (cur == null)
			{
				android.util.Log.e("Calculate", "Failed to get cursor");
				return;
			}
			if (cur.getCount() > 0)
			{
				int maxSize = Preferences.inboxSize(context);
				boolean unreadOnly = Preferences.unreadOnly(context);
				android.util.Log.e("Calculate", "Inbox max size " + maxSize);
				android.util.Log.e("Calculate", "Unread only? " + unreadOnly);
				if (cur.getCount() > 0)
				{
					int count = 0;
					/*for (int i = 0; i < cur.getColumnCount(); i++)
					{
						android.util.Log.i("MSG", "Column:" + i+ cur.getColumnName(i) + ":");
					}
					android.util.Log.i("MSG", "Count:" + cur.getCount());
					*/
					cur.moveToFirst();
					do {				
						String SENDER = cur.getString(0);
						String SUBJECT = cur.getString(1);
						String UNREAD = cur.getString(2);
						String UUID = cur.getString(3);
						String PREVIEW = cur.getString(4);

						MyLogger.i("Calculate",
								", count: " + count + 
								", SENDER: " + SENDER.length() +
								", SUBJECT: " + SUBJECT.length() +
								", UNREAD: " + UNREAD +
								", UUID: " + UUID );
						MyLogger.i("Calculate", "Body length: " + PREVIEW.length());

						boolean unread = UNREAD.equals("true");

						if (unreadOnly && !unread)
						{
							continue;
						}

						emails.add(new emailRecord(SENDER, SUBJECT, UUID, unread, PREVIEW));
						count++;
					} while ((cur.moveToNext() == true) && count < maxSize);
				}
			}

			cur.close();

		}

		public void delete(Context context, String url, MessageQueue msgQ)
		{
			String result = null;
			String[] mProjection =
				{
					K9Defines.MessageColumns.DELETE_URI,
					K9Defines.MessageColumns.URI,
				};
			{
				Cursor cur = getContent(context, mProjection, "", msgQ);

				if (cur == null)
				{
					return;
				}

				if (cur.getCount() > 0)
				{
					cur.moveToFirst();

					do {				
						String delURI = cur.getString(0);
						String URI = cur.getString(1);

						boolean match = url.equals(URI);
						//android.util.Log.i("Message", 
						//		 ", PREVIEW: " + PREVIEW +
						//		 ", URI: " + URI +
						//		 ", match? " + match
						//		 );

						if (match == true)
						{
							result = delURI;
							break;
						}

					} while (cur.moveToNext() == true);
				}
				cur.close();
			}

			{
				Uri k9Uri = Uri.parse(result);

				android.util.Log.e("MSG", "Start delete");
				for (int i = 0; i < 2; i++)
				{
					try {
						context.getContentResolver().delete(k9Uri, null, null);
						break;
					} catch (java.lang.SecurityException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					try {
						Thread.sleep(500);
					} catch (InterruptedException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					android.util.Log.e("MSG", "End delete");
				}
			}
		} 



	}

	private class k9PureAdaptation extends ProviderAdaptation
	{
		public k9PureAdaptation()
		{
		}
		
		public String getPreview(Context context, String url, String account, MessageQueue msgQ)	
		{
			String result = null;
			String[] mProjection =
				{
					K9Defines.MessageColumns.PREVIEW,
					K9Defines.MessageColumns.URI,
				};

			Cursor cur = getContent(context, mProjection, getAccountUUID(context, account), msgQ);

			if (cur == null)
			{
				return null;
			}

			if (cur.getCount() > 0)
			{
				cur.moveToFirst();

				do {				
					String PREVIEW = cur.getString(0);
					String URI = cur.getString(1);

					boolean match = url.equals(URI);
					//android.util.Log.i("Message", 
					//		 ", PREVIEW: " + PREVIEW +
					//		 ", URI: " + URI +
					//		 ", match? " + match
					//		 );

					if (match == true)
					{
						result = PREVIEW;
						break;
					}

				} while (cur.moveToNext() == true);
			}
			cur.close();


			// send in 100 byte chunks
			if (result != null && result.length() > 0)
			{
				return Html.fromHtml(result).toString();
			}
			return "";
		} 
	
		public void delete(Context context, String emailurl, MessageQueue msgQ)
		{
			String[] mProjection =
				{
					K9Defines.MessageColumns.DELETE_URI,
					K9Defines.MessageColumns.URI,
				};
			
			//email://messages/0/INBOX/43843 to get del id 
			//content://org.koxx.k9ForPureWidget.messageprovider/delete_message/0/INBOX/43843
				
			Uri uri = Uri.parse(emailurl);

			List<String> segments = null;
	        segments = uri.getPathSegments();
			//android.util.Log.e("MSG", "segments: " + segments);

	        String messageUid = segments.get(2);
	        String accountNum = segments.get(0);

			//android.util.Log.e("MSG", "account num: " + accountNum + " messageId: " + messageUid);
	        
			String UUID = getAccountUUID(context, Long.decode(accountNum));
			
			String result = "content://" + Preferences.inboxProvider(context.getApplicationContext()) + 
					".messageprovider/delete_message/" + UUID + "/" +  messageUid;

			Uri k9Uri = Uri.parse(result);

			android.util.Log.e("MSG", "Start delete");
			for (int i = 0; i < 2; i++)
			{
				try {
					context.getContentResolver().delete(k9Uri, null, null);
					break;
				} catch (java.lang.SecurityException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				try {
					Thread.sleep(500);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				android.util.Log.e("MSG", "End delete");
			}
		} 

		public void calculateInbox(Context context, ArrayList<emailRecord> emails)
		{
			emails.clear();

			String[] mProjection =
				{
					K9Defines.MessageColumns.SENDER,   
					K9Defines.MessageColumns.SUBJECT,
					K9Defines.MessageColumns.K9PURE_READ,
					K9Defines.MessageColumns.URI,
					K9Defines.MessageColumns.PREVIEW,
					K9Defines.MessageColumns.SEND_DATE
				};

			String[] UUIDs = getAccountUUIDs(context);

			TreeMap<Long,emailRecord> emailMap = new TreeMap<Long,emailRecord>(); 

			int maxSize = Preferences.inboxSize(context);
			boolean unreadOnly = Preferences.unreadOnly(context);
			android.util.Log.e("Calculate", "Inbox max size " + maxSize);
			android.util.Log.e("Calculate", "Unread only? " + unreadOnly);

			for (int uuidIndex = 0; uuidIndex < UUIDs.length; uuidIndex++ )
			{

				Cursor cur = getContent(context, mProjection, UUIDs[uuidIndex], null);

				if (cur == null)
				{
					android.util.Log.e("Calculate", "Failed to get cursor");
					return;
				}
				if (cur.getCount() > 0)
				{
					if (cur.getCount() > 0)
					{
						int keyvalue = K9Defines.KEY_START;

						//for (int i = 0; i < cur.getColumnCount(); i++)
						{
							//android.util.Log.i("MSG", "Column: " + i+ cur.getColumnName(i) + ": "/* + cur.getType(i)*/);
						}
						android.util.Log.i("MSG", "Count:" + cur.getCount());

						cur.moveToFirst();
						do {				
							String SENDER = cur.getString(0);
							String SUBJECT = cur.getString(1);
							String READ = cur.getString(2);
							String UUID = cur.getString(3);
							String PREVIEW = cur.getString(4);
							Long DATE = cur.getLong(5);

							MyLogger.i("Calculate",
									", SENDER: " + SENDER +
									", SUBJECT: " + SUBJECT +
									", READ: " + READ +
									", UUID: " + UUID +
									", Data: " + DATE);
							MyLogger.i("Calculate", "Body length: " + PREVIEW.length());

							boolean unread = !READ.equals("true");

							if (unreadOnly && !unread)
							{
								continue;
							}
							emailMap.put(DATE, new emailRecord(SENDER, SUBJECT, UUID, unread, PREVIEW));

						} while ((cur.moveToNext() == true) );
					}
				}

				cur.close();
			}
			
			//NavigableMap<Long, emailRecord> nmap = emailMap.descendingMap();

			int count = 0;
			// now copy from map to the emails list
			emailRecord values[] = emailMap.values().toArray(new emailRecord[0]) ;
			for(int i = values.length -1; i >= 0; i--) 
			{
				if (count < maxSize)
				{
					emails.add(values[i]);
					count++;
				}
			}
		}



		/*public String getAccountUUID(Context context)
		{
			Uri k9Uri = Uri.parse("content://" + Preferences.inboxProvider(context.getApplicationContext()) + ".messageprovider/accounts");

			android.util.Log.d("MSG", "Start read");
			for (int i = 0; i < 2; i++)
			{
				try {
					Cursor cur = context.getContentResolver().query(k9Uri, null, null, null, null);
					if (cur != null)
					{
						android.util.Log.d("MSG", "Got cursor, returning");
						if (cur.getCount() > 0)
						{
							//"accountNumber", "accountName", "accountUuid"
							cur.moveToFirst();
							String accountNumber = cur.getString(0);
							String accountName = cur.getString(1);
							String accountUuid = cur.getString(2);
							MyLogger.i("Calculate",
									", accountNumber: " + accountNumber 
									+ ", accountName: " + accountName
									+ ", accountUuid: " + accountUuid
									);
							cur.close();
							return accountUuid;
						}
						cur.close();

						return null;
					}
				} catch (java.lang.SecurityException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();

					android.util.Log.e("MSG", "Permissions fail");
					return null;
				}
				try {
					Thread.sleep(500);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				android.util.Log.e("MSG", "Failed to get cursor!");
			}
			android.util.Log.e("MSG", "exiting with no email data!");
			return null;

		}
		*/
		public String[] getAccountUUIDs(Context context)
		{
			Uri k9Uri = Uri.parse("content://" + Preferences.inboxProvider(context.getApplicationContext()) + ".messageprovider/accounts");

			android.util.Log.d("MSG", "Start read");
			for (int i = 0; i < 2; i++)
			{
				try {
					Cursor cur = context.getContentResolver().query(k9Uri, null, null, null, null);
					if (cur != null)
					{
						String[] names = null;
						android.util.Log.d("MSG", "Got cursor, returning");
						if (cur.getCount() > 0)
						{
							//"accountNumber", "accountName", "accountUuid"
							names = new String[cur.getCount()];
							int count = 0;
							cur.moveToFirst();
							do {				
								String accountNumber = cur.getString(0);
								String accountName = cur.getString(1);
								String accountUuid = cur.getString(2);
								MyLogger.i("Calculate",
										", accountNumber: " + accountNumber 
										+ ", accountName: " + accountName
										+ ", accountUuid: " + accountUuid
										);
								names[count++] = accountUuid;

							} while ((cur.moveToNext() == true));
						}
						cur.close();

						return names;
					}
				} catch (java.lang.SecurityException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();

					android.util.Log.e("MSG", "Permissions fail");
					return null;
				}
				try {
					Thread.sleep(500);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				android.util.Log.e("MSG", "Failed to get cursor!");
			}
			android.util.Log.e("MSG", "exiting with no email data!");
			return null;
		}

		
	}

}
