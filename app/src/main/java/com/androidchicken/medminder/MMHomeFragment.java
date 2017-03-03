package com.androidchicken.medminder;


import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
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
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;


/**
 * A placeholder fragment containing a simple view.
 */
public class MMHomeFragment extends Fragment {

    private static final String TAG = "MainMMTakeDoseFragment";
    private static final int HALF_SECOND = 500;

    /***********************************************/
    /*          UI Widgets                         */
    /***********************************************/

    //main area of screen fragment
    private Button mEditPatientButton;
    //private Button mAddPersonsButton;
    private Button mSelectPatientButton;
    private Button mSaveButton;
    private Button mExportHistoryButton;
    //private Button mAddMedicationButton;
    ArrayList<Button> mMedButtons = new ArrayList<>();
    ArrayList<EditText> mMedEdits = new ArrayList<>();


    private TextView mPatientNickName;

    private EditText mTimeInput;

    private Cursor                  mConcurrentDoseCursor;
    private RecyclerView            mRecyclerView;
    private MMConcurrentDoseCursorAdapter mAdapter;



    /***********************************************/
    /*          Member Variables                   */
    /***********************************************/
    private int      mPersonID;



    /***********************************************/
    /*          Static Methods                     */
    /***********************************************/
    //need to pass a person into the fragment
    public static MMHomeFragment newInstance(int personID){
        //create a bundle to hold the arguments
        Bundle args = new Bundle();

        //It will be some work to make all of the data model serializable
        //so for now, just pass the person values
        args.putInt         (MMPerson.sPersonIDTag,personID);

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
            MMDatabaseManager databaseManager = MMDatabaseManager.getInstance(getActivity());
        }catch (Exception e) {
            Log.e(TAG,Log.getStackTraceString(e));
        }

        Bundle args = getArguments();

        if (args != null) {
            mPersonID = args.getInt(MMPerson.sPersonIDTag);
        }

    }


    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        //Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_home, container, false);

        //Wire up the UI widgets so they can handle events later
        wireWidgets(v);
        initializeRecyclerView(v);
        initializeUI();

        //hide the soft keyboard
        // Check if no view has focus:
        View view = getActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager imm =
                    (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }

        //start the medButton animation
        startMedButtonBlink();

        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        return v;
    }

    @Override
    public void onResume(){
        super.onResume();

        //set the title bar subtitle
        ((MainActivity) getActivity()).setMMSubtitle(R.string.title_take_dose);
    }

    /***********************************************/
    /*          Member Methods                     */
    /***********************************************/

    private void   wireWidgets(View v){

        //Patient Profile Button
        mEditPatientButton = (Button) v.findViewById(R.id.patientProfileButton);
        mEditPatientButton.setText(R.string.patient_edit_profile_label);
        //the order of images here is left, top, right, bottom
        // mEditPatientButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_project, 0, 0);
        mEditPatientButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(),
                        R.string.patient_edit_profile_label,
                        Toast.LENGTH_SHORT).show();
                //switch to person screen
                // But the switching happens on the container Activity
                if (mPersonID == 0) {
                    ((MainActivity) getActivity()).switchToPersonScreen();
                } else {
                    //pre-populate
                    ((MainActivity) getActivity()).switchToPersonScreen(mPersonID);
                }

            }
        });

        //Export History Button
        mExportHistoryButton = (Button) v.findViewById(R.id.exportHistoryButton);
        mExportHistoryButton.setText(R.string.exportHistoryLabel);
        //the order of images here is left, top, right, bottom
        // mExportHistoryButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_project, 0, 0);
        mExportHistoryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(),
                        R.string.exportHistoryLabel,
                        Toast.LENGTH_SHORT).show();
                //switch to home screen
                // But the switching happens on the container Activity
                if (mPersonID == 0) {
                    ((MainActivity) getActivity()).switchToExportScreen();
                } else {
                    //pre-populate
                    ((MainActivity) getActivity()).switchToExportScreen(mPersonID);
                }

            }
        });

