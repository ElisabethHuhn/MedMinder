package com.androidchicken.medminder;

import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
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


/**
 * The main UI screen for maintaining (CRUD) a Medication
 */
public class MMMedicationFragment extends Fragment implements AdapterView.OnItemSelectedListener {

    private static final int SET_SCHEDULE_FOR_MEDICATION = 0;
    private static final int AS_NEEDED                   = 1;

    private static final String SCHEDULE_STRATEGY  = "Schedule Medication Doses";
    private static final String AS_NEEDED_STRATEGY = "Take as needed";

    /***********************************************/
    /*        UI Widget Views                      */
    /***********************************************/

    private TextView mMedicationForPerson;
    private EditText mMedicationForInput;
    private EditText mMedicationIDInput;
    private EditText mMedicationNickNameInput;
    private EditText mMedicationBrandNameInput;
    private EditText mMedicationGenericNameInput;
    private EditText mMedicationDoseAmountInput;
    private EditText mMedicationDoseUnitsInput;
    private TextView mMedicationDoseNumInput;
    //put the strategy into a spinner
   // private EditText mMedicationDoseStrategyInput;

    private Button   mUpDoseNumber;
    private Button   mDownDoseNumber;





    /***********************************************************/
    /*   Variables that need to survive configuration change   */
    /***********************************************************/
    boolean isInputChanged = false;


    /***********************************************/
    /*        Passed Arguments to this Fragment    */
    /***********************************************/
    private long     mPersonID;
    private int      mPosition;

    /***********************************************************/
    /******  Strategy types for Spinner Widgets     **********/
    /***********************************************************/
    private String[] mStrategyTypes  = new String[]{SCHEDULE_STRATEGY, AS_NEEDED_STRATEGY };

    //private String   mSelectedStrategyType;
    private int      mSelectedStrategyTypePosition;
    private int      mOldPosition;




    /***********************************************/
    /*          RecyclerView Widgets               */
    /***********************************************/
    //all are now local to initializeRecyclerView()


    /***********************************************/
    /*          Static Methods                     */
    /***********************************************/


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

    /***********************************************/
    /*          Constructor                        */
    /***********************************************/
    public MMMedicationFragment() {
    }


    /***********************************************/
    /*          Lifecycle Methods                  */
    /***********************************************/


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

        //set the title bar subtitle
        ((MainActivity) getActivity()).setMMSubtitle(R.string.title_medication);

       setUISaved();

