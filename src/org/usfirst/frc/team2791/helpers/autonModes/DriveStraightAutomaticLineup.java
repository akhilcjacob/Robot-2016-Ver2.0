package org.usfirst.frc.team2791.helpers.autonModes;

import static org.usfirst.frc.team2791.robot.Robot.*;

/**
 * Created by team2791 on 3/17/2016.
 * This auton drives straight,turns some angle,then uses vision to shoot
 * Distance and angle are set via the constructor
 */
public class DriveStraightAutomaticLineup extends AutonMode {
    private double firstDistance;
    private double turnToAngle;

    public DriveStraightAutomaticLineup(double distance, double angle) {
        firstDistance = distance;
        turnToAngle = angle;
    }

    public void run() {
        switch (state) {
            case 0:
                driveTrain.disable();
                shooterWheels.stopMotors();
                break;
            case 1:
                System.out.println("Starting the drive straight autoLinup ");
                driveTrain.resetEncoders();
                intake.extendIntake();
                shooterArm.setMiddle();
                state++;
                break;
            case 2:
                if (driveTrain.setDistance(firstDistance, 0, 0.65, false, false)) {
                    // intake.setArmAttachmentDown();
                    System.out.println("Drove the first distance");
                    driveTrain.resetEncoders();
                    shooterWheels.shooterArmMoving();
                    state++;
                }
                break;
            case 3:
                if (driveTrain.setAngle(turnToAngle, 0.6)) {
                    driveTrain.resetEncoders();
                    state++;
                }
                break;

            case 4:
                visionShot.setShootAfterAligned(true);
                visionShot.setUseMultipleFrames(true);
                visionShot.setQuickLineUpShot(false);
                visionShot.start();
                System.out.println("Starting autoLineup");
                state++;
                break;
            case 5:
                if (!visionShot.isRunning()) {
                    state++;
                    System.out.println("Auto lineup is no longer running and finishing up");
                }
                break;
            case 6:
                visionShot.reset();
                System.out.println("I am done with the drive striaght auto");
                driveTrain.resetEncoders();
                state = 0;
                break;
        }
    }
}