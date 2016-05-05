package org.usfirst.frc.team2791.abstractSubsystems;

import edu.wpi.first.wpilibj.AnalogInput;
import edu.wpi.first.wpilibj.CANTalon;
import edu.wpi.first.wpilibj.Servo;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import org.usfirst.frc.team2791.util.Constants;

import static org.usfirst.frc.team2791.robot.Robot.shooterArm;
import static org.usfirst.frc.team2791.robot.Robot.shooterWheels;

/**
 * Created by Akhil on 4/10/2016.
 * This class is created to control the shooter wheels specifically
 * it uses pid control and also designed to run on its own thread
 * it also includes the actuation of the servo
 */
public class AbstractShakerShooterWheels extends ShakerSubsystem implements Runnable {
    //the number of ticks in the shooter wheels
    private final int encoderTicks = 128 * 4;
    //Shooter CAN talons, uses CAN for communication
    protected CANTalon leftShooterTalon;
    protected CANTalon rightShooterTalon;
    //servo that pushes the ball into the shooter wheels
    protected Servo servo;
    //Analog distance sensor used to determine if shooter has ball
    protected AnalogInput distanceSensor;
    //PID Values
    private double feedForward = 0.31;
    private double closeShotSetPoint = 575;
    private double farShotSetpoint = 830;
    //Run method flags
    private boolean prepShot = false;
    private boolean completeShot = false;
    private boolean cancelShot = false;
    private boolean overrideShot = false;
    private boolean shooterArmMoving = false;
    private boolean prepShotAfterShooterArm = false;
    //if shooter wheels are busy(with pid)they wont be interuptted
    private boolean busy = false;
    //completeShot timer

    public AbstractShakerShooterWheels() {
        System.out.println("New instance of shooterWheels created.");
    }

    /**
     * Init method needs to be called by the extending class,
     * this is so that the extending class can instantiate anything
     * that is necessary for the init method to work
     */
    protected void init() {
        System.out.println("Updating shooter preference...");
        // shooter talon config
        rightShooterTalon.setInverted(false);
        rightShooterTalon.reverseOutput(false);
        leftShooterTalon.reverseOutput(false);
        leftShooterTalon.reverseSensor(true);
        rightShooterTalon.reverseSensor(false);
        leftShooterTalon.setIZone(500);
        rightShooterTalon.setIZone(500);
        // choose the type of sensor attached to the talon
        leftShooterTalon.setFeedbackDevice(CANTalon.FeedbackDevice.QuadEncoder);
        rightShooterTalon.setFeedbackDevice(CANTalon.FeedbackDevice.QuadEncoder);
        // control mode - speed(rpms),voltage(how many volts to be sent to
        // the talons percentage(voltage sent/ 12 v)
        leftShooterTalon.changeControlMode(CANTalon.TalonControlMode.Speed);
        rightShooterTalon.changeControlMode(CANTalon.TalonControlMode.Speed);
        // enable the talons
        leftShooterTalon.enableControl();
        rightShooterTalon.enableControl();
        leftShooterTalon.enable();
        rightShooterTalon.enable();
        // how many ticks are in the feed back device
        leftShooterTalon.configEncoderCodesPerRev(encoderTicks);
        rightShooterTalon.configEncoderCodesPerRev(encoderTicks);
        //backward limit on shooter pid to prevent oscillation
        leftShooterTalon.configPeakOutputVoltage(+12.0f, 0);
        rightShooterTalon.configPeakOutputVoltage(+12.0f, 0);
        leftShooterTalon.configNominalOutputVoltage(0, 0);
        rightShooterTalon.configNominalOutputVoltage(0, 0);

        // put the shooter pid values on the dashboard
        SmartDashboard.putNumber("Shooter p", Constants.SHOOTER_P);
        SmartDashboard.putNumber("Shooter i", Constants.SHOOTER_I);
        SmartDashboard.putNumber("Shooter d", Constants.SHOOTER_D);
        SmartDashboard.putNumber("FeedForward", feedForward);
        // put setpoints on the dashboard
        SmartDashboard.putNumber("closeShotSetpoint", closeShotSetPoint);
        SmartDashboard.putNumber("farShotSetpoint", farShotSetpoint);

    }

    public void run() {
        while (true) {
            if (shooterArmMoving) {
                internalShooterArmMoving();
                stopMotors();
                shooterArmMoving = false;
                if (prepShotAfterShooterArm)
                    prepShot = true;
            }
            //this continues to update the fact that the shooter is busy
            busy = prepShot || completeShot;
            double setPoint = internalGetSetPoint();
            if (prepShot) {
                internalPrepShot(setPoint);
            }
            if (completeShot) {
                internalAutoFire(setPoint);
                stopMotors();
                //TODO look at possibly doing this but make sure to remove this out of the vision shot code if so
//                IntakeAndShooterSynergy.setPosition(AbstractShakerShooterArm.ShooterHeight.LOW);
            }
            internalReset();

        }
    }
    /**************INTERNAL RUN METHODS****************/
    /**
     * These methods are only to be used by the run method!!
     */
    private void internalReset() {
        shooterArmMoving = false;
        completeShot = false;
        cancelShot = false;
        prepShotAfterShooterArm = false;
        busy = false;
    }

