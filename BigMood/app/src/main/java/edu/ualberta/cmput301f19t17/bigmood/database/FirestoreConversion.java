package edu.ualberta.cmput301f19t17.bigmood.database;

import android.graphics.Bitmap;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.GeoPoint;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import edu.ualberta.cmput301f19t17.bigmood.model.EmotionalState;
import edu.ualberta.cmput301f19t17.bigmood.model.Mood;
import edu.ualberta.cmput301f19t17.bigmood.model.Request;
import edu.ualberta.cmput301f19t17.bigmood.model.SocialSituation;

/**
 * This class is solely responsible for converting Firestore data to model objects and vice versa.
 */
class FirestoreConversion {

    /**
     * This constructor is private as to disallow the object's instantiation.
     */
    private FirestoreConversion() {
    }

    // FROM FIRESTORE METHODS //

    /**
     * Converts valid Firestore data to a User object.
     * @param document A Firestore DocumentSnapshot containing the ID and User information.
     * @return         Returns a User object converted from the data
     */
    static User UserFromFirestore(DocumentSnapshot document) throws IllegalArgumentException {

        // Since we accept a DocumentSnapshot we have no guarantee that it exists, unlike a QueryDocumentSnapshot (direct subclass of DocumentSnapshot). So we have to make sure to check this.
        if (! document.exists())
            throw new IllegalArgumentException(String.format("The document with the document ID {%s} does not exist in the database. Please pass in a document that exists.", document.getId()));

        // Get first name and last name
        String firstName = document.getString( FirestoreMapping.FIELD_USER_FIRSTNAME );
        String lastName = document.getString( FirestoreMapping.FIELD_USER_LASTNAME );

        return new User(document.getId(), firstName, lastName);
    }

    /**
     * Converts valid Firestore data to a Request object.
     * @param document A Firestore DocumentSnapshot containing the ID and Request information.
     * @return         Returns a Request object converted from the data
     */
    static Request RequestFromFirestore(DocumentSnapshot document) throws IllegalArgumentException {

        // Since we accept a DocumentSnapshot we have no guarantee that it exists, unlike a QueryDocumentSnapshot (direct subclass of DocumentSnapshot). So we have to make sure to check this.
        if (! document.exists())
            throw new IllegalArgumentException(String.format("The document with the document ID {%s} does not exist in the database. Please pass in a document that exists.", document.getId()));

        // Get from and to fields from the document data
        String from = document.getString(FirestoreMapping.FIELD_REQUEST_FROM);
        String to = document.getString(FirestoreMapping.FIELD_REQUEST_TO);

        // Create new request object and associate the firestoreId with it
        return new Request(document.getId(), from, to);

    }

    /**
     * Converts valid Firestore data to a Mood object.
     * @param document A Firestore DocumentSnapshot containing the ID and Mood information.
     * @return         Returns a Mood object converted from the data
     */
    static Mood MoodFromFirestore(DocumentSnapshot document) throws IllegalArgumentException {

        // Since we accept a DocumentSnapshot we have no guarantee that it exists, unlike a QueryDocumentSnapshot (direct subclass of DocumentSnapshot). So we have to make sure to check this.
        if (! document.exists())
            throw new IllegalArgumentException(String.format("The document with the document ID {%s} does not exist in the database. Please pass in a document that exists.", document.getId()));

        // Get emotional state by doing a reverse lookup on the code
        // Since the integers stored in FS are 64 bit (long) we have to narrow the long to an integer. This can result in overflow, but since this is our enumeration and the security rules are set so that the number is in between a very short range this cannot happen.
        Integer stateCode = document.get(FirestoreMapping.FIELD_MOOD_STATE, Integer.class);

        // This should not happen, but we can cover ourselves if it does
        if (stateCode == null)
            throw new IllegalArgumentException(String.format("Mood with ID {%s} has a state field for which it's null. This document is not allowed with the security rules, please delete and recreate it on the Firebase Console.", document.getId()));

        // Get Emotional State code from the document. Should not be null.
        EmotionalState state = EmotionalState.findByStateCode(stateCode);

        // Get Date from data. This is a Firestore type so we can convert it directly.
        Date dateField = document.getDate(FirestoreMapping.FIELD_MOOD_DATETIME);

        // This should not happen, but we can cover ourselves if it does
        if (dateField == null)
            throw new IllegalArgumentException(String.format("Mood with ID {%s} has a date field for which it's null. This document is not allowed with the security rules, please delete and recreate it on the Firebase Console.", document.getId()));

        // Instantiate new Calendar object and set the date to what the timestamp's Date() is. This should include all the information.
        Calendar datetime = GregorianCalendar.getInstance();
        datetime.setTime( dateField );

        // Get social situation by doing a reverse lookup on the code. This can be null, so we have to account for that.
        Integer situationCode = document.get(FirestoreMapping.FIELD_MOOD_SITUATION, Integer.class);
        SocialSituation situation;

        // If the situation code is null, we would produce an Exception if we call findBySituationCode(null). Situations are allowed to be null so we can set it to that instead.
        if (situationCode == null)
            situation = null;
        else
            situation = SocialSituation.findBySituationCode(situationCode);

        // Get the reason. It is a string and should not be null.
        String reason = document.getString(FirestoreMapping.FIELD_MOOD_REASON);

        // This should not happen, but we can cover ourselves if it does
        if (reason == null)
            throw new IllegalArgumentException(String.format("Mood with ID {%s} has a reason field for which it's null. This document is not allowed with the security rules, please delete and recreate it on the Firebase Console.", document.getId()));

        // Get the GeoPoint (long, lat). This is a Firestore type so we can convert it directly. This field can be null, but we don't have to check since we are not calling anything on the location.
        GeoPoint location = document.getGeoPoint(FirestoreMapping.FIELD_MOOD_LOCATION);

        // TODO: 2019-11-01 Nectarios: IMAGE STORAGE
        // Get the image. This field can be null, but we don't have to check since we are not calling anything on the location.
        Bitmap image = (Bitmap) document.get(FirestoreMapping.FIELD_MOOD_IMAGE);

        // Since we are getting a new mood from the database, we have to associate an ID with it in order to edit or delete it at a different time. So we have to use the constructor that has the firestoreId
        // TODO: 2019-11-01 Nectarios: Replace with factory?
        return new Mood(document.getId(), state, datetime, situation, reason, location, image);

    }

