package uk.co.k9topebble;

public class emailRecord
{
	public String m_SENDER;  
	public String m_SUBJECT;
	public String m_URI;
	public boolean m_UNREAD;
	public String m_PREVIEW;
	
	public emailRecord(String sender, String subject, String uri, boolean unread, String preview)
	{
		m_SENDER = sender;  
		m_SUBJECT = subject;
		m_URI = uri;
		m_UNREAD = unread;
		m_PREVIEW = preview;
	}

}