package Library4997.MasqExternal.MasqSpecifications;

/**
 * Created by Archish on 12/30/16.
 */

public enum  SleepTime {
    OPTIMAL (1),
    LOW (+0.5),
    HIGH (2);
    public final double value;
    SleepTime (double value) {this.value = value;}
}
