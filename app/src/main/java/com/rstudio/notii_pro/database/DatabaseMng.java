package com.rstudio.notii_pro.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.rstudio.notii_pro.item.CheckItem;
import com.rstudio.notii_pro.item.NoteItem;

import java.util.ArrayList;

public class DatabaseMng extends SQLiteOpenHelper {

	private Context mContext;

	private String KEY_ID = "ID";
	private String TABLE_NOTES = "TableNotes", LIST_CHECK_TABLE = "List";
	static String DATABASE_NAME = "NotesDatabases.db";
	static int DATABASE_VERSION = 1;

	private String KEY_TITLE = "Title", KEY_TEXT = "Text", KEY_DATE = "Date"
			, KEY_COLOR = "Color", KEY_BOLD = "isBold", KEY_REMIND = "Remind";
	private String KEY_ORDER = "NoteOrder", KEY_NOTE = "KeyNote", KEY_CHECK = "isChecked";

	public DatabaseMng (Context context){
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		mContext = context;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		String CREATE_NOTES_TABLE = "CREATE TABLE " + TABLE_NOTES + "(" + KEY_ID + " INTEGER PRIMARY KEY UNIQUE,"
				+ KEY_TITLE + " TEXT," + KEY_TEXT + " TEXT," + KEY_DATE + " TEXT,"
				+ KEY_COLOR + " INTEGER," + KEY_BOLD + " BOOLEAN, " + KEY_REMIND + " TEXT " +")";

		String CREATE_CHECKLIST_TABLE = "CREATE TABLE " + LIST_CHECK_TABLE + "(" + KEY_ID + " INTEGER PRIMARY KEY UNIQUE,"
				+ KEY_NOTE + " INTEGER," + KEY_ORDER + " INTEGER," + KEY_CHECK + " BOOLEAN,"
				+ KEY_TEXT + " TEXT" +", FOREIGN KEY (" + KEY_NOTE + ") REFERENCES " + TABLE_NOTES
				+ "(" + KEY_ID + "))";

		db.execSQL(CREATE_NOTES_TABLE);
		db.execSQL(CREATE_CHECKLIST_TABLE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVerion) {
		// TODO Auto-generated method stub
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_NOTES);
		onCreate(db);
	}

	/**
	 * this support previous version with old database struct
	 */
	public void checkAndCreateTable() {
		String check = "SELECT name FROM sqlite_master WHERE type='table' AND name='" + LIST_CHECK_TABLE + "'";
		SQLiteDatabase db = getWritableDatabase();
		Cursor cur =  db.rawQuery(check, null);
		if (cur.getCount() == 0) {

			String rename = "ALTER TABLE " + TABLE_NOTES + " RENAME TO " + TABLE_NOTES + "_TEMP";

			db.execSQL(rename);

			String CREATE_NOTES_TABLE = "CREATE TABLE " + TABLE_NOTES + "(" + KEY_ID + " INTEGER PRIMARY KEY UNIQUE,"
					+ KEY_TITLE + " TEXT," + KEY_TEXT + " TEXT," + KEY_DATE + " TEXT,"
					+ KEY_COLOR + " INTEGER," + KEY_BOLD + " BOOLEAN, " + KEY_REMIND + " TEXT " +")";

			db.execSQL(CREATE_NOTES_TABLE);

			String insert = "INSERT INTO " + TABLE_NOTES + "("  + KEY_ID + ", "
					+ KEY_TITLE + ", " + KEY_TEXT + ", " + KEY_DATE + ", "
					+ KEY_COLOR + ", " + KEY_BOLD + ", " + KEY_REMIND + ") SELECT "
					+ KEY_ID + ", " + KEY_TITLE + ", " + KEY_TEXT + ", " + KEY_DATE + ", "
					+ KEY_COLOR + ", " + KEY_BOLD + ", " + KEY_REMIND + " FROM " + TABLE_NOTES + "_TEMP";

			db.execSQL(insert);

			String drop = "DROP TABLE " + TABLE_NOTES + "_TEMP";

			db.execSQL(drop);

			String CREATE_CHECKLIST_TABLE = "CREATE TABLE " + LIST_CHECK_TABLE + "(" + KEY_ID + " INTEGER PRIMARY KEY UNIQUE,"
					+ KEY_NOTE + " INTEGER," + KEY_ORDER + " INTEGER," + KEY_CHECK + " BOOLEAN,"
					+ KEY_TEXT + " TEXT" +", FOREIGN KEY (" + KEY_NOTE + ") REFERENCES " + TABLE_NOTES
					+ "(" + KEY_ID + "))";

			db.execSQL(CREATE_CHECKLIST_TABLE);

			ArrayList<NoteItem> data = new ArrayList<>();
			data.addAll(getAllNote());
			removeAllNote();
			for (int i = 0; i < data.size(); i++) {
				addNote(data.get(i));
			}
		}
		cur.close();
		db.close();
	}

