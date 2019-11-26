package edu.ualberta.cmput301f19t17.bigmood.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputLayout;

import edu.ualberta.cmput301f19t17.bigmood.R;

/**
 * SignUpActivity allows the user to create a new account. Basic input validation is preformed and assuming they all pass, a new user is registered.
 */
public class SignUpActivity extends AppCompatActivity {

    private AppPreferences appPreferences;

    private TextInputLayout textInputFirstName;
    private TextInputLayout textInputLastName;
    private TextInputLayout textInputUsername;
    private TextInputLayout textInputPassword;
    private TextInputLayout textInputConfirmPassword;

    private Button button;

    /**
     * onCreate is called when the Activity is created, and it is used to perform the logic that the Activity
     * needs, such as setting onClickListeners.
     * @param savedInstanceState if the instance was saved, this would be sent in when the Activity is created
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        // Bind AppPreferences
        this.appPreferences = AppPreferences.getInstance();

        // get Toolbar
        Toolbar toolbar = this.findViewById(R.id.toolbar_activity_sign_up);

        // Set Title
        toolbar.setTitle(this.getString(R.string.title_activity_sign_up));

        // Bind back navigation action
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SignUpActivity.this.onBackNavigationClicked();
            }
        });

        // Find all layouts
        this.textInputFirstName = this.findViewById(R.id.text_input_first_name);
        this.textInputLastName = this.findViewById(R.id.text_input_last_name);
        this.textInputUsername = this.findViewById(R.id.text_input_username);
        this.textInputPassword = this.findViewById(R.id.text_input_password);
        this.textInputConfirmPassword = this.findViewById(R.id.text_input_confirm_password);

        this.button = this.findViewById(R.id.button_sign_up);

        // Set onClickListener for the button. This is where the input validation starts. We call all the verification methods with the information from the textInputs and validate each field (and set errors if applicable)
        this.button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Get first name
                final String firstName = SignUpActivity.this.textInputFirstName
                        .getEditText()
                        .getText()
                        .toString()
                        .trim();

                //get last name
                final String lastName = SignUpActivity.this.textInputLastName
                        .getEditText()
                        .getText()
                        .toString()
                        .trim();

                // Get username
                final String username = SignUpActivity.this.textInputUsername
                        .getEditText()
                        .getText()
                        .toString()
                        .trim();

                // Get password
                final String password = SignUpActivity.this.textInputPassword
                        .getEditText()
                        .getText()
                        .toString();

                // Get confirmed password
                final String confirmPassword = SignUpActivity.this.textInputConfirmPassword
                        .getEditText()
                        .getText()
                        .toString();

                // Only if ALL of the validation methods succeed do we proceed. We still have to check if the user exists in the database, which is what we do below.
                if (SignUpActivity.this.validateAll(firstName, lastName, username, password, confirmPassword)) {

                    // In order to prevent multiple attempts on the async task, we have to disable the button momentarily as it finishes.
                    SignUpActivity.this.button.setEnabled(false);

                    // Get repository and check if the user exists.
                    SignUpActivity.this.appPreferences
                            .getRepository()
                            .userExists(

                                    username,

                                    new OnSuccessListener<Boolean>() {
                                        @Override
                                        public void onSuccess(Boolean aBoolean) {

                                            // This block of code is run when the userExists() function succeeds (not when the user exists, we check that in the next line). //

                                            // If the user exists, then print an error message and return.
                                            if (aBoolean) {

                                                Toast.makeText(
                                                        SignUpActivity.this,
                                                        R.string.toast_error_user_exists,
                                                        Toast.LENGTH_LONG
                                                ).show();

                                                // Enable the button again
                                                SignUpActivity.this.button.setEnabled(true);

                                                return;
                                            }

                                            // At this point the user does NOT exist so we get the repository and register a user
                                            SignUpActivity.this.appPreferences
                                                    .getRepository()
                                                    .registerUser(

                                                            username,
                                                            password,
                                                            firstName,
                                                            lastName,

                                                            new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void aVoid) {

                                                                    // If the registration succeeds, print a toast message and finish()
                                                                    Toast.makeText(
                                                                            SignUpActivity.this,
                                                                            R.string.toast_success_registration,
                                                                            Toast.LENGTH_SHORT
                                                                    ).show();

                                                                    // Go back to login page. We don't have to show the button because this activity will be destroyed and upon a second call the button will be recreated again.
                                                                    SignUpActivity.this.finish();

                                                                }
                                                            },  // End of OnSuccessListener for registerUser()

                                                            new OnFailureListener() {
                                                                @Override
                                                                public void onFailure(@NonNull Exception e) {

                                                                    // If the registration failed, display a toast to the user.
                                                                    Toast.makeText(
                                                                            SignUpActivity.this,
                                                                            SignUpActivity.this.getString(R.string.toast_error_registration),
                                                                            Toast.LENGTH_LONG
                                                                    ).show();

                                                                    // Enable the button again
                                                                    SignUpActivity.this.button.setEnabled(true);

                                                                }
                                                            }  // End of OnFailureListener for registerUser()

                                                    );  // End of registerUser()


                                        }
                                    },  // End of OnSuccessListener for userExists()

                                    new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {

                                            // This block of code is run when the userExists() function fails. //

                                            // Log error
                                            Log.e(HomeActivity.LOG_TAG, "FAILED TO CHECK USER EXISTENCE: " + e.toString());

                                            // Display error toast.
                                            Toast.makeText(
                                                    SignUpActivity.this,
                                                    SignUpActivity.this.getString(R.string.toast_error_db),
                                                    Toast.LENGTH_LONG
                                            ).show();

                                            // Enable the button again
                                            SignUpActivity.this.button.setEnabled(true);

                                        }
                                    }  // End of OnFailureListener for userExists()

                            );  // End of userExists()

                }  // End of validateAll()

            }
        });  // End set onClickListener

    }

    /**
     * This method calls all the passed in information against the specific rules in the validate methods and returns an ANDed condition upon all the methods.
     * @param firstName       First name of the user
     * @param lastName        Last name of the user
     * @param username        Username of the account
     * @param password        Password of the account
     * @param confirmPassword Confirmed password of the account
     * @return                Returns a boolean that represents if all the methods passed or failed.
     */
    private boolean validateAll(String firstName, String lastName, String username, String password, String confirmPassword) {

        // Return an AND operation on all the validate methods. A single & means the boolean will not short circuit. Since we want to display
        return (this.validateFirstName(firstName) &
                this.validateLastName(lastName) &
                this.validateUsername(username) &
                this.validatePassword(password) &
                this.validateConfirmPassword(password, confirmPassword)
        );

    }

