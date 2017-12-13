package com.androidchicken.medminder;

import android.content.DialogInterface;
import android.content.res.Configuration;
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
import android.support.v7.widget.SwitchCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;


/**
 * A placeholder fragment containing a simple view.
 */
public class MMPersonFragment extends Fragment {

    private boolean  isUIChanged = false;


    //**********************************************/
    //          Static Methods                     */
    //**********************************************/


    //**********************************************/
    //          Constructor                        */
    //**********************************************/
    public MMPersonFragment() {
    }

    //**********************************************/
    //          Lifecycle Methods                  */
    //**********************************************/

    //pull the arguments out of the fragment bundle and store in the member variables
    //In this case, prepopulate the personID this screen refers to
    @Override
    public void onCreate(Bundle savedInstanceState){

        super.onCreate(savedInstanceState);

        //initialize the DB, providing it with a context if necessary
        MMDatabaseManager.getInstance(getActivity());
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        MMMainActivity activity = (MMMainActivity)getActivity();
        if (activity == null)return null;


        //Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_person, container, false);

        if (getPatientID() != MMUtilities.ID_DOES_NOT_EXIST) {
            initializeRecyclerView(v);
        }

        //Wire up the UI widgets so they can handle events later
        wireWidgets(v);
        wireListTitleWidgets(v);

        //If we had any arguments passed, update the screen with them
        initializeUI(v);


        //set the title bar subtitle
        activity.setMMSubtitle(R.string.title_person);

        //Set the changed UI flag based on whether we are recreating the View
        if (savedInstanceState != null) {
            isUIChanged = savedInstanceState.getBoolean(MMHomeFragment.sIsUIChangedTag);
            if (isUIChanged) {
                setUIChanged(v);
            } else {
                setUISaved(v);
            }
        } else {
            setUISaved(v);
        }

        activity.handleFabVisibility();

        return v;
    }

    @Override
    public void onResume(){

        super.onResume();

        MMMainActivity activity = (MMMainActivity)getActivity();
        if (activity == null)return ;

        //MMUtilities.clearFocus(activity);

        //The following kludge is necessary because the RecyclerView list
        // disappears in Landscape mode unless the soft keyboard is visible
        // I never could figure out the right way to fix it.
        MMUtilities utilities = MMUtilities.getInstance();
        if (getOrientation() == Configuration.ORIENTATION_LANDSCAPE) {

            //get rid of the soft keyboard if it is visible
            View v = getView();
            if (v != null) {
                EditText personNickNameInput = v.findViewById(R.id.personNickNameInput);
                utilities.showSoftKeyboard(activity, personNickNameInput);
            }
        } else {
            //get rid of the soft keyboard if it is visible
            utilities.hideSoftKeyboard(activity);
        }


        //set the title bar subtitle
        activity.setMMSubtitle(R.string.title_person);

        //Set the FAB visible
        activity.handleFabVisibility();
    }

    public int getOrientation(){
        int orientation = Configuration.ORIENTATION_PORTRAIT;
        if (getResources().getDisplayMetrics().widthPixels >
            getResources().getDisplayMetrics().heightPixels) {

            orientation = Configuration.ORIENTATION_LANDSCAPE;
        }
        return orientation;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle savedInstanceState){
        // Save custom values into the bundle

        //Save the isUIChanged flag
        savedInstanceState.putBoolean(MMHomeFragment.sIsUIChangedTag, isUIChanged);

        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
    }


    //*************************************************************/
    /*  Convenience Methods for accessing things on the Activity  */
    //*************************************************************/

    private long     getPatientID(){

        MMMainActivity activity = (MMMainActivity)getActivity();
        if (activity == null)return MMUtilities.ID_DOES_NOT_EXIST ;
        return activity.getPatientID();
    }
    private void     setPatientID(long patientID) {
        MMMainActivity activity = (MMMainActivity)getActivity();
        if (activity == null)return ;
        activity.setPatientID(patientID);
    }

    private MMPerson getPerson(){
        return MMPersonManager.getInstance().getPerson(getPatientID());
    }

