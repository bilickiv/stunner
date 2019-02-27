package hu.uszeged.inf.wlab.stunner.utils;

/**
 * Utility class to hold the general constants.
 * 
 * @author szelezsant
 */
public final class Constants {

	/** The default timeout of each test. */
	public static final int DEFAULT_TIMEOUT = 7900;

	/* bundle keys */
	/** The address of the selected STUN server. */
	public static final String KEY_SERVER_ADDRESS = "serverAddress";
	/** The port of the STUN server where the service can be accessed. */
	public static final String KEY_SERVER_PORT = "serverPort";
	/** The visible IP address of the device. */
	public static final String KEY_IP_ADDRESS = "ipAddress";
	/** The key to get the data. */
	public static final String KEY_DATA = "data";
    /** The key to get the type. */
    public static final String KEY_TYPE = "type";
    /** The key to get the receiver. */
	public static final String KEY_RECEIVER = "receiver";
	/** The key to get the wifi dto. */
	public static final String KEY_WIFI_DTO = "wifiDTO";
	/** The key to get the mobile dto. */
	public static final String KEY_MOBILE_DTO = "mobileDTO";
	/** The key to get the connection ready flag. */
	public static final String KEY_CONNECTION_READY = "connectionReady";
	/** The key to get the connection ready flag. */
	public static final String KEY_CONNECTION_ENABLED = "connectionEnabled";

	/** intent extra keys */
	public static final String KEY_DISCOVERY_DTO = "discoveryDTO";
	public static final String KEY_DISCOVERY_DTO_LIST = "discoveryDtoList";
	public static final String KEY_P2P_RESULTS = "p2pResults";
	public static final String KEY_WEBRTC_RESULTS = "webRTCResults";
	public static final String KEY_LAST_DISCONNECT = "lastDisconnect";
	public static final String KEY_STATE = "state";
    public static final String KEY_JOB_PARAMS = "JobParameters";
    public static final String KEY_ID = "identifierUnique";
    public static final String KEY_EXIT_STATUS = "exitStatus";


	/** intent extra keys P2PService */
    public static final String KEY_OFFER ="offer";
    public static final String KEY_START ="start";
    public static final String KEY_LOGIN ="login";
    public static final String KEY_UPDATE_STATE ="updateState";
    public static final String KEY_ENABLE_P2P ="enableP2P";
    public static final String KEY_CLOSE_CONNECTION ="closeConnection";
    public static final String KEY_ANSWER ="answer";
    public static final String KEY_REJECT ="reject";
    public static final String KEY_ICE ="ice";
	public static final String KEY_MESSAGE_DATA = "messageData";
    public static final String KEY_LOCAL_ANDROID_ID = "localAndroidID";
    public static final String KEY_TIMESTAMP = "timestamp";
    public static final String KEY_CONNECTION_ID = "connectionID";
    public static final String KEY_REMOTE_PEER_ID = "remotePeerID";
	public static final String KEY_FIREBASE_MESSAGE_FROM_WHO = "fromWho";
	public static final String KEY_IS_FIRST_P2P_TIMEOUT_HAPPENED_BOOLEAN = "isFirstP2PTimeoutHappened";

	public static final String VALUE_NOTIFICATIONS_TABLE = "notifications";
	public static final String VALUE_NOTIFICATIONS_TO_RANDOM_DEVICE_TABLE = "notificationsToRandomDevices";

