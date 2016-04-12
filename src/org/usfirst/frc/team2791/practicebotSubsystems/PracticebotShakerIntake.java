package org.usfirst.frc.team2791.practicebotSubsystems;

import edu.wpi.first.wpilibj.DoubleSolenoid;
import edu.wpi.first.wpilibj.Talon;
import org.usfirst.frc.team2791.abstractSubsystems.AbstractShakerIntake;

public class PracticebotShakerIntake extends AbstractShakerIntake {
    private DoubleSolenoid intakeSolenoid;
    private DoubleSolenoid armAttachment;

    public PracticebotShakerIntake() {
        // init
        super();
        leftIntakeMotor = new Talon(PracticebotPorts.INTAKE_TALON_LEFT_PORT);
        rightIntakeMotor = new Talon(PracticebotPorts.INTAKE_TALON_RIGHT_PORT);

        intakeSolenoid = new DoubleSolenoid(PracticebotPorts.PCM_MODULE, PracticebotPorts.INTAKE_PISTON_CHANNEL_FORWARD,
                PracticebotPorts.INTAKE_PISTON_CHANNEL_REVERSE);

        armAttachment = new DoubleSolenoid(PracticebotPorts.PCM_MODULE, PracticebotPorts.INTAKE_ARM_CHANNEL_FORWARD,
                PracticebotPorts.INTAKE_ARM_CHANNEL_REVERSE);
        init();

    }

    // This system does not need to do anything continiously so this method is blank 
    public void run() {
    }

    public void internalRetractIntake() {
        // bring intake back behind bumpers
        intakeSolenoid.set(PracticebotConstants.INTAKE_RECTRACTED_VALUE);

    }

    public void internalExtendIntake() {
        // extends the intake for ball pickup
        intakeSolenoid.set(PracticebotConstants.INTAKE_EXTENDED_VALUE);

    }


    public IntakeState getSolenoidState() {
        // returns state of intake in form of the enum IntakeState
        if (intakeSolenoid.get().equals(PracticebotConstants.INTAKE_RECTRACTED_VALUE))
            return IntakeState.RETRACTED;
        else if (intakeSolenoid.get().equals(PracticebotConstants.INTAKE_EXTENDED_VALUE))
            return IntakeState.EXTENDED;
        else
            return IntakeState.EXTENDED;
    }


    public void setArmAttachmentUp() {
        armAttachment.set(PracticebotConstants.INTAKE_ARM_UP_VALUE);
    }


    public void setArmAttachmentDown() {
        armAttachment.set(PracticebotConstants.INTAKE_ARM_DOWN_VALUE);
    }


    public boolean getArmAttachementUp() {
        return false;
    }

}
