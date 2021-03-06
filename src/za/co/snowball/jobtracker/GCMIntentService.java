/*
 * GCMIntentService
 * 
 * Handles all incoming messages and takes appropriate action
 * 
 * The main message handler for messages from the Messaging server is onMessage
 * 
 * Check out cool sample notification code here:
 * https://android.googlesource.com/platform/packages/providers/DownloadProvider/+/52b703c5d0c4cff72bafdec0e2229368d3cc20d0/src/com/android/providers/downloads/DownloadNotifier.java
 * 
 */

package za.co.snowball.jobtracker;

import static za.co.snowball.jobtracker.CommonUtilities.SENDER_ID;
import static za.co.snowball.jobtracker.CommonUtilities.displayMessage;

import java.util.ArrayList;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import com.google.android.gcm.GCMBaseIntentService;

import za.co.snowball.jobtracker.db.MyContentProvider;

import android.app.Notification;
import android.app.Notification.InboxStyle;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.util.Log;

public class GCMIntentService extends GCMBaseIntentService implements AsyncResponse {

	private static final String TAG = "GCMIntentService";
	
	private static final int NOTIFY_ME_ID=1337;
	
	HTTPTask asyncTask;

	private Context mContext;
	
    public GCMIntentService() {
        super(SENDER_ID);
    }

    /**
     * Method called on device registered
     **/
    @Override
    protected void onRegistered(Context context, String registrationId) {
    	mContext = context;
        Log.i(TAG, "Device registered: regId = " + registrationId);
        
        ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(4);
		nameValuePairs.add(new BasicNameValuePair("action", "register"));
		nameValuePairs.add(new BasicNameValuePair("regId", registrationId));				
		nameValuePairs.add(new BasicNameValuePair("name", Build.MODEL));
		//String firstEmailAccount = CommonUtilities.getDeviceAccounts(this);
		String firstEmailAccount = CommonUtilities.getEmailAddress(this);
		nameValuePairs.add(new BasicNameValuePair("email", firstEmailAccount));
		TelephonyManager telephonyManager;
		telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		nameValuePairs.add(new BasicNameValuePair("device_id", telephonyManager.getDeviceId()));
		doAsyncTask(nameValuePairs);
        //ServerUtilities.register(context, registrationId);
        //displayMessage(context, "Your device registered with Cloud Messaging");
    }
    
    @SuppressWarnings("unchecked")
	private void doAsyncTask(ArrayList<NameValuePair> nameValuePairs) {
		asyncTask = new HTTPTask(this);
		asyncTask.delegate = GCMIntentService.this;
		asyncTask.execute(nameValuePairs);
	}
	
	@Override
	public void asyncProcessFinish(String output) {
		//displayMessage(mContext, "Your device registered with Cloud Messaging");
		displayMessage(mContext, output);
		//Toast.makeText(MainActivity.this, output, Toast.LENGTH_SHORT).show();
	}

    /**
     * Method called on device unregistered
     * */
    @Override
    protected void onUnregistered(Context context, String registrationId) {
        Log.i(TAG, "Device unregistered");
        displayMessage(context, getString(R.string.gcm_unregistered));        
        //String firstEmailAccount = CommonUtilities.getDeviceAccounts(this);		       
        String firstEmailAccount = CommonUtilities.getEmailAddress(this);
        ServerUtilities.unregister(context, registrationId, firstEmailAccount);
    }

    /**
     * Method called on Receiving a new message
     * */
    @Override
    protected void onMessage(Context context, Intent intent) {
    	// jobUri will be created when a new record is inserted or an existing record is updated
    	// This is passed to generateNotification which will add it to the intent    	
    	
    	// TODO dbRecordId not used since migration to MessageReceiver
    	int dbRecordId = 0;
    	
    	String messageType = "";
        String message = intent.getExtras().getString("price");
        Log.i(TAG, "Received message '" + message + "' and now examining content...");
        
        
		// The AndroidHive demo return NULL first time you register so we handle it gracefully
        if (message == null) {
        	messageType = "First time registration! Welcome!";
        	Log.e(TAG, "Message is NULL assuming first time registration...!");
        } else {
        	MessageReceiver incomingMessage = new MessageReceiver(context);
        	messageType = incomingMessage.checkType(message); 
//        	String action = JobsContentProvider.getAction(message);
//        	if (action != null) {
//        		values = JobsContentProvider.convertMessageToContentValues(action, message);
//        		department = values.getAsString("department");
//        		extra = values.getAsString("extra");
//        		if (action.equals("insert")) {
//        			// Insert record and obtain ID for use in NotificationIntent
//        			Uri jobUri = getContentResolver().insert(JobsContentProvider.CONTENT_URI_JOBS, values);
//        			long newDbRecord = ContentUris.parseId(jobUri);
//        			// Convert long to int because Pending Intent Request Codes need to be integer
//        			dbRecordId = (int) newDbRecord;
//        			Log.d(TAG, "New ID generated after GCM payload: " + dbRecordId);
//        			messageType = "New " + department;
//        		} else if (action.equals("update")) {
//        			dbRecordId = values.getAsInteger("calendar_id");
//        			Uri todoUri = Uri.parse(JobsContentProvider.CONTENT_URI_JOBS + "/ticket" + "/" + dbRecordId);        			
//        			getContentResolver().update(todoUri, values, null, null);
//        			// Update notes (for now, insert)
//        			ContentValues values2 = JobsContentProvider.getNotes(action, message);
//        			getContentResolver().insert(JobsContentProvider.CONTENT_URI_NOTES, values2);
//        			
//        			messageType = extra;
//        		} else if (action.equals("delete")) {        			
//        			dbRecordId = values.getAsInteger("calendar_id");
//        			Log.d(TAG, "Action delete being executed on calendar_id " + String.valueOf(dbRecordId));
//        			Uri todoUri = Uri.parse(JobsContentProvider.CONTENT_URI_JOBS + "/ticket" + "/" + dbRecordId);        			
//        			getContentResolver().delete(todoUri, null, null);
//        			//messageType = "Deleted " + department;
//        			messageType = "Deleted item";
//        		}
//        	} else {
//        		messageType = "System Message: " + message;
//        	}
        }
        displayMessage(context, messageType);        
		generateNotification(context, messageType, dbRecordId);                    
    }
    
