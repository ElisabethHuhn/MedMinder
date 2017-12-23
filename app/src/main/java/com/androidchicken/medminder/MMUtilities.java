package com.androidchicken.medminder;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.telephony.SmsManager;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static android.content.Context.ALARM_SERVICE;
import static com.androidchicken.medminder.MMAlarmReceiver.scheduleNotificationID;

/**
 * Created by elisabethhuhn on 10/12/2016.
 *
 * This class contains utilities used by other classes in the package
 */

public class MMUtilities {
    //************************************/
    /*    Static (class) Constants       */
    //************************************/


    static final boolean BUTTON_DISABLE = false;
    static final boolean BUTTON_ENABLE  = true;


    static final long    ID_DOES_NOT_EXIST = -1;

    static final long milliPerSecond   = 1000;
    static final long secondsPerMinute = 60;
    static final long minutesPerHour   = 60;



    //************************************/
    /*    Static (class) Variables       */
    //************************************/
    private static MMUtilities ourInstance ;

    //************************************/
    /*    Member (instance) Variables    */
    //************************************/



    //************************************/
    /*         Static Methods            */
    //************************************/
    static MMUtilities getInstance() {
        if (ourInstance == null){
            ourInstance = new MMUtilities();
        }
        return ourInstance;
    }



    //************************************/
    /*         CONSTRUCTOR               */
    //************************************/
    private MMUtilities() {
    }


    //************************************/
    /*    Member setter/getter Methods   */
    //************************************/



    //************************************/
    /*          Member Methods           */
    //************************************/

    //convert pixels to dp
     int convertPixelsToDp(Context context, int sizeInDp) {
        //int sizeInDp = 10; //padding between buttons
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (sizeInDp * scale + 0.5f); // dpAsPixels;
    }

    //Just a stub for now, but figure out what to do
     void errorHandler(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

     void errorHandler(Context context, int messageResource) {
        errorHandler(context, context.getString(messageResource));
    }

     void showStatus(Context context, String message){
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

     void showStatus(Context context, int messageResource){
        showStatus(context, context.getString(messageResource));
    }


     void showHint(View view, String msg){
        Snackbar.make(view, msg, Snackbar.LENGTH_LONG).setAction("Action", null).show();
    }


    //****************************************/
    /*    ConcurrentDose row list builder    */
    //****************************************/

     android.support.v7.widget.AppCompatEditText createDoseEditText(Context context,
                                                                           int padding){

         android.support.v7.widget.AppCompatEditText edtView;
         LinearLayout.LayoutParams lp =
                new LinearLayout.LayoutParams(0,//width
                        ViewGroup.LayoutParams.WRAP_CONTENT);//height
         lp.weight = 3f;
         //lp.gravity = Gravity.CENTER;
         lp.setMarginEnd(padding);

         edtView = new android.support.v7.widget.AppCompatEditText(context);
         edtView.setHint("0");
         edtView.setInputType(InputType.TYPE_CLASS_NUMBER);
         edtView.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
         edtView.setLayoutParams(lp);
         edtView.setPadding(0,0,padding,0);
         edtView.setGravity(Gravity.CENTER);
         edtView.setTextColor      (ContextCompat.getColor(context,R.color.colorTextBlack));
         edtView.setBackgroundColor(ContextCompat.getColor(context,R.color.colorInputBackground));

        return edtView;
    }


    //************************************/
    /*         Date / Time Utilities     */
    //************************************/

     String getDateTimeString(long milliSeconds){
        Date date = new Date(milliSeconds);
        return DateFormat.getDateTimeInstance().format(date);
    }


     String getDateString(){
        return  DateFormat.getDateInstance().format(new Date());
    }


// TODO: 10/13/2017 move these out of utilities and into utilitiestime

    //This method is used to get the string corresponding to a value of milliseconds since 1970
     String getTimeString(MMMainActivity activity, long milliSeconds){
        boolean is24Format = MMSettings.getInstance().isClock24Format(activity);
        return getTimeString(milliSeconds, is24Format);
    }

     private String getTimeString(long milliSeconds, boolean is24format){
        String timeFormat = getTimeFormatString(is24format);
        Date dateFromMilli = new Date(milliSeconds);
        return getTimeString(timeFormat, dateFromMilli);
    }

    private String getTimeString(String timeFormat, Date date){
        SimpleDateFormat dateFormat = new SimpleDateFormat(timeFormat, Locale.getDefault());

        return dateFormat.format(date);
    }


    private String getTimeFormatString(boolean is24format){
        CharSequence timeFormat = "h:mm a";
        if (is24format){
            timeFormat = "H:mm a";
        }
        return timeFormat.toString();
    }



    //************************************/
    /*    get Data Object instances      */
    //************************************/


    //************************************/
    /*         Widget Utilities          */
    //************************************/
     void enableButton(Context context, Button button, boolean enable){
        button.setEnabled(enable);
        if (enable == BUTTON_ENABLE) {
            button.setTextColor(ContextCompat.getColor(context, R.color.colorTextBlack));
        } else {
            button.setTextColor(ContextCompat.getColor(context, R.color.colorGray));
        }
    }

     void showSoftKeyboard(FragmentActivity context, EditText textField){
        //Give the view the focus, then show the keyboard

        textField.requestFocus();
        InputMethodManager imm =
                (InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm == null)return;

        //second parameter is flags. We don't need any of them
        imm.showSoftInput(textField, InputMethodManager.SHOW_FORCED);

    }

     void hideSoftKeyboard(FragmentActivity context){
         // Check if no view has focus:
        View view = context.getCurrentFocus();
        if (view != null) {
            view.clearFocus();
            InputMethodManager imm =
                    (InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm == null)return;

            //second parameter is flags. We don't need any of them
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);


        }
        //close the keyboard
        context.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
    }

     void toggleSoftKeyboard(FragmentActivity context){
        //hide the soft keyboard
        // Check if no view has focus:
        View view = context.getCurrentFocus();
        if (view != null) {
            view.clearFocus();
            InputMethodManager imm =
                    (InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm == null)return;

            //second parameter is flags. We don't need any of them
            imm.toggleSoftInputFromWindow(view.getWindowToken(),0, 0);
        }

    }


     void clearFocus(FragmentActivity context){
        //hide the soft keyboard
        // Check if no view has focus:
        View view = context.getCurrentFocus();
        if (view != null) {
            view.clearFocus();
        }
    }



    //*****************************************************/
    /*  For scheduling notifications that a dose is due   */
    /*          Alarm / Notification Utilities            */
    //*****************************************************/
    void enableAlarmReceiver(Context activity){
        //Enable the Alarm receiver. It will stay enabled across reboots
        ComponentName receiver = new ComponentName(activity, MMAlarmReceiver.class);
        PackageManager pm = activity.getPackageManager();

        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);
    }



