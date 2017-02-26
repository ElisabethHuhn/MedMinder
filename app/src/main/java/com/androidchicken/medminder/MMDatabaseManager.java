package com.androidchicken.medminder;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;

import static com.androidchicken.medminder.MMDataBaseSqlHelper.TABLE_CONCURRENT_DOSE;
import static com.androidchicken.medminder.MMDataBaseSqlHelper.TABLE_DOSE;
import static com.androidchicken.medminder.MMDataBaseSqlHelper.TABLE_MEDICATION;
import static com.androidchicken.medminder.MMDataBaseSqlHelper.TABLE_PERSON;
import static com.androidchicken.medminder.MMDataBaseSqlHelper.TABLE_SCHED_MED;


/**
 * Created by Elisabeth Huhn on 5/18/2016.
 * This manager hides the CRUD routines of the DB.
 * Originally this is a pass through layer, but if background threads are required to get
 * IO off the UI thread, this manager will maintain them.
 * This manager is a singleton that holds the one connection to the DB for the app.
 * This connection is opened when the app is first initialized, and never closed
 */
public class MMDatabaseManager {

    private static final String TAG = "MMDatabaseManager";
    public static final long   sDB_ERROR_CODE = -1;


    /************************************************/
    /*         static variables                     */
    /************************************************/

    private static MMDatabaseManager  sManagerInstance ;

    private static String sNoContextException = "Can not create database without a context";
    private static String sNotInitializedException =
            "Attempt to access the database before it has been initialized";
    private static String sNotOpenedException =
            "Attempt to access the database before it has been opened";

    /************************************************/
    /*         Instance variables                   */
    /************************************************/
    private MMDataBaseSqlHelper mDatabaseHelper;
    private        SQLiteDatabase     mDatabase;


    /************************************************/
    /*         setters & getters                    */
    /************************************************/

    //mDatabaseHelper
    public void setDatabaseHelper(MMDataBaseSqlHelper mDatabaseHelper) {
        this.mDatabaseHelper = mDatabaseHelper;
    }
    //Return null if the field has not yet been initialized
    public synchronized MMDataBaseSqlHelper getDatabaseHelper()  {
        return mDatabaseHelper;
    }


    //mDatabase
    public    void   setDatabase(SQLiteDatabase mDatabase) {this.mDatabase = mDatabase; }
    //return null if the field has not yet been initialized
    public synchronized SQLiteDatabase getDatabase()       { return mDatabase; }

    /************************************************/
    /*         constructor                          */
    /************************************************/

    //null constructor. It should never be called. But you have to have one
    //    initializeInstance() is the proper protocol
    private MMDatabaseManager() {}



    /************************************************/
    /*         static methods                       */
    /************************************************/


    /**********************
     * This method initializes the singleton Database Manager
     *
     * The database Manager holds onto a single instance of the helper connection
     *    to the database.
     *
     * The purpose of a singleton connection is to keep the app threadsafe
     *    in the case of attempted concurrent access to the database.
     *
     * There can be no concurrent access to the database from multiple threads,
     *    as there is only one connection, it can be accessed serially,
     *    from only one thread at a time.
     *
     * Thie lifetime of this singleton is the execution lifetime of the App,
     *    thus the application context is passed, not the activity context
     *
     * synchronized method to ensure only 1 instance exists
     *
     * @param context               The application context
     * @throws RuntimeException     Thrown if there is no context passed
     *
     * USAGE
     * MMDatabaseManager.initializeInstance(getApplicationContext());
     */
    public static synchronized MMDatabaseManager initializeInstance(Context context) throws RuntimeException {
        if (sManagerInstance == null){
            try {
                //Note the hard coded strings here.
                // todo: figure out how to access the string resources from the DatabaseManager
                if (context == null) throw new RuntimeException(sNoContextException);
                //create the singleton Database Manager
                sManagerInstance = new MMDatabaseManager();

                //create and store it's singleton connection to the database
                //The helper is the database connection
                //It's a singleton to keep the app thread safe
                sManagerInstance.setDatabaseHelper(new MMDataBaseSqlHelper(context));

                //Now that we have the connection, create the database as well
                sManagerInstance.setDatabase (sManagerInstance.getDatabaseHelper().getWritableDatabase());
            }catch (Exception e) {
                Log.e(TAG, Log.getStackTraceString(e));
            }

        } else if (sManagerInstance.getDatabaseHelper() == null){
            //if here, we had a Database Manager, but no connection to a database
            //Something is definitely fishy, but maybe we can recover
            if (context == null) throw new RuntimeException(sNoContextException);

            //all the constructor does is save the context
            sManagerInstance.setDatabaseHelper( new MMDataBaseSqlHelper(context));
            //opening the database will create the tables if they do not
            //  already exist. It will also force an upgrade if the system
            //  detects a new version of the DB since the last time it was opened
            sManagerInstance.setDatabase(sManagerInstance.getDatabaseHelper().getWritableDatabase());

        } else if (sManagerInstance.getDatabase() == null){
            //again, if we had a database manager, and a database helper/connection
            // we certainly should have had an instance of the database
            //something is fishy, but attempt recovery
            sManagerInstance.setDatabase(sManagerInstance.getDatabaseHelper().getWritableDatabase());
        }

        return sManagerInstance;
    }