    private void internalShooterArmMoving() {
        //if the shooter arm is moving then run the wheels inward
        Timer tempTimer = new Timer();
        tempTimer.reset();
        tempTimer.start();
        //run it inward for .4s
        while (tempTimer.get() < 0.4) {
            setToggledShooterSpeeds(-0.7, false);
            //if shooter is busy break out of this b/c it isnt as important
        }
    }

    private double internalGetSetPoint() {
        //updates setpoint values from the dashboard
        closeShotSetPoint = SmartDashboard.getNumber("closeShotSetpoint");
        farShotSetpoint = SmartDashboard.getNumber("farShotSetpoint");
        //returns a value depnding on the shooter postition
        return shooterArm.getShooterHeight().equals(AbstractShakerShooterArm.ShooterHeight.MID) ? farShotSetpoint : closeShotSetPoint;
    }

    private void internalPrepShot(double setPoint) {
        setShooterSpeeds(setPoint, true);
        if (overrideShot || completeShot || cancelShot) {
            System.out.println("Finished prepping the shot");
            if (!completeShot)
                prepShot = false;
        }
    }

    private void internalAutoFire(double setPoint) {
        System.out.println("Auto Firing starting");
        Timer completeShotTimer = new Timer();
        // set the shooter speeds to the set point using pid
        setShooterSpeeds(setPoint, true);
        //zero the timer
        completeShotTimer.reset();
        completeShotTimer.start();
        //make sure that the pid is good, for at least 0.2 s
        while (completeShotTimer.get() < 0.2) {
            // if the wheels aren't at speed reset the timer
            if (!shooterAtSpeed()) {
                completeShotTimer.reset();
                completeShotTimer.start();
                /* if the prepshot override was there(basically if we attempt to shoot after prepshot) and we werent
                    at the setpoint the we will follow the regular .2 s error check */
                prepShot = false;
            } else if (prepShot) {
                /*if we prepped the shot then we probably are at the setpoint already
                This is in the else if statement of the error checking if statment so that we are 100%
                sure we reached the setpoint */
                break;
            }
            // set the shooter speeds to the set point using pid
            setShooterSpeeds(setPoint, true);
            //if overridden or canceled go to the next thing
            if (overrideShot || cancelShot || prepShot)
                break;
        }
        //if the shot hasn't been canceled continue
        if (!cancelShot) {
            System.out.println("starting autofire servo push");
            //zero and start the timer
            completeShotTimer.reset();
            completeShotTimer.start();
            //run the servo arm to extended position for 0.8 seconds
            while (completeShotTimer.get() < 0.8) {
                //hold the shooter speed using pid
                setShooterSpeeds(setPoint, true);
                //extend the servo arm
                extendServoArm();
            }
            completeShotTimer.reset();
            completeShotTimer.start();
            while (completeShotTimer.get() < 0.4) {
                //hold the shooter wheels at the setpoint a few seconds after
                //the servo push to maintain accuracy
                setShooterSpeeds(setPoint, true);
            }

            //resetting procedure
            retractServoArm();
            stopMotors();
            overrideShot = false;
            System.out.println("Finishing autofire");
        } else {
            System.out.println("The shot was calceled");
        }

    }

    /*************
     * END INTERNAL RUN METHODS
     ************/
    /*
     *control flags for the looped run methods
     */
    public void resetShooterFlags() {
        //set all the run method flags to false
        busy = false;
        overrideShot = false;
        prepShot = false;
        cancelShot = true;
        shooterArmMoving = false;
        prepShotAfterShooterArm = false;
    }

    public void completeShot() {
        completeShot = busy = true;
    }

    public void overrideAutoShot() {
        //this flag causes an override to control loop
        overrideShot = true;
    }

    public void prepShot() {
        //this flag runs the shooter wheels but doesn't shoot
        System.out.println("I am currently prepping the shot");
        prepShot = busy = true;
    }

    public void shooterArmMoving() {
        //this flag runs the wheels inward so it sucks the ball in
        shooterArmMoving = busy = true;
    }

    /**
     * This method tells the shooter wheels to run prepshot right after running the shooter arm moving command.
     * The shooter arm moving command runs the wheels inward for a few seconds to make sure the ball securely
     * in the shooter. Then prepshot, this should theortically get the shot ready right when we need to fire
     */
    public void shooterArmMoveAndPrepShot() {
        shooterArmMoving = true;
        prepShotAfterShooterArm = true;
    }

    public boolean getIfPreppingShot() {
        //get the status of the prepshot flag
        return prepShot;

    }

