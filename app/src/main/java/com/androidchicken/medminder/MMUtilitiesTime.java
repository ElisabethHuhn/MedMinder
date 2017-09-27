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









    // ****************************** //
    //                                //
    //   Conversion Utilities         //
    //                                //
    // ****************************** //

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
            e.printStackTrace();
        }

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
        } catch (Exception e) {
            MMUtilities.getInstance().errorHandler(activity, R.string.error_parsing_date_time);
            e.printStackTrace();
        }

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




/*
    static void testTimes2(long dateMilliGmt, long dateMilliLocal){
        Date date0 = new Date(dateMilliGmt);
        Date date1 = new Date(dateMilliLocal);

    }

    static void testTimes(long schedTimeDueLocalMs,
                          long mostRecentDoseTimeGMT,
                          long currentGmtMs){



        Date date1 = new Date(schedTimeDueLocalMs);

        Date date3 = new Date(mostRecentDoseTimeGMT);
        Date date4 = new Date(currentGmtMs);

    }

    static void testTimeMethod2(MMMainActivity activity){
        boolean is24Format    = true;
        //even though this is a GMT time,
        // the string is just a test that it's correct, so we can get away with creating it as local
        //Use java DateFormat
        java.text.DateFormat defaultFormatter = java.text.DateFormat.getDateInstance(
                java.text.DateFormat.DEFAULT); // one of SHORT, MEDIUM, LONG, FULL, or DEFAULT
        java.text.DateFormat shortFormatter = java.text.DateFormat.getDateInstance(
                java.text.DateFormat.SHORT); // one of SHORT, MEDIUM, LONG, FULL, or DEFAULT
        java.text.DateFormat mediumFormatter = java.text.DateFormat.getDateInstance(
                java.text.DateFormat.MEDIUM); // one of SHORT, MEDIUM, LONG, FULL, or DEFAULT
        java.text.DateFormat longFormatter = java.text.DateFormat.getDateInstance(
                java.text.DateFormat.LONG); // one of SHORT, MEDIUM, LONG, FULL, or DEFAULT
        java.text.DateFormat fullFormatter = java.text.DateFormat.getDateInstance(
                java.text.DateFormat.FULL); // one of SHORT, MEDIUM, LONG, FULL, or DEFAULT

//

        String nowLocal       = getTimeString(activity);
        long gmtMidnightToday = getGMTToday(0); //at midnight
        long localMidnightMS = MMUtilitiesTime.convertGMTtoLocal(gmtMidnightToday);

        //First the DateStrings
        String dDateString     = defaultFormatter.format(gmtMidnightToday);
        String sDateString     = shortFormatter.format(gmtMidnightToday);
        String mDateString     = mediumFormatter.format(gmtMidnightToday);
        String lDateString     = longFormatter.format(gmtMidnightToday);
        String fDateString     = fullFormatter.format(gmtMidnightToday);


        //check with a date object
        Date checkDate = new Date(gmtMidnightToday);
        String checkDateString = checkDate.toString();
        long checkMS = checkDate.getTime();

        Date checkDateLocalMidnight = new Date(localMidnightMS);
        Date checkDateNow = new Date(getGmtNow());


        //initially create the format in the local time zone
        SimpleDateFormat df   = new SimpleDateFormat(getTimeFormatString(is24Format), Locale.getDefault());
        String calendarString  = df.format(gmtMidnightToday);
        //then switch it to the GMT
        df.setTimeZone(TimeZone.getTimeZone("GMT"));
        String calendarString0 = df.format(gmtMidnightToday);

        //then check other times
        long gmtToday         = getGMTToday(360);//at 6am
        long now = gmtMidnightToday + gmtToday;

        String calendarString1 = df.format(gmtToday);
        String calendarString2 = df.format(now);


        gmtToday               = getGMTToday(1080);//at 6pm
        String calendarString3 = df.format(gmtToday);

    }

    static void testTimeMethod(){
        boolean is24Format = true;

        //Calendar - default local time
        Calendar calendar     = Calendar.getInstance();
        long calendarMillisec = calendar.getTimeInMillis();
        long localTimeMs      = calendarMillisec;

        //Georgian Calendar - Local
        TimeZone tz = TimeZone.getDefault();
        Calendar georgeCal = GregorianCalendar.getInstance(tz);
        long georgeCalMs   = georgeCal.getTimeInMillis();

        tz = TimeZone.getTimeZone("GMT");//this creates a calendar on local as above
        Calendar georgeGmtCal = GregorianCalendar.getInstance(tz);
        long georgeGmtCalMs   = georgeGmtCal.getTimeInMillis();

        //see if can create calendar with GMT milliseconds Yes, both of these are on the same time as above
        Calendar mCalendar = Calendar.getInstance(TimeZone.getTimeZone("gmt"));
        long millies = mCalendar.getTimeInMillis();

        long currentGmtMs = System.currentTimeMillis();


        //Date
        Date date         = new Date();
        long dateMillisec = date.getTime();



//See what conversions do
        calendarMillisec = convertGMTtoLocal(localTimeMs);
        georgeCalMs      = convertGMTtoLocal(localTimeMs);
        georgeGmtCalMs   = convertGMTtoLocal(localTimeMs);
        dateMillisec     = convertGMTtoLocal(localTimeMs);
//values not changed

        calendarMillisec = convertLocaltoGMT(localTimeMs);
        georgeCalMs      = convertLocaltoGMT(localTimeMs);
        georgeGmtCalMs   = convertLocaltoGMT(localTimeMs);
        dateMillisec     = convertLocaltoGMT(localTimeMs);

//values not changed



        String timeString = date.toString();



        //Use java DateFormat
        java.text.DateFormat formatter = java.text.DateFormat.getDateInstance(
                java.text.DateFormat.DEFAULT); // one of SHORT, MEDIUM, LONG, FULL, or DEFAULT

//
        String calendarString     = formatter.format(calendar.getTime());
        String georgeCalString    = formatter.format(georgeCal.getTime());
        String georgeGmtCalString = formatter.format(georgeGmtCal.getTime());
        String dateString         = formatter.format(date);




        String timeFormat = getTimeFormatString(is24Format);

//All of these giVe the correct local time as HH:MM AM
        //Use Android SimpleDateFormat
        SimpleDateFormat df = new SimpleDateFormat(getTimeFormatString(is24Format), Locale.getDefault());
        calendarString     = df.format(calendar    .getTimeInMillis());
        georgeCalString    = df.format(georgeCal   .getTimeInMillis());
        georgeGmtCalString = df.format(georgeGmtCal.getTimeInMillis());
        dateString         = df.format(date);

        calendarString     = df.format(calendar    .getTime());
        georgeCalString    = df.format(georgeCal   .getTime());
        georgeGmtCalString = df.format(georgeGmtCal.getTime());

//all of these give the correct GMT time as HH:MM AM
        df.setTimeZone(TimeZone.getTimeZone("GMT"));
        calendarString     = df.format(calendar    .getTimeInMillis());
        georgeCalString    = df.format(georgeCal   .getTimeInMillis());
        georgeGmtCalString = df.format(georgeGmtCal.getTimeInMillis());
        dateString         = df.format(date);

        calendarString     = df.format(calendar    .getTime());
        georgeCalString    = df.format(georgeCal   .getTime());
        georgeGmtCalString = df.format(georgeGmtCal.getTime());


//All of these give the correct local time as HH:MM AM
        df.setTimeZone(TimeZone.getDefault());
        calendarString     = df.format(calendar    .getTimeInMillis());
        georgeCalString    = df.format(georgeCal   .getTimeInMillis());
        georgeGmtCalString = df.format(georgeGmtCal.getTimeInMillis());
        dateString         = df.format(date);

        calendarString     = df.format(calendar.getTime());
        georgeCalString    = df.format(georgeCal.getTime());
        georgeGmtCalString = df.format(georgeGmtCal.getTime());



 //Convert to GMT and then back

//assert: calendar always assumes the new time is local
        calendar    .setTimeInMillis(localTimeMs);
        georgeCal   .setTimeInMillis(localTimeMs);
        georgeGmtCal.setTimeInMillis(localTimeMs);
        date        .setTime        (localTimeMs);
//all give correct local time
        df.setTimeZone(TimeZone.getDefault());
        calendarString     = df.format(calendar.getTimeInMillis());
        georgeCalString    = df.format(georgeCal.getTimeInMillis());
        georgeGmtCalString = df.format(georgeGmtCal.getTimeInMillis());
        dateString         = df.format(date);

        calendarString     = df.format(localTimeMs);


//all give correct GMT time
        df.setTimeZone(TimeZone.getTimeZone("GMT"));
        calendarString     = df.format(calendar.getTimeInMillis());
        georgeCalString    = df.format(georgeCal.getTimeInMillis());
        georgeGmtCalString = df.format(georgeGmtCal.getTimeInMillis());
        dateString         = df.format(date);

        long gmtTimeMillisec = convertLocaltoGMT(localTimeMs);
//INCORRECT double conversion to GMT
        calendarString     = df.format(gmtTimeMillisec);



//convert to Local values still unchanged by conversion routine
        localTimeMs = convertGMTtoLocal(gmtTimeMillisec);

        calendar    .setTimeInMillis(localTimeMs);
        georgeCal   .setTimeInMillis(localTimeMs);
        georgeGmtCal.setTimeInMillis(localTimeMs);
        date        .setTime        (localTimeMs);
//correct local time
        df.setTimeZone(TimeZone.getDefault());
        calendarString     = df.format(calendar.getTimeInMillis());
        georgeCalString    = df.format(georgeCal.getTimeInMillis());
        georgeGmtCalString = df.format(georgeGmtCal.getTimeInMillis());
        dateString         = df.format(date);

        calendarString     = df.format(localTimeMs);


//correct GMT time
        df.setTimeZone(TimeZone.getTimeZone("GMT"));
        calendarString     = df.format(calendar.getTimeInMillis());
        georgeCalString    = df.format(georgeCal.getTimeInMillis());
        georgeGmtCalString = df.format(georgeGmtCal.getTimeInMillis());
        dateString         = df.format(date);

        gmtTimeMillisec = convertLocaltoGMT(localTimeMs);

//INCORRECT double conversion
        calendarString     = df.format(gmtTimeMillisec);


//But resetting the timezone results in a correct GMT (but it thinks it's local from the incorrect GMT above
        df.setTimeZone(TimeZone.getDefault());
        calendarString     = df.format(gmtTimeMillisec);



        //algorithm from Stack Overflow. Format Date into a String, then parse String into a Date
        //Get milliseconds from String
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));



        Date dateUTC = new Date(localTimeMs);
        SimpleDateFormat dateParser = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        long gmtMilliSeconds ;
        Date dateTimeDate = null ;
        String dateTimeString ;
        try {
            dateTimeString  = dateFormat.format(dateUTC);
            dateTimeDate    = dateParser.parse(dateTimeString);
            gmtMilliSeconds = dateTimeDate.getTime();
        } catch (Exception e){

        }
        dateString = df.format(dateTimeDate.getTime());

        //Create a date -> local time




    }

*/




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
        return new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());
    }





}
