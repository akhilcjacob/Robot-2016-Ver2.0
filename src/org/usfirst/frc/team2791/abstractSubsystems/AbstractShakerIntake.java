package org.usfirst.frc.team2791.abstractSubsystems;

import edu.wpi.first.wpilibj.Talon;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

/**
 * Created by Akhil on 4/10/2016.
 * This class is created to control the actuation of the intake and all
 * of it's components
 */
public abstract class AbstractShakerIntake extends ShakerSubsystem {
    //Global variable to control the speed of the intake motor
    private static final double INTAKE_SPEED = 1;
    //Intake motor talons
    protected Talon rightIntakeMotor;
    protected Talon leftIntakeMotor;
    //timer for between actuation states
    protected Timer actuationTimer;
    protected boolean busy;

    public AbstractShakerIntake() {
        System.out.println("New instance of intake created.");
    }

    /**
     * Init method needs to be called by the extending class,
     * this is so that the extending class can instantiate sensors
     * that are necessary for the init method to work
     */
    protected void init() {
        System.out.println("Updating intake preference...");
        leftIntakeMotor.setInverted(true);
        actuationTimer = new Timer();
        actuationTimer.start();
    }

    protected abstract void internalRetractIntake();

    protected abstract void internalExtendIntake();

    protected abstract IntakeState getSolenoidState();

    public void retractIntake() {
        //sets the extending classes solenoid to retracted state
        internalRetractIntake();
        if (getSolenoidState().equals(IntakeState.EXTENDED)) {
            //timer save time since last actuation
            actuationTimer.reset();
            actuationTimer.start();
        }
    }

    public void extendIntake() {
        //sets the extending classes solenoid to extended state
        internalExtendIntake();
        if (getSolenoidState().equals(IntakeState.RETRACTED)) {
            //timer save time since last actuation
            actuationTimer.reset();
            actuationTimer.start();
        }
    }

    public void toggledRetractIntake() {
        //if the intake isn't busy then retract
        if (!busy)
            internalRetractIntake();
    }

    public void toggledExtendIntake() {
        //if the intake ins't busy then extend
        if (!busy)
            internalExtendIntake();
    }

    public void retractIntake(boolean forceHold) {
        //move the intake and mark as busy so that nothing else can actuate
        busy = forceHold;
        retractIntake();
    }

    public void extendIntake(boolean forceHold) {
        //move the intake and mark as busy so that nothing else can actuate
        busy = forceHold;
        extendIntake();
    }

    public void breakIntakeHold() {
        busy = false;
    }

    public IntakeState getCurrentIntakeState() {
        //if the time since the last actuation is big enough then return the
        //acutal solenoid else return that it is moving
        if (actuationTimer.get() > 0.6)
            return getSolenoidState();
        return IntakeState.MOVING;
    }

    public boolean getIfExtended() {
        //convert the getCurrentIntakeState into a boolean value
        return getCurrentIntakeState().equals(IntakeState.EXTENDED);
    }

    public boolean getIfRetracted() {
        //convert the getCurrentIntakeState into a boolean value
        return getCurrentIntakeState().equals(IntakeState.RETRACTED);
    }

    public boolean getIfMoving() {
        //convert the getCurrentIntakeState into a boolean value
        return getCurrentIntakeState().equals(IntakeState.MOVING);
    }

    public void pullBall() {
        // runs intake inward at global variable INTAKE_SPEED
        leftIntakeMotor.set(INTAKE_SPEED);
        rightIntakeMotor.set(INTAKE_SPEED);
    }

    public void pushBall() {
        // runs intake outward, run at slower speed so a ball doesn't
        //fly outward
        leftIntakeMotor.set(-INTAKE_SPEED / 2);
        rightIntakeMotor.set(-INTAKE_SPEED / 2);

    }

    public void stopMotors() {
        // sends 0 to both motors to stop them
        leftIntakeMotor.set(0.0);
        rightIntakeMotor.set(0.0);
    }

    public void updateSmartDash() {
    }

    public void debug() {
        SmartDashboard.putString("Intake state", getSolenoidState().toString());
    }

    public void reset() {
        // runs methods to bring back to original position
        internalRetractIntake();
        stopMotors();
        busy = false;
    }

    public void disable() {
        // when disabled makes sure that motors are stopped
        stopMotors();
    }

    public enum IntakeState {
        MOVING, RETRACTED, EXTENDED
    }
}
