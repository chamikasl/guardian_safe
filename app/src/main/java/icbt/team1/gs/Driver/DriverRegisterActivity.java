package icbt.team1.gs.Driver;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import icbt.team1.gs.databinding.DriverActivityRegisterBinding;

public class DriverRegisterActivity extends AppCompatActivity {
    private DriverActivityRegisterBinding binding;
    private FirebaseAuth auth;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DriverActivityRegisterBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        // Initialize Firebase Auth and Firestore
        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        binding.toback.setOnClickListener(view1 -> startActivity(new Intent(DriverRegisterActivity.this, DriverLoginActivity.class)));

        binding.regbtn.setOnClickListener(v -> {
            String txtName = binding.name.getText().toString();
            String txtEmail = binding.email.getText().toString();
            String txtPhno = binding.phone.getText().toString();
            String txtPsw = binding.password.getText().toString();
            String txtAddress = binding.address.getText().toString();
            String txtId = binding.idno.getText().toString();
            String txtVehi = binding.vehicleno.getText().toString();
            String txtLic = binding.licenseno.getText().toString();

            if (isValidInput(txtName, txtEmail, txtPhno, txtPsw, txtId, txtVehi, txtLic, txtAddress)) {
                registerUser(txtName, txtEmail, txtPhno, txtPsw, txtId, txtVehi, txtLic, txtAddress);
            } else {
                showToast("Fill in all fields");
            }
        });
    }

    private boolean isValidInput(String txtName, String txtEmail, String txtPhno, String txtPsw, String txtId, String txtVehi, String txtLic, String txtAddress) {
        // Implement your validation logic here
        // For example, you can check if the fields are not empty and meet specific criteria
        return !TextUtils.isEmpty(txtName) &&
                !TextUtils.isEmpty(txtEmail) &&
                !TextUtils.isEmpty(txtPhno) &&
                !TextUtils.isEmpty(txtPsw) &&
                !TextUtils.isEmpty(txtId) &&
                !TextUtils.isEmpty(txtVehi) &&
                !TextUtils.isEmpty(txtLic) &&
                !TextUtils.isEmpty(txtAddress);
    }

    private void registerUser(final String txtName, final String txtEmail, final String txtPhno, final String txtPsw, String txtId, String txtBusno, String txtLic, String txtAddress) {
        auth.createUserWithEmailAndPassword(txtEmail, txtPsw)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String id = Objects.requireNonNull(auth.getCurrentUser()).getUid();

                        // Create a data object for Firestore
                        Map<String, Object> firestoreData = new HashMap<>();
                        firestoreData.put("uid", id);
                        firestoreData.put("name", txtName);
                        firestoreData.put("email", txtEmail);
                        firestoreData.put("phone", txtPhno);
                        firestoreData.put("nic", txtId);
                        firestoreData.put("busno", txtBusno);
                        firestoreData.put("address", txtAddress);
                        firestoreData.put("licno", txtLic);

                        // Create a data object for Realtime Database
                        Map<String, Object> realtimeData = new HashMap<>();
                        realtimeData.put("uid", id);
                        realtimeData.put("name", txtName);
                        realtimeData.put("email", txtEmail);
                        realtimeData.put("phone", txtPhno);
                        realtimeData.put("nic", txtId);
                        realtimeData.put("busno", txtBusno);
                        realtimeData.put("address", txtAddress);
                        realtimeData.put("licno", txtLic);

                        // Save data in Firestore
                        firestore.collection("drivers")
                                .document(id)
                                .set(firestoreData)
                                .addOnCompleteListener(task1 -> {
                                    if (task1.isSuccessful()) {
                                        // Data saved in Firestore, now save data in Firebase Realtime Database
                                        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("drivers");
                                        databaseReference.child(id).setValue(realtimeData)
                                                .addOnCompleteListener(task11 -> {
                                                    if (task11.isSuccessful()) {
                                                        showToast("Registration successful");
                                                        Intent intent = new Intent(DriverRegisterActivity.this, DriverLoginActivity.class);
                                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                        startActivity(intent);
                                                        finish();
                                                    } else {
                                                        showToast("Error: " + Objects.requireNonNull(task11.getException()).getMessage());
                                                    }
                                                });
                                    } else {
                                        showToast("Error: " + Objects.requireNonNull(task1.getException()).getMessage());
                                    }
                                });
                    } else {
                        showToast("Error: " + Objects.requireNonNull(task.getException()).getMessage());
                    }
                });
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}
