package mibandsdk;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.content.Context;
import android.util.Log;


import java.util.Arrays;
import java.util.Calendar;
import java.util.Locale;

import mibandsdk.listeners.ActivityDataNotifyListener;
import mibandsdk.listeners.HeartRateNotifyListener;
import mibandsdk.listeners.NotifyListener;
import mibandsdk.listeners.RealtimeStepsNotifyListener;
import mibandsdk.model.ActivityDataSample;
import mibandsdk.model.BatteryInfo;
import mibandsdk.model.DeviceInfo;
import mibandsdk.model.LedColor;
import mibandsdk.model.Profile;
import mibandsdk.model.Protocol;
import mibandsdk.model.UserInfo;
import mibandsdk.model.VibrationMode;

public class MiBand {

    private static final String TAG = "miband-android";
    private static final int ACTIVITY_METADATA_LENGTH = 11;
    private ActivityDataSample activityBuffer;
    private Context context;
    private BluetoothIO io;

    public MiBand(Context context) {
        this.context = context;
        this.io = new BluetoothIO();
    }

    public static void startScan (ScanCallback callback) {

        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (null == adapter) {
            Log.e(TAG, "BluetoothAdapter is null");
            return;
        }
        BluetoothLeScanner scanner = adapter.getBluetoothLeScanner();
        if (null == scanner) {
            Log.e(TAG, "BluetoothLeScanner is null");
            return;
        }
        scanner.startScan(callback);
    }

    public static void stopScan(ScanCallback callback) {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (null == adapter) {
            Log.e(TAG, "BluetoothAdapter is null");
            return;
        }
        BluetoothLeScanner scanner = adapter.getBluetoothLeScanner();
        if (null == scanner) {
            Log.e(TAG, "BluetoothLeScanner is null");
            return;
        }
        scanner.stopScan(callback);
    }

    /**
     * 连接指定的手环
     *
     * @param callback
     */
    public void connect(BluetoothDevice device, final ActionCallback callback) {
        this.io.connect(context, device, callback);
    }

    public BluetoothGatt getGatt() {
        return this.io.gatt;
    }

    public void disconnect() {
        if (io != null)
            io.disconnect();
    }

    public void setDisconnectedListener(NotifyListener disconnectedListener) {
        this.io.setDisconnectedListener(disconnectedListener);
    }

    public void setLowLatency() {
        this.io.writeCharacteristic(Profile.UUID_SERVICE_MILI, Profile.UUID_CHAR_LE_PARAMS, Protocol.LOW_LATENCY, null, RequestType.LE_PARAMS);
    }

    public void setHighLatency() {
        this.io.writeCharacteristic(Profile.UUID_SERVICE_MILI, Profile.UUID_CHAR_LE_PARAMS, Protocol.HIGH_LATENCY, null, RequestType.LE_PARAMS);
    }


    /**
     * 和手环配对, 实际用途未知, 不配对也可以做其他的操作
     *
     * @return data = null
     */
    public void pair(final ActionCallback callback) {
        ActionCallback ioCallback = new ActionCallback() {

            @Override
            public void onSuccess(Object data, String deviceAddress, RequestType requestType) {
                BluetoothGattCharacteristic characteristic = (BluetoothGattCharacteristic) data;
                Log.d(TAG, "pair result " + Arrays.toString(characteristic.getValue()));
                if (characteristic.getValue().length == 1 && characteristic.getValue()[0] == 2) {
                    callback.onSuccess(null, io.getDevice().getAddress(), RequestType.PAIR);
                } else {
                    callback.onFail(-1, "pair failed", RequestType.PAIR);
                }
            }

            @Override
            public void onFail(int errorCode, String msg, RequestType requestType) {
                callback.onFail(errorCode, msg, requestType);
            }
        };
        this.io.writeAndRead(Profile.UUID_CHAR_PAIR, Protocol.PAIR, ioCallback, RequestType.PAIR);
    }

    public BluetoothDevice getDevice() {
        return this.io.getDevice();
    }

    /**
     * 读取和连接设备的信号强度RSSI值
     *
     * @param callback
     * @return data : int, rssi值
     */
    public void readRssi(ActionCallback callback) {
        this.io.readRssi(callback);
    }

