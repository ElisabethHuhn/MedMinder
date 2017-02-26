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
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


/**
 * A placeholder fragment containing a simple view.
 */
public class MainMMPersonFragment extends Fragment {

    //main area of screen fragment
    private Button   mSaveButton;
    private Button   mAddMedButton;

    private EditText mPersonNickNameInput;
    private EditText mPersonEmailAddrInput;
    private EditText mPersonTextAddrInput;

    
    private int      mPersonID;


    /***********************************************/
    /*          Static Methods                     */
    /***********************************************/


    //need to pass a person into the fragment
    public static MainMMPersonFragment newInstance(int personID){
        //create a bundle to hold the arguments
        Bundle args = new Bundle();

        //It will be some work to make all of the data model serializable
        //so for now, just pass the person values
        args.putInt         (MMPerson.sPersonIDTag,personID);

        MainMMPersonFragment fragment = new MainMMPersonFragment();

        fragment.setArguments(args);
        return fragment;
    }

    /***********************************************/
    /*          Constructor                        */
    /***********************************************/

    //
    public MainMMPersonFragment() {
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
            //initialize the DB, providing it with a context if necessarary
            MMDatabaseManager.getInstance(getActivity());

            //Get the ID of the person passed to this screen
            mPersonID = args.getInt(MMPerson.sPersonIDTag);
            MMPersonManager personManager = MMPersonManager.getInstance();

        }

    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        //Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_person_with_meds, container, false);

        if (mPersonID != 0) {
            initializeRecyclerView(v);
        }

        //Wire up the UI widgets so they can handle events later
        wireWidgets(v);
        wireListTitleWidgets(v);

        //If we had any arguments passed, update the screen with them
        initializeUI();

        //set the title bar subtitle
        ((MainActivity) getActivity()).setMMSubtitle(R.string.title_person);

        return v;
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
        RecyclerView recyclerView;
        MMMedicationCursorAdapter adapter;

        //1) Inflate the layout for this fragment
        //      done in the caller


        //2) find and remember the RecyclerView
        recyclerView = (RecyclerView) v.findViewById(R.id.medicationList);

        //3) create and assign a layout manager to the recycler view
        //RecyclerView.LayoutManager mLayoutManager  = new LinearLayoutManager(getActivity());
        RecyclerView.LayoutManager mLayoutManager  = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(mLayoutManager);

        //4) Get the list of Medication Instances from the Person
        MMMedicationManager medicationManager = MMMedicationManager.getInstance();
        Cursor cursor = medicationManager.getAllMedicationsCursor(mPersonID);

        //5) Use the data to Create and set out medication Adapter
        //     even though we're giving the Adapter the list,
        //     Adapter uses the MedicationManager to maintain the list and
        //     the items in the list.
        //     The medication manager is smart enough to go find the person,
        //       then find the medication list, and maintain it from there


        adapter = new MMMedicationCursorAdapter(cursor);
        adapter.setAdapterContext(mPersonID);
        recyclerView.setAdapter(adapter);

        //6) create and set the itemAnimator
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        //7) create and add the item decorator
        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(),
                                                                 DividerItemDecoration.VERTICAL));
/*
        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(),
                                                                 LinearLayoutManager.VERTICAL));
*/

        //8) add event listeners to the recycler view
        recyclerView.addOnItemTouchListener(
                new RecyclerTouchListener(getActivity(), recyclerView, new MainMMPersonListFragment.ClickListener() {
                    @Override
                    public void onClick(View view, int position) {
                        onSelect(position);
                    }

                    @Override
                    public void onLongClick(View view, int position) {

                    }
                }));

    }

    private void wireWidgets(View v){
        View field_container;
        TextView label;

        //save Button
        mSaveButton = (Button) v.findViewById(R.id.personSaveButton);
        mSaveButton.setText(R.string.save_label);
        //the order of images here is left, top, right, bottom
        //mSaveButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_collect, 0, 0);
        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(),
                        R.string.save_label,
                        Toast.LENGTH_SHORT).show();


               // if (mPersonID == 0)return;
                onSave();

                //switch to home screen with the person as a patient
                ((MainActivity) getActivity()).switchToHomeScreen(mPersonID);

            }
        });


        //add Button
        mAddMedButton = (Button) v.findViewById(R.id.personAddMedicationButton);
        mAddMedButton.setText(R.string.patient_add_medication_label);
        mAddMedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPersonID == 0){
                    MMUtilities.errorHandler(getActivity(), R.string.person_save_first);
                    return;
                }


                Toast.makeText(getActivity(),
                        R.string.patient_add_medication_label,
                        Toast.LENGTH_SHORT).show();
                //switch to medication screen
                // But the switching happens on the container Activity
                ((MainActivity) getActivity()).switchToMedicationScreen(mPersonID);

            }
        });



        field_container = v.findViewById(R.id.personNickName);
        label = (TextView)(field_container.findViewById(R.id.fieldLabel));
        label.setText(R.string.person_nick_name_label);

        mPersonNickNameInput = (EditText) (field_container.findViewById(R.id.fieldInput));

        //mPersonNickNameInput.setHint(R.string.person_nick_name_hint);
        mPersonNickNameInput.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                return false;
            }
        });


        field_container = v.findViewById(R.id.personEmailAddr);
        label = (TextView)(field_container.findViewById(R.id.fieldLabel));
        label.setText(R.string.person_email_addr_label);

        mPersonEmailAddrInput = (EditText) (field_container.findViewById(R.id.fieldInput));
        //mPersonEmailAddrInput.setHint(R.string.person_email_addr_hint);
        mPersonEmailAddrInput.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);

        mPersonEmailAddrInput.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                return false;
            }
        });



        field_container = v.findViewById(R.id.personTextAddr);
        label = (TextView)(field_container.findViewById(R.id.fieldLabel));
        label.setText(R.string.person_text_addr_label);

        mPersonTextAddrInput = (EditText)(field_container.findViewById(R.id.fieldInput));
        mPersonTextAddrInput.setInputType(InputType.TYPE_CLASS_PHONE);
        //mPersonTextAddrInput.setHint(R.string.person_text_addr_hint);
        mPersonTextAddrInput.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                return false;
            }
        });


    }

    private void wireListTitleWidgets(View v){
        View field_container;
        TextView label;

        MainActivity myActivity = (MainActivity)getActivity();

        //set up the labels for the medication list
        field_container = v.findViewById(R.id.medicationTitleRow);

        label = (EditText) (field_container.findViewById(R.id.medicationNickNameInput));
        label.setText(R.string.medication_nick_name_label);
        label.setBackgroundColor(ContextCompat.getColor(myActivity, R.color.colorHistoryLabelBackground));

        label = (EditText) (field_container.findViewById(R.id.medicationBrandNameInput));
        label.setText(R.string.medication_brand_name_label);
        label.setBackgroundColor(ContextCompat.getColor(myActivity, R.color.colorHistoryLabelBackground));

        label = (EditText) (field_container.findViewById(R.id.medicationGenericNameInput));
        label.setText(R.string.medication_generic_name_label);
        label.setBackgroundColor(ContextCompat.getColor(myActivity, R.color.colorHistoryLabelBackground));

        label = (EditText) (field_container.findViewById(R.id.medicationForPersonInput));
        label.setText(R.string.medication_for_label);
        label.setBackgroundColor(ContextCompat.getColor(myActivity, R.color.colorHistoryLabelBackground));

        label = (EditText) (field_container.findViewById(R.id.medicationDoseStrategyInput));
        label.setText(R.string.medication_dose_strategy_label);
        label.setBackgroundColor(ContextCompat.getColor(myActivity, R.color.colorHistoryLabelBackground));

        label = (EditText) (field_container.findViewById(R.id.medicationDoseAmountInput));
        label.setText(R.string.medication_dose_amount_label);
        label.setBackgroundColor(ContextCompat.getColor(myActivity, R.color.colorHistoryLabelBackground));

        label = (EditText) (field_container.findViewById(R.id.medicationDoseUnitsInput));
        label.setText(R.string.medication_dose_units_label);
        label.setBackgroundColor(ContextCompat.getColor(myActivity, R.color.colorHistoryLabelBackground));

        label = (EditText) (field_container.findViewById(R.id.medicationDoseNumInput));
        label.setText(R.string.medication_num_label);
        label.setBackgroundColor(ContextCompat.getColor(myActivity, R.color.colorHistoryLabelBackground));

    }

    private void initializeUI() {
        MMPerson person = null;
        if (mPersonID == 0) {
            //just create a temporary person object without an id
            person = new MMPerson(mPersonID);
        } else {
            MMPersonManager personManager = MMPersonManager.getInstance();
            person = personManager.getPerson(mPersonID);
            if (person == null){
                String message = getString(R.string.person_does_not_exist) + mPersonID;
                MMUtilities.errorHandler(getActivity(), message);
                // TODO: 2/21/2017 How should this error be handled???
                mPersonID = 0;
                person = new MMPerson(mPersonID);
            }
        }


        mPersonNickNameInput .setText(person.getNickname()    .toString().trim());
        mPersonEmailAddrInput.setText(person.getEmailAddress().toString().trim());
        mPersonTextAddrInput .setText(person.getTextAddress() .toString().trim());
    }

    private void onSave(){
        CharSequence nickname = mPersonNickNameInput.getText();
        if (nickname == null){
            MMUtilities.errorHandler(getActivity(), R.string.person_not_valid);
            return;
        }
        //If this person already exists, we do NOT want to create a new Person object
        MMPerson person;
        if (mPersonID == 0) {
            person = new MMPerson(nickname);
            mPersonID = person.getPersonID();
        } else {
            MMPersonManager personManager = MMPersonManager.getInstance();
            person = personManager.getPerson(mPersonID);
        }
        person.setNickname(nickname);

        //strings are set to "" in the constructor, so the empty case can be ignored
        //but do need to know if legal input has been made
        String temp = mPersonEmailAddrInput.getText().toString().trim();
        if (!(temp.isEmpty())){
            person.setEmailAddress(temp);
        }

        temp = mPersonTextAddrInput.getText().toString().trim();
        if (!(temp.isEmpty())){
            person.setTextAddress(temp);
        }

        //done in constructor
        // person.setMedications(new ArrayList<MMMedication>());

        //so add/update the person to/in permanent storage
        //This adds/updates any medications that are recorded on the Person to the DB
        MMPersonManager personManager = MMPersonManager.getInstance();
        personManager.add(person);

    }

    /**********************************************************/
    //      Utility Functions used in handling events         //
    /**********************************************************/

    //called from onClick(), executed when a medication is selected
    private void onSelect(int position){
        //todo need to update selection visually
/*
The medication list is maintained on the person object, not locally here

        // TODO: 10/3/2016 Need to query list in Person Manager or Adapter, not locally
        MMMedication selectedMedication = mMedicationList.get(position);

        Toast.makeText(getActivity().getApplicationContext(),
                selectedMedication.getMedicationNickname() + " is selected!",
                Toast.LENGTH_SHORT).show();
*/
        //But, don't know if this makes sense in the flow of things
        ((MainActivity) getActivity()).switchToMedicationScreen(mPersonID, position);
    }


    //Add some code to improve the recycler view
    //Here is the interface for event handlers for Click and LongClick
    public interface ClickListener {
        void onClick(View view, int position);

        void onLongClick(View view, int position);
    }

    public static class RecyclerTouchListener implements RecyclerView.OnItemTouchListener {

        private GestureDetector gestureDetector;
        private MainMMPersonListFragment.ClickListener clickListener;

        public RecyclerTouchListener(Context context,
                                     final RecyclerView recyclerView,
                                     final MainMMPersonListFragment.ClickListener clickListener) {

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
