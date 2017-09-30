package Library4997.MasqExternal.MasqSpecifications;

/**
 * Created by Archish on 12/30/16.
 */

public enum Timeout {
    DRIVE_OPTIMAL (8),
    TURN_OPTIMAL (3),
    DRIVE_LOW (4),
    TURN_LOW (2),
    DRIVE_HIGH (11),
    TURN_HIGH (5);
    public final double value;
    Timeout (double value) {this.value = value;}
}
