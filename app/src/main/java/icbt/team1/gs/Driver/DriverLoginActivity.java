package icbt.team1.gs.Driver;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.Objects;
import icbt.team1.gs.MainActivity;
import icbt.team1.gs.R;
import icbt.team1.gs.databinding.DriverActivityLoginBinding;

public class DriverLoginActivity extends AppCompatActivity {
    private DriverActivityLoginBinding binding;
    private FirebaseAuth auth;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DriverActivityLoginBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        // Initialize Firebase Auth and Firestore
        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        binding.toback.setOnClickListener(view1 -> startActivity(new Intent(DriverLoginActivity.this, MainActivity.class)));

        binding.tosignup.setOnClickListener(v -> startActivity(new Intent(DriverLoginActivity.this, DriverRegisterActivity.class)));

        binding.logbtn.setOnClickListener(v -> {
            String email = binding.email.getText().toString();
            String password = binding.password.getText().toString();

            if (isValidInput(email, password)) {
                loginUser(email, password);
            } else {
                showToast(R.string.fill_in_all_fields);
            }
        });
    }

    private boolean isValidInput(String email, String password) {
        return !TextUtils.isEmpty(email) && !TextUtils.isEmpty(password);
    }

    private void loginUser(String email, String password) {
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String driverId = Objects.requireNonNull(auth.getCurrentUser()).getUid();
                        checkIfUserExists(driverId);
                    } else {
                        showToast(R.string.login_error);
                    }
                })
                .addOnFailureListener(e -> showToast("Error: " + e.getMessage()));
    }

    private void checkIfUserExists(String driverId) {
        CollectionReference driversCollection = firestore.collection("drivers");

        driversCollection.document(driverId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            showToast(R.string.login_successful);
                            Intent intent = new Intent(DriverLoginActivity.this, DriverHomeActivity.class);
                            intent.putExtra("driverId", driverId);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish();
                        } else {
                            showToast("You Are Not A Driver Please Go Back and Login As Parent");
                        }
                    } else {
                        showToast("Error checking user: " + task.getException().getMessage());
                    }
                });
    }

    private void showToast(int messageResId) {
        Toast.makeText(this, messageResId, Toast.LENGTH_SHORT).show();
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
