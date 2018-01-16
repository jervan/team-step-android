package edu.uwp.jeremiah.vanofferen.csci475.mi_band.Fragments;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import edu.uwp.jeremiah.vanofferen.csci475.mi_band.Activities.HomeActivity;
import edu.uwp.jeremiah.vanofferen.csci475.mi_band.Adaptors.SimpleItemTouchHelperCallback;
import edu.uwp.jeremiah.vanofferen.csci475.mi_band.Interfaces.FragmentItemSelectedListener;
import edu.uwp.jeremiah.vanofferen.csci475.mi_band.Connections.MiBandManager;
import edu.uwp.jeremiah.vanofferen.csci475.mi_band.Models.Group;
import edu.uwp.jeremiah.vanofferen.csci475.mi_band.R;
import edu.uwp.jeremiah.vanofferen.csci475.mi_band.Adaptors.MemberRecycleAdapter;

public class MemberListFragment extends Fragment implements MemberRecycleAdapter.MemberClickListener {

    private static final String TAG = "MemberListFragment";
    private int mColumnCount = 1;
    private FragmentItemSelectedListener mListener;
    private MiBandManager mMiBandManager;
    private Group group;
    private RecyclerView recyclerView;
    private MemberRecycleAdapter mAdaptor;
    private Handler handler;


    
    public MemberListFragment() {

    }

    public static MemberListFragment newInstance() {
        MemberListFragment fragment = new MemberListFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mMiBandManager = MiBandManager.getInstance(getActivity());
        group = mMiBandManager.getGroup();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_member_list, container, false);



        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
             recyclerView = (RecyclerView) view;
            if (mColumnCount <= 1) {
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
            } else {
                recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
            }
            mAdaptor = new MemberRecycleAdapter(group, this, getActivity());
            recyclerView.setAdapter(mAdaptor);

            //=================================================================
            ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(mAdaptor);
            ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
            touchHelper.attachToRecyclerView(recyclerView);
            //=================================================================
        }

        handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == 1)
                    mAdaptor.notifyDataSetChanged();
                super.handleMessage(msg);
            }
        };

        mMiBandManager.addHandler(handler);
        return view;
    }


    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof FragmentItemSelectedListener) {
            mListener = (FragmentItemSelectedListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
        mMiBandManager.removeHandler(handler);
        handler = null;
    }

    @Override
    public void onItemClick(int position, View v) {
        Log.d("clicked", "member");
        if (position < 0) {
            Log.e("Member Adaptor", " index out of bound value = " + position);
            return;
        }
        if (position == 0) {
            mListener.onMemberSelectedFragmentInteraction(group.getLeader());
        } else {
            try {
                mListener.onMemberSelectedFragmentInteraction(group.getMembers().get(position - 1));
            } catch (Exception e) {
                Log.e(TAG, "Item Clicked", e);
            }
        }
    }

    @Override
    public void onItemDoubleClick(int position, View v) {
        Log.d("double clicked", "member");
        if (position < 0) {
            Log.e("Member Adaptor", " index out of bound value = " + position);
            return;
        }
        if (position == 0) {
            mListener.onMemberDoubleClickedFragmentInteraction(group.getLeader());
        } else {
            try {
                mListener.onMemberDoubleClickedFragmentInteraction(group.getMembers().get(position - 1));
            } catch (Exception e) {
                Log.e(TAG, "Item Double Clicked", e);
            }
        }
    }

    @Override
    public void onSwipe(int position) {
        Log.d("swiped", "member");
        if (position < 0) {
            Log.e("Member Adaptor", " index out of bound value = " + position);
        } else {
            try {
                mListener.onMemberSwipedFragmentInteraction(group.getMembers().get(position-1));
            } catch (Exception e) {
                Log.e(TAG, "Item Swiped", e);
            }
        }
    }

    public void updateGroup (Group group) {
        this.group = group;
        mAdaptor.replaceGroup(group);
    }

}
