package edu.uwp.jeremiah.vanofferen.csci475.mi_band.Connections;

import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import edu.uwp.jeremiah.vanofferen.csci475.mi_band.Models.ActivityData;
import edu.uwp.jeremiah.vanofferen.csci475.mi_band.Models.AvailableMiBand;
import edu.uwp.jeremiah.vanofferen.csci475.mi_band.Models.Group;
import edu.uwp.jeremiah.vanofferen.csci475.mi_band.Models.Leader;
import edu.uwp.jeremiah.vanofferen.csci475.mi_band.Models.Member;
import edu.uwp.jeremiah.vanofferen.csci475.mi_band.Service.QueryPreferences;
import mibandsdk.ActionCallback;
import mibandsdk.MiBand;
import mibandsdk.RequestType;
import mibandsdk.listeners.ActivityDataNotifyListener;
import mibandsdk.listeners.NotifyListener;
import mibandsdk.listeners.RealtimeStepsNotifyListener;
import mibandsdk.model.ActivityDataSample;
import mibandsdk.model.BatteryInfo;
import mibandsdk.model.DeviceInfo;
import mibandsdk.model.UserInfo;


/**
 * Created by Jeremiah on 10/21/16.
 */


public class MiBandManager extends Application {

    private static final String TAG = "MiBandManager";

    public static final String AVAILABLE = "Available";
    public static final String NOT_AVAILABLE = "Not Available";
    public static final String CONNECTING = "Connecting";
    public static final String CONNECTED = "Connected";

    private static final int MIBAND_CYCLE_RATE_MILLISECONDS = 60000;

    private static MiBandManager mInstance;
    private boolean isRunning;
    private Handler availableDeviceHandler;
    private Handler memberDeviceHandler;
    private AvailableDevicesRunnable availableDeviceRunnable;
    private MemberDevicesRunnable memberDeviceRunnable;

    private Context mContext;
    private volatile Group group;
    private volatile HashMap<String, AvailableMiBand> availableDevices;
    private volatile Queue<Member> discoveredMembers;
    private volatile ArrayList<AvailableMiBand> unregisteredAvailableMibands;
    private volatile ArrayList<Handler> handlers;
    private volatile MiBand selectedMemberDevice;
    private volatile MiBand cyclingMemberDevice;
    private volatile Member selectedMember;


