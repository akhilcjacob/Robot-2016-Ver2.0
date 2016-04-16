package org.usfirst.frc.team2791.abstractSubsystems;

import edu.wpi.first.wpilibj.ControllerPower;
import edu.wpi.first.wpilibj.Encoder;
import edu.wpi.first.wpilibj.RobotDrive;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import org.usfirst.frc.team2791.overridenClasses.BasicPID;
import org.usfirst.frc.team2791.util.Constants;
import org.usfirst.frc.team2791.util.Util;

/**
 * Created by Akhil on 4/7/2016.
 * This class is designed to control the driveTrain it is runnable,
 * so that it can run on its own thread for higher angle,stationary,
 * and distance pid
 **/

public class AbstractShakerDriveTrain extends ShakerSubsystem implements Runnable {
    private static final float WHEEL_DIAMETER = (float) (8.0 / 12.0);
    //Number of ticks in one rotation of the encoder
    private static final double encoderTicks = 128;
    //control loops to control the driveTrain accurately
    private static BasicPID movingAnglePID;
    private static BasicPID distancePID;
    private static BasicPID stationaryAnglePID;
    //Encoders - Distance sensors on the robot
    protected Encoder leftDriveEncoder = null;
    protected Encoder rightDriveEncoder = null;
    // this should be instantiated by the extending class
    protected RobotDrive robotDrive = null;
    //Distance PID flags
    private boolean useDistancePID = false;
    private boolean distancePID_useFastExit = false;
    private double distancePID_maxOutput = 1.0;
    private double distancePID_distanceSetpoint;
    private double distancePID_movingAngleSetpoint = 0;
    private boolean distancePID_atTarget = false;
    private boolean distancePID_holdSetpoint = false;
    //Angle PID flags
    private boolean useAnglePID = false;
    private boolean anglePID_useFastExit = false;
    private double anglePID_maxOutput = 1.0;
    private double anglePID_angleSetpoint = 0;
    private boolean anglePID_atTarget = false;
    private boolean anglePID_holdSetPoint = false;
    //miscellaneous
    private Timer anglePIDTimer;
    private Timer distancePIDTimer;
    private boolean busy = false;


    public AbstractShakerDriveTrain() {
    }

    /**
     * Init method needs to be called by the extending class,
     * this is so that the extending class can instantiate anything
     * that is necessary for the init method to work
     */
    protected void init() {
        System.out.println("Updating driveTrain preference...");
        //zero the encoders
        leftDriveEncoder.reset();
        rightDriveEncoder.reset();
        //set how many ticks equals one foot
        leftDriveEncoder.setDistancePerPulse(Util.tickToFeet(encoderTicks, WHEEL_DIAMETER));
        rightDriveEncoder.setDistancePerPulse(-Util.tickToFeet(encoderTicks, WHEEL_DIAMETER));
        //create the control loops using default values
        movingAnglePID = new BasicPID(Constants.DRIVE_ANGLE_P, Constants.DRIVE_ANGLE_I, Constants.DRIVE_ANGLE_D);
        distancePID = new BasicPID(Constants.DRIVE_DISTANCE_P, Constants.DRIVE_DISTANCE_I, Constants.DRIVE_DISTANCE_D);
        stationaryAnglePID = new BasicPID(Constants.STATIONARY_ANGLE_P, Constants.STATIONARY_ANGLE_I,
                Constants.STATIONARY_ANGLE_D);
        //PID preferences
        movingAnglePID.setInvertOutput(true);
        stationaryAnglePID.setInvertOutput(true);

        stationaryAnglePID.setIZone(4);
        distancePID.setIZone(0.25);
        movingAnglePID.setIZone(4);
        //timer
        anglePIDTimer = new Timer();
        distancePIDTimer = new Timer();
    }

