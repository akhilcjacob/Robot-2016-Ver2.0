package org.usfirst.frc.team2791.commands;

import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import org.usfirst.frc.team2791.util.ShakerCamera.ParticleReport;

import static org.usfirst.frc.team2791.robot.Robot.*;

public class AutoLineUpShot extends ShakerCommand implements Runnable {
    private static final double angleMaxOutput = 0.5;
    // to correct any curving of the shot leftward or right ward
    public static double shootOffset = 0.5;
    // this is the counter that decides what stop to run in the auto lineup
    // process
    private static int autoLineUpCounter = 0;
    // target angle during the entire process
    private static double target = 0;
    // just to count how many frames we used to lineup
    private static int frames_used = 0;
    private static ParticleReport currentTarget;
    private static Timer totalTime = new Timer();

    private static boolean useMultipleFrames = false;
    private static boolean shootAfterAligned = false;

    public AutoLineUpShot() {

    }

    private static void resetAndStartTimer() {
        totalTime.reset();
        totalTime.start();
    }

    public static boolean isRunning() {
        return running;
    }

    public void setUseMultipleFrames(boolean value) {
        useMultipleFrames = value;
    }

    public void setShootAfterAligned(boolean value) {
        shootAfterAligned = value;
    }

    public boolean setUseMultipleFrames() {
        return useMultipleFrames;
    }

    public boolean setShootAfterAligned() {
        return shootAfterAligned;
    }

    public void overrideAutoLineUp() {
        autoLineUpCounter = 30;
    }

    public void reset() {
        //This sets the camera to automatically update to the dash again
        camera.setAutomaticCaptureAndUpdate();
        //restart the totalTime that counts how long the whole process takes
        resetAndStartTimer();
        //set the running flag to false
        running = false;
        //reset the counter
        autoLineUpCounter = 0;
        //stop all shooter stuff
        shooterWheels.resetShooterFlags();
        //run method flags
        useMultipleFrames = false;
        shootAfterAligned = false;
    }

    public void start() {
        if (!running) {
            //makes sure everything is fresh
            reset();
            //sets the running boolean to true
            running = true;
            //puts camera into manual mode meaning take frame by frame when requested
            camera.setManualCapture();
            //actually run the code... this should run on its own thread
            run();
        }
    }

    @Override
    public void updateSmartDash() {

    }

    @Override
    public void debug() {

    }

