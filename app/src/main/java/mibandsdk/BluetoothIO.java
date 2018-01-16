package mibandsdk;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.util.Log;


import java.util.HashMap;
import java.util.UUID;

import mibandsdk.listeners.NotifyListener;
import mibandsdk.model.Profile;

import static mibandsdk.model.Profile.UUID_DESCRIPTOR_UPDATE_NOTIFICATION;

class BluetoothIO extends BluetoothGattCallback {
    private static final String TAG = "BluetoothIO";
    BluetoothGatt gatt;
    ActionCallback currentCallback;
    RequestType mRequestType = RequestType.UNKNOWN;

    HashMap<UUID, NotifyListener> notifyListeners = new HashMap<UUID, NotifyListener>();
    NotifyListener disconnectedListener = null;

    public void connect(final Context context, BluetoothDevice device, final ActionCallback callback) {
        mRequestType = RequestType.CONNECT;
        BluetoothIO.this.currentCallback = callback;
        device.connectGatt(context, false, BluetoothIO.this);
    }

    public void disconnect() {
        if (gatt != null) {
            gatt.close();
        }
        notifyListeners = null;
        disconnectedListener = null;
    }

    public void setDisconnectedListener(NotifyListener disconnectedListener) {
        this.disconnectedListener = disconnectedListener;
    }

    public BluetoothDevice getDevice() {
        if (null == gatt) {
            Log.e(TAG, "connect to miband first");
            return null;
        }
        return gatt.getDevice();
    }

    public void writeAndRead(final UUID uuid, byte[] valueToWrite, final ActionCallback callback, final RequestType requestType) {
        ActionCallback readCallback = new ActionCallback() {

            @Override
            public void onSuccess(Object characteristic, String deviceAddress, RequestType serviceType) {
                BluetoothIO.this.readCharacteristic(uuid, callback, requestType);
            }

            @Override
            public void onFail(int errorCode, String msg, RequestType requestType) {
                callback.onFail(errorCode, msg, requestType);
            }
        };
        this.writeCharacteristic(uuid, valueToWrite, readCallback, requestType);
    }

    public void writeCharacteristic(UUID characteristicUUID, byte[] value, ActionCallback callback, RequestType requestType) {
        writeCharacteristic(Profile.UUID_SERVICE_MILI, characteristicUUID, value, callback, requestType);
    }

    public void writeCharacteristic(UUID serviceUUID, UUID characteristicUUID, byte[] value, ActionCallback callback, RequestType requestType) {
        mRequestType = requestType;
        Log.d(TAG, requestType.toString());
        try {
            if (null == gatt) {
                Log.e(TAG, "connect to miband first");
                throw new Exception("connect to miband first");
            }
            this.currentCallback = callback;
            BluetoothGattCharacteristic chara;
            try {
                chara = gatt.getService(serviceUUID).getCharacteristic(characteristicUUID);
            } catch (NullPointerException e){
                this.onFail(-1, "BluetoothGattCharacteristic " + characteristicUUID + " is not exsit");
                return;
            }
            chara.setValue(value);
            if (false == this.gatt.writeCharacteristic(chara)) {
                this.onFail(-1, "gatt.writeCharacteristic() return false");
            }
        } catch (Throwable tr) {
            Log.e(TAG, "writeCharacteristic", tr);
            this.onFail(-1, tr.getMessage());
        }
    }

    public void readCharacteristic(UUID serviceUUID, UUID uuid, ActionCallback callback, RequestType requestType) {
        this.mRequestType = requestType;
        try {
            if (null == gatt) {
                Log.e(TAG, "connect to miband first");
                throw new Exception("connect to miband first");
            }
            this.currentCallback = callback;
            BluetoothGattCharacteristic chara = gatt.getService(serviceUUID).getCharacteristic(uuid);
            if (null == chara) {
                this.onFail(-1, "BluetoothGattCharacteristic " + uuid + " is not exsit");
                return;
            }
            if (false == this.gatt.readCharacteristic(chara)) {
                this.onFail(-1, "gatt.readCharacteristic() return false");
            }
        } catch (Throwable tr) {
            Log.e(TAG, "readCharacteristic", tr);
            this.onFail(-1, tr.getMessage());
        }
    }

    public void readCharacteristic(UUID uuid, ActionCallback callback, RequestType requestType) {
        this.readCharacteristic(Profile.UUID_SERVICE_MILI, uuid, callback, requestType);
    }

    public void readRssi(ActionCallback callback) {
        try {
            if (null == gatt) {
                Log.e(TAG, "connect to miband first");
                throw new Exception("connect to miband first");
            }
            this.currentCallback = callback;
            this.gatt.readRemoteRssi();
        } catch (Throwable tr) {
            Log.e(TAG, "readRssi", tr);
            this.onFail(-1, tr.getMessage());
        }

    }

