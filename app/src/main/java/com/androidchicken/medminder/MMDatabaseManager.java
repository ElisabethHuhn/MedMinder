package com.androidchicken.medminder;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;

import static com.androidchicken.medminder.MMDataBaseSqlHelper.TABLE_CONCURRENT_DOSE;
import static com.androidchicken.medminder.MMDataBaseSqlHelper.TABLE_DOSE;
import static com.androidchicken.medminder.MMDataBaseSqlHelper.TABLE_MEDICATION;
import static com.androidchicken.medminder.MMDataBaseSqlHelper.TABLE_MEDICATION_ALERT;
import static com.androidchicken.medminder.MMDataBaseSqlHelper.TABLE_PERSON;
import static com.androidchicken.medminder.MMDataBaseSqlHelper.TABLE_SCHEDULE;


/**
 * Created by Elisabeth Huhn on 5/18/2016.
 * This manager hides the CRUD routines of the DB.
 * Originally this is a pass through layer, but if background threads are required to get
 * IO off the UI thread, this manager will maintain them.
 * This manager is a singleton that holds the one connection to the DB for the app.
 * This connection is opened when the app is first initialized, and never closed
 */
class MMDatabaseManager {

    private static final String TAG = "MMDatabaseManager";
    static final long   sDB_ERROR_CODE = -1;


    //***********************************************/
    /*         static variables                     */
    //***********************************************/

    private static MMDatabaseManager  sManagerInstance ;

    private static String sNoContextException = "Can not create database without a context";
    private static String sNotInitializedException =
            "Attempt to access the database before it has been initialized";


    //***********************************************/
    /*         Instance variables                   */
    //***********************************************/
    private MMDataBaseSqlHelper mDatabaseHelper;
    private        SQLiteDatabase     mDatabase;


    //***********************************************/
    /*         static methods                       */
    //***********************************************/


    /*********************
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
    private static synchronized MMDatabaseManager initializeInstance(Context context) throws RuntimeException {
        if (sManagerInstance == null){
            try {
                //create the singleton Database Manager
                sManagerInstance = new MMDatabaseManager();

            } catch (Exception e) {
                Log.e(TAG, Log.getStackTraceString(e));
            }
        }

        if (sManagerInstance.getDatabaseHelper() == null){

            //Note the hard coded strings here.
            // todo: figure out how to access the string resources from the DatabaseManager without a context
            if (context == null) throw new RuntimeException(sNoContextException);

            try{
                //all the constructor does is save the context
                sManagerInstance.setDatabaseHelper( new MMDataBaseSqlHelper(context));
            } catch (Exception e) {
                Log.e(TAG, Log.getStackTraceString(e));
            }
        }

        if (sManagerInstance.getDatabase() == null){

            if (context == null) throw new RuntimeException(sNoContextException);

            try{
                sManagerInstance.setDatabase(sManagerInstance.getDatabaseHelper().getWritableDatabase());
            } catch (Exception e) {
                Log.e(TAG, Log.getStackTraceString(e));
            }
        }

        return sManagerInstance;
    }


    //returns null if the Database Manager has not yet bee initialized
    //in that case, initializeInstance() must be called first
    // We can't just fix the problem, as we need the application context
    //because this is an error condition. Treat it as an error

    static synchronized MMDatabaseManager getInstance() throws RuntimeException {
        //The reason we can't just initialize it now is because we need a context to initialize
        if (sManagerInstance == null)  {
            throw new RuntimeException(sNotInitializedException);
        }
        return sManagerInstance;
    }


    //But if we do happen to have a context, we can initialize
    static synchronized MMDatabaseManager getInstance(Context context) throws RuntimeException {
        //The reason we can't just initialize it now is because we need a context to initialize
        if (sManagerInstance == null)  {
            if (context == null) throw new RuntimeException(sNotInitializedException);
            MMDatabaseManager.initializeInstance(context);
        }
        return sManagerInstance;
    }


    //***********************************************/
    /*         constructor                          */
    //***********************************************/

    //null constructor. It should never be called. But you have to have one
    //    initializeInstance() is the proper protocol
    private MMDatabaseManager() {}



