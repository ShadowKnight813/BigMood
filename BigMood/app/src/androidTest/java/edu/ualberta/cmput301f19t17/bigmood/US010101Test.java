package edu.ualberta.cmput301f19t17.bigmood;

import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;

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
import edu.ualberta.cmput301f19t17.bigmood.model.EmotionalState;
import edu.ualberta.cmput301f19t17.bigmood.model.SocialSituation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

// TODO: 2019-11-06 Cameron: remove waits (replace with MockRepository calls)
/**
 * Test class for DefineMoodDialogFragment. All the UI tests are written here. Robotium test framework is used.
 */
public class US010101Test {
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

        AppPreferences.getInstance().getRepository().deleteAllMoods(AppPreferences.getInstance().getCurrentUser());
        solo.sleep(3000);

    }
    @AfterClass //runs after all tests have run
    public static void cleanUp() {
        AppPreferences.getInstance().getRepository().deleteAllMoods(AppPreferences.getInstance().getCurrentUser());
    }

    @Test
    public void checkAddMood() {
        solo.assertCurrentActivity("Wrong Activity", HomeActivity.class);

        View fab = solo.getCurrentActivity().findViewById(R.id.floatingActionButton);

        solo.clickOnView(fab);

        solo.pressSpinnerItem(0, EmotionalState.DISGUST.getStateCode()); //disgusted
        solo.pressSpinnerItem(3, SocialSituation.SEVERAL.getSituationCode()); //two to several
        solo.typeText(((TextInputLayout) solo.getView(R.id.text_input_reason)).getEditText(), "got puked on");

        solo.clickOnView(solo.getView(R.id.action_save));
        solo.waitForDialogToClose();

        ListAdapter moodArrayAdapter = ((ListView) solo.getView(R.id.mood_list)).getAdapter();

        //make sure we correctly added the mood
        assertTrue(solo.waitForText(EmotionalState.DISGUST.toString()));
        assertEquals(1, moodArrayAdapter.getCount());
    }

}
