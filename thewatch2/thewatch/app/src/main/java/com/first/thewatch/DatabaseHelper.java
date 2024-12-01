package com.first.thewatch;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "emails.db";
    private static final int DATABASE_VERSION = 1;

    // Table name and columns
    private static final String TABLE_EMAILS = "emails";
    private static final String TABLE_TOKEN = "fcm_tokens";
    private static final String COLUMN_ID = "id";
    public static final String COLUMN_EMAIL = "email";
    private static final String COLUMN_TOKEN = "fcmtoken";

    // SQL statement to create the emails table
    private static final String SQL_CREATE_EMAILS_TABLE =
            "CREATE TABLE " + TABLE_EMAILS + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    COLUMN_EMAIL + " TEXT)";

    // SQL statement to create the fcm_tokens table
    private static final String SQL_CREATE_TOKENS_TABLE =
            "CREATE TABLE " + TABLE_TOKEN + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    COLUMN_TOKEN + " TEXT)";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create the emails table
        db.execSQL(SQL_CREATE_EMAILS_TABLE);
        // Create the fcm_tokens table
        db.execSQL(SQL_CREATE_TOKENS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop the tables if they exist and recreate them
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EMAILS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TOKEN);
        onCreate(db);
    }

    public long addEmail(String email) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_EMAIL, email);
        // Insert the new row, returning the primary key value of the new row
        long newRowId = db.insert(TABLE_EMAILS, null, values);
        db.close();
        return newRowId;
    }

    // Method to retrieve all emails from the database
    public Cursor getAllEmails() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_EMAILS, null, null, null, null, null, null);
    }

    public void deleteEmail(String email) {
        SQLiteDatabase db = this.getWritableDatabase();
        // Define the WHERE clause
        String selection = COLUMN_EMAIL + " = ?";
        // Define the values for the WHERE clause
        String[] selectionArgs = { email };
        // Delete the contact from the table
        int deletedRows = db.delete(TABLE_EMAILS, selection, selectionArgs);
        db.close();
    }

    public static String getColumnEmail() {
        return COLUMN_EMAIL;
    }


    public long saveFCMToken(String token) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TOKEN, token);
        // Insert the new row, or replace the existing row if the token already exists
        long result = db.insertWithOnConflict(TABLE_TOKEN, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        db.close();
        return result;
    }

    public String getSavedFCMToken() {
        SQLiteDatabase db = this.getReadableDatabase();
        String token = null;
        Cursor cursor = db.query(TABLE_TOKEN, new String[]{COLUMN_TOKEN}, null, null, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            int columnIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_TOKEN);
            if (columnIndex != -1) {
                token = cursor.getString(columnIndex);
            }
            cursor.close();
        }
        return token;
    }
}




