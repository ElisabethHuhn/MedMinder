package com.androidchicken.medminder;

import android.widget.EditText;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by Elisabeth Huhn on 9/18/2017.
 *
 * This class contains the Time manipulation utilities used by the application
 * It has been stripped out of utilities because of the unique nature of the problems
 * caused by time zone offsets, DST offsets, local time to GMT and vice verse
 */

class MMUtilitiesTime {

    // ************************************************************* //
    //                                                               //
    //                          Constants                            //
    //                                                               //
    // ************************************************************* //

    private static long MsPerSec   = 1000;
    private static long SecPerMin  = 60;
    private static long MinPerHour = 60;
    private static long HourPerDay = 24;

    private static long MsPerHour  = (MinPerHour * SecPerMin * MsPerSec);
    private static long MsPerMin   = (SecPerMin * MsPerSec);

    // ************************************************************* //
    //                                                               //
    //            Utility Methods                                    //
    //                                                               //
    // ************************************************************* //

    //returns time today corresponding to the time being displayed in the UI view
    static long getTodayTime(MMMainActivity activity,
                             EditText       timeInputView){
        //Get the string from the UI View
        //This is local time
        String timeString = timeInputView.getText().toString();

        boolean isTimeFlag = true; //as opposed to date
        long msSinceMidnight = convertStringToTimeMs(activity, timeString, isTimeFlag);

        //get midnight
        long timeAtMidnightMs = getMidnightInMS();
        long totalTime = getTotalTime(timeAtMidnightMs, msSinceMidnight);
        return totalTime;
    }

    static long getTotalTime(long dateMs, long timeMs){
        //simple addition gives GMT, so we need to convert to local here
        long totalTime = dateMs + timeMs;

        return convertGMTtoLocal(totalTime);
    }


    //Assumes input time is GMT. Returns equivalent time in local time zone
    static long convertGMTtoLocal(long milliGMT){

        long offsetTZ  = getTimezoneOffset();
        long offsetDST = getDSTOffset();
        long localTimeMilli = milliGMT + offsetTZ - offsetDST; //

        return localTimeMilli;
    }

    //Assumes input time is Local. Returns equivalent time in GMT
    static long convertLocaltoGMT(long milliLocal){

        long offsetTZ     = getTimezoneOffset();
        long offsetDST    = getDSTOffset();
        long gmtTimeMilli = milliLocal  - offsetTZ + offsetDST;// ;

        return gmtTimeMilli;
    }



    static long addOffset(long timeAtMidnight, long msSinceMidnight){
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timeAtMidnight);

        int hour   = (int) (msSinceMidnight / MsPerHour);
        int minute = (int)((msSinceMidnight - (hour * MsPerHour))/MsPerMin);
        int second = (int)((msSinceMidnight - (hour * MsPerHour) - (minute * MsPerMin))/MsPerSec);
        int millis = (int)((msSinceMidnight - (hour * MsPerHour) - (minute * MsPerMin) - (second * MsPerSec))/MsPerSec);

        cal.set(Calendar.HOUR,   hour);
        cal.set(Calendar.MINUTE, minute);
        cal.set(Calendar.SECOND, second);
        cal.set(Calendar.MILLISECOND, millis);