    /**
     * 读取手环电池信息
     *
     * @return {@link BatteryInfo}
     */
    public void getBatteryInfo(final ActionCallback callback) {
        ActionCallback ioCallback = new ActionCallback() {

            @Override
            public void onSuccess(Object data, String deviceAddress, RequestType requestType  ) {
                BluetoothGattCharacteristic characteristic = (BluetoothGattCharacteristic) data;
                Log.d(TAG, "getBatteryInfo result " + Arrays.toString(characteristic.getValue()));
                if (characteristic.getValue().length == 10) {
                    BatteryInfo info = BatteryInfo.fromByteData(characteristic.getValue());
                    callback.onSuccess(info, io.getDevice().getAddress(), RequestType.BATTERY);
                } else {
                    callback.onFail(-1, "result format wrong!", RequestType.BATTERY);
                }
            }

            @Override
            public void onFail(int errorCode, String msg, RequestType requestType) {
                callback.onFail(errorCode, msg, requestType);
            }
        };
        this.io.readCharacteristic(Profile.UUID_CHAR_BATTERY, ioCallback, RequestType.BATTERY);
    }

    public void getDeviceInfo(final ActionCallback callback) {
        ActionCallback ioCallback = new ActionCallback() {

            @Override
            public void onSuccess(Object data, String deviceAddress, RequestType requestType  ) {
                BluetoothGattCharacteristic characteristic = (BluetoothGattCharacteristic) data;
                Log.d(TAG, "getDeviceInfo result " + Arrays.toString(characteristic.getValue()));
                if (characteristic.getValue().length == 20) {
                    DeviceInfo deviceInfo = new DeviceInfo(characteristic.getValue());
                    callback.onSuccess(deviceInfo, io.getDevice().getAddress(), RequestType.DEVICE_INFO);
                } else {
                    callback.onFail(-1, "result format wrong!", RequestType.DEVICE_INFO);
                }
            }

            @Override
            public void onFail(int errorCode, String msg, RequestType requestType) {
                callback.onFail(errorCode, msg, RequestType.DEVICE_INFO);
            }
        };
        this.io.readCharacteristic(Profile.UUID_CHAR_DEVICE_INFO, ioCallback, RequestType.DEVICE_INFO);
    }

    public void getDate(final ActionCallback callback) {
        ActionCallback ioCallback = new ActionCallback() {

            @Override
            public void onSuccess(Object data, String deviceAddress, RequestType requestType  ) {
                BluetoothGattCharacteristic characteristic = (BluetoothGattCharacteristic) data;
                Log.d(TAG, "getDate result " + Arrays.toString(characteristic.getValue()));
                    callback.onSuccess(data, io.getDevice().getAddress(), RequestType.GET_DATE);

            }

            @Override
            public void onFail(int errorCode, String msg, RequestType requestType) {
                callback.onFail(errorCode, msg, requestType);
            }
        };
        this.io.readCharacteristic(Profile.UUID_CHAR_DATA_TIME, ioCallback, RequestType.GET_DATE);
    }

    public void setDate(final ActionCallback callback) {
        ActionCallback ioCallback = new ActionCallback() {
            @Override
            public void onSuccess(Object data, String deviceAddress, RequestType requestType) {
                callback.onSuccess(data, deviceAddress, requestType);
            }

            @Override
            public void onFail(int errorCode, String msg, RequestType requestType) {
                callback.onFail(errorCode, msg, requestType);
            }
        };

        byte[] date = getCurrentTimeBytes();

        this.io.writeCharacteristic(Profile.UUID_CHAR_DATA_TIME, date, ioCallback, RequestType.SET_DATE);
    }

    /**
     * 让手环震动
     */
    public void startVibration(VibrationMode mode) {
        byte[] protocal;
        switch (mode) {
            case VIBRATION_WITH_LED:
                protocal = Protocol.VIBRATION_WITH_LED;
                break;
            case VIBRATION_10_TIMES_WITH_LED:
                protocal = Protocol.VIBRATION_10_TIMES_WITH_LED;
                break;
            case VIBRATION_WITHOUT_LED:
                protocal = Protocol.VIBRATION_WITHOUT_LED;
                break;
            default:
                return;
        }
        this.io.writeCharacteristic(Profile.UUID_SERVICE_VIBRATION, Profile.UUID_CHAR_VIBRATION, protocal, null, RequestType.START_VIBRATE);
    }

