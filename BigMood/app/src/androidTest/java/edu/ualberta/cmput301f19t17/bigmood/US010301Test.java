package edu.ualberta.cmput301f19t17.bigmood;

import android.view.View;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;

import com.google.android.material.textfield.TextInputLayout;
import com.robotium.solo.Solo;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;

import edu.ualberta.cmput301f19t17.bigmood.activity.AppPreferences;
import edu.ualberta.cmput301f19t17.bigmood.activity.HomeActivity;
import edu.ualberta.cmput301f19t17.bigmood.database.MockUser;
import edu.ualberta.cmput301f19t17.bigmood.model.EmotionalState;
import edu.ualberta.cmput301f19t17.bigmood.model.SocialSituation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class US010301Test {
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
        appPreferences.getRepository().deleteAllMoods(appPreferences.getCurrentUser());
        solo.waitForText("text", 0, 1000);
    }
    @AfterClass //runs after all tests have run
    public static void cleanUp() {
        AppPreferences.getInstance().getRepository().deleteAllMoods(AppPreferences.getInstance().getCurrentUser());
    }

    @Test
    public void testDisplayStateTimeDate() {
        View fab = solo.getCurrentActivity().findViewById(R.id.floatingActionButton);
        solo.clickOnView(fab);

        solo.pressSpinnerItem(0, EmotionalState.HAPPINESS.getStateCode());
        solo.clickOnView(solo.getView(R.id.action_save));
        solo.waitForDialogToClose();

        solo.clickInList(1, 0);

        Integer drawableID = (Integer) solo.getView(R.id.imageview_placeholder_emote).getTag();
        assertTrue(R.drawable.ic_emoticon_happy == drawableID);

        assertTrue(solo.waitForText("Happy", 1, 2000));
    }

    @Test
    public void  testDisplayStateTimeDateSituation() {
        View fab = solo.getCurrentActivity().findViewById(R.id.floatingActionButton);
        solo.clickOnView(fab);

        solo.pressSpinnerItem(0, EmotionalState.SURPRISE.getStateCode());
        solo.pressSpinnerItem(3, SocialSituation.CROWD.getSituationCode());
        solo.clickOnView(solo.getView(R.id.action_save));
        solo.waitForDialogToClose();

        solo.clickInList(1, 0);

        Integer drawableID = (Integer) solo.getView(R.id.imageview_placeholder_emote).getTag();
        assertEquals(R.drawable.ic_emoticon_surprise, (int) drawableID);

        assertTrue(solo.waitForText("Surprise", 1, 2000));
        assertTrue(solo.waitForText("Crowd", 1, 2000));
    }

    @Test
    public void  testDisplayStateTimeDateSituationReason() {
        View fab = solo.getCurrentActivity().findViewById(R.id.floatingActionButton);
        solo.clickOnView(fab);

        solo.pressSpinnerItem(0, EmotionalState.DISGUST.getStateCode());
        solo.pressSpinnerItem(3, SocialSituation.ALONE.getSituationCode());
        solo.typeText(((TextInputLayout) solo.getView(R.id.text_input_reason)).getEditText(), "Stepped on poop");
        solo.clickOnView(solo.getView(R.id.action_save));
        solo.waitForDialogToClose();

        solo.clickInList(1, 0);

        solo.waitForDialogToClose();

        solo.clickInList(1, 0);

        Integer drawableID = (Integer) solo.getView(R.id.imageview_placeholder_emote).getTag();
        assertTrue(R.drawable.ic_emoticon_disgust == drawableID);

        assertTrue(solo.waitForText("Disgusted", 1, 2000));
        assertTrue(solo.waitForText("Alone", 1, 2000));
        assertTrue(solo.waitForText("Stepped on poop", 1, 2000));
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
