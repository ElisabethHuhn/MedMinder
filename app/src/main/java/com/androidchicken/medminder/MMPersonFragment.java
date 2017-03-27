package com.androidchicken.medminder;

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
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


/**
 * A placeholder fragment containing a simple view.
 */
public class MMPersonFragment extends Fragment {

    //Screen Widgets

    private EditText mPersonIDOutput;
    private EditText mPersonNickNameInput;
    private EditText mPersonEmailAddrInput;
    private EditText mPersonTextAddrInput;


    private long     mPersonID;
    private boolean  isUIChanged = false;


    /***********************************************/
    /*          Static Methods                     */
    /***********************************************/
    //need to pass a person into the fragment
    public static MMPersonFragment newInstance(long personID){
        //create a bundle to hold the arguments
        Bundle args = new Bundle();

        //It will be some work to make all of the data model serializable
        //so for now, just pass the person values
        args.putLong         (MMPerson.sPersonIDTag,personID);

        MMPersonFragment fragment = new MMPersonFragment();

        fragment.setArguments(args);
        return fragment;
    }

    /***********************************************/
    /*          Constructor                        */
    /***********************************************/
    public MMPersonFragment() {
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
            mPersonID = args.getLong(MMPerson.sPersonIDTag);

        } else {
            mPersonID = MMUtilities.ID_DOES_NOT_EXIST;
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        if (savedInstanceState != null){
            //the fragment is being restored so restore the person ID
            mPersonID = savedInstanceState.getLong(MMPerson.sPersonIDTag);
        }


        //Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_person, container, false);

        if (mPersonID != MMUtilities.ID_DOES_NOT_EXIST) {
            initializeRecyclerView(v);
        }

        //Wire up the UI widgets so they can handle events later
        wireWidgets(v);
        wireListTitleWidgets(v);

        //If we had any arguments passed, update the screen with them
        initializeUI(v);

        //get rid of the soft keyboard if it is visible
        MMUtilities.hideSoftKeyboard(getActivity());

        //set the title bar subtitle
        ((MainActivity) getActivity()).setMMSubtitle(R.string.title_person);

        setUISaved(v);
        return v;
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState){
        // Save custom values into the bundle
        savedInstanceState.putLong(MMPerson.sPersonIDTag, mPersonID);
        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
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
        //      implemented in the caller: onCreateView()


        //2) find and remember the RecyclerView
        RecyclerView recyclerView = getRecyclerView(v);

        //3) create and assign a layout manager to the recycler view
        //RecyclerView.LayoutManager mLayoutManager  = new LinearLayoutManager(getActivity());
        RecyclerView.LayoutManager mLayoutManager  = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(mLayoutManager);

        //4) Get the Cursor of Medication Instances for this Person from the DB
        MMMedicationManager medicationManager = MMMedicationManager.getInstance();
        Cursor cursor = medicationManager.getAllMedicationsCursor(mPersonID);

        //5) Use the data to Create and set out medication Adapter
        MMMedicationCursorAdapter adapter = new MMMedicationCursorAdapter(getActivity(), mPersonID, cursor);
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
                })
        );

    }

    private void wireWidgets(View v){
        View field_container;
        TextView label;

        //Exit Button
        Button exitButton = (Button) v.findViewById(R.id.personExitButton);
        exitButton.setText(R.string.exit_label);
        //the order of images here is left, top, right, bottom
        //exitButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_collect, 0, 0);
        exitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onExit();
            }
        });


        //Save Button
        Button saveButton = (Button) v.findViewById(R.id.personSaveButton);
        saveButton.setText(R.string.save_label);
        //the order of images here is left, top, right, bottom
        //saveButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_collect, 0, 0);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSave();
            }
        });

