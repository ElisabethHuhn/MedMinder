package com.androidchicken.medminder;


import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;


/**
 * A placeholder fragment containing a simple view.
 */
public class MMHomeFragment extends Fragment {

    private static final String TAG = "MainMMTakeDoseFragment";
    private static final int HALF_SECOND = 500;

    /***********************************************/
    /*          UI Widgets                         */
    /***********************************************/

    ArrayList<Button>   mMedButtons = new ArrayList<>();
    ArrayList<EditText> mMedEdits   = new ArrayList<>();
    


    /***********************************************/
    /*          Member Variables                   */
    /***********************************************/
    private long  mPersonID;
    private boolean isUIChanged = false;


    /***********************************************/
    /*          Static Methods                     */
    /***********************************************/
    //need to pass a person into the fragment
    public static MMHomeFragment newInstance(long personID){
        //create a bundle to hold the arguments
        Bundle args = new Bundle();

        //It will be some work to make all of the data model serializable
        //so for now, just pass the person values
        args.putLong         (MMPerson.sPersonIDTag,personID);

        MMHomeFragment fragment = new MMHomeFragment();

        fragment.setArguments(args);
        return fragment;
    }


    /***********************************************/
    /*          Constructor                        */
    /***********************************************/
    public MMHomeFragment() {
    }

    /***********************************************/
    /*          Lifecycle Methods                  */
    /***********************************************/

