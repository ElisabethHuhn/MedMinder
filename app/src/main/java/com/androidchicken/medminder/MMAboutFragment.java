package com.androidchicken.medminder;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import static com.androidchicken.medminder.R.id.settingPersonNickNameInput;


/**
 * A placeholder fragment containing a simple view.
 */
public class MMAboutFragment extends Fragment {

    //**********************************************/
    //          Constructor                        */
    //**********************************************/
    public MMAboutFragment() {

    }

    //**********************************************/
    //          Static Methods                     */
    //**********************************************/


    //**********************************************/
    //          Static Constants                   */
    //**********************************************/

    //**********************************************/
    //         Member Constants                    */
    //**********************************************/
    private static final String mWhoShortStmt = "Mobile Gurus Inc.";
    private static final String mWhoStmt     = "Mobile Gurus Inc.\n" +
            "mobilegurusinc@gmail.com\n";

    private static final String mPrivacyShortStmt = "Statement of Privacy";
    private static final String mPrivacyStmt = "Privacy Policy\n" +
            "MedMinder \n" +
            "Version October 31, 2017 \n" +
            "\n" +
            "\n" +
            "At Mobile Gurus Inc, we collect and manage user data according to the following Privacy Policy.\n" +
            "\n" +
            "All MedMinder patient and medication information is stored on the user handset in the private MedMinder directory, ONLY. \n" +
            "We do not keep any user information on any remote servers, company or otherwise. \n" +
            "Mobile Gurus Inc does not collect nor will we ever share your personal information, unless legally required to. \n" +
            "\n" +
            "\n" +
            "1) WHAT THIS POLICY COVERS \n" +
            "This policy covers how we treat the information, including personal information, that we receive when you use MedMinder. \n" +
            "\n" +
            "By using MedMinder, you are accepting the practices described in this privacy policy.\n" +
            "\n" +
            "\"Personal information\" is information that can be linked to you as an individual, like your name, address, e-mail address, or phone number.\n" +
            "\n" +
            "2) INFORMATION YOU KNOWINGLY GIVE TO US \n" +
            "We do store patient profile information such as a nick name, and prescribed medications on the user handset. We also collect and store a history of when such medications are taken. All information collected by MedMinder is stored in the private MedMinder directory on the handset. No information is ever transmitted to remote servers. \n" +
            "\n" +
            "3) NO PERSONAL INFORMATION IS AUTOMATICALLY COLLECTED BY OUR SYSTEM \n" +
            "We do not receive nor do we store any types of personal information when you interact with MedMinder. The only information we collect is requested explicitly through the user interface of MedMinder. We never collect any information without your explicit knowledge.\n" +
            "\n" +
            "Because there is never any connection between your handset and any remote servers, we have no need for cookies, so none are ever recorded on your handset. Neither do we collect or log any technical information about your visit, such as IP (internet protocol) addresses, Browser type, ISP (internet service provider), or referring and exit pages. \n" +
            "\n" +
            "4) NO THIRD PARTIES PROVIDING SERVICES ON OUR BEHALF \n" +
            "We never use third party vendors to perform any aspects of our services, such as identity verifications. So we never provide any information about you to any third parties. \n" +
            "\n" +
            "5) WHAT WE DO WITH INFORMATION \n" +
            "You may explicitly choose to export dose history information to a file, a text message or to email. But this is under your direct control. \n" +
            "\n" +
            "We do not rent, sell, or share your personal information with any other entity, ever, with the sole exception of when we legally are required to. Even so, such information is on your local handset and not ever on our servers. So we do not have access to information you provide to MedMinder. \n" +
            "\n" +
            "\n" +
            "Even when we must respond to subpoenas, court orders, or other legal processes, we do not have access to the information on your local handset. \n" +
            "\n" +
            "If you choose to uninstall MedMinder, any patient profiles and histories are permanently removed from the handset. If you reinstall, you must reenter all such information. \n" +
            "\n" +
            "6) CONFIDENTIALITY AND SECURITY \n" +
            "The only way to achieve access to information entered into MedMinder, other than through the MedMinder user interface, is to root your handset and directly access the MedMinder database in the private MedMinder directory. This is a difficult process, made even more so by the need for the MedMinder database schema. Although difficult, it is not an impossible technical task. However, accessing data in this manner violates our EULA with you and is grounds for termination of your license to use MedMinder.\n" +
            "\n" +
            "None of your data ever resides on Mobile Gurus Inc servers, so it is never visible to any of our employees or contractors. \n" +
            "\n" +
            "7) CHANGES TO THIS PRIVACY POLICY \n" +
            "We may update this policy from time to time. In such event, we will post a notification in the settings menu so you may be made aware of the changes. The date of the version of this agreement is stated at the top of the policy. \n" +
            " \n";

