package com.markduenas.android.aquickim;

import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.markduenas.android.aquickim.aquickimDB.Row;
import com.markduenas.android.businesscard.ContactAccessor;
import com.markduenas.android.businesscard.ContactInfo;

public class aquickim extends Activity  implements View.OnClickListener, View.OnLongClickListener {
    /**
     * An SDK-specific instance of {@link ContactAccessor}.  
     * The activity does not need to know what SDK it is running in: 
     * all idiosyncrasies of different SDKs are encapsulated in the 
     * implementations of the ContactAccessor class.
     */
    private final ContactAccessor mContactAccessor = ContactAccessor.getInstance();
	// Request code for the contact picker activity
    private static final int PICK_CONTACT_REQUEST = 1;
    private static final int DO_SPEECHSTUFF_REQUEST = VoiceRecognition.VOICE_RECOGNITION_REQUEST_CODE;
    // The constants for the two menu items
	private static final int ADD_ID = Menu.FIRST+2;
	private static final int DELETE_ID = Menu.FIRST+4;
	private static final int CLOSE_ID = Menu.FIRST+6;
	// database
	private static aquickimDB db = null;
	// the list of contacts
	private static List<Row> contactList = null;
	// the currently selected contact id
	private static long current_id = 0;
	private static long next_id = 0;
	private static long previous_id = 0;
	// UI widgets
	Button msg;
	Button quickmsg1;
	Button quickmsg2;
	Button quickmsg3;
	Button quickmsg4;
	Button quickmsg5;
	Button quickmsg6;
	Button voiceRecognition;
	TextView tvContact1;
	TextView tvContact2;
	TextView tvContact3;
	TextView tvContactLabel1;
	TextView tvContactLabel2;
	TextView tvContactLabel3;
	TableLayout pageCurrent;
	TableLayout pagePrevious;
	TableLayout pageNext;
	
	private static GestureDetector gestureDetector = null;
	View.OnTouchListener gestureListener;
	
	private boolean selectingContact = false;

