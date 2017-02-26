package com.androidchicken.medminder;

import android.database.Cursor;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
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
 */

public class MainExportHistoryFragment extends Fragment {
    private static final String TAG = "ExportHistoryFragment";


    /***********************************************/
    /*          UI Widgets                         */
    /***********************************************/
    private Button mEditPatientButton;
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
    private int      mPersonID;

    CharSequence mCDFileName ;
    CharSequence mCDFPath ;
    CharSequence mCDFTimestamp ;

    /***********************************************/
    /*          Static Methods                     */
    /***********************************************/


    //need to pass a person into the fragment
    public static MainExportHistoryFragment newInstance(int personID){
        //create a bundle to hold the arguments
        Bundle args = new Bundle();

        //It will be some work to make all of the data model serializable
        //so for now, just pass the person values
        args.putInt         (MMPerson.sPersonIDTag,personID);

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
            mPersonID = args.getInt(MMPerson.sPersonIDTag);
            MMPersonManager personManager = MMPersonManager.getInstance();

            //If personID can't be found in the list, mPatient will be null
            mPatient = personManager.getPerson(mPersonID);
            /*
            if (mPatient != null) {
                mMedications = mPatient.getMedications();
            }
            */

        }

    }


    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        //Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_export_history, container, false);

        //Wire up the UI widgets so they can handle events later
        wireWidgets(v);

        initializeUI();

        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        return v;
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
        mEditPatientButton = (Button) v.findViewById(R.id.editPatientButton);
        mEditPatientButton.setText(R.string.patient_edit_profile_label);
        //the order of images here is left, top, right, bottom
        // mEditPatientButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_project, 0, 0);
        mEditPatientButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(),
                        R.string.patient_edit_profile_label,
                        Toast.LENGTH_SHORT).show();
                //switch to person screen
                // But the switching happens on the container Activity
                if (mPersonID == 0) {
                    ((MainActivity) getActivity()).switchToPersonScreen();
                } else {
                    //pre-populate
                    ((MainActivity) getActivity()).switchToPersonScreen(mPersonID);
                }

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
                Toast.makeText(getActivity(),
                        R.string.exit_label,
                        Toast.LENGTH_SHORT).show();
                //switch to home screen
                // But the switching happens on the container Activity
                if (mPersonID == 0) {
                    ((MainActivity) getActivity()).switchToHomeScreen();
                } else {
                    //pre-populate
                    ((MainActivity) getActivity()).switchToHomeScreen(mPersonID);
                }

            }
        });

        //Show Persons Button
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
                ((MainActivity) getActivity()).switchToPersonListScreen();
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
        mFilterStartInput.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {


                return false;
            }
        });


        field_container = v.findViewById(R.id.filterEndDate);
        label = (TextView) (field_container.findViewById(R.id.fieldLabel));
        label.setText(R.string.end_date);

        mFilterEndInput = (EditText) (field_container.findViewById(R.id.fieldInput));
        //mFilterEndInput.setHint(R.string.person_email_addr_hint);
        mFilterEndInput.setInputType(InputType.TYPE_CLASS_DATETIME);

        mFilterEndInput.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                return false;
            }
        });


        field_container = v.findViewById(R.id.directoryPath);
        label = (TextView) (field_container.findViewById(R.id.fieldLabel));
        label.setText(R.string.directory_path);

        mDirectoryInput = (EditText) (field_container.findViewById(R.id.fieldInput));
        mDirectoryInput.setInputType(InputType.TYPE_TEXT_FLAG_AUTO_CORRECT);
        //mDirectoryInput.setHint(R.string.person_text_addr_hint);
        mDirectoryInput.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                return false;
            }
        });


        field_container = v.findViewById(R.id.subDirectory);
        label = (TextView) (field_container.findViewById(R.id.fieldLabel));
        label.setText(R.string.sub_directory);

        mSubDirectoryInput = (EditText) (field_container.findViewById(R.id.fieldInput));
        //mSubDirectoryInput.setHint(R.string.person_order_hint);
        mSubDirectoryInput.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                return false;
            }
        });


        field_container = v.findViewById(R.id.fileName);
        label = (TextView) (field_container.findViewById(R.id.fieldLabel));
        label.setText(R.string.person_order_label);

        mFileNameInput = (EditText) (field_container.findViewById(R.id.fieldInput));
        //mFileNameInput.setHint(R.string.person_order_hint);
        mFileNameInput.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                return false;
            }
        });


        field_container = v.findViewById(R.id.fileSuffix);
        label = (TextView) (field_container.findViewById(R.id.fieldLabel));
        label.setText(R.string.filename_suffix);

        mFileNameSuffixInput = (EditText) (field_container.findViewById(R.id.fieldInput));
        mFileNameSuffixInput.setHint(R.string.suffix_hint);
        mFileNameSuffixInput.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                return false;
            }
        });

    }

    private void   initializeUI(){
        //determine if a person is yet associated with the fragment
        if (mPersonID != 0){
            //if there is a person corresponding to the patientID, put the name up on the screen
            if (mPatient != null) {
                mPatientNickName.setText(mPatient.getNickname().toString().trim());
            }
        }
    }

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
