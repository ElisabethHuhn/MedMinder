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
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;

import static com.androidchicken.medminder.R.id.directoryPath;

/**
 * Created by Elisabeth Huhn on 2/4/2017.
 * This is the UI for defining a filter for Exporting Doses Taken History
 */

public class MMExportHistoryFragment extends Fragment {
    private static final String TAG = "ExportHistoryFragment";

    private static final int EXPORT_PRESCRIPTIONS = 0;
    private static final int EXPORT_HISTORY       = 1;

    private static final int EXPORT_EMAIL         = 2;
    private static final int EXPORT_FILE          = 3;

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
            Log.e(TAG,Log.getStackTraceString(e));
        }

    }


    @Override
    public View onCreateView(LayoutInflater inflater,
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

        ((MMMainActivity)getActivity()).isFilePermissionGranted();

        return v;
    }


    @Override
    public void onSaveInstanceState(Bundle savedInstanceState){
        // Save custom values into the bundle
        savedInstanceState.putString(sCDF_FILENAME_TAG,   mCDFileName.toString());
        savedInstanceState.putString(sCDF_PATH_TAG,       mCDFPath.toString());


        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
    }


    @Override
    public void onResume(){
        super.onResume();

        //set the title bar subtitle
        ((MMMainActivity) getActivity()).setMMSubtitle(R.string.title_export_history);

        //Set the FAB invisible
        ((MMMainActivity) getActivity()).hideFAB();

        //get rid of soft keyboard if it is visible
        MMUtilities utilities = MMUtilities.getInstance();
        utilities.hideSoftKeyboard(getActivity());

        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

    }



    //*************************************************************/
    /*  Convenience Methods for accessing things on the Activity  */
    //*************************************************************/

    private long     getPatientID(){return ((MMMainActivity)getActivity()).getPatientID();}

    private MMPerson getPerson()    {return ((MMMainActivity)getActivity()).getPerson();}


    //**********************************************/
    /*          Member Methods                     */
    //**********************************************/
    private void   wireWidgets(View v) {
        TextView label;

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
        Button exportButton = (Button) v.findViewById(R.id.exportButton);

        exportButton.setText(R.string.export_label);
        //the order of images here is left, top, right, bottom
        // exportButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_project, 0, 0);
        exportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //onExport(EXPORT_PRESCRIPTIONS);
                exportDialog();
            }
        });


        label = (TextView)v.findViewById(R.id.filterStartDateLabel);
        label.setText(R.string.start_date_label);

        EditText filterStartDayInput = (EditText) v.findViewById(R.id.filterStartDay);
        filterStartDayInput.addTextChangedListener(textWatcher);

        EditText filterStartMonthInput = (EditText) v.findViewById(R.id.filterStartMonth);
        filterStartMonthInput.addTextChangedListener(textWatcher);

        EditText filterStartYearInput = (EditText) v.findViewById(R.id.filterStartYear);
        filterStartYearInput.addTextChangedListener(textWatcher);


        label = (TextView) (v.findViewById(R.id.filterEndDateLabel));
        label.setText(R.string.end_date_label);

        EditText filterEndDayInput = (EditText) (v.findViewById(R.id.filterEndDay));
        filterEndDayInput.addTextChangedListener(textWatcher);

        EditText filterEndMonthInput = (EditText) (v.findViewById(R.id.filterEndMonth));
        filterEndMonthInput.addTextChangedListener(textWatcher);

        EditText filterEndYearInput = (EditText) (v.findViewById(R.id.filterEndYear));
        filterEndYearInput.addTextChangedListener(textWatcher);

        label = (TextView) (v.findViewById(R.id.directoryPathLabel));
        label.setText(R.string.directory_path_label);

        EditText directoryInput = (EditText) (v.findViewById(directoryPath));
        directoryInput.setInputType(InputType.TYPE_TEXT_FLAG_AUTO_CORRECT);
        //directoryInput.setHint(R.string.person_text_addr_hint);
        directoryInput.addTextChangedListener(textWatcher);

        label = (TextView) (v.findViewById(R.id.fileNameLabel));
        label.setText(R.string.export_filename_label);

        EditText fileNameInput = (EditText) (v.findViewById(R.id.fileName));
        //fileNameInput.setHint(R.string.person_order_hint);
        fileNameInput.addTextChangedListener(textWatcher);

        label = (TextView) (v.findViewById(R.id.fileExtentLabel));
        label.setText(R.string.filename_extent_label);

        EditText fileNameExtentInput = (EditText) (v.findViewById(R.id.fileExtent));
        fileNameExtentInput.setHint(R.string.extent_hint);
        fileNameExtentInput.addTextChangedListener(textWatcher);

    }

    private void   initializeUI(View v){
        //determine if a person is yet associated with the fragment
        if (getPatientID() != MMUtilities.ID_DOES_NOT_EXIST){
            MMPerson patient = MMPersonManager.getInstance().getPerson(getPatientID());
            //if there is a person corresponding to the patientID, put the name up on the screen
            if (patient != null) {
                //Patient Nick Name
                TextView patientNickName = (TextView) v.findViewById(R.id.historyNickNameLabel);
                //There are no events associated with this field
                patientNickName.setText(patient.getNickname().toString().trim());
            }
        }

        EditText directoryPath = (EditText) v.findViewById(R.id.directoryPath);
        mCDFPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).toString();

        File pathFile = new File(mCDFPath.toString(), getString(R.string.app_name));
        mCDFPath = pathFile.getAbsolutePath();