    // TO FIRESTORE METHODS //

    /**
     * Converts a valid User object into a Map to send to Firestore.
     * @param user User object to convert into a Firestore compatible Map.
     * @return     Returns a Map object converted from the User.
     */
    static Map<String, Object> UserToFirestore(User user) {

        // Create map to store fields
        Map<String, Object> data = new HashMap<>();

        // Put first name and last name
        data.put( FirestoreMapping.FIELD_USER_FIRSTNAME, user.getFirstName() );
        data.put( FirestoreMapping.FIELD_USER_LASTNAME, user.getLastName() );

        return data;

    }

    /**
     * Converts a valid Request object into a Map to send to Firestore.
     * @param request Request object to convert into a Firestore compatible Map.
     * @return        Returns a Map object converted from the Request.
     */
    static Map<String, Object> RequestToFirestore(Request request) {

        // Create map to store fields
        Map<String, Object> data = new HashMap<>();

        // Put from and to fields
        data.put( FirestoreMapping.FIELD_REQUEST_FROM, request.getFrom() );
        data.put( FirestoreMapping.FIELD_REQUEST_TO, request.getTo() );

        return data;

    }

    /**
     * Converts a valid Mood object into a Map to send to Firestore.
     * @param mood Mood object to convert into a Firestore compatible Map.
     * @param user The currently logged in user. This function needs this field to set the owner field of the mood document.
     * @return     Returns a Map object converted from the Mood.
     */
    static Map<String, Object> MoodToFirestore(Mood mood, User user) {

        // Create new Hash Map to store all the values
        Map<String, Object> data = new HashMap<>();

        // We need to put this owner field here since it is required to be set in the database for the getFollowingMoods() function in the FirestoreRepository. However, it is not required by our model because we always know the user of a mood that we are saving/editing. In other words, we set the owner here but we never retrieve it.
        data.put( FirestoreMapping.FIELD_MOOD_OWNER, user.getUsername() );

        // These fields always exist, so we don't have to check if they are null.
        data.put( FirestoreMapping.FIELD_MOOD_STATE, mood.getState().getStateCode() );
        data.put( FirestoreMapping.FIELD_MOOD_DATETIME, new Timestamp( mood.getDatetime().getTime() ) );

        // Declare new situation code
        Integer situationCode;

        // Here we check if situation is null. Since it's an enum, we have to get the integer its associated with to store it in the database. The security rules are set up so that nulls are allowed in all optional fields EXCEPT reason (which should be an empty string if it is not provided.
        if (mood.getSituation() != null)
            situationCode = mood.getSituation().getSituationCode();
        else
            situationCode = null;

        data.put( FirestoreMapping.FIELD_MOOD_SITUATION, situationCode );

        // As mentioned, we can also assume that reason is not null so we don't have to check this.
        data.put( FirestoreMapping.FIELD_MOOD_REASON, mood.getReason() );

        // Location is either a GeoPoint or null, and both are allowed in the database. We don't check for null here because we don't have to access an instance method of that type.
        data.put( FirestoreMapping.FIELD_MOOD_LOCATION, mood.getLocation() );

        // TODO: 2019-10-27 IMAGE STORAGE
        data.put( FirestoreMapping.FIELD_MOOD_IMAGE, null );

        return data;

    }

}
