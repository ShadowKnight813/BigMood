package edu.ualberta.cmput301f19t17.bigmood.fragment.dialog;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.GeoPoint;
import com.master.permissionhelper.PermissionHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import edu.ualberta.cmput301f19t17.bigmood.R;
import edu.ualberta.cmput301f19t17.bigmood.activity.HomeActivity;
import edu.ualberta.cmput301f19t17.bigmood.adapter.MoodSpinnerAdapter;
import edu.ualberta.cmput301f19t17.bigmood.model.EmotionalState;
import edu.ualberta.cmput301f19t17.bigmood.model.LocationHelper;
import edu.ualberta.cmput301f19t17.bigmood.model.Mood;
import edu.ualberta.cmput301f19t17.bigmood.model.SocialSituation;

import static android.app.Activity.RESULT_OK;

/**
 * DefineMoodDialogFragment is used to create a new mood, or edit a currently existing mood
 */
public class DefineMoodDialogFragment extends DialogFragment implements OnMapReadyCallback {

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_PICK_IMAGE = 2;
    private static final int MY_LOCATION_REQUEST_CODE = 1;


    private Toolbar toolbar;

    private OnButtonPressListener listener;
    private Mood moodToEdit = null;

    private Spinner stateSpinner;
    private Spinner situationSpinner;
    private Spinner dateSpinner;
    private Spinner timeSpinner;
    private TextInputLayout reasonInputLayout;

    private ImageView imageView;
    private View mapContainer;

    private MoodSpinnerAdapter moodSpinnerAdapter;
    ArrayList<EmotionalState> moodSpinnerArrayList;
    ArrayList<String> situationSpinnerArrayList;

    private LocationHelper locationHelper;
    private PermissionHelper permissionHelper;
    private GoogleMap googleMap;
    private MapView mMapView;
    private View addLocation;
    private LatLng savedLatLng;

    /**
     * This is an interface contained by this class to define the method for the save action. A class can either implement this or define it as a new anonymous class
     */
    public interface OnButtonPressListener {
        void onSavePressed(Mood moodToSave);
    }

    /**
     * This is the default constructor for the dialog. newInstance() methods. Technically a user of this class should not use this constructor. If it happens, the Dialog will not error, but will spawn as in a state of adding a mood
     */
    public DefineMoodDialogFragment() {

        this.listener = new OnButtonPressListener() {
            @Override
            public void onSavePressed(Mood moodToSave) {

                throw new UnsupportedOperationException("DefineMoodDialogFragment.OnButtonPressListener is NOT IMPLEMENTED. use setOnButtonPressListener() to set one.");

            }
        };

    }

    /**
     * This method creates a new instance of a DefineMoodDialog for the purposes of adding a Mood. Because we have no Mood to prepopulate we don't have to specify one.
     *
     * @return A new instance of a DefineMoodDialogFragment
     */
    public static DefineMoodDialogFragment newInstance() {

        // Create new stock fragment. We don't have to set any arguments
        return new DefineMoodDialogFragment();

    }

    /**
     * This method creates a new instance of a DefineMoodDialog for the purposes of editing a Mood.
     * Because we have a Mood to prepopulate we must specify it in here so it can be added to the fragment's arguments.
     *
     * @param mood The mood to edit
     * @return A new instance of a DefineMoodDialogFragment
     */
    public static DefineMoodDialogFragment newInstance(Mood mood) {

        // Define new Bundle for storing arguments
        Bundle args = new Bundle();

        // Put arguments in Bundle
        args.putParcelable(Mood.TAG_MOOD_OBJECT, mood);

        // Create new stock fragment and set arguments
        DefineMoodDialogFragment fragment = new DefineMoodDialogFragment();
        fragment.setArguments(args);

        return fragment;

    }

    /**
     * This method sets the OnButtonPressListener for the save action.
     *
     * @param listener This is the listener that will be set for this fragment.
     */
    public void setOnButtonPressListener(OnButtonPressListener listener) {
        this.listener = listener;
    }

