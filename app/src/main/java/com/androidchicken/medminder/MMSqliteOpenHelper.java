package com.androidchicken.medminder;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

/**
 * Created by Elisabeth Huhn on 7/9/2016.
 * This class makes all the actual calls to the DB
 * Thus, if there is a need to put such calls on a background thread, that
 * can be managed by the DB Manager.
 * But if it touches the DB directly, this class does it
 */
public class MMSqliteOpenHelper extends SQLiteOpenHelper {
    //logcat Tag
    private static final String TAG = "MMSqliteOpenHelper";

    /*****************************************************/
    /*****************************************************/
    /*****************************************************/

    //Database Version
    private static final int DATABASE_VERSION = 1;

    //Database Name
    private static final String DATABASE_NAME = "MedMinder";

    /*****************************************************/
    /*****************************************************/
    /*****************************************************/
    //Common Column Names
    public static final String KEY_ID = "id";
    public static final String KEY_CREATED_AT = "created_at";

    /*****************************************************/
    /*****    Person Table     ***************************/
    /*****************************************************/

    //Table Names
    public static final String TABLE_PERSON          = "Person";

      //Person Column Names
    public static final String PERSON_ID       = "person_id";
    public static final String PERSON_NICKNAME = "person_nickname";
    public static final String PERSON_EMAIL    = "person_email";
    public static final String PERSON_TEXT     = "person_text";
    public static final String PERSON_DURATION = "person_duration";
    public static final String PERSON_ORDER    = "person_order";



    //create person table
    private static final String CREATE_TABLE_PERSON = "CREATE TABLE " + TABLE_PERSON +"(" +
            KEY_ID          + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            PERSON_ID       + " INTEGER, " +
            PERSON_NICKNAME + " TEXT, "      +
            PERSON_EMAIL    + " TEXT, "      +
            PERSON_TEXT     + " TEXT, "      +
            PERSON_DURATION + " INTEGER, "   +
            PERSON_ORDER    + " INTEGER, "   +
            KEY_CREATED_AT  + " DATETIME "  + ")";

    /*****************************************************/
    /*****    Medication Table          ******************/
    /*****************************************************/

    //Table Name
    public static final String TABLE_MEDICATION      = "Medication";

    // Column Names
    public static final String MEDICATION_ID             = "medication_id";
    public static final String MEDICATION_FOR_PERSON_ID  = "medication_for_person_id";
    public static final String MEDICATION_BRAND_NAME     = "medication_brand_name";
    public static final String MEDICATION_GENERIC_NAME   = "medication_generic_name";
    public static final String MEDICATION_NICK_NAME      = "medication_nick_name";
    public static final String MEDICATION_ORDER          = "medication_order";
    public static final String MEDICATION_DOSE_AMOUNT    = "medication_dose_amount";
    public static final String MEDICATION_DOSE_UNITS     = "medication_dose_units";
    public static final String MEDICATION_WHEN_DUE       = "medication_when_due";
    public static final String MEDICATION_NUMBER_PER_DAY = "medication_number_per_day";


    //create  table
    private static final String CREATE_TABLE_MEDICATION = "CREATE TABLE " + TABLE_MEDICATION +"(" +
            KEY_ID                    + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            MEDICATION_ID             + " INTEGER, "  +
            MEDICATION_FOR_PERSON_ID  + " INTEGER, "  +
            MEDICATION_BRAND_NAME     + " TEXT, "     +
            MEDICATION_GENERIC_NAME   + " TEXT, "     +
            MEDICATION_NICK_NAME      + " TEXT, "     +
            MEDICATION_ORDER          + " INTEGER, "  +
            MEDICATION_DOSE_AMOUNT    + " INTEGER, "  +
            MEDICATION_DOSE_UNITS     + " INTEGER, "  +
            MEDICATION_WHEN_DUE       + " TEXT, "     +
            MEDICATION_NUMBER_PER_DAY + " INTEGER, "  +
            KEY_CREATED_AT            + " INTEGER "  + ")";

    /*****************************************************/
    /*****    Concurrent Dose Table     ******************/
    /*****************************************************/
    //Table Name
    public static final String TABLE_CONCURRENT_DOSE = "ConcurrentDose";

    // Column Names
    public static final String CONCURRENT_DOSE_ID              = "concurrent_dose_id";
    public static final String CONCURRENT_DOSE_FOR_PERSON_ID   = "concurrent_dose_for_person_id";
    public static final String CONCURRENT_DOSE_IS_START_OF_DAY = "concurrent_dose_is_start_of_day";
    public static final String CONCURRENT_DOSE_START_TIME      = "concurrent_dose_start_time";



    //create  table
    private static final String CREATE_TABLE_CONCURRENT_DOSE = "CREATE TABLE " + TABLE_CONCURRENT_DOSE +"(" +
            KEY_ID                          + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            CONCURRENT_DOSE_ID              + " INTEGER, " +
            CONCURRENT_DOSE_FOR_PERSON_ID   + " INTEGER, " +
            CONCURRENT_DOSE_IS_START_OF_DAY + " INTEGER, " + //Boolean 0=false, 1 = true
            CONCURRENT_DOSE_START_TIME      + " INTEGER, " +
            KEY_CREATED_AT                  + " INTEGER " + ")";


    /*****************************************************/
    /*****    Dose Table                ******************/
    /*****************************************************/
    //Table Name
    public static final String TABLE_DOSE            = "Dose";

