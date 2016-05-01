package org.usfirst.frc.team2791.commands;


import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import org.usfirst.frc.team2791.abstractSubsystems.AbstractShakerShooterArm;
import org.usfirst.frc.team2791.util.ShakerCamera;

import static org.usfirst.frc.team2791.robot.Robot.*;

/**
 * Created by Akhil on 4/27/2016.
 * This is the code that uses the values from the camera to turn and lineup and fire
 */
public class AutoLineUpShot extends ShakerCommand implements Runnable {
    //This will decide which case to run
    private static final int STAGE_ONE = 0;
    //use one frame to lineup to the target and shoot
    private static final int SINGLE_FRAME_SHOT = 1;
    //single frame no shoot
    private static final int SINGLE_FRAME_LINEUP = 2;
    //multiple frames and shoot
    private static final int MULTIPLE_FRAME_SHOOT = 3;
    //special case after shooting
    private static final int AFTER_SHOT_CLEANUP = 4;
    //reset for anything than ran
    private static final int GENERAL_RESET = 5;
    private static final double angleMaxOutput = 0.6;
    //Settings
    // to correct any curving of the shot leftward or right ward
    public static double shootOffset = 0.5;
    //Run method flags
    private static boolean useMultipleFrames = false;
    private static boolean shootAfterAligned = false;
    private static boolean quickLineUpShot = false;
    //internal values
    private double targetTurnAngle = 0;
    private ShakerCamera.ParticleReport currentTarget;
    private Timer totalTime;
    private double frames_used = 0;

    public AutoLineUpShot() {
        totalTime = new Timer();
    }


