package edu.ualberta.cmput301f19t17.bigmood.database;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.ualberta.cmput301f19t17.bigmood.activity.AppPreferences;
import edu.ualberta.cmput301f19t17.bigmood.database.listener.FollowingListener;
import edu.ualberta.cmput301f19t17.bigmood.database.listener.MoodsListener;
import edu.ualberta.cmput301f19t17.bigmood.database.listener.RequestsListener;
import edu.ualberta.cmput301f19t17.bigmood.model.Mood;
import edu.ualberta.cmput301f19t17.bigmood.model.Request;

/**
 * This class handles all the database requests coming from the application. It implements methods for interacting with the collections and documents that this application should need. Most methods return a Task<T>, where T is the applicable return type. Since these are asynchronous you must make sure to add an OnSuccessListener() and/or an OnFailureListener() to the tasks you get back so that you can give feedback to the user. For the tasks that return a ListenerRegistration, this defines a live callback routine so you must make sure to detach it whenever the activity is out of view as to not waste network resources.
 */
public class FirestoreRepository implements Repository {

    // Singleton implementation
    private static FirestoreRepository repository = null;

    // Firestore database object
    private FirebaseFirestore db;

    /**
     * This method gets the single instance of the FirestoreRepository class. If it does not exist it creates one. No public constructor is available.
     * @return Returns the instance of the repository class
     */
    public static FirestoreRepository getInstance() {

        if (FirestoreRepository.repository == null)
            FirestoreRepository.repository = new FirestoreRepository();

        return FirestoreRepository.repository;

    }

    /**
     * This constructor handles the responsibility of instantiating the only instance of FirestoreRepository (singleton class). It is private because we want only one instance to exist at once.
     */
    private FirestoreRepository() {
        this.db = FirebaseFirestore.getInstance();
    }


    // USER RELATED METHODS //

    /**
     * This method checks if a user exists in the database by a username lookup.
     * @param username Username string of the user to look up
     * @param successListener A SuccessListener of type <code>Void</code>. This will be called when the task succeeds (can connect to the DB and security rules allow the request)
     * @param failureListener A FailureListener for the Task. This will be called when the task fails (likely when the security rules prevent a certain request).
     */
    @Override
    public void userExists(final String username, OnSuccessListener<Boolean> successListener, OnFailureListener failureListener) {

        // Try to retrieve a document in the users collection whose name is exactly the username. We attach the success and failure listener
        this.db
                .collection(FirestoreMapping.COLLECTION_USERS)
                .document(username)
                .get()
                .continueWith(new Continuation<DocumentSnapshot, Boolean>() {
                    @Override
                    public Boolean then(@NonNull Task<DocumentSnapshot> task) throws Exception {

                        // Will propagate an exception if .getResult() produces one. If not, then the Document is assigned to doc.
                        DocumentSnapshot doc = task.getResult();

                        // We have a potential document, we just have to check if that particular entry exists or not.
                        return doc.exists();

                    }


                })
                .addOnSuccessListener(successListener)
                .addOnFailureListener(failureListener);

    }

