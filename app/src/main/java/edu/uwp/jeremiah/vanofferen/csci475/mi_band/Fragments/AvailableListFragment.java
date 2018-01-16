package edu.uwp.jeremiah.vanofferen.csci475.mi_band.Fragments;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import edu.uwp.jeremiah.vanofferen.csci475.mi_band.Adaptors.AvailableRecycleAdapter;
import edu.uwp.jeremiah.vanofferen.csci475.mi_band.Interfaces.FragmentItemSelectedListener;
import edu.uwp.jeremiah.vanofferen.csci475.mi_band.Connections.MiBandManager;
import edu.uwp.jeremiah.vanofferen.csci475.mi_band.Models.AvailableMiBand;
import edu.uwp.jeremiah.vanofferen.csci475.mi_band.Models.Member;
import edu.uwp.jeremiah.vanofferen.csci475.mi_band.R;

/**
 * Created by maxrogers on 10/23/16.
 */

public class AvailableListFragment extends Fragment implements AvailableRecycleAdapter.AvailableClickListener, FragmentItemSelectedListener {

    private static final String TAG = "AvalableListFragment";
    private int mColumnCount = 1;
    private FragmentItemSelectedListener mListener;
    private MiBandManager mMiBandManager;
    private ArrayList<AvailableMiBand> mAvailableMiBands;
    private AvailableRecycleAdapter mAdaptor;
    private Handler handler;


    public AvailableListFragment(){

    }

    public static AvailableListFragment newInstance(){
        AvailableListFragment fragment = new AvailableListFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        mMiBandManager = MiBandManager.getInstance(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){

        View view = inflater.inflate(R.layout.fragment_available_list, container, false);
        mAvailableMiBands = mMiBandManager.getAvailableDevices();

        //set adapter
        if (view instanceof RecyclerView){
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            if (mColumnCount<=1){
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
            } else{
                recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
            }
            mAdaptor = new AvailableRecycleAdapter(mAvailableMiBands, this);
            recyclerView.setAdapter(mAdaptor);
        }

        handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == 1) {
                    mAvailableMiBands = mMiBandManager.getAvailableDevices();
                    mAdaptor.replaceAvailableMiBands(mAvailableMiBands);
                }
                super.handleMessage(msg);
            }
        };

        mMiBandManager.addHandler(handler);
        return view;
    }

    @Override
    public void onAttach(Context context){
        super.onAttach(context);
        if(context instanceof  FragmentItemSelectedListener){
            mListener = (FragmentItemSelectedListener) context;
        }else{
            throw new RuntimeException(context.toString()
                        + "must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach(){
        super.onDetach();
        mListener = null;
        mMiBandManager.removeHandler(handler);
        handler = null;
    }

    @Override
    public void onItemClick(int position, View v) {
        if (position >= 0) {
            try {
                mListener.onDeviceSelectedFragmentInteraction(mAvailableMiBands.get(position).getMiband().getAddress());
            } catch (Exception e) {
                Log.e(TAG, "Item Clicked", e);
            }
        }
    }

    @Override
    public void onMemberSelectedFragmentInteraction(Member member) {
    }

    @Override
    public void onMemberDoubleClickedFragmentInteraction(Member member) {

    }

    @Override
    public void onDeviceSelectedFragmentInteraction(String deviceID) {
        mListener.onDeviceSelectedFragmentInteraction(deviceID);
    }

    @Override
    public void onMemberSwipedFragmentInteraction(Member member) {

    }
}
