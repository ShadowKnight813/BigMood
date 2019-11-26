package edu.ualberta.cmput301f19t17.bigmood.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import edu.ualberta.cmput301f19t17.bigmood.R;
import edu.ualberta.cmput301f19t17.bigmood.model.EmotionalState;
import edu.ualberta.cmput301f19t17.bigmood.model.Mood;

/**
 * This class serves as a custom ArrayAdapter specifically for Moods.
 * This Adapter does the following:
 * 1) Stores a collection of Ride objects in tandem with the ArrayList passed into its constructor.
 * 2) Inflates the different aspects of the row layout that are defined.
 */
public class MoodAdapter extends ArrayAdapter<Mood> implements Filterable {
    private final int resource;
    private ArrayList<Mood> currentMoodList;
    private ArrayList<Mood> originalMoodList;

    /**
     * This constructor is used to create a new MoodAdapter
     *
     * @param context  the activity that the MoodAdapter is created in
     * @param resource the ID of the layout resource that getView() would inflate to create the view
     * @param moodList the list of moods
     */
    public MoodAdapter(@NonNull Context context, int resource, @NonNull ArrayList<Mood> moodList) {
        super(context, resource, moodList);
        this.resource = resource;
        this.currentMoodList = moodList;
        this.originalMoodList = moodList;
    }

    /**
     * This method overrides the default one with the filtered array list's item
     *
     * @param position the position of the mood we want to get
     * @return the mood at position
     */
    @Override
    public Mood getItem(int position) {
        return currentMoodList.get(position);
    }

    /**
     * This method overrides the default one with the filtered array list's count
     *
     * @return null if the currentMoodList is null, or the size of the currentMoodList
     */
    @Override
    public int getCount() {
        return currentMoodList != null ? currentMoodList.size() : 0;
    }

    /**
     * This method gets called when a row is either being created or re-created (recycled).
     * Since findViewByIds can be expensive especially in a large list,
     * we cache the TextView objects in a small holder class we've defined below.
     *
     * @param position    the position of the view we are creating? TODO Cameron 10-26-2019 research position
     * @param convertView this is the view that we receive if the view is being recycled
     * @param parent      the parent ViewGroup that the view is contained within (Eg. LinearLayout)
     * @return convertView, which is either the recycled view, or the newly created/inflated view
     */
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        //This MoodHolder will hold our views while we create them
        MoodHolder moodHolder;

        // We test if convertView is null so we can know if we have to inflate it or not (findViewById)
        if (convertView == null) {

            // Define new inflater and inflate the view.
            LayoutInflater inflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(this.resource, parent, false);

            // Create new holder object since we are in a part of execution where the row has not been cached yet.
            moodHolder = new MoodHolder();

            // Set all fields of the holder class
            moodHolder.date = convertView.findViewById(R.id.mood_item_date);
            moodHolder.time = convertView.findViewById(R.id.mood_item_time);
            moodHolder.state = convertView.findViewById(R.id.mood_item_state);
            moodHolder.image = convertView.findViewById(R.id.mood_item_emoticon);

            // Cache views for that row using setTag on the full row view
            convertView.setTag(moodHolder);

        } else {

            // The row has been created and we can reuse it, but to change the fields in
            // the row we need to pull the holder from cache using getTag
            moodHolder = (MoodHolder) convertView.getTag();

        }

        // Get the current ride in the array using methods in ArrayAdapter
        Mood currentMood = this.getItem(position);

        // Set each of the fields in the row. For the date and time, we get the already formatted string from the Ride object. For the distance we do some manual formatting with the distance data.

        Date date = currentMood.getDatetime().getTime();

        moodHolder.state.setText(currentMood.getState().toString());
        moodHolder.date.setText(new SimpleDateFormat("yyyy-MM-dd", Locale.CANADA).format(date));
        moodHolder.time.setText(new SimpleDateFormat("HH:mm", Locale.CANADA).format(date));

        // Set image based on enum
        Resources res = this.getContext().getResources();
        Drawable emoticon = res.getDrawable(currentMood.getState().getDrawableId());
        moodHolder.image.setImageDrawable(emoticon);
        moodHolder.image.setTag(currentMood.getState().getDrawableId());

        // Return the created/reused view as per the method signature
        return convertView;
    }

    /**
     * This function reapplies the filter with the mood selected in the menu
     * If it's the first time running, this won't do anything
     *
     * @param menuItemFilter the item that we want to filter by
     * @param menu           the reference to the menu
     */
    public void applyFilter(View menuItemFilter, PopupMenu menu) {
        if (menuItemFilter == null || menu == null)
            return;

        int stateLen = menu.getMenu().size();

        // Traverse through the item list, filter the list with the selected mood
        for (int i = 0; i < stateLen; i++) {

            MenuItem item = menu.getMenu().getItem(i);

            if (item == null || (item.getItemId() == R.id.filter_none && item.isChecked())) {

                this.getFilter().filter("None");
                break;

            } else if (item.isChecked()) {

                this.getFilter().filter(item.getTitle().toString());

            }
        }

    }

    /**
     * This class implements Filterable to enable filtering mood list by emotional state.
     * This method is the implementation of a Filterable method.
     */
    @Override
    public Filter getFilter() {
        // Initialized a filter
        Filter filter = new Filter() {
            /**
             * This method creates and returns a sublist of the currentMoodList based off of the filter that was sent in.
             * @param constraint the filter that the user has suggested
             * @return the list of objects that make it through the filtering process
             */
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {

                // Setup results for the filter
                FilterResults results = new FilterResults();
                ArrayList<Mood> filteredMoodList = new ArrayList<Mood>();


                // If the user opt for "NONE" filter option, will return the original list
                // In case something unexpected happened, it will also return the original list

                if (constraint == null || constraint.toString().equals("None") || constraint.toString().length() == 0) {

                    results.count = originalMoodList.size();
                    results.values = originalMoodList;
                } else {

                    // Select the mood with the matching criteria and add them into the filteredMoodList
                    for (int i = 0; i < originalMoodList.size(); i++) {
                        Mood currentMood = originalMoodList.get(i);
                        EmotionalState emotionalState = currentMood.getState();
                        String state = emotionalState.toString();
                        if (state.startsWith(constraint.toString())) {
                            filteredMoodList.add(currentMood);
                        }
                    }

                    results.count = filteredMoodList.size();
                    results.values = filteredMoodList;
                }

                // return the result with its count and values (the list we choose to show)
                return results;

            }

            /**
             * This method will publish the result according to the selected filter
             * This also tell the adapter that the current list has change, hence updating
             * the List View
             * @param constraint the filter item that the user selected
             * @param results the resulting list that comes from the original list being filtered
             */
            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                // If the result is not null (filter applied), assign the new filteredMoodList
                if (results != null && results.values != null) {
                    currentMoodList = (ArrayList<Mood>) results.values;
                    notifyDataSetChanged();
                } else {
                    // If no filter, or null, set the array to the original one
                    currentMoodList = originalMoodList;
                }
            }

        };
        return filter;
    }

    /**
     * This class is a small helper class to cache the views taken from
     * convertView.findViewById() since these finds can be expensive when in a ListView.
     * It just holds TextView resources we'll get and set in this class only.
     */
    private static class MoodHolder {
        //TODO Cameron 10-26-2019 implement location and image?
        TextView date;
        TextView time;
        TextView state;
        ImageView image;
    }
}


