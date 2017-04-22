package com.androidchicken.medminder;


import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.NotificationCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import static android.content.Context.ALARM_SERVICE;
import static com.androidchicken.medminder.MMHomeFragment.sSELECTED_DIALOG_NOT_VISIBLE;


/**
 * The screen for displaying Medication Alerts for a given patient
 */
public class MMMedicationAlertFragment extends Fragment {

    //**********************************************/
    /*        UI Widget Views                      */
    //**********************************************/





    //**********************************************************/
    /*   Variables that need to survive configuration change   */
    //**********************************************************/



    //**********************************************/
    /*        Passed Arguments to this Fragment    */
    //**********************************************/
    private long  mPersonID;
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


    //need to pass a medication into the fragment
    //position is the index of the medication in the person list
    //-1 indicates add new medication
    public static MMMedicationAlertFragment newInstance(long personID){
        //create a bundle to hold the arguments
        Bundle args = new Bundle();

        //It will be some work to make all of the data model serializable
        //so for now, just pass the person values
        args.putLong        (MMPerson.sPersonIDTag,personID);

        MMMedicationAlertFragment fragment = new MMMedicationAlertFragment();

        fragment.setArguments(args);

        return fragment;
    }

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

        Bundle args = getArguments();

        if (args != null) {
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
        View v = inflater.inflate(R.layout.fragment_medication, container, false);

        //Wire up the UI widgets so they can handle events later
        wireWidgets(v);
        wireListTitleWidgets(v);

        initializeRecyclerView(v);
        initializeUI(v);

        return v;
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState){
        // Save custom values into the bundle
        savedInstanceState.putLong(MMPerson.sPersonIDTag, mPersonID);

        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onResume(){
        super.onResume();

        //set the title bar subtitle
        ((MMMainActivity) getActivity()).setMMSubtitle(R.string.title_medication_alert);

        //Set the FAB visible
        ((MMMainActivity) getActivity()).showFAB();


        //hide the soft keyboard if it is visible
        MMUtilities.hideSoftKeyboard(getActivity());
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
    }

    //********************************************/
    //*******   Initialization Methods  **********/
    //********************************************/
    private void wireWidgets(View v){

        Button medicationExitButton = (Button) v.findViewById(R.id.medicationExitButton);
        medicationExitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onExit();
            }
        });

        Button upAlarmNumber = (Button) v.findViewById(R.id.medicationAlertUpButton);
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
        label.setText(R.string.medication_for_label);
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
        Cursor cursor = medicationAlertManager.getAllMedicationAlertsCursor(mPersonID);
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
        if (mPersonID == MMUtilities.ID_DOES_NOT_EXIST){
            throw new RuntimeException(getString(R.string.no_person_alert));
        }

        MMPerson person = MMUtilities.getPerson(mPersonID);

        CharSequence nickname;
        if (person == null) {
            nickname = getString(R.string.no_person_alert);
        } else {
            nickname = person.getNickname().toString().trim();
        }
        TextView forPersonNickname = (TextView) v.findViewById(R.id.medAlertForPersonNickName);
        forPersonNickname.setText(nickname);

        TextView forPersonID       = (TextView) v.findViewById(R.id.medAlertForPersonIDInput);
        forPersonID      .setText(Long.valueOf(mPersonID).toString().trim());

        //
        //# of alerts for this patient
        //

        MMMedicationAlertCursorAdapter adapter = getAdapter(v);

        TextView numAlerts = (TextView) v.findViewById(R.id.medAlertNumInput);
        numAlerts.setText(adapter.getItemCount());
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




    //*********************************************************/
    //      Utility Functions using passed arguments          //
    //*********************************************************/










    //********************************************/
    //*********    Event Handlers       **********/
    //********************************************/


    private void onExit(){
        Toast.makeText(getActivity(),
                R.string.exit_label,
                Toast.LENGTH_SHORT).show();

        MMMedicationAlertCursorAdapter adapter = getAdapter(getView());
        if (adapter != null) adapter.closeCursor();

        ((MMMainActivity) getActivity()).switchToPersonScreen(mPersonID);
    }

    public void handleUpButton(){

        View v = getView();
        if (v == null)return;

        //increment the value on the UI
        TextView medAlertNumInput = (TextView) v.findViewById(R.id.medAlertNumInput);
        int size = Integer.valueOf(medAlertNumInput.getText().toString());
        size++;

        addAlertDialog(mSelectedPosition);


/*
            //Enable the Alarm receiver. It will stay enabled across reboots
            ComponentName receiver = new ComponentName(getActivity(), MMAlarmReceiver.class);
            PackageManager pm = getActivity().getPackageManager();

            pm.setComponentEnabledSetting(receiver,
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                    PackageManager.DONT_KILL_APP);

            //create an Alarm to generate a notification for this scheduled dose
            MMUtilities.setNotificationAlarm(getActivity(), schedule.getTimeDue());
*/

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
            adapter.reinitializeCursor(mPersonID);

        } else {
            msg = msg + " unable to delete";
        }

        Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
    }



    //***********************************/
    //****    Add Alert Dialogue    *****/
    //***********************************/
    //Build and display the alert dialog
    private void addAlertDialog(int selectedPosition){
        
/*

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
*/

        //
        //Build the (Dialog) layout and it's contained views
        // that define the ConcurrentDose and its contained Doses
        //
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View v = inflater.inflate(R.layout.list_row_medication_alerts, null);
        LinearLayout layout = (LinearLayout) v.findViewById(R.id.medAlertLine);


        // TODO: 4/22/2017 add the listeners to the fields 
        // TODO: 4/22/2017 make fields spinners if possible 

       
            


        //Create the AlertDialog to display the current doses to the user
        //and allow the user to update the amounts
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder .setView(v) //The View we just built for the Alert Dialog
                .setTitle(R.string.create_medication_alert)
                .setIcon(R.drawable.ground_station_icon)
                .setMessage(R.string.med_alert_defaults)
                .setPositiveButton(R.string.create_label,
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
                (LinearLayout) ((AlertDialog) dialog).findViewById(R.id.medAlertLine);

        //Add a new medication Alert
        MMMedicationAlert medicationAlert = new MMMedicationAlert();

        TextView medNicknameInput = (TextView)((AlertDialog)dialog).
                                                        findViewById(R.id.medAlertMedNickNameInput);
        TextView patientInput     = (TextView)((AlertDialog)dialog).
                                                            findViewById(R.id.medAlertPersonInput);
        TextView typeInput        = (TextView)((AlertDialog)dialog).
                                                              findViewById(R.id.medAlertTypeInput);
        TextView overdueTimeInput = (TextView)((AlertDialog)dialog).
                                                        findViewById(R.id.medAlertOverdueTimeInput);


        // TODO: 4/22/2017 build the medication Alert


        //reset the selected position so we'll know the dialog is finished
        mSelectedPosition = sSELECTED_DIALOG_NOT_VISIBLE;

        MMMedicationAlertCursorAdapter adapter = getAdapter(getView());
        //reinitialize the cursor
        adapter.reinitializeCursor(mPersonID);
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
        Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();

        mSelectedPosition = position;

        //adapter.notifyItemChanged(position);


    }


    //*********************************/
    //     Alarms and  Notifications  //
    //********************************/
    private void cancelOneAlarm(int minutesSinceMidnight){
        //If this is the only schedule due at this time
        // Remove any alarms for this schedule
        MMSchedMedManager schedMedManager = MMSchedMedManager.getInstance();
        int howManyMedsDue = schedMedManager.howManyDueAt(minutesSinceMidnight);

        //if there is only one medication dose due at this time, delete the alarm
        //if there are more than one due at this time, leave the existing alarm in place
        if (howManyMedsDue == 1) {
            //The alarm is based on when the dose is due
            cancelNotificationAlarms(minutesSinceMidnight);
        }
    }

    private void cancelNotificationAlarms(int minutesSinceMidnight){
        //get the PendingIntent that describes the action we desire,
        // so that it can be performed when the alarm goes off
        PendingIntent pendingIntent = getNotificationAlarmAction(minutesSinceMidnight);

        AlarmManager alarmManager = (AlarmManager) getActivity().getSystemService(ALARM_SERVICE);

        //cancel any previous alarms
        alarmManager.cancel(pendingIntent);
    }

    private PendingIntent getNotificationAlarmAction(int requestCode){
        //explicit intent naming the receiver class
        Intent alarmIntent = new Intent(getActivity(), MMAlarmReceiver.class);
        alarmIntent.putExtra(MMAlarmReceiver.NOTIFICATION_ID, 1);
        alarmIntent.putExtra(MMAlarmReceiver.NOTIFICATION,getNotification());

        //wake up the explicit Activity when the alarm goes off
        //return PendingIntent.getActivity (getActivity(), //context
        //broadcast when the alarm goes off
        return PendingIntent.getBroadcast(getActivity(), //context
                                          requestCode,   //request code
                                          alarmIntent,   //explicit intent to be broadcast
                                          PendingIntent.FLAG_UPDATE_CURRENT);
                                                         //flags that control which unspecified
                                                         // parts of the intent can be supplied
                                                         // when the actual send happens


    }

    private Notification getNotification(){
                /*  Create a Notification Builder  */
        NotificationCompat.Builder builder =
                (NotificationCompat.Builder) new NotificationCompat.Builder(getActivity())
                        .setSmallIcon(R.drawable.ground_station_icon)
                        .setContentTitle(getString(R.string.app_name))
                        .setContentText(getString(R.string.time_to_take))
                        //notification is canceled as soon as it is touched by the user
                        .setAutoCancel(true);

        return builder.build();

    }


}
