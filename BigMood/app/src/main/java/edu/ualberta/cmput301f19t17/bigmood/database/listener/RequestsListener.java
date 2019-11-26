package edu.ualberta.cmput301f19t17.bigmood.database.listener;

import java.util.List;

import edu.ualberta.cmput301f19t17.bigmood.model.Request;

/**
 * This interface defines a callback method for live request updates from the database the repository pulls from.
 */
public interface RequestsListener {

    void onUpdate(List<Request> requestList);

}