    //***********************************************/
    /*         setters & getters                    */
    //***********************************************/

    //mDatabaseHelper
    private void setDatabaseHelper(MMDataBaseSqlHelper mDatabaseHelper) {
        this.mDatabaseHelper = mDatabaseHelper;
    }
    //Return null if the field has not yet been initialized
    private synchronized MMDataBaseSqlHelper getDatabaseHelper()  {
        return mDatabaseHelper;
    }


    //mDatabase
       void   setDatabase(SQLiteDatabase mDatabase) {this.mDatabase = mDatabase; }
    //return null if the field has not yet been initialized
    synchronized SQLiteDatabase getDatabase()       { return mDatabase; }


    //***********************************************/
    /*         Instance methods                     */
    //***********************************************/
    //The CRUD routines:


    //***********************************************/
    /*        Person CRUD methods                   */
    //***********************************************/

    ///*****************************    COUNT    ***********************
    //Get count of persons
    //int getPersonCount() {}

    ///*****************************    Create    ***********************
    long addPerson(MMPerson person){

        long returnCode = sDB_ERROR_CODE;
        MMPersonManager personManager = MMPersonManager.getInstance();
        returnCode =  mDatabaseHelper.add(mDatabase,
                                         TABLE_PERSON,
                                         personManager.getCVFromPerson(person),
                                         getPersonWhereClause(person.getPersonID()),
                                         MMDataBaseSqlHelper.PERSON_ID);
        if (returnCode == sDB_ERROR_CODE)return returnCode;
        person.setPersonID(returnCode);
        return returnCode;
    }


    ///**********************  Read **********************************

    Cursor getAllPersonsCursor(boolean currentOnly){
        String whereClause;
        if (currentOnly) {
            whereClause = getCurrentPersonWhereClause();
        } else {
            whereClause = null;
        }
        return mDatabaseHelper.getObject(  mDatabase,
                                        TABLE_PERSON,
                                        null,    //get the whole object
                                        whereClause,
                                        null, null, null, null);

    }


    //Reads the Persons into memory
    //Returns the number of persons read in
    ArrayList<MMPerson>  getAllPersons(boolean currentOnly){

        Cursor cursor = getAllPersonsCursor(currentOnly);

       //convert the cursor into a list of Person instances

        //create a person object from the Cursor object
        MMPersonManager personManager = MMPersonManager.getInstance();

        int position = 0;
        int last = cursor.getCount();
        MMPerson person;
        ArrayList<MMPerson> persons = new ArrayList<>();

        while (position < last) {
            person = personManager.getPersonFromCursor(cursor, position);
            if (person != null) {
                persons.add(person);
            }
            position++;
        }
        cursor.close();
        return persons;
    }

    //NOTE this routine does NOT add the person to the RAM list maintained by PersonManager
    MMPerson getPerson(long personID){

        //get the person row from the DB
        Cursor cursor = mDatabaseHelper.getObject(
                                                mDatabase,     //the db to access
                                                TABLE_PERSON,  //table name
                                                null,          //get the whole person
                                                getPersonWhereClause(personID), //where clause
                                                null, null, null, null);//args, group, row grouping, order

        //create a person object from the Cursor object
        MMPersonManager personManager = MMPersonManager.getInstance();
        int row = 0; //get the first row in the cursor
        return personManager.getPersonFromCursor(cursor, row);

    }



    //********************************    Update   *************************


    //*********************************     Delete    ***************************


    //***********************************************/
    /*        Person specific CRUD  utility         */
    //***********************************************/
    private String getPersonWhereClause(long personID){
        return MMDataBaseSqlHelper.PERSON_ID + " = " + String.valueOf(personID);
    }

    private String getCurrentPersonWhereClause(){
        return MMDataBaseSqlHelper.PERSON_EXISTS + " = 1" ;
    }



    //***********************************************/
    /*        Medication CRUD methods               */
    //***********************************************/


    //Get count of medications

    //int getMedicationCount() {}

