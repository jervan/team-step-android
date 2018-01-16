package mibandsdk.model;

import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * 手环电池相关信息类
 */
public class BatteryInfo {
    /**
     * 电池当前所在的状态
     */
    static enum Status {
        UNKNOWN, NORMAL, LOW, FULL, CHARGING, NOT_CHARGING;

        public static Status fromByte(byte b) {
            switch (b) {
                case 0:
                    return NORMAL;
                case 1:
                    return LOW;
                case 2:
                    return CHARGING;
                case 3:
                    return FULL;
                case 4:
                    return NOT_CHARGING;

                default:
                    return UNKNOWN;
            }
        }
    }

    private int level;
    private int cycles;
    private Status status;
    private Calendar lastChargedDate;

    private BatteryInfo() {

    }

    public static BatteryInfo fromByteData(byte[] data) {
        if (data.length < 10) {
            return null;
        }
        BatteryInfo info = new BatteryInfo();

        info.level = data[0];
        info.status = Status.fromByte(data[9]);
        info.cycles = 0xffff & (0xff & data[7] | (0xff & data[8]) << 8);
        info.lastChargedDate = DeviceInfo.convertTime(data);

        return info;
    }

    public String toString() {
        return "cycles:" + this.getCycles()
                + ",level:" + this.getLevel()
                + ",status:" + this.getStatus()
                + ",last:" + new SimpleDateFormat("yyyy-MM-dd HH:mm:SS", Locale.CHINA).format(this.getLastChargedDate().getTime());
    }

    /**
     * 电池电量百分比, level=40 表示有40%的电量
     */
    public int getLevel() {
        return level;
    }

    /**
     * 充电循环次数
     */
    public int getCycles() {
        return cycles;
    }

    /**
     * 当前状态
     *
     * @see Status
     */
    public Status getStatus() {
        return status;
    }

    /**
     * 最后充电时间
     */
    public Calendar getLastChargedDate() {
        return lastChargedDate;
    }

}
