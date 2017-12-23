package com.androidchicken.medminder;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import static com.androidchicken.medminder.MMDatabaseManager.sDB_ERROR_CODE;

/**
 * Created by Elisabeth Huhn on 7/9/2016.
 * This class makes all the actual calls to the DB
 * Thus, if there is a need to put such calls on a background thread, that
 * can be managed by the DB Manager.
 * But if it touches the DB directly, this class does it
 */
class MMDataBaseSqlHelper extends SQLiteOpenHelper {

    //****************************************************/
    //****************************************************/
    //****************************************************/

    //Database Version
    private static final int DATABASE_VERSION = 1;

    //Database Name
    private static final String DATABASE_NAME = "MedMinder";

    //****************************************************/
    //****************************************************/
    //****************************************************/
    //Common Column Names
    private static final String KEY_ID = "id";
    private static final String KEY_CREATED_AT = "created_at";

    //****************************************************/
    //****    Person Table     ***************************/
    //****************************************************/

    //Table Name
    static final String TABLE_PERSON          = "Person";

      //Person Column Names
    static final String PERSON_ID       = "person_id";
    static final String PERSON_NICKNAME = "person_nickname";
    static final String PERSON_EMAIL    = "person_email";
    static final String PERSON_TEXT     = "person_text";
    static final String PERSON_EXISTS   = "person_exists";



    //create person table
    private static final String CREATE_TABLE_PERSON = "CREATE TABLE " + TABLE_PERSON +"(" +
            KEY_ID          + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            PERSON_ID       + " INTEGER, "   +
            PERSON_NICKNAME + " TEXT, "      +
            PERSON_EMAIL    + " TEXT, "      +
            PERSON_TEXT     + " TEXT, "      +
            PERSON_EXISTS   + " INTEGER, "   +
            KEY_CREATED_AT  + " DATETIME "   + ")";

    //****************************************************/
    //****    Medication Table          ******************/
    //****************************************************/

    //Table Name
    static final String TABLE_MEDICATION      = "Medication";

    // Column Names
    static final String MEDICATION_ID             = "med_id";
    static final String MEDICATION_FOR_PERSON_ID  = "med_for_person_id";
    static final String MEDICATION_BRAND_NAME     = "med_brand_name";
    static final String MEDICATION_GENERIC_NAME   = "med_generic_name";
    static final String MEDICATION_NICK_NAME      = "med_nick_name";
    static final String MEDICATION_DOSE_STRATEGY  = "med_dose_strategy";
    static final String MEDICATION_DOSE_AMOUNT    = "med_dose_amount";
    static final String MEDICATION_DOSE_UNITS     = "med_dose_units";
    static final String MEDICATION_DOSE_NUM_PER_DAY = "med_number_per_day";
    static final String MEDICATION_NOTES          = "med_notes";
    static final String MEDICATION_SIDE_EFFECTS   = "med_side_effects";
    static final String MEDICATION_CURRENTLY_TAKEN = "med_curr_taken";//stored as integer, 0 = false, 1 = true


    //create  table
    private static final String CREATE_TABLE_MEDICATION = "CREATE TABLE " + TABLE_MEDICATION +"(" +
            KEY_ID                    + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            MEDICATION_ID             + " INTEGER, "  +
            MEDICATION_FOR_PERSON_ID  + " INTEGER, "  +
            MEDICATION_BRAND_NAME     + " TEXT, "     +
            MEDICATION_GENERIC_NAME   + " TEXT, "     +
            MEDICATION_NICK_NAME      + " TEXT, "     +
            MEDICATION_DOSE_STRATEGY  + " INTEGER, "  +
            MEDICATION_DOSE_AMOUNT    + " INTEGER, "  +
            MEDICATION_DOSE_UNITS     + " INTEGER, "  +
            MEDICATION_DOSE_NUM_PER_DAY + " INTEGER, "  +
            MEDICATION_NOTES          + " TEXT, "     +
            MEDICATION_SIDE_EFFECTS   + " TEXT, "     +
            MEDICATION_CURRENTLY_TAKEN  + " INTEGER, "  +
            KEY_CREATED_AT            + " INTEGER "  + ")";


    //****************************************************/
    //****    Medication Alert Table    ******************/
    //****************************************************/

    //Table Name
    static final String TABLE_MEDICATION_ALERT      = "MedicationAlert";

