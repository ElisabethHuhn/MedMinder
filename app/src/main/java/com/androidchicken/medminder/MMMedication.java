package com.androidchicken.medminder;

import android.database.Cursor;

import java.util.ArrayList;

/**
 * Created by elisabethhuhn on 10/12/2016.
 *
 * This medication object is actually a combination of three
 * very simple objects. They have been combined to make the app simpler.
 * This medication object represents a medication taken by a single person.
 * It redundantly defines the medication if it is taken by another person.
 * The dosages are specific to a single person.
 * One attribute: scheduled doses, lists all the times the medication
 * should be taken. This complex string must be parsed in order to be used
 * Either scheduled doses OR #AsNeeded is used by a single person.
 */

class MMMedication {
    //************************************/
    /*    Static (class) Constants       */
    //************************************/

    static final String MEDICATION_ID = "medication_id";

    static final int sAS_NEEDED                   = 0;
    static final int sSET_SCHEDULE_FOR_MEDICATION = 1;
    static final int sIN_X_HOURS                  = 2;


    //************************************/
    /*    Static (class) Variables       */
    //************************************/


    //************************************/
    /*    Member (instance) Variables    */
    //************************************/
    private long         mForPersonID;
    private long         mMedicationID;
    private CharSequence mMedicationNickname;
    private int          mDoseStrategy;
    private int          mDoseNumPerDay;
    private int          mDoseAmount;
    private CharSequence mDoseUnits;
    private CharSequence mBrandName;
    private CharSequence mGenericName;
    private CharSequence mNotes;
    private CharSequence mSideEffects;
    private boolean      mCurrentlyTaken;

    private ArrayList<MMSchedule> mSchedules;

    //************************************/
    /*         Static Methods            */
    //************************************/



    //************************************/
    /*         CONSTRUCTORS              */
    //************************************/

    //Generic instance with no attributes
    MMMedication() {
        initializeDefaultVariables();
    }

    MMMedication(long tempMedID){
        initializeDefaultVariables();
        mMedicationID = tempMedID;
    }

    private void initializeDefaultVariables(){
        mMedicationID = MMUtilities.ID_DOES_NOT_EXIST;
        mForPersonID  = getDefaultForPersonID();
        mBrandName    = getDefaultBrandName();
        mGenericName  = getDefaultGenericName();
        mMedicationNickname = getDefaultMedicationNickname();
        mDoseStrategy = getDefaultDoseStrategy();//scheduled
        mDoseAmount   = getDefaultDoseAmount();
        mDoseUnits    = getDefaultDoseUnits();
        mDoseNumPerDay= getDefaultDoseNumPerDay();
        mNotes        = getDefaultNotes();
        mSideEffects  = getDefaultSideEffects();
        mCurrentlyTaken = getDefaultCurrentlyTaken();

        mSchedules    = getDefaultSchedules();
    }


    //************************************/
    /*    Member setter/getter Methods   */
    //************************************/

    long         getForPersonID()                 { return mForPersonID; }
    void         setForPersonID(long forPersonID) {  mForPersonID = forPersonID; }

    long         getMedicationID()                  {  return mMedicationID; }
    void         setMedicationID(long medicationID) { mMedicationID = medicationID; }

    CharSequence getMedicationNickname() {return mMedicationNickname; }
    void         setMedicationNickname(CharSequence medicationNickname) {
                                                  mMedicationNickname = medicationNickname; }

    int          getDoseStrategy()          {  return mDoseStrategy;  }
    void         setDoseStrategy(int doseStrategy) {  mDoseStrategy = doseStrategy;  }

    int          getDoseNumPerDay()                  { return mDoseNumPerDay;   }
    void         incrDoseNumPerDay()                 {mDoseNumPerDay++;}
    void         decrDoseNumPerDay()                 {mDoseNumPerDay--;}
    void         setDoseNumPerDay(int doseNumPerDay) { mDoseNumPerDay = doseNumPerDay; }

    int          getDoseAmount()               { return mDoseAmount; }
    void         setDoseAmount(int doseAmount) { mDoseAmount = doseAmount;  }

