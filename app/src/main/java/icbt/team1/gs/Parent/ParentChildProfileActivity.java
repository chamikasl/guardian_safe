package icbt.team1.gs.Parent;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;

import icbt.team1.gs.R;

public class ParentChildProfileActivity extends AppCompatActivity {
    private ImageButton toBack, viewChild, viewHistory, busInfo;
    private WebView webView;
    private String childId, busId;
    private TextView timeTextView;

    private ImageButton phone;

    private TextView phonenum;

    // Firestore variables
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.parent_activity_child_profile);

        childId = getIntent().getStringExtra("uid");
        busId = getIntent().getStringExtra("cbusid");
        phonenum = findViewById(R.id.textView4phone);


        toBack = findViewById(R.id.to_back);
        toBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ParentChildProfileActivity.this, ParentHomeActivity.class);
                startActivity(intent);
            }
        });

        viewChild = findViewById(R.id.viewchild);
        viewChild.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ParentChildProfileActivity.this, ParentChildViewActivity.class);
                intent.putExtra("uid", childId);
                intent.putExtra("cbusid", busId);
                startActivity(intent);
            }
        });


        // Initialize the WebView
        webView = findViewById(R.id.webView);

        // Enable JavaScript (required for Google Maps)
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        // Load the Google Map link from Firebase Realtime Database
        loadGoogleMapLink();

        // Initialize the TextView
        timeTextView = findViewById(R.id.textView3);


        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Fetch and display the "time" value from Firestore
        fetchTimeValue();
        fetchTimeValue2();
        fetchTimeValue3();
        fetchTimeValue4();
        fetchTimeValue5();


        phone = findViewById(R.id.callButton);
        phone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Call the method to initiate a phone call
                callDriver();
            }
        });
    }

    // ...
    private void initiatePhoneCall(String phoneNumber) {
        // Create an intent to initiate a phone call
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + phoneNumber));

        // Check if the device can handle the intent (e.g., a phone app is available)
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        } else {
            // Handle the case where the device cannot make a phone call (e.g., no phone app installed)
            Toast.makeText(this, "Phone call cannot be initiated. No phone app found.", Toast.LENGTH_SHORT).show();
        }
    }

    private void callDriver() {
        String phoneNumber = phonenum.getText().toString();

        // Check if the phone number is not empty
        if (!phoneNumber.isEmpty()) {
            // Create an intent to initiate a phone call
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:" + phoneNumber));

            // Start the dialer activity
            startActivity(intent);
        }
    }

    private void fetchTimeValue5() {
        String currentDate = getCurrentDate();
        db.collection("drivers")
                .document(busId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot collection = task.getResult();
                            if (collection.exists()) {
                                // Retrieve the "time" field from the document
                                String time = collection.getString("phone");

                                if (time != null) {
                                    // Display the "time" value in the TextView
                                    phonenum.setText(time);

                                    // Update the TextView in your XML layout
                                    TextView hpTextView = findViewById(R.id.textView4phone);
                                    hpTextView.setText(time);
                                }
                            }
                        }
                    }
                });
    }

    private void fetchTimeValue4() {
        // Construct the Firestore document reference based on UID
        String currentDate = getCurrentDate();
        db.collection("scanned")
                .document(childId)
                .collection(currentDate)
                .document("home pick")
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot collection = task.getResult();
                            if (collection.exists()) {
                                // Retrieve the "time" field from the document
                                String time = collection.getString("time");

                                if (time != null) {
                                    // Display the "time" value in the TextView
                                    timeTextView.setText(time);

                                    // Update the TextView in your XML layout
                                    TextView hpTextView = findViewById(R.id.hp);
                                    hpTextView.setText(time);

                                    // Send a notification
                                    sendNotification("Your Child ", "Home Picked at " + time);

                                }
                            }
                        }
                    }
                });
    }

    private void sendNotification(String title, String message) {
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);

        // Check if the notification has already been sent
        if (!sharedPreferences.getBoolean("notification_sent", false)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel("default", "Default", NotificationManager.IMPORTANCE_DEFAULT);
                NotificationManager notificationManager = getSystemService(NotificationManager.class);
                notificationManager.createNotificationChannel(channel);
            }

            Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "default")
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle(title)
                    .setContentText(message)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setSound(soundUri); // Set the sound for the notification

            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            notificationManager.notify(0, builder.build());

            // Mark the notification as sent
            sharedPreferences.edit().putBoolean("notification_sent", true).apply();
        }
    }


    private void fetchTimeValue3() {
        String currentDate = getCurrentDate();
        db.collection("scanned")
                .document(childId)
                .collection(currentDate)
                .document("school drop")
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot collection = task.getResult();
                            if (collection.exists()) {
                                // Retrieve the "time" field from the document
                                String time = collection.getString("time");

                                if (time != null) {
                                    // Display the "time" value in the TextView
                                    timeTextView.setText(time);

                                    // Update the TextView in your XML layout
                                    TextView hpTextView = findViewById(R.id.sd);
                                    hpTextView.setText(time);

                                    sendNotification("QR Code Scanned", "QR code was scanned at " + time);
                                }
                            }
                        }
                    }
                });
    }

    private void fetchTimeValue2() {
        String currentDate = getCurrentDate();
        db.collection("scanned")
                .document(childId)
                .collection(currentDate)
                .document("school pick")
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot collection = task.getResult();
                            if (collection.exists()) {
                                // Retrieve the "time" field from the document
                                String time = collection.getString("time");

                                if (time != null) {
                                    // Display the "time" value in the TextView
                                    timeTextView.setText(time);

                                    // Update the TextView in your XML layout
                                    TextView hpTextView = findViewById(R.id.sp);
                                    hpTextView.setText(time);

                                    sendNotification("QR Code Scanned", "QR code was scanned at " + time);
                                }
                            }
                        }
                    }
                });
    }
    private void fetchTimeValue() {
        String currentDate = getCurrentDate();
        db.collection("scanned")
                .document(childId)
                .collection(currentDate)
                .document("home drop")
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot collection = task.getResult();
                            if (collection.exists()) {
                                // Retrieve the "time" field from the document
                                String time = collection.getString("time");

                                if (time != null) {
                                    // Display the "time" value in the TextView
                                    timeTextView.setText(time);

                                    // Update the TextView in your XML layout
                                    TextView hpTextView = findViewById(R.id.hd);
                                    hpTextView.setText(time);

                                    sendNotification("QR Code Scanned", "QR code was scanned at " + time);
                                }
                            }
                        }
                    }
                });


    }
    private String getCurrentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(new Date());
    }
    private void loadGoogleMapLink() {
        DatabaseReference driverRef = FirebaseDatabase.getInstance().getReference("drivers").child(busId).child("locationLink");
        driverRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String link = dataSnapshot.getValue(String.class);

                if (link != null) {
                    // Load the map link in the WebView
                    webView.loadUrl(link);

                    // Optional: Configure WebView to open links within itself
                    webView.setWebViewClient(new WebViewClient());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle onCancelled event if needed
            }
        });
    }
}