    //returns null if the Database Manager has not yet bee initialized
    //in that case, initializeInstance() must be called first
    // We can't just fix the problem, as we need the application context
    //because this is an error condition. Treat it as an error

    public static synchronized MMDatabaseManager getInstance() throws RuntimeException {
        //The reason we can't just initialize it now is because we need a context to initialize
        if (sManagerInstance == null)  {
            throw new RuntimeException(sNotInitializedException);
        }
        return sManagerInstance;
    }


    //But if we do happen to have a context, we can initialize
    public static synchronized MMDatabaseManager getInstance(Context context) throws RuntimeException {
        //The reason we can't just initialize it now is because we need a context to initialize
        if (sManagerInstance == null)  {
            if (context == null) throw new RuntimeException(sNotInitializedException);
            MMDatabaseManager.initializeInstance(context);
        }
        return sManagerInstance;
    }


    /************************************************/
    /*         Instance methods                     */
    /************************************************/
    //The CRUD routines:


    /************************************************/
    /*        Person CRUD methods                   */
    /************************************************/


    //******************************    COUNT    ***********************
    //Get count of persons
    //// TODO: 11/1/2016 write this routine if it is needed
    //public int getPersonCount() {}

    //******************************    Create    ***********************
    public boolean addPerson(MMPerson person){
        long returnCode = 0;
        MMPersonManager personManager = MMPersonManager.getInstance();
        returnCode = mDatabaseHelper.add(mDatabase,
                                         TABLE_PERSON,
                                         null,
                                         personManager.getCVFromPerson(person));
        if (returnCode == sDB_ERROR_CODE)return false;
        return true;
    }


    //***********************  Read **********************************

    public Cursor getAllPersonsCursor(){
        Cursor cursor = mDatabaseHelper.getObject(  mDatabase,
                                                    TABLE_PERSON,
                                                    null,    //get the whole object
                                                    null,
                                                    null, null, null, null);
        return cursor;
    }


    //Reads the Persons into memory
    //Returns the number of persons read in
    public int getAllPersons(){
        Cursor cursor = mDatabaseHelper.getObject(  mDatabase,
                                                    TABLE_PERSON,
                                                    null,    //get the whole person
                                                    null,    //get all persons.
                                                    null, null, null, null);

        //get the person row from the DB
        /********************************
         Cursor query (String table, //Table Name
         String[] columns,   //Columns to return, null for all columns
         String where_clause,
         String[] selectionArgs, //replaces ? in the where_clause with these arguments
         String groupBy, //null meanas no grouping
         String having,   //row grouping
         String orderBy)  //null means the default sort order
         *********************************/

        //create a person object from the Cursor object
        MMPersonManager personManager = MMPersonManager.getInstance();

        int position = 0;
        int last = cursor.getCount();
        MMPerson person;
        while (position < last) {
            person = personManager.getPersonFromCursor(cursor, position);
            if (person != null) {
                personManager.addFromDB(person);
            }
            position++;
        }
        cursor.close();
        return last;

    }

    //NOTE this routine does NOT add the person to the RAM list maintained by PersonManager
    public MMPerson getPerson(int personID){

        //get the person row from the DB
        Cursor cursor = mDatabaseHelper.getObject(
                                                mDatabase,     //the db to access
                                                TABLE_PERSON,  //table name
                                                null,          //get the whole person
                                                getPersonWhereClause(personID), //where clause
                                                null, null, null, null);//args, group, row grouping, order


/********************************
        Cursor query (String table, //Table Name
                String[] columns,   //Columns to return, null for all columns
                String where_clause,
                String[] selectionArgs, //replaces ? in the where_clause with these arguments
                String groupBy, //null meanas no grouping
                String having,   //row grouping
                String orderBy)  //null means the default sort order
*********************************/

        //create a person object from the Cursor object
        MMPersonManager personManager = MMPersonManager.getInstance();
        return personManager.getPersonFromCursor(cursor, 0);//get the first row in the cursor

    }



