package com.androidchicken.medminder;

import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Elisabeth Huhn on 2/4/2017.
 * This is the UI for defining a filter for Exporting Doses Taken History
 */

public class MainExportHistoryFragment extends Fragment {
    private static final String TAG = "ExportHistoryFragment";


    /***********************************************/
    /*          UI Widgets                         */
    /***********************************************/
    private Button mExportButton;
    private Button mSelectPatientButton;
    private Button mExitButton;

    EditText mFilterStartInput;
    EditText mFilterEndInput;
    EditText mDirectoryInput;
    EditText mSubDirectoryInput;
    EditText mFileNameInput;
    EditText mFileNameSuffixInput;


    private TextView mPatientNickName;

    private EditText mTimeInput;



    private Cursor mConcurrentDoseCursor;

    /***********************************************/
    /*          Member Variables                   */
    /***********************************************/
    private MMPerson mPatient;
    private long     mPersonID;
    private boolean  isUIChanged = false;

    CharSequence mCDFileName ;
    CharSequence mCDFPath ;
    CharSequence mCDFTimestamp ;

    /***********************************************/
    /*          Static Methods                     */
    /***********************************************/


    //need to pass a person into the fragment
    public static MainExportHistoryFragment newInstance(long personID){
        //create a bundle to hold the arguments
        Bundle args = new Bundle();

        //It will be some work to make all of the data model serializable
        //so for now, just pass the person values
        args.putLong         (MMPerson.sPersonIDTag,personID);

        MainExportHistoryFragment fragment = new MainExportHistoryFragment();

        fragment.setArguments(args);
        return fragment;
    }


    /***********************************************/
    /*          Constructor                        */
    /***********************************************/