    /**
     * of the on*()methods, this is the first. When we first want to create the dialog we set the theme to the fullscreen theme so that the edges match the parent. Here we also check for the existence of a mood in the arguments bundle and set it to our instance variable.
     *
     * @param savedInstanceState a bundle that holds the state of the fragment
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        this.setStyle(DialogFragment.STYLE_NORMAL, R.style.AppTheme_FullScreenDialog);

        // Get the arguments bundle. This will be NULL if the fragment was constructed without a mood to "edit".
        Bundle args = this.getArguments();

        // Check if the arguments are null.
        if (args != null) {

            // Get mood. If we have arguments we probably have a mood object but we check just in case.
            Mood mood = args.getParcelable(Mood.TAG_MOOD_OBJECT);

            // If a Mood object is not received, this object was not created using the newInstance() methods. We throw an exception if this is the case.
            if (mood != null)
                this.moodToEdit = mood;
            else
                throw new IllegalStateException("Something went wrong with creating the view. Received an argument bundle but not a proper Mood. Did you use the newInstance() methods?");

        }

    }

    /**
     * of the on*()methods, this is the second. After the dialog has been started we want to inflate the dialog.
     * This is where we inflate all the views and *if applicable* populate all the fields.
     *
     * @param inflater           View inflater service
     * @param container          Container that the inflater is housed in
     * @param savedInstanceState A bundle that holds the state of the fragment
     * @return Returns the inflated view
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        // Set inflater view
        View view = inflater.inflate(R.layout.dialog_define_mood, container, false);

        // Bind toolbar XML to view
        this.toolbar = view.findViewById(R.id.toolbar_define_fragment);

        // Find and bind elements
        initStateSpinnerList();
        this.stateSpinner = view.findViewById(R.id.spinner_state);
        this.moodSpinnerAdapter = new MoodSpinnerAdapter(this.getContext(), 0, moodSpinnerArrayList);
        this.stateSpinner.setAdapter(this.moodSpinnerAdapter);

        this.situationSpinner = view.findViewById(R.id.situation_spinner);
        this.reasonInputLayout = view.findViewById(R.id.text_input_reason);

        this.imageView = view.findViewById(R.id.image);
        this.mapContainer = view.findViewById(R.id.map_container);
        this.addLocation = view.findViewById(R.id.add_location_label);

        this.dateSpinner = view.findViewById(R.id.spinner_date);
        this.timeSpinner = view.findViewById(R.id.spinner_time);

        initLocationHelper();


        // Return view that has been created
        return view;

    }

    private void initLocationHelper() {
        locationHelper = new LocationHelper(getContext());
        locationHelper.setLocationUpdatesListener(mLocationUpdatesListener);
    }

    private LocationHelper.LocationRequestUpdatesListener mLocationUpdatesListener = new LocationHelper.LocationRequestUpdatesListener() {
        @Override
        public void onLocationChanged(Location location) {
            if (isAdded() && googleMap != null) {
                LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                addMarketAtLocation(currentLatLng);
            }
        }
    };

    private void addMarketAtLocation(LatLng latLng) {
        if (googleMap != null) {
            googleMap.clear();
            googleMap.addMarker(new MarkerOptions().position(latLng));
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            this.savedLatLng = latLng;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (permissionHelper != null) {
            permissionHelper.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    /**
     * of the on*()methods, this is the third. This is executed when the view is created. Here we set onClickListeners, etc. This is where we will actually error check all the views and
     *
     * @param view               The view that was created and inflated
     * @param savedInstanceState A bundle that holds the state of the fragment
     */
    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        // Inflate Menu resource onto the toolbar
        this.toolbar.inflateMenu(R.menu.define_mood);

