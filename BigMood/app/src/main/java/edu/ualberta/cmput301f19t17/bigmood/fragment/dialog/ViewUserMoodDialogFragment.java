package edu.ualberta.cmput301f19t17.bigmood.fragment.dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import edu.ualberta.cmput301f19t17.bigmood.R;
import edu.ualberta.cmput301f19t17.bigmood.activity.HomeActivity;
import edu.ualberta.cmput301f19t17.bigmood.model.Mood;

/**
 * ViewUserMoodDialogFragment is used to view a mood of the current user. It subclasses ViewMoodDialogFragment
 * and it adds the Edit and Delete buttons.
 */
public class ViewUserMoodDialogFragment extends ViewMoodDialogFragment {

    // Define new listener
    private @NonNull OnButtonPressListener listener;

    /**
     * This is an interface contained by this class to define the method for the save action. A class can either implement this or define it as a new anonymous class
     */
    public interface OnButtonPressListener {
        void onDeletePressed(Mood moodToDelete);
        void onEditPressed(Mood moodToEdit);
    }

    /**
     * This is the default constructor. Since this dialog has buttons, we set a default listener to avoid a crash if for some reason it is not overridden. This should not happen in the code, but it is here as a good measure.
     */
    public ViewUserMoodDialogFragment() {

        this.listener = new OnButtonPressListener() {
            @Override
            public void onDeletePressed(Mood moodToDelete) {
                this.logError();
            }

            @Override
            public void onEditPressed(Mood moodToEdit) {
                this.logError();
            }

            private void logError() {
                Log.e(HomeActivity.LOG_TAG, "ViewMoodDialogFragment.OnButtonPressListener is NOT IMPLEMENTED");
            }
        };

    }

    /**
     * This method creates a new instance of a ViewMoodDialogFragment for the purposes of viewing a Mood. This method should be used instead of the base constructor as the mood must get put into the arguments Bundle of the Fragment.
     * @param mood The user mood to view
     * @return     A new instance of ViewMoodDialogFragment.
     */
    public static ViewUserMoodDialogFragment newInstance(Mood mood) {

        // Define new Bundle for storing arguments
        Bundle args = new Bundle();

        // Put arguments in Bundle
        args.putParcelable(Mood.TAG_MOOD_OBJECT, mood);

        // Create new stock fragment and set arguments
        ViewUserMoodDialogFragment fragment = new ViewUserMoodDialogFragment();
        fragment.setArguments(args);

        return fragment;

    }

    /**
     * This sets the OnButtonPressListener for the edit and delete actions.
     * @param listener The listener to set
     */
    public void setOnButtonPressListener(@NonNull OnButtonPressListener listener) {
        this.listener = listener;
    }

    /**
     * This method overrides the method from the superclass. It is responsible for creating a new dialog. In this case it is an AlertDialog. Specifically we set the buttons on the dialog and the listener.
     * @param view This is the view that the Dialog has to attach to. We presume this has already been created.
     * @return     Fully built dialog
     */
    @Override
    protected Dialog buildDialog(View view) {

        // Define new AlertDialog.Builder so we can display a custom dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        // Build and return the dialog. We set the onClick listeners for each button to point to a different method in our interface which take different parameters
        return builder
                .setView(view)
                .setPositiveButton(this.getText(R.string.menu_option_edit), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        Log.d(HomeActivity.LOG_TAG, "EDIT button clicked");
                        ViewUserMoodDialogFragment.this.listener.onEditPressed(ViewUserMoodDialogFragment.this.moodToView);

                    }
                })
                .setNeutralButton(this.getText(R.string.menu_option_delete), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        Log.d(HomeActivity.LOG_TAG, "DELETE button clicked");
                        ViewUserMoodDialogFragment.this.listener.onDeletePressed(ViewUserMoodDialogFragment.this.moodToView);

                    }
                })
                .create();
    }


}