        return v;
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState){
        // Save custom values into the bundle
        savedInstanceState.putLong(MMPerson.sPersonIDTag, mPersonID);
        savedInstanceState.putInt(MMPerson.sPersonMedicationPositionTag, mPosition);
        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onResume(){
        super.onResume();

        setUISaved();
    }

    /*********************************************/
    /********   Initialization Methods  **********/
    /*********************************************/
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

/*
        Button medicationDeleteButton = (Button) v.findViewById(R.id.medicationDeleteButton);
        MMMedication medication = getMedicationInstance(mPersonID, mPosition);
        if (medication == null) {
            //Delete button should be disabled until first save
            deleteButtonEnable(v, MMUtilities.BUTTON_DISABLE);
        } else {
            deleteButtonEnable(v, MMUtilities.BUTTON_ENABLE);
            if (!medication.isCurrentlyTaken()) {
                medicationDeleteButton.setText(R.string.reinstate_title);
            }
        }
        medicationDeleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onDelete();
            }
        });
*/

        SwitchCompat existSwitch = (SwitchCompat) v.findViewById(R.id.switchExists) ;
        existSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                setUIChanged();
            }
        });



        //Patient Nick Name
        //There are no events associated with this field
        mMedicationForPerson = (TextView) v.findViewById(R.id.medicationForPerson);

        //Patient ID
        //There are no events associated with this field
        mMedicationForInput = (EditText) v.findViewById(R.id.medicationForPersonInput);


        //Medication ID
        //There are no events associated with this field
        mMedicationIDInput = (EditText) v.findViewById(R.id.medicationIdInput);


        mMedicationNickNameInput = (EditText) v.findViewById(R.id.medicationNickNameInput);
        mMedicationNickNameInput.addTextChangedListener(new TextWatcher() {
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



        mMedicationBrandNameInput = (EditText) v.findViewById(R.id.medicationBrandNameInput);
        mMedicationBrandNameInput.addTextChangedListener(new TextWatcher() {
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



        mMedicationGenericNameInput = (EditText) v.findViewById(R.id.medicationGenericNameInput);
        mMedicationGenericNameInput.addTextChangedListener(new TextWatcher() {
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



        mMedicationDoseAmountInput = (EditText) v.findViewById(R.id.medicationDoseAmountInput);
        mMedicationDoseAmountInput.addTextChangedListener(new TextWatcher() {
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



        mMedicationDoseUnitsInput = (EditText) v.findViewById(R.id.medicationDoseUnitsInput);
        mMedicationDoseUnitsInput.addTextChangedListener(new TextWatcher() {
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


        mMedicationDoseNumInput = (TextView) v.findViewById(R.id.medicationDoseNumInput);

        mUpDoseNumber = (Button) v.findViewById(R.id.medicationUpButton);
        mUpDoseNumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isUpDownEnabled())return;

                //increment the value on the UI
                int size = Integer.valueOf(mMedicationDoseNumInput.getText().toString());
                size++;

                //Create a new schedule with time = 6AM
                MMMedication medication = getMedicationInstance(mPersonID, mPosition);
                if (medication != null) {
                    mMedicationDoseNumInput.setText(String.valueOf(size));
                    long medicationID = medication.getMedicationID();
                    MMScheduleMedication schedule =
                            new MMScheduleMedication(medicationID,mPersonID,(6*60));
                    medication.addSchedule(schedule);

                    //shouldn't have to reinitialize cursor because onSave() will do it for us
                    //reinitializeCursor(medicationID);
                    onSave();
                }

            }
        });

        mDownDoseNumber = (Button) v.findViewById(R.id.medicationDownButton);
        mDownDoseNumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isUpDownEnabled())return;

                //Decrement the value in the UI
                int size = Integer.valueOf(mMedicationDoseNumInput.getText().toString());
                size--;

                //get rid of the last schedule
                int last = size;
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
                    if (latestTime < schedule.getTimeDue()){
                        latestTime = schedule.getTimeDue();
                        latestTimePosition = position;
                        latestTimeSchedule = schedule;
                        latestTimeScheduleID = schedule.getSchedMedID();
                    }
                    position++;
                }
                if (latestTimeSchedule != null){
                    mMedicationDoseNumInput.setText(String.valueOf(size));
                    schedules.remove(latestTimePosition);
                    MMSchedMedManager schedMedManager = MMSchedMedManager.getInstance();
                    schedMedManager.removeSchedMedFromDB(latestTimeScheduleID);
                }
                onSave();
            }
        });
    }

    private void wireStrategySpinner(View v){
        //set the default
        mSelectedStrategyTypePosition = SET_SCHEDULE_FOR_MEDICATION;
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

        MainActivity myActivity = (MainActivity)getActivity();

        //set up the labels for the medication list
        field_container = v.findViewById(R.id.schedMedTitleRow);

        label = (EditText) (field_container.findViewById(R.id.scheduleTimeHourOutput));
        label.setEnabled(false);
        label.setText(R.string.medication_dose_hours);
        label.setBackgroundColor(ContextCompat.getColor(myActivity, R.color.colorHistoryLabelBackground));

        label = (EditText) (field_container.findViewById(R.id.scheduleTimeMinutesOutput));
        label.setEnabled(false);
        label.setText(R.string.medication_dose_minutes);
        label.setBackgroundColor(ContextCompat.getColor(myActivity, R.color.colorHistoryLabelBackground));

        label = (EditText) (field_container.findViewById(R.id.scheduleTimeAmPmOutput));
        label.setEnabled(false);

        label.setText(R.string.medication_dose_am_pm);
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
        MMSchedMedCursorAdapter adapter
                = new MMSchedMedCursorAdapter(scheduleCursor, MMUtilities.is24Format());
        adapter.setAdapterContext(medication.getMedicationID());
        recyclerView.setAdapter(adapter);

        //initialize the UI for number per day equal to the number of existing schedules
        mMedicationDoseNumInput.setText(String.valueOf(adapter.getItemCount()));

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
                                onSelect(view, position);
                            }

                            @Override
                            public void onLongClick(View view, int position) {

                            }
                        }));

    }


    private void initializeUI(View v){
        if (mPersonID == MMUtilities.ID_DOES_NOT_EXIST){
            throw new RuntimeException(getString(R.string.no_person));
        }
        MMPerson person = MMUtilities.getPerson(mPersonID);

        CharSequence nickname;
        if (person == null) {
            nickname = getString(R.string.no_person);
        } else {
            nickname = person.getNickname().toString().trim();
        }
        mMedicationForPerson.       setText(nickname);
        //mMedNickNameLastInput = nickname;

        mMedicationForInput.        setText(Long.valueOf(mPersonID).toString().trim());

        MMMedication medication = getMedicationInstance(mPersonID, mPosition);

        CharSequence selectedStrategyType;

        if (medication == null) {
            mMedicationIDInput         .setText(String.valueOf(MMUtilities.ID_DOES_NOT_EXIST));

            CharSequence brand = MMMedication.getDefaultBrandName().toString().trim();
            mMedicationBrandNameInput.setText(brand);
            //mMedBrandNameLastInput = brand;

            CharSequence generic = MMMedication.getDefaultGenericName().toString().trim();
            mMedicationGenericNameInput.setText(generic);
           // mMedGenericNameLastInput = generic;

            CharSequence medNick = MMMedication.getDefaultMedicationNickname().toString().trim();
            mMedicationNickNameInput.setText(medNick);
            //mMedNickNameLastInput = medNick;

            CharSequence strategy = String.valueOf(MMMedication.getDefaultDoseStrategy());
            //mMedicationDoseStrategyInput.setText(strategy);
           // mMedDoseStrategyLastInput = strategy;
            selectedStrategyType = strategy.toString();

            CharSequence amt = String.valueOf(MMMedication.getDefaultDoseAmount());
            mMedicationDoseAmountInput.setText(amt);
           // mMedDoseAmountLastInput = amt;

            CharSequence units = MMMedication.getDefaultDoseUnits().toString().trim();
            mMedicationDoseUnitsInput.setText(units);
            //mMedDoseUnitsLastInput = units;

            CharSequence numPerDay = String.valueOf(MMMedication.getDefaultDoseNumPerDay());
            mMedicationDoseNumInput.setText(numPerDay);
            //mMedDoseNumLastInput = numPerDay;

        } else {
            mMedicationIDInput         .setText(String.valueOf( medication.getMedicationID()));

            CharSequence brand = medication.getBrandName().toString().trim();
            mMedicationBrandNameInput  .setText(brand);
            //mMedBrandNameLastInput = brand;

            CharSequence generic = medication.getGenericName().toString().trim();
            mMedicationGenericNameInput.setText(generic);
            //mMedGenericNameLastInput = generic;

            CharSequence medNick = medication.getMedicationNickname().toString().trim();
            mMedicationNickNameInput   .setText(medNick);
           // mMedNickNameLastInput = medNick;

            CharSequence strategy = String.valueOf(medication.getDoseStrategy());
            //mMedicationDoseStrategyInput.setText(strategy);
            //mMedDoseStrategyLastInput = strategy;
            selectedStrategyType = strategy.toString();

            CharSequence amt = String.valueOf(medication.getDoseAmount());
            mMedicationDoseAmountInput.setText(amt);
            //mMedDoseAmountLastInput = amt;

            CharSequence units = medication.getDoseUnits().toString().trim();
            mMedicationDoseUnitsInput  .setText(units);
            //mMedDoseUnitsLastInput = units;

            CharSequence numPerDay = String.valueOf(medication.getDoseNumPerDay());
            mMedicationDoseNumInput.setText(numPerDay);
           // mMedDoseNumLastInput = numPerDay;
        }



        if (selectedStrategyType.equals(SCHEDULE_STRATEGY)) {
            mSelectedStrategyTypePosition = SET_SCHEDULE_FOR_MEDICATION;
        } else if (selectedStrategyType == AS_NEEDED_STRATEGY) {
            mSelectedStrategyTypePosition = AS_NEEDED;
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
            } else {
                existSwitch.setChecked(false);
            }
        }


        setUISaved(v);

    }



    /*************************************************/
    /**********  UI Saved / Changed  Methods  ********/
    /*************************************************/

    private void setUISaved(){
        View v = getView();
        setUISaved(v);
    }

    private void setUISaved(View v){
        if (v == null)return;

        isInputChanged = false;

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

        isInputChanged = true;

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
        if (isUpDownEnabled()){
            mUpDoseNumber.setEnabled(true);
            mUpDoseNumber.setBackgroundColor(
                    ContextCompat.getColor(getActivity(),R.color.colorButton1Background));

            mDownDoseNumber.setEnabled(true);
            mDownDoseNumber.setBackgroundColor(
                    ContextCompat.getColor(getActivity(),R.color.colorButton1Background));
        } else {
            mUpDoseNumber.setEnabled(false);
            mUpDoseNumber.setBackgroundColor(
                    ContextCompat.getColor(getActivity(),R.color.colorButton2Background));

            mDownDoseNumber.setEnabled(false);
            mDownDoseNumber.setBackgroundColor(
                    ContextCompat.getColor(getActivity(),R.color.colorButton2Background));
        }
    }

    private boolean isUpDownEnabled(){
        int strategy = mSelectedStrategyTypePosition;

        if (strategy != SET_SCHEDULE_FOR_MEDICATION) {
            //This button doesn't do anything if the strategy isn't set
            //Toast.makeText(getActivity(),R.string.wrong_strategy, Toast.LENGTH_SHORT).show();
            return false;
        }
        if (isInputChanged){
            //Can not do this unless/until save
            //Toast.makeText(getActivity(),R.string.save_first, Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }



    /************************************************************/
    /**********  RecyclerView / Adapter related Methods  ********/
    /************************************************************/

    private void reinitializeCursor(long medicationID){
        if (medicationID == MMUtilities.ID_DOES_NOT_EXIST) return;

        //reset the list
        MMSchedMedCursorAdapter adapter = getAdapter(getView());

        if (adapter != null) {
            adapter.reinitializeCursor(medicationID);
        } else {
            //we did not have a medication earlier so it never got initialized
            initializeRecyclerView(getView());
        }
    }

    private Cursor getCursor(){
        MMSchedMedCursorAdapter scheduleAdapter = getAdapter(getView());
        return scheduleAdapter.getSchedMedCursor();
    }

    private RecyclerView getRecyclerView(View v){
        return  (RecyclerView) v.findViewById(R.id.scheduleList);
    }

    private MMSchedMedCursorAdapter getAdapter(View v){
        return (MMSchedMedCursorAdapter)  getRecyclerView(v).getAdapter();
    }


    /*********************************************/
    /**********     Schedule Methods    **********/
    /*********************************************/

    private TimePickerDialog showPicker(final long schedMedID,
                                              int hour,
                                              int minute,
                                              boolean is24Format){
        TimePickerDialog timePickerDialog =
                new TimePickerDialog( getActivity(), new TimePickerDialog.OnTimeSetListener() {

                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        ArrayList<MMScheduleMedication> schedules =
                                                            getSchedules(mPersonID, mPosition);
                        if (schedules == null)return;

                        int last = schedules.size();
                        int position = 0;
                        MMScheduleMedication scheduleMedication;
                        while (position < last){
                            scheduleMedication = schedules.get(position);
                            if (schedMedID == scheduleMedication.getSchedMedID()){
                                //timeOfDay is number of MINNUTES since midnight
                                int timeOfDay = (hourOfDay * 60) + minute;
                                //update the time to take the med, and
                                scheduleMedication.setTimeDue(timeOfDay);
                                //update the DB row with this schedule
                                MMSchedMedManager schedMedManager = MMSchedMedManager.getInstance();
                                schedMedManager.addScheduleMedication(scheduleMedication);

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
        timePickerDialog.show();
        return timePickerDialog;
    }


    /**********************************************************/
    //      Utility Functions using passed arguments          //
    /**********************************************************/
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


    /*********************************************/
    /**********     Spinner Callbacks   **********/
    /*********************************************/

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


    /****************************************/
    /*****  Listeners for List Views    *****/
    /****************************************/


    /*********************************************/
    /**********    Event Handlers       **********/
    /*********************************************/
    private void onSave(){
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
        //Set the medication as belonging to the person
        medication.setForPersonID       (mPersonID);
        medication.setMedicationNickname(mMedicationNickNameInput.   getText().toString().trim());

        medication.setBrandName         (mMedicationBrandNameInput.  getText().toString().trim());
        medication.setGenericName       (mMedicationGenericNameInput.getText().toString().trim());

        // TODO: 3/14/2017 only one of these should be retained
        medication.setDoseStrategy (
                //Integer.valueOf(mMedicationDoseStrategyInput.getText().toString().trim()));
                mSelectedStrategyTypePosition);

        medication.setDoseUnits         (mMedicationDoseUnitsInput.  getText().toString().trim());
        medication.setDoseAmount   (
                Integer.valueOf(mMedicationDoseAmountInput.getText().toString().trim()));

        medication.setDoseNumPerDay(
                Integer.valueOf(mMedicationDoseNumInput.   getText().toString().trim()));

        //put in the check to silence lint
        View v = getView();
        if (v != null) {
            SwitchCompat existSwitch = (SwitchCompat) v.findViewById(R.id.switchExists);
            if (existSwitch.isChecked()) {
                medication.setCurrentlyTaken(true);
            } else {
                medication.setCurrentlyTaken(false);
            }
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
                mMedicationIDInput.setText(String.valueOf(medication.getMedicationID()));

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
                while (position < last){
                    checkMed = medications.get(position);
                    if (medicationID == checkMed.getMedicationID()){
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

        if (isInputChanged) {
            areYouSureExit();
        } else {
            switchToExit();
        }
    }

    /************************************/
    /*****  Exit Button Dialogue    *****/
    /************************************/
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
        MMSchedMedCursorAdapter adapter = getAdapter(getView());
        adapter.closeCursor();

        ((MainActivity) getActivity()).switchToPersonScreen(mPersonID);   //switchToPopBackstack();
    }


    /**************************************/
    /*****  Delete Button Dialogue    *****/
    /**************************************/
    //Build and display the alert dialog
    private void areYouSureDelete(){
        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.delete_title)
                .setIcon(R.drawable.ground_station_icon)
                .setMessage(R.string.are_you_sure)
                .setPositiveButton(R.string.delete_title,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                //Leave even though project has chaged
                                //Toast.makeText(getActivity(), R.string.exit_label, Toast.LENGTH_SHORT).show();
                                performDelete();

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

    private void performDelete(){
        MMMedication medication = getMedicationInstance(mPersonID, mPosition);
        int msg;

        if (medication == null) {
            msg = R.string.med_not_found_delete;
        } else {
            if (medication.isCurrentlyTaken()) {
                msg = R.string.delete_medication;
                medication.setCurrentlyTaken(false);
            } else {
                msg = R.string.reinstate_medication;
                medication.setCurrentlyTaken(true);
            }
        }
        Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();

    }

    private void areYouSureReinstate(){
        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.reinstate_title)
                .setIcon(R.drawable.ground_station_icon)
                .setMessage(R.string.are_you_sure)
                .setPositiveButton(R.string.reinstate_title,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                //Leave even though project has chaged
                                //Toast.makeText(getActivity(), R.string.exit_label, Toast.LENGTH_SHORT).show();
                                performDelete();

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



    /**********************************************************/
    //      Utility Functions used in handling events         //
    /**********************************************************/

    //called from onClick(), executed when a Schedule is selected
    private void onSelect(View linearLayout, int position){
        //todo need to update selection visually

        Toast.makeText(getActivity(),
                "Position " + String.valueOf(position) + " updated in DB!",
                Toast.LENGTH_SHORT).show();

        //Need the schedule being updated, so ask the Adapter
        MMSchedMedCursorAdapter adapter = getAdapter(getView());
        MMScheduleMedication schedule = adapter.getScheduleAt(position);

        //create a dialogue to allow the user to:
        // change the Schedules on this Medication
        //The schedule is actually changed in the picker handler

        int hours = schedule.getTimeDue()/60;
        int minutes = schedule.getTimeDue() - (hours * 60);
        showPicker(schedule.getSchedMedID(), hours, minutes, MMUtilities.is24Format() );

        //The schedule itself is actually updated in the TimePicker callback


    }



}
