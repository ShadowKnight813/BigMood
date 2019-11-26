package edu.ualberta.cmput301f19t17.bigmood.fragment.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.List;

import edu.ualberta.cmput301f19t17.bigmood.R;
import edu.ualberta.cmput301f19t17.bigmood.activity.AppPreferences;
import edu.ualberta.cmput301f19t17.bigmood.activity.HomeActivity;
import edu.ualberta.cmput301f19t17.bigmood.adapter.MoodAdapter;
import edu.ualberta.cmput301f19t17.bigmood.database.listener.MoodsListener;
import edu.ualberta.cmput301f19t17.bigmood.fragment.dialog.DefineMoodDialogFragment;
import edu.ualberta.cmput301f19t17.bigmood.fragment.dialog.MapDialogFragment;
import edu.ualberta.cmput301f19t17.bigmood.fragment.dialog.ViewUserMoodDialogFragment;
import edu.ualberta.cmput301f19t17.bigmood.model.EmotionalState;
import edu.ualberta.cmput301f19t17.bigmood.model.Mood;

/**
 * UserMoodsFragment houses the logic for displaying as a list the current moods that the user has under their profile.
 */
public class UserMoodsFragment extends Fragment {

    private AppPreferences appPreferences;

    private ArrayList<Mood> moodList;
    private MoodAdapter moodAdapter;

    private EmotionalState filter = null;

    private View menuItemFilter = null;
    private PopupMenu menu = null;

    private ListenerRegistration listenerRegistration;