    //******************************    Create    ***********************
    long addMedication(MMMedication medication){
        long returnCode = sDB_ERROR_CODE;

        //first add/update the medication
        MMMedicationManager medicationManager = MMMedicationManager.getInstance();
        returnCode = mDatabaseHelper.add(mDatabase,
                                        TABLE_MEDICATION,
                                        medicationManager.getCVFromMedication(medication),
                                        getMedicationIDWhereClause(medication.getMedicationID()),
                                        MMDataBaseSqlHelper.MEDICATION_ID);
        if (returnCode == sDB_ERROR_CODE)return returnCode;
        medication.setMedicationID(returnCode);

        //only need to update the DB with schedules if there are some in memory
        //we need this check first, as the medication.getSchedules()
        //  reads any DB objects into memory.
        //  We don't need to read them in then write them back out immediately
        if (medication.isSchedulesChanged()) {
            ArrayList<MMSchedule> schedules = medication.getSchedules();

            int last = schedules.size();
            int position = 0;
            while (position < last) {
                MMSchedule schedule =  schedules.get(position);
                returnCode = addSchedule(schedule);
                if (returnCode == sDB_ERROR_CODE) return returnCode;
                //schedule.setScheduleID(returnCode);
                position++;
            }
        }

        return returnCode;
    }


    //***********************  Read **********************************

    Cursor getAllMedicationsCursor(long personID, boolean currentOnly){
        String whereClause = getMedicationWhereClause(personID);
        if (currentOnly){
            whereClause = getCurrentMedicationWhereClause(personID);
        }
        return mDatabaseHelper.getObject(mDatabase,
                                        TABLE_MEDICATION,
                                        null,    //get the whole object
                                        whereClause,
                                        null, null, null, null);

    }

    //gets the Medications linked to this person
    ArrayList<MMMedication> getAllMedications(long personID, boolean currentOnly){
        if (personID == 0) return null;

        Cursor cursor = getAllMedicationsCursor(personID, currentOnly);

        //create a medication object from the Cursor object
        MMMedicationManager medicationManager = MMMedicationManager.getInstance();

        int position = 0;
        int last = cursor.getCount();
        MMMedication medication;
        ArrayList<MMMedication> medications = new ArrayList<>();

        while (position < last) {
            //translate the cursor into a medication object
            medication = medicationManager.getMedicationFromCursor(cursor, position);
            if (medication != null) {
                //ignore the currentOnly lint warning as clarity of the condition is more important
                if ((!currentOnly) || (currentOnly && medication.isCurrentlyTaken())) {
                    medications.add(medication);
                }
            }
            position++;
        }
        cursor.close();
        return medications;
    }


    //NOTE this routine does NOT add the medication to the Person where
    MMMedication getMedication(long medicationID){

        //get the medication row from the DB
        Cursor cursor = mDatabaseHelper.getObject(
                                        mDatabase,     //the db to access
                                        TABLE_MEDICATION,  //table name
                                        null,          //get the whole medication
                                        getMedicationIDWhereClause(medicationID), //where clause
                                        null, null, null, null);//args, group, row grouping, order

        //create a medication object from the Cursor object
        MMMedicationManager medicationManager = MMMedicationManager.getInstance();
        int row = 0;//get the first row in the cursor
        return medicationManager.getMedicationFromCursor(cursor, row);
    }


    //********************************    Update   *************************
    //add first attempts an update, if that fails, it attempts an insert
    //so there is no need for an update


    //*********************************     Delete    ***************************


    //***********************************************/
    /*        Medication specific CRUD  utility         */
    //***********************************************/


    //This gets a single medication
    private String getMedicationIDWhereClause(long medicationID){
        return  MMDataBaseSqlHelper.MEDICATION_ID + " = '" + String.valueOf(medicationID) +"'";
    }

    //This gets all medications linked to this person
    private String getMedicationWhereClause(long personID){
        return MMDataBaseSqlHelper.MEDICATION_FOR_PERSON_ID + " = '" + String.valueOf(personID) + "'";
    }

