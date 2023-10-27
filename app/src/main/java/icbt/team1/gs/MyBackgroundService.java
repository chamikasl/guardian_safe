package icbt.team1.gs;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

public abstract class MyBackgroundService extends Service {
    @Override
    public void onCreate() {
        super.onCreate();
        // Initialize your background tasks here.
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Start your background tasks here.
        return START_STICKY; // This flag restarts the service if it's killed by the system.
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
