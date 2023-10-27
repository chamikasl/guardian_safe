package icbt.team1.gs.Driver.Fragments;

import android.os.Bundle;
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
import com.google.firebase.database.*;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import icbt.team1.gs.Adapter.RequestedChildAdapter;
import icbt.team1.gs.Model.Child;
import icbt.team1.gs.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RequestsFragment extends Fragment implements RequestedChildAdapter.OnAcceptRejectClickListener {

    private RecyclerView recyclerView;
    private RequestedChildAdapter requestedChildAdapter;
    private List<Child> requestedChildrenList;
    private DatabaseReference driverToApproveRef;
    private FirebaseUser driverUid;
    private FirebaseFirestore firestore;

    private static final String DRIVER_TO_APPROVE_PATH = "drivers/%s/toApprove";

    public RequestsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.driver_fragment_requests, container, false);

        initializeFirebase();

        recyclerView = view.findViewById(R.id.requests_recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());
        recyclerView.setLayoutManager(layoutManager);

        requestedChildrenList = new ArrayList<>();
        requestedChildAdapter = new RequestedChildAdapter(requestedChildrenList, this);
        recyclerView.setAdapter(requestedChildAdapter);

        fetchRequestedChildrenData();

        return view;
    }

    private void initializeFirebase() {
        firestore = FirebaseFirestore.getInstance();
        driverUid = FirebaseAuth.getInstance().getCurrentUser();
        driverToApproveRef = FirebaseDatabase.getInstance().getReference(String.format(DRIVER_TO_APPROVE_PATH, driverUid.getUid()));
    }

    private void fetchRequestedChildrenData() {
        driverToApproveRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                requestedChildrenList.clear();

                for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                    Child child = childSnapshot.getValue(Child.class);
                    if (child != null) {
                        requestedChildrenList.add(child);
                    }
                }
                requestedChildAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle any errors here
                Toast.makeText(requireContext(), "Error fetching data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onAcceptClick(Child child) {
        updateChildData(child, driverUid.getUid());
        Toast.makeText(requireContext(), "Accepted: " + child.getName(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRejectClick(Child child) {
        updateChildData(child, "none");
        Toast.makeText(requireContext(), "Rejected: " + child.getName(), Toast.LENGTH_SHORT).show();
    }

    private void updateChildData(Child child, String busId) {
        updateFirestoreChildData(child, busId);
        updateRealtimeChildData(child, busId);
    }

    private void updateFirestoreChildData(Child child, String busId) {
        Map<String, Object> childData = new HashMap<>();
        childData.put("uid", child.getUid());
        childData.put("name", child.getName());
        childData.put("parentid", child.getParentid());
        childData.put("school", child.getSchool());

        // Update child's bus ID in Firestore
        firestore.collection("children").document(child.getUid()).update("busid", busId);

        // Remove the child from driver's toApprove field
        firestore.collection("drivers").document(driverUid.getUid()).update("toApprove", FieldValue.arrayRemove(child.getUid()));

        // Add the child to driver's children field
        firestore.collection("drivers").document(driverUid.getUid()).update("children", FieldValue.arrayUnion(child.getUid()));

        // Update child's bus ID in parent's children field
        firestore.collection("parents").document(child.getParentid()).collection("children").document(child.getUid()).update("busid", busId);
    }

    private void updateRealtimeChildData(Child child, String busId) {

        Map<String, Object> childData = new HashMap<>();
        childData.put("uid", child.getUid());
        childData.put("name", child.getName());
        childData.put("parentid", child.getParentid());
        childData.put("school", child.getSchool());

        // Remove the child from driver-toapprove field in Realtime Database
        FirebaseDatabase.getInstance().getReference().child("drivers").child(driverUid.getUid()).child("toApprove").child(child.getUid()).removeValue();

        // Update child's bus ID in Realtime Database
        FirebaseDatabase.getInstance().getReference().child("children").child(child.getUid()).child("busid").setValue(busId);

        // Add the child to driver's children field
        FirebaseDatabase.getInstance().getReference().child("drivers").child(driverUid.getUid()).child("children").child(child.getUid()).setValue(childData);

        // Update child's bus ID in parent-child relationship in Realtime Database
        FirebaseDatabase.getInstance().getReference().child("parents").child(child.getParentid()).child("children").child(child.getUid()).child("busid").setValue(busId);
    }
}
