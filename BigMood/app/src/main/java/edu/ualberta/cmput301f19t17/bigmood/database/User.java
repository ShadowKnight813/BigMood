package edu.ualberta.cmput301f19t17.bigmood.database;

/**
 * This is meant to be an object representing a "database user". It is part of our """authentication""". Therefore, the only way to instantiate a user is to get it from another class in this package, namely from the Repository.
 */
public class User {

    private final String username;
    private final String firstName;
    private final String lastName;

    /**
     * Creates a new User object.
     * @param username  Username of the user. This is also the unique ID of the document in the Firestore DB.
     * @param firstName First name of user
     * @param lastName  Last name of user
     */

    User(String username, String firstName, String lastName) {

        if (username == null || firstName == null || lastName == null)
            throw new IllegalArgumentException("All of username, firstName, and lastName must not be null.");

        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    /**
     * Gets the username
     * @return A string containing the username of the user
     */
    public String getUsername() {
        return username;
    }

    /**
     * Gets the first name
     * @return A string containing the first name of the user
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * gets the last name
     * @return A string containing the last name of the user
     */
    public String getLastName() {
        return lastName;
    }


}
