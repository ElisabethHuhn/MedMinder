package com.androidchicken.medminder;

import android.content.Context;
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
        return getTotalTime(timeAtMidnightMs, msSinceMidnight);
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
        return milliGMT + offsetTZ - offsetDST; //
    }

    //Assumes input time is Local. Returns equivalent time in GMT
    static long convertLocaltoGMT(long milliLocal){

        long offsetTZ     = getTimezoneOffset();
        long offsetDST    = getDSTOffset();

        //gmtTimeMilli
        return (milliLocal  - offsetTZ + offsetDST);
    }


    //returns the number of MS at midnight today (i.e. previous midnight)
    // parameter determines whether the time is local or GMT
    private static long getMidnightInMS() {

        // get a calendar instance for midnight time
        Calendar midnightCalendar = Calendar.getInstance();
        midnightCalendar.set(Calendar.HOUR_OF_DAY, 0);
        midnightCalendar.set(Calendar.MINUTE, 0);
        midnightCalendar.set(Calendar.SECOND, 0);
        return midnightCalendar.getTimeInMillis();
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
        SimpleDateFormat dateTimeFormat = getDateTimeFormat(activity, is24Format);
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
            timeFormat = getTimeFormat(activity, is24Format);
        } else {
            //false means date to be returned
            timeFormat = getDateFormat(activity);
        }


        try {
            returnString = timeFormat.format(date);
        } catch (Exception e) {
            MMUtilities.getInstance().errorHandler(activity, R.string.error_parsing_date_time);

        }

        if (returnString == null)return "";
        return returnString;

    }








    static Date convertStringToDate(MMMainActivity activity,
                             String timeSinceMidnightString){
        Date date = null;

        SimpleDateFormat timeFormat;


        timeFormat = getEditTextDateFormat(activity);

        //TimeZone gmtTz   = TimeZone.getTimeZone("GMT");
        //timeFormat.setTimeZone(gmtTz);

        try {
            date = timeFormat.parse(timeSinceMidnightString);
        } catch (Exception e) {
            MMUtilities.getInstance().errorHandler(activity, R.string.error_parsing_date_time);

        }

        return date;
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
            timeFormat = getTimeFormat(activity, is24Format);
        } else {
            timeFormat = getDateFormat(activity);
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



    //Time string is since midnight
    static long convertStringToMinutesSinceMidnight(MMMainActivity activity,
                                             String timeSinceMidnightString){
        boolean isTimeFlag = true;
        long msSinceMidnight = MMUtilitiesTime.convertStringToTimeMs(activity,
                                                                    timeSinceMidnightString,
                                                                    isTimeFlag);
        return MMUtilitiesTime.convertMsToMin(msSinceMidnight) ;
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

    private static Calendar getCalendar(int hours, int minutes){
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

    private static long getTimezoneOffset() {
        TimeZone tz = TimeZone.getDefault();
        Calendar cal = GregorianCalendar.getInstance(tz);
        return (long) tz.getOffset(cal.getTimeInMillis());
    }

    private static long getDSTOffset() {
        //Creaate a calendar with this time now
        Calendar nowCal = Calendar.getInstance();

        //This offset indicates the number of milliseconds due to DST in effect
        // as of the date of the Calendar
        return nowCal.get(Calendar.DST_OFFSET);
    }




    // ****************************** //
    //                                //
    //   Time/Date string Utilities   //
    //                                //
    // ****************************** //

    static String getTimeString(MMMainActivity activity){
        boolean is24Format  = MMSettings.getInstance().getClock24Format(activity);
        Calendar calendar   = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat(getTimeFormatString(activity, is24Format),
                                                    Locale.getDefault());
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


    private static SimpleDateFormat getTimeFormat(Context activity, boolean is24format){
        String timeFormat = getTimeFormatString(activity, is24format);
        return new SimpleDateFormat(timeFormat, Locale.getDefault());
    }

    private static  String getTimeFormatString(Context activity, boolean is24format){
        CharSequence timeFormat = activity.getString(R.string.time_format_12);
        if (is24format){
            timeFormat = activity.getString(R.string.time_format_24);
        }
        return timeFormat.toString();
    }

    private static SimpleDateFormat getDateFormat(Context activity){
        return new SimpleDateFormat(getDateFormatString(activity), Locale.getDefault());
    }
    private static String getDateFormatString(Context activity){
        return activity.getString(R.string.date_format);
    }

    private static SimpleDateFormat getDateTimeFormat(Context activity, boolean is24format){
        String dateTimeFormat = getDateFormatString(activity) + " " +
                                getTimeFormatString(activity, is24format);
        return new SimpleDateFormat(dateTimeFormat, Locale.getDefault());
    }


    private static SimpleDateFormat getEditTextDateFormat(Context activity){
        return new SimpleDateFormat(activity.getString(R.string.short_date_format), Locale.getDefault());
    }





}
