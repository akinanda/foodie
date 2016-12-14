package edu.sjsu.cafe.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Akshay on 12/5/2016.
 */

public class DatabaseHelper extends SQLiteOpenHelper {

    // Database Name
    static final String DB_NAME = "AkshayNanda.DB";
    // Database Version
    static final int DB_VERSION = 1;

    // Table Names
    public static final String OFFER_TABLE_NAME = "offers";
    public static final String MESSAGE_TABLE_NAME = "messages";

    // Table columns
    public static final String ID = "_id";
    public static final String OFFER_CODE = "code";
    public static final String OFFER_DESC = "description";
    public static final String OFFER_EXPIRY = "expiry";

    public static final String MESSAGE_TITLE = "title";
    public static final String MESSAGE_BODY = "message";
    public static final String MESSAGE_DATE = "date";


    // Table queries
    private static final String CREATE_OFFER_TABLE = "CREATE TABLE " + OFFER_TABLE_NAME + "("
            + ID + " INTEGER AUTO_INCREMENT, "
            + OFFER_CODE + " TEXT PRIMARY KEY, "
            + OFFER_DESC + " TEXT, "
            + OFFER_EXPIRY +" TEXT);";

    private static final String CREATE_MESSAGE_TABLE = "CREATE TABLE " + MESSAGE_TABLE_NAME + "("
            + ID + " INTEGER AUTO_INCREMENT, "
            + MESSAGE_TITLE + " TEXT, "
            + MESSAGE_BODY + " TEXT, "
            + MESSAGE_DATE +" TEXT);";


    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_OFFER_TABLE);
        db.execSQL(CREATE_MESSAGE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Logic to update to the next version
        onCreate(db);
    }

}
