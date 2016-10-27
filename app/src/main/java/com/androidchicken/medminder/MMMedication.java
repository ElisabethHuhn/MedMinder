package com.androidchicken.medminder;

/**
 * Created by elisabethhuhn on 10/12/2016.
 *
 * This medication object is acutally a combination of three
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
    private int          mOrder;
    private int          mDoseAmount;
    private CharSequence mDoseUnits;
    private CharSequence mWhenDue;
    private int          mNum;

    /*************************************/
    /*         Static Methods            */
    /*************************************/


    /*************************************/
    /*         CONSTRUCTORS              */

    /*************************************/

    //Generic instance with no attributes
    public MMMedication() {  mMedicationID = MMUtilities.getUniqueID();  }


    public MMMedication(CharSequence brandName, int amount, CharSequence units) {
        mMedicationID = MMUtilities.getUniqueID();
        mBrandName    = brandName;
        mMedicationNickname = brandName;
        mDoseAmount   = amount;
        mDoseUnits    = units;
        mNum = 1;

    }


    /*************************************/
    /*    Member setter/getter Methods   */
    /*************************************/
    public int          getMedicationID()  {  return mMedicationID; }
    //          can not setMedicationID, this is done automatically when created

    public CharSequence getBrandName()                       {  return mBrandName;   }
    public void         setBrandName(CharSequence brandName) { mBrandName = brandName; }

    public CharSequence getGenericName()                         {  return mGenericName;    }
    public void         setGenericName(CharSequence genericName) { mGenericName = genericName; }

    public CharSequence getMedicationNickname() {return mMedicationNickname; }
    public void         setMedicationNickname(CharSequence medicationNickname) {
                                                  mMedicationNickname = medicationNickname; }

    public int  getForPersonID()                { return mForPersonID; }
    public void setForPersonID(int forPersonID) {  mForPersonID = forPersonID; }

    public int  getOrder()          {  return mOrder;  }
    public void setOrder(int order) {  mOrder = order;  }

    public int  getDoseAmount()               { return mDoseAmount; }
    public void setDoseAmount(int doseAmount) { mDoseAmount = doseAmount;  }

    public CharSequence getDoseUnits()                       { return mDoseUnits; }
    public void         setDoseUnits(CharSequence doseUnits) { mDoseUnits = doseUnits; }

    public CharSequence getWhenDue()             {return mWhenDue; }
    public void setWhenDue(CharSequence whenDue) {mWhenDue = whenDue; }

    public int getNum()         { return mNum;   }
    public void setNum(int num) { mNum = num; }

    /*************************************/
    /*          Member Methods           */
    /*************************************/

}
