package edu.ualberta.cmput301f19t17.bigmood.activity;

import android.util.Log;

import androidx.annotation.VisibleForTesting;

import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.List;

import edu.ualberta.cmput301f19t17.bigmood.database.FirestoreRepository;
import edu.ualberta.cmput301f19t17.bigmood.database.Repository;
import edu.ualberta.cmput301f19t17.bigmood.database.User;
import edu.ualberta.cmput301f19t17.bigmood.database.listener.FollowingListener;

/**
 * This Preferences class is a singleton class that holds the currently activated User,
 * as well as the reference to a repository interface. This is in most cases Firestore
 * but could point to an in-memory database for testing purposes for example.
 */
public class AppPreferences {

    private static AppPreferences preferences = null;

    private Repository repository;

    private User currentUser;

    private List<String> followingList;
    private ListenerRegistration followerRegistration;

    /**
     * Since this is a singleton, we force the user of the class to call getInstance()
     * which will create a new instance of the class if the instance does not already exist.
     * This ensures there is a single instance, hence "singleton"
     * @return the instance of AppPreferences
     */
    public static AppPreferences getInstance() {
        if (preferences == null)
            preferences = new AppPreferences();

        return preferences;
    }

    /**
     * This constructor initializes the "class" attributes.
     * We set currentUser to null since it is the job of the repository to
     * validate the user whenever they try to login. At that point, we will
     * set the user using the setter below
     */
    private AppPreferences() {
        this.currentUser = null;
        this.followingList = new ArrayList<>();

        // This is by default, using setRepository() you can switch it out.
        this.repository = FirestoreRepository.getInstance();
    }

    /**
     * This function removes the followerRegistration if it exists. We are supposed to remove any ListenerRegistration that we don't need anymore because it can and will update when we don't expect it to.
     */
    private void removeRegistration() {

        if (this.followerRegistration != null) {

            // Remove the registration and set to null
            this.followerRegistration.remove();
            this.followerRegistration = null;

        }

    }

    /**
     * This method sets the current user to a validated user. This "logs" the user into the system.
     * @param user the <code>User</code> to log in
     */
    public void login(User user) {

        if (user == null)
            throw new IllegalArgumentException("You cannot log in with a null user. If you were looking to log out. use the logout() method.");

        // Just for safety, we log out first before "logging in" again. This makes sure that everything is cleared (like the ListenerRegistration) before we log in again.
        this.logout();

        // Set the current user to the one passed in.
        this.currentUser = user;

        // Set up a listener to update the list whenever there is a repository change.
        this.followerRegistration = this.repository.getFollowingList(

                this.currentUser,

                new FollowingListener() {
                    @Override
                    public void onUpdate(List<String> followingList) {

                        // Clear the current list and add everything from the list we get from the callback.
                        AppPreferences.this.followingList.clear();
                        AppPreferences.this.followingList.addAll(followingList);

                        Log.d(HomeActivity.LOG_TAG, "follower list updated:\n" + followingList.toString());

                    }
                }

        );


    }

    /**
     * This method logs out the current user and clears all related variables.
     */
    public void logout() {

        // In order to log out the current user we have to clear the currentUser variable and clear the followingList so that it does not bleed over into the next login.
        this.currentUser = null;
        this.followingList.clear();
        this.removeRegistration();

    }

    /**
     * This method returns the current user.
     * This is useful for firestore access
     * @return the current user
     */
    public User getCurrentUser() {
        return this.currentUser;
    }

    /**
     * This method returns the current user.
     * This is useful for accessing firestore methods in the database package, as
     * the repository contains all of the methods to access firestore
     * @return the repository
     */
    public Repository getRepository() {
        return this.repository;
    }

    /**
     * This method returns the cached follower list from the preferences.
     * @return A copy of the follower list for use in other queries.
     */
    public List<String> getFollowingList() {
        return new ArrayList<>(followingList);
    }

    /**
     * This method sets the repository to a new repository.
     * This can be used for replacing the repository class
     * with a testable in-memory database.
     * @param repository the repo to set
     */
    @VisibleForTesting
    public void setRepository(Repository repository) {
        this.repository = repository;
    }

}
