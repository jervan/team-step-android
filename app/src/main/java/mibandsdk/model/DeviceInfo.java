package mibandsdk.model;


import java.util.Calendar;
import java.util.GregorianCalendar;

public class DeviceInfo {
    public final String deviceId;
    public final int profileVersion;
    public final int fwVersion;
    public final int hwVersion;
    public final int feature;
    public final int appearance;
    public final int fw2Version;


    public DeviceInfo(byte[] data) {
            deviceId = String.format("%02X%02X%02X%02X%02X%02X%02X%02X", data[0], data[1], data[2], data[3], data[4], data[5], data[6], data[7]);
            profileVersion = getInt(data, 8);
            fwVersion = getInt(data, 12);
            hwVersion = data[6] & 255;
            appearance = data[5] & 255;
            feature = data[4] & 255;
            if (data.length == 20) {
                int s = 0;
                for (int i = 0; i < 4; ++i) {
                    s |= (data[16 + i] & 255) << i * 8;
                }
                fw2Version = s;
            } else {
                fw2Version = -1;
            }
    }

    public static int getInt(byte[] data, int from, int len) {
        int ret = 0;
        for (int i = 0; i < len; ++i) {
            ret |= (data[from + i] & 255) << i * 8;
        }
        return ret;
    }

    private int getInt(byte[] data, int from) {
        return getInt(data, from, 4);
    }

    @Override
    public String toString() {
        return "DeviceInfo{" +
                "deviceId='" + deviceId + '\'' +
                ", profileVersion=" + profileVersion +
                ", fwVersion=" + fwVersion +
                ", hwVersion=" + hwVersion +
                ", feature=" + feature +
                ", appearance=" + appearance +
                ", fw2Version (hr)=" + fw2Version +
                '}';
    }

    public static Calendar convertTime (byte[] data) {
        Calendar time = Calendar.getInstance();

        time.set(Calendar.YEAR, data[1] + 2000);
        time.set(Calendar.MONTH, data[2]);
        time.set(Calendar.DATE, data[3]);

        time.set(Calendar.HOUR_OF_DAY, data[4]);
        time.set(Calendar.MINUTE, data[5]);
        time.set(Calendar.SECOND, data[6]);

        return time;
    }
}
