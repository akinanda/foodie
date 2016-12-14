package edu.sjsu.cafe.messages;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import edu.sjsu.cafe.database.DatabaseHelper;

import static edu.sjsu.cafe.database.DatabaseHelper.MESSAGE_TABLE_NAME;

/**
 * Created by Akshay on 12/8/2016.
 */

public class NotificationMessageManager {
    private DatabaseHelper dbHelper;
    private Context context;
    private SQLiteDatabase database;

    public NotificationMessageManager(Context c) {
        context = c;
    }

    public NotificationMessageManager open() throws SQLException {
        if(dbHelper==null) {
            dbHelper = new DatabaseHelper(context);
            database = dbHelper.getWritableDatabase();
        }
        return this;
    }

    public void close() {
        dbHelper.close();
    }

    public void insertMessage(String title, String body, String date) {
        ContentValues contentValue = new ContentValues();
        contentValue.put(DatabaseHelper.MESSAGE_TITLE, title);
        contentValue.put(DatabaseHelper.MESSAGE_BODY, body);
        contentValue.put(DatabaseHelper.MESSAGE_DATE, date);
        database.insert(MESSAGE_TABLE_NAME, null, contentValue);
    }

    public Cursor getMessages() {
        //fetch data
        String[] columns = new String[]{DatabaseHelper.ID, DatabaseHelper.MESSAGE_TITLE, DatabaseHelper.MESSAGE_BODY, DatabaseHelper.MESSAGE_DATE};
        Cursor cursor = database.query(MESSAGE_TABLE_NAME, columns, null, null, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
        }
        return cursor;
    }

    public void deleteMessage(String title, String message, String date) {
        database.delete(MESSAGE_TABLE_NAME,
                DatabaseHelper.MESSAGE_TITLE + " = '" + title + "' AND "
                        + DatabaseHelper.MESSAGE_BODY + " = '" + message + "' AND "
                        + DatabaseHelper.MESSAGE_DATE + " = '" + date + "'", null);
    }

    public void deleteAll() {
        database.delete(MESSAGE_TABLE_NAME, null, null);
        database.execSQL("vacuum");
    }
}
