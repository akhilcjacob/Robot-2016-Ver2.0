package org.usfirst.frc.team2791.competitionSubsystems;

import edu.wpi.first.wpilibj.DoubleSolenoid;
import org.usfirst.frc.team2791.abstractSubsystems.AbstractShakerShooterArm;
import org.usfirst.frc.team2791.util.Constants;

/**
 * Created by Akhil on 4/10/2016.
 * Extends off of the class AbstractShakerSHooter it is designed for the
 * competition robot and uses those ports.
 */
public class ShakerShooterArm extends AbstractShakerShooterArm {
    private DoubleSolenoid shortPiston;
    private DoubleSolenoid longPiston;

    public ShakerShooterArm() {

        longPiston = new DoubleSolenoid(Constants.PCM_MODULE, Constants.LONG_PISTON_FORWARD,
                Constants.LONG_PISTON_REVERSE);
        shortPiston = new DoubleSolenoid(Constants.PCM_MODULE, Constants.SHORT_PISTON_FORWARD,
                Constants.SHORT_PISTON_REVERSE);
    }

    public ShooterHeight getSolenoidState() {
        // get current shooter height by determining which solenoid are true
        if (shortPiston.get().equals(Constants.SMALL_PISTON_HIGH_STATE)
                && longPiston.get().equals(Constants.LARGE_PISTON_HIGH_STATE))
            return ShooterHeight.HIGH;
        else if (longPiston.get().equals(Constants.LARGE_PISTON_HIGH_STATE))
            return ShooterHeight.MID;
        else
            return ShooterHeight.LOW;

    }

    protected void moveShooterPistonsLow() {
        // both pistons will be set to true to get max height
        // both pistons will be set to true to get low height
        shortPiston.set(Constants.SMALL_PISTON_HIGH_STATE); // was reverse
        // //this is short
        // one
        longPiston.set(Constants.LARGE_PISTON_HIGH_STATE);
    }

    protected void moveShooterPistonsMiddle() {
        // set shooter height to middle meaning only one piston will be true
        shortPiston.set(Constants.SMALL_PISTON_LOW_STATE);
        longPiston.set(Constants.LARGE_PISTON_HIGH_STATE);
    }

    protected void moveShooterPistonsHigh() {
        // set shooter height to low , set both pistons to false
        shortPiston.set(Constants.SMALL_PISTON_LOW_STATE);
        longPiston.set(Constants.LARGE_PISTON_LOW_STATE);
        // short needs to switch
    }
}
