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
import java.util.Locale;

import static com.androidchicken.medminder.MMUtilities.getTimeString;


/**
 * A placeholder fragment containing a simple view.
 */
public class MMHomeFragment extends Fragment {

    private static final String TAG = "MainMMTakeDoseFragment";
    private static final int HALF_SECOND = 500;

    public  static final String sIsUIChangedTag       = "IS_UI_CHANGED";
    private static final String sDoseTimeTag         = "DOSE_TIME";
    private static final String sDoseAmountTag       = "DOSE_AMOUNT_%d";
    public  static final String sSelectedPositionTag = "SELECTED_POSITION";

    public  static final int sSELECTED_DIALOG_NOT_VISIBLE = -1;


    //**********************************************/
    /*          UI Widgets                         */
    //**********************************************/

    //These do not need to be saved during configuration change as the View is rebuild.
    // Thus these lists will be rebuilt in the process
    ArrayList<Button>   mMedButtons = new ArrayList<>();
    ArrayList<EditText> mMedEdits   = new ArrayList<>();

    //null indicates reconfigure for use when rebuilding the Dose views
    private EditText mTimeInput;



    //**********************************************/
    /*          Member Variables                   */
    /*    These variables will need to survive     */
    /*          configuration change               */
    //**********************************************/
    private long    mPersonID;
    private boolean isUIChanged   = false;
    private int mSelectedPosition = sSELECTED_DIALOG_NOT_VISIBLE;

    //**********************************************/
    /*          Static Methods                     */
    //**********************************************/
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

    //**********************************************/
    /*          Constructor                        */
    //**********************************************/
    public MMHomeFragment() {
    }

    //**********************************************/
    /*          Lifecycle Methods                  */
    //**********************************************/

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
            mSelectedPosition = savedInstanceState.getInt(sSelectedPositionTag);
            //The rest of the saved state in the bundle will be used later
            // to recreate the Dose views and the isUIChanged flag
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
        wireWidgets(v, savedInstanceState);
        initializeRecyclerView(v);
        initializeUI(v);


        //hide the soft keyboard
        MMUtilities.hideSoftKeyboard(getActivity());

        //start the medButton animation
        startMedButtonBlink(v);

        getActivity().getWindow().
                setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        //Set the changed UI flag based on whether we are recreating the View
        if (savedInstanceState != null){
            isUIChanged = savedInstanceState.getBoolean(sIsUIChangedTag);
            if (isUIChanged){
                setUIChanged(v);
            } else {
                setUISaved(v);
            }

            mSelectedPosition = savedInstanceState.getInt(sSelectedPositionTag);
            if (mSelectedPosition != sSELECTED_DIALOG_NOT_VISIBLE){
                //Can't put the dialog back up
                //onSelectDoseDialog(mSelectedPosition);
                MMUtilities.errorHandler(getActivity(), R.string.reselect_dose);
            }
        }