    /**
     * Called when the activity is first created.
     */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.main);

		pageCurrent = (TableLayout)findViewById(R.id.TableLayout01);
		pageNext = (TableLayout)findViewById(R.id.TableLayout02);
		pagePrevious = (TableLayout)findViewById(R.id.TableLayout03);
		
		setButtonListener(pageCurrent, R.id.quickmsg1);
		setButtonListener(pageCurrent, R.id.quickmsg2);
		setButtonListener(pageCurrent, R.id.quickmsg3);
		setButtonListener(pageCurrent, R.id.quickmsg4);
		setButtonListener(pageCurrent, R.id.quickmsg5);
		setButtonListener(pageCurrent, R.id.quickmsg6);
		setButtonListener(pageNext, R.id.quickmsg1);
		setButtonListener(pageNext, R.id.quickmsg2);
		setButtonListener(pageNext, R.id.quickmsg3);
		setButtonListener(pageNext, R.id.quickmsg4);
		setButtonListener(pageNext, R.id.quickmsg5);
		setButtonListener(pageNext, R.id.quickmsg6);
		setButtonListener(pagePrevious, R.id.quickmsg1);
		setButtonListener(pagePrevious, R.id.quickmsg2);
		setButtonListener(pagePrevious, R.id.quickmsg3);
		setButtonListener(pagePrevious, R.id.quickmsg4);
		setButtonListener(pagePrevious, R.id.quickmsg5);
		setButtonListener(pagePrevious, R.id.quickmsg6);
		
		//setButtonListener(pageCurrent, R.id.speechTest);
		//setButtonListener(pageNext, R.id.speechTest);
		//setButtonListener(pagePrevious, R.id.speechTest);

		tvContact1=(TextView)pageCurrent.findViewById(R.id.tvContact);
		tvContact2=(TextView)pageNext.findViewById(R.id.tvContact);
		tvContact3=(TextView)pagePrevious.findViewById(R.id.tvContact);
		tvContact1.setText(R.string.text_view_contact_selection);
		tvContact2.setText(R.string.text_view_contact_selection);
		tvContact3.setText(R.string.text_view_contact_selection);
		
		tvContactLabel1=(TextView)pageCurrent.findViewById(R.id.TextView01);
		tvContactLabel2=(TextView)pageNext.findViewById(R.id.TextView02);
		tvContactLabel3=(TextView)pagePrevious.findViewById(R.id.TextView03);
		tvContactLabel1.setText(R.string.text_view_current_contact);
		tvContactLabel2.setText(R.string.text_view_current_contact);
		tvContactLabel3.setText(R.string.text_view_current_contact);
		
		db = new aquickimDB(this);
		db.onCreate(db.getWritableDatabase());
		// reset stuff on startup
		resetCurrentContact();
		
		// now listen for gestures
		ViewFlipper viewFlip = (ViewFlipper) findViewById(R.id.viewFlipper);
		gestureDetector = new GestureDetector(new MyGestureDetector(this, viewFlip));
		gestureListener = new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                if (gestureDetector.onTouchEvent(event)) {
                    return true;
                }
                return false;
            }
        };
        if(savedInstanceState != null) {
	        String originatingAddress = savedInstanceState.getString("OriginatingAddress");
	        if(originatingAddress != null) {
	        	wakeUp();
	        	setReplyContact(originatingAddress);
	        }
        } else {
        	// 	Do nothing
        }
	}
	
	private void wakeUp() {
		if (ManageKeyguard.inKeyguardRestrictedInputMode()) {
			ManageWakeLock.acquireFull(this);
	    }
	    /* check if the keyguard is on */
	    ManageKeyguard.disableKeyguard(this);		
	}
	
	private void setReplyContact(String originatingAddress) {
    	/* grab the current contact id by contact address (phoneNumber) */
		int index = getCurrentContactIndexByPhone(originatingAddress);
		// set them all based on the current contact index
		if(index > -1) {
			current_id = setContactId(index, "current");
			next_id = setContactId(index, "next");
			previous_id = setContactId(index, "prev");
			setAllContactInfo(pageCurrent, current_id);
			setAllContactInfo(pageNext, next_id);
			setAllContactInfo(pagePrevious, previous_id);
		} else {
			toastMsg("No quickim contact found!", 3000);
		}
		
		ManageWakeLock.releaseFull();
	}
	
	/*
	 * get the current contacts index (in the List<Row>)
	 */
	private int getCurrentContactIndexByPhone(String phone) {
		int index = -1;
		for(Row r : contactList) {
			String tempPhone = r.phoneNumber.replace("-", "");
			if(tempPhone.equals(phone)) {
				index = contactList.indexOf(r);
			}
		}
		return index;
	}

	private void setButtonListener(LinearLayout l, int id) {
		msg=(Button)l.findViewById(id);
		msg.setOnClickListener(this);
		msg.setOnLongClickListener(this);
	}
	/* This is used to capture the onTouchEvent
	 * (non-Javadoc)
	 * @see android.app.Activity#onTouchEvent(android.view.MotionEvent)
	 */
	@Override
    public boolean onTouchEvent(MotionEvent event) {
        if (gestureDetector.onTouchEvent(event))
	        return true;
	    else
	    	return false;
    }
	/* 
	 * resets the contact
	 */
	private void resetCurrentContact() {
		// now fetch the new row and place it in the array
		contactList = db.fetchAllRows();
		if(!contactList.isEmpty()) {
			// initialize the current
			current_id = setContactId(0, "current");
			// if we can initialize the next
			if(contactList.size() > 1) {
				next_id = setContactId(0, "next");
				previous_id = setContactId(0, "prev");
			}
			setAllContactPages();
		} else {
			current_id = 0;
			next_id = 0;
			previous_id = 0;
		}
	}
	
	public boolean canFlipLeft() {
//		if ((previous_id > 0) == false) {
//			toastMsg("You've reached the first of the contacts.", 3000);
//		}
		return (previous_id > 0);
	}
	public boolean canFlipRight() {
//		if ((next_id > 0) == false) {
//			toastMsg("You've reached the end of the contacts.", 3000);
//		}
		return (next_id > 0);
	}
	
	/*
	 * return a valid quickim_id from the passed in current_index
	 * this pages through an array of contacts
	 * 
	 * NOTE:  You must always pass the index of the current contact
	 * 
	 */
	private long setContactId(int currentContactIndex, String whichPage) {
		int newIndex = 0;
		int maxIndex = contactList.size() - 1;
		// Handle the cases for the current page
		if(whichPage == "current") {
			if(currentContactIndex == -1) {
				newIndex = maxIndex;
			} else if (currentContactIndex == maxIndex + 1) {
				newIndex = 0;
			} else {
				newIndex = currentContactIndex;
			}
		}
		// Handle the cases for the next page
		if (whichPage == "next") {
			if(currentContactIndex == -1) {
				newIndex = 0;
			} else if(currentContactIndex == maxIndex + 1) {
				newIndex = 1;
			} else if(currentContactIndex == maxIndex) {
				newIndex = 0;
			} else {
				newIndex = currentContactIndex + 1;
			}
		}
		// Handle the cases for the previous page
		if (whichPage == "prev") {
			if(currentContactIndex == -1) {
				newIndex = maxIndex -1;
			} else if (currentContactIndex == maxIndex + 1) { 
				newIndex = maxIndex;
			} else if (currentContactIndex == 0) {
				newIndex = maxIndex;
			} else {
				newIndex = currentContactIndex - 1;
			}
		}
		
		return contactList.get(newIndex).aquickim_id;
	}
	
	public void setNextContact() {
		if(contactList.size() == 1) {
			current_id = contactList.get(0).aquickim_id;
			next_id = 0;
			previous_id = 0;
			return;
		}
		int index = 0;
		index = getCurrentContactIndex(index);
		// because the next contact is now our current
		index++;
		// set them all based on the current contact index
		current_id = setContactId(index, "current");
		next_id = setContactId(index, "next");
		previous_id = setContactId(index, "prev");
		//This is confusing!!!
		TableLayout temp = pageCurrent;
		pageCurrent = pageNext;
		pageNext = pagePrevious;
		pagePrevious = temp;
		setAllContactInfo(pageNext, next_id);
		setAllContactInfo(pagePrevious, previous_id);
	}
	
	public void setPreviousContact() {
		if(contactList.size() == 1) {
			current_id = contactList.get(0).aquickim_id;
			next_id = 0;
			previous_id = 0;
			return;
		}
		int index = 0;
		index = getCurrentContactIndex(index);
		// because the next contact is now our current
		index--;
		// set them all based on the new current contact index
		current_id = setContactId(index, "current");
		previous_id = setContactId(index, "prev");
		next_id = setContactId(index, "next");
		// This is confusing!!!
		TableLayout temp = pageCurrent;
		pageCurrent = pagePrevious;
		pagePrevious = pageNext;
		pageNext = temp;
		setAllContactInfo(pagePrevious, previous_id);
		setAllContactInfo(pageNext, next_id);
	}
	
	/*
	 * get the current contacts index (in the List<Row>)
	 */
	private int getCurrentContactIndex(int index) {
		for(Row r : contactList) {
			if(r.aquickim_id == current_id) {
				index = contactList.indexOf(r);
			}
		}
		return index;
	}

	/**
	 * Create the menu items
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(Menu.NONE, ADD_ID, Menu.NONE, "Add Contact")
				.setIcon(R.drawable.add)
				.setAlphabeticShortcut('a');		
		menu.add(Menu.NONE, DELETE_ID, Menu.NONE, "Remove Contact")
				.setIcon(R.drawable.delete)
				.setAlphabeticShortcut('e');		
		menu.add(Menu.NONE, CLOSE_ID, Menu.NONE, "Close")
				.setIcon(R.drawable.eject)
				.setAlphabeticShortcut('c');

		return(super.onCreateOptionsMenu(menu));
	}
	/**
	 * check which menu item was selected
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case ADD_ID:
				try {
					selectingContact = true;
					pickContact();
				} catch (Exception e) {
					e.printStackTrace();
				}
				return(true);
			case DELETE_ID:
				try {
					deleteContact(current_id);
				} catch (Exception e) {
					e.printStackTrace();
				}
				return(true);				
			case CLOSE_ID:
				finish();
				return(true);
		}

		return(super.onOptionsItemSelected(item));
	}
	
	private void deleteContact(final long id) {
		AlertDialog.Builder alert = new AlertDialog.Builder(this);
		alert.setMessage("Are you sure you want to remove this contact?");
		alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {  
			public void onClick(DialogInterface dialog, int whichButton) {  
				db.deleteRow(id);
				// reset the current contact
				current_id = 0;
				resetCurrentContact();
			  }  
			});  
			alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {  
			  public void onClick(DialogInterface dialog, int whichButton) {  
			    // Canceled.  
			  }  
			});  
			alert.show();
	}
	/**
     * Click handler for the Pick Contact button.  Invokes a contact picker activity.
     * The specific intent used to bring up that activity differs between versions
     * of the SDK, which is why we delegate the creation of the intent to ContactAccessor.
     */
    protected void pickContact() {
    	try {
    		startActivityForResult(mContactAccessor.getPickContactIntent(), PICK_CONTACT_REQUEST);
    	} catch (Exception ex) {
    		ex.printStackTrace();
    	}
    }
    /**
     * Invoked when the contact picker activity is finished. The {@code contactUri} parameter
     * will contain a reference to the contact selected by the user. We will treat it as
     * an opaque URI and allow the SDK-specific ContactAccessor to handle the URI accordingly.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	// handle the result of the contact pick here
        if (requestCode == PICK_CONTACT_REQUEST && resultCode == RESULT_OK) {
            loadContactInfo(data.getData());
        }
        // handle the speech result here
        if (requestCode == DO_SPEECHSTUFF_REQUEST && resultCode == RESULT_OK) {
        	
        }
    }
    /**
     * Load contact information on a background thread.
     * @param contactUri
     */
    private void loadContactInfo(Uri contactUri) {
        /*
         * We should always run database queries on a background thread. The database may be
         * locked by some process for a long time.  If we locked up the UI thread while waiting
         * for the query to come back, we might get an "Application Not Responding" dialog.
         */
        AsyncTask<Uri, Void, ContactInfo> task = new AsyncTask<Uri, Void, ContactInfo>() {

            @Override
            protected ContactInfo doInBackground(Uri... uris) {
                return mContactAccessor.loadContact(getContentResolver(), uris[0]);
            }
            @Override
            protected void onPostExecute(ContactInfo result) {
                bindView(result);
            }
        };
        task.execute(contactUri);
    }
    /**
     * Displays contact information: name and phone number.
     * @param contactInfo
     */
    protected void bindView(ContactInfo contactInfo) {
    	if (contactInfo.getPhoneNumber() != null) {
    		// create a row in the database and set the default for all the quickmsgs
    		Row r = aquickim.db.new Row();
    		r.contactName = contactInfo.getDisplayName();
    		r.phoneNumber = contactInfo.getPhoneNumber();
    		r.msg1 = "Long click here to set msg1";
    		r.msg2 = "Long click here to set msg2";
    		r.msg3 = "Long click here to set msg3";
    		r.msg4 = "Long click here to set msg4";
    		r.msg5 = "Long click here to set msg5";
    		r.msg6 = "Long click here to set msg6";
    		try {
    			long id = db.contactExists(r.contactName, r.phoneNumber);
    			if(id == 0) {
    				current_id = db.insertContact(r);
    				resetCurrentContact();
    			} else {
    				// only if we're not currently showing this contact
    				if(id != current_id) {
    					current_id = id;
    					next_id = setContactId((int)id, "next");
    					previous_id = setContactId((int)id, "prev");
    				}
    			}
    		} catch (Exception e) {
    			toastMsg("Error saving new contact", 3000);
    			Log.e("(bindView) Exception saving contact", e.toString());
    		}
    		
    	} else {
    		toastMsg("The contact you selected does not have a mobile number!", 3000);
    	}
    }
	/**
     * Allows the configuration of the quick text behind each button
     * @param view
     */
	public boolean onLongClick(View view) {
		final View v = view;
		AlertDialog.Builder alert = new AlertDialog.Builder(this);
		// Set an EditText view to get user input   
		final EditText input = new EditText(this);		
		setDialogText(view, alert, input);
		alert.setView(input);				
		alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {  
			public void onClick(DialogInterface dialog, int whichButton) {  
				updateButtonAndSetting(v, input.getText().toString());
			  }  
			});  
			alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {  
			  public void onClick(DialogInterface dialog, int whichButton) {  
			    // Canceled.  
			  }  
			});  
			  
			alert.show();
		return true;
	}
	/**
	 * sets the dialog for each button accordingly
	 * @param view
	 * @param alert
	 */
	private void setDialogText(View view, AlertDialog.Builder alert, EditText input) {
		alert.setTitle("Quick Text");
		alert.setMessage("Enter quicktext: ");
		input.setText(((Button)view).getText());
	}
	/**
	 * updates the button text and setting for the quick text
	 * @param view
	 * @param quickText
	 */
	protected void updateButtonAndSetting(View view, String quickText) {
		// Get the parent so we can grab the Contact and PhoneNumber
		View viewParent = (View) view.getParent();
		// Cast it to a Button
		Button b = ((Button)view);
		//Set the text for the button being changed
		b.setText(quickText);
		// Prepare to update the database
		Row r = aquickim.db.new Row();
		//Get the button id
		int id = b.getId();		
		switch(id) {
			case R.id.quickmsg1:
				r.msg1 = quickText;
				break;
			case R.id.quickmsg2:
				r.msg2 = quickText;
				break;
			case R.id.quickmsg3:
				r.msg3 = quickText;
				break;
			case R.id.quickmsg4:
				r.msg4 = quickText;
				break;
			case R.id.quickmsg5:
				r.msg5 = quickText;
				break;
			case R.id.quickmsg6:
				r.msg6 = quickText;
				break;			
		}
		r.aquickim_id = current_id;
		String contactInfo = "";
		// portrait layout
		if(viewParent.findViewById(R.id.tvContact) != null) {
			contactInfo = ((TextView)viewParent.findViewById(R.id.tvContact)).getText().toString();
		} else {
			// landscape layout account for the table layout
			View v = ((View) view.getParent().getParent()).findViewById(R.id.tvContact);
			contactInfo = ((TextView)v).getText().toString();
		}
		if(contactInfo.length() > 0)
		r.contactName = contactInfo.split(":")[0].trim();
		r.phoneNumber = contactInfo.split(":")[1].trim();
		// update the row in the database
		db.updateRow(r);
		// grab the data after its updated
		contactList = db.fetchAllRows();
	}
	/**
	 * Handle the click for each button
	 * @param view
	 */
	public void onClick(View view) {
		String msg = "default msg";
		msg = getButtonMessage(view, msg);
		if(!msg.contentEquals("Speech")) {
			sendSMSMessage(msg, getPhoneNumber(view));
			toastMsg(msg + " sent to: " + getContactName(view), Toast.LENGTH_SHORT);
		} else {
			try {
				// start speech intent
				//Context ctx = view.getContext();
				//Intent i = new Intent(ctx, VoiceRecognition.class);
	        	//startActivityForResult(i, VoiceRecognition.VOICE_RECOGNITION_REQUEST_CODE);
			} catch (ActivityNotFoundException e) {
				Log.e("onclick", e.getMessage());
			}
		}
	}
	/**
	 * Set the clicked buttons message
	 * @param view
	 * @param msg
	 * @return
	 */
	private String getButtonMessage(View view, String defaultMsg) {
		String returnMsg = ((Button)view).getText().toString();
		if(returnMsg.length() == 0) returnMsg = defaultMsg;
		return returnMsg;
	}
	/*
	 * getContactName
	 */
	private String getContactName(View view) {
		String contactName = "Contact";
		try {
			TableLayout v = (TableLayout)pageCurrent;
			if(v != null) 
			{
				if (((TextView)v.findViewById(R.id.tvContact)) != null) 
				{
					contactName = ((TextView)v.findViewById(R.id.tvContact)).getText().toString().split(":")[0].trim();
				}
			}
		} catch (Exception e){
			Log.e("(getContactName) Error parsing", e.getMessage());
		}
		return contactName;
	}
	/*
	 * getPhoneNumber
	 */
	private String getPhoneNumber(View view) {
		String contactName = "Contact";
		try {
			TableLayout v = (TableLayout)pageCurrent;
			if (v != null) 
			{
				if (((TextView)v.findViewById(R.id.tvContact)) != null) 
				{
					contactName = ((TextView)v.findViewById(R.id.tvContact)).getText().toString().split(":")[1].trim();
				}
			}
		} catch (Exception e){
			Log.e("(getContactName) Error parsing", e.getMessage());
		}
		return contactName;
	}
	/**
	 * Sends the SMS message to the configured contacts phone number
	 * @param msg
	 */
	private void sendSMSMessage(String msg, String phoneNumber) {
		try {
			PendingIntent pi = PendingIntent.getActivity(this, 0, new Intent(), 0);
			SmsManager sm = SmsManager.getDefault();
			sm.sendTextMessage(phoneNumber, null, msg, pi, null);
		} catch (Exception e) {
			toastMsg("Exception: "+ e.toString(), 2000);	
		}
	}
	/**
	 * show a toast message to the user
	 * @param msg
	 * @param toastLength
	 */
	private void toastMsg(String msg, int toastLength) {
		Toast
		.makeText(this, msg, toastLength)
		.show();
	}
	/**
	 * Handle the resume by restoring the settings
	 */
	public void onResume() {
		super.onResume();
		selectingContact = false;
		try {
			setAllContactPages();
			wakeUp();
			Bundle b = getIntent().getExtras();
	        if(b != null) {
		        final String originatingAddress = b.getString("OriginatingAddress");
		        if(originatingAddress != "") {
		        	AlertDialog.Builder alert = new AlertDialog.Builder(this);
		    		alert.setMessage("Would you like to reply?");
		    		alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {  
		    			public void onClick(DialogInterface dialog, int whichButton) {  
		    				setReplyContact(originatingAddress);
		    			  }  
		    			});  
		    			alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {  
		    			  public void onClick(DialogInterface dialog, int whichButton) {  
		    			    // Canceled.
		    				finish();
		    			  }  
		    			});  
		    			alert.show();
		        }
	        }
	        ManageWakeLock.releaseFull();
		}
		catch (Throwable t) {
			toastMsg("Exception: "+t.toString(), 2000);
		}
	}
	private void setAllContactPages() {
		if(current_id > 0) setAllContactInfo(pageCurrent, (int)current_id);
		if(previous_id > 0) setAllContactInfo(pagePrevious, (int)previous_id);
		if(next_id > 0) setAllContactInfo(pageNext, (int)next_id);
	}

	private void setAllContactInfo(LinearLayout l, long id) {
		if (id > 0) {
			if (contactList != null) {
				// Set the previous contact as the current one
				for (Row r : contactList) {
					if(r.aquickim_id == id) {
						((TextView)l.findViewById(R.id.tvContact)).setText(r.contactName + " : " + r.phoneNumber);
						setButtonText(l, r.msg1, 0);
						setButtonText(l, r.msg2, 1);
						setButtonText(l, r.msg3, 2);
						setButtonText(l, r.msg4, 3);
						setButtonText(l, r.msg5, 4);
						setButtonText(l, r.msg6, 5);
					}
				}
			}
		}
	}
	
	private void setButtonText(LinearLayout l, String str, int count) {
		switch(count) {
			case 0:
				((Button)l.findViewById(R.id.quickmsg1)).setText(str);
				break;
			case 1:
				((Button)l.findViewById(R.id.quickmsg2)).setText(str);
				break;
			case 2:
				((Button)l.findViewById(R.id.quickmsg3)).setText(str);
				break;
			case 3:
				((Button)l.findViewById(R.id.quickmsg4)).setText(str);
				break;
			case 4:
				((Button)l.findViewById(R.id.quickmsg5)).setText(str);
				break;								
			case 5:
				((Button)l.findViewById(R.id.quickmsg6)).setText(str);
				break;				
		}
	}
	/**
	 * Handle the pause by ??
	 */
	public void onPause() {
		super.onPause();
		// If the user is going somewhere else just end it
		// otherwise we don't want to do this when they are
		// selecting a contact to configure
//		if(!selectingContact) {
//			finish();
//		}
	}
}