    //This gets all current medications linked to this person
    private String getCurrentMedicationWhereClause(long personID){
        return MMDataBaseSqlHelper.MEDICATION_FOR_PERSON_ID + " = '" +
                String.valueOf(personID) + "' AND " +
                MMDataBaseSqlHelper.MEDICATION_CURRENTLY_TAKEN + " = '1'" ;
    }




    //***********************************************/
    /*        Medication Alert CRUD methods         */
    //***********************************************/


    //******************************    Create    ***********************
    long addMedicationAlert(MMMedicationAlert medicationAlert){
        long returnCode = sDB_ERROR_CODE;

        //first add/update the medicationAlert
        MMMedicationAlertManager medicationAlertManager = MMMedicationAlertManager.getInstance();
        returnCode = mDatabaseHelper.add(mDatabase,
                             TABLE_MEDICATION_ALERT,
                             medicationAlertManager.getCVFromMedicationAlert(medicationAlert),
                             getMedicationAlertIDWhereClause(medicationAlert.getMedicationAlertID()),
                             MMDataBaseSqlHelper.MEDICATION_ALERT_ID);
        if (returnCode == sDB_ERROR_CODE)return returnCode;
        medicationAlert.setMedicationAlertID(returnCode);

        return returnCode;
    }


    //***********************  Read **********************************

    Cursor getAllMedicationAlertsCursor(long personID){
        return mDatabaseHelper.getObject(mDatabase,
                TABLE_MEDICATION_ALERT,
                null,    //get the whole object
                getMedicationAlertWhereClause(personID),
                null, null, null, null);

    }


    private Cursor getAllMedicationAlertsCursor(long personID, long medicationID){
        return mDatabaseHelper.getObject(mDatabase,
                                         TABLE_MEDICATION_ALERT,
                                         null,    //get the whole object
                                         getMedicationAlertWhereClause(personID, medicationID),
                                         null, null, null, null);

    }


    //gets the MedicationAlerts linked to this person
    //if personID is MMUtilities.ID_DOES_NOT_EXIST,
    // then all the MMMedicationAlerts in the DB are returned
    ArrayList<MMMedicationAlert> getMedicationAlerts(long personID){
        Cursor cursor = getAllMedicationAlertsCursor(personID);

        return createMedAlertsFromCursor(cursor);
    }


    //gets the MedicationAlerts linked to this person for this medication
    // if personID     is MMUtilities.ID_DOES_NOT_EXIST, returns null
    // if medicationID is MMUtilities.ID_DOES_NOT_EXIST, returns null
    ArrayList<MMMedicationAlert> getMedicationAlerts(long personID, long medicationID){
        if ((personID     == MMUtilities.ID_DOES_NOT_EXIST) ||
            (medicationID == MMUtilities.ID_DOES_NOT_EXIST)   ){
            return null;
        }
        Cursor cursor = getAllMedicationAlertsCursor(personID, medicationID);

        return createMedAlertsFromCursor(cursor);
    }

    private ArrayList<MMMedicationAlert> createMedAlertsFromCursor(Cursor cursor){
        //create a medicationAlert object from the Cursor row
        MMMedicationAlertManager medicationAlertManager = MMMedicationAlertManager.getInstance();

        int position = 0;
        int last = cursor.getCount();
        MMMedicationAlert medicationAlert;
        ArrayList<MMMedicationAlert> medicationAlerts = new ArrayList<>();

        while (position < last) {
            //translate the cursor into a medicationAlert object
            medicationAlert = medicationAlertManager.getMedicationAlertFromCursor(cursor, position);
            if (medicationAlert != null) {
                medicationAlerts.add(medicationAlert);
            }
            position++;
        }
        cursor.close();
        return medicationAlerts;
    }



    MMMedicationAlert getMedicationAlert(long medicationAlertID){

        //get the medicationAlert row from the DB
        Cursor cursor = mDatabaseHelper.getObject(
                                mDatabase,     //the db to access
                                TABLE_MEDICATION_ALERT,  //table name
                                null,          //get the whole medicationAlert
                                getMedicationAlertIDWhereClause(medicationAlertID), //where clause
                                null, null, null, null);//args, group, row grouping, order

        //create a medicationAlert object from the Cursor object
        MMMedicationAlertManager medicationAlertManager = MMMedicationAlertManager.getInstance();
        int row = 0;//get the first row in the cursor
        return medicationAlertManager.getMedicationAlertFromCursor(cursor, row);
    }


