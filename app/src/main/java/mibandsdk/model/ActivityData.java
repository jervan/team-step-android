package mibandsdk.model;

import java.util.Date;

/**
 * Created by Jeremiah on 11/12/16.
 */

public class ActivityData {

    private Date timestamp;
    private int intensity;
    private int steps;
    private String category;
    private int heartRate;

    public ActivityData(long timeMilliseconds, byte[] data) {
        this.timestamp = new Date(timeMilliseconds);
        this.category = getCategoryString(data[0] & 0xff);
        this.intensity = data[1] & 0xff;
        this.steps = data[2] & 0xff;
        this.heartRate = data[3] & 0xff;
    }

    public String getCategoryString(int id) {
        switch(id) {

            case 0:
                return "SILENT";

            case 1:
                return "WALKING";

            case 2:
                return "RUNNING";

            case 3:
                return "NOT_WEARING";

            case 4:
                return "DEEP_SLEEP";

            case 5:
                return "LIGHT_SLEEP";

            case 6:
                return "CHARGING";

            default:
                return "UNKNOWN";

        }
    }

    @Override
    public String toString() {
        return "\nActivityData{" +
                "timestamp=" + timestamp +
                ", category=" + category +
                ", intensity=" + intensity +
                ", steps=" + steps +
                ", heartrate=" + heartRate +
                "}";
    }
}