    //*************************************************************/
    /*                    Initialization Methods                  */
    //*************************************************************/

    private void wireWidgets(View v){

        TextView label;

        final MMMainActivity activity = (MMMainActivity)getActivity();
        if (activity == null)return;

        //Save Button
        Button saveButton = v.findViewById(R.id.personSaveButton);
        saveButton.setText(R.string.save_label);
        //the order of images here is left, top, right, bottom
        //saveButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_collect, 0, 0);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSave();
            }
        });



        final SwitchCompat existSwitch = v.findViewById(R.id.switchExists);
        existSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                setUIChanged();
                View v = getView();
                if (v != null) {
                    if (existSwitch.isChecked()) {
                        v.setBackgroundColor(ContextCompat.
                                        getColor(activity, R.color.colorScreenBackground));
                    } else {
                        v.setBackgroundColor(ContextCompat.
                                        getColor(activity, R.color.colorScreenDeletedBackground));
                    }
                }
            }
        });


        label = v.findViewById(R.id.personIDLabel);
        label.setEnabled(false);
        label.setText(R.string.person_label);

        EditText personNickNameInput = (v.findViewById(R.id.personNickNameInput));
        personNickNameInput.addTextChangedListener(new TextWatcher() {
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

        MMMainActivity activity = (MMMainActivity)getActivity();
        if (activity == null)return;


        //set up the labels for the medication list
        field_container = v.findViewById(R.id.medicationTitleRow);

        label = (EditText) (field_container.findViewById(R.id.medicationNickNameInput));
        label.setText(R.string.medication_nick_name_label);
        label.setEnabled(false);
        label.setBackgroundColor(ContextCompat.getColor(activity, R.color.colorHistoryLabelBackground));

        label = (EditText) (field_container.findViewById(R.id.medicationDoseNumInput));
        label.setText(R.string.number_hash_tag);
        label.setEnabled(false);
        label.setBackgroundColor(ContextCompat.getColor(activity, R.color.colorHistoryLabelBackground));

        label = (EditText) (field_container.findViewById(R.id.medicationDoseAmountInput));
        label.setText(R.string.medication_dose_amount_label);
        label.setEnabled(false);
        label.setBackgroundColor(ContextCompat.getColor(activity, R.color.colorHistoryLabelBackground));

        label = (EditText) (field_container.findViewById(R.id.medicationDoseUnitsInput));
        label.setText(R.string.medication_dose_units_label);
        label.setEnabled(false);
        label.setBackgroundColor(ContextCompat.getColor(activity, R.color.colorHistoryLabelBackground));

        label = (EditText) (field_container.findViewById(R.id.medicationBrandNameInput));
        label.setText(R.string.medication_brand_name_label);
        label.setEnabled(false);
        label.setBackgroundColor(ContextCompat.getColor(activity, R.color.colorHistoryLabelBackground));

        label = (EditText) (field_container.findViewById(R.id.medicationGenericNameInput));
        label.setText(R.string.medication_generic_name_label);
        label.setEnabled(false);
        label.setBackgroundColor(ContextCompat.getColor(activity, R.color.colorHistoryLabelBackground));


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

        MMMainActivity activity = (MMMainActivity)getActivity();
        if (activity == null)return;

        //2) find and remember the RecyclerView
        RecyclerView recyclerView = getRecyclerView(v);

        //3) create and assign a layout manager to the recycler view
        //RecyclerView.LayoutManager mLayoutManager  = new LinearLayoutManager(activity);
        RecyclerView.LayoutManager mLayoutManager  = new LinearLayoutManager(activity);
        recyclerView.setLayoutManager(mLayoutManager);

        //4) Get the Cursor of Medication Instances for this Person from the DB
        MMMedicationManager medicationManager = MMMedicationManager.getInstance();
        boolean currentOnly = MMSettings.getInstance().showOnlyCurrentMeds(activity);
        Cursor cursor = medicationManager.getAllMedicationsCursor(getPatientID(), currentOnly);

        //5) Use the data to Create and set out medication Adapter
        MMMedicationCursorAdapter adapter =
                            new MMMedicationCursorAdapter(activity, getPatientID(), cursor);
        recyclerView.setAdapter(adapter);

        //6) create and set the itemAnimator
        recyclerView.setItemAnimator(new DefaultItemAnimator());


        //7) create and add the item decorator
        recyclerView.addItemDecoration(new DividerItemDecoration(activity,
                                                                 DividerItemDecoration.VERTICAL));
/*
        recyclerView.addItemDecoration(new DividerItemDecoration(activity,
                                                                 LinearLayoutManager.VERTICAL));
*/

        //8) add event listeners to the recycler view
        recyclerView.addOnItemTouchListener(
                new MMHomeFragment.RecyclerTouchListener(activity,
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

    private void initializeUI(View v) {
        MMMainActivity activity = (MMMainActivity)getActivity();
        if (activity == null)return;


        EditText personNickNameInput  = v.findViewById(R.id.personNickNameInput);
        //EditText personEmailAddrInput = (EditText) (v.findViewById(R.id.personEmailAddrInput));
        //EditText personTextAddrInput  = (EditText) (v.findViewById(R.id.personTextAddrInput));

        MMPerson person = getPerson();
        if (getPatientID() == MMUtilities.ID_DOES_NOT_EXIST) {
            //just create a temporary person object without an id
            person = new MMPerson(getPatientID());

        } else {

            if (person == null){
                String message = getString(R.string.person_does_not_exist) + getPatientID();
                MMUtilities.getInstance().errorHandler(activity, message);
                setPatientID( MMUtilities.ID_DOES_NOT_EXIST);
                person = new MMPerson(getPatientID());
            }
        }


        //set the radio button to whether the Person exists
        SwitchCompat existSwitch = v.findViewById(R.id.switchExists) ;

        //This is certainly overkill, but it is explicit
        if (person.isCurrentlyExists()){
            existSwitch.setChecked(true);
            v.setBackgroundColor(ContextCompat.getColor(activity, R.color.colorScreenBackground));
         } else {
            existSwitch.setChecked(false);
            v.setBackgroundColor(ContextCompat.getColor(activity, R.color.colorScreenDeletedBackground));
        }

        personNickNameInput .setText(person.getNickname()    .toString().trim());
        //personEmailAddrInput.setText(person.getEmailAddress().toString().trim());
        //personTextAddrInput .setText(person.getTextAddress() .toString().trim());

        setUISaved(v);
    }

    private void onSave(){
        MMMainActivity activity = (MMMainActivity)getActivity();
        if (activity == null)return;

        MMUtilities.getInstance().showStatus(activity, R.string.save_label);

        //get rid of the soft keyboard
        MMUtilities.getInstance().hideSoftKeyboard(activity);

        View v = getView();
        if (v == null)return;

        EditText personNickNameInput  = v.findViewById(R.id.personNickNameInput);


        CharSequence nickname = personNickNameInput.getText();
        if (nickname.toString().isEmpty()){
            MMUtilities.getInstance().errorHandler(activity, R.string.person_not_valid);
            return;
        }
        //If this person already exists, we do NOT want to create a new Person object
        MMPerson person;
        if (getPatientID() == MMUtilities.ID_DOES_NOT_EXIST) {
            //but the ID isn't assigned until the DB save
            person = new MMPerson(getPatientID());
        } else {
            MMPersonManager personManager = MMPersonManager.getInstance();
            person = personManager.getPerson(getPatientID());
        }

        SwitchCompat existSwitch = getView().findViewById(R.id.switchExists);

        if (existSwitch.isChecked()) {
            person.setCurrentlyExists(true);
            v.setBackgroundColor(ContextCompat.
                    getColor(activity,R.color.colorScreenBackground));
        } else {
            person.setCurrentlyExists(false);
            v.setBackgroundColor(ContextCompat.
                    getColor(activity, R.color.colorScreenDeletedBackground));
        }


        person.setNickname(nickname);


        //done in constructor
        // person.setMedications(new ArrayList<MMMedication>());

        //so add/update the person to/in permanent storage
        //This adds/updates any medications that are recorded on the Person to the DB
        boolean currentOnly = MMSettings.getInstance().showOnlyCurrentMeds(activity);
        long returnCode = MMPersonManager.getInstance().addPerson(person, true, currentOnly);
        if (returnCode != MMDatabaseManager.sDB_ERROR_CODE) {
            MMUtilities.getInstance().errorHandler(activity, R.string.save_successful);
            //if the person is newly created, the ID is assigned on DB add
            setPatientID(returnCode);

            MMMedicationCursorAdapter adapter = getAdapter(getView());

            if (adapter != null) {
                //because the adapter exists, initializeRecyclerView() has already run
                //so all we need to reinitialize is the adapter
                adapter.reinitializeCursor(getPatientID());
            } else {
                //we did not have a medication earlier so the entire recyclerView never got initialized
                initializeRecyclerView(getView());
            }
            setUISaved();
        } else {
            MMUtilities.getInstance().errorHandler(activity, R.string.save_unsuccessful);
        }
     }

    public void onExit(){
        MMMainActivity activity = (MMMainActivity)getActivity();
        if (activity == null)return;

        MMUtilities.getInstance().showStatus(activity, R.string.exit_label);

        //if something has changed in the UI, ask first
        if (isUIChanged){
            areYouSureExit();
        } else {
            switchToExit();
        }
    }


    //***********************************/
    //****  Exit Button Dialogue    *****/
    //***********************************/
    //Build and display the alert dialog
    private void areYouSureExit(){
        final MMMainActivity activity = (MMMainActivity)getActivity();
        if (activity == null)return;

        new AlertDialog.Builder(activity)
                .setTitle(R.string.abandon_title)
                .setIcon(R.drawable.ic_mortar_black_24dp)
                .setMessage(R.string.are_you_sure)
                .setPositiveButton(R.string.exit_label,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                //Leave even though project has chaged
                                switchToExit();

                            }
                        })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                        MMUtilities.getInstance().showStatus(activity, R.string.pressed_cancel);

                    }
                })
                .setIcon(R.drawable.ic_mortar_black_24dp)
                .show();
    }

    private void switchToExit(){
        MMMedicationCursorAdapter adapter = getAdapter(getView());
        if (adapter != null)adapter.closeCursor();
    }





    //***********************************/
    //****      Widget Methods      *****/
    //***********************************/


    private RecyclerView getRecyclerView(View v){
        return  (RecyclerView) v.findViewById(R.id.medicationList);
    }

    private MMMedicationCursorAdapter getAdapter(View v){
        return (MMMedicationCursorAdapter)  getRecyclerView(v).getAdapter();
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

        //disable the save button
        saveButtonEnable(v, MMUtilities.BUTTON_DISABLE);
    }

    private void saveButtonEnable(boolean isEnabled){
        View v = getView();
        saveButtonEnable(v, isEnabled);
    }

    private void saveButtonEnable(View v, boolean isEnabled){
        if (v == null)return; //onCreateView() hasn't run yet
        MMMainActivity activity = (MMMainActivity)getActivity();
        if (activity == null)return;

        Button personSaveButton = v.findViewById(R.id.personSaveButton);

        MMUtilities utilities = MMUtilities.getInstance();
        utilities.enableButton(activity, personSaveButton, isEnabled);
    }


    //*********************************************************/
    //      Utility Functions used in handling events         //
    //*********************************************************/

    //called from onClick(), executed when a medication is selected
    private void onSelect(int position){
        MMMainActivity activity = (MMMainActivity)getActivity();
        if (activity == null)return;

        MMMedicationCursorAdapter adapter = getAdapter(getView());
        //MMMedication selectedMedication = adapter.getMedicationAt(position);

        adapter.notifyItemChanged(position);

        activity.switchToMedicationScreen(position, MMMainActivity.sPersonTag);
    }



}
