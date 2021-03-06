package com.androidchicken.medminder;

import android.database.Cursor;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
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
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

import static com.androidchicken.medminder.R.id.directoryPath;

/**
 * Created by Elisabeth Huhn on 2/4/2017.
 * This is the UI for defining a filter for Exporting Doses Taken History
 */

public class MMExportHistoryFragment extends Fragment {
    private static final String TAG = "MMExportHistoryFragment";

    private static final int EXPORT_HISTORY       = 0;
    private static final int EXPORT_PRESCRIPTIONS = 1;
    private static final int EXPORT_CDF           = 2;

    private static final int EXPORT_EMAIL         = 0;
    private static final int EXPORT_SMS           = 1;
    private static final int EXPORT_FILE          = 2;
    private static final int EXPORT_GENERAL       = 3;

    private static final int START_FILTER = 0;
    private static final int END_FILTER   = 1;

    //**********************************************/
    /*          UI Widgets                         */
    //**********************************************/

    //**********************************************/
    /*          Static Variables                   */
    //**********************************************/

    private static final String sCDF_FILENAME_TAG  = "cdfFilenameTag" ;
    private static final String sCDF_PATH_TAG      = "cdfPathTag" ;
   // private static final String sCDF_TIMESTAMP_TAG = "cdfTimestampTag" ;

    //**********************************************/
    /*          Member Variables                   */
    //**********************************************/
    private boolean  isUIChanged = false;

    CharSequence mCDFileName ;
    CharSequence mCDFPath ;


    //**********************************************/
    /*          Static Methods                     */
    //**********************************************/



    //**********************************************/
    /*          Constructor                        */
    //**********************************************/
    public MMExportHistoryFragment() {
    }

    //**********************************************/
    /*          Lifecycle Methods                  */
    //**********************************************/

    //pull the arguments out of the fragment bundle and store in the member variables
    //In this case, prepopulate the personID this screen refers to
    @Override
    public void onCreate(Bundle savedInstanceState){

        super.onCreate(savedInstanceState);

        //Initialize the DB if necessary

        try {
            MMDatabaseManager.getInstance(getActivity());
        }catch (Exception e) {

            MMUtilities.getInstance().showStatus(getActivity(),e.getMessage());
            Log.e(TAG,Log.getStackTraceString(e));

        }

    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        if (savedInstanceState != null){
            //the fragment is being restored so restore the person ID
            mCDFileName   = savedInstanceState.getString(sCDF_FILENAME_TAG);
            mCDFPath      = savedInstanceState.getString(sCDF_PATH_TAG);

        }


        //Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_export_history, container, false);

        //Wire up the UI widgets so they can handle events later
        wireWidgets(v);

        initializeUI(v);

        setUIChanged();

        MMMainActivity activity = (MMMainActivity)getActivity();
        if (activity == null) return v;

        activity.isFilePermissionGranted();
        activity.handleFabVisibility();


        return v;
    }


    @Override
    public void onSaveInstanceState(@NonNull Bundle savedInstanceState){
        // Save custom values into the bundle
        savedInstanceState.putString(sCDF_FILENAME_TAG,   mCDFileName.toString());
        savedInstanceState.putString(sCDF_PATH_TAG,       mCDFPath.toString());


        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
    }


    @Override
    public void onResume(){
        super.onResume();

        MMMainActivity activity = (MMMainActivity)getActivity();
        if (activity == null)return;

        //set the title bar subtitle
        activity.setMMSubtitle(R.string.title_export_history);

        //Set the FAB invisible
        activity.hideFAB();

        //get rid of soft keyboard if it is visible
        MMUtilities utilities = MMUtilities.getInstance();
        utilities.hideSoftKeyboard(activity);

        activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

    }



    //*************************************************************/
    /*  Convenience Methods for accessing things on the Activity  */
    //*************************************************************/

    private long     getPatientID(){
        MMMainActivity activity = (MMMainActivity)getActivity();
        if (activity == null)return MMUtilities.ID_DOES_NOT_EXIST;

        return activity.getPatientID();
    }


