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

 class MMSettings {
    //************************************/
    /*    Static (class) Constants       */
    //************************************/


    private static final String sDefaultTimeDueTag      = "timeDue"; //in minutes since midnight
    private static final String sClock24FormatTag       = "clock24" ;
    private static final String sPersonDeleteTag        = "personDelete" ;
    private static final String sMedOnlyCurrentTag      = "medDelete" ;
    private static final String sShowOnlyTwoWeeksTag    = "showOnlyTwoWeeks";
    private static final String sSoundNotificationTag   = "soundNotification" ;
    private static final String sLightNotificationTag   = "lightNotification" ;
    private static final String sVibrateNotificationTag = "vibrateNotification" ;
    private static final String sLengthOfHistoryTag     = "lengthHistory";
    private static final String sFabVisibleTag          = "fabVisible";
    private static final String sHomeShadingTag         = "homeShading";
    private static final String sUserInputTag           = "userInput" ;
    private static final String sSelectedPositionTag    = "selectedPosition";

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
        return getLongSetting(activity, MMPerson.sPersonIDTag, MMUtilities.ID_DOES_NOT_EXIST);
    }
    void setPatientID (MMMainActivity activity, long patientID){
        //Store the PersonID for the next time
        setLongSetting(activity, MMPerson.sPersonIDTag, patientID);
    }

    //minutes since midnight
    long getDefaultTimeDue (MMMainActivity activity)  {

        long defaultValue = 420;
        long defaultTime = getLongSetting(activity, sDefaultTimeDueTag, defaultValue);
        //If it wasn't in preferences, return the default
        if (defaultTime == defaultValue){
            MMSettings.getInstance().setDefaultTimeDue(activity, defaultTime);
        }
        return defaultTime;
    }
    void setDefaultTimeDue (MMMainActivity activity, long minutesSinceMidnight){
        setLongSetting(activity, MMSettings.sDefaultTimeDueTag, minutesSinceMidnight);
    }

    long getHistoryDate (MMMainActivity activity)  {

        long historyDateDefault = 1;
        long historyDateMilli = getLongSetting(activity, sLengthOfHistoryTag, defaultValue);
        //If it wasn't in preferences, return the default
        if (historyDateMilli == historyDateDefault){
            //set default to today's date
            String historyDateString = MMUtilitiesTime.getDateString();
            historyDateMilli = MMUtilitiesTime.convertStringToTimeMs(activity,
                                                                     historyDateString,
                                                                     false);
            if (historyDateMilli == 0){
                historyDateMilli = historyDateDefault;

            }
        }
        return historyDateMilli;
    }
    void setHistoryDate (MMMainActivity activity, long minutesSinceMidnight){
        setLongSetting(activity, MMSettings.sLengthOfHistoryTag, minutesSinceMidnight);
    }

    boolean isClock24Format(MMMainActivity activity)  {
        return getBooleanSetting(activity, MMSettings.sClock24FormatTag, false);
    }
    void    setClock24Format(MMMainActivity activity, boolean is24Format){
        setBooleanSetting(activity, MMSettings.sClock24FormatTag, is24Format);
    }

    boolean showOnlyCurrentPersons(MMMainActivity activity)  {
        return getBooleanSetting(activity, MMSettings.sPersonDeleteTag, true);
    }
    void setShowOnlyCurrentPersons(MMMainActivity activity, boolean showDeletedPerson){
        setBooleanSetting(activity, MMSettings.sPersonDeleteTag, showDeletedPerson);
    }

    boolean showOnlyCurrentMeds(MMMainActivity activity)  {
        return getBooleanSetting(activity, MMSettings.sMedOnlyCurrentTag, true);
    }
    void setShowOnlyCurrentMeds(MMMainActivity activity, boolean showOnlyCurrentMed){
        setBooleanSetting(activity, MMSettings.sMedOnlyCurrentTag, showOnlyCurrentMed);
    }

    boolean showOnlyTwoWeeks(MMMainActivity activity)  {
        return getBooleanSetting(activity, MMSettings.sShowOnlyTwoWeeksTag, true);
    }
    void setShowOnlyTwoWeeks(MMMainActivity activity, boolean showOnlyTwoWeeks){
        setBooleanSetting(activity, MMSettings.sShowOnlyTwoWeeksTag, showOnlyTwoWeeks);
    }

    boolean isSoundNotification(MMMainActivity activity)  {
        return getBooleanSetting(activity, MMSettings.sSoundNotificationTag, true);
    }
    void    setSoundNotification(MMMainActivity activity, boolean isSoundNotif){
        setBooleanSetting(activity, MMSettings.sSoundNotificationTag, isSoundNotif);
    }

    boolean isVibrateNotification(MMMainActivity activity)  {
        return getBooleanSetting(activity, MMSettings.sVibrateNotificationTag, true);
    }
    void    setVibrateNotification(MMMainActivity activity, boolean isVibrateNotif){
        setBooleanSetting(activity, MMSettings.sVibrateNotificationTag, isVibrateNotif);
    }

    boolean isLightNotification(MMMainActivity activity)  {
        return getBooleanSetting(activity, MMSettings.sLightNotificationTag, true);
    }
    void    setLightNotification(MMMainActivity activity, boolean isLightNotif){
        setBooleanSetting(activity, MMSettings.sLightNotificationTag, isLightNotif);
    }

    boolean isFabVisible(MMMainActivity activity)  {
        return getBooleanSetting(activity, MMSettings.sFabVisibleTag, true);
    }
    void    setFabVisible(MMMainActivity activity, boolean isFabVisible){
        setBooleanSetting(activity, MMSettings.sFabVisibleTag, isFabVisible);
    }

    boolean isHomeShading(MMMainActivity activity)  {
        return getBooleanSetting(activity, MMSettings.sHomeShadingTag, true);
    }
    void    setHomeShading(MMMainActivity activity, boolean isHomeShading){
        setBooleanSetting(activity, MMSettings.sHomeShadingTag, isHomeShading);
     }

    boolean isUserInput(MMMainActivity activity)  {
        return getBooleanSetting(activity, MMSettings.sUserInputTag, false);
    }
    void    setUserInput(MMMainActivity activity, boolean isUserInput){
        setBooleanSetting(activity, MMSettings.sUserInputTag, isUserInput);
    }

    int  getSelectedPosition (MMMainActivity activity)  {
        return getIntSetting(activity, sSelectedPositionTag, (int)MMUtilities.ID_DOES_NOT_EXIST);
    }
    void setSelectedPosition (MMMainActivity activity, int selectedPosition){
        setIntSetting(activity, sSelectedPositionTag, selectedPosition);
    }




    private int  getIntSetting (MMMainActivity activity, String tag, int defaultValue){
        SharedPreferences sharedPref = activity.getPreferences(Context.MODE_PRIVATE);
        return sharedPref.getInt(tag, defaultValue);
    }
    private void setIntSetting (MMMainActivity activity, String tag, int putValue){
        SharedPreferences sharedPref = activity.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(tag, putValue);
        editor.apply();
    }

    private long getLongSetting (MMMainActivity activity, String tag, long defaultValue){
        SharedPreferences sharedPref = activity.getPreferences(Context.MODE_PRIVATE);
        return sharedPref.getLong(tag, defaultValue);
    }
    private void setLongSetting (MMMainActivity activity, String tag, long putValue){
        SharedPreferences sharedPref = activity.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putLong(tag, putValue);
        editor.apply();
    }

    private boolean getBooleanSetting (MMMainActivity activity, String tag, boolean defaultValue){
        SharedPreferences sharedPref = activity.getPreferences(Context.MODE_PRIVATE);
        return sharedPref.getBoolean(tag, defaultValue);
    }
    private void setBooleanSetting (MMMainActivity activity, String tag, boolean putValue){
        SharedPreferences sharedPref = activity.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(tag, putValue);
        editor.apply();
    }


}