    // Column Names
    static final String MEDICATION_ALERT_ID               = "med_alert_id";
    static final String MEDICATION_ALERT_MEDICATION_ID    = "med_alert_med_id";
    static final String MEDICATION_ALERT_FOR_PATIENT_ID   = "med_alert_for_patient_id";
    static final String MEDICATION_ALERT_NOTIFY_PERSON_ID = "med_alert_notify_person_id";
    static final String MEDICATION_ALERT_TYPE_NOTIFY      = "med_alert_type_notify";
    static final String MEDICATION_ALERT_OVERDUE_TIME     = "med_alert_overdue_time";



    //create  table
    private static final String CREATE_TABLE_MEDICATION_ALERT = "CREATE TABLE " +
            TABLE_MEDICATION_ALERT +"(" +
            KEY_ID                            + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            MEDICATION_ALERT_ID               + " INTEGER, "  +
            MEDICATION_ALERT_MEDICATION_ID    + " INTEGER, "  +
            MEDICATION_ALERT_FOR_PATIENT_ID   + " INTEGER, "  +
            MEDICATION_ALERT_NOTIFY_PERSON_ID + " INTEGER, "  +
            MEDICATION_ALERT_TYPE_NOTIFY      + " INTEGER, "  +
            MEDICATION_ALERT_OVERDUE_TIME     + " INTEGER, "  +
            KEY_CREATED_AT                    + " INTEGER "  + ")";





    //****************************************************/
    //****    Concurrent Dose Table     ******************/
    //****************************************************/
    //Table Name
    static final String TABLE_CONCURRENT_DOSE = "ConcurrentDose";

    // Column Names
    static final String CONCURRENT_DOSE_ID              = "conc_dose_id";
    static final String CONCURRENT_DOSE_FOR_PERSON_ID   = "conc_dose_for_person_id";
    static final String CONCURRENT_DOSE_TIME            = "conc_dose_start_time";



    //create  table
    private static final String CREATE_TABLE_CONCURRENT_DOSE = "CREATE TABLE " + TABLE_CONCURRENT_DOSE +"(" +
            KEY_ID                          + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            CONCURRENT_DOSE_ID              + " INTEGER, " +
            CONCURRENT_DOSE_FOR_PERSON_ID   + " INTEGER, " +
            CONCURRENT_DOSE_TIME            + " INTEGER, " +
            KEY_CREATED_AT                  + " INTEGER " + ")";


    //****************************************************/
    //****    Dose Table                ******************/
    //****************************************************/
    //Table Name
    static final String TABLE_DOSE            = "Dose";

    //Dose Column Names
    static final String DOSE_ID               = "dose_id";
    static final String DOSE_OF_MEDICATION_ID = "dose_of_med_id";
    static final String DOSE_FOR_PERSON_ID    = "dose_for_person_id";
    static final String DOSE_CONTAINED_IN_CONCURRENT_DOSE
                                                     = "dose_contained_in_concurrent_dose";
    static final String DOSE_POSITION_WITHIN_CONCURRENT_DOSE
                                                     = "dose_position";
    static final String DOSE_TIME_TAKEN       = "dose_time_taken";
    static final String DOSE_AMOUNT_TAKEN     = "dose_amount_taken";




    //create dose table
    private static final String CREATE_TABLE_DOSE = "CREATE TABLE " + TABLE_DOSE +"(" +
            KEY_ID                            + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            DOSE_ID                           + " INTEGER, " +
            DOSE_OF_MEDICATION_ID             + " INTEGER, " +
            DOSE_FOR_PERSON_ID                + " INTEGER, " +
            DOSE_CONTAINED_IN_CONCURRENT_DOSE + " INTEGER, " +
            DOSE_POSITION_WITHIN_CONCURRENT_DOSE
                                              + " INTEGER, " +
            DOSE_TIME_TAKEN                   + " INTEGER, " +
            DOSE_AMOUNT_TAKEN                 + " INTEGER, " +
            KEY_CREATED_AT                    + " INTEGER"   + ")";



    //****************************************************/
    //****    Schedule Medication Table    ***************/
    //****************************************************/
    //Table Name
    static final String TABLE_SCHED_MED            = "SchedMed";

    //Dose Column Names
    static final String SCHED_MED_ID               = "sched_med_id";
    static final String SCHED_MED_OF_MEDICATION_ID = "sched_med_of_med_id";
    static final String SCHED_MED_FOR_PERSON_ID    = "sched_med_for_person_id";
    static final String SCHED_MED_TIME_DUE         = "sched_med_time_due";
    static final String SCHED_MED_STRATEGY         = "sched_med_strategy";