    /**
     * This runs on its own thread, originally for higher accuracy
     * in the PID
     */
    public void run() {
        while (true) {
            try {
                //busy mode pretty much makes sure nothing else should run
                //while pid's are running
                busy = useAnglePID || useDistancePID;
                if (useAnglePID || anglePID_holdSetPoint) {
                    //global variable is returned when something calls the public setAngle
                    anglePID_atTarget = setAngleInternal(anglePID_angleSetpoint,
                            anglePID_maxOutput, anglePID_useFastExit);
                    //if the target value is reached break out of the pid
                    useAnglePID = !anglePID_atTarget;
                }

                if (useDistancePID || distancePID_holdSetpoint) {
                    //global variable is returned when something calls the public setDistance
                    distancePID_atTarget = setDistanceInternal(distancePID_distanceSetpoint,
                            distancePID_movingAngleSetpoint, distancePID_maxOutput, distancePID_useFastExit);
                    //if the target value is reached break out of the pid
                    useDistancePID = !distancePID_atTarget;
                }
                Thread.sleep(updateDelayMs);//Run at updateDelayMs hertz
            } catch (Exception e) {
                //print the error
                e.printStackTrace();
                //re-run if a problem occurs
                this.run();
            }
        }
    }
    /**************INTERNAL RUN METHODS****************/
    /**
     * These methods are only to be used by the run method!!
     */
    private boolean setAngleInternal(double angle, double maxOutput, boolean fastExit) {
        //update some pid values
        stationaryAnglePID.setSetPoint(angle);
        stationaryAnglePID.setMaxOutput(maxOutput);
        stationaryAnglePID.setMinOutput(-maxOutput);
        //get the ouptut from the pid
        double anglePIDOutput = stationaryAnglePID.updateAndGetOutput(getAngleEncoder());
        //set the pid values in voltage to the driveTrain
        setLeftRightVoltage(anglePIDOutput, -anglePIDOutput);
        //fast exit doesnt wait for time for the bot to be good
        if (fastExit) {
            return (Math.abs(stationaryAnglePID.getError()) < 0.5) && getEncoderAngleRate() < 0.5;
        }
        //this exit method uses time for pid to be good for(more accurate)
        if (!(Math.abs(stationaryAnglePID.getError()) < 0.5)) {
            // Makes sure pid is good error is minimal
            anglePIDTimer.reset();
            anglePIDTimer.start();
        } else if (anglePIDTimer.get() > 0.5) {
            // then makes sure that certain time has passed to be absolutely
            // positive
            return true;
        }
        return false;
    }

    private boolean setDistanceInternal(double distance, double angle, double maxOutput, boolean fastExit) {
        //PID - update values
        distancePID.setSetPoint(distance);
        movingAnglePID.setSetPoint(angle);
        distancePID.setMaxOutput(maxOutput);
        distancePID.setMinOutput(-maxOutput);
        movingAnglePID.setMaxOutput(maxOutput / 2);
        movingAnglePID.setMinOutput(-maxOutput / 2);
        //get pid output
        double drivePIDOutput = -distancePID.updateAndGetOutput(getAverageDist());
        double anglePIDOutput = movingAnglePID.updateAndGetOutput(getAngleEncoder());
        //set the voltages
        setLeftRightVoltage(drivePIDOutput + anglePIDOutput, drivePIDOutput - anglePIDOutput);
        System.out.println("distError: " + distancePID.getError() + " output: " + drivePIDOutput);
        System.out.println("angleError: " + movingAnglePID.getError() + " output: " + anglePIDOutput);
        //fast exit doesnt wait for time for the shot to be good
        if (fastExit) {
            return ((Math.abs(distancePID.getError()) < 0.05) ||
                    (Math.abs(movingAnglePID.getError()) < 1.5)) && getAverageVelocity() < 0.5;
        }
        //this exit method uses time for pid to be good for(more accurate
        if (!(Math.abs(distancePID.getError()) < 0.05) || !(Math.abs(movingAnglePID.getError()) < 1.5)) {
            // Makes sure pid is good error is minimal
            distancePIDTimer.reset();
            distancePIDTimer.start();
        } else if (distancePIDTimer.get() > 0.5) {
            // then makes sure that certain time has passed to be absolutely positive
            return true;
        }
        return false;
    }

