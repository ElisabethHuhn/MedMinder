package com.androidchicken.medminder;


import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.NotificationCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.ArrayList;

import static android.content.Context.ALARM_SERVICE;
import static com.androidchicken.medminder.R.id.medicationDoseAmountInput;
import static com.androidchicken.medminder.R.id.medicationDoseNumInput;


/**
 * The main UI screen for maintaining (CRUD) a Medication
 */
public class MMMedicationFragment extends Fragment implements AdapterView.OnItemSelectedListener {

    public static final int sSET_SCHEDULE_FOR_MEDICATION = 0;
    public static final int sAS_NEEDED = 1;

    private static final String SCHEDULE_STRATEGY  = "Schedule Medication Doses";
    private static final String AS_NEEDED_STRATEGY = "Take as needed";

    //**********************************************/
    /*        UI Widget Views                      */
    //**********************************************/





    //**********************************************************/
    /*   Variables that need to survive configuration change   */
    //**********************************************************/
    boolean isUIChanged = false;
    boolean userPressedOKinTimePicker = false;


    //**********************************************/
    /*        Passed Arguments to this Fragment    */
    //**********************************************/
    private long     mPersonID;
    private int      mPosition;

    //**********************************************************/
    //*****  Strategy types for Spinner Widgets     **********/
    //**********************************************************/
    private static String[] mStrategyTypes  = new String[]{SCHEDULE_STRATEGY, AS_NEEDED_STRATEGY };

    private int      mSelectedStrategyTypePosition = sSET_SCHEDULE_FOR_MEDICATION;
    private int      mOldPosition = sSET_SCHEDULE_FOR_MEDICATION;




    //**********************************************/
    /*          RecyclerView Widgets               */
    //**********************************************/
    //all are now local to initializeRecyclerView()


    //**********************************************/
    /*          Static Methods                     */
    //**********************************************/


    //need to pass a medication into the fragment
    //position is the index of the medication in the person list
    //-1 indicates add new medication
    public static MMMedicationFragment newInstance(long personID, int position){
        //create a bundle to hold the arguments
        Bundle args = new Bundle();

        //It will be some work to make all of the data model serializable
        //so for now, just pass the person values
        args.putLong        (MMPerson.sPersonIDTag,personID);
        args.putInt         (MMPerson.sPersonMedicationPositionTag, position);

        MMMedicationFragment fragment = new MMMedicationFragment();

        fragment.setArguments(args);
        
        return fragment;
    }

    //**********************************************/
    /*          Constructor                        */
    //**********************************************/
    public MMMedicationFragment() {
    }


    //**********************************************/
    /*          Lifecycle Methods                  */
    //**********************************************/


