package icbt.team1.gs.Driver;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import icbt.team1.gs.Driver.Fragments.HomeFragment;
import icbt.team1.gs.Driver.Fragments.RequestsFragment;
import icbt.team1.gs.Driver.Fragments.SettingsFragment;
import icbt.team1.gs.Driver.Fragments.StudentsFragment;
import icbt.team1.gs.MainActivity;
import icbt.team1.gs.R;

public class DriverHomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private DrawerLayout drawerLayout;
    private TextView nameTextView, vehicleNumberTextView;
    private DatabaseReference userReference;
    private FirebaseUser currentUser;
    private ImageView msg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.driver_activity_main);

        msg = findViewById(R.id.msg1);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Initialize views in the nav_header
        nameTextView = navigationView.getHeaderView(0).findViewById(R.id.drivername);
        vehicleNumberTextView = navigationView.getHeaderView(0).findViewById(R.id.vehicleno);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open_nav,
                R.string.close_nav);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Get the current user
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        // Initialize the Firebase Realtime Database reference
        userReference = FirebaseDatabase.getInstance().getReference("drivers").child(currentUser.getUid());

        // Listen for changes in the user data

        msg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Replace the current fragment with the MessageFragment
                MessageFragment messageFragment = new MessageFragment();
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment_container, messageFragment);
                transaction.addToBackStack(null); // Add to back stack for navigation
                transaction.commit();
            }
        });
        userReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Fetch and display user data
                    String name = dataSnapshot.child("name").getValue(String.class);
                    String busNo = dataSnapshot.child("busno").getValue(String.class);
                    if (name != null) {
                        nameTextView.setText(name);
                    }
                    if (busNo != null) {
                        vehicleNumberTextView.setText(busNo);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(DriverHomeActivity.this, "Failed to retrieve user data", Toast.LENGTH_SHORT).show();
            }
        });

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HomeFragment()).commit();
            navigationView.setCheckedItem(R.id.nav_home);
            setTitle("Home"); // Set the default toolbar title to "Home"
            msg.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        int itemId = item.getItemId();

        if (itemId == R.id.nav_home) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HomeFragment()).commit();
            setTitle("Home");
            msg.setVisibility(View.VISIBLE);
        } else if (itemId == R.id.nav_students) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new StudentsFragment()).commit();
            setTitle("Students");
            msg.setVisibility(View.INVISIBLE);
        } else if (itemId == R.id.nav_requests) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new RequestsFragment()).commit();
            setTitle("Requests");
            msg.setVisibility(View.INVISIBLE);
        } else if (itemId == R.id.nav_settings) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new SettingsFragment()).commit();
            setTitle("Settings");
            msg.setVisibility(View.INVISIBLE);
        } else if (itemId == R.id.nav_logout) {
            logout();
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;

    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    private void logout() {
        FirebaseAuth.getInstance().signOut();
        // Start the main screen activity
        Intent intent = new Intent(DriverHomeActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish(); // Close the current activity
    }

}
