package com.androidchicken.medminder;

import android.content.DialogInterface;
import android.content.res.Configuration;
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



    private long     mPersonID;
    private boolean  isUIChanged = false;


    //**********************************************/
    //          Static Methods                     */
    //**********************************************/
    //need to pass a person into the fragment
    public static MMPersonFragment newInstance(long personID){
        //create a bundle to hold the arguments
        Bundle args = new Bundle();

        args.putLong         (MMPerson.sPersonIDTag,personID);

        MMPersonFragment fragment = new MMPersonFragment();

        fragment.setArguments(args);
        return fragment;
    }

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
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
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


        //set the title bar subtitle
        ((MMMainActivity) getActivity()).setMMSubtitle(R.string.title_person);

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

        return v;
    }

    @Override
    public void onResume(){

        super.onResume();
        //MMUtilities.clearFocus(getActivity());

        //The following kludge is necessary because the RecyclerView list
        // disappears in Landscape mode unless the soft keyboard is visible
        // I never could figure out the right way to fix it.
        if (getOrientation() == Configuration.ORIENTATION_LANDSCAPE) {

            //get rid of the soft keyboard if it is visible
            View v = getView();
            if (v != null) {
                EditText personNickNameInput = (EditText) (v.findViewById(R.id.personNickNameInput));
                MMUtilities.showSoftKeyboard(getActivity(), personNickNameInput);
            }
        } else {

            //get rid of the soft keyboard if it is visible
            MMUtilities.hideSoftKeyboard(getActivity());
        }


        //set the title bar subtitle
        ((MMMainActivity) getActivity()).setMMSubtitle(R.string.title_person);

        //Set the FAB visible
        ((MMMainActivity) getActivity()).showFAB();


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
    public void onSaveInstanceState(Bundle savedInstanceState){
        // Save custom values into the bundle
        savedInstanceState.putLong(MMPerson.sPersonIDTag, mPersonID);

        //Save the isUIChanged flag
        savedInstanceState.putBoolean(MMHomeFragment.sIsUIChangedTag, isUIChanged);

        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
    }


    private void wireWidgets(View v){

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


        label = (TextView)(v.findViewById(R.id.personIDLabel));
        label.setEnabled(false);
        label.setText(R.string.person_label);

        EditText personIDOutput = (EditText) (v.findViewById(R.id.personIdInput));
        personIDOutput.setEnabled(false);
        personIDOutput.setBackgroundColor(ContextCompat.getColor(getActivity(),R.color.colorGray));


        EditText personNickNameInput = (EditText) (v.findViewById(R.id.personNickNameInput));
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


        label = (TextView)(v.findViewById(R.id.personEmailAddrLabel));
        label.setText(R.string.person_email_addr_label);


        EditText personEmailAddrInput = (EditText) (v.findViewById(R.id.personEmailAddrInput));
        //personEmailAddrInput.setHint(R.string.person_email_addr_hint);
        personEmailAddrInput.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        personEmailAddrInput.addTextChangedListener(new TextWatcher() {
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



        label = (TextView)(v.findViewById(R.id.personTextAddrLabel));
        label.setText(R.string.person_text_addr_label);

        EditText personTextAddrInput = (EditText)(v.findViewById(R.id.personTextAddrInput));
        personTextAddrInput.setInputType(InputType.TYPE_CLASS_PHONE);
        //personTextAddrInput.setHint(R.string.person_text_addr_hint);
        personTextAddrInput.addTextChangedListener(new TextWatcher() {
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

        MMMainActivity myActivity = (MMMainActivity)getActivity();

        //set up the labels for the medication list
        field_container = v.findViewById(R.id.medicationTitleRow);

        label = (EditText) (field_container.findViewById(R.id.medicationNickNameInput));
        label.setText(R.string.medication_nick_name_label);
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

     private void initializeUI(View v) {

        EditText personIDOutput       = (EditText) (v.findViewById(R.id.personIdInput));
        EditText personNickNameInput  = (EditText) (v.findViewById(R.id.personNickNameInput));
        EditText personEmailAddrInput = (EditText) (v.findViewById(R.id.personEmailAddrInput));
        EditText personTextAddrInput  = (EditText) (v.findViewById(R.id.personTextAddrInput));

        MMPerson person;
        if (mPersonID == MMUtilities.ID_DOES_NOT_EXIST) {
            //just create a temporary person object without an id
            person = new MMPerson(mPersonID);
            personIDOutput.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.colorLightPink));
        } else {
            personIDOutput.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.colorGray));
            MMPersonManager personManager = MMPersonManager.getInstance();
            person = personManager.getPerson(mPersonID);
            if (person == null){
                String message = getString(R.string.person_does_not_exist) + mPersonID;
                MMUtilities.errorHandler(getActivity(), message);
                mPersonID = MMUtilities.ID_DOES_NOT_EXIST;
                person = new MMPerson(mPersonID);
            }
        }


        //set the radio button to whether the Person exists
        SwitchCompat existSwitch = (SwitchCompat) v.findViewById(R.id.switchExists) ;

        //This is certainly overkill, but it is explicit
        if (person.isCurrentlyExists()){
            existSwitch.setChecked(true);
            v.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.colorScreenBackground));
         } else {
            existSwitch.setChecked(false);
            v.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.colorScreenDeletedBackground));
        }


        //Debug Statments. Remove if you see these
        String temp = String.valueOf(person.getPersonID());
        personIDOutput      .setText(temp);

        temp = person.getNickname()    .toString().trim();
        personNickNameInput .setText(temp);

        temp = person.getEmailAddress().toString().trim();
        personEmailAddrInput.setText(temp);

        temp = person.getTextAddress() .toString().trim();
        personTextAddrInput .setText(temp);

        setUISaved(v);
    }

    private void onSave(){
        Toast.makeText(getActivity(),
                R.string.save_label,
                Toast.LENGTH_SHORT).show();

        //get rid of the soft keyboard
        MMUtilities.hideSoftKeyboard(getActivity());

        View v = getView();
        if (v == null)return;

        EditText personIDOutput       = (EditText) (v.findViewById(R.id.personIdInput));
        EditText personNickNameInput  = (EditText) (v.findViewById(R.id.personNickNameInput));
        EditText personEmailAddrInput = (EditText) (v.findViewById(R.id.personEmailAddrInput));
        EditText personTextAddrInput  = (EditText) (v.findViewById(R.id.personTextAddrInput));


        CharSequence nickname = personNickNameInput.getText();
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

        SwitchCompat existSwitch = (SwitchCompat) getView().findViewById(R.id.switchExists);

        if (existSwitch.isChecked()) {
            person.setCurrentlyExists(true);
            v.setBackgroundColor(ContextCompat.
                    getColor(getActivity(),R.color.colorScreenBackground));
        } else {
            person.setCurrentlyExists(false);
            v.setBackgroundColor(ContextCompat.
                    getColor(getActivity(), R.color.colorScreenDeletedBackground));
        }


        person.setNickname(nickname);

        //strings are set to "" in the constructor, so the empty case can be ignored
        //but do need to know if legal input has been made
        String temp = personEmailAddrInput.getText().toString().trim();
        if (!(temp.isEmpty())){
            person.setEmailAddress(temp);
        }

        temp = personTextAddrInput.getText().toString().trim();
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
            //MMUtilities.enableButton(getActivity(), getAddMedButton(getView()), MMUtilities.BUTTON_ENABLE);
        }
        //if the person is newly created, the ID is assigned on DB add
        mPersonID = returnCode;
        //update the ID field on the UI
        personIDOutput.setText(String.valueOf(mPersonID));
        personIDOutput.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.colorGray));


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


    public long getPersonID(){
        return mPersonID;
    }


    //***********************************/
    //****  Exit Button Dialogue    *****/
    //***********************************/
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
        if (adapter != null)adapter.closeCursor();

        ((MMMainActivity) getActivity()).switchToHomeScreen(mPersonID);   //switchToPopBackstack();

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
/*
    private Button getAddMedButton(View v){
        return (Button) v.findViewById(R.id.personAddMedicationButton);
    }
*/

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

        Button personSaveButton =
                (Button) v.findViewById(R.id.personSaveButton);

        MMUtilities.enableButton(getActivity(),
                                 personSaveButton,
                                 isEnabled);
    }


    //*********************************************************/
    //      Utility Functions used in handling events         //
    //*********************************************************/

    //called from onClick(), executed when a medication is selected
    private void onSelect(int position){
        MMMedicationCursorAdapter adapter = getAdapter(getView());
        //MMMedication selectedMedication = adapter.getMedicationAt(position);

        adapter.notifyItemChanged(position);

        ((MMMainActivity) getActivity()).switchToMedicationScreen(mPersonID, position);
    }



}
