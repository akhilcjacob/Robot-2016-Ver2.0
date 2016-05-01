package org.usfirst.frc.team2791.helpers.autonModes;

import edu.wpi.first.wpilibj.Timer;

import static org.usfirst.frc.team2791.robot.Robot.*;

/**
 * Created by Akhil on 4/11/2016.
 * This class will run a sort of search pattern to look for the target
 * if it doens't find it, this is in-case that over the defense the bot
 * gets off angle
 */
public class VisionLineupWithSearch extends AutonMode {
    private double firstDistance;
    private double multiplier = -1.25;//This is how much the search angle increases by if no target is found
    private double angle = 30;
    private Timer visionLineUpTimer;

    public VisionLineupWithSearch(double distance, double turnAngle) {
        firstDistance = distance;
        angle = -turnAngle;
        visionLineUpTimer = new Timer();
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
                    state++;
                }
                break;
            case 3:
                visionShot.setShootAfterAligned(true);
                visionShot.setUseMultipleFrames(true);
                visionShot.start();
                visionLineUpTimer.reset();
                visionLineUpTimer.start();
                System.out.println("Starting autoLineup");
                state++;
                break;
            case 4:
                if (camera.getTarget() == null && !visionShot.isRunning() && visionLineUpTimer.get() < 1) {
                    //That means that autolineup probably ran too quickly so do a quick turn
                    //to scan nearby
                    state++;
                } else if (!visionShot.isRunning()) {
                    state = 7;
                    System.out.println("Auto lineup is no longer running and finishing up");
                }
                break;
            case 5:
                //set the drive train to look rightward first then leftward
                if (driveTrain.setAngle(angle *= multiplier, maxOutput, false, true)) {
                    visionLineUpTimer.reset();
                    visionLineUpTimer.start();
                    state = 4;
                }
                break;
            case 6:
                if (driveTrain.setAngle(-angle, maxOutput, false, true)) {
                    driveTrain.resetEncoders();
                    state++;
                }
                break;
            case 7:
                //dive back the original distance minus some amount just incase not to cross the line
                if (driveTrain.setDistance(-firstDistance - .75, 0, maxOutput, false, true))
                    state++;
                break;
            case 8:
                //turn around
                if (driveTrain.setAngle(180, maxOutput, false, true)) {
                    state++;
                }
                break;
            case 9:
                visionShot.reset();
                System.out.println("I am done with the drive straight auto");
                driveTrain.resetEncoders();
                state = 0;
                break;
        }
    }
}

