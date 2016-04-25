package org.usfirst.frc.team2791.helpers.autonModes;

import edu.wpi.first.wpilibj.Timer;
import org.usfirst.frc.team2791.commands.AutoLineUpShot;

import static org.usfirst.frc.team2791.robot.Robot.*;

/**
 * Created by Akhil on 4/11/2016.
 * This class will run a sort of search pattern to look for the target
 * if it doens't find it, this is in-case that over the defense the bot
 * gets off angle
 */
public class VisionLineupWithSearch extends AutonMode {
    private double firstDistance;
    private double multiplier = -1;
    private double angle = 30;
    private Timer visionLineUpTimer;
    private double angleTurnAccum = 0;

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
                if (driveTrain.setDistance(firstDistance, 0, 0.65, false, false)) {
                    // intake.setArmAttachmentDown();
                    System.out.println("Drove the first distance");
                    driveTrain.resetEncoders();
                    shooterArm.setMiddle();
                    state++;
                }
                break;
            case 3:
                //LOGIC THAT GOES HERE
                //if there is no target go 20degrees positive
                //if still no target then go -20 degrees

            case 4:
                visionShot.setShootAfterAligned(true);
                visionShot.setUseMultipleFrames(true);
                visionShot.start();
                visionLineUpTimer.reset();
                visionLineUpTimer.start();
                System.out.println("Starting autoLineup");
                state++;
                break;
            case 5:
                if (camera.getTarget() == null && visionLineUpTimer.get() < 1) {
                    //That means that autolineup probably ran too quickly so do a quick turn
                    //to scan nearby
                    state++;
                } else if (!AutoLineUpShot.isRunning()) {
                    state = 7;
                    System.out.println("Auto lineup is no longer running and finishing up");
                }
                break;
            case 6:
                //set the drive train to look rightward first then leftward
                if (driveTrain.setAngle(angle *= multiplier, 0.65, false, true)) {
                    state = 4;
                    angleTurnAccum = angle;
                }
                break;
            case 7:
                //zero the angle
                if (driveTrain.setAngle(-angleTurnAccum, 0.65, false, true))
                    state++;
                break;
            case 8:
                if (driveTrain.setDistance(-firstDistance, 0, .65, false, true))
                    state++;
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

