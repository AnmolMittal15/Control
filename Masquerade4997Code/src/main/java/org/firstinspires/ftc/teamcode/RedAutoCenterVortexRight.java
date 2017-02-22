package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.VoltageSensor;

import BasicLib4997.MasqMotors.TankDrive.Direction;
import BasicLib4997.MasqMotors.TankDrive.TankDrive;

/**
 * Beacons + Shoot for Red8
 */

@Autonomous(name = "RedAutoCenterVortexRight", group = "G1") // change name

public class RedAutoCenterVortexRight extends LinearOpMode { // change file name
    public void main() throws InterruptedException {

    }



    @Override
    public void runOpMode() throws InterruptedException {
        boolean telemetrizeModules;
        double POWER = 0.50;
        TankDrive chimera = new TankDrive(telemetry);
        while (!isStarted()) {
            chimera.setIndexer(0);
            chimera.imu.telemetryRun();
            chimera.leftColor.telemetryRun();
            chimera.rangeSensor.telemetryRun();
            telemetry.update();
            idle();
        }
        waitForStart();
        //SetPower to the shooter and drive foreword in orer to make a shot
        chimera.colorRejection.setActiveMode();
        chimera.rightColor.setPassiveMode();
        boolean isNeccesary = true;
        //SetPower to the shooter and drive foreword in order to make a shot
        chimera.setPowerCollector(-1);
        double power = -0.45;
        chimera.setPowerShooter(power);
        double parallelAngle = chimera.imu.getHeading();
        chimera.drivePID(POWER, 15, Direction.FORWARD);
        // find how arr of we are from our orignial position
        double disruption = chimera.imu.getHeading();
        double i  = 0.001;
        while ((((chimera.shooter.getRate() + chimera.shooter2.getRate())/2) > - 500 && ((chimera.shooter.getRate() + chimera.shooter2.getRate())/2) > -525)) {
            chimera.setIndexer(0);
            telemetry.addData("RATE", (chimera.shooter.getRate() + chimera.shooter2.getRate())/2);
            chimera.setPowerShooter(power - i);
            i += 0.001;
            telemetry.update();
        }
        chimera.setIndexer(0.6);
        chimera.sleep(500);
        chimera.setIndexer(0);
        chimera.sleep(2000);
        i = 0.001;
        while ((((chimera.shooter.getRate() + chimera.shooter2.getRate())/2) > - 500 && ((chimera.shooter.getRate() + chimera.shooter2.getRate())/2) > -525)) {
            chimera.setIndexer(0);
            telemetry.addData("RATE", (chimera.shooter.getRate() + chimera.shooter2.getRate())/2);
            chimera.setPowerShooter(power - i);
            i += 0.001;
            telemetry.update();
        }
        chimera.setIndexer(0.6);
        chimera.sleep(700);
        chimera.setPowerShooter(-0.5);
        chimera.setPowerShooter(-0.3);
        chimera.setPowerShooter(0);
        chimera.setPowerCollector(0);
        //turn away from the center vortex 48 degrees - our disruption
        chimera.turnPID(POWER, (int) (49 - disruption), Direction.RIGHT, 3);
        if (chimera.angleLeftCover >= 0.5 && chimera.angleLeftCover <= 2) {
            isNeccesary = false;
        }
        // drive parallel to the ramp
        chimera.drivePID(0.5, 298, Direction.FORWARD);
        // turn parallel to the Beacon
        double changeTurn = chimera.imu.getHeading();
        double turn = changeTurn + parallelAngle;
        chimera.turnPID(POWER, (int) turn, Direction.RIGHT, 5);
        // Stop at first Beacon
        chimera.stopWhite(0.2, Direction.BACKWARD);
        chimera.stopRedRight(0.2, Direction.BACKWARD);
        // drive in position to hit the beacon
        chimera.drivePID(POWER, 33, Direction.BACKWARD);
        // move the beacon presser
        chimera.setLeftPresser(-1);
        chimera.sleep(1000);
        chimera.setLeftPresser(1);
        double turn2 = parallelAngle + chimera.imu.getHeading();
        chimera.turnPID(POWER, (int) turn2, Direction.RIGHT, 2, 0.3);
        if (isNeccesary) {
            chimera.turnPID(POWER, 3, Direction.RIGHT, 1, 0.3);
        }
        // drive to the next beacon
        double turn3 = parallelAngle + chimera.imu.getHeading();
        chimera.drivePID(0.5, 150, Direction.FORWARD, 500);
        chimera.turnPID(POWER, (int) turn3, Direction.RIGHT, 2, 0.1);
        chimera.setLeftPresser(0);
        chimera.stopWhite(0.3, Direction.BACKWARD);
        chimera.stopRedRight(0.2, Direction.BACKWARD);
        // drive in position to hit the beacon
        chimera.drivePID(POWER, 25, Direction.BACKWARD);
        // move the beacon presser
        chimera.setLeftPresser(-1);
        chimera.sleep(1000);
        chimera.setLeftPresser(1);








    }
    private double getBatteryVoltage() {
        double result = Double.POSITIVE_INFINITY;
        for (VoltageSensor sensor : hardwareMap.voltageSensor) {
            double voltage = sensor.getVoltage();
            if (voltage > 0) {
                result = Math.min(result, voltage);
            }
        }
        return result;
    }
    private double newShooterPower(double targetPower){
        double voltsge = getBatteryVoltage();
        double targetVoltage = 13.5;
        double error = targetVoltage - voltsge;
        return targetPower - (error * 0.02);
    }


}