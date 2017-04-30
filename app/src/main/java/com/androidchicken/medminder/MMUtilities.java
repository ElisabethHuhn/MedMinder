package com.androidchicken.medminder;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.provider.Telephony;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.NotificationCompat;
import android.telephony.SmsManager;
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
import java.util.ArrayList;
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

    public static void showStatus(Context context, String message){
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public static void showStatus(Context context, int messageResource){
        showStatus(context, context.getString(messageResource));
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
    public static void enableAlarmReceiver(Context activity){
        //Enable the Alarm receiver. It will stay enabled across reboots
        ComponentName receiver = new ComponentName(activity, MMAlarmReceiver.class);
        PackageManager pm = activity.getPackageManager();

        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);
    }



    public static void createAlertAlarm(Context activity, MMMedicationAlert medAlert){
        //get the last dose of this medication for this patient
        MMDoseManager doseManager = MMDoseManager.getInstance();
        MMDose mostRecentDose = doseManager.getMostRecentDose(medAlert.getMedicationID());

        long lastTaken;
        if (mostRecentDose == null){
            lastTaken = System.currentTimeMillis();
        } else {
            lastTaken = mostRecentDose.getTimeTaken();
        }

        //calculate when the Alert should be sent (in milliseconds since Jan 1, 1970)
        long timeOverdueMillisec = (medAlert.getOverdueTime() * 60 * 1000);
        timeOverdueMillisec = timeOverdueMillisec + lastTaken;

        //build the Pending Intent which will be activated when the Alarm triggers
        int alertRequestcode = MMAlarmReceiver.alertRequestCode;
        PendingIntent alertIntent = buildAlertIntent(activity, alertRequestcode, medAlert);

        AlarmManager alarmManager = (AlarmManager) activity.getSystemService((Context.ALARM_SERVICE));
        alarmManager.set(AlarmManager.RTC_WAKEUP, timeOverdueMillisec, alertIntent);
    }

    public static void scheduleNotification(Context activity, long timeOfDayMillisec) {
        //start by building the Notification
        int notificationID = MMAlarmReceiver.scheduleNotificationID;
        Notification notification = buildNotification(activity, notificationID);

        //But we need to set an Alarm Intent to send to the Alarm Manager.
        PendingIntent alarmIntent = buildAlarmIntent(activity, notificationID, notification);
        // When the Alarm fires, it will broadcast the PendingIntent
        // that will be picked up by our AlarmReceiver


        AlarmManager alarmManager = (AlarmManager) activity.getSystemService(Context.ALARM_SERVICE);

        //set to repeat every 24 hours
        long repeatInterval = (24 * 60 * 60 * 1000); //hours * minutes * seconds * milli

        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,  //alarm type, real time clock wake up device
                                  timeOfDayMillisec,        //time to first trigger alarm
                                  repeatInterval,           //interval between repeats
                                  alarmIntent);             //Action to perform when the alarm goes off
    }


    public static void cancelNotificationAlarms(Context activity, int minutesSinceMidnight){
        //get the PendingIntent that describes the action we desire,
        // so that it can be performed when the alarm goes off
        int notificationID = MMAlarmReceiver.scheduleNotificationID;
        Notification notification = buildNotification(activity, notificationID);
        PendingIntent alarmIntent = buildAlarmIntent(activity, notificationID, notification);

        AlarmManager alarmManager = (AlarmManager) activity.getSystemService(ALARM_SERVICE);

        //cancel any previous alarms
        alarmManager.cancel(alarmIntent);
    }


    private static Notification buildNotification(Context activity,
                                                  int notificationID){
        //want the Notification to wake up the MMMainActivity
        Intent activityIntent = new Intent(activity, MMMainActivity.class);


        //insert the Intent that will be passed to the app Activity into a Pending Intent.
        // This wrapper Pending Intent is consumed by the system (AlarmManager??)
        PendingIntent contentIntent = PendingIntent.getActivity(
                activity,        //context
                notificationID,  //ID so the notification can be referenced later
                activityIntent,  //Intent to wake up our Activity
                PendingIntent.FLAG_CANCEL_CURRENT); //override any existing

        //  Create a Notification Builder that will do the actual creation of the Notification
        //     and insert the PendingIntent into the Notification as it's ContentIntent
        NotificationCompat.Builder builder =
                (NotificationCompat.Builder) new NotificationCompat.Builder(activity)
                        .setContentTitle(activity.getResources().getString(R.string.app_name))
                        .setContentText(activity.getResources().getString(R.string.time_to_take))
                        .setSmallIcon(R.drawable.ground_station_icon)
                        // .setLargeIcon(((BitmapDrawable) activity.getResources().getDrawable(R.drawable.app_icon)).getBitmap())
                        .setAutoCancel(true)//notification is canceled as soon as it is touched by the user
                        .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                        .setContentIntent(contentIntent);

        //build and return the Notification
        return builder.build();
    }

    private static PendingIntent buildAlarmIntent(Context activity,
                                                  int notificationRequestCode,
                                                  Notification notification){
        //Start by building an Intent targeting our AlarmReceiver,
        // and insert the ID and notification into this Intent
        Intent notificationIntent = new Intent(activity, MMAlarmReceiver.class);
        notificationIntent.putExtra(MMAlarmReceiver.NOTIFICATION_ID, notificationRequestCode);
        notificationIntent.putExtra(MMAlarmReceiver.NOTIFICATION, notification);
        notificationIntent.putExtra(MMAlarmReceiver.ALARM_TYPE, MMAlarmReceiver.schedNotifAlarmType);

        //Insert this intent into a wrapper that is used to schedule an Alarm
        //When the alarm triggers, the notificationIntent will be Broadcast
        //Our MMAlarmReceiver will receive the broadcast and know to post the notification
        return  PendingIntent.getBroadcast( activity,
                                            notificationRequestCode,
                                            notificationIntent,
                                            PendingIntent.FLAG_CANCEL_CURRENT);
    }


    private static PendingIntent buildAlertIntent(Context activity,
                                                  int     alertRequestCode,
                                                  MMMedicationAlert medAlert){
        //Start by building an Intent targeting our AlarmReceiver,
        // and insert the ID and notification into this Intent
        Intent alertIntent = new Intent(activity, MMAlarmReceiver.class);
        alertIntent.putExtra(MMAlarmReceiver.ALARM_TYPE,           MMAlarmReceiver.alertAlarmType);
        alertIntent.putExtra(MMMedicationAlert.sMedAlertID,        medAlert.getMedicationAlertID());
        alertIntent.putExtra(MMMedicationAlert.sMedicationIDTag,   medAlert.getMedicationID());
        alertIntent.putExtra(MMMedicationAlert.sPatientIDTag,      medAlert.getForPatientID());
        alertIntent.putExtra(MMMedicationAlert.sNotifyPersonIDTag, medAlert.getForPatientID());
        alertIntent.putExtra(MMMedicationAlert.sNotifyTypeTag,     medAlert.getNotifyType());


        //Insert this intent into a wrapper that is used to schedule an Alarm
        //When the alarm triggers, the notificationIntent will be Broadcast
        //Our MMAlarmReceiver will receive the broadcast and know to post the notification
        return  PendingIntent.getBroadcast( activity,
                                            alertRequestCode,
                                            alertIntent,
                                            PendingIntent.FLAG_CANCEL_CURRENT);
    }

    public static void sendAlert(Context context,
                                 long medAlertID,
                                 long timeTakenMilliseconds,
                                 long timeDueMilliseconds){
        MMMedicationAlertManager medicationAlertManager = MMMedicationAlertManager.getInstance();
        MMMedicationAlert medicationAlert = medicationAlertManager.getMedicationAlert(medAlertID);

        String dateTimeDueString = getDateTimeString(timeDueMilliseconds);
        String timeTakenString   = getDateTimeString(timeTakenMilliseconds);

        MMPersonManager personManager = MMPersonManager.getInstance();
        MMPerson notifyPerson = getPerson(medicationAlert.getForPatientID());
        String personNickname = notifyPerson.getNickname().toString();

        MMMedicationManager medicationManager = MMMedicationManager.getInstance();
        MMMedication medication = medicationManager.getMedicationFromID(medicationAlert.getMedicationID());
        String medicationName = medication.getMedicationNickname().toString();

        //"Patient %s has not taken a dose of %s since %s, even though one was due %s";

        String msg = String.format(context.getString(R.string.medication_alert_msg),
                                   personNickname,
                                   medicationName,
                                   timeTakenString,
                                   dateTimeDueString);

        int alertType = medicationAlert.getNotifyType();
        if (alertType == MMMedicationAlert.sNOTIFY_BY_TEXT) {
            sendSMS(context, notifyPerson.getTextAddress().toString(), msg);
        } else if (alertType == MMMedicationAlert.sNOTIFY_BY_EMAIL){
            sendEmail(context, notifyPerson.getEmailAddress().toString(), msg);
        } //else if any other type in the future......


    }



    //************************************/
    /*         Send Email using Intent   */
    //************************************/

    protected static void sendEmail(Context context, String toAddress, String msg) {

        String[] TO = {toAddress};     //{"someone@gmail.com"};
        //String[] CC = {"elisabethhuhn@gmail.com"};
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setData(Uri.parse("mailto:"));
        emailIntent.setType("text/plain");


        emailIntent.putExtra(Intent.EXTRA_EMAIL, TO);
        //emailIntent.putExtra(Intent.EXTRA_CC, CC);
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Missed Medication Dose");
        emailIntent.putExtra(Intent.EXTRA_TEXT, msg);

        try {
            context.startActivity(Intent.createChooser(emailIntent, "Send mail..."));
            ((MMMainActivity)context).finish();

        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(context,
                    "There is no email client installed.", Toast.LENGTH_SHORT).show();
        }
    }


    //************************************/
    /*         Send Text to phone #      */
    //************************************/

    //---sends an SMS message to another device---

    // but it opens the message app
    private void sendSMSIntent(Context activity, String phoneNumber, String message)
    {
        Intent messageIntent = new Intent(activity, Telephony.Sms.class);
        PendingIntent pi     = PendingIntent.getActivity(activity, 0, messageIntent, 0);
        SmsManager smsMgr    = SmsManager.getDefault();
        smsMgr.sendTextMessage(phoneNumber, null, message, pi, null);
    }



    public static void sendSMS(Context context, String phoneNo, String msg){
        boolean isSMSPermitted = ((MMMainActivity)context).isSMSPermissionGranted();

        if (isSMSPermitted){
            SmsManager smsManager = SmsManager.getDefault();
            if (msg.length() > 159) {
                ArrayList<String> parts = smsManager.divideMessage(msg);
                smsManager.sendMultipartTextMessage(phoneNo, null, parts, null, null);
            } else {
                try {
                    smsManager.sendTextMessage(phoneNo, null, msg, null, null);

                } catch (Exception ex) {
                    Toast.makeText(context, ex.getMessage().toString(), Toast.LENGTH_LONG).show();
                    ex.printStackTrace();
                }
            }
        }


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
