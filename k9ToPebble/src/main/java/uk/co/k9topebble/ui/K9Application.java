package uk.co.k9topebble.ui;

import uk.co.k9topebble.MessageQueue;
import android.app.Application;

public class K9Application  extends Application {
	MessageQueue m_msgs;

	@Override
	public void onCreate() {
		super.onCreate();
	}

	@Override
	public void onLowMemory() {
		super.onLowMemory();
	}

	@Override
	public void onTerminate() {
		super.onTerminate();
	}

	public MessageQueue getMessages()
	{
		return m_msgs;
	}

}