    //create sched_med table
    private static final String CREATE_TABLE_SCHED_MED = "CREATE TABLE " + TABLE_SCHED_MED +"(" +
            KEY_ID                      + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            SCHED_MED_ID                + " INTEGER, " +
            SCHED_MED_OF_MEDICATION_ID  + " INTEGER, " +
            SCHED_MED_FOR_PERSON_ID     + " INTEGER, " +
            SCHED_MED_TIME_DUE          + " INTEGER, " +
            SCHED_MED_STRATEGY          + " INTEGER, " +
            KEY_CREATED_AT              + " INTEGER"   + ")";


    //****************************************************/
    //****************************************************/
    //****************************************************/
    private Context mContext;



    //****************************************************/
    //******  Constructor               ******************/
    //****************************************************/

    //This should be called with the APPLICATION context
    MMDataBaseSqlHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);

        this.mContext = context;
    }

    //****************************************************/
    //******  Lifecycle Methods         ******************/
    //****************************************************/

    /*****************
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
        db.execSQL(CREATE_TABLE_MEDICATION_ALERT);
        db.execSQL(CREATE_TABLE_CONCURRENT_DOSE);
        db.execSQL(CREATE_TABLE_DOSE);
        db.execSQL(CREATE_TABLE_SCHED_MED);
    }

    /*****************
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
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MEDICATION_ALERT);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DOSE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CONCURRENT_DOSE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SCHED_MED);

        //Create new tables
        onCreate(db);
    }

    @Override
    public void onOpen(SQLiteDatabase db){
        super.onOpen(db);
    }





    //***********************************************/
    /*         Generic CRUD routines                */
    /* The same routine will do for all data types  */
    //***********************************************/


    ///***************************** Create ****************************

    /***************************************
     * Add is actually a complex function.
     * Add checks whether the object already exists within the DB.
     * If it does, the row is updated with the new values.
     * Else, the object is inserted into the DB, returning the new ID
     *   (which is unique within the table).
     *   The function then re-updates the row with the new ID.
     *
     * @param db            - The database to be updated
     * @param table         - The table within the database
     * @param values        - A content values structure describing the row
     * @param where_clause  - Uniquely describes the row to be updated
     * @param id_key        - The key of the ID column within the row
     * @return              - The databaseID of the object added/updated.
     *                        sDB_ERROR_CODE if an error occurred
     */
    long add( SQLiteDatabase db,
                        String         table,
                        ContentValues  values,          //Column names and new values
                        String         where_clause,
                        String         id_key){//null updates all rows

        long returnCode = 0;
        long returnKey = 0;

        long id_value = (long) values.get(id_key);
        if (id_value == MMUtilities.ID_DOES_NOT_EXIST){
            //Add it to the DB
            //need to insert
            returnCode = db.insert(table, null, values);
            if (returnCode == sDB_ERROR_CODE)return sDB_ERROR_CODE;

            //get ready to update the DB row with the new ID
            values.put(id_key, returnCode);

            //get ready to pass back the new ID
            returnKey = returnCode;

            returnCode = db.update(table, values, where_clause, null);
        } else {
            //Update the existing DB row

            //get ready to pass back the instance ID
            returnKey = (long) values.get(id_key);

            //update the row in the DB
            returnCode = db.update(table, values, where_clause, null);
        }

        //db.close(); //never close the db instance. Just leave the connection open

        if (returnCode == sDB_ERROR_CODE)return sDB_ERROR_CODE;

        //return the instance/row ID
        return returnKey;
    }

    //**************************** READ *******************************
    Cursor getObject(SQLiteDatabase db,
                            String   table,
                            String[] columns,
                            String   where_clause,
                            String[] selectionArgs,
                            String   groupBy,
                            String   having,
                            String   orderBy){
        /* ******************************
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
    //use add, it attempts an insert. If that fails, it tries an update


    //***************** DELETE ***************************************
    //returns the number of rows affected
    int remove (SQLiteDatabase  db,
                       String         table,
                       String         where_clause,//null updates all rows
                       String[]       where_args ){ //values that replace ? in where clause

        return (delete(db, table, where_clause, where_args));
    }


    //returns the number of rows affected
    int delete (SQLiteDatabase  db,
                       String         table,
                       String         where_clause,//null updates all rows
                       String[]       where_args ){ //values that replace ? in where clause

        return (db.delete(table, where_clause, where_args));
    }


    //***********************************************/
    /*      Object Specific CRUD routines           */
    /*     Each Class has it's own routine          */
    //***********************************************/

    //********************* PERSON ****************************************************88




}
