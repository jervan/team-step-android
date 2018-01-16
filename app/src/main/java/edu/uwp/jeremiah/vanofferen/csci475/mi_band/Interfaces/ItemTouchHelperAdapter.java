package edu.uwp.jeremiah.vanofferen.csci475.mi_band.Interfaces;

/**
 * Created by maxrogers on 12/3/16.
 */

public interface ItemTouchHelperAdapter {
    boolean onItemMove(int fromPosition, int toPosition);

    void onItemDismiss(int position);
}
