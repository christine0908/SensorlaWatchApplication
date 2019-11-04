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

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import sensorla.watch.application.Service.ApiService;
import sensorla.watch.application.Service.ServiceGenerator.ServiceGenerator;

public class DbHelper extends SQLiteOpenHelper {
    WorkOrder workOrder = new WorkOrder();
    WorkOrderDetail workOrderDetail = new WorkOrderDetail();

    private static final String DATABASE_NAME = "Work_Order_DB";    // Database Name
    private static final String TABLE_NAME_1 = "workorder";   // Table1 Name
    private static final String TABLE_NAME_2 = "workorderdetail";   // Table2 Name
    private static final int DATABASE_Version = 1;    // Database Version

    // Common column names
    private static final String WORK_ORDER_ID = "work_order_id";
    private static final String WORK_ORDER_DETAIL_ID = "work_order_detail_id";

    // TABLE_NAME_1 -column names
    private static final String WORK_ORDER_NAME = "work_order_name";
    private static final String WORK_ORDER_LOCATION_NAME = "work_order_location_name";
    private static final String WORK_ORDER_INSTRUCTION = "work_order_instruction";
    private static final String WORK_ORDER_PRIORITY = "work_order_priority";
    private static final String WORK_ORDER_STATUS = "work_order_status";
    private static final String WORK_ORDER_SNOOZE = "work_order_snooze"; //Boolean
    private static final String SNOOZE_DATETIME = "snooze_datetime"; //DateTime
    private static final String LAST_STATUS_TIME = "last_status_time"; //DateTime
    private static final String USER_ID = "user_id";
    //private static final String WORK_ORDER_TYPE ="work_order_type";

    // TABLE_NAME_2 -column names
    private static final String STATUS = "status";
    private static final String EXTERNAL_ID = "external_id";
    private static final String ENVIRONMENT = "environment";
    private static final String CREATE_DATETIME = "create_datetime";

    public DbHelper( Context context) {
        super(context, DATABASE_NAME, null, DATABASE_Version);
    }

    // Table1 Create Statements
    private static final String CREATE_TABLE_1 =
            "CREATE TABLE "+ TABLE_NAME_1 +
                    "(" + WORK_ORDER_ID + " INTEGER PRIMARY KEY,"
                    + WORK_ORDER_NAME + " TEXT," + WORK_ORDER_LOCATION_NAME + " TEXT," + WORK_ORDER_INSTRUCTION + " TEXT,"
                    + WORK_ORDER_SNOOZE + " TEXT," + WORK_ORDER_PRIORITY + " TEXT,"
                    + WORK_ORDER_STATUS + " TEXT," + USER_ID + " TEXT," + CREATE_DATETIME + " TEXT )";

    // Table2 Create Statements
    private static final String CREATE_TABLE_2 =
            "CREATE TABLE " + TABLE_NAME_2 +
                    "(" + WORK_ORDER_DETAIL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,"
                    + WORK_ORDER_ID + " TEXT," + STATUS + " TEXT," + EXTERNAL_ID + " TEXT,"
                    + ENVIRONMENT + " TEXT," + CREATE_DATETIME + " TEXT" + ")";

    private static final String DROP_TABLE_1 ="DROP TABLE IF EXISTS " + TABLE_NAME_1;
    private static final String DROP_TABLE_2 ="DROP TABLE IF EXISTS " + TABLE_NAME_2;

