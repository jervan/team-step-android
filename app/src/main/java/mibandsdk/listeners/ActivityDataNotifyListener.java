package mibandsdk.listeners;

import mibandsdk.RequestType;
import mibandsdk.model.ActivityDataSample;

/**
 * Created by Jeremiah on 11/13/16.
 */

public interface ActivityDataNotifyListener {
    public void onActivityDataNotify(ActivityDataSample dataSample, RequestType requestType, String deviceAddress);
}
