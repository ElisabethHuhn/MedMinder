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

public class MainActivity extends AppCompatActivity {

    private static final String sHomeTag        = "HOME";//HOME screen fragment
    private static final String sPersonTag      = "PERSON";
    private static final String sPersonListTag  = "PERSON_LIST";
    private static final String sMedicationTag  = "MEDICATION";
    private static final String sExportTag      = "EXPORT";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();

            }
        });

        //Set the fragment to Dose Taken screen
        //set up fragments
        android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.fragment_container);

        if (fragment == null) {
            //when we first create the activity,
            // the fragment needs to be the home screen
            fragment = new MMHomeFragment();
            fm.beginTransaction()
                    .add(R.id.fragment_container, fragment)
                    .commit();
        }


        //Put Home on the title bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setSubtitle(R.string.title_take_dose);
        }

        //Put HOME in the title bar

        //Initialize the DBManager and open the DB

    }

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

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /***************************************************************/
    /******************** Routines to switch fragments *************/
    /***************************************************************/

    /****** Routine to actually switch the screens *******/
    private void switchScreen(Fragment fragment, String tag) {
        //Need the Fragment Manager to do the swap for us
        android.support.v4.app.FragmentManager fm = getSupportFragmentManager();

        //Are any fragments already being displayed?
        Fragment oldFragment = fm.findFragmentById(R.id.fragment_container);

        if (oldFragment == null) {
            //It shouldn't ever be the case that we got this far with no fragments on the screen,
            // but code defensively
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
        int      title       = R.string.title_take_dose;

        switchScreen(fragment, tag);
        setMMSubtitle(title);
    }

    public void switchToHomeScreen(int personID){
        //replace the fragment with the Home UI

        //Need the Fragment Manager to do the swap for us
        android.support.v4.app.FragmentManager fm = getSupportFragmentManager();

        //clear the back stack
        while (fm.getBackStackEntryCount() > 0){
            fm.popBackStackImmediate();
        }

        Fragment fragment    = MMHomeFragment.newInstance(personID);
        String   tag         = sHomeTag;
        int      title       = R.string.title_take_dose;

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

    public void switchToPersonScreen(int personID){
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
    public void switchToPersonListScreen(){
        //replace the fragment with the list of persons already defined

        Fragment fragment    = new MMPersonListFragment();
        String   tag         = sPersonListTag;
        int      title       = R.string.title_person_list;

        switchScreen(fragment, tag);
        setMMSubtitle(title);
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

    public void switchToMedicationScreen(int personID){
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

    public void switchToMedicationScreen(int personID, int position){
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

    public void switchToExportScreen(int personID){
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
