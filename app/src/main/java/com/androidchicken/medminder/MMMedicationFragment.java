package com.androidchicken.medminder;

import android.app.TimePickerDialog;
import android.content.Context;
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
import android.text.Editable;
import android.text.TextWatcher;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
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
    private Button   mMedicationExitButton;
    private Button   mMedicationSaveButton;
    private Button   mMedicationAddScheduleButton;

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
    private CharSequence mMedNickNameLastInput;
    private CharSequence mMedBrandNameLastInput;
    private CharSequence mMedGenericNameLastInput;
    private CharSequence mMedDoseAmountLastInput;
    private CharSequence mMedDoseUnitsLastInput;
    private CharSequence mMedDoseNumLastInput;
    private CharSequence mMedDoseStrategyLastInput;

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
        initializeUI();

        //hide the soft keyboard if it is visible
        MMUtilities.hideSoftKeyboard(getActivity());

        //set the title bar subtitle
        ((MainActivity) getActivity()).setMMSubtitle(R.string.title_medication);

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



    /*********************************************/
    /********   Initialization Methods  **********/
    /*********************************************/


    private void wireWidgets(View v){



        mMedicationExitButton = (Button) v.findViewById(R.id.medicationExitButton);
        mMedicationExitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(),
                        R.string.exit_label,
                        Toast.LENGTH_SHORT).show();

                if (isInputChanged) {
                    areYouSureExit();
                } else {
                    switchToExit();
                }
            }

        });


        mMedicationSaveButton = (Button) v.findViewById(R.id.medicationSaveButton);
        mMedicationSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(),
                        R.string.save_label,
                        Toast.LENGTH_SHORT).show();
                //Save the Medication to the Medication Manager
                onSave();

            }

        });


        mMedicationAddScheduleButton = (Button) v.findViewById(R.id.medicationAddScheduleButton);
        mMedicationAddScheduleButton.setEnabled(false);
        MMMedication medication = getMedicationInstance(mPersonID, mPosition);
        if (medication != null){
            //have the button say the correct thing
            if (medication.isSchedulesChanged()){
                mMedicationAddScheduleButton.setText(R.string.medication_update_schedule);
            }
        } else {
            //don't enable button until the medication is actually created
            MMUtilities.enableButton(getActivity(),
                                     mMedicationAddScheduleButton,
                                     MMUtilities.BUTTON_DISABLE);

            Toast.makeText(getActivity(), R.string.medication_save_first, Toast.LENGTH_SHORT).show();
        }
        mMedicationAddScheduleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Toast.makeText(getActivity(),
                        R.string.medication_maintain_schedule,
                        Toast.LENGTH_SHORT).show();
                //Save the Medication to the Medication Manager
                addSchedule();
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


