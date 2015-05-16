package com.rstudio.notii_pro;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

public class DatabaseMng extends SQLiteOpenHelper {
	String KEY_ID = "ID";
	String TABLE_NOTES = "TableNotes", LIST_CHECK = "List";
	static String DATABASE_NAME = "NotesDatabases.db";
	static int DATABASE_VERSION = 1;
	String KEY_TITLE = "Title", KEY_TEXT = "Text", KEY_DATE = "Date", KEY_COLOR = "Color", KEY_BOLD = "isBold", KEY_REMIND = "Remind";
	
	public DatabaseMng (Context context){
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		String CREATE_NOTES_TABLE = "CREATE TABLE " + TABLE_NOTES + "(" + KEY_ID + " INTEGER,"
				+ KEY_TITLE + " TEXT," + KEY_TEXT + " TEXT," + KEY_DATE + " TEXT,"
				+ KEY_COLOR + " INTEGER," + KEY_BOLD + " BOOLEAN, " + KEY_REMIND + " TEXT " +")";
		Log.d(TABLE_NOTES, CREATE_NOTES_TABLE);
		db.execSQL(CREATE_NOTES_TABLE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVerion) {
		// TODO Auto-generated method stub
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_NOTES);
		onCreate(db);
	}
	
	public void addNote(Note_item note){
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(KEY_ID, note.getId());
        values.put(KEY_TITLE, note.getTitle());
        values.put(KEY_TEXT, note.getText());
        values.put(KEY_DATE, note.getDate());
        values.put(KEY_COLOR, note.getColor());
        values.put(KEY_BOLD, note.getBold());
        values.put(KEY_REMIND, note.getRemind());
        db.insertOrThrow(TABLE_NOTES, null, values);
        db.close();
	}
	
	public Note_item getNote(int id){
		SQLiteDatabase db = this.getReadableDatabase();
		String selectQuery = "SELECT * FROM " + TABLE_NOTES + " WHERE " + KEY_ID + " = " + id;
		Cursor cursor = db.rawQuery(selectQuery, null);
		if (cursor != null){
			cursor.moveToFirst();
		}
		Note_item note = new Note_item();
		note.setId(cursor.getInt(cursor.getColumnIndex(KEY_ID)));
		note.setTitle(cursor.getString(cursor.getColumnIndex(KEY_TITLE)));
		note.setText(cursor.getString(cursor.getColumnIndex(KEY_TEXT)));
		note.setDate(cursor.getString(cursor.getColumnIndex(KEY_DATE)));
		note.setColor(cursor.getInt(cursor.getColumnIndex(KEY_COLOR)));
		note.setBold(getBoolean(cursor.getInt(cursor.getColumnIndex(KEY_BOLD))));
		note.setRemind(cursor.getLong(cursor.getColumnIndex(KEY_REMIND)));
		return note;
	}
	
	public Boolean isNote(int id){
		SQLiteDatabase db = this.getReadableDatabase();
		String selectQuery = "SELECT * FROM " + TABLE_NOTES + " WHERE " + KEY_ID + " = " + id;
		Cursor cursor = db.rawQuery(selectQuery, null);
		if (cursor != null){
			return true;
		}
		else return false;
	}
	
	public ArrayList<Note_item> getAllNote(){
		ArrayList<Note_item> listNote = new ArrayList<Note_item>();
		String selectQuery = "SELECT * FROM " + TABLE_NOTES;
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(selectQuery, null);
		if (cursor.moveToFirst()){
			do {
				Note_item note = new Note_item(Integer.parseInt(cursor.getString(0)), cursor.getString(1), cursor.getString(2), cursor.getString(3), Integer.parseInt(cursor.getString(4)), Boolean.parseBoolean(cursor.getString(5)));
				note.setRemind(cursor.getLong(cursor.getColumnIndex(KEY_REMIND)));
				listNote.add(0, note);
			} while (cursor.moveToNext());
		}
		return listNote;
	}
	
	public int getNoteCount(){
		String countQuery = "SELECT * FROM " + TABLE_NOTES;
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(countQuery, null);
		return cursor.getCount();
	}
	
	public int updateNote(Note_item note){
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(KEY_TITLE, note.getTitle());
		values.put(KEY_TEXT, note.getText());
		values.put(KEY_DATE, note.getDate());
		values.put(KEY_COLOR, note.getColor());
		values.put(KEY_BOLD, note.getBold());
		values.put(KEY_ID, note.getId());
		values.put(KEY_REMIND, note.getRemind());
		return db.update(TABLE_NOTES, values, KEY_ID + "=?", new String[]{String.valueOf(note.getId())});
	}
	
	public void delNote(Note_item note){
		SQLiteDatabase db = this.getWritableDatabase();
		db.delete(TABLE_NOTES, KEY_ID + "=?", new String[]{String.valueOf(note.getId())});
		db.close();
	}
	
	public void delAllNote(){
		SQLiteDatabase db = this.getWritableDatabase();
		db.delete(TABLE_NOTES, null, null);
		db.close();
	}
	
	public boolean getBoolean(int x){
		if (x==0){
			return false;
		}
		return true;
	}
}
