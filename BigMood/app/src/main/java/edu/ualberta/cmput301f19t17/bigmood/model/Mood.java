package edu.ualberta.cmput301f19t17.bigmood.model;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.firestore.GeoPoint;

import java.util.Calendar;
import java.util.TimeZone;

/**
 * This class models a mood object.
 */
public class Mood implements Parcelable {

    // Tag for Parceling
    public static final String TAG_MOOD_OBJECT = "edu.ualberta.cmput301f19t17.bigmood.model.MOOD_OBJECT";

    // Stored because it is required for deletion. Once we pull the data from the DB there is no way to distinguish it from other Mood objects, so we have to store this information.
    private final String firestoreId;

    // Mood instance variables
    private EmotionalState state;
    private Calendar datetime;
    private SocialSituation situation;
    private String reason;
    private GeoPoint location;
    private Bitmap image;

    /**
     * Base constructor for a NEW mood. This constructor should not be used to recreate a mood from the database, since the firestoreId will be null.
     * @param state     The emotional state of the mood. This can be any one of the categories defined in the EmotionalState enum.
     * @param datetime  The Calendar object representing the date and time that the mood was created.
     * @param situation An optional social situation of the mood. This can be any one of the categories defined in the SocialSituation enum.
     * @param reason    An optional textual reason describing why the user felt the mood. This cannot be null.
     * @param location  An optional location representing the longitude and latitude of the mood.
     * @param image     An optional image that the user can attach to the mood.
     */
    public Mood(EmotionalState state, Calendar datetime, SocialSituation situation, String reason, GeoPoint location, Bitmap image) {
        this.firestoreId = null;

        if (state == null || datetime == null)
            throw new IllegalArgumentException("`state`and `datetime` are required arguments, and cannot be null.");

        if (reason == null)
            throw new IllegalArgumentException("`reason` cannot be null. Use an empty string instead.");

        this.state = state;
        this.datetime = datetime;
        this.situation = situation;
        this.reason = reason;
        this.location = location;
        this.image = image;

    }

    /**
     * Base constructor for an OLD mood. This constructor should not be used to create a new mood, as you would have to define a firestoreId.
     * @param firestoreId The Firestore ID associated with the current mood. We have to keep this information within the mood itself so that it can be identified for deletion, for example.
     * @param state       The emotional state of the mood. This can be any one of the categories defined in the EmotionalState enum.
     * @param datetime    The Calendar object representing the date and time that the mood was created.
     * @param situation   An optional social situation of the mood. This can be any one of the categories defined in the SocialSituation enum.
     * @param reason      An optional textual reason describing why the user felt the mood. This cannot be null.
     * @param location    An optional location representing the longitude and latitude of the mood.
     * @param image       An optional image that the user can attach to the mood.
     */
    public Mood(String firestoreId, EmotionalState state, Calendar datetime, SocialSituation situation, String reason, GeoPoint location, Bitmap image) {

        if (state == null || datetime == null)
            throw new IllegalArgumentException("state and datetime are required arguments, and cannot be null.");

        if (reason == null)
            throw new IllegalArgumentException("Reason cannot be null. Use an empty string instead.");

        this.firestoreId = firestoreId;

        this.state = state;
        this.datetime = datetime;
        this.situation = situation;
        this.reason = reason;
        this.location = location;
        this.image = image;
    }

    /**
     * Gets the Firestore ID associated with the mood. This will not exist if a new mood is created, so this call can return a null.
     * @return Returns the Firestore ID as a <code>String</code> or a <code>null</code> if the information was not included.
     */
    @Nullable
    public String getFirestoreId() {
        return firestoreId;
    }

    /**
     * Gets the emotional state associated with the mood. This should not be null.
     * @return Returns one of the elements defined in {@see edu.ualberta.cmput301f19t17.bigmood.model.EmotionalState}
     */
    @NonNull
    public EmotionalState getState() {
        return state;
    }

    /**
     * Gets the Calendar object associated with the mood. This should not be null.
     * @return Returns the date and time encoded into a calendar object.
     */
    @NonNull
    public Calendar getDatetime() {

        // We clone this as to enforce that the time is mutable.
        return (Calendar) datetime.clone();

    }

    /**
     * Gets the social situation associated with the mood. This is optional, so this call can return a null.
     * @return Returns one of the elements defined in {@see edu.ualberta.cmput301f19t17.bigmood.model.EmotionalState} or a <code>null</code> if the information was not included.
     */
    @Nullable
    public SocialSituation getSituation() {
        return situation;
    }

    /**
     * Gets the textual reason associated with the mood. Although this is optional, it cannot be null.
     * @return Returns a string containing the reason (
     */
    @NonNull
    public String getReason() {
        return reason;
    }

    /**
     * Gets the location associated with the mood. This is optional, so this call can return a null.
     * @return Returns the location as a <code>Geopoint</code> or a <code>null</code> if the information was not included.
     */
    @Nullable
    public GeoPoint getLocation() {
        return location;
    }

    /**
     * Gets the image associated with the mood. This is optional, so this call can return a null.
     * @return Returns the image as a <code>Bitmap</code> or a <code>null</code> if the information was not included.
     */
    @Nullable
    public Bitmap getImage() {
        return image;
    }

    // PARCELABLE METHODS //

    /**
     * This method gets called when Android wants to reconstruct our object. We read in the same order we wrote in.
     *
     * @param in The Parcel with all the information in it
     */
    private Mood(Parcel in) {

        this.firestoreId = in.readString();

        this.state = EmotionalState.valueOf(in.readString());

        this.datetime = Calendar.getInstance(TimeZone.getTimeZone(in.readString()));
        this.datetime.setTimeInMillis(in.readLong());

        this.situation = SocialSituation.valueOf(in.readString());
        this.reason = in.readString();
        this.location = new GeoPoint(in.readLong(), in.readLong());

//        this.image = ? // (Bitmap.class.getClassLoader());

    }

    /**
     * This method is required for Parcelable. It is used to pass a Mood to another Fragment or Activity.
     * @param out   The Parcel in which the object should be written.
     * @param flags Additional flags about how the object should be written
     */
    @Override
    public void writeToParcel(Parcel out, int flags) {

        // Write the firestore Id, if applicable
        out.writeString(this.firestoreId);

        // Write the state as a string (from the enumeration)
        out.writeString(this.state.name());

        // Write timestamp and time zone ID
        out.writeLong(this.datetime.getTimeInMillis());
        out.writeString(this.datetime.getTimeZone().getID());

        // Write the situation as a string (from the enumeration)
        out.writeString(this.situation.name());

        // Write the reason as a string
        out.writeString(this.reason);

        // Write the longitude and latitude
        out.writeDouble(location.getLatitude());
        out.writeDouble(location.getLongitude());

        // TODO write image to parcelable, need to do more research for how to do this
//        out.writeParcelable(image);

    }

    /**
     * All parcelable objects must implement the CREATOR interface which has two methods. The former of which calls the special constructor below.
     */
    public static final Creator<Mood> CREATOR = new Creator<Mood>() {
        @Override
        public Mood createFromParcel(Parcel in) {
            return new Mood(in);
        }

        @Override
        public Mood[] newArray(int size) {
            return new Mood[size];
        }
    };

    /**
     * Describe the kinds of special objects contained in this Parcelable instance's marshaled representation.
     * @return a bitmask indicating the set of special object types marshaled by this Parcelable object instance. Value is either 0 or CONTENTS_FILE_DESCRIPTOR
     */
    @Override
    public int describeContents() {
        return 0;
    }


}


