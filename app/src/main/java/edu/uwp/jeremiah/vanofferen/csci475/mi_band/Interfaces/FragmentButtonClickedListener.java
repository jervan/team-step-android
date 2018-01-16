package edu.uwp.jeremiah.vanofferen.csci475.mi_band.Interfaces;

import android.support.annotation.Nullable;

import edu.uwp.jeremiah.vanofferen.csci475.mi_band.Models.Leader;
import edu.uwp.jeremiah.vanofferen.csci475.mi_band.Models.Member;

/**
 * Created by Jeremiah on 10/22/16.
 */

public interface FragmentButtonClickedListener {
    void onButtonClickedFragmentInteraction(int buttonID);

    @Nullable
    void onLoginLeader(Leader leader);

    @Nullable
    void onCreateLeader(Leader leader);

    @Nullable
    void onAddMemberInfo(Member member);
}