    //********************************    Update   *************************


    public  int updatePerson(MMPerson person) {
        MMPersonManager personManager = MMPersonManager.getInstance();
        int             personID      = person.getPersonID();

        return mDatabaseHelper.update(
                mDatabase,
                TABLE_PERSON,
                personManager.getCVFromPerson(person),
                getPersonWhereClause(personID),
                null);  //values that replace ? in where clause

    }

    //*********************************     Delete    ***************************
    //The return code indicates how many rows affected
    public int removePerson(int personID){

        return mDatabaseHelper.remove(  mDatabase,
                                        TABLE_PERSON,
                                        getPersonWhereClause(personID),
                                        null);  //values that replace ? in where clause
    }


    /************************************************/
    /*        Person specific CRUD  utility         */
    /************************************************/
    private String getPersonWhereClause(int personID){
        return MMDataBaseSqlHelper.PERSON_ID + " = " + String.valueOf(personID);
    }






    /************************************************/
    /*        Medication CRUD methods               */
    /************************************************/


    //Get count of medications
    //// TODO: 11/1/2016 write this routine if it is needed
    //public int getMedicationCount() {}

    //******************************    Create    ***********************
    public boolean addMedication(MMMedication medication){
        long returnCode = 0;

        MMMedicationManager medicationManager = MMMedicationManager.getInstance();
        returnCode = mDatabaseHelper.add(mDatabase,
                                        TABLE_MEDICATION,
                                        null,
                                        medicationManager.getCVFromMedication(medication));
        if (returnCode == sDB_ERROR_CODE)return false;

        ArrayList<MMScheduleMedication> schedules = medication.getSchedules();

        int last = schedules.size();
        int position = 0;
        while (position < last){
            MMScheduleMedication scheduleMed  = (MMScheduleMedication)schedules.get(position);
            if (!addSchedMed(scheduleMed))return false;
            position++;
        }

        return true;
    }


    //***********************  Read **********************************

    public Cursor getAllMedicationsCursor(int personID){
        Cursor cursor = mDatabaseHelper.getObject(  mDatabase,
                                                    TABLE_MEDICATION,
                                                    null,    //get the whole object
                                                    getMedicationWhereClause(personID),
                                                    null, null, null, null);
        return cursor;
    }

    //Reads the Medications linked to this person into memory
    //Returns the number of medications read in
    public int getAllMedications(int personID){
        if (personID == 0) return 0;

        Cursor cursor = getAllMedicationsCursor(personID);

        //create a medication object from the Cursor object
        MMMedicationManager medicationManager = MMMedicationManager.getInstance();

        int position = 0;
        int last = cursor.getCount();
        MMMedication medication;
        while (position < last) {
            //translate the cursor into a medication object
            medication = medicationManager.getMedicationFromCursor(cursor, position);
            if (medication != null) {
                //add the medication object to the medication manager's list (which is on the person)
                if (!medicationManager.addFromDB(medication)) {
                    throw new RuntimeException("Can't add medication from DB");
                }
            }
            position++;
        }
        cursor.close();
        return last;

    }

    //NOTE this routine does NOT add the medication to the Person where
    public MMMedication getMedication(int medicationID, int personID){

        //get the medication row from the DB
        Cursor cursor = mDatabaseHelper.getObject(
                mDatabase,     //the db to access
                TABLE_MEDICATION,  //table name
                null,          //get the whole medication
                getMedicationWhereClause(medicationID, personID), //where clause
                null, null, null, null);//args, group, row grouping, order


/********************************
 Cursor query (String table, //Table Name
 String[] columns,   //Columns to return, null for all columns
 String where_clause,
 String[] selectionArgs, //replaces ? in the where_clause with these arguments
 String groupBy, //null meanas no grouping
 String having,   //row grouping
 String orderBy)  //null means the default sort order
 *********************************/

        //create a medication object from the Cursor object
        MMMedicationManager medicationManager = MMMedicationManager.getInstance();
        return medicationManager.getMedicationFromCursor(cursor, 0);//get the first row in the cursor
    }


