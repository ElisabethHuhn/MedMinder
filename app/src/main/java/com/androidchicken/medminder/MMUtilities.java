package com.androidchicken.medminder;

import android.content.Context;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import static com.androidchicken.medminder.R.id.personID;

/**
 * Created by elisabethhuhn on 10/12/2016.
 *
 * This class contains utilities used by other classes in the package
 */

public class MMUtilities {
    /*************************************/
    /*    Static (class) Constants       */
    /*************************************/


    /*************************************/
    /*    Static (class) Variables       */
    /*************************************/


    /*************************************/
    /*    Member (instance) Variables    */
    /*************************************/



    /*************************************/
    /*         Static Methods            */
    /*************************************/
    //generate a guarenteed unique ID
    public static int getUniqueID(){
        return (int) (System.currentTimeMillis() & 0xfffffff);
    }


    //convert pixels to dp
    public static int convertPixelsToDp(Context context, int sizeInDp) {
        //int sizeInDp = 10; //padding between buttons
        float scale = context.getResources().getDisplayMetrics().density;
        int dpAsPixels = (int) (sizeInDp * scale + 0.5f);
        return dpAsPixels;
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
        String time = DateFormat.getDateTimeInstance().format(new Date());
        return  time;
    }

    public static String getDateTimeString(long milliSeconds){
        Date date = new Date(milliSeconds);
        SimpleDateFormat dateFormat = new SimpleDateFormat();//without format uses locale
        return dateFormat.format(date);
    }

    public static String getDateString(){
        return  DateFormat.getDateInstance().format(new Date());
    }

    public static String getDateString(long milliSeconds){
        Date date = new Date(milliSeconds);
        return DateFormat.getDateInstance().format(date);

    }

    public static String getTimeString(){
        return  DateFormat.getTimeInstance().format(new Date());
    }

    public static String getTimeString(long milliSeconds){
        Date date = new Date(milliSeconds);
        return DateFormat.getTimeInstance().format(date);

    }


    /*************************************/
    /*         get person instances      */
    /*************************************/
    public static MMPerson getPerson(int personID){
        MMPersonManager personManager = MMPersonManager.getInstance();
        return personManager.getPerson(personID);
    }


    /*************************************/
    /*     get Medication instances      */
    /*************************************/
    public static MMMedication getMedication(int medicationID){
        MMMedicationManager medicationManager = MMMedicationManager.getInstance();
        return medicationManager.getMedicationFromID(medicationID);
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
