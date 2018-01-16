package edu.uwp.jeremiah.vanofferen.csci475.mi_band.Adaptors;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import edu.uwp.jeremiah.vanofferen.csci475.mi_band.Models.AvailableMiBand;
import edu.uwp.jeremiah.vanofferen.csci475.mi_band.R;

/**
 * Available Adapter used with recycle view. Has viewholder to display multiple cardviews with
 * available devices information. onclick listener to select desired device.
 */

public class AvailableRecycleAdapter extends RecyclerView.Adapter<AvailableRecycleAdapter.AvailableHolder>{
    private static String LOG_TAG = "AvRecycleAdapter";
    private ArrayList<AvailableMiBand> mAvailableMiBands;
    private static AvailableClickListener availableClickListener;


    //custom view holder for available_cardview with onclick listenere
    public static class AvailableHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        TextView deviceID;
        TextView rssi;

        public AvailableHolder(View cardView){
            super(cardView);
            deviceID = (TextView) cardView.findViewById(R.id.device_address);
            rssi = (TextView) cardView.findViewById(R.id.rssi);
            Log.i(LOG_TAG, "Adding Listener");
            cardView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            availableClickListener.onItemClick(getAdapterPosition(), v);
        }
    }

    public void setOnItemClickListener(AvailableClickListener availableClickListener){
        this.availableClickListener = availableClickListener;
    }

    public AvailableRecycleAdapter(ArrayList<AvailableMiBand> availableMiBands, AvailableClickListener availableClickListener){
        setOnItemClickListener(availableClickListener);
        this.mAvailableMiBands = availableMiBands;
    }

    @Override
    public AvailableHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.available_cardview, parent, false);
        AvailableHolder availableHolder = new AvailableHolder(view);
        return availableHolder;
    }

    @Override
    public void onBindViewHolder(AvailableHolder holder, int position) {


        holder.deviceID.setText(mAvailableMiBands.get(position).getMiband().getAddress());
        holder.rssi.setText("" + mAvailableMiBands.get(position).getRssi());
    }


    public void addItem(AvailableMiBand availableMiBand){
        mAvailableMiBands.add(availableMiBand);
    }

    public void deleteItem(AvailableMiBand availableMiBand){
        mAvailableMiBands.remove(availableMiBand);
    }

    public void replaceAvailableMiBands (ArrayList<AvailableMiBand> availableMiBands) {
        mAvailableMiBands = availableMiBands;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return mAvailableMiBands.size();
    }

    public interface AvailableClickListener{
        public void onItemClick(int position, View v);
    }

}