    CharSequence getDoseUnits()                       { return mDoseUnits; }
    void         setDoseUnits(CharSequence doseUnits) { mDoseUnits = doseUnits; }


    CharSequence getBrandName()                       {  return mBrandName;   }
    void         setBrandName(CharSequence brandName) { mBrandName = brandName; }

    CharSequence getGenericName()                         {  return mGenericName;    }
    void         setGenericName(CharSequence genericName) { mGenericName = genericName; }

    CharSequence getNotes()                           {  return mNotes;    }
    void         setNotes(CharSequence notes)         { mNotes = notes; }

    CharSequence getSideEffects()                         {  return mSideEffects;    }
    void         setSideEffects(CharSequence sideEffects) { mSideEffects = sideEffects; }


    boolean      isCurrentlyTaken() {return mCurrentlyTaken;}
    void         setCurrentlyTaken(boolean isTaken) {mCurrentlyTaken = isTaken;}

    boolean      isSchedulesChanged() {
        if ((mSchedules == null) || (mSchedules.size() == 0)) {
            return false;
        }
        return true;
    }
    void         setSchedules(ArrayList<MMSchedule> schedules){mSchedules = schedules;}
    ArrayList<MMSchedule> getSchedules(){
        if (!isSchedulesChanged()) {
            MMDatabaseManager databaseManager = MMDatabaseManager.getInstance();
            mSchedules = databaseManager.getAllSchedules(mMedicationID);

            MMScheduleManager schedMedManager = MMScheduleManager.getInstance();
            mSchedules = schedMedManager.getAllSchedules(mMedicationID);
        }
        return mSchedules;
    }

    //************************************/
    /*    Default Attribute Values       */
    //************************************/

    static long         getDefaultForPersonID()         { return -1L; }

    static long         getDefaultMedicationID()       {  return -1L; }

    static CharSequence getDefaultMedicationNickname() {return "Med Nick Name"; }

    static int          getDefaultDoseStrategy()  {  return sSET_SCHEDULE_FOR_MEDICATION;  }

    static int          getDefaultDoseNumPerDay()       { return 0;   }

    static int          getDefaultDoseAmount()          { return 1; }

    static CharSequence getDefaultDoseUnits()           { return "mg"; }

    static CharSequence getDefaultBrandName()          {  return "";   }

    static CharSequence getDefaultGenericName()        {  return "";    }

    static CharSequence getDefaultNotes()              {  return "";    }

    static CharSequence getDefaultSideEffects()        {  return "";    }

    static boolean      getDefaultCurrentlyTaken()     {return true;}

    static ArrayList<MMSchedule> getDefaultSchedules(){
        return new ArrayList<>();}

    //************************************/
    /*          Member Methods           */
    //************************************/
    Cursor getSchedulesCursor(){
        MMScheduleManager schedMedManager = MMScheduleManager.getInstance();
        return schedMedManager.getAllSchedulesCursor(mMedicationID);
    }

    //Note that the DB is not updataed with this method!!
    boolean addSchedule(MMSchedule schedule){
       return mSchedules.add(schedule);
    }

    void removeSchedules(MMMainActivity activity){
        ArrayList<MMSchedule> schedules = getSchedules();
        MMSchedule schedule;
        if ((schedules != null) && (schedules.size() > 0)){
            int last = schedules.size();
            int position = 0;

            while (position < last) {
                schedule = schedules.get(position);
                if (!(MMScheduleManager.getInstance()
                        .removeScheduleFromDB(schedule.getScheduleID()))){

                    MMUtilities.getInstance().errorHandler(activity, R.string.error_removing_schedule);
                }
                position++;
            }
        }
        //remove the schedules from the medication
        setSchedules(getDefaultSchedules());
    }



    //************************************/
    /*    Export / output Methods        */
    //************************************/

    String cdfHeaders(){
        return  "PersonID, "      +
                "MedicationID, "  +
                "Nickname, "      +
                "Strategy, "      +
                "NumPerDay "      +
                "DoseAmount, "    +
                "DoseUnits"       +
                "BrandName, "     +
                "GenericName, "   +
                "Notes, "         +
                "SideEffects, "   +
                "Current"         +
                System.getProperty("line.separator");

    }

