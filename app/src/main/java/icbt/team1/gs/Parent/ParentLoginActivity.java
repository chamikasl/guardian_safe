package icbt.team1.gs.Parent;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import icbt.team1.gs.MainActivity;
import icbt.team1.gs.databinding.ParentActivityLoginBinding;

public class ParentLoginActivity extends AppCompatActivity {
    private ParentActivityLoginBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ParentActivityLoginBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        // Initialize Firebase Auth and Firestore
        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        binding.toback.setOnClickListener(view1 -> startActivity(new Intent(ParentLoginActivity.this, MainActivity.class)));

        binding.tosignup.setOnClickListener(view12 -> startActivity(new Intent(ParentLoginActivity.this, ParentRegisterActivity.class)));

        binding.logbtn.setOnClickListener(view13 -> {
            String email = binding.email.getText().toString().trim();
            String password = binding.otp.getText().toString();

            // Validate user input
            if (isValidInput(email, password)) {
                signInWithEmailAndPassword(email, password);
            }
        });
    }

    private boolean isValidInput(String email, String password) {
        return !email.isEmpty() && !password.isEmpty();
    }

    private void signInWithEmailAndPassword(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        String parentUserId = mAuth.getCurrentUser().getUid();
                        checkIfParentExists(parentUserId);
                    } else {
                        // Sign in failed
                        String errorMessage = "Authentication failed. Please check your credentials.";
                        showToast(errorMessage);
                    }
                });
    }

    private void checkIfParentExists(String parentUserId) {
        CollectionReference parentsCollection = firestore.collection("parents");

        parentsCollection.document(parentUserId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            showToast("Login successful");
                            Intent intent = new Intent(ParentLoginActivity.this, ParentHomeActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish();
                        } else {
                            showToast("You are not registered as a parent. Please register first.");
                        }
                    } else {
                        showToast("Error checking user: " + task.getException().getMessage());
                    }
                });
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
