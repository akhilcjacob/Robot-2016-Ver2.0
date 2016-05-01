package org.usfirst.frc.team2791.helpers.autonModes;

import edu.wpi.first.wpilibj.Timer;

import static org.usfirst.frc.team2791.robot.Robot.*;

//import org.usfirst.frc.team2791.

/**
 * Created by team2791 on 3/15/2016.
 * This is a low bar 20 pt auton, this really hasnt actually fully worked, but weve never needed it
 */
public class BasicCloseAuton extends AutonMode {
    private double firstDistance;
    private double turnToAngle;
    private double secondDistance;

    private Timer timer = new Timer();

    public BasicCloseAuton(double firstTravelDistance, double turnAngle, double secondTravelDistance) {
        this.firstDistance = firstTravelDistance;
        this.turnToAngle = turnAngle;
        this.secondDistance = secondTravelDistance;
    }

    public void run() {
        switch (state) {
            case 0:
                driveTrain.disable();
                shooterWheels.stopMotors();
                timer.reset();
                break;
            case 1:
                System.out.println("Starting basic close auto.");
                driveTrain.resetEncoders();
                intake.extendIntake();
                timer.reset();
                timer.start();
                state++;
            case 2:
                if (intake.getIfExtended()) {
                    System.out.println("Intake down, starting my first drive.");
                    // go to next state
                    state++;
                }
                break;
            case 3:
                if (driveTrain.setDistance(firstDistance / 2, 0, maxOutput, false, false)) {
                    System.out.println("Finished driving, now time to raise the shooter.");
                    // if reached the distance then reset the encoders

                    // raise the arm and continue to the next case

                    timer.reset();
                    state++;
                }
                break;
            case 4:
                if (driveTrain.setDistance(firstDistance, 0, 1, false, false)) {
                    System.out.println("Finished driving, now time to raise the shooter.");
                    // if reached the distance then reset the encoders
                    driveTrain.resetEncoders();

                    // raise the arm and continue to the next case

                    timer.reset();
                    state++;
                }
                break;
            case 5:
                // allow 1s for the arm to rise
                if (timer.get() > 1) {
                    shooterWheels.stopMotors();
                    state++;
                }
                break;
            case 6:
                if (driveTrain.setAngle(turnToAngle, 0.7)) {
                    // if reached the angle target then reset encoders
                    driveTrain.resetEncoders();
                    shooterArm.setHigh();
                    // continue to the next case
                    state++;
                    System.out.println("Finsihed turn now going to drive " + secondDistance);
                }
                break;
            case 7:
                if (driveTrain.setDistance(secondDistance, 0, 1, false, false)) {
                    // after reaching the final distance fire
                    shooterWheels.completeShot();
                    state++;
                }
                break;
            case 8:
                if (!shooterWheels.getIfCompleteShot()) {
                    // if the shooter is done firing reset
                    System.out.println("I am done with the basic close auton");
                    state = 0;
                }
                break;
        }
    }
}