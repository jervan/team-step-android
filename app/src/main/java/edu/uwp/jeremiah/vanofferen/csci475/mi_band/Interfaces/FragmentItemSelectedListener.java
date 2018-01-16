package edu.uwp.jeremiah.vanofferen.csci475.mi_band.Interfaces;

import android.support.annotation.Nullable;
import android.view.View;

import edu.uwp.jeremiah.vanofferen.csci475.mi_band.Models.Member;

/**
 * Created by Jeremiah on 10/22/16.
 */

public interface FragmentItemSelectedListener {
    @Nullable
    void onMemberSelectedFragmentInteraction(Member member);

    void onMemberDoubleClickedFragmentInteraction(Member member);

    void onDeviceSelectedFragmentInteraction(String deviceAddress);

    void onMemberSwipedFragmentInteraction(Member member);

}
