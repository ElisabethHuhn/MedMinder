package com.androidchicken.medminder;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Date;

import static android.R.attr.defaultValue;

/**
 * Created by elisabethhuhn on 5/27/17.
 *
 * This class contains application settings.
 * The values are stored in the shared preferences structure
 * This class contains the setters and getters for all such settings
 */

 class MMSettings {
    //************************************/
    /*    Static (class) Constants       */
    //************************************/


    private static final String sDefaultTimeDueTag      = "timeDue"; //in minutes since midnight
    private static final String sClock24FormatTag       = "clock24" ;
    private static final String sPersonDeleteTag        = "personDelete" ;
    private static final String sMedDeleteTag           = "medDelete" ;
    private static final String sSoundNotificationTag   = "soundNotification" ;
    private static final String sVibrateNotificationTag = "vibrateNotification" ;
    private static final String sLengthOfHistoryTag = "lengthHistory";

    //6:00 AM, minutes since Midnight local time
    static final int   sDefaultTimeDue         =  (6*60);
    //This puts the AS NEEDED schedules at the top of the Schedule list
    static final int   sDefaultAsNeededTimeDue = 0;







    //************************************/
    /*    Static (class) Variables       */
    //************************************/
    private static MMSettings ourInstance ;

    //************************************/
    /*    Member (instance) Variables    */
    //************************************/



    //************************************/
    /*         Static Methods            */
    //************************************/
    static MMSettings getInstance() {
        if (ourInstance == null){
            ourInstance = new MMSettings();
        }
        return ourInstance;
    }



    //************************************/
    /*         CONSTRUCTOR               */
    //************************************/
    private MMSettings() {
    }


    //************************************/
    /*    Member setter/getter Methods   */
    //************************************/


    //*********************************************************/
    //               Preferences setters and getters         //
    //*********************************************************/

    long getPatientID (MMMainActivity activity)  {
        SharedPreferences sharedPref = activity.getPreferences(Context.MODE_PRIVATE);
        long defaultValue = MMUtilities.ID_DOES_NOT_EXIST;
        return sharedPref.getLong(MMPerson.sPersonIDTag, defaultValue);
    }
    long setPatientID (MMMainActivity activity, long patientID){

        //Store the PersonID for the next time
        SharedPreferences sharedPref = activity.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putLong(MMPerson.sPersonIDTag, patientID);
        editor.apply();

        return patientID;
    }


    //minutes since midnight
    long getDefaultTimeDue (MMMainActivity activity)  {
        SharedPreferences sharedPref = activity.getPreferences(Context.MODE_PRIVATE);
        long defaultValue = 420;
        long defaultTime = sharedPref.getLong(sDefaultTimeDueTag, defaultValue);
        //If it wasn't in preferences, return the default
        if (defaultTime == defaultValue){

            MMSettings.getInstance().setDefaultTimeDue(activity, defaultTime);
        }
        return defaultTime;
    }
    void setDefaultTimeDue (MMMainActivity activity, long minutesSinceMidnight){
        SharedPreferences sharedPref = activity.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putLong(MMSettings.sDefaultTimeDueTag, minutesSinceMidnight);
        editor.apply();
    }


    long getHistoryDate (MMMainActivity activity)  {
        SharedPreferences sharedPref = activity.getPreferences(Context.MODE_PRIVATE);
        long historyDateDefault = 1;
        long historyDateMilli = sharedPref.getLong(sLengthOfHistoryTag, defaultValue);
        //If it wasn't in preferences, return the default
        if (historyDateMilli == historyDateDefault){
            //set default to today's date
            String historyDateString = MMUtilities.getInstance().getDateString();
            boolean isTimeFlag = false;
            Date historyDate = MMUtilities.getInstance().
                                convertStringToTimeDate(activity, historyDateString, isTimeFlag);
            if (historyDate != null){
                historyDateMilli = historyDate.getTime();
                setHistoryDate(activity, historyDateMilli);
            }
        }
        return historyDateMilli;
    }
    void setHistoryDate (MMMainActivity activity, long minutesSinceMidnight){
        SharedPreferences sharedPref = activity.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putLong(MMSettings.sLengthOfHistoryTag, minutesSinceMidnight);
        editor.apply();
    }


    boolean getClock24Format(MMMainActivity activity)  {
        SharedPreferences sharedPref = activity.getPreferences(Context.MODE_PRIVATE);
        boolean defaultValue = false;
        return sharedPref.getBoolean(MMSettings.sClock24FormatTag, defaultValue);
    }
    void    setClock24Format(MMMainActivity activity, boolean is24Format){
        SharedPreferences sharedPref = activity.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(MMSettings.sClock24FormatTag, is24Format);
        editor.apply();
    }

    boolean getShowDeletedPersons(MMMainActivity activity)  {
        SharedPreferences sharedPref = activity.getPreferences(Context.MODE_PRIVATE);
        boolean defaultValue = true;
        return sharedPref.getBoolean(MMSettings.sPersonDeleteTag, defaultValue);
    }
    void    setShowDeletedPersons(MMMainActivity activity, boolean showDeletedPerson){
        SharedPreferences sharedPref = activity.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(MMSettings.sPersonDeleteTag, showDeletedPerson);
        editor.apply();
    }

    boolean getShowDeletedMeds(MMMainActivity activity)  {
        SharedPreferences sharedPref = activity.getPreferences(Context.MODE_PRIVATE);
        boolean defaultValue = true;
        return sharedPref.getBoolean(MMSettings.sMedDeleteTag, defaultValue);
    }
    void    setShowDeletedMeds(MMMainActivity activity, boolean showDeletedMed){
        SharedPreferences sharedPref = activity.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(MMSettings.sMedDeleteTag, showDeletedMed);
        editor.apply();
    }

    boolean getSoundNotification(MMMainActivity activity)  {
        SharedPreferences sharedPref = activity.getPreferences(Context.MODE_PRIVATE);
        boolean defaultValue = true;
        return sharedPref.getBoolean(MMSettings.sSoundNotificationTag, defaultValue);
    }
    void    setSoundNotification(MMMainActivity activity, boolean isSoundNotif){
        SharedPreferences sharedPref = activity.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(MMSettings.sSoundNotificationTag, isSoundNotif);
        editor.apply();
    }

    boolean getVibrateNotification(MMMainActivity activity)  {
        SharedPreferences sharedPref = activity.getPreferences(Context.MODE_PRIVATE);
        boolean defaultValue = true;
        return sharedPref.getBoolean(MMSettings.sVibrateNotificationTag, defaultValue);
    }
    void    setVibrateNotification(MMMainActivity activity, boolean isVibrateNotif){
        SharedPreferences sharedPref = activity.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(MMSettings.sVibrateNotificationTag, isVibrateNotif);
        editor.apply();
    }



}
