package edu.ualberta.cmput301f19t17.bigmood.database.listener;

import java.util.List;

public interface FollowingListener {

    void onUpdate(List<String> followingList);

}
