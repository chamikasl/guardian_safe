package icbt.team1.gs.Parent;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import java.util.HashMap;
import java.util.Map;

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

        mAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();
        mRealtimeDatabase = FirebaseDatabase.getInstance().getReference();

        binding.toback.setOnClickListener(view1 -> startActivity(new Intent(ParentRegisterActivity.this, ParentLoginActivity.class)));

        binding.addbtn.setOnClickListener(view12 -> {
            String parentName = binding.pname.getText().toString().trim();
            String phone = binding.phone.getText().toString().trim();
            String email = binding.email.getText().toString().trim();
            String role = binding.role.getText().toString().trim();
            String password = binding.password.getText().toString();
            String address = binding.address.getText().toString().trim();

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
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            String userId = user.getUid();

                            // Retrieve the FCM token
                            FirebaseMessaging.getInstance().getToken()
                                    .addOnCompleteListener(new OnCompleteListener<String>() {
                                        @Override
                                        public void onComplete(@NonNull Task<String> task) {
                                            if (task.isSuccessful()) {
                                                String fcmToken = task.getResult();
                                                // Create a userMap with the FCM token
                                                Map<String, Object> userMap = new HashMap<>();
                                                userMap.put("name", parentName);
                                                userMap.put("phone", phone);
                                                userMap.put("email", email);
                                                userMap.put("role", role);
                                                userMap.put("address", address);
                                                userMap.put("fcmToken", fcmToken); // Save the FCM token

                                                // Save user data to Firestore
                                                mFirestore.collection("parents").document(userId)
                                                        .set(userMap)
                                                        .addOnCompleteListener(task1 -> {
                                                            if (task1.isSuccessful()) {
                                                                // Save user data to Firebase Realtime Database
                                                                DatabaseReference userRef = mRealtimeDatabase.child("parents").child(userId);
                                                                userRef.setValue(userMap);

                                                                showToast("Registration Successful");
                                                                startActivity(new Intent(ParentRegisterActivity.this, ParentLoginActivity.class));
                                                                finish();
                                                            } else {
                                                                showToast("Error: " + task1.getException().getMessage());
                                                            }
                                                        });
                                            } else {
                                                showToast("Error getting FCM token: " + task.getException().getMessage());
                                            }
                                        }
                                    });
                        }
                    } else {
                        showToast("Error: " + task.getException().getMessage());
                    }
                });
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}