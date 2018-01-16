package edu.uwp.jeremiah.vanofferen.csci475.mi_band.Models;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jeremiah on 11/13/16.
 */

public class ActivityData {

    private int memberId;
    private int leaderId;
    private boolean isLeader;
    private List<mibandsdk.model.ActivityData> activityDataPerMinute;

    public ActivityData(boolean isLeader, int id, List<mibandsdk.model.ActivityData> activityDataPerMinute) {
        this.isLeader = isLeader;
        this.activityDataPerMinute = activityDataPerMinute;
        if (isLeader) {
            this.leaderId = id;
        } else {
            this.memberId = id;
        }

    }

    public int getMemberId() {
        return memberId;
    }

    public void setMemberId(int memberId) {
        this.memberId = memberId;
    }

    public int getLeaderId() {
        return leaderId;
    }

    public void setLeaderId(int leaderId) {
        this.leaderId = leaderId;
    }

    public boolean isLeader() {
        return isLeader;
    }

    public void setLeader(boolean leader) {
        isLeader = leader;
    }

    public List<mibandsdk.model.ActivityData> getActivityDataPerMinute() {
        return activityDataPerMinute;
    }

    public void setActivityDataPerMinute(ArrayList<mibandsdk.model.ActivityData> activityDataPerMinute) {
        this.activityDataPerMinute = activityDataPerMinute;
    }

    @Override
    public String toString() {
        return "ActivityData{" +
                "memberId=" + memberId +
                ", leaderId=" + leaderId +
                ", activityDataPerMinute=" + activityDataPerMinute +
                '}';
    }
}
