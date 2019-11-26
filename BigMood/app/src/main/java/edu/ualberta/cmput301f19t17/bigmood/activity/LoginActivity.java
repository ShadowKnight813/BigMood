package edu.ualberta.cmput301f19t17.bigmood.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputLayout;

import edu.ualberta.cmput301f19t17.bigmood.R;
import edu.ualberta.cmput301f19t17.bigmood.database.User;

/**
 * LoginActivity is the activity that allows the user to log into the app with their unique username and their password.
 * It also allows the user to navigate to the SignUpFragment, where they can create an account.
 */
public class LoginActivity extends AppCompatActivity {

    private AppPreferences appPreferences;

    private TextInputLayout textInputUsername;
    private TextInputLayout textInputPassword;

    private Button buttonLogin;

    /**
     * onCreate is called when the Activity is created, and it is used to perform the logic that the Activity
     * needs, such as setting onClickListeners.
     * @param savedInstanceState if the instance was saved, this would be sent in when the Activity is created
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        this.appPreferences = AppPreferences.getInstance();

        // Get ids of the textfields
        this.textInputUsername = (TextInputLayout) this.findViewById(R.id.text_input_username);
        this.textInputPassword = (TextInputLayout) this.findViewById(R.id.text_input_password);

        // Get Id of button
        this.buttonLogin = (Button) this.findViewById(R.id.button_login);

        // Get Id of clickable Textview
        TextView buttonSignup = (TextView) this.findViewById(R.id.textview_button_sign_up);

        // Set the textview to navigate to the sign up activity
        buttonSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
                LoginActivity.this.startActivity(intent);

            }
        });

        // Set the onClickListener for the button. We want it to be able to log in the user using the repository's methods in the Preferences file.
        this.buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Basically creates an or switch between both the username and password. The reason for this is because we want ALL errors to appear, not just the first one.
                boolean failed = false;

                // Get the username and password. Trim the username to remove any leading or trailing spaces. We do NOT do this to the password.
                String username = LoginActivity.this.textInputUsername.getEditText().getText().toString().trim();
                String password = LoginActivity.this.textInputPassword.getEditText().getText().toString();

                // If the username is empty, set an error and continue. If it isn't we clear an error if there is one.
                if (username.isEmpty()) {
                    LoginActivity.this.textInputUsername.setError(LoginActivity.this.getString(R.string.error_empty_username));
                    failed = true;
                } else {
                    LoginActivity.this.textInputUsername.setError(null);
                }

                // If the password is empty, set an error and continue. If it isn't we clear an error if there is one.
                if (password.isEmpty()) {
                    LoginActivity.this.textInputPassword.setError(LoginActivity.this.getString(R.string.error_empty_password));
                    failed = true;
                } else {
                    LoginActivity.this.textInputPassword.setError(null);
                }

                // If any of the above failed there is no reason to submit so we return.
                if (failed)
                    return;

                // Since we have to wait for an async task we have to disable the button so further attempts are not submitted. We enable it later on.
                LoginActivity.this.buttonLogin.setEnabled(false);

                // Validate the user
                LoginActivity.this.appPreferences
                        .getRepository()
                        .validateUser(
                                username,
                                password,
                                new OnSuccessListener<User>() {
                                    @Override
                                    public void onSuccess(User user) {

                                        // When the user is null that means that the read succeeded but either the username or password didn't match. Therefore we have to let the user know.
                                        if (user == null) {
                                            Toast.makeText(LoginActivity.this, R.string.toast_error_incorrect_user_pw, Toast.LENGTH_SHORT).show();

                                            // Enable the button again
                                            LoginActivity.this.buttonLogin.setEnabled(true);

                                            return;
                                        }

                                        // Set user in the ViewModel
                                        LoginActivity.this.appPreferences.login(user);

                                        // Go to the home screen
                                        Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                                        LoginActivity.this.startActivity(intent);

                                        // Clear password field
                                        LoginActivity.this.textInputPassword.getEditText().setText("");

                                        // Enable the button again
                                        LoginActivity.this.buttonLogin.setEnabled(true);

                                    }
                                },  // End of OnSuccessListener
                                new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {

                                        // Create some text t
                                        Toast.makeText(LoginActivity.this, R.string.toast_error_login, Toast.LENGTH_SHORT).show();

                                        // Log the error
                                        Log.d(HomeActivity.LOG_TAG, "User Validation failed: " + e.toString());

                                        // Enable the button again
                                        LoginActivity.this.buttonLogin.setEnabled(true);

                                    }
                                }  // End of OnFailureListener
                        );  // End of validateUser()

            }  // End of onClick()
        });  // End of setOnClickListener



    }

}
