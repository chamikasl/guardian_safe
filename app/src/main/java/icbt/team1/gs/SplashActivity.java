package icbt.team1.gs;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ProgressBar;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;

import icbt.team1.gs.Parent.ParentHomeActivity;
import icbt.team1.gs.Parent.ParentLoginActivity;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DURATION = 3000; // Splash screen duration in milliseconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        ImageView animatedImageView = findViewById(R.id.animatedImageView);
        ProgressBar loadingProgressBar = findViewById(R.id.loadingProgressBar);

        // Animation for ImageView fade-in
        Animation fadeInAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        animatedImageView.setVisibility(View.VISIBLE);
        animatedImageView.startAnimation(fadeInAnimation);

        // Handler to delay the transition to the next activity
        new Handler().postDelayed(() -> {
            // Animation for ImageView fade-out
            Animation fadeOutAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_out);
            animatedImageView.startAnimation(fadeOutAnimation);
            animatedImageView.setVisibility(View.INVISIBLE);

            // Check user login status
            FirebaseAuth auth = FirebaseAuth.getInstance();
            if (auth.getCurrentUser() != null) {
                // User is already logged in, navigate to the home activity
                Intent intent = new Intent(SplashActivity.this, ParentHomeActivity.class);
                startActivity(intent);
            } else {
                // User is not logged in, navigate to the login activity
                Intent intent = new Intent(SplashActivity.this, ParentLoginActivity.class);
                startActivity(intent);
            }

            finish(); // Finish the splash activity to prevent going back to it
        }, SPLASH_DURATION);
    }
}
