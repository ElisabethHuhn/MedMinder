package com.androidchicken.medminder;

import android.app.TimePickerDialog;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.ArrayList;


/**
 * The main UI screen for maintaining (CRUD) a Medication
 */
public class MainMMMedicationFragment extends Fragment {

    /***********************************************/
    /*        UI Widget Views                      */
    /***********************************************/
    private Button   mMedicationSaveButton;
    private Button   mMedicationAddScheduleButton;

    private TextView mMedicationForPerson;
    private EditText mMedicationBrandNameInput;
    private EditText mMedicationGenericNameInput;
    private EditText mMedicationNickNameInput;
    private EditText mMedicationForInput;
    private EditText mMedicationDoseAmountInput;
    private EditText mMedicationDoseUnitsInput;
    private EditText mMedicationDoseNumInput;
    private EditText mMedicationDoseStrategyInput;


    /***********************************************/
    /*        Passed Arguments to this Fragment    */
    /***********************************************/
    private int      mPersonID;
    private int      mPosition;


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
    public static MainMMMedicationFragment newInstance(int personID, int position){
        //create a bundle to hold the arguments
        Bundle args = new Bundle();

        //It will be some work to make all of the data model serializable
        //so for now, just pass the person values
        args.putInt         (MMPerson.sPersonIDTag,personID);
        args.putInt         (MMPerson.sPersonMedicationPositionTag, position);

        MainMMMedicationFragment fragment = new MainMMMedicationFragment();

        fragment.setArguments(args);
        
        return fragment;
    }

    /***********************************************/
    /*          Constructor                        */
    /***********************************************/
    public MainMMMedicationFragment() {
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
            mPersonID = args.getInt(MMPerson.sPersonIDTag);
            mPosition = args.getInt(MMPerson.sPersonMedicationPositionTag);

        }

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        //Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_medication, container, false);

        //Wire up the UI widgets so they can handle events later
        wireWidgets(v);
        initializeRecyclerView(v);
        initializeUI();

        //set the title bar subtitle
        ((MainActivity) getActivity()).setMMSubtitle(R.string.title_medication);