    /*************
     * END INTERNAL RUN METHODS
     ************/

    public void setLeftRight(double left, double right) {
        //This sets the voltages from -1 to 1
        robotDrive.setLeftRightMotorOutputs(left, right);
    }

    public void setToggledLeftRight(double left, double right) {
        //setLeftRight values when the drive train is not busy
        if (!busy)
            setLeftRight(left, right);
    }

    public void setLeftRightVoltage(double leftVoltage, double rightVoltage) {
        //takes in input as voltages and uses current voltage state to vary output accordingly
        leftVoltage *= 12;
        rightVoltage *= 12;
        leftVoltage /= ControllerPower.getInputVoltage();
        rightVoltage /= ControllerPower.getInputVoltage();
        setLeftRight(leftVoltage, rightVoltage);
    }

    /**
     * This method is called in teleop and sets variables that are used in the
     * run method to run our angle PID.
     *
     * @param angle        The angle you want to robot to turn to
     * @param maxOutput    The maximum output to use for this turn
     * @param holdSetPoint If this is true the angle code will hold the angle even after reaching it, must be
     *                     call method releaseAnglePID() to break out of it
     * @param fastExit     If this is true this method returns true when the drive train
     *                     stop moving quickly. If this is false the default behavior of
     *                     returning true if the error is less than 0.5 for longer than
     *                     0.5s.
     * @return whether or not the target angle has been reached
     */
    public boolean setAngle(double angle, double maxOutput, boolean holdSetPoint, boolean fastExit) {
        useAnglePID = true;
        anglePID_angleSetpoint = angle;
        anglePID_maxOutput = maxOutput;
        anglePID_useFastExit = fastExit;
        anglePID_holdSetPoint = holdSetPoint;
        return anglePID_atTarget;
    }

    public boolean setAngle(double angle, double maxOutput) {
        return setAngle(angle, maxOutput, false, false);
    }

    public void releaseAnglePID() {
        anglePID_holdSetPoint = false;
    }

    /**
     * This method is called in teleop and sets variables that are used in the
     * run method to run our distance PID.
     *
     * @param distance     The distance you want the pid to go
     * @param angle        The angle you want the robot to maintain while going the distance
     * @param maxOutput    The max output for driving
     * @param holdSetPoint If this is true the robot will continue to hold the distance until it is released, using
     *                     method releaseDistancePID()
     * @param fastExit     If this is true this method returns true when the drive train
     *                     stop moving quickly. If this is false the default behavior of
     *                     returning true if the error is less than 0.5 for longer than
     *                     0.5s.
     * @return whether the distance has been reached
     */
    public boolean setDistance(double distance, double angle, double maxOutput, boolean holdSetPoint, boolean fastExit) {
        useDistancePID = true;
        distancePID_distanceSetpoint = distance;
        distancePID_movingAngleSetpoint = angle;
        distancePID_maxOutput = maxOutput;
        distancePID_useFastExit = fastExit;
        distancePID_holdSetpoint = holdSetPoint;
        return distancePID_atTarget;
    }

    public boolean setDistance(double distance, double angle, double maxOutput) {
        return setDistance(distance, angle, maxOutput, false, false);
    }

    public void releaseDistancePID() {
        distancePID_holdSetpoint = false;
    }

    public boolean getIfBusy() {
        return busy;
    }

    public void forceBreakAnglePID() {
        //forcefully breaks out of the angle pid even if hasnt reached the target
        useAnglePID = false;
        releaseAnglePID();
    }

    public void forceBreakDistancePID() {
        //forcefully break out of the distance pid even if setpoint hasnt been reaced
        useDistancePID = false;
        releaseDistancePID();
    }

    public void forceBreakPID() {
        //break out of any running pid
        forceBreakAnglePID();
        forceBreakDistancePID();
    }

    public void resetEncoders() {
        leftDriveEncoder.reset();
        rightDriveEncoder.reset();
    }

    public double getStationaryPIDError() {
        return stationaryAnglePID.getError();
    }

    public double getDistancePIDError() {
        return distancePID.getError();
    }