    /**
     * This method validates the first name field and sets an error in the TextInputLayout if it failed, depending on which error condition triggered.
     * @param firstName First name of the user
     * @return          Returns a boolean representing if the validation passed or failed.
     */
    private boolean validateFirstName(String firstName) {

        if (firstName.length() < 1) {

            this.textInputFirstName.setError(this.getString(R.string.error_no_value));
            return false;

        } else {

            this.textInputFirstName.setError(null);
            return true;

        }

    }

    /**
     * This method validates the last name field and sets an error in the TextInputLayout if it failed, depending on which error condition triggered.
     * @param lastName Last name of the user
     * @return         Returns a boolean representing if the validation passed or failed.
     */
    private boolean validateLastName(String lastName) {

        if (lastName.length() < 1) {

            this.textInputLastName.setError(this.getString(R.string.error_no_value));
            return false;

        } else {

            this.textInputLastName.setError(null);
            return true;

        }

    }

    /**
     * This method validates the username field and sets an error in the TextInputLayout if it failed, depending on which error condition triggered.
     * @param username Username of the account
     * @return         Returns a boolean representing if the validation passed or failed.
     */
    private boolean validateUsername(String username) {

        if (username.length() < 1) {

            this.textInputUsername.setError(this.getString(R.string.error_no_value));
            return false;

        } else if (username.contains(" ")) {

            this.textInputUsername.setError(this.getString(R.string.error_spaces));
            return false;

        } else {

            this.textInputUsername.setError(null);
            return true;

        }

    }

    /**
     * This method validates the password field and sets an error in the TextInputLayout if it failed, depending on which error condition triggered.
     * @param password Password of the account
     * @return         Returns a boolean representing if the validation passed or failed.
     */
    private boolean validatePassword(String password) {

        if (password.length() < 1) {

            this.textInputPassword.setError(this.getString(R.string.error_no_value));
            return false;

        } else if (password.contains(" ")) {

            this.textInputPassword.setError(this.getString(R.string.error_spaces));
            return false;

        } else {

            this.textInputPassword.setError(null);
            return true;

        }

    }

    /**
     * This method validates the first name field and sets an error in the TextInputLayout if it failed, depending on which error condition triggered.
     * @param password        Password of the account
     * @param confirmPassword The password, rewritten. If this does not equal <code>password</code>, then this test will fail.
     * @return                Returns a boolean representing if the validation passed or failed.
     */
    private boolean validateConfirmPassword(String password, String confirmPassword) {

        if (password.length() < 1) {

            this.textInputConfirmPassword.setError(this.getString(R.string.error_no_value));
            return false;

        } else if (! password.equals(confirmPassword)) {

            this.textInputConfirmPassword.setError(this.getString(R.string.error_no_pw_match));
            return false;

        } else {

            this.textInputConfirmPassword.setError(null);
            return true;

        }

    }

    /**
     * This method is called when the software navigation button is pressed. We log the event and make sure the activity is destroyed.
     */
    private void onBackNavigationClicked() {

        Log.d(HomeActivity.LOG_TAG, "Back navigation (Software) from " + this.getClass().getSimpleName());
        this.finish();

    }

    /**
     * This method is called when the hardware navigation button is pressed. We log the event and make sure the activity is destroyed.
     */
    @Override
    public void onBackPressed() {

        Log.d(HomeActivity.LOG_TAG, "Back navigation (Hardware) from " + this.getClass().getSimpleName());
        this.finish();

    }

}