    //********************************    Update   *************************
    //add first attempts an update, if that fails, it attempts an insert
    //so there is no need for an update


    //*********************************     Delete    ***************************
    //The return code indicates how many rows affected
    int removeMedicationAlert(long medicationAlertID){
        return mDatabaseHelper.remove(  mDatabase,
                                        TABLE_MEDICATION_ALERT,
                                        getMedicationAlertIDWhereClause(medicationAlertID),
                                        null);  //values that replace ? in where clause
    }


    //***********************************************/
    /*   MedicationAlert specific CRUD  utility     */
    //***********************************************/

    //This gets a single medicationAlert based on its ID
    private String getMedicationAlertIDWhereClause(long medicationAlertID){
        return  MMDataBaseSqlHelper.MEDICATION_ALERT_ID + " = '" +
                                                            String.valueOf(medicationAlertID) +"'";
    }

    //This gets all medicationAlerts linked to this person
    private String getMedicationAlertWhereClause(long personID){
        if (personID == MMUtilities.ID_DOES_NOT_EXIST) return null;

        return MMDataBaseSqlHelper.MEDICATION_ALERT_FOR_PATIENT_ID + " = '" +
                                                                     String.valueOf(personID) + "'";
    }


    //This gets all medicationAlerts linked to this person
    private String getMedicationAlertWhereClause(long personID, long medicationID){
        if (personID == MMUtilities.ID_DOES_NOT_EXIST) return null;

        return MMDataBaseSqlHelper.MEDICATION_ALERT_FOR_PATIENT_ID + " = '" +
                String.valueOf(personID) + "' AND " +
                MMDataBaseSqlHelper.MEDICATION_ALERT_MEDICATION_ID + " = '" +
                String.valueOf(medicationID) + "'";
    }




    //***********************************************/
    /*        Concurrent Dose CRUD methods          */
    //***********************************************/
    //gets the ConcurrentDoses linked to this person
    Cursor getAllConcurrentDosesCursor(long personID, String orderClause){
        String whereClause = getConcurrentDosesWhereClause(personID) ;
        return mDatabaseHelper.getObject(   mDatabase,
                                            TABLE_CONCURRENT_DOSE,
                                            null,    //get the whole object
                                            whereClause,
                                            null, null, null,
                                            orderClause);   //order by clause
    }

    Cursor getAllConcurrentDosesCursor(long personID, long earliestDate, String orderClause){
        String whereClause = getConcurrentDosesWhereClause(personID) + " AND " +
                getConcurrentDosesHistoryWhereClause(earliestDate);
        return mDatabaseHelper.getObject(   mDatabase,
                                            TABLE_CONCURRENT_DOSE,
                                            null,    //get the whole object
                                            whereClause,
                                            null, null, null,
                                            orderClause);   //order by clause
    }

    Cursor getAllConcurrentDosesCursor(long personID,
                                              long earliestDate,
                                              long latestDate,
                                              String orderClause){
        String whereClause = getConcurrentDosesWhereClause(personID) + " AND " +
                             getConcurrentDosesHistoryWhereClause(earliestDate, latestDate) ;

        return mDatabaseHelper.getObject(   mDatabase,
                                            TABLE_CONCURRENT_DOSE,
                                            null,    //get the whole object
                                            whereClause,
                                            null, null, null,
                                            orderClause);   //order by clause


    }



    long addConcurrentDose(MMConcurrentDose concurrentDose){
        long returnCode = sDB_ERROR_CODE;
        MMConcurrentDoseManager concurrentDoseManager = MMConcurrentDoseManager.getInstance();
        returnCode =  mDatabaseHelper.add(mDatabase,
                            TABLE_CONCURRENT_DOSE,
                            concurrentDoseManager.getCVFromConcurrentDose(concurrentDose),
                            getConcurrentDosesIDWhereClause(concurrentDose.getConcurrentDoseID()),
                            MMDataBaseSqlHelper.CONCURRENT_DOSE_ID);
        if (returnCode == sDB_ERROR_CODE)return returnCode;
        concurrentDose.setConcurrentDoseID(returnCode);
        return returnCode;
    }