    /** possible service state monitor intent action */
	public static final String USER_TRIGGERED_SERVICE_START = "hu.uszeged.inf.wlab.stunner.service.ServiceMonitor.UserTriggeredServiceStart";
	public static final String BACKGROUND_NAT_DISCOVERY_WEBRTC_TEST_AND_P2P_SERVICE_START = "hu.uszeged.inf.wlab.stunner.service.ServiceMonitor.BACKGROUND_NAT_DISCOVERY_WEBRTC_TEST_AND_P2P_SERVICE_START";
	public static final String NAT_DISCOVERY_AND_WEBRTC_TEST_SERVICE_FINISHED = "hu.uszeged.inf.wlab.stunner.service.ServiceMonitor.NAT_DISCOVERY_AND_WEBRTC_TEST_SERVICE_FINISHED";
	public static final String P2P_SERVICE_FINISHED = "hu.uszeged.inf.wlab.stunner.service.ServiceMonitor.P2PServiceFinished";
	public static final String FIREBASE_MESSAGE_IS_RECEIVED = "hu.uszeged.inf.wlab.stunner.service.ServiceMonitor.FIREBASE_MESSAGE_IS_RECEIVED";
    public static final String STOP_EVERYTHING = "hu.uszeged.inf.wlab.stunner.service.ServiceMonitor.STOP_EVERYTHING";
    public static final String FIREBASE_UPDATE_STATE = "hu.uszeged.inf.wlab.stunner.service.ServiceMonitor.FIREBASE_UPDATE_STATE";
    public static final String SERVICE_MONITOR_RESTART_IS_REQUIRED = "hu.uszeged.inf.wlab.stunner.service.ServiceMonitor.SERVICE_MONITOR_RESTART_IS_REQUIRED";
    public static final String SERVICE_START_IS_NOT_NECESSARY = "hu.uszeged.inf.wlab.stunner.service.ServiceMonitor.SERVICE_START_IS_NOT_NECESSARY";

	/** possible p2p service state */
    public static final String IDLE = "hu.uszeged.inf.wlab.stunner.service.P2PConnectionStarterService.IDLE";
    public static final String HAVE_ALREADY_STARTED = "hu.uszeged.inf.wlab.stunner.service.P2PConnectionStarterService.HAVE_ALREADY_STARTED";
    public static final String HAVE_ALREADY_FINISHED_BUT_WAITING_FOR_OTHER_SERVICES_RESULTS = "hu.uszeged.inf.wlab.stunner.service.P2PConnectionStarterService.HAVE_ALREADY_FINISHED_BUT_WAITING_FOR_OTHER_SERVICES_RESULTS";
    //public static final String HAVE_ALREADY_FINISHED_AND_SAVED = "hu.uszeged.inf.wlab.stunner.service.P2PConnectionStarterService.HAVE_ALREADY_FINISHED_AND_SAVED";

	/**
	 * Preference key to indicate if the user agreed with the background reporting.
	 */
	public static final String PREF_KEY_BACKGROUND_SERVICE = "prefAllowBackground";
	/** Preference key to get the previously active network type. */
	public static final String PREF_KEY_PREV_NETWORK_TYPE = "prevNetworkType";
	/** */
	public static final String PREF_KEY_PARSE_SERVICE = "prefAllowParseUpload";
	/** Preference key to get the timeout of each test. */
	public static final String PREF_KEY_TIMEOUT = "prefTimeout";
	/** Preference key to get the previous network subtype. */
	public static final String PREF_KEY_PREV_NETWORK_SUBTYPE = "prefNetworkSubType";

	public static final String PREF_STRING_VALUE_EMPTY = "N/A";

	/** Result code to indicate the start of the connection test. */
	public static final int RESULT_CONNECTION_START = 0;
	/** Result code to indicate the success of the connection test. */
	public static final int RESULT_CONNECTION_OK = 1;
	/** Result code to indicate an error of the connection test. */
	public static final int RESULT_CONNECTION_ERROR = 2;
	/** Result code to indicate the success of the stun test. */
	public static final int RESULT_STUN_OK = 3;
	/** Result code to indicate the update of the log. */
	public static final int RESULT_STUN_LOG = 4;
	/** Result code to an error of the stun test. */
	public static final int RESULT_STUN_ERROR = 5;

	/* app constants */
	/** The type of the account used by the sync adapter. */
	public static final String ACCOUNT_TYPE = "hu.uszeged.inf.wlab.stunit";
	/** Dummy account name. */
	public static final String DUMMY_ACCOUNT = "dummy";
	/** IPv4 address format. */
	public static final String IP_FORMAT = "%d.%d.%d.%d";

