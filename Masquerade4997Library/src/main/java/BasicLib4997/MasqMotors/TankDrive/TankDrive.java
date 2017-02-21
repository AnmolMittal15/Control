package BasicLib4997.MasqMotors.TankDrive;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

import org.firstinspires.ftc.robotcontroller.internal.FtcOpModeRegister;
import org.firstinspires.ftc.robotcore.external.Telemetry;

import BasicLib4997.MasqMotors.MasqMotor;
import BasicLib4997.MasqMotors.MasqMotorSystem;
import BasicLib4997.MasqSensors.AdafruitIMU;
import BasicLib4997.MasqSensors.MasqClock;
import BasicLib4997.MasqSensors.MasqColorSensor;
import BasicLib4997.MasqSensors.MasqRangeSensor;
import BasicLib4997.MasqSensors.MasqODS;
import BasicLib4997.MasqSensors.Sensor_Thresholds;
import BasicLib4997.MasqServos.MasqCRServo;
import BasicLib4997.MasqServos.MasqServo;
import static BasicLib4997.MasqMotors.MasqMotorSystem.convert;

/**
 * Created by Archish on 10/28/16.
 */

public class TankDrive implements PID_Constants, Sensor_Thresholds {

    public TankDrive(Telemetry telemetry){
        this.telemetry  = telemetry;
        instance = this;
    }
    public static TankDrive getTelemetry(){
        return instance;
    }
    private static TankDrive instance;
    private Telemetry telemetry;
    public void addTelemetry(String string) {
        telemetry.addLine(string);
    }
    public void addTelemetry(String string, Object data) {
        telemetry.addData(string, data);
    }
    public void addSticky(String string){
        telemetry.log().add(string);
        telemetry.update();
    }
    public void addSticky(String string, Object data){
        telemetry.log().add(string, data);
        telemetry.update();
    }
    // MasqMotor and MasqMotor Systems
    public MasqMotorSystem driveTrain = new MasqMotorSystem("leftFront", "leftBack", "rightFront", "rightBack");
    //private MasqMotor collector = new MasqMotor("collector");
    public MasqMotor shooter = new MasqMotor("shooter");
    public MasqMotor shooter2 = new MasqMotor("shooter2");
    ///MasqClock
    //Servos
    private MasqCRServo rightPresser = new MasqCRServo("rightPresser");
    private MasqCRServo leftPresser = new MasqCRServo("leftPresser");
    private MasqMotor collector = new MasqMotor("collector");
    private MasqServo indexer = new MasqServo("indexer");
    //IMU
    public AdafruitIMU imu = new AdafruitIMU("imu");
    public MasqODS ods = new MasqODS("ods");
    //ColorSensor
    public MasqColorSensor rightColor = new MasqColorSensor("rightColor" , 62);
    public MasqColorSensor colorRejection = new MasqColorSensor("colorRejection", 64);
    public MasqColorSensor leftColor = new MasqColorSensor("leftColor", 60);
    //RangeSensor
    public MasqRangeSensor rangeSensor = new MasqRangeSensor("rangeSensor");
    private static final int DEFAULT_SLEEP_TIME = 500;
    private static final double DEFAULT_TIMEOUT = 3;
    public double angleLeftCover = 0;
    private boolean opModeIsActive() {
        return ((LinearOpMode) (FtcOpModeRegister.opModeManager.getActiveOpMode())).opModeIsActive();
    }
    public void drivePID(double power, int distance, Direction DIRECTION, int sleepTime, double targetAngle) {
        double angle = targetAngle;
        int newDistance = convert(distance);
        driveTrain.resetEncoders();
        driveTrain.setDistance((int)((-newDistance) * DIRECTION.value));
        driveTrain.runToPosition();
        while (driveTrain.rightIsBusy() && opModeIsActive()) {
            double newPowerLeft = power;
            double imuVal = imu.getHeading();
            double error = angle - imuVal;
            double errorkp = error * KP_STRAIGHT;
            newPowerLeft = (newPowerLeft - (errorkp) * DIRECTION.value);
            driveTrain.setPowerRight(power);
            driveTrain.setPowerLeft(newPowerLeft);
            TankDrive.getTelemetry().addTelemetry("Heading", imuVal);
            TankDrive.getTelemetry().addTelemetry("DistanceLeft", newDistance + driveTrain.getCurrentPos());
            telemetry.update();
        }
        driveTrain.StopDriving();
        driveTrain.runUsingEncoder();
        sleep(sleepTime);
    }
    public void drivePID(double power, int distance, double targetAngle, Direction DIRECTION) {
        drivePID(power, distance, DIRECTION, DEFAULT_SLEEP_TIME, targetAngle);
    }
    public void drivePID(double power, int distance, Direction DIRECTION, int sleepTime) {
        double targetAngle = imu.getHeading();
        int newDistance = convert(distance);
        driveTrain.resetEncoders();
        driveTrain.setDistance((int)((-newDistance) * DIRECTION.value));
        driveTrain.runToPosition();
        while (driveTrain.rightIsBusy() && opModeIsActive()) {
            double newPowerLeft = power;
            double imuVal = imu.getHeading();
            double error = targetAngle - imuVal;
            double errorkp = error * KP_STRAIGHT;
            newPowerLeft = (newPowerLeft - (errorkp) * DIRECTION.value);
            driveTrain.setPowerRight(power);
            driveTrain.setPowerLeft(newPowerLeft);
            TankDrive.getTelemetry().addTelemetry("Heading", imuVal);
            TankDrive.getTelemetry().addTelemetry("DistanceLeft", newDistance + driveTrain.getCurrentPos());
            telemetry.update();
        }
        driveTrain.StopDriving();
        driveTrain.runUsingEncoder();
        sleep(sleepTime);
    }
    public void drivePID(double power, int distance, Direction DIRECTION) {
        drivePID(power, distance, DIRECTION, DEFAULT_SLEEP_TIME);
    }
    public void turnPID(double power, int angle, Direction DIRECTION, double timeout, double ki) {
        double targetAngle = imu.adjustAngle(imu.getHeading() + (DIRECTION.value * angle));
        double acceptableError = 0.5;
        double currentError = 1;
        double prevError = 0;
        double integral = 0;
        double newPower = power;
        double previousTime = 0;
        MasqClock clock = new MasqClock("clock");
        while (opModeIsActive() && (imu.adjustAngle(Math.abs(currentError)) > acceptableError) && !clock.elapsedTime(timeout, MasqClock.Resolution.SECONDS)) {
            double tChange = System.nanoTime() - previousTime;
            previousTime = System.nanoTime();
            tChange = tChange / 1e9;
            double imuVAL = imu.getHeading();
            currentError = imu.adjustAngle(targetAngle - imuVAL);
            integral += currentError  * ID;
            double errorkp = currentError * KP_TURN;
            double integralki = currentError * ki * tChange;
            double dervitive = (currentError - prevError) / tChange;
            double dervitivekd = dervitive * KD_TURN;
            newPower = (errorkp + integralki + dervitivekd);
            driveTrain.setPowerRight(-newPower);
            driveTrain.setPowerLeft(newPower);
            prevError = currentError;
            TankDrive.getTelemetry().addTelemetry("TargetAngle", targetAngle);
            TankDrive.getTelemetry().addTelemetry("Heading", imuVAL);
            TankDrive.getTelemetry().addTelemetry("AngleLeftToCover", currentError);
            telemetry.update();
        }
        driveTrain.StopDriving();
        sleep(1000);
    }
    public void turnPID(double power, int angle, Direction DIRECTION, double timeOut,  int sleepTime) {
        double targetAngle = imu.adjustAngle(imu.getHeading() + (DIRECTION.value * angle));
        double acceptableError = 0.5;
        double currentError = 1;
        double prevError = 0;
        double integral = 0;
        double newPower = power;
        double previousTime = 0;
        MasqClock clock = new MasqClock("clock");
        while (opModeIsActive() && (imu.adjustAngle(Math.abs(currentError)) > acceptableError) && !clock.elapsedTime(timeOut, MasqClock.Resolution.SECONDS)) {
            double tChange = System.nanoTime() - previousTime;
            previousTime = System.nanoTime();
            tChange = tChange / 1e9;
            double imuVAL = imu.getHeading();
            currentError = imu.adjustAngle(targetAngle - imuVAL);
            integral += currentError  * ID;
            double errorkp = currentError * KP_TURN;
            double integralki = currentError * KI_TURN * tChange;
            double dervitive = (currentError - prevError) / tChange;
            double dervitivekd = dervitive * KD_TURN;
            newPower = (errorkp + integralki + dervitivekd);
            driveTrain.setPowerRight(-newPower);
            driveTrain.setPowerLeft(newPower);
            prevError = currentError;
            TankDrive.getTelemetry().addTelemetry("TargetAngle", targetAngle);
            TankDrive.getTelemetry().addTelemetry("Heading", imuVAL);
            TankDrive.getTelemetry().addTelemetry("AngleLeftToCover", currentError);
            angleLeftCover = currentError;
            telemetry.update();
        }
        driveTrain.StopDriving();
        sleep(sleepTime);
    }
    public void turnPID(double power, int angle, Direction DIRECTION, double timeout)  {
        turnPID(power, angle, DIRECTION, timeout, DEFAULT_SLEEP_TIME);
    }
    public void turnPID(double power, int angle, Direction DIRECTION)  {
        turnPID(power, angle, DIRECTION, DEFAULT_TIMEOUT);
    }
    public void drivePIDRange(double power, int distance, int sleepTime) {
        double targetAngle = imu.getHeading();
        driveTrain.runUsingEncoder();
        while (rangeSensor.rawUltrasonic() > distance && opModeIsActive()) {
            double newPowerLeft = power;
            double imuVal = imu.getHeading();
            double error = targetAngle - imuVal;
            double errorkp = error * KP_STRAIGHT;
            newPowerLeft = (newPowerLeft - (errorkp));
            driveTrain.setPowerRight(-power);
            driveTrain.setPowerLeft(-newPowerLeft);
            TankDrive.getTelemetry().addTelemetry("Heading", imuVal);
            TankDrive.getTelemetry().addTelemetry("Ultrasonic", rangeSensor.rawUltrasonic());
            telemetry.update();
        }
        driveTrain.StopDriving();
        driveTrain.runWithoutEncoders();
        sleep(sleepTime);
    }
    public void drivePIDRange(double power, int distance ) {
        drivePIDRange(power, distance, DEFAULT_SLEEP_TIME);
    }
    public void setBrakeMode(int time) {
        driveTrain.setBrakeMode();
        sleep(time);
    }
    public void stopRed(double power, Direction Direction) {
        driveTrain.runUsingEncoder();
        double targetAngle = imu.getHeading();
        while (!(leftColor.isRed()) && opModeIsActive()) {
            double newPower = power;
            double heading = imu.getHeading();
            double error = targetAngle - heading;
            double errorkp = error * KP_STRAIGHT;
            newPower = newPower - (errorkp * Direction.value);
            driveTrain.setPowerLeft(power * Direction.value);
            driveTrain.setPowerRight(newPower * Direction.value);
            TankDrive.getTelemetry().addTelemetry("Heading", heading);
            TankDrive.getTelemetry().addTelemetry("red Val", leftColor.colorNumber());
            telemetry.update();
        }
        driveTrain.StopDriving();
    }
    public void stopBlue(double power, Direction Direction) {
        driveTrain.runUsingEncoder();
        double targetAngle = imu.getHeading();
        while ((!leftColor.isBlue()) && opModeIsActive()){
            double newPower = power;
            double heading = imu.getHeading();
            double error = targetAngle - heading;
            double errorkp = error * KP_STRAIGHT;
            newPower = newPower - (errorkp * Direction.value);
            driveTrain.setPowerLeft(power * Direction.value);
            driveTrain.setPowerRight(newPower * Direction.value);
            TankDrive.getTelemetry().addTelemetry("Heading", heading);
            TankDrive.getTelemetry().addTelemetry("Blue Val", leftColor.colorNumber());
            telemetry.update();
        }
        driveTrain.StopDriving();
    }
    public void stopRedRight(double power, Direction Direction) {
        driveTrain.runUsingEncoder();
        double targetAngle = imu.getHeading();
        while (!(rightColor.isRed()) && opModeIsActive()) {
            double newPower = power;
            double heading = imu.getHeading();
            double error = targetAngle - heading;
            double errorkp = error * KP_STRAIGHT;
            newPower = newPower - (errorkp * Direction.value);
            driveTrain.setPowerLeft(power * Direction.value);
            driveTrain.setPowerRight(newPower * Direction.value);
            TankDrive.getTelemetry().addTelemetry("Heading", heading);
            TankDrive.getTelemetry().addTelemetry("red Val", leftColor.colorNumber());
            telemetry.update();
        }
        driveTrain.StopDriving();
    }
    public void stopBlueRight(double power, Direction Direction) {
        driveTrain.runUsingEncoder();
        double targetAngle = imu.getHeading();
        while ((!rightColor.isBlue()) && opModeIsActive()){
            double newPower = power;
            double heading = imu.getHeading();
            double error = targetAngle - heading;
            double errorkp = error * KP_STRAIGHT;
            newPower = newPower - (errorkp * Direction.value);
            driveTrain.setPowerLeft(power * Direction.value);
            driveTrain.setPowerRight(newPower * Direction.value);
            TankDrive.getTelemetry().addTelemetry("Heading", heading);
            TankDrive.getTelemetry().addTelemetry("Blue Val", leftColor.colorNumber());
            telemetry.update();
        }
        driveTrain.StopDriving();
    }
    public void stopWhite (double power, Direction Direction) {
        driveTrain.runUsingEncoder();
        double targetAngle = imu.getHeading();
        while ((!colorRejection.isWhite() || !ods.isWhite()) && opModeIsActive()) {
            double newPower = power;
            double heading = imu.getHeading();
            double error = targetAngle - heading;
            double errorkp = error * KP_STRAIGHT;
            newPower = newPower - (errorkp * Direction.value);
            driveTrain.setPowerLeft(power * Direction.value);
            driveTrain.setPowerRight(newPower * Direction.value);
            TankDrive.getTelemetry().addTelemetry("Heading", heading);
            TankDrive.getTelemetry().addTelemetry("red Val", leftColor.colorNumber());
            telemetry.update();
        }
        driveTrain.StopDriving();
    }
    //rangeSensor
    //setPower
    public void setPowerLeft(double power) {
        driveTrain.setPowerLeft(power);
    }
    public void setPowerRight(double power) {
        driveTrain.setPowerRight(power);
    }
    public void setPowerCollector(double powerCollector) {
        collector.setPower(powerCollector);
    }
    public void setPowerShooter(double powerShooter){
      shooter2.runWithoutEncoders();
      shooter.runWithoutEncoders();
      shooter2.setPower(powerShooter);
      shooter.setPower(powerShooter);
    }
    public void resetShooterEncoder () {
        shooter.resetEncoder();
        shooter2.resetEncoder();
    }
    public void sleep() {
        sleep(1000);
    }
    public void sleep(int time) {
        try {
            Thread.sleep((long) time);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

    //servos
    public void setIndexer(double position) {
     indexer.setPosition(position);
    }
    public void setRightPresser(double power) {
        rightPresser.setPower(power);
    }
    public void setLeftPresser(double power) {
        power *= -1;
        leftPresser.setPower(power);
    }
    public void runAllTelemetry() {
        runSensorTelemetry();
        driveTrain.telemetryRun();
        collector.telemetryRun(false);
        shooter.telemetryRun(true);
        rightPresser.telemetryRun();
        indexer.telemetryRun();
    }
    public void runSensorTelemetry () {
        imu.telemetryRun();
        leftColor.telemetryRun();
        rightColor.telemetryRun();
        colorRejection.telemetryRun();
    }




}
