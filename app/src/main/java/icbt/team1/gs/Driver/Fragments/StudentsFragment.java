package icbt.team1.gs.Driver.Fragments;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

import icbt.team1.gs.Adapter.SelectedChildAdapter;
import icbt.team1.gs.Model.Child;
import icbt.team1.gs.R;

public class StudentsFragment extends Fragment implements SelectedChildAdapter.OnAcceptRejectClickListener {

    private RecyclerView recyclerView;
    private SelectedChildAdapter selectedChildAdapter;
    private List<Child> selectedChildrenList;
    private DatabaseReference driverChildrenRef;
    private FirebaseUser driverUid;
    private FirebaseFirestore firestore;
    private PackageManager packageManager;

    private static final String DRIVER_TO_APPROVE_PATH = "drivers/%s/children";

    public StudentsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.driver_fragment_students, container, false);
        recyclerView = view.findViewById(R.id.children_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        selectedChildrenList = new ArrayList<>();
        selectedChildAdapter = new SelectedChildAdapter(selectedChildrenList, this);
        recyclerView.setAdapter(selectedChildAdapter);

        packageManager = requireActivity().getPackageManager();

        initializeFirebase();
        fetchSelectedChildrenData();

        return view;
    }

    private void initializeFirebase() {
        firestore = FirebaseFirestore.getInstance();
        driverUid = FirebaseAuth.getInstance().getCurrentUser();
        driverChildrenRef = FirebaseDatabase.getInstance().getReference(String.format(DRIVER_TO_APPROVE_PATH, driverUid.getUid()));
    }

    private void fetchSelectedChildrenData() {
        driverChildrenRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                selectedChildrenList.clear();

                for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                    Child child = childSnapshot.getValue(Child.class);
                    if (child != null) {
                        selectedChildrenList.add(child);
                    }
                }
                selectedChildAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle any errors here
                Toast.makeText(requireContext(), "Error fetching data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onCallClick(Child child) {
        DatabaseReference parentRef = FirebaseDatabase.getInstance().getReference("parents").child(child.getParentid()).child("phone");
        parentRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String phoneNumber = dataSnapshot.getValue(String.class);

                if (phoneNumber != null) {
                    // Create an intent to open the dialer with the phone number
                    Intent intent = new Intent(Intent.ACTION_DIAL);
                    intent.setData(Uri.parse("tel:" + phoneNumber));

                    // Check if there is an activity that can handle the intent
                    if (intent.resolveActivity(packageManager) != null) {
                        startActivity(intent);
                    } else {
                        Toast.makeText(requireContext(), "No app can handle this action", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // This method is called if there is an error reading the data
                Log.e("Firebase", "Error reading data", databaseError.toException());
            }
        });
    }

    @Override
    public void onViewClick(Child child) {
        // Handle view click here
    }
}