    @Override
    public void onCreate(SQLiteDatabase db) {
        // creating tables
        db.execSQL(CREATE_TABLE_1);
        db.execSQL(CREATE_TABLE_2);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if exist
        db.execSQL(DROP_TABLE_1);
        db.execSQL(DROP_TABLE_2);
        // Create tables again
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

    // region WorkOrder Table
    public boolean InsertWorkOrder (String work_order_id, String work_order_name,
                                    String work_order_location_name, String work_order_instruction,
                                    String work_order_snooze, String work_order_priority,
                                    String work_order_status, String user_id) {
        try {
            //Get the Data Repository in write mode
            SQLiteDatabase db = getWritableDatabase();
            //Create a new map of values, where column names are the keys
            ContentValues cValues = new ContentValues();
            cValues.put(WORK_ORDER_ID, work_order_id);
            cValues.put(WORK_ORDER_NAME,work_order_name);
            cValues.put(WORK_ORDER_LOCATION_NAME,work_order_location_name);
            cValues.put(WORK_ORDER_INSTRUCTION,work_order_instruction);
            cValues.put(WORK_ORDER_SNOOZE,work_order_snooze);
            cValues.put(CREATE_DATETIME, now());
            cValues.put(WORK_ORDER_PRIORITY,work_order_priority);
            cValues.put(WORK_ORDER_STATUS,work_order_status);
            cValues.put(USER_ID, user_id);
            db.insert(TABLE_NAME_1, null, cValues);
            db.close();
            return true;
        }
        catch (Exception e) {
            return false;
        }
    }

    public List<WorkOrder> GetWorkOrderListByUserId(String user_id) {
        SQLiteDatabase db = getWritableDatabase();
        List<WorkOrder> workOrderList = new ArrayList<>();

        Cursor cursor = db.query(TABLE_NAME_1,new String[]{
                        WORK_ORDER_ID , WORK_ORDER_SNOOZE , CREATE_DATETIME ,
                        WORK_ORDER_NAME , WORK_ORDER_LOCATION_NAME , WORK_ORDER_INSTRUCTION , WORK_ORDER_STATUS },
                USER_ID+ " = ? ",new String[]{user_id},null, null, WORK_ORDER_ID, null);

        while (cursor.moveToNext()) {
            WorkOrder workOrder = new WorkOrder();
            workOrder.work_order_id = getDbString(cursor, WORK_ORDER_ID);
            workOrder.work_order_name = getDbString(cursor, WORK_ORDER_NAME);
            workOrder.work_order_location_name = getDbString(cursor, WORK_ORDER_LOCATION_NAME);
            workOrder.work_order_instruction = getDbString(cursor, WORK_ORDER_INSTRUCTION);
            workOrder.work_order_status = getDbString(cursor, WORK_ORDER_STATUS);
            workOrder.lastStatusTime = toDate(getDbString(cursor, CREATE_DATETIME));
            workOrderList.add(workOrder);
        }

        return workOrderList;
    }

    public WorkOrder GetWorkOrderByWorkOrderId(String woID) {
        SQLiteDatabase db = getWritableDatabase();
        WorkOrder workOrder = new WorkOrder();
        String query = "SELECT work_order_name , work_order_location_name , work_order_instruction , work_order_status" +
                " FROM " + TABLE_NAME_1;
        Cursor cursor = db.query(TABLE_NAME_1,new String[]{
                        WORK_ORDER_ID , WORK_ORDER_SNOOZE , CREATE_DATETIME ,
                        WORK_ORDER_NAME , WORK_ORDER_LOCATION_NAME , WORK_ORDER_INSTRUCTION , WORK_ORDER_STATUS },
                WORK_ORDER_ID+ " = ? ",new String[]{woID},null, null, WORK_ORDER_ID, null);

        while (cursor.moveToNext()){
            workOrder.work_order_id = getDbString(cursor, WORK_ORDER_ID);
            workOrder.work_order_name = getDbString(cursor, WORK_ORDER_NAME);
            workOrder.work_order_location_name = getDbString(cursor, WORK_ORDER_LOCATION_NAME);
            workOrder.work_order_instruction = getDbString(cursor, WORK_ORDER_INSTRUCTION);
            workOrder.work_order_status = getDbString(cursor, WORK_ORDER_STATUS);
            workOrder.lastStatusTime = toDate(getDbString(cursor, CREATE_DATETIME));
        }
        return workOrder;
    }

    public Boolean UpdateWorkOrder (WorkOrder updatedWO) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cValues = new ContentValues();
        cValues.put(WORK_ORDER_ID, updatedWO.work_order_id);
        cValues.put(WORK_ORDER_NAME, updatedWO.work_order_name);
        cValues.put(WORK_ORDER_LOCATION_NAME, updatedWO.work_order_location_name);
        cValues.put(WORK_ORDER_INSTRUCTION, updatedWO.work_order_instruction);
        cValues.put(WORK_ORDER_SNOOZE, updatedWO.work_order_snooze);
        cValues.put(CREATE_DATETIME, fromDate(updatedWO.lastStatusTime));
        cValues.put(WORK_ORDER_PRIORITY, updatedWO.work_order_priority);
        cValues.put(WORK_ORDER_STATUS, updatedWO.work_order_status);
        String[] whereArgs= {updatedWO.work_order_id};
        db.update(TABLE_NAME_1,cValues,WORK_ORDER_ID +" = ?", whereArgs);
        return true;
    }