	/** License string. */
	public static final String LICENSE = "<p><b>This application is a part of FICT - Future ICT.</b><br/><br/>"
			+ "http://www.futurict.hu<br/><br/>"
			+ "This application is a simple lightweight STUN utility. It shows the user the type of NAT (if there is) his/her"
			+ " device is behind. It also shows the parameters of the active and available connection user can save tests as bookmarks. "
			+ "Furthermore, the application contains the opportunity of Peer-to-Peer connection with other mobile devices that use Stunner in order to test the Peer-to-Peer capabilities of mobile devices."
			+ "<br/><br/>"
			+ "The application is developed by:<br/><br/> <b>University of Szeged, Department of Software Engineering.</b>"
			+ "<br/><br/>"
			+ "<b>The application sends ANONYMOUS statistics to a Server maintained by University of Szeged and user agrees by using this application to use his/her NAT, Peer-to-Peer, location, battery and IP data ANONYMOUSLY.</b>"
			+ "<br/><br/>It is guaranteed that all data that could identify a device is hashed by an algorithm which cannot be reverted by the people who are evaluating these statistics. The statistics are used for R&D purposes and will be deleted when FICT project ends."
			+ "<p><b>This application uses JSTUN library.</b><br/><br/>" + " Copyright (c) 2005 Thomas King <king@t-king.de> - All rights"
			+ " reserved."
			+ "<br/>http://jstun.javawi.de<br/><br/>";

	/** The API key required to use the upload servlet. */
	public static final String API_KEY = "b4d9ca79fb9ffb1329f5160cb0161ffe36fc7ce8";
	public static final int API_LEVEL_JOB_SERVICE = 24; //Nougat
	/** The package name used when uploading the request. */
	public static final String PACKAGE = "hu.uszeged.inf.wlab.stunner";
	/** The URL of the servlet. */
	public static final String SERVLET_URL = "https://addYourServerUrl";

	/** The action to trigger regular battery checks. */
	public static final String ACTION_BATTERY_CHECK = "hu.uszeged.inf.wlab.stunner.ACTION_BATTERY_CHECK";

	/** The action to trigger regular online state checks. */
	public static final String ACTION_STATE_CHECK = "hu.uszeged.inf.wlab.stunner.ACTION_STATE_CHECK";

	/** The action of user interaction trigger event */
	public static final String ACTION_USER = "hu.uszeged.inf.wlab.stunner.ACTION_USER";
	public static final String AIRPLANE_MODE_CHANGED = "hu.uszeged.inf.wlab.stunner.AIRPLANE_MODE_CHANGED";


	public static final String ACTION_CONNECTION_ESTABLISHED = "hu.uszeged.inf.wlab.stunner.ACTION_CONNECTION_ESTABLISHED";
	public static final String ACTION_CONNECTION_LOST = "hu.uszeged.inf.wlab.stunner.ACTION_CONNECTION_LOST";
	/**
	 * The action to trigger background service immediately to record starting / ending states.
	 */
	public static final String ACTION_SERVICE_TOGGLED = "hu.uszeged.inf.wlab.stunner.ACTION_SERVICE_TOGGLED";

	/**
	 * The action to trigger background service immediately to record starting / ending states.
	 */
	public static final String ACTION_SERVICE_TOGGLED_OFF = "hu.uszeged.inf.wlab.stunner.ACTION_SERVICE_TOGGLED_OFF";

	/**
     * The action to trigger discovery by P2P background service message is arrived from peer.
     */
    public static final String ACTION_FIREBASE_MESSAGE_IS_RECEIVED = "hu.uszeged.inf.wlab.stunner.ACTION_FIREBASE_MESSAGE_IS_RECEIVED";


