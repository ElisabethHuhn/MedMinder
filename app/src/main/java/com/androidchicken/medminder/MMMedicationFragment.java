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
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import java.util.ArrayList;

import static com.androidchicken.medminder.R.id.medicationDoseAmountInput;
import static com.androidchicken.medminder.R.id.medicationDoseNumInput;
import static com.androidchicken.medminder.R.id.medicationNotesInput;
import static com.androidchicken.medminder.R.id.medicationSideEffectsInput;


/**
 * The main UI screen for maintaining (CRUD) a Medication
 */
public class MMMedicationFragment extends Fragment implements AdapterView.OnItemSelectedListener {



    public static final String AS_NEEDED_STRATEGY = "Take as needed";
    public static final String SCHEDULE_STRATEGY  = "Schedule Medication Doses";

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

    private int      mPosition;
    private String   mReturnTag;

    //**********************************************************/
    //*****  Strategy types for Spinner Widgets     **********/
    //**********************************************************/
    private static String[] mStrategyTypes  = new String[]{AS_NEEDED_STRATEGY, SCHEDULE_STRATEGY};

    private int      mSelectedStrategyTypePosition = MMMedication.sSET_SCHEDULE_FOR_MEDICATION;
    private int      mOldPosition = MMMedication.sSET_SCHEDULE_FOR_MEDICATION;




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
    public static MMMedicationFragment newInstance(int position, String returnTag){
        //create a bundle to hold the arguments
        Bundle args = new Bundle();

        //It will be some work to make all of the data model serializable
        //so for now, just pass the person values
        args.putInt   (MMPerson.sPersonMedicationPositionTag, position);
        args.putString(MMMainActivity.sFragmentTag, returnTag);

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
            mPosition = args.getInt(MMPerson.sPersonMedicationPositionTag);
            mReturnTag = args.getString(MMMainActivity.sFragmentTag);
        } else {
            //use the flag for a new medication being created
            mPosition = (int)MMUtilities.ID_DOES_NOT_EXIST;
            //by default, return to the Home screen
            mReturnTag = MMMainActivity.sHomeTag;
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if (savedInstanceState != null){
            //the fragment is being restored so restore the person ID
            mPosition = savedInstanceState.getInt(MMPerson.sPersonMedicationPositionTag);
            mReturnTag = savedInstanceState.getString(MMMainActivity.sFragmentTag);
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
        MMUtilities utilities = MMUtilities.getInstance();
        utilities.hideSoftKeyboard(getActivity());
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

        ((MMMainActivity) getActivity()).handleFabVisibility();


        return v;
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState){
        // Save custom values into the bundle
        savedInstanceState.putInt(MMPerson.sPersonMedicationPositionTag, mPosition);
        savedInstanceState.putString(MMMainActivity.sFragmentTag, mReturnTag);

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
        ((MMMainActivity) getActivity()).handleFabVisibility();


        //hide the soft keyboard if it is visible
        MMUtilities utilities = MMUtilities.getInstance();
        utilities.hideSoftKeyboard(getActivity());
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        setUISaved();
    }


    //*************************************************************/
    /*  Convenience Methods for accessing things on the Activity  */
    //*************************************************************/

    private long     getPatientID(){
        return ((MMMainActivity)getActivity()).getPatientID();
    }

    private MMPerson getPerson(){
        return MMPersonManager.getInstance().getPerson(getPatientID());
    }


    //********************************************/
    //*******   Initialization Methods  **********/
    //********************************************/
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
        medNickNameInput.addTextChangedListener(textWatcher);

        EditText medBrandNameInput = (EditText) v.findViewById(R.id.medicationBrandNameInput);
        medBrandNameInput.addTextChangedListener(textWatcher);

        EditText medGenericNameInput = (EditText) v.findViewById(R.id.medicationGenericNameInput);
        medGenericNameInput.addTextChangedListener(textWatcher);

        EditText medNotesInput = (EditText) v.findViewById(R.id.medicationNotesInput);
        medNotesInput.addTextChangedListener(textWatcher);

        EditText medSideEffectsInput = (EditText) v.findViewById(R.id.medicationSideEffectsInput);
        medSideEffectsInput.addTextChangedListener(textWatcher);




        EditText medDoseAmountInput = (EditText) v.findViewById(medicationDoseAmountInput);
        medDoseAmountInput.addTextChangedListener(textWatcher);

        EditText medDoseUnitsInput = (EditText) v.findViewById(R.id.medicationDoseUnitsInput);
        medDoseUnitsInput.addTextChangedListener(textWatcher);

        Button upDoseNumber = (Button) v.findViewById(R.id.medicationUpButton);
        upDoseNumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onUpButton();
            }
        });

        Button downDoseNumber = (Button) v.findViewById(R.id.medicationDownButton);
        downDoseNumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onDownButton();
            }
        });
    }

    private void wireStrategySpinner(View v){
        //set the default
        mSelectedStrategyTypePosition = MMMedication.sSET_SCHEDULE_FOR_MEDICATION;
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
        MMMedication medication = getMedicationInstance(getPatientID(), mPosition);
        if (medication == null)return;

        Cursor scheduleCursor = medication.getSchedulesCursor();

        //5) Use the data to Create and set out SchedMed Adapter
        MMScheduleCursorAdapter adapter= new MMScheduleCursorAdapter((MMMainActivity)getActivity(),
                                                                    scheduleCursor,
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
        if (getPatientID() == MMUtilities.ID_DOES_NOT_EXIST){
            MMUtilities.getInstance().errorHandler(getActivity(), R.string.no_person_med);
            return;
            //throw new RuntimeException(getString(R.string.no_person_med));
        }

        CharSequence nickname;
        if (getPerson() == null) {
            nickname = getString(R.string.no_person_med);
        } else {
            nickname = getPerson().getNickname().toString().trim();
        }
        TextView medicationForPerson = (TextView) v.findViewById(R.id.medicationForPersonNickName);
        medicationForPerson.       setText(nickname);
        //mMedNickNameLastInput = nickname;

        MMMedication medication = getMedicationInstance(getPatientID(), mPosition);


        EditText medBrandNameInput   = (EditText) v.findViewById(R.id.medicationBrandNameInput);
        EditText medGenericNameInput = (EditText) v.findViewById(R.id.medicationGenericNameInput);
        EditText medNickNameInput    = (EditText) v.findViewById(R.id.medicationNickNameInput);
        EditText medNotesInput       = (EditText) v.findViewById(R.id.medicationNotesInput) ;
        EditText medSideEffectsInput = (EditText) v.findViewById(R.id.medicationSideEffectsInput);
        TextView medDoseAmountInput  = (TextView) v.findViewById(medicationDoseAmountInput);
        EditText medDoseUnitsInput   = (EditText) v.findViewById(R.id.medicationDoseUnitsInput);
        TextView medDoseNumInput     = (TextView) v.findViewById(medicationDoseNumInput);



        if (medication == null) {

            //For now, remove some of the defaults

            //CharSequence brand = MMMedication.getDefaultBrandName().toString().trim();
            //medBrandNameInput.setText(brand);

            //CharSequence generic = MMMedication.getDefaultGenericName().toString().trim();
            // medGenericNameInput.setText(generic);

            //CharSequence medNick = MMMedication.getDefaultMedicationNickname().toString().trim();
            //medNickNameInput.setText(medNick);

            mSelectedStrategyTypePosition = MMMedication.getDefaultDoseStrategy();



            CharSequence amt = String.valueOf(MMMedication.getDefaultDoseAmount());
            medDoseAmountInput.setText(amt);


            CharSequence units = MMMedication.getDefaultDoseUnits().toString().trim();
            medDoseUnitsInput.setText(units);


            CharSequence numPerDay = String.valueOf(MMMedication.getDefaultDoseNumPerDay());
            medDoseNumInput.setText(numPerDay);


        } else {

            CharSequence brand = medication.getBrandName().toString().trim();
            medBrandNameInput  .setText(brand);


            CharSequence generic = medication.getGenericName().toString().trim();
            medGenericNameInput.setText(generic);


            CharSequence medNick = medication.getMedicationNickname().toString().trim();
            medNickNameInput   .setText(medNick);

            CharSequence medNotes = medication.getNotes().toString().trim();
            medNotesInput      .setText(medNotes);

            CharSequence medSideEffects = medication.getSideEffects().toString().trim();
            medSideEffectsInput .setText(medSideEffects);


            mSelectedStrategyTypePosition = medication.getDoseStrategy();


            CharSequence amt = String.valueOf(medication.getDoseAmount());
            medDoseAmountInput.setText(amt);


            CharSequence units = medication.getDoseUnits().toString().trim();
            medDoseUnitsInput  .setText(units);


            CharSequence numPerDay = String.valueOf(medication.getDoseNumPerDay());
            medDoseNumInput.setText(numPerDay);

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

        Button medicationSaveButton = (Button) v.findViewById(R.id.medicationSaveButton);

        MMUtilities utilities = MMUtilities.getInstance();
        utilities.enableButton(getActivity(), medicationSaveButton, isEnabled);
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
            //upDoseNumber.setEnabled(false);
            upDoseNumber.setBackgroundColor(
                    ContextCompat.getColor(getActivity(),R.color.colorButton2Background));

            //downDoseNumber.setEnabled(false);
            downDoseNumber.setBackgroundColor(
                    ContextCompat.getColor(getActivity(),R.color.colorButton2Background));
        }
    }
    private boolean isUpDownEnabled(){

        return !isUIChanged;
    }


    //***********************************************************/
    //*********  RecyclerView / Adapter related Methods  ********/
    //***********************************************************/

    private void reinitializeCursor(long medicationID){
        if (medicationID == MMUtilities.ID_DOES_NOT_EXIST) return;

        //reset the list
        MMScheduleCursorAdapter adapter = getAdapter(getView());

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

    private MMScheduleCursorAdapter getAdapter(View v){
        return (MMScheduleCursorAdapter)  getRecyclerView(v).getAdapter();
    }


    //********************************************/
    //*********     Schedule Methods    **********/
    //********************************************/

    private TimePickerDialog showPicker(final long schedMedID,
                                        int hour,
                                        int minute,
                                        boolean is24Format){

        //This first chunk is defining the listener that will be used by the dialog:
        //
        // Define The Listeners First
        //
        //The timeSetListener is invoked when the OK button is pressed
        TimePickerDialog.OnTimeSetListener
                timeSetListener = new TimePickerDialog.OnTimeSetListener() {

            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minuteOfHour) {
                //this routine executes when OK is pressed
                userPressedOKinTimePicker = true;
                ArrayList<MMSchedule> schedules =
                        getSchedules(getPatientID(), mPosition);
                if (schedules == null)return;

                //look for the particular schedule being updated
                int last = schedules.size();
                int position = 0;
                MMSchedule scheduleMedication;
                while (position < last){
                    scheduleMedication = schedules.get(position);
                    if (schedMedID == scheduleMedication.getSchedMedID()){
                        //timeOfDay is number of MINUTES since midnight GMT
                        //get the new time from the time picker
                        //get minutes since midnight
                        int timeOfDay = (hourOfDay * 60) + minuteOfHour;


                        //update the schedule with the new time to take the med, and
                        scheduleMedication.setTimeDue(timeOfDay);
                        //update the DB row with this schedule
                        MMScheduleManager schedMedManager = MMScheduleManager.getInstance();
                        schedMedManager.addScheduleMedication(scheduleMedication);

                        //issue an alarm for this new time
                        MMUtilities utilities = MMUtilities.getInstance();
                        utilities.createScheduleNotification(getActivity(), timeOfDay);

                        //Now update the UI list
                        reinitializeCursor(scheduleMedication.getOfMedicationID());
                        return;
                    }
                    position++;
                }
            }
        };

        //The dismiss listener is invoked on both OK and on Cancel
        TimePickerDialog.OnDismissListener dismissListener = new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                //This listener fires regardless of whether the user picks OK, Cancel, or Back
                //need to be able to differentiate between the three cases.
                if (!userPressedOKinTimePicker) {
                    //need to set an Alarm for the old time as the user pressed cancel
                    /*
                    MMUtilities.getInstance()
                            .createScheduleNotification(getActivity(), minutesSinceMidnight);
                            */
                }
            }
        };


        //
        //Then the code for bringing up the picker
        //

        //in the onDismissListener() need to know if user pressed OK or Cancel
        userPressedOKinTimePicker = false;

        //cancel the alarm for the original time
        //this needs to be final so it can be accessed within the dialog
        final int minutesSinceMidnight = (hour * 60) + minute;
        cancelOneAlarm(minutesSinceMidnight);

        //The listener defined above is used here and called when the user presses OK
        //hour and minute are the initial values of the picker
        final TimePickerDialog timePickerDialog = new TimePickerDialog( getActivity(),
                                                                        timeSetListener,
                                                                        hour,
                                                                        minute,
                                                                        is24Format);

        //This listener is invoked on both OK and cancel
        timePickerDialog.setOnDismissListener(dismissListener);
        timePickerDialog.setMessage(getString(R.string.medication_sched_time_title));
        timePickerDialog.show();
        return timePickerDialog;
    }


    //*********************************************************/
    //      Utility Functions using passed arguments          //
    //*********************************************************/
    public MMMedication getMedicationInstance(){
        return getMedicationInstance(getPatientID(), mPosition);
    }

    private MMMedication getMedicationInstance(long personID, int position){
        //If personID can't be found in the list, person will be null
        MMPersonManager personManager = MMPersonManager.getInstance();
        MMPerson person = personManager.getPerson(personID);
        if (person == null)return null;
        if (position < 0)return null; //means we are adding the medication

        ArrayList<MMMedication> medications = person.getMedications();
        if (medications == null){
            medications = new ArrayList<>();
        }
        MMMedication medication;

        //flag assumes the medication is already there, but if not......
        if ((position == (int)MMUtilities.ID_DOES_NOT_EXIST) ||
            (medications.size() < mPosition)) {
            //return an error
           return null;
        } else {
            medication = person.getMedications().get(mPosition);
        }

        return medication;
    }

    private ArrayList<MMSchedule> getSchedules(long personID, int position){
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

        if (mOldPosition != mSelectedStrategyTypePosition) {

            setUIChanged();

            //tell the adapter about the new strategy
            View medFragment = getView();
            if (medFragment == null)return;

            RecyclerView recyclerView = getRecyclerView(medFragment);
            if (recyclerView == null)return;

            MMScheduleCursorAdapter adapter = (MMScheduleCursorAdapter)recyclerView.getAdapter();
            if (adapter != null) {
                adapter.resetStrategy(mSelectedStrategyTypePosition);
            }
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

        MMUtilities.getInstance().showStatus(getActivity(), R.string.save_label);

        //get rid of the soft keyboard if it is visible
        MMUtilities utilities = MMUtilities.getInstance();
        utilities.hideSoftKeyboard(getActivity());

        //Person medication is the medication in the Person's medication list
        //update it with values from this screen
        MMMedication medication = getMedicationInstance(getPatientID(), mPosition);

        if (medication == null) {
            //we are creating this medication for the first time
            medication = new MMMedication(MMUtilities.ID_DOES_NOT_EXIST);
            medication.setForPersonID(getPatientID());
        }

        //get handles for the UI widgets
        EditText medNickNameInput    = (EditText) v.findViewById(R.id.medicationNickNameInput);
        EditText medBrandNameInput   = (EditText) v.findViewById(R.id.medicationBrandNameInput);
        EditText medGenericNameInput = (EditText) v.findViewById(R.id.medicationGenericNameInput);
        TextView medDoseNumInput     = (TextView) v.findViewById(medicationDoseNumInput);
        EditText medDoseUnitsInput   = (EditText) v.findViewById(R.id.medicationDoseUnitsInput);
        EditText medDoseAmountInput  = (EditText) v.findViewById(medicationDoseAmountInput);
        EditText medNotesInput       = (EditText) v.findViewById(medicationNotesInput) ;
        EditText medSideEffectsInput = (EditText) v.findViewById(medicationSideEffectsInput);

        //Set the medication as belonging to the person
        medication.setForPersonID       (getPatientID());
        medication.setMedicationNickname(medNickNameInput.   getText().toString().trim());
        medication.setBrandName         (medBrandNameInput.  getText().toString().trim());
        medication.setGenericName       (medGenericNameInput.getText().toString().trim());
        medication.setNotes             (medNotesInput      .getText().toString().trim());
        medication.setSideEffects       (medSideEffectsInput.getText().toString().trim());

        medication.setDoseStrategy      (mSelectedStrategyTypePosition);

        medication.setDoseUnits         (medDoseUnitsInput.  getText().toString().trim());
        medication.setDoseAmount (Integer.valueOf(medDoseAmountInput.getText().toString().trim()));

        medication.setDoseNumPerDay(Integer.valueOf(medDoseNumInput. getText().toString().trim()));

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

        //Add the medication to the person, and to the DB
        boolean addToDBToo = true;
        if (getPerson() == null) {
            MMUtilities.getInstance().errorHandler(getActivity(), R.string.exception_medication_not_added);
        } else {
            if (!medicationManager.addToPerson(getPerson(), medication, addToDBToo)) {
                MMUtilities.getInstance().errorHandler(getActivity(), R.string.exception_medication_not_added);
            } else {

                //Tell the user everything went well
                MMUtilities.getInstance().showStatus(getActivity(), R.string.save_successful);

                //disable the save button because we just saved
                saveButtonEnable(MMUtilities.BUTTON_DISABLE);


                //update position with this medications position
                ArrayList<MMMedication> medications = getPerson().getMedications();
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

    public void onExit(){
        MMUtilities.getInstance().showStatus(getActivity(), R.string.exit_label);

        if (isUIChanged) {
            areYouSureExit();
        } else {
            switchToExit();
        }
    }

    public void onUpButton(){
        if (!isUpDownEnabled()){
            MMUtilities.getInstance().
                                showStatus(getActivity(), R.string.medication_button_after_save);
            return;
        }
        View v = getView();
        if (v == null)return;

        //increment the value on the UI
        TextView medDoseNumInput = (TextView) v.findViewById(medicationDoseNumInput);
        int size = Integer.valueOf(medDoseNumInput.getText().toString());
        size++;

        //Create a new schedule with default time taken from the preferences
        MMMedication medication = getMedicationInstance(getPatientID(), mPosition);
        if (medication != null) {
            medDoseNumInput.setText(String.valueOf(size));
            long medicationID = medication.getMedicationID();

            //timeDue will be in minutes since midnight
            long timeDue = MMSettings.getInstance().getDefaultTimeDue((MMMainActivity)getActivity());

            int strategy = medication.getDoseStrategy();

            MMSchedule schedule = new MMSchedule(medicationID,
                                                 getPatientID(),
                                                (int)timeDue,
                                                 strategy);
            medication.addSchedule(schedule);

            //shouldn't have to reinitialize cursor because onSave() will do it for us
            onSave();

            //Enable the Alarm receiver. It will stay enabled across reboots
            MMUtilities utilities = MMUtilities.getInstance();
            utilities.enableAlarmReceiver(getActivity());

            //create an Alarm to generate a notification for this scheduled dose
            utilities.createScheduleNotification(getActivity(), schedule.getTimeDue());
        }
    }

    public void onDownButton(){
        if (!isUpDownEnabled()){
            MMUtilities.getInstance().
                    showStatus(getActivity(), R.string.medication_button_after_save);
            return;
        }

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
        MMSchedule latestTimeSchedule = null;
        MMSchedule schedule;

        MMMedication medication = getMedicationInstance(getPatientID(), mPosition);
        if (medication == null)return;
        ArrayList<MMSchedule> schedules = medication.getSchedules();

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
            MMScheduleManager schedMedManager = MMScheduleManager.getInstance();
            schedMedManager.removeSchedMedFromDB(latestTimeScheduleID);

            //let the user know the number of doses has decreased
            medDoseNumInput.setText(String.valueOf(last-1));

        }
        onSave();
    }

    //called from onClick(), executed when a Schedule is selected
    private void onSelect(int position){

        //  if the strategy is asNeeded, do not respond to selection
        if (mSelectedStrategyTypePosition == MMMedication.sAS_NEEDED){
            return;
        }

        //Need the schedule being updated, so ask the Adapter
        MMScheduleCursorAdapter adapter = getAdapter(getView());
        adapter.notifyItemChanged(position);

        MMSchedule schedule = adapter.getScheduleAt(position);

        //create a dialogue to allow the user to:
        // change the Schedules on this Medication
        //The schedule is actually changed in the picker handler
        //and a new alarm is set there for the new time

        int timeDue = schedule.getTimeDue(); //minutes since midnight in GMT time zone



        int hours    = timeDue/(int)MMUtilities.minutesPerHour;
        int minutes  = timeDue - (hours * (int)MMUtilities.minutesPerHour);
        boolean is24format = MMSettings.getInstance().getClock24Format((MMMainActivity)getActivity());
        showPicker(schedule.getSchedMedID(), hours, minutes, is24format );

        //The schedule itself and its alarms are actually updated in the TimePicker callbacks
    }


    //***********************************/
    //****  Exit Button Dialogue    *****/
    //***********************************/
    //Build and display the alert dialog
    private void areYouSureExit(){
        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.abandon_title)
                .setIcon(R.drawable.ic_mortar_black_24dp)
                .setMessage(R.string.are_you_sure)
                .setPositiveButton(R.string.exit_label,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                //Leave even though project has chaged
                                //MMUtilities.getInstance().showStatus(getActivity(), R.string.exit_label);
                                switchToExit();

                            }
                        })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                        MMUtilities.getInstance().showStatus(getActivity(), R.string.pressed_cancel);

                    }
                })
                .setIcon(R.drawable.ic_mortar_black_24dp)
                .show();
    }

    private void switchToExit(){
        MMScheduleCursorAdapter adapter = getAdapter(getView());
        if (adapter != null) adapter.closeCursor();

        ((MMMainActivity) getActivity()).switchToMedicationReturn(mReturnTag);

    }




    //*********************************/
    //     Alarms and  Notifications  //
    //********************************/
    private void cancelOneAlarm(int minutesSinceMidnight){
        //If this is the only schedule due at this time
        // Remove any alarms for this schedule
        MMScheduleManager schedMedManager = MMScheduleManager.getInstance();
        int howManyMedsDue = schedMedManager.howManyDueAt(minutesSinceMidnight);

        //if there is only one medication dose due at this time, delete the alarm
        //if there are more than one due at this time, leave the existing alarm in place
        if (howManyMedsDue == 1) {
            //The alarm is based on when the dose is due
            MMUtilities utilities = MMUtilities.getInstance();
            utilities.cancelNotificationAlarms(getActivity());
        }
    }



}
