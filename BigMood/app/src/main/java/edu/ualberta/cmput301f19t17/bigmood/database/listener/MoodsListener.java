package edu.ualberta.cmput301f19t17.bigmood.database.listener;

import java.util.List;

import edu.ualberta.cmput301f19t17.bigmood.model.Mood;

/**
 * This interface defines a callback method for live mood updates from the database the repository pulls from.
 */
public interface MoodsListener {

    void onUpdate(List<Mood> moodList);

}
