//need to get url of saved qr

package icbt.team1.gs.Parent;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import icbt.team1.gs.R;

public class ParentChildRegisterActivity extends AppCompatActivity {
    private EditText studentNameEditText, schoolNameEditText, birthdayEditText, genderEditText, noteEditText;
    private FirebaseAuth mAuth;
    private FirebaseFirestore mFirestore;
    private DocumentReference childRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.parent_activity_child_register);

        mAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();

        studentNameEditText = findViewById(R.id.sname);
        schoolNameEditText = findViewById(R.id.sclname);
        birthdayEditText = findViewById(R.id.birthday);
        genderEditText = findViewById(R.id.gender);
        noteEditText = findViewById(R.id.note);

        Button addChildButton = findViewById(R.id.addbtn);
        ImageButton toBack = findViewById(R.id.toback);

        StorageReference qrCodeStorageRef = FirebaseStorage.getInstance().getReference().child("qrcodes");

        toBack.setOnClickListener(view -> onBackPressed());

        addChildButton.setOnClickListener(view -> registerChild());

    }

    private void registerChild() {
        try {
            String studentName = studentNameEditText.getText().toString().trim();
            String schoolName = schoolNameEditText.getText().toString().trim();
            String birthday = birthdayEditText.getText().toString().trim();
            String gender = genderEditText.getText().toString().trim();
            String note = noteEditText.getText().toString().trim();

            if (validateInput(studentName, schoolName, birthday, gender, note)) {
                String parentId = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();

                // Generate a unique child document ID
                String childId = mFirestore.collection("children").document().getId();

                // Initialize childData map and add child UID to it
                Map<String, Object> childData = new HashMap<>();
                childData.put("uid", childId);
                childData.put("name", studentName);
                childData.put("school", schoolName);
                childData.put("birthday", birthday);
                childData.put("gender", gender);
                childData.put("note", note);
                childData.put("busid", "none");
                childData.put("parentid", parentId);

                Bitmap qrCodeBitmap = generateQRCode(childId);

                if (qrCodeBitmap != null) {
                    uploadQRCodeImage(qrCodeBitmap, childId, () -> {
                        // Save data in Firestore using the same child UID
                        mFirestore.collection("children").document(childId)
                                .set(childData)
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        FirebaseFirestore.getInstance()
                                                .collection("parents").document(parentId)
                                                .collection("children").document(childId)
                                                .set(childData);

                                        // Save data in Firebase Realtime Database under the "children" collection
                                        DatabaseReference childRealtimeRef = FirebaseDatabase.getInstance()
                                                .getReference("children")
                                                .child(childId);

                                        childRealtimeRef.setValue(childData);

                                        // Save data in the parent's collection as well
                                        DatabaseReference parentRealtimeRef = FirebaseDatabase.getInstance()
                                                .getReference("parents")
                                                .child(parentId)
                                                .child("children")
                                                .child(childId);

                                        parentRealtimeRef.setValue(childData);

                                        showToast("Child added successfully");
                                        navigateToParentHome();
                                    } else {
                                        showToast("Error: " + Objects.requireNonNull(task.getException()).getMessage());
                                    }
                                });
                    });
                } else {
                    showToast("Error generating QR code");
                }
            }
        } catch (WriterException e) {
            e.printStackTrace();
            showToast("Error generating QR code: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            showToast("Error: " + e.getMessage());
        }
    }

    private boolean validateInput(String studentName, String schoolName, String birthday, String gender, String note) {
        if (studentName.isEmpty() || schoolName.isEmpty() || birthday.isEmpty() || gender.isEmpty() || note.isEmpty()) {
            showToast("Please fill in all fields");
            return false;
        }
        // Add more validation logic as needed
        return true;
    }

    private Bitmap generateQRCode(String text) throws WriterException {
        BitMatrix bitMatrix = new QRCodeWriter().encode(text, BarcodeFormat.QR_CODE, 400, 400);
        int bitMatrixWidth = bitMatrix.getWidth();
        int bitMatrixHeight = bitMatrix.getHeight();
        Bitmap qrCodeBitmap = Bitmap.createBitmap(bitMatrixWidth, bitMatrixHeight, Bitmap.Config.ARGB_8888);

        for (int x = 0; x < bitMatrixWidth; x++) {
            for (int y = 0; y < bitMatrixHeight; y++) {
                qrCodeBitmap.setPixel(x, y, bitMatrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF);
            }
        }
        return qrCodeBitmap;
    }

    private void uploadQRCodeImage(Bitmap qrCodeBitmap, String childId, Runnable onSuccess) {
        try {
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReference();
            StorageReference qrCodeRef = storageRef.child("qrcodes/" + childId + ".png");

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            qrCodeBitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
            byte[] qrCodeData = baos.toByteArray();

            UploadTask uploadTask = qrCodeRef.putBytes(qrCodeData);
            uploadTask.addOnSuccessListener(taskSnapshot -> {
                showToast("QR code uploaded successfully");
                onSuccess.run();
            }).addOnFailureListener(e -> showToast("Error uploading QR code: " + e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            showToast("Error uploading QR code: " + e.getMessage());
        }
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }


    private void navigateToParentHome() {
        startActivity(new Intent(ParentChildRegisterActivity.this, ParentHomeActivity.class));
        finish();
    }
}
