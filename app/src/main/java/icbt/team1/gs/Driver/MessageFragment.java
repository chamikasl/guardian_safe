package icbt.team1.gs.Driver;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import icbt.team1.gs.R;

public class MessageFragment extends Fragment {
    private EditText messageEditText;
    private Button sendButton;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.message_fragment, container, false);

        messageEditText = view.findViewById(R.id.messageEditText);
        sendButton = view.findViewById(R.id.sendButton);

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });

        return view;
    }

    private void sendMessage() {
        String message = messageEditText.getText().toString().trim();

        if (!message.isEmpty()) {
            // Get the current date and time
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String timestamp = sdf.format(new Date());

            // Create a message object
            Map<String, Object> messageData = new HashMap<>();
            messageData.put("message", message);
            messageData.put("timestamp", timestamp);

            // Add the message to Firestore
            db.collection("messages")
                    .add(messageData)
                    .addOnCompleteListener(new OnCompleteListener() {
                        @Override
                        public void onComplete(@NonNull Task task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(getActivity(), "Message sent successfully", Toast.LENGTH_SHORT).show();
                                messageEditText.setText(""); // Clear the input field
                            } else {
                                Toast.makeText(getActivity(), "Error sending message", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        } else {
            Toast.makeText(getActivity(), "Please enter a message", Toast.LENGTH_SHORT).show();
        }
    }
}
