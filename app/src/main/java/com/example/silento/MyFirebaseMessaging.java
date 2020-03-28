package com.example.silento;

import android.content.Context;
import android.os.Vibrator;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.squareup.okhttp.MediaType;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MyFirebaseMessaging extends FirebaseMessagingService {
    FirebaseFirestore db;
    FirebaseAuth mAuth=FirebaseAuth.getInstance();
    SoundMessage soundMessage;
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // ...
        if(soundMessage==null){
           Log.d("action: ","Nancy its null");
        }
        Log.d("action: ","Nancy We are message recieved");
        Map<String, String> data=remoteMessage.getData();
        String message = data.get("body");
        Log.d("action: ","Nancy We are coun "+message);
        setListeningMessage(message);
    }

    public void setListeningMessage(String soundT){
        MainActivity.HearSound(soundT);
    }

    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the InstanceID token
     * is initially generated so this is where you would retrieve the token.
     */
    @Override
    public void onNewToken(String token) {

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.

//        The app deletes Instance ID
//        The app is restored on a new device
//        The user uninstalls/reinstall the app
//        The user clears app data.
        FirebaseUser user=mAuth.getCurrentUser();
        if(user!=null) {
            final String uid = user.getUid();
            db = FirebaseFirestore.getInstance();
            if (uid != null) {
                DocumentReference contact = db.collection("users").document(uid);
                contact.update("FCM", token).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                    }
                });
            }
        }

    }
}
