package mibandsdk;

public interface ActionCallback {
    public void onSuccess(Object data, String deviceAddress, RequestType requestType);

    public void onFail(int errorCode, String msg, RequestType requestType);
}