    //**********************************************/
    /*          Member Methods                     */
    //**********************************************/
    private void   wireWidgets(View v) {
        TextView label;

        final MMMainActivity activity = (MMMainActivity)getActivity();
        if (activity == null)return;

        TextWatcher textWatcher = new TextWatcher() {
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
        };

        //export prescriptions
        Button exportButton = v.findViewById(R.id.exportButton);

        exportButton.setText(R.string.export_label);
        //the order of images here is left, top, right, bottom
        // exportButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_project, 0, 0);
        exportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                onExport();
            }
        });


        label = v.findViewById(R.id.filterStartingDateLabel);
        label.setText(R.string.start_date_label);

        label = v.findViewById(R.id.filterEndingDateLabel);
        label.setText(R.string.end_date_label);

        final EditText filterEndingDateInput = v.findViewById(R.id.filterEndingDate);
        filterEndingDateInput.addTextChangedListener(textWatcher);

        final EditText filterStartingDateInput = v.findViewById(R.id.filterStartingDate);
        filterStartingDateInput.addTextChangedListener(textWatcher);


        label = v.findViewById(R.id.directoryPathLabel);
        label.setText(R.string.directory_path_label);

        final EditText directoryInput = v.findViewById(directoryPath);
        directoryInput.setInputType(InputType.TYPE_TEXT_FLAG_AUTO_CORRECT);
        //directoryInput.setHint(R.string.person_text_addr_hint);
        directoryInput.addTextChangedListener(textWatcher);

        label = v.findViewById(R.id.fileNameLabel);
        label.setText(R.string.export_filename_label);

        final EditText fileNameInput = v.findViewById(R.id.fileName);
        //fileNameInput.setHint(R.string.person_order_hint);
        fileNameInput.addTextChangedListener(textWatcher);

        label = v.findViewById(R.id.fileExtentLabel);
        label.setText(R.string.filename_extent_label);

        final EditText fileNameExtentInput = v.findViewById(R.id.fileExtent);
        fileNameExtentInput.setHint(R.string.extent_hint);
        fileNameExtentInput.addTextChangedListener(textWatcher);


        RadioGroup destinationGroup          = v.findViewById(R.id.radioDestination);
        final RadioButton emailRadio         = v.findViewById(R.id.radioEmail) ;
        final RadioButton textRadio          = v.findViewById(R.id.radioText) ;
        final RadioButton fileRadio          = v.findViewById(R.id.radioFile) ;
        final RadioButton generalRadio       = v.findViewById(R.id.radioGeneral);
        RadioGroup contentGroup              = v.findViewById(R.id.radioContent);
        final RadioButton prescriptionRadio  = v.findViewById(R.id.radioPrescription) ;
        final RadioButton historyRadio       = v.findViewById(R.id.radioHistory) ;

        destinationGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId)
            {
                if((emailRadio.isChecked())   ||
                        (textRadio.isChecked())    ||
                        (generalRadio.isChecked()) ) {
                    directoryInput.setBackgroundColor(ContextCompat.
                                                        getColor(activity, R.color.colorGray));
                    fileNameInput.setBackgroundColor(ContextCompat.
                                                        getColor(activity, R.color.colorGray));
                    fileNameExtentInput.setBackgroundColor(ContextCompat.
                                                        getColor(activity, R.color.colorGray));
                   // directoryInput     .setFocusable(false);
                    directoryInput     .setEnabled  (false);
                    //fileNameInput      .setFocusable(false);
                    fileNameInput      .setEnabled  (false);
                    //fileNameExtentInput.setFocusable(false);
                    fileNameExtentInput.setEnabled  (false);
                } else if(fileRadio.isChecked()) {
                    directoryInput.setBackgroundColor(ContextCompat.
                                                        getColor(activity, R.color.colorWhite));
                    fileNameInput.setBackgroundColor(ContextCompat.
                                                        getColor(activity, R.color.colorWhite));
                    fileNameExtentInput.setBackgroundColor(ContextCompat.
                                                        getColor(activity, R.color.colorWhite));
                    //directoryInput     .setFocusable(true);
                    directoryInput     .setEnabled  (true);
                    //fileNameInput      .setFocusable(true);
                    fileNameInput      .setEnabled  (true);
                    //fileNameExtentInput.setFocusable(true);
                    fileNameExtentInput.setEnabled  (true);
                }
            }
        });


        contentGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId)
            {
                if((prescriptionRadio.isChecked())   ) {
                    filterStartingDateInput.setBackgroundColor(ContextCompat.
                                                        getColor(activity, R.color.colorGray));
                    filterEndingDateInput.setBackgroundColor(ContextCompat.
                                                        getColor(activity, R.color.colorGray));

                    //filterStartingDateInput.setFocusable(false);
                    filterStartingDateInput.setEnabled  (false);
                    //filterEndingDateInput  .setFocusable(false);
                    filterEndingDateInput  .setEnabled  (false);
                } else if(historyRadio.isChecked()) {
                    filterStartingDateInput.setBackgroundColor(ContextCompat.
                                                        getColor(activity, R.color.colorWhite));
                    filterEndingDateInput.setBackgroundColor(ContextCompat.
                                                        getColor(activity, R.color.colorWhite));

                    //filterStartingDateInput.setFocusable(true);
                    filterStartingDateInput.setEnabled  (true);
                    //filterEndingDateInput  .setFocusable(true);
                    filterEndingDateInput  .setEnabled  (true);
                }
            }
        });


        //initialize
        directoryInput.setBackgroundColor(ContextCompat.getColor(activity, R.color.colorGray));
        fileNameInput.setBackgroundColor (ContextCompat.getColor(activity, R.color.colorGray));
        fileNameExtentInput.setBackgroundColor(ContextCompat.
                                                    getColor(activity, R.color.colorGray));
        filterStartingDateInput.setBackgroundColor(ContextCompat.
                                                    getColor(activity, R.color.colorWhite));
        filterEndingDateInput.setBackgroundColor(ContextCompat.
                                                    getColor(activity, R.color.colorWhite));

        directoryInput         .setEnabled(false);
        fileNameInput          .setEnabled(false);
        fileNameExtentInput    .setEnabled(false);
        filterStartingDateInput.setEnabled(true);
        filterEndingDateInput  .setEnabled(true);

        }


    private void   initializeUI(View v){
        //determine if a person is yet associated with the fragment
        if (getPatientID() != MMUtilities.ID_DOES_NOT_EXIST){
            MMPerson patient = MMPersonManager.getInstance().getPerson(getPatientID());
            //if there is a person corresponding to the patientID, put the name up on the screen
            if (patient != null) {
                //Patient Nick Name
                TextView patientNickName = v.findViewById(R.id.historyNickNameLabel);
                //There are no events associated with this field
                patientNickName.setText(patient.getNickname().toString().trim());
            }
        }

        EditText directoryPath = v.findViewById(R.id.directoryPath);
        mCDFPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).toString();

        File pathFile = new File(mCDFPath.toString(), getString(R.string.app_name));
        mCDFPath = pathFile.getAbsolutePath();