    /**
     * This method attempts to register a user using the parameters it was passed.
     * @param username  Username of the user to register. This must be unique or else the query will fail.
     * @param password  Password of the user to register.
     * @param firstName First name of the user to register.
     * @param lastName  Last name of the user to register.
     * @param successListener A SuccessListener of type <code>Void</code>. This will be called when the task succeeds (can connect to the DB and security rules allow the request)
     * @param failureListener A FailureListener for the Task. This will be called when the task fails (likely when the security rules prevent a certain request).
     */
    @Override
    public void registerUser(String username, String password, String firstName, String lastName, OnSuccessListener<Void> successListener, OnFailureListener failureListener) {

        // Check if the username, password, firstName, and lastName are at least one character.
        if (username.length() <= 0 || password.length() <= 0 || firstName.length() <= 0 || lastName.length() <= 0)
            throw new IllegalArgumentException("Any of username, password, firstName, and lastName have to be at least one character.");

        // Create batch object
        WriteBatch batch = this.db.batch();


        // Prepare user data
        User newUser = new User(username, firstName, lastName);

        // Prepare password data
        Map<String, Object> passwordData = new HashMap<>();
        passwordData.put(FirestoreMapping.FIELD_CREDENTIAL_PASSWORD, password);

        // Prepare follower_list
        Map<String, Object> followerData = new HashMap<>();
        followerData.put(FirestoreMapping.FIELD_FOLLOWER_FOLLOWERLIST, FieldValue.arrayUnion());


        // Create user document
        DocumentReference userDocument = this.db
                .collection(FirestoreMapping.COLLECTION_USERS)
                .document(username);

        // Create password document
        DocumentReference passwordDocument = this.db
                .collection(FirestoreMapping.COLLECTION_USERS)
                .document(newUser.getUsername())
                .collection(FirestoreMapping.COLLECTION_PRIVATE)
                .document(FirestoreMapping.DOCUMENT_CREDENTIAL);

        // Create follower document
        DocumentReference followerDocument = this.db
                .collection(FirestoreMapping.COLLECTION_USERS)
                .document(newUser.getUsername())
                .collection(FirestoreMapping.COLLECTION_PRIVATE)
                .document(FirestoreMapping.DOCUMENT_FOLLOWER);


        // ALL three of these transactions have to be in a batch write or else the write will fail. EVERY user must have these three documents to be valid. The application order does not matter.
        batch.set(userDocument, FirestoreConversion.UserToFirestore(newUser));
        batch.set(passwordDocument, passwordData);
        batch.set(followerDocument, followerData);

        // Since there is no object or document to return it creates a task of type Void. We attach a success and failure listener.
        batch.commit()
                .addOnSuccessListener(successListener)
                .addOnFailureListener(failureListener);

    }

    /**
     * This method handles our """authentication""". It basically validates a username and password against the database and if it is successful it returns a constructed User object that is used as the token for """authentication""".
     * @param username Username of the user to validate
     * @param password Password of the use to validate
     * @param successListener A SuccessListener of type <code>Void</code>. This will be called when the task succeeds (can connect to the DB and security rules allow the request)
     * @param failureListener A FailureListener for the Task. This will be called when the task fails (likely when the security rules prevent a certain request).
     */
    @Override
    public void validateUser(String username, final String password, OnSuccessListener<User> successListener, OnFailureListener failureListener) {

        // Queue up a task to get the user document
        Task<DocumentSnapshot> userTask = this.db
                .collection(FirestoreMapping.COLLECTION_USERS)
                .document(username)
                .get();

        // Queue up a task to get the credential document of the user.
        Task<DocumentSnapshot> credentialTask = this.db
                .collection(FirestoreMapping.COLLECTION_USERS)
                .document(username)
                .collection(FirestoreMapping.COLLECTION_PRIVATE)
                .document(FirestoreMapping.DOCUMENT_CREDENTIAL)
                .get();

        // Define a master task that will only be run when both tasks succeed.
        Task<List<DocumentSnapshot>> allTasks = Tasks.whenAllSuccess(userTask, credentialTask);

        // Define a continuation to actually verify the data. This returns a Task of type User.
        allTasks
                .continueWith(new Continuation<List<DocumentSnapshot>, User>() {
                    @Override
                    public User then(@NonNull Task<List<DocumentSnapshot>> task) throws Exception {

                        // If any of the two tasks failed, .getResult() will propagate an error.
                        List<DocumentSnapshot> taskList = task.getResult();

                        // Check all the documents in the list exist. I am not sure if this is entirely necessary but just to be sure I included it.
                        for (DocumentSnapshot doc : taskList)
                            if (doc == null)
                                throw new IllegalArgumentException("One or more documents are null in the task list. This should not happen.");

                        // Since the task list is evaluated in the order we listed them in the first element is the user document. If the user document does not exist then the username/password combo does not match anything. In that case we return null.
                        DocumentSnapshot userDoc = taskList.get(0);
                        if (!userDoc.exists())
                            return null;

                        // Just to be safe (and to avoid an error), if the password document does not exist we have to throw an error. We assume that the username exists but since they have no password this user is in an illegal state and should not exist in the DB.
                        DocumentSnapshot passwordDoc = taskList.get(1);
                        if (!passwordDoc.exists())
                            throw new IllegalStateException(String.format("User '%s' does not have a password document in the database. This user is in an invalid state and must be recreated. Please delete this user in the Firebase console.", taskList.get(0).getId()));

                        // Get the password stored in the database. Don't freak out. It's fine.
                        String dbPassword = passwordDoc.getString(FirestoreMapping.FIELD_CREDENTIAL_PASSWORD);

                        if (dbPassword == null)
                            throw new IllegalStateException("The password document retrieved does not have a password field in it. This document is invalid, please recreate it in Firestore.");

                        // We validate the user's password here, and if it succeeds we return a user object.
                        // Okay, stop yelling at me. I know this is an eternal sin to do this but here me out. Firstly, we have no custom authentication server to dish out JWTs, and secondly, for the purposes of our app (basically school demonstration) using Firebase's Authentication would be way too overkill and cumbersome to demo. This app is not supposed to be public, which is why I am committing this unpardonable sin. I'm so sorry. It hurts me as well.
                        if (dbPassword.equals(password))
                            return FirestoreConversion.UserFromFirestore(userDoc);
                        else
                            return null;

                    }
                })
                .addOnSuccessListener(successListener)
                .addOnFailureListener(failureListener);

    }


