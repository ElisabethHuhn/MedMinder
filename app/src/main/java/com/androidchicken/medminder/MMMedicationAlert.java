package com.androidchicken.medminder;

/**
 * Created by elisabethhuhn on 4/18/2017.
 *
 * This medication Alert object records the many to many relationship of a Medication Alert
 */

public class MMMedicationAlert {
    //************************************/
    /*    Static (class) Constants       */
    //************************************/
    public static final int sNOTIFY_BY_EMAIL = 0;
    public static final int sNOTIFY_BY_TEXT  = 1;

    public static final String sNotifyEmailString = "Email";
    public static final String sNotifyTextString  = "Text";

    public static final String sMedAlertID        = "med_alert_id";
    public static final String sPatientIDTag      = "patient_id";
    public static final String sMedicationIDTag   = "medication_id";
    public static final String sNotifyPersonIDTag = "notify_person_id";
    public static final String sNotifyTypeTag     = "notify_type";



    //************************************/
    /*    Static (class) Variables       */
    //************************************/


    //************************************/
    /*    Member (instance) Variables    */
    //************************************/
    private long mMedicationAlertID;
    private long mMedicationID;
    private long mForPatientID;
    private long mNotifyPersonID;
    private int  mTypeNotify;
    private int  mOverdueTime; //minutes late before notification goes off

    //************************************/
    /*         Static Methods            */
    //************************************/


    //************************************/
    /*         CONSTRUCTORS              */
    //************************************/

    //Generic instance with no attributes
    public MMMedicationAlert() {
        initializeDefaultVariables();
    }


    private void initializeDefaultVariables(){
        mMedicationAlertID = getDefaultMedicationAlertID();
        mMedicationID      = getDefaultMedicationID();
        mForPatientID      = getDefaultForPatientID();
        mNotifyPersonID    = getDefaultNotifyPersonID();
        mTypeNotify        = getDefaultTypeNotify();
        mOverdueTime       = getDefaultOverdueTime();
    }


    //************************************/
    /*    Member setter/getter Methods   */
    //************************************/
    public long  getMedicationAlertID()                  {  return mMedicationAlertID; }
    public void  setMedicationAlertID(long medicationAlertID) { mMedicationAlertID = medicationAlertID; }

    public long  getMedicationID()                  {  return mMedicationID; }
    public void  setMedicationID(long medicationID) { mMedicationID = medicationID; }

    public long  getForPatientID()                 { return mForPatientID; }
    public void  setForPatientID(long forPatientID) {  mForPatientID = forPatientID; }

    public long  getNotifyPersonID()          {  return mNotifyPersonID;  }
    public void  setNotifyPersonID(long notifyPersonID) {  mNotifyPersonID = notifyPersonID;  }

    public int   getNotifyType()               { return mTypeNotify; }
    public void  setNotifyType(int typeNotify) { mTypeNotify = typeNotify;  }

    public int   getOverdueTime()                       { return mOverdueTime; }
    public int   getOverdueDays()    {return  mOverdueTime/(24 * 60);}
    public int   getOverdueHours()   {return (mOverdueTime - (getOverdueDays() * 24 * 60))/60;}
    public int   getOverdueMinutes() {return (mOverdueTime - (getOverdueDays() * 24 * 60) - (getOverdueHours() * 60));}

    public void  setOverdueTime(int overdueTime) { mOverdueTime = overdueTime; }
    public void  setOverdueTime(int days, int hours, int minutes){
        mOverdueTime = (days * 24 * 60) + (hours * 60) + (minutes);
    }


    //************************************/
    /*    Default Attribute Values       */
    //************************************/
    public static long getDefaultMedicationAlertID()   {  return MMUtilities.ID_DOES_NOT_EXIST; }

    public static long getDefaultMedicationID()   {  return MMUtilities.ID_DOES_NOT_EXIST; }

    public static long getDefaultForPatientID()   { return MMUtilities.ID_DOES_NOT_EXIST; }

    public static long getDefaultNotifyPersonID() {  return MMUtilities.ID_DOES_NOT_EXIST;  }

    public static int  getDefaultTypeNotify()     { return sNOTIFY_BY_EMAIL; }

    public static int  getDefaultOverdueTime()    { return 60; }



    //************************************/
    /*          Member Methods           */
    //************************************/
    public String cdfHeaders(){
        return  "MedicationAlertID, " +
                "MedicationID, "      +
                "PatientID, "         +
                "PersonNotifiedID,"   +
                "NotificationType, "  +
                "MinutesOverdue "     +
                System.getProperty("line.separator");
    }

    //Convert point to comma delimited file for exchange
    public String convertToCDF() {
        return  String.valueOf(this.getMedicationAlertID())   + ", " +
                String.valueOf(this.getMedicationID())        + ", " +
                String.valueOf(this.getForPatientID())        + ", " +
                String.valueOf(this.getNotifyPersonID())      + ", " +
                String.valueOf(this.getNotifyType())          + ", " +
                String.valueOf(this.getOverdueTime())         +
                System.getProperty("line.separator");
    }


}