    private String getStrategyString() {
        if (getDoseStrategy() == sAS_NEEDED){
            return "as needed";
        } else {
            return "schedule";
        }
    }

    //Convert point to comma delimited file for exchange
    String convertToCDF() {


        return  String.valueOf(this.getForPersonID())         + ", " +
                String.valueOf(this.getMedicationID())        + ", " +
                String.valueOf(this.getMedicationNickname() ) + ", " +
                getDoseStrategy()                             + ", " +
                String.valueOf(this.getDoseNumPerDay()        + ", " +

                String.valueOf(this.getDoseAmount())          + ", " +
                String.valueOf(this.getDoseUnits())           + ", " +

                getBrandName()                                + ", " +
                getGenericName()                              + ", " +

                getNotes()                                    + ", " +
                getSideEffects()                              + ", " +

                String.valueOf(this.isCurrentlyTaken())       +

                         // TODO: 2/18/2017 list schedules
                "Show schedule times too" +
                System.getProperty("line.separator"));
    }

    public String toString() {
        MMPerson person = MMPersonManager.getInstance().getPerson(mForPersonID);
        return
                System.getProperty("line.separator") +
                        "MEDICATION:"    + System.getProperty("line.separator") +
                        "PersonID:     " + person.getNickname()              + System.getProperty("line.separator") +
                        "MedicationID: " + String.valueOf(mMedicationID)     + System.getProperty("line.separator") +
                        "Nickname:     " + String.valueOf(mMedicationNickname) + System.getProperty("line.separator") +
                        "Strategy      " + getStrategyString()               + System.getProperty("line.separator") +
                        "NumPerDay:    " + String.valueOf(mDoseNumPerDay)    + System.getProperty("line.separator") +
                        "DoseAmount:   " + String.valueOf(mDoseAmount)       + System.getProperty("line.separator") +
                        "DoseUnits:    " + String.valueOf(mDoseUnits)        + System.getProperty("line.separator") +
                        "BrandName:    " + getBrandName()                    + System.getProperty("line.separator") +
                        "GenericName:  " + getGenericName()                  + System.getProperty("line.separator") +
                        "Notes:        " + getNotes()                        + System.getProperty("line.separator") +
                        "SideEffects:  " + getSideEffects()                  + System.getProperty("line.separator") +
                        "Current?      " + String.valueOf(mCurrentlyTaken)   + System.getProperty("line.separator");
    }

    String shortString() {
        String ls = System.getProperty("line.separator");
        MMPerson person = MMPersonManager.getInstance().getPerson(mForPersonID);

        StringBuilder message = new StringBuilder();

        message.append(ls);
        message.append("MED: (ID ");
        message.append(String.valueOf(mMedicationID));
        message.append(") : ");
        message.append(ls);
        message.append(mMedicationNickname);
        if (!mBrandName.toString().isEmpty()) {
            message.append("BrandName ");
            message.append(mBrandName);
        }
        if (!mGenericName.toString().isEmpty()) {
            message.append("GenericName  ");
            message.append(mGenericName);
        }
        message.append(ls);
        message.append("Currently ");
        if (!isCurrentlyTaken()){
            message.append("NOT ");
        }
        message.append("being taken.");
        message.append(ls);

        String notes = getNotes().toString();
        if (!notes.isEmpty()) {

            message.append(ls);
            message.append("Notes:  ");
            message.append(notes);
            message.append(ls);
        }

        String sideEffects = getSideEffects().toString();
        if (!sideEffects.isEmpty()) {
            message.append(ls);
            message.append("SideEffects:  ");
            message.append(sideEffects);
            message.append(ls);
        }

        message.append("Dose Strategy: ");
        message.append(getStrategyString());
        message.append(ls);

        message.append("Dosage: ");
        message.append(String.valueOf(mDoseAmount));
        message.append(" ");
        message.append(String.valueOf(mDoseUnits));
        message.append(" ");
        if (mDoseNumPerDay > 0) {
            message.append(String.valueOf(mDoseNumPerDay));
            message.append(" times per day at ");
        }

        return message.toString();
    }


}