/*
        mMedicationDoseStrategyInput = (EditText) v.findViewById(R.id.medicationDoseStrategyInput);
        mMedicationDoseStrategyInput.addTextChangedListener(new TextWatcher() {
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

        mMedicationDoseStrategyInput.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (hasFocus) {
                    mMedDoseStrategyLastInput = mMedicationDoseStrategyInput.getText();
                } else {
                    if (!mMedDoseStrategyLastInput.equals(mMedicationDoseStrategyInput.getText())) {
                        isInputChanged = true;

                        //if we must now set schedule
                        int strategy = Integer.valueOf(
                                mMedicationDoseStrategyInput.getText().toString().trim());

                        if (strategy == SET_SCHEDULE_FOR_MEDICATION) {
                            MMMedication medication = getMedicationInstance(mPersonID, mPosition);
                            if (medication != null) {
                                MMUtilities.enableButton(getActivity(),
                                        mMedicationAddScheduleButton,
                                        BUTTON_ENABLE);
                                // TODO: 3/11/2017 maybe add schedules here
                            }
                        }
                    }
                }
            }
        });
*/

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
                if (!upDownEnabled())return;

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
                if (!upDownEnabled())return;

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
                MMScheduleMedication schedule = null;

                MMMedication medication = getMedicationInstance(mPersonID, mPosition);
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
                if (latestTimeSchedule == null){
                    // TODO: 3/13/2017 report error somehow
                } else {
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
        RecyclerView recyclerView = (RecyclerView) v.findViewById(R.id.scheduleList);

        //3) create and assign a layout manager to the recycler view
        //RecyclerView.LayoutManager mLayoutManager  = new LinearLayoutManager(getActivity());
        RecyclerView.LayoutManager mLayoutManager  = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(mLayoutManager);

        //4) Get the Cursor of ScheduleMedication DB rows from the DB.
        // The medication knows how to do this
        MMMedication medication = getMedicationInstance(mPersonID, mPosition);
        if (medication == null)return;

        //The next two lines are for debug. If you see them, remove them
        ArrayList<MMScheduleMedication> schedules = medication.getSchedules();
        int size = schedules.size();

        Cursor scheduleCursor = medication.getSchedulesCursor();

        //debug only, remove size line if you see it
        size = scheduleCursor.getCount();



        //5) Use the data to Create and set out SchedMed Adapter
        MMSchedMedCursorAdapter adapter = new MMSchedMedCursorAdapter(scheduleCursor);
        adapter.setAdapterContext(medication.getMedicationID());
        recyclerView.setAdapter(adapter);

        size = adapter.getItemCount();
        //initialize the UI for number per day equal to the number of existing schedules
        mMedicationDoseNumInput.setText(String.valueOf(size));

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

    private void initializeUI(){
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
        mMedNickNameLastInput = nickname;

        mMedicationForInput.        setText(Long.valueOf(mPersonID).toString().trim());

        MMMedication medication = getMedicationInstance(mPersonID, mPosition);

        CharSequence selectedStrategyType;

        if (medication == null) {
            mMedicationIDInput         .setText(String.valueOf(MMUtilities.ID_DOES_NOT_EXIST));

            CharSequence brand = MMMedication.getDefaultBrandName().toString().trim();
            mMedicationBrandNameInput.setText(brand);
            mMedBrandNameLastInput = brand;

            CharSequence generic = MMMedication.getDefaultGenericName().toString().trim();
            mMedicationGenericNameInput.setText(generic);
            mMedGenericNameLastInput = generic;

            CharSequence medNick = MMMedication.getDefaultMedicationNickname().toString().trim();
            mMedicationNickNameInput.setText(medNick);
            mMedNickNameLastInput = medNick;

            CharSequence strategy = String.valueOf(MMMedication.getDefaultDoseStrategy()).toString().trim();
            //mMedicationDoseStrategyInput.setText(strategy);
            mMedDoseStrategyLastInput = strategy;
            selectedStrategyType = strategy.toString();

            CharSequence amt = String.valueOf(MMMedication.getDefaultDoseAmount()).toString().trim();
            mMedicationDoseAmountInput.setText(amt);
            mMedDoseAmountLastInput = amt;

            CharSequence units = MMMedication.getDefaultDoseUnits().toString().trim();
            mMedicationDoseUnitsInput.setText(units);
            mMedDoseUnitsLastInput = units;

            CharSequence numPerDay = String.valueOf(MMMedication.getDefaultDoseNumPerDay()).toString().trim();
            mMedicationDoseNumInput.setText(numPerDay);
            mMedDoseNumLastInput = numPerDay;

        } else {
            mMedicationIDInput         .setText(String.valueOf( medication.getMedicationID()));

            CharSequence brand = medication.getBrandName().toString().trim();
            mMedicationBrandNameInput  .setText(brand);
            mMedBrandNameLastInput = brand;

            CharSequence generic = medication.getGenericName().toString().trim();
            mMedicationGenericNameInput.setText(generic);
            mMedGenericNameLastInput = generic;

            CharSequence medNick = medication.getMedicationNickname().toString().trim();
            mMedicationNickNameInput   .setText(medNick);
            mMedNickNameLastInput = medNick;

            CharSequence strategy = String.valueOf(medication.getDoseStrategy()).toString().trim();
            //mMedicationDoseStrategyInput.setText(strategy);
            mMedDoseStrategyLastInput = strategy;
            selectedStrategyType = strategy.toString();

            CharSequence amt = String.valueOf(medication.getDoseAmount()).toString().trim();
            mMedicationDoseAmountInput.setText(amt);
            mMedDoseAmountLastInput = amt;

            CharSequence units = medication.getDoseUnits().toString().trim();
            mMedicationDoseUnitsInput  .setText(units);
            mMedDoseUnitsLastInput = units;

            CharSequence numPerDay = String.valueOf(medication.getDoseNumPerDay()).toString().trim();
            mMedicationDoseNumInput.setText(numPerDay);
            mMedDoseNumLastInput = numPerDay;
        }


        if (selectedStrategyType.equals(SCHEDULE_STRATEGY)) {
            mSelectedStrategyTypePosition = SET_SCHEDULE_FOR_MEDICATION;
        } else if (selectedStrategyType == AS_NEEDED_STRATEGY) {
            mSelectedStrategyTypePosition = AS_NEEDED;
        }
        Spinner spinner = (Spinner) getView().findViewById(R.id.strategy_type_spinner);
        spinner.setSelection(mSelectedStrategyTypePosition);


        isInputChanged = false; //initialize to fields not yet changed

        //set the up/down buttons properly
        setUpDownEnabled();

    }


    private void setUIChanged(){
        isInputChanged = true;
        setUpDownEnabled();
    }

    private void setUpDownEnabled(){
        if (upDownEnabled()){
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

    private boolean upDownEnabled(){
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



    /*********************************************/
    /**********     Cursor Methods      **********/
    /*********************************************/

    private void reinitializeCursor(long medicationID){
        if (medicationID == MMUtilities.ID_DOES_NOT_EXIST) return;

        //reset the list
        RecyclerView recyclerView =
                (RecyclerView) getView().findViewById(R.id.scheduleList);

        MMSchedMedCursorAdapter adapter =
                (MMSchedMedCursorAdapter) recyclerView.getAdapter();

        if (adapter != null) {
            adapter.reinitializeCursor(medicationID);
        } else {
            //we did not have a medication earlier so it never got initialized
            initializeRecyclerView(getView());
        }
    }

    private Cursor getCursor(){
        RecyclerView recyclerView = (RecyclerView) getView().findViewById(R.id.scheduleList);
        MMSchedMedCursorAdapter scheduleAdapter =
                (MMSchedMedCursorAdapter) recyclerView.getAdapter();

        return scheduleAdapter.getSchedMedCursor();
    }



    /*********************************************/
    /**********     Schedule Methods    **********/
    /*********************************************/

    private void addSchedule(){
        //Does a schedule already exist?
        MMMedication medication = getMedicationInstance(mPersonID, mPosition);
        if (medication == null)return;
        addSchedule(medication);

    }

    private void addSchedule(MMMedication medication){

        if (medication.getDoseStrategy() != SET_SCHEDULE_FOR_MEDICATION){
            MMUtilities.errorHandler(getActivity(), R.string.medication_not_scheduled);
            return;
        }

        int numPerDay = Integer.valueOf(mMedicationDoseNumInput.getText().toString());

        ArrayList<MMScheduleMedication> schedules = medication.getSchedules();
        if (schedules == null) {
            //need to create schedule from scratch
            //Creates a default copy and stores it in the DB
            schedules = createSchedules(mPersonID, medication.getMedicationID());
            medication.setSchedules(schedules);
        } else  if  (schedules.size() != numPerDay){
            // TODO: 3/6/2017 delete the existing schedules and start over
            //need to create schedule from scratch
            //Creates a local copy, not yet in the DB
            schedules = createSchedules(mPersonID, medication.getMedicationID());
            medication.setSchedules(schedules);
        }


        int position = numPerDay - 1;
        int last = 0-1;
        MMScheduleMedication scheduleMedication;
        int hours;
        int minutes;
        long schedMedID;

        boolean is24Format = false;
        while (position > last){
            scheduleMedication = schedules.get(position);
            hours = scheduleMedication.getTimeDue() / 60;
            minutes = (scheduleMedication.getTimeDue()) - (hours * 60);
            schedMedID = scheduleMedication.getSchedMedID();

            showPicker(schedMedID, hours, minutes, is24Format);
            position--;
        }

    }

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
                                int timeOfDay = (hourOfDay * 60) + minute;
                                //update the time to take the med, and
                                scheduleMedication.setTimeDue(timeOfDay);
                                //update the DB row with this schedule
                                MMSchedMedManager schedMedManager = MMSchedMedManager.getInstance();
                                schedMedManager.addScheduleMedication(scheduleMedication);
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

    //creates the proper number of schedule objects and assigns each a default time
    private ArrayList<MMScheduleMedication> createSchedules(long personID, long medicationID){

        int numPerDay = Integer.valueOf(mMedicationDoseNumInput.getText().toString().trim());
        ArrayList<MMScheduleMedication> schedules = new ArrayList<>();
        if (numPerDay == 0)return schedules;

        //divide the doses up throughout the day, starting at 6am
        int minutesBetween = (24/numPerDay) * 60;
        int minutesDue = 6*60;//first dose due at 6am

        //count backwards because it is easier for the user to start with the morning dose
        int position = numPerDay-1;//subtract one because of index range from 0
        int last = 0-1;
        MMScheduleMedication scheduleMed;
        MMSchedMedManager schedMedManager = MMSchedMedManager.getInstance();

        while (position > last){
            scheduleMed = new MMScheduleMedication( medicationID, personID, minutesDue);

            schedules.add(scheduleMed);
            //storing the object in the DB assigns the ID
            schedMedManager.addScheduleMedication(scheduleMed);

            position--;
            minutesDue = minutesDue + minutesBetween;
        }
        return schedules;
    }


    /**********************************************************/
    //      Utility Functions using passed arguments          //
    /**********************************************************/
    private ArrayList<MMMedication> getMedications(long personID){
        //If personID can't be found in the list, person will be null
        MMPerson person = MMUtilities.getPerson(mPersonID);
        if (person == null)return null;

        return person.getMedications();
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

    private boolean isInMedications(MMMedication medication){
        ArrayList<MMMedication> medications = getMedications(mPersonID);
        return medications.contains(medication);
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
        mSelectedStrategyTypePosition = position;
       //mSelectedStrategyType = (String) parent.getItemAtPosition(position);

        setUIChanged();

    }

    public void onNothingSelected(AdapterView<?> parent) {
        //for now, do nothing
    }


    /****************************************/
    /*****  Listeners for List Views    *****/
    /****************************************/

    public void onHoursClick(View hoursView){
        //String hours = (EditText) hoursView.getText();
        int temp = 0;

    }


    /*********************************************/
    /**********    Event Handlers       **********/
    /*********************************************/
    private void onSave(){
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
                //enable the button to add schedules to this medication
                if (medication.getDoseStrategy() == SET_SCHEDULE_FOR_MEDICATION) {
                    MMUtilities.enableButton(getActivity(),
                            mMedicationAddScheduleButton,
                            MMUtilities.BUTTON_ENABLE);
                }
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

                //set the up/down buttons properly
                setUpDownEnabled();

                //reinitialize the schedule list
                reinitializeCursor(medicationID);

            }
        }

        isInputChanged = false;
        setUpDownEnabled();
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
        ((MainActivity) getActivity()).switchToPersonScreen(mPersonID);   //switchToPopBackstack();

    }



    /**********************************************************/
    //      Utility Functions used in handling events         //
    /**********************************************************/

    //called from onClick(), executed when a person is selected
    private void onSelect(View linearLayout, int position){
        //todo need to update selection visually
/*
        Cursor scheduleCursor = getCursor();

        MMSchedMedManager schedMedManager = MMSchedMedManager.getInstance();
        MMScheduleMedication schedule =
                schedMedManager.getScheduleMedicationFromCursor(scheduleCursor,position);

        //schedule now points at the ScheduleMedication object to be updated
        //now get the values from the screen

        EditText hourView    = (EditText) ((LinearLayout)linearLayout).getChildAt(0);
        EditText minuetsView = (EditText) ((LinearLayout)linearLayout).getChildAt(1);

        int hours   = Integer.valueOf(hourView   .getText().toString().trim());
        int minutes = Integer.valueOf(minuetsView.getText().toString().trim());

        int timeDue = (hours * 60) + minutes;

        schedule.setTimeDue(timeDue);

        //write out to the DB

        schedMedManager.addScheduleMedication(schedule);

        //update the cursor in the adapter
        reinitializeCursor(schedule.getOfMedicationID());

*/

        Toast.makeText(getActivity(),
                "Position " + String.valueOf(position) + " updated in DB!",
                Toast.LENGTH_SHORT).show();
    }


    //Add some code to improve the recycler view
    //Here is the interface for event handlers for Click and LongClick
    public interface ClickListener {
        void onClick(View view, int position);

        void onLongClick(View view, int position);
    }

    public static class RecyclerTouchListener implements RecyclerView.OnItemTouchListener {

        private GestureDetector gestureDetector;
        private MMHomeFragment.ClickListener clickListener;

        public RecyclerTouchListener(Context context,
                                     final RecyclerView recyclerView,
                                     final MMHomeFragment.ClickListener clickListener) {

            this.clickListener = clickListener;
            gestureDetector = new GestureDetector(context,
                    new GestureDetector.SimpleOnGestureListener() {

                        @Override
                        public boolean onSingleTapUp(MotionEvent e) {
                            return true;
                        }

                        @Override
                        public void onLongPress(MotionEvent e) {
                            View child = recyclerView.findChildViewUnder(e.getX(), e.getY());
                            if (child != null && clickListener != null) {
                                clickListener.onLongClick(child, recyclerView.getChildLayoutPosition(child));
                            }
                        }
                    });
        }

        @Override
        public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {

            View child = rv.findChildViewUnder(e.getX(), e.getY());
            if (child != null && clickListener != null && gestureDetector.onTouchEvent(e)) {
                clickListener.onClick(child, rv.getChildLayoutPosition(child));
            }
            return false;
        }

        @Override
        public void onTouchEvent(RecyclerView rv, MotionEvent e) {
        }

        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

        }
    }


}
