package com.androidchicken.medminder;


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
import android.text.InputType;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;


/**
 * A placeholder fragment containing a simple view.
 */
public class MMHomeFragment extends Fragment {

    private static final String TAG = "MainMMTakeDoseFragment";
    private static final int HALF_SECOND = 500;

    public  static final String sIsUIChangedTag      = "IS_UI_CHANGED";
    private static final String sDoseAmountTag       = "DOSE_AMOUNT_%d";
    public  static final String sSelectedPositionTag = "SELECTED_POSITION";

    public  static final int    sSELECTED_DIALOG_NOT_VISIBLE = -1;

    public static final int    sPaddingBetweenViews = 3;


    //**********************************************/
    /*          UI Widgets                         */
    //**********************************************/

    //These do not need to be saved during configuration change as the View is rebuild.
    // Thus these lists will be rebuilt in the process
    //ArrayList<Button>   mMedButtons = new ArrayList<>();
    //ArrayList<EditText> mMedEdits   = new ArrayList<>();

    //null indicates reconfigure for use when rebuilding the Dose views
    //private EditText mTimeInput;



    //**********************************************/
    /*          Member Variables                   */
    /*    These variables will need to survive     */
    /*          configuration change               */
    //**********************************************/

    private boolean isUIChanged   = false;
    private int mSelectedPosition = sSELECTED_DIALOG_NOT_VISIBLE;

