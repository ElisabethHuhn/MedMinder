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

public class MMMedication {
    //************************************/
    /*    Static (class) Constants       */
    //************************************/

    public static final String MEDICATION_ID = "medication_id";

    public static final int sSET_SCHEDULE_FOR_MEDICATION = 0;
    public static final int sAS_NEEDED = 1;

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
    private boolean      mCurrentlyTaken;

    private ArrayList<MMScheduleMedication> mSchedules;

    //************************************/
    /*         Static Methods            */
    //************************************/



    //************************************/
    /*         CONSTRUCTORS              */
    //************************************/

    //Generic instance with no attributes
    public MMMedication() {
        initializeDefaultVariables();
    }

    public MMMedication(long tempMedID){
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
        mCurrentlyTaken = getDefaultCurrentlyTaken();

        mSchedules    = getDefaultSchedules();
    }


    //************************************/
    /*    Member setter/getter Methods   */
    //************************************/

    public long         getForPersonID()                 { return mForPersonID; }
    public void         setForPersonID(long forPersonID) {  mForPersonID = forPersonID; }

    public long         getMedicationID()                  {  return mMedicationID; }
    public void         setMedicationID(long medicationID) { mMedicationID = medicationID; }

    public CharSequence getMedicationNickname() {return mMedicationNickname; }
    public void         setMedicationNickname(CharSequence medicationNickname) {
                                                  mMedicationNickname = medicationNickname; }

    public int          getDoseStrategy()          {  return mDoseStrategy;  }
    public void         setDoseStrategy(int doseStrategy) {  mDoseStrategy = doseStrategy;  }

    public int          getDoseNumPerDay()                  { return mDoseNumPerDay;   }
    public void         setDoseNumPerDay(int doseNumPerDay) { mDoseNumPerDay = doseNumPerDay; }

    public int          getDoseAmount()               { return mDoseAmount; }
    public void         setDoseAmount(int doseAmount) { mDoseAmount = doseAmount;  }

    public CharSequence getDoseUnits()                       { return mDoseUnits; }
    public void         setDoseUnits(CharSequence doseUnits) { mDoseUnits = doseUnits; }


    public CharSequence getBrandName()                       {  return mBrandName;   }
    public void         setBrandName(CharSequence brandName) { mBrandName = brandName; }

    public CharSequence getGenericName()                         {  return mGenericName;    }
    public void         setGenericName(CharSequence genericName) { mGenericName = genericName; }


    public boolean      isCurrentlyTaken() {return mCurrentlyTaken;}
    public void         setCurrentlyTaken(boolean isTaken) {mCurrentlyTaken = isTaken;}

    public boolean isSchedulesChanged() {
        if ((mSchedules == null) ||
                (mSchedules.size() == 0)) {
            return false;
        }
        return true;
    }
    public void     setSchedules(ArrayList<MMScheduleMedication> schedules){mSchedules = schedules;}
    public ArrayList<MMScheduleMedication> getSchedules(){
        if (!isSchedulesChanged()) {
            MMDatabaseManager databaseManager = MMDatabaseManager.getInstance();
            mSchedules = databaseManager.getAllSchedMeds(mMedicationID);

            MMSchedMedManager schedMedManager = MMSchedMedManager.getInstance();
            mSchedules = schedMedManager.getAllSchedMeds(mMedicationID);
        }
        return mSchedules;
    }

    //************************************/
    /*    Default Attribute Values       */
    //************************************/

    public static long         getDefaultForPersonID()         { return -1L; }

    public static long         getDefaultMedicationID()       {  return -1L; }

    public static CharSequence getDefaultMedicationNickname() {return "Med Nick Name"; }

    public static int          getDefaultDoseStrategy()  {  return sSET_SCHEDULE_FOR_MEDICATION;  }

    public static int          getDefaultDoseNumPerDay()       { return 0;   }

    public static int          getDefaultDoseAmount()          { return 1; }

    public static CharSequence getDefaultDoseUnits()           { return "mg"; }

    public static CharSequence getDefaultBrandName()          {  return "";   }

    public static CharSequence getDefaultGenericName()        {  return "";    }

    public static boolean      getDefaultCurrentlyTaken()      {return true;}

    public static ArrayList<MMScheduleMedication> getDefaultSchedules(){
        return new ArrayList<>();}

    //************************************/
    /*          Member Methods           */
    //************************************/
    public Cursor getSchedulesCursor(){
        MMSchedMedManager schedMedManager = MMSchedMedManager.getInstance();
        return schedMedManager.getAllSchedMedsCursor(mMedicationID);
    }

    public boolean addSchedule(MMScheduleMedication schedule){
       return mSchedules.add(schedule);
    }




    public String cdfHeaders(){
        String msg =
                "PersonID, "      +
                "MedicationID, "  +
                "Nickname, "      +
                "Strategy, "      +
                "NumPerDay "      +
                "DoseAmount, "    +
                "DoseUnits"       +
                "BrandName, "     +
                "GenericName, "   +
                "Current"         +
                System.getProperty("line.separator");
        return msg;
    }

    private String getStrategyString() {
        if (getDoseStrategy() == sAS_NEEDED){
            return "as needed";
        } else {
            return "schedule";
        }
    }

    //Convert point to comma delimited file for exchange
    public String convertToCDF() {


        return  String.valueOf(this.getForPersonID())         + ", " +
                String.valueOf(this.getMedicationID())        + ", " +
                String.valueOf(this.getMedicationNickname() ) + ", " +
                getDoseStrategy()                             + ", " +
                String.valueOf(this.getDoseNumPerDay()        + ", " +

                String.valueOf(this.getDoseAmount())          + ", " +
                String.valueOf(this.getDoseUnits())           + ", " +

                String.valueOf(this.getBrandName())           + ", " +
                String.valueOf(this.getGenericName())         + ", " +

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
                        "BrandName:    " + String.valueOf(mBrandName)        + System.getProperty("line.separator") +
                        "GenericName:  " + String.valueOf(mGenericName)      + System.getProperty("line.separator") +
                        "Current?      " + String.valueOf(mCurrentlyTaken)   + System.getProperty("line.separator");
    }

    public String shortString() {
        String ls = System.getProperty("line.separator");
        MMPerson person = MMPersonManager.getInstance().getPerson(mForPersonID);

        StringBuilder message = new StringBuilder();

        message.append(ls);
        message.append("MEDICATION: (ID ");
        message.append(String.valueOf(mMedicationID));
        message.append(") : ");
        message.append(mMedicationNickname);
        if (!mBrandName.toString().isEmpty()) {
            message.append("BrandName ");
            message.append(mBrandName);
        }
        if (!mGenericName.toString().isEmpty()) {
            message.append("GenericName  ");
            message.append(mGenericName);
        }
        message.append(" is currently ");
        if (!isCurrentlyTaken()){
            message.append("NOT ");
        }
        message.append("being taken.");
        message.append(ls);

        message.append("Dose Strategy is: ");
        message.append(getStrategyString());
        message.append(ls);

        message.append("Dosage is: ");
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
