package com.androidchicken.medminder;

import android.content.Context;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by elisabethhuhn on 10/12/2016.
 *
 * This class contains utilities used by other classes in the package
 */

public class MMUtilities {
    /*************************************/
    /*    Static (class) Constants       */
    /*************************************/

    // TODO: 3/22/2017  this eventually needs to be a user setting
    //# of minutes prior to scheduled due time that notification is giving to take dose
    public static final int WITHIN_DOSE_WINDOW = 5;
    public static final long MINUTES_IN_DAY = 24 * 60;


    public static final boolean BUTTON_DISABLE = false;
    public static final boolean BUTTON_ENABLE  = true;

    public static final boolean HOUR12FORMAT = false;
    public static final boolean HOUR24FORMAT = true;


    public static final long    ID_DOES_NOT_EXIST = -1;

    public static final String  PERSONID_TAG = "personid_tag";
    public static final String  MEDICATIONID_TAG = "medicataionid_tag";
    public static final String  POSITION_TAG = "position_tag";


    /*************************************/
    /*    Static (class) Variables       */
    /*************************************/


    /*************************************/
    /*    Member (instance) Variables    */
    /*************************************/



    /*************************************/
    /*         Static Methods            */
    /*************************************/
/*
    //generate a guarenteed unique ID
    public static int getUniqueID(){
        long temp = System.currentTimeMillis();
        int  tempID = (int) temp;
        long temp2 = System.currentTimeMillis() & 0xfffffff;
        int  tempID2 = (int) temp2;
        return (int) (System.currentTimeMillis() & 0xfffffff);
    }
*/

    //convert pixels to dp
    public static int convertPixelsToDp(Context context, int sizeInDp) {
        //int sizeInDp = 10; //padding between buttons
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (sizeInDp * scale + 0.5f); // dpAsPixels;
    }

    //Just a stub for now, but figure out what to do
    public static void errorHandler(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public static void errorHandler(Context context, int messageResource) {
        errorHandler(context, context.getString(messageResource));
    }

    /*************************************/
    /*         Date / Time Utilities     */
    /*************************************/



    //The only reason these are here is so that the app
    // will use a consistent method of displaying dates
    public static String getDateTimeString(){
        return DateFormat.getDateTimeInstance().format(new Date());
    }

    public static String getDateTimeString(long milliSeconds){
        Date date = new Date(milliSeconds);
        /*
        SimpleDateFormat dateFormat = new SimpleDateFormat("h:mm a", Locale.US);
        return dateFormat.format(date);
        */
        return DateFormat.getDateTimeInstance().format(date);
    }

    public static String getDateString(){
        return  DateFormat.getDateInstance().format(new Date());
    }

    public static String getDateString(long milliSeconds){
        Date date = new Date(milliSeconds);
        return DateFormat.getDateInstance().format(date);

    }

    public static String getTimeString(){
        SimpleDateFormat dateFormat = new SimpleDateFormat("h:mm a", Locale.US);
        return dateFormat.format(new Date());
    }

    public static String getTimeString(long milliSeconds){
        Date date = new Date(milliSeconds);
        SimpleDateFormat dateFormat = new SimpleDateFormat("h:mm a", Locale.US);
        return dateFormat.format(date);

    }


    /*************************************/
    /*    get Data Object instances      */
    /*************************************/
    public static MMPerson getPerson(long personID){
        MMPersonManager personManager = MMPersonManager.getInstance();
        return personManager.getPerson(personID);
    }

    public static MMMedication getMedication(long medicationID){
        MMMedicationManager medicationManager = MMMedicationManager.getInstance();
        return medicationManager.getMedicationFromID(medicationID);
    }


    /*************************************/
    /*         Widget Utilities          */
    /*************************************/
    public static void enableButton(Context context, Button button, boolean enable){
        button.setEnabled(enable);
        if (enable == BUTTON_ENABLE) {
            button.setTextColor(ContextCompat.getColor(context, R.color.colorTextBlack));
        } else {
            button.setTextColor(ContextCompat.getColor(context, R.color.colorGray));
        }
    }

    public static void hideSoftKeyboard(FragmentActivity context){
        //hide the soft keyboard
        // Check if no view has focus:
        View view = context.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm =
                    (InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }

    }


    /*************************************/
    /*         Clock Utilities          */
    /*************************************/
    public static boolean is24Format() {
        return HOUR12FORMAT;
    }

    /*************************************/
    /*         CONSTRUCTOR               */
    /*************************************/



    /*************************************/
    /*    Member setter/getter Methods   */
    /*************************************/


    /*************************************/
    /*          Member Methods           */
    /*************************************/


}