	public void addNote(NoteItem note){
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues values = new ContentValues();
        values.put(KEY_TITLE, note.getTitle());
        values.put(KEY_TEXT, note.getText());
        values.put(KEY_DATE, note.getDate());
        values.put(KEY_COLOR, note.getColor());
        values.put(KEY_BOLD, note.getBold());
        values.put(KEY_REMIND, note.getRemind());
        db.insertOrThrow(TABLE_NOTES, null, values);
		db.close();
	}
	
	public NoteItem getNote(int id){
		NoteItem note = null;
		SQLiteDatabase db = this.getReadableDatabase();
		String selectQuery = "SELECT * FROM " + TABLE_NOTES + " WHERE " + KEY_ID + " = " + id;
		Cursor cursor = db.rawQuery(selectQuery, null);
		if (cursor != null){
			if (cursor.getCount() > 0) {
				cursor.moveToFirst();

				note = new NoteItem();
				note.setId(cursor.getInt(cursor.getColumnIndex(KEY_ID)));
				note.setTitle(cursor.getString(cursor.getColumnIndex(KEY_TITLE)));
				note.setText(cursor.getString(cursor.getColumnIndex(KEY_TEXT)));
				note.setDate(cursor.getString(cursor.getColumnIndex(KEY_DATE)));
				note.setColor(cursor.getInt(cursor.getColumnIndex(KEY_COLOR)));
				note.setBold(getBoolean(cursor.getInt(cursor.getColumnIndex(KEY_BOLD))));
				note.setRemind(cursor.getLong(cursor.getColumnIndex(KEY_REMIND)));

				return note;
			}
		}
		cursor.close();
		db.close();
		return null;
	}

	public ArrayList<NoteItem> getAllNote() {
		ArrayList<NoteItem> itemList = null;
		String query = "SELECT * FROM " + TABLE_NOTES;
		SQLiteDatabase db = getReadableDatabase();
		Cursor cursor = db.rawQuery(query, null);

		if (cursor != null) {
			if (cursor.getCount() > 0) {
				cursor.moveToLast();
				itemList = new ArrayList<>();
				do {
					NoteItem note = new NoteItem();
					note.setId(cursor.getInt(cursor.getColumnIndex(KEY_ID)));
					note.setTitle(cursor.getString(cursor.getColumnIndex(KEY_TITLE)));
					note.setText(cursor.getString(cursor.getColumnIndex(KEY_TEXT)));
					note.setDate(cursor.getString(cursor.getColumnIndex(KEY_DATE)));
					note.setColor(cursor.getInt(cursor.getColumnIndex(KEY_COLOR)));
					note.setBold(getBoolean(cursor.getInt(cursor.getColumnIndex(KEY_BOLD))));
					note.setRemind(cursor.getLong(cursor.getColumnIndex(KEY_REMIND)));
					itemList.add(note);
					if (cursor.isFirst()) {
						break;
					}
					else {
						cursor.moveToPrevious();
					}
				} while (true);
			}
		}

		cursor.close();
		db.close();
		return itemList;
	}

	public int getNoteCount(){
		String countQuery = "SELECT * FROM " + TABLE_NOTES;
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(countQuery, null);
		int count = cursor.getCount();
		cursor.close();
		db.close();
		return count;
	}
	
	public void updateNote(NoteItem note){
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(KEY_TITLE, note.getTitle());
		values.put(KEY_TEXT, note.getText());
		values.put(KEY_DATE, note.getDate());
		values.put(KEY_COLOR, note.getColor());
		values.put(KEY_BOLD, note.getBold());
		values.put(KEY_REMIND, note.getRemind());
		db.update(TABLE_NOTES, values, KEY_ID + "=" + note.getId(), null);
		db.close();
	}

