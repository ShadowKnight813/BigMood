package edu.ualberta.cmput301f19t17.bigmood;


import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;

import com.google.android.material.textfield.TextInputLayout;
import com.robotium.solo.Solo;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import edu.ualberta.cmput301f19t17.bigmood.activity.AppPreferences;
import edu.ualberta.cmput301f19t17.bigmood.activity.SignUpActivity;
import edu.ualberta.cmput301f19t17.bigmood.database.MockUser;

import static org.junit.Assert.assertTrue;

public class SignupActivityTest {
    private Solo solo;
    private AppPreferences appPreferences;

    @Rule
    public ActivityTestRule<SignUpActivity> rule=new ActivityTestRule<>(SignUpActivity.class,true,true);

    @Before
    public void setUp() throws Exception {
        solo=new Solo(InstrumentationRegistry.getInstrumentation(), rule.getActivity());
        appPreferences=AppPreferences.getInstance();
        appPreferences.login(new MockUser("CMPUT301", "CMPUT", "301"));
    }


    @Test
    public void SignUpActivity() {
        //SignUp failed
        solo.assertCurrentActivity("Wrong activity",SignUpActivity.class);
        solo.enterText(((TextInputLayout) solo.getView(R.id.text_input_first_name)).getEditText(), "CMPUT");
        solo.enterText(((TextInputLayout) solo.getView(R.id.text_input_last_name)).getEditText(), "301");
        solo.enterText(((TextInputLayout) solo.getView(R.id.text_input_username)).getEditText(), "CMPUT301");
        solo.enterText(((TextInputLayout) solo.getView(R.id.text_input_password)).getEditText(), "cmput301");
        solo.enterText(((TextInputLayout) solo.getView(R.id.text_input_confirm_password)).getEditText(), "cmput301");
        solo.clickOnView(solo.getView(R.id.button_sign_up));
        solo.clickOnButton("Sign Up");
        assertTrue(solo.waitForText("Sorry, a user already exists with that username."));

        //Successful SignUp
        solo.assertCurrentActivity("Wrong activity",SignUpActivity.class);
        solo.enterText(((TextInputLayout) solo.getView(R.id.text_input_first_name)).getEditText(), "CMPUT");
        solo.enterText(((TextInputLayout) solo.getView(R.id.text_input_last_name)).getEditText(), "301");
        solo.enterText(((TextInputLayout) solo.getView(R.id.text_input_username)).getEditText(), "CMPUT301");
        solo.enterText(((TextInputLayout) solo.getView(R.id.text_input_password)).getEditText(), "cmput301");
        solo.enterText(((TextInputLayout) solo.getView(R.id.text_input_confirm_password)).getEditText(), "cmput301");
        solo.clickOnView(solo.getView(R.id.button_sign_up));
        solo.clickOnButton("Sign Up");


    }

}
