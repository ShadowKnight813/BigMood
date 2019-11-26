package edu.ualberta.cmput301f19t17.bigmood;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;

import com.google.android.material.textfield.TextInputLayout;
import com.robotium.solo.Solo;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;

import edu.ualberta.cmput301f19t17.bigmood.activity.AppPreferences;
import edu.ualberta.cmput301f19t17.bigmood.activity.HomeActivity;
import edu.ualberta.cmput301f19t17.bigmood.database.MockUser;

import static org.junit.Assert.assertTrue;

public class US050101Test {
    private Solo solo;
    private AppPreferences appPreferences;

    @BeforeClass //runs before anything else runs
    public static void setUpAppPrefs() throws Exception {
        AppPreferences.getInstance().login(new MockUser("CMPUT301", "CMPUT", "301"));
    }

    @Rule
    public ActivityTestRule<HomeActivity> rule = new ActivityTestRule<>(HomeActivity.class, true, true);

    @Before //Clears the mood list before each test
    public void setUp() throws Exception {
        solo = new Solo(InstrumentationRegistry.getInstrumentation(), rule.getActivity());
        appPreferences = AppPreferences.getInstance(); // used to call deleteAllMoods method
    }

    @Test
    public void checkUserDoesNotExist() {
        solo.assertCurrentActivity("Wrong Activity", HomeActivity.class);
        solo.clickOnText("Profile");
        solo.typeText(((TextInputLayout) solo.getView(R.id.text_input_username)).getEditText(), "SuperMario");
        solo.clickOnButton("REQUEST");
        assertTrue(solo.waitForText("User does not exist", 1, 2000));
        solo.sleep(2000);
    }

    @Test
    public void checkRequestCreated() {
        solo.assertCurrentActivity("Wrong Activity", HomeActivity.class);
        solo.clickOnText("Profile");
        String requester_username = "CMPUT301";
        String requested_username = "apple";
        solo.typeText(((TextInputLayout) solo.getView(R.id.text_input_username)).getEditText(), requested_username);
        solo.clickOnButton("REQUEST");
        assertTrue(solo.waitForText("Request sent", 1, 2000));

        solo.clickOnText("My Moods");
        solo.sleep(2000);
        appPreferences.getInstance().login(new MockUser(requested_username, "Bob", "Smith"));
        solo.clickOnText("Profile");
        solo.sleep(2000);
        solo.clickOnText("Requests");
        solo.sleep(2000);
        assertTrue(solo.waitForText(requester_username, 1, 2000));
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