    private static final String mEulaShortStmt = "End-User License Agreement (EULA)";
    private static final String mEulaStmt    = "End-User License Agreement (EULA) of MedMinder \n" +
            "\n" +
            "This End-User License Agreement (\"EULA\") is a legal agreement between you and Mobile Gurus Inc \n" +
            "\n" +
            "This EULA agreement governs your acquisition and use of our MedMinder  software (\"Software\") directly from Mobile Gurus Inc  or indirectly through a Mobile Gurus Inc  authorized reseller or distributor (a \"Reseller\").\n" +
            "\n" +
            "Please read this EULA agreement carefully before completing the installation process and using the MedMinder  software. It provides a license to use the MedMinder  software and contains warranty information and liability disclaimers.\n" +
            "\n" +
            "If you register for a free trial of the MedMinder  software, this EULA agreement will also govern that trial. By clicking \"accept\" or installing and/or using the MedMinder  software, you are confirming your acceptance of the Software and agreeing to become bound by the terms of this EULA agreement.\n" +
            "\n" +
            "If you are entering into this EULA agreement on behalf of a company or other legal entity, you represent that you have the authority to bind such entity and its affiliates to these terms and conditions. If you do not have such authority or if you do not agree with the terms and conditions of this EULA agreement, do not install or use the Software, and you must not accept this EULA agreement.\n" +
            "\n" +
            "This EULA agreement shall apply only to the Software supplied by Mobile Gurus Inc  herewith regardless of whether other software is referred to or described herein. The terms also apply to any Mobile Gurus Inc  updates, supplements, Internet-based services, and support services for the Software, unless other terms accompany those items on delivery. If so, those terms apply.\n" +
            "\n" +
            "License Grant\n" +
            "\n" +
            "Mobile Gurus Inc  hereby grants you a personal, non-transferable, non-exclusive licence to use the MedMinder  software on your devices in accordance with the terms of this EULA agreement.\n" +
            "\n" +
            "You are permitted to load the MedMinder  software on a mobile or tablet under your control. You are responsible for ensuring your device meets the minimum requirements of the MedMinder  software.\n" +
            "\n" +
            "You are not permitted to:\n" +
            " Edit, alter, modify, adapt, translate or otherwise change the whole or any part of the Software nor permit the whole or any part of the Software to be combined with or become incorporated in any other software, nor decompile, disassemble or reverse engineer the Software or attempt to do any such things\n" +
            "Reproduce, copy, distribute, resell or otherwise use the Software for any commercial purpose\n" +
            "Allow any third party to use the Software on behalf of or for the benefit of any third party\n" +
            "Use the Software in any way which breaches any applicable local, national or international law\n" +
            "use the Software for any purpose that Mobile Gurus Inc  considers is a breach of this EULA agreement\n" +
            "Use MedMinder or respond to any MedMinder notifications in any way that distracts or prevents you from obeying any traffic or safety laws or policies, including, but not limited to, use while driving or operating heavy machinery;\n" +
            "Use MedMinder to Abuse or harass any other user or person\n" +
            "\n" +
            "Intellectual Property and Ownership\n" +
            "\n" +
            "Mobile Gurus Inc  shall at all times retain ownership of the Software as originally downloaded by you and all subsequent downloads of the Software by you. The Software (and the copyright, and other intellectual property rights of whatever nature in the Software, including any modifications made thereto) are and shall remain the property of Mobile Gurus Inc .\n" +
            "\n" +
            "Mobile Gurus Inc  reserves the right to grant licences to use the Software to third parties.\n" +
            "\n" +
            "Termination\n" +
            "\n" +
            "This EULA agreement is effective from the date you first use the Software and shall continue until terminated. You may terminate it at any time upon written notice to Mobile Gurus Inc .\n" +
            "\n" +
            "It will also terminate immediately if you fail to comply with any term of this EULA agreement. Upon such termination, the licenses granted by this EULA agreement will immediately terminate and you agree to stop all access and use of the Software. The provisions that by their nature continue and survive will survive any termination of this EULA agreement.\n" +
            "\n" +
            "\n" +
            "Disclaimer of Warranties.  MedMinder IS PROVIDED \"AS-IS\" AND “AS AVAILABLE” BASIS. WE EXPRESSLY DISCLAIM ANY WARRANTIES OF ANY KIND, WHETHER EXPRESS, IMPLIED OR STATUTORY, TO THE FULLEST EXTENT PROVIDED BY LAW, INCLUDING, BUT NOT LIMITED TO, ANY WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, TITLE OR NON-INFRINGEMENT OR ANY REGARDING AVAILABILITY, USEFULNESS, RELIABILITY OR ACCURACY OF THE CONTENT AND SERVICES. Mobile Gurus Inc DOES NOT WARRANT OR MAKE ANY REPRESENTATIONS REGARDING THE USE OR THE CONSEQUENCES OF THE USE OF MedMinder.  WITHOUT LIMITING THE FOREGOING, WE DO NOT WARRANT THAT MedMinder WILL BE UNINTERRUPTED,ERROR-FREE OR SUBJECT TO OTHER LIMITATIONS.\n" +
            "\n" +
            "\n" +
            "Limitation of Liability.  WE ASSUME NO LIABILITY OR RESPONSIBILITY FOR ANY ERRORS OR OMISSIONS ARISING FROM THE USE OF MedMinder CONTENT OR SERVICES; ANY FAILURES, DELAYS OR INTERRUPTIONS IN THE DELIVERY OF ANY CONTENT OR SERVICE CONTAINED ON OUR SERVERS; OR LOSS OR DAMAGES ARISING FROM THE USE OF THE CONTENT OR SERVICE PROVIDED BY MedMinder. IN NO EVENT WILL WE BE LIABLE TO YOU OR ANY OTHER PERSON FOR ANY INDIRECT, CONSEQUENTIAL, EXEMPLARY, INCIDENTAL, SPECIAL OR PUNITIVE DAMAGES, INCLUDING BUT NOT LIMITED TO, LOST PROFITS ARISING OUT OF YOUR USE OF, OR INABILITY TO USE, MedMinder EVEN IF WE HAVE BEEN ADVISED AS TO THE POSSIBILITY OF SUCH DAMAGES. UNDER NO CIRCUMSTANCES SHALL OUR TOTAL LIABILITY TO YOU FOR ANY CLAIM OR CAUSE OF ACTION WHATSOEVER, AND REGARDLESS OF THE FORM OF ACTION, WHETHER ARISING IN CONTRACT, TORT OR OTHERWISE, EXCEED THE AMOUNT OF ANY FEES PAID TO OR RECEIVED BY YOU , IF ANY, IN EACH CASE DURING THE 90 DAY PERIOD IMMEDIATELY PRECEDING THE DATE ON WHICH YOU ASSERT ANY SUCH CLAIM. THE FOREGOING LIMITATIONS SHALL APPLY TO THE FULLEST EXTENT PERMITTED BY APPLICABLE LAW.\n" +
            "\n" +
            "\n" +
            "Governing Law\n" +
            "\n" +
            "This EULA agreement, and any dispute arising out of or in connection with this EULA agreement, shall be governed by and construed in accordance with the laws of the State of Georgia.  We each irrevocably consent to bring any action to enforce this Agreement in the federal or state courts located in Atlanta, Georgia. You consent to the exclusive jurisdiction of the federal or state courts located in Atlanta, Georgia for such purposes.\n" +
            "\n";

