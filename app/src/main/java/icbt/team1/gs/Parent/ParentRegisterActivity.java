//finished

package icbt.team1.gs.Parent;

import android.content.Intent;
import android.os.Bundle;
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

import icbt.team1.gs.databinding.ParentActivityRegisterBinding;

public class ParentRegisterActivity extends AppCompatActivity {
    private ParentActivityRegisterBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseFirestore mFirestore;
    private DatabaseReference mRealtimeDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ParentActivityRegisterBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        // Initialize Firebase Auth, Firestore, and Realtime Database
        mAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();
        mRealtimeDatabase = FirebaseDatabase.getInstance().getReference(); // Initialize Firebase Realtime Database reference

        binding.toback.setOnClickListener(view1 -> startActivity(new Intent(ParentRegisterActivity.this, ParentLoginActivity.class)));

        binding.addbtn.setOnClickListener(view12 -> {
            String parentName = binding.pname.getText().toString().trim();
            String phone = binding.phone.getText().toString().trim();
            String email = binding.email.getText().toString().trim();
            String role = binding.role.getText().toString().trim();
            String password = binding.password.getText().toString();
            String address = binding.address.getText().toString().trim();

            // Validate user input
            if (isValidInput(parentName, phone, email, role, password, address)) {
                registerParent(parentName, phone, email, role, password, address);
            }
        });
    }

    private boolean isValidInput(String parentName, String phone, String email, String role, String password, String address) {
        if (parentName.isEmpty() || phone.isEmpty() || email.isEmpty() || role.isEmpty() || password.isEmpty() || address.isEmpty()) {
            showToast("All fields are required");
            return false;
        } else if (password.length() < 6) {
            showToast("Password must be at least 6 characters long");
            return false;
        } else {
            return true;
        }
    }

    private void registerParent(String parentName, String phone, String email, String role, String password, String address) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // User registration successful
                        String userId = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();

                        // Create a user document in Firestore
                        Map<String, Object> user = new HashMap<>();
                        user.put("name", parentName);
                        user.put("phone", phone);
                        user.put("email", email);
                        user.put("role", role);
                        user.put("address", address);
                        user.put("uid", userId); // Save the parent's UID in Firestore

                        // Save user data to Firestore
                        mFirestore.collection("parents").document(userId)
                                .set(user)
                                .addOnCompleteListener(task1 -> {
                                    if (task1.isSuccessful()) {
                                        // User data saved successfully in Firestore

                                        // Save user data to Firebase Realtime Database
                                        DatabaseReference userRef = mRealtimeDatabase.child("parents").child(userId);
                                        userRef.setValue(user);

                                        // Save UID in Realtime Database
                                        userRef.child("uid").setValue(userId);

                                        showToast("Registration Successful");
                                        startActivity(new Intent(ParentRegisterActivity.this, ParentLoginActivity.class));
                                        finish(); // Finish the registration activity
                                    } else {
                                        // Error saving user data in Firestore
                                        showToast("Error: " + Objects.requireNonNull(task1.getException()).getMessage());
                                    }
                                });
                    } else {
                        // User registration failed
                        showToast("Error: " + Objects.requireNonNull(task.getException()).getMessage());
                    }
                });
    }
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