/*
        //Add Persons Button
        mAddPersonsButton = (Button) v.findViewById(R.id.addPersonsButton);
        mAddPersonsButton.setText(R.string.patient_add_persons_label);
        //the order of images here is left, top, right, bottom
        // mAddPersonsButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_project, 0, 0);
        mAddPersonsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(),
                        R.string.patient_add_persons_label,
                        Toast.LENGTH_SHORT).show();
                //switch to person screen
                // But the switching happens on the container Activity
                ((MainActivity) getActivity()).switchToPersonScreen();
            }
        });
*/
        //Show Persons Button
        mSelectPatientButton = (Button) v.findViewById(R.id.selectPatientButton);
        mSelectPatientButton.setText(R.string.select_patient_label);
        //the order of images here is left, top, right, bottom
        // mSelectPatientButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_project, 0, 0);
        mSelectPatientButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(),
                        R.string.select_patient_label,
                        Toast.LENGTH_SHORT).show();
                //switch to person screen
                // But the switching happens on the container Activity
                ((MainActivity) getActivity()).switchToPersonListScreen();
            }
        });

        //save Button
        mSaveButton = (Button) v.findViewById(R.id.patientSaveButton);
        mSaveButton.setText(R.string.save_label);
        //the order of images here is left, top, right, bottom
        //mSaveButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_collect, 0, 0);
        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPersonID == 0)return;
                Toast.makeText(getActivity(),
                        R.string.save_label,
                        Toast.LENGTH_SHORT).show();

                onSave();
                ((MainActivity) getActivity()).switchToHomeScreen(mPersonID);

            }
        });