    public void run() {
        while (running) {
            SmartDashboard.putNumber("Auto Line Up step: ", autoLineUpCounter);
            //this waits on the camera thread for the new image to be gotten and processed

            switch (autoLineUpCounter) {
                default:
                case 0:
                    // only run if there is a target available
                    reUpdateCurrentTarget();
                    driveTrain.resetEncoders();

                        /*prep the shot, runs the shooter wheels to setpoint
                        saves time in firing the useMultipleFrames is there because we always fire if
                        we're using multiple frames*/
                    if (shootAfterAligned || useMultipleFrames)
                        shooterWheels.prepShot();

                    // the target angle == current angle + targetAngleDiff + offset
                    target = driveTrain.getAngle() + currentTarget.ThetaDifference + shootOffset;
                    // Print out the values for debugging
                    System.out.print("Last System Out was " + totalTime.get());
                    System.out.println("my target is " + target + " current angle is " + driveTrain.getAngle()
                            + "the shooter offset is " + shootOffset + " t:" + totalTime.get());
                    resetAndStartTimer();
                    // we used one frame so far
                    frames_used = 1;
                    // go to next step after this
                    if (useMultipleFrames) {
                        if (shootAfterAligned) {
                            autoLineUpCounter = 20; // this is the double check and
                            // shoot case
                        } else {

                            System.out.println(
                                    "We have no code to line up with multiple frame and not shoot. Shooting anyway. t:"
                                            + totalTime.get());
                            autoLineUpCounter = 20;
                            resetAndStartTimer();
                        }
                    } else {
                        if (shootAfterAligned) {
                            autoLineUpCounter = 10; // use 1 frame and shoot
                        } else {
                            autoLineUpCounter = 15; // use 1 frame and don't shoot
                        }
                    }


                    break;

                case 10: // this is the single line up and shoot case
                    // set the drive train to the target angle, will return true when
                    // reached there
                    if (driveTrain.setAngle(target, angleMaxOutput, true) && shooterWheels.shooterAtSpeed()) {
                        // for debugging
                        shooterWheels.completeShot();

                        System.out.print("Last System Out was " + totalTime.get());
                        System.out.println("I'm trying to get to " + target + " I got to " + driveTrain.getAngle()
                                + "\n    angle-target= " + (driveTrain.getAngle() - target));
                        autoLineUpCounter = 30;
                    }
                    //
                    break;

                case 15: // this is the single line up and don't shoot case
                    if (driveTrain.setAngle(target, angleMaxOutput, true)) {
                        // for debugging

                        System.out.print("Last System Out was " + totalTime.get());
                        System.out.println("I'm trying to get to " + target + " I got to " + driveTrain.getAngle()
                                + "\n    angle-target= " + (driveTrain.getAngle() - target));
                        autoLineUpCounter = 40;
                    }

                    break;

                case 20:
                    // here we check if our current angele is good enough
                    // if now we reset out target using the latest camera image
                    // and try to drive to it
                    if (driveTrain.setAngle(target, angleMaxOutput, true)) { // keep the
                        // drivetrain
                        System.out.println("I'm trying to get to " + target + " I got to " + driveTrain.getAngle()
                                + "\n    angle-target= " + (driveTrain.getAngle() - target));
                        reUpdateCurrentTarget();
                        frames_used++;
                        // engaged
                        // double check that we are close to the target angle
                        // if we got a new frame process it

                        double camera_error = currentTarget.ThetaDifference + shootOffset;
                        System.out.println("Double check camera error: " + camera_error);
                        // if error is minimal shoot
                        if (Math.abs(camera_error) < 0.75 && shooterWheels.shooterAtSpeed()) {
                            // go to the next step
                            // shoot whenever ready
                            System.out.println(
                                    "I've found a good angle and am going to busy it while the shooter spins up. t:"
                                            + totalTime.get());
                            shooterWheels.completeShot();
                            // we should be firing when the auto fire method is
                            // called
                            // the state we jump to will busy our angle until
                            // the auto fire method
                            // is completed
                            autoLineUpCounter = 30;
                        } else {
                            if (!shooterWheels.shooterAtSpeed()) {
                                System.out.println("I am waiting on the shooter wheels t:" + totalTime.get());
                            }
                            if (!(Math.abs(camera_error) < 0.75)) {
                                System.out.println("I am waiting on camera error t:" + totalTime.get());
                            }
                            // too much error so we're going to drive again
                            // update the target and the setAngle in the if
                            // statement in the top of
                            // this method will move us
                            target = driveTrain.getAngle() + currentTarget.ThetaDifference + shootOffset;
                        }

                    } // endif driveTrain.setAngle
                    break;

                case 30:
                    // keep the same angle until we are done shooting
                    if (driveTrain.setAngle(target, angleMaxOutput) && shooterWheels.shooterAtSpeed()) {
                        if (!shooterWheels.getIfCompleteShot()) {
                            // only run once the shot is finished

                            System.out.println("done shooting t:" + totalTime.get());
                            // if done running go to the next step
                            autoLineUpCounter = 40;
                        }
                    }
                    break;

                case 40:
                    // reset everything
                    System.out.println("Finished auto line up and resetting. t:" + totalTime.get());
                    // the fames_used + 1 is to include the check frame
                    System.out.println("I took " + (frames_used + 1) + " frames to shoot");
                    reset();
                    break;
            }
        }
    }

    /**
     * Through threading this method tells the camera thread to get
     * a new frame and then waits on it to process it, when the camera thread is
     * done it will send a notification to this thread which will then update the getTarget
     * <p>
     * This solves problems we had earlier about having the new frame be the same as the previous frame
     * it also lets us process the frame on the camera thread
     */
    public void reUpdateCurrentTarget() {
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
        if (currentTarget == null) {
            System.out.println("Target Reports are empty so aborting.");
            autoLineUpCounter = 40;
        }
    }
}