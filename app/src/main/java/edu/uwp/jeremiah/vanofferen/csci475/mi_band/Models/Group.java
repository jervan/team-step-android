package edu.uwp.jeremiah.vanofferen.csci475.mi_band.Models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * Created by Jeremiah on 10/21/16.
 */

public class Group implements Parcelable {

    private int id;
    private String name;
    private Leader leader;
    private ArrayList<Member> members;

    public Group() {
        this.members = new ArrayList<>();
    }

    public Group(String name) {
        this.name = name;
        this.members = new ArrayList<>();
    }

    public Group(int id, ArrayList<Member> members) {
        this.id = id;
        this.members = members;
    }


    protected Group(Parcel in) {
        id = in.readInt();
        name = in.readString();
        leader = in.readParcelable(Leader.class.getClassLoader());
        members = in.createTypedArrayList(Member.CREATOR);
    }

    public static final Creator<Group> CREATOR = new Creator<Group>() {
        @Override
        public Group createFromParcel(Parcel in) {
            return new Group(in);
        }

        @Override
        public Group[] newArray(int size) {
            return new Group[size];
        }
    };

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Leader getLeader() {
        return leader;
    }

    public void setLeader(Leader leader) {
        this.leader = leader;
    }

    public ArrayList<Member> getMembers() {

        return members;
    }

    public void setMembers(ArrayList<Member> members) {
        this.members = members;
    }

    public void addMember(Member member) {
        this.members.add(member);
    }

    public void removeMember(Member member) {
        members.remove(member);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(id);
        parcel.writeString(name);
        parcel.writeParcelable(leader, i);
        parcel.writeTypedList(members);
    }
}