    //Dose Column Names
    public static final String DOSE_ID               = "dose_id";
    public static final String DOSE_OF_MEDICATION_ID = "dose_of_medication_id";
    public static final String DOSE_FOR_PERSON_ID    = "dose_for_person_id";
    public static final String DOSE_CONTAINED_IN_CONCURRENT_DOSE
                                                     = "dose_contained_in_concurrent_dose";
    public static final String DOSE_TIME_TAKEN       = "dose_time_taken";
    public static final String DOSE_AMOUNT_TAKEN     = "dose_amount_taken";




    //create dose table
    private static final String CREATE_TABLE_DOSE = "CREATE TABLE " + TABLE_DOSE +"(" +
            KEY_ID                            + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            DOSE_ID                           + " INTEGER, " +
            DOSE_OF_MEDICATION_ID             + " INTEGER, " +
            DOSE_FOR_PERSON_ID                + " INTEGER, " +
            DOSE_CONTAINED_IN_CONCURRENT_DOSE + " INTEGER, " +
            DOSE_TIME_TAKEN                   + " INTEGER, " +
            DOSE_AMOUNT_TAKEN                 + " INTEGER, " +
            KEY_CREATED_AT                    + " INTEGER"   + ")";

    /*****************************************************/
    /*****************************************************/
    /*****************************************************/



    private Context mContext;
    private SQLiteDatabase mDatabase;


    /*****************************************************/
    /*******  Constructor               ******************/
    /*****************************************************/

    //This should be called with the APPLICATION context
    public MMSqliteOpenHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);

        this.mContext = context;
    }

    /*****************************************************/
    /*******  Lifecycle Methods         ******************/
    /*****************************************************/

    /******************
     * onCreate()
     * when the helper constructor is executed with a name (2nd param),
     * the platform checks if the database (second parameter) exists or not and
     * if the database exists, it gets the version information from the database file header and
     * triggers the right call back (e.g. onUpdate())
     * if the database with the name doesn't exist, the platform triggers onCreate().
     *
     * @param db  The instance of the database that is being created
     */
    @Override
    public void onCreate(SQLiteDatabase db){
        //create the tables using the pre-defined SQL
        db.execSQL(CREATE_TABLE_PERSON);
        db.execSQL(CREATE_TABLE_MEDICATION);
        db.execSQL(CREATE_TABLE_CONCURRENT_DOSE);
        db.execSQL(CREATE_TABLE_DOSE);
    }

    /******************
     * This default version of the onUpgrade() method just
     * deletes any data in the database file, and recreates the
     * database from scratch.
     *
     * Obviously, in the production version, this method will have
     * to migrate data in the old version table layout
     * to the new version table layout.
     * Renaming tables,
     * creating new tables,
     * writing data from renamed table to the new table,
     * then dropping the renamed table.
     * And doing this in a cascading fashion so the tables can
     * be brought up to date over several versions.
     * @param db         The instance of the db to be upgraded
     * @param OldVersion The old version number
     * @param newVersion The new version number
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int OldVersion, int newVersion){
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PERSON);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MEDICATION);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DOSE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CONCURRENT_DOSE);

        //Create new tables
        onCreate(db);
    }

    @Override
    public void onOpen(SQLiteDatabase db){
        super.onOpen(db);
        String temp = "";
    }





    /************************************************/
    /*         Generic CRUD routines                */
    /* The same routine will do for all data types  */
    /************************************************/


    //****************************** Create ****************************
    public void add(SQLiteDatabase db,
                    String   table,
                    String   nullColumnHack,
                    ContentValues  values){
        // Inserting Rows
        db.insert(table, null, values);
        //db.close(); //never close the db instance. Just leave the connection open
    }

    //**************************** READ *******************************
    public Cursor getObject(SQLiteDatabase db,
                            String   table,
                            String[] columns,
                            String   where_clause,
                            String[] selectionArgs,
                            String   groupBy,
                            String   having,
                            String   orderBy){
        /********************************
         Cursor query (String table, //Table Name
                         String[] columns,   //Columns to return, null for all columns
                         String where_clause,
                         String[] selectionArgs, //replaces ? in the where_clause with these arguments
                         String groupBy, //null meanas no grouping
                         String having,   //row grouping
                         String orderBy)  //null means the default sort order
         *********************************/
            return (db.query(table, columns, where_clause, selectionArgs, groupBy, having, orderBy));
    }


    //********************* UPDATE *************************
    //returns the number of rows affected
    public int update (SQLiteDatabase  db,
                        String         table,
                        ContentValues  cv,          //Column names and new values
                        String         where_clause,//null updates all rows
                        String[]       where_args ){ //values that replace ? in where clause
        //update(String table, ContentValues values, String whereClause, String[] whereArgs)
        //Any ? in the where_clause are replaced with arguments
        return (db.update(table, cv, where_clause, where_args));
    }


    //***************** DELETE ***************************************
    //returns the number of rows affected
    public int remove (SQLiteDatabase  db,
                       String         table,
                       String         where_clause,//null updates all rows
                       String[]       where_args ){ //values that replace ? in where clause

        return (delete(db, table, where_clause, where_args));
    }


    //returns the number of rows affected
    public int delete (SQLiteDatabase  db,
                       String         table,
                       String         where_clause,//null updates all rows
                       String[]       where_args ){ //values that replace ? in where clause

        return (db.delete(table, where_clause, where_args));
    }


    /************************************************/
    /*      Object Specific CRUD routines           */
    /*     Each Class has it's own routine          */
    /************************************************/

    //********************* PERSON ****************************************************88




}
