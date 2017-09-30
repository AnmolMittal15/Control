package Library4997.MasqExternal.MasqSpecifications;

/**
 * Created by Archish on 12/28/16.
 */

public enum Speed {
    OPTIMAL (+0.5),
    LOW (+0.3),
    HIGH (+0.7);
    public final double value;
    Speed (double value) {this.value = value;}
}