    /**
     * http://stackoverflow.com/questions/6391870/how-exactly-to-use-notification-builder
     * 
     * @param context
     * @param message
     */
    private static void generateNotification(Context context, String message, int dbRecordId) {
    	SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(context);
    	String pref_notification_sound = mPrefs.getString("notification_sound", "DEFAULT_RINGTONE_URI");
    	Uri soundUri = Uri.parse(pref_notification_sound);
    	
    	String title = context.getString(R.string.app_name);
    	
    	// Old code commented out because we don't launch to JobDetailActivity any more
    	//Intent notificationIntent = new Intent(context, JobDetailActivity.class);
    	Intent notificationIntent = new Intent(context, JobListActivity.class);
    	if (dbRecordId != 0) {
    		Uri jobUri = Uri.parse(MyContentProvider.CONTENT_URI_JOBS + "/" + dbRecordId);
    		notificationIntent.putExtra(MyContentProvider.CONTENT_ITEM_TYPE, jobUri);
    		// Indicate we're coming from GCMIntent
    		//notificationIntent.putExtra("gcmTrue", "true");
    		Log.d(TAG, "Adding jobUri " + jobUri + " to intent");
    	}		
    	notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
    	
    	PendingIntent contentIntent = PendingIntent.getActivity(context, dbRecordId, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);
 
    	NotificationManager notificationManager = (NotificationManager) context
    	        .getSystemService(Context.NOTIFICATION_SERVICE);
    	    	    	
    	App.addMessage(message);   	
    	
    	int pendingNotificationsCount = App.getPendingNotificationsCount() + 1;
        App.setPendingNotificationsCount(pendingNotificationsCount);
    	
    	String eventsSummary = "";
    	if (pendingNotificationsCount > 0) {
    		//eventsSummary = Integer.toString(count) + " new events";
    		eventsSummary = "New events";
    	}
    	
    	Resources resources = context.getResources();
    	Notification.Builder builder = new Notification.Builder(context);
    	builder.setContentIntent(contentIntent)    				
    	       .setSmallIcon(R.drawable.snowball_statusbar_36px)
    	       .setLargeIcon(BitmapFactory.decodeResource(resources, R.drawable.snowball_list_48px))
    	       .setTicker(message)    	       
    	       .setWhen(System.currentTimeMillis())
    	       .setAutoCancel(true)
    	       .setContentTitle(title)
    	       .setContentText(message)
    	       
    	       .setNumber(pendingNotificationsCount)
    	       .setSound(soundUri);
    	
    	// .setContentInfo(eventsSummary)
    	// .setNumber(count)
    	       
    	InboxStyle inboxStyle = new Notification.InboxStyle(builder);
    	
    	for (String m : App.getMessages()) {
    		inboxStyle.addLine(m);
    	}
    	inboxStyle.setSummaryText(eventsSummary);
    	
    	Notification notif = inboxStyle.build();
    	
    	notif.flags |= Notification.FLAG_AUTO_CANCEL;    	
    	notificationManager.notify(NOTIFY_ME_ID, notif);
    }
    
    /**
     * Overloaded generateNotification method using a signature that doesn't require the optional parameter
     * Since it called from various locations
     * 
     * @param context
     * @param message
     */
    private static void generateNotification(Context context, String message) {
    	generateNotification(context, message);
    }

    /**
     * Method called on receiving a GCM deleted message
     * */
    @Override
    protected void onDeletedMessages(Context context, int total) {
        Log.i(TAG, "Received deleted messages notification");
        String message = getString(R.string.gcm_deleted, total);
        displayMessage(context, message);
        // notifies user
        generateNotification(context, message);
    }

    /**
     * Method called on Error
     * */
    @Override
    public void onError(Context context, String errorId) {
        Log.i(TAG, "Received error: " + errorId);
        displayMessage(context, getString(R.string.gcm_error, errorId));
    }

    @Override
    protected boolean onRecoverableError(Context context, String errorId) {
        // log message
        Log.i(TAG, "Received recoverable error: " + errorId);
        displayMessage(context, getString(R.string.gcm_recoverable_error,
                errorId));
        return super.onRecoverableError(context, errorId);
    }

}
