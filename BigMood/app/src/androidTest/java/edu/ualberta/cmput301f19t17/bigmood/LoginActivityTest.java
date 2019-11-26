package edu.ualberta.cmput301f19t17.bigmood;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;

import com.google.android.material.textfield.TextInputLayout;
import com.robotium.solo.Solo;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import edu.ualberta.cmput301f19t17.bigmood.activity.AppPreferences;
import edu.ualberta.cmput301f19t17.bigmood.activity.LoginActivity;
import edu.ualberta.cmput301f19t17.bigmood.database.MockUser;

/**
 * Test class for LoginActivity. All the UI tests are written here. Robotium test framework is
 used
 */
// unit test to ensure that logic returns desired result
public class LoginActivityTest {

    private Solo solo;
    private AppPreferences appPreferences;

    @Rule
    public ActivityTestRule<LoginActivity> rule = new ActivityTestRule<>(LoginActivity.class,true, true);

    @Before
    public void setUp() throws Exception {
        solo = new Solo(InstrumentationRegistry.getInstrumentation(), rule.getActivity());
        appPreferences = AppPreferences.getInstance();
        appPreferences.login(new MockUser("CMPUT301", "CMPUT", "301"));
    }

    @Test
    public void testWrongUsername() {
        solo.assertCurrentActivity("Wrong activity", LoginActivity.class);
        solo.typeText(((TextInputLayout) solo.getView(R.id.text_input_username)).getEditText(), "HELLO");
        solo.typeText(((TextInputLayout) solo.getView(R.id.text_input_password)).getEditText(), "password");
        solo.clickOnView(solo.getView(R.id.button_login));
        solo.clickOnButton("Log In");
        solo.waitForText("Username/password incorrect", 1, 1000);
    }

    @Test
    public void testWrongPassword() {
        solo.assertCurrentActivity("Wrong activity", LoginActivity.class);
        solo.typeText(((TextInputLayout) solo.getView(R.id.text_input_username)).getEditText(), "CMPUT301");
        solo.typeText(((TextInputLayout) solo.getView(R.id.text_input_password)).getEditText(), "HELLO");
        solo.clickOnView(solo.getView(R.id.button_login));
        solo.clickOnButton("Log In");
        solo.waitForText("Username/password incorrect", 1, 1000);
    }

    @Test
    public void testCorrectUsernamePassword() {
        solo.assertCurrentActivity("Wrong activity", LoginActivity.class);
        solo.typeText(((TextInputLayout) solo.getView(R.id.text_input_username)).getEditText(), "CMPUT301");
        solo.typeText(((TextInputLayout) solo.getView(R.id.text_input_password)).getEditText(), "password");
        solo.clickOnView(solo.getView(R.id.button_login));
        solo.clickOnButton("Log In");
        solo.waitForText("Username/password incorrect", 0, 1000);
    }

    /**
     * Closes the activity after each test
     * @throws Exception
     */
    @After
    public void tearDown() throws Exception{
        solo.finishOpenedActivities();
    }
}
