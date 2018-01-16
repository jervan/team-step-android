package mibandsdk.listeners;

import mibandsdk.RequestType;
import mibandsdk.model.ActivityDataSample;

public interface NotifyListener {
    public void onNotify(byte[] data, RequestType requestType, String deviceAddress);
}