    //**********************************************/
    /*          Static Methods                     */
    //**********************************************/

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



    }


    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        if (savedInstanceState != null){
            //the fragment is being restored so restore the person ID
             mSelectedPosition = savedInstanceState.getInt(sSelectedPositionTag);
            //The rest of the saved state in the bundle will be used later
            // to recreate the Dose views and the isUIChanged flag
        }

        //Initialize the DB if necessary
        try {
            //initialize the database
            MMDatabaseManager.getInstance(getActivity());
        }catch (Exception e) {
            Log.e(TAG,Log.getStackTraceString(e));
        }

        //Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_home, container, false);


        //Wire up the UI widgets so they can handle events later
        wireWidgets(v, savedInstanceState);
        initializeRecyclerView(v);
        initializeUI(v);


        //hide the soft keyboard
        MMUtilities utilities = MMUtilities.getInstance();
        utilities.hideSoftKeyboard(getActivity());



        //start the medButton animation
        startMedButtonBlink(v);

        //Set the changed UI flag based on whether we are recreating the View
        initializeChangedUI(v, savedInstanceState);

        return v;
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState){
        // Save custom values into the bundle

        //Save the isUIChanged flag
        savedInstanceState.putBoolean(sIsUIChangedTag, isUIChanged);

        //save the selected position in case we are in the middle of the dialog
        savedInstanceState.putInt(sSelectedPositionTag, mSelectedPosition);

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
            MMUtilities.getInstance().showStatus(getActivity(), R.string.reselect_dose);
            mSelectedPosition = sSELECTED_DIALOG_NOT_VISIBLE;
            //onSelectDoseDialog(mSelectedPosition);
        }
    }

    //**********************************************/
    /*   Initialization Methods                    */
    //**********************************************/


    private void wireWidgets(View v, Bundle savedInstanceState){

        //save Button
        Button saveButton = (Button) v.findViewById(R.id.homeSaveButton);
        saveButton.setText(R.string.save_label);
        //the order of images here is left, top, right, bottom
        //mSaveButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_collect, 0, 0);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getPersonID() == MMUtilities.ID_DOES_NOT_EXIST)return;

                MMUtilities.getInstance().showStatus(getActivity(), R.string.save_label);
                onSave();

                ((MMMainActivity) getActivity()).switchToHomeScreen();

            }
        });

        int medButtonPosition = 0;
        LinearLayout medButtonLayout = (LinearLayout)v.findViewById(R.id.medButtonLayout);
        EditText timeInput = (EditText) medButtonLayout.getChildAt(medButtonPosition);
        //do not need to rebuild the views if they already exist
        if ((timeInput == null) &&
            (getPersonID() != MMUtilities.ID_DOES_NOT_EXIST) )  {

            MMPerson person = getPerson();

            if (person != null) {

                ArrayList<MMMedication> medications = person.getMedications();

                int last = medications.size();
                if (last > 0) {
                    //convert pixels to dp
                    int paddingBetweenBtsDP = sPaddingBetweenViews; //padding between buttons

                    addDateTimeFieldsToView(v,  paddingBetweenBtsDP);

                    int position = 0;

                    while (position < last) {
                        MMMedication medication = medications.get(position);
                        if ((medication != null) && (medication.isCurrentlyTaken())) {
                            addMedButtonToView(v,
                                               savedInstanceState,
                                               position,
                                               medication,
                                               paddingBetweenBtsDP);
                        }
                        position++;
                    }
                }
            }
        }
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
        boolean reverseLayout = false;
        RecyclerView.LayoutManager mLayoutManager  = new LinearLayoutManager(getActivity(),
                                                                       LinearLayoutManager.VERTICAL,
                                                                       reverseLayout);

        recyclerView.setLayoutManager(mLayoutManager);

        //4) Get the list of ConcurrentDose Instances from the ConcurrentDoseManager

        MMConcurrentDoseManager concurrentDoseManager = MMConcurrentDoseManager.getInstance();
        //      get  list of concurrentDoses
        long earliestDate = MMSettings.getInstance().getHistoryDate((MMMainActivity)getActivity());
        // TODO: 6/1/2017 get rid of debug statements
        String earliestString = MMUtilities.getInstance().getDateString(earliestDate);
        Cursor concurrentDoseCursor = concurrentDoseManager.getAllConcurrentDosesCursor(
                                                ((MMMainActivity)getActivity()).getPatientID(),
                                                  earliestDate);

        //5) Use the data to Create and set out concurrentDose Adapter
        //     even though we're giving the Adapter the list,
        //     Adapter uses the ConcurrentDoseManager to maintain the list and
        //     the items in the list.
        int numbMeds = 0; //initialize to person not yet existing

        if (getPersonID() != MMUtilities.ID_DOES_NOT_EXIST){
            MMPerson person = getPerson();
            if (person != null) {
                numbMeds = person.getMedications().size();
            }
        }
        MMConcurrentDoseCursorAdapter adapter =
                new MMConcurrentDoseCursorAdapter(getActivity(),
                                                  ((MMMainActivity)getActivity()).getPatientID(),
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

    private void initializeUI(View v){
        //determine if a person is yet associated with the fragment
        if (getPersonID() != MMUtilities.ID_DOES_NOT_EXIST){
            //if there is a person corresponding to the patientID, put the name up on the screen
            MMPerson person = getPerson();

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

        //Time for current dose is set in addTimeFieldsToView() as it is created,
        //  so it doesn't need to be initialized here

        setUISaved(v);
    }

    private void initializeChangedUI(View v, Bundle savedInstanceState){
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
                MMUtilities.getInstance().errorHandler(getActivity(), R.string.reselect_dose);
            }
        }
    }


    //**********************************************/
    /*       Convenience Methods                   */
    //**********************************************/
    private long     getPersonID(){
        return ((MMMainActivity)getActivity()).getPatientID();
    }

    private MMPerson getPerson()   {return ((MMMainActivity)getActivity()).getPerson();}

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
        MMPerson person = MMPersonManager.getInstance().getPerson(personID);
        return getMedicationFromPerson(person, position);
    }

    private MMMedication getMedicationFromPerson(MMPerson person, int position){
        return person.getMedications().get(position);
    }


    private int getNumberOfMedications(){
        return getPerson().getMedications().size();
    }


    //**********************************************/
    /*   Initialization of Views                   */
    //**********************************************/



    private void   addDateTimeFieldsToView(View v,  int sizeInDp){

        int padding = MMUtilities.getInstance().convertPixelsToDp(getActivity(), sizeInDp);

        LinearLayout layout = (LinearLayout) v.findViewById(R.id.medDoseInputLayout);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                0,//width
                ViewGroup.LayoutParams.WRAP_CONTENT);//height
        lp.weight = 4f;
        //lp.gravity = Gravity.CENTER;//set below too
        lp.setMarginEnd(padding);

        EditText timeInput = new EditText(getActivity());

        timeInput.setInputType(InputType.TYPE_CLASS_TEXT);
        timeInput.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
        timeInput.setLayoutParams(lp);
        timeInput.setPadding(0,0,padding,0);
        timeInput.setGravity(Gravity.CENTER);
        timeInput.setTextColor      (ContextCompat.getColor(getActivity(),R.color.colorTextBlack));
        timeInput.setBackgroundColor(ContextCompat.getColor(getActivity(),R.color.colorInputBackground));
        timeInput.setFocusable(true);


        //Time input for this dose
        //There is no label for this field
        timeInput.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // TODO: 5/29/2017 Allow user to change time of dose
                return false;
            }
        });

        String timeString;

        timeString = MMUtilities.getInstance().getTimeString((MMMainActivity)getActivity());

        timeInput.setText(timeString);

        layout.addView(timeInput);

    }

    private Button addMedButtonToView(View         v,
                                      Bundle       savedInstanceState,
                                      int          viewNumber,
                                      MMMedication medication,
                                      int          sizeInDp){
        Button   medButton;
        EditText edtView;
        String   buttonText = medication.getMedicationNickname().toString();

        int padding = MMUtilities.getInstance().convertPixelsToDp(getActivity(), sizeInDp);

        //
        //Add the button to the button layout
        //
        LinearLayout medButtonsLayout = getMedButtonsLayout(v);

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
        medButtonsLayout.addView(medButton);

        addMedButtonListener(medButton);

        //save the pointer to the button
        // mMedButtons.add(medButton);


        //
        //add EditText to the dose layout
        //
        LinearLayout medDoseLayout = (LinearLayout) v.findViewById(R.id.medDoseInputLayout);
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
            String etTag = String.format(Locale.getDefault(), sDoseAmountTag, viewNumber);
            amountString = savedInstanceState.getString(etTag);
        }
        edtView.setText(amountString);

        medDoseLayout.addView(edtView);

        return medButton;
    }



    private void   addMedButtonListener(Button medButton){

        if (medButton == null) return;

        //add the listeners to the button
        medButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                LinearLayout medButtonsLayout = getMedButtonsLayout(null);

                //int last = mMedButtons.size();

                int position = 0;
                int last = getPerson().getMedications().size() + position;
                Button medButton;
                while (position < last){
                    medButton = (Button)medButtonsLayout.getChildAt(position);
                    //medButton = mMedButtons.get(position);
                    if (medButton == v){

                        //stop the blinking
                        medButton.clearAnimation();

                        //Show the amount taken
                        showDose(position);

                        //indicate the UI has changed
                        setUIChanged();
                        return;
                    }
                    position++;
                }
                MMUtilities.getInstance().errorHandler(getActivity(), R.string.patient_no_med_but);

            }
        });
        medButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                MMUtilities.getInstance().showStatus(getActivity(), R.string.person_med_long_click);
                ///Determine which button has been pressed

                //parameter is flag to ask fragment for the layout
                LinearLayout medButtonsLayout = getMedButtonsLayout(null);

                //int last = mMedButtons.size();

                int position = 0;
                int last = getPerson().getMedications().size() + position;

                Button medButton;
                while (position < last){
                    medButton = (Button)medButtonsLayout.getChildAt(position);

                    if (medButton == v){

                        ((MMMainActivity)getActivity()).switchToMedicationScreen(position, MMMainActivity.sHomeTag);

                        return true;//The long click has been consumed
                    }
                    position++;
                }

                MMUtilities.getInstance().showStatus(getActivity(), R.string.couldnt_find_medication);

                return true;//even so, consume the long click
            }
        });

    }


    private void   startMedButtonBlink(View v){
        //if (true)return;
        if (getPersonID() == MMUtilities.ID_DOES_NOT_EXIST)return;


        MMPerson person = getPerson();
        if (person == null)return;

        LinearLayout medButtonsLayout = getMedButtonsLayout(v);
        if (medButtonsLayout == null)return;



        Calendar calendar = Calendar.getInstance();
        long currentTimeMinutesSinceMidnight =
                                        MMUtilities.getInstance().getMinutesFromCalendar(calendar);

        ArrayList<MMMedication> medications = person.getMedications();
        int lastMed = getNumberOfMedications();
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
                if (medication.getDoseStrategy() != MMMedication.sAS_NEEDED) {
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
                        // TODO: 6/7/2017 remove the context from the next call
                        lastTakenMinutes = MMUtilities.getInstance().getLastTakenMinutes((MMMainActivity) getActivity(), timeTaken);
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
                            //AND if the schedule time is prior to now
                            if (scheduleTimeDue <= currentTimeMinutesSinceMidnight) {
                                //it is time to take the dose, blink the proper button
                                medButton = (Button) medButtonsLayout.getChildAt(positionButton);
                                animateButton(medButton);
                                //no need to loop through the rest of the schedules
                                positionSched = lastSched;
                            }
                        }
                        positionSched++;
                    }//end while schedule loop

                }//!As Needed
                positionButton++;
            }//currently taken (must be currently taken to get a button)
            positionMed++;
        }//end while medication loop
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
        if (getPersonID() != MMUtilities.ID_DOES_NOT_EXIST){

            MMPerson person = getPerson();

            if (person != null) {

                //get the medication
                MMMedication medication = getMedicationFromPerson(person, position);

                if (medication != null) {
                    //and the dose
                    int doseAmt = medication.getDoseAmount();
                    //and the EditText field
                    LinearLayout medDoseLayout = getDoseLayout(null);
                    //                                                      +1 is to skip time
                    EditText medField = (EditText)medDoseLayout.getChildAt(position+1);

                    if (medField != null) {
                        //show the user
                        medField.setText(String.valueOf(doseAmt));
                    }
                }
            }
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

        MMUtilities utilities = MMUtilities.getInstance();
        utilities.enableButton(getActivity(), personSaveButton, isEnabled);
    }

    //******************************************************************/
    //*********  Views, RecyclerView / Adapter related Methods  ********/
    //******************************************************************/
    private LinearLayout getMedButtonsLayout(View homeFragment){
        if (homeFragment == null) {
            homeFragment = getView();
            if (homeFragment == null) return null;
        }
        return (LinearLayout)homeFragment.findViewById(R.id.medButtonLayout);

    }

    private LinearLayout getDoseLayout(View homeFragment){
        if (homeFragment == null) {
            homeFragment = getView();
            if (homeFragment == null) return null;
        }
        return (LinearLayout) homeFragment.findViewById(R.id.medDoseInputLayout);
    }

    private EditText getTimeInput(View v){
        if (v == null){
            v = getView();
            if (v == null)return null;
        }
        LinearLayout layout = (LinearLayout) v.findViewById(R.id.medDoseInputLayout);
        return (EditText) layout.getChildAt(0);
    }

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
        if (getPersonID() == MMUtilities.ID_DOES_NOT_EXIST)return MMDatabaseManager.sDB_ERROR_CODE;

        MMPerson person = getPerson();

        if (person == null) return MMDatabaseManager.sDB_ERROR_CODE;

        EditText timeInput = getTimeInput(null);

        CharSequence timeString = timeInput.getText();
        if (timeString == null)timeString = "";

        // TODO: 5/27/2017 Need to do some error checking on the timeString
        long milliSeconds = MMUtilities.getInstance().convertStringToMilliSinceMidnightToday(
                                                                    (MMMainActivity)getActivity(),
                                                                    timeString.toString());

        // TODO: 6/11/2017 get rid of debug timeStrings
        String debugTimeString =
               MMUtilities.getInstance().getTimeString((MMMainActivity)getActivity(), milliSeconds);
        String debugDateString =
                MMUtilities.getInstance().getDateString(milliSeconds);

        MMConcurrentDose concurrentDoses = new MMConcurrentDose(getPersonID(), milliSeconds);
        ArrayList<MMDose> doses = concurrentDoses.getDoses();

        ArrayList<MMMedication> medications = person.getMedications();

        int last = medications.size();
        MMMedication medication;
        int positionMed = 0;
        //only have buttons for active meds
        int positionButton = 0;
        LinearLayout medDoseLayout = getDoseLayout(null); //parameter is for initialization view
        EditText doseView;
        int amtTaken;
        String amtTakenString;
        while (positionMed < last) {
            medication = medications.get(positionMed);
            if (medication.isCurrentlyTaken()) {//There are no buttons and no views for old meds
                try {
                    doseView = (EditText) medDoseLayout.getChildAt(positionButton + 1);
                    amtTakenString = doseView.getText().toString().trim();
                    if (!amtTakenString.isEmpty()) {
                        amtTaken = Integer.valueOf(amtTakenString);
                    } else {
                        amtTaken = 0;
                    }
                } catch (NullPointerException e){
                    amtTaken = 0;
                }

                MMDose dose = new MMDose(   medications.get(positionMed).getMedicationID(),
                                            getPersonID(),
                                            concurrentDoses.getConcurrentDoseID(),
                                            positionMed,
                                            milliSeconds,
                                            amtTaken);
                doses.add(dose);

                //for each medAlert for this medication:
                MMMedicationAlertManager medAlertManager = MMMedicationAlertManager.getInstance();
                ArrayList<MMMedicationAlert> medAlerts = medAlertManager.
                                getMedicationAlerts(getPersonID(), medication.getMedicationID());
                int lastMedAlert = medAlerts.size();
                int positionMedAlert = 0;
                MMMedicationAlert medAlert;
                while (positionMedAlert < lastMedAlert) {
                    medAlert = medAlerts.get(positionMedAlert);
                    long medAlertID = medAlert.getMedicationAlertID();

                    MMUtilities.getInstance().createAlertAlarm(getActivity(), medAlertID);
                    positionMedAlert++;
                }
                positionButton++;
            }
            positionMed++;
        }

        MMConcurrentDoseManager concurrentDoseManager = MMConcurrentDoseManager.getInstance();
        long cDoseID = concurrentDoseManager.add(concurrentDoses);

        reinitializeCursor(getPersonID());
        /*
        */
        MMConcurrentDoseCursorAdapter adapter = getAdapter(getView());
        last = adapter.getItemCount();
        adapter.notifyItemChanged(last);

        adapter.notifyDataSetChanged();


        setUISaved();
        return cDoseID;
    }

    public void onExit(){
        MMConcurrentDoseCursorAdapter adapter = getAdapter(getView());
        adapter.closeCursor();
    }


    //*********************************************************/
    //      Utility Functions used in handling events         //
    //*********************************************************/
    //called from onClick(), executed when a concurrent dose is selected from list
    private void onSelect(int position){
        mSelectedPosition = position;

        //allow user to change the dose amount and date, then save the changes
        onSelectDoseDialog();

    }

    //*********************************************/
    //****  Select ConcurrentDose Dialogue    *****/
    //*********************************************/
    //Build and display the alert dialog
    private void onSelectDoseDialog(){
        //Get the concurrentDose to be updated
        MMConcurrentDoseCursorAdapter adapter   = getAdapter(getView());
        MMConcurrentDose selectedConcurrentDose = adapter.getConcurrentDoseAt(mSelectedPosition);
        //Get the Dose Amounts already recorded for this concurrentDose
        ArrayList<MMDose> doses = selectedConcurrentDose.getDoses();


        //
        //Build the (Dialog) layout and it's contained views
        // that define the ConcurrentDose and its contained Doses
        //
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View v = inflater.inflate(R.layout.list_row_dose_history_horz, null);
        LinearLayout layout = (LinearLayout) v.findViewById(R.id.doseHistoryLine);


        EditText doseDate = (EditText) v.findViewById(R.id.doseDateLabel);
        doseDate.setText(MMUtilities.getInstance().getDateString(selectedConcurrentDose.getStartTime()));

        EditText doseTime = (EditText) v.findViewById(R.id.doseTimeInput);
        doseTime.setText(MMUtilities.getInstance().
                getTimeString((MMMainActivity)getActivity(), selectedConcurrentDose.getStartTime()));




        //Build EditText views that will allow the user to input dose amounts

        android.support.v7.widget.AppCompatEditText edtView;
        int sizeInDp = 2;
        int padding = MMUtilities.getInstance().convertPixelsToDp(getActivity(), sizeInDp);

        //Create an EditText for each dose in the original
        int last = doses.size();
        int dosePosition = 0;
        MMDose dose;
        int    amount;

        while (dosePosition < last){
            //get the dose amount that is already recorded for this medication
            dose = doses.get(dosePosition);
            amount = dose.getAmountTaken();

            //actually create the EditText view in the utility
            edtView = MMUtilities.getInstance().createDoseEditText(getActivity(), padding);

            //show the amount of this dose in the View
            edtView.setText(String.valueOf(amount));

            //Add the EditText view to the layout
            layout.addView(edtView);
            dosePosition++;
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
                        MMUtilities.getInstance().showStatus(getActivity(), R.string.pressed_cancel);
                        mSelectedPosition = sSELECTED_DIALOG_NOT_VISIBLE; //set flag for dialog gone
                    }
                })
                .show();
    }

    //called when the user presses OK on Dialog
    private void onSaveSelected(DialogInterface dialog, int which){
        //This routine called from the POSITIVE button of the dialog that
        // invites the user to update the Dose Amounts
        //dialog is the AlertDialog built in onSelectDoseDialog()
        //which is a constant on DialogInterface
        //      = BUTTON_POSITIVE or
        //      = BUTTON_NEGATIVE or
        //      = BUTTON_NEUTRAL

        //// need selectedConcurrentDose
        MMConcurrentDoseCursorAdapter adapter   = getAdapter(getView());
        MMConcurrentDose selectedConcurrentDose = adapter.getConcurrentDoseAt(mSelectedPosition);
        //Get the Dose Amounts already recorded for this concurrentDose
        ArrayList<MMDose> doses = selectedConcurrentDose.getDoses();

        //Get the pointers to the views in the Dialog
        LinearLayout layout =
                (LinearLayout) ((AlertDialog) dialog).findViewById(R.id.doseHistoryLine);






        //get the amounts from the dialog EditText views, and
        // store them in the Doses in the ConcurrentDose instance

        //set up the parameters for the While loop
        int dosePosition = 0;
        EditText medicationDoseInput;
        MMDose   dose;
        int      amt;
        String   amtString;
        MMDoseManager doseManager = MMDoseManager.getInstance();



        //Update the doses from the concurrent dose with the amounts from the UI
        int lastDose = 0;
        if (doses != null){
            lastDose = doses.size();
            dosePosition = 0;
            while (dosePosition < lastDose){
                //get the dose from  the concurrent dose
                dose = doses.get(dosePosition);

                //update the dose with the new amount
                medicationDoseInput = (EditText) layout.getChildAt(dosePosition+2);
                amtString = medicationDoseInput.getText().toString().trim();

                amt = 0;
                if (!amtString.isEmpty()){
                    amt = Integer.valueOf(amtString);
                }
                dose.setAmountTaken(amt);
                dosePosition++;
            }
        }











        //Now update the time in the concurrent dose and each of the doses
        EditText doseDate = (EditText) layout.findViewById(R.id.doseDateLabel);
        EditText doseTime = (EditText) layout.findViewById(R.id.doseTimeInput);

        CharSequence doseDateString = doseDate.getText();
        CharSequence doseTimeString = doseTime.getText();

        boolean isTimeFlag = true;
        Date dosageTime = MMUtilities.getInstance().convertStringToTimeDate(
                                                                    (MMMainActivity) getActivity(),
                                                                    doseTimeString.toString(),
                                                                    isTimeFlag);
        isTimeFlag = false;
        Date dosageDate = MMUtilities.getInstance().convertStringToTimeDate(
                                                                    (MMMainActivity) getActivity(),
                                                                    doseDateString.toString(),
                                                                    isTimeFlag);


        //calculate the proper time
        long dateTimeMilliseconds;
        long timeMilliseconds;
        long dateMilliseconds;

        if ((dosageDate != null) && (dosageTime != null)) {
            dateMilliseconds = dosageDate.getTime();
            timeMilliseconds = dosageTime.getTime();

            dateTimeMilliseconds = dateMilliseconds + timeMilliseconds;
            dateTimeMilliseconds = MMUtilities.getInstance().convertLocaltoGMT(dateTimeMilliseconds);
        } else {
            dateTimeMilliseconds = selectedConcurrentDose.getStartTime();
        }

        //set the new time in the ConcurrentDose
        selectedConcurrentDose.setStartTime(dateTimeMilliseconds);



        //Update the concurrent Dose and it's contained doses in the DB
        boolean addToDBToo = true;
        MMConcurrentDoseManager.getInstance().addConcurrentDose(selectedConcurrentDose, addToDBToo);


        //reset the selected position so we'll know the dialog is finished
        mSelectedPosition = sSELECTED_DIALOG_NOT_VISIBLE;

        //reinitialize the cursor
        adapter.reinitializeCursor();
    }

    //***********************************/
    //****  Delete Button Dialogue    *****/
    //***********************************/


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