    public boolean getIfCompleteShot() {
        //get the status of the completeShot flag
        return completeShot;
    }

    public boolean getIfBusy() {
        return busy;
    }
    /*
     *shooter wheel control
     */

    /**
     * This sets the shooter wheel speed with an option for pid on
     * the target speed
     *
     * @param targetSpeed the speed that the shooter should be set to
     *                    either as a percentage from -1 to 1 if withPID
     *                    is false or as rpms if withPID is true
     * @param withPID     decides whether or not to use pid to reach the
     *                    targetSpeed
     */
    public void setShooterSpeeds(double targetSpeed, boolean withPID) {
        if (withPID) {
            //using PID will supercede all other attmepts to control the wheels
            busy = true;
            //Switch to velocity control mode
            leftShooterTalon.changeControlMode(CANTalon.TalonControlMode.Speed);
            rightShooterTalon.changeControlMode(CANTalon.TalonControlMode.Speed);
            // update the pid and feedforward values
            leftShooterTalon.setP(SmartDashboard.getNumber("Shooter p"));
            leftShooterTalon.setI(SmartDashboard.getNumber("Shooter i"));
            leftShooterTalon.setD(SmartDashboard.getNumber("Shooter d"));
            rightShooterTalon.setP(SmartDashboard.getNumber("Shooter p"));
            rightShooterTalon.setI(SmartDashboard.getNumber("Shooter i"));
            rightShooterTalon.setD(SmartDashboard.getNumber("Shooter d"));
            leftShooterTalon.setF(SmartDashboard.getNumber("FeedForward"));
            rightShooterTalon.setF(SmartDashboard.getNumber("FeedForward"));
            // set the speeds (THEY ARE IN RPMS)
            leftShooterTalon.set(targetSpeed);
            rightShooterTalon.set(targetSpeed);

        } else if (!completeShot && !prepShot) {
            // if shooters is not autofiring or prepping the shot then use
            // inputs given, including 0
            leftShooterTalon.changeControlMode(CANTalon.TalonControlMode.PercentVbus);
            rightShooterTalon.changeControlMode(CANTalon.TalonControlMode.PercentVbus);
            leftShooterTalon.set(targetSpeed);
            rightShooterTalon.set(targetSpeed);
        }
    }

    public void setToggledShooterSpeeds(double targetSpeed, boolean withPID) {
        //if the shooter isnt busy with pid or anything else
        if (!busy) {
            setShooterSpeeds(targetSpeed, withPID);
        }
    }

    public boolean shooterAtSpeed() {
        //check whether the combined error of the left and right shooter talon is minimal
        double total_error = Math.abs(leftShooterTalon.getError()) + Math.abs(rightShooterTalon.getError());
        return total_error < 30;
    }

    public void stopMotors() {
        // set the motors to 0 to stop
        leftShooterTalon.set(0);
        rightShooterTalon.set(0);
    }

    /*
     * ball detection
     */
    public boolean hasBall() {
        // returns the sensor value
        return distanceSensor.getVoltage() > 0.263;
    }

    /*
     * Servo actuation
     */
    public void extendServoArm() {
        // will be used to push ball toward the shooter
        servo.set(0.5);
    }

    public void retractServoArm() {
        // bring servo back to original position
        servo.set(1);
    }

    /*
     * Methods from ShakerSubsystem
     */
    public void reset() {
        shooterWheels.stopMotors();
    }

    public void disable() {
        // disable code will stop motors
        stopMotors();
        completeShot = false;
        prepShot = false;
        cancelShot = false;
        resetShooterFlags();
    }

    public void updateSmartDash() {
        // update the smartdashbaord with values
        SmartDashboard.putBoolean("Does shooter have ball", hasBall());
        SmartDashboard.putBoolean("Is auto firing", completeShot);
        SmartDashboard.putBoolean("Is preparing shot", prepShot);
    }

    public void debug() {
        SmartDashboard.putNumber("LeftShooterSpeed", leftShooterTalon.getEncVelocity());
        SmartDashboard.putNumber("RightShooterSpeed", rightShooterTalon.getEncVelocity());
        SmartDashboard.putNumber("Left Shooter Error", leftShooterTalon.getClosedLoopError());
        SmartDashboard.putNumber("Right Shooter Error", -rightShooterTalon.getClosedLoopError());
        SmartDashboard.putNumber("left output voltage", leftShooterTalon.getOutputVoltage());
        SmartDashboard.putNumber("left speed", -leftShooterTalon.getEncVelocity());
        SmartDashboard.putNumber("right output voltage", rightShooterTalon.getOutputVoltage());
        SmartDashboard.putNumber("right speed", rightShooterTalon.getEncVelocity());
        SmartDashboard.putNumber("Right error", rightShooterTalon.getError());
        SmartDashboard.putNumber("Left error", leftShooterTalon.getError());
    }
}
