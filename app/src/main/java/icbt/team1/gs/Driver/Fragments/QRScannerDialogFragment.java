package icbt.team1.gs.Driver.Fragments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.DialogFragment;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.zxing.ResultPoint;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.CompoundBarcodeView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import icbt.team1.gs.R;

public class QRScannerDialogFragment extends DialogFragment {
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 1;
    private static final String ARG_SELECTED_OPTION = "selectedOption";

    private CompoundBarcodeView barcodeView;
    private String selectedOption;
    private FirebaseFirestore db;
    private SimpleDateFormat dateSdf;
    private SimpleDateFormat timeSdf;
    private Vibrator vibrator;
    private boolean isScanned = false;
    private List<String> scannedDataList = new ArrayList<>();

    public static QRScannerDialogFragment newInstance(String selectedOption) {
        QRScannerDialogFragment fragment = new QRScannerDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_SELECTED_OPTION, selectedOption);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
        Bundle args = getArguments();
        if (args != null && args.containsKey(ARG_SELECTED_OPTION)) {
            selectedOption = args.getString(ARG_SELECTED_OPTION);
        }
        dateSdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        timeSdf = new SimpleDateFormat("HH:mm:ss", Locale.UK);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.driver_fragment_qr_scanner, container, false);
        barcodeView = view.findViewById(R.id.qrScannerView);
        barcodeView.decodeContinuous(callback);
        barcodeView.getStatusView().setVisibility(View.GONE);
        vibrator = (Vibrator) requireContext().getSystemService(requireContext().VIBRATOR_SERVICE);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        getDialog().getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        getDialog().setCanceledOnTouchOutside(true);

        String toastMessage = "Selected Option: " + selectedOption;
        Toast.makeText(requireContext(), toastMessage, Toast.LENGTH_SHORT).show();

        checkAndRequestCameraPermission();
    }

    private void checkAndRequestCameraPermission() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
        } else {
            startScanning();
        }
    }

    private void startScanning() {
        barcodeView.resume();
    }

    private Task<DocumentSnapshot> checkScanDataExists(String scannedData) {
        String currentDate = dateSdf.format(new Date());
        return db.collection("scanned")
                .document(scannedData)
                .collection(currentDate)
                .document(selectedOption)
                .get();
    }

    private void updateDatabase(String scannedData) {
        if (isScanned) {
            return; // If already scanned, do nothing
        }

        if (scannedDataList.contains(scannedData)) {
            return; // If the same data has already been scanned in this session, do nothing
        }

        scannedDataList.add(scannedData);

        checkScanDataExists(scannedData)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (task.getResult().exists()) {
                            // Already scanned, vibrate and show a message
                            vibrateAndShowMessage("Already Scanned");
                        } else {
                            saveDataToFirestore(scannedData);
                        }
                    } else {
                        Toast.makeText(requireContext(), "Error querying the database.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveDataToFirestore(String scannedData) {
        String scannedTime = timeSdf.format(new Date());
        Map<String, Object> scanData = createScanData(scannedData, scannedTime);

        db.collection("scanned")
                .document(scannedData)
                .collection(dateSdf.format(new Date()))
                .document(selectedOption)
                .set(scanData, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    vibrateAndShowMessage("Scanned: " + scannedData + " at " + scannedTime);
                    isScanned = true; // Mark as scanned
                })
                .addOnFailureListener(e -> {
                    handleFailure();
                });
    }

    private void handleFailure() {
        Toast.makeText(requireContext(), "Error saving data to Firestore.", Toast.LENGTH_SHORT).show();
    }

    private Map<String, Object> createScanData(String qrCodeText, String scannedTime) {
        Map<String, Object> scanData = new HashMap<>();
        scanData.put("student_id", qrCodeText);
        scanData.put("time", scannedTime);
        return scanData;
    }

    private void vibrateAndShowMessage(String message) {
        vibrator.vibrate(200); // Vibrate for 200 milliseconds
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }

    private final BarcodeCallback callback = new BarcodeCallback() {
        @Override
        public void barcodeResult(BarcodeResult result) {
            if (result != null) {
                String scannedData = result.getText();
                updateDatabase(scannedData);
            }
        }

        @Override
        public void possibleResultPoints(List<ResultPoint> resultPoints) {
            // Optional method
        }
    };

    @Override
    public void onPause() {
        super.onPause();
        barcodeView.pause();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startScanning();
            } else {
                Toast.makeText(requireContext(), "Camera permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
