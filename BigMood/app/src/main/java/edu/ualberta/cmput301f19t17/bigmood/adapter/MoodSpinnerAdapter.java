package edu.ualberta.cmput301f19t17.bigmood.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

import edu.ualberta.cmput301f19t17.bigmood.R;
import edu.ualberta.cmput301f19t17.bigmood.model.EmotionalState;

/**
 * This class provides a ArrayAdapter for the mood/state spinner so that it can display the mood emoticons.
 */
public class MoodSpinnerAdapter extends ArrayAdapter<EmotionalState> {

    /**
     * This constructor is used to create a MoodSpinnerAdapter.
     *
     * @param context the activity that the MoodSpinnerAdapter will be created in.
     * @param resource the ID of the layout resource that getView() would inflate to create the view.
     * @param moodSpinnerArrayList a list of EmotionalStates.
     */
    public MoodSpinnerAdapter(@NonNull Context context, int resource, @NonNull ArrayList<EmotionalState> moodSpinnerArrayList) {
        super(context, resource, moodSpinnerArrayList);
    }

    /**
     * This sets up how the spinner will look.
     *
     * @param position The position of the item within the adapter's data set of the item whose view we want.
     * @param convertView The old view to reuse, if possible.
     * @param parent the parent that this view will be attached to.
     * @return  returns a View corresponding to the data at the specified position.
     */
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.mood_spinner_item,parent,false);
        }
        ImageView emoticonImageView = convertView.findViewById(R.id.mood_spinner_item_emoticon);
        TextView stateTextView = convertView.findViewById(R.id.mood_spinner_item_state);

        EmotionalState emotionalState = getItem(position);

        emoticonImageView.setImageResource(emotionalState.getDrawableId());
        stateTextView.setText(emotionalState.toString());

        return convertView;
    }

    /**
     * This sets up how the spinner items will look.
     *
     * @param position The position of the item within the adapter's data set of the item whose view we want.
     * @param convertView The old view to reuse, if possible.
     * @param parent the parent that this view will be attached to.
     * @return  returns a View corresponding to the data at the specified position.
     */
    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.mood_spinner_item,parent,false);
        }
        ImageView emoticonImageView = convertView.findViewById(R.id.mood_spinner_item_emoticon);
        TextView stateTextView = convertView.findViewById(R.id.mood_spinner_item_state);

        EmotionalState emotionalState = getItem(position);

        emoticonImageView.setImageResource(emotionalState.getDrawableId());
        stateTextView.setText(emotionalState.toString());

        return convertView;
    }


}