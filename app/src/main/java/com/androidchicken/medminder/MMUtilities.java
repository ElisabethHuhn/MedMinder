package com.androidchicken.medminder;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.NotificationCompat;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import static android.content.Context.ALARM_SERVICE;

/**
 * Created by elisabethhuhn on 10/12/2016.
 *
 * This class contains utilities used by other classes in the package
 */

public class MMUtilities {
    //************************************/
    /*    Static (class) Constants       */
    //************************************/

    // TODO: 3/22/2017  this eventually needs to be a user setting
    //# of minutes prior to scheduled due time that notification is giving to take dose
    public static final int WITHIN_DOSE_WINDOW = 5;
    public static final long MINUTES_IN_DAY = 24 * 60;


    public static final boolean BUTTON_DISABLE = false;
    public static final boolean BUTTON_ENABLE  = true;

    public static final boolean HOUR12FORMAT = false;
    public static final boolean HOUR24FORMAT = true;


    public static final long    ID_DOES_NOT_EXIST = -1;



    //************************************/
    /*    Static (class) Variables       */
    //************************************/


    //************************************/
    /*    Member (instance) Variables    */
    //************************************/



    //************************************/
    /*         Static Methods            */
    //************************************/
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

    public static void showHint(View view, String msg){
        Snackbar.make(view, msg, Snackbar.LENGTH_LONG).setAction("Action", null).show();
    }

    //************************************/
    /*         Date / Time Utilities     */
    //************************************/



    //****************************************/
    /*    ConcurrentDose row list builder    */
    //****************************************/

    public static EditText createDoseEditText(Context context, int padding){
        EditText edtView;
        LinearLayout.LayoutParams lp =
                new LinearLayout.LayoutParams(0,//width
                        ViewGroup.LayoutParams.WRAP_CONTENT);//height
        lp.weight = 3f;
        //lp.gravity = Gravity.CENTER;
        lp.setMarginEnd(padding);

        edtView = new EditText(context);
        edtView.setHint("0");
        edtView.setInputType(InputType.TYPE_CLASS_NUMBER);
        edtView.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
        edtView.setLayoutParams(lp);
        edtView.setPadding(0,0,padding,0);
        edtView.setGravity(Gravity.CENTER);
        edtView.setTextColor      (ContextCompat.getColor(context,R.color.colorTextBlack));
        edtView.setBackgroundColor(ContextCompat.getColor(context,R.color.colorInputBackground));
/*
        //add listener
        edtView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return false;
            }
        });
*/
        return edtView;
    }


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
        return getTimeString(getTimeFormatString(false), new Date());
    }

    public static String getTimeString(long milliSeconds){
        boolean is24Format = false;
        return getTimeString(getTimeFormatString(is24Format), new Date(milliSeconds));
    }

    public static String getTimeString(boolean is24format){
         return getTimeString(getTimeFormatString(is24format), new Date());
    }


    public static String getTimeString(long milliSeconds, boolean is24format){
        return getTimeString(getTimeFormatString(is24format), new Date(milliSeconds));
    }

    public static String getTimeString(String timeFormat, Date date){
        SimpleDateFormat dateFormat = new SimpleDateFormat(timeFormat, Locale.US);
        return dateFormat.format(date);
    }

    public static String getTimeFormatString(boolean is24format){
        CharSequence timeFormat = "h:mm a";
        if (is24format){
            timeFormat = "H:mm a";
        }
        return timeFormat.toString();
    }

    //************************************/
    /*    get Data Object instances      */
    //************************************/
    public static MMPerson getPerson(long personID){
        MMPersonManager personManager = MMPersonManager.getInstance();
        return personManager.getPerson(personID);
    }

    public static MMMedication getMedication(long medicationID){
        MMMedicationManager medicationManager = MMMedicationManager.getInstance();
        return medicationManager.getMedicationFromID(medicationID);
    }


    //************************************/
    /*         Widget Utilities          */
    //************************************/
    public static void enableButton(Context context, Button button, boolean enable){
        button.setEnabled(enable);
        if (enable == BUTTON_ENABLE) {
            button.setTextColor(ContextCompat.getColor(context, R.color.colorTextBlack));
        } else {
            button.setTextColor(ContextCompat.getColor(context, R.color.colorGray));
        }
    }

    public static void showSoftKeyboard(FragmentActivity context, EditText textField){
        //Give the view the focus, then show the keyboard

        textField.requestFocus();
        InputMethodManager imm =
                (InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);
        //second parameter is flags. We don't need any of them
        imm.showSoftInput(textField, InputMethodManager.SHOW_FORCED);

    }

    public static void hideSoftKeyboard(FragmentActivity context){
        //hide the soft keyboard
        // Check if no view has focus:
        View view = context.getCurrentFocus();
        if (view != null) {
            view.clearFocus();
            InputMethodManager imm =
                    (InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);
            //second parameter is flags. We don't need any of them
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }

    }

    public static void toggleSoftKeyboard(FragmentActivity context){
        //hide the soft keyboard
        // Check if no view has focus:
        View view = context.getCurrentFocus();
        if (view != null) {
            view.clearFocus();
            InputMethodManager imm =
                    (InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);
            //second parameter is flags. We don't need any of them
            imm.toggleSoftInputFromWindow(view.getWindowToken(),0, 0);
        }

    }


    public static void clearFocus(FragmentActivity context){
        //hide the soft keyboard
        // Check if no view has focus:
        View view = context.getCurrentFocus();
        if (view != null) {
            view.clearFocus();
        }
    }


    //************************************/
    /*         Clock Utilities          */
    //************************************/
    public static boolean is24Format() {
        // TODO: 4/21/2017 This needs to go into preferences
        // TODO: 4/21/2017  There probably needs to be some central preferences handling
        return HOUR12FORMAT;
    }



    //*****************************************/
    /*      Alarm / Notification Utilities    */
    //*****************************************/
    public static void setNotificationAlarm(Context context, int minutesSinceMidnight){
        //get the PendingIntent that describes the action we desire,
        // so that it can be performed when the alarm goes off
        PendingIntent pendingIntent = getNotificationAlarmAction(context, minutesSinceMidnight);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);

        int hours   = minutesSinceMidnight / 60;
        int minutes = minutesSinceMidnight - (hours * 60);

        Calendar alarmStartTime = Calendar.getInstance();
        alarmStartTime.set(Calendar.HOUR_OF_DAY, hours);
        alarmStartTime.set(Calendar.MINUTE,      minutes);
        alarmStartTime.set(Calendar.SECOND,      0);
