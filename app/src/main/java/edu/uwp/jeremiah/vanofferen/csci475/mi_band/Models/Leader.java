package edu.uwp.jeremiah.vanofferen.csci475.mi_band.Models;


import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Jeremiah on 10/21/16.
 */

public class Leader extends Member implements Parcelable {

    private String username;
    private String password;
    private String email;
    private boolean admin;
    private int defaultGroup;

    public Leader() {
        this.admin = false;
        setDeviceStatus("Not Available");
    }

    public Leader(String username, String password) {
        this.username = username;
        this.password = password;
        this.admin = false;
        setDeviceStatus("Not Available");
    }

    public Leader(String username, String password, String email) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.admin = false;
        setDeviceStatus("Not Available");
    }

    protected Leader(Parcel in) {
        username = in.readString();
        password = in.readString();
        email = in.readString();
        admin = in.readByte() != 0;
        defaultGroup = in.readInt();
        setId(in.readInt());
        setGroupNumber(in.readInt());
        setDeviceAddress(in.readString());
        setFirstName(in.readString());
        setLastName(in.readString());
        setGender(in.readInt());
        setAge(in.readInt());
        setHeightInInches(in.readInt());
        setWeightInPounds(in.readInt());
        setLastKnownStepCount(in.readInt());
        setDeviceStatus(in.readString());
        setBatteryLevel(in.readInt());
    }

    public static final Creator<Leader> CREATOR = new Creator<Leader>() {
        @Override
        public Leader createFromParcel(Parcel in) {
            return new Leader(in);
        }

        @Override
        public Leader[] newArray(int size) {
            return new Leader[size];
        }
    };

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getDefaultGroup() {
        return defaultGroup;
    }

    public void setDefaultGroup(int defaultGroup) {
        this.defaultGroup = defaultGroup;
    }

    public boolean isAdmin() {
        return admin;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }

    @Override
    public String toString() {
        return "Leader{" +
                "id=" + getId() +
                ", defaultGroup=" + getDefaultGroup() +
                ", groupNumber=" + getGroupNumber() +
                ", deviceAddress='" + getDeviceAddress() + '\'' +
                ", firstName='" + getFirstName() + '\'' +
                ", lastName='" + getLastName() + '\'' +
                ", gender=" + getGender() +
                ", age=" + getAge() +
                ", heightInCentimeters=" + getHeightInCentimeters() +
                ", weightInKilograms=" + getWeightInKilograms() +
                ", lastKnownStepCount=" + getLastKnownStepCount() +
                ", deviceStatus='" + getDeviceStatus() + '\'' +
                ", batteryLevel=" + getBatteryLevel() + '\'' +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", email='" + email + '\'' +
                ", admin=" + admin +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(username);
        parcel.writeString(password);
        parcel.writeString(email);
        parcel.writeByte((byte) (admin ? 1 : 0));
        parcel.writeInt(defaultGroup);
        parcel.writeInt(getId());
        parcel.writeInt(getGroupNumber());
        parcel.writeString(getDeviceAddress());
        parcel.writeString(getFirstName());
        parcel.writeString(getLastName());
        parcel.writeInt(getGender());
        parcel.writeInt(getAge());
        parcel.writeInt(getHeightInInches());
        parcel.writeInt(getWeightInPounds());
        parcel.writeInt(getLastKnownStepCount());
        parcel.writeString(getDeviceStatus());
        parcel.writeInt(getBatteryLevel());
    }
}