    // MOOD RELATED METHODS //

    /**
     * This method gets as a List the set of all moods belonging to a particular User.
     * @param user The User whose moods we will retrieve.
     * @return     Returns a ListenerRegistration. Upon the first call and any other change to the database the callback method will be invoked.
     */
    @Override
    public ListenerRegistration getUserMoods(User user, final MoodsListener listener) {

        return this.db
                .collection(FirestoreMapping.COLLECTION_USERS)
                .document(user.getUsername())
                .collection(FirestoreMapping.COLLECTION_MOODS)
                .orderBy(FirestoreMapping.FIELD_MOOD_DATETIME, Query.Direction.DESCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {

                        // If the passed listener is null this SnapshotListener will be called but it won't do anything.
                        if (listener == null)
                            return;

                        // Create new mood list
                        List<Mood> moodList = new ArrayList<>();

                        // Add every mood to the mood list
                        for (DocumentSnapshot doc : queryDocumentSnapshots)
                            moodList.add( FirestoreConversion.MoodFromFirestore(doc) );

                        // Call the callback method in the caller.
                        listener.onUpdate(moodList);

                    }
                });

    }

    /**
     * This method sets up a ListenerRegistration that polls for changes in the following document, which holds the following list for each user.
     * @param user     The User who the following list belongs to
     * @param listener An implemented callback interface that will be called whenever there is an update in the follower list.
     * @return         Returns a ListenerRegistration. Make sure to remove() it when you don't need it anymore.
     */
    @Override
    public ListenerRegistration getFollowingList(User user, final FollowingListener listener) {

        // We target the follower document in a query like fashion so we can listen for changes.
        return this.db
                .collection(FirestoreMapping.COLLECTION_USERS)
                .document(user.getUsername())
                .collection(FirestoreMapping.COLLECTION_PRIVATE)
                .whereEqualTo(FieldPath.documentId(), FirestoreMapping.DOCUMENT_FOLLOWER)
                .limit(1)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {

                        // If the passed listener is null this SnapshotListener will be called but it won't do anything.
                        if (listener == null)
                            return;

                        // Create new follower list (usernames as strings)
                        List<String> followerList = new ArrayList<>();

                        // Define a new runtime exception in case we need to raise this exception
                        RuntimeException documentDNEException = new IllegalStateException("The follower document does not exist in the database. This is against the security rules. Please recreate it in the Firebase console.");

                        // Define a document variable
                        DocumentSnapshot doc;

                        // We wrap this operation in a try/except block because we need to check if there exists a document in the DB. We also have to check if the document exists (it should, if it passes the first test. In either case we have to raise an exception because we cannot proceed.
                        try {

                            // Get the follower document. Since we limited this query to 1 there should always be a document in the first position.
                            doc = queryDocumentSnapshots.getDocuments().get(0);

                            if (! doc.exists())
                                throw documentDNEException;

                        } catch (IndexOutOfBoundsException e1) {

                            throw documentDNEException;

                        }

                        // Define an ArrayList<Object> that will hold the array data from Firestore.
                        ArrayList firestoreList = (ArrayList) doc.get(FirestoreMapping.FIELD_FOLLOWER_FOLLOWERLIST);

                        // If the firestore array is null this means that document is illegal and we cannot proceed.
                        if (firestoreList == null)
                            throw new IllegalStateException("The follower list does not exist in the follower document. This is against the security rules. Please recreate it in the Firebase console.");

                        // Cast every object in the array to a string (representing a username) and add it to our follower list.
                        for (Object username : firestoreList)
                            followerList.add((String) username);

                        // We're done assembling the follower list, so we call the listener.
                        listener.onUpdate(followerList);

                    }
                });

    }

    /**
     * This method gets the most recent mood (read: limit 1) from ALL the Users the passed in User is following.
     * @param user The current User. We will use their information to get the applicable moods for the users they are following.
     * @return     Returns a ListenerRegistration. Upon the first call and any other change to the database the callback method will be invoked.
     */
    @Override
    public ListenerRegistration getFollowingMoods(User user, final MoodsListener listener) {

        return this.db
                .collectionGroup(FirestoreMapping.COLLECTION_MOODS)
                .orderBy(FirestoreMapping.FIELD_MOOD_DATETIME, Query.Direction.DESCENDING)
                .orderBy(FirestoreMapping.FIELD_MOOD_OWNER)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {

                        if (queryDocumentSnapshots == null)
                            return;

                        // Get preferences
                        AppPreferences preferences = AppPreferences.getInstance();

                        // Define a new mood list and get the current cached follower list from the preferences.
                        List<Mood> followingMoodList = new ArrayList<>();
                        List<String> usernameList = preferences.getFollowingList();

                        // The idea is to do one sweep of the document list, checking if any mood document is owned by someone on our follower list. If we find one, we know it's the most recent one (since the query is ordered by datetime). Once we remove that follower from the temporary follower list we will never add another one of their moods.
                        for (DocumentSnapshot doc : queryDocumentSnapshots) {

                            // "Base" case, it makes no sense to continue down the list of documents if there are no more users to match.
                            if (usernameList.size() == 0)
                                break;

                            // For safety, check if the document exists. If for some reason it does not, go to the next document.
                            if (! doc.exists())
                                continue;

                            // Store username of the document in a variable
                            String username = doc.get(FirestoreMapping.FIELD_MOOD_OWNER, String.class);

                            // If the owner of the current document we're looking at matches ANY of the owners in our list, add that specific mood and remove the username from the list. This gives the effect of finding only the most recent mood document from the document list given by the query.
                            if (usernameList.contains(username)) {

                                followingMoodList.add(FirestoreConversion.MoodFromFirestore(doc));
                                usernameList.remove(username);

                            }

                        }

                        // At this point we have exhausted the entire username list and added the first occurrence of a mood matching that username. We now call the callback function to submit the final list.
                        listener.onUpdate(followingMoodList);

                    }
                });  // End addSnapshotListener

    }

    /**
     * This method attempts to create a Mood in the database given the parameters.
     * @param user The User for which the new Mood should fall under.
     * @param mood The Mood we are trying to post to the database. The mood passed in should be a NEW mood and NOT have a firestoreId.
     * @param successListener A SuccessListener of type <code>Void</code>. This will be called when the task succeeds (can connect to the DB and security rules allow the request)
     * @param failureListener A FailureListener for the Task. This will be called when the task fails (likely when the security rules prevent a certain request).
     */
    @Override
    public void createMood(User user, Mood mood, OnSuccessListener<Void> successListener, OnFailureListener failureListener) {

        // Since the Firestore ID is final in the class, we can assume that if it has one, it came from the database. We don't want to add another duplicate mood to the database if it already exists. In other words we play it safe
        if (mood.getFirestoreId() != null)
            throw new IllegalArgumentException("This mood cannot be from the database -- it must be created as new.");

        // Go to the moods collection and add a mood, leaving the task of generating an ID to Firestore.
        this.db
                .collection(FirestoreMapping.COLLECTION_USERS)
                .document(user.getUsername())
                .collection(FirestoreMapping.COLLECTION_MOODS)
                .add(FirestoreConversion.MoodToFirestore(mood, user))
                .continueWith(new Continuation<DocumentReference, Void>() {
                    @Override
                    public Void then(@NonNull Task<DocumentReference> task) throws Exception {

                        // Propagate error and return null.
                        task.getResult();

                        return null;
                    }
                })
                .addOnSuccessListener(successListener)
                .addOnFailureListener(failureListener);

    }

    /**
     * This method attempts to delete a Mood in the database given the parameters.
     * @param user The User where we would find the given Mood.
     * @param mood The Mood we are trying to delete from the database. The mood passed in should be an OLD mood and HAVE a firestoreId.
     * @param successListener A SuccessListener of type <code>Void</code>. This will be called when the task succeeds (can connect to the DB and security rules allow the request)
     * @param failureListener A FailureListener for the Task. This will be called when the task fails (likely when the security rules prevent a certain request).
     */
    @Override
    public void deleteMood(User user, Mood mood, OnSuccessListener<Void> successListener, OnFailureListener failureListener) {

        // Any mood that is passed into this task has to have a FirestoreId. If it doesn't, then this mood was not created as valid.
        if (mood.getFirestoreId() == null)
            throw new IllegalArgumentException("This mood cannot be new -- it must be created from the database");

        // Delete the document that has a Firestore ID that matches the mood we have. We attach a success and failure listener.
        this.db
                .collection(FirestoreMapping.COLLECTION_USERS)
                .document(user.getUsername())
                .collection(FirestoreMapping.COLLECTION_MOODS)
                .document(mood.getFirestoreId())
                .delete()
                .addOnSuccessListener(successListener)
                .addOnFailureListener(failureListener);

    }

    /**
     * this method attempts to modify a Mood in the database given the parameters.
     * @param user The User where we would find the given Mood.
     * @param mood The Mood we are trying to delete from the database. The mood passed in should be an OLD mood and HAVE a firestoreId.
     * @param successListener A SuccessListener of type <code>Void</code>. This will be called when the task succeeds (can connect to the DB and security rules allow the request)
     * @param failureListener A FailureListener for the Task. This will be called when the task fails (likely when the security rules prevent a certain request).
     */
    @Override
    public void updateMood(User user, Mood mood, OnSuccessListener<Void> successListener, OnFailureListener failureListener) {

        // Any mood that is passed into this task has to have a FirestoreId. If it doesn't, then this mood was not created as valid.
        if (mood.getFirestoreId() == null)
            throw new IllegalArgumentException("This mood cannot be new -- it must be created from the database");

        // We want to target the specific mood passed in as a parameter. That means we need to get the firestore ID and update the document with all the new values.
        this.db
                .collection(FirestoreMapping.COLLECTION_USERS)
                .document(user.getUsername())
                .collection(FirestoreMapping.COLLECTION_MOODS)
                .document(mood.getFirestoreId())
                .update(FirestoreConversion.MoodToFirestore(mood, user))
                .addOnSuccessListener(successListener)
                .addOnFailureListener(failureListener);

    }


    // REQUEST RELATED METHODS //

    /**
     * This method gets as a List the set of all Requests belonging to a particular User.
     * @param user The User whose Requests we will retrieve.
     * @return     Returns a ListenerRegistration. Upon the first call and any other change to the database the callback method will be invoked.
     */
    @Override
    public ListenerRegistration getUserRequests(User user, final RequestsListener listener) {

        return this.db
                .collection(FirestoreMapping.COLLECTION_REQUESTS)
                .whereEqualTo(FirestoreMapping.FIELD_REQUEST_TO, user.getUsername())
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {

                        // We have to do this in case the user does not provide a listener.
                        if (listener == null)
                            return;

                        // Create new List object
                        List<Request> requestList = new ArrayList<>();

                        // Add every request to the request list
                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots)
                            requestList.add(FirestoreConversion.RequestFromFirestore(doc));

                        // Call the callback method in the caller.
                        listener.onUpdate(requestList);

                    }
                });

    }

    /**
     * This method attempts to create a Request in the database given the parameters.
     * @param request The Request we are trying to post to the database. The Request passed in should be a NEW request and the destination user exists.
     * @param successListener A SuccessListener of type <code>Void</code>. This will be called when the task succeeds (can connect to the DB and security rules allow the request)
     * @param failureListener A FailureListener for the Task. This will be called when the task fails (likely when the security rules prevent a certain request).
     */
    @Override
    public void createRequest(Request request, OnSuccessListener<Void> successListener, OnFailureListener failureListener) {

        if (request.getFirestoreId() != null)
            throw new IllegalArgumentException("This request cannot be from the database -- it must be created as new.");

        // TODO: 2019-10-31 https://github.com/CMPUT301F19T17/BigMood/issues/4

        // We target the requests collection and add a request. We leave the creation of an id to Firestore. We attach a success and failure listener.
        this.db
                .collection(FirestoreMapping.COLLECTION_REQUESTS)
                .add(FirestoreConversion.RequestToFirestore(request))
                .continueWith(new Continuation<DocumentReference, Void>() {
                    @Override
                    public Void then(@NonNull Task<DocumentReference> task) throws Exception {

                        // This will propagate an error if there was an issue with the task.
                        task.getResult();

                        return null;

                    }
                })
                .addOnSuccessListener(successListener)
                .addOnFailureListener(failureListener);

    }

    /**
     * This method handles the operation of "accepting" a request. This means that the recipient (the "to" field) has accepted the sender's (the "from" field) request to follow them. Therefore we need to add the recipient to the sender's follower list.
     * @param request The Request to accept. The Request passed in should be an OLD Request and MUST have a firestoreId.
     * @param successListener A SuccessListener of type <code>Void</code>. This will be called when the task succeeds (can connect to the DB and security rules allow the request)
     * @param failureListener A FailureListener for the Task. This will be called when the task fails (likely when the security rules prevent a certain request).
     */
    @Override
    public void acceptRequest(Request request, OnSuccessListener<Void> successListener, OnFailureListener failureListener) {

        // If this document comes from the database (and we will know that for sure because we reference it by ID) then we know that both the "from" and "to" user exists. Therefore we don't have to do any user checking on the client side as we can be sure that both users exists to be able to complete the transaction.
        if (request.getFirestoreId() == null)
            throw new IllegalArgumentException("This request cannot be new -- it must be created from the database");

        // Create new batch object
        WriteBatch batch = this.db.batch();

        // Reference the document with the follower list in it
        DocumentReference followerDocument = this.db
                .collection(FirestoreMapping.COLLECTION_USERS)
                .document(request.getFrom())
                .collection(FirestoreMapping.COLLECTION_PRIVATE)
                .document(FirestoreMapping.DOCUMENT_FOLLOWER);

        // Reference the document with the particular request associated with this method calll
        DocumentReference requestDocument = this.db
                .collection(FirestoreMapping.COLLECTION_REQUESTS)
                .document(request.getFirestoreId());

        // Create new Map and put the arrayUnion request in it
        Map<String, Object> arrayUpdateData = new HashMap<>();
        arrayUpdateData.put(FirestoreMapping.FIELD_FOLLOWER_FOLLOWERLIST, FieldValue.arrayUnion(request.getTo()));

        // Apply both changes to the batch request
        batch.update(followerDocument, arrayUpdateData);
        batch.delete(requestDocument);

        // Attempt to commit the changes to the database. We attach a success and failure listener.
        batch.commit()
                .addOnSuccessListener(successListener)
                .addOnFailureListener(failureListener);

    }

    /**
     * This method handles the operation of "declining" a request. This means that the recipient (the "to" field) has declined the sender's (the "from" field) request to follow them. Therefore we just need to delete the request and not change anything else.
     * @param request The Request to decline. The Request passed in should be an OLD Request and MUST have a firestoreId.
     * @param successListener A SuccessListener of type <code>Void</code>. This will be called when the task succeeds (can connect to the DB and security rules allow the request)
     * @param failureListener A FailureListener for the Task. This will be called when the task fails (likely when the security rules prevent a certain request).
     */
    @Override
    public void declineRequest(Request request, OnSuccessListener<Void> successListener, OnFailureListener failureListener) {

        // If this document comes from the database (and we will know that for sure because we reference it by ID) then we know that both the "from" and "to" user exists. Therefore we don't have to do any user checking on the client side as we can be sure that both users exists to be able to complete the transaction.
        if (request.getFirestoreId() == null)
            throw new IllegalArgumentException("This request cannot be new -- it must be created from the database");

        // We target the request collection and delete the request with the same Firestore ID.
        this.db
                .collection(FirestoreMapping.COLLECTION_REQUESTS)
                .document(request.getFirestoreId())
                .delete()
                .addOnSuccessListener(successListener)
                .addOnFailureListener(failureListener);

    }

}
