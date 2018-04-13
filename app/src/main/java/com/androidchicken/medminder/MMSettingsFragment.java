package com.androidchicken.medminder;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.SwitchCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;

import java.util.ArrayList;




/**
 * A placeholder fragment containing a simple view.
 */
public class MMSettingsFragment extends Fragment {


    //**********************************************/
    //          Static Methods                     */
    //**********************************************/


    //**********************************************/
    //          Static Constants                   */
    //**********************************************/
    private static final String sDefaultDateTag  = "DefaultDate";
    private static final String sEarliestDateTag = "EarliestDate";

    //**********************************************/
    //         Member Variables                    */
    //**********************************************/
    boolean isUIChanged           = false;
    boolean isDefaultDateInError  = false;
    boolean isEarliestDateInError = false;

    //**********************************************/
    //          Constructor                        */
    //**********************************************/
    public MMSettingsFragment() {
    }

    //**********************************************/
    //          Lifecycle Methods                  */
    //**********************************************/

    //pull the arguments out of the fragment bundle and store in the member variables
    //In this case, prepopulate the personID this screen refers to
    @Override
    public void onCreate(Bundle savedInstanceState){

        super.onCreate(savedInstanceState);

        //initialize the DB, providing it with a context if necessary
        MMDatabaseManager.getInstance(getActivity());
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        MMMainActivity activity = (MMMainActivity)getActivity();
        if (activity == null)return null;

        //Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_settings, container, false);

        //Wire up the UI widgets so they can handle events later
        wireWidgets(v);

        initializeUI(v);

        //set the title bar subtitle
        activity.setMMSubtitle(R.string.title_settings);

        //Set the changed UI flag based on whether we are recreating the View
        if (savedInstanceState != null) {
            isDefaultDateInError = savedInstanceState.getBoolean(sDefaultDateTag);
            isEarliestDateInError = savedInstanceState.getBoolean(sEarliestDateTag);

            isUIChanged = savedInstanceState.getBoolean(MMHomeFragment.sIsUIChangedTag);
            if (isUIChanged) {
                setUIChanged(v);
            } else {
                setUISaved(v);
            }
        } else {
            isEarliestDateInError = false;
            isDefaultDateInError  = false;

            isUIChanged = false;
            setUISaved(v);
        }

        ((MMMainActivity) getActivity()).handleFabVisibility();

        return v;
    }

    @Override
    public void onResume(){

        super.onResume();

        if (getOrientation() == Configuration.ORIENTATION_LANDSCAPE) {

            //get rid of the soft keyboard if it is visible
            View v = getView();
            if (v != null) {
                EditText defaultTime = v.findViewById(R.id.settingDefaultTimeDueInput);
                MMUtilities.getInstance().showSoftKeyboard(getActivity(), defaultTime);
            }
        } else {
            //get rid of the soft keyboard if it is visible
            MMUtilities.getInstance().hideSoftKeyboard(getActivity());
        }

        MMMainActivity activity = (MMMainActivity)getActivity();
        if (activity == null)return;

        //set the title bar subtitle
        activity.setMMSubtitle(R.string.title_settings);

        //Set the FAB invisible
        activity.hideFAB();
    }

    public int getOrientation(){
        int orientation = Configuration.ORIENTATION_PORTRAIT;
        if (getResources().getDisplayMetrics().widthPixels >
            getResources().getDisplayMetrics().heightPixels) {

            orientation = Configuration.ORIENTATION_LANDSCAPE;
        }
        return orientation;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle savedInstanceState){
        // Save custom values into the bundle

        //Save the isUIChanged flag
        savedInstanceState.putBoolean(MMHomeFragment.sIsUIChangedTag, isUIChanged);
        savedInstanceState.putBoolean(sDefaultDateTag, isDefaultDateInError);
        savedInstanceState.putBoolean(sEarliestDateTag, isEarliestDateInError);

        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
    }




    //*************************************************************/
    /*                    Initialization Methods                  */
    //*************************************************************/

