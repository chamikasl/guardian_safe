package icbt.team1.gs;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import icbt.team1.gs.Driver.DriverLoginActivity;
import icbt.team1.gs.Parent.ParentLoginActivity;
import icbt.team1.gs.databinding.MainActivityBinding;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        icbt.team1.gs.databinding.MainActivityBinding binding = MainActivityBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        binding.parentlogbtn.setOnClickListener(v -> openParentLoginActivity());
        binding.driverlogbtn.setOnClickListener(v -> openDriverLoginActivity());
    }

    private void openParentLoginActivity() {
        Intent intent = new Intent(this, ParentLoginActivity.class);
        startActivity(intent);
    }

    private void openDriverLoginActivity() {
        Intent intent = new Intent(this, DriverLoginActivity.class);
        startActivity(intent);
    }
}
