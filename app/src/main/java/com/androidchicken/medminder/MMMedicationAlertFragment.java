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
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;

import static com.androidchicken.medminder.MMHomeFragment.sSELECTED_DIALOG_NOT_VISIBLE;


/**
 * The screen for displaying Medication Alerts for a given patient
 */
public class MMMedicationAlertFragment extends Fragment  {

    //**********************************************/
    /*        UI Widget Views                      */
    //**********************************************/
    Spinner                 mMedSpinner = null;
    ArrayList<MMMedication> mMedications = null;
    ArrayList<String>       mMedicationNames = null;
    int                     mMedSelected = (int)MMUtilities.ID_DOES_NOT_EXIST;

    Spinner             mPersonSpinner = null;
    ArrayList<MMPerson> mPersons = null;
    ArrayList<String>   mPersonNames = null;
    int                 mPersonSelected = (int)MMUtilities.ID_DOES_NOT_EXIST;

    Spinner             mTypeSpinner = null;
    ArrayList<String>   mTypeNames = null;
    int                 mTypeSelected = (int)MMUtilities.ID_DOES_NOT_EXIST;


    EditText mMedIDInput;
    EditText mPersonIDInput;



    //**********************************************************/
    /*   Variables that need to survive configuration change   */
    //**********************************************************/



    //**********************************************/
    /*        Passed Arguments to this Fragment    */
    //**********************************************/

    private int   mSelectedPosition = sSELECTED_DIALOG_NOT_VISIBLE;


    //**********************************************************/
    //*****  Strategy types for Spinner Widgets     **********/
    //**********************************************************/




    //**********************************************/
    /*          RecyclerView Widgets               */
    //**********************************************/
    //all are now local to initializeRecyclerView()


    //**********************************************/
    /*          Static Methods                     */
    //**********************************************/



    //**********************************************/
    /*          Constructor                        */
    //**********************************************/
    public MMMedicationAlertFragment() {
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        //Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_medication_alert, container, false);

        //Wire up the UI widgets so they can handle events later
        wireWidgets(v);
        wireListTitleWidgets(v);

        initializeRecyclerView(v);
        initializeUI(v);

        ((MMMainActivity)getActivity()).isSMSPermissionGranted();
        ((MMMainActivity) getActivity()).handleFabVisibility();


        return v;
    }



    @Override
    public void onResume(){
        super.onResume();

        //set the title bar subtitle
        ((MMMainActivity) getActivity()).setMMSubtitle(R.string.title_medication_alert);

        //Set the FAB visible
        ((MMMainActivity) getActivity()).handleFabVisibility();


        //hide the soft keyboard if it is visible
        MMUtilities utilities = MMUtilities.getInstance();
        utilities.hideSoftKeyboard(getActivity());
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
    }


    //*************************************************************/
    /*  Convenience Methods for accessing things on the Activity  */
    //*************************************************************/

    private long     getPatientID(){return ((MMMainActivity)getActivity()).getPatientID();}

    private MMPerson getPerson()    {return ((MMMainActivity)getActivity()).getPerson();}