/*
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,          //alarm type, real time clock wake up device
                                  alarmStartTime.getTimeInMillis(), //time to first trigger alarm
                                  getInterval(),                    //interval between repeats
                                  pendingIntent);                   //Action to perform when the alarm goes off
*/
        long futureInMillis = SystemClock.elapsedRealtime() + 30000;

        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, futureInMillis, pendingIntent);

    }

    public static PendingIntent getNotificationAlarmAction(Context context, int requestCode){
        //explicit intent naming the receiver class
        Intent alarmIntent = new Intent(context, MMAlarmReceiver.class);
        alarmIntent.putExtra(MMAlarmReceiver.NOTIFICATION_ID, 1);
        alarmIntent.putExtra(MMAlarmReceiver.NOTIFICATION,getNotification(context));

        //wake up the explicit Activity when the alarm goes off
        //return PendingIntent.getActivity (getActivity(), //context
        //broadcast when the alarm goes off
        return PendingIntent.getBroadcast(context, //context
                requestCode,   //request code
                alarmIntent,   //explicit intent to be broadcast
                PendingIntent.FLAG_UPDATE_CURRENT);
        //flags that control which unspecified
        // parts of the intent can be supplied
        // when the actual send happens


    }

    public static Notification getNotification(Context context){
                /*  Create a Notification Builder  */
        NotificationCompat.Builder builder =
                (NotificationCompat.Builder) new NotificationCompat.Builder(context)

                        .setSmallIcon(R.drawable.ground_station_icon)
                        .setContentTitle(context.getResources().getString(R.string.app_name))
                        .setContentText(context.getResources().getString(R.string.time_to_take))
                        //notification is canceled as soon as it is touched by the user
                        .setAutoCancel(true);

        return builder.build();

    }



    //************************************/
    /*         CONSTRUCTOR               */
    //************************************/



    //************************************/
    /*    Member setter/getter Methods   */
    //************************************/


    //************************************/
    /*          Member Methods           */
    //************************************/


}
