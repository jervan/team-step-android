package edu.uwp.jeremiah.vanofferen.csci475.mi_band.Adaptors;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Debug;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.view.ContextThemeWrapper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Collections;

import edu.uwp.jeremiah.vanofferen.csci475.mi_band.Activities.HomeActivity;
import edu.uwp.jeremiah.vanofferen.csci475.mi_band.Interfaces.ItemTouchHelperAdapter;
import edu.uwp.jeremiah.vanofferen.csci475.mi_band.Models.Group;
import edu.uwp.jeremiah.vanofferen.csci475.mi_band.Models.Member;
import edu.uwp.jeremiah.vanofferen.csci475.mi_band.R;

/**
 *The RecycleAdapter is used in conjunction with the recycleview. It handles real time data collection
 * and has a view holder to display multiple cardviews. It includes a onclick listener to activate
 * different features.
 */

public class MemberRecycleAdapter extends RecyclerView.Adapter<MemberRecycleAdapter.StudentsHolder> implements ItemTouchHelperAdapter {
    private static String LOG_TAG = "RecycleAdapter";
    private Group group;
    private static MemberClickListener sMemberClickListener;
    protected static Context mContext;


    //==============================================

    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
                Collections.swap(group.getMembers(), i, i + 1);
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                Collections.swap(group.getMembers(), i, i - 1);
            }
        }
        notifyItemMoved(fromPosition, toPosition);
        return true;
    }

    @Override
    public void onItemDismiss(final int position) {
        if(position==0) {
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            builder.setTitle("Cannot Delete Leader!");
            builder.setCancelable(false);
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            });
            builder.show();
        }
        else {
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            builder.setTitle("Delete Member");
            builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    sMemberClickListener.onSwipe(position);
                    group.getMembers().remove(position - 1);
                    notifyItemRemoved(position);
                    Toast.makeText(mContext, "Member Deleted", Toast.LENGTH_SHORT).show();
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            });
            builder.show();
        }
    }

    //==============================================

    //custom view holder (student_cardview)  with onclick listener
    public static class StudentsHolder extends RecyclerView.ViewHolder
            implements GestureDetector.OnGestureListener,
            GestureDetector.OnDoubleTapListener,
            View.OnTouchListener {

        private static final String DEBUG_TAG = "Gestures";
        private GestureDetectorCompat mDetector;
        private View mCardView;

        TextView name;
        TextView status;
        TextView steps;
        TextView battery;

        public StudentsHolder(View cardView) {
            super(cardView);
            mCardView = cardView;
            name = (TextView) cardView.findViewById(R.id.device);
            status = (TextView) cardView.findViewById(R.id.status);
            steps = (TextView) cardView.findViewById(R.id.steps);
            battery = (TextView) cardView.findViewById(R.id.battery);

            mDetector = new GestureDetectorCompat(mContext, this);
            // Set the gesture detector as the double tap
            // listener.
            mDetector.setOnDoubleTapListener(this);

            cardView.setOnTouchListener(this);

        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            this.mDetector.onTouchEvent(event);
            return true;
        }


        @Override
        public boolean onDown(MotionEvent event) {
            Log.d(DEBUG_TAG, "onDown: " + event.toString());
            return true;
        }

        @Override
        public boolean onFling(MotionEvent event1, MotionEvent event2,
                               float velocityX, float velocityY) {
            Log.d(DEBUG_TAG, "onFling: " + event1.toString() + event2.toString());
            return true;
        }

        @Override
        public void onLongPress(MotionEvent event) {
            Log.d(DEBUG_TAG, "onLongPress: " + event.toString());
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
                                float distanceY) {
            Log.d(DEBUG_TAG, "onScroll: " + e1.toString() + e2.toString());
            return true;
        }

        @Override
        public void onShowPress(MotionEvent event) {
            Log.d(DEBUG_TAG, "onShowPress: " + event.toString());
        }

        @Override
        public boolean onSingleTapUp(MotionEvent event) {
            Log.d(DEBUG_TAG, "onSingleTapUp: " + event.toString());
            return true;
        }

        @Override
        public boolean onDoubleTap(MotionEvent event) {
            sMemberClickListener.onItemDoubleClick(getAdapterPosition(), mCardView);
            Log.d(DEBUG_TAG, "onDoubleTap: " + event.toString());
            return true;
        }

        @Override
        public boolean onDoubleTapEvent(MotionEvent event) {
            Log.d(DEBUG_TAG, "onDoubleTapEvent: " + event.toString());
            return true;
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent event) {
            sMemberClickListener.onItemClick(getAdapterPosition(), mCardView);
            Log.d(DEBUG_TAG, "onSingleTapConfirmed: " + event.toString());
            return true;
        }
    }

    public void setOnItemClickListener(MemberClickListener memberClickListener) {
        this.sMemberClickListener = memberClickListener;
    }

    public MemberRecycleAdapter(Group group, MemberClickListener memberClickListener, Context context) {
        setOnItemClickListener(memberClickListener);
        this.group = group;
        mContext = context;
    }

    //inflate/display the student_cardview that is connected
    @Override
    public StudentsHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.member_cardview, parent, false);

        StudentsHolder studentHolder = new StudentsHolder(view);
        return studentHolder;
    }

    //internally updates the student_cardview with corresponding data at position
    @Override
    public void onBindViewHolder(StudentsHolder holder, int position) {
        String name;
        String status;
        String steps;
        String battery;
        if (position == 0) {
            if (group.getLeader().getFirstName() == null || group.getLeader().getFirstName().equals("")) {
                name = "Leader";
            } else {
                name = group.getLeader().getFirstName() + " " + group.getLeader().getLastName();
            }
            status = group.getLeader().getDeviceStatus();
            steps = "" + group.getLeader().getLastKnownStepCount();
            battery = "" + group.getLeader().getBatteryLevel() + "%";
        } else {
            name = group.getMembers().get(position - 1).getFirstName() + " " + group.getMembers().get(position - 1).getLastName();
            status = group.getMembers().get(position - 1).getDeviceStatus();
            steps = "" + group.getMembers().get(position - 1).getLastKnownStepCount();
            battery = "" + group.getMembers().get(position - 1).getBatteryLevel() + "%";
        }
        holder.name.setText(name);
        holder.status.setText(status);
        holder.steps.setText(steps);
        holder.battery.setText(battery);
    }

    public void additem(Member member, int index) {
        group.getMembers().add(index - 1, member);
        notifyItemInserted(index);
    }

    public void deleteItem(int index) {
        group.getMembers().remove(index - 1);
        notifyItemRemoved(index);
    }

    public void replaceGroup(Group group) {
        this.group = group;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return group.getMembers().size() + 1;
    }

    public interface MemberClickListener {
        void onItemClick(int position, View v);

        void onItemDoubleClick(int position, View v);

        void onSwipe(int position);
    }

}