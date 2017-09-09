package org.firstinspires.ftc.teamcode.TeleOp;

import Library4997.MasqWrappers.MasqLinearOpMode;

/**
 * Created by Archish on 9/8/17.
 */

public class RelicTeleOp extends MasqLinearOpMode {
    @Override
    public void runLinearOpMode() throws InterruptedException {
        while (!opModeIsActive()){
            dash.create(robot.imu);
        }
        while (opModeIsActive()){
            robot.NFS(controller1);
        }
    }
}