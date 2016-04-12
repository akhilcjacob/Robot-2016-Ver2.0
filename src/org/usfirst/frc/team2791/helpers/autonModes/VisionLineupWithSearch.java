package org.usfirst.frc.team2791.helpers.autonModes;

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

    public VisionLineupWithSearch(double distance) {
        firstDistance = distance;
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
                if (driveTrain.setDistance(firstDistance, 0, 0.65, false)) {
                    // intake.setArmAttachmentDown();
                    System.out.println("Drove the first distance");
                    driveTrain.resetEncoders();
                    shooterArm.setShooterMiddle();
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
                System.out.println("Starting autoLineup");
                state++;
                break;
            case 5:
                if (!AutoLineUpShot.isRunning()) {
                    state++;
                    System.out.println("Auto lineup is no longer running and finishing up");
                } else
                    visionShot.run();
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
}
