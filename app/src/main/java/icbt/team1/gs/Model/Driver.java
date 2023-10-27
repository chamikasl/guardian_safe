package icbt.team1.gs.Model;

public class Driver {
    private String name;
    private String busno;
    private String uid;

    // No-argument constructor (required for Firebase Firestore)
    public Driver() {
    }

    public Driver(String name, String busno, String uid) {
        this.name = name;
        this.busno = busno;
        this.uid = uid;
    }

    // Getter and Setter methods for name
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    // Getter and Setter methods for busNumber
    public String getBusno() {
        return busno;
    }

    public void setBusno(String busno) {
        this.busno = busno;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

}
