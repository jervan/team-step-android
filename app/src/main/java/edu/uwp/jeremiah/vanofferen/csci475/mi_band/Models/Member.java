package edu.uwp.jeremiah.vanofferen.csci475.mi_band.Models;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Jeremiah on 10/21/16.
 */

public class Member implements Parcelable {
    private int id;
    private int groupNumber;
    private String deviceAddress;
    private String firstName;
    private String lastName;
    private int gender;
    private int age;
    private int heightInInches; //full inches
    private int weightInPounds;
    private int lastKnownStepCount;
    private String deviceStatus;
    private int batteryLevel;

    public Member() {
        this.deviceAddress = "";
        this.deviceStatus = "Not Available";
        this.firstName = "";
        this.lastName = "";
    }

    public Member(String deviceAddress, String firstName, String lastName, int gender, int age, int heightInInches, int weightInPounds) {
        this.deviceAddress = deviceAddress;
        this.firstName = firstName;
        this.lastName = lastName;
        this.gender = gender;
        this.age = age;
        this.heightInInches = heightInInches;
        this.weightInPounds = weightInPounds;
        this.deviceStatus = "Not Available";
    }

    protected Member(Parcel in) {
        id = in.readInt();
        groupNumber = in.readInt();
        deviceAddress = in.readString();
        firstName = in.readString();
        lastName = in.readString();
        gender = in.readInt();
        age = in.readInt();
        heightInInches = in.readInt();
        weightInPounds = in.readInt();
        lastKnownStepCount = in.readInt();
        deviceStatus = in.readString();
        batteryLevel = in.readInt();
    }

    public static final Creator<Member> CREATOR = new Creator<Member>() {
        @Override
        public Member createFromParcel(Parcel in) {
            return new Member(in);
        }

        @Override
        public Member[] newArray(int size) {
            return new Member[size];
        }
    };

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getGroupNumber() {
        return groupNumber;
    }

    public void setGroupNumber(int groupNumber) {
        this.groupNumber = groupNumber;
    }

    public String getDeviceAddress() {
        return deviceAddress;
    }

    public void setDeviceAddress(String deviceAddress) {
        this.deviceAddress = deviceAddress;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public int getGender() {
        return gender;
    }

    public void setGender(int gender) {
        this.gender = gender;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public double getHeightInCentimeters() {
        return this.heightInInches * 2.54;
    }

    public double getWeightInKilograms() {
        return this.weightInPounds * 0.453592;
    }


    public int getHeightInInches() {
        return heightInInches;
    }

    public void setHeightInInches(int heightInInches) {
        this.heightInInches = heightInInches;
    }

    public int getWeightInPounds() {
        return weightInPounds;
    }

    public void setWeightInPounds(int weightInPounds) {
        this.weightInPounds = weightInPounds;
    }

    public int getLastKnownStepCount() {
        return lastKnownStepCount;
    }

    public void setLastKnownStepCount(int lastKnownStepCount) {
        this.lastKnownStepCount = lastKnownStepCount;
    }

    public String getDeviceStatus() {
        return deviceStatus;
    }

    public void setDeviceStatus(String deviceStatus) {
        this.deviceStatus = deviceStatus;
    }

    public int getBatteryLevel() {
        return batteryLevel;
    }

    public void setBatteryLevel(int batteryLevel) {
        this.batteryLevel = batteryLevel;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(id);
        parcel.writeInt(groupNumber);
        parcel.writeString(deviceAddress);
        parcel.writeString(firstName);
        parcel.writeString(lastName);
        parcel.writeInt(gender);
        parcel.writeInt(age);
        parcel.writeInt(heightInInches);
        parcel.writeInt(weightInPounds);
        parcel.writeInt(lastKnownStepCount);
        parcel.writeString(deviceStatus);
        parcel.writeInt(batteryLevel);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        if (!(o instanceof Member)) return false;

        Member member = (Member) o;
        return (this.getId() == member.getId());

    }

    @Override
    public int hashCode() {
        int result = getId();
        result = 31 * result + getGroupNumber();
        result = 31 * result + (getDeviceAddress() != null ? getDeviceAddress().hashCode() : 0);
        result = 31 * result + (getFirstName() != null ? getFirstName().hashCode() : 0);
        result = 31 * result + (getLastName() != null ? getLastName().hashCode() : 0);
        result = 31 * result + getGender();
        return result;
    }

    @Override
    public String toString() {
        return "Member{" +
                "id=" + id +
                ", groupNumber=" + groupNumber +
                ", deviceAddress='" + deviceAddress + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", gender=" + gender +
                ", age=" + age +
                ", heightInInches=" + heightInInches +
                ", weightInPounds=" + weightInPounds +
                ", lastKnownStepCount=" + lastKnownStepCount +
                ", deviceStatus='" + deviceStatus + '\'' +
                ", batteryLevel=" + batteryLevel +
                '}';
    }
}