    /**
     * 停止以模式Protocol.VIBRATION_10_TIMES_WITH_LED 开始的震动
     */
    public void stopVibration() {
        this.io.writeCharacteristic(Profile.UUID_SERVICE_VIBRATION, Profile.UUID_CHAR_VIBRATION, Protocol.STOP_VIBRATION, null, RequestType.STOP_VIBRATE);
    }

    public void setNormalNotifyListener(NotifyListener listener, ActionCallback callback) {
        this.io.setNotifyListener(Profile.UUID_SERVICE_MILI, Profile.UUID_CHAR_NOTIFICATION, listener, callback, RequestType.NORMAL);
    }

    /**
     * 重力感应器数据通知监听, 设置完之后需要另外使用 {@link MiBand#enableRealtimeStepsNotify} 开启 和
     * {@link MiBand##disableRealtimeStepsNotify} 关闭通知
     *
     * @param listener
     */
    public void setSensorDataNotifyListener(final NotifyListener listener, ActionCallback callback) {
        this.io.setNotifyListener(Profile.UUID_SERVICE_MILI, Profile.UUID_CHAR_SENSOR_DATA, new NotifyListener() {

            @Override
            public void onNotify(byte[] data, RequestType requestType, String deviceAddress) {
                listener.onNotify(data, RequestType.SENSOR_DATA, getDevice().getAddress());
            }
        },callback, RequestType.SENSOR_DATA);
    }

    /**
     * 开启重力感应器数据通知
     */
    public void enableSensorDataNotify(ActionCallback callback) {
        this.io.writeCharacteristic(Profile.UUID_CHAR_CONTROL_POINT, Protocol.ENABLE_SENSOR_DATA_NOTIFY, callback, RequestType.SENSOR_DATA_ENABLE);
    }

    /**
     * 关闭重力感应器数据通知
     */
    public void disableSensorDataNotify() {
        this.io.writeCharacteristic(Profile.UUID_CHAR_CONTROL_POINT, Protocol.DISABLE_SENSOR_DATA_NOTIFY, null, RequestType.SENSOR_DATA);
    }

    public void setActivityNotifyListener (final ActivityDataNotifyListener notifyListener, ActionCallback callback) {
        this.io.setNotifyListener(Profile.UUID_SERVICE_MILI, Profile.UUID_CHAR_ACTIVITY, new NotifyListener() {

            @Override
            public void onNotify(byte[] data, RequestType requestType, String deviceAddress) {
                Log.d(TAG, "Device Address: " + deviceAddress + " DATA: " + Arrays.toString(data));
                if (data.length == ACTIVITY_METADATA_LENGTH ) {
                    handleActivityMetadata(notifyListener, data);
                } else {
                    handleActivityData(data);
                }
            }

        },callback, RequestType.ACTIVITY_LISTENER);
    }

    public void fetchActivityData(final ActionCallback callback) {
        ActionCallback ioCallBack = new ActionCallback() {
            @Override
            public void onSuccess(Object data, String deviceAddress, RequestType requestType) {
                callback.onSuccess(data, deviceAddress, requestType);
                Log.d(TAG, "fetch data received");
            }

            @Override
            public void onFail(int errorCode, String msg, RequestType requestType) {
                callback.onFail(errorCode, msg, requestType);
                Log.e(TAG, "fetch data failure");
                Log.e(TAG, "errorCode: " + errorCode + " msg: " + msg + " request type: " + requestType);
            }
        };
        this.io.writeCharacteristic(Profile.UUID_CHAR_CONTROL_POINT, Protocol.FETCH_DATA, ioCallBack, RequestType.ACTIVITY);
    }

    public void notifyActivityDataRecieved(final ActionCallback callback, byte[] data) {
        this.io.writeCharacteristic(Profile.UUID_CHAR_CONTROL_POINT, data, callback, RequestType.ACTIVITY_DATA_ACK);
    }

    /**
     * 实时步数通知监听器, 设置完之后需要另外使用 {@link MiBand#enableRealtimeStepsNotify} 开启 和
     * {@link MiBand##disableRealtimeStepsNotify} 关闭通知
     *
     * @param listener
     */
    public void setRealtimeStepsNotifyListener(final RealtimeStepsNotifyListener listener, ActionCallback callback) {
        this.io.setNotifyListener(Profile.UUID_SERVICE_MILI, Profile.UUID_CHAR_REALTIME_STEPS, new NotifyListener() {

            @Override
            public void onNotify(byte[] data, RequestType requestType, String deviceAddress) {
                Log.d(TAG, Arrays.toString(data));
                if (data.length == 4) {
                    int steps = data[3] << 24 | (data[2] & 0xFF) << 16 | (data[1] & 0xFF) << 8 | (data[0] & 0xFF);
                    listener.onRealtimeStepsNotify(getDevice().getAddress(), steps);
                }
            }
        },callback, RequestType.REALTIME_STEPS_LISTENER);
    }

