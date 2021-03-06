package com.androidchicken.medminder;


import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
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
import java.util.Locale;


/**
 * The main fragment for MedMinder. Shows current dose, and dosage history
 */
public class MMHomeFragment extends Fragment {

    private static final String TAG = "MainMMTakeDoseFragment";
    private static final int HALF_SECOND = 500;

    public  static final String sIsUIChangedTag      = "IS_UI_CHANGED";
    private static final String sDoseAmountTag       = "DOSE_AMOUNT_%d";
    public  static final String sSelectedPositionTag = "SELECTED_POSITION";

    private static final String sDosesSavedTag       = "DOSES_SAVED";


    public  static final int    sSELECTED_DIALOG_NOT_VISIBLE = -1;

    public static final int    sPaddingBetweenViews = 3;


    //**********************************************/
    /*          UI Widgets                         */
    //**********************************************/




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
    public View onCreateView(@NonNull LayoutInflater inflater,
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
        //Set the changed UI flag based on whether we are recreating the View
        initializeChangedUI(v, savedInstanceState);

        MMMainActivity activity = (MMMainActivity) getActivity();
        if (activity == null) return v;

        //hide the soft keyboard
        MMUtilities utilities = MMUtilities.getInstance();
        utilities.hideSoftKeyboard(activity);
        activity.handleFabVisibility();

        //start the medButton animation
        startMedButtonBlink(v);

        return v;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle savedInstanceState){
        // Save custom values into the bundle

        //Save the isUIChanged flag
        savedInstanceState.putBoolean(sIsUIChangedTag, isUIChanged);

        //save the selected position in case we are in the middle of the dialog
        savedInstanceState.putInt(sSelectedPositionTag, mSelectedPosition);

        //save the doses in the current line
        saveDose(savedInstanceState);

        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onResume(){
        super.onResume();

        MMMainActivity activity = (MMMainActivity)getActivity();
        if (activity == null)return;

        //set the title bar subtitle
        activity.setMMSubtitle(R.string.title_home);

        //Set the FAB visible if user wants it
        activity.handleFabVisibility();

        if (mSelectedPosition != sSELECTED_DIALOG_NOT_VISIBLE){
            //put the dialog back up
            MMUtilities.getInstance().showStatus(getActivity(), R.string.reselect_dose);
            mSelectedPosition = sSELECTED_DIALOG_NOT_VISIBLE;
            //onSelectDoseDialog(mSelectedPosition);
        }

        //If the user hasn't entered any dose data, update the current time
        if (!haveDose()){
            setCurrentTime();
        }


    }

    //**********************************************/
    /*   Initialization Methods                    */
    //**********************************************/


    private void wireWidgets(View v, Bundle savedInstanceState){
        final MMMainActivity activity = (MMMainActivity)getActivity();
        if (activity == null)return;

        //Set up for long press on patient nickname
        TextView patientNick = v.findViewById(R.id.patientNickNameLabel);
        if (patientNick == null)return;
        patientNick.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                ((MMMainActivity)getActivity()).switchToPersonScreen();
                return true;
            }
        });

        //save Button
        Button saveButton =  v.findViewById(R.id.homeSaveButton);
        saveButton.setText(R.string.save_label);
        //the order of images here is left, top, right, bottom
        //mSaveButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_collect, 0, 0);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getPersonID() == MMUtilities.ID_DOES_NOT_EXIST)return;

                MMUtilities.getInstance().showStatus(getActivity(), R.string.save_label);
                onSave();

                activity.switchToHomeScreen();

            }
        });

        MMPerson person = getPerson();

        //Can't rebuild views without a patient
        if (person == null) return;

        EditText timeInput = getTimeInputView(v);
        //do not need to rebuild the views if they already exist or if the patient doesn't
        if ((timeInput != null) || (getPersonID() == MMUtilities.ID_DOES_NOT_EXIST) ) return;

        boolean currentOnly = MMSettings.getInstance().showOnlyCurrentMeds(activity);
        ArrayList<MMMedication> medications = person.getMedications(currentOnly);

        int last = medications.size();

        //nothing to build if no medications
        if (last <= 0) return;

        //convert pixels to dp
        int paddingBetweenBtsDP = sPaddingBetweenViews; //padding between buttons

        //FINALLY build the views
        addDateTimeFieldsToView(v,  paddingBetweenBtsDP);

        int position = 0;

        //add a button for each medication
        while (position < last) {
            MMMedication medication = medications.get(position);
            //ignore the lint comment that once currentOnly is established true,
            // you don't have to include it in the condition.
            // Leave it in so that human programmers can better
            // understand what is going on.
            if ((medication != null) &&
                (!currentOnly || (currentOnly && medication.isCurrentlyTaken()))) {
                addMedButtonToView(v,
                                   savedInstanceState,
                                   position,
                                   medication,
                                   paddingBetweenBtsDP);
            }
            position++;
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

        MMMainActivity activity = (MMMainActivity)getActivity();
        if (activity == null)return;


        //2) find and remember the RecyclerView
        RecyclerView recyclerView = getRecyclerView(v);

        //3) create and assign a layout manager to the recycler view

        RecyclerView.LayoutManager mLayoutManager  = new LinearLayoutManager(activity,
                                                                       LinearLayoutManager.VERTICAL,
                                                                       false);

        recyclerView.setLayoutManager(mLayoutManager);

        //4) Get the list of ConcurrentDose Instances from the ConcurrentDoseManager

        MMConcurrentDoseManager concurrentDoseManager = MMConcurrentDoseManager.getInstance();
        //      get  list of concurrentDoses
        MMSettings settings = MMSettings.getInstance();
        long earliestDate = settings.getHistoryDate(activity);

        boolean isTwoWeeks = settings.showOnlyTwoWeeks(activity);
        if (isTwoWeeks){
            //calculate the milliseconds date two weeks prior to the current date
            earliestDate = MMUtilitiesTime.getTwoWeeksAgo();
        }


        Cursor concurrentDoseCursor =
                concurrentDoseManager.getAllConcurrentDosesCursor(getPersonID(), earliestDate);

        //5) Use the data to Create and set out concurrentDose Adapter
        //     even though we're giving the Adapter the list,
        //     Adapter uses the ConcurrentDoseManager to maintain the list and
        //     the items in the list.
        int numbMeds = 0; //initialize to person not yet existing

        boolean currentOnly = MMSettings.getInstance().showOnlyCurrentMeds(activity);
        if (getPersonID() != MMUtilities.ID_DOES_NOT_EXIST){
            MMPerson person = getPerson();
            if (person != null) {
                numbMeds = person.getMedications(currentOnly).size();
            }
        }
        MMConcurrentDoseCursorAdapter adapter = new MMConcurrentDoseCursorAdapter(activity,
                                                                              getPersonID(),
                                                                              numbMeds,
                                                                              concurrentDoseCursor);
        recyclerView.setAdapter(adapter);

        //6) create and set the itemAnimator
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        //7) create and add the item decorator
        recyclerView.addItemDecoration(new DividerItemDecoration(activity,
                                                                 DividerItemDecoration.VERTICAL));
 /*
           recyclerView.addItemDecoration(new DividerItemDecoration(
                getActivity(),
                LinearLayoutManager.VERTICAL));
*/

        //8) add event listeners to the recycler view
        recyclerView.addOnItemTouchListener(
            new RecyclerTouchListener(activity, recyclerView, new ClickListener() {

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
        MMMainActivity activity = (MMMainActivity)getActivity();
        if (activity == null)return;

        //determine if a person is yet associated with the fragment
        if (getPersonID() != MMUtilities.ID_DOES_NOT_EXIST){
            //if there is a person corresponding to the patientID, put the name up on the screen
            MMPerson person = getPerson();

            if (person != null) {
                TextView patientNickName =  v.findViewById(R.id.patientNickNameLabel);
                if (patientNickName != null) {
                    patientNickName.setText(person.getNickname().toString().trim());

                    if (person.isCurrentlyExists()) {
                        v.setBackgroundColor(ContextCompat.
                                getColor(activity, R.color.colorScreenBackground));
                    } else {
                        v.setBackgroundColor(ContextCompat.
                                getColor(activity, R.color.colorScreenDeletedBackground));
                    }
                }
            }
        } else {
            v.setBackgroundColor(ContextCompat.
                    getColor(activity, R.color.colorScreenDeletedBackground));
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

            //restore any amounts
            if (!restoreDose(savedInstanceState, v)){
                //if any current doses are non-zero, update time to the current time
                setCurrentTime();
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

        Button personSaveButton = v.findViewById(R.id.homeSaveButton);

        MMUtilities utilities = MMUtilities.getInstance();
        utilities.enableButton(getActivity(), personSaveButton, isEnabled);
    }

    //**********************************************/
    /*       Convenience Methods                   */
    //**********************************************/
    private long     getPersonID(){
        MMMainActivity activity = (MMMainActivity)getActivity();
        if (activity != null) return activity.getPatientID();

        return MMUtilities.ID_DOES_NOT_EXIST;
    }

    private MMPerson getPerson() {
        MMMainActivity activity = (MMMainActivity) getActivity();
        if (activity != null) return activity.getPerson();

        return null;
    }


    //**********************************************/
    /*   Get object instances                      */
    //**********************************************/
    private long getMostRecentDoseTimeGMT(View v, MMMedication medication){
        MMDose dose = getMostRecentDose(v, medication);
        if (dose == null)return 0;

        return dose.getTimeTaken();
    }

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

    private MMMedication getMedicationFromPerson(MMPerson person, int position){
        MMMainActivity activity = (MMMainActivity)getActivity();
        if (activity == null)return null;

        boolean currentOnly = MMSettings.getInstance().showOnlyCurrentMeds(activity);
        return person.getMedications(currentOnly).get(position);
    }


    private int getNumberOfMedications(){
        MMMainActivity activity = (MMMainActivity)getActivity();
        if (activity == null) return 0;

        boolean currentOnly = MMSettings.getInstance().showOnlyCurrentMeds(activity);

        MMPerson person = getPerson();
        if (person == null)return 0;
        return person.getMedications(currentOnly).size();
    }


    //**********************************************/
    /*   Initialization of Views                   */
    //**********************************************/
    private void addDateTimeFieldsToView(View v,  int sizeInDp){

        int padding = MMUtilities.getInstance().convertPixelsToDp(getActivity(), sizeInDp);

        LinearLayout layout = getDoseLayout(v);
        if (layout == null)return;

        MMMainActivity activity = (MMMainActivity)getActivity();
        if (activity == null)return;

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                                                    0,//width
                                                    ViewGroup.LayoutParams.WRAP_CONTENT);//height
        lp.weight = 2f;
        //lp.gravity = Gravity.CENTER;//set below too
        lp.setMarginEnd(padding);

        final EditText timeInputView = new EditText(getActivity());

        timeInputView.setInputType(InputType.TYPE_CLASS_TEXT);
        timeInputView.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
        timeInputView.setLayoutParams(lp);
        timeInputView.setPadding(0,0,padding,0);
        timeInputView.setGravity(Gravity.CENTER);
        timeInputView.setTextColor      (ContextCompat.getColor(activity,R.color.colorTextBlack));
        timeInputView.setBackgroundColor(ContextCompat.getColor(activity,R.color.colorInputBackground));
        timeInputView.setFocusable(true);

        setCurrentTime(timeInputView);

        //Set the listeners for the time field
        //There is no label for this field
        timeInputView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setUIChanged();
            }
        });

        //reset time to current time on long press
        timeInputView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                //reset time to current time
                setCurrentTime(timeInputView);
                return false;
            }
        });


        layout.addView(timeInputView);

    }

    private void setCurrentTime(EditText timeInputView){
        String timeString;
        if (timeInputView == null)return;

        //put the local time up on the UI
        timeString = MMUtilitiesTime.getTimeString((MMMainActivity)getActivity());
        timeInputView.setText(timeString);

    }
    private void setCurrentTime(){

        LinearLayout medDoseLayout = getDoseLayout(null);
        if (medDoseLayout == null)return;

        EditText timeInputView =   (EditText)medDoseLayout.getChildAt(0);

        if (timeInputView != null)setCurrentTime(timeInputView);

    }

    private void addMedButtonToView(  View         v,
                                      Bundle       savedInstanceState,
                                      int          viewNumber,
                                      MMMedication medication,
                                      int          sizeInDp){

        final MMMainActivity activity = (MMMainActivity)getActivity();
        if (activity == null)return;

        String   buttonText = medication.getMedicationNickname().toString();

        int padding = MMUtilities.getInstance().convertPixelsToDp(activity, sizeInDp);

        //
        //Add the button to the button layout
        //
        LinearLayout medButtonsLayout = getMedButtonsLayout(v);
        if (medButtonsLayout == null)return;

        Button medButton = new Button(activity);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,//width
                ViewGroup.LayoutParams.WRAP_CONTENT);//height
        lp.weight = 1f;
        lp.setMarginEnd(padding);

        medButton.setLayoutParams(lp);
        medButton.setBackgroundColor(ContextCompat.getColor(activity,R.color.colorButton1Background));

        medButton.setPadding(0,0,padding,0);
        medButton.setText(buttonText);
        medButton.setTextColor(ContextCompat.getColor(activity,R.color.colorTextBlack));
        //I know this looks weird, but have to make both calls for this to work.
        // found in Stack Overflow https://stackoverflow.com/questions/26390303/android-overriding-button-minheight-programmatically
        medButton.setMinHeight(0);
        medButton.setMinimumHeight(0);

        boolean isMedCurrent = medication.isCurrentlyTaken();
        if (!isMedCurrent){
            medButton.setBackgroundColor(ContextCompat.getColor(activity, R.color.colorGray));
        }

        medButtonsLayout.addView(medButton);

        addMedButtonListener(medButton);

        //
        //add EditText to the dose layout
        //
        LinearLayout medDoseLayout = getDoseLayout(v);
        if (medDoseLayout == null)return;

        lp = new LinearLayout.LayoutParams( 0,//width
                                            ViewGroup.LayoutParams.WRAP_CONTENT);//height
        lp.weight = 1f;
        lp.setMarginEnd(padding);


        final EditText amountView = new EditText(activity);
        amountView.setFreezesText(true);
        amountView.setHint("0");
        amountView.setInputType(InputType.TYPE_CLASS_TEXT);
        amountView.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
        amountView.setLayoutParams(lp);
        amountView.setPadding(0,0,padding,0);
        amountView.setGravity(Gravity.CENTER);
        amountView.setTextColor      (ContextCompat.getColor(activity,R.color.colorTextBlack));
        amountView.setBackgroundColor(ContextCompat.getColor(activity,R.color.colorInputBackground));

        //If the user clicks in this view, enable the save button
         amountView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
             @Override
             public void onFocusChange(View view, boolean b) {
                 //regardless if the focus is in or out
                 setUIChanged();
                 amountView.setBackgroundColor(ContextCompat.getColor(activity,R.color.colorLightPink));
             }
         });


        String amountString;
        if (savedInstanceState == null){
            amountString = "0";
        } else {
            String etTag = String.format(Locale.getDefault(), sDoseAmountTag, viewNumber);
            amountString = savedInstanceState.getString(etTag);
        }
        amountView.setText(amountString);
        if (!isMedCurrent){
            amountView.setBackgroundColor(ContextCompat.getColor(activity, R.color.colorGray));
        }

        medDoseLayout.addView(amountView);
    }

    private void addMedButtonListener(Button medButton){

        //first define the listeners for the buttons
        View.OnClickListener medButtonListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                LinearLayout medButtonsLayout = getMedButtonsLayout(null);
                if (medButtonsLayout == null)return;

                //int last = mMedButtons.size();
                MMMainActivity activity = (MMMainActivity)getActivity();
                if (activity == null)return;

                boolean currentOnly = MMSettings.getInstance().showOnlyCurrentMeds(activity);

                int position = 0;
                MMPerson person = getPerson();
                if (person == null)return;

                int last = getPerson().getMedications(currentOnly).size() + position;
                Button medButton;
                while (position < last){
                    medButton = (Button)medButtonsLayout.getChildAt(position);

                    //medButton = mMedButtons.get(position);
                    if ((medButton != null) && (medButton == v)){

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
        };

        View.OnLongClickListener medButtonLongLilstener = new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                MMMainActivity activity = (MMMainActivity)getActivity();
                if (activity == null)return false;

                //parameter is flag to ask fragment for the layout
                LinearLayout medButtonsLayout = getMedButtonsLayout(null);
                if (medButtonsLayout == null)return false;

                MMUtilities.getInstance().showStatus(getActivity(), R.string.person_med_long_click);
                ///Determine which button has been pressed

                //int last = mMedButtons.size();
                 boolean currentOnly = MMSettings.getInstance().showOnlyCurrentMeds(activity);

                int position = 0;
                MMPerson person = getPerson();
                if (person == null)return false;//long click not consumed

                int last = person.getMedications(currentOnly).size() + position;

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
        };

        if (medButton == null) return;

        //add the listeners to the button
        medButton.setOnClickListener(medButtonListener);
        medButton.setOnLongClickListener(medButtonLongLilstener);

    }

    private void startMedButtonBlink(View v){
        MMMainActivity activity = (MMMainActivity)getActivity();
        if (activity == null)return;

        //if (true)return;
        if (getPersonID() == MMUtilities.ID_DOES_NOT_EXIST)return;

        MMPerson person = getPerson();
        if (person == null)return;


        boolean currentOnly = MMSettings.getInstance().showOnlyCurrentMeds(activity);
        ArrayList<MMMedication> medications = person.getMedications(currentOnly);
        int lastMed = getNumberOfMedications();
        //position within medications for this person
        //corresponds to the position of medButton for this medication
        int positionMed = 0;
        int positionButton = 0;

        //If the dose is overdue, blink the button
        while (positionMed < lastMed){
            //Get the most recent dose taken for this medication
            MMMedication medication = medications.get(positionMed);
            if (medication.isCurrentlyTaken()) {
                if (medication.getDoseStrategy() == MMMedication.sSET_SCHEDULE_FOR_MEDICATION) {
                    //There are only medButtons for medications that are currently taken

                    blinkScheduleMedButton(v, medication, positionButton);
                }else if (medication.getDoseStrategy() == MMMedication.sIN_X_HOURS){
                    blinkInXMedButton(v, medication, positionButton);
                }
                positionButton++;
            }//End currently taken (must be currently taken to get a button)
            positionMed++;
        }//end while medication loop
    }

    private void animateButton(Button medButton){
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

    private void blinkScheduleMedButton(View v, MMMedication medication, int positionButton){
        LinearLayout medButtonsLayout = getMedButtonsLayout(v);
        if (medButtonsLayout == null)return;

        //Get current time in MS
        long currentTimeMs    = MMUtilitiesTime.getTimeNow();

        //get the time the most recent dose was taken
        long mostRecentDoseTimeGMT = getMostRecentDoseTimeGMT(v, medication);

        //so we have the time the dose was taken. When is/was the next dose due?

        //The schedule of the medication gives us
        // how many minutes since midnight that the dose is due
        MMSchedule schedule;
        ArrayList<MMSchedule> schedules = medication.getSchedules();

        long schedTimeDueMs;

        int lastSched = schedules.size();
        int positionSched = 0;

        Button medButton;
        int localMinutesDue;
        while (positionSched < lastSched) {
            schedule = schedules.get(positionSched);

            //Minutes since Midnight
            localMinutesDue = schedule.getTimeDue();

            //Convert to a MS time
            schedTimeDueMs = MMUtilitiesTime.getCurrentMilli(localMinutesDue);


            if ((mostRecentDoseTimeGMT < schedTimeDueMs) &&
                    (schedTimeDueMs        < currentTimeMs)){

                //it is (past) time to take the dose, so blink the proper button
                medButton = (Button) medButtonsLayout.getChildAt(positionButton);
                animateButton(medButton);

                //no need to loop through the rest of the schedules
                positionSched = lastSched;
            }
            positionSched++;
        }//end while schedule loop
    }
    private void blinkInXMedButton     (View v, MMMedication medication, int positionButton){
        LinearLayout medButtonsLayout = getMedButtonsLayout(v);
        if (medButtonsLayout == null)return;

        //Get current time in MS
        long currentTimeMs    = MMUtilitiesTime.getTimeNow();

        //get the time the most recent dose was taken
        long mostRecentDoseTimeGMT = getMostRecentDoseTimeGMT(v, medication);

        //so we have the time the dose was taken. When is/was the next dose due?

        //The schedule of the medication gives us
        // how many minutes from now that the next dose is due

        ArrayList<MMSchedule> schedules = medication.getSchedules();
        if ((schedules == null) || (schedules.size() <= 0)) return;

        MMSchedule schedule = schedules.get(0);
        if (schedule == null)return;

        //Determine when the next dose is due
        int minutesUntilDue = schedule.getTimeDue();
        long milliDue = MMUtilitiesTime.convertMinutesToMs(minutesUntilDue);
        milliDue = milliDue + mostRecentDoseTimeGMT;

        if ((milliDue < currentTimeMs)){

            //it is (past) time to take the dose, so blink the proper button
            Button medButton = (Button) medButtonsLayout.getChildAt(positionButton);
            if (medButton != null) animateButton(medButton);

        }
    }

    //******************************************************************/
    //*********  Methods dealing with the current dose fields   ********/
    //******************************************************************/

    private void    showDose(int position){

        if (getPersonID() == MMUtilities.ID_DOES_NOT_EXIST)return;
        MMPerson person = getPerson();
        if (person == null) return;

        //get the medication
        //This only gets deleted medications if the settings say to show them
        MMMedication medication = getMedicationFromPerson(person, position);
        if (medication == null) return;

        LinearLayout medDoseLayout = getDoseLayout(null);
        if (medDoseLayout == null)return;

        //get the default dose from the medication
        int doseAmt = medication.getDoseAmount();
        //and show the user                                     +1 is to skip time
        EditText medField = (EditText)medDoseLayout.getChildAt(position+1);
        if (medField != null) setDose(medField, doseAmt);
    }

    private void    setDose(EditText medField, int doseAmt){
        MMMainActivity activity = (MMMainActivity)getActivity();
        if (activity == null)return;

        if (medField == null)return;

        //show the user
        medField.setText(String.valueOf(doseAmt));
        if (doseAmt > 0) {
            medField.setBackgroundColor(ContextCompat.
                    getColor(getActivity(), R.color.colorLightPink));
        }else {
            medField.setBackgroundColor(ContextCompat.
                    getColor(getActivity(), R.color.colorWhite));
        }
    }

    private void    saveDose   (Bundle savedInstanceState){
        MMMainActivity activity = (MMMainActivity)getActivity();
        if (activity == null)return;

        if (savedInstanceState == null)return;
        if (getPersonID() == MMUtilities.ID_DOES_NOT_EXIST)return;

        MMPerson person = getPerson();
        if (person == null) return;

        LinearLayout medDoseLayout = getDoseLayout(null);
        if (medDoseLayout == null)return;

        int position = 0;
        int last = getNumberOfMedications(); //Knows whether deleted meds are to be included or not

        int doseAmt;
        boolean haveDoses = haveDose();

        //save flag indicating whether there are any doses
        savedInstanceState.putBoolean(sDosesSavedTag, haveDoses);
        if (!haveDoses) return;

        //
        //there are doses saved, also save the amounts
        //

        position = 0; //reset the loop
        String tag;
        String doseAmtString;
        //First, see if there are any to save
        while (position < last) {

            //                                                      +1 is to skip time
            EditText medField = (EditText) medDoseLayout.getChildAt(position + 1);

            if (medField != null) {

                //save the amount
                tag = String.format(sDoseAmountTag, position);
                doseAmtString = medField.getText().toString().trim();
                if (doseAmtString.isEmpty()){
                    doseAmt = 0;
                } else {
                    doseAmt = Integer.valueOf(doseAmtString);
                }
                savedInstanceState.putInt(tag, doseAmt);
            }

            position++;
        }

    }

    //return code indicates whether any of the doses were non-zero
    private boolean restoreDose(Bundle savedInstanceState, View v){

        MMMainActivity activity = (MMMainActivity)getActivity();
        if (activity == null)return false;
        if (savedInstanceState == null)return false;
        if (getPersonID() == MMUtilities.ID_DOES_NOT_EXIST)return false;

        MMPerson person = getPerson();
        if (person == null) return false;

        if (!savedInstanceState.getBoolean(sDosesSavedTag))return false;
        //So there are saved doses in the savedInstanceState

        LinearLayout medDoseLayout = getDoseLayout(v);
        if (medDoseLayout == null)return false;

        int position = 0;
        int last = getNumberOfMedications(); //Knows whether deleted meds are to be included or not
        int doseAmt;
        String tag;

        boolean haveDoses = false;

        while (position < last) {

            //                                                      +1 is to skip time
            EditText medField = (EditText) medDoseLayout.getChildAt(position + 1);

            if (medField != null) {
                //save the amount
                tag = String.format(sDoseAmountTag, position);
                doseAmt = savedInstanceState.getInt(tag);
                //show the user
                setDose(medField, doseAmt);

                if (doseAmt > 0)haveDoses = true;
            }
            position++;
        }
        return haveDoses;
    }

    //return code indicates whether the user has entered any doses.
    private boolean haveDose   (){

        MMMainActivity activity = (MMMainActivity)getActivity();
        if (activity == null)return false;

        if (getPersonID() == MMUtilities.ID_DOES_NOT_EXIST)return false;

        MMPerson person = getPerson();
        if (person == null) return false;

        LinearLayout medDoseLayout = getDoseLayout(null);
        if (medDoseLayout == null)return false;

        int position = 0;
        int last = getNumberOfMedications(); //Knows whether deleted meds are to be included or not

        int doseAmt;
        String doseAmtString;

        //First, see if there are any to save
        while (position < last) {
            //                                                      +1 is to skip time
            EditText medField = (EditText) medDoseLayout.getChildAt(position + 1);

            if (medField != null) {
                doseAmtString = medField.getText().toString().trim();
                if (!doseAmtString.isEmpty()) {
                    doseAmt = Integer.valueOf(doseAmtString);
                    if (doseAmt > 0) {
                        return true; //return on the first non-zero dose
                    }
                }
            }
            position++;
        }
        return false; //no non-zero doses found
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

    private EditText getTimeInputView(View v){
        if (v == null){
            v = getView();
            if (v == null)return null;
        }
        LinearLayout layout = getDoseLayout(v);
        if (layout == null)return null;

        return (EditText) layout.getChildAt(0);
    }

    private EditText getDoseTimeInputView(View v){
        if (v == null)return null;

        return (EditText) v.findViewById(R.id.doseTimeInput);
    }
    private EditText getDoseDateInputView(View v){
       if (v == null)return null;
       return  (EditText) v.findViewById(R.id.doseDateLabel);
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
    private void onSave(){

        MMMainActivity activity = (MMMainActivity)getActivity();
        if (activity == null)return;

        //Creates in memory structure to save all the doses taken concurrently
        if (getPersonID() == MMUtilities.ID_DOES_NOT_EXIST)return ;

        MMPerson person = getPerson();
        if (person == null) return ;

        LinearLayout medDoseLayout = getDoseLayout(null); //parameter is for initialization view
        if (medDoseLayout == null)return ;

        //Get the time input by the user for this dose
        EditText timeInputView = getTimeInputView(null); //look up the UI view
        if (timeInputView == null)return;

        long timeTaken = MMUtilitiesTime.getTodayTime((MMMainActivity)getActivity(),timeInputView);


        //create the concurrent dose with the time from the UI

        MMConcurrentDose concurrentDoses = new MMConcurrentDose(getPersonID(), timeTaken);
        ArrayList<MMDose> doses = concurrentDoses.getDoses();//just an empty list

        boolean currentOnly = MMSettings.getInstance().showOnlyCurrentMeds(activity);
        ArrayList<MMMedication> medications = person.getMedications(currentOnly);

        int last = medications.size();
        MMMedication medication;
        int positionMed = 0;

        int positionButton = 0;

        EditText doseView;
        int amtTaken;
        String amtTakenString;
        while (positionMed < last) {
            medication = medications.get(positionMed);

            //Depending on Settings, there may be no buttons or views for non-current meds
            // But regardless, can only take current meds
            if (medication.isCurrentlyTaken()) {
                try {
                    //Plus one is to skip over time field
                    doseView = (EditText) medDoseLayout.getChildAt(positionButton + 1);
                    amtTaken = 0;
                    if (doseView != null) {
                        amtTakenString = doseView.getText().toString().trim();
                        if (!amtTakenString.isEmpty()) {
                            amtTaken = Integer.valueOf(amtTakenString);
                        }
                    }
                } catch (NullPointerException e){
                    amtTaken = 0;
                }
                //Save a dose object regardless of whether anything was actually taken.
                // As the user may change the amount later
                MMDose dose = new MMDose(   medications.get(positionMed).getMedicationID(),
                                            getPersonID(),
                                            concurrentDoses.getConcurrentDoseID(),
                                            positionMed,
                                            timeTaken,
                                            amtTaken);
                doses.add(dose);

                if (amtTaken > 0){
                    //If the user actually took some of the medication,

                    // set notification for next dose Based on Strategy
                    int strategy = medication.getDoseStrategy();
                    if (strategy == MMMedication.sSET_SCHEDULE_FOR_MEDICATION){
                        setScheduleNotification(medication);

                    } else if (strategy == MMMedication.sIN_X_HOURS){
                        setInXNotification(medication, timeTaken);
                    } // else strategy = as needed, do noting
                }


                positionButton++;
            }
            positionMed++;
        }

        MMConcurrentDoseManager concurrentDoseManager = MMConcurrentDoseManager.getInstance();
        long cDoseID = concurrentDoseManager.add(concurrentDoses);

        reinitializeCursor(getPersonID());

        MMConcurrentDoseCursorAdapter adapter = getAdapter(getView());
        last = adapter.getItemCount();
        adapter.notifyItemChanged(last);

        adapter.notifyDataSetChanged();

        setUISaved();
    }

    void setScheduleNotification(MMMedication medication){
        MMMainActivity activity = (MMMainActivity)getActivity();
        if (activity == null)return;

        //delete any existing notifications
        MMUtilities.getInstance().cancelNotificationAlarms(
                                                    activity,
                                                    MMAlarmReceiver.schedNotifAlarmType,
                                                    MMAlarmReceiver.scheduleNotificationID,
                                                    medication.getMedicationID(),
                                                    medication.getMedicationNickname().toString());

        //calculate the time the notification is due

        //of all the schedules on this medication,
        // find one that is > than now, but the least greater
        ArrayList<MMSchedule> schedules = medication.getSchedules();
        int schedulePosition          = 0;
        int scheduleLast              = schedules.size();
        MMSchedule schedule           = null;

        int  currentMinutes           = MMUtilitiesTime.getMinutesSinceMidnight();
        int  scheduleTime             = -1;
        int  foundScheduleTime        = -1;


        while (schedulePosition < scheduleLast){
            schedule = schedules.get(schedulePosition);
            scheduleTime = schedule.getTimeDue();

            //is schedule greater than now?
            if (scheduleTime > currentMinutes){
                if ( (foundScheduleTime > scheduleTime) ||(foundScheduleTime < 0) ) {
                    foundScheduleTime = scheduleTime;
                }
            }

            schedulePosition++;
        }

        // set the notification for the time found
        if (foundScheduleTime >= 0){
            MMUtilities.getInstance().createScheduleNotification(
                                                    activity,
                                                    foundScheduleTime,
                                                    medication.getMedicationID(),
                                                    medication.getMedicationNickname().toString());
        }


    }

    void setInXNotification(MMMedication medication, long timeTaken){
        MMMainActivity activity = (MMMainActivity)getActivity();
        if (activity == null)return;

        //delete any existing notifications
        MMUtilities.getInstance().cancelNotificationAlarms(
                                                    activity,
                                                    MMAlarmReceiver.inXNotifAlarmType,
                                                    MMAlarmReceiver.inXNotificationID,
                                                    medication.getMedicationID(),
                                                    medication.getMedicationNickname().toString());

        //calculate the time the notification is due

        //of all the schedules on this medication,
        // find one that is > than now, but the least greater
        ArrayList<MMSchedule> schedules = medication.getSchedules();

        MMSchedule schedule  = schedules.get(0);
        if (schedule == null)return;

        int scheduleTime  = schedule.getTimeDue();

        if (scheduleTime >= 0){

            long todayDoseTimeMS      = timeTaken - MMUtilitiesTime.getMidnightInMS();
            int  todayDoseTimeMinutes = (int)MMUtilitiesTime.convertMsToMin(todayDoseTimeMS);
            int  notificationTime     = todayDoseTimeMinutes + scheduleTime;

            MMUtilities.getInstance().createInXNotification(
                                                    activity,
                                                    notificationTime,
                                                    medication.getMedicationID(),
                                                    medication.getMedicationNickname().toString());
        }


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

        // TODO: 12/21/2017 this might be helpful for changing a value in a dose
        //Get the view holder for a given position
        //holder = myRecyclerView.findViewHolderForAdapterPosition(pos);
        //returns null if the viewholder has been recycled

        //https://stackoverflow.com/questions/33784369/recyclerview-get-view-at-particular-position

        //allow user to change the dose amount and date, then save the changes
        //onSelectDoseDialog();
        MMMainActivity activity = (MMMainActivity)getActivity();
        if (activity == null)return;

        //set flag so we will know that it is the user changing the med amounts
        MMSettings.getInstance().setUserInput(activity, true);
        //remember the selected Position
        MMSettings.getInstance().setSelectedPosition(activity, mSelectedPosition);

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
        MMMainActivity activity = (MMMainActivity)getActivity();
        if (activity == null)return;

        LayoutInflater inflater = activity.getLayoutInflater();
        View v = inflater.inflate(R.layout.list_row_dose_history_horz_input, null);
        LinearLayout layout = v.findViewById(R.id.doseHistoryLine);


        long dateMilli      = selectedConcurrentDose.getStartTime();

        //flag date to be returned
        String dateStringLocal = MMUtilitiesTime.convertTimeMStoString((MMMainActivity)getActivity(),
                                                                        dateMilli,
                                                                        false);
        EditText doseDate      = getDoseDateInputView(layout);
        if (doseDate == null)return;
        doseDate.setText(dateStringLocal);


        //indicating time string to be returned
        String timeStringLocal = MMUtilitiesTime.convertTimeMStoString((MMMainActivity)getActivity() ,
                                                                        dateMilli, true);
        EditText doseTime = getDoseTimeInputView(layout);
        if (doseTime== null)return;
        doseTime.setText(timeStringLocal);


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
            if (edtView != null) {

                //show the amount of this dose in the View
                edtView.setText(String.valueOf(amount));

                //Add the EditText view to the layout
                layout.addView(edtView);
            }
            dosePosition++;
        }


        //Create the AlertDialog to display the current doses to the user
        //and allow the user to update the amounts
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder .setView(v) //The View we just built for the Alert Dialog
                .setTitle(R.string.fix_dose_amounts)
                .setIcon(R.drawable.ic_mortar_black_24dp)
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

        //  copy the time/date from the Dialog views to the concurrent Dose
        //Get the pointers to the views in the Dialog
        LinearLayout layout =((AlertDialog) dialog).findViewById(R.id.doseHistoryLine);

        EditText  doseDate   = getDoseDateInputView(layout);
        if (doseDate == null)return;
        String    dateString = doseDate.getText().toString().trim();

        EditText doseTime   = getDoseTimeInputView(layout);
        if (doseTime == null)return;
        String   timeString =  doseTime.getText().toString().trim();

        //convert the local string to local ms
        //first convert the date
        long dateMs = MMUtilitiesTime.convertStringToTimeMs((MMMainActivity)getActivity(),
                                                             dateString,
                                                             false);
         //set to convert time string
        long timeMs = MMUtilitiesTime.convertStringToTimeMs((MMMainActivity)getActivity(),
                                                              timeString,
                                                              true);

        //if there was an error parsing either the date or the time, don't try save
        if ((dateMs == 0) || (timeMs == 0)){
            MMUtilities.getInstance().errorHandler(getActivity(), R.string.error_parsing_date_time);
            MMUtilities.getInstance().errorHandler(getActivity(), R.string.change_not_saved);
            return;
        }

        //add together to get the new time for the concurrent dose
        long cdTimeMs = MMUtilitiesTime.getTotalTime(dateMs, timeMs);






        //store in the concurrent dose
        selectedConcurrentDose.setStartTime(cdTimeMs);



        //get the amounts from the dialog EditText views, and
        // store them in the Doses in the ConcurrentDose instance

        //set up the parameters for the While loop
        int dosePosition = 0;
        EditText medicationDoseInput;
        MMDose   dose;
        int      amt;
        String   amtString;

        //Update the doses from the concurrent dose with the amounts from the UI
        int lastDose = 0;
        if (doses != null){
            lastDose = doses.size();
            dosePosition = 0;
            while (dosePosition < lastDose){
                //get the dose from  the concurrent dose
                dose = doses.get(dosePosition);

                //update the dose with the new amount
                amtString = "";
                medicationDoseInput = (EditText) layout.getChildAt(dosePosition+2);
                if (medicationDoseInput != null) {
                    amtString = medicationDoseInput.getText().toString().trim();
                }

                amt = 0;
                if (!amtString.isEmpty()){
                    amt = Integer.valueOf(amtString);
                }
                dose.setAmountTaken(amt);
                //  assure the time at the concurrent dose level matches the time at the dose level
                dose.setTimeTaken(cdTimeMs);
                dosePosition++;
            }
        }






        //Update the concurrent Dose and it's contained doses in the DB
        MMConcurrentDoseManager.getInstance().
                                        addConcurrentDose(selectedConcurrentDose, true);


        //reset the selected position so we'll know the dialog is finished
        mSelectedPosition = sSELECTED_DIALOG_NOT_VISIBLE;

        //reinitialize the cursor
        adapter.reinitializeCursor();
    }



    //***********************************/
    //****  Stuff for RecyclerView  *****/
    //***********************************/

    //Add some code to improve the recycler view
    //Here is the interface for event handlers for Click and LongClick
    interface ClickListener {
        void onClick(View view, int position);

        void onLongClick(View view, int position);
    }

    static class RecyclerTouchListener implements RecyclerView.OnItemTouchListener {

        private GestureDetector gestureDetector;
        private MMHomeFragment.ClickListener clickListener;

        RecyclerTouchListener(Context context,
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