    //pull the arguments out of the fragment bundle and store in the member variables
    //In this case, prepopulate the personID this screen refers to
    @Override
    public void onCreate(Bundle savedInstanceState){

        super.onCreate(savedInstanceState);

        //Initialize the DB if necessary
        try {
            //initialize the database
            MMDatabaseManager.getInstance(getActivity());
        }catch (Exception e) {
            Log.e(TAG,Log.getStackTraceString(e));
        }

        Bundle args = getArguments();

        if (args != null) {
            mPersonID = args.getLong(MMPerson.sPersonIDTag);
        } else {
            mPersonID = MMUtilities.ID_DOES_NOT_EXIST;
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        //First, get the mPersonID set properly
        if (savedInstanceState != null){
            //the fragment is being restored so restore the person ID
            mPersonID = savedInstanceState.getLong(MMPerson.sPersonIDTag);
        }

        if (mPersonID == MMUtilities.ID_DOES_NOT_EXIST){
            //see if there is anything stored in shared preferences from the last time Home ran
            SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
            long defaultValue = MMUtilities.ID_DOES_NOT_EXIST;
            mPersonID = sharedPref.getLong(MMPerson.sPersonIDTag, defaultValue);
        }

        if (mPersonID != MMUtilities.ID_DOES_NOT_EXIST) {
            //Store the PersonID for the next time the Home Fragment runs
            SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putLong(MMPerson.sPersonIDTag, mPersonID);
            editor.apply();
        }


        //Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_home, container, false);


        //Wire up the UI widgets so they can handle events later
        wireWidgets(v);
        initializeRecyclerView(v);
        initializeUI(v);


        //hide the soft keyboard
        MMUtilities.hideSoftKeyboard(getActivity());

        //start the medButton animation
        startMedButtonBlink(v);

        getActivity().getWindow().
                setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        return v;
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState){
        // Save custom values into the bundle
        savedInstanceState.putLong(MMPerson.sPersonIDTag, mPersonID);
        // Always call the superclass so it can save the view hierarchy statesuper.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onResume(){
        super.onResume();

        //set the title bar subtitle
        ((MainActivity) getActivity()).setMMSubtitle(R.string.title_home);
    }

    /***********************************************/
    /*   Initialization Methods                    */
    /***********************************************/

    private void   wireWidgets(View v){

        //Patient Profile Button
        Button editPatientButton = (Button) v.findViewById(R.id.patientProfileButton);
        editPatientButton.setText(R.string.patient_edit_profile_label);
        //the order of images here is left, top, right, bottom
        // editPatientButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_project, 0, 0);
        if (mPersonID != MMUtilities.ID_DOES_NOT_EXIST){
            //if there is a person active, enable the button
            editPatientButton.setEnabled(true);
            editPatientButton.setTextColor(ContextCompat.getColor(getActivity(),
                                            R.color.colorTextBlack));
        }
        editPatientButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(),
                        R.string.patient_edit_profile_label,
                        Toast.LENGTH_SHORT).show();
                //switch to person screen
                // But the switching happens on the container Activity
                if (mPersonID == MMUtilities.ID_DOES_NOT_EXIST) {
                    ((MainActivity) getActivity()).switchToPersonScreen();
                } else {
                    //pre-populate
                    ((MainActivity) getActivity()).switchToPersonScreen(mPersonID);
                }

            }
        });


        //Select Patient Button
        Button selectPatientButton = (Button) v.findViewById(R.id.selectPatientButton);
        selectPatientButton.setText(R.string.select_patient_label);
        //the order of images here is left, top, right, bottom
        // selectPatientButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_project, 0, 0);
        selectPatientButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(),
                        R.string.select_patient_label,
                        Toast.LENGTH_SHORT).show();
                //switch to person screen
                // But it has to know where to come back to
                ((MainActivity)getActivity()).
                        switchToPersonListScreen(MainActivity.sHomeTag, mPersonID);
            }
        });

        //save Button
        Button mSaveButton = (Button) v.findViewById(R.id.homeSaveButton);
        mSaveButton.setText(R.string.save_label);
        //the order of images here is left, top, right, bottom
        //mSaveButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_collect, 0, 0);
        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPersonID == MMUtilities.ID_DOES_NOT_EXIST)return;
                Toast.makeText(getActivity(),
                        R.string.save_label,
                        Toast.LENGTH_SHORT).show();

                onSave();
                ((MainActivity) getActivity()).switchToHomeScreen(mPersonID);

            }
        });




        //Medication Buttons
        if (mPersonID != MMUtilities.ID_DOES_NOT_EXIST){

            MMPerson person = MMUtilities.getPerson(mPersonID);

            if (person != null) {

                ArrayList<MMMedication> medications = person.getMedications();

                int last = medications.size();
                if (last > 0) {
                    //convert pixels to dp
                    int sizeInDp = 10; //padding between buttons

                    addDateTimeFieldsToView(v, sizeInDp);

                    int position = 0;

                    while (position < last) {
                        MMMedication medication = medications.get(position);
                        if ((medication != null) && (medication.isCurrentlyTaken())) {
                            addMedButtonToView(v, medication, sizeInDp);
                        }
                        position++;
                    }
                }
            }
        }
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
        RecyclerView recyclerView = getRecyclerView(v);

        //3) create and assign a layout manager to the recycler view
        //RecyclerView.LayoutManager mLayoutManager  = new LinearLayoutManager(getActivity());
        RecyclerView.LayoutManager mLayoutManager  = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(mLayoutManager);

        //4) Get the list of ConcurrentDose Instances from the ConcurrentDoseManager

        //      get the singleton list container
        MMConcurrentDoseManager concurrentDoseManager = MMConcurrentDoseManager.getInstance();
        //      then go get our list of concurrentDoses
        Cursor concurrentDoseCursor = concurrentDoseManager.getAllConcurrentDosesCursor(mPersonID);

        //5) Use the data to Create and set out concurrentDose Adapter
        //     even though we're giving the Adapter the list,
        //     Adapter uses the ConcurrentDoseManager to maintain the list and
        //     the items in the list.
        int numbMeds = 0; //initialize to person not yet existing

        if (mPersonID != MMUtilities.ID_DOES_NOT_EXIST){
            MMPerson person = MMUtilities.getPerson(mPersonID);
            if (person != null) {
                numbMeds = person.getMedications().size();

            }
        }
        MMConcurrentDoseCursorAdapter adapter =
                new MMConcurrentDoseCursorAdapter(getActivity(),
                                                  mPersonID,
                                                  numbMeds,
                                                  concurrentDoseCursor);
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
            new RecyclerTouchListener(getActivity(), recyclerView, new ClickListener() {

                @Override
                public void onClick(View view, int position) {
                    onSelect(position);
                }

                @Override
                public void onLongClick(View view, int position) {

                }
            }));

    }

    private void   initializeUI(View v){
        //determine if a person is yet associated with the fragment
        if (mPersonID != MMUtilities.ID_DOES_NOT_EXIST){
            //if there is a person corresponding to the patientID, put the name up on the screen
            MMPerson person = MMUtilities.getPerson(mPersonID);

            if (person != null) {
                TextView patientNickName = (TextView) v.findViewById(R.id.patientNickNameLabel);
                patientNickName.setText(person.getNickname().toString().trim());
            }
        }
        setUISaved(v);
    }


    /***********************************************/
    /*   Initialization of Views                   */
    /***********************************************/

    private void   addDateTimeFieldsToView(View v, int sizeInDp){

        int padding = MMUtilities.convertPixelsToDp(getActivity(), sizeInDp);

        LinearLayout layout = (LinearLayout) v.findViewById(R.id.medInputLayout);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                0,//width
                ViewGroup.LayoutParams.WRAP_CONTENT);//height
        lp.weight = 4f;
        //lp.gravity = Gravity.CENTER;//set below too
        lp.setMarginEnd(padding);

        EditText timeInput = new EditText(getActivity());
        timeInput.setHint(R.string.dose_default_time);
        timeInput.setInputType(InputType.TYPE_CLASS_TEXT);
        timeInput.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
        timeInput.setLayoutParams(lp);
        timeInput.setPadding(0,0,padding,0);
        timeInput.setGravity(Gravity.CENTER);
        timeInput.setTextColor      (ContextCompat.getColor(getActivity(),R.color.colorTextBlack));
        timeInput.setBackgroundColor(ContextCompat.getColor(getActivity(),R.color.colorInputBackground));


        //Time input for this dose
        //There is no label for this field
        timeInput.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
               /* Toast.makeText(getActivity(),
                        R.string.input_received,
                        Toast.LENGTH_SHORT).show();
               */
                return false;
            }
        });

        timeInput.setText(MMUtilities.getTimeString());

        layout.addView(timeInput);

    }

    private Button addMedButtonToView(View v, MMMedication medication, int sizeInDp){
        Button medButton;
        EditText edtView;

        int padding = MMUtilities.convertPixelsToDp(getActivity(), sizeInDp);

        //
        //Add the button to the button layout
        //
        LinearLayout layout = (LinearLayout) v.findViewById(R.id.medButtonLayout);

        medButton = new Button(getActivity());
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,//width
                ViewGroup.LayoutParams.WRAP_CONTENT);//height
        lp.weight = 1f;
        lp.setMarginEnd(padding);

        medButton.setLayoutParams(lp);
        medButton.setBackgroundColor(ContextCompat.getColor(getActivity(),R.color.colorButton1Background));

        medButton.setPadding(0,0,padding,0);
        medButton.setText(medication.getMedicationNickname());
        medButton.setTextColor(ContextCompat.getColor(getActivity(),R.color.colorTextBlack));
        layout.addView(medButton);

        addMedButtonListener(medButton);

        //save the pointer to the button
        mMedButtons.add(medButton);


        //
        //add EditText to the dose layout
        //
        layout = (LinearLayout) v.findViewById(R.id.medInputLayout);
        lp = new LinearLayout.LayoutParams(
                0,//width
                ViewGroup.LayoutParams.WRAP_CONTENT);//height
        lp.weight = 1f;
        //lp.gravity = Gravity.CENTER;
        lp.setMarginEnd(padding);

        edtView = new EditText(getActivity());
        edtView.setHint("0");
        edtView.setInputType(InputType.TYPE_CLASS_TEXT);
        edtView.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
        edtView.setLayoutParams(lp);
        edtView.setPadding(0,0,padding,0);
        edtView.setGravity(Gravity.CENTER);
        edtView.setTextColor      (ContextCompat.getColor(getActivity(),R.color.colorTextBlack));
        edtView.setBackgroundColor(ContextCompat.getColor(getActivity(),R.color.colorInputBackground));

        //add listener
        edtView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                return false;
            }
        });

        layout.addView(edtView);

        mMedEdits.add(edtView);

        return medButton;
    }

    private void   addMedButtonListener(Button medButton){

        if (medButton == null) return;

        //add the listeners to the button
        medButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int last = mMedButtons.size();
                int position = 0;
                Button medButton;
                while (position < last){
                    medButton = mMedButtons.get(position);
                    if (medButton == v){

                        //stop the blinking
                        medButton.clearAnimation();

                        CharSequence msg = getString(R.string.patient_medication_button) +
                                " "+ String.valueOf(position+1);
                        Toast.makeText(getActivity(),msg, Toast.LENGTH_SHORT).show();

                        //Show the amount taken
                        showDose(position);

                        //indicate the UI has changed
                        setUIChanged();
                        return;
                    }
                    position++;
                }
                MMUtilities.errorHandler(getActivity(), R.string.patient_no_med_but);

            }
        });
        medButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                Toast.makeText(getActivity(),
                        R.string.person_med_long_click,
                        Toast.LENGTH_SHORT).show();

                setUIChanged();
                return true;
            }
        });

    }

    private void   startMedButtonBlink(View v){
        //if (true)return;
        if (mPersonID == MMUtilities.ID_DOES_NOT_EXIST)return;

        MMPerson person = MMUtilities.getPerson(mPersonID);
        if (person == null)return;

        if (mMedButtons == null){
            mMedButtons = new ArrayList<>();
        }

        Calendar calendar = Calendar.getInstance();
        long currentTimeMinutesSinceMidnight = getMinutesFromCalendar(calendar);

        ArrayList<MMMedication> medications = person.getMedications();
        int lastMed = medications.size();
        //position within medications for this person
        //corresponds to the position of medButton for this medication
        int positionMed = 0;
        Button medButton;

        //If the dose is overdue, blink the button
        while (positionMed < lastMed){
            //Get the most recent dose taken for this medication
            MMMedication medication = medications.get(positionMed);
            MMDose dose = getMostRecentDose(v, medication);

            long lastTakenMinutes;
            boolean firstDoseOfDay = true;

            //If there are no doses yet taken, it is due
            if (dose == null){
                lastTakenMinutes = 0;  //minutes since midnight
            } else {
                //The timeTaken is the number of MINUTES since midnight
                long timeTaken = dose.getTimeTaken();

                //takes into account whether it's was taken today
                //zero if the first dose of the day has not yet been taken
                lastTakenMinutes = getLastTakenMinutes(timeTaken);
            }
            //so we have the time the dose was taken. When is/was the next dose due?

            //The schedule of the medication gives us
            // how many minutes since midnight that the dose is due

            MMScheduleMedication schedule;
            ArrayList<MMScheduleMedication> schedules = medication.getSchedules();
            int scheduleTimeDue;
            int lastSched = schedules.size();
            int positionSched = 0;
            while (positionSched < lastSched){
                schedule = schedules.get(positionSched);
                scheduleTimeDue = schedule.getTimeDue();

                //if schedule time is greater than the time the last dose was taken
                if (scheduleTimeDue > lastTakenMinutes) {
                    //So this scheduled dose has not yet been taken
                    if (scheduleTimeDue <= currentTimeMinutesSinceMidnight) {
                        //it is time to take the dose, blink the proper button
                        medButton = mMedButtons.get(positionMed);
                        animateButton(medButton);
                        //no need to loop through the rest of the schedules
                        positionSched = lastSched;
                    }
                }
                positionSched++;
            }//end while schedule loop

            positionMed++;
        }//end while medication loop
    }

    private long   getLastTakenMinutes(long timeTaken){
        long lastTakenMinutes = 0L;


        //compare whether the last dose was taken today
        Date lastTaken = new Date(timeTaken);

        Date now = new Date();

        SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMdd");
        boolean firstDoseOfDay = !(fmt.format(lastTaken).equals(fmt.format(now)));

        if (firstDoseOfDay) {
            lastTakenMinutes = 0;
        } else {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(lastTaken);
            lastTakenMinutes = getMinutesFromCalendar(calendar);
        }
        return lastTakenMinutes;
    }

    private long   getMinutesFromCalendar (Calendar calendar){
        return (calendar.get(Calendar.HOUR_OF_DAY) * 60) + calendar.get(Calendar.MINUTE);
    }

    private void   animateButton(Button medButton){
        // Change alpha from fully visible to invisible
        final Animation animation = new AlphaAnimation(1.0f, 0.0f);

        // duration - half a second
        animation.setDuration(HALF_SECOND);
        // do not alter animation rate
        animation.setInterpolator(new LinearInterpolator());
        // Repeat animation infinitely
        animation.setRepeatCount(Animation.INFINITE);
        // Reverse animation at the end so the button will fade back in
        animation.setRepeatMode(Animation.REVERSE);

        medButton.startAnimation(animation);
    }

    private void   showDose(int position){
        if (mPersonID != MMUtilities.ID_DOES_NOT_EXIST){
            MMPerson person = MMUtilities.getPerson(mPersonID);

            if (person != null) {

                //get the medication
                MMMedication medication = getMedicationFromPerson(person, position);

                if (medication != null) {
                    //and the dose
                    int doseAmt = medication.getDoseAmount();
                    //and the EditText field
                    EditText medField = mMedEdits.get(position);

                    if (medField != null) {
                        //show the user
                        medField.setText(String.valueOf(doseAmt));
                    }
                }
            }
        }

    }



  /***********************************************/
    /*   Get object instances                      */
    /***********************************************/
    private MMDose getMostRecentDose(View v, MMMedication medication){
        //The concurrent doses for this person are in the Cursor that the Adapter is holding
        Cursor cursor = getCursor(v);
        if (cursor == null)return null;

        ArrayList<MMDose> doses;
        MMDose dose;
        MMDose mostRecentDose = null;

        Calendar c = Calendar.getInstance();
        long milliSeconds = c.getTimeInMillis();     // = c.get(Calendar.SECOND);

        //position within cursor
        int positionConDose = 0;
        int last = cursor.getCount();
        while (positionConDose < last) {
            MMConcurrentDose concurrentDose = getAdapter(v).getConcurrentDoseAt(positionConDose);
            long timeOfCCDose = concurrentDose.getStartTime();
            doses = concurrentDose.getDoses();
            int lastDoses = doses.size();
            int positionDoses = 0;

            while (positionDoses < lastDoses){
                dose = doses.get(positionDoses);
                if (dose.getOfMedicationID() == medication.getMedicationID()){
                    if ((mostRecentDose == null)||
                        (dose.getTimeTaken() > mostRecentDose.getTimeTaken())){
                        //replace the mostRecentDose with the one we just found
                        mostRecentDose = dose;
                    }
                }
                positionDoses++;
            }
            positionConDose++;
        }
        return mostRecentDose;
    }

    private MMMedication getMedicationFromPersonID(long personID, int position){
        MMPerson person = MMUtilities.getPerson(personID);
        return getMedicationFromPerson(person, position);
    }

    private MMMedication getMedicationFromPerson(MMPerson person, int position){
        return person.getMedications().get(position);
    }

    private int getNumberOfMedicationsFromPerson(MMPerson person){
        return person.getMedications().size();
    }



    /**********************************************************/
    //      Methods dealing with whether the UI has changed   //
    /**********************************************************/
    private void setUIChanged(){
        isUIChanged = true;
        saveButtonEnable(MMUtilities.BUTTON_ENABLE);
    }

    private void setUISaved(){
        isUIChanged = false;

        //disable the save button
        saveButtonEnable(MMUtilities.BUTTON_DISABLE);
    }

    private void setUISaved(View v){
        isUIChanged = false;

        //disable the export button
        saveButtonEnable(v, MMUtilities.BUTTON_DISABLE);
    }

    private void saveButtonEnable(boolean isEnabled){
        View v = getView();
        saveButtonEnable(v, isEnabled);
    }


    private void saveButtonEnable(View v, boolean isEnabled){

        if (v == null)return; //onCreateView() hasn't run yet

        Button personSaveButton =
                (Button) v.findViewById(R.id.homeSaveButton);

        MMUtilities.enableButton(getActivity(),
                personSaveButton,
                isEnabled);
    }



    /************************************************************/
    /**********  RecyclerView / Adapter related Methods  ********/
    /************************************************************/

    private void reinitializeCursor(long personID){
        if (personID == MMUtilities.ID_DOES_NOT_EXIST) return;

        //reset the list
        MMConcurrentDoseCursorAdapter adapter = getAdapter(getView());

        if (adapter != null) {
            adapter.reinitializeCursor();
        } else {
            //we did not have a medication earlier so it never got initialized
            initializeRecyclerView(getView());
        }
    }

    private Cursor getCursor(View v){
        MMConcurrentDoseCursorAdapter adapter = getAdapter(v);
        return adapter.getCursor();
    }

    private RecyclerView getRecyclerView(View v){
        return  (RecyclerView) v.findViewById(R.id.doseHistoryList);
    }

    private MMConcurrentDoseCursorAdapter getAdapter(View v){
        return (MMConcurrentDoseCursorAdapter) getRecyclerView(v).getAdapter();
    }




    /***********************************************/
    /*    Event Handler Methods                    */
    /***********************************************/
    private long onSave(){

        //Creates in memory structure to save all the doses taken concurrently
        if (mPersonID == MMUtilities.ID_DOES_NOT_EXIST)return MMDatabaseManager.sDB_ERROR_CODE;

        MMPerson person = MMUtilities.getPerson(mPersonID);

        if (person == null) return MMDatabaseManager.sDB_ERROR_CODE;

        Calendar c = Calendar.getInstance();
        long milliSeconds = c.getTimeInMillis();     // = c.get(Calendar.SECOND);

        MMConcurrentDose concurrentDoses = new MMConcurrentDose(mPersonID, milliSeconds);
        ArrayList<MMDose> doses = new ArrayList<>();
        concurrentDoses.setDoses(doses);

        ArrayList<MMMedication> medications = person.getMedications();

        int last = medications.size();
        int position = 0;
        int amtTaken;
        String amtTakenString;
        while (position < last) {
            amtTakenString = mMedEdits.get(position).getText().toString().trim();
            if (!amtTakenString.isEmpty()) {
                amtTaken = Integer.valueOf(amtTakenString);
                if (amtTaken > 0) {
                    MMDose dose = new MMDose(medications.get(position).getMedicationID(),
                            mPersonID,
                            concurrentDoses.getConcurrentDoseID(),
                            position,
                            milliSeconds,
                            amtTaken);
                    doses.add(dose);
                }
            }
            position++;
        }

        MMConcurrentDoseManager concurrentDoseManager = MMConcurrentDoseManager.getInstance();
        long cDoseID = concurrentDoseManager.add(concurrentDoses);

        reinitializeCursor(mPersonID);
        setUISaved();
        return cDoseID;

    }

    private void onExit(){
        MMConcurrentDoseCursorAdapter adapter = getAdapter(getView());
        adapter.closeCursor();
    }

    private void onDelete(){
        Toast.makeText(getActivity(),
                R.string.delete_title,
                Toast.LENGTH_SHORT).show();

        areYouSureDelete();
    }



    /**********************************************************/
    //      Utility Functions used in handling events         //
    /**********************************************************/

    //called from onClick(), executed when a concurrent dose is selected from list
    private void onSelect(int position){
        //todo need to update selection visually

        MMConcurrentDoseCursorAdapter adapter = getAdapter(getView());
        MMConcurrentDose selectedConcurrentDose = adapter.getConcurrentDoseAt(position);

        // TODO: 3/22/2017 Need to decide how to use the selected concurrent dose
        //create a dialogue to allow the user to:
        // 1) change the Doses on this Concurrent Dose:
        // OR
        // 2) delete the line

        //Date
        //Time
        //For each Medication on the Person: a dose amount

        //Save the Concurrent Dose on the Callback from the Dialogue

        Toast.makeText(getActivity(),
                "Position " + String.valueOf(position) + " is selected!",
                Toast.LENGTH_SHORT).show();


    }

    /************************************/
    /*****  Delete Button Dialogue    *****/
    /************************************/
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
        Toast.makeText(getActivity(),
                R.string.delete_concurrent_dose,
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





    /**************************************************/
    //      Dialogues for Changing List Doses         //
    /**************************************************/


}
