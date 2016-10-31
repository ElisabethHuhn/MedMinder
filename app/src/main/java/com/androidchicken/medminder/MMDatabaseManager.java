package com.androidchicken.medminder;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;

import static com.androidchicken.medminder.MMSqliteOpenHelper.TABLE_CONCURRENT_DOSE;
import static com.androidchicken.medminder.MMSqliteOpenHelper.TABLE_DOSE;
import static com.androidchicken.medminder.MMSqliteOpenHelper.TABLE_MEDICATION;
import static com.androidchicken.medminder.MMSqliteOpenHelper.TABLE_PERSON;

/**
 * Created by elisabethhuhn on 5/18/2016.
 */
public class MMDatabaseManager {


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
    private        MMSqliteOpenHelper mDatabaseHelper;
    private        SQLiteDatabase     mDatabase;


    /************************************************/
    /*         setters & getters                    */
    /************************************************/

    //mDatabaseHelper
    public void setDatabaseHelper(MMSqliteOpenHelper mDatabaseHelper) {
        this.mDatabaseHelper = mDatabaseHelper;
    }
    //Return null if the field has not yet been initialized
    public synchronized MMSqliteOpenHelper getDatabaseHelper()  {
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
     * SQLiteDatabase myDB = MMDatabaseManager.getInstance().getDatabase();
     * using the helper the database may be maintained
     * (i.e. CRUD routines can be called from the helper)
     */
    public static synchronized void initializeInstance(Context context) throws RuntimeException {
        if (sManagerInstance == null){
            //Note the hard coded strings here.
            // todo: figure out how to access the string resources from the DatabaseManager
            if (context == null) throw new RuntimeException(sNoContextException);
            //create the singleton Database Manager
            sManagerInstance = new MMDatabaseManager();

            //create and store it's singleton connection to the database
            //The helper is the database connection
            //It's a singleton to keep the app thread safe
            sManagerInstance.setDatabaseHelper(new MMSqliteOpenHelper(context));

            //Now that we have the connection, create the database as well
            sManagerInstance.setDatabase (sManagerInstance.getDatabaseHelper().getWritableDatabase());

        } else if (sManagerInstance.getDatabaseHelper() == null){
            //if here, we had a Database Manager, but no connection to a database
            //Something is definately fishy, but maybe we can recover
            if (context == null) throw new RuntimeException(sNoContextException);

            //all the constructor does is save the context
            sManagerInstance.setDatabaseHelper( new MMSqliteOpenHelper(context));
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


    /************************************************/
    /*         Instance methods                     */
    /************************************************/
    //The CRUD routines:


    //CRUD routines for a Person
    //STUBS for now

    public ArrayList<MMPerson> getAllPersons(){
        return new ArrayList<>();
    }

    public MMPerson getPerson(int personID){
        return new MMPerson();
    }

    public void addPerson(MMPerson person){
        MMPersonManager personManager = MMPersonManager.getInstance();
        mDatabaseHelper.add(mDatabase, TABLE_PERSON, null, personManager.getPersonCV(person));
    }

    public  int updatePerson(MMPerson person) {

        MMPersonManager personManager = MMPersonManager.getInstance();

        String where_clause = MMSqliteOpenHelper.PERSON_ID +
                             " = "                        +
                             String.valueOf(person.getPersonID());

        return mDatabaseHelper.update(
                                    mDatabase,
                                    TABLE_PERSON,
                                    personManager.getPersonCV(person),
                                    where_clause,
                                    null);  //values that replace ? in where clause

    }

    public void removePerson(int personID){}




    //CRUD routines for a Medication
    //STUBS for now
    public ArrayList<MMMedication> getAllMedications(){
        return new ArrayList<>();
    }

    public MMMedication getMedication(int medicationID){
        return new MMMedication();
    }

    public void addMedication(MMMedication medication){
        MMMedicationManager medicationManager = MMMedicationManager.getInstance();
        mDatabaseHelper.add(mDatabase, TABLE_MEDICATION, null, medicationManager.getMedicationCV(medication));
    }

    public  void updateMedication(MMMedication medication){}

    public void removeMedication(int medicationID){}







    //CRUD routines for a Dose
    //STUBS for now
    public ArrayList<MMDose> getAllDoses(){
        return new ArrayList<>();
    }

    public MMDose getDose(int doseID){
        return new MMDose();
    }

    public void addDose(MMDose dose){
        MMDoseManager doseManager = MMDoseManager.getInstance();
        mDatabaseHelper.add(mDatabase, TABLE_DOSE, null, doseManager.getDoseCV(dose));
    }

    public void updateDose(MMDose dose){}

    public void removeDose(MMDose dose){}




    //CRUD routines for a ConcurrentDose
    //STUBS for now
    public ArrayList<MMConcurrentDoses> getAllConcurrentDoses(){
        return new ArrayList<>();
    }

    public MMConcurrentDoses getConcurrentDose(int concurrentDoseID){
        return new MMConcurrentDoses();
    }

    public void addConcurrentDose(MMConcurrentDoses concurrentDose){
        MMConcurrentDoseManager concurrentDoseManager = MMConcurrentDoseManager.getInstance();
        mDatabaseHelper.add(mDatabase, TABLE_CONCURRENT_DOSE, null, concurrentDoseManager.getConcurrentDoseCV(concurrentDose));
    }

    public void updateConcurrentDose(MMConcurrentDoses concurrentDose){}

    public void removeConcurrentDose(int concurrentDoseID){}






    /************************************************/
    /*         Static inner classes                 */
    /************************************************/



    /************************************************/
    /*         inner classes                        */
    /************************************************/







}
