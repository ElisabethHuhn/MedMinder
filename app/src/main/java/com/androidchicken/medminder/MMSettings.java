package com.androidchicken.medminder;

import android.content.Context;
import android.content.SharedPreferences;

import static android.R.attr.defaultValue;

/**
 * Created by elisabethhuhn on 5/27/17.
 *
 * This class contains application settings.
 * The values are stored in the shared preferences structure
 * This class contains the setters and getters for all such settings
 */

public class MMSettings {
    //************************************/
    /*    Static (class) Constants       */
    //************************************/


    public static final String sDefaultTimeDueTag      = "timeDue"; //in minutes since midnight
    public static final String sClock24FormatTag       = "clock24" ;
    public static final String sPersonDeleteTag        = "personDelete" ;
    public static final String sMedDeleteTag           = "medDelete" ;
    public static final String sSoundNotificationTag   = "soundNotification" ;
    public static final String sVibrateNotificationTag = "vibrateNotification" ;
    public static final String sLengthOfHistoryTag = "lengthHistory";

    //6:00 AM, minutes since Midnight local time
    public static final int   sDefaultTimeDue         =  (6*60);
    //This puts the AS NEEDED schedules at the top of the Schedule list
    public static final int   sDefaultAsNeededTimeDue = 1;







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
    public static MMSettings getInstance() {
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

    public long getPatientID (MMMainActivity activity)  {
        SharedPreferences sharedPref = activity.getPreferences(Context.MODE_PRIVATE);
        long defaultValue = MMUtilities.ID_DOES_NOT_EXIST;
        return sharedPref.getLong(MMPerson.sPersonIDTag, defaultValue);
    }
    public long setPatientID (MMMainActivity activity, long patientID){

        //Store the PersonID for the next time
        SharedPreferences sharedPref = activity.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putLong(MMPerson.sPersonIDTag, patientID);
        editor.apply();

        return patientID;
    }


    public long getDefaultTimeDue (MMMainActivity activity)  {
        SharedPreferences sharedPref = activity.getPreferences(Context.MODE_PRIVATE);
        long defaultValue = 1;
        long defaultTime = sharedPref.getLong(sDefaultTimeDueTag, defaultValue);
        //If it wasn't in preferences, return the default
        if (defaultTime == defaultValue){

            MMUtilities utilities = MMUtilities.getInstance();
            defaultTime = utilities.convertMinutesLocaltoGMT(sDefaultTimeDue);

            MMSettings.getInstance().setDefaultTimeDue(activity, defaultTime);
        }
        return defaultTime;
    }
    public void setDefaultTimeDue (MMMainActivity activity, long minutesSinceMidnight){
        SharedPreferences sharedPref = activity.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putLong(MMSettings.sDefaultTimeDueTag, minutesSinceMidnight);
        editor.apply();
    }


    public long getHistoryDate (MMMainActivity activity)  {
        SharedPreferences sharedPref = activity.getPreferences(Context.MODE_PRIVATE);
        long historyDateDefault = 1;
        long historyDateMilli = sharedPref.getLong(sLengthOfHistoryTag, defaultValue);
        //If it wasn't in preferences, return the default
        if (historyDateMilli == historyDateDefault){
            //set default to today's date
            String historyDateString = MMUtilities.getInstance().getDateString();
            boolean isTimeFlag = false;
            historyDateMilli = MMUtilities.getInstance().
                            convertStringToTimeDate(activity, historyDateString, isTimeFlag).
                            getTime();
            setHistoryDate(activity, historyDateMilli);
        }
        return historyDateMilli;
    }
    public void setHistoryDate (MMMainActivity activity, long minutesSinceMidnight){
        SharedPreferences sharedPref = activity.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putLong(MMSettings.sLengthOfHistoryTag, minutesSinceMidnight);
        editor.apply();
    }


    public boolean getClock24Format(MMMainActivity activity)  {
        SharedPreferences sharedPref = activity.getPreferences(Context.MODE_PRIVATE);
        boolean defaultValue = false;
        return sharedPref.getBoolean(MMSettings.sClock24FormatTag, defaultValue);
    }
    public void    setClock24Format(MMMainActivity activity, boolean is24Format){
        SharedPreferences sharedPref = activity.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(MMSettings.sClock24FormatTag, is24Format);
        editor.apply();
    }

    public boolean getShowDeletedPersons(MMMainActivity activity)  {
        SharedPreferences sharedPref = activity.getPreferences(Context.MODE_PRIVATE);
        boolean defaultValue = true;
        return sharedPref.getBoolean(MMSettings.sPersonDeleteTag, defaultValue);
    }
    public void    setShowDeletedPersons(MMMainActivity activity, boolean showDeletedPerson){
        SharedPreferences sharedPref = activity.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(MMSettings.sPersonDeleteTag, showDeletedPerson);
        editor.apply();
    }

    public boolean getShowDeletedMeds(MMMainActivity activity)  {
        SharedPreferences sharedPref = activity.getPreferences(Context.MODE_PRIVATE);
        boolean defaultValue = true;
        return sharedPref.getBoolean(MMSettings.sMedDeleteTag, defaultValue);
    }
    public void    setShowDeletedMeds(MMMainActivity activity, boolean showDeletedMed){
        SharedPreferences sharedPref = activity.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(MMSettings.sMedDeleteTag, showDeletedMed);
        editor.apply();
    }

    public boolean getSoundNotification(MMMainActivity activity)  {
        SharedPreferences sharedPref = activity.getPreferences(Context.MODE_PRIVATE);
        boolean defaultValue = true;
        return sharedPref.getBoolean(MMSettings.sSoundNotificationTag, defaultValue);
    }
    public void    setSoundNotification(MMMainActivity activity, boolean isSoundNotif){
        SharedPreferences sharedPref = activity.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(MMSettings.sSoundNotificationTag, isSoundNotif);
        editor.apply();
    }

    public boolean getVibrateNotification(MMMainActivity activity)  {
        SharedPreferences sharedPref = activity.getPreferences(Context.MODE_PRIVATE);
        boolean defaultValue = true;
        return sharedPref.getBoolean(MMSettings.sVibrateNotificationTag, defaultValue);
    }
    public void    setVibrateNotification(MMMainActivity activity, boolean isVibrateNotif){
        SharedPreferences sharedPref = activity.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(MMSettings.sVibrateNotificationTag, isVibrateNotif);
        editor.apply();
    }



}
