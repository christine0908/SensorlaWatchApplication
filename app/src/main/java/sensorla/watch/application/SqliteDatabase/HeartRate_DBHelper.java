package sensorla.watch.application.SqliteDatabase;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import sensorla.watch.application.Constants;
import sensorla.watch.application.Model.HeartRateModel;

public class HeartRate_DBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "ChrisDB";    // Database Name mandatory
    private static final String TABLE_NAME_HEARTRATE = "Heartrate";
    private static final int DATABASE_Version = 1;    // Database Version mandatory


    // TABLE_NAME_1 -column names
    private static final String HEART_RATE_ID = "heart_rate_id";
    private static final String HEART_RATE = "heart_rate";
    private static final String RECORDED_DATETIME = "recorded_datetime"; //DateTime
    private static final String USER_ID = "user_id";

    // initialise the database and null is refering to the cursors
    public HeartRate_DBHelper( Context context) {
        super(context, DATABASE_NAME, null, DATABASE_Version);
    }

    private static final String CREATE_TABLE_Heartrate =
            "CREATE TABLE "+ TABLE_NAME_HEARTRATE +
                    "(" + HEART_RATE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + HEART_RATE + " TEXT, "
                    + USER_ID + " TEXT, "
                    + RECORDED_DATETIME + " TEXT )";

    private static final String DROP_TABLE_Heartrate ="DROP TABLE IF EXISTS " + TABLE_NAME_HEARTRATE;

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_Heartrate);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        db.execSQL(DROP_TABLE_Heartrate);
        onCreate(db);
    }

    public void InitDB()
    {
        SQLiteDatabase db = getWritableDatabase();
        onUpgrade(db, 1, 2);
    }

    public String getDbString(Cursor cursor, String columnName) {
        return cursor.getString(cursor.getColumnIndex(columnName));
    }




    public boolean InsertHeartRate(String heart_rate, String user_id) {
        try {
            //Get the Data Repository in write mode. means get access to database
            SQLiteDatabase db = getWritableDatabase();
            //Create a new map of values, where column names are the keys
            // say what values you want to insert by creating a ContentValues object, this is used to hold name/value pairs.
            ContentValues cValues = new ContentValues();
            cValues.put(HEART_RATE, heart_rate);
            cValues.put(RECORDED_DATETIME, now());
            cValues.put(USER_ID,user_id);
            // finally use insert method after inserting all above in one single ROW.
            db.insert(TABLE_NAME_HEARTRATE, null, cValues);
            db.close();
            return true;
        }
        catch (Exception e) {
            return false;
        }
    }

    public List<HeartRateModel> GetHeartRate(String userId) {
        SQLiteDatabase db = getReadableDatabase();
//        String query = "SELECT * " +
//                " FROM " + TABLE_NAME_HEARTRATE + " where " + USER_ID + " =?";

        String query = "SELECT * " +
                " FROM " + TABLE_NAME_HEARTRATE + " where " + USER_ID + " =? AND " +  HEART_RATE + "<" + Constants.TRESHOLD_LIMIT;

        Cursor cursor = db.rawQuery(query, new String[]{userId});
//        Cursor cursor = db.query(TABLE_NAME_HEARTRATE,new String[]{
//                        USER_ID},
//                USER_ID + " = ? ",new String[]{userId},null, null, null, null);

        if(cursor != null && cursor.moveToFirst())
        {
            List<HeartRateModel> HeartRateList = new ArrayList<>();

            while (cursor.moveToNext()){
                HeartRateModel heartRateModel = new HeartRateModel() ;
                heartRateModel.setUserId(getDbString(cursor, USER_ID));
                heartRateModel.setDatetime(getDbString(cursor, RECORDED_DATETIME));
                heartRateModel.setValue(getDbString(cursor, HEART_RATE));
                HeartRateList.add(heartRateModel);
            }
            return HeartRateList;
        }
        return null;
    }

    public void DeleteHeartRates(String userId) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_NAME_HEARTRATE, USER_ID + " = ?", new String[]{ userId });
        db.close();
    }

    public String now() {
        Date date = new Date();
        return fromDate(date);
    }

    public Date toDate(String date) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            return dateFormat.parse(date);
        }
        catch (Exception e) { return null; }
    }

    public String fromDate(Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        return dateFormat.format(date);
    }


}
