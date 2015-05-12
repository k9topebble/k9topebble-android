package uk.co.k9topebble;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.UUID;

import android.content.Context;
import android.util.Log;

import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.util.PebbleDictionary;

public class MessageQueue {
	private static final String TAG = "MessageQueue";
	LinkedList<PebbleDictionary> m_msgs;
	HashMap<Integer,PebbleDictionary> m_old;
	int m_resendCount = 0;
	int m_transactionId = 0;
	int m_inProgressCount = 0;

	MessageQueue()
	{
		m_msgs = new LinkedList<PebbleDictionary>();
		m_old = new HashMap<Integer,PebbleDictionary>();
	}

	
	void addData(PebbleDictionary data)
	{
		m_msgs.add(data);
	}
	
	void insertData(PebbleDictionary data)
	{
		m_msgs.addFirst(data);
	}
	
	void msgDone()
	{
		--m_inProgressCount;
		if (m_inProgressCount < 0)
		{
			m_inProgressCount = 0;
		}
	}
	
	public void reset() {
		m_msgs.clear();
		m_old.clear();
		m_resendCount = 0;
		m_inProgressCount = 0;
		m_transactionId = 0;
	}
	
	public void send(Context context, UUID uuid) {
		if (m_inProgressCount > 3)
		{
		//	return;
		}
		PebbleDictionary data = m_msgs.peek();
		if (data != null)
		{
			//m_resendCount++;
			m_transactionId = (m_transactionId + 1) % 128;
			if (m_transactionId == 0)
			{
				m_transactionId = 1;
			}			
			PebbleKit.sendDataToPebbleWithTransactionId(context, uuid, data, m_transactionId);
			m_inProgressCount++;
			if (K9Defines.DEBUG_ENABLED)
			{
				Log.e(TAG, "Sending dictionary, trans " + m_transactionId + " size " + 
						data.toJsonString().length() + " : " + data.toJsonString() );
				m_old.put(m_transactionId, data);
			}

			m_msgs.removeFirst();
		}
	}
	public void ack(Context context, int transactionId, UUID uuid ) {
		msgDone();
		m_resendCount = 0;
		
		m_old.remove(transactionId);

		if (transactionId == m_transactionId || m_transactionId == 0)
		{
			if (m_msgs.size() > 0)
			{
				send(context, uuid);
			}
		}
	}

	public void nack(Context context, int transactionId, UUID uuid ) {
		msgDone();
		m_resendCount++;
		if (m_resendCount > 20)
		{
			if (K9Defines.DEBUG_ENABLED)
			{
				Log.e(TAG, "three naks in a row, resetting!" );
			}
			reset();
		}
		
		PebbleDictionary data = m_old.remove(transactionId);
		if (transactionId == m_transactionId || m_transactionId == 0)
		{
			if (data != null)
			{
				m_msgs.addFirst(data);
			}
	
			send(context, uuid);
		}
	}
}