    /**
     * 开启实时步数通知
     */
    public void enableRealtimeStepsNotify(final ActionCallback callback) {
        ActionCallback ioCallBack = new ActionCallback() {
            @Override
            public void onSuccess(Object data, String deviceAddress, RequestType requestType) {
                callback.onSuccess(data, deviceAddress, requestType);
                Log.d(TAG, "enable realtime steps success");
            }

            @Override
            public void onFail(int errorCode, String msg, RequestType requestType) {
                callback.onFail(errorCode, msg, requestType);
                Log.e(TAG, "enable realtime steps failure");
                Log.e(TAG, "errorCode: " + errorCode + " msg: " + msg + " request type: " + requestType);
            }
        };
        this.io.writeCharacteristic(Profile.UUID_CHAR_CONTROL_POINT, Protocol.ENABLE_REALTIME_STEPS_NOTIFY, ioCallBack, RequestType.REALTIME_STEPS_ENABLE);
    }

    /**
     * 关闭实时步数通知
     */
    public void disableRealtimeStepsNotify(final ActionCallback callback) {
        ActionCallback ioCallBack = new ActionCallback() {
            @Override
            public void onSuccess(Object data, String deviceAddress, RequestType requestType) {
                callback.onSuccess(data, deviceAddress, requestType);
                Log.d(TAG, "disable realtime steps success");
            }

            @Override
            public void onFail(int errorCode, String msg, RequestType requestType) {
                callback.onFail(errorCode, msg, requestType);
                Log.e(TAG, "disable realtime steps failure");
                Log.e(TAG, "errorCode: " + errorCode + " msg: " + msg + " request type: " + requestType);
            }
        };
        this.io.writeCharacteristic(Profile.UUID_CHAR_CONTROL_POINT, Protocol.DISABLE_REALTIME_STEPS_NOTIFY, null, RequestType.REALTIME_STEPS_DISABLE);
    }

    /**
     * 设置led灯颜色
     */
    public void setLedColor(LedColor color) {
        byte[] protocal;
        switch (color) {
            case RED:
                protocal = Protocol.SET_COLOR_RED;
                break;
            case BLUE:
                protocal = Protocol.SET_COLOR_BLUE;
                break;
            case GREEN:
                protocal = Protocol.SET_COLOR_GREEN;
                break;
            case ORANGE:
                protocal = Protocol.SET_COLOR_ORANGE;
                break;
            default:
                return;
        }
        this.io.writeCharacteristic(Profile.UUID_CHAR_CONTROL_POINT, protocal, null, RequestType.LED);
    }

    /**
     * 设置用户信息
     *
     * @param userInfo
     */
    public void setUserInfo(UserInfo userInfo, ActionCallback callback) {
        BluetoothDevice device = this.io.getDevice();
        byte[] data = userInfo.getBytes(device.getAddress());
        this.io.writeCharacteristic(Profile.UUID_CHAR_USER_INFO, data, callback, RequestType.USER_INFO);
    }

    public void showServicesAndCharacteristics() {
        for (BluetoothGattService service : this.io.gatt.getServices()) {
            Log.d(TAG, "onServicesDiscovered:" + service.getUuid());

            for (BluetoothGattCharacteristic characteristic : service.getCharacteristics()) {
                Log.d(TAG, "  char:" + characteristic.getUuid());

                for (BluetoothGattDescriptor descriptor : characteristic.getDescriptors()) {
                    Log.d(TAG, "    descriptor:" + descriptor.getUuid());
                }
            }
        }
    }

    public void enableHeartRateSleepSupport(final ActionCallback callback) {
        ActionCallback ioCallBack = new ActionCallback() {
            @Override
            public void onSuccess(Object data, String deviceAddress, RequestType requestType) {
                callback.onSuccess(data, deviceAddress, requestType);
            }

            @Override
            public void onFail(int errorCode, String msg, RequestType requestType) {
                callback.onFail(errorCode, msg, requestType);
            }
        };
        this.io.writeCharacteristic(Profile.UUID_SERVICE_HEARTRATE, Profile.UUID_CHAR_HEART_RATE_CONTROL_POINT, Protocol.START_HEART_RATE_SCAN_SLEEP, ioCallBack, RequestType.HEART_RATE_SLEEP);
    }