        // Set the Listener for the close button in the toolbar
        this.toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(HomeActivity.LOG_TAG, "Close button clicked");
                DefineMoodDialogFragment.this.dismiss();
            }
        });


        // set up the spinner with the social situations
        initSituationSpinnerList();
        final ArrayAdapter<String> situationAdapter = new ArrayAdapter<>(
                this.getContext(),
                android.R.layout.simple_spinner_item,
                situationSpinnerArrayList
        );

        situationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        this.situationSpinner.setAdapter(situationAdapter);


        final Calendar calendar;

        // Here we populate values in the fragment if we have a mood and set the appropriate title.
        if (this.moodToEdit != null) {

            // Set title
            this.toolbar.setTitle(getString(R.string.title_dialog_edit_mood));

            // Set the calendar object since we have it in the mood object.
            calendar = this.moodToEdit.getDatetime();

            // Populate state
            int statePosition = moodSpinnerAdapter.getPosition(this.moodToEdit.getState());
            this.stateSpinner.setSelection(statePosition);


            // Populate
            if (this.moodToEdit.getSituation() != null) {
                int situationPosition = situationAdapter.getPosition(this.moodToEdit.getSituation().toString());
                this.situationSpinner.setSelection(situationPosition);
            }

            if (!this.moodToEdit.getReason().equals(""))
                this.reasonInputLayout.getEditText().setText(this.moodToEdit.getReason());

        } else {

            // Set Title
            this.toolbar.setTitle(getString(R.string.title_dialog_add_mood));

            // Set calendar to a new calendar since it's a new mood
            calendar = Calendar.getInstance();

        }

        // Set text of date spinner
        dateSpinner.setAdapter(
                new ArrayAdapter<String>(
                        this.getContext(),
                        android.R.layout.simple_spinner_item,
                        Collections.singletonList(new SimpleDateFormat("yyyy-MM-dd", Locale.CANADA).format(calendar.getTime()))
                ));
        dateSpinner.setEnabled(false);

        // Set text of time spinner
        timeSpinner.setAdapter(
                new ArrayAdapter<String>(
                        this.getContext(),
                        android.R.layout.simple_spinner_item,
                        Collections.singletonList(new SimpleDateFormat("HH:mm", Locale.CANADA).format(calendar.getTime()))
                ));
        timeSpinner.setEnabled(false);


        // add click listener to the image to pick picture from gallery or camera
        this.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String title = "Open Photo";
                CharSequence[] itemlist = {"Take a Photo",
                        "Pick from Gallery"};

                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle(title);
                builder.setItems(itemlist, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:// Take Photo
                                // Do Take Photo task here
                                askForCameraPermission();
                                break;
                            case 1:// Choose Existing Photo
                                // Do Pick Photo task here
                                askForGalleryPermission();
                                break;
                            default:
                                break;
                        }
                    }
                });
                AlertDialog alert = builder.create();
                alert.setCancelable(true);
                alert.show();
            }
        });

        this.addLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                askForLocationPermission();
            }
        });

        // Set the OnMenuItemClickListener for the one menu option we have, which is SAVE. Just for extendability we check if the ID matches.
        // This is where the core of the input validation will happen -- that is when the user tries to press Save.
        this.toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == R.id.action_save) {

                    // Get emotional state from state spinner
                    EmotionalState emotionalState = moodSpinnerAdapter.getItem(stateSpinner.getSelectedItemPosition());

                    // Get social situation from situation spinner
                    //SocialSituation socialSituation = situationAdapter.getItem(situationSpinner.getSelectedItemPosition());
                    String socialSituationString = situationAdapter.getItem(situationSpinner.getSelectedItemPosition());
                    // Set the social situation to a default value of null until the user chooses one.
                    SocialSituation socialSituation = null;
                    switch (socialSituationString.toLowerCase()) {
                        case "alone":
                            socialSituation = SocialSituation.ALONE;
                            break;
                        case "one person":
                            socialSituation = SocialSituation.ONE;
                            break;
                        case "two to several people":
                            socialSituation = SocialSituation.SEVERAL;
                            break;
                        case "crowd":
                            socialSituation = SocialSituation.CROWD;
                            break;
                    }

                    // Get reason
                    String reason = DefineMoodDialogFragment.this.reasonInputLayout
                            .getEditText()
                            .getText()
                            .toString()
                            .trim();

                    // Get the max length and max word count tha the reason is allowed to be
                    int maxLength = DefineMoodDialogFragment.this.getContext().getResources().getInteger(R.integer.max_length_reason);
                    int maxWordCount = DefineMoodDialogFragment.this.getContext().getResources().getInteger(R.integer.max_word_count_reason);

                    // This controls the logic for the max reason length. We display error messages depending on what is violated. For checking word count, we split the string by any space character (\s) one or more times (+). This makes sure that any number sf spaces is counted as one delimiter.
                    if (reason.length() > maxLength) {

                        // Set error and return, since this is not valid.
                        DefineMoodDialogFragment.this.reasonInputLayout.setError(DefineMoodDialogFragment.this.getString(R.string.error_reason_too_long));
                        return false;

                    } else if (reason.split("\\s+").length > maxWordCount) {

                        // Set error and return, since this is not valid.
                        DefineMoodDialogFragment.this.reasonInputLayout.setError(DefineMoodDialogFragment.this.getString(R.string.error_reason_word_count));
                        return false;

                    } else {

                        // Technically has no effect because the dialog is immediately dismissed but for completeness we do this.
                        DefineMoodDialogFragment.this.reasonInputLayout.setError(null);

                    }

                    // TODO add image, location - canned for now

                    // Declare mood. Can be initialized as an "old" mood (with firestoreId) or a "new" mood (without firestoreId).
                    Mood mood;

                    // If we have an old mood, pass the firestoreId along.
                    if (DefineMoodDialogFragment.this.moodToEdit != null)
                        mood = new Mood(
                                DefineMoodDialogFragment.this.moodToEdit.getFirestoreId(),
                                emotionalState,
                                calendar,
                                socialSituation,
                                reason,
                                new GeoPoint(savedLatLng != null ? savedLatLng.latitude : 0,
                                        savedLatLng != null ? savedLatLng.longitude : 0),
                                null
                        );

                        // If we don't have an old mood, we have to create a brand new one, without the firestoreId.
                    else
                        mood = new Mood(
                                emotionalState,
                                calendar,
                                socialSituation,
                                reason,
                                new GeoPoint(savedLatLng != null ? savedLatLng.latitude : 0,
                                        savedLatLng != null ? savedLatLng.longitude : 0),
                                null
                        );

                    // Invoke the callback method with the mood and dismiss the fragment
                    DefineMoodDialogFragment.this.listener.onSavePressed(mood);
                    DefineMoodDialogFragment.this.dismiss();
                    return true;

                }  // End if statement on R.id.action_save

                // Base case
                return false;

            }
        });

        mMapView = (MapView) view.findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);

        mMapView.onResume(); // needed to get the map to display immediately

        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        mMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                DefineMoodDialogFragment.this.googleMap = googleMap;
                DefineMoodDialogFragment.this.googleMap.getUiSettings().setAllGesturesEnabled(false);

                if (moodToEdit != null && moodToEdit.getLocation() != null && moodToEdit.getLocation().getLatitude() != 0) {
                    addLocation.setVisibility(View.GONE);
                    LatLng currentLatLng = new LatLng(moodToEdit.getLocation().getLatitude(), moodToEdit.getLocation().getLongitude());
                    addMarketAtLocation(currentLatLng);
                }
            }
        });

