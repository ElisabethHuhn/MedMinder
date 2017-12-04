package com.androidchicken.medminder;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import static android.os.Build.VERSION_CODES.M;

public class MMMainActivity extends AppCompatActivity {

    //Tags for the fragments which are the screens of the app
            static final String sFragmentTag      = "FragmentTag";
            static final String sHomeTag          = "HOME";
            static final String sPersonTag        = "PERSON";
    private static final String sPersonListTag    = "PERSON_LIST";
    private static final String sMedicationTag    = "MEDICATION";
    private static final String sMedAlertTag      = "MED_ALERT";
    private static final String sExportTag        = "EXPORT";
    private static final String sScheduleListTag  = "SCHEDULE_LIST";
    private static final String sHistoryTitleTag  = "HISTORY_TITLE_LIST";
    private static final String sAboutTag         = "ABOUT_LIST";
    private static final String sSettingsTag      = "SETTINGS";


    private static final int SMS_PERMISSIONS_REQUEST_CODE = 0;
    private static final int FILE_PERMISSIONS_REQUEST_CODE = 1;

    private long mPatientID = MMUtilities.ID_DOES_NOT_EXIST;

    //**************************************************************/
    //********** Livecycle methods                     *************/
    //**************************************************************/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        initializeFAB();

        initializeFragment();


        // int buildVersion = Build.VERSION.SDK_INT;

        initializePermissions();

        setMMSubtitle(R.string.title_home);


