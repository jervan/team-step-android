package mibandsdk.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Created by Jeremiah on 11/12/16.
 */

public class ActivityDataSample {

    private int dataType;
    private Date timeStamp;
    private long progressTimeStamp;
    private int totalData;
    private int nextHeader;
    private int progress;
    private List<ActivityData> data;
    private Queue<Byte[]> deviceAcks;

    public ActivityDataSample(int dataType, Date timeStamp, int totalData, int nextHeader) {
        this.dataType = dataType;
        this.timeStamp = timeStamp;
        this.progressTimeStamp = timeStamp.getTime();
        this.totalData = totalData;
        this.nextHeader = nextHeader;
        this.progress = 0;
        this.data = new ArrayList<>();
        this.deviceAcks = new LinkedList<>();
    }

    public int getDataType() {
        return dataType;
    }

    public Date getTimeStamp() {
        return timeStamp;
    }

    public int getTotalData() {
        return totalData;
    }

    public int getNextHeader() {
        return nextHeader;
    }

    public int getProgress() {
        return progress;
    }

    public List<ActivityData> getData() {
        return data;
    }

    public void addDeviceAck(byte[] ack) {
        Byte[] temp = new Byte[ack.length];
        for (int i = 0; i < ack.length; i++) {
            temp[i] = ack[i];
        }
        deviceAcks.add(temp);
    }

    public byte[] getNextAck() {
        if (deviceAcks.size() > 0) {
            Byte[] ack = deviceAcks.poll();
            byte[] temp = new byte[ack.length];
            for (int i = 0; i < ack.length; i++) {
                temp[i] = ack[i];
            }
            return temp;
        } else {
            return null;
        }
    }

    public Queue<Byte[]> getDeviceAcks() {
        return deviceAcks;
    }

    public void updateHeader(int dataType, Date timeStamp, int totalData, int nextHeader) {
        this.dataType = dataType;
        this.timeStamp = timeStamp;
        this.progressTimeStamp = timeStamp.getTime();
        this.totalData = totalData;
        this.nextHeader = nextHeader;

    }

    public void addByteArray(byte[] data) {
        for (int i = 0; i < data.length; i += 4) {
            byte[] temp = new byte[] {data[i], data[i+1], data[i+2], data[i+3]};
            this.data.add(new ActivityData(progressTimeStamp, temp));
            progressTimeStamp += 60000;
            nextHeader --;
            progress ++;
        }
    }

    @Override
    public String toString() {
        return "ActivityDataSample{" +
                "dataType=" + dataType +
                ", timeStamp=" + timeStamp +
                ", progressTimeStamp=" + new Date(progressTimeStamp) +
                ", totalData=" + totalData +
                ", nextHeader=" + nextHeader +
                ", progress=" + progress +
                ", data=" + data +
                '}';
    }
}
