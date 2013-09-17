package com.anwarelmakrahy.pwncore;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.msgpack.MessagePack;
import org.msgpack.packer.Packer;
import org.msgpack.type.Value;

import com.anwarelmakrahy.pwncore.structures.ModuleItem;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHandler extends SQLiteOpenHelper {
	
	public static final String COLUMN_MODULE_ID = "_id";
	public static final String COLUMN_MODULE_PATH = "path";
	public static final String COLUMN_MODULE_OPTIONS = "options";
	public static final String COLUMN_MODULE_INFO = "info";

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
		  + COLUMN_MODULE_PATH + " text not null, "
		  + COLUMN_MODULE_OPTIONS + " blob, "
		  + COLUMN_MODULE_INFO + " blob);",
		  
		  "create table " + TABLE_PAYLOADS + "(" 
		  + COLUMN_MODULE_ID + " integer primary key autoincrement, " 
		  + COLUMN_MODULE_PATH + " text not null, "
		  + COLUMN_MODULE_OPTIONS + " blob, "
		  + COLUMN_MODULE_INFO + " blob);",
		  
		  "create table " + TABLE_POSTS + "(" 
		  + COLUMN_MODULE_ID + " integer primary key autoincrement, " 
		  + COLUMN_MODULE_PATH + " text not null, "
		  + COLUMN_MODULE_OPTIONS + " blob, "
		  + COLUMN_MODULE_INFO + " blob);",
		  
		  "create table " + TABLE_ENCODERS + "(" 
		  + COLUMN_MODULE_ID + " integer primary key autoincrement, " 
		  + COLUMN_MODULE_PATH + " text not null, "
		  + COLUMN_MODULE_OPTIONS + " blob, "
		  + COLUMN_MODULE_INFO + " blob);",
		  
		  "create table " + TABLE_NOPS + "(" 
		  + COLUMN_MODULE_ID + " integer primary key autoincrement, " 
		  + COLUMN_MODULE_PATH + " text not null, "
		  + COLUMN_MODULE_OPTIONS + " blob, "
		  + COLUMN_MODULE_INFO + " blob);",
		  
		  "create table " + TABLE_AUXILIARY + "(" 
		  + COLUMN_MODULE_ID + " integer primary key autoincrement, " 
		  + COLUMN_MODULE_PATH + " text not null, "
		  + COLUMN_MODULE_OPTIONS + " blob, "
		  + COLUMN_MODULE_INFO + " blob);",
		  
	};

	public static final String TABLES[] = { TABLE_EXPLOITS, TABLE_PAYLOADS, TABLE_POSTS, TABLE_ENCODERS, TABLE_NOPS, TABLE_AUXILIARY};
	
	private static DatabaseHandler mInstance = null;
	public static DatabaseHandler getInstance(Context ctx) {
	      
	    // Use the application context, which will ensure that you 
	    // don't accidentally leak an Activity's context.
	    // See this article for more information: http://bit.ly/6LRzfx
	    if (mInstance == null) {
	      mInstance = new DatabaseHandler(ctx.getApplicationContext());
	    }
	    return mInstance;
	  }
	
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
  
	public synchronized void deleteTable(int id) {
		SQLiteDatabase db = this.getWritableDatabase();
		db.execSQL("DROP TABLE IF EXISTS " + TABLES[id]);
		db.close();
	}
  
	public synchronized void createTable(int id) {
		SQLiteDatabase db = this.getWritableDatabase();
		db.execSQL(DATABASE_CREATE[id]);
		db.close();	  
	}
  
	public synchronized void recreateTable(int id) {
		SQLiteDatabase db = this.getWritableDatabase();
		db.execSQL("DROP TABLE IF EXISTS " + TABLES[id]);
		db.execSQL(DATABASE_CREATE[id]);
		db.close();	  
	}
	
	public synchronized int getTableIdByName(String name) {
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
  
	public synchronized ArrayList<ModuleItem> searchModules(String q) {
		return new ArrayList<ModuleItem>();
	}
		
	private int getIdByString(String type) {
		for (int i=0; i<TABLES.length; i++)
			if (TABLES[i].equals(type))
				return i;
		return 0;
	}
	
  	public synchronized void addModule(ModuleItem item, String type) {
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

  	public synchronized void addModules(String[] paths, String type) {
	    SQLiteDatabase db = this.getWritableDatabase();
    	try {
    		db.execSQL("PRAGMA synchronous=OFF");
    		db.beginTransaction();    		
    		for (int i=0; i<paths.length; i++) {
	    	    ContentValues values = new ContentValues();
	    	    values.put(COLUMN_MODULE_PATH, paths[i]);
	    	    db.insert(TABLES[getIdByString(type)], null, values);
    		}       	
        	db.setTransactionSuccessful();
    	}
    	finally {
    		db.endTransaction();
    		db.execSQL("PRAGMA synchronous=NORMAL");
    	}   	
	    db.close();
  	}
  	

	public synchronized void addModule(String path, Map<String, Value> info, Map<String, Value> options,
			String type) {
	    SQLiteDatabase db = this.getWritableDatabase();
    	try {
    		db.execSQL("PRAGMA synchronous=OFF");
    		db.beginTransaction();    		

    	    ContentValues values = new ContentValues();
    	    values.put(COLUMN_MODULE_PATH, path);
    	    
    	    if (options != null) {
				MessagePack msgpack = new MessagePack();
		        ByteArrayOutputStream out = new ByteArrayOutputStream();
		        Packer packer = msgpack.createPacker(out);
		        packer.write(options);
		        values.put(COLUMN_MODULE_OPTIONS, out.toByteArray());
    	    }
    	    
    	    if (info != null) {
				MessagePack msgpack = new MessagePack();
		        ByteArrayOutputStream out = new ByteArrayOutputStream();
		        Packer packer = msgpack.createPacker(out);
		        packer.write(info); 
        	    values.put(COLUMN_MODULE_INFO, out.toByteArray());
    	    }
     	
    	    db.insert(TABLES[getIdByString(type)], null, values);
        	db.setTransactionSuccessful();
    	} 
    	catch (IOException e) { }
    	finally {
    		db.endTransaction();
    		db.execSQL("PRAGMA synchronous=NORMAL");
    	}   	
	    db.close();
	}
  	
  	public synchronized ModuleItem getModule(int id, String type) {
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
  	    db.close();
  	    return item;
  	}
  	
	public synchronized List<ModuleItem> getAllModules(String type) {
  		List<ModuleItem> list = new ArrayList<ModuleItem>();
  	    String selectQuery = "SELECT * FROM " + TABLES[getIdByString(type)];	 
  	    SQLiteDatabase db = this.getReadableDatabase();
  	    Cursor cursor = db.rawQuery(selectQuery, null);	 
  	    if (cursor.moveToFirst()) {
  	        do {
  	        	ModuleItem item = new ModuleItem();
  	            item.setID(cursor.getInt(0));
  	            item.setPath(cursor.getString(1));
  	            item.setType(type);

  	            list.add(item);
  	        } while (cursor.moveToNext());
  	    }
  	    if(cursor != null && !cursor.isClosed())
  	    	cursor.close();    
  	    db.close();
  	    return list;
  	}
  	
	public synchronized List<String> getAllModulesString(String type) {
  		List<String> list = new ArrayList<String>();
  	    String selectQuery = "SELECT " + COLUMN_MODULE_PATH + " FROM " + TABLES[getIdByString(type)];	 
  	    SQLiteDatabase db = this.getReadableDatabase();
  	    Cursor cursor = db.rawQuery(selectQuery, null);	 
  	    if (cursor.moveToFirst()) {
  	        do {
  	            list.add(cursor.getString(0));
  	        } while (cursor.moveToNext());
  	    }
  	    if(cursor != null && !cursor.isClosed())
  	    	cursor.close();    
  	    db.close();
  	    return list;
	}
	
    public synchronized int getModulesCount(String type) {
        String countQuery = "SELECT COUNT("+ COLUMN_MODULE_ID +") FROM " + TABLES[getIdByString(type)];
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        cursor.moveToFirst();
        int count = cursor.getInt(0);
        cursor.close();
        db.close();
        return count;
    }
    
    public synchronized int updateModule(ModuleItem item, String type) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_MODULE_PATH, item.getPath());
        int ret = db.update(TABLES[getIdByString(type)], values, COLUMN_MODULE_ID + " = ?",
                new String[] { String.valueOf(item.getID()) });      
        db.close();
        return ret;
    }
    
    public synchronized void deleteModule(ModuleItem item, String type) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLES[getIdByString(type)], COLUMN_MODULE_ID + " = ?",
                new String[] { String.valueOf(item.getID()) });
        db.close();
    } 
    
    public synchronized void updateModuleOptions(
    		ModuleItem item, String type, byte[] options) {
        SQLiteDatabase db = this.getWritableDatabase();
        
        ContentValues params = new ContentValues();
        params.put(COLUMN_MODULE_PATH, item.getPath());
        params.put(COLUMN_MODULE_OPTIONS, options);
        
        db.update(
        		TABLES[getIdByString(type)], 
        		params, 
        		COLUMN_MODULE_ID + " = " + item.getID(), 
        		null);
        db.close();
    }

} 