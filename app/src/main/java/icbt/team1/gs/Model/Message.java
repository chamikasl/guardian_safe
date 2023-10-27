package icbt.team1.gs.Model;

public class Message {
    private String message;
    private String timestamp;

    public Message() {
        // Default constructor required for Firestore
    }

    public Message(String message, String timestamp) {
        this.message = message;
        this.timestamp = timestamp;
    }

    public String getMessage() {
        return message;
    }

    public String getTimestamp() {
        return timestamp;
    }
}
