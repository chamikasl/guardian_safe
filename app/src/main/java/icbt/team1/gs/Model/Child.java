package icbt.team1.gs.Model;

public class Child {
    private String name;
    private String uid;
    private String school;
    private String birthday;
    private String gender;
    private String note;
    private String busid;
    private String parentid;

    // No-argument constructor required for Firestore deserialization
    public Child(String name, String uid) {
    }

    public Child( ) {
    }

    // Constructor with arguments
    public Child(String name, String school, String birthday, String gender, String note, String busid, String uid, String parentid) {
        this.name = name;
        this.school = school;
        this.birthday = birthday;
        this.gender = gender;
        this.note = note;
        this.busid = busid;
        this.uid = uid;
        this.parentid = parentid;
    }

    // Getters and setters (add as needed)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    public String getSchool() {
        return school;
    }
    public void setSchool(String school) {
        this.school = school;
    }
    public String getBusid() {
        return busid;
    }
    public void setBusid(String busid) {
        this.busid = busid;
    }

    public String getUid() {
        return uid;
    }
    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getParentid() {
        return parentid;
    }
    public void setParentid(String parentid) {
        this.parentid = parentid;
    }

    public String getGender() {
        return gender;
    }
    public void setGender(String gender) {
        this.gender = gender;
    }

}

