//finished

package icbt.team1.gs.Parent;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import icbt.team1.gs.Adapter.DriverAdapter;
import icbt.team1.gs.Model.Driver;
import icbt.team1.gs.R;

public class ParentChildAddBusActivity extends AppCompatActivity implements DriverAdapter.OnItemClickListener {
    private DriverAdapter driverAdapter;
    private List<Driver> driverList;
    private FirebaseFirestore firestore;
    private DatabaseReference realtimeDatabaseRef;
    private FirebaseUser currentUser;
    private String selectedDriverId, childId, parentId, childScl, childName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.parent_activity_child_add_bus);

        childId = getIntent().getStringExtra("uid");
        childScl = getIntent().getStringExtra("cscl");
        childName = getIntent().getStringExtra("cname");

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        realtimeDatabaseRef = FirebaseDatabase.getInstance().getReference();

        ImageButton toBack = findViewById(R.id.to_back);
        toBack.setOnClickListener(v -> onBackPressed());

        RecyclerView recyclerView = findViewById(R.id.driverRecyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        driverList = new ArrayList<>();
        firestore = FirebaseFirestore.getInstance();

        // Initialize the DriverAdapter
        driverAdapter = new DriverAdapter(driverList, this, childId);
        recyclerView.setAdapter(driverAdapter);

        // Fetch the list of registered drivers from Firestore
        fetchDrivers();

        TextView addBtn = findViewById(R.id.add_btn);
        addBtn.setOnClickListener(v -> sendRequestToDriver());

        // Implement your logic to send a request to the selected driver
        // ...
    }

    @Override
    public void onItemClick(Driver driver, String childId) {
        // Handle the item click here, e.g., store the selected driver ID
        // You can also update the selected driver's position in the adapter

        // Example:
        selectedDriverId = driver.getUid();
        driverAdapter.setSelectedDriverPosition(driverList.indexOf(driver));
        Toast.makeText(ParentChildAddBusActivity.this, driver.getName() + " selected.", Toast.LENGTH_SHORT).show();
    }

    private void fetchDrivers() {
        firestore.collection("drivers")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        driverList.clear(); // Clear the list to avoid duplicates
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            // Create Driver objects and add them to the list
                            Driver driver = document.toObject(Driver.class);
                            driverList.add(driver);
                        }
                        driverAdapter.notifyDataSetChanged(); // Notify the adapter of data change
                    } else {
                        // Handle the error
                        Toast.makeText(this, "Error fetching drivers.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void sendRequestToDriver() {
        parentId = currentUser.getUid();

        Map<String, Object> childData = new HashMap<>();
        childData.put("uid", childId);
        childData.put("name", childName);
        childData.put("parentid", parentId);
        childData.put("school", childScl);

        if (selectedDriverId != null && childId != null) {
            // Firebase Realtime Database
            DatabaseReference driverToApproveRef = realtimeDatabaseRef.child("drivers").child(selectedDriverId).child("toApprove").child(childId);
            driverToApproveRef.setValue(childData);

            DatabaseReference childBusIdRef = realtimeDatabaseRef.child("children").child(childId).child("busid");
            childBusIdRef.setValue("pending");

            DatabaseReference parentChildBusIdRef = realtimeDatabaseRef.child("parents").child(parentId).child("children").child(childId).child("busid");
            parentChildBusIdRef.setValue("pending");

            // Firebase Firestore
            FirebaseFirestore.getInstance().runTransaction(transaction -> {
                transaction.update(
                        FirebaseFirestore.getInstance().collection("parents").document(parentId).collection("children").document(childId),
                        "busid", "pending"
                );
                transaction.update(
                        FirebaseFirestore.getInstance().collection("children").document(childId),
                        "busid", "pending"
                );
                transaction.update(
                        FirebaseFirestore.getInstance().collection("drivers").document(selectedDriverId),
                        "toApprove", FieldValue.arrayUnion(childId)
                );
                return null;
            }).addOnSuccessListener(aVoid -> Toast.makeText(ParentChildAddBusActivity.this, "Request sent successfully", Toast.LENGTH_SHORT).show()).addOnFailureListener(e -> Toast.makeText(ParentChildAddBusActivity.this, "Error sending request", Toast.LENGTH_SHORT).show());

            Intent intent = new Intent(ParentChildAddBusActivity.this, ParentHomeActivity.class);
            finish();
            startActivity(intent);
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(ParentChildAddBusActivity.this, ParentHomeActivity.class);
        startActivity(intent);
    }

}