    //NOTE this routine does NOT add the medication to the Person where
    public MMMedication getMedication(int medicationID){

        //get the medication row from the DB
        Cursor cursor = mDatabaseHelper.getObject(
                mDatabase,     //the db to access
                TABLE_MEDICATION,  //table name
                null,          //get the whole medication
                getMedicationWhereClause(medicationID), //where clause
                null, null, null, null);//args, group, row grouping, order

        //create a medication object from the Cursor object
        MMMedicationManager medicationManager = MMMedicationManager.getInstance();
        return medicationManager.getMedicationFromCursor(cursor, 0);//get the first row in the cursor
    }


    //********************************    Update   *************************

    public  int updateMedication(MMMedication medication) {

        MMMedicationManager medicationManager = MMMedicationManager.getInstance();

        return mDatabaseHelper.update(
                mDatabase,
                TABLE_MEDICATION,
                medicationManager.getCVFromMedication(medication),
                getMedicationWhereClause(medication.getMedicationID(), medication.getForPersonID()),
                null);  //values that replace ? in where clause
    }



    //*********************************     Delete    ***************************
    //The return code indicates how many rows affected
    public int removeMedication(int medicationID){

        return mDatabaseHelper.remove(  mDatabase,
                                        TABLE_MEDICATION,
                                        getMedicationWhereClause(medicationID),
                                        null);  //values that replace ? in where clause
    }


    /************************************************/
    /*        Medication specific CRUD  utility         */
    /************************************************/
    //This gets a single medication
    private String getMedicationWhereClause(int medicationID, int personID){

        return  MMDataBaseSqlHelper.MEDICATION_ID + " = '" +
                String.valueOf(medicationID) +"' AND " +
                MMDataBaseSqlHelper.MEDICATION_FOR_PERSON_ID + " = '" +
                String.valueOf(personID) + "'";
    }

    //This gets all medications linked to this person
    private String getMedicationWhereClause(int personID){

        return MMDataBaseSqlHelper.MEDICATION_FOR_PERSON_ID + " = '" + String.valueOf(personID) + "'";
    }



    /************************************************/
    /*        Dose CRUD methods                     */
    /************************************************/


    //CRUD routines for a Dose


    public ArrayList<MMDose> getDosesForCD(MMConcurrentDose concurrentDose){

        ArrayList<MMDose> doses = new ArrayList<>();

        //get the dose row from the DB
        Cursor cursor = mDatabaseHelper.getObject(
                mDatabase,     //the db to access
                TABLE_DOSE,   //table name
                null,          //get the whole dose
                getDosesWhereClause(concurrentDose.getConcurrentDoseID()), //where clause
                null, null, null, null);//args, group, row grouping, order

        //create Dose objects from the Cursor object
        MMDoseManager doseManager = MMDoseManager.getInstance();

        int position = 0;
        int last = cursor.getCount();
        MMDose dose;

        while (position < last) {
            //translate the cursor into a dose object
            dose = doseManager.getDoseFromCursor(cursor, position);
            if (dose != null) {
                //add the dose object to the list
                doses.add(dose);
            }
            position++;
        }
        cursor.close();
        return doses;
    }


    public boolean addDose(MMDose dose){
        long returnCode = 0;
        MMDoseManager doseManager = MMDoseManager.getInstance();
        returnCode = mDatabaseHelper.add(mDatabase, TABLE_DOSE, null, doseManager.getCVFromDose(dose));
        if (returnCode == sDB_ERROR_CODE)return false;
        return true;
    }


    /************************************************/
    /*        Dose specific CRUD  utility         */
    /************************************************/
    //This gets a single medication
    private String getDosesWhereClause(int concurrentDoseID){
        return MMDataBaseSqlHelper.DOSE_CONTAINED_IN_CONCURRENT_DOSE + " = '" +
                                        String.valueOf(concurrentDoseID) + "'";
    }


    /************************************************/
    /*        Concurrent Dose CRUD methods          */
    /************************************************/

    //CRUD routines for a ConcurrentDose

    public Cursor getAllConcurrentDosesCursor(int personID){
        Cursor cursor = mDatabaseHelper.getObject(  mDatabase,
                                                    TABLE_CONCURRENT_DOSE,
                                                    null,    //get the whole object
                                                    getConcurrentDosesWhereClause(personID),
                                                    null, null, null, null);

        return cursor;
    }


