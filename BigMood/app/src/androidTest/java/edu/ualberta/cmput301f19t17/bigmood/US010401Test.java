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

// TODO: 2019-11-06 Cameron: remove waits (replace with MockRepository calls)

public class US010401Test {
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
        solo.waitForText("HillyBillyBobTesterino", 0, 2000);
    }
    @AfterClass //runs after all tests have run
    public static void cleanUp() {
        AppPreferences.getInstance().getRepository().deleteAllMoods(AppPreferences.getInstance().getCurrentUser());
    }

    @Test
    public void checkEditMood() {
        solo.assertCurrentActivity("Wrong Activity", HomeActivity.class);

        View fab = solo.getCurrentActivity().findViewById(R.id.floatingActionButton);

        solo.clickOnView(fab);
        solo.pressSpinnerItem(0, EmotionalState.DISGUST.getStateCode()); //disgusted
        solo.pressSpinnerItem(1, SocialSituation.SEVERAL.getSituationCode()); //two to several

        solo.enterText(((TextInputLayout) solo.getView(R.id.text_input_reason)).getEditText(), "check edit mood");

        solo.clickOnView(solo.getView(R.id.action_save));

        // TODO: 2019-11-06 Cameron:
        solo.waitForText("HillyBillyBobTesterino", 0, 2000);
        ListAdapter moodArrayAdapter = ((ListView) solo.getView(R.id.mood_list)).getAdapter();
        int originalNumListItems = moodArrayAdapter.getCount();


        solo.clickOnMenuItem("Disgust"); //select the mood we just created (it will be 0 since the list is sorted newest -> oldest)

        solo.clickOnButton("EDIT");
        solo.waitForText("Edit Mood", 1, 1000); //make sure DefineMoodDialogFragment opens itself correctly as a "Edit" rather than "Add"

        solo.pressSpinnerItem(0, EmotionalState.SADNESS.getStateCode()); //sad
        solo.pressSpinnerItem(1, SocialSituation.CROWD.getSituationCode()); //crowd

        solo.clickOnView(solo.getView(R.id.action_save));
        //make sure the edit worked by checking that the SADNESS shows up
        solo.waitForText(EmotionalState.SADNESS.toString(), 1, 1000);

        //make sure no new items were added, and no items deleted
        // TODO: 2019-11-06 Cameron:
        solo.waitForText("HillyBillyBobTesterino", 0, 2000);
        assertEquals(originalNumListItems, moodArrayAdapter.getCount());

        appPreferences.getRepository().deleteAllMoods(appPreferences.getCurrentUser());

    }

}