	/**
	 * Support constant for API 17 prior devices to define the wireless charging mode.
	 */
	public static final int BATTERY_PLUGGED_WIRELESS = 4;
	/**
	 * Support constants for API 11 prior devices to define the battery health "cold" constant.
	 */
	public static final int BATTERY_HEALT_COLD = 7;
	/** Bundle key to retrieve the startId of a process. */
	public static final String KEY_START_ID = "startId";
	/** Intent action to register alarms on the first start of the activity. */
	public static final String ACTION_REGISTER_ALARMS_FIRST_RUN = "hu.uszeged.inf.wlab.stunner.ACTION_REGISTER_ALARM";
	/** The ratio used when exchanging milliseconds to hours. */
	public static final int MILLISEC_TO_SECOND_RATIO = 1000 ;
	public static final int MILLISEC_TO_MINUTE_RATIO = 60 * MILLISEC_TO_SECOND_RATIO ;
	public static final int MILLISEC_TO_HOURS_RATIO = 60 * MILLISEC_TO_MINUTE_RATIO;
	public static final int MILLISEC_TO_DAY_RATIO = 24 * MILLISEC_TO_HOURS_RATIO;
	public static final int MILLISEC_TO_WEEK_RATIO = 7 * MILLISEC_TO_DAY_RATIO;


	/**
	 * The interval between the battery tests should run when the device has active connection. Value given in milliseconds.
	 */
	public static final int DISCOVERY_START_INTERVAL_ONLINE = 10 * MILLISEC_TO_MINUTE_RATIO;

	/**
	 * The interval for waiting in P2P handler. Value given in milliseconds.
	 */
	public static final int P2P_WAIT_INTERVAL_ONLINE = 60 * MILLISEC_TO_SECOND_RATIO;
	//public static final int DISCOVERY_START_INTERVAL_ONLINE = 3 * MILLISEC_TO_MINUTE_RATIO;
	/**
	 * The interval between the battery tests should run when the device is offline. Value given in milliseconds.
	 */
	public static final int DISCOVERY_START_INTERVAL_OFFLINE = 30 * MILLISEC_TO_MINUTE_RATIO;
	//public static final int DISCOVERY_START_INTERVAL_OFFLINE = 2 * MILLISEC_TO_MINUTE_RATIO;
	/** 100 percent. */
	public static final int HUNDRED_PERCENT = 100;

	/**
	 * The interval between the state tests should run. Value given in milliseconds.
	 */
	public static final int STATE_CHECK_INTERVAL = 1 * MILLISEC_TO_MINUTE_RATIO;
	/**
	 * The interval until waiting for end of services. Value given in milliseconds.
	 */
	public static final int SERVICE_END_WAITING_INTERVAL = 1 * MILLISEC_TO_MINUTE_RATIO;

	/**
	 * Last refreshed date, need this when clear the daily upload limit, and when deleting the old records from database (it also happens
	 * daily).
	 */
	public static final String LAST_REFRESH_YEAR = "lastRefreshYear";
	public static final String LAST_REFRESH_MONTH = "lastRefreshMonth";
	public static final String LAST_REFRESH_DAY = "lastRefreshDay";

	/** The amount of record we uploaded today */
	public static final String TODAY_UPLOADED_RECORD_COUNT = "todayUploadedRecordCount";
	/** The maximum amount of record we can upload per day */
	public static final int MAX_DAILY_UPLOAD_LIMIT = 500;

	public static final String REMOTE_PARSE_SERVER_TABLE_NAME = "Stuntest";
	public static final String FICT_SERVER_LOCAL_PARSE_TABLE_NAME = "fictServer";

	public static final String TABLE_PEER_CANDIDATES = "PeerCandidates";
	public static final String TABLE_USERS = "Users";
	public static final String FIELD_ONLINE = "online";
	public static final String FIELD_TOKEN_ID = "tokenID";
	public static final String FIELD_LAST_DISCONNECT = "lastDisconnect";

    /**
	 * Hidden constructor.
	 */
	private Constants() {
	}

}
