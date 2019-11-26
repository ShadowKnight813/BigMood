package edu.ualberta.cmput301f19t17.bigmood.database;

/**
 * This class holds the complete set of string mappings for the Firestore database. This includes collection, document, and field references.
 * The idea behind this class is that if we ever had to modify the Firestore DB security rules (in a schema sense) it would be trivial to replace the reference in the program.
 * This is package private as to not reveal implementation to outer classes.
 */
class FirestoreMapping {

    // Collections //
    static final String COLLECTION_USERS = "users";

    static final String COLLECTION_REQUESTS = "requests";

    static final String COLLECTION_MOODS = "moods";

    static final String COLLECTION_PRIVATE = "private";

    // Documents //
    static final String DOCUMENT_CREDENTIAL = "credential";
    static final String DOCUMENT_FOLLOWER = "follower";
    
    // Fields //
    static final String FIELD_USER_FIRSTNAME = "first_name";
    static final String FIELD_USER_LASTNAME = "last_name";

    static final String FIELD_CREDENTIAL_PASSWORD = "password";

    static final String FIELD_FOLLOWER_FOLLOWERLIST = "follower_list";


    static final String FIELD_MOOD_OWNER = "owner";
    static final String FIELD_MOOD_STATE = "state";
    static final String FIELD_MOOD_DATETIME = "datetime";
    static final String FIELD_MOOD_SITUATION = "situation";
    static final String FIELD_MOOD_REASON = "reason";
    static final String FIELD_MOOD_IMAGE = "image";
    static final String FIELD_MOOD_LOCATION = "location";

    static final String FIELD_REQUEST_FROM = "from";
    static final String FIELD_REQUEST_TO = "to";

    /**
     * We keep this private to stop anyone from instantiating this class
     */
    private FirestoreMapping() {}
    
}
