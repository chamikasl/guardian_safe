package icbt.team1.gs.Driver.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.DialogFragment;

import icbt.team1.gs.R;

public class TimeSelectorFragment extends DialogFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.driver_frament_time_selector, container, false);

        // Find the buttons by their IDs
        Button homePickButton = view.findViewById(R.id.hpbtn);
        Button schoolDropButton = view.findViewById(R.id.sdbtn);
        Button schoolPickButton = view.findViewById(R.id.spbtn);
        Button homeDropButton = view.findViewById(R.id.hdbtn);

        // Set a single click listener for all buttons
        View.OnClickListener buttonClickListener = v -> {
            String selectedOption = "";
            if (v.getId() == R.id.hpbtn) {
                selectedOption = "home pick";
            } else if (v.getId() == R.id.sdbtn) {
                selectedOption = "school drop";
            } else if (v.getId() == R.id.spbtn) {
                selectedOption = "school pick";
            } else if (v.getId() == R.id.hdbtn) {
                selectedOption = "home drop";
            }
            openQRScannerDialog(selectedOption);
        };

        // Set the click listener for all buttons
        homePickButton.setOnClickListener(buttonClickListener);
        schoolDropButton.setOnClickListener(buttonClickListener);
        schoolPickButton.setOnClickListener(buttonClickListener);
        homeDropButton.setOnClickListener(buttonClickListener);

        return view;
    }

    private void openQRScannerDialog(String selectedOption) {
        // Create and show the QR scanner dialog
        QRScannerDialogFragment qrScannerDialogFragment = new QRScannerDialogFragment();
        // Pass the selected option to the QRScannerDialogFragment
        Bundle bundle = new Bundle();
        bundle.putString("selectedOption", selectedOption);
        qrScannerDialogFragment.setArguments(bundle);
        // Replace the current fragment with the QRScannerDialogFragment
        replaceFragment(qrScannerDialogFragment);
    }

    private void replaceFragment(DialogFragment fragment) {
        // Replace the current fragment with the provided fragment
        if (getFragmentManager() != null) {
            fragment.show(getFragmentManager(), "qr_scanner_dialog");
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        getDialog().getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        getDialog().setCanceledOnTouchOutside(true); // Close the dialog when tapped outside
    }

    @Override
    public void onPause() {
        super.onPause();
    }
}