    public void run() {
        while (running) {
            SmartDashboard.putNumber("Vision Shot stage: ", counter);
            switch (counter) {
                default:
                case STAGE_ONE:
                    //reset the number of frames that have been used
                    frames_used = 0;
                    //get a new frame
                    reUpdateCurrentTarget();
                    //reset encoders because that is what we used to turn
                    driveTrain.resetEncoders();
                    /*prep the shot, runs the shooter wheels to setpoint
                      saves time in firing the useMultipleFrames is there because we always fire if
                      we're using multiple frames*/
                    if (shootAfterAligned || useMultipleFrames)
                        shooterWheels.prepShot();
                    /*This decides what case to call depending on
                    the flags that are set true;
                     */
                    if (useMultipleFrames) {
                        if (shootAfterAligned)
                            counter = MULTIPLE_FRAME_SHOOT;
                        else {
                            printTimeStamp();
                            System.out.println(
                                    "We have no code to line up with multiple frame and not shoot. Shooting anyway.");
                            counter = MULTIPLE_FRAME_SHOOT;
                        }
                    } else {
                        if (shootAfterAligned) counter = SINGLE_FRAME_SHOT;
                        else counter = SINGLE_FRAME_LINEUP;
                    }
                    totalTime.reset();
                    totalTime.start();
                    debugSystemOut();
                    break;

                case SINGLE_FRAME_SHOT:
                    //uses the single frame and fires
                    if (driveTrain.setAngle(targetTurnAngle, angleMaxOutput, true, true)) {
                        //after the desired angle is reached it will do a complete shot
                        shooterWheels.completeShot();
                        debugSystemOut();
                        counter = AFTER_SHOT_CLEANUP;
                    }
                    break;
                case SINGLE_FRAME_LINEUP:
                    if (driveTrain.setAngle(targetTurnAngle, angleMaxOutput, true, true)) {
                        debugSystemOut();
                        counter = GENERAL_RESET;
                    }
                    break;
                case MULTIPLE_FRAME_SHOOT:
                    if (driveTrain.setAngle(targetTurnAngle, angleMaxOutput, true, true)) {
                        reUpdateCurrentTarget();
                        double camera_error = currentTarget.optimalTurnAngle + shootOffset;
                        double camera_error_threshold = 0.75;
                        if (quickLineUpShot)
                            camera_error_threshold = 1.5;
                        if (Math.abs(camera_error) < camera_error_threshold) {
                            printTimeStamp();
                            System.out.println("I've found a good angle and am " +
                                    "going to busy it while the shooter spins up.");
                            shooterWheels.completeShot();
                            counter = AFTER_SHOT_CLEANUP;
                        } else if (!(Math.abs(camera_error) < camera_error_threshold)) {
                            printTimeStamp();
                            System.out.println("I am waiting on camera error");
                            //the error is still greater than the thresh so update then angle value
                            targetTurnAngle = driveTrain.getAngle() + currentTarget.optimalTurnAngle + shootOffset;
                        }
                    }
                    break;
                case AFTER_SHOT_CLEANUP:
                    // keep the same angle until we are done shooting
                    if (driveTrain.setAngle(targetTurnAngle, angleMaxOutput, true, true)) {
                        if (!shooterWheels.getIfCompleteShot()) {
                            printTimeStamp();
                            System.out.println("Done shooting and bringing arm down");
                            //once we are done shooting do a reset
                            IntakeAndShooterSynergy.setPosition(AbstractShakerShooterArm.ShooterHeight.LOW);
                            counter = GENERAL_RESET;
                        }
                    }
                    break;
                case GENERAL_RESET:
                    // reset everything
                    printTimeStamp();
                    System.out.println("Finished auto line up and resetting.");
                    System.out.println("I took " + frames_used + " frames to shoot");
                    reset();
                    break;
            }
            try {
                Thread.sleep(100);//Run @ a 100 hz
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    /***
     * INTERNAL RUN METHODS
     ****/

    /**
     * Through threading this method tells the camera thread to get
     * a new frame and then waits on it to process it, when the camera thread is
     * done it will send a notification to this thread which will then update the getTarget
     * <p>
     * This solves problems we had earlier about having the new frame be the same as the previous frame
     * it also lets us process the frame on the camera thread
     */
    private void reUpdateCurrentTarget() {
        synchronized (cameraThread) {
            camera.getNextFrame();
            try {
                cameraThread.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
                run();
            }
        }
        currentTarget = camera.getTarget();
        frames_used++;
        if (currentTarget == null) {
            System.out.println("Target Reports are empty so aborting.");
            counter = 40;
            return;
        } else
            // the target angle == current angle + targetAngleDiff + offset
            targetTurnAngle = driveTrain.getAngle() + currentTarget.optimalTurnAngle + shootOffset;

    }

    private void printTimeStamp() {
        System.out.print("TimeStamp: " + totalTime.get());
    }

    private void debugSystemOut() {
        printTimeStamp();
        System.out.println(" My target is: " + targetTurnAngle + " Current angle is: " + driveTrain.getAngle()
                + " Shooter offset is: " + shootOffset);

    }

    /**************
     * End Internal run methods
     ****************/


    public void setUseMultipleFrames(boolean value) {
        //This will use multiple frames to lineup and fire
        useMultipleFrames = value;
    }

    public void setShootAfterAligned(boolean value) {
        //this will control whether to shoot after the lineup
        shootAfterAligned = value;
    }

    public void setQuickLineUpShot(boolean value) {
        //increases the camera error on the mutipleframe case for faster lineup
        quickLineUpShot = value;
    }

    public void start() {
        if (!running) {
            //sets the running boolean to true
            running = true;
            //puts camera into manual mode meaning take frame by frame when requested
            camera.setManualCapture();
            //actually run the code... this should run on its own thread
            run();
        }
    }

    public void reset() {
        //This sets the camera to automatically update to the dash again
        camera.setAutomaticCaptureAndUpdate();
        //restart the totalTime that counts how long the whole process takes
        totalTime.reset();
        totalTime.stop();
        //set the running flag to false
        running = false;
        //reset the counter
        counter = STAGE_ONE;
        //stop all shooter stuff
        shooterWheels.resetShooterFlags();
        //run method flags
        useMultipleFrames = false;
        shootAfterAligned = false;
        quickLineUpShot = false;
        driveTrain.forceBreakPID();
    }

    public void updateSmartDash() {
        //This wasn't necessary either because we chose to spam System.out with info
    }

    public void debug() {
        //There was really nothing to put here....cuz who needs debugging
    }
}
