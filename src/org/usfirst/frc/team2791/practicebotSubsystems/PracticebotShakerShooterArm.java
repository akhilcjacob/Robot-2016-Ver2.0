package org.usfirst.frc.team2791.practicebotSubsystems;

import edu.wpi.first.wpilibj.DoubleSolenoid;
import org.usfirst.frc.team2791.abstractSubsystems.AbstractShakerShooterArm;

/**
 * Created by Akhil on 4/10/2016.
 * This class is used for the practice robot which utilizes different ports
 * electronics(solenoids) and ports than the competiton robot
 */
public abstract class PracticebotShakerShooterArm extends AbstractShakerShooterArm {
    // shooter arm positiion pistons
    private DoubleSolenoid shortPiston;
    private DoubleSolenoid longPiston;

    public PracticebotShakerShooterArm() {    // shooter arm movement pistons
        super();
        longPiston = new DoubleSolenoid(PracticebotPorts.PCM_MODULE, PracticebotPorts.LONG_PISTON_FORWARD,
                PracticebotPorts.LONG_PISTON_REVERSE);
        shortPiston = new DoubleSolenoid(21, 0, 1);

    }

    public ShooterHeight getShooterHeight() {
        // get current shooter height by determining which solenoid are true
        if (shortPiston.get().equals(PracticebotConstants.SMALL_PISTON_HIGH_STATE)
                && longPiston.get().equals(PracticebotConstants.LARGE_PISTON_HIGH_STATE)) {
            return ShooterHeight.HIGH;
        } else if (longPiston.get().equals(PracticebotConstants.LARGE_PISTON_HIGH_STATE)) {
            return ShooterHeight.MID;
        } else {
            return ShooterHeight.LOW;
        }
    }

    protected void moveShooterPistonsLow() {
        shortPiston.set(PracticebotConstants.SMALL_PISTON_LOW_STATE);
        longPiston.set(PracticebotConstants.LARGE_PISTON_LOW_STATE);
    }

    protected void moveShooterPistonsMiddle() {
        shortPiston.set(PracticebotConstants.SMALL_PISTON_LOW_STATE);
        longPiston.set(PracticebotConstants.LARGE_PISTON_HIGH_STATE);
    }

    protected void moveShooterPistonsHigh() {
        shortPiston.set(PracticebotConstants.SMALL_PISTON_HIGH_STATE);
        longPiston.set(PracticebotConstants.LARGE_PISTON_HIGH_STATE);
    }
}
