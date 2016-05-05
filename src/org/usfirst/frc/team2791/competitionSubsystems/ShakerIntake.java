package org.usfirst.frc.team2791.competitionSubsystems;

import edu.wpi.first.wpilibj.Solenoid;
import edu.wpi.first.wpilibj.Talon;
import org.usfirst.frc.team2791.abstractSubsystems.AbstractShakerIntake;
import org.usfirst.frc.team2791.util.Constants;


public class ShakerIntake extends AbstractShakerIntake {
    private Solenoid intakeSolenoid;

    public ShakerIntake() {
        // init
        super();
        leftIntakeMotor = new Talon(Constants.INTAKE_TALON_LEFT_PORT);
        rightIntakeMotor = new Talon(Constants.INTAKE_TALON_RIGHT_PORT);
        intakeSolenoid = new Solenoid(Constants.PCM_MODULE, Constants.INTAKE_PISTON);
        init();
    }

    public void internalRetractIntake() {
        // bring intake back behind bumpers
        intakeSolenoid.set(false);

    }

    public void internalExtendIntake() {
        // extends the intake for ball  pickup
        intakeSolenoid.set(true);

    }

    public IntakeState getSolenoidState() {
        // returns state of intake in form of the enum IntakeState
        if (!intakeSolenoid.get())
            return IntakeState.RETRACTED;
        else if (intakeSolenoid.get())
            return IntakeState.EXTENDED;
        else
            return IntakeState.EXTENDED;
    }

    public void run() {

    }
}
