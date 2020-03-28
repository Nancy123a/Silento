package com.example.silento;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.github.nisrulz.sensey.Sensey;
import com.github.nisrulz.sensey.ShakeDetector;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessaging;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements ShakeDetector.ShakeListener{
    private TextView mText;
    private SpeechRecognizer sr;
    private static final String TAG = "Nancy";
    Intent intent;
    int counter=0;
    boolean isNotRegister=false;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    static TextToSpeech textToSpeech;
    FirebaseUser user;
    boolean isUserSelected=false;
    List<String> FCM=new ArrayList<>();
    String UserXD="";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-IN");
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "en-IN");
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getPackageName());
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 1);
        }
        sr = SpeechRecognizer.createSpeechRecognizer(this);
        sr.setRecognitionListener(new RecognitionListener() {
            public void onReadyForSpeech(Bundle params)
            {
                Log.d(TAG, "onReadyForSpeech");
            }
            public void onBeginningOfSpeech()
            {
                Log.d(TAG, "onBeginningOfSpeech");
            }
            public void onRmsChanged(float rmsdB)
            {
            }
            public void onBufferReceived(byte[] buffer)
            {
                Log.d(TAG, "onBufferReceived");
            }
            public void onEndOfSpeech()
            {
                counter=0;
            }
            public void onError(int error){ }
            public void onResults(Bundle results)
            {
                String str = new String();
                Log.d(TAG, "onResults " + results);
                ArrayList data = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                user = mAuth.getCurrentUser();
                if (user==null) {
                    int cnt=0;
                    for (int i = 0; i < data.size(); i++) {
                        str = data.get(i).toString();

                        if(str.matches("\\d+")){
                            cnt++;
                            final int num = Integer.parseInt(str);
                            mAuth.signInAnonymously().addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        FirebaseUser user = mAuth.getCurrentUser();
                                        final String uid = user.getUid();
                                        // Get new Instance ID token
                                        System.out.println("We are success");
                                        FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                                            @Override
                                            public void onComplete(@NonNull Task<InstanceIdResult> task) {
                                                if (!task.isSuccessful()) {
                                                    Log.w(TAG, "getInstanceId failed", task.getException());
                                                    return;
                                                }

                                                // Get new Instance ID token
                                                String token = task.getResult().getToken();
                                                Map<String, Object> map = new HashMap<>();
                                                map.put("UID", uid);
                                                map.put("FCM", token);
                                                map.put("Counter", num);
                                                db.collection("users")
                                                        .add(map)
                                                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                            @Override
                                                            public void onSuccess(DocumentReference documentReference) {
                                                                String textSuccess = "You have registered successfully. Shake one time to record the voice message.";
                                                                textToSpeech.speak(textSuccess, TextToSpeech.QUEUE_FLUSH, null, null);
                                                                Log.d("We are ", "DocumentSnapshot added with ID: " + documentReference.getId());

                                                            }
                                                        })
                                                        .addOnFailureListener(new OnFailureListener() {
                                                            @Override
                                                            public void onFailure(@NonNull Exception e) {
                                                                String textFailure = "Registration failed. Make sure you are connected to the internet";
                                                                textToSpeech.speak(textFailure, TextToSpeech.QUEUE_FLUSH, null, null);
                                                            }
                                                        });

                                            }
                                        });
                                    }
                                }
                            })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            String textFailure = "Registration failed. Make sure you are connected to the internet.";
                                            textToSpeech.speak(textFailure, TextToSpeech.QUEUE_FLUSH, null, null);
                                        }
                                    });
                        }
                        if(i==data.size()-1 && cnt==0){
                            String textFailure = "You have to register using a number. Try again!";
                            textToSpeech.speak(textFailure, TextToSpeech.QUEUE_FLUSH, null, null);
                        }
                    }
                }
                else{
                    if(!isUserSelected){
                        int cnt=0;
                        for (int i = 0; i < data.size(); i++) {
                            str = data.get(i).toString();
                            System.out.println("Nancy "+str);
                            if(str.matches("\\d+")){
                                cnt++;
                                System.out.println("Nancy it contains integer");
                                final int num = Integer.parseInt(str);
                                String uidUser=user.getUid();
                                final Query users = db.collection("users").whereEqualTo("UID", uidUser);
                                users.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                        if (task.isSuccessful()) {
                                            if(task.getResult().getDocuments().size()>0) {
                                                for (QueryDocumentSnapshot document : task.getResult()) {
                                                    Map<String, Object> objects = document.getData();
                                                    Long usernumber = (Long) objects.get("Counter");
                                                    UserXD="User "+usernumber;
                                                    final Query users = db.collection("users").whereEqualTo("Counter", num);
                                                    users.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                            if (task.isSuccessful()) {
                                                                FCM.clear();
                                                                if(task.getResult().getDocuments().size()>0) {
                                                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                                                        Map<String, Object> objects = document.getData();
                                                                        String fcm = (String) objects.get("FCM");
                                                                        FCM.add(fcm);
                                                                        String textSuccess = "User is found. Please shake your phone and send your message.";
                                                                        textToSpeech.speak(textSuccess, TextToSpeech.QUEUE_FLUSH, null, null);
                                                                        sr.stopListening();
                                                                        isUserSelected=true;
                                                                        counter=0;
                                                                    }
                                                                }
                                                                else {
                                                                    String textfailure = "User is not found. Please try again.";
                                                                    textToSpeech.speak(textfailure, TextToSpeech.QUEUE_FLUSH, null, null);
                                                                    isUserSelected=false;
                                                                    sr.stopListening();
                                                                    counter=0;
                                                                }

                                                            }
                                                        }
                                                    });
                                                }
                                            }

                                        }
                                    }
                                });
                            }
                            if(i==data.size()-1 && cnt==0){
                                String textfailure = "Please record the number of user. Shake and try again.";
                                textToSpeech.speak(textfailure, TextToSpeech.QUEUE_FLUSH, null, null);
                                isUserSelected=false;
                            }
                        }
                    }
                    else{
                        str = data.get(0).toString();
                        System.out.println("Nancy "+str);
                        sendNotification(str);
                    }
                }
            }

            public void onPartialResults(Bundle partialResults)
            {
                Log.d(TAG, "onPartialResults");
            }
            public void onEvent(int eventType, Bundle params)
            {
                Log.d(TAG, "onEvent " + eventType);
            }
        });
        Sensey.getInstance().init(this);
        Sensey.getInstance().startShakeDetection(10, 1000, this);
        db= FirebaseFirestore.getInstance();
        mAuth= FirebaseAuth.getInstance();
        textToSpeech = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    int ttsLang = textToSpeech.setLanguage(Locale.US);

                    if (ttsLang == TextToSpeech.LANG_MISSING_DATA
                            || ttsLang == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.e("TTS", "The Language is not supported!");
                    } else {
                        Log.i("TTS", "Language Supported.");
                    }
                    Log.i("TTS", "Initialization success.");
                } else {
                    Toast.makeText(getApplicationContext(), "TTS Initialization failed!", Toast.LENGTH_SHORT).show();
                }
            }
        });
        Intent backgroundService = new Intent(getApplicationContext(), MyReciever.class);
        startService(backgroundService);


        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                //Do something after 100ms
                ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
                if (networkInfo != null && networkInfo.isConnected()) {
                    System.out.println("Nancy Internet connected");
                } else {
                    System.out.println("Nancy Internet not connected");
                    String textfailure = "Please connect to the internet.";
                    textToSpeech.speak(textfailure, TextToSpeech.QUEUE_FLUSH, null, null);
                }
            }
        }, 500);
        BroadcastReceiver receiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                //some action
                ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

                // Check internet connection and according to state change the
                // text of activity by calling method
                if (networkInfo != null && networkInfo.isConnected()) {
                    System.out.println("Nancy Internet abc is connected");
                }
                else{
                    System.out.println("Nancy Internet abc not connected");
                    ConnectToInternet();
                }

            }
        };

        IntentFilter filter = new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE");
        registerReceiver(receiver, filter);

    }

    @Override
    public void onShakeDetected() {
        Log.d("action:","Nancy Shake detected");
    }

    @Override
    public void onShakeStopped() {
        counter++;
        Log.d("action: ","Nancy Shake stop");
        if(counter==1) {
            if(sr!=null) {
                sr.startListening(intent);
            }
        }

    }



    public static final  MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private void sendNotification(final String msg) {
        System.out.println("Nancy onSendNoti "+msg+" "+FCM+" "+UserXD);
        for(int i=0;i<FCM.size();i++){
            final int finalI = i;
            new AsyncTask<Void,Void,Void>(){
                @Override
                protected Void doInBackground(Void... params) {
                    try {
                        OkHttpClient client = new OkHttpClient();
                        JSONObject json=new JSONObject();
                        JSONObject dataJson=new JSONObject();
                        String Message=UserXD+" Said "+msg;
                        dataJson.put("body",Message);
                        json.put("data",dataJson);
                        json.put("to",FCM.get(finalI));
                        RequestBody body = RequestBody.create(JSON, json.toString());
                        Request request = new Request.Builder()
                                .header("Authorization","key=AAAA5tTn7J8:APA91bGZpDgUbJGmIBWIGtuwvJ2Lu69guj-13rWWV9c6krLgZALSw6ZJSkWYHOAIXDFeuc5LRDDgGUjXZFRvfxgiblVD425PU8_BkwRQmm0d4xPMahocoK51oIcnmo4-3jOaWN6yGv23")
                                .url("https://fcm.googleapis.com/fcm/send")
                                .post(body)
                                .build();
                        Response response = client.newCall(request).execute();
                        String finalResponse = response.body().string();
                        System.out.println("We are finalResponse "+finalResponse);
                        boolean isSuccessful=response.isSuccessful();
                        if(isSuccessful){
                            System.out.println("Nancy success");
                            String textSuccess = "Message is sent successfully.";
                            textToSpeech.speak(textSuccess, TextToSpeech.QUEUE_FLUSH, null, null);
                            isUserSelected=false;
                            counter=0;

                        }
                        else{
                            System.out.println("Nancy successnot");
                            String textSuccess = "Message is not sent. Make sure you are connected to the internet.";
                            textToSpeech.speak(textSuccess, TextToSpeech.QUEUE_FLUSH, null, null);
                            counter=0;
                        }

                    }catch (Exception e){
                        //Log.d(TAG,e+"");
                        System.out.println("Nancy error123"+e);
                        String textSuccess = "Message is not sent. Make sure you are connected to the internet.";
                        textToSpeech.speak(textSuccess, TextToSpeech.QUEUE_FLUSH, null, null);
                        counter=0;
                    }
                    return null;
                }

                @Override
                protected void onPostExecute(Void aVoid) {
                    super.onPostExecute(aVoid);
                    sr.stopListening();
                }
            }.execute();
        }

    }

    public  static void HearSound(String message){
        textToSpeech.speak(message, TextToSpeech.QUEUE_FLUSH,null,null);
    }

    public static void ConnectToInternet(){
        System.out.println("Nancy Internet reached here");
        textToSpeech.speak("Please connect to the internet.", TextToSpeech.QUEUE_FLUSH,null,null);
    }

}
