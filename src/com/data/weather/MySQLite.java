package com.data.weather;

import java.util.ArrayList;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

public class MySQLite extends SQLiteOpenHelper{
	private static int version=1;
	public MySQLite(Context context, String name, CursorFactory factory,
			int version) {
		super(context, name, factory, version);
	}
	public MySQLite(Context context, String name, CursorFactory factory) {
		super(context, name, factory, version);
	}
	public MySQLite(Context context, String name) {
		super(context, name, null, version);
	}
	@Override
	public void onCreate(SQLiteDatabase db) {
		String sql="create table if not exists city (id integer primary key autoincrement,province varchar(20),name varchar(30),district varchar(30),cityId varchar(20))";
		db.execSQL(sql);
	}
	@Override
	public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {
		
		
	}
	public static CityAttribute getCity(Context context,String name)
	{
		MySQLite sqlite=new MySQLite(context,"city.db");
		SQLiteDatabase db = sqlite.getWritableDatabase();
    	Cursor c = db.rawQuery("select * from city", null);
    	c.moveToFirst();
    	while(!c.isAfterLast())
    	{
    		if(name.equals(c.getString(3))) break;
    		else c.moveToNext();
    	}
    	if(c.isAfterLast()) 
    	{
    		c.moveToFirst();
    		while(!c.isAfterLast())
    		{
    			if(name.equals(c.getString(2))) break;
    			else c.moveToNext();
    		}
    		if(c.isAfterLast())
    		{
    			db.close();
            	sqlite.close();
        		c.close();
        		return null;
    		}
    		else
    		{
    			CityAttribute temp = new CityAttribute(c.getString(1),c.getString(2),c.getString(3),c.getString(4));
        		db.close();
            	sqlite.close();
        		c.close();
        		return temp;
    		}
    	}
    	else 
    	{
    		CityAttribute temp = new CityAttribute(c.getString(1),c.getString(2),c.getString(3),c.getString(4));
    		db.close();
        	sqlite.close();
    		c.close();
    		return temp;
    	}
	}
}


