/*
        //Add Medication Button
        mAddMedicationButton = (Button) v.findViewById(R.id.patientAddMedicationButton);
        mAddMedicationButton.setText(R.string.patient_add_medication_label);
        //the order of images here is left, top, right, bottom
        //mAddMedicationButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_stakeout, 0, 0);
        mAddMedicationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(),
                        R.string.patient_add_medication_label,
                        Toast.LENGTH_SHORT).show();
                //switch to medication screen
                // But the switching happens on the container Activity
                ((MainActivity) getActivity()).switchToMedicationScreen(mPatient.getPersonID());
            }
        });
*/
        //Medication Buttons
        if (mPersonID != 0){

            MMPerson person = MMUtilities.getPerson(mPersonID);

            if (person != null) {

                int last = person.getMedications().size();
                if (last > 0) {
                    //convert pixels to dp
                    int sizeInDp = 10; //padding between buttons

                    addDateTimeFieldsToView(v, sizeInDp);

                    int position = 0;

                    while (position < last) {
                        addMedButtonToView(v, position, sizeInDp);
                        position++;
                    }
                }
            }
        }

        //Patient Nick Name
        mPatientNickName = (TextView) v.findViewById(R.id.patientNickNameLabel);
        //There are no events associated with this field

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
        mRecyclerView = (RecyclerView) v.findViewById(R.id.doseHistoryList);

        //3) create and assign a layout manager to the recycler view
        //RecyclerView.LayoutManager mLayoutManager  = new LinearLayoutManager(getActivity());
        RecyclerView.LayoutManager mLayoutManager  = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);

        //4) Get the list of ConcurrentDose Instances from the ConcurrentDoseManager

        //      get the singleton list container
        MMConcurrentDoseManager concurrentDoseManager = MMConcurrentDoseManager.getInstance();
        //      then go get our list of concurrentDoses
        mConcurrentDoseCursor = concurrentDoseManager.getAllConcurrentDosesCursor(mPersonID);

        //5) Use the data to Create and set out concurrentDose Adapter
        //     even though we're giving the Adapter the list,
        //     Adapter uses the ConcurrentDoseManager to maintain the list and
        //     the items in the list.
        mAdapter = new MMConcurrentDoseCursorAdapter(mConcurrentDoseCursor);
        if (mPersonID != 0){

            MMPerson person = MMUtilities.getPerson(mPersonID);

            if (person != null) {
                // TODO: 2/22/2017 Perhaps need to bail on list here if null
                int numbMeds = person.getMedications().size();
                mAdapter.setAdapterContext(getActivity(), mPersonID, numbMeds);
            }
        }
        mRecyclerView.setAdapter(mAdapter);

        //6) create and set the itemAnimator
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        //7) create and add the item decorator
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(),
                                                                   DividerItemDecoration.VERTICAL));
 /*
           mRecyclerView.addItemDecoration(new DividerItemDecoration(
                getActivity(),
                LinearLayoutManager.VERTICAL));
*/

        //8) add event listeners to the recycler view
        mRecyclerView.addOnItemTouchListener(
            new RecyclerTouchListener(getActivity(), mRecyclerView, new ClickListener() {

                @Override
                public void onClick(View view, int position) {
                    onSelect(position);
                }

                @Override
                public void onLongClick(View view, int position) {

                }
            }));

    }

    private String getMedNickname(int position){

        MMMedication medication;

        String nickName;
        position = position-1;

        if (mPersonID != 0){
            MMPerson person = MMUtilities.getPerson(mPersonID);

            if (person != null) {

                int last = person.getMedications().size();

                if (last > 0) {
                    if (position < last) {
                        medication = person.getMedications().get(position);
                        if (medication != null) {
                            nickName = medication.getMedicationNickname().toString().trim();
                            return nickName;
                        }
                    }
                }
            }
        }
        return "Med"+ String.valueOf(position+1);
    }

    private void   addDateTimeFieldsToView(View v, int sizeInDp){

        int padding = MMUtilities.convertPixelsToDp(getActivity(), sizeInDp);

        LinearLayout layout = (LinearLayout) v.findViewById(R.id.medInputLayout);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                0,//width
                ViewGroup.LayoutParams.WRAP_CONTENT);//height
        lp.weight = 4f;
        //lp.gravity = Gravity.CENTER;//set below too
        lp.setMarginEnd(padding);

        EditText mTimeInput = new EditText(getActivity());
        mTimeInput.setHint(R.string.dose_default_time);
        mTimeInput.setInputType(InputType.TYPE_CLASS_TEXT);
        mTimeInput.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
        mTimeInput.setLayoutParams(lp);
        mTimeInput.setPadding(0,0,padding,0);
        mTimeInput.setGravity(Gravity.CENTER);
        mTimeInput.setTextColor      (ContextCompat.getColor(getActivity(),R.color.colorTextBlack));
        mTimeInput.setBackgroundColor(ContextCompat.getColor(getActivity(),R.color.colorInputBackground));


        //Time input for this dose
        //There is no label for this field
        mTimeInput.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
               /* Toast.makeText(getActivity(),
                        R.string.input_received,
                        Toast.LENGTH_SHORT).show();
               */
                return false;
            }
        });

        mTimeInput.setText(MMUtilities.getDateTimeString());

        layout.addView(mTimeInput);

    }

    private Button addMedButtonToView(View v, int position, int sizeInDp){
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
        medButton.setText(getMedNickname(position + 1));
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

                return true;
            }
        });

    }

    private void   startMedButtonBlink(){
        //if (true)return;
        if (mPersonID == 0)return;

        if (mMedButtons == null){
            mMedButtons = new ArrayList<Button>();
        }

        int last = mMedButtons.size();
        int position = 0;
        Button medButton;
        while (position < last){
            medButton = mMedButtons.get(position);
            if (position == 1)animateButton(medButton);
            position++;
        }
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
        if (mPersonID != 0){
            MMPerson person = MMUtilities.getPerson(mPersonID);

            if (person != null) {

                //get the medication
                MMMedication medication = person.getMedications().get(position);

                if (medication != null) {
                    //and the dose
                    int dose = medication.getDoseAmount();
                    //and the EditText field
                    EditText medField = mMedEdits.get(position);

                    if (medField != null) {
                        //show the user
                        medField.setText(String.valueOf(dose));
                    }
                }
            }
        }

    }

    private void   initializeUI(){
        //determine if a person is yet associated with the fragment
        if (mPersonID != 0){
            //if there is a person corresponding to the patientID, put the name up on the screen
            MMPerson person = MMUtilities.getPerson(mPersonID);

            if (person != null) {
                mPatientNickName.setText(person.getNickname().toString().trim());
            }
        }
    }

    private boolean onSave(){

        //Creates in memory structure to save all the doses taken concurrently
        if (mPersonID == 0)return false;

        MMPerson person = MMUtilities.getPerson(mPersonID);

        if (person == null) return false;

        Calendar c = Calendar.getInstance();
        long seconds = c.getTimeInMillis();     // = c.get(Calendar.SECOND);

        MMConcurrentDose concurrentDoses = new MMConcurrentDose(mPersonID, seconds);
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
                                             seconds,
                                             amtTaken);
                    doses.add(dose);
                }
            }
            position++;
        }

        MMConcurrentDoseManager concurrentDoseManager = MMConcurrentDoseManager.getInstance();
        return concurrentDoseManager.add(concurrentDoses);

    }



    /**********************************************************/
    //      Utility Functions used in handling events         //
    /**********************************************************/

    //called from onClick(), executed when a concurrent dose is selected from list
    private void onSelect(int position){
        //todo need to update selection visually

        // TODO: 10/3/2016 Need to query list in Manager or Adapter, not locally

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
