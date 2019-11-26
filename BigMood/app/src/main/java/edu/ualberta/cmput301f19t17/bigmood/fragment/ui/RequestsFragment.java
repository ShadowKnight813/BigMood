package edu.ualberta.cmput301f19t17.bigmood.fragment.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.List;

import edu.ualberta.cmput301f19t17.bigmood.R;
import edu.ualberta.cmput301f19t17.bigmood.activity.AppPreferences;
import edu.ualberta.cmput301f19t17.bigmood.adapter.RequestAdapter;
import edu.ualberta.cmput301f19t17.bigmood.database.listener.RequestsListener;
import edu.ualberta.cmput301f19t17.bigmood.model.Request;

/**
 * RequestsFragment is used to view the follow requests that the user has received.
 */
public class RequestsFragment extends Fragment {

    private AppPreferences appPreferences;

    private ArrayList<Request> requestList;
    private RequestAdapter requestAdapter;

    private ListenerRegistration listenerRegistration;

    /**
     * of the on*()methods, this is the second. After the dialog has been started we want to inflate the dialog.
     * This is where we inflate all the views and *if applicable* populate all the fields.
     * @param inflater           View inflater service
     * @param container          Container that the inflater is housed in
     * @param savedInstanceState A bundle that holds the state of the fragment
     * @return                   Returns the inflated view
     */
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_requests, container, false);
        this.appPreferences = AppPreferences.getInstance();

        // Initialize a new ArrayList
        this.requestList = new ArrayList<>();
        this.requestAdapter = new RequestAdapter(root.getContext(), R.layout.request_item, requestList);

        ListView requestListView = root.findViewById(R.id.request_list);
        requestListView.setAdapter(requestAdapter);

        // Set up the RequestsListener to listen to updates in FireStore
        this.listenerRegistration = this.appPreferences
                .getRepository()
                .getUserRequests(

                        this.appPreferences.getCurrentUser(),

                        new RequestsListener() {
                            /**
                             * This method is called whenever the listener hears that there is an update in the requestList
                             * in FireStore, and updates the list
                             * @param requestList the new list that has the updated values
                             */
                            @Override
                            public void onUpdate(List<Request> requestList) {

                                RequestsFragment.this.requestList.clear();
                                RequestsFragment.this.requestList.addAll(requestList);
                                RequestsFragment.this.requestAdapter.notifyDataSetChanged();

                            }
                        });

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


}