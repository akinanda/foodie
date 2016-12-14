package edu.sjsu.cafe.offers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;

import edu.sjsu.cafe.AppUtility;
import edu.sjsu.cafe.database.DatabaseHelper;

import static edu.sjsu.cafe.database.DatabaseHelper.OFFER_TABLE_NAME;

/**
 * Created by Akshay on 12/9/2016.
 */

public class OfferManager {

    private DatabaseHelper dbHelper;
    private Context context;
    private SQLiteDatabase database;

    public OfferManager(Context c) {
        context = c;
    }

    public OfferManager open() throws SQLException {
        if(dbHelper==null) {
            dbHelper = new DatabaseHelper(context);
            database = dbHelper.getWritableDatabase();
        }
        return this;
    }

    public void close() {
        dbHelper.close();
    }

    public void insertOffer(String offerCode, String desc, String expiryDate) {
        ContentValues contentValue = new ContentValues();
        contentValue.put(DatabaseHelper.OFFER_CODE, offerCode);
        contentValue.put(DatabaseHelper.OFFER_DESC, desc);
        contentValue.put(DatabaseHelper.OFFER_EXPIRY, expiryDate);
        database.insert(OFFER_TABLE_NAME, null, contentValue);
        ++AppUtility.offerCount;
    }

    public Cursor getOffers() {
        //first delete expired offers
        deleteExpiredOffers();

        //fetch data
        String[] columns = new String[] { DatabaseHelper.ID, DatabaseHelper.OFFER_CODE, DatabaseHelper.OFFER_DESC, DatabaseHelper.OFFER_EXPIRY };
        Cursor cursor = database.query(OFFER_TABLE_NAME, columns, null, null, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
        }
        return cursor;
    }

    public int getOfferCount() {
        deleteExpiredOffers();
        String countQuery = "SELECT  * FROM " + OFFER_TABLE_NAME;
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int offers = cursor.getCount();
        cursor.close();
        AppUtility.offerCount = offers;
        return offers;
    }

    public void deleteExpiredOffers() {
        String today = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        database.delete(OFFER_TABLE_NAME, DatabaseHelper.OFFER_EXPIRY + " < '" + today +"'", null);
    }

    public void deleteOffer(String offerCode) {
        database.delete(OFFER_TABLE_NAME, DatabaseHelper.OFFER_CODE + " = '" + offerCode+"'", null);
        --AppUtility.offerCount;
    }
}
