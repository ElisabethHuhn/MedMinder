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
import android.os.Environment;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.NotificationCompat;
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
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

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


    public static final boolean BUTTON_DISABLE = false;
    public static final boolean BUTTON_ENABLE  = true;

    public static final boolean HOUR12FORMAT = false;
    public static final boolean HOUR24FORMAT = true;


    public static final long    ID_DOES_NOT_EXIST = -1;

    public static final long milliPerSecond   = 1000;
    public static final long secondsPerMinute = 60;
    public static final long minutesPerHour   = 60;



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
    public static MMUtilities getInstance() {
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
    public  int convertPixelsToDp(Context context, int sizeInDp) {
        //int sizeInDp = 10; //padding between buttons
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (sizeInDp * scale + 0.5f); // dpAsPixels;
    }

    //Just a stub for now, but figure out what to do
    public  void errorHandler(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public  void errorHandler(Context context, int messageResource) {
        errorHandler(context, context.getString(messageResource));
    }

    public  void showStatus(Context context, String message){
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public  void showStatus(Context context, int messageResource){
        showStatus(context, context.getString(messageResource));
    }


    public  void showHint(View view, String msg){
        Snackbar.make(view, msg, Snackbar.LENGTH_LONG).setAction("Action", null).show();
    }


    //****************************************/
    /*    ConcurrentDose row list builder    */
    //****************************************/

    public  EditText createDoseEditText(Context context, int padding){
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


    //************************************/
    /*         Date / Time Utilities     */
    //************************************/

    public long convertMinutesToMilli(long minutes){
        return minutes * secondsPerMinute * milliPerSecond;
    }

    public long convertMilliToMinutes(long milli){
        return milli / (milliPerSecond * secondsPerMinute);
    }


    //The only reason these are here is so that the app
    // will use a consistent method of displaying dates
    public  String getDateTimeString(){
        return DateFormat.getDateTimeInstance().format(new Date());
    }

    public  String getDateTimeString(long milliSeconds){
        Date date = new Date(milliSeconds);
        return DateFormat.getDateTimeInstance().format(date);
    }

    public String getDateTimeStr(long milliseconds){
        Date date = new Date(milliseconds);
        //SimpleDateFormat dateFormat = new SimpleDateFormat("h:mm a", Locale.US);
        SimpleDateFormat dateFormat = new SimpleDateFormat("d MMM yy HH:mm", Locale.US);
        TimeZone gmtTz   = TimeZone.getTimeZone("GMT");
        dateFormat.setTimeZone(gmtTz);

        return dateFormat.format(date);
    }

    public  String getDateString(){
        return  DateFormat.getDateInstance().format(new Date());
    }

    public  String getDateString(long milliSeconds){
        Date date = new Date(milliSeconds);
        return DateFormat.getDateInstance().format(date);
    }

    public long getTimezoneOffset() {
        TimeZone tz = TimeZone.getDefault();
        Calendar cal = GregorianCalendar.getInstance(tz);
        long offsetInMillis = (long) tz.getOffset(cal.getTimeInMillis());

        return offsetInMillis;
    }

    public long getDSTOffset() {
        //Creaate a calendar with this time now
        Calendar nowCal = Calendar.getInstance();

        //This offset indicates the number of milliseconds due to DST in effect
        // as of the date of the Calendar
        long dstOffset = nowCal.get(Calendar.DST_OFFSET);

        return dstOffset;
    }




    // TODO: 5/28/2017 Get rid of these debug methods


    public boolean testDate(MMMainActivity activity){
        boolean is24Format = false;
        boolean returnCode = true;

        //get the value of the offset between GMT and the local timezone
        long tzOffset = getTimezoneOffset();

        //Creaate a calendar with this time now
        Calendar nowCal = Calendar.getInstance();

        //This offset indicates the number of milliseconds due to DST in effect
        // as of the date of the Calendar
        long dstOffset = nowCal.get(Calendar.DST_OFFSET);


        // convert milliseconds to Strings [separating Time and Date during the conversion]
        // Re-convert Strings back to milliseconds
        // then add them back together
        long nowTime = nowCal.getTimeInMillis();
        String nowTimeString = getTimeString(activity, nowTime);
        String nowDateString = getDateString(nowTime);
        //ASSERT nowTimeSTring should be since midnight in local time zone [pure time at epoch 1970]
        //ASSERT nowDateString should be today's date, local time zone
        //RESULT for Time: Success
        //RESULT for Date: Success



        //Convert back to milliseconds  [pure time at epoch 1970]
        Date nowTimeRevertedDate     = convertStringToTimeDate(activity, nowTimeString, true);
        long nowTimeReverted         = nowTimeRevertedDate.getTime();
        String nowTimeRevMSSTring    = getTimeString(activity, nowTimeReverted);

        //Date = milliseconds Today at midnight
        Date nowDateRevertedDate     = convertStringToTimeDate(activity, nowDateString, false);
        long nowDateReverted         = nowDateRevertedDate.getTime();
        String nowDateRevDateString  = getTimeString(activity, nowDateReverted);
        String nowRevDateString      = getDateString(nowDateReverted);
        //Result for Time - Correct EST!!!! for Jan 1, 1970 [Because that is what is in effect in January!!!]
        //Result for Date - Correct Midnight EDT for today's date

        //Add to get milliseconds, then convert milliseconds to Strings
        long   nowTimeRevertedSum    = nowTimeReverted + nowDateReverted;
        String incorrectTimeString   = getTimeString(activity, nowTimeReverted);
        //RESULT: - Wrong by GMT offset - Can not do math with local timezone
        //          But also can not add EST Date to EDT Time and expect to get something meaningful

        //Need offset for local timezone from GMT: GMT = local - offsetTZ
        //Need offset for DST as the time is for standard time in Jan 1 1970:
        // IF current time has DST, Time = Time + offset
        long   nowTimeRevertedWOffset = (nowTimeReverted  + tzOffset) +
                                        (nowDateReverted - dstOffset);
        String nowTimeRevOffsetString = getTimeString(activity, nowTimeRevertedWOffset);
        //RESULT: CORRECT!!! the offsets are properly applied

        String nowDateRevertedString = getDateString(nowTimeReverted);
        //Result for Date - Correct


        //Experiment more with separating time and date then adding them back together
        //Create a new Calendar corresponding to last night's midnight
        Calendar calMidnight             = getCalAtMidnight(nowTime);

        long msAtMidnightLocal           = calMidnight.getTimeInMillis();
        String midnightLocalTimeString   = getTimeString(activity, msAtMidnightLocal);
        String midnightLocalDateString   = getDateString(msAtMidnightLocal);

        Date midnightLocalDate           = convertStringToTimeDate(activity, midnightLocalDateString, false);
        long msAtMidnightLocalRev        = midnightLocalDate.getTime();
        String msAtMidnightGMTRevString  = getTimeString(activity, msAtMidnightLocalRev);


        long msAtMidnightGMT             = msAtMidnightLocal - tzOffset + dstOffset;
        String midnightGMTTimeString     = getTimeString(activity, msAtMidnightGMT);

        String midnightDateString        = getDateString(msAtMidnightGMT);
        //ASSERT Should see local and GMT
        //RESULT Time Local: Correct
        //Result Time GMT:   Correct, but it requires both offsets
        //Result Date:       Correct




        //convert String to milliseconds since midnight
        long msSinceMidnightGMT = convertStringToMilliSinceMidnightGMT(activity, nowTimeString);
        long msSinceMidnightLocal = nowTime - msAtMidnightGMT ;
        //ASSERT: msSinceMidnightGMT   == msSinceMidnightLocal - tzOffset
        //ASSERT: msSinceMidnightLocal == msSinceMidnightGMT   + tzOffset
        //RESULT: msSinceMidnightGMT <  >,     msSinceMidnightLocal   <  >

        String msSinceMidnightGMTString    = getTimeString(activity, msSinceMidnightGMT);
        String msSinceMidnightLocalString  = getTimeString(activity, msSinceMidnightLocal);
        //RESULT:  msSinceMidnightGMTString <  >,     msSinceMidnightLocalString <  >


        //convert the two Date strings into milliseconds
        Date dateNow      = convertStringToTimeDate(activity, nowDateString, false);
        Date dateMidnight = convertStringToTimeDate(activity, midnightDateString, false);
        //RESULT: Success - midnight of today's date EDT
        long msDateNowEDT      = dateNow.getTime();
        long msDateMidnightEDT = dateMidnight.getTime();
        //RESULT; both correct for Midnight EDT


        //Add milliseconds to get a value for now
        long msAgainNowGMT = (msDateNowEDT - tzOffset) + msSinceMidnightGMT;
        Date againDate = new Date(msAgainNowGMT);

        String timeAgainString = getTimeString(activity, msAgainNowGMT);
        String dateAgainString = getDateString(msAgainNowGMT);
        //RESULT Time again String:
        //RESULT Date again String: Correct


        long checkMsAgainNowGMT  = (msDateNowEDT - tzOffset) + (msSinceMidnightLocal - tzOffset);
        Date checkAgainDate = new Date(checkMsAgainNowGMT);
        String checkTimeAgainString = getTimeString(activity, checkMsAgainNowGMT);
        String checkDateAgainString = getDateString(checkMsAgainNowGMT);
        //RESULT Time again String:
        //RESULT Date again String: Correct

        String timesetString = getTimeString(activity, nowTime);
        timesetString        = getTimeString(activity, msAtMidnightLocal);
        timesetString        = getTimeString(activity, msSinceMidnightGMT);
        timesetString        = getTimeString(activity, msSinceMidnightLocal);
        timesetString        = getTimeString(activity, msAgainNowGMT);
        timesetString        = getTimeString(activity, checkMsAgainNowGMT);




        return returnCode;
    }








    public Calendar getCalAtMidnight(long millisec){
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(millisec);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE,      0);
        cal.set(Calendar.SECOND,      0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal;
    }





    //This method is used to get current time as a string
    public  String getTimeString(MMMainActivity activity){
        boolean is24Format = MMSettings.getInstance().getClock24Format(activity);
        String timeFormat = getTimeFormatString(is24Format);
        Date date = new Date();
        return getTimeString(timeFormat, date);
    }

    //This method is used to get the string corresponding to a value of milliseconds since 1970
    public  String getTimeString(MMMainActivity activity, long milliSeconds){
        boolean is24Format = MMSettings.getInstance().getClock24Format(activity);
        return getTimeString(milliSeconds, is24Format);
    }

    public  String getTimeString(long milliSeconds, boolean is24format){
        String timeFormat = getTimeFormatString(is24format);
        Date dateFromMilli = new Date(milliSeconds);
/*
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(milliSeconds);
        Date dateFromCalendar = c.getTime();

        long offsetMsTz = getTimezoneOffset();
        long offsetMs   = offsetMsTz + milliSeconds;
        Date dateWithOffset = new Date(offsetMs);

        c.set(Calendar.HOUR_OF_DAY, hours);
        c.set(Calendar.MINUTE, minutes);
        c.set(Calendar.SECOND, 0);
        dateFromCalendar = c.getTime();
*/

        return getTimeString(timeFormat, dateFromMilli);
    }

    private String getTimeString(String timeFormat, Date date){
        SimpleDateFormat dateFormat = new SimpleDateFormat(timeFormat, Locale.getDefault());

        return dateFormat.format(date);
    }






    public SimpleDateFormat getTimeFormat(boolean is24format){
        String timeFormat = getTimeFormatString(is24format);
        return new SimpleDateFormat(timeFormat, Locale.getDefault());
    }

    public  String getTimeFormatString(boolean is24format){
        CharSequence timeFormat = "h:mm a";
        if (is24format){
            timeFormat = "H:mm a";
        }
        return timeFormat.toString();
    }

    public SimpleDateFormat getDateFormat(){
        return new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());
    }




    public long convertGMTtoLocal(long milliGMT){
        long offsetTZ  = getTimezoneOffset();
        long offsetDST = getDSTOffset();
        long localTimeMilli = milliGMT + offsetDST - offsetTZ;

        // TODO: 6/2/2017 get rid of debug String
        boolean is24Format = true;
        String localTimeString = getTimeString(localTimeMilli, is24Format);
        return localTimeMilli;
    }

    public long convertLocaltoGMT(long milliLocal){
        long offsetTZ  = getTimezoneOffset();
        long offsetDST = getDSTOffset();
        long gmtTimeMilli = milliLocal - offsetDST + offsetTZ;

        // TODO: 6/2/2017 get rid of debug String
        boolean is24Format = true;
        String gmtTimeString = getTimeString(gmtTimeMilli, is24Format);
        return gmtTimeMilli;
    }

    public int convertMinutesGMTtoLocal(int minutesGMT){
        long timeDueMilli = (long) minutesGMT * MMUtilities.secondsPerMinute * MMUtilities.milliPerSecond;
        long offsetTZ  = MMUtilities.getInstance().getTimezoneOffset();
        long offsetDST = MMUtilities.getInstance().getDSTOffset();
        timeDueMilli = timeDueMilli + offsetTZ - offsetDST;

        // TODO: 6/2/2017 get rid of debug string
        boolean is24Format = true;
        String timeString = getTimeString(timeDueMilli, is24Format);

        return (int)(timeDueMilli / (MMUtilities.secondsPerMinute * MMUtilities.milliPerSecond));
    }

    public int convertMinutesLocaltoGMT(int minutesLocal){
        long timeDueMilli = (long) minutesLocal * MMUtilities.secondsPerMinute * MMUtilities.milliPerSecond;
        long offsetTZ  = MMUtilities.getInstance().getTimezoneOffset();
        long offsetDST = MMUtilities.getInstance().getDSTOffset();
        timeDueMilli = timeDueMilli - offsetTZ + offsetDST;

        // TODO: 6/2/2017 get rid of debug string
        boolean is24Format = true;
        String timeString = getTimeString(timeDueMilli, is24Format);

        return (int)(timeDueMilli / (MMUtilities.secondsPerMinute * MMUtilities.milliPerSecond));
    }



    public Date convertStringToTimeDate(MMMainActivity activity,
                                        String timeSinceMidnightString,
                                        boolean isTimeFlag){
        //Get the clock format
        boolean is24Format = MMSettings.getInstance().getClock24Format(activity);

        Date date = null;

        SimpleDateFormat timeFormat;

        if (isTimeFlag){
            timeFormat = getTimeFormat(is24Format);
        } else {
            timeFormat = getDateFormat();
        }
        //TimeZone gmtTz   = TimeZone.getTimeZone("GMT");
        //timeFormat.setTimeZone(gmtTz);

        try {
            date = timeFormat.parse(timeSinceMidnightString);
        } catch (Exception e) {
            MMUtilities.getInstance().errorHandler(activity, R.string.error_parsing_date_time);
            e.printStackTrace();
        }

        return date;
    }



    //Time string is since midnight, result gives today at that time
    public long convertStringToMilliSinceMidnightToday(MMMainActivity activity,
                                                       String timeSinceMidnightString){
        long msSinceMidnight =
                convertStringToMilliSinceMidnightGMT(activity, timeSinceMidnightString);
        if (msSinceMidnight == 0)return 0;

        long startWithMidnight = MMUtilities.getInstance().getStartMidnightToday();
        return (msSinceMidnight + startWithMidnight);
    }


    //Time string is since midnight, result gives milliseconds since midnight only
    public long convertStringToMilliSinceMidnightGMT(MMMainActivity activity,
                                                     String timeSinceMidnightString){
        boolean isTimeFlag = true;
        Date date = convertStringToTimeDate(activity, timeSinceMidnightString, isTimeFlag);
        if (date == null){
            return 0;
        }

        return date.getTime();
    }

    //Time string is since midnight
    public long convertStringToMinutesSinceMidnight(MMMainActivity activity,
                                                    String timeSinceMidnightString){
        long msSinceMidnight =
                            convertStringToMilliSinceMidnightGMT(activity, timeSinceMidnightString);
        if (msSinceMidnight == 0)return 0;

        return msSinceMidnight / 60000;
    }



    public long getStartMidnightToday(){

        try {
            DateFormat justDay       = new SimpleDateFormat("yyyyMMdd");
            TimeZone gmtTz   = TimeZone.getTimeZone("GMT");
            justDay.setTimeZone(gmtTz);

            Date thisMorningMidnight = justDay.parse(justDay.format(new Date()));
            return thisMorningMidnight.getTime();
        } catch (Exception e){
            e.printStackTrace();
        }
        return 0;

    }


    public long getLastTakenMinutes(long timeTaken){

        //compare whether the last dose was taken today
        Date lastTaken = new Date(timeTaken);

        Date now = new Date();

        SimpleDateFormat fmt   = new SimpleDateFormat("yyyyMMdd");
        boolean firstDoseOfDay = !(fmt.format(lastTaken).equals(fmt.format(now)));

        long lastTakenMinutes = 0;
        if (!firstDoseOfDay) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(lastTaken);
            lastTakenMinutes = getMinutesFromCalendar(calendar);
        }
        return lastTakenMinutes;
    }


    public long getMinutesFromCalendar (Calendar calendar){
        return (calendar.get(Calendar.HOUR_OF_DAY) * 60) + calendar.get(Calendar.MINUTE);
    }

    //************************************/
    /*    get Data Object instances      */
    //************************************/


    //************************************/
    /*         Widget Utilities          */
    //************************************/
    public  void enableButton(Context context, Button button, boolean enable){
        button.setEnabled(enable);
        if (enable == BUTTON_ENABLE) {
            button.setTextColor(ContextCompat.getColor(context, R.color.colorTextBlack));
        } else {
            button.setTextColor(ContextCompat.getColor(context, R.color.colorGray));
        }
    }

    public  void showSoftKeyboard(FragmentActivity context, EditText textField){
        //Give the view the focus, then show the keyboard

        textField.requestFocus();
        InputMethodManager imm =
                (InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);
        //second parameter is flags. We don't need any of them
        imm.showSoftInput(textField, InputMethodManager.SHOW_FORCED);

    }

    public  void hideSoftKeyboard(FragmentActivity context){
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

        //close the keyboard
        context.getWindow().
                setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);


    }

    public  void toggleSoftKeyboard(FragmentActivity context){
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


    public  void clearFocus(FragmentActivity context){
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
    public void enableAlarmReceiver(Context activity){
        //Enable the Alarm receiver. It will stay enabled across reboots
        ComponentName receiver = new ComponentName(activity, MMAlarmReceiver.class);
        PackageManager pm = activity.getPackageManager();

        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);
    }



    public void createScheduleNotification(Context activity, long timeOfDayMillisec) {
        //start by building the Notification
        int requestCode = scheduleNotificationID;
        Notification notification = buildSchedNotification(activity, requestCode);

        //But we need to set an Alarm Intent to send to the Alarm Manager.
        PendingIntent alarmIntent = buildScheduleAlarmIntent(activity, requestCode, notification);
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




    public void cancelNotificationAlarms(Context activity, int minutesSinceMidnight){
        //get the PendingIntent that describes the action we desire,
        // so that it can be performed when the alarm goes off
        int notificationID = scheduleNotificationID;
        Notification notification = buildSchedNotification(activity, notificationID);
        PendingIntent alarmIntent = buildScheduleAlarmIntent(activity, notificationID, notification);

        AlarmManager alarmManager = (AlarmManager) activity.getSystemService(ALARM_SERVICE);

        //cancel any previous alarms
        alarmManager.cancel(alarmIntent);
    }


    private  Notification buildSchedNotification(Context activity, int requestCode) {
        return buildNotification(activity, requestCode, MMUtilities.ID_DOES_NOT_EXIST);
    }

    private  Notification buildNotification(Context activity,
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

        //  Create a Notification Builder that will do the actual creation of the Notification
        //     and insert the PendingIntent into the Notification as it's ContentIntent
        NotificationCompat.Builder builder =
                (NotificationCompat.Builder) new NotificationCompat.Builder(activity)
                        .setContentTitle(activity.getResources().getString(R.string.app_name))
                        .setContentText(activity.getResources().getString(textMessage))
                        .setSmallIcon(R.drawable.ground_station_icon)
                        // .setLargeIcon(((BitmapDrawable) activity.getResources().getDrawable(R.drawable.app_icon)).getBitmap())
                        .setAutoCancel(true)//notification is canceled as soon as it is touched by the user
                        .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                        .setContentIntent(contentIntent);

        //build and return the Notification
        return builder.build();
    }



    //*****************************************************/
    /*       For sending a text that a dose is overdue    */
    /*          Alarm / Notification Utilities            */
    //*****************************************************/


    public void createAlertAlarm(Context activity, long medAlertID){
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


    public  void sendAlert(Context context,
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

        // TODO: 5/6/2017 Use string format here
        String lineSep = System.getProperty("line.separator");
        String msg;
        if (timeTakenMilliseconds > 0) {
             msg = "Patient: <" + personNickname + "> has not taken a dose of " + medicationName +
                   lineSep + "since " + timeTakenString + ". ";
        } else {
            msg = "Patient: <" + personNickname + "> has never taken a dose of " + medicationName ;
        }


        int alertType = medicationAlert.getNotifyType();
        if (alertType == MMMedicationAlert.sNOTIFY_BY_TEXT) {
            sendSMSviaAPI(context, notifyPerson.getTextAddress().toString(), msg);
        } else if (alertType == MMMedicationAlert.sNOTIFY_BY_EMAIL){
            String subject = "Missed Medication Dose";
            sendEmail(context, subject, notifyPerson.getEmailAddress().toString(), msg);
        } //else if any other type in the future......
    }


    //************************************/
    /*         Send Email using Intent   */
    //************************************/
    public void sendEmail(Context context, String subject, String toAddress, String msg) {

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
            Toast.makeText(context,
                    "There is no email client installed.", Toast.LENGTH_SHORT).show();
        }
    }


/*
http://stackoverflow.com/questions/2020088/sending-email-in-android-using-javamail-api-without-using-the-default-built-in-a/2033124#2033124

    public void sendEmailNoIntent (){
        try {
            GMailSender sender = new GMailSender("username@gmail.com", "password");
            sender.sendMail("This is Subject",
                    "This is Body",
                    "user@gmail.com",
                    "user@yahoo.com");
        } catch (Exception e) {
            Log.e("SendMail", e.getMessage(), e);
        }
    }
*/

    //************************************/
    /*         Send Text to phone #      */
    //************************************/


    public void sendSMSviaAPI(Context activity, String phoneNo, String msg){
        msg = "sendSMSviaAPI: " + msg;
        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(phoneNo, null, msg, null, null);
    }


    //************************************/
    /*             File utilities        */
    //************************************/

    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    /* Checks if external storage is available to at least read */
    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }

}
