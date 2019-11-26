package edu.ualberta.cmput301f19t17.bigmood;

import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;

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

import static org.junit.Assert.assertEquals;

public class US040201Test {
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
    public void checkFilterMood() {
        solo.assertCurrentActivity("Wrong Activity", HomeActivity.class);


        //get message from async update before checking number of items in list
        solo.waitForText("HillyBillyBobTesterino", 0, 2000);

        ListView moodList = (ListView) solo.getView(R.id.mood_list);
        ListAdapter moodArrayAdapter = moodList.getAdapter();

        View fab = solo.getCurrentActivity().findViewById(R.id.floatingActionButton);

        // Create 2 Mood for each state

        int mood_quantity = 2;
        int state_quantity = EmotionalState.values().length;

        for (EmotionalState state : EmotionalState.values()) {
            for (int i = 0; i < mood_quantity; i++) {
                solo.clickOnView(fab);
                solo.pressSpinnerItem(0, state.getStateCode());
                solo.clickOnView(solo.getView(R.id.action_save));
            }
        }
        // This assert also guarantees the filter at startup stay at None
        // Call sleep for 1 second every time we count the number of Mood on the screen
        solo.sleep(1000);
        assertEquals(mood_quantity*state_quantity, moodArrayAdapter.getCount());

        View filter = solo.getCurrentActivity().findViewById(R.id.action_filter);
        solo.clickOnView(filter);
        for (EmotionalState state : EmotionalState.values()) {
            // select a mood and re-click the filter to make it disappear
            solo.clickOnMenuItem(state.toString());
            solo.clickOnView(filter);
            // the number of mood show should be equal to the number of mood being filtered
            assertEquals(mood_quantity, moodArrayAdapter.getCount());
        }

        // Go back to None filter, it should show full moods

        solo.clickOnMenuItem("None");
        solo.clickOnView(filter);
        solo.sleep(1000);
        assertEquals(mood_quantity*state_quantity, moodArrayAdapter.getCount());


        // We select Happy filter and then try to Edit/Delete
        solo.clickOnMenuItem("Happy");

        // 1) Try to add a Mood
        solo.clickOnView(fab);
        solo.pressSpinnerItem(0, EmotionalState.HAPPINESS.getStateCode());
        solo.clickOnView(solo.getView(R.id.action_save));
        solo.sleep(1000);
        assertEquals(mood_quantity+1, moodArrayAdapter.getCount());

        // 2) Try to delete that Mood
        solo.clickOnMenuItem("Happy");
        solo.clickOnButton("DELETE");
        solo.sleep(1000);
        assertEquals(mood_quantity, moodArrayAdapter.getCount());

        // 3) Edit a mood
        solo.clickOnMenuItem("Happy");
        solo.clickOnButton("EDIT");
        solo.pressSpinnerItem(0, EmotionalState.SADNESS.getStateCode());
        solo.clickOnView(solo.getView(R.id.action_save));
        solo.sleep(1000);
        // The number of Happy Mood should be decrease by 1
        assertEquals(mood_quantity-1, moodArrayAdapter.getCount());
        // Check if our Happy turn into Sad
        solo.clickOnView(filter);
        solo.clickOnMenuItem("Sad");
        solo.sleep(1000);
        // The number of Sad Mood should increase by 1
        assertEquals(mood_quantity+1, moodArrayAdapter.getCount());

        appPreferences.getRepository().deleteAllMoods(appPreferences.getCurrentUser());

    }

} 