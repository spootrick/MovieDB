package com.example.furkan.moviedb;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by furkan on 27.12.2015.
 */
public class DBHelper extends SQLiteOpenHelper{

    private static final String DBNAME = "moviedb.db";
    private static final int VERSION = 2;

    //table name
    private static final String TABLE_NAME = "movies";

    //db columns
    public static final String ID = "_id";
    public static final String TITLE = "title";
    public static final String YEAR = "year";
    public static final String IMDB = "imdb";
    public static final String GENRE = "genre";
    public static final String TYPE = "type";
    public static final String URL = "url";
    public static final String ISWATCHED = "isWatched";

    private SQLiteDatabase myDB;


    public DBHelper(Context context) {
        super(context, DBNAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //Creating database table with given parameters
        String queryTable = "CREATE TABLE " + TABLE_NAME + " ("
                + ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + TITLE + " TEXT NOT NULL, "
                + YEAR + " TEXT NOT NULL, "
                + IMDB + " TEXT NOT NULL, "
                + GENRE + " TEXT NOT NULL, "
                + TYPE + " TEXT NOT NULL, "
                + URL + " TEXT NOT NULL, "
                + ISWATCHED + " TEXT NOT NULL"
                + ")";

        db.execSQL(queryTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    /**
     * This method opens the database
     */
    public void openDB(){
        myDB = getWritableDatabase();
    }

    /**
     * This method closes the database
     */
    public void closeDB(){
        if(myDB != null && myDB.isOpen()){
            myDB.close();
        }
    }

    /**
     * This method inserts data into the database
     * @return
     */
    public long insert(String title, String year, String imdb, String genre, String type, String url, String isWatched){
        ContentValues values = new ContentValues();
        //values.put(column name, variable)
        values.put(TITLE, title);
        values.put(YEAR, year);
        values.put(IMDB, imdb);
        values.put(GENRE, genre);
        values.put(TYPE, type);
        values.put(URL, url);
        values.put(ISWATCHED, isWatched);

        //insert method will return long value
        return myDB.insert(TABLE_NAME, null, values);
    }

    /**
     * This method deletes the data with given id
     * @param id
     * @return
     */
    public long delete(int id){
        //where argument
        String where = ID + " = " + id;

        //insert method will return long value
        return myDB.delete(TABLE_NAME, where, null);
    }

    /**
     * This method updates the data with given id
     * @param id
     * @return
     */
    public long update(int id, String isWatched){
        ContentValues values = new ContentValues();
        //values.put(column name, variable)
        values.put(ISWATCHED, isWatched);

        //where argument
        String where = ID + " = " + id;

        //insert methods returns long value
        return myDB.update(TABLE_NAME, values, where, null);
    }

    public int deleteAllRecords(){
        return myDB.delete(TABLE_NAME, null, null);
    }

    /**
     * This method retrieves all the records
     * @return
     */
    public Cursor getAllRecords(){
        String query = "SELECT * FROM " + TABLE_NAME;
        return myDB.rawQuery(query, null);
    }

    public Cursor getRow(long id){
        String query = "SELECT * FROM " + TABLE_NAME + " WHERE " + ID + " = " + id;
        return myDB.rawQuery(query, null);
    }
}