        return v;
    }

    private void wireWidgets(View v){

        //Patient Nick Name
        mMedicationForPerson = (TextView) v.findViewById(R.id.medicationForPerson);
        //There are no events associated with this field

        mMedicationSaveButton = (Button) v.findViewById(R.id.medicationSaveButton);
        mMedicationSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(),
                        R.string.save_label,
                        Toast.LENGTH_SHORT).show();
                //Save the Medication to the Medication Manager
                onSave();

                //switch to home screen
                // But the switching happens on the container Activity
                ((MainActivity) getActivity()).switchToHomeScreen(mPersonID);
            }

        });



        mMedicationAddScheduleButton = (Button) v.findViewById(R.id.medicationAddScheduleButton);
        MMMedication medication = getMedicationInstance(mPersonID, mPosition);
        if (medication != null){
            ArrayList<MMScheduleMedication> schedules = medication.getSchedules();
            int schedSize = 0;
            if (schedules != null) {
                schedSize = schedules.size();
            }
            if (schedSize > 0){
                mMedicationAddScheduleButton.setText(R.string.medication_update_schedule);
            }
        }
        mMedicationAddScheduleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPosition < 0 ){
                    Toast.makeText(getActivity(),
                            R.string.medication_save_first,
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                Toast.makeText(getActivity(),
                        R.string.medication_maintain_schedule,
                        Toast.LENGTH_SHORT).show();
                //Save the Medication to the Medication Manager
                addSchedule();
            }

        });


        mMedicationBrandNameInput = (EditText) v.findViewById(R.id.medicationBrandNameInput);
        mMedicationBrandNameInput.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                return false;
            }
        });


        mMedicationGenericNameInput = (EditText) v.findViewById(R.id.medicationGenericNameInput);
        mMedicationGenericNameInput.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                return false;
            }
        });


        mMedicationNickNameInput = (EditText) v.findViewById(R.id.medicationNickNameInput);
        mMedicationNickNameInput.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                return false;
            }
        });

        mMedicationForInput = (EditText) v.findViewById(R.id.medicationForPersonInput);
        mMedicationForInput.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return false;
            }
        });


        mMedicationDoseAmountInput = (EditText) v.findViewById(R.id.medicationDoseAmountInput);
        mMedicationDoseAmountInput.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                return false;
            }
        });


        mMedicationDoseUnitsInput = (EditText) v.findViewById(R.id.medicationDoseUnitsInput);
        mMedicationDoseUnitsInput.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                return false;
            }
        });


        mMedicationDoseNumInput = (EditText) v.findViewById(R.id.medicationDoseNumInput);
        mMedicationDoseNumInput.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {


                return false;
            }
        });


        mMedicationDoseStrategyInput = (EditText) v.findViewById(R.id.medicationDoseStrategyInput);
        mMedicationDoseStrategyInput.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {


                return false;
            }
        });

    }

    private void   initializeRecyclerView(View v){
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

        //4) Get the Cursor of ScheduleMedication DB rows from the ScheduleMedicationManager
        MMMedication medication = getMedicationInstance(mPersonID, mPosition);
        if (medication == null)return;

        MMSchedMedManager schedMedManager = MMSchedMedManager.getInstance();
        Cursor scheduleCursor = schedMedManager.getAllScheduleMedicationsCursor(medication.getMedicationID());

        //5) Use the data to Create and set out SchedMed Adapter
        MMSchedMedCursorAdapter adapter = new MMSchedMedCursorAdapter(scheduleCursor);
        adapter.setAdapterContext(medication.getMedicationID());
        recyclerView.setAdapter(adapter);

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
                        new MainMMHomeFragment.RecyclerTouchListener(getActivity(),
                        recyclerView,
                        new MainMMHomeFragment.ClickListener() {

                    @Override
                    public void onClick(View view, int position) {
                        onSelect(position);
                    }

                    @Override
                    public void onLongClick(View view, int position) {

                    }
                }));

    }

    private void initializeUI(){
        if (mPersonID == 0) throw new RuntimeException(getString(R.string.no_person));
        MMPerson person = MMUtilities.getPerson(mPersonID);

        CharSequence nickname;
        if (person == null) {
            nickname = getString(R.string.no_person);
        } else {
            nickname = person.getNickname().toString().trim();
        }
        mMedicationForPerson.       setText(nickname);
        mMedicationForInput.        setText(Integer.valueOf(mPersonID).toString().trim());

        MMMedication medication = getMedicationInstance(mPersonID, mPosition);

        if (medication == null) {
            mMedicationBrandNameInput.  setText(MMMedication.getDefaultBrandName().toString().trim());
            mMedicationGenericNameInput.setText(MMMedication.getDefaultGenericName().toString().trim());
            mMedicationNickNameInput.   setText(MMMedication.getDefaultMedicationNickname().toString().trim());

            mMedicationDoseStrategyInput.setText(Integer.valueOf(MMMedication.getDefaultDoseStrategy()).toString().trim());
            mMedicationDoseAmountInput. setText(Integer.valueOf(MMMedication.getDefaultDoseAmount()).toString().trim());
            mMedicationDoseUnitsInput.  setText(MMMedication.getDefaultDoseUnits().toString().trim());
            mMedicationDoseNumInput.    setText(Integer.valueOf(MMMedication.getDefaultDoseNumPerDay()).toString().trim());

        } else {
            mMedicationBrandNameInput.setText(medication.getBrandName().toString().trim());
            mMedicationGenericNameInput.setText(medication.getGenericName().toString().trim());
            mMedicationNickNameInput.setText(medication.getMedicationNickname().toString().trim());

            mMedicationDoseStrategyInput.setText(Integer.valueOf(medication.getDoseStrategy()).toString().trim());
            mMedicationDoseAmountInput.setText(Integer.valueOf(medication.getDoseAmount()).toString().trim());
            mMedicationDoseUnitsInput.setText(medication.getDoseUnits().toString().trim());
            mMedicationDoseNumInput.setText(Integer.valueOf(medication.getDoseNumPerDay()).toString().trim());
        }
    }

    private void onSave(){
        //Person medication is the medication in the Person's medication list
        //update it with values from this screen
        MMMedication medication = getMedicationInstance(mPersonID, mPosition);

        if (medication == null) {
            medication = new MMMedication();
            medication.setForPersonID(mPersonID);
        }
        //Set the medication as belonging to the person
        medication.setForPersonID(mPersonID);
        medication.setMedicationNickname(mMedicationNickNameInput.   getText().toString().trim());
        medication.setBrandName         (mMedicationBrandNameInput.  getText().toString().trim());
        medication.setGenericName       (mMedicationGenericNameInput.getText().toString().trim());
        medication.setDoseStrategy(Integer.valueOf(mMedicationDoseStrategyInput.getText().toString().trim()));
        medication.setDoseUnits         (mMedicationDoseUnitsInput.  getText().toString().trim());
        medication.setDoseAmount  (Integer.valueOf(mMedicationDoseAmountInput.getText().toString().trim()));
        medication.setDoseNumPerDay(Integer.valueOf(mMedicationDoseNumInput.   getText().toString().trim()));

        if (!isInMedications(medication)){
            //If this flag is false, then need to add the medication to the person
            //AND add it to the DB
            MMMedicationManager medicationManager = MMMedicationManager.getInstance();

            MMPerson person = MMUtilities.getPerson(mPersonID);
            //Add the medication to the person, and to the DB
            boolean addToDBToo = true;
            if ((person == null) ||
                (!medicationManager.addToPerson(person, medication, addToDBToo))) {
                MMUtilities.errorHandler(getActivity(), R.string.exception_medication_not_added);
            }

        }

        //add the scheduleMedications to the DB as well
        MMSchedMedManager schedMedManager = MMSchedMedManager.getInstance();
        ArrayList<MMScheduleMedication> schedules = medication.getSchedules();
        if (schedules != null) {
            MMScheduleMedication scheduleMedication;
            boolean returnCode;
            if ((schedules != null) && (schedules.size() > 0)) {
                int last = schedules.size();
                int position = 0;
                while (position < last) {
                    scheduleMedication = schedules.get(position);
                    returnCode = schedMedManager.addScheduleMedication(scheduleMedication);
                    if (!returnCode) {
                        returnCode = schedMedManager.updateScheduleMedication(scheduleMedication);
                        // TODO: 2/26/2017 what if db update fails totally???
                    }
                    position++;
                }
            }
        }

    }

    private void addSchedule(){
        //Does a schedule already exist?
        MMMedication medication = getMedicationInstance(mPersonID, mPosition);
        if (medication == null)return;

        int numPerDay = Integer.valueOf(mMedicationDoseNumInput.getText().toString());

        ArrayList<MMScheduleMedication> schedules = medication.getSchedules();
        if ((schedules == null) || (schedules.size() < numPerDay)){
            //need to create schedule from scratch
            schedules = createSchedules(mPersonID, medication.getMedicationID());
            medication.setSchedules(schedules);
        }

        int position = 0;
        MMScheduleMedication scheduleMedication;
        int hours;
        int minutes;
        int schedMedID;

        boolean is24Format = false;
        while (position < numPerDay){
            scheduleMedication = schedules.get(position);
            hours = scheduleMedication.getTimeDue() / 60;
            minutes = scheduleMedication.getTimeDue() - (hours * 60);
            schedMedID = scheduleMedication.getSchedMedID();

            showPicker(schedMedID, hours, minutes, is24Format);
            position++;
        }

    }

    private TimePickerDialog showPicker(final int schedMedID, int hour, int minute, boolean is24Format){
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                getActivity(),
                new TimePickerDialog.OnTimeSetListener() {

                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        ArrayList<MMScheduleMedication> schedules = getSchedules(mPersonID, mPosition);
                        if (schedules == null)return;

                        int last = schedules.size();
                        int position = 0;
                        MMScheduleMedication scheduleMedication;
                        while (position < last){
                            scheduleMedication = schedules.get(position);
                            if (schedMedID == scheduleMedication.getSchedMedID()){
                                int timeOfDay = (hourOfDay * 60) + minute;
                                scheduleMedication.setTimeDue(timeOfDay);
                                return;
                            }
                        }
                    }
                },
                hour,
                minute,
                is24Format);
        timePickerDialog.show();
        return timePickerDialog;
    }

    private ArrayList<MMScheduleMedication> createSchedules(int personID, int medicationID){

        int numPerDay = Integer.valueOf(mMedicationDoseNumInput.getText().toString().trim());
        ArrayList<MMScheduleMedication> schedules = new ArrayList<>();
        if (numPerDay == 0)return schedules;

        int hoursBetween = 24/numPerDay;
        int minutesBetween = hoursBetween*60;
        int minutesDue = 0;

        int position = 0;
        MMScheduleMedication scheduleMed;

        while (position < numPerDay){
            scheduleMed = new MMScheduleMedication( medicationID, personID, minutesDue);
            schedules.add(scheduleMed);
            position++;
            minutesDue = minutesDue + minutesBetween;
        }

        return schedules;

    }


    /**********************************************************/
    //      Utility Functions using passed arguments          //
    /**********************************************************/
    private ArrayList<MMMedication> getMedications(int personID){
        //If personID can't be found in the list, person will be null
        MMPerson person = MMUtilities.getPerson(mPersonID);
        if (person == null)return null;

        return person.getMedications();
    }

    private MMMedication getMedicationInstance(int personID, int position){
        //If personID can't be found in the list, person will be null
        MMPerson person = MMUtilities.getPerson(mPersonID);
        if (person == null)return null;
        if (position < 0)return null; //means we are adding the medication

        ArrayList<MMMedication> medications = person.getMedications();
        if (medications == null){
            medications = new ArrayList<>();
        }
        MMMedication medication;

        //flag assumes the medication is already there, but if not......
        if ((position == -1) || (medications.size() < mPosition)) {
            //just create a new medication and add it
            medication = new MMMedication();
        } else {
            medication = person.getMedications().get(mPosition);
        }

        return medication;
    }

    private boolean isInMedications(MMMedication medication){
        ArrayList<MMMedication> medications = getMedications(mPersonID);
        return medications.contains(medication);
    }

    private ArrayList<MMScheduleMedication> getSchedules(int personID, int position){
        MMMedication medication = getMedicationInstance(personID, position);
        return medication.getSchedules();
    }


    /**********************************************************/
    //      Utility Functions used in handling events         //
    /**********************************************************/

    //called from onClick(), executed when a person is selected
    private void onSelect(int position){
        //todo need to update selection visually
/*
        mSelectedPosition = position;

        MMSchedMedManager schedMedManager = MMSchedMedManager.getInstance();
        mSelectedScheduleMedication =
                schedMedManager.getScheduleMedicationFromCursor(scheduleCursor,position);
*/
        Toast.makeText(getActivity(),
                "Position " + String.valueOf(position) + " is selected!",
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
        private MainMMHomeFragment.ClickListener clickListener;

        public RecyclerTouchListener(Context context,
                                     final RecyclerView recyclerView,
                                     final MainMMHomeFragment.ClickListener clickListener) {

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
