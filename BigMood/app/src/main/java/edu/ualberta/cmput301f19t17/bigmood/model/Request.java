package edu.ualberta.cmput301f19t17.bigmood.model;

import edu.ualberta.cmput301f19t17.bigmood.database.User;

/**
 * This class models a request object. It has fields representing a requester (from) and a requestee (to). This is used for the purposes of viewing the requests in a ListView.
 */
public class Request {

    private final String firestoreId;

    private final String from;
    private final String to;

    /**
     * This constructor is used to call a request, and this is what will be called inside the fragments
     * @param user the currently logged in user
     * @param to the username of the person that the user is sending a request to
     */
    public Request(User user, String to) {

        if (user == null || to == null)
            throw new IllegalArgumentException("Both the 'user' and 'to' fields have to exist.");

        this.firestoreId = null;

        this.from = user.getUsername();
        this.to = to;

    }

    /**
     * This constructor is the same as the first, but contains a firestore ID, which it will only have if we are pulling
     * the request from FireStore
     * @param firestoreId the firestoreID of the request
     * @param from the username of the person that is sending the request
     * @param to the username of the person that the user is sending a request to
     */
    public Request(String firestoreId, String from, String to) {

        if (firestoreId == null || from == null || to == null)
            throw new IllegalArgumentException("All of firestoreId, from, and to fields have to exist.");

        this.firestoreId = firestoreId;

        this.from = from;
        this.to = to;
    }

    /**
     * Gets the firestoreID
     * @return the firestoreID of the request
     */
    public String getFirestoreId() {
        return firestoreId;
    }

    /**
     * Gets the from
     * @return the person who is sending the request
     */
    public String getFrom() {
        return from;
    }

    /**
     * Gets the to
     * @return the person who the request is sent to
     */
    public String getTo() {
        return to;
    }

}
