package org.usfirst.frc.team2791.abstractSubsystems;

import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

/**
 * Created by Akhil on 4/10/2016.
 * This class controls the actuation of the shooter arm, originally
 * part of the shooter wheels class but seperated for easier readability
 */
public abstract class AbstractShakerShooterArm extends ShakerSubsystem {
    private Timer actuationTimer;

    public AbstractShakerShooterArm() {
        System.out.println("New instance of shooterArm created.");
        actuationTimer = new Timer();
        actuationTimer.start();
    }

    public abstract ShooterHeight getSolenoidState();

    protected abstract void moveShooterPistonsLow();

    protected abstract void moveShooterPistonsMiddle();

    protected abstract void moveShooterPistonsHigh();

    public ShooterHeight getShooterHeight() {
        //if the time since the last actuation is >0.6 then the shooter
        //is at the desired set point other wise it is still moving
        if (actuationTimer.get() > 0.6)
            return getSolenoidState();
        return ShooterHeight.MOVING;
    }

    public void setShooterLow() {
        // both pistons will be set to true to get max height
        moveShooterPistonsLow();
        if (!getSolenoidState().equals(ShooterHeight.LOW)) {
            actuationTimer.reset();
            actuationTimer.start();
        }
    }

    public void setShooterMiddle() {
        // set shooter height to middle meaning only one piston will be true
        moveShooterPistonsMiddle();
        if (!getSolenoidState().equals(ShooterHeight.MID)) {
            actuationTimer.reset();
            actuationTimer.start();
        }
    }

    public void setShooterHigh() {
        // set shooter height to low, set both pistons to false
        moveShooterPistonsHigh();
        if (!getSolenoidState().equals(ShooterHeight.HIGH)) {
            actuationTimer.reset();
            actuationTimer.start();
        }
    }

    public boolean getIfShooterHigh() {
        return (getShooterHeight().equals(ShooterHeight.HIGH));
    }

    public boolean getIfShooterMiddle() {
        return (getShooterHeight().equals(ShooterHeight.MID));
    }

    public boolean getIfShooterLow() {
        return (getShooterHeight().equals(ShooterHeight.LOW));
    }

    public boolean getIfShooterMoving() {
        return (getShooterHeight().equals(ShooterHeight.MOVING));
    }


    public void updateSmartDash() {

    }

    public void disable() {

    }

    public void debug() {
        SmartDashboard.putString("Current shooter setpoint", getShooterHeight().toString());
    }

    public void run() {

    }

    public enum ShooterHeight {
        LOW, MID, HIGH, MOVING
    }
}