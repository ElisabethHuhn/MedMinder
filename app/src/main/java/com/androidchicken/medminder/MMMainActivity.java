package com.androidchicken.medminder;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

public class MMMainActivity extends AppCompatActivity {

    //Tags for the fragments which are the screens of the app
    public  static final String sHomeTag          = "HOME";
    private static final String sPersonTag        = "PERSON";
    private static final String sPersonListTag    = "PERSON_LIST";
    private static final String sMedicationTag    = "MEDICATION";
    private static final String sMedAlertTag      = "MED_ALERT";
    public  static final String sExportTag        = "EXPORT";
    private static final String sScheduleListTag  = "SCHEDULE_LIST";
    public  static final String sHistoryTitleTag  = "HISTORY_TITLE_LIST";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        initializeFAB();

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


        //Put Home on the title bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setSubtitle(R.string.title_home);
        }

    }


    /***************************************************************/
    /*********** Methods dealing with the FAB          *************/
    /***************************************************************/
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

        if (fragment instanceof MMHomeFragment) {
            //Add a new person
            MMUtilities.showHint(view, getString(R.string.add_person));
            switchToPersonScreen();

        } else if (fragment instanceof MMPersonListFragment) {
            //Add a new person
            MMUtilities.showHint(view, getString(R.string.add_person));
            switchToPersonScreen();

        } else if (fragment instanceof MMPersonFragment) {
            //Add a new medication
            long personID = ((MMPersonFragment) fragment).getPersonID();
            MMPerson person = MMUtilities.getPerson(personID);

            String msg =
                    String.format(getString(R.string.add_medication), person.getNickname());
            MMUtilities.showHint(view, msg);

            switchToMedicationScreen(personID);

        } else if (fragment instanceof MMMedicationFragment) {
            //Add a new schedule
            MMMedication medication =
                    ((MMMedicationFragment) fragment).getMedicationInstance();
            if (medication != null) {
                String msg = String.format(getString(R.string.add_schedule),
                        medication.getMedicationNickname());
                MMUtilities.showHint(view, msg);
                ((MMMedicationFragment) fragment).handleUpButton();
            }
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


    /***************************************************************/
    /********************          Menu Methods        *************/
    /***************************************************************/
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
            return true;
        } else  if (id == R.id.action_export) {
            switchToExportScreen();
            return true;
        } else  if (id == R.id.action_list_schedules) {
            switchToScheduleListScreen();
            return true;
        }


        return super.onOptionsItemSelected(item);
    }

    /***************************************************************/
    /******************** Routines to switch fragments *************/
    /***************************************************************/

    private Fragment getCurrentFragment(){
        android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
        return fm.findFragmentById(R.id.fragment_container);
    }

    /****** Routine to actually switch the screens *******/
    private void switchScreen(Fragment fragment, String tag) {
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


    public void switchToPopBackstack(){
        android.support.v4.app.FragmentManager fm = getSupportFragmentManager();

        //settings is at the top of the back stack, so pop it off
        fm.popBackStack();

    }


    public void popToScreen(String tag){
        android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
        //fm.popBackStack(tag, FragmentManager.POP_BACK_STACK_INCLUSIVE);

        boolean stillLooking = true;
        if (fm.getBackStackEntryCount() == 0) stillLooking = false;

        int i;
        CharSequence fragName;
        while (stillLooking){
            i = fm.getBackStackEntryCount()-1;
            fragName = fm.getBackStackEntryAt(i).getName();
            if (fragName.equals(tag)){
                stillLooking = false;
            } else {
                fm.popBackStackImmediate();
                if (fm.getBackStackEntryCount() == 0) stillLooking = false;
            }
        }


    }




    /****
     * Method to switch fragment to home screen
     * EMH 10/17/16
     */
    public void switchToHomeScreen(){
        //replace the fragment with the Home UI

        //Need the Fragment Manager to do the swap for us
        android.support.v4.app.FragmentManager fm = getSupportFragmentManager();

        //clear the back stack
        while (fm.getBackStackEntryCount() > 0){
            fm.popBackStackImmediate();
        }

        Fragment fragment    = new MMHomeFragment();
        String   tag         = sHomeTag;
        int      title       = R.string.title_home;

        switchScreen(fragment, tag);
        setMMSubtitle(title);
    }

    public void switchToHomeScreen(long personID){
        //replace the fragment with the Home UI

        //Need the Fragment Manager to do the swap for us
        android.support.v4.app.FragmentManager fm = getSupportFragmentManager();

        //clear the back stack
        while (fm.getBackStackEntryCount() > 0){
            fm.popBackStackImmediate();
        }

        Fragment fragment    = MMHomeFragment.newInstance(personID);
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
    public void switchToHistoryTitleScreen(long personID){
        //replace the fragment with the Home UI

        //Need the Fragment Manager to do the swap for us
        android.support.v4.app.FragmentManager fm = getSupportFragmentManager();

        //clear the back stack
        while (fm.getBackStackEntryCount() > 0){
            fm.popBackStackImmediate();
        }

        Fragment fragment    = MMHistoryTitleLineFragment.newInstance(personID);
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

        //Need the Fragment Manager to do the swap for us
        android.support.v4.app.FragmentManager fm = getSupportFragmentManager();

        //clear the back stack
        while (fm.getBackStackEntryCount() > 0){
            fm.popBackStackImmediate();
        }

        Fragment fragment    = new MMPersonFragment();
        String   tag         = sPersonTag;
        int      title       = R.string.title_person;

        switchScreen(fragment, tag);
        setMMSubtitle(title);
    }

    public void switchToPersonScreen(long personID){
        //replace the fragment with the Home UI

        //Need the Fragment Manager to do the swap for us
        android.support.v4.app.FragmentManager fm = getSupportFragmentManager();

        //clear the back stack
        while (fm.getBackStackEntryCount() > 0){
            fm.popBackStackImmediate();
        }

        Fragment fragment    = MMPersonFragment.newInstance(personID);
        String   tag         = sPersonTag;
        int      title       = R.string.title_person;

        switchScreen(fragment, tag);
        setMMSubtitle(title);
    }


    /****
     * Method to switch fragment to List Persons screen
     * EMH 10/17/16
     */

    public void switchToPersonListScreen(CharSequence returnTag, long personID){
        //replace the fragment with the list of persons already defined

        Fragment fragment    = MMPersonListFragment.newInstance(returnTag, personID);
        String   tag         = sPersonListTag;
        int      title       = R.string.title_person_list;

        switchScreen(fragment, tag);
        setMMSubtitle(title);
    }

    public void switchToPersonListReturn(CharSequence returnFragmentTag, long personID){

        if (returnFragmentTag.equals(sHomeTag)){
            switchToHomeScreen(personID);
        } else if (returnFragmentTag.equals(sExportTag)){
            switchToExportScreen(personID);
        } else if (returnFragmentTag.equals(sHistoryTitleTag)){
            switchToHistoryTitleScreen(personID);
        }
    }


    /****
     * Method to switch fragment to Medication screen
     * EMH 10/17/16
     */
    public void switchToMedicationScreen(){
        //replace the fragment with the Home UI

        //Need the Fragment Manager to do the swap for us
        android.support.v4.app.FragmentManager fm = getSupportFragmentManager();

        //clear the back stack
        while (fm.getBackStackEntryCount() > 0){
            fm.popBackStackImmediate();
        }

        Fragment fragment    = new MMMedicationFragment();
        String   tag         = sMedicationTag;
        int      title       = R.string.title_medication;

        switchScreen(fragment, tag);
        setMMSubtitle(title);
    }

    public void switchToMedicationScreen(long personID){
        //replace the fragment with the Home UI

        //Need the Fragment Manager to do the swap for us
        android.support.v4.app.FragmentManager fm = getSupportFragmentManager();

        //clear the back stack
        while (fm.getBackStackEntryCount() > 0){
            fm.popBackStackImmediate();
        }

        Fragment fragment    = MMMedicationFragment.newInstance(personID, -1);
        String   tag         = sMedicationTag;
        int      title       = R.string.title_medication;

        switchScreen(fragment, tag);
        setMMSubtitle(title);
    }

    public void switchToMedicationScreen(long personID, int position){
        //replace the fragment with the Home UI

        //Need the Fragment Manager to do the swap for us
        android.support.v4.app.FragmentManager fm = getSupportFragmentManager();

        //clear the back stack
        while (fm.getBackStackEntryCount() > 0){
            fm.popBackStackImmediate();
        }

        Fragment fragment    = MMMedicationFragment.newInstance(personID, position);
        String   tag         = sMedicationTag;
        int      title       = R.string.title_medication;

        switchScreen(fragment, tag);
        setMMSubtitle(title);
    }


    /*****
     * Method to switch fragment to Medication Alert Screen
     * EMH 4/22/2017
     */
    public void switchToMedicationAlertScreen(long personID){
        //replace the fragment with the Home UI

        //Need the Fragment Manager to do the swap for us
        android.support.v4.app.FragmentManager fm = getSupportFragmentManager();

        //clear the back stack
        while (fm.getBackStackEntryCount() > 0){
            fm.popBackStackImmediate();
        }

        Fragment fragment    = MMMedicationAlertFragment.newInstance(personID);
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
        android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.fragment_container);

        long personID = MMUtilities.ID_DOES_NOT_EXIST;;

        //get the specific person ID if we can
        if (fragment instanceof MMHomeFragment) {
            personID = ((MMHomeFragment)fragment).getPersonID();
        } else if (fragment instanceof MMPersonListFragment) {
            personID = ((MMPersonListFragment)fragment).getPersonID();
        } else if (fragment instanceof MMPersonFragment) {
            personID = ((MMPersonFragment) fragment).getPersonID();
        } else if (fragment instanceof MMMedicationFragment) {
            personID = ((MMMedicationFragment) fragment).getPersonID();
        }
        //if the personID is still non-existant, all schedules for all meds for all people
        switchToScheduleListScreen(personID);
    }

    public void switchToScheduleListScreen(long personID){
        //Need the Fragment Manager to do the swap for us
        android.support.v4.app.FragmentManager fm = getSupportFragmentManager();


        Fragment fragment    = MMScheduleListFragment.newInstance(personID);
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

        //Need the Fragment Manager to do the swap for us
        android.support.v4.app.FragmentManager fm = getSupportFragmentManager();

        //clear the back stack
        while (fm.getBackStackEntryCount() > 0){
            fm.popBackStackImmediate();
        }

        Fragment fragment    = new MainExportHistoryFragment();
        String   tag         = sExportTag;
        int      title       = R.string.title_export_history;

        switchScreen(fragment, tag);
        setMMSubtitle(title);
    }

    public void switchToExportScreen(long personID){
        //Need the Fragment Manager to do the swap for us
        android.support.v4.app.FragmentManager fm = getSupportFragmentManager();

        //clear the back stack
        while (fm.getBackStackEntryCount() > 0){
            fm.popBackStackImmediate();
        }

        Fragment fragment    = MainExportHistoryFragment.newInstance(personID);
        String   tag         = sExportTag;
        int      title       = R.string.title_export_history;

        switchScreen(fragment, tag);
        setMMSubtitle(title);
    }


}
