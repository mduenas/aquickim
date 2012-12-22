package com.markduenas.android.aquickim;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.*;
import android.util.Log;

public class aquickimSMSReceiver extends BroadcastReceiver {
 
        private static final String LOG_TAG = "SMSReceiver";
        static final String ACTION = "android.provider.Telephony.SMS_RECEIVED";
        
        static boolean replyToIM = false;

        @Override
		public void onReceive(Context context, Intent intent) {
	        	String from = "";
	        	NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
	        	if (intent.getAction().equals(ACTION)) {
		        	// if(message starts with SMStretcher recognize BYTE)
		        	StringBuilder sb = new StringBuilder();
		
		        	Bundle bundle = intent.getExtras();
		        	if (bundle != null) {
		        	/* Get all messages contained in the Intent*/
		        	SmsMessage[] messages = getMessagesFromIntent(intent);
	
		        	/* Feed the StringBuilder with all Messages found. */
		        	for (SmsMessage currentMessage : messages){
		        		sb.append("Received compressed SMS\nFrom: ");
		        		/* Sender-Number */
		        		sb.append(currentMessage.getDisplayOriginatingAddress());
		        		// set
		        		from = currentMessage.getDisplayOriginatingAddress();
		        		sb.append("\n—-Message—-\n");
		        		/* Actual Message-Content */
		        		sb.append(currentMessage.getDisplayMessageBody());
		        	}
	        	}
	        	/* Logger Debug-Output */
	        	Log.i(LOG_TAG, "[SMSApp] onReceiveIntent: " + sb);
	        	this.abortBroadcast();
	        	
	        	//TODO: make this work so that it unlocks
	        	/* check if the user wants to answer with aquickim */
	        	if (ManageKeyguard.inKeyguardRestrictedInputMode()) {
	        	    ManageWakeLock.acquireFull(context);
        	    }
	        	/* check if the keyguard is on */
	        	ManageKeyguard.disableKeyguard(context);
	        	
  	        	/* Start the Main-Activity */
  	        	Intent i = new Intent(context, aquickim.class);
  	        	i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
  	        	Bundle b = new Bundle();
  	        	b.putString("OriginatingAddress", from);
  	        	i.putExtras(b);
  	        	context.startActivity(new Intent(i));
  	        	
  	        	//ManageWakeLock.releaseFull();
	
	        	//CharSequence from = currentMessage.getDisplayOriginatingAddress();
	        	//CharSequence appName = "SMSNotifyExample";
	        	//String tickerMessage = sb.toString();
	        	//Long theWhen = System.currentTimeMillis();
	        	//Intent notificationIntent = new Intent(context, aquickim.class);
	        	//PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);
	        	//Notification notif = new Notification(R.drawable.mail_generic, tickerMessage + "***", 0);
	        	//notif.vibrate = new long[] { 100, 250, 100, 500};
	        	//notif.contentIntent = contentIntent;
	        	//nm.notify(R.string.alert_message, notif);
        	}
        }
        
 
    	/**
    	   * Read the PDUs out of an {@link #SMS_RECEIVED_ACTION} or a
    	   * {@link #DATA_SMS_RECEIVED_ACTION} intent.
    	   * 
    	   * @param intent
    	   *           the intent to read from
    	   * @return an array of SmsMessages for the PDUs
    	   */
    	  public static final SmsMessage[] getMessagesFromIntent(Intent intent) {
    	    Object[] messages = (Object[]) intent.getSerializableExtra("pdus");
    	    if (messages == null) {
    	      return null;
    	    }
    	    if (messages.length == 0) {
    	      return null;
    	    }

    	    byte[][] pduObjs = new byte[messages.length][];

    	    for (int i = 0; i < messages.length; i++) {
    	      pduObjs[i] = (byte[]) messages[i];
    	    }
    	    byte[][] pdus = new byte[pduObjs.length][];
    	    int pduCount = pdus.length;
    	    SmsMessage[] msgs = new SmsMessage[pduCount];
    	    for (int i = 0; i < pduCount; i++) {
    	      pdus[i] = pduObjs[i];
    	      msgs[i] = SmsMessage.createFromPdu(pdus[i]);
    	    }
    	    return msgs;
    	  }
}