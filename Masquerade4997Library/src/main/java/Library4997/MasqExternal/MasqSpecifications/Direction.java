package Library4997.MasqExternal.MasqSpecifications;

/**
 * Created by Archish on 12/28/16.
 */

public enum Direction {
    FORWARD (+1.0),
    BACKWARD (-1.0),
    LEFT (+1.0),
    RIGHT (-1.0);
    public final double value;
    Direction (double value) {this.value = value;}
}