    private static final String mTipsShortStmt = "Home Screen Tips" ;
    private static final String mTipsStmt = "Home Screen Tips\n" +
            "There are shortcuts to the patient profile screen and to the medication prescription screen. \n" +
            "O Long press on patient name will take you to the Patient Profile Screen\n" +
            "O Long press on a medication button will take you to the Medication Prescription Screen\n" +
            "\n" +
            "Because of the way Android works, if you bring up the MedMinder app from the recent apps list, the current time field may be old. (It is because the MedMinder app has resided in memory and is not brought up from scratch). Long press on the time field will result in an update of that field to the current time.\n" +
            "\n" +
            "Because of all the active areas on the Home screen, sometimes vertical scrolling is tricky. If you press, hesitate, then swipe up or down the screen will scroll through the historical dosage data.\n" +
            "\n" +
            "The shading of dates is purely aesthetic to allow you to tell the days apart more easily. The algorithm is not sophisticated: odd days are white, even days are shaded. Occasionally there are two successive days that aren’t distinguished, for example at the end of a month with 31 days. Both the 31st and the 1st are odd. Shading is controlled from the settings screen. \n" +
            "\n";

    //**********************************************/
    //          Lifecycle Methods                  */
    //**********************************************/

    //pull the arguments out of the fragment bundle and store in the member variables
    //In this case, prepopulate the personID this screen refers to
    @Override
    public void onCreate(Bundle savedInstanceState){

        super.onCreate(savedInstanceState);

        //initialize the DB, providing it with a context
        //MMDatabaseManager.getInstance(getActivity());
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        //Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_about, container, false);

        //Wire up the UI widgets so they can handle events later
        wireWidgets(v);

        initializeUI(v);

        //set the title bar subtitle
        ((MMMainActivity) getActivity()).setMMSubtitle(R.string.title_about);
        ((MMMainActivity) getActivity()).handleFabVisibility();

        return v;
    }

