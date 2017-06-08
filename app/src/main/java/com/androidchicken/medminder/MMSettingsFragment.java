package com.androidchicken.medminder;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.SwitchCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;

import java.util.Date;

import static com.androidchicken.medminder.R.id.settingPersonNickNameInput;


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

    //**********************************************/
    //         Member Variables                    */
    //**********************************************/
    boolean isUIChanged = false;

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
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        //Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_settings, container, false);

        //Wire up the UI widgets so they can handle events later
        wireWidgets(v);

        initializeUI(v);

        //set the title bar subtitle
        ((MMMainActivity) getActivity()).setMMSubtitle(R.string.title_person);

        //Set the changed UI flag based on whether we are recreating the View
        if (savedInstanceState != null) {
            isUIChanged = savedInstanceState.getBoolean(MMHomeFragment.sIsUIChangedTag);
            if (isUIChanged) {
                setUIChanged(v);
            } else {
                setUISaved(v);
            }
        } else {
            setUISaved(v);
        }

        return v;
    }

    @Override
    public void onResume(){

        super.onResume();
        //MMUtilities.clearFocus(getActivity());


        if (getOrientation() == Configuration.ORIENTATION_LANDSCAPE) {

            //get rid of the soft keyboard if it is visible
            View v = getView();
            if (v != null) {
                EditText personNickNameInput = (EditText) (v.findViewById(settingPersonNickNameInput));
                MMUtilities.getInstance().showSoftKeyboard(getActivity(), personNickNameInput);
            }
        } else {
            //get rid of the soft keyboard if it is visible
            MMUtilities.getInstance().hideSoftKeyboard(getActivity());
        }


        //set the title bar subtitle
        ((MMMainActivity) getActivity()).setMMSubtitle(R.string.title_settings);

        //Set the FAB invisible
        ((MMMainActivity) getActivity()).hideFAB();
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
    public void onSaveInstanceState(Bundle savedInstanceState){
        // Save custom values into the bundle

        //Save the isUIChanged flag
        savedInstanceState.putBoolean(MMHomeFragment.sIsUIChangedTag, isUIChanged);

        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
    }


    //*************************************************************/
    /*  Convenience Methods for accessing things on the Activity  */
    //*************************************************************/

    private long     getPatientID(){
        return ((MMMainActivity)getActivity()).getPatientID();
    }
    private void     setPatientID(long patientID) {
        ((MMMainActivity)getActivity()).setPatientID(patientID);
    }

    private MMPerson getPerson(){
        return MMPersonManager.getInstance().getPerson(getPatientID());
    }

    //*************************************************************/
    /*                    Initialization Methods                  */
    //*************************************************************/

    private void wireWidgets(View v){

        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
                //This tells you that text is about to change.
                // Starting at character "start", the next "count" characters
                // will be changed with "after" number of characters

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                //This tells you where the text has changed
                //Starting at character "start", the "before" number of characters
                // has been replaced with "count" number of characters

            }

            @Override
            public void afterTextChanged(Editable editable) {
                //This tells you that somewhere within editable, it's text has changed
                setUIChanged();
            }
        };

        //Save Button
        Button saveButton = (Button) v.findViewById(R.id.settingsSaveButton);
        saveButton.setText(R.string.save_label);
        //the order of images here is left, top, right, bottom
        //saveButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_collect, 0, 0);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSave();
            }
        });



        EditText defaultTimeDueInput = (EditText) v.findViewById(R.id.settingDefaultTimeDueInput);
        defaultTimeDueInput.addTextChangedListener(textWatcher);

        EditText earliestHistoryDateInput =
                                    (EditText) v.findViewById(R.id.settingEarliestHistoryDateInput);
        earliestHistoryDateInput.addTextChangedListener(textWatcher);

        SwitchCompat clock24Switch = (SwitchCompat) v.findViewById(R.id.switch24Format);
        clock24Switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                MMSettings.getInstance().setClock24Format((MMMainActivity)getActivity(), isChecked);
            }
        });


        SwitchCompat showDelPersonSwitch = (SwitchCompat) v.findViewById(R.id.switchShowDeletedPeople);
        showDelPersonSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                MMSettings.getInstance().setShowDeletedPersons(
                                                        (MMMainActivity)getActivity(), isChecked);
            }
        });


        SwitchCompat showDelMedSwitch = (SwitchCompat) v.findViewById(R.id.switchShowDeletedMeds);
        showDelMedSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                MMSettings.getInstance().setShowDeletedMeds(
                                                          (MMMainActivity)getActivity(), isChecked);
            }
        });


        SwitchCompat soundWithNotifSwitch = (SwitchCompat) v.findViewById(R.id.switchSoundWithMedNotif);
        soundWithNotifSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                MMSettings.getInstance().setSoundNotification(
                                                        (MMMainActivity)getActivity(), isChecked);
            }
        });


        SwitchCompat vibrateWithNotifSwitch =
                                    (SwitchCompat) v.findViewById(R.id.switchVibrateWithMedNotif);
        vibrateWithNotifSwitch.
                        setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                MMSettings.getInstance().setVibrateNotification(
                                                         (MMMainActivity)getActivity(), isChecked);
            }
        });

     }


    private void initializeUI(View v) {

        MMSettings settings = MMSettings.getInstance();

        //Initialize the Patient Name
        EditText personNickNameInput = (EditText) (v.findViewById(settingPersonNickNameInput));
        long patientId = ((MMMainActivity)getActivity()).getPatientID();
        CharSequence personName = null;
        if (patientId != MMUtilities.ID_DOES_NOT_EXIST) {
            MMPerson person = MMPersonManager.getInstance().getPerson(patientId);
            personName = person.getNickname();
        } else {
            personName = getString(R.string.person_not_exist);
        }
        personNickNameInput.setText(personName);


        //Initialize the default time due for any created schedules
        EditText defaultTimeDueInput = (EditText) v.findViewById(R.id.settingDefaultTimeDueInput);
        long timeMinutes   = settings.getDefaultTimeDue((MMMainActivity)getActivity());
        long timeMilliseconds = (timeMinutes * 60000);

        CharSequence timeString = MMUtilities.getInstance().
                                    getTimeString((MMMainActivity)getActivity(), timeMilliseconds);
        defaultTimeDueInput.setText(timeString);


        EditText earliestHistoryDateInput =
                (EditText) v.findViewById(R.id.settingEarliestHistoryDateInput);
        long historyDate = settings.getHistoryDate((MMMainActivity)getActivity());
        String historyDateString = MMUtilities.getInstance().getDateString(historyDate);
        earliestHistoryDateInput.setText(historyDateString);



        //Set all the switches from the stored Preferences
        SwitchCompat clock24Switch = (SwitchCompat) v.findViewById(R.id.switch24Format);
        clock24Switch.setChecked(settings.getClock24Format((MMMainActivity)getActivity()));


        SwitchCompat showDelPersonSwitch =
                                    (SwitchCompat) v.findViewById(R.id.switchShowDeletedPeople);
        showDelPersonSwitch.setChecked(settings.
                                            getShowDeletedPersons((MMMainActivity)getActivity()));


        SwitchCompat showDelMedSwitch =
                                    (SwitchCompat) v.findViewById(R.id.switchShowDeletedMeds);
        showDelMedSwitch.setChecked(settings.getShowDeletedMeds((MMMainActivity)getActivity()));


        SwitchCompat soundWithNotifSwitch =
                                    (SwitchCompat) v.findViewById(R.id.switchSoundWithMedNotif);
        soundWithNotifSwitch.setChecked(settings.
                                            getSoundNotification((MMMainActivity)getActivity()));


        SwitchCompat vibrateWithNotifSwitch =
                                    (SwitchCompat) v.findViewById(R.id.switchVibrateWithMedNotif);
        vibrateWithNotifSwitch.setChecked(settings.
                                            getVibrateNotification((MMMainActivity)getActivity()));

        //setUISaved(v);
    }

    private void onSave(){
        MMUtilities.getInstance().showStatus(getActivity(), R.string.save_label);

        View v = getView();
        if (v == null)return;

        //Save the default value for scheduling medications
        EditText defaultTimeDueInput = (EditText) v.findViewById(R.id.settingDefaultTimeDueInput);
        CharSequence timeString = defaultTimeDueInput.getText();

        MMSettings settings = MMSettings.getInstance();

        //convert to # minutes since midnight
        long minutesSinceMidnight = MMUtilities.getInstance().convertStringToMinutesSinceMidnight(
                                                                (MMMainActivity)getActivity(),
                                                                timeString.toString());

        // TODO: 5/28/2017  This conversion is just for debug. If you see it remove it
        timeString = MMUtilities.getInstance().
                getTimeString((MMMainActivity) getActivity(), minutesSinceMidnight * 60000);

        if (minutesSinceMidnight < 0) {
            MMUtilities.getInstance().errorHandler(getActivity(), R.string.settings_incorrect_format);
            //if there is an error, reset to 6 AM
            minutesSinceMidnight = MMSettings.sDefaultTimeDue;
            long millisecondsSinceMidnight = minutesSinceMidnight * 60 * 1000;
            //and put it back on the screen
            timeString = MMUtilities.getInstance().
                    getTimeString((MMMainActivity) getActivity(), millisecondsSinceMidnight);
            defaultTimeDueInput.setText(timeString);
        }
        settings.setDefaultTimeDue((MMMainActivity)getActivity(), minutesSinceMidnight);

        // TODO: 5/28/2017 debug
        long tempTime = settings.getDefaultTimeDue((MMMainActivity)getActivity());


        //Earliest date in home history
        EditText earliestHistoryDateInput =
                                    (EditText) v.findViewById(R.id.settingEarliestHistoryDateInput);
        String dateString = earliestHistoryDateInput.getText().toString();

        boolean isTimeFlag = false; //We are converting to a date, not a time
        Date historyDate = MMUtilities.getInstance().
                    convertStringToTimeDate((MMMainActivity)getActivity(), dateString, isTimeFlag);

        if (historyDate != null) {
            long historyDateMilli = historyDate.getTime();

            settings.setHistoryDate((MMMainActivity) getActivity(), historyDateMilli);

            // TODO: 6/1/2017 delete the debug double check to assure the string is right
            String historyDateString = MMUtilities.getInstance().getDateString(historyDateMilli);
        }

        //get rid of the soft keyboard
        MMUtilities.getInstance().hideSoftKeyboard(getActivity());


        setUISaved();
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

        Button saveButton = (Button) v.findViewById(R.id.settingsSaveButton);

        MMUtilities utilities = MMUtilities.getInstance();
        utilities.enableButton(getActivity(), saveButton, isEnabled);
    }


}