    public boolean addConcurrentDose(MMConcurrentDose concurrentDose){
        long returnCode = 0;
        MMConcurrentDoseManager concurrentDoseManager = MMConcurrentDoseManager.getInstance();
        mDatabaseHelper.add(mDatabase,
                            TABLE_CONCURRENT_DOSE,
                            null,
                            concurrentDoseManager.getCVFromConcurrentDose(concurrentDose));
        if (returnCode == sDB_ERROR_CODE)return false;
        return true;
    }


    //The return code indicates how many rows affected
    public int removeConcurrentDose(int concurrentDoseID){

        return mDatabaseHelper.remove(  mDatabase,
                TABLE_CONCURRENT_DOSE,
                getConcurrentDosesIDWhereClause(concurrentDoseID),
                null);  //values that replace ? in where clause
    }



    /************************************************/
    /*    Concurrent Dose specific CRUD  utility    */
    /************************************************/

    private String getConcurrentDosesWhereClause(int personID){
        return MMDataBaseSqlHelper.CONCURRENT_DOSE_FOR_PERSON_ID + " = '" +
                                                                    String.valueOf(personID) + "'";
    }


    private String getConcurrentDosesIDWhereClause(int concurrentDoseID){
        return MMDataBaseSqlHelper.CONCURRENT_DOSE_ID + " = '" +
                                                            String.valueOf(concurrentDoseID) + "'";
    }


    /************************************************/
    /*     Schedule Medications CRUD methods        */
    /************************************************/

    //CRUD routines for a Schedule Medication

    public Cursor getAllSchedMedsCursor(int medicationID){
        Cursor cursor = mDatabaseHelper.getObject(  mDatabase,
                                                    TABLE_SCHED_MED,
                                                    null,    //get the whole object
                                                    getSchedMedWhereClause(medicationID),
                                                    null, null, null, null);

        return cursor;

    }

    public ArrayList<MMScheduleMedication> getTimesForSM(int medicationID){
        ArrayList<MMScheduleMedication> times = new ArrayList<>();

        Cursor cursor = getAllSchedMedsCursor(medicationID);
        if (cursor != null) {
            MMScheduleMedication scheduleMedication;
            MMSchedMedManager schedMedManager = MMSchedMedManager.getInstance();
            int last = cursor.getCount();
            int position = 0;
            while (position < last){
                scheduleMedication = schedMedManager.getScheduleMedicationFromCursor(cursor, position);
                times.add(scheduleMedication);
                position++;
            }
        }
        return times;
    }



    public boolean addSchedMed(MMScheduleMedication schedMed){
        long returnCode = 0;
        MMSchedMedManager schedMedManager = MMSchedMedManager.getInstance();
        mDatabaseHelper.add(mDatabase,
                            TABLE_SCHED_MED,
                            null,
                            schedMedManager.getCVFromScheduleMedication(schedMed));
        if (returnCode == sDB_ERROR_CODE)return false;
        return true;
    }



    public boolean updateSchedMed(MMScheduleMedication schedMed){
        long returnCode = 0;
        MMSchedMedManager schedMedManager = MMSchedMedManager.getInstance();
        returnCode = mDatabaseHelper.update(mDatabase,
                                            TABLE_SCHED_MED,
                                            schedMedManager.getCVFromScheduleMedication(schedMed),
                                            getSchedMedIDWhereClause(schedMed.getSchedMedID()),
                                            null);
        if (returnCode == sDB_ERROR_CODE)return false;
        return true;
    }



    //The return code indicates how many rows affected
    public int removeSchedMed(int schedMedID){
        return mDatabaseHelper.remove(  mDatabase,
                                        TABLE_SCHED_MED,
                                        getSchedMedIDWhereClause(schedMedID),
                                        null);  //values that replace ? in where clause
    }



    /************************************************/
    /*    Concurrent Dose specific CRUD  utility    */
    /************************************************/

    private String getSchedMedWhereClause(int medicationID){
        return MMDataBaseSqlHelper.SCHED_MED_OF_MEDICATION_ID + " = '" +
                                                                String.valueOf(medicationID) + "'";
    }


    private String getSchedMedIDWhereClause(int schedMedID){
        return MMDataBaseSqlHelper.SCHED_MED_ID + " = '" + String.valueOf(schedMedID) + "'";
    }

    /************************************************/
    /*         Static inner classes                 */
    /************************************************/



    /************************************************/
    /*         inner classes                        */
    /************************************************/







}