    @Override
    public void onResume(){

        super.onResume();
        //MMUtilities.clearFocus(getActivity());


        if (getOrientation() == Configuration.ORIENTATION_LANDSCAPE) {

            //get rid of the soft keyboard if it is visible
            View v = getView();
            if (v != null) {
                EditText personNickNameInput = v.findViewById(settingPersonNickNameInput);
                MMUtilities.getInstance().showSoftKeyboard(getActivity(), personNickNameInput);
            }
        } else {
            //get rid of the soft keyboard if it is visible
            MMUtilities.getInstance().hideSoftKeyboard(getActivity());
        }


        //set the title bar subtitle
        ((MMMainActivity) getActivity()).setMMSubtitle(R.string.title_about);

        //Set the FAB invisible
        ((MMMainActivity) getActivity()).hideFAB();
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

        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
    }




    //*************************************************************/
    /*                    Initialization Methods                  */
    //*************************************************************/

    private void wireWidgets(View v){
        //Who Button
        final Button whoButton =  v.findViewById(R.id.about_who_Button);
        whoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                View view = getView();
                if (view == null)return;
                EditText whoLabel = view.findViewById(R.id.about_who_output);
                String whoString = whoLabel.getText().toString();
                if (whoString.equals(mWhoShortStmt)){
                    whoLabel.setText(mWhoStmt);
                } else {
                    whoLabel.setText(mWhoShortStmt);
                }

            }
        });

        //Privacy Button
        final Button privacyButton =  v.findViewById(R.id.about_privacy_Button);
        privacyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                View view = getView();
                if (view == null)return;
                EditText privacyLabel = view.findViewById(R.id.about_privacy_output);
                String privacyString = privacyLabel.getText().toString();
                if (privacyString.equals(mPrivacyShortStmt)){
                    privacyLabel.setText(mPrivacyStmt);
                } else {
                    privacyLabel.setText(mPrivacyShortStmt);
                }

            }
        });

        //EULA Button
        final Button eulaButton = v.findViewById(R.id.about_eula_Button);
        eulaButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                View view = getView();
                if (view == null)return;
                EditText eulaLabel = view.findViewById(R.id.about_eula_output);
                String eulaString = eulaLabel.getText().toString();
                if (eulaString.equals(mEulaShortStmt)){
                    eulaLabel.setText(mEulaStmt);
                } else {
                    eulaLabel.setText(mEulaShortStmt);
                }

            }
        });

        //Tips Button
        final Button tipsButton =  v.findViewById(R.id.about_tips_Button);
        tipsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                View view = getView();
                if (view == null)return;
                EditText tipsLabel = view.findViewById(R.id.about_tips_output);
                String tipsString = tipsLabel.getText().toString();
                if (tipsString.equals(mTipsShortStmt)){
                    tipsLabel.setText(mTipsStmt);
                } else {
                    tipsLabel.setText(mTipsShortStmt);
                }

            }
        });

    }


    private void initializeUI(View v) {


        EditText whoOutput     = v.findViewById(R.id.about_who_output) ;
        EditText privacyOutput = v.findViewById(R.id.about_privacy_output);
        EditText eulaOutput    = v.findViewById(R.id.about_eula_output);
        EditText tipsOutput    = v.findViewById(R.id.about_tips_output);

        whoOutput    .setText(mWhoShortStmt);
        privacyOutput.setText(mPrivacyShortStmt);
        eulaOutput   .setText(mEulaShortStmt);
        tipsOutput   .setText(mTipsShortStmt);
    }


}