    //gets the ConcurrentDoses linked to this person
    Cursor getConcurrentDose(long cDoseID){

        return mDatabaseHelper.getObject(   mDatabase,
                                            TABLE_CONCURRENT_DOSE,
                                            null,    //get the whole object
                                            getConcurrentDosesIDWhereClause(cDoseID),
                                            null, null, null,
                                            null);   //order by clause
    }



    //The return code indicates how many rows affected
    int removeConcurrentDose(long concurrentDoseID){

        return mDatabaseHelper.remove(  mDatabase,
                TABLE_CONCURRENT_DOSE,
                getConcurrentDosesIDWhereClause(concurrentDoseID),
                null);  //values that replace ? in where clause
    }



    //***********************************************/
    /*    Concurrent Dose specific CRUD  utility    */
    //***********************************************/

    private String getConcurrentDosesWhereClause(long personID){
        return MMDataBaseSqlHelper.CONCURRENT_DOSE_FOR_PERSON_ID + " = '" +
                                                                    String.valueOf(personID) + "'";
    }

    private String getConcurrentDosesHistoryWhereClause(long earliestDate){
        return MMDataBaseSqlHelper.CONCURRENT_DOSE_TIME + " > '" +
                String.valueOf(earliestDate) + "'";
    }

    private String getConcurrentDosesHistoryWhereClause(long earliestDate, long latestDate){
        return
        MMDataBaseSqlHelper.CONCURRENT_DOSE_TIME + " >= '" + String.valueOf(earliestDate) + "' AND " +
        MMDataBaseSqlHelper.CONCURRENT_DOSE_TIME + " <= '" + String.valueOf(latestDate)   + "'";
    }

    private String getConcurrentDosesIDWhereClause(long concurrentDoseID){
        return MMDataBaseSqlHelper.CONCURRENT_DOSE_ID + " = '" +
                                                            String.valueOf(concurrentDoseID) + "'";
    }



    //***********************************************/
    /*        Dose CRUD methods                     */
    //***********************************************/

    private Cursor getAllDosesCursor(long concurrentDoseID){
        //get the dose row from the DB
        return mDatabaseHelper.getObject(
                mDatabase,     //the db to access
                TABLE_DOSE,   //table name
                null,          //get the whole dose
                getDosesWhereClause(concurrentDoseID), //where clause
                null, null, null, null);//args, group, row grouping, order
    }

    Cursor getAllDosesCursor(long medicationID, String orderClause){
        //get the dose row from the DB
        return mDatabaseHelper.getObject(
                mDatabase,        //the db to access
                TABLE_DOSE,       //table name
                null,             //get the whole dose
                getDosesMedicationWhereClause(medicationID), //where clause
                null, null, null, //args, group, row grouping,
                orderClause);//column to order result set

    }

