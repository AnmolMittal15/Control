package Library4997.MasqSensors;


import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.VoltageSensor;
import com.qualcomm.robotcore.util.RollingAverage;

import org.firstinspires.ftc.robotcontroller.internal.FtcOpModeRegister;

import Library4997.MasqExternal.MasqOpModeInternal;

public class MasqVoltageSensor {
    public final static int samples = 2000000;
    VoltageSensor sensor;
    RollingAverage average;

    public MasqVoltageSensor() {
        sensor = MasqOpModeInternal.getInstance(null).getHardwareMap().voltageSensor.iterator().next();
        average = new RollingAverage(samples);
    }

    public void update() {
        average.addNumber(getVoltageInstantaneous());
    }

    public double getVoltage() {
        return average.getAverage();
    }

    public int getVoltageInstantaneous() {
        return (int) sensor.getVoltage();
    }
}