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
    /*         setters & getters                    */
    //***********************************************/

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

    //***********************************************/
    /*         constructor                          */
    //***********************************************/

    //null constructor. It should never be called. But you have to have one
    //    initializeInstance() is the proper protocol
    private MMDatabaseManager() {}



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


    //***********************************************/
    /*         Instance methods                     */
    //***********************************************/
    //The CRUD routines:


    //***********************************************/
    /*        Person CRUD methods                   */
    //***********************************************/

    ///*****************************    COUNT    ***********************
    //Get count of persons
    //public int getPersonCount() {}

    ///*****************************    Create    ***********************
    public long addPerson(MMPerson person){
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

    public Cursor getAllPersonsCursor(){
        return mDatabaseHelper.getObject(  mDatabase,
                                        TABLE_PERSON,
                                        null,    //get the whole object
                                        null,
                                        null, null, null, null);

    }


    //Reads the Persons into memory
    //Returns the number of persons read in
    public ArrayList<MMPerson>  getAllPersons(){

        Cursor cursor = getAllPersonsCursor();

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
    public MMPerson getPerson(long personID){

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
    //The return code indicates how many rows affected
    public int removePerson(long personID){

        return mDatabaseHelper.remove(  mDatabase,
                                        TABLE_PERSON,
                                        getPersonWhereClause(personID),
                                        null);  //values that replace ? in where clause
    }


    //***********************************************/
    /*        Person specific CRUD  utility         */
    //***********************************************/
    private String getPersonWhereClause(long personID){
        return MMDataBaseSqlHelper.PERSON_ID + " = " + String.valueOf(personID);
    }



    //***********************************************/
    /*        Medication CRUD methods               */
    //***********************************************/


    //Get count of medications

    //public int getMedicationCount() {}

    //******************************    Create    ***********************
    public long addMedication(MMMedication medication){
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
            ArrayList<MMScheduleMedication> schedules = medication.getSchedules();

            int last = schedules.size();
            int position = 0;
            while (position < last) {
                MMScheduleMedication scheduleMed =  schedules.get(position);
                returnCode = addSchedMed(scheduleMed);
                if (returnCode == sDB_ERROR_CODE) return returnCode;
                //scheduleMed.setSchedMedID(returnCode);
                position++;
            }
        }

        return returnCode;
    }


    //***********************  Read **********************************

    public Cursor getAllMedicationsCursor(long personID){
        return mDatabaseHelper.getObject(mDatabase,
                                        TABLE_MEDICATION,
                                        null,    //get the whole object
                                        getMedicationWhereClause(personID),
                                        null, null, null, null);

    }

    //gets the Medications linked to this person
    public ArrayList<MMMedication> getAllMedications(long personID){
        if (personID == 0) return null;

        Cursor cursor = getAllMedicationsCursor(personID);

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
                medications.add(medication);
            }
            position++;
        }
        cursor.close();
        return medications;
    }


    //NOTE this routine does NOT add the medication to the Person where
    public MMMedication getMedication(long medicationID){

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
    //The return code indicates how many rows affected
    public int removeMedication(long medicationID){

        return mDatabaseHelper.remove(  mDatabase,
                                        TABLE_MEDICATION,
                                        getMedicationIDWhereClause(medicationID),
                                        null);  //values that replace ? in where clause
    }


    //***********************************************/
    /*        Medication specific CRUD  utility         */
    //***********************************************/
    //This gets a single medication
    private String getMedicationWhereClause(long medicationID, long personID){

        return  MMDataBaseSqlHelper.MEDICATION_ID + " = '" +
                String.valueOf(medicationID) +"' AND " +
                MMDataBaseSqlHelper.MEDICATION_FOR_PERSON_ID + " = '" +
                String.valueOf(personID) + "'";
    }

    //This gets a single medication
    private String getMedicationIDWhereClause(long medicationID){
        return  MMDataBaseSqlHelper.MEDICATION_ID + " = '" + String.valueOf(medicationID) +"'";
    }

    //This gets all medications linked to this person
    private String getMedicationWhereClause(long personID){

        return MMDataBaseSqlHelper.MEDICATION_FOR_PERSON_ID + " = '" + String.valueOf(personID) + "'";
    }




    //***********************************************/
    /*        Medication Alert CRUD methods         */
    //***********************************************/


    //******************************    Create    ***********************
    public long addMedicationAlert(MMMedicationAlert medicationAlert){
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

    public Cursor getAllMedicationAlertsCursor(long personID){
        return mDatabaseHelper.getObject(mDatabase,
                TABLE_MEDICATION_ALERT,
                null,    //get the whole object
                getMedicationAlertWhereClause(personID),
                null, null, null, null);

    }


    public Cursor getAllMedicationAlertsCursor(long personID, long medicationID){
        return mDatabaseHelper.getObject(mDatabase,
                                         TABLE_MEDICATION_ALERT,
                                         null,    //get the whole object
                                         getMedicationAlertWhereClause(personID, medicationID),
                                         null, null, null, null);

    }


    //gets the MedicationAlerts linked to this person
    //if personID is MMUtilities.ID_DOES_NOT_EXIST,
    // then all the MMMedicationAlerts in the DB are returned
    public ArrayList<MMMedicationAlert> getMedicationAlerts(long personID){
        Cursor cursor = getAllMedicationAlertsCursor(personID);

        return createMedAlertsFromCursor(cursor);
    }


    //gets the MedicationAlerts linked to this person for this medication
    // if personID     is MMUtilities.ID_DOES_NOT_EXIST, returns null
    // if medicationID is MMUtilities.ID_DOES_NOT_EXIST, returns null
    public ArrayList<MMMedicationAlert> getMedicationAlerts(long personID, long medicationID){
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



    public MMMedicationAlert getMedicationAlert(long medicationAlertID){

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
    public int removeMedicationAlert(long medicationAlertID){
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
    public Cursor getAllConcurrentDosesCursor(long personID){
        return mDatabaseHelper.getObject(  mDatabase,
                                        TABLE_CONCURRENT_DOSE,
                                        null,    //get the whole object
                                        getConcurrentDosesWhereClause(personID),
                                        null, null, null, null);


    }


    //gets the ConcurrentDoses linked to this person
    public ArrayList<MMConcurrentDose> getAllConcurrentDoses(long personID){
        if (personID == 0) return null;

        Cursor cursor = getAllConcurrentDosesCursor(personID);

        //create a concurrentDose object from the Cursor object
        MMConcurrentDoseManager concurrentDoseManager = MMConcurrentDoseManager.getInstance();

        int position = 0;
        int last = cursor.getCount();
        MMConcurrentDose concurrentDose;
        ArrayList<MMConcurrentDose> concurrentDoses = new ArrayList<>();

        while (position < last) {
            //translate the cursor into a concurrentDose object
            concurrentDose = concurrentDoseManager.getConcurrentDoseFromCursor(cursor, position);
            if (concurrentDose != null) {
                concurrentDoses.add(concurrentDose);
            }
            position++;
        }
        cursor.close();
        return concurrentDoses;
    }


    public long addConcurrentDose(MMConcurrentDose concurrentDose){
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


    //The return code indicates how many rows affected
    public int removeConcurrentDose(long concurrentDoseID){

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


    private String getConcurrentDosesIDWhereClause(long concurrentDoseID){
        return MMDataBaseSqlHelper.CONCURRENT_DOSE_ID + " = '" +
                                                            String.valueOf(concurrentDoseID) + "'";
    }



    //***********************************************/
    /*        Dose CRUD methods                     */
    //***********************************************/

    public Cursor getAllDosesCursor(long concurrentDoseID){
        //get the dose row from the DB
        return mDatabaseHelper.getObject(
                mDatabase,     //the db to access
                TABLE_DOSE,   //table name
                null,          //get the whole dose
                getDosesWhereClause(concurrentDoseID), //where clause
                null, null, null, null);//args, group, row grouping, order
    }

    public Cursor getAllDosesCursor(long medicationID, String orderClause){
        //get the dose row from the DB
        return mDatabaseHelper.getObject(
                mDatabase,        //the db to access
                TABLE_DOSE,       //table name
                null,             //get the whole dose
                getDosesMedicationWhereClause(medicationID), //where clause
                null, null, null, //args, group, row grouping,
                orderClause);//column to order result set

    }

    public ArrayList<MMDose> getAllDoses(long concurrentDoseID){

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


    public long addDose(MMDose dose){
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

    //The return code indicates how many rows affected
    public int removeDose(long doseID){

        return mDatabaseHelper.remove(  mDatabase,
                                        TABLE_DOSE,
                                        getDosesIDWhereClause(doseID),
                                        null);  //values that replace ? in where clause
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
    public Cursor getAllSchedMedsCursor(){
        return mDatabaseHelper.getObject(  mDatabase,
                TABLE_SCHED_MED,
                null,    //get the whole object
                null,
                null, null, null, null);



    }

    public Cursor getAllSchedMedsCursor(long medicationID){
        Cursor cursor = mDatabaseHelper.getObject(  mDatabase,
                TABLE_SCHED_MED,
                null,    //get the whole object
                getSchedMedWhereClause(medicationID),
                null, null, null, null);

        return cursor;

    }

    public Cursor getAllSchedMedsForPersonCursor(long personID, String orderClause){
        return mDatabaseHelper.getObject(   mDatabase,
                                            TABLE_SCHED_MED,
                                            null,    //get the whole object
                                            getSchedMedForPersonWhereClause(personID),
                                            null, null, null, orderClause);

    }

    public ArrayList<MMScheduleMedication> getAllSchedMeds(long medicationID){
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
            cursor.close();
        }

        return times;
    }



    public long addSchedMed(MMScheduleMedication schedMed){
        long returnCode = sDB_ERROR_CODE;
        MMSchedMedManager schedMedManager = MMSchedMedManager.getInstance();
        returnCode = mDatabaseHelper.add( mDatabase,
                                         TABLE_SCHED_MED,
                                         schedMedManager.getCVFromScheduleMedication(schedMed),
                                         getSchedMedIDWhereClause(schedMed.getSchedMedID()),
                                         MMDataBaseSqlHelper.SCHED_MED_ID);
        if (returnCode == sDB_ERROR_CODE)return returnCode;
        schedMed.setSchedMedID(returnCode);

        return returnCode;
    }



    //The return code indicates how many rows affected
    public int removeSchedMed(long schedMedID){
        return mDatabaseHelper.remove(  mDatabase,
                                        TABLE_SCHED_MED,
                                        getSchedMedIDWhereClause(schedMedID),
                                        null);  //values that replace ? in where clause
    }



    //***********************************************/
    /*    Concurrent Dose specific CRUD  utility    */
    //***********************************************/

    private String getSchedMedWhereClause(long medicationID){
        return MMDataBaseSqlHelper.SCHED_MED_OF_MEDICATION_ID + " = '" +
                String.valueOf(medicationID) + "'";
    }

    private String getSchedMedForPersonWhereClause(long personID){
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