    //
    public MainExportHistoryFragment() {
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
            mPersonID = args.getLong(MMPerson.sPersonIDTag);
            MMPersonManager personManager = MMPersonManager.getInstance();

            //If personID can't be found in the list, mPatient will be null
            mPatient = personManager.getPerson(mPersonID);
            /*
            if (mPatient != null) {
                mMedications = mPatient.getMedications();
            }
            */

        } else {
            mPersonID = MMUtilities.ID_DOES_NOT_EXIST;
            mPatient = null;
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
        View v = inflater.inflate(R.layout.fragment_export_history, container, false);

        //Wire up the UI widgets so they can handle events later
        wireWidgets(v);

        initializeUI();

        //get rid of soft keyboard if it is visible
        MMUtilities.hideSoftKeyboard(getActivity());

        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

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


    @Override
    public void onResume(){
        super.onResume();

        //set the title bar subtitle
        ((MainActivity) getActivity()).setMMSubtitle(R.string.title_export_history);
    }

    /***********************************************/
    /*          Member Methods                     */
    /***********************************************/
    private void   wireWidgets(View v) {
        View field_container;
        TextView label;

        //Patient Profile Button
        mExportButton = (Button) v.findViewById(R.id.exportButton);
        mExportButton.setText(R.string.export_label);
        //the order of images here is left, top, right, bottom
        // mExportButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_project, 0, 0);
        mExportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(),
                        R.string.export_label,
                        Toast.LENGTH_SHORT).show();
                onExport();
            }
        });

        //Exit Button
        mExitButton = (Button) v.findViewById(R.id.exitButton);
        mExitButton.setText(R.string.exit_label);
        //the order of images here is left, top, right, bottom
        // mExitButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_project, 0, 0);
        mExitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onExit();
            }
        });

        //Select Persons Button
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

                ((MainActivity)getActivity()).
                        switchToPersonListScreen(MainActivity.sExportTag, mPersonID);

            }
        });


        //Patient Nick Name
        mPatientNickName = (TextView) v.findViewById(R.id.historyNickNameLabel);
        //There are no events associated with this field


        field_container = v.findViewById(R.id.filterStartDate);
        label = (TextView) (field_container.findViewById(R.id.fieldLabel));
        label.setText(R.string.start_date);

        mFilterStartInput = (EditText) (field_container.findViewById(R.id.fieldInput));

        //mFilterStartInput.setHint(R.string.person_nick_name_hint);
        mFilterStartInput.setInputType(InputType.TYPE_CLASS_DATETIME);
        mFilterStartInput.addTextChangedListener(new TextWatcher() {
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


        field_container = v.findViewById(R.id.filterEndDate);
        label = (TextView) (field_container.findViewById(R.id.fieldLabel));
        label.setText(R.string.end_date);

        mFilterEndInput = (EditText) (field_container.findViewById(R.id.fieldInput));
        //mFilterEndInput.setHint(R.string.person_email_addr_hint);
        mFilterEndInput.setInputType(InputType.TYPE_CLASS_DATETIME);

        mFilterEndInput.addTextChangedListener(new TextWatcher() {
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


        field_container = v.findViewById(R.id.directoryPath);
        label = (TextView) (field_container.findViewById(R.id.fieldLabel));
        label.setText(R.string.directory_path);

        mDirectoryInput = (EditText) (field_container.findViewById(R.id.fieldInput));
        mDirectoryInput.setInputType(InputType.TYPE_TEXT_FLAG_AUTO_CORRECT);
        //mDirectoryInput.setHint(R.string.person_text_addr_hint);
        mDirectoryInput.addTextChangedListener(new TextWatcher() {
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

        field_container = v.findViewById(R.id.subDirectory);
        label = (TextView) (field_container.findViewById(R.id.fieldLabel));
        label.setText(R.string.sub_directory);

        mSubDirectoryInput = (EditText) (field_container.findViewById(R.id.fieldInput));
        //mSubDirectoryInput.setHint(R.string.person_order_hint);
        mSubDirectoryInput.addTextChangedListener(new TextWatcher() {
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


        field_container = v.findViewById(R.id.fileName);
        label = (TextView) (field_container.findViewById(R.id.fieldLabel));
        label.setText(R.string.export_filename);

        mFileNameInput = (EditText) (field_container.findViewById(R.id.fieldInput));
        //mFileNameInput.setHint(R.string.person_order_hint);
        mFileNameInput.addTextChangedListener(new TextWatcher() {
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


        field_container = v.findViewById(R.id.fileSuffix);
        label = (TextView) (field_container.findViewById(R.id.fieldLabel));
        label.setText(R.string.filename_suffix);

        mFileNameSuffixInput = (EditText) (field_container.findViewById(R.id.fieldInput));
        mFileNameSuffixInput.setHint(R.string.suffix_hint);
        mFileNameSuffixInput.addTextChangedListener(new TextWatcher() {
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

    private void   initializeUI(){
        //determine if a person is yet associated with the fragment
        if (mPersonID != MMUtilities.ID_DOES_NOT_EXIST){
            //if there is a person corresponding to the patientID, put the name up on the screen
            if (mPatient != null) {
                mPatientNickName.setText(mPatient.getNickname().toString().trim());
            }
        }
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


    private void onExport(){
        Toast.makeText(getActivity(),
                R.string.export_label,
                Toast.LENGTH_SHORT).show();

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

        if (mPersonID == MMUtilities.ID_DOES_NOT_EXIST) {
            ((MainActivity) getActivity()).switchToHomeScreen();
        } else {
            //pre-populate
            ((MainActivity) getActivity()).switchToHomeScreen(mPersonID);
        }

    }



    /**********************************************************/
    //      Methods dealing with whether the UI has changed   //
    /**********************************************************/
    private void setUIChanged(){
        isUIChanged = true;
        exportButtonEnable(MMUtilities.BUTTON_ENABLE);
    }

    private void setUISaved(){
        isUIChanged = false;

        //disable the save button
        exportButtonEnable(MMUtilities.BUTTON_DISABLE);
    }

    private void setUISaved(View v){
        isUIChanged = false;

        //disable the save button
        exportButtonEnable(v, MMUtilities.BUTTON_DISABLE);
    }

    private void exportButtonEnable(boolean isEnabled){
        View v = getView();
        exportButtonEnable(v, isEnabled);
    }

    private void exportButtonEnable(View v, boolean isEnabled){
        if (v == null)return; //onCreateView() hasn't run yet

        Button exportButton =
                (Button) v.findViewById(R.id.exportButton);

        MMUtilities.enableButton(getActivity(),
                exportButton,
                isEnabled);
    }



    /************************************/
    /*****                          *****/
    /************************************/

    private File createPersonCDFile(MMPerson patient) throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());

        String cdfFileName = patient.getNickname() + "_" + timeStamp + "_";


        /*
        //getExternalStoragePublicDirectory()
        //accessible to all apps and the user
        //with the DIRECTORY_DOCUMENTS argument
        //requires manifest permission
        //<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
        //
        // if use the method: getExternalFilesDir()
        // files are deleted when app uninstalled
        //requires manifest permission:
        //<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"
        //                               android:maxSdkVersion="18" />
        */

        //Assure the path exists
        File mmDirectory = new File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
                //getActivity().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
                "MedMinder");


        //check if the project subdirectory already exists
        if (!mmDirectory.exists()){
            //if not, create it
            if (!mmDirectory.mkdirs()){
                //Unable to create the file
                return null;
            }
        }


        File cdfFile = File.createTempFile(cdfFileName,     /* prefix */
                                           ".txt",          /* suffix */
                                           mmDirectory      /* directory */
        );



        // Save a file: path for use with ACTION_VIEW intents
        mCDFileName   = cdfFileName ;
        mCDFPath      = cdfFile.getAbsolutePath();
        mCDFTimestamp = timeStamp;
        return cdfFile;
    }

    private File writePersonCDF(MMPerson patient){
        File cdfFile = null;
        FileWriter writer = null;
        try {
            cdfFile = createPersonCDFile(patient);
            writer = new FileWriter(cdfFile);

            //Export the Patient
            String cdfString = patient.cdfHeaders();
            writer.append(cdfString);
            cdfString = patient.convertToCDF();
            writer.append(cdfString);

            //export the list of medications
            ArrayList<MMMedication> medications = patient.getMedications();
            MMMedication medication;
            int last = medications.size();
            int position = 0;
            while (position < last){
                medication = medications.get(position);
                cdfString = medication.cdfHeaders();
                writer.append(cdfString);
                cdfString = medication.convertToCDF();
                writer.append(cdfString);
            }

            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return cdfFile;
    }


    private File writeConcDoseCDF(MMPerson patient, File cdfFile){

        FileWriter writer = null;
        try {
            writer = new FileWriter(cdfFile);

            //Export the Patient
            String cdfString = patient.cdfHeaders();
            writer.append(cdfString);
            cdfString = patient.convertToCDF();
            writer.append(cdfString);

            //export the list of medications
            ArrayList<MMMedication> medications = patient.getMedications();
            MMMedication medication;
            int last = medications.size();
            int position = 0;
            while (position < last){
                medication = medications.get(position);
                cdfString = medication.cdfHeaders();
                writer.append(cdfString);
                cdfString = medication.convertToCDF();
                writer.append(cdfString);
            }

            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return cdfFile;
    }

}