        //Don't have to check savedInstanceState for mPatientID
        // as it is always saved in the preferences
    }




    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
            Fragment fragment = fm.findFragmentById(R.id.fragment_container);

            if (fragment instanceof MMHomeFragment) {
                ((MMHomeFragment) fragment).onExit();

            } else if (fragment instanceof MMMedicationAlertFragment) {
                ((MMMedicationAlertFragment) fragment).onExit();

            } else if (fragment instanceof MMMedicationFragment) {
                ((MMMedicationFragment) fragment).onExit();
                //switchToHomeScreen();   //switchToPopBackstack();

            } else if (fragment instanceof MMPersonFragment) {
                ((MMPersonFragment) fragment).onExit();

            } else if (fragment instanceof MMPersonListFragment) {
                ((MMPersonListFragment) fragment).onExit();

            } else if (fragment instanceof MMScheduleListFragment) {
                ((MMScheduleListFragment) fragment).onExit();
            }

            onBackPressed();
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed(); // finish();
    }



    //**************************************************************/
    //**********   Permission Methods                  *************/
    //**************************************************************/


    private void initializePermissions(){
        int permissionCheck =
                ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            // MMUtilities.showStatus(this, R.string.med_alert_text_permission);
            PackageManager pm = this.getPackageManager();

            if (pm.hasSystemFeature(PackageManager.FEATURE_TELEPHONY)) {
                MMUtilities.getInstance().showStatus(this, R.string.telephony_supported);
            } else {
                MMUtilities.getInstance().showStatus(this, R.string.teelphony_not_supported);
            }
        }

    }

    public  boolean isSMSPermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            int permissionCheck =
                            ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS);
            if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                //Permission has been granted
                return true;
            } else {
                //Permission has not yet been granted. Ask for it

                ActivityCompat.requestPermissions(this,
                                                  new String[]{Manifest.permission.SEND_SMS},
                                                  SMS_PERMISSIONS_REQUEST_CODE);

                return false;
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
            return true;
        }
    }

    public void isFilePermissionGranted() {
        if (Build.VERSION.SDK_INT >= M) {
            int hasWriteExternalStoragePermission =
                    ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if (hasWriteExternalStoragePermission != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                                FILE_PERMISSIONS_REQUEST_CODE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[],
                                           int[] grantResults) {
        switch (requestCode) {

            case SMS_PERMISSIONS_REQUEST_CODE: {

                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    MMUtilities.getInstance().showStatus(this, "Permission to send text granted");

                    //send sms here call your method
                   // sendSms(String phoneNo, String msg);
                } else {
                    MMUtilities.getInstance().showStatus(this, "Permission to send text DENIED");
                }
            }

            case FILE_PERMISSIONS_REQUEST_CODE: {

                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    MMUtilities.getInstance().showStatus(this, "Permission to write file");
                } else {
                    MMUtilities.getInstance().showStatus(this, "Permission to write file denied");
                }
            }


            // other 'case' lines to check for other
            // permissions this app might request
        }
    }



    public long getPatientID()  {
        if (mPatientID == MMUtilities.ID_DOES_NOT_EXIST) {
            //see if there is anything stored in shared preferences
            mPatientID = MMSettings.getInstance().getPatientID(this);
        }

        return mPatientID;
    }

    public void setPatientID (long patientID){
        long oldPatientID = mPatientID;
        if (oldPatientID != patientID) {
            mPatientID = patientID;
            //Store the PersonID for the next time
            MMSettings.getInstance().setPatientID(this,patientID);
        }
    }

    public MMPerson getPerson(){
        return MMPersonManager.getInstance().getPerson(getPatientID());
    }

    //**************************************************************/
    //********** Methods dealing with the FAB          *************/
    //**************************************************************/
    private void initializeFAB(){
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleFAB(view);
            }
        });
    }

    private void handleFAB(View view){
        android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.fragment_container);

        if (( fragment instanceof MMHomeFragment) ||
             (fragment instanceof MMPersonListFragment)) {
            //Add a new person
            MMUtilities.getInstance().showHint(view, getString(R.string.add_person));
            setPatientID(MMUtilities.ID_DOES_NOT_EXIST);
            switchToPersonScreen();

        } else if (fragment instanceof MMPersonFragment) {
            //Add a new medication
            MMPerson person = getPerson();
            if (person != null) {

                String msg =
                        String.format(getString(R.string.add_medication), person.getNickname());
                MMUtilities.getInstance().showHint(view, msg);

                switchToMedicationScreen((int) MMUtilities.ID_DOES_NOT_EXIST, sPersonTag);
            }

        } else if (fragment instanceof MMMedicationFragment) {
            //Add a new schedule
            MMMedication medication = ((MMMedicationFragment) fragment).getMedicationInstance();
            if (medication != null) {
                String msg = String.format(getString(R.string.add_schedule),
                                                                medication.getMedicationNickname());
                MMUtilities.getInstance().showHint(view, msg);
                ((MMMedicationFragment) fragment).onUpButton();
            }
        } else if (fragment instanceof MMMedicationAlertFragment) {
            //Add a new Medication Alert
            MMUtilities.getInstance().showHint(view, getString(R.string.add_medicaition_alert));
            ((MMMedicationAlertFragment) fragment).handleUpButton();
        } else {
            //do nothing
            Snackbar.make(view, getString(R.string.add_noting), Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();

        }
    }

    public void showFAB(){
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setVisibility(View.VISIBLE);
    }

    public void hideFAB(){
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setVisibility(View.GONE);
    }


    //**************************************************************/
    //*******************          Menu Methods        *************/
    //**************************************************************/
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            switchToSettingsScreen();
            return true;
        } else if (id == R.id.action_home) {
            switchToHomeScreen();
            return true;
        } else if (id == R.id.action_edit_patient) {
            switchToPersonScreen();
            return true;
        } else if (id == R.id.action_switch_patient){

            //Control will pass to the Person List screen, but where control returns to
            //depends on what is currently being displayed

            //get the fragment currently being displayed
             android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
            Fragment fragment = fm.findFragmentById(R.id.fragment_container);

            if (( fragment instanceof MMHomeFragment) ) {
                switchToPersonListScreen(sHomeTag);
            } else if (fragment instanceof MMExportHistoryFragment) {
                switchToPersonListScreen(sExportTag);
            } else if (fragment instanceof MMHistoryTitleLineFragment) {
                switchToPersonListScreen(sExportTag);
            } else {
               //just return to the home screen
                switchToPersonListScreen(sHomeTag);
            }
            return true;
        } else  if (id == R.id.action_list_schedules) {
            switchToScheduleListScreen();
            return true;
        } else  if (id == R.id.action_export) {
            switchToExportScreen();
            return true;
        } else if (id == R.id.action_alert){
            switchToMedicationAlertScreen();
            return true;
        } else if (id == R.id.action_med_help){
            if (getPatientID() != MMUtilities.ID_DOES_NOT_EXIST) {
                //Show the medication positions for the history list
                switchToHistoryTitleScreen();
            }
        } else if (id == R.id.action_about){
            switchToAboutScreen();
            return true;
        } else if (id == R.id.action_midnight_noon){
            MMUtilities.getInstance().showStatus(this, R.string.action_midnight_noon);
            return true;
        }


        return super.onOptionsItemSelected(item);
    }

    //**************************************************************/
    //******************* Routines to switch fragments *************/
    //**************************************************************/


    private void initializeFragment() {

        //Set the fragment to Home screen
        android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.fragment_container);

        if (fragment == null) {
            //when we first create the activity, the fragment needs to be the home screen
            fragment = new MMHomeFragment();
            fm.beginTransaction()
                    .add(R.id.fragment_container, fragment)
                    .commit();
        }
    }

    private Fragment getCurrentFragment(){
        android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
        return fm.findFragmentById(R.id.fragment_container);
    }

    private void clearBackStack(){
        //Need the Fragment Manager to do the swap for us
        android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
        //clear the back stack

        while (fm.getBackStackEntryCount() > 0){
            fm.popBackStackImmediate();
        }
    }

    //***** Routine to actually switch the screens *******/
    private void switchScreen(Fragment fragment, String tag) {
        //clear the back stack
        clearBackStack();

        //Need the Fragment Manager to do the swap for us
        android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
        //Are any fragments already being displayed?
        Fragment oldFragment = fm.findFragmentById(R.id.fragment_container);

        if (oldFragment == null) {
            //It shouldn't ever be the case that we got this far with no fragments on the screen,
            // but code defensively. Who knows how the app will evolve
            fm.beginTransaction()
                    .add(R.id.fragment_container, fragment, tag)
                    .commit();
        } else {
            fm.beginTransaction()
                    //replace whatever is being displayed with the Home fragment
                    .replace(R.id.fragment_container, fragment, tag)
                    //and add the transaction to the back stack
                    .addToBackStack(tag)
                    .commit();
        }
    }


    public void setMMSubtitle(int subtitle){

        //Put the name of the fragment on the title bar

        if (getSupportActionBar() != null){
            getSupportActionBar().setSubtitle(subtitle);
        }


    }




    /****
     * Method to switch fragment to home screen
     * EMH 10/17/16
     */
    public void switchToSettingsScreen(){
        //replace the fragment with the Home UI

        Fragment fragment    = new MMSettingsFragment();
        String   tag         = sSettingsTag;
        int      title       = R.string.title_settings;

        switchScreen(fragment, tag);
        setMMSubtitle(title);
    }


    /****
     * Method to switch fragment to home screen
     * EMH 10/17/16
     */
    public void switchToHomeScreen(){
        //replace the fragment with the Home UI


        Fragment fragment    = new MMHomeFragment();
        String   tag         = sHomeTag;
        int      title       = R.string.title_home;

        switchScreen(fragment, tag);
        setMMSubtitle(title);
    }



    /****
     * Method to switch fragment to History Title Line
     * It defines the order of the medications in the history list of the Home screen
     * EMH 10/17/16
     */
    public void switchToHistoryTitleScreen(){
        //replace the fragment with the Home UI


        Fragment fragment    = new MMHistoryTitleLineFragment();
        String   tag         = sHistoryTitleTag;
        int      title       = R.string.title_history_title_line;

        switchScreen(fragment, tag);
        setMMSubtitle(title);
    }


    /****
     * Method to switch fragment to Edit Person Profile screen
     * EMH 10/17/16
     */
    public void switchToPersonScreen(){
        //replace the fragment with the Home UI

        Fragment fragment    = new MMPersonFragment();
        String   tag         = sPersonTag;
        int      title       = R.string.title_person;

        switchScreen(fragment, tag);
        setMMSubtitle(title);
    }



    /****
     * Method to switch fragment to List Persons screen
     * EMH 10/17/16
     */

    public void switchToPersonListScreen(CharSequence returnTag){
        //replace the fragment with the list of persons already defined


        Fragment fragment    = MMPersonListFragment.newInstance(returnTag);
        String   tag         = sPersonListTag;
        int      title       = R.string.title_person_list;

        switchScreen(fragment, tag);
        setMMSubtitle(title);
    }

    public void switchToPersonListReturn(CharSequence returnFragmentTag){

        if (returnFragmentTag.equals(sHomeTag)){
            switchToHomeScreen();
        } else if (returnFragmentTag.equals(sExportTag)){
            switchToExportScreen();
        } else if (returnFragmentTag.equals(sHistoryTitleTag)){
            switchToHistoryTitleScreen();
        }
    }


    /****
     * Method to switch fragment to Medication screen
     * EMH 10/17/16
     */



    public void switchToMedicationScreen(int position, String returnTag){
        //replace the fragment with the Home UI

        Fragment fragment    = MMMedicationFragment.newInstance(position, returnTag);
        String   tag         = sMedicationTag;
        int      title       = R.string.title_medication;

        switchScreen(fragment, tag);
        setMMSubtitle(title);
    }

    public void switchToMedicationReturn(CharSequence returnFragmentTag){

        if (returnFragmentTag.equals(sPersonTag)){
            switchToPersonScreen();
        }
        //return to the Home screen, even if it's not the tag,
        // because if it's not, there is something wrong and we are trying to recover
        switchToHomeScreen();

    }


    /*****
     * Method to switch fragment to Medication Alert Screen
     * EMH 4/22/2017
     */
    public void switchToMedicationAlertScreen(){
        //replace the fragment with the Home UI

        Fragment fragment    = new MMMedicationAlertFragment();
        String   tag         = sMedAlertTag;
        int      title       = R.string.title_medication_alert;

        switchScreen(fragment, tag);
        setMMSubtitle(title);
    }



    /****
     * Method to switch fragment to Schedule List screen
     * EMH 2/4/17
     */
    public void switchToScheduleListScreen(){

        Fragment fragment    = new MMScheduleListFragment();
        String   tag         = sScheduleListTag;
        int      title       = R.string.title_schedule_list;

        switchScreen(fragment, tag);
        setMMSubtitle(title);
    }

    /****
     * Method to switch fragment to Export screen
     * EMH 2/4/17
     */
    public void switchToExportScreen(){


        Fragment fragment    = new MMExportHistoryFragment();
        String   tag         = sExportTag;
        int      title       = R.string.title_export_history;

        switchScreen(fragment, tag);
        setMMSubtitle(title);
    }

    /****
     * Method to switch fragment to About screen
     * EMH 10/22/17
     */
    public void switchToAboutScreen(){


        Fragment fragment    = new MMAboutFragment();
        String   tag         = sAboutTag;
        int      title       = R.string.title_about;

        switchScreen(fragment, tag);
        setMMSubtitle(title);
    }




}