	public void removeNote(int id) {
		String where = "" + KEY_ID + "=" + id;
		SQLiteDatabase db = getReadableDatabase();
		db.delete(TABLE_NOTES, where, null);
		db.close();
	}

	public void addCheckItem(CheckItem item) {
		ContentValues content = new ContentValues();
		content.put(KEY_NOTE, item.getKeyNote());
		content.put(KEY_ORDER, item.getKeyOrder());
		content.put(KEY_CHECK, item.isCheck());
		content.put(KEY_TEXT, item.getText());
		SQLiteDatabase db = getWritableDatabase();
		db.insertOrThrow(LIST_CHECK_TABLE, null, content);
		db.close();
	}

	public void updateCheckItem(CheckItem item) {
		ContentValues content = new ContentValues();
		content.put(KEY_NOTE, item.getKeyNote());
		content.put(KEY_ORDER, item.getKeyOrder());
		content.put(KEY_CHECK, item.isCheck());
		content.put(KEY_TEXT, item.getText());
		SQLiteDatabase db = getWritableDatabase();
		db.update(LIST_CHECK_TABLE, content, "" + KEY_ID + "=" + item.getID(), null);
		db.close();
	}

	public void removeCheckedItem(int id) {
		SQLiteDatabase db = getWritableDatabase();
		db.delete(LIST_CHECK_TABLE, "" + KEY_ID + "=" + id, null);
		db.close();
	}

	public CheckItem getCheckItem(int id) {
		CheckItem item = null;
		SQLiteDatabase db = getReadableDatabase();
		String query = "SELECT * FROM " + LIST_CHECK_TABLE + " WHERE " + KEY_ID + "=" + id;

		Cursor cur = db.rawQuery(query, null);

		if (cur != null) {
			cur.moveToFirst();
			if (cur.getCount() > 0) {
				item = new CheckItem();
				item.setID(cur.getInt(cur.getColumnIndex(KEY_ID)));
				item.setIsCheck(getBoolean(cur.getInt(cur.getColumnIndex(KEY_CHECK))));
				item.setKeyNote(cur.getInt(cur.getColumnIndex(KEY_NOTE)));
				item.setKeyOrder(cur.getInt(cur.getColumnIndex(KEY_ORDER)));
				item.setText(cur.getString(cur.getColumnIndex(KEY_TEXT)));
			}
		}

		cur.close();
		db.close();
		return item;
	}

	public ArrayList<CheckItem> getAllCheckItemWithNoteID(int id) {
		ArrayList<CheckItem> itemList = null;
		SQLiteDatabase db = getReadableDatabase();
		String query = "SELECT * FROM " + LIST_CHECK_TABLE + " WHERE " + KEY_NOTE + "=" + id;

		Cursor cur = db.rawQuery(query, null);

		if (cur != null) {
			if (cur.getCount() > 0) {
				cur.moveToFirst();
				itemList = new ArrayList<>();
				do {
					CheckItem item = new CheckItem();
					item.setID(cur.getInt(cur.getColumnIndex(KEY_ID)));
					item.setIsCheck(getBoolean(cur.getInt(cur.getColumnIndex(KEY_CHECK))));
					item.setKeyNote(cur.getInt(cur.getColumnIndex(KEY_NOTE)));
					item.setKeyOrder(cur.getInt(cur.getColumnIndex(KEY_ORDER)));
					item.setText(cur.getString(cur.getColumnIndex(KEY_TEXT)));
					itemList.add(item);

					if (cur.isLast()) {
						break;
					}
					else {
						cur.moveToNext();
					}
				} while (true);
			}
		}

		cur.close();
		db.close();
		return itemList;
	}

	public void removeCheckItemWithNoteId(int id) {
		SQLiteDatabase db = getWritableDatabase();
		db.delete(LIST_CHECK_TABLE, "" + KEY_NOTE + "=" + id, null);
		db.close();
	}