    /**
     * of the on*()methods, this is the second. After the dialog has been started we want to inflate the dialog.
     * This is where we inflate all the views and *if applicable* populate all the fields.
     *
     * @param inflater           View inflater service
     * @param container          Container that the inflater is housed in
     * @param savedInstanceState A bundle that holds the state of the fragment
     * @return Returns the inflated view
     */
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_user_moods, container, false);

        // Enable options menu
        this.setHasOptionsMenu(true);

        // Set App Preferences
        this.appPreferences = AppPreferences.getInstance();

        // Initialize a new ArrayList
        this.moodList = new ArrayList<>();
        this.moodAdapter = new MoodAdapter(root.getContext(), R.layout.mood_item, moodList);

        ListView moodListView = root.findViewById(R.id.mood_list);

        moodListView.setAdapter(moodAdapter);

        FloatingActionButton fab = root.findViewById(R.id.floatingActionButton);

        // Set up the MoodsListener to listen to updates in FireStore
        this.listenerRegistration = this.appPreferences
                .getRepository()
                .getUserMoods(

                        this.appPreferences.getCurrentUser(),

                        new MoodsListener() {
                            /**
                             * This method is called whenever the listener hears that there is an update in the moodList
                             * in FireStore, and updates the list, and applies a filter, if the user has selected one
                             *
                             * @param moodList the new list that has the updated values
                             */
                            @Override
                            public void onUpdate(List<Mood> moodList) {

                                UserMoodsFragment.this.moodList.clear();
                                UserMoodsFragment.this.moodList.addAll(moodList);
                                UserMoodsFragment.this.moodAdapter.notifyDataSetChanged();

                                // This refreshes the filter with the updated data
                                UserMoodsFragment.this.moodAdapter.applyFilter(menuItemFilter, menu);

                            }
                        });

        fab.setOnClickListener(new View.OnClickListener() {
            /**
             * This method is called when the FAB is clicked on
             *
             * @param v the FAB itself
             */
            @Override
            public void onClick(View v) {

                // create an instance of DefineMoodDialogFragment since the user wants to add a new Mood
                DefineMoodDialogFragment addMoodFragment = DefineMoodDialogFragment.newInstance();
                addMoodFragment.setOnButtonPressListener(
                        new DefineMoodDialogFragment.OnButtonPressListener() {
                            /**
                             * This method is called when the "Save" button is pressed in the DefineMoodDialogFragment
                             * @param moodToSave the mood that the user wants to save, created from values that they inputted
                             */
                            @Override
                            public void onSavePressed(Mood moodToSave) {

                                // Create the mood using the repository.

                                UserMoodsFragment.this.appPreferences
                                        .getRepository()
                                        .createMood(

                                                UserMoodsFragment.this.appPreferences.getCurrentUser(),
                                                moodToSave,

                                                // We do this because we don't want to handle anything in the success case.
                                                null,

                                                new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {

                                                        // Log error
                                                        Log.e(HomeActivity.LOG_TAG, "Mood failed to save (add) with exception: " + e.toString());

                                                        // Show UI feedback if deletion failed
                                                        Toast.makeText(
                                                                UserMoodsFragment.this.getContext(),
                                                                R.string.toast_error_add_mood,
                                                                Toast.LENGTH_SHORT
                                                        ).show();

                                                    }
                                                }  // End of OnFailureListener for createMood()

                                        );  // End of createMood()

                            }

                        });  // End setOnButtonPressListener

                // Show the add mood fragment once the save button listener has been defined.
                addMoodFragment.show(getFragmentManager(), "FRAGMENT_DEFINE_MOOD_ADD");

            }

        }); // End setOnClickListener

        // Set the on item click listener for the ListView. Recall that we have to display something, and then on an delete or edit event, we must do something else.
        moodListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                // Create dialog and set the button press listener for delete and edit
                ViewUserMoodDialogFragment viewUserFragment = ViewUserMoodDialogFragment.newInstance(moodAdapter.getItem(i));
                viewUserFragment.setOnButtonPressListener(new ViewUserMoodDialogFragment.OnButtonPressListener() {
                    @Override
                    public void onDeletePressed(Mood moodToDelete) {

                        // If the user happens to be null, throw an error
                        if (UserMoodsFragment.this.appPreferences.getCurrentUser() == null)
                            throw new IllegalStateException("The current user is null, this should not happen. Did the user log in correctly?");

                        // Use the repository to delete the mood.
                        UserMoodsFragment.this.appPreferences
                                .getRepository()
                                .deleteMood(

                                        UserMoodsFragment.this.appPreferences.getCurrentUser(),
                                        moodToDelete,

                                        null,

                                        new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {

                                                // Log error
                                                Log.e(HomeActivity.LOG_TAG, "Mood failed to delete with exception: " + e.toString());

                                                // Show UI feedback if deletion failed
                                                Toast.makeText(
                                                        UserMoodsFragment.this.getContext(),
                                                        R.string.toast_error_delete_mood,
                                                        Toast.LENGTH_SHORT
                                                ).show();

                                            }
                                        }  // End of OnFailureListener for deleteMood()

                                );  // End of deleteMood()

                    }  // End onDeletePressed()

                    @Override
                    public void onEditPressed(final Mood moodToEdit) {

                        // If the user happens to be null, throw an error
                        if (UserMoodsFragment.this.appPreferences.getCurrentUser() == null)
                            throw new IllegalStateException("The current user is null, this should not happen. Did the user log in correctly?");

                        // Define a dialog fragment in the edit mode and set the listener for the save button.
                        DefineMoodDialogFragment editMoodFragment = DefineMoodDialogFragment.newInstance(moodToEdit);
                        editMoodFragment.setOnButtonPressListener(
                                new DefineMoodDialogFragment.OnButtonPressListener() {
                                    /**
                                     * This method is called when the "Save" button is pressed in the DefineMoodDialogFragment
                                     * @param moodToSave the mood that the user wants to save, created from values that they inputted,
                                     *                   and the
                                     */
                                    @Override
                                    public void onSavePressed(Mood moodToSave) {

                                        // Update the mood using the repository.
                                        UserMoodsFragment.this.appPreferences
                                                .getRepository()
                                                .updateMood(

                                                        UserMoodsFragment.this.appPreferences.getCurrentUser(),
                                                        moodToSave,

                                                        null,

                                                        new OnFailureListener() {
                                                            @Override
                                                            public void onFailure(@NonNull Exception e) {

                                                                // Log error
                                                                Log.e(HomeActivity.LOG_TAG, "Mood failed to save (edit) with exception: " + e.toString());

                                                                // Show UI feedback if deletion failed
                                                                Toast.makeText(
                                                                        UserMoodsFragment.this.getContext(),
                                                                        R.string.toast_error_save_mood,
                                                                        Toast.LENGTH_SHORT
                                                                ).show();

                                                            }
                                                        }  // End of OnFailureListener for updateMood()

                                                );  // End of updateMood()

                                    }
                                });  // End of setOnButtonPressListener() for the SAVE button is DefineMoodDialogFragment.

                        // Show the edit fragment after defining the SAVE button behaviour
                        editMoodFragment.show(getFragmentManager(), "FRAGMENT_DEFINE_MOOD_EDIT");

                    }  // End onEditPressed()

                });  // End of setOnButtonPressListener()

                // Show the view Dialog after setting the behaviour of the delete button and the edit button.
                viewUserFragment.show(getFragmentManager(), "FRAGMENT_VIEW_MOOD");

            }
        });  // End setOnItemClickListener

        return root;

    }

    /**
     * We need to unbind the ListenerRegistration so that updates do not occur in the background, so we have to make sure we do that upon exit only.
     */
    @Override
    public void onDestroyView() {

        this.listenerRegistration.remove();

        super.onDestroyView();

    }

    /**
     * This method gets called when the fragment needs to assemble menu options.
     *
     * @param menu     The options menu in which you place your items.
     * @param inflater The menu inflater
     */
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {

        inflater.inflate(R.menu.fragment_user_moods, menu);

        super.onCreateOptionsMenu(menu, inflater);

    }

    /**
     * This method gets called when a menu item in the toolbar is clicked. We only have one item here so we only check one
     *
     * @param item The menu item that was selected. This value must never be null.
     * @return Return false to allow normal menu processing to proceed, true to consume it here.
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == R.id.action_filter) {

            // If the menuItemFilter is uninitialized, then find it
            if (this.menuItemFilter == null)
                this.menuItemFilter = (View) this.getActivity().findViewById(R.id.action_filter);

            // If the menu is uninitialized, inflate it only once to save on performance
            if (this.menu == null) {

                this.menu = new PopupMenu(this.getContext(), this.menuItemFilter);
                this.menu.getMenuInflater().inflate(R.menu.filter_states, menu.getMenu());

                // Add all emotional states to the menu
                for (EmotionalState state : EmotionalState.values())
                    this.menu.getMenu().add(R.id.group_filter, state.getStateCode(), Menu.NONE, state.toString());

                // Set the checkable state of the group
                this.menu.getMenu().setGroupCheckable(R.id.group_filter, true, true);

                // Set the onclick listener
                this.menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    /**
                     * This method is called when an item in the Filter list is clicked
                     * @param item the item that was clicked
                     * @return true // TODO: 2019-11-07 Cameron: not sure why it always returns true, investigate
                     */
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {

                        // Once we click an item, we have to set the appropriate filter. In the case of the none item, we select that, and for every other action we set it to the correct emotional state. Keep in mind that we set the item id for each emotional state menu item to exactly the statecode, so it is easy to reverse match it here.
                        if (item.getItemId() == R.id.filter_none) {

                            UserMoodsFragment.this.filter = null;

                            // Show the full list
                            moodAdapter.getFilter().filter("None");

                        } else {

                            // Filter the list based on the selected item's title
                            UserMoodsFragment.this.filter = EmotionalState.findByStateCode(item.getItemId());
                            moodAdapter.getFilter().filter(filter.toString());

                        }

                        // For any menu item click we set the checked state to true and return true.
                        item.setChecked(true);

                        return true;

                    }
                });

            }  // end of menu initialization
            // We now have a complete menu but in order to render it properly we need to set the item that is selected to checked. We iterate through every state and if it matches with the current filter, set its checked state to true.
            for (EmotionalState state : EmotionalState.values()) {
                MenuItem menuItem = this.menu.getMenu().findItem(state.getStateCode());

                if (this.filter == state)
                    menuItem.setChecked(true);

            }

            // If the filter happens to be null, that means that there is no filter, so we set the checked state of the "None" item.
            if (this.filter == null)
                this.menu.getMenu().findItem(R.id.filter_none).setChecked(true);

            // Show the menu
            menu.show();

        } else if (item.getItemId() == R.id.action_maps_user) {

            Toast.makeText(this.getContext(), "Display User Maps", Toast.LENGTH_SHORT).show();
            MapDialogFragment mapDialogFragment = new MapDialogFragment(moodAdapter);
            mapDialogFragment.show(getFragmentManager(), "FRAGMENT_VIEW_USER_MAP");

        }
        return true;
    }


}

