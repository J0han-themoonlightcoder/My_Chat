package org.mesibo.messenger.fcm;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.core.app.JobIntentService;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.FirebaseMessaging;

import java.io.IOException;

public class MesiboRegistrationIntentService extends JobIntentService {

    private static final String TAG = "RegIntentService";
    private static final String[] TOPICS = {"global"};
    private static String SENDER_ID = "";
    private static GCMListener mListener = null;

    public MesiboRegistrationIntentService() {
        super();
    }

    public interface GCMListener {
        void Mesibo_onGCMToken(String token);
        void Mesibo_onGCMMessage(boolean inService);
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        onHandleIntent(intent);
    }

    protected void onHandleIntent(Intent intent) {
        try {
            FirebaseMessaging.getInstance().getToken()
                    .addOnCompleteListener(task -> {
                        if (!task.isSuccessful()) {
                            Log.d(TAG, "Fetching FCM registration token failed", task.getException());
                            return;
                        }

                        // Copy mListener to a local final variable
                        final GCMListener listener = mListener;

                        // Directly use the task result inside the lambda
                        final String token = task.getResult();  // Declare 'token' as final
                        Log.d(TAG, "FCM Registration Token: " + token);

                        // Send token to listener (using the local copy of mListener)
                        if (listener != null) {
                            listener.Mesibo_onGCMToken(token);
                        }
                    });

        } catch (Exception e) {
            Log.d(TAG, "Failed to complete token refresh", e);
        }
    }

    /**
     * Persist registration to third-party servers.
     *
     * Modify this method to associate the user's FCM registration token with any server-side account
     * maintained by your application.
     *
     * @param token The new token.
     */
    private void sendRegistrationToServer(String token) {
        // Add custom implementation, as needed.
        Log.d("Token", token);
    }

    /**
     * Subscribe to any FCM topics of interest, as defined by the TOPICS constant.
     *
     * @throws IOException if unable to reach the FCM PubSub service
     */
    private void subscribeTopics(String token) throws IOException {
        // Uncomment and use FirebaseMessaging to subscribe to topics
        // FirebaseMessaging.getInstance().subscribeToTopic(TOPICS[0]);
    }

    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        return (resultCode == ConnectionResult.SUCCESS);
    }

    public static final int JOB_ID = 1;
    public static void enqueueWork(Context context, Intent work) {
        enqueueWork(context, MesiboRegistrationIntentService.class, JOB_ID, work);
    }

    public static void startRegistration(Context context, String senderId, GCMListener listener) {
        if (!TextUtils.isEmpty(senderId)) {
            SENDER_ID = senderId;
        }

        if (listener != null) {
            mListener = listener;
        }

        try {
            Intent intent = new Intent(context, MesiboRegistrationIntentService.class);
            enqueueWork(context, intent);
        } catch (Exception e) {
            Log.d(TAG, "Failed to start registration", e);
        }
    }

    public static void sendMessageToListener(boolean inService) {
        if (null != mListener) {
            mListener.Mesibo_onGCMMessage(inService);
        }
    }
}
