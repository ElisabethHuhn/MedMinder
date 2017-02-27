package com.androidchicken.medminder;

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
    /*************************************/
    /*    Static (class) Constants       */
    /*************************************/
    public static int DOSE_AMOUNT_UNITS_MG        = 1;
    public static int DOSE_AMOUNT_UNITS_G         = 2;
    public static int DOSE_AMOUNT_UNITS_GRAIN     = 3;
    public static int DOSE_AMOUNT_UNITS_DROPS     = 4;
    public static int DOSE_AMOUNT_UNITS_PUFFS     = 5;
    public static int DOSE_AMOUNT_UNITS_SPRAY     = 6;
    public static int DOSE_AMOUNT_UNITS_ML        = 7;
    public static int DOSE_AMOUNT_UNITS_POUND     = 8;
    public static int DOSE_AMOUNT_UNITS_SPOONFUL  = 9;
    public static int DOSE_AMOUNT_UNITS_PILL      = 10;
    public static int DOSE_AMOUNT_UNITS_CAPSULE   = 11;
    public static int DOSE_AMOUNT_UNITS_TABLET    = 12;
    public static int DOSE_AMOUNT_UNITS_AS_NEEDED = 13;
    //public static int DOSE_AMOUNT_UNITS_MILLIEQUIVALENT = 14;



    /*************************************/
    /*    Static (class) Variables       */
    /*************************************/


    /*************************************/
    /*    Member (instance) Variables    */
    /*************************************/
    private int          mMedicationID;
    private CharSequence mBrandName;
    private CharSequence mGenericName;
    private CharSequence mMedicationNickname;
    private int          mForPersonID;
    private int          mDoseStrategy;
    private int          mDoseAmount;
    private CharSequence mDoseUnits;
    private int          mDoseNumPerDay;

    private ArrayList<MMScheduleMedication> mSchedules;

    /*************************************/
    /*         Static Methods            */
    /*************************************/


    /*************************************/
    /*         CONSTRUCTORS              */
    /*************************************/

    //Generic instance with no attributes
    public MMMedication() {
        initializeDefaultVariables();
        mMedicationID = MMUtilities.getUniqueID();

    }



    private void initializeDefaultVariables(){
        mMedicationID = MMUtilities.getUniqueID();
        mForPersonID  = getDefaultForPersonID();
        mBrandName    = getDefaultBrandName();
        mGenericName  = getDefaultGenericName();
        mMedicationNickname = getDefaultMedicationNickname();
        mDoseStrategy = getDefaultDoseStrategy();//scheduled
        mDoseAmount   = getDefaultDoseAmount();
        mDoseUnits    = getDefaultDoseUnits();
        mDoseNumPerDay= getDefaultDoseNumPerDay();

        mSchedules    = getDefaultSchedules();
    }
/*
    private void initializeDefaultVariables(){
        mMedicationID = MMUtilities.getUniqueID();
        mForPersonID  = -1;
        mBrandName    = "6 ";
        mGenericName  = "7";
        mMedicationNickname = "Nick ";
        mDoseStrategy = 8;
        mDoseAmount   = 9;
        mDoseUnits    = "10";
        mWhenDue      = "11";
        mDoseNumPerDay = 12;
    }
*/

    /*************************************/
    /*    Member setter/getter Methods   */
    /*************************************/
    public int          getMedicationID()                 {  return mMedicationID; }
    public void         setMedicationID(int medicationID) { mMedicationID = medicationID; }

    public CharSequence getBrandName()                       {  return mBrandName;   }
    public void         setBrandName(CharSequence brandName) { mBrandName = brandName; }

    public CharSequence getGenericName()                         {  return mGenericName;    }
    public void         setGenericName(CharSequence genericName) { mGenericName = genericName; }

    public CharSequence getMedicationNickname() {return mMedicationNickname; }
    public void         setMedicationNickname(CharSequence medicationNickname) {
                                                  mMedicationNickname = medicationNickname; }

    public int          getForPersonID()                { return mForPersonID; }
    public void         setForPersonID(int forPersonID) {  mForPersonID = forPersonID; }

    public int          getDoseStrategy()          {  return mDoseStrategy;  }
    public void         setDoseStrategy(int doseStrategy) {  mDoseStrategy = doseStrategy;  }

    public int          getDoseAmount()               { return mDoseAmount; }
    public void         setDoseAmount(int doseAmount) { mDoseAmount = doseAmount;  }

    public CharSequence getDoseUnits()                       { return mDoseUnits; }
    public void         setDoseUnits(CharSequence doseUnits) { mDoseUnits = doseUnits; }

    public int          getDoseNumPerDay()         { return mDoseNumPerDay;   }
    public void         setDoseNumPerDay(int doseNumPerDay) { mDoseNumPerDay = doseNumPerDay; }

    public ArrayList<MMScheduleMedication> getSchedules(){
        if (mSchedules != null)return mSchedules;
        MMDatabaseManager databaseManager = MMDatabaseManager.getInstance();
        mSchedules = databaseManager.getTimesForSM(mMedicationID);
        return mSchedules;
    }
    public void     setSchedules(ArrayList<MMScheduleMedication> schedules){mSchedules = schedules;}

    /*************************************/
    /*    Default Attribute Values       */
    /*************************************/
    public static int          getDefaultMedicationID()       {  return -1; }

    public static CharSequence getDefaultBrandName()          {  return "Brand Name";   }

    public static CharSequence getDefaultGenericName()        {  return "Generic Name";    }

    public static CharSequence getDefaultMedicationNickname() {return "Med Nick Name"; }

    public static int          getDefaultForPersonID()         { return -1; }

    public static int          getDefaultDoseStrategy()        {  return 1;  }

    public static int          getDefaultDoseAmount()          { return 1; }

    public static CharSequence getDefaultDoseUnits()           { return "mg"; }

    public static int          getDefaultDoseNumPerDay()         { return 1;   }

    public static ArrayList<MMScheduleMedication> getDefaultSchedules(){
        return new ArrayList<MMScheduleMedication>();}

    /*************************************/
    /*          Member Methods           */
    /*************************************/

    public String cdfHeaders(){
        String msg =
                "MedicationID, "  +
                "BrandName, "     +
                "GenericName, "   +
                "Nickname, "      +
                "PersonID, "      +
                "Order, "         +
                "DoseAmount, "    +
                "DoseUnits"       +
                "NumPerDay "      +
                System.getProperty("line.separator");
        return msg;
    }

    //Convert point to comma delimited file for exchange
    public String convertToCDF() {
        return  String.valueOf(this.getMedicationID())        + ", " +
                String.valueOf(this.getBrandName())           + ", " +
                String.valueOf(this.getGenericName())         + ", " +
                String.valueOf(this.getMedicationNickname() ) + ", " +
                String.valueOf(this.getForPersonID())         + ", " +
                String.valueOf(this.getDoseStrategy())               + ", " +
                String.valueOf(this.getDoseAmount())          + ", " +
                String.valueOf(this.getDoseUnits())           + ", " +
                String.valueOf(this.getDoseNumPerDay()            +
                        // TODO: 2/18/2017 list schedules
                "Show schedule times too" +
                System.getProperty("line.separator"));
    }


}