    // Scan for nearby devices
    final ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult Result) {
            try {
                final BluetoothDevice device = Result.getDevice();

                if (isRunning && device != null && device.getName() != null && device.getName().equals("MI1S")) {
                    AvailableMiBand miband = new AvailableMiBand(device, Result.getRssi(), System.currentTimeMillis());
                    unregisteredAvailableMibands.remove(miband);
                    Member member = getBandsMember(device.getAddress());
                    if (member != null) {
                        if (!discoveredMembers.contains(member)) {
                            discoveredMembers.add(member);
                        }
                        if (member.getDeviceStatus().equals(NOT_AVAILABLE)) {
                            member.setDeviceStatus(AVAILABLE);
                        }
                    } else {
                        unregisteredAvailableMibands.add(miband);
                        Collections.sort(unregisteredAvailableMibands);
                        if (unregisteredAvailableMibands.size() > 10) {
                            unregisteredAvailableMibands = new ArrayList<>(unregisteredAvailableMibands.subList(0, 10));
                        }
                    }
                    updateUI();
                    availableDevices.put(device.getAddress(), new AvailableMiBand(device, Result.getRssi(), System.currentTimeMillis()));
                }
            } catch (Exception e) {
                Log.e(TAG, "Bluetooth Scan Callback Error", e);
            }
        }
    };

    private final BroadcastReceiver bluetoothStateChangedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                        BluetoothAdapter.ERROR);
                Log.d(TAG, "Bluetooth State Change action: " + action + ", state: " + state);
                switch (state) {

                    case BluetoothAdapter.STATE_TURNING_OFF:
                    case BluetoothAdapter.STATE_OFF:
                        stopScanning();
                        break;
                    case BluetoothAdapter.STATE_ON:
                        startScanning();
                        break;
                }
            }
        }
    };

    public static MiBandManager getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new MiBandManager(context.getApplicationContext());
        }

        return mInstance;
    }

    private MiBandManager(Context context) {
        this.mContext = context.getApplicationContext();
        availableDevices = new HashMap<>();
        discoveredMembers = new LinkedList<>();
        unregisteredAvailableMibands = new ArrayList<>();
        handlers = new ArrayList<>();
        mContext.registerReceiver(bluetoothStateChangedReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
        isRunning = false;
        availableDeviceHandler = new Handler();
        memberDeviceHandler = new Handler();
    }

    public void close() {
        mContext.unregisterReceiver(bluetoothStateChangedReceiver);
        stopScanning();
        mInstance = null;
    }

    public void startScanning() {
        if (!isRunning && BluetoothAdapter.getDefaultAdapter().isEnabled()) {
            isRunning = true;
            MiBand.startScan(scanCallback);
            Log.d(TAG, "Scan Started");
            updateAvailableDevices();
            cycleMemberDevices();
        }
    }

    public void stopScanning() {
        if (isRunning) {
            MiBand.stopScan(scanCallback);
            isRunning = false;
            if (availableDeviceHandler != null) {
                availableDeviceHandler.removeCallbacks(availableDeviceRunnable);
            }

            if (availableDeviceRunnable != null) {
                availableDeviceRunnable.stop();
            }

            if (memberDeviceHandler != null) {
                memberDeviceHandler.removeCallbacks(memberDeviceRunnable);
            }

            if (memberDeviceRunnable != null) {
                memberDeviceRunnable.stop();
            }

            if (selectedMemberDevice != null) {
                disconnectSelectedMemberDevice();
            }

            if (cyclingMemberDevice != null) {
                cyclingMemberDevice.disconnect();
            }

            if (group != null) {
                group.getLeader().setDeviceStatus(NOT_AVAILABLE);
                for (Member member : group.getMembers()) {
                    member.setDeviceStatus(NOT_AVAILABLE);
                }
            }
            unregisteredAvailableMibands.clear();
            updateUI();
        }
        Log.d(TAG, "Scan Stopped");
    }

    private void updateAvailableDevices() {
        availableDeviceRunnable = new AvailableDevicesRunnable();
        availableDeviceHandler.post(availableDeviceRunnable);
    }

    private void cycleMemberDevices() {
        memberDeviceRunnable = new MemberDevicesRunnable();
        memberDeviceHandler.post(memberDeviceRunnable);
    }


    public void selectMemberDevice (final Member member, final boolean isFirstConnection) {

        // if there is an old selected member device and it's doesnt have same address we are connecting to disconnect
        if (!member.equals(selectedMember)) {
            disconnectSelectedMemberDevice();
        }

        // cycling device was selected
        if (cyclingMemberDevice != null && cyclingMemberDevice.getDevice() != null && cyclingMemberDevice.getDevice().getAddress() != null
                && cyclingMemberDevice.getDevice().getAddress().equals(member.getDeviceAddress())) {
            // check if device is connected
            if (cyclingMemberDevice.getGatt() != null) {
                selectedMemberDevice = cyclingMemberDevice;
            } else {
                selectedMemberDevice = connectToBand(member, isFirstConnection);
            }
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (selectedMember != null && selectedMember.equals(member))
                        disconnectSelectedMemberDevice();
                }
            }, MIBAND_CYCLE_RATE_MILLISECONDS * 2);
            cyclingMemberDevice = null;

        }

        // make sure device wasnt already selected
        else if (!member.equals(selectedMember))  {
            selectedMemberDevice = connectToBand(member, isFirstConnection);
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (selectedMember != null && selectedMember.equals(member))
                        disconnectSelectedMemberDevice();
                }
            }, MIBAND_CYCLE_RATE_MILLISECONDS * 2);
            if (selectedMemberDevice != null) {
                discoveredMembers.remove(member);
            }
        }

        selectedMember = member;
    }

    public void disconnectSelectedMemberDevice() {
        if (selectedMemberDevice != null) {
            Log.d(TAG, "disconnecting selected member");
            selectedMemberDevice.disconnect();
            selectedMemberDevice = null;
        }
        if (selectedMember != null) {
            selectedMember.setDeviceStatus(NOT_AVAILABLE);
            selectedMember = null;
            updateUI();
        }
    }

    private MiBand connectToBand(final Member member, final boolean isFirstConnection) {
        Log.d(TAG, "connecting to miband address: " + member.getDeviceAddress());
        AvailableMiBand availableMiBand = availableDevices.get(member.getDeviceAddress());
        if (availableMiBand == null) {
            member.setDeviceStatus(NOT_AVAILABLE);
            updateUI();
            return null;
        }
        member.setDeviceStatus(CONNECTING);
        updateUI();
        BluetoothDevice device = availableMiBand.getMiband();
        final MiBand miBand = new MiBand(mContext);

        final NotifyListener notifyListener = new NotifyListener() {
            @Override
            public void onNotify(byte[] data, RequestType requestType, String deviceAddress) {
                switch (requestType) {
                    case DISCONNECT:
                        if (selectedMemberDevice != null && selectedMember != null && selectedMember.getDeviceAddress().equals(deviceAddress)) {
                            selectedMemberDevice.disconnect();
                            selectedMemberDevice = null;
                            selectedMember = null;
                        } else if (cyclingMemberDevice != null && cyclingMemberDevice.getDevice() != null
                                && cyclingMemberDevice.getDevice().getAddress() != null && cyclingMemberDevice.getDevice().getAddress().equals(deviceAddress)) {
                            cyclingMemberDevice.disconnect();
                        }
                        availableDevices.remove(deviceAddress);
                        member.setDeviceStatus(NOT_AVAILABLE);
                        updateUI();
                        Log.d(TAG, "Device Address: " + deviceAddress + " Disconnected");
                        break;

                    default:
                        if ( !isRunning || !(cyclingMemberDevice != null && cyclingMemberDevice.getDevice() != null && deviceAddress.equals(cyclingMemberDevice.getDevice().getAddress()) ||
                                selectedMemberDevice != null && selectedMemberDevice.getDevice() != null && deviceAddress.equals(selectedMemberDevice.getDevice().getAddress()))) {
                            miBand.disconnect();
                        }
                        Log.d(TAG,"Device Address: " + deviceAddress +" Notify Listener Request type: " + requestType + " Data: " + Arrays.toString(data));
                }
            }
        };

        final ActivityDataNotifyListener activityDataNotifyListener = new ActivityDataNotifyListener() {
            @Override
            public void onActivityDataNotify(final ActivityDataSample dataSample, RequestType requestType, String deviceAddress) {
                if ( !isRunning || !(cyclingMemberDevice != null && cyclingMemberDevice.getDevice() != null && deviceAddress.equals(cyclingMemberDevice.getDevice().getAddress()) ||
                        selectedMemberDevice != null && selectedMemberDevice.getDevice() != null && deviceAddress.equals(selectedMemberDevice.getDevice().getAddress()))) {
                    miBand.disconnect();
                }
                Queue<ActivityData> temp;

                if (member instanceof Leader) {
                    temp = queueActivityData(true, member.getId(), dataSample.getData());
                } else {
                    temp = queueActivityData(false, member.getId(), dataSample.getData());
                }

                final Queue<ActivityData> activityDataQueue = temp;

                NetworkConnectionManager<ActivityData, Void, Integer> postActivityDataMember =
                        new NetworkConnectionManager<ActivityData, Void, Integer>(NetworkConnectionManager.RequestType.POST_ACTIVITY_DATA) {
                            @Override
                            protected void onPostExecute(Integer stepCount) {
                                if (stepCount != null) {
                                    // network connection success
                                    Log.d(TAG, "Step Count: " + stepCount);
                                    if (member.getLastKnownStepCount() < stepCount) {
                                        member.setLastKnownStepCount(stepCount);
                                        updateUI();
                                    }
                                    if (activityDataQueue.size() > 0) {
                                        this.execute(activityDataQueue.poll());
                                    } else if (miBand.getGatt() != null && dataSample.getDeviceAcks().size() > 0) {
                                        miBand.notifyActivityDataRecieved(new ActionCallback() {
                                            @Override
                                            public void onSuccess(Object data, String deviceAddress, mibandsdk.RequestType requestType) {
                                                if (dataSample.getDeviceAcks().size() > 0) {
                                                    miBand.notifyActivityDataRecieved(this, dataSample.getNextAck());
                                                } else {
                                                    Log.d(TAG, "ActivityData cleared on " + member.getFirstName() + "'s device");
                                                }
                                            }

                                            @Override
                                            public void onFail(int errorCode, String msg, mibandsdk.RequestType requestType) {
                                                Log.e(TAG, "Failed to clear " + member.getFirstName() + "'s devices data");
                                                Log.e(TAG, "RequestType: " + requestType + " Message: " + msg + " Error Code: " + errorCode);
                                            }
                                        }, dataSample.getNextAck());
                                    } else {
                                        Log.e(TAG, "activity data acknowledgement error");
                                    }
                                } else {
                                    Toast.makeText(mContext, "Unable to connect to network", Toast.LENGTH_SHORT).show();
                                }
                            }
                };

                postActivityDataMember.execute(activityDataQueue.poll());
            }
        };

        final RealtimeStepsNotifyListener realtimeStepsNotifyListener = new RealtimeStepsNotifyListener() {
            @Override
            public void onRealtimeStepsNotify(String deviceAddress, int steps) {
                if ( !isRunning || !(cyclingMemberDevice != null && cyclingMemberDevice.getDevice() != null && deviceAddress.equals(cyclingMemberDevice.getDevice().getAddress()) ||
                        selectedMemberDevice != null && selectedMemberDevice.getDevice() != null && deviceAddress.equals(selectedMemberDevice.getDevice().getAddress()))) {
                    miBand.disconnect();
                }
                member.setLastKnownStepCount(steps);
                updateUI();
                Log.d(TAG, member.getFirstName() + " step count updated: " + member.getLastKnownStepCount());
            }
        };

        ActionCallback callback = new ActionCallback() {
            @Override
            public void onSuccess(Object data, String deviceAddress, RequestType requestType) {
                switch (requestType) {

                    case CONNECT:
                        member.setDeviceStatus(CONNECTED);
                        updateUI();
                        miBand.setDisconnectedListener(notifyListener);
                        UserInfo userInfo;
                        if (isFirstConnection) {
                            userInfo = new UserInfo(member.getId(), member.getGender(), member.getAge(),
                                    (int) member.getHeightInCentimeters(), (int) member.getWeightInKilograms(), member.getFirstName(), 1);
                        } else {
                            userInfo = new UserInfo(member.getId(), member.getGender(), member.getAge(),
                                    (int) member.getHeightInCentimeters(), (int) member.getWeightInKilograms(), member.getFirstName(), 2);
                        }
                        miBand.setUserInfo(userInfo, this);
                        break;

                    case SET_DATE:
                        miBand.getDate(this);
                        break;

                    case GET_DATE:
                        miBand.setNormalNotifyListener(notifyListener, this);
                        Log.d(TAG, "Device Address: " + deviceAddress + " Connection Success.");
                        break;

                    case NORMAL:
                        miBand.getDeviceInfo(this);
                        Log.d(TAG, "Device Address: " + deviceAddress + " Normal Listener Set.");
                        break;

                    case USER_INFO:
                        miBand.setDate(this);
                        Log.d(TAG, "Device Address: " + deviceAddress + " User info set.");
                        break;

                    case DEVICE_INFO:
                        DeviceInfo deviceInfo = (DeviceInfo) data;
                        miBand.getBatteryInfo(this);
                        Log.d(TAG, "Device Address: " + deviceAddress + " " + deviceInfo.toString());
                        break;

                    case BATTERY:
                        BatteryInfo batteryInfo = (BatteryInfo) data;
                        member.setBatteryLevel(batteryInfo.getLevel());
                        updateUI();
                        miBand.setRealtimeStepsNotifyListener(realtimeStepsNotifyListener, this);
                        //example display Cycles: 4, Level: 44, Status: unknow, Last: 2015-04-15 03:37:55
                        Log.d(TAG, "Device Address: " + deviceAddress + " Battery Info:" + batteryInfo.toString());
                        break;

                    case REALTIME_STEPS_LISTENER:
                        miBand.enableRealtimeStepsNotify(this);
                        Log.d(TAG, "Device Address: " + deviceAddress + " Realtime steps listener set.");
                        break;

                    case REALTIME_STEPS_ENABLE:
                        miBand.enableHeartRateSleepSupport(this);
                        Log.d(TAG, "Device Address: " + deviceAddress + " Realtime steps enabled.");
                        break;

                    case HEART_RATE_SLEEP:
                        if (!isFirstConnection) {
                            miBand.setActivityNotifyListener(activityDataNotifyListener, this);
                        }
                        Log.d(TAG, "Device Address: " + deviceAddress + " HeartRate sleep enabled.");
                        break;

                    case ACTIVITY_LISTENER:
                        miBand.fetchActivityData(this);
                        Log.d(TAG, "Device Address: " + deviceAddress + " Activity Data enabled.");
                        break;

                    default:
                        Log.w(TAG, "Device Address: " + deviceAddress + " Request Type: " + requestType + " Data: " + data);
                }
            }

            @Override
            public void onFail(int errorCode, String msg, RequestType requestType) {
                Log.e(TAG, "RequestType: " + requestType + " Message: " + msg + " Error Code: " + errorCode);
            }
        };

        miBand.connect(device, callback);
        return miBand;
    }

    private Queue<ActivityData> queueActivityData(boolean isLeader, int memberId, List<mibandsdk.model.ActivityData> dataList) {
        Queue<ActivityData> returnQueue = new LinkedList<>();

        do {
            ActivityData temp;
            // split activityData by daily amount
            if (dataList.size() <= 1440) {
                temp = new ActivityData(isLeader, memberId, dataList);
                dataList = new ArrayList<>();
            } else {
                temp = new ActivityData(isLeader, memberId, dataList.subList(0, 1440));
                dataList = dataList.subList(1440, dataList.size());
            }
            returnQueue.add(temp);
        } while (dataList.size() > 0);

        return returnQueue;
    }

    private Member getBandsMember(String deviceAddress) {
        if (group != null && group.getLeader().getDeviceAddress() != null && group.getLeader().getDeviceAddress().equals(deviceAddress)) {
            return group.getLeader();
        } else if (group != null) {
            for (Member member: group.getMembers()) {
                if (member.getDeviceAddress() != null && member.getDeviceAddress().equals(deviceAddress))
                    return member;
            }
        }
        return null;
    }

    private void updateUI() {
        for (Handler handler: handlers)
            handler.sendEmptyMessage(1);
    }

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        if (selectedMemberDevice != null)
            disconnectSelectedMemberDevice();
        if (cyclingMemberDevice != null)
            cyclingMemberDevice.disconnect();
        group.getLeader().setDeviceStatus(NOT_AVAILABLE);
        for(Member member: group.getMembers()) {
            member.setDeviceStatus(NOT_AVAILABLE);
        }
        stopScanning();
        discoveredMembers.clear();
        QueryPreferences.setStoredGroup(mContext, group);
        this.group = group;
        startScanning();
    }

    public void removeMember(Member member) {
        if (selectedMemberDevice != null && selectedMemberDevice.getDevice() != null
                && selectedMemberDevice.getDevice().getAddress() != null && selectedMemberDevice.getDevice().getAddress().equals(member.getDeviceAddress())) {
            disconnectSelectedMemberDevice();
        }
        if (cyclingMemberDevice != null && cyclingMemberDevice.getDevice() != null
                && cyclingMemberDevice.getDevice().getAddress() != null && cyclingMemberDevice.getDevice().getAddress().equals(member.getDeviceAddress())) {
            cyclingMemberDevice.disconnect();
        }
        this.group.getMembers().remove(member);
        this.discoveredMembers.remove(member);
    }

    public void addHandler(Handler handler) { this.handlers.add(handler); }

    public void removeHandler(Handler handler) {this.handlers.remove(handler); }

    public ArrayList<AvailableMiBand> getAvailableDevices() {
        return unregisteredAvailableMibands;
    }

    private class AvailableDevicesRunnable implements Runnable {

        private volatile boolean isStopped = false;

        @Override
        public void run() {
            if (!isRunning || isStopped) {
                return;
            }
            ArrayList<String> keys = new ArrayList<>(availableDevices.keySet());
            for (int i = 0; i < keys.size(); i++) {
                if (availableDevices.get(keys.get(i)).getTimeLastSeenInMilliSeconds() < (System.currentTimeMillis() - 30000)) {
                    Log.d(TAG, "removed:" + availableDevices.get(keys.get(i)));
                    AvailableMiBand miBand = availableDevices.get(keys.get(i));
                    availableDevices.remove(keys.get(i));
                    if (unregisteredAvailableMibands.contains(miBand)) {
                        unregisteredAvailableMibands.remove(miBand);
                    }
                    updateUI();
                }
            }
            availableDeviceHandler.postDelayed(this, 5000);
        }

        public void stop() {
            Log.d(TAG, "available devices thread stopped");
            isStopped = true;
        }
    }

    private class MemberDevicesRunnable implements Runnable {

        private volatile boolean isStopped = false;

        @Override
        public void run() {
            final Runnable looper = this;
            if (!isRunning || isStopped) {
                Log.d(TAG, "member device thread stopped");
                return;
            }
            if (discoveredMembers.size() == 0) {
                memberDeviceHandler.postDelayed(this, 5000); // check again in 5 seconds
                return;
            }
            final Member member = discoveredMembers.poll();
            if (!member.equals(selectedMember)) {
                cyclingMemberDevice = connectToBand(member, false);
            }
            if (cyclingMemberDevice == null) {
                memberDeviceHandler.postDelayed(this, 5000); // check again in 5 seconds
                return;
            }
            memberDeviceHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    // application is not running disconnect and stop cycling
                    if (!isRunning || isStopped) {
                        if (cyclingMemberDevice != null && cyclingMemberDevice.getGatt() != null) {
                            cyclingMemberDevice.disconnect();
                        }
                        return;
                    }

                    if (cyclingMemberDevice != null  && !(cyclingMemberDevice.equals(selectedMemberDevice))) {
                        cyclingMemberDevice.disconnect();
                        member.setDeviceStatus(NOT_AVAILABLE);
                        updateUI();
                    } else if (cyclingMemberDevice == null && !member.equals(selectedMember)) {
                        availableDevices.remove(member.getDeviceAddress());
                        member.setDeviceStatus(NOT_AVAILABLE);
                        updateUI();
                    }
                    memberDeviceHandler.post(looper);

                }
            }, MIBAND_CYCLE_RATE_MILLISECONDS);
        }

        public void stop() {
            Log.d(TAG, "member device thread request stop");
            isStopped = true;
        }
    }
}