/*
        String temp = getDocumentDirectory().getAbsolutePath();

        if (!pathFile.isDirectory()){
            if (!pathFile.mkdirs()){
                MMUtilities.getInstance().errorHandler(activity, R.string.error_unable_to_access_storage);
            }
        }
*/
        directoryPath.setText(mCDFPath);

        EditText fileName = v.findViewById(R.id.fileName);
        mCDFileName   = getFileName(getPatientID());
        fileName.setText(mCDFileName);

        EditText fileExtent = v.findViewById(R.id.fileExtent);
        fileExtent.setText(R.string.export_file_extent);
     }


    private void onExport() {

        MMMainActivity activity = (MMMainActivity)getActivity();
        if (activity == null)return;

        View v = getView();
        if (v == null)return;

        RadioButton emailRadio         = v.findViewById(R.id.radioEmail) ;
        RadioButton textRadio          = v.findViewById(R.id.radioText) ;
        RadioButton fileRadio          = v.findViewById(R.id.radioFile) ;
        RadioButton generalRadio       = v.findViewById(R.id.radioGeneral);
        RadioButton prescriptionRadio  = v.findViewById(R.id.radioPrescription) ;
        RadioButton historyRadio       = v.findViewById(R.id.radioHistory) ;
        RadioButton cdfRadio           = v.findViewById(R.id.radioCdf);

        //set Defaults
        int whatFlag  = EXPORT_EMAIL;
        int whereFlag = EXPORT_PRESCRIPTIONS;

        //Determine what the user specified
        if (emailRadio       .isChecked()) whereFlag = EXPORT_EMAIL;
        if (textRadio        .isChecked()) whereFlag = EXPORT_SMS;
        if (fileRadio        .isChecked()) whereFlag = EXPORT_FILE;
        if (generalRadio     .isChecked()) whereFlag = EXPORT_GENERAL;
        if (historyRadio     .isChecked()) whatFlag  = EXPORT_HISTORY;
        if (prescriptionRadio.isChecked()) whatFlag  = EXPORT_PRESCRIPTIONS;
        if (cdfRadio         .isChecked()) whatFlag  = EXPORT_CDF;


        String message = null;
        String subject = null;
        int statusMsg = R.string.export_label;
        MMUtilities utilities = MMUtilities.getInstance();


        MMPerson patient = MMPersonManager.getInstance().getPerson(getPatientID());
        if (patient == null) return;

        int suffix = R.string.export_history;
        if (whatFlag == EXPORT_PRESCRIPTIONS) {
            message = getPrescript(patient).toString();
            statusMsg = R.string.export_prescription_label;
            subject =
                    String.format(getString(R.string.export_title_prescriptions),
                                  patient.getNickname().toString());
            suffix = R.string.export_prescriptions;
        } else if (whatFlag == EXPORT_HISTORY) {

            long startMilli = getDateFilter(START_FILTER);
            long endMilli = getDateFilter(END_FILTER);
            message = getDoseHistoryTab(patient, startMilli, endMilli).toString();
            statusMsg = R.string.export_history_label;
            subject = String.format(getString(R.string.export_title_history),
                                    patient.getNickname().toString());
            suffix = R.string.export_history;
        } else if (whatFlag == EXPORT_CDF) {

            long startMilli = getDateFilter(START_FILTER);
            long endMilli = getDateFilter(END_FILTER);
            message = getCdfHistoryTab(patient, startMilli, endMilli).toString();
            statusMsg = R.string.export_cdf_label;
            subject = String.format(getString(R.string.export_title_cdf),
                    patient.getNickname().toString());
            suffix = R.string.export_cdf;
        }
        utilities.showStatus(activity, statusMsg);



        String chooser_title =getString(R.string.export_chooser_title);
        if (whereFlag == EXPORT_EMAIL){
            String emailAddr = patient.getEmailAddress().toString();
            if (emailAddr.isEmpty()) {
                utilities.errorHandler(activity, R.string.email_not_defined);
                return;
            }

            //MMUtilities.getInstance().sendEmail(activity, subject, emailAddr, message);
            MMUtilities.getInstance()
                           .exportEmail(activity, subject, emailAddr, message, chooser_title );

        } else if (whereFlag == EXPORT_FILE){
            writeFile(whatFlag, suffix);
            MMUtilities.getInstance().showStatus(activity, R.string.export_file_written);

        } else if (whereFlag == EXPORT_SMS){
            MMUtilities.getInstance().exportSMS(activity, subject, message);
        } else if (whereFlag == EXPORT_GENERAL){
            MMUtilities.getInstance().exportText(activity, subject, message, chooser_title);
        }

    }


    private long getDateFilter(int flag){
        EditText dateView;
        View v = getView();
        if (v == null)return MMUtilities.ID_DOES_NOT_EXIST;

        MMMainActivity activity = (MMMainActivity)getActivity();
        if (activity == null)return MMUtilities.ID_DOES_NOT_EXIST;

        if (flag == START_FILTER){
            dateView   = v.findViewById(R.id.filterStartingDate);
        } else {
            dateView   = v.findViewById(R.id.filterEndingDate);
        }

        String dateString   = dateView  .getText().toString();
        if (dateString.isEmpty())return MMUtilities.ID_DOES_NOT_EXIST;

        Date dateDate = MMUtilitiesTime.convertStringToDate(activity, dateString);
        if (dateDate == null)return MMUtilities.ID_DOES_NOT_EXIST;

        return dateDate.getTime();
    }



    //*********************************************************/
    //      Methods dealing with whether the UI has changed   //
    //*********************************************************/
    private void setUIChanged(){
        isUIChanged = true;
        exportButtonEnable(MMUtilities.BUTTON_ENABLE);

    }


    private void exportButtonEnable(boolean isEnabled){
        View v = getView();
        exportButtonEnable(v, isEnabled);
    }

    private void exportButtonEnable(View v, boolean isEnabled){
        if (v == null)return; //onCreateView() hasn't run yet

        MMMainActivity activity = (MMMainActivity)getActivity();
        if (activity == null)return;

        Button exportButton = v.findViewById(R.id.exportButton);

        MMUtilities utilities = MMUtilities.getInstance();
        utilities.enableButton(activity,  exportButton, isEnabled);
    }



    //***********************************/
    //****    File Methods          *****/
    //***********************************/
    private String getFileName(long patientID) {
        MMPerson patient = MMPersonManager.getInstance().getPerson(patientID);
        if (patient == null)return null;

        // Create an image file name
        return  patient.getNickname().toString();
    }

    public File getDocumentDirectory(){
        //Get the public directory for documents for this app
        return new File(
                //top-level shared/external storage directory for placing files
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
                //getActivity().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
                getString(R.string.app_name));
    }

    private File createPersonCDFile(int suffix) throws IOException {

        MMPerson patient = MMPersonManager.getInstance().getPerson(getPatientID());
        if (patient == null) return null;

        View v = getView();
        if (v == null)return null;

        EditText directoryPath = v.findViewById(R.id.directoryPath);
        mCDFPath = directoryPath.getText();

        EditText fileName      = v.findViewById(R.id.fileName);
        mCDFileName   = fileName.getText() + getString(suffix);

        EditText fileExtent    = v.findViewById(R.id.fileExtent);
        String fileExtentString = fileExtent.getText().toString();



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
        //File mmDirectory = new File(mCDFPath.toString());
        File mmDirectory = getDocumentDirectory();


        //check if the project subdirectory already exists
        if (!mmDirectory.exists()){
            //if not, create it and any intervening directories necessary
            if (!mmDirectory.mkdirs()){
                //Unable to create the file
                return null;
            }
        }

        return File.createTempFile(mCDFileName.toString(),     /*   prefix  */
                                           fileExtentString,   /*   extent  */
                                           mmDirectory );      /* directory */
    }

    private File writeFile(int flag, int suffix){
        File cdfFile = null;
        FileWriter writer;
        View v = getView();
        if (v == null)return null;

        MMMainActivity activity = (MMMainActivity)getActivity();
        if (activity == null)return null;

        if (!MMUtilities.getInstance().isExternalStorageWritable()){
            MMUtilities.getInstance()
                    .errorHandler(activity, R.string.error_unable_to_access_storage);
            return null;
        }

        //must make sure we have permission to write to external storage


        try {

            cdfFile = createPersonCDFile(suffix);
            if (cdfFile == null){
                MMUtilities.getInstance()
                        .errorHandler(activity, R.string.error_unable_to_create_file);
                Log.e(TAG, getActivity().getString(R.string.error_unable_to_create_file));

                return null;
            }
            writer = new FileWriter(cdfFile);

            String message = null;
            MMPerson patient = MMPersonManager.getInstance().getPerson(getPatientID());

            if (flag == EXPORT_PRESCRIPTIONS) {
                message = getPrescript(patient).toString();
            } else if (flag == EXPORT_HISTORY){

                long startMilli = getDateFilter(START_FILTER);
                long endMilli   = getDateFilter(END_FILTER);
                message = getDoseHistoryTab(patient, startMilli, endMilli).toString();
            }
            writer.append(message);

            writer.flush();
            writer.close();
        } catch (IOException e) {
            MMUtilities.getInstance().showStatus(activity,e.getMessage());
            Log.e(TAG, Log.getStackTraceString(e));

        }
        return cdfFile;
    }


    //***************************************/
    //****     Export String Builders   *****/
    //***************************************/
    private StringBuilder getPrescript(MMPerson patient){
        MMMainActivity activity = (MMMainActivity)getActivity();
        StringBuilder prescription = new StringBuilder();
        String ls = System.getProperty("line.separator");

        try {
            //Export the Patient
            prescription.append(patient.shortString());
            prescription.append(ls);


            //export the list of medications
            boolean currentOnly = MMSettings.getInstance().showOnlyCurrentMeds(activity);
            ArrayList<MMMedication> medications = patient.getMedications(currentOnly);
            MMMedication medication;
            int last = medications.size();
            int position = 0;

            if (last > 0 ){
                prescription.append(ls);
                prescription.append(patient.getNickname());
                prescription.append(" takes the following ");
                prescription.append(String.valueOf(last));
                prescription.append("  medications:");
                prescription.append(ls);
            }
            while (position < last){
                medication = medications.get(position);
                prescription.append(medication.shortString());

                //get any schedules attached to this medication
                ArrayList<MMSchedule> schedules = medication.getSchedules();
                if (schedules != null) {
                    int lastSched = schedules.size();
                    int positionSched = 0;
                    MMSchedule schedule;

                    while (positionSched < lastSched){
                        if ((positionSched > 0) && (positionSched != lastSched-1)){
                            prescription.append(", ");
                        }
                        //put "and" in front of last dose of the day, unless there is only one
                        if ((positionSched == lastSched-1) && (lastSched != 1)){
                            prescription.append(" and ");
                        }
                        schedule = schedules.get(positionSched);
                        prescription.append(schedule.getTimeDueString((MMMainActivity)getActivity()));

                        positionSched++;
                    }

                    prescription.append(ls);
                }//end medication

                position++;

            }//end all medications for this person


        } catch (Exception e) {
            MMUtilities.getInstance().showStatus(activity,e.getMessage());
            Log.e(TAG, Log.getStackTraceString(e));

        }
        return prescription;
    }

    private StringBuilder getDoseHistoryTab(MMPerson patient, long startMilli, long endMilli){
        MMMainActivity activity = (MMMainActivity)getActivity();
        String tab_as_string = String.valueOf(Character.toChars(9));
        String lf = System.getProperty("line.separator");

        StringBuilder history = new StringBuilder();
        Cursor concurrentDoseCursor = null;
        try {
            //Export the Patient
            history.append(patient.getNickname());
            history.append(": History of Doses Taken ");// from:");
            if (startMilli > 0) {
                history.append("from: ");

                history.append(lf);
                history.append(MMUtilitiesTime.getDateString(startMilli));
                history.append(" to: ");
                //decrement into the previous day
                history.append(MMUtilitiesTime.getDateString(endMilli - 100));
            }
            history.append(lf);
            history.append(lf);

            //Doses on the concurrent dose are in the same order as medications on the patient
            boolean currentOnly = MMSettings.getInstance().showOnlyCurrentMeds(activity);
            ArrayList<MMMedication> medications = patient.getMedications(currentOnly);
            ArrayList<MMDose> doses;
            MMMedication medication;
            MMDose dose;
            int lastMed = medications.size();
            int positionMed = 0;

            //list medications as title
            if (positionMed < lastMed) {
                history.append("Position of Medications: ");
                history.append(lf);
            }

            while (positionMed < lastMed) {
                medication = medications.get(positionMed);

                history.append(String.valueOf(positionMed));
                history.append(" ");
                history.append(medication.getMedicationNickname());
                history.append(lf);

                positionMed++;
            }

            history.append(lf);

            //Now list the history
            //get all the concurrent doses for this patient
            MMConcurrentDoseManager concurrentDoseManager = MMConcurrentDoseManager.getInstance();
            if ((startMilli <= 0) || (endMilli <= 0)){
                concurrentDoseCursor = concurrentDoseManager.
                                                        getAllConcurrentDosesCursor(getPatientID());
            } else {
                concurrentDoseCursor = concurrentDoseManager.
                                 getAllConcurrentDosesCursor(getPatientID(), startMilli, endMilli);
            }
            MMConcurrentDose concurrentDose;


            int lastCD = concurrentDoseCursor.getCount();
            int positionCD = 0;
            while (positionCD < lastCD) {
                //get the concurrent dose for this position
                concurrentDose = concurrentDoseManager
                        .getConcurrentDoseFromCursor(concurrentDoseCursor, positionCD);

                long timeTaken = concurrentDose.getStartTime();
                if ((startMilli <= 0) || (endMilli <= 0) ||
                    (startMilli < timeTaken) && (timeTaken < endMilli)) {

                    history.append("<");
                    history.append(MMUtilitiesTime.convertMStoDateTimeString(activity, timeTaken));

                    history.append(">");
                    history.append(tab_as_string);

                    //get the medication doses taken at this time
                    doses = concurrentDose.getDoses();
                    //Now list the doses
                    int lastDose = doses.size();
                    int positionDose = 0;
                    long medicationID;
                    while (positionDose < lastDose) {
                        dose = doses.get(positionDose);
                        medicationID = dose.getOfMedicationID();
                        medication = MMMedicationManager.getInstance().
                                                                getMedicationFromID(medicationID);
                        history.append("{");
                        history.append(medication.getMedicationNickname());
                        history.append(" ");
                        history.append(String.valueOf(dose.getAmountTaken()));
                        history.append(" ");
                        history.append(medication.getDoseUnits());
                        history.append("}");
                        history.append(tab_as_string);

                        positionDose++;
                    }
                    history.append(lf);
                }

                positionCD++;
            }

        } catch (Exception e) {
            MMUtilities.getInstance().showStatus(activity,e.getMessage());
            Log.e(TAG, Log.getStackTraceString(e));

        }

        history.append(lf);
        history.append("End of patient history");

        if (concurrentDoseCursor != null)concurrentDoseCursor.close();
        return history;
    }

    private StringBuilder getCdfHistoryTab(MMPerson patient, long startMilli, long endMilli){
        MMMainActivity activity = (MMMainActivity)getActivity();
        String tab_as_string = String.valueOf(Character.toChars(9));
        String lf = System.getProperty("line.separator");

        StringBuilder history = new StringBuilder();
        Cursor concurrentDoseCursor = null;
        try {
            //Export the Patient
            history.append(patient.getNickname());
            history.append(": History of Doses Taken ");// from:");
            if (startMilli > 0) {
                history.append("from: ");

                history.append(lf);
                history.append(MMUtilitiesTime.getDateString(startMilli));
                history.append(" to: ");
                //decrement into the previous day
                history.append(MMUtilitiesTime.getDateString(endMilli - 100));
            }
            history.append(lf);
            history.append(lf);

            //Doses on the concurrent dose are in the same order as medications on the patient
            boolean currentOnly = MMSettings.getInstance().showOnlyCurrentMeds(activity);
            ArrayList<MMMedication> medications = patient.getMedications(currentOnly);
            ArrayList<MMDose> doses;
            MMMedication medication;
            MMDose dose;
            int lastMed = medications.size();
            int positionMed = 0;

            while (positionMed < lastMed) {
                medication = medications.get(positionMed);
                if (positionMed > 0) history.append(", ");
                history.append(medication.getMedicationNickname());
                positionMed++;
            }

            history.append(lf);


            //Now list the history
            //get all the concurrent doses for this patient
            MMConcurrentDoseManager concurrentDoseManager = MMConcurrentDoseManager.getInstance();
            if ((startMilli <= 0) || (endMilli <= 0)){
                concurrentDoseCursor = concurrentDoseManager.
                        getAllConcurrentDosesCursor(getPatientID());
            } else {
                concurrentDoseCursor = concurrentDoseManager.
                        getAllConcurrentDosesCursor(getPatientID(), startMilli, endMilli);
            }
            MMConcurrentDose concurrentDose;


            int lastCD = concurrentDoseCursor.getCount();
            int positionCD = 0;
            while (positionCD < lastCD) {
                //get the concurrent dose for this position
                concurrentDose = concurrentDoseManager
                        .getConcurrentDoseFromCursor(concurrentDoseCursor, positionCD);

                long timeTaken = concurrentDose.getStartTime();
                if ((startMilli <= 0) || (endMilli <= 0) ||
                        (startMilli < timeTaken) && (timeTaken < endMilli)) {

                    history.append(MMUtilitiesTime.convertMStoDateTimeString(activity, timeTaken));
                    history.append(",");
                    history.append(tab_as_string);

                    //get the medication doses taken at this time
                    doses = concurrentDose.getDoses();
                    //Now list the doses
                    int lastDose = doses.size();
                    int positionDose = 0;
                    long medicationID;
                    while (positionDose < lastDose) {
                        dose = doses.get(positionDose);
                        medicationID = dose.getOfMedicationID();
                        medication = MMMedicationManager.getInstance().
                                getMedicationFromID(medicationID);

                        history.append(String.valueOf(dose.getAmountTaken()));
                        history.append(" ");
                        history.append(medication.getDoseUnits());
                        if (positionDose < (lastDose -1)) history.append(",");
                        history.append(tab_as_string);

                        positionDose++;
                    }
                    history.append(lf);
                }

                positionCD++;
            }

        } catch (Exception e) {
            MMUtilities.getInstance().showStatus(activity,e.getMessage());
            Log.e(TAG, Log.getStackTraceString(e));

        }

        history.append(lf);
        history.append("End of patient history");

        if (concurrentDoseCursor != null)concurrentDoseCursor.close();
        return history;
    }


}
