package edu.uwp.jeremiah.vanofferen.csci475.mi_band.Models;

import android.bluetooth.BluetoothDevice;

/**
 * Created by Jeremiah on 10/21/16.
 */

public class AvailableMiBand implements Comparable<AvailableMiBand> {

    private BluetoothDevice miband;
    private int rssi;
    private long timeLastSeenInMilliSeconds;


    public AvailableMiBand(BluetoothDevice miband, int rssi, long timeLastSeenInMilliSeconds) {
        this.miband = miband;
        this.rssi = rssi;
        this.timeLastSeenInMilliSeconds = timeLastSeenInMilliSeconds;
    }

    public BluetoothDevice getMiband() {
        return miband;
    }

    public void setMiband(BluetoothDevice miband) {
        this.miband = miband;
    }

    public int getRssi() {
        return rssi;
    }

    public void setRssi(int rssi) {
        this.rssi = rssi;
    }

    public long getTimeLastSeenInMilliSeconds() {
        return timeLastSeenInMilliSeconds;
    }

    public void setTimeLastSeenInMilliSeconds(long timeLastSeenInMilliSeconds) {
        this.timeLastSeenInMilliSeconds = timeLastSeenInMilliSeconds;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AvailableMiBand)) return false;

        AvailableMiBand that = (AvailableMiBand) o;

        return getMiband().getAddress().equals(that.getMiband().getAddress());

    }

    @Override
    public int hashCode() {
        return getMiband().getAddress().hashCode();
    }

    @Override
    public int compareTo(AvailableMiBand o) {
        return Integer.valueOf(o.getRssi()).compareTo(this.getRssi());
    }

    @Override
    public String toString() {
        return "AvailableMiBand{" +
                "miband=" + miband +
                ", rssi=" + rssi +
                ", timeLastSeenInMilliSeconds=" + timeLastSeenInMilliSeconds +
                '}';
    }
}
