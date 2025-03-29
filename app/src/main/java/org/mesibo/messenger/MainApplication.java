package org.mesibo.messenger;

import android.app.Application;
import androidx.lifecycle.LifecycleObserver;
import android.content.Context;
import android.util.Log;
import com.mesibo.api.Mesibo;
import com.mesibo.calls.api.MesiboCall;
import com.mesibo.calls.api.MesiboGroupCallUiProperties;
import com.mesibo.mediapicker.ImagePicker;
import com.mesibo.mediapicker.MediaPicker;
import com.mesibo.calls.ui.MesiboCallUi;
import com.mesibo.messaging.MesiboUI;
import com.mesibo.messaging.MesiboUiDefaults;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainApplication extends Application implements Mesibo.RestartListener, LifecycleObserver {
    public static final String TAG = "MesiboDemoApplication";
    private static Context mContext = null;
    private static MesiboCallUi mCallUi = null;
    private static AppConfig mConfig = null;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
        Mesibo.setRestartListener(this);
        mConfig = new AppConfig(this);
        SampleAPI.init(getApplicationContext());

        // Firebase Authentication
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser != null) {
            // User is signed in, retrieve their unique Firebase UID
            String userId = currentUser.getUid();

            // Fetch the Mesibo token for the user
            getMesiboToken(userId);
        } else {
            // Handle case when the user is not signed in (e.g., show login screen)
            Log.d(TAG, "User not signed in");
        }

        mCallUi = MesiboCallUi.getInstance();
        MesiboCall.getInstance().init(mContext);

        MesiboUiDefaults opt = MesiboUI.getUiDefaults();
        opt.mToolbarColor = 0xff00868b;
        opt.emptyUserListMessage = "No messages! Click on the message icon above to start messaging!";
        opt.showAddressInProfileView = true;
        opt.showAddressAsPhoneInProfileView = true;
        MediaPicker.setToolbarColor(opt.mToolbarColor);
        ImagePicker.getInstance().setApp(this);

        // Customize call screen
        MesiboCall.UiProperties up = MesiboCall.getInstance().getDefaultUiProperties();
        // up.showScreenSharing = true;

        // Customize conference call screen
        MesiboGroupCallUiProperties gcp = MesiboCall.getInstance().getDefaultGroupCallUiProperties();
        // gcp.exitPrompt = "Exit?";
    }

    public static String getRestartIntent() {
        return "com.mesibo.sampleapp.restart";
    }

    public static Context getAppContext() {
        return mContext;
    }

    @Override
    public void Mesibo_onRestart() {
        Log.d(TAG, "OnRestart");
        StartUpActivity.newInstance(this, true);
    }

    /**
     * Fetch the Mesibo Token for the current user.
     * @param userId The unique ID of the user from Firebase
     */
    private void getMesiboToken(String userId) {
        // You should implement token generation logic here.
        // The example assumes you have a Firebase database with Mesibo tokens stored for users.

        DatabaseReference tokenRef = FirebaseDatabase.getInstance().getReference("mesibo_tokens").child(userId);
        tokenRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                String mesiboToken = task.getResult().getValue(String.class);

                if (mesiboToken != null) {
                    // Set the Mesibo token dynamically
                    Mesibo.setAccessToken(mesiboToken);
                    Mesibo.start();  // Start the Mesibo service
                    Log.d(TAG, "Mesibo token set and service started");
                } else {
                    Log.d(TAG, "No Mesibo token found for the user.");
                }
            } else {
                Log.d(TAG, "Failed to fetch Mesibo token: " + task.getException());
            }
        });
    }
}
