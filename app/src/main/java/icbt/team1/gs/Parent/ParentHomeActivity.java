//need to setup present absence of the day, status of each child

package icbt.team1.gs.Parent;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import icbt.team1.gs.Adapter.ChildAdapter;
import icbt.team1.gs.Model.Child;
import icbt.team1.gs.R;
import icbt.team1.gs.databinding.ParentActivityHomeBinding;

public class ParentHomeActivity extends AppCompatActivity implements ChildAdapter.OnItemClickListener {
    private ChildAdapter childAdapter;
    private List<Child> childList;
    private FirebaseAuth mAuth;
    private FirebaseFirestore mFirestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ParentActivityHomeBinding binding = ParentActivityHomeBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        mAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();

        BottomNavigationView bottomNavigationView = binding.bottomNavigation;
        bottomNavigationView.setSelectedItemId(R.id.bottom_home);


        RecyclerView recyclerView = binding.childRecyclerView;
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        binding.addchildBtn.setOnClickListener(view1 -> {
            Intent intent = new Intent(ParentHomeActivity.this, ParentChildRegisterActivity.class);
            startActivity(intent);
        });

        childList = new ArrayList<>();
        childAdapter = new ChildAdapter(childList, this);
        recyclerView.setAdapter(childAdapter);

        fetchChildrenData();

        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.bottom_home) {
                return true;
            } else if (item.getItemId() == R.id.bottom_settings) {
                startActivity(new Intent(getApplicationContext(), ParentSettingsActivity.class));
                finish();
                return true;
            } else if (item.getItemId() == R.id.bottom_messages) {
                startActivity(new Intent(getApplicationContext(), ParentMessagesActivity.class));
                finish();
                return true;
            }
            return false;
        });
    }

    private void fetchChildrenData() {
        String parentId = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
        mFirestore.collection("parents").document(parentId)
                .collection("children")
                .get()
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        handleFetchChildrenResult(task.getResult());
                    } else {
                        showToast("Error: " + Objects.requireNonNull(task.getException()).getMessage());
                    }
                });
    }

    private void handleFetchChildrenResult(@NonNull QuerySnapshot querySnapshot) {
        List<Child> newChildList = querySnapshot.toObjects(Child.class);

        // Calculate the difference between the old and new lists
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new ChildDiffCallback(childList, newChildList));

        // Update the dataset
        childList.clear();
        childList.addAll(newChildList);

        // Apply the diff result to the adapter for efficient updates
        diffResult.dispatchUpdatesTo(childAdapter);
    }


    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void showAddBusAlertDialog(Child child) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.add_child_to_bus));
        builder.setMessage(getString(R.string.add_child_to_bus_message));
        builder.setPositiveButton(getString(R.string.yes), (dialog, which) -> {
            Intent intent = new Intent(getApplicationContext(), ParentChildAddBusActivity.class);
            intent.putExtra("uid", child.getUid());
            intent.putExtra("cname", child.getName());
            intent.putExtra("cscl", child.getSchool());
            startActivity(intent);
        });
        builder.setNegativeButton(getString(R.string.no), (dialog, which) -> dialog.dismiss());

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public void onItemClick(Child child) {
        if ("none".equals(child.getBusid())) {
            showAddBusAlertDialog(child);
        } else if ("pending".equals(child.getBusid())) {
            Toast.makeText(this, "Please wait for the driver's approval.", Toast.LENGTH_SHORT).show();
        } else {
            Intent intent = new Intent(ParentHomeActivity.this, ParentChildProfileActivity.class);
            intent.putExtra("uid", child.getUid());
            intent.putExtra("cbusid", child.getBusid());
            startActivity(intent);
        }
    }
}
