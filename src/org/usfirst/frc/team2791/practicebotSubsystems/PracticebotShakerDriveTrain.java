package org.usfirst.frc.team2791.practicebotSubsystems;

import edu.wpi.first.wpilibj.Encoder;
import edu.wpi.first.wpilibj.RobotDrive;
import edu.wpi.first.wpilibj.Talon;
import org.usfirst.frc.team2791.abstractSubsystems.AbstractShakerDriveTrain;

public class PracticebotShakerDriveTrain extends AbstractShakerDriveTrain {

    private Talon leftTalonA;
    private Talon leftTalonB;
    private Talon rightTalonA;
    private Talon rightTalonB;

    public PracticebotShakerDriveTrain() {
        super();
        // instanciate the four talons for the drive train
        this.leftTalonA = new Talon(PracticebotPorts.DRIVE_TALON_LEFT_PORT_FRONT);
        this.leftTalonB = new Talon(PracticebotPorts.DRIVE_TALON_LEFT_PORT_BACK);
        this.rightTalonA = new Talon(PracticebotPorts.DRIVE_TALON_RIGHT_PORT_FRONT);
        this.rightTalonB = new Talon(PracticebotPorts.DRIVE_TALON_RIGHT_PORT_BACK);
        // use the talons to create a roboDrive (it has methods that allow for
        // easier control)
        this.robotDrive = new RobotDrive(leftTalonA, leftTalonB, rightTalonA, rightTalonB);
        // stop all motors right away just in case
        robotDrive.stopMotor();


        this.leftDriveEncoder = new Encoder(PracticebotPorts.LEFT_DRIVE_ENCODER_PORT_A,
                PracticebotPorts.LEFT_DRIVE_ENCODER_PORT_B);
        this.rightDriveEncoder = new Encoder(PracticebotPorts.RIGHT_DRIVE_ENCOODER_PORT_A,
                PracticebotPorts.RIGHT_DRIVE_ENCODER_PORT_B);

        init();
    }

}