    private void wireWidgets(View v){

        final MMMainActivity activity = (MMMainActivity)getActivity();
        if (activity == null)return;

        //Save Button
        Button saveButton = v.findViewById(R.id.settingsSaveButton);
        saveButton.setText(R.string.save_label);
        //the order of images here is left, top, right, bottom
        //saveButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_collect, 0, 0);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSave();
            }
        });



        //
        // Default time to take dose
        //
        final EditText defaultTimeDueInput = v.findViewById(R.id.settingDefaultTimeDueInput);
        TextWatcher defaultTimeTextWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence timeString, int start, int count, int after) {
                //This tells you that text is about to change.
                // Starting at character "start", the next "count" characters
                // will be changed with "after" number of characters

            }

            @Override
            public void onTextChanged(CharSequence timeString, int start, int before, int count) {
                //This tells you where the text has changed
                //Starting at character "start", the "before" number of characters
                // has been replaced with "count" number of characters

                MMSettings settings = MMSettings.getInstance();

                //convert to # minutes since midnight
                long minutesSinceMidnight =
                        MMUtilitiesTime.convertStringToMinutesSinceMidnight(activity,
                                                                            timeString.toString());
                int backgroundColor ;
                if (minutesSinceMidnight <= 0) {
                    backgroundColor = ContextCompat.getColor(activity, R.color.colorError);
                    //if there is an error, nothing is changed until the user selects save
                    isDefaultDateInError = true;
                    setUIChanged();
                } else {
                    backgroundColor = ContextCompat.getColor(activity, R.color.colorWhite);
                    settings.setDefaultTimeDue(activity, minutesSinceMidnight);

                    //set the UI changed flags accordingly
                    isDefaultDateInError = false;
                    if (isEarliestDateInError){
                        setUIChanged();
                    } else {
                        setUISaved();;
                    }
                }
                defaultTimeDueInput.setBackgroundColor(backgroundColor);
            }

            @Override
            public void afterTextChanged(Editable editable) {
                //This tells you that somewhere within editable, it's text has changed

            }
        };

         defaultTimeDueInput.addTextChangedListener(defaultTimeTextWatcher);




        //
        // Earliest Date of shown Dosage History
        //

        final EditText earliestHistoryDateInput = v.findViewById(R.id.settingEarliestHistoryDateInput);
        TextWatcher earliestTimeTextWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence dateString, int start, int count, int after) {
                //This tells you that text is about to change.
                // Starting at character "start", the next "count" characters
                // will be changed with "after" number of characters

            }

            @Override
            public void onTextChanged(CharSequence dateString, int start, int before, int count) {
                //This tells you where the text has changed
                //Starting at character "start", the "before" number of characters
                // has been replaced with "count" number of characters

                MMSettings settings = MMSettings.getInstance();

                boolean isTwoWeeks = settings.showOnlyTwoWeeks(activity);

                //We are converting to a date, not a time
                long historyDateMilli = MMUtilitiesTime.
                        convertStringToTimeMs(activity, dateString.toString(), false);


                int backgroundColor = ContextCompat.getColor(activity, R.color.colorWhite);
                if (isTwoWeeks){
                    backgroundColor = ContextCompat.getColor(activity, R.color.colorGray);
                }
                if (historyDateMilli != 0) {
                    settings.setHistoryDate((MMMainActivity) getActivity(), historyDateMilli);

                    isEarliestDateInError = false;
                    if (isDefaultDateInError){
                        setUIChanged();
                    } else {
                        setUISaved();
                    }
                } else {
                    isEarliestDateInError = true;
                    setUIChanged();
                    backgroundColor = ContextCompat.getColor(activity, R.color.colorError);
                }
                earliestHistoryDateInput.setBackgroundColor(backgroundColor);
            }

            @Override
            public void afterTextChanged(Editable editable) {
                //This tells you that somewhere within editable, it's text has changed

            }
        };

        earliestHistoryDateInput.addTextChangedListener(earliestTimeTextWatcher);

        SwitchCompat isTwoWeeks = v.findViewById(R.id.switchShowOnlyTwoWeeks);
        isTwoWeeks.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {

                MMSettings.getInstance().setShowOnlyTwoWeeks(activity, isChecked);
                int backgroundColor = ContextCompat.getColor(activity, R.color.colorWhite);
                if (isChecked){
                    backgroundColor = ContextCompat.getColor(activity, R.color.colorGray);
                }
                earliestHistoryDateInput.setBackgroundColor(backgroundColor);
            }
        });







        SwitchCompat clock24Switch = v.findViewById(R.id.switch24Format);
        clock24Switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                MMSettings.getInstance().setClock24Format(activity, isChecked);
            }
        });


        SwitchCompat showDelPersonSwitch = v.findViewById(R.id.switchShowOnlyCurrentPeople);
        showDelPersonSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                MMSettings.getInstance().setShowOnlyCurrentPersons(activity, isChecked);
            }
        });


        SwitchCompat showOnlyCurrentMedSwitch = v.findViewById(R.id.switchShowOnlyCurrentMeds);
        showOnlyCurrentMedSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                MMSettings.getInstance().setShowOnlyCurrentMeds(activity, isChecked);
                //Whenever this setting is changed, in either direction,
                // need to reset the medications list on ALL the person objects
                // regardless if they are current or not
                ArrayList<MMPerson> allPeople =
                        MMDatabaseManager.getInstance().getAllPersons(false);
                int position = 0;
                int last = allPeople.size();
                MMPerson person;
                while (position < last){
                    person = allPeople.get(position);
                    person.resetMedicationsChanged();
                    position++;
                }
            }
        });


        SwitchCompat showFAB = v.findViewById(R.id.switchFabVisible);
        showFAB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                MMSettings.getInstance().setFabVisible(activity, isChecked);
            }
        });

        SwitchCompat homeShading = v.findViewById(R.id.switchHomeShading);
        homeShading.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                MMSettings.getInstance().setHomeShading(activity, isChecked);
            }
        });



        SwitchCompat lightWithNotifSwitch = v.findViewById(R.id.switchLightWithMedNotif);
        lightWithNotifSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                MMSettings.getInstance().setLightNotification(activity, isChecked);
            }
        });


        SwitchCompat vibrateWithNotifSwitch = v.findViewById(R.id.switchVibrateWithMedNotif);
        vibrateWithNotifSwitch.
                        setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                MMSettings.getInstance().setVibrateNotification(activity, isChecked);
            }
        });


        SwitchCompat soundWithNotifSwitch = v.findViewById(R.id.switchSoundWithMedNotif);
        soundWithNotifSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                MMSettings.getInstance().setSoundNotification(activity, isChecked);
            }
        });

    }


    private void initializeUI(View v) {

        MMSettings settings = MMSettings.getInstance();
        MMMainActivity activity = (MMMainActivity)getActivity();


        //Initialize the default time due for any created schedules
        EditText defaultTimeDueInput = v.findViewById(R.id.settingDefaultTimeDueInput);
        long timeMinutes   = settings.getDefaultTimeDue(activity);
        long timeMilliseconds = (timeMinutes * 60000);
        CharSequence timeString = MMUtilitiesTime.getTimeString(activity, timeMilliseconds);
        defaultTimeDueInput.setText(timeString);



        SwitchCompat isTwoWeeksView =  v.findViewById(R.id.switchShowOnlyTwoWeeks);
        boolean isTwoWeeks = settings.showOnlyTwoWeeks(activity);
        isTwoWeeksView.setChecked(isTwoWeeks);

        EditText earliestHistoryDateInput = v.findViewById(R.id.settingEarliestHistoryDateInput);
        long historyDate = settings.getHistoryDate(activity);
        String historyDateString = MMUtilitiesTime.convertMStoDateString(activity, historyDate);
        earliestHistoryDateInput.setText(historyDateString);
        int backgroundColor = ContextCompat.getColor(activity, R.color.colorWhite);
        if (isTwoWeeks){
            backgroundColor = ContextCompat.getColor(activity, R.color.colorGray);
        }
        earliestHistoryDateInput.setBackgroundColor(backgroundColor);




        //Set all the switches from the stored Preferences
        SwitchCompat clock24Switch =  v.findViewById(R.id.switch24Format);
        clock24Switch.setChecked(settings.isClock24Format(activity));


        SwitchCompat showOnlyCurrentPersonSwitch = v.findViewById(R.id.switchShowOnlyCurrentPeople);
        showOnlyCurrentPersonSwitch.setChecked(settings.showOnlyCurrentPersons(activity));


        SwitchCompat showDelMedSwitch = v.findViewById(R.id.switchShowOnlyCurrentMeds);
        showDelMedSwitch.setChecked(settings.showOnlyCurrentMeds(activity));


        SwitchCompat soundWithNotifSwitch = v.findViewById(R.id.switchSoundWithMedNotif);
        soundWithNotifSwitch.setChecked(settings.isSoundNotification(activity));


        SwitchCompat vibrateWithNotifSwitch = v.findViewById(R.id.switchVibrateWithMedNotif);
        vibrateWithNotifSwitch.setChecked(settings.isVibrateNotification(activity));


        SwitchCompat showFAB = v.findViewById(R.id.switchFabVisible);
        showFAB.setChecked(settings.isFabVisible(activity));

        SwitchCompat homeShading = v.findViewById(R.id.switchHomeShading);
        homeShading.setChecked(settings.isHomeShading(activity));
        //setUISaved(v);
    }

    private void onSave(){
        MMUtilities.getInstance().showStatus(getActivity(), R.string.save_label);

        View v = getView();
        if (v == null)return;

        MMSettings settings = MMSettings.getInstance();
        MMMainActivity activity = (MMMainActivity)getActivity();

        //Save the default value for scheduling medications
        EditText defaultTimeDueInput = v.findViewById(R.id.settingDefaultTimeDueInput);
        CharSequence timeString = defaultTimeDueInput.getText();

        //convert to # minutes since midnight
        long minutesSinceMidnight = MMUtilitiesTime.convertStringToMinutesSinceMidnight(
                                                                activity,
                                                                timeString.toString());

        if (minutesSinceMidnight <= 0) {
            isDefaultDateInError = true;
            MMUtilities.getInstance().errorHandler(getActivity(), R.string.settings_incorrect_format);
            //if there is an error, reset to default set by the user
            minutesSinceMidnight = MMSettings.sDefaultTimeDue;
            long msSinceMidnight = MMUtilitiesTime.convertMinutesToMs(minutesSinceMidnight);
            //and put it back on the screen
            timeString = MMUtilitiesTime.getTimeString(activity, msSinceMidnight);
            defaultTimeDueInput.setText(timeString);
        } else {
            isDefaultDateInError = false;
        }
        settings.setDefaultTimeDue(activity, minutesSinceMidnight);

        //Earliest date in home history
        EditText earliestHistoryDateInput = v.findViewById(R.id.settingEarliestHistoryDateInput);
        String dateString = earliestHistoryDateInput.getText().toString();

        //We are converting to a date, not a time
        long historyDateMilli = MMUtilitiesTime.
                                        convertStringToTimeMs(activity, dateString, false);

        if (historyDateMilli <= 0) {
            isEarliestDateInError = true;
        } else {
            isEarliestDateInError = false;
            settings.setHistoryDate((MMMainActivity) getActivity(), historyDateMilli);
        }

        //get rid of the soft keyboard
        MMUtilities.getInstance().hideSoftKeyboard(getActivity());


        if (isEarliestDateInError || isDefaultDateInError){
            setUIChanged();
        } else {
            setUISaved();
        }
    }

    //*********************************************************/
    //      Methods dealing with whether the UI has changed   //
    //*********************************************************/
    private void setUIChanged(){
        isUIChanged = true;
        saveButtonEnable(MMUtilities.BUTTON_ENABLE);
    }
    private void setUIChanged(View v){
        isUIChanged = true;
        saveButtonEnable(v, MMUtilities.BUTTON_ENABLE);
    }

    private void setUISaved(){
        isUIChanged = false;

        //disable the save button
        saveButtonEnable(MMUtilities.BUTTON_DISABLE);
    }
    private void setUISaved(View v){
        isUIChanged = false;

        //disable the save button
        saveButtonEnable(v, MMUtilities.BUTTON_DISABLE);
    }

    private void saveButtonEnable(boolean isEnabled){
        View v = getView();
        saveButtonEnable(v, isEnabled);
    }

    private void saveButtonEnable(View v, boolean isEnabled){
        if (v == null)return; //onCreateView() hasn't run yet

        Button saveButton = v.findViewById(R.id.settingsSaveButton);

        MMUtilities utilities = MMUtilities.getInstance();
        utilities.enableButton(getActivity(), saveButton, isEnabled);
    }


}