    //pull the arguments out of the fragment bundle and store in the member variables
    //In this case, prepopulate the personID this screen refers to
    @Override
    public void onCreate(Bundle savedInstanceState){

        super.onCreate(savedInstanceState);

        Bundle args = getArguments();

        if (args != null) {
            mPersonID = args.getLong(MMPerson.sPersonIDTag);
            mPosition = args.getInt(MMPerson.sPersonMedicationPositionTag);

        } else {
            mPersonID = MMUtilities.ID_DOES_NOT_EXIST;
            mPosition = -1;
        }

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if (savedInstanceState != null){
            //the fragment is being restored so restore the person ID
            mPersonID = savedInstanceState.getLong(MMPerson.sPersonIDTag);
            mPosition = savedInstanceState.getInt(MMPerson.sPersonMedicationPositionTag);
        }


        //Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_medication, container, false);

        //Wire up the UI widgets so they can handle events later
        wireWidgets(v);
        wireListTitleWidgets(v);
        wireStrategySpinner(v);
        initializeRecyclerView(v);
        initializeUI(v);

        //hide the soft keyboard if it is visible
        MMUtilities.hideSoftKeyboard(getActivity());
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        //set the title bar subtitle
        ((MMMainActivity) getActivity()).setMMSubtitle(R.string.title_medication);

        if (savedInstanceState == null) {
            setUISaved(v);
        } else {
            isUIChanged = savedInstanceState.getBoolean(MMHomeFragment.sIsUIChangedTag);
            if (isUIChanged){
                setUIChanged(v);
            } else {
                setUISaved(v);
            }
        }

        return v;
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState){
        // Save custom values into the bundle
        savedInstanceState.putLong(MMPerson.sPersonIDTag, mPersonID);
        savedInstanceState.putInt(MMPerson.sPersonMedicationPositionTag, mPosition);

        //Save the isUIChanged flag
        savedInstanceState.putBoolean(MMHomeFragment.sIsUIChangedTag, isUIChanged);

        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onResume(){
        super.onResume();

        //set the title bar subtitle
        ((MMMainActivity) getActivity()).setMMSubtitle(R.string.title_medication);

        //Set the FAB visible
        ((MMMainActivity) getActivity()).showFAB();


        //hide the soft keyboard if it is visible
        MMUtilities.hideSoftKeyboard(getActivity());
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        setUISaved();
    }

    //********************************************/
    //*******   Initialization Methods  **********/
    //********************************************/
    private void wireWidgets(View v){

        Button medicationExitButton = (Button) v.findViewById(R.id.medicationExitButton);
        medicationExitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onExit();
            }
        });


        Button medicationSaveButton = (Button) v.findViewById(R.id.medicationSaveButton);
        medicationSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSave();
            }
        });


        final SwitchCompat existSwitch = (SwitchCompat) v.findViewById(R.id.switchExists);
        existSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                setUIChanged();
                View v = getView();
                if (v != null) {
                    if (existSwitch.isChecked()) {
                        v.setBackgroundColor(ContextCompat.
                                getColor(getActivity(), R.color.colorScreenBackground));
                    } else {
                        v.setBackgroundColor(ContextCompat.
                                getColor(getActivity(), R.color.colorScreenDeletedBackground));
                    }
                }
            }
        });




        EditText medNickNameInput = (EditText) v.findViewById(R.id.medicationNickNameInput);
        medNickNameInput.addTextChangedListener(new TextWatcher() {
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
        });



        EditText medBrandNameInput = (EditText) v.findViewById(R.id.medicationBrandNameInput);
        medBrandNameInput.addTextChangedListener(new TextWatcher() {
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
        });



        EditText medGenericNameInput = (EditText) v.findViewById(R.id.medicationGenericNameInput);
        medGenericNameInput.addTextChangedListener(new TextWatcher() {
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
        });



        EditText medDoseAmountInput = (EditText) v.findViewById(medicationDoseAmountInput);
        medDoseAmountInput.addTextChangedListener(new TextWatcher() {
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
        });



        EditText medDoseUnitsInput = (EditText) v.findViewById(R.id.medicationDoseUnitsInput);
        medDoseUnitsInput.addTextChangedListener(new TextWatcher() {
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
        });



        Button upDoseNumber = (Button) v.findViewById(R.id.medicationUpButton);
        upDoseNumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleUpButton();
            }
        });

        Button downDoseNumber = (Button) v.findViewById(R.id.medicationDownButton);
        downDoseNumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleDownButton();
            }
        });
    }

    private void wireStrategySpinner(View v){
        //set the default
        mSelectedStrategyTypePosition = sSET_SCHEDULE_FOR_MEDICATION;
        mOldPosition = mSelectedStrategyTypePosition;

        //Then initialize the spinner itself
        Spinner spinner = (Spinner) v.findViewById(R.id.strategy_type_spinner);

        // Create an ArrayAdapter using the Activities context AND
        // the string array and a default spinner layout
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_spinner_item,
                mStrategyTypes);

        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);

        //attach the listener to the spinner
        spinner.setOnItemSelectedListener(this);

    }

    private void wireListTitleWidgets(View v){
        View field_container;
        TextView label;

        MMMainActivity myActivity = (MMMainActivity)getActivity();

        //set up the labels for the medication list
        field_container = v.findViewById(R.id.schedTitleRow);

        label = (EditText) (field_container.findViewById(R.id.scheduleTimeOutput));
        label.setText(R.string.medication_dose_time);
        label.setBackgroundColor(ContextCompat.getColor(myActivity, R.color.colorHistoryLabelBackground));
    }

    private void initializeRecyclerView(View v){
            /*
             * The steps for doing recycler view in onCreateView() of a fragment are:
             * 1) inflate the .xml
             *
             * the special recycler view stuff is:
             * 2) get and store a reference to the recycler view widget that you created in xml
             * 3) create and assign a layout manager to the recycler view
             * 4) assure that there is data for the recycler view to show.
             * 5) use the data to create and set an adapter in the recycler view
             * 6) create and set an item animator (if desired)
             * 7) create and set a line item decorator
             * 8) add event listeners to the recycler view
             *
             * 9) return the view
             */
        //1) Inflate the layout for this fragment
        //      done in the caller


        //2) find and remember the RecyclerView
        RecyclerView recyclerView = getRecyclerView(v);

        //3) create and assign a layout manager to the recycler view
        //RecyclerView.LayoutManager mLayoutManager  = new LinearLayoutManager(getActivity());
        RecyclerView.LayoutManager mLayoutManager  = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(mLayoutManager);

        //4) Get the Cursor of ScheduleMedication DB rows from the DB.
        // The medication knows how to do this
        MMMedication medication = getMedicationInstance(mPersonID, mPosition);
        if (medication == null)return;

        Cursor scheduleCursor = medication.getSchedulesCursor();

        //5) Use the data to Create and set out SchedMed Adapter
        MMSchedCursorAdapter adapter
                = new MMSchedCursorAdapter(scheduleCursor,
                                           MMUtilities.is24Format(),
                                           medication.getMedicationID());
        recyclerView.setAdapter(adapter);

        //initialize the UI for number per day equal to the number of existing schedules
        TextView medDoseNumInput = (TextView) v.findViewById(medicationDoseNumInput);
        medDoseNumInput.setText(String.valueOf(adapter.getItemCount()));

        //6) create and set the itemAnimator
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        //7) create and add the item decorator
        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(),
                DividerItemDecoration.VERTICAL));
 /*
           recyclerView.addItemDecoration(new DividerItemDecoration(
                getActivity(),
                LinearLayoutManager.VERTICAL));
*/

        //8) add event listeners to the recycler view
        recyclerView.addOnItemTouchListener(
                new MMHomeFragment.RecyclerTouchListener(getActivity(),
                                                         recyclerView,
                                                         new MMHomeFragment.ClickListener() {

                            @Override
                            public void onClick(View view, int position) {
                                onSelect(position);
                            }

                            @Override
                            public void onLongClick(View view, int position) {

                            }
                        }));

    }

    private void initializeUI(View v){
        if (mPersonID == MMUtilities.ID_DOES_NOT_EXIST){
            throw new RuntimeException(getString(R.string.no_person_med));
        }
        MMPerson person = MMUtilities.getPerson(mPersonID);

        CharSequence nickname;
        if (person == null) {
            nickname = getString(R.string.no_person_med);
        } else {
            nickname = person.getNickname().toString().trim();
        }
        TextView medicationForPerson = (TextView) v.findViewById(R.id.medicationForPersonNickName);
        medicationForPerson.       setText(nickname);
        //mMedNickNameLastInput = nickname;

        EditText medicationForInput = (EditText) v.findViewById(R.id.medicationForPersonIDInput);
        medicationForInput.        setText(Long.valueOf(mPersonID).toString().trim());

        MMMedication medication = getMedicationInstance(mPersonID, mPosition);

        CharSequence selectedStrategyType;

        EditText medIDInput          = (EditText) v.findViewById(R.id.medicationIdInput);
        EditText medBrandNameInput   = (EditText) v.findViewById(R.id.medicationBrandNameInput);
        EditText medGenericNameInput = (EditText) v.findViewById(R.id.medicationGenericNameInput);
        EditText medNickNameInput    = (EditText) v.findViewById(R.id.medicationNickNameInput);
        TextView medDoseAmountInput  = (TextView) v.findViewById(medicationDoseAmountInput);
        EditText medDoseUnitsInput   = (EditText) v.findViewById(R.id.medicationDoseUnitsInput);
        TextView medDoseNumInput     = (TextView) v.findViewById(medicationDoseNumInput);

        if (medication == null) {
            medIDInput         .setText(String.valueOf(MMUtilities.ID_DOES_NOT_EXIST));
            medIDInput.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.colorLightPink));

            CharSequence brand = MMMedication.getDefaultBrandName().toString().trim();
            medBrandNameInput.setText(brand);
            //mMedBrandNameLastInput = brand;

            CharSequence generic = MMMedication.getDefaultGenericName().toString().trim();
             medGenericNameInput.setText(generic);
           // mMedGenericNameLastInput = generic;

            CharSequence medNick = MMMedication.getDefaultMedicationNickname().toString().trim();
              medNickNameInput.setText(medNick);
            //mMedNickNameLastInput = medNick;

            CharSequence strategy = String.valueOf(MMMedication.getDefaultDoseStrategy());
            //mMedicationDoseStrategyInput.setText(strategy);
            // mMedDoseStrategyLastInput = strategy;
            selectedStrategyType = strategy.toString();

           CharSequence amt = String.valueOf(MMMedication.getDefaultDoseAmount());
           medDoseAmountInput.setText(amt);
           // mMedDoseAmountLastInput = amt;

            CharSequence units = MMMedication.getDefaultDoseUnits().toString().trim();
            medDoseUnitsInput.setText(units);
            //mMedDoseUnitsLastInput = units;

            CharSequence numPerDay = String.valueOf(MMMedication.getDefaultDoseNumPerDay());
            medDoseNumInput.setText(numPerDay);
            //mMedDoseNumLastInput = numPerDay;

        } else {
            medIDInput         .setText(String.valueOf( medication.getMedicationID()));
            medIDInput.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.colorGray));

            CharSequence brand = medication.getBrandName().toString().trim();
            medBrandNameInput  .setText(brand);
            //mMedBrandNameLastInput = brand;

            CharSequence generic = medication.getGenericName().toString().trim();
            medGenericNameInput.setText(generic);
            //mMedGenericNameLastInput = generic;

            CharSequence medNick = medication.getMedicationNickname().toString().trim();
            medNickNameInput   .setText(medNick);
           // mMedNickNameLastInput = medNick;

            CharSequence strategy = String.valueOf(medication.getDoseStrategy());
            //mMedicationDoseStrategyInput.setText(strategy);
            //mMedDoseStrategyLastInput = strategy;
            selectedStrategyType = strategy.toString();

            CharSequence amt = String.valueOf(medication.getDoseAmount());
            medDoseAmountInput.setText(amt);
            //mMedDoseAmountLastInput = amt;

            CharSequence units = medication.getDoseUnits().toString().trim();
            medDoseUnitsInput  .setText(units);
            //mMedDoseUnitsLastInput = units;

            CharSequence numPerDay = String.valueOf(medication.getDoseNumPerDay());
            medDoseNumInput.setText(numPerDay);
           // mMedDoseNumLastInput = numPerDay;
        }



        if (selectedStrategyType.equals(SCHEDULE_STRATEGY)) {
            mSelectedStrategyTypePosition = sSET_SCHEDULE_FOR_MEDICATION;
        } else if (selectedStrategyType == AS_NEEDED_STRATEGY) {
            mSelectedStrategyTypePosition = sAS_NEEDED;
        }
        Spinner spinner = (Spinner) v.findViewById(R.id.strategy_type_spinner);
        spinner.setSelection(mSelectedStrategyTypePosition);

        mOldPosition = mSelectedStrategyTypePosition;


        //set the switch to whether the Person exists
        SwitchCompat existSwitch = (SwitchCompat) v.findViewById(R.id.switchExists) ;

        //This is certainly overkill, but it is explicit
        if (medication != null) {
            if (medication.isCurrentlyTaken()) {
                existSwitch.setChecked(true);
                v.setBackgroundColor(ContextCompat.
                        getColor(getActivity(), R.color.colorScreenBackground));
            } else {
                existSwitch.setChecked(false);
                v.setBackgroundColor(ContextCompat.
                        getColor(getActivity(), R.color.colorScreenDeletedBackground));
            }
        } else {
            //set the default to exists
            existSwitch.setChecked(true);
            v.setBackgroundColor(ContextCompat.
                    getColor(getActivity(), R.color.colorScreenBackground));
        }
        setUISaved(v);
    }



    public long getPersonID(){
        return mPersonID;
    }


    //************************************************/
    //*********  UI Saved / Changed  Methods  ********/
    //************************************************/

    private void setUISaved(){
        View v = getView();
        setUISaved(v);
    }
    private void setUISaved(View v){
        if (v == null)return;

        isUIChanged = false;

        //disable the save button
        saveButtonEnable(v, MMUtilities.BUTTON_DISABLE);

        setUpDownEnabled();
    }

    private void setUIChanged(){
        View v = getView();
        setUIChanged(v);
    }
    private void setUIChanged(View v){
        if (v == null)return;

        isUIChanged = true;

        saveButtonEnable(v, MMUtilities.BUTTON_ENABLE);
        setUpDownEnabled();
    }

    private void saveButtonEnable(boolean isEnabled){
        View v = getView();
        saveButtonEnable(v, isEnabled);
    }
    private void saveButtonEnable(View v, boolean isEnabled){

        if (v == null)return; //onCreateView() hasn't run yet

        Button medicationSaveButton =
                (Button) v.findViewById(R.id.medicationSaveButton);

        MMUtilities.enableButton(getActivity(),
                medicationSaveButton,
                isEnabled);
    }

    private void    setUpDownEnabled(){
        View v = getView();
        if (v == null)return;

        Button upDoseNumber = (Button) v.findViewById(R.id.medicationUpButton);
        Button downDoseNumber = (Button) v.findViewById(R.id.medicationDownButton);

        if (isUpDownEnabled()){
            upDoseNumber.setEnabled(true);
            upDoseNumber.setBackgroundColor(
                    ContextCompat.getColor(getActivity(),R.color.colorButton1Background));

            downDoseNumber.setEnabled(true);
            downDoseNumber.setBackgroundColor(
                    ContextCompat.getColor(getActivity(),R.color.colorButton1Background));
        } else {
            upDoseNumber.setEnabled(false);
            upDoseNumber.setBackgroundColor(
                    ContextCompat.getColor(getActivity(),R.color.colorButton2Background));

            downDoseNumber.setEnabled(false);
            downDoseNumber.setBackgroundColor(
                    ContextCompat.getColor(getActivity(),R.color.colorButton2Background));
        }
    }
    private boolean isUpDownEnabled(){
        int strategy = mSelectedStrategyTypePosition;

        if (strategy != sSET_SCHEDULE_FOR_MEDICATION) {
            //This button doesn't do anything if the strategy isn't set
            //Toast.makeText(getActivity(),R.string.wrong_strategy, Toast.LENGTH_SHORT).show();
            return false;
        }
        return !isUIChanged;
    }


    //***********************************************************/
    //*********  RecyclerView / Adapter related Methods  ********/
    //***********************************************************/

    private void reinitializeCursor(long medicationID){
        if (medicationID == MMUtilities.ID_DOES_NOT_EXIST) return;

        //reset the list
        MMSchedCursorAdapter adapter = getAdapter(getView());

        if (adapter != null) {
            adapter.reinitializeCursor(medicationID);
        } else {
            //we did not have a medication earlier so it never got initialized
            initializeRecyclerView(getView());
        }
    }

    private RecyclerView getRecyclerView(View v){
        return  (RecyclerView) v.findViewById(R.id.scheduleList);
    }

    private MMSchedCursorAdapter getAdapter(View v){
        return (MMSchedCursorAdapter)  getRecyclerView(v).getAdapter();
    }


    //********************************************/
    //*********     Schedule Methods    **********/
    //********************************************/

    private TimePickerDialog showPicker(final long schedMedID,
                                        int hour,
                                        int minute,
                                        boolean is24Format){

        //in the onDismissListener() need to know if user pressed OK or Cancel
        userPressedOKinTimePicker = false;

        //cancel the alarm for the original time
        final int minutesSinceMidnight = (hour * 60) + minute;
        cancelOneAlarm(minutesSinceMidnight);

        final TimePickerDialog timePickerDialog =
                new TimePickerDialog( getActivity(), new TimePickerDialog.OnTimeSetListener() {

                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        userPressedOKinTimePicker = true;
                        ArrayList<MMScheduleMedication> schedules =
                                                            getSchedules(mPersonID, mPosition);
                        if (schedules == null)return;

                        int last = schedules.size();
                        int position = 0;
                        MMScheduleMedication scheduleMedication;
                        while (position < last){
                            scheduleMedication = schedules.get(position);
                            if (schedMedID == scheduleMedication.getSchedMedID()){
                                //timeOfDay is number of MINUTES since midnight
                                int timeOfDay = (hourOfDay * 60) + minute;
                                //update the time to take the med, and
                                scheduleMedication.setTimeDue(timeOfDay);
                                //update the DB row with this schedule
                                MMSchedMedManager schedMedManager = MMSchedMedManager.getInstance();
                                schedMedManager.addScheduleMedication(scheduleMedication);

                                //issue an alarm for this new time
                                MMUtilities.setNotificationAlarm(getActivity(), timeOfDay);

                                //Now update the UI list
                                reinitializeCursor(scheduleMedication.getOfMedicationID());
                                return;
                            }
                            position++;
                        }
                    }
                },
                hour,
                minute,
                is24Format);
        timePickerDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                //This listener fires regardless of whether the user picks OK, Cancel, or Back
                //need to be able to differentiate between the three cases.
                if (!userPressedOKinTimePicker) {
                    //need to set an Alarm for the old time as the user pressed cancel
                    MMUtilities.setNotificationAlarm(getActivity(), minutesSinceMidnight);
                }
            }
        });
        timePickerDialog.show();
        return timePickerDialog;
    }


    //*********************************************************/
    //      Utility Functions using passed arguments          //
    //*********************************************************/
    public MMMedication getMedicationInstance(){
        return getMedicationInstance(mPersonID, mPosition);
    }

    private MMMedication getMedicationInstance(long personID, int position){
        //If personID can't be found in the list, person will be null
        MMPerson person = MMUtilities.getPerson(personID);
        if (person == null)return null;
        if (position < 0)return null; //means we are adding the medication

        ArrayList<MMMedication> medications = person.getMedications();
        if (medications == null){
            medications = new ArrayList<>();
        }
        MMMedication medication;

        //flag assumes the medication is already there, but if not......
        if ((position == -1) || (medications.size() < mPosition)) {
            //return an error
           return null;
        } else {
            medication = person.getMedications().get(mPosition);
        }

        return medication;
    }


    private ArrayList<MMScheduleMedication> getSchedules(long personID, int position){
        MMMedication medication = getMedicationInstance(personID, position);
        if (medication == null)return null;

        return medication.getSchedules();
    }


    //********************************************/
    //*********     Spinner Callbacks   **********/
    //********************************************/
    public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
        mOldPosition = mSelectedStrategyTypePosition;
        mSelectedStrategyTypePosition = position;
       //mSelectedStrategyType = (String) parent.getItemAtPosition(position);

        if (mOldPosition != mSelectedStrategyTypePosition) {
            setUIChanged();
        }

    }

    public void onNothingSelected(AdapterView<?> parent) {
        //for now, do nothing
    }



    //********************************************/
    //*********    Event Handlers       **********/
    //********************************************/
    private void onSave(){
        View v = getView();
        if (v == null)return;

        Toast.makeText(getActivity(),
                R.string.save_label,
                Toast.LENGTH_SHORT).show();

        //get rid of the soft keyboard if it is visible
        MMUtilities.hideSoftKeyboard(getActivity());

        //Person medication is the medication in the Person's medication list
        //update it with values from this screen
        MMMedication medication = getMedicationInstance(mPersonID, mPosition);

        if (medication == null) {
            //we are creating this medication for the first time
            medication = new MMMedication(MMUtilities.ID_DOES_NOT_EXIST);
            medication.setForPersonID(mPersonID);
        }

        //get handles for the UI widgets
        EditText medNickNameInput    = (EditText) v.findViewById(R.id.medicationNickNameInput);
        EditText medBrandNameInput   = (EditText) v.findViewById(R.id.medicationBrandNameInput);
        EditText medGenericNameInput = (EditText) v.findViewById(R.id.medicationGenericNameInput);
        TextView medDoseNumInput     = (TextView) v.findViewById(medicationDoseNumInput);
        EditText medDoseUnitsInput   = (EditText) v.findViewById(R.id.medicationDoseUnitsInput);
        EditText medDoseAmountInput  = (EditText) v.findViewById(medicationDoseAmountInput);

        //Set the medication as belonging to the person
        medication.setForPersonID       (mPersonID);
        medication.setMedicationNickname(medNickNameInput.   getText().toString().trim());
        medication.setBrandName         (medBrandNameInput.  getText().toString().trim());
        medication.setGenericName       (medGenericNameInput.getText().toString().trim());

        // TODO: 3/14/2017 only one of these should be retained
        medication.setDoseStrategy (
                //Integer.valueOf(mMedicationDoseStrategyInput.getText().toString().trim()));
                mSelectedStrategyTypePosition);

        medication.setDoseUnits         (medDoseUnitsInput.  getText().toString().trim());
        medication.setDoseAmount   (
                Integer.valueOf(medDoseAmountInput.getText().toString().trim()));

        medication.setDoseNumPerDay(
                Integer.valueOf(medDoseNumInput.   getText().toString().trim()));

        SwitchCompat existSwitch = (SwitchCompat) v.findViewById(R.id.switchExists);
        if (existSwitch.isChecked()) {
            medication.setCurrentlyTaken(true);
            v.setBackgroundColor(ContextCompat.
                    getColor(getActivity(), R.color.colorScreenBackground));
        } else {
            medication.setCurrentlyTaken(false);
            v.setBackgroundColor(ContextCompat.
                    getColor(getActivity(), R.color.colorScreenDeletedBackground));
        }



        //Add the medication to the person if necessary, but definitely add to the DB
        //add the scheduleMedications to the DB as well
        MMMedicationManager medicationManager = MMMedicationManager.getInstance();
        MMPerson person = MMUtilities.getPerson(mPersonID);
        //Add the medication to the person, and to the DB
        boolean addToDBToo = true;
        if (person == null) {
            MMUtilities.errorHandler(getActivity(), R.string.exception_medication_not_added);
        } else {
            if (!medicationManager.addToPerson(person, medication, addToDBToo)) {
                MMUtilities.errorHandler(getActivity(), R.string.exception_medication_not_added);
            } else {

                //update the medicationID on the UI
                EditText medIDInput = (EditText) v.findViewById(R.id.medicationIdInput);
                medIDInput.setText(String.valueOf(medication.getMedicationID()));
                medIDInput.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.colorGray));

                //Tell the user everything went well
                Toast.makeText(getActivity(), R.string.save_successful, Toast.LENGTH_SHORT).show();

                //disable the save button because we just saved
                saveButtonEnable(MMUtilities.BUTTON_DISABLE);


                //update position with this medications position
                ArrayList<MMMedication> medications = person.getMedications();
                MMMedication checkMed;
                int last = medications.size();
                int position = 0;
                long medicationID = medication.getMedicationID();
                while (position < last) {
                    checkMed = medications.get(position);
                    if (medicationID == checkMed.getMedicationID()) {
                        mPosition = position;
                        position = last;//so we'll fall out of the loop
                    }
                    position++;
                }

                //up / down buttons are set properly in the setUISaved() method

                //reinitialize the schedule list
                reinitializeCursor(medicationID);


            }
        }
        setUISaved();

    }

    private void onExit(){
        Toast.makeText(getActivity(),
                R.string.exit_label,
                Toast.LENGTH_SHORT).show();

        if (isUIChanged) {
            areYouSureExit();
        } else {
            switchToExit();
        }
    }

    public void handleUpButton(){
        if (!isUpDownEnabled())return;
        View v = getView();
        if (v == null)return;

        //increment the value on the UI
        TextView medDoseNumInput = (TextView) v.findViewById(medicationDoseNumInput);
        int size = Integer.valueOf(medDoseNumInput.getText().toString());
        size++;

        //Create a new schedule with time = 6AM
        MMMedication medication = getMedicationInstance(mPersonID, mPosition);
        if (medication != null) {
            medDoseNumInput.setText(String.valueOf(size));
            long medicationID = medication.getMedicationID();
            MMScheduleMedication schedule =
                    new MMScheduleMedication(medicationID,mPersonID,(6*60));
            medication.addSchedule(schedule);

            //shouldn't have to reinitialize cursor because onSave() will do it for us
            //reinitializeCursor(medicationID);
            onSave();

            //Enable the Alarm receiver. It will stay enabled across reboots
            ComponentName receiver = new ComponentName(getActivity(), MMAlarmReceiver.class);
            PackageManager pm = getActivity().getPackageManager();

            pm.setComponentEnabledSetting(receiver,
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                    PackageManager.DONT_KILL_APP);

            //create an Alarm to generate a notification for this scheduled dose
            MMUtilities.setNotificationAlarm(getActivity(), schedule.getTimeDue());
        }
    }

    public void handleDownButton(){
        if (!isUpDownEnabled())return;
        View v = getView();
        if (v == null)return;


        //Decrement the value in the UI
        TextView medDoseNumInput = (TextView) v.findViewById(medicationDoseNumInput);

        //get rid of the last schedule
        int last = Integer.valueOf(medDoseNumInput.getText().toString());
        int position = 0;
        int latestTime = 0;
        int latestTimePosition = 0;
        long latestTimeScheduleID = 0;
        MMScheduleMedication latestTimeSchedule = null;
        MMScheduleMedication schedule;

        MMMedication medication = getMedicationInstance(mPersonID, mPosition);
        if (medication == null)return;
        ArrayList<MMScheduleMedication> schedules = medication.getSchedules();

        while (position < last){
            schedule = schedules.get(position);
            if (latestTime == 0)latestTime = schedule.getTimeDue();
            if (latestTime <= schedule.getTimeDue()){
                latestTime = schedule.getTimeDue();
                latestTimePosition = position;
                latestTimeSchedule = schedule;
                latestTimeScheduleID = schedule.getSchedMedID();
            }
            position++;
        }
        if (latestTimeSchedule != null){

            //If this is the only schedule due at this time
            // Remove any alarms for this schedule
            schedule = schedules.get(latestTimePosition);
            cancelOneAlarm(schedule.getTimeDue());

            //remove the schedule from the Medications List
            schedules.remove(latestTimePosition);

            //get rid of the schedule row in the DB
            MMSchedMedManager schedMedManager = MMSchedMedManager.getInstance();
            schedMedManager.removeSchedMedFromDB(latestTimeScheduleID);

            //let the user know the number of doses has decreased
            medDoseNumInput.setText(String.valueOf(last-1));

        }
        onSave();
    }



    //***********************************/
    //****  Exit Button Dialogue    *****/
    //***********************************/
    //Build and display the alert dialog
    private void areYouSureExit(){
        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.abandon_title)
                .setIcon(R.drawable.ground_station_icon)
                .setMessage(R.string.are_you_sure)
                .setPositiveButton(R.string.exit_label,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                //Leave even though project has chaged
                                //Toast.makeText(getActivity(), R.string.exit_label, Toast.LENGTH_SHORT).show();
                                switchToExit();

                            }
                        })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                        Toast.makeText(getActivity(),
                                "Pressed Cancel",
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .setIcon(R.drawable.ground_station_icon)
                .show();
    }

    private void switchToExit(){
        MMSchedCursorAdapter adapter = getAdapter(getView());
        if (adapter != null) adapter.closeCursor();

        ((MMMainActivity) getActivity()).switchToPersonScreen(mPersonID);   //switchToPopBackstack();
    }

    //*********************************************************/
    //      Utility Functions used in handling events         //
    //*********************************************************/
    //called from onClick(), executed when a Schedule is selected
    private void onSelect(int position){

        //Need the schedule being updated, so ask the Adapter
        MMSchedCursorAdapter adapter = getAdapter(getView());
        adapter.notifyItemChanged(position);

        MMScheduleMedication schedule = adapter.getScheduleAt(position);

        //create a dialogue to allow the user to:
        // change the Schedules on this Medication
        //The schedule is actually changed in the picker handler
        //and a new alarm is set there for the new time

        int hours = schedule.getTimeDue()/60;
        int minutes = schedule.getTimeDue() - (hours * 60);
        showPicker(schedule.getSchedMedID(), hours, minutes, MMUtilities.is24Format() );

        //The schedule itself and its alarms are actually updated in the TimePicker callbacks
    }


    //*********************************/
    //     Alarms and  Notifications  //
    //********************************/
    private void cancelOneAlarm(int minutesSinceMidnight){
        //If this is the only schedule due at this time
        // Remove any alarms for this schedule
        MMSchedMedManager schedMedManager = MMSchedMedManager.getInstance();
        int howManyMedsDue = schedMedManager.howManyDueAt(minutesSinceMidnight);

        //if there is only one medication dose due at this time, delete the alarm
        //if there are more than one due at this time, leave the existing alarm in place
        if (howManyMedsDue == 1) {
            //The alarm is based on when the dose is due
            cancelNotificationAlarms(minutesSinceMidnight);
        }
    }

    private void cancelNotificationAlarms(int minutesSinceMidnight){
        //get the PendingIntent that describes the action we desire,
        // so that it can be performed when the alarm goes off
        PendingIntent pendingIntent = getNotificationAlarmAction(minutesSinceMidnight);

        AlarmManager alarmManager = (AlarmManager) getActivity().getSystemService(ALARM_SERVICE);

        //cancel any previous alarms
        alarmManager.cancel(pendingIntent);
    }

    private PendingIntent getNotificationAlarmAction(int requestCode){
        //explicit intent naming the receiver class
        Intent alarmIntent = new Intent(getActivity(), MMAlarmReceiver.class);
        alarmIntent.putExtra(MMAlarmReceiver.NOTIFICATION_ID, 1);
        alarmIntent.putExtra(MMAlarmReceiver.NOTIFICATION,getNotification());

        //wake up the explicit Activity when the alarm goes off
        //return PendingIntent.getActivity (getActivity(), //context
        //broadcast when the alarm goes off
        return PendingIntent.getBroadcast(getActivity(), //context
                                          requestCode,   //request code
                                          alarmIntent,   //explicit intent to be broadcast
                                          PendingIntent.FLAG_UPDATE_CURRENT);
                                                         //flags that control which unspecified
                                                         // parts of the intent can be supplied
                                                         // when the actual send happens


    }

    private Notification getNotification(){
                /*  Create a Notification Builder  */
        NotificationCompat.Builder builder =
                (NotificationCompat.Builder) new NotificationCompat.Builder(getActivity())
                        .setSmallIcon(R.drawable.ground_station_icon)
                        .setContentTitle(getString(R.string.app_name))
                        .setContentText(getString(R.string.time_to_take))
                        //notification is canceled as soon as it is touched by the user
                        .setAutoCancel(true);

        return builder.build();

    }


}
