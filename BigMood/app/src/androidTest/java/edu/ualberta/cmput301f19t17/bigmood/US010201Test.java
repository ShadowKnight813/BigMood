package edu.ualberta.cmput301f19t17.bigmood;

import android.view.View;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;

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

import static org.junit.Assert.assertTrue;

public class US010201Test {
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
        // TODO: 2019-11-06 Cameron:
        solo.waitForText("HillyBillyBobTesterino", 0, 1000);
    }

    @Test
    public void testHappyEmoticon() {
        View fab = solo.getCurrentActivity().findViewById(R.id.floatingActionButton);
        solo.clickOnView(fab);
        solo.pressSpinnerItem(0, EmotionalState.HAPPINESS.getStateCode());
        solo.clickOnView(solo.getView(R.id.action_save));
        solo.waitForDialogToClose();

        Integer listItemDrawableID = (Integer) solo.getView(R.id.mood_item_emoticon).getTag();
        assertTrue(R.drawable.ic_emoticon_happy == listItemDrawableID);

        solo.clickInList(1, 0);
        Integer imageViewDrawableID = (Integer) solo.getView(R.id.imageview_placeholder_emote).getTag();
        assertTrue(R.drawable.ic_emoticon_happy == imageViewDrawableID);

        assertTrue(solo.waitForText("Happy", 1, 2000));
    }

    @Test
    public void testSadEmoticon() {
        View fab = solo.getCurrentActivity().findViewById(R.id.floatingActionButton);
        solo.clickOnView(fab);
        solo.pressSpinnerItem(0, EmotionalState.SADNESS.getStateCode());
        solo.clickOnView(solo.getView(R.id.action_save));
        solo.waitForDialogToClose();

        Integer listItemDrawableID = (Integer) solo.getView(R.id.mood_item_emoticon).getTag();
        assertTrue(R.drawable.ic_emoticon_sad == listItemDrawableID);

        solo.clickInList(1, 0);
        Integer imageViewDrawableID = (Integer) solo.getView(R.id.imageview_placeholder_emote).getTag();
        assertTrue(R.drawable.ic_emoticon_sad == imageViewDrawableID);

        assertTrue(solo.waitForText("Sad", 1, 2000));
    }

    @Test
    public void testAngerEmoticon() {
        View fab = solo.getCurrentActivity().findViewById(R.id.floatingActionButton);
        solo.clickOnView(fab);
        solo.pressSpinnerItem(0, EmotionalState.ANGER.getStateCode());
        solo.clickOnView(solo.getView(R.id.action_save));
        solo.waitForDialogToClose();

        Integer listItemDrawableID = (Integer) solo.getView(R.id.mood_item_emoticon).getTag();
        assertTrue(R.drawable.ic_emoticon_anger == listItemDrawableID);

        solo.clickInList(1, 0);
        Integer imageViewDrawableID = (Integer) solo.getView(R.id.imageview_placeholder_emote).getTag();
        assertTrue(R.drawable.ic_emoticon_anger == imageViewDrawableID);

        assertTrue(solo.waitForText("Angry", 1, 2000));
    }

    @Test
    public void testDisgustEmoticon() {
        View fab = solo.getCurrentActivity().findViewById(R.id.floatingActionButton);
        solo.clickOnView(fab);
        solo.pressSpinnerItem(0, EmotionalState.DISGUST.getStateCode());
        solo.clickOnView(solo.getView(R.id.action_save));
        solo.waitForDialogToClose();

        Integer listItemDrawableID = (Integer) solo.getView(R.id.mood_item_emoticon).getTag();
        assertTrue(R.drawable.ic_emoticon_disgust == listItemDrawableID);

        solo.clickInList(1, 0);
        Integer imageViewDrawableID = (Integer) solo.getView(R.id.imageview_placeholder_emote).getTag();
        assertTrue(R.drawable.ic_emoticon_disgust == imageViewDrawableID);

        assertTrue(solo.waitForText("Disgusted", 1, 2000));
    }

    @Test
    public void testFearEmoticon() {
        View fab = solo.getCurrentActivity().findViewById(R.id.floatingActionButton);
        solo.clickOnView(fab);
        solo.pressSpinnerItem(0, EmotionalState.FEAR.getStateCode());
        solo.clickOnView(solo.getView(R.id.action_save));
        solo.waitForDialogToClose();

        Integer listItemDrawableID = (Integer) solo.getView(R.id.mood_item_emoticon).getTag();
        assertTrue(R.drawable.ic_emoticon_fear == listItemDrawableID);

        solo.clickInList(1, 0);
        Integer imageViewDrawableID = (Integer) solo.getView(R.id.imageview_placeholder_emote).getTag();
        assertTrue(R.drawable.ic_emoticon_fear == imageViewDrawableID);

        assertTrue(solo.waitForText("Afraid", 1, 2000));
    }

    @Test
    public void testSurpriseEmoticon() {
        View fab = solo.getCurrentActivity().findViewById(R.id.floatingActionButton);
        solo.clickOnView(fab);
        solo.pressSpinnerItem(0, EmotionalState.SURPRISE.getStateCode());
        solo.clickOnView(solo.getView(R.id.action_save));
        solo.waitForDialogToClose();

        Integer listItemDrawableID = (Integer) solo.getView(R.id.mood_item_emoticon).getTag();
        assertTrue(R.drawable.ic_emoticon_surprise == listItemDrawableID);

        solo.clickInList(1, 0);
        Integer imageViewDrawableID = (Integer) solo.getView(R.id.imageview_placeholder_emote).getTag();
        assertTrue(R.drawable.ic_emoticon_surprise == imageViewDrawableID);

        assertTrue(solo.waitForText("Surprise", 1, 2000));
    }

    /**
     * Closes the activity after each test
     * @throws Exception
     */
    @After
    public void tearDown() throws Exception{
        solo.finishOpenedActivities();
    }

    /**
     * Clears the mood list after all tests are finished
     * @throws Exception
     */
    @AfterClass //runs after all tests have run
    public static void cleanUp() {
        AppPreferences.getInstance().getRepository().deleteAllMoods(AppPreferences.getInstance().getCurrentUser());
    }
}
