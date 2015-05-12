package uk.co.k9topebble;

import java.util.UUID;

import android.provider.BaseColumns;

public class K9Defines {
	public static final boolean DEBUG_ENABLED           = true;
	public static final boolean DEBUG_FILE_ENABLED           = false;
 
    public static final String EXTRA_ACCOUNT_POST            = ".intent.extra.ACCOUNT";
    public static final String EXTRA_FOLDER_POST             = ".intent.extra.FOLDER";
    public static final String EXTRA_SENT_DATE_POST          = ".intent.extra.SENT_DATE";
    public static final String EXTRA_FROM_POST               = ".intent.extra.FROM";
    public static final String EXTRA_TO_POST                 = ".intent.extra.TO";
    public static final String EXTRA_CC_POST                 = ".intent.extra.CC";
    public static final String EXTRA_BCC_POST                = ".intent.extra.BCC";
    public static final String EXTRA_SUBJECT_POST            = ".intent.extra.SUBJECT";
    public static final String EXTRA_FROM_SELF_POST          = ".intent.extra.FROM_SELF";
    
    //public static final String WATCHFACE_URL  = "https://s3.amazonaws.com/k9topebble.co.uk/releases/1.05/K9ToPebble.pbw";

    public final static UUID PEBBLE_APP_UUID = UUID.fromString("c4447eb9-5f01-4f3f-92b8-d2ea8f209830");
    
    public static interface MessageColumns extends BaseColumns {
        /**
         * The number of milliseconds since Jan. 1, 1970, midnight GMT.
         *
         * <P>Type: INTEGER (long)</P>
         */
        String SEND_DATE = "date";

        /**
         * <P>Type: TEXT</P>
         */
        String SENDER = "sender";

        /**
         * <P>Type: TEXT</P>
         */
        String SENDER_ADDRESS = "senderAddress";

        /**
         * <P>Type: TEXT</P>
         */
        String SUBJECT = "subject";

        /**
         * <P>Type: TEXT</P>
         */
        String PREVIEW = "preview";

        /**
         * <P>Type: BOOLEAN</P>
         */
        String UNREAD = "unread";
        String K9PURE_READ = "read_flag";

        /**
         * <P>Type: TEXT</P>
         */
        String ACCOUNT = "account";

        /**
         * <P>Type: INTEGER</P>
         */
        String ACCOUNT_NUMBER = "accountNumber";

        /**
         * <P>Type: BOOLEAN</P>
         */
        String HAS_ATTACHMENTS = "hasAttachments";

        /**
         * <P>Type: BOOLEAN</P>
         */
        String HAS_STAR = "hasStar";

        /**
         * <P>Type: INTEGER</P>
         */
        String ACCOUNT_COLOR = "accountColor";

        String URI = "uri";
        String DELETE_URI = "delUri"; 
    }    

    

    final public static int MAJOR_VERSION = 1;
    final public static int MINOR_VERSION = 10;
    final public static int PROTOCOL_VERSION = 6;

	final public static int MAX_URL_LENGTH = 100;
	final public static int MAX_SENDER_LENGTH = 50;
	final public static int MAX_SUBJECT_LENGTH = 100;
	final public static int MAX_BODY_PACKET = 97;

    final public static int MAX_BODY_SIZE = MAX_BODY_PACKET * 10;

    final public static int eMT_RequestStart = 0;
    final public static int eMT_RequestBody = 1;
    final public static int eMT_RequestStop = 2;
    final public static int eMT_RequestLog = 3;
    final public static int eMT_SendImage = 4;
    final public static int eMT_RequestMissing = 5;
    final public static int eMT_ReplyPong = 6;
    final public static int eMT_RequestDelete = 7;
    final public static int eMT_ConfirmTag = 8;
    
    final public static int eMT_Reset = 50;
    final public static int eMT_Update = 51;
    final public static int eMT_Body = 52;
    final public static int eMT_ErrorMsg = 53;
    final public static int eMT_RequestPing = 54;
    final public static int eMT_ConfirmDelete = 55;
    final public static int eMT_Config = 56;
    
    final public static int KEY_COMMAND   = 0;
    final public static int KEY_URL       = 1;
    final public static int KEY_PROTOCOL_VERSION = 2;
    final public static int KEY_MESSAGE  = 3;
    final public static int KEY_IMG_SIZE = 4;
    final public static int KEY_EXPECTED = 5;
    final public static int KEY_LAST     = 6;
    final public static int KEY_PONG_TAG = 7;
    final public static int KEY_BODY_TEXT_SIZE= 8;
	final public static int KEY_INBOX_TEXT_SIZE= 9;
	
	final public static int KEY_IMG_START = 100;
	
    final public static int KEY_UUID_OFFSET = 0;
    final public static int KEY_SENDER_OFFSET = 1;
    final public static int KEY_SUBJECT_OFFSET = 2;
    final public static int KEY_UNREAD_OFFSET = 3;
    final public static int KEY_NEW_OFFSET = 4;
    final public static int KEY_DELETED_OFFSET = 5;    
    
    final public static int KEY_START = 100;
    final public static int KEY_INCREMENT = 100;
    
    /// commands from broadcastreciever
	public final static int COMMAND_START = 1;
	public final static int COMMAND_STOP  = 2;
	public final static int COMMAND_BODY  = 3;
	public final static int COMMAND_ACK   = 4;
	public final static int COMMAND_NACK  = 5;
	public final static int COMMAND_IMAGE  = 6;
	public final static int COMMAND_MISSING = 7;
	public final static int COMMAND_DELETE = 8;
	public final static int COMMAND_DELETE_NOTIFY = 9;
	public final static int COMMAND_INBOX_CHANGED = 10;
	public final static int COMMAND_ACTIVATE = 11;
	
	public final static String COMMAND_EXTRA = "command";
	public final static String UUID_EXTRA = "uuid";
	public final static String TRANSACTION_EXTRA = "trans";
	public final static String IMG_POS = "imgpos";
	public final static String IMG_DATA = "imgdata";
	public final static String RANGE_START = "rs";
	public final static String RANGE_END = "re";
	}
