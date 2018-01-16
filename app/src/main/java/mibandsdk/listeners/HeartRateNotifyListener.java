package mibandsdk.listeners;

public interface HeartRateNotifyListener {
    public void onHeartRateNotify(String deviceAddress, int heartRate);
}