    public void setNotifyListener(UUID serviceUUID, UUID characteristicId, NotifyListener listener, ActionCallback callback, RequestType requestType) {
        if (null == gatt) {
            Log.e(TAG, "connect to miband first");
            return;
        }
        if (this.notifyListeners == null) {
            gatt.close();
            callback.onFail(-1, "MiBand Disconnected", requestType);
            return;
        }
        BluetoothGattCharacteristic chara;
        try {
            chara = gatt.getService(serviceUUID).getCharacteristic(characteristicId);
        }
        catch (NullPointerException e) {
            Log.e(TAG, "characteristicId " + characteristicId.toString() + " not found in service " + serviceUUID.toString());
            return;
        }

        this.currentCallback = callback;
        this.mRequestType = requestType;

        this.gatt.setCharacteristicNotification(chara, true);
        BluetoothGattDescriptor descriptor = chara.getDescriptor(UUID_DESCRIPTOR_UPDATE_NOTIFICATION);
        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        this.gatt.writeDescriptor(descriptor);
        this.notifyListeners.put(characteristicId, listener);
    }

    @Override
    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
        super.onConnectionStateChange(gatt, status, newState);

        if (newState == BluetoothProfile.STATE_CONNECTED) {
            gatt.discoverServices();
        } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
            if (this.disconnectedListener != null)
                this.disconnectedListener.onNotify(null, RequestType.DISCONNECT, getDevice().getAddress());
            gatt.close();
        }
    }

    @Override
    public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        super.onCharacteristicRead(gatt, characteristic, status);
        if (BluetoothGatt.GATT_SUCCESS == status) {
            this.onSuccess(characteristic);
        } else {
            this.onFail(status, "onCharacteristicRead fail");
        }
    }

    @Override
    public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        super.onCharacteristicWrite(gatt, characteristic, status);
        if (BluetoothGatt.GATT_SUCCESS == status) {
            this.onSuccess(characteristic);
        } else {
            this.onFail(status, "onCharacteristicWrite fail");
        }
    }

    @Override
    public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
        super.onDescriptorWrite(gatt, descriptor, status);
        if (BluetoothGatt.GATT_SUCCESS == status) {
            this.onSuccess(descriptor);
        } else {
            this.onFail(status, "onDescriptorWrite fail");
        }
    }

    @Override
    public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
        super.onReadRemoteRssi(gatt, rssi, status);
        if (BluetoothGatt.GATT_SUCCESS == status) {
            this.onSuccess(rssi);
        } else {
            this.onFail(status, "onCharacteristicRead fail");
        }
    }

    @Override
    public void onServicesDiscovered(BluetoothGatt gatt, int status) {
        super.onServicesDiscovered(gatt, status);
        if (status == BluetoothGatt.GATT_SUCCESS) {
            this.gatt = gatt;
            this.onSuccess(null);
        } else {
            this.onFail(status, "onServicesDiscovered fail");
        }
    }

    @Override
    public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        super.onCharacteristicChanged(gatt, characteristic);
        if (this.notifyListeners != null && this.notifyListeners.containsKey(characteristic.getUuid())) {
            UUID uuid = characteristic.getUuid();
            RequestType requestType;
            if (uuid.equals(Profile.UUID_CHAR_NOTIFICATION)) {
                requestType = RequestType.NORMAL;

            } else if (uuid.equals(Profile.UUID_NOTIFICATION_HEART_RATE)) {
                requestType = RequestType.HEART_RATE_LISTENER;

            } else if (uuid.equals(Profile.UUID_CHAR_REALTIME_STEPS)) {
                requestType = RequestType.REALTIME_STEPS_LISTENER;

            } else if (uuid.equals(Profile.UUID_CHAR_ACTIVITY)) {
                requestType = RequestType.ACTIVITY_LISTENER;

            } else {
                Log.w(TAG, "Characteristic Changed Listener Unknown UUID: " + uuid.toString());
                requestType = RequestType.UNKNOWN;
            }

            this.notifyListeners.get(uuid).onNotify(characteristic.getValue(), requestType, getDevice().getAddress());
        }
    }

    private void onSuccess(Object data) {
        if (this.currentCallback != null) {
            ActionCallback callback = this.currentCallback;
            RequestType requestType = this.mRequestType;
            this.currentCallback = null;
            this.mRequestType = RequestType.UNKNOWN;
            callback.onSuccess(data, getDevice().getAddress(), requestType);
        }
    }

    private void onFail(int errorCode, String msg) {
        if (this.currentCallback != null) {
            ActionCallback callback = this.currentCallback;
            RequestType requestType = this.mRequestType;
            this.currentCallback = null;
            this.mRequestType = RequestType.UNKNOWN;
            callback.onFail(errorCode, msg, requestType);
        }
    }

}