    ArrayList<MMDose> getAllDoses(long concurrentDoseID){

        ArrayList<MMDose> doses = new ArrayList<>();

        Cursor cursor = getAllDosesCursor(concurrentDoseID);

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


    long addDose(MMDose dose){
        long returnCode = sDB_ERROR_CODE;
        MMDoseManager doseManager = MMDoseManager.getInstance();
        returnCode = mDatabaseHelper.add(mDatabase,
                                         TABLE_DOSE,
                                         doseManager.getCVFromDose(dose),
                                         getDosesIDWhereClause(dose.getDoseID()),
                                         MMDataBaseSqlHelper.DOSE_ID);
        if (returnCode == sDB_ERROR_CODE)return returnCode;

        dose.setDoseID(returnCode);

        return returnCode;

    }



    //***********************************************/
    /*        Dose specific CRUD  utility         */
    //***********************************************/
    //This gets a single Dose
    private String getDosesWhereClause(long concurrentDoseID){
        return MMDataBaseSqlHelper.DOSE_CONTAINED_IN_CONCURRENT_DOSE + " = '" +
                String.valueOf(concurrentDoseID) + "'";
    }

    //This gets a single Dose
    private String getDosesIDWhereClause(long doseID){
        return MMDataBaseSqlHelper.DOSE_ID + " = '" + String.valueOf(doseID) + "'";
    }

    //This gets all the Doses for a single medication
    private String getDosesMedicationWhereClause(long medicationID){
        return MMDataBaseSqlHelper.DOSE_OF_MEDICATION_ID + " = '" +
                                                                String.valueOf(medicationID) + "'";
    }


    //***********************************************/
    /*     Schedule Medications CRUD methods        */
    //***********************************************/

    //CRUD routines for a Schedule Medication

    //This method is for debug. If you see it, delete it and fix the errors
    Cursor getAllSchedulesCursor(){
        return mDatabaseHelper.getObject(mDatabase,
                                        TABLE_SCHEDULE,
                                        null,    //get the whole object
                                        null,
                                        null, null, null, null);



    }

    Cursor getAllSchedulesCursor(long medicationID){
        return mDatabaseHelper.getObject(  mDatabase,
                                            TABLE_SCHEDULE,
                                            null,    //get the whole object
                                            getScheduleWhereClause(medicationID),
                                            null, null, null, null);
    }

    Cursor getAllSchedulesForPersonCursor(long personID, String orderClause){
        return mDatabaseHelper.getObject(   mDatabase,
                                            TABLE_SCHEDULE,
                                            null,    //get the whole object
                                            getScheduleForPersonWhereClause(personID),
                                            null, null, null, orderClause);

    }

    ArrayList<MMSchedule> getAllSchedules(long medicationID){
        ArrayList<MMSchedule> times = new ArrayList<>();

        Cursor cursor = getAllSchedulesCursor(medicationID);

        if (cursor != null) {
            MMSchedule schedule;
            MMScheduleManager scheduleManager = MMScheduleManager.getInstance();
            int last = cursor.getCount();
            int position = 0;
            while (position < last){
                schedule = scheduleManager.getScheduleFromCursor(cursor, position);
                times.add(schedule);
                position++;
            }
            cursor.close();
        }

        return times;
    }



    long addSchedule(MMSchedule schedule){
        long returnCode = sDB_ERROR_CODE;
        MMScheduleManager schedMedManager = MMScheduleManager.getInstance();
        returnCode = mDatabaseHelper.add( mDatabase,
                                         TABLE_SCHEDULE,
                                         schedMedManager.getCVFromSchedule(schedule),
                                         getSchedMedIDWhereClause(schedule.getScheduleID()),
                                         MMDataBaseSqlHelper.SCHED_MED_ID);
        if (returnCode == sDB_ERROR_CODE)return returnCode;
        schedule.setScheduleID(returnCode);

        return returnCode;
    }



    //The return code indicates how many rows affected
    int removeSchedule(long scheduleID){
        return mDatabaseHelper.remove(  mDatabase,
                TABLE_SCHEDULE,
                                        getSchedMedIDWhereClause(scheduleID),
                                        null);  //values that replace ? in where clause
    }



    //***********************************************/
    /*    Concurrent Dose specific CRUD  utility    */
    //***********************************************/

    private String getScheduleWhereClause(long medicationID){
        return MMDataBaseSqlHelper.SCHED_MED_OF_MEDICATION_ID + " = '" +
                String.valueOf(medicationID) + "'";
    }

    private String getScheduleForPersonWhereClause(long personID){
        return
              MMDataBaseSqlHelper.SCHED_MED_FOR_PERSON_ID + " = '" + String.valueOf(personID) + "'";
    }


    private String getSchedMedIDWhereClause(long schedMedID){
        return MMDataBaseSqlHelper.SCHED_MED_ID + " = '" + String.valueOf(schedMedID) + "'";
    }

    //***********************************************/
    /*         Static inner classes                 */
    //***********************************************/



    //***********************************************/
    /*         inner classes                        */
    //***********************************************/







}