        return cal.getTimeInMillis();

    }




    //returns the number of MS at midnight today (i.e. previous midnight)
    // parameter determines whether the time is local or GMT
    static long getMidnightInMS() {

        // get a calendar instance for midnight time
        Calendar midnightCalendar = Calendar.getInstance();
        midnightCalendar.set(Calendar.HOUR_OF_DAY, 0);
        midnightCalendar.set(Calendar.MINUTE, 0);
        midnightCalendar.set(Calendar.SECOND, 0);
        long midnightInMS = midnightCalendar.getTimeInMillis();

        return midnightInMS;
    }






    static boolean isDayOdd(long timeInMs) {

        // get a calendar instance for midnight time
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timeInMs);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int remainder = day % 2;
        return (remainder > 0);
    }



    // ****************************** //
    //                                //
    //   Conversion Utilities         //
    //                                //
    // ****************************** //


    static String convertMStoDateTimeString(MMMainActivity activity, long timeMs){
        //Get the clock format
        boolean is24Format = MMSettings.getInstance().getClock24Format(activity);

        Date date = new Date(timeMs);
        SimpleDateFormat dateTimeFormat = getDateTimeFormat(is24Format);
        String returnString = null;



        try {
            returnString = dateTimeFormat.format(date);
        } catch (Exception e) {
            MMUtilities.getInstance().errorHandler(activity, R.string.error_parsing_date_time);
        }

        if (returnString == null)return " ";
        return returnString;

    }


    //parameter indicates whether Time String (true) or Date String (false) is to returned
    static String convertTimeMStoString(MMMainActivity activity,
                                        long timeMs,
                                        boolean isTimeFlag){
        //Get the clock format
        boolean is24Format = MMSettings.getInstance().getClock24Format(activity);

        Date date = new Date(timeMs);
        SimpleDateFormat timeFormat;
        String returnString = null;

        if (isTimeFlag){
            //true means time to be returned
            timeFormat = getTimeFormat(is24Format);
        } else {
            //false means date to be returned
            timeFormat = getDateFormat();
        }


        try {
            returnString = timeFormat.format(date);
        } catch (Exception e) {
            MMUtilities.getInstance().errorHandler(activity, R.string.error_parsing_date_time);

        }

        if (returnString == null)return "";
        return returnString;

    }



    //returns MS corresponding to the time today in the String
    static long convertStringToTimeMs(MMMainActivity activity,
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


        try {
            date = timeFormat.parse(timeSinceMidnightString);
            if (!timeSinceMidnightString.equals(timeFormat.format(date))) {
                date = null;
            }
        } catch (Exception e) {
            MMUtilities.getInstance().errorHandler(activity, R.string.error_parsing_date_time);

        }

        if (date == null) return 0;
        return date.getTime();

    }





    // ****************************** //
    //                                //
    //   Time Methods Needed in App   //
    //                                //
    // ****************************** //




    static long getCurrentMilli(int minutesSinceMidnight){
        //get calendar in local time zone
        int hours = minutesSinceMidnight / (int)MinPerHour;
        int minutes = minutesSinceMidnight - (hours * (int)MinPerHour);

        Calendar c = getCalendar(hours, minutes);

        return c.getTimeInMillis();
    }

    static Calendar getCalendar(int hours, int minutes){
        //get calendar in local time zone
        Calendar c = Calendar.getInstance();

        //set local hours and minutes
        c.set(Calendar.HOUR_OF_DAY, hours);
        c.set(Calendar.MINUTE, minutes);
        c.set(Calendar.SECOND, 0);

        return c;
    }




    static long getGmtNow(){
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("gmt"));
        return calendar.getTimeInMillis();
    }

    static String getShortDate(long milliseconds, boolean inGMT, boolean outGMT){
        java.text.DateFormat shortFormatter = java.text.DateFormat.getDateInstance(
                java.text.DateFormat.SHORT); // one of SHORT, MEDIUM, LONG, FULL, or DEFAULT
        if (outGMT) {
            shortFormatter.setTimeZone(TimeZone.getTimeZone("GMT"));
        }
        return shortFormatter.format(milliseconds);
    }

    static String getTimeStr(MMMainActivity activity, long timeMs, boolean inGMT, boolean outGMT){
        Calendar calendar;
        TimeZone tz;
        //The formatter created depends upon whether the input is GMT or Local
        if (inGMT){
            tz = TimeZone.getTimeZone("GMT");//this creates a calendar on local as above
            calendar = GregorianCalendar.getInstance(tz);

        } else {
            // Calendar - Local
            tz = TimeZone.getDefault();
            calendar = Calendar.getInstance(tz);
        }

        calendar.setTimeInMillis(timeMs);

        boolean is24Format  = MMSettings.getInstance().getClock24Format(activity);

        if (outGMT){
            tz = TimeZone.getTimeZone("GMT");
        } else {
            tz = TimeZone.getDefault();
        }
        SimpleDateFormat df = new SimpleDateFormat(getTimeFormatString(is24Format), Locale.getDefault());
        df.setTimeZone(tz);

        return df.format(calendar.getTimeInMillis());

    }







    static long convertMsToMin (long milliSeconds){
        return milliSeconds * MsPerSec * SecPerMin;
    }

    static long convertMinutesToMs (long minutes){
        return minutes * SecPerMin * MsPerSec;
    }




    // ****************************** //
    //                                //
    //      Offset Utilities          //
    //                                //
    // ****************************** //

    static long getTimezoneOffset() {
        TimeZone tz = TimeZone.getDefault();
        Calendar cal = GregorianCalendar.getInstance(tz);
        long offsetInMillis = (long) tz.getOffset(cal.getTimeInMillis());

        return offsetInMillis;
    }

    static long getDSTOffset() {
        //Creaate a calendar with this time now
        Calendar nowCal = Calendar.getInstance();

        //This offset indicates the number of milliseconds due to DST in effect
        // as of the date of the Calendar
        long dstOffset = nowCal.get(Calendar.DST_OFFSET);

        return dstOffset;
    }

    static int getTimezoneOffsetMinutes(){
        long ms = getTimezoneOffset();
        return (int)(ms / (MsPerSec * SecPerMin));
    }

    static int getDSTOffsetMinutes(){
        long ms = getDSTOffset();
        return (int) (ms /(MsPerSec * SecPerMin));
    }




    // ****************************** //
    //                                //
    //   Time/Date string Utilities   //
    //                                //
    // ****************************** //

    static String getTimeString(MMMainActivity activity){
        boolean is24Format  = MMSettings.getInstance().getClock24Format(activity);
        Calendar calendar   = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat(getTimeFormatString(is24Format), Locale.getDefault());
        return df.format(calendar.getTimeInMillis());
    }
    static String getTimeString(long milliseconds, boolean is24Format){

        Calendar calendar   = Calendar.getInstance();
        calendar.setTimeInMillis(milliseconds);
        SimpleDateFormat df = new SimpleDateFormat(getTimeFormatString(is24Format), Locale.getDefault());
        return df.format(calendar.getTimeInMillis());
    }




    static  String getDateString(long milliSeconds){
        Date date = new Date(milliSeconds);
        return DateFormat.getDateInstance().format(date);
    }





    // ****************************** //
    //                                //
    //   Format Utilities             //
    //                                //
    // ****************************** //


    static SimpleDateFormat getTimeFormat(boolean is24format){
        String timeFormat = getTimeFormatString(is24format);
        return new SimpleDateFormat(timeFormat, Locale.getDefault());
    }

    static  String getTimeFormatString(boolean is24format){
        CharSequence timeFormat = "h:mm a";
        if (is24format){
            timeFormat = "H:mm a";
        }
        return timeFormat.toString();
    }

    static SimpleDateFormat getDateFormat(){
        return new SimpleDateFormat(getDateFormatString(), Locale.getDefault());
    }
    static String getDateFormatString(){
        return "MMM d, yyyy";
    }

    static SimpleDateFormat getDateTimeFormat(boolean is24format){
        String dateTimeFormat = getDateFormatString() + " " + getTimeFormatString(is24format);
        return new SimpleDateFormat(dateTimeFormat, Locale.getDefault());
    }







}