        return v;
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState){
        // Save custom values into the bundle

        //Save the PersonID of this screen
        savedInstanceState.putLong(MMPerson.sPersonIDTag, mPersonID);

        //Save the isUIChanged flag
        savedInstanceState.putBoolean(sIsUIChangedTag, isUIChanged);

        //save the selected position in case we are in the middle of the dialog
        savedInstanceState.putInt(sSelectedPositionTag, mSelectedPosition);

        //the dose time and amounts
        if ((mPersonID != MMUtilities.ID_DOES_NOT_EXIST) &&
            (mTimeInput != null)) {
                savedInstanceState.putString(sDoseTimeTag, mTimeInput.getText().toString());

                //The dose amounts already entered but not saved
                EditText editText;
                String etTag;
                int last = mMedEdits.size();
                int position = 0;

                while (position < last) {
                    editText = mMedEdits.get(position);
                    etTag = String.format(sDoseAmountTag, position);
                    savedInstanceState.putString(etTag, editText.getText().toString());

                    position++;
                }
        }

        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onResume(){
        super.onResume();

        //set the title bar subtitle
        ((MMMainActivity) getActivity()).setMMSubtitle(R.string.title_home);

        //Set the FAB visible
        ((MMMainActivity) getActivity()).showFAB();

        if (mSelectedPosition != sSELECTED_DIALOG_NOT_VISIBLE){
            //put the dialog back up
            Toast.makeText(getActivity(), "Please re-select the dose", Toast.LENGTH_SHORT).show();
            mSelectedPosition = sSELECTED_DIALOG_NOT_VISIBLE;
            //onSelectDoseDialog(mSelectedPosition);
        }
    }

    //**********************************************/
    /*   Initialization Methods                    */
    //**********************************************/

    private void   wireWidgets(View v, Bundle savedInstanceState){

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
                    ((MMMainActivity) getActivity()).switchToPersonScreen();
                } else {
                    //pre-populate
                    ((MMMainActivity) getActivity()).switchToPersonScreen(mPersonID);
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
                ((MMMainActivity)getActivity()).
                        switchToPersonListScreen(MMMainActivity.sHomeTag, mPersonID);
            }
        });

        //save Button
        Button saveButton = (Button) v.findViewById(R.id.homeSaveButton);
        saveButton.setText(R.string.save_label);
        //the order of images here is left, top, right, bottom
        //mSaveButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_collect, 0, 0);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPersonID == MMUtilities.ID_DOES_NOT_EXIST)return;
                Toast.makeText(getActivity(),
                        R.string.save_label,
                        Toast.LENGTH_SHORT).show();

                onSave();
                ((MMMainActivity) getActivity()).switchToHomeScreen(mPersonID);

            }
        });


        //Show Medications Help Button
        Button showMedsHelpButton = (Button) v.findViewById(R.id.showMedsHelpButton);
        showMedsHelpButton.setText(R.string.show_meds_help_label);
        //the order of images here is left, top, right, bottom
        //mShowMedsHelpButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_collect, 0, 0);
        showMedsHelpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPersonID == MMUtilities.ID_DOES_NOT_EXIST)return;
                //Show the medication positions for the history list
                ((MMMainActivity) getActivity()).switchToHistoryTitleScreen(mPersonID);

            }
        });


        //Medication Alerts
        Button medAlertsButton = (Button) v.findViewById(R.id.patientAlertsButton);
        medAlertsButton.setText(R.string.medication_alert_screen_label);
        //the order of images here is left, top, right, bottom
        //mShowMedsHelpButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_collect, 0, 0);
        medAlertsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPersonID == MMUtilities.ID_DOES_NOT_EXIST)return;
                //Show the medication positions for the history list
                ((MMMainActivity) getActivity()).switchToMedicationAlertScreen(mPersonID);

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

                    addDateTimeFieldsToView(v, savedInstanceState, sizeInDp);

                    int position = 0;

                    while (position < last) {
                        MMMedication medication = medications.get(position);
                        if ((medication != null) && (medication.isCurrentlyTaken())) {
                            addMedButtonToView(v,
                                               savedInstanceState,
                                               position,
                                               medication.getMedicationNickname().toString(),
                                               sizeInDp);
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
        boolean reverseLayout = true;
        RecyclerView.LayoutManager mLayoutManager  = new LinearLayoutManager(getActivity(),
                                                                       LinearLayoutManager.VERTICAL,
                                                                       reverseLayout);

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

                if (person.isCurrentlyExists()){
                    v.setBackgroundColor(ContextCompat.
                            getColor(getActivity(), R.color.colorScreenBackground));
                } else {
                    v.setBackgroundColor(ContextCompat.
                            getColor(getActivity(), R.color.colorScreenDeletedBackground));
                }
            }
        } else {
            v.setBackgroundColor(ContextCompat.
                    getColor(getActivity(), R.color.colorScreenDeletedBackground));
        }
        setUISaved(v);
    }


    public long getPersonID(){
        return mPersonID;
    }

    //**********************************************/
    /*   Initialization of Views                   */
    //**********************************************/

    private void   addDateTimeFieldsToView(View v, Bundle savedInstanceState, int sizeInDp){

        int padding = MMUtilities.convertPixelsToDp(getActivity(), sizeInDp);

        LinearLayout layout = (LinearLayout) v.findViewById(R.id.medInputLayout);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                0,//width
                ViewGroup.LayoutParams.WRAP_CONTENT);//height
        lp.weight = 4f;
        //lp.gravity = Gravity.CENTER;//set below too
        lp.setMarginEnd(padding);

        mTimeInput = new EditText(getActivity());

        mTimeInput.setInputType(InputType.TYPE_CLASS_TEXT);
        mTimeInput.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
        mTimeInput.setLayoutParams(lp);
        mTimeInput.setPadding(0,0,padding,0);
        mTimeInput.setGravity(Gravity.CENTER);
        mTimeInput.setTextColor      (ContextCompat.getColor(getActivity(),R.color.colorTextBlack));
        mTimeInput.setBackgroundColor(ContextCompat.getColor(getActivity(),R.color.colorInputBackground));
        mTimeInput.setFocusable(false);


        //Time input for this dose
        //There is no label for this field
        mTimeInput.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                return false;
            }
        });

        String timeString;
        if (savedInstanceState == null) {
            timeString = MMUtilities.getTimeString();
        } else {
            timeString = savedInstanceState.getString(sDoseTimeTag);
        }
        mTimeInput.setText(timeString);

        layout.addView(mTimeInput);

    }

    private Button addMedButtonToView(View         v,
                                      Bundle       savedInstanceState,
                                      int          viewNumber,
                                      String       buttonText,
                                      int          sizeInDp){
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
        medButton.setText(buttonText);
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

        lp.setMarginEnd(padding);


        edtView = new EditText(getActivity());
        edtView.setFreezesText(true);
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

        String amountString;
        if (savedInstanceState == null){
            amountString = "0";
        } else {
            String etTag = String.format(sDoseAmountTag, viewNumber);
            amountString = savedInstanceState.getString(etTag);
        }
        edtView.setText(amountString);

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
        int positionButton = 0;
        Button medButton;

        //If the dose is overdue, blink the button
        while (positionMed < lastMed){
            //Get the most recent dose taken for this medication
            MMMedication medication = medications.get(positionMed);
            if (medication.isCurrentlyTaken()) {
                //There are only medButtons for medications that are currently taken
                MMDose dose = getMostRecentDose(v, medication);

                long lastTakenMinutes;

                //If there are no doses yet taken, it is due
                if (dose == null) {
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
                while (positionSched < lastSched) {
                    schedule = schedules.get(positionSched);
                    scheduleTimeDue = schedule.getTimeDue();

                    //if schedule time is greater than the time the last dose was taken
                    if (scheduleTimeDue > lastTakenMinutes) {
                        //So this scheduled dose has not yet been taken
                        if (scheduleTimeDue <= currentTimeMinutesSinceMidnight) {
                            //it is time to take the dose, blink the proper button
                            medButton = mMedButtons.get(positionButton);
                            animateButton(medButton);
                            //no need to loop through the rest of the schedules
                            positionSched = lastSched;
                        }
                    }
                    positionSched++;
                }//end while schedule loop
                positionButton++;
            }

            positionMed++;
        }//end while medication loop
    }

    private long   getLastTakenMinutes(long timeTaken){

        //compare whether the last dose was taken today
        Date lastTaken = new Date(timeTaken);

        Date now = new Date();

        SimpleDateFormat fmt   = new SimpleDateFormat("yyyyMMdd");
        boolean firstDoseOfDay = !(fmt.format(lastTaken).equals(fmt.format(now)));

        long lastTakenMinutes = 0;
        if (!firstDoseOfDay) {
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

    //**********************************************/
    /*   Get object instances                      */
    //**********************************************/
    private MMDose getMostRecentDose(View v, MMMedication medication){
        //The concurrent doses for this person are in the Cursor that the Adapter is holding
        Cursor cursor = getCursor(v);
        if (cursor == null)return null;

        ArrayList<MMDose> doses;
        MMDose dose;
        MMDose mostRecentDose = null;

        //Calendar c = Calendar.getInstance();
        //long milliSeconds = c.getTimeInMillis();     // = c.get(Calendar.SECOND);

        //position within cursor
        int positionConDose = 0;
        int last = cursor.getCount();
        while (positionConDose < last) {
            MMConcurrentDose concurrentDose = getAdapter(v).getConcurrentDoseAt(positionConDose);

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

    //***********************************************************/
    //*********  RecyclerView / Adapter related Methods  ********/
    //***********************************************************/

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

    //**********************************************/
    /*    Event Handler Methods                    */
    //**********************************************/
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
        MMMedication medication;
        int positionMed = 0;
        int positionButton = 0; //only have buttons for active meds
        int amtTaken;
        String amtTakenString;
        while (positionMed < last) {
            medication = medications.get(positionMed);
            if (medication.isCurrentlyTaken()) {
                amtTakenString = mMedEdits.get(positionButton).getText().toString().trim();
                if (!amtTakenString.isEmpty()) {
                    amtTaken = Integer.valueOf(amtTakenString);
                    if (amtTaken > 0) {
                        MMDose dose = new MMDose(medications.get(positionMed).getMedicationID(),
                                mPersonID,
                                concurrentDoses.getConcurrentDoseID(),
                                positionMed,
                                milliSeconds,
                                amtTaken);
                        doses.add(dose);
                    }
                }
                positionButton++;
            }
            positionMed++;
        }

        MMConcurrentDoseManager concurrentDoseManager = MMConcurrentDoseManager.getInstance();
        long cDoseID = concurrentDoseManager.add(concurrentDoses);

        reinitializeCursor(mPersonID);
        /*
        */
        MMConcurrentDoseCursorAdapter adapter = getAdapter(getView());
        last = adapter.getItemCount();
        adapter.notifyItemChanged(last);

        RecyclerView recyclerView = getRecyclerView(getView());
        recyclerView.scrollToPosition(last);

        adapter.notifyDataSetChanged();


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

    //*********************************************************/
    //      Utility Functions used in handling events         //
    //*********************************************************/
    //called from onClick(), executed when a concurrent dose is selected from list
    private void onSelect(int position){
        mSelectedPosition = position;

        //allow user to change the dose amount and date, then save the changes
        onSelectDoseDialog(position);

    }

    //*********************************************/
    //****  Select ConcurrentDose Dialogue    *****/
    //*********************************************/
    //Build and display the alert dialog
    private void onSelectDoseDialog(int selectedPosition){
        //Get the concurrentDose to be updated
        MMConcurrentDoseCursorAdapter adapter   = getAdapter(getView());
        MMConcurrentDose selectedConcurrentDose = adapter.getConcurrentDoseAt(selectedPosition);
        //Get the Dose Amounts already recorded for this concurrentDose
        ArrayList<MMDose> doses = selectedConcurrentDose.getDoses();
        MMDose dose;
        int    amount;

        //
        //Build the (Dialog) layout and it's contained views
        // that define the ConcurrentDose and its contained Doses
        //
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View v = inflater.inflate(R.layout.list_row_dose_history, null);
        LinearLayout layout = (LinearLayout) v.findViewById(R.id.doseHistoryLine);

        TextView doseDate = (TextView) v.findViewById(R.id.doseDateLabel);
        doseDate.setEnabled  (false);
        doseDate.setFocusable(false);
        doseDate.setText(MMUtilities.getDateString(selectedConcurrentDose.getStartTime()));

        EditText doseTime = (EditText) v.findViewById(R.id.doseTimeInput);
        doseTime.setEnabled  (false);
        doseTime.setFocusable(false);
        doseTime.setText(getTimeString(selectedConcurrentDose.getStartTime()));


        //Get the medications this patient is taking
        int last     = 0;
        int medicationPosition = 0;
        ArrayList<MMMedication> medications = null;
        if (mPersonID != MMUtilities.ID_DOES_NOT_EXIST){
            MMPerson person = MMUtilities.getPerson(mPersonID);
            if (person != null) {
                medications = person.getMedications();
                last = medications.size();
            }
        }

        if (medications == null)return;

        //Build EditText views that will allow the user to input dose amounts
        //ArrayList<EditText> doseEditTexts = new ArrayList<>();
        EditText edtView;
        int sizeInDp = 2;
        int padding = MMUtilities.convertPixelsToDp(getActivity(), sizeInDp);

        //Create an EditText for each medication this patient takes
        //Each EditText corresponds positionally to the Medication the Patient takes
        int dosePosition = 0;
        while (medicationPosition < last){
            //get the dose amount that is already recorded for this medication
            if (dosePosition < doses.size()) {
                dose = doses.get(dosePosition);
                if (dose.getPositionWithinConcDose() == medicationPosition) {
                    amount = dose.getAmountTaken();
                    dosePosition++;
                } else {
                    amount = 0;
                }
            } else {
                //There weren't as many doses as there are medications. So the amt must be null
                amount = 0;
            }

            //actually create the EditText view in the utility
            edtView = MMUtilities.createDoseEditText(getActivity(), padding);

            //show the amount of this dose in the View
            edtView.setText(String.valueOf(amount));

            //Add the EditText view to the layout
            layout.addView(edtView);
            medicationPosition++;
        }


        //Create the AlertDialog to display the current doses to the user
        //and allow the user to update the amounts
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder .setView(v) //The View we just built for the Alert Dialog
                .setTitle(R.string.fix_dose_amounts)
                .setIcon(R.drawable.ground_station_icon)
                .setMessage(R.string.correct_amounts)
                .setPositiveButton(R.string.save_label,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                //which is a constant on DialogInterface
                                //      = BUTTON_POSITIVE or
                                //      = BUTTON_NEGATIVE or
                                //      = BUTTON_NEUTRAL
                                //Save these values
                                onSaveSelected(dialog, which);
                            }
                        })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                        Toast.makeText(getActivity(),
                                "Pressed Cancel",
                                Toast.LENGTH_SHORT).show();
                        mSelectedPosition = sSELECTED_DIALOG_NOT_VISIBLE; //set flag for dialog gone
                    }
                })
                .show();
    }

    private void onSaveSelected(DialogInterface dialog, int which){
        //This routine called from the POSITIVE button of the dialog that
        // invites the user to update the Dose Amounts
        //dialog is the AlertDialog built in onSelectDoseDialog()
        //which is a constant on DialogInterface
        //      = BUTTON_POSITIVE or
        //      = BUTTON_NEGATIVE or
        //      = BUTTON_NEUTRAL

        //Get the pointers to the views in the Dialog
        LinearLayout layout =
                (LinearLayout) ((AlertDialog) dialog).findViewById(R.id.doseHistoryLine);

        //can not change concurrentDoseID or personID
        //But convert the date to milliseconds
        TextView doseDate = (TextView) layout.findViewById(R.id.doseDateLabel);
        EditText doseTime = (EditText) layout.findViewById(R.id.doseTimeInput);

        //For now, do not allow update of time. It's just too hard to convert
        // TODO: 4/3/2017 allow user to update time dose taken

        CharSequence doseDateString = doseDate.getText();
        CharSequence doseTimeString = doseTime.getText();

        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy", Locale.US);
        SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a", Locale.US);

        Date dosageDate;
        Date dosageTime;

        try {
            dosageDate = dateFormat.parse(doseDateString.toString());
            dosageTime = timeFormat.parse(doseTimeString.toString());
        } catch (Exception e) {
            MMUtilities.errorHandler(getActivity(), R.string.error_parsing_date_time);
            dosageDate = null;
            dosageTime = null;
        }
        //need to leave the time calculations for a bit as
        // need selectedConcurrentDose before we can finish



        //set up the parameters for the While loop
        //  where the amounts will be updated
        int lastMedication     = 0;
        //child 0 is the doseDate and child 1 is the doseTime
        //Medication doses are children 2 through last
        int dosePosition = 0;
        int medicationPosition = 0;
        ArrayList<MMMedication> medications = null;
        if (mPersonID != MMUtilities.ID_DOES_NOT_EXIST){
            MMPerson person = MMUtilities.getPerson(mPersonID);
            if (person != null) {
                medications    = person.getMedications();
                lastMedication = medications.size();
            } else {
                medications    = new ArrayList<MMMedication>();
                lastMedication = 0;
            }
        }

        //get the amounts from the dialog EditText views, and
        // store them in the Doses in the ConcurrentDose instance
        EditText medicationDoseInput;
        MMDose   dose;
        int      amt;
        String   amtString;
        MMDoseManager doseManager = MMDoseManager.getInstance();

        //Get the ConcurrentDose to be updated with the values from the Dialog
        MMConcurrentDoseCursorAdapter adapter   = getAdapter(getView());
        MMConcurrentDose selectedConcurrentDose = adapter.getConcurrentDoseAt(mSelectedPosition);
        ArrayList<MMDose> doses = selectedConcurrentDose.getDoses();

        //Delete the old Dose Instances from the DB
        int lastDose = 0;
        if (doses != null){
            lastDose = doses.size();
            dosePosition = 0;
            while (dosePosition < lastDose){
                dose = doses.get(dosePosition);
                doseManager.removeDose(dose.getDoseID());

                dosePosition++;
            }
        }

        doses = new ArrayList<>();
        selectedConcurrentDose.setDoses(doses);

        //calculate the proper time
        long dateTimeMilliseconds;
        long timeMilliseconds;
        long dateMilliseconds;

        if ((dosageDate != null) && (dosageTime != null)) {
            dateMilliseconds = dosageDate.getTime();
            timeMilliseconds = dosageTime.getTime();

            dateTimeMilliseconds = dateMilliseconds + timeMilliseconds;
        } else {
            dateTimeMilliseconds = selectedConcurrentDose.getStartTime();
        }


        //If the initial amount was zero, there was no dose instance in the CD
        //So we have to check if any holes existed in the original data structure
        while (medicationPosition < lastMedication) {
            //Get the EditText view from the AlertDialog
            //Note the offBy2 is caused by the 2 EditText views that are not medications: Date and Time
            medicationDoseInput = (EditText) layout.getChildAt(medicationPosition+2);
            amtString = medicationDoseInput.getText().toString().trim();

            if (!amtString.isEmpty()){
                amt = Integer.valueOf(amtString);
                if (amt > 0) {
                    dose = new MMDose(medications.get(medicationPosition).getMedicationID(),
                            mPersonID,
                            selectedConcurrentDose.getConcurrentDoseID(),
                            medicationPosition,
                            dateTimeMilliseconds,
                            amt);
                    doses.add(dose);
                    //save/update the dose in the DB
                    doseManager.add(dose);
                }
            }

            dosePosition++;
            medicationPosition++;
        }
        //reset the selected position so we'll know the dialog is finished
        mSelectedPosition = sSELECTED_DIALOG_NOT_VISIBLE;

        //reinitialize the cursor
        adapter.reinitializeCursor();
    }

    //***********************************/
    //****  Delete Button Dialogue    *****/
    //***********************************/
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

}
