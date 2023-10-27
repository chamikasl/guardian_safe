package icbt.team1.gs.Parent;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import icbt.team1.gs.R;

public class ParentChildViewActivity extends AppCompatActivity {
    private ImageButton toBack;
    private ImageView imageView;
    private String childId,busId;
    private TextView timeTextView1;
    private TextView timeTextView2;
    private TextView timeTextView3;
    private FirebaseFirestore db;
    private TextView timeTextView4;
    private TextView timeTextView5;
    private TextView timeTextView6;
    private TextView timeTextView7;
    private TextView selectedDateTextView;
    private Button selectDateButton;
    private Calendar calendar;

    private static final int PERMISSION_REQUEST_WRITE_STORAGE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.parent_activity_child_view);

        childId = getIntent().getStringExtra("uid");
        busId = getIntent().getStringExtra("cbusid");
        timeTextView1 = findViewById(R.id.dn2);
        timeTextView2 = findViewById(R.id.bn2);
        timeTextView3 = findViewById(R.id.pn2);
        selectedDateTextView = findViewById(R.id.date);
        selectDateButton = findViewById(R.id.selectDateButton);
        timeTextView4 = findViewById(R.id.homepick2);
        timeTextView5 = findViewById(R.id.schooldrop2);
        timeTextView6 = findViewById(R.id.schoolpick2);
        timeTextView7 = findViewById(R.id.homedrop2);

        db = FirebaseFirestore.getInstance();
        calendar = Calendar.getInstance();

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        selectedDateTextView.setText(dateFormat.format(calendar.getTime()));

        selectDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDatePickerDialog();
            }
        });

        childId = getIntent().getStringExtra("uid");
        // Fetch and display the "time" value from Firestore
        fetchTimeValues();
        fetchTimeValue1();
        fetchTimeValue2();
        fetchTimeValue3();

        toBack = findViewById(R.id.to_back);
        toBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ParentChildViewActivity.this, ParentChildProfileActivity.class);
                startActivity(intent);
            }
        });

        imageView = findViewById(R.id.qr); // ImageView to display the QR code
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Create a custom AlertDialog with your layout
                AlertDialog.Builder builder = new AlertDialog.Builder(ParentChildViewActivity.this);
                View dialogView = getLayoutInflater().inflate(R.layout.custom_dialog_layout, null);
                builder.setView(dialogView);

                // Set a positive button for downloading the QR code
                builder.setPositiveButton("Download", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // User clicked "Download," initiate the download and save
                        downloadQRCodeAndSave();
                    }
                });

                // Set a negative button for canceling
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // User clicked "Cancel," close the dialog
                        dialog.dismiss();
                    }
                });

                // Create and show the AlertDialog
                AlertDialog dialog = builder.create();
                dialog.show();

                // Load the QR code image into the dialog
                ImageView qrImageView = dialogView.findViewById(R.id.qrImageView);
                loadQRCodeIntoImageView(qrImageView);
            }
        });
    }
    private void showDatePickerDialog() {
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int selectedYear, int selectedMonth, int selectedDay) {
                        calendar.set(selectedYear, selectedMonth, selectedDay);
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                        selectedDateTextView.setText(dateFormat.format(calendar.getTime()));
                        // Fetch and display the updated "time" values when the date is changed
                        fetchTimeValues();
                    }
                },
                year,
                month,
                day
        );

        datePickerDialog.show();
    }

    private void fetchTimeValues() {
        String selectedDate = getDate(selectedDateTextView);

        // Fetch the "time" values for all categories
        fetchTimeValue("home pick", timeTextView4, selectedDate);
        fetchTimeValue("school drop", timeTextView5, selectedDate);
        fetchTimeValue("school pick", timeTextView6, selectedDate);
        fetchTimeValue("home drop", timeTextView7, selectedDate);
    }

    private void fetchTimeValue(String category, TextView textView, String date) {
        db.collection("scanned")
                .document(childId)
                .collection(date)
                .document(category)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                String time = document.getString("time");
                                if (time != null) {
                                    textView.setText(time);
                                }
                            } else {
                                // Handle the case where the document does not exist
                                textView.setText("No data available");
                            }
                        } else {
                            // Handle errors
                            textView.setText("Error fetching data");
                        }
                    }
                });
    }

    private String getDate(TextView selectedDateTextView) {
        return selectedDateTextView.getText().toString();
    }
    private void fetchTimeValue3() {
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
                                    timeTextView3.setText(time);

                                    // Update the TextView in your XML layout
                                    TextView hpTextView = findViewById(R.id.pn2);
                                    hpTextView.setText(time);
                                }
                            }
                        }
                    }
                });
    }

    private void fetchTimeValue2() {
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
                                String time = collection.getString("busno");

                                if (time != null) {
                                    // Display the "time" value in the TextView
                                    timeTextView2.setText(time);

                                    // Update the TextView in your XML layout
                                    TextView hpTextView = findViewById(R.id.bn2);
                                    hpTextView.setText(time);
                                }
                            }
                        }
                    }
                });

    }

    private void fetchTimeValue1() {
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
                                String time = collection.getString("name");

                                if (time != null) {
                                    // Display the "time" value in the TextView
                                    timeTextView1.setText(time);

                                    // Update the TextView in your XML layout
                                    TextView hpTextView = findViewById(R.id.dn2);
                                    hpTextView.setText(time);
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
    // Method to initiate the download of the QR code
    private void downloadQRCode(ImageView imageView) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        StorageReference qrCodeRef = storageRef.child("qrcodes/" + childId + ".png");

        qrCodeRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                // Get the download URL
                String downloadUrl = uri.toString();

                // Use Picasso to load and display the QR code image
                Picasso.get().load(downloadUrl).into(imageView);

                // Provide success feedback
                Toast.makeText(ParentChildViewActivity.this, "QR code downloaded.", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle any errors
                Toast.makeText(ParentChildViewActivity.this, "Failed to download QR code: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Check and request WRITE_EXTERNAL_STORAGE permission
    private void checkAndRequestPermission() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted, request it
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PERMISSION_REQUEST_WRITE_STORAGE);
        } else {
            // Permission is already granted, proceed with downloading and saving
            downloadQRCodeAndSave();
        }
    }

    // Handle permission request result
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_WRITE_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, proceed with downloading and saving
                downloadQRCodeAndSave();
            } else {
                // Permission denied, show a message or handle it accordingly
                Toast.makeText(this, "Permission denied to save QR code.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Method to initiate the download of the QR code and save it
    private void downloadQRCodeAndSave() {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        StorageReference qrCodeRef = storageRef.child("qrcodes/" + childId + ".png");

        // Provide feedback to the user
        Toast.makeText(this, "Downloading QR code...", Toast.LENGTH_SHORT).show();

        qrCodeRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                // Get the download URL
                String downloadUrl = uri.toString();

                // Use Picasso to load and display the QR code image
                Picasso.get().load(downloadUrl).into(imageView);

                // Save the QR code to external storage
                saveQRCodeToStorage(downloadUrl);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle any errors
                Toast.makeText(ParentChildViewActivity.this, "Failed to download QR code: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Method to save the QR code to external storage
    private void saveQRCodeToStorage(String downloadUrl) {
        Picasso.get().load(downloadUrl).into(new com.squareup.picasso.Target() {
            @Override
            public void onBitmapLoaded(android.graphics.Bitmap bitmap, com.squareup.picasso.Picasso.LoadedFrom from) {
                try {
                    // Define a directory to save the image
                    File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
                    dir.mkdirs(); // Create the directory if it doesn't exist

                    // Create a file for the image
                    File file = new File(dir, "QRCode.png");

                    // Write the Bitmap to the file
                    FileOutputStream fos = new FileOutputStream(file);
                    bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, fos);
                    fos.flush();
                    fos.close();

                    // Notify the user that the image has been saved
                    Toast.makeText(ParentChildViewActivity.this, "QR code saved to Pictures folder", Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    // Handle IO exception
                    e.printStackTrace();
                    Toast.makeText(ParentChildViewActivity.this, "Failed to save QR code", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                // Handle loading failure
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {
                // Handle loading preparation
            }
        });
    }

    // Method to load the QR code image into an ImageView
    private void loadQRCodeIntoImageView(ImageView qrImageView) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        StorageReference qrCodeRef = storageRef.child("qrcodes/" + childId + ".png");

        qrCodeRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                // Get the download URL
                String downloadUrl = uri.toString();

                // Use Picasso to load and display the QR code image
                Picasso.get().load(downloadUrl).into(qrImageView);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle any errors
                Toast.makeText(ParentChildViewActivity.this, "Failed to load QR code: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
