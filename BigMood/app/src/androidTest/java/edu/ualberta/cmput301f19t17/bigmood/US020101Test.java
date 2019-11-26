package edu.ualberta.cmput301f19t17.bigmood;

import android.view.View;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;

import com.google.android.material.textfield.TextInputLayout;
import com.robotium.solo.Solo;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;

import edu.ualberta.cmput301f19t17.bigmood.activity.AppPreferences;
import edu.ualberta.cmput301f19t17.bigmood.activity.HomeActivity;
import edu.ualberta.cmput301f19t17.bigmood.database.MockUser;

import static org.junit.Assert.assertTrue;

public class US020101Test {
    private Solo solo;
    private AppPreferences appPreferences;

    @BeforeClass //runs before anything else runs
    public static void setUpAppPrefs() throws Exception {
        AppPreferences.getInstance().login(new MockUser("CMPUT301", "CMPUT", "301"));
    }

    @Rule
    public ActivityTestRule<HomeActivity> rule = new ActivityTestRule<>(HomeActivity.class, true, true);

    @Before //runs before every test
    public void setUp() throws Exception {
        solo = new Solo(InstrumentationRegistry.getInstrumentation(), rule.getActivity());
        appPreferences = AppPreferences.getInstance();

        appPreferences.getRepository().deleteAllMoods(appPreferences.getCurrentUser());
        solo.waitForText("HillyBillyBobTesterino", 0, 2000);
    }
    @AfterClass //runs after all tests have run
    public static void cleanUp() {
        AppPreferences.getInstance().getRepository().deleteAllMoods(AppPreferences.getInstance().getCurrentUser());
    }

    @Test
    public void checkReasonMaxLength(){

        solo.assertCurrentActivity("Wrong Activity", HomeActivity.class);
        View fab = solo.getCurrentActivity().findViewById(R.id.floatingActionButton);
        solo.clickOnView(fab);

        solo.enterText(((TextInputLayout) solo.getView(R.id.text_input_reason)).getEditText(), "check reason length is too long.");
        solo.clickOnView(solo.getView(R.id.action_save));
        assertTrue(solo.waitForText("Reason too long"));

    }

    @Test
    public void checkReasonNumWords() {
        solo.assertCurrentActivity("Wrong Activity", HomeActivity.class);
        View fab = solo.getCurrentActivity().findViewById(R.id.floatingActionButton);
        solo.clickOnView(fab);

        solo.enterText(((TextInputLayout) solo.getView(R.id.text_input_reason)).getEditText(), "chk len too long");
        solo.clickOnView(solo.getView(R.id.action_save));
        assertTrue(solo.waitForText("Reason cannot be longer than 3 words"));
    }


}