    public void setHeartRateScanListener(final HeartRateNotifyListener listener, ActionCallback callback) {
        this.io.setNotifyListener(Profile.UUID_SERVICE_HEARTRATE, Profile.UUID_NOTIFICATION_HEART_RATE, new NotifyListener() {
            @Override
            public void onNotify(byte[] data, RequestType requestType, String deviceAddress) {
                Log.d(TAG, Arrays.toString(data));
                if (data.length == 2 && data[0] == 6) {
                    int heartRate = data[1] & 0xFF;
                    listener.onHeartRateNotify(getDevice().getAddress(), heartRate);
                }
            }
        },callback, RequestType.HEART_RATE_LISTENER);
    }

    public void startHeartRateScan(final ActionCallback callback) {
        ActionCallback ioCallBack = new ActionCallback() {
            @Override
            public void onSuccess(Object data, String deviceAddress, RequestType requestType) {
                callback.onSuccess(data, deviceAddress, requestType);
                Log.d(TAG, "enable heart rate success");
            }

            @Override
            public void onFail(int errorCode, String msg, RequestType requestType) {
                callback.onFail(errorCode, msg, requestType);
                Log.e(TAG, "enable heart rate failure");
                Log.e(TAG, "errorCode: " + errorCode + " msg: " + msg + " request type: " + requestType);
            }
        };

        this.io.writeCharacteristic(Profile.UUID_SERVICE_HEARTRATE, Profile.UUID_CHAR_HEART_RATE_CONTROL_POINT, Protocol.START_HEART_RATE_SCAN_MANUAL, ioCallBack, RequestType.HEART_RATE_ENABLE);

    }

    private byte[] getCurrentTimeBytes() {
        Calendar timestamp = Calendar.getInstance(Locale.getDefault());
        byte[] date = new byte[]{
                (byte) (timestamp.get(Calendar.YEAR) - 2000),
                (byte) timestamp.get(Calendar.MONTH),
                (byte) timestamp.get(Calendar.DAY_OF_MONTH),
                (byte) timestamp.get(Calendar.HOUR_OF_DAY),
                (byte) timestamp.get(Calendar.MINUTE),
                (byte) timestamp.get(Calendar.SECOND)
        };
        Log.d(TAG, "SET_DATE " + Arrays.toString(date));
        return date;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MiBand)) return false;

        MiBand miBand = (MiBand) o;
        return !(this.getDevice() == null || miBand.getDevice() == null) &&
                (io.getDevice().getAddress() != null ? io.getDevice().getAddress().equals(miBand.io.getDevice().getAddress()) : miBand.io == null);

    }

    @Override
    public int hashCode() {
        return io.getDevice().getAddress().hashCode();
    }

    private void handleActivityMetadata(ActivityDataNotifyListener notifyListener, byte[] data) {
        int dataType = data[0];
        Calendar timeStamp = DeviceInfo.convertTime(data);
        int totalData = (data[7] & 0xff) | ((data[8] & 0xff) << 8);
        int nextHeader = (data[9] & 0xff) | ((data[10] & 0xff) << 8);
        if (activityBuffer == null) {
            activityBuffer = new ActivityDataSample(dataType, timeStamp.getTime(), totalData, nextHeader);
            byte[] ack = new byte[] {0xa, data[1], data[2], data[3], data[4], data[5], data[6], data[9], data[10]};
            activityBuffer.addDeviceAck(ack);
        } else {
            activityBuffer.updateHeader(dataType, timeStamp.getTime(), totalData, nextHeader);
            byte[] ack = new byte[] {0xa, data[1], data[2], data[3], data[4], data[5], data[6], data[9], data[10]};
            activityBuffer.addDeviceAck(ack);
            if (nextHeader == 0 && activityBuffer.getProgress() == activityBuffer.getTotalData()) {
                notifyListener.onActivityDataNotify(activityBuffer, RequestType.ACTIVITY, io.getDevice().getAddress());
                activityBuffer = null;
            }
        }
    }

    private void handleActivityData(byte[] data) {
        activityBuffer.addByteArray(data);
    }
}