    //********************************************/
    //*******   Initialization Methods  **********/
    //********************************************/
    private void wireWidgets(View v){


        Button upAlarmNumber = (Button) v.findViewById(R.id.medAlertUpButton);
        upAlarmNumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleUpButton();
            }
        });

        Button downAlertNumber = (Button) v.findViewById(R.id.medicationAlertDownButton);
        downAlertNumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleDownButton();
            }
        });
    }

    private void wireListTitleWidgets(View v){
        View field_container;
        TextView label;

        MMMainActivity myActivity = (MMMainActivity)getActivity();

        //set up the labels for the medication list
        field_container = v.findViewById(R.id.medAlertTitleRow);

        label = (TextView) (field_container.findViewById(R.id.medAlertMedNickNameInput));
        label.setText(R.string.medication_id_label);
        label.setBackgroundColor(ContextCompat.getColor(myActivity, R.color.colorHistoryLabelBackground));


        label = (TextView) (field_container.findViewById(R.id.medAlertPersonInput));
        label.setText(R.string.medication_alert_notify);
        label.setBackgroundColor(ContextCompat.getColor(myActivity, R.color.colorHistoryLabelBackground));


        label = (TextView) (field_container.findViewById(R.id.medAlertTypeInput));
        label.setText(R.string.medication_alert_type);
        label.setBackgroundColor(ContextCompat.getColor(myActivity, R.color.colorHistoryLabelBackground));


        label = (TextView) (field_container.findViewById(R.id.medAlertOverdueTimeInput));
        label.setText(R.string.medication_alert_time_overdue);
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
        //      done in the caller


        //2) find and remember the RecyclerView
        RecyclerView recyclerView = getRecyclerView(v);

        //3) create and assign a layout manager to the recycler view
        //RecyclerView.LayoutManager mLayoutManager  = new LinearLayoutManager(getActivity());
        RecyclerView.LayoutManager mLayoutManager  = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(mLayoutManager);

        //4) Get the Cursor of MedicationAlert DB rows from the DB.
        MMMedicationAlertManager medicationAlertManager = MMMedicationAlertManager.getInstance();
        Cursor cursor = medicationAlertManager.getAllMedicationAlertsCursor(getPatientID());
        if (cursor == null)return;

        //5) Use the data to Create and set out SchedMed Adapter
        MMMedicationAlertCursorAdapter adapter  = new MMMedicationAlertCursorAdapter(getActivity(), cursor);
        recyclerView.setAdapter(adapter);


        //6) create and set the itemAnimator
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        //7) create and add the item decorator
        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(),
                DividerItemDecoration.VERTICAL));


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
                        }));

    }

    private void initializeUI(View v){

        //
        //Patient ID and Nickname
        //
        if (getPatientID() == MMUtilities.ID_DOES_NOT_EXIST){
            throw new RuntimeException(getString(R.string.no_person_alert));
        }


        MMPerson person = getPerson();

        CharSequence nickname;
        if (person == null) {
            nickname = getString(R.string.no_person_alert);
        } else {
            nickname = person.getNickname().toString().trim();
        }
        TextView forPersonNickname = (TextView) v.findViewById(R.id.medAlertForPersonNickName);
        forPersonNickname.setText(nickname);

        //
        //# of alerts for this patient
        //

        MMMedicationAlertCursorAdapter adapter = getAdapter(v);
        int last = adapter.getItemCount();

        TextView numAlerts = (TextView) v.findViewById(R.id.medAlertNumInput);
        numAlerts.setText(String.valueOf(last));
    }

    //************************************************/
    //*********  UI Saved / Changed  Methods  ********/
    //************************************************/


    //***********************************************************/
    //*********  RecyclerView / Adapter related Methods  ********/
    //***********************************************************/

    private RecyclerView getRecyclerView(View v){
        return  (RecyclerView) v.findViewById(R.id.scheduleList);
    }

    private MMMedicationAlertCursorAdapter getAdapter(View v){
        return (MMMedicationAlertCursorAdapter)  getRecyclerView(v).getAdapter();
    }




    //********************************************/
    //*********    Event Handlers       **********/
    //********************************************/
    public void onExit(){
        //MMUtilities.getInstance().showStatus(getActivity(), R.string.exit_label);

        MMMedicationAlertCursorAdapter adapter = getAdapter(getView());
        if (adapter != null) adapter.closeCursor();
        ((MMMainActivity) getActivity()).switchToHomeScreen();
    }

    public void handleUpButton(){

        View v = getView();
        if (v == null)return;

        addAlertDialog(mSelectedPosition);
    }

    public void handleDownButton(){

        View v = getView();
        if (v == null)return;
        if (mSelectedPosition == sSELECTED_DIALOG_NOT_VISIBLE)return;

        MMMedicationAlertCursorAdapter adapter = getAdapter(v);
        if (adapter.getItemCount() < mSelectedPosition){
            mSelectedPosition = sSELECTED_DIALOG_NOT_VISIBLE;
            return;
        }

        Cursor cursor = adapter.getCursor();
        MMMedicationAlertManager medicationAlertManager = MMMedicationAlertManager.getInstance();

        MMMedicationAlert medicationAlert =
                medicationAlertManager.getMedicationAlertFromCursor(cursor, mSelectedPosition);

        MMMedicationManager medicationManager = MMMedicationManager.getInstance();
        MMMedication medication =
                medicationManager.getMedicationFromID(medicationAlert.getMedicationID());
        CharSequence medNickname = medication.getMedicationNickname();
        String msg = "Alert for medication " + medNickname;
        boolean returnCode = medicationAlertManager
                            .removeMedicationAlertFromDB(medicationAlert.getMedicationAlertID());
        if (returnCode){
            msg = msg + " deleted";
            //Decrement the value in the UI
            TextView medNumInput = (TextView) v.findViewById(R.id.medAlertNumInput);

            //get rid of the last schedule
            int last = Integer.valueOf(medNumInput.getText().toString());
            last--;
            medNumInput.setText(String.valueOf(last));
            adapter.reinitializeCursor(getPatientID());

        } else {
            msg = msg + " unable to delete";
        }

        MMUtilities.getInstance().showStatus (getActivity(), msg);
    }


    //***********************************/
    //****    Add Alert Dialogue    *****/
    //***********************************/
    //Build and display the alert dialog
    private void addAlertDialog(int selectedPosition){

        if (getPatientID() == MMUtilities.ID_DOES_NOT_EXIST)return;

        //
        //Build the (Dialog) layout and it's contained views
        // that define the ConcurrentDose and its contained Doses
        //
        LayoutInflater inflater = getActivity().getLayoutInflater();
        //Lint screams about the null here, but a dialog does not know it's parent at inflate time.
        // Thus using null here is correct. Just ignore the lint. I could suppress the lint warning,
        // but ..... for some reason I'm reluctant to suppress warnings.
        View v = inflater.inflate(R.layout.dialog_medication_alerts, null);
        //LinearLayout layout = (LinearLayout) v.findViewById(R.id.dMedAlertLine);


        mMedIDInput               = (EditText) v.findViewById(R.id.dMedAlertMedID) ;
        mPersonIDInput            = (EditText) v.findViewById(R.id.dMedAlertPersonID) ;
        EditText notifyDay        = (EditText) v.findViewById(R.id.dMedAlertOverdueDayInput) ;
        EditText notifyHour       = (EditText) v.findViewById(R.id.dMedAlertOverdueHourInput) ;
        EditText notifyMinute     = (EditText) v.findViewById(R.id.dMedAlertOverdueMinuteInput) ;


        mMedIDInput.setFocusable(false);
        mPersonIDInput.setFocusable(false);

        loadSpinnerData(v);

        int positiveButtonText;
        int dialogTitle;
        int dialogMessage;
        if (mSelectedPosition == sSELECTED_DIALOG_NOT_VISIBLE){
            //The up button was selected
            // and we want to create a new Alert
            //have to set the ID fields to match the names being displayed in the spinners
            int medPosition = mMedSpinner.getSelectedItemPosition();
            MMMedication medication = mMedications.get(medPosition);
            mMedIDInput.setText(String.valueOf(medication.getMedicationID()));

            int personPosition = mPersonSpinner.getSelectedItemPosition();
            MMPerson person    = mPersons.get(personPosition);
            mPersonIDInput.setText(String.valueOf(person.getPersonID()));

           //let the loadSpinnerData() pick the default type

            //by default: one hour overdue before reminder
            notifyDay.setText(getString(R.string.one_string));
            notifyHour.setText(getString(R.string.zero_string));
            notifyMinute.setText(getString(R.string.zero_string));

            dialogTitle        = R.string.create_medication_alert;
            dialogMessage      = R.string.med_alert_defaults;
            positiveButtonText = R.string.create_label;
        } else {
            //A medication Alert (from the recycler view) was selected,
            // so the user wants to modify it.
            //The (selected position) Medication Alert is loaded into the dialog.
            //Then need to set the spinner selections to the selected MedicationAlert

            MMMedicationAlertCursorAdapter adapter = getAdapter(getView());
            MMMedicationAlert medicationAlert = adapter.getMedAlertAt(mSelectedPosition);



            //Put the medication ID in the dialog
            long selectedMedID      = medicationAlert.getMedicationID();
            mMedIDInput.setText(String.valueOf(selectedMedID));

            //find the selected MedicationAlert in the spinner list
            //mMedications is the list of objects that the spinner list was built from
            //so it's in the same order as the spinner list
            int selectedMedPosition = (int)MMUtilities.ID_DOES_NOT_EXIST;
            int lastMed             = mMedications.size();
            int positionMed         = 0;
            MMMedication medication;

            while (positionMed < lastMed){
                medication = mMedications.get(positionMed);
                if (selectedMedID == medication.getMedicationID()){
                    selectedMedPosition = positionMed;
                    //No need to loop through the rest of the list
                    positionMed = lastMed;
                }
                positionMed++;
            }
            if (selectedMedPosition == ((int)MMUtilities.ID_DOES_NOT_EXIST)){
                MMUtilities.getInstance().errorHandler(getActivity(), R.string.med_not_found );
                return; //can not create or update the medication alert
            }
            //set the initial position of the spinner
            mMedSpinner.setSelection(selectedMedPosition);



            //put the notified person's ID in the dialog view
            long selectedPersonID = medicationAlert.getNotifyPersonID();
            mPersonIDInput.setText(String.valueOf(selectedPersonID));

            //Then determine what position to set the spinner
            //the mPersons list is a list of the Person Objects, and
            // is in the same order as the spinner dropdown list of person names
            int selectedPersonPosition = (int)MMUtilities.ID_DOES_NOT_EXIST;
            int lastPerson             = mPersons.size();
            int positionPerson         = 0;
            MMPerson person;
            while (positionPerson < lastPerson){
                person = mPersons.get(positionPerson);
                if (selectedPersonID == person.getPersonID()){
                    selectedPersonPosition = positionPerson;
                    //no need to loop through the rest of the person list, so skip to the end
                    positionPerson = lastPerson;
                }

                positionPerson++;
            }
            if (selectedPersonPosition == ((int)MMUtilities.ID_DOES_NOT_EXIST)){
                MMUtilities.getInstance().errorHandler(getActivity(), R.string.person_not_found);
                return;  //can not create or update the medication alert
            }
            //set the initial position of the spinner
            mPersonSpinner.setSelection(selectedPersonPosition);

            //set the initial position of the spinner
            mTypeSpinner.setSelection(medicationAlert.getNotifyType());

            //Set the fields for how long the dose must be overdue before the Alert goes off
            int day    = medicationAlert.getOverdueDays();
            int hour   = medicationAlert.getOverdueHours();
            int minute = medicationAlert.getOverdueMinutes();
            notifyDay   .setText(String.valueOf(day));
            notifyHour  .setText(String.valueOf(hour));
            notifyMinute.setText(String.valueOf(minute));

            //load up the text fields in the dialog itself
            positiveButtonText = R.string.update;
            dialogTitle        = R.string.update_medication_alert;
            dialogMessage      = R.string.med_alert_values;
        }

        MMPerson patient = getPerson();
        patient.getNickname();
        String dialogMsg = String.format(getString(dialogMessage) , patient.getNickname());

        //Create the AlertDialog to display the current doses to the user
        //and allow the user to update the amounts
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder .setView(v) //The View we just built for the Alert Dialog
                .setTitle(dialogTitle)
                .setIcon(R.drawable.ic_mortar_black_24dp)
                .setMessage(dialogMsg)
                .setPositiveButton(positiveButtonText,
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
                .setNeutralButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                       onDeleteSelected(dialog,  which);
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                        MMUtilities.getInstance().showStatus(getActivity(), R.string.pressed_cancel);

                        mSelectedPosition = sSELECTED_DIALOG_NOT_VISIBLE; //set flag for dialog gone
                        mPersonSelected = (int)MMUtilities.ID_DOES_NOT_EXIST; //and nothing is selected
                        mMedSelected    = (int)MMUtilities.ID_DOES_NOT_EXIST;
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
        //LinearLayout layout = (LinearLayout) ((AlertDialog) dialog).findViewById(R.id.dMedAlertLine);


        EditText medIDInput      = (EditText)((AlertDialog)dialog).
                findViewById(R.id.dMedAlertMedID);
        EditText personIDInput   = (EditText)((AlertDialog)dialog).
                findViewById(R.id.dMedAlertPersonID);
        EditText overdueDay      = (EditText)((AlertDialog)dialog).
                findViewById(R.id.dMedAlertOverdueDayInput);
        EditText overdueHour     = (EditText)((AlertDialog)dialog).
                findViewById(R.id.dMedAlertOverdueHourInput);
        EditText overdueMinute   = (EditText)((AlertDialog)dialog).
                findViewById(R.id.dMedAlertOverdueMinuteInput);

        if ((medIDInput == null) || (personIDInput == null) ||
                (overdueDay == null) || (overdueHour == null)   || (overdueMinute == null) ){
            MMUtilities.getInstance().errorHandler(getActivity(), R.string.prog_error_views_not_found);
            return;
        }

        //I don't like exceptions to check for normal errors, so check digits first
        String dayString    = overdueDay.getText().toString();
        String hourString   = overdueHour.getText().toString();
        String minuteString = overdueMinute.getText().toString();

        if ((dayString.isEmpty())    || (!TextUtils.isDigitsOnly(dayString))   ||
                (hourString.isEmpty())   || (!TextUtils.isDigitsOnly(hourString))  ||
                (minuteString.isEmpty()) || (!TextUtils.isDigitsOnly(minuteString)) ){
            MMUtilities.getInstance().errorHandler(getActivity(), R.string.error_time_entry);
            return;
        }

        int day     = Integer.decode(dayString);
        int hours   = Integer.decode(hourString);
        int minutes = Integer.decode(minuteString);

        //create / update the MedicationAlert
        MMMedicationAlertCursorAdapter adapter = getAdapter(getView());
        MMMedicationAlert medicationAlert;
        int message;
        if (mSelectedPosition == sSELECTED_DIALOG_NOT_VISIBLE){
            //create a new MedicationAlert
            medicationAlert = new MMMedicationAlert();
            message = R.string.med_alert_created;
        } else {
            //update the MedicationAlert at mSelectedPosition
            medicationAlert = adapter.getMedAlertAt(mSelectedPosition);
            message = R.string.med_alert_updated;
        }

        medicationAlert.setForPatientID(getPatientID());
        medicationAlert.setMedicationID(Long.valueOf(medIDInput.getText().toString()));
        medicationAlert.setNotifyPersonID(Long.valueOf(personIDInput.getText().toString()));


        int notifyTypePosition = mTypeSpinner.getSelectedItemPosition();
        medicationAlert.setNotifyType(notifyTypePosition);


        medicationAlert.setOverdueTime(day, hours, minutes);

        MMMedicationAlertManager medicationAlertManager = MMMedicationAlertManager.getInstance();

        if ((notifyTypePosition == MMMedicationAlert.sNOTIFY_BY_TEXT)&&
                //We need permission to send a text. Assure that we have it.
                (!((MMMainActivity)getActivity()).isSMSPermissionGranted())) {
            message =  R.string.med_alert_text_permission;
            //showSMSPermissionsDialog();

        } else {// TODO: 5/5/2017 need something to check for email permissions here

            //add the alert to the DB
            medicationAlertManager.addMedicationAlert(medicationAlert);
            //Schedule the first overdue text. This will be overridden if the dose is taken


            //  set the alert alarm. When it triggers, an alert will be sent
            MMUtilities.getInstance().createAlertAlarm(getActivity(),
                    medicationAlert.getMedicationAlertID());
        }

        MMUtilities.getInstance().showStatus(getActivity(), message);

        //reset the selected position so we'll know the dialog is finished
        mSelectedPosition = sSELECTED_DIALOG_NOT_VISIBLE;
        mMedSelected      = (int)MMUtilities.ID_DOES_NOT_EXIST;
        mPersonSelected   = (int)MMUtilities.ID_DOES_NOT_EXIST;

        adapter = getAdapter(getView());

        //Restart the fragment so the Medication Alert will show
        adapter.closeCursor();
        ((MMMainActivity)getActivity()).switchToMedicationAlertScreen();
    }

    private void onDeleteSelected(DialogInterface dialog, int which){
        //This routine called from the POSITIVE button of the dialog that
        // invites the user to update the Dose Amounts
        //dialog is the AlertDialog built in onSelectDoseDialog()
        //which is a constant on DialogInterface
        //      = BUTTON_POSITIVE or
        //      = BUTTON_NEGATIVE or
        //      = BUTTON_NEUTRAL

        //Get the pointers to the views in the Dialog
        //LinearLayout layout = (LinearLayout) ((AlertDialog) dialog).findViewById(R.id.dMedAlertLine);


        //delete the MedicationAlert
        MMMedicationAlertCursorAdapter adapter = getAdapter(getView());

        Cursor cursor = adapter.getCursor();
        MMMedicationAlertManager medicationAlertManager = MMMedicationAlertManager.getInstance();

        MMMedicationAlert medicationAlert =
                medicationAlertManager.getMedicationAlertFromCursor(cursor, mSelectedPosition);

        int message;
        if (!medicationAlertManager.removeMedicationAlertFromDB(medicationAlert.getMedicationAlertID())) {
            message =  R.string.medication_alert_unable_to_delete;

        } else {
            message = R.string.medication_alert_delete_successful;
        }

        MMUtilities.getInstance().showStatus(getActivity(), message);

        //reset the selected position so we'll know the dialog is finished
        mSelectedPosition = sSELECTED_DIALOG_NOT_VISIBLE;
        mMedSelected      = (int)MMUtilities.ID_DOES_NOT_EXIST;
        mPersonSelected   = (int)MMUtilities.ID_DOES_NOT_EXIST;

        adapter = getAdapter(getView());

        //Restart the fragment so the Medication Alert will show
        adapter.closeCursor();
        ((MMMainActivity)getActivity()).switchToMedicationAlertScreen();
    }


    //*******************************/
    //     Spinner Methods         //
    //******************************/
    //There are three spinners in the dialog view: medication, person to notify, and notify type.
    private void loadSpinnerData(View v) {
        mMedSpinner    = v.findViewById(R.id.dMedAlertMedNickNameSpinner) ;
        mPersonSpinner = v.findViewById(R.id.dMedAlertPersonSpinner);
        mTypeSpinner   = v.findViewById(R.id.dMedAlertTypeSpinner) ;

        MMPerson patient = getPerson();

        //build spinner dropdown of medication names
        MMMainActivity activity = (MMMainActivity)getActivity();
        boolean currentOnly = MMSettings.getInstance().showOnlyCurrentMeds(activity);
        mMedications = patient.getMedications(currentOnly);

        int lastMed = mMedications.size();
        int positionMed = 0;
        MMMedication medication;
        mMedicationNames = new ArrayList<>();

        while (positionMed < lastMed){
            medication = mMedications.get(positionMed);
            mMedicationNames.add(medication.getMedicationNickname().toString());

            positionMed++;
        }

        //build spinner dropdown of person names

        // TODO: 12/11/2017 this needs to be fixed when Alerts added again
        mPersons = MMPersonManager.getInstance().getPersonList(false);
        int lastPerson = mPersons.size();
        int positionPerson = 0;
        MMPerson person;
        mPersonNames = new ArrayList<>();


        while (positionPerson < lastPerson){
            person = mPersons.get(positionPerson);
            mPersonNames.add(person.getNickname().toString());

            positionPerson++;
        }

        //build spinner dropdown of notification types
        mTypeNames = new ArrayList<>();
        mTypeNames.add(MMMedicationAlert.sNotifyEmailString);
        mTypeNames.add(MMMedicationAlert.sNotifyTextString);



        // Creating adapter for Med spinner
        ArrayAdapter<String> medAdapter = new ArrayAdapter<>(getActivity(),
                                                              android.R.layout.simple_spinner_item,
                                                              mMedicationNames);

        // Creating adapter for person spinner
        ArrayAdapter<String> personAdapter = new ArrayAdapter<>(getActivity(),
                                                                android.R.layout.simple_spinner_item,
                                                                mPersonNames);

        // Creating adapter for Notification Type
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(getActivity(),
                                                               android.R.layout.simple_spinner_item,
                                                               mTypeNames);


        // Drop down layout style - list view with radio button
        medAdapter   .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        personAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        typeAdapter  .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // attaching data adapter to spinner


        mMedSpinner   .setAdapter(medAdapter);
        mPersonSpinner.setAdapter(personAdapter);
        mTypeSpinner  .setAdapter(typeAdapter);


        //attach the listener to the spinner
        mPersonSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                mPersonSelected        = position;
                MMPerson notifyPerson  = mPersons.get(position);
                long notifyPersonID    = notifyPerson.getPersonID();
                mPersonIDInput.setText(String.valueOf(notifyPersonID));

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        //attach the listener to the spinner
        mMedSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                mMedSelected          = position;
                MMMedication alertMed = mMedications.get(position);
                long notifyMedID      = alertMed.getMedicationID();
                mMedIDInput.setText(String.valueOf(notifyMedID));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        //attach the listener to the spinner
        mTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                mTypeSelected        = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }


    //*********************************************************/
    //      Utility Functions used in handling events         //
    //*********************************************************/
    //called from onClick(), executed when a Schedule is selected
    private void onSelect(int position){
        //Tell the user which Alert is selected

        View v = getView();
        if (v == null)return;

        MMMedicationAlertCursorAdapter adapter = getAdapter(v);

        Cursor cursor = adapter.getCursor();
        MMMedicationAlertManager medicationAlertManager = MMMedicationAlertManager.getInstance();

        MMMedicationAlert medicationAlert =
                            medicationAlertManager.getMedicationAlertFromCursor(cursor, position);

        MMMedicationManager medicationManager = MMMedicationManager.getInstance();
        MMMedication medication =
                        medicationManager.getMedicationFromID(medicationAlert.getMedicationID());
        CharSequence medNickname = medication.getMedicationNickname();
        String msg = "Alert for medication " + medNickname + " selected";
        MMUtilities.getInstance().showStatus(getActivity(), msg);

        mSelectedPosition = position;
        addAlertDialog(position);

        //adapter.notifyItemChanged(position);


    }


    //*********************************/
    //     Alarms and  Notifications  //
    //********************************/


}
