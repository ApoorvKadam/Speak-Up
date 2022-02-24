package com.example.myspeaker;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class SpeakerDB extends SQLiteOpenHelper{
	static final String dbName="speakerDB";
	Context context;
	public SpeakerDB(Context context) {
		super(context, dbName, null, 1);
		this.context=context;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("create table records (recordid INTEGER PRIMARYKEY, recordtxt TEXT,filename TEXT)");
	}

	@Override
	public synchronized void close() {
		// TODO Auto-generated method stub
		super.close();
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int arg1, int arg2) {
		db.execSQL("drop table if exists records");
		onCreate(db);
	}

	public void save(VoiceRecording v) {
		Log.d("recording", v.toString());
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues c=new ContentValues();
		c.put("recordtxt", v.getPlayText());
		c.put("filename", v.getFilename());
		long id=db.insert("records", "recordid", c);
		Log.d("insert id", id+"");
	}

	public String getFileName(String play_text) {
		String filename = null;
		SQLiteDatabase db = this.getReadableDatabase();
		//Cursor c=db.query("records", new String[]{"recordtxt","filename"}, "recordtxt="+play_text, null, null, null, null);
		Cursor c=db.rawQuery("select filename from records where recordtxt='"+play_text+"'", new String[] {});
		while(c.moveToNext()){
			filename=c.getString(c.getColumnIndex("filename"));
		}
		Log.d("db filename", filename+".");
		c.close();
		return filename;
	}

	public boolean clearAll() {
		boolean flag=true;
		try{
		SQLiteDatabase db = this.getReadableDatabase();
		db.execSQL("drop table if exists records");
		onCreate(db);
		}catch(SQLException e){
			flag=false;
		}
		return flag;
	}

	public List<VoiceRecording> getAllRecords() {
		List<VoiceRecording> allrecords=new ArrayList<VoiceRecording>();
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor c=db.rawQuery("select * from records",new String[] {} );
		while(c.moveToNext()){
			VoiceRecording v=new VoiceRecording(c.getString(c.getColumnIndex("recordtxt")), c.getString(c.getColumnIndex("filename")));
			allrecords.add(v);
		}
		c.close();
		db.close();
		return allrecords;
	}

	public void delete(VoiceRecording voiceRecording) {
		String qery="delete from records where filename='"+voiceRecording.getFilename()+"'";
		SQLiteDatabase db =this.getWritableDatabase();
		db.execSQL(qery);
		db.close();
	}

}