    void createScheduleNotification(MMMainActivity activity, long timeOfDayMillisec) {
        //start by building the Notification
        int requestCode = scheduleNotificationID;
        Notification notification = buildSchedNotification(activity, requestCode);

        //But we need to set an Alarm Intent to send to the Alarm Manager.
        PendingIntent alarmIntent = buildScheduleAlarmIntent(activity, requestCode, notification);
        // When the Alarm fires, AlarmManager will broadcast the PendingIntent
        // that will be picked up by our AlarmReceiver



        AlarmManager alarmManager = (AlarmManager) activity.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null)return;

        //set to repeat every 24 hours
        //The Interval is expressed in milliseconds, so convert hours to milliseconds
        long repeatInterval = (24 * 60 * 60 * 1000); //hours * minutes * seconds * milli

        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,  //alarm type, real time clock wake up device
                                  timeOfDayMillisec,        //time to first trigger alarm
                                  repeatInterval,           //interval between repeats
                                  alarmIntent);             //Action to perform when the alarm goes off
    }


    private  Notification buildSchedNotification(MMMainActivity activity, int requestCode) {
        return buildNotification(activity, requestCode, MMUtilities.ID_DOES_NOT_EXIST);
    }

    private  Notification buildNotification(MMMainActivity activity,
                                                  int requestCode,
                                                  long medAlertID){

        //want the Notification to wake up the MMMainActivity
        Intent activityIntent = new Intent(activity, MMMainActivity.class);


        //insert the Intent that will be passed to the app Activity into a Pending Intent.
        // This wrapper Pending Intent is consumed by the system (AlarmManager??)
        PendingIntent contentIntent = PendingIntent.getActivity(
                                    activity,        //context
                                    requestCode,     //requestCode
                                    activityIntent,  //Intent to wake up our Activity
                                    PendingIntent.FLAG_CANCEL_CURRENT); //override any existing

        int textMessage;
        if (requestCode == scheduleNotificationID){
            textMessage = R.string.time_to_take;
        } else {
            textMessage = R.string.time_to_send_alert;
            activityIntent.putExtra(MMAlarmReceiver.ALARM_TYPE,   MMAlarmReceiver.alertAlarmType);
            activityIntent.putExtra(MMMedicationAlert.sMedAlertID, medAlertID);
        }

        MMSettings settings = MMSettings.getInstance();
        boolean isLight   = settings.isLightNotification(activity);
        boolean isSound   = settings.isSoundNotification(activity);
        boolean isVibrate = settings.isVibrateNotification(activity);


        NotificationManager notificationManager =
                    (NotificationManager) activity.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager == null)return null;

        String NOTIFICATION_CHANNEL_ID = "my_channel_id_01";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel =
                                    new NotificationChannel(NOTIFICATION_CHANNEL_ID,
                                                            "My Notifications",
                                                            NotificationManager.IMPORTANCE_DEFAULT);

            // Configure the notification channel.
            notificationChannel.setDescription("Channel description");
            if (isLight) {
                notificationChannel.enableLights(true);
                notificationChannel.setLightColor(Color.RED);
            } else {
                notificationChannel.enableLights(false);
            }
            if (isVibrate) {
                notificationChannel.enableVibration(true);
                notificationChannel.setVibrationPattern(new long[]{0, 1000, 500, 1000});
            } else {
                notificationChannel.enableVibration(false);
            }

            notificationManager.createNotificationChannel(notificationChannel);
        }


        NotificationCompat.Builder notificationBuilder =
                                new NotificationCompat.Builder(activity, NOTIFICATION_CHANNEL_ID);

        notificationBuilder
                //.setDefaults(Notification.DEFAULT_ALL)
                //.setWhen(System.currentTimeMillis())
                .setContentTitle(activity.getResources().getString(R.string.app_name))
                .setContentText(activity.getResources().getString(textMessage))
                .setSmallIcon(R.drawable.ic_mortar_white)
                .setAutoCancel(true) //notification is canceled as soon as it is touched by the user
                //.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setContentIntent(contentIntent)
                ;
        if (isSound){
            notificationBuilder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
        }

        //notificationManager.notify(/*notification id*/1, notificationBuilder.build());
        /*

        //  Create a Notification Builder that will do the actual creation of the Notification
        //     and insert the PendingIntent into the Notification as it's ContentIntent
        NotificationCompat.Builder builder =
                (NotificationCompat.Builder) new NotificationCompat.Builder(activity)
                        .setContentTitle(activity.getResources().getString(R.string.app_name))
                        .setContentText(activity.getResources().getString(textMessage))
                        .setSmallIcon(R.drawable.ic_mortar_white)
                        // .setLargeIcon(((BitmapDrawable) activity.getResources().getDrawable(R.drawable.app_icon)).getBitmap())
                        .setAutoCancel(true)//notification is canceled as soon as it is touched by the user
                        .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                        .setContentIntent(contentIntent);
       */

        //build and return the Notification
        return notificationBuilder.build();
    }


    private  PendingIntent buildScheduleAlarmIntent(Context activity,
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





    void cancelNotificationAlarms(MMMainActivity activity){
        //get the PendingIntent that describes the action we desire,
        // so that it can be performed when the alarm goes off
        int notificationID = scheduleNotificationID;
        Notification notification = buildSchedNotification(activity, notificationID);
        PendingIntent alarmIntent = buildScheduleAlarmIntent(activity, notificationID, notification);

        AlarmManager alarmManager = (AlarmManager) activity.getSystemService(ALARM_SERVICE);
        if (alarmManager == null)return;

        //cancel any previous alarms
        alarmManager.cancel(alarmIntent);
    }




    //*****************************************************/
    /*       For sending a text that a dose is overdue    */
    /*          Alarm / Notification Utilities            */
    //*****************************************************/


    void createAlertAlarm(Context activity, long medAlertID){
        MMMedicationAlert medAlert =
                            MMMedicationAlertManager.getInstance().getMedicationAlert(medAlertID);
        if (medAlert == null)return;

        //Figure out when the Alarm should fire


        //get the last dose of this medication for this patient
        MMDoseManager doseManager = MMDoseManager.getInstance();
        MMDose mostRecentDose = doseManager.getMostRecentDose(medAlertID);

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
        //Note that the same requestCode is used here as for the Notification above
        PendingIntent alertIntent = buildAlertAlarmIntent(activity, medAlertID);

        AlarmManager alarmManager = (AlarmManager) activity.getSystemService((Context.ALARM_SERVICE));
        if (alarmManager == null)return;

        alarmManager.set(AlarmManager.RTC_WAKEUP, timeOverdueMillisec, alertIntent);
    }


    private  PendingIntent buildAlertAlarmIntent(Context activity,
                                            long  medAlertID){

        //Start by building an Intent targeting our AlarmReceiver,
        // and insert the ID and notification into this Intent
        Intent alarmIntent = new Intent(activity, MMAlarmReceiver.class);
        alarmIntent.putExtra(MMAlarmReceiver.ALARM_TYPE, MMAlarmReceiver.alertAlarmType);
        alarmIntent.putExtra(MMAlarmReceiver.MED_ALERT_ID, medAlertID);



        //Insert this intent into a wrapper that is used to schedule an Alarm
        //When the alarm triggers, the alarmIntent will be Broadcast
        //Our MMAlarmReceiver will receive the broadcast and know to text/email the alert
        return  PendingIntent.getBroadcast( activity,
                                            MMAlarmReceiver.alertRequestCode,
                                            alarmIntent,
                                            PendingIntent.FLAG_CANCEL_CURRENT);
    }


     void sendAlert(Context context,
                           long medAlertID,
                           long timeTakenMilliseconds){

        MMMedicationAlertManager medicationAlertManager = MMMedicationAlertManager.getInstance();
        MMMedicationAlert medicationAlert = medicationAlertManager.getMedicationAlert(medAlertID);

        String timeTakenString   = getDateTimeString(timeTakenMilliseconds);

        MMPersonManager personManager = MMPersonManager.getInstance();
        MMPerson notifyPerson = personManager.getPerson(medicationAlert.getForPatientID());
        String personNickname = notifyPerson.getNickname().toString();

        MMMedicationManager medicationManager = MMMedicationManager.getInstance();
        MMMedication medication = medicationManager.getMedicationFromID(medicationAlert.getMedicationID());
        String medicationName = medication.getMedicationNickname().toString();

        //String lineSep = System.getProperty("line.separator");
        String msg;
        if (timeTakenMilliseconds > 0) {
            msg = String.format(Locale.getDefault(),
                                "Patient: <%s> has not taken a dose of %s since %s. ",
                                personNickname, medicationName,timeTakenString);
        } else {
            msg = String.format(Locale.getDefault(),
                                "Patient: <%s> has never taken a dose of %s" ,
                                personNickname, medicationName );
        }


        int alertType = medicationAlert.getNotifyType();

        if (alertType == MMMedicationAlert.sNOTIFY_BY_TEXT) {
            sendSMSviaAPI(notifyPerson.getTextAddress().toString(), msg);
        } else if (alertType == MMMedicationAlert.sNOTIFY_BY_EMAIL){
            String subject = "Missed Medication Dose";
            sendEmail(context, subject, notifyPerson.getEmailAddress().toString(), msg);
        } //else if any other type in the future......

    }




    //************************************/
    /*         Send with Intents         */
    //************************************/

    void exportEmail(Context context, String subject, String emailAddr, String body, String chooser_title){

        // TODO: 12/13/2017 have to set the body to the input parameter
        Intent intent2 = new Intent();
        intent2.setAction(Intent.ACTION_SEND);
        intent2.setType("message/rfc822");
        intent2.putExtra(Intent.EXTRA_EMAIL,   emailAddr);
        intent2.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent2.putExtra(Intent.EXTRA_TEXT,    chooser_title );
        context.startActivity(intent2);
    }

    void exportText(Context context,String subject,String body, String chooser_title){
        Intent exportIntent = new Intent(Intent.ACTION_SEND);
        exportIntent.setType("text/plain");

        exportIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, subject);
        exportIntent.putExtra(android.content.Intent.EXTRA_TEXT, body);

        //always display the chooser
        if (exportIntent.resolveActivity(context.getPackageManager()) != null)
            context.startActivity(Intent.createChooser(exportIntent, chooser_title ));
        else {
            MMUtilities.getInstance().showStatus(context, R.string.export_no_app);
        }
    }

    void exportSMS(Context context, String subject, String body){
        // TODO: 12/13/2017 Should subject be removed from this method? It isn't used
        Intent sendIntent = new Intent(Intent.ACTION_VIEW);
        sendIntent.putExtra("sms_body", body);
        sendIntent.setType("vnd.android-dir/mms-sms");
        context.startActivity(sendIntent);
    }

    //************************************/
    /*         Send Email using Intent   */
    //************************************/
    void sendEmail(Context context, String subject, String toAddress, String msg) {

        String[] TO = {toAddress};     //{"someone@gmail.com"};
        //String[] CC = {"elisabethhuhn@gmail.com"};
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setData(Uri.parse("mailto:"));
        emailIntent.setType("text/plain");


        emailIntent.putExtra(Intent.EXTRA_EMAIL, TO);
        //emailIntent.putExtra(Intent.EXTRA_CC, CC);
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
        emailIntent.putExtra(Intent.EXTRA_TEXT, msg);

        try {
            context.startActivity(Intent.createChooser(emailIntent, "Send mail..."));
            ((MMMainActivity)context).finish();

        } catch (android.content.ActivityNotFoundException ex) {
            errorHandler(context, R.string.no_email_client);
        }
    }




    //************************************/
    /*         Send Text to phone #      */
    //************************************/


    void sendSMSviaAPI(String phoneNo, String msg){
        msg = "sendSMSviaAPI: " + msg;
        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(phoneNo, null, msg, null, null);
    }


    //************************************/
    /*             File utilities        */
    //************************************/

    /* Checks if external storage is available for read and write */
    boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return (Environment.MEDIA_MOUNTED.equals(state)) ;
    }

    /* Checks if external storage is available to at least read */
    boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        return (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) ;
    }

}
