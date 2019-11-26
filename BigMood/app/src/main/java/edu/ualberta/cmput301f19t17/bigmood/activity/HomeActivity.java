package edu.ualberta.cmput301f19t17.bigmood.activity;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import edu.ualberta.cmput301f19t17.bigmood.R;

/**
 * HomeActivity is the Activity the hosts the fragments:
 * UserMoodsFragment, FollowingFragment, RequestsFragment, and ProfileFragment.
 * It also hosts the toolbar that allows the user to navigate through these fragments
 */
public class HomeActivity extends AppCompatActivity {

    public static final String LOG_TAG = "BigMoodLogger";

    private AppPreferences appPreferences;

    /**
     * onCreate is called when the Activity is created, and it is used to perform the logic that the Activity
     * needs, such as setting onClickListeners.
     * @param savedInstanceState if the instance was saved, this would be sent in when the Activity is created
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        this.appPreferences = AppPreferences.getInstance();

        // Bind the toolbar in XML to the SupportActionBar of the Activity
        Toolbar toolbar = (Toolbar) this.findViewById(R.id.toolbar_home);
        this.setSupportActionBar(toolbar);

        // Get BottomNavigationView from XML
        BottomNavigationView navView = findViewById(R.id.nav_view);

        // Get the fragment window from XML
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);

        // Create new configuration for the navigation bar.
        // Passing each menu ID as a set of Ids because each menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_user_moods,
                R.id.navigation_following,
                R.id.navigation_requests,
                R.id.navigation_profile
        ).build();

        // Bind toolbar to change with the Nav Bar
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        // Bind fragment to change with the Nav Bar
        NavigationUI.setupWithNavController(navView, navController);

    }

    /**
     * This method is called when the hardware navigation button is pressed. Since we don't want to accidentally log out by pressing the back button we basically don't do anything here but log the event.
     */
    @Override
    public void onBackPressed() {

        Log.d(HomeActivity.LOG_TAG, "Back navigation (Hardware) from " + this.getClass().getSimpleName());

    }

    /**
     * This method is called when the software button is pressed (if applicable). Since we don't want to accidentally log out by pressing the back button we basically don't do anything here but log the event.
     */
    @Override
    public boolean onSupportNavigateUp() {

        Log.d(HomeActivity.LOG_TAG, "Back navigation (Software) from " + this.getClass().getSimpleName());

        return false;

    }
}
