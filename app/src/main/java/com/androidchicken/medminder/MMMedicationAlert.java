package com.androidchicken.medminder;

/**
 * Created by elisabethhuhn on 4/18/2017.
 *
 * This medication Alert object records the many to many relationship of a Medication Alert
 */

class MMMedicationAlert {
    //************************************/
    /*    Static (class) Constants       */
    //************************************/
    static final int sNOTIFY_BY_EMAIL = 0;
    static final int sNOTIFY_BY_TEXT  = 1;

    static final String sNotifyEmailString = "Email";
    static final String sNotifyTextString  = "Text";

    static final String sMedAlertID        = "med_alert_id";
    static final String sPatientIDTag      = "patient_id";
    static final String sMedicationIDTag   = "medication_id";
    static final String sNotifyPersonIDTag = "notify_person_id";
    static final String sNotifyTypeTag     = "notify_type";



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
    MMMedicationAlert() {
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
    long  getMedicationAlertID()                  {  return mMedicationAlertID; }
    void  setMedicationAlertID(long medicationAlertID) { mMedicationAlertID = medicationAlertID; }

    long  getMedicationID()                  {  return mMedicationID; }
    void  setMedicationID(long medicationID) { mMedicationID = medicationID; }

    long  getForPatientID()                 { return mForPatientID; }
    void  setForPatientID(long forPatientID) {  mForPatientID = forPatientID; }

    long  getNotifyPersonID()          {  return mNotifyPersonID;  }
    void  setNotifyPersonID(long notifyPersonID) {  mNotifyPersonID = notifyPersonID;  }

    int   getNotifyType()               { return mTypeNotify; }
    void  setNotifyType(int typeNotify) { mTypeNotify = typeNotify;  }

    int   getOverdueTime()                       { return mOverdueTime; }
    int   getOverdueDays()    {return  mOverdueTime/(24 * 60);}
    int   getOverdueHours()   {return (mOverdueTime - (getOverdueDays() * 24 * 60))/60;}
    int   getOverdueMinutes() {return (mOverdueTime - (getOverdueDays() * 24 * 60) - (getOverdueHours() * 60));}

    void  setOverdueTime(int overdueTime) { mOverdueTime = overdueTime; }
    void  setOverdueTime(int days, int hours, int minutes){
        mOverdueTime = (days * 24 * 60) + (hours * 60) + (minutes);
    }


    //************************************/
    /*    Default Attribute Values       */
    //************************************/
    static long getDefaultMedicationAlertID()   {  return MMUtilities.ID_DOES_NOT_EXIST; }

    static long getDefaultMedicationID()   {  return MMUtilities.ID_DOES_NOT_EXIST; }

    static long getDefaultForPatientID()   { return MMUtilities.ID_DOES_NOT_EXIST; }

    static long getDefaultNotifyPersonID() {  return MMUtilities.ID_DOES_NOT_EXIST;  }

    static int  getDefaultTypeNotify()     { return sNOTIFY_BY_EMAIL; }

    static int  getDefaultOverdueTime()    { return 60; }



    //************************************/
    /*          Member Methods           */
    //************************************/
    String cdfHeaders(){
        return  "MedicationAlertID, " +
                "MedicationID, "      +
                "PatientID, "         +
                "PersonNotifiedID,"   +
                "NotificationType, "  +
                "MinutesOverdue "     +
                System.getProperty("line.separator");
    }

    //Convert point to comma delimited file for exchange
    String convertToCDF() {
        return  String.valueOf(this.getMedicationAlertID())   + ", " +
                String.valueOf(this.getMedicationID())        + ", " +
                String.valueOf(this.getForPatientID())        + ", " +
                String.valueOf(this.getNotifyPersonID())      + ", " +
                String.valueOf(this.getNotifyType())          + ", " +
                String.valueOf(this.getOverdueTime())         +
                System.getProperty("line.separator");
    }


}
