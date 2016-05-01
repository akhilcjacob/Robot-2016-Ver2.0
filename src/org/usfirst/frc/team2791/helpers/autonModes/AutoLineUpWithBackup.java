package org.usfirst.frc.team2791.helpers.autonModes;

import static org.usfirst.frc.team2791.robot.Robot.*;

/**
 * Created by Akhil on 4/25/2016.
 * This does the regular automatic lineup but also recrosses the defense backward
 */
public class AutoLineUpWithBackup extends AutonMode {
    private double firstDistance;
    private double turnToAngle;

    public AutoLineUpWithBackup(double distance, double angle) {
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
                state++;
                break;
            case 2:
                if (driveTrain.setDistance(firstDistance, 0, maxOutput, false, false)) {
                    // intake.setArmAttachmentDown();
                    System.out.println("Drove the first distance");
                    driveTrain.resetEncoders();
                    shooterArm.setMiddle();
                    //wait .3 seconds before continuing to give arm some time to move to location
                    try {
                        Thread.sleep(300);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    state++;
                }
                break;
            case 3:
                if (driveTrain.setAngle(turnToAngle, maxOutput)) {
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
                if (driveTrain.setAngle(-turnToAngle, maxOutput, false, false)) {
                    driveTrain.resetEncoders();
                    state++;
                }
                break;
            case 7:
                if (driveTrain.setDistance(-firstDistance, 0, maxOutput, false, false)) {
                    state++;
                }
                break;
            case 8:
                visionShot.reset();
                System.out.println("I am done with the drive straight auto");
                driveTrain.resetEncoders();
                state = 0;
                break;
        }
    }
}