//        SupportMapFragment mapFragment = ((SupportMapFragment) getActivity().getSupportFragmentManager().findFragmentById(R.id.map));
//        mapFragment.getMapAsync(this);
    }

    private void askForLocationPermission() {
        permissionHelper = new PermissionHelper(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 100);
        permissionHelper.request(new PermissionHelper.PermissionCallback() {
            @Override
            public void onPermissionGranted() {
                locationHelper.init();
                addLocation.setVisibility(View.GONE);
            }

            @Override
            public void onIndividualPermissionGranted(String[] grantedPermission) {

            }

            @Override
            public void onPermissionDenied() {
                Toast.makeText(getContext(), "Permission denied.", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onPermissionDeniedBySystem() {

            }
        });
    }

    private void askForGalleryPermission() {
        permissionHelper = new PermissionHelper(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);
        permissionHelper.request(new PermissionHelper.PermissionCallback() {
            @Override
            public void onPermissionGranted() {
                dispatchPickImageIntent();
            }

            @Override
            public void onIndividualPermissionGranted(String[] grantedPermission) {

            }

            @Override
            public void onPermissionDenied() {
                Toast.makeText(getContext(), "Permission denied.", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onPermissionDeniedBySystem() {

            }
        });
    }

    private void askForCameraPermission() {
        permissionHelper = new PermissionHelper(this, new String[]{Manifest.permission.CAMERA}, 100);
        permissionHelper.request(new PermissionHelper.PermissionCallback() {
            @Override
            public void onPermissionGranted() {
                dispatchTakePictureIntent();
            }

            @Override
            public void onIndividualPermissionGranted(String[] grantedPermission) {

            }

            @Override
            public void onPermissionDenied() {
                Toast.makeText(getContext(), "Permission denied.", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onPermissionDeniedBySystem() {

            }
        });
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (locationHelper != null) {
            locationHelper.stopLocationUpdates();
        }
        if (mMapView != null) {
            mMapView.onDestroy();
        }
    }

    public void onDestroyView() {
        super.onDestroyView();


    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        this.googleMap.getUiSettings().setAllGesturesEnabled(false);
    }

    /**
     * of the on*()methods, this is the fourth. We set the width and height of the view and also set its animation.
     */
    @Override
    public void onStart() {
        super.onStart();

        Dialog dialog = this.getDialog();

        if (dialog != null) {

            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.MATCH_PARENT;

            dialog.getWindow().setLayout(width, height);
            dialog.getWindow().setWindowAnimations(R.style.AppTheme_Slide);

        }

    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getContext().getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }


    private void dispatchPickImageIntent() {
        Intent i = new Intent(
                Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        if (i.resolveActivity(getContext().getPackageManager()) != null) {
            startActivityForResult(i, REQUEST_PICK_IMAGE);
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            imageView.setImageBitmap(imageBitmap);
        } else if (requestCode == REQUEST_PICK_IMAGE && resultCode == RESULT_OK) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};

            Cursor cursor = getContext().getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();

            imageView.setImageBitmap(BitmapFactory.decodeFile(picturePath));

        }
    }

    // Creates an ArrayList for stateSpinner
    private void initStateSpinnerList() {
        moodSpinnerArrayList = new ArrayList<>();
        moodSpinnerArrayList.add(EmotionalState.HAPPINESS);
        moodSpinnerArrayList.add(EmotionalState.SADNESS);
        moodSpinnerArrayList.add(EmotionalState.ANGER);
        moodSpinnerArrayList.add(EmotionalState.DISGUST);
        moodSpinnerArrayList.add(EmotionalState.FEAR);
        moodSpinnerArrayList.add(EmotionalState.SURPRISE);
    }

    // Creates an ArrayList for situationSpinner
    private void initSituationSpinnerList() {
        situationSpinnerArrayList = new ArrayList<>();
        situationSpinnerArrayList.add(0, "No situation selected");
        situationSpinnerArrayList.add(SocialSituation.ALONE.toString());
        situationSpinnerArrayList.add(SocialSituation.ONE.toString());
        situationSpinnerArrayList.add(SocialSituation.SEVERAL.toString());
        situationSpinnerArrayList.add(SocialSituation.CROWD.toString());
    }

}