	public int getCountCheckedItemWithNoteID(int id) {
		SQLiteDatabase db = getReadableDatabase();
		String query = "SELECT * FROM " + LIST_CHECK_TABLE + " WHERE " + KEY_NOTE + "=" + id;
		Cursor cur = db.rawQuery(query, null);
		int count = cur.getCount();
		cur.close();
		db.close();
		return count;
	}

	public boolean checkIfCheckItemExist(int id) {
		String where = "SELECT * FROM " + LIST_CHECK_TABLE + " WHERE " + KEY_ID + "=" + id;
		SQLiteDatabase db = getReadableDatabase();
		Cursor cur = db.rawQuery(where, null);
		if (cur != null) {
			if (cur.getCount() > 0) {
				db.close();
				cur.close();
				return true;
			}
		}
		return false;
	}

	public ArrayList<NoteItem> search(String s) {
		SQLiteDatabase db = getReadableDatabase();
		String query = "SELECT "

                    + " TB1." + KEY_ID + ", TB1." + KEY_TITLE + ", TB1." + KEY_TEXT
                    + ", TB1." + KEY_DATE + ", TB1." + KEY_COLOR + ", TB1." + KEY_BOLD + ", TB1." + KEY_REMIND

                    + " FROM " + TABLE_NOTES + " AS TB1 LEFT OUTER JOIN " + LIST_CHECK_TABLE + " AS TB2 "
					+ " ON TB1." + KEY_ID + "=" + "TB2." + KEY_NOTE + ""

					+ " WHERE TB1." + KEY_TEXT + " LIKE '%" + s + "%'"
					+ " OR TB1." + KEY_TITLE + " LIKE '%" + s + "%'"
					+ " OR TB2." + KEY_TEXT + " LIKE '%" + s + "%'";
		Cursor cur = db.rawQuery(query, null);

		if (cur != null && cur.getCount() > 0) {
			ArrayList<NoteItem> ret = new ArrayList<>();

			print(cur, s);

			cur.moveToLast();
			do {

                int id = cur.getInt(cur.getColumnIndex(KEY_ID));

                if (!checkIfIdExistInList(ret, id)) {
                    NoteItem note = new NoteItem();
                    note.setId(id);
                    note.setTitle(cur.getString(cur.getColumnIndex(KEY_TITLE)));
                    note.setText(cur.getString(cur.getColumnIndex(KEY_TEXT)));
                    note.setDate(cur.getString(cur.getColumnIndex(KEY_DATE)));
                    note.setColor(cur.getInt(cur.getColumnIndex(KEY_COLOR)));
                    note.setBold(getBoolean(cur.getInt(cur.getColumnIndex(KEY_BOLD))));
                    note.setRemind(cur.getLong(cur.getColumnIndex(KEY_REMIND)));
                    ret.add(note);
                }

				if (cur.isFirst()) break;
				else cur.moveToPrevious();
			} while (true);
			return ret;
		}
		else {
			return new ArrayList<>();
		}
	}

    public boolean checkIfIdExistInList(ArrayList<NoteItem> list, int id) {
        for (NoteItem item : list) {
            if (item.getId() == id) return true;
        }
        return false;
    }

	public void print(Cursor cur, String s) {

		if (cur != null && cur.getCount() > 0) {
			cur.moveToFirst();
			do {

//                StringBuilder ss = new StringBuilder("");
//
//                for(int i=0; i<cur.getColumnCount();i++)
//                {
//                    ss.append(cur.getColumnName(i) + "\t");
//                }
//
//                Log.d(s, ss.toString());

				Log.d(s, "" + cur.getInt(cur.getColumnIndex(KEY_ID)) + "\t"
							+ cur.getString(cur.getColumnIndex(KEY_TITLE)) + "\t"
							+ cur.getString(cur.getColumnIndex(KEY_TEXT)) + "\t"
							+ cur.getString(cur.getColumnIndex(KEY_DATE)) + "\t"
							+ cur.getInt(cur.getColumnIndex(KEY_COLOR)) + "\t"
							+ cur.getInt(cur.getColumnIndex(KEY_BOLD)) + "\t"
							+ cur.getLong(cur.getColumnIndex(KEY_REMIND)));

				if (cur.isLast()) break;
				else cur.moveToNext();
			} while(true);
		}

	}

	public void removeAllNote(){
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
