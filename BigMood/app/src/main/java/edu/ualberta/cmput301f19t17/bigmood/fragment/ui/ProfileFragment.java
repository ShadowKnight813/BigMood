package edu.ualberta.cmput301f19t17.bigmood.fragment.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputLayout;

import edu.ualberta.cmput301f19t17.bigmood.R;
import edu.ualberta.cmput301f19t17.bigmood.activity.AppPreferences;
import edu.ualberta.cmput301f19t17.bigmood.activity.HomeActivity;
import edu.ualberta.cmput301f19t17.bigmood.database.User;
import edu.ualberta.cmput301f19t17.bigmood.model.Request;

/**
 * ProfileFragment is used to view the current user's profile. It has the logic for logging out and requesting follows
 */
public class ProfileFragment extends Fragment {

    private AppPreferences appPreferences;

    private TextInputLayout textInputUsername;
    private Button buttonRequest;

    /**
     * of the on*()methods, this is the second. After the dialog has been started we want to inflate the dialog.
     * This is where we inflate all the views and *if applicable* populate all the fields.
     * @param inflater           View inflater service
     * @param container          Container that the inflater is housed in
     * @param savedInstanceState A bundle that holds the state of the fragment
     * @return                   Returns the inflated view
     */
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflate the fragment
        final View rootView = inflater.inflate(R.layout.fragment_profile, container, false);

        // Enable options menu
        this.setHasOptionsMenu(true);

        // Get the App Preferences
        this.appPreferences = AppPreferences.getInstance();

        // Get the TextInputLayout
        this.textInputUsername = rootView.findViewById(R.id.text_input_username);

        // Get the request button
        this.buttonRequest = rootView.findViewById(R.id.button_request);

        // Get the TextViews for each of the username, first name, and last name.
        TextView textViewUsername = rootView.findViewById(R.id.textview_username);
        TextView textViewFirstName = rootView.findViewById(R.id.textview_firstname);
        TextView textViewLastName = rootView.findViewById(R.id.textview_lastname);

        // Get the current user
        final User currentUser = this.appPreferences.getCurrentUser();

        // Set each TextView to their correct value
        textViewUsername.setText(String.format("@%s", currentUser.getUsername()));
        textViewFirstName.setText(currentUser.getFirstName());
        textViewLastName.setText(currentUser.getLastName());

        // Set the listener for the request button.
        this.buttonRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Get trimmed string of username.
                final String username = ProfileFragment.this.textInputUsername
                        .getEditText()
                        .getText()
                        .toString()
                        .trim();

                // If the username is empty, display an error. If not, clear it.
                if (username.length() < 1) {

                    ProfileFragment.this.textInputUsername.setError(
                            ProfileFragment.this.getContext().getText(R.string.error_empty_username)
                    );

                    return;

                } else {

                    // Clear error
                    ProfileFragment.this.textInputUsername.setError(null);

                }

                // Assuming we've gotten past the input validation, we can now submit the request operation. //

                // Disable the button to prevent multiple attempts.
                ProfileFragment.this.buttonRequest.setEnabled(false);

                // Call the repository and check if the user exists or not. If they do, then we can submit a request.
                ProfileFragment.this.appPreferences
                        .getRepository()
                        .userExists(

                                username,

                                new OnSuccessListener<Boolean>() {
                                    @Override
                                    public void onSuccess(Boolean aBoolean) {

                                        // If the user DOES NOT EXIST, then we have to error and exit this function.
                                        if (!aBoolean) {

                                            // Display toast to the user.
                                            Toast.makeText(
                                                    ProfileFragment.this.getContext(),
                                                    ProfileFragment.this.getContext().getText(R.string.toast_error_user_dne),
                                                    Toast.LENGTH_LONG
                                            ).show();

                                            ProfileFragment.this.buttonRequest.setEnabled(true);

                                            // Exit out because it makes no sense to create a request for a user that DNE (will be rejected anyway)
                                            return;

                                        }

                                        // At this point the user does exist. Now we create a request between the current user and the requested username.
                                        ProfileFragment.this.appPreferences
                                                .getRepository()
                                                .createRequest(

                                                        new Request(currentUser, username),

                                                        new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void aVoid) {

                                                                // Display a success toast to the user.
                                                                Toast.makeText(
                                                                        ProfileFragment.this.getContext(),
                                                                        ProfileFragment.this.getContext().getText(R.string.toast_success_request_sent),
                                                                        Toast.LENGTH_SHORT
                                                                ).show();

                                                                // Enable button again
                                                                ProfileFragment.this.buttonRequest.setEnabled(true);

                                                            }
                                                        },  // End of OnSuccessListener for createRequest()

                                                        new OnFailureListener() {
                                                            @Override
                                                            public void onFailure(@NonNull Exception e) {

                                                                // Log error
                                                                Log.e(HomeActivity.LOG_TAG, "FAILED TO CREATE REQUEST: " + e.toString());

                                                                // Display an error toast to the user describing that there was a problem with connecting to the database.
                                                                Toast.makeText(
                                                                        ProfileFragment.this.getContext(),
                                                                        ProfileFragment.this.getContext().getText(R.string.toast_error_db),
                                                                        Toast.LENGTH_SHORT
                                                                ).show();

                                                                // Enable button again
                                                                ProfileFragment.this.buttonRequest.setEnabled(true);

                                                            }
                                                        }  // End of OnFailureListener for createRequest()

                                                );

                                    }
                                },  // End of OnSuccessListener for userExists()

                                new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {

                                        // Log error
                                        Log.e(HomeActivity.LOG_TAG, "FAILED TO CHECK USER EXISTENCE: " + e.toString());

                                        // Display an error toast to the user describing that there was a problem with connecting to the database.
                                        Toast.makeText(
                                                ProfileFragment.this.getContext(),
                                                ProfileFragment.this.getContext().getText(R.string.toast_error_db),
                                                Toast.LENGTH_SHORT
                                        ).show();

                                        // Enable the button again
                                        ProfileFragment.this.buttonRequest.setEnabled(true);

                                    }
                                }  // End of OnFailureListener for userExists()

                        );  // End of userExists()


            }
        });  // End of setOnClickListener for button

        return rootView;
    }

    /**
     * This method gets called when the fragment needs to assemble menu options.
     * @param menu     The options menu in which you place your items.
     * @param inflater The menu inflater
     */
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {

        // Inflate the menu
        inflater.inflate(R.menu.fragment_profile, menu);

        super.onCreateOptionsMenu(menu, inflater);

    }

    /**
     * This method gets called when a menu item in the toolbar is clicked. We only have one item here so we only check one
     * @param item The menu item that was selected. This value must never be null.
     * @return     Return false to allow normal menu processing to proceed, true to consume it here.
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == R.id.action_sign_out) {

            // If the sign out button is pressed, clear the current user and call finish() on the underlying activity.
            this.appPreferences.logout();
            this.getActivity().finish();
            return true;

        } else {

            return super.onOptionsItemSelected(item);

        }

    }
}