/*
        String temp = getDocumentDirectory().getAbsolutePath();

        if (!pathFile.isDirectory()){
            if (!pathFile.mkdirs()){
                MMUtilities.getInstance().errorHandler(getActivity(), R.string.error_unable_to_access_storage);
            }
        }
*/
        directoryPath.setText(mCDFPath);

        EditText fileName = (EditText) v.findViewById(R.id.fileName);
        mCDFileName   = getFileName(getPatientID());
        fileName.setText(mCDFileName);

        EditText fileExtent = (EditText) v.findViewById(R.id.fileExtent);
        fileExtent.setText(R.string.export_file_extent);
     }


    private void onExit(){
        Toast.makeText(getActivity(),
                R.string.exit_label,
                Toast.LENGTH_SHORT).show();

        //if something has changed in the UI, ask first
        if (isUIChanged){
            //areYouSureExit();
            switchToExit();
        } else {
            switchToExit();
        }
    }

    private void onExport(int whatFlag, int whereFlag) {
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
            long startMilli = getFilter(START_FILTER);
            long endMilli = getFilter(END_FILTER);
            message = getDoseHistoryTab(patient, startMilli, endMilli).toString();
            statusMsg = R.string.export_history_label;
            subject = String.format(getString(R.string.export_title_history),
                                    patient.getNickname().toString());
            suffix = R.string.export_history;
        }

        utilities.showStatus(getActivity(), statusMsg);


        if (whereFlag == EXPORT_EMAIL){
            String emailAddr = patient.getEmailAddress().toString();
            if (emailAddr.isEmpty()) {
                utilities.errorHandler(getActivity(), R.string.email_not_defined);
                return;
            }

            MMUtilities.getInstance().sendEmail(getActivity(), subject, emailAddr, message);
        } else if (whereFlag == EXPORT_FILE){
            writeFile(whatFlag, suffix);
            MMUtilities.getInstance().showStatus(getActivity(), R.string.export_file_written);
        }

    }

    private long getFilter(int flag){
        EditText dayView, monthView, yearView;
        View v = getView();
        if (v == null)return 0;

        if (flag == START_FILTER){
            dayView   = (EditText) v.findViewById(R.id.filterStartDay);
            monthView = (EditText) v.findViewById(R.id.filterStartMonth);
            yearView  = (EditText) v.findViewById(R.id.filterStartYear);
        } else {
            dayView   = (EditText) v.findViewById(R.id.filterEndDay);
            monthView = (EditText) v.findViewById(R.id.filterEndMonth);
            yearView  = (EditText) v.findViewById(R.id.filterEndYear);
        }

        int day, month, year;
        String dayString   = dayView  .getText().toString();
        String monthString = monthView.getText().toString();
        String yearString  = yearView .getText().toString();

        if ((dayString.isEmpty()) || (monthString.isEmpty()) || (yearString.isEmpty())){
            if (flag == START_FILTER) return 0;
            return Long.MAX_VALUE;
        }

        day   = Integer.parseInt(dayString );
        month = Integer.parseInt(monthString );
        year  = Integer.parseInt(yearString );

         if (year > 99){
            MMUtilities.getInstance().errorHandler(getActivity(), R.string.export_year_error);
             if (flag == START_FILTER) return 0;
             return Long.MAX_VALUE;
        }
        year = year + 2000;


        if (month > 12){
            MMUtilities.getInstance().errorHandler(getActivity(), R.string.export_month_error);
            if (flag == START_FILTER) return 0;
            return Long.MAX_VALUE;
        }


        if (((month == 2) && (day > 29)) ||
           (((month == 4)||(month == 6)||(month == 9)||(month == 11)) && (day > 30)) ||
           (((month == 1)||(month == 3)||(month == 5)||(month == 7)||(month == 8)||(month == 10)
                         ||(month == 12)) && (day > 31))){

            MMUtilities.getInstance().errorHandler(getActivity(), R.string.export_day_error);
            if (flag == START_FILTER) return 0;
            return Long.MAX_VALUE;
        }

        month--;//months start with 0 in Android

        //Want the limit to be midnight, so set to first millisecond of the next day
        if (flag == END_FILTER) day++;

        Date date = new GregorianCalendar(year, month, day).getTime();
        return date.getTime();
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

        if (getPatientID() == MMUtilities.ID_DOES_NOT_EXIST) {
            ((MMMainActivity) getActivity()).switchToHomeScreen();
        } else {
            //pre-populate with patientID from Activity level
            ((MMMainActivity) getActivity()).switchToHomeScreen();
        }

    }

    //Build and display the alert dialog
    private void exportDialog(){

        if (getPatientID() == MMUtilities.ID_DOES_NOT_EXIST)return;

        //
        //Build the (Dialog) layout and it's contained views
        // that define the ConcurrentDose and its contained Doses
        //
        LayoutInflater inflater = getActivity().getLayoutInflater();
        //Lint screams about the null here, but a dialog does not know it's parent at inflate time.
        // Thus using null here is correct. Just ignore the lint. I could suppress the lint warning,
        // but ..... for some reason I'm reluctant to suppress warnings.
        View v = inflater.inflate(R.layout.dialog_export, null);

        //load up the text fields in the dialog itself
        int positiveButtonText = R.string.ok;
        int dialogTitle        = R.string.export_dialog_title;
        int dialogMessage      = R.string.export_dialog_message;

        //Create the AlertDialog to display the current doses to the user
        //and allow the user to update the amounts
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder .setView(v) //The View we just built for the Alert Dialog
                .setTitle(dialogTitle)
                .setIcon(R.drawable.ground_station_icon)
                .setMessage(dialogMessage)
                .setPositiveButton(positiveButtonText,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                //which is a constant on DialogInterface
                                //      = BUTTON_POSITIVE or
                                //      = BUTTON_NEGATIVE or
                                //      = BUTTON_NEUTRAL
                                //Save these values
                                onExportDialog(dialog, which);
                            }
                        })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                        MMUtilities.getInstance().showStatus(getActivity(), R.string.pressed_cancel);
                    }
                })
                .show();
    }

    private void onExportDialog(android.content.DialogInterface dialog, int which){
/*
        LinearLayout layout =
                (LinearLayout) ((AlertDialog) dialog).findViewById(R.id.radioExport);
*/
        RadioButton emailHistory      = (RadioButton) ((AlertDialog) dialog).findViewById(R.id.radioEHistory) ;
        RadioButton emailPrescription = (RadioButton) ((AlertDialog) dialog).findViewById(R.id.radioEPrescription) ;
        RadioButton fileHistory       = (RadioButton) ((AlertDialog) dialog).findViewById(R.id.radioFHistory) ;
        RadioButton filePrescription  = (RadioButton) ((AlertDialog) dialog).findViewById(R.id.radioFPrescription) ;

        if (emailHistory     .isChecked()) onExport(EXPORT_HISTORY,       EXPORT_EMAIL);
        if (emailPrescription.isChecked()) onExport(EXPORT_PRESCRIPTIONS, EXPORT_EMAIL);
        if (fileHistory      .isChecked()) onExport(EXPORT_HISTORY,       EXPORT_FILE);
        if (filePrescription .isChecked()) onExport(EXPORT_PRESCRIPTIONS, EXPORT_FILE);
    }

    //*********************************************************/
    //      Methods dealing with whether the UI has changed   //
    //*********************************************************/
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

        Button exportButton = (Button) v.findViewById(R.id.exportButton);

        MMUtilities utilities = MMUtilities.getInstance();
        utilities.enableButton(getActivity(),  exportButton, isEnabled);
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

        EditText directoryPath = (EditText) v.findViewById(R.id.directoryPath);
        mCDFPath = directoryPath.getText();

        EditText fileName      = (EditText) v.findViewById(R.id.fileName);
        mCDFileName   = fileName.getText() + getString(suffix);

        EditText fileExtent    = (EditText) v.findViewById(R.id.fileExtent);
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

        if (!MMUtilities.getInstance().isExternalStorageWritable()){
            MMUtilities.getInstance()
                    .errorHandler(getActivity(), R.string.error_unable_to_access_storage);
            return null;
        }

        //must make sure we have permission to write to external storage


        try {

            cdfFile = createPersonCDFile(suffix);
            if (cdfFile == null){
                MMUtilities.getInstance()
                        .errorHandler(getActivity(), R.string.error_unable_to_create_file);
                return null;
            }
            writer = new FileWriter(cdfFile);

            String message = null;
            MMPerson patient = MMPersonManager.getInstance().getPerson(getPatientID());

            if (flag == EXPORT_PRESCRIPTIONS) {
                message = getPrescript(patient).toString();
            } else if (flag == EXPORT_HISTORY){
                long startMilli = getFilter(START_FILTER);
                long endMilli   = getFilter(END_FILTER);
                message = getDoseHistoryTab(patient, startMilli, endMilli).toString();
            }
            writer.append(message);

            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return cdfFile;
    }


    //***************************************/
    //****     Export String Builders   *****/
    //***************************************/
    private StringBuilder getPrescript(MMPerson patient){
        StringBuilder prescription = new StringBuilder();

        try {
            //Export the Patient
            prescription.append(patient.shortString());
            prescription.append(System.getProperty("line.separator"));


            //export the list of medications
            ArrayList<MMMedication> medications = patient.getMedications();
            MMMedication medication;
            int last = medications.size();
            int position = 0;

            if (last > 0 ){
                prescription.append(System.getProperty("line.separator"));
                prescription.append(patient.getNickname());
                prescription.append(" takes the following ");
                prescription.append(String.valueOf(last));
                prescription.append("  medications:");
                prescription.append(System.getProperty("line.separator"));
            }
            while (position < last){
                medication = medications.get(position);
                prescription.append(medication.shortString());

                //get any schedules attached to this medication
                ArrayList<MMScheduleMedication> schedules = medication.getSchedules();
                if (schedules != null) {
                    int lastSched = schedules.size();
                    int positionSched = 0;
                    MMScheduleMedication schedule;

                    while (positionSched < lastSched){
                        if ((positionSched > 0) && (positionSched != lastSched-1)){
                            prescription.append(", ");
                        }
                        if (positionSched == lastSched-1){
                            prescription.append(" and ");
                        }
                        schedule = schedules.get(positionSched);
                        prescription.append(schedule.shortString((MMMainActivity)getActivity()));

                        positionSched++;
                    }

                    prescription.append(System.getProperty("line.separator"));
                }//end medication

                position++;

            }//end all medications for this person


        } catch (Exception e) {
            e.printStackTrace();
        }
        return prescription;
    }

    private StringBuilder getDoseHistoryTab(MMPerson patient, long startMilli, long endMilli){
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
                history.append(MMUtilities.getInstance().getDateString(startMilli));
                history.append(" to: ");
                //decrement into the previous day
                history.append(MMUtilities.getInstance().getDateString(endMilli - 100));
            }
            history.append(lf);
            history.append(lf);

            //Doses on the concurrent dose are in the same order as medications on the patient
            ArrayList<MMMedication> medications = patient.getMedications();
            ArrayList<MMDose> doses;
            MMMedication medication;
            MMDose       dose;
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
            //reset the positionMed back to the start of the med list
            positionMed = 0;


            //Now list the history
            //get all the concurrent doses for this patient
            MMConcurrentDoseManager concurrentDoseManager =  MMConcurrentDoseManager.getInstance();
            concurrentDoseCursor =
                    concurrentDoseManager.getAllConcurrentDosesCursor(getPatientID());
            MMConcurrentDose concurrentDose;


            int lastCD = concurrentDoseCursor.getCount();
            int positionCD = 0;
            while (positionCD < lastCD) {
                //get the concurrent dose for this position
                concurrentDose = concurrentDoseManager
                        .getConcurrentDoseFromCursor(concurrentDoseCursor, positionCD);

                long timeTaken = concurrentDose.getStartTime();
                if ((startMilli < timeTaken) && (timeTaken < endMilli)) {

                    history.append("<");
                    history.append(MMUtilities.getInstance().getDateTimeStr(timeTaken));
                    history.append(">");
                    history.append(tab_as_string);

                    //get the medication doses taken at this time
                    doses = concurrentDose.getDoses();
                    //Now list the doses
                    // TODO: 5/15/2017 Need to coordinate dose list with med list
                    int lastDose = doses.size();
                    int positionDose = 0;
                    while (positionDose < lastDose) {
                        dose = doses.get(positionDose);
                        medication = medications.get(positionDose);

                        history.append(String.valueOf(dose.getAmountTaken()));
                        history.append(" ");
                        history.append(medication.getDoseUnits());
                        history.append(tab_as_string);

                        positionDose++;
                    }
                    history.append(lf);
                }

                positionCD++;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        history.append(lf);
        history.append("End of patient history");

        if (concurrentDoseCursor != null)concurrentDoseCursor.close();
        return history;
    }


}
