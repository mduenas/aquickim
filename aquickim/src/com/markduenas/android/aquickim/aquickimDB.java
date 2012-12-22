package com.markduenas.android.aquickim;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class aquickimDB extends SQLiteOpenHelper {
	public class Row extends Object {
	    public long aquickim_id;
	    public String contactName;
	    public String phoneNumber;
	    public String msg1;
	    public String msg2;
	    public String msg3;
	    public String msg4;
	    public String msg5;
	    public String msg6;
	}
	
    private final static String DB_NAME = "apasspigsDB"; 
    private final static String DB_TABLE_QUICKCONTACTS = "aquickimcontacts";
    private final static int    DB_VERSION = 1;
    
    private static final String CREATE_TABLE_QUICKCONTACTS = "CREATE TABLE IF NOT EXISTS " + DB_TABLE_QUICKCONTACTS
    	+ "(aquickim_id integer primary key autoincrement, "
        + "contactName text not null, phoneNumber text not null, "
        + "msg1 text not null, msg2 text not null, msg3 text not null, "
        + "msg4 text not null, msg5 text not null, msg6 text not null);";
    
	public aquickimDB(Context ctx) {
		super(ctx, DB_NAME, null, DB_VERSION);
	}
	
	@Override
	public void onCreate(SQLiteDatabase db) {
		try {
        	db.execSQL(CREATE_TABLE_QUICKCONTACTS);
		} catch (SQLException e1) {
			Log.e("(onCreate) Exception on query", e1.toString());
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub

	}
	
	public long insertContact(Row r) {
        ContentValues values = new ContentValues();
        values.put("contactName", r.contactName);
        values.put("phoneNumber", r.phoneNumber);
        values.put("msg1", r.msg1 == null ? "" : r.msg1);
        values.put("msg2", r.msg2 == null ? "" : r.msg2);
        values.put("msg3", r.msg3 == null ? "" : r.msg3);
        values.put("msg4", r.msg4 == null ? "" : r.msg4);
        values.put("msg5", r.msg5 == null ? "" : r.msg5);
        values.put("msg6", r.msg6 == null ? "" : r.msg6);
        SQLiteDatabase db = null;
        long id = 0;
        try {
        	db = this.getWritableDatabase();
        	id = db.insertOrThrow(DB_TABLE_QUICKCONTACTS, null, values);
        } catch (SQLException e) {
        	Log.e("(insertContact) Exception on query", e.toString());
        } finally {
        	db.close();
        }
        return id;
	}
	
	public boolean updateRow(Row r) {
        ContentValues args = new ContentValues();
        args.put("contactName", r.contactName);
        args.put("phoneNumber", r.phoneNumber);
        if(r.msg1 != null)
        	args.put("msg1", r.msg1 == null ? "" : r.msg1);
        if(r.msg2 != null)
        	args.put("msg2", r.msg2 == null ? "" : r.msg2);
        if(r.msg3 != null)
        	args.put("msg3", r.msg3 == null ? "" : r.msg3);
        if(r.msg4 != null)
        	args.put("msg4", r.msg4 == null ? "" : r.msg4);
        if(r.msg5 != null)
        	args.put("msg5", r.msg5 == null ? "" : r.msg5);
        if(r.msg6 != null)
        	args.put("msg6", r.msg6 == null ? "" : r.msg6);
        SQLiteDatabase db = null;
        int rowsAffected = 0;
        try {
        	db = this.getWritableDatabase();
        	rowsAffected = db.update(DB_TABLE_QUICKCONTACTS, args, "aquickim_id=" + r.aquickim_id, null);
        } catch (Exception e) {
        	Log.e("(updateRow) Error updating quickcontacts", e.toString());
        } finally {
        	db.close();
        }
        if(rowsAffected > 0) 
        	return true;
        else
        	return false;
    }
	
	public int deleteRow(long rowId) {
		SQLiteDatabase db = null;
		int rowsAffected = 0;
        try {
        	db = this.getWritableDatabase();
        	rowsAffected = db.delete(DB_TABLE_QUICKCONTACTS, "aquickim_id=" + rowId, null);
        } catch (Exception e) {
        	Log.e("(deleteRow) Error deleting a contact", e.toString());
        } finally {
        	db.close();
        }
        return rowsAffected;
    }

    public List<Row> fetchAllRows() {
    	SQLiteDatabase db = null;
        ArrayList<Row> ret = new ArrayList<Row>();
        try {
        	db = this.getReadableDatabase();
            Cursor c = db.query(DB_TABLE_QUICKCONTACTS, new String[] {
                    "aquickim_id", "contactName", "phoneNumber", "msg1", "msg2", "msg3", "msg4", "msg5", "msg6"}, null, null, null, null, null);
            int numRows = c.getCount();
            c.moveToFirst();
            for (int i = 0; i < numRows; ++i) {
                Row row = new Row();
                row.aquickim_id = c.getLong(0);
                row.contactName = c.getString(1);
                row.phoneNumber = c.getString(2);
                row.msg1 = c.getString(3);
                row.msg2 = c.getString(4);
                row.msg3 = c.getString(5);
                row.msg4 = c.getString(6);
                row.msg5 = c.getString(7);
                row.msg6 = c.getString(8);
                ret.add(row);
                c.moveToNext();
            }
            c.close();
        } catch (SQLException e) {
            Log.e("(fetchAllRows) Exception on query", e.toString());
        } finally {
        	db.close();
        }
        return ret;
    }
    
    public Cursor GetAllRows() {
    	SQLiteDatabase db = this.getReadableDatabase();
        try {
            return db.query(DB_TABLE_QUICKCONTACTS, new String[] {
            		"aquickim_id", "contactName", "phoneNumber", "msg1", "msg2", "msg3", "msg4", "msg5", "msg6"}, null, null, null, null, null);
        } catch (SQLException e) {
            Log.e("(GetAllRows) Exception on query", e.toString());
            return null;
        } finally {
        	db.close();
        }
    }

	public long contactExists(String contactName, String phoneNumber) {
		long retVal = 0;
		Cursor c = null;
		SQLiteDatabase db = this.getReadableDatabase();
		try {
			 c = db.query(DB_TABLE_QUICKCONTACTS, new String[] {
	                 "aquickim_id", "contactName", "phoneNumber"}, "contactName='" + contactName + "' and phoneNumber='" + phoneNumber + "'", null, null, null, null);
	        int numRows = c.getCount();
	        if(numRows > 0)
	        {
	        	c.moveToFirst();
	        	retVal = c.getLong(0);
	        }
		} finally {
			c.close();
			db.close();
		}
    	return retVal;
	}
}