    public double getLeftVelocity() {
        //Left encoder velocity in ft/s
        return leftDriveEncoder.getRate();
    }

    public double getRightVelocity() {
        //Right encoder velocity in ft/s
        return rightDriveEncoder.getRate();
    }

    public double getAverageVelocity() {
        //the average of both encoder velocities
        return (getLeftVelocity() + getRightVelocity()) / 2;
    }

    public double getLeftDistance() {
        // distance of left encoder in feet
        return leftDriveEncoder.getDistance();
    }

    public double getRightDistance() {
        // distance of right encoder in feet
        return rightDriveEncoder.getDistance();
    }

    public double getAverageDist() {
        // average distance of both encoders
        return (getLeftDistance() + getRightDistance()) / 2;
    }

    public double getAngle() {
        return getAngleEncoder();

    }

    public double getAngleEncoder() {
        //returns the angle using encoder values
        return (90 / 2.3) * (getLeftDistance() - getRightDistance()) / 2.0;

    }

    public double getEncoderAngleRate() {
        //return the rate at which we are spinning
        return (90 / 2.3) * (leftDriveEncoder.getRate() - rightDriveEncoder.getRate()) / 2.0;

    }


    public void disable() {
        // Stops all the motors
        robotDrive.stopMotor();
        forceBreakPID();
    }

    /**
     * This updates PID values from the SmartDashboard
     */
    public void updatePIDGains() {
        //Get values from the smartdashboard
        Constants.STATIONARY_ANGLE_P = SmartDashboard.getNumber("Stat Angle P");
        Constants.STATIONARY_ANGLE_I = SmartDashboard.getNumber("Stat Angle I");
        Constants.STATIONARY_ANGLE_D = SmartDashboard.getNumber("Stat Angle D");

        Constants.DRIVE_ANGLE_P = SmartDashboard.getNumber("Angle P");
        Constants.DRIVE_ANGLE_I = SmartDashboard.getNumber("Angle I");
        Constants.DRIVE_ANGLE_D = SmartDashboard.getNumber("Angle D");

        Constants.DRIVE_DISTANCE_P = SmartDashboard.getNumber("DISTANCE P");
        Constants.DRIVE_DISTANCE_I = SmartDashboard.getNumber("DISTANCE I");
        Constants.DRIVE_DISTANCE_D = SmartDashboard.getNumber("Distance D");
        //update the control loops
        movingAnglePID.changeGains(Constants.DRIVE_ANGLE_P, Constants.DRIVE_ANGLE_I, Constants.DRIVE_ANGLE_D);
        distancePID.changeGains(Constants.DRIVE_DISTANCE_P, Constants.DRIVE_DISTANCE_I, Constants.DRIVE_DISTANCE_D);
        stationaryAnglePID.changeGains(Constants.STATIONARY_ANGLE_P, Constants.STATIONARY_ANGLE_I,
                Constants.STATIONARY_ANGLE_D);
    }

    public void updateSmartDash() {
        updatePIDGains();
    }

    public void debug() {
        SmartDashboard.putNumber("Left Drive Encoders Rate", leftDriveEncoder.getRate());
        SmartDashboard.putNumber("Right Drive Encoders Rate", rightDriveEncoder.getRate());
        SmartDashboard.putNumber("Encoder Angle", getAngleEncoder());
        SmartDashboard.putNumber("Encoder Angle Rate Change", getEncoderAngleRate());
        SmartDashboard.putNumber("Angle PID Error", stationaryAnglePID.getError());
        SmartDashboard.putNumber("Angle PID Output", stationaryAnglePID.getOutput());
        SmartDashboard.putNumber("Average Encoder Distance", getAverageDist());
        SmartDashboard.putNumber("Left Encoder Distance", getLeftDistance());
        SmartDashboard.putNumber("Right Encoder Distance", getRightDistance());
        SmartDashboard.putNumber("Distance PID output", distancePID.getOutput());
        SmartDashboard.putNumber("Distance PID error", distancePID.getError());
    }

}