    public void DeleteWorkOrder(String work_order_id) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_NAME_1, WORK_ORDER_ID + " = ?", new String[]{ work_order_id });
        db.close();
    }
    // endregion

    // region WorkOrderDetail Table
    public boolean InsertWorkOrderDetail (String work_order_id, String status,
                                          String external_id, String environment, String create_datetime) {
        try {
            //Get the Data Repository in write mode
            SQLiteDatabase db = getWritableDatabase();
            //Create a new map of values, where column names are the keys
            ContentValues cValues = new ContentValues();
            cValues.put(WORK_ORDER_ID, work_order_id);
            cValues.put(STATUS, status);
            cValues.put(EXTERNAL_ID, external_id);
            cValues.put(ENVIRONMENT, environment);
            cValues.put(CREATE_DATETIME, create_datetime);
            db.insert(TABLE_NAME_2, null, cValues);
            db.close();
            return true;
        }
        catch (Exception e){
            return false;
        }
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

    public List<WorkOrderDetail> GetWorkOrderDetailList(){
        SQLiteDatabase db = getWritableDatabase();
        List<WorkOrderDetail> workOrderDetailList = new ArrayList<>();
        String query = "SELECT * FROM " + TABLE_NAME_2;
        Cursor cursor = db.rawQuery(query,null);

        while (cursor.moveToNext()){
            WorkOrderDetail wod = new WorkOrderDetail();
            wod.work_order_detail_id = getDbString(cursor, WORK_ORDER_DETAIL_ID);
            wod.work_order_id = getDbString(cursor, WORK_ORDER_ID);
            wod.external_id = getDbString(cursor, EXTERNAL_ID);
            wod.create_datetime = getDbString(cursor, CREATE_DATETIME);
            wod.status = getDbString(cursor, STATUS);
            wod.environment = getDbString(cursor, ENVIRONMENT);
            workOrderDetailList.add(wod);
        }
        return workOrderDetailList;
    }

    public void DeleteWorkOrderDetail(String work_order_detail_id) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_NAME_2, WORK_ORDER_DETAIL_ID + " = ?", new String[]{work_order_detail_id});
        db.close();
    }

    // region BackgroundWorker
    final ApiService service = ServiceGenerator.createService(ApiService.class);
    Call<String> apiCall;
    public void UploadWorkOrderDetailToServer()
    {
        List<WorkOrderDetail> workOrderDetailList = GetWorkOrderDetailList();
        for (final WorkOrderDetail wod : workOrderDetailList)
        {
            try {

                apiCall = service.CreateWODetail(wod.work_order_id, wod.status, wod.external_id, wod.environment, wod.create_datetime);
                apiCall.enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(Call<String> call, Response<String> response) {
                        if(response.isSuccessful()){
                            DeleteWorkOrderDetail(wod.work_order_detail_id);
                        }
                    }
                    @Override
                    public void onFailure(Call<String> call, Throwable t) { }
                });
            }catch (Exception ex)
            {
                System.out.println(ex);
            }
        }
    }
    // endregion

    public Boolean DeleteAllWorkOrder() {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_NAME_1, null, null);
        return true;
    }

    public Boolean UpdateWorkOrderDetail (String work_order_detail_id, String work_order_id, String status,
                                          String external_id, String environment, String create_datetime) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cValues = new ContentValues();
        cValues.put(WORK_ORDER_ID,work_order_id);
        cValues.put(STATUS,status);
        cValues.put(EXTERNAL_ID,external_id);
        cValues.put(ENVIRONMENT,environment);
        cValues.put(CREATE_DATETIME,create_datetime);
        String[] whereArgs= {work_order_detail_id};
        db.update(TABLE_NAME_2, cValues,WORK_ORDER_DETAIL_ID + " = ?", whereArgs);
        return true;
    }

    public Boolean DeleteAllWorkOrderDetail() {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_NAME_2, null, null);
        return true;
    }

    public List<WorkOrder> GetWorkOrderList() {
        SQLiteDatabase db = getWritableDatabase();
        List<WorkOrder> workOrderList = new ArrayList<>();
        String query = "SELECT * FROM " + TABLE_NAME_1;
        Cursor cursor = db.rawQuery(query,null);

        while (cursor.moveToNext()){
            WorkOrder workOrder = new WorkOrder();
            workOrder.work_order_id = getDbString(cursor, WORK_ORDER_ID);
            workOrder.work_order_name = getDbString(cursor, WORK_ORDER_NAME);
            workOrder.work_order_location_name = getDbString(cursor, WORK_ORDER_LOCATION_NAME);
            workOrder.work_order_instruction = getDbString(cursor, WORK_ORDER_INSTRUCTION);
            workOrder.work_order_status = getDbString(cursor, WORK_ORDER_STATUS);
            workOrderList.add(workOrder);
        }

        return workOrderList;
    }
}