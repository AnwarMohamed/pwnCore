package com.anwarelmakrahy.pwncore;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHandler extends SQLiteOpenHelper {
	
	
	public static final String COLUMN_MODULE_ID = "_id";
	public static final String COLUMN_MODULE_PATH = "path";

	public static final String TABLE_EXPLOITS = "exploits";
	public static final String TABLE_PAYLOADS = "payloads";
	public static final String TABLE_POSTS = "post";
	public static final String TABLE_ENCODERS = "encoders";
	public static final String TABLE_NOPS = "nops";
	public static final String TABLE_AUXILIARY = "auxiliary";
	
	public static final String TABLE_NMAP_SCANS = "nmap_scans";
	public static final String COLUMN_NMAP_SCAN_ID = "_id";
	public static final String COLUMN_NMAP_SCAN_RESULT = "result";
	public static final String COLUMN_NMAP_SCAN_ARGV = "argv";
	
	public static final String DATABASE_NAME = "pwncore.db";
	private static final int DATABASE_VERSION = 1;

	// Database creation sql statement
	private static final String[] DATABASE_CREATE = { 
		  "create table " + TABLE_EXPLOITS + "(" 
		  + COLUMN_MODULE_ID + " integer primary key autoincrement, " 
		  + COLUMN_MODULE_PATH + " text not null);",
		  
		  "create table " + TABLE_PAYLOADS + "(" 
		  + COLUMN_MODULE_ID + " integer primary key autoincrement, " 
		  + COLUMN_MODULE_PATH + " text not null);",
		  
		  "create table " + TABLE_POSTS + "(" 
		  + COLUMN_MODULE_ID + " integer primary key autoincrement, " 
		  + COLUMN_MODULE_PATH + " text not null);",
		  
		  "create table " + TABLE_ENCODERS + "(" 
		  + COLUMN_MODULE_ID + " integer primary key autoincrement, " 
		  + COLUMN_MODULE_PATH + " text not null);",
		  
		  "create table " + TABLE_NOPS + "(" 
		  + COLUMN_MODULE_ID + " integer primary key autoincrement, " 
		  + COLUMN_MODULE_PATH + " text not null);",
		  
		  "create table " + TABLE_AUXILIARY + "(" 
		  + COLUMN_MODULE_ID + " integer primary key autoincrement, " 
		  + COLUMN_MODULE_PATH + " text not null);",
		  
		  "create table " + TABLE_NMAP_SCANS + "(" 
		  + COLUMN_NMAP_SCAN_ID + " integer primary key autoincrement, " 
		  + COLUMN_NMAP_SCAN_RESULT + " text not null, " 
		  + COLUMN_NMAP_SCAN_ARGV + " text not null);",
	};

	public static final String TABLES[] = { TABLE_EXPLOITS, TABLE_PAYLOADS, TABLE_POSTS, TABLE_ENCODERS, TABLE_NOPS, TABLE_AUXILIARY};
	
	public DatabaseHandler(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase database) {
		for (int i=0; i<DATABASE_CREATE.length; i++)
			database.execSQL(DATABASE_CREATE[i]);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	  
		for (int i=0; i<TABLES.length; i++)
			db.execSQL("DROP TABLE IF EXISTS " + TABLES[i]);
	  
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_NMAP_SCANS);
		onCreate(db);
	}
  
	public void deleteTable(int id) {
		SQLiteDatabase db = this.getWritableDatabase();
		db.execSQL("DROP TABLE IF EXISTS " + TABLES[id]);
		db.close();
	}
  
	public void createTable(int id) {
		SQLiteDatabase db = this.getWritableDatabase();
		db.execSQL(DATABASE_CREATE[id]);
		db.close();	  
	}
  
	public int getTableIdByName(String name) {
		for (int i=0; i<TABLES.length; i++) {
			if (TABLES[i].toLowerCase().equals(name)) {
				return i;
			}
		}
		return -1;
	}
  
  /*
   * DATABASE FUNCTIONS
   */
  
	public ArrayList<ModuleItem> searchModules(String q) {
		return new ArrayList<ModuleItem>();
	}
	
	public void addNmapScan(String id, String cmd, String res) {
	    SQLiteDatabase db = this.getWritableDatabase();
	    
    	try {
    		db.execSQL("PRAGMA synchronous=OFF");
    		db.beginTransaction();
    		
    	    ContentValues values = new ContentValues();
    	    values.put(COLUMN_NMAP_SCAN_ID, id);
    	    values.put(COLUMN_NMAP_SCAN_ARGV, cmd);
    	    values.put(COLUMN_NMAP_SCAN_RESULT, res);
    	    db.insert(TABLE_NMAP_SCANS, null, values);
        	
        	db.setTransactionSuccessful();
    	}
    	finally {
    		db.endTransaction();
    		db.execSQL("PRAGMA synchronous=NORMAL");
    	}
    	
	    db.close();
	}
	
	public String getNmapScanResult(String id) {
 	    SQLiteDatabase db = this.getReadableDatabase();	 
  	    Cursor cursor = db.query(TABLE_NMAP_SCANS, new String[] { COLUMN_NMAP_SCAN_ID,
  	    		COLUMN_NMAP_SCAN_RESULT }, COLUMN_NMAP_SCAN_ID + "=?",
  	            new String[] { String.valueOf(id) }, null, null, null, null);
  	    if (cursor != null)
  	        cursor.moveToFirst();
  	 
  	    String res = cursor.getString(1);
  	    if(cursor != null && !cursor.isClosed())
  	    	cursor.close();

  	    return res;
	}
	
	private int getIdByString(String type) {
		for (int i=0; i<TABLES.length; i++)
			if (TABLES[i].equals(type))
				return i;
		return 0;
	}
	
  	public void addModule(ModuleItem item, String type) {
	    SQLiteDatabase db = this.getWritableDatabase();
	    
    	try {
    		db.execSQL("PRAGMA synchronous=OFF");
    		db.beginTransaction();
    		
    	    ContentValues values = new ContentValues();
    	    values.put(COLUMN_MODULE_PATH, item.getPath());
    	    db.insert(TABLES[getIdByString(type)], null, values);
        	
        	db.setTransactionSuccessful();
    	}
    	finally {
    		db.endTransaction();
    		db.execSQL("PRAGMA synchronous=NORMAL");
    	}
    	
	    db.close();
  	}

  	public ModuleItem getModule(int id, String type) {
  	    SQLiteDatabase db = this.getReadableDatabase();	 
  	    Cursor cursor = db.query(TABLES[getIdByString(type)], new String[] { COLUMN_MODULE_ID,
  	    		COLUMN_MODULE_PATH }, COLUMN_MODULE_ID + "=?",
  	            new String[] { String.valueOf(id) }, null, null, null, null);
  	    if (cursor != null)
  	        cursor.moveToFirst();
  	 
  	  ModuleItem item = new ModuleItem();
  	    item.setID(Integer.parseInt(cursor.getString(0)));
  	    item.setPath(cursor.getString(1));
  	    item.setType(type);
  	    

  	    if(cursor != null && !cursor.isClosed())
  	    	cursor.close();

  	    return item;
  	}
  	
  	public List<ModuleItem> getAllModules(String type) {
  	    List<ModuleItem> list = new ArrayList<ModuleItem>();

  	    String selectQuery = "SELECT  * FROM " + TABLES[getIdByString(type)];
  	 
  	    SQLiteDatabase db = this.getWritableDatabase();
  	    Cursor cursor = db.rawQuery(selectQuery, null);
  	 
  	    if (cursor.moveToFirst()) {
  	        do {
  	        	ModuleItem item = new ModuleItem();
  	            item.setID(Integer.parseInt(cursor.getString(0)));
  	            item.setPath(cursor.getString(1));
  	            item.setType(type);

  	            list.add(item);
  	        } while (cursor.moveToNext());
  	    }
  	 
  	    if(cursor != null && !cursor.isClosed())
  	    	cursor.close();
  	    
  	    return list;
  	}
  	
    public int getModulesCount(String type) {
        String countQuery = "SELECT COUNT("+ COLUMN_MODULE_ID +") FROM " + TABLES[getIdByString(type)];
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        
        cursor.moveToFirst();
        int count = cursor.getInt(0);
        cursor.close();
 
        return count;
    }
    
    public int updateModule(ModuleItem item, String type) {
        SQLiteDatabase db = this.getWritableDatabase();
     
        ContentValues values = new ContentValues();
        values.put(COLUMN_MODULE_PATH, item.getPath());
     
        return db.update(TABLES[getIdByString(type)], values, COLUMN_MODULE_ID + " = ?",
                new String[] { String.valueOf(item.getID()) });
    }
    
    public void deleteModule(ModuleItem item, String type) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLES[getIdByString(type)], COLUMN_MODULE_ID + " = ?",
                new String[] { String.valueOf(item.getID()) });
        db.close();
    }    
} 