/*
        //Delete Button
        Button deleteButton = (Button) v.findViewById(R.id.personDeleteButton);
        if (mPersonID == MMUtilities.ID_DOES_NOT_EXIST){
            MMUtilities.enableButton(getActivity(), deleteButton, MMUtilities.BUTTON_DISABLE);
        } else {
            MMPerson person = MMUtilities.getPerson(mPersonID);
            if (person == null) {
                MMUtilities.enableButton(getActivity(), deleteButton, MMUtilities.BUTTON_DISABLE);
            } else {
                MMUtilities.enableButton(getActivity(), deleteButton, MMUtilities.BUTTON_ENABLE);
                if (!person.isCurrentlyExists()){
                    deleteButton.setText(R.string.reinstate_title);
                }
            }
        }
        deleteButton.setText(R.string.delete_title);
        //the order of images here is left, top, right, bottom
        //deleteButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_collect, 0, 0);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onDelete();
            }
        });

*/

        //add Button
        Button addMedButton = (Button) v.findViewById(R.id.personAddMedicationButton);
        addMedButton.setText(R.string.patient_add_medication_label);
        //only enable the Add button after the person has been created
        if (mPersonID == MMUtilities.ID_DOES_NOT_EXIST){
            MMUtilities.enableButton(getActivity(), addMedButton, MMUtilities.BUTTON_DISABLE);
            Toast.makeText(getActivity(), R.string.person_save_first, Toast.LENGTH_SHORT).show();
        }
        addMedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Toast.makeText(getActivity(),
                        R.string.patient_add_medication_label,
                        Toast.LENGTH_SHORT).show();
                //switch to medication screen
                // But the switching happens on the container Activity
                ((MainActivity) getActivity()).switchToMedicationScreen(mPersonID);

            }
        });


        SwitchCompat existSwitch = (SwitchCompat) v.findViewById(R.id.switchExists);
        existSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                setUIChanged();
            }
        });





        field_container = v.findViewById(R.id.personIDName);
        label = (TextView)(field_container.findViewById(R.id.fieldLabel));
        label.setEnabled(false);
        label.setText(R.string.person_label);

        mPersonIDOutput = (EditText) (field_container.findViewById(R.id.fieldIdInput));
        mPersonIDOutput.setEnabled(false);
        mPersonIDOutput.setBackgroundColor(ContextCompat.getColor(getActivity(),R.color.colorGray));


        mPersonNickNameInput = (EditText) (field_container.findViewById(R.id.fieldInput));
        mPersonNickNameInput.addTextChangedListener(new TextWatcher() {
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






        field_container = v.findViewById(R.id.personEmailAddr);
        label = (TextView)(field_container.findViewById(R.id.fieldLabel));
        label.setText(R.string.person_email_addr_label);

        mPersonEmailAddrInput = (EditText) (field_container.findViewById(R.id.fieldInput));
        //mPersonEmailAddrInput.setHint(R.string.person_email_addr_hint);
        mPersonEmailAddrInput.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        mPersonEmailAddrInput.addTextChangedListener(new TextWatcher() {
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



        field_container = v.findViewById(R.id.personTextAddr);
        label = (TextView)(field_container.findViewById(R.id.fieldLabel));
        label.setText(R.string.person_text_addr_label);

        mPersonTextAddrInput = (EditText)(field_container.findViewById(R.id.fieldInput));
        mPersonTextAddrInput.setInputType(InputType.TYPE_CLASS_PHONE);
        //mPersonTextAddrInput.setHint(R.string.person_text_addr_hint);
        mPersonTextAddrInput.addTextChangedListener(new TextWatcher() {
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
    }

    private void wireListTitleWidgets(View v){
        View field_container;
        TextView label;

        MainActivity myActivity = (MainActivity)getActivity();

        //set up the labels for the medication list
        field_container = v.findViewById(R.id.medicationTitleRow);


        label = (EditText) (field_container.findViewById(R.id.medicationForPersonInput));
        label.setText(R.string.medication_for_label);
        label.setEnabled(false);
        label.setBackgroundColor(ContextCompat.getColor(myActivity, R.color.colorHistoryLabelBackground));


        label = (EditText) (field_container.findViewById(R.id.medicationNickNameInput));
        label.setText(R.string.medication_nick_name_label);
        label.setEnabled(false);
        label.setBackgroundColor(ContextCompat.getColor(myActivity, R.color.colorHistoryLabelBackground));


        label = (EditText) (field_container.findViewById(R.id.medicationDoseStrategyInput));
        label.setText(R.string.medication_dose_strategy_label);
        label.setEnabled(false);
        label.setBackgroundColor(ContextCompat.getColor(myActivity, R.color.colorHistoryLabelBackground));

        label = (EditText) (field_container.findViewById(R.id.medicationDoseNumInput));
        label.setText(R.string.number_hash_tag);
        label.setEnabled(false);
        label.setBackgroundColor(ContextCompat.getColor(myActivity, R.color.colorHistoryLabelBackground));

        label = (EditText) (field_container.findViewById(R.id.medicationDoseAmountInput));
        label.setText(R.string.medication_dose_amount_label);
        label.setEnabled(false);
        label.setBackgroundColor(ContextCompat.getColor(myActivity, R.color.colorHistoryLabelBackground));

        label = (EditText) (field_container.findViewById(R.id.medicationDoseUnitsInput));
        label.setText(R.string.medication_dose_units_label);
        label.setEnabled(false);
        label.setBackgroundColor(ContextCompat.getColor(myActivity, R.color.colorHistoryLabelBackground));

        label = (EditText) (field_container.findViewById(R.id.medicationBrandNameInput));
        label.setText(R.string.medication_brand_name_label);
        label.setEnabled(false);
        label.setBackgroundColor(ContextCompat.getColor(myActivity, R.color.colorHistoryLabelBackground));

        label = (EditText) (field_container.findViewById(R.id.medicationGenericNameInput));
        label.setText(R.string.medication_generic_name_label);
        label.setEnabled(false);
        label.setBackgroundColor(ContextCompat.getColor(myActivity, R.color.colorHistoryLabelBackground));


     }

    private void initializeUI(View v) {
        MMPerson person = null;
        if (mPersonID == MMUtilities.ID_DOES_NOT_EXIST) {
            //just create a temporary person object without an id
            person = new MMPerson(mPersonID);
        } else {
            MMPersonManager personManager = MMPersonManager.getInstance();
            person = personManager.getPerson(mPersonID);
            if (person == null){
                String message = getString(R.string.person_does_not_exist) + mPersonID;
                MMUtilities.errorHandler(getActivity(), message);
                // TODO: 2/21/2017 How should this error be handled???
                mPersonID = MMUtilities.ID_DOES_NOT_EXIST;
                person = new MMPerson(mPersonID);
            }
        }


        //set the radio button to whether the Person exists
        SwitchCompat existSwitch = (SwitchCompat) v.findViewById(R.id.switchExists) ;

        //This is certainly overkill, but it is explicit
        if (person.isCurrentlyExists()){
            existSwitch.setChecked(true);
         } else {
            existSwitch.setChecked(false);
        }


        mPersonIDOutput      .setText(String.valueOf(person.getPersonID()));
        mPersonNickNameInput .setText(person.getNickname()    .toString().trim());
        mPersonEmailAddrInput.setText(person.getEmailAddress().toString().trim());
        mPersonTextAddrInput .setText(person.getTextAddress() .toString().trim());

        setUISaved(v);
    }

    private void onSave(){
        Toast.makeText(getActivity(),
                R.string.save_label,
                Toast.LENGTH_SHORT).show();

        //get rid of the soft keyboard
        MMUtilities.hideSoftKeyboard(getActivity());

        CharSequence nickname = mPersonNickNameInput.getText();
        if (nickname == null){
            MMUtilities.errorHandler(getActivity(), R.string.person_not_valid);
            return;
        }
        //If this person already exists, we do NOT want to create a new Person object
        MMPerson person;
        if (mPersonID == MMUtilities.ID_DOES_NOT_EXIST) {
            //but the ID isn't assigned until the DB save
            person = new MMPerson(mPersonID);
        } else {
            MMPersonManager personManager = MMPersonManager.getInstance();
            person = personManager.getPerson(mPersonID);
        }

        //This check really isn't necessary, but use it to shut up lint
        View v = getView();
        if (v != null) {
            SwitchCompat existSwitch = (SwitchCompat) getView().findViewById(R.id.switchExists);

            if (existSwitch.isChecked()) {
                person.setCurrentlyExists(true);
            } else {
                person.setCurrentlyExists(false);
            }
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
        boolean addToDBToo = true;
        long returnCode = personManager.addPerson(person, addToDBToo);
        if (returnCode != MMDatabaseManager.sDB_ERROR_CODE) {
            Toast.makeText(getActivity(), R.string.save_successful, Toast.LENGTH_SHORT).show();
            MMUtilities.enableButton(getActivity(), getAddMedButton(getView()), MMUtilities.BUTTON_ENABLE);

        }
        //if the person is newly created, the ID is assigned on DB add
        mPersonID = returnCode;
        //update the ID field on the UI
        mPersonIDOutput.setText(String.valueOf(mPersonID));


        MMMedicationCursorAdapter adapter = getAdapter(getView());

        if (adapter != null) {
            //because the adapter exists, initializeRecyclerView() has already run
            //so all we need to reinitialize is the adapter
            adapter.reinitializeCursor(mPersonID);
        } else {
            //we did not have a medication earlier so the entire recyclerView never got initialized
            initializeRecyclerView(getView());
        }
        setUISaved();
    }

    private void onExit(){
        Toast.makeText(getActivity(),
                R.string.exit_label,
                Toast.LENGTH_SHORT).show();

        //if something has changed in the UI, ask first
        if (isUIChanged){
            areYouSureExit();
        } else {
            switchToExit();
        }
    }

    private void onDelete(){
        MMPerson person = MMUtilities.getPerson(mPersonID);
        if (person != null){
            if (person.isCurrentlyExists()){
                areYouSureDelete();
            } else {
                areYouSureReinstate();
            }
        } else {
            Toast.makeText(getActivity(), R.string.person_not_found_delete, Toast.LENGTH_SHORT).show();

        }
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

    private void performDelete(){
        MMPerson person = MMUtilities.getPerson(mPersonID);
        int msg;

        if (person == null) {
            msg = R.string.person_not_found_delete;
        } else {
            if (person.isCurrentlyExists()) {
                msg = R.string.delete_person;
                person.setCurrentlyExists(false);
            } else {
                msg = R.string.reinstate_person;
                person.setCurrentlyExists(true);
            }
        }
        Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
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
        MMMedicationCursorAdapter adapter = getAdapter(getView());
        adapter.closeCursor();

        ((MainActivity) getActivity()).switchToHomeScreen(mPersonID);   //switchToPopBackstack();

    }





    /************************************/
    /*****      Widget Methods      *****/
    /************************************/


    private RecyclerView getRecyclerView(View v){
        return  (RecyclerView) v.findViewById(R.id.medicationList);
    }

    private MMMedicationCursorAdapter getAdapter(View v){
        return (MMMedicationCursorAdapter)  getRecyclerView(v).getAdapter();
    }

    private Button getAddMedButton(View v){
        return (Button) v.findViewById(R.id.personAddMedicationButton);
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

        //ENable the delete button
        deleteButtonEnable(MMUtilities.BUTTON_ENABLE);
    }

    private void setUISaved(View v){
        isUIChanged = false;

        //disable the save button
        saveButtonEnable(v, MMUtilities.BUTTON_DISABLE);
    }

    private void saveButtonEnable(boolean isEnabled){
        View v = getView();
        saveButtonEnable(v, isEnabled);
    }

    private void saveButtonEnable(View v, boolean isEnabled){
        if (v == null)return; //onCreateView() hasn't run yet

        Button personSaveButton =
                (Button) v.findViewById(R.id.personSaveButton);

        MMUtilities.enableButton(getActivity(),
                personSaveButton,
                isEnabled);
    }


    private void deleteButtonEnable(boolean isEnabled){
        View v = getView();
        deleteButtonEnable(v, isEnabled);
    }

    private void deleteButtonEnable(View v, boolean isEnabled){
/*
        if (v == null)return; //onCreateView() hasn't run yet

        Button medicationDeleteButton =
                (Button) v.findViewById(R.id.personDeleteButton);

        MMUtilities.enableButton(getActivity(),
                medicationDeleteButton,
                isEnabled);
 */
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
*/


        MMMedicationCursorAdapter adapter = getAdapter(getView());
        MMMedication selectedMedication = adapter.getMedicationAt(position);

        Toast.makeText(getActivity().getApplicationContext(),
                selectedMedication.getMedicationNickname() + " is selected!",
                Toast.LENGTH_SHORT).show();



        // No need to change the Medications on this Person: That happens on the Person Screen
        //Crete a dialogue to ask if the user wants to Delete the medication

        //Medication Attributes
        //Time
        //For each Medication on the Person: a dose amount

        //Save the Concurrent Dose on the Callback from the Dialogue



        //But, don't know if this makes sense in the flow of things
        ((MainActivity) getActivity()).switchToMedicationScreen(mPersonID, position);
    }



}
