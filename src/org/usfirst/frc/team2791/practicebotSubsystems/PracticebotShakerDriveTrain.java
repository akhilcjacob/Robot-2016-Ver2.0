package org.usfirst.frc.team2791.practicebotSubsystems;
/**
 * Created by Akhil on 4/10/2016.
 * This class daisy chains speed controllers so you can put them into roboDrive as one set this allows you
 * to use multiple cims
 */

import edu.wpi.first.wpilibj.Encoder;
import edu.wpi.first.wpilibj.RobotDrive;
import edu.wpi.first.wpilibj.Talon;
import org.usfirst.frc.team2791.abstractSubsystems.AbstractShakerDriveTrain;
import org.usfirst.frc.team2791.overridenClasses.TalonSet;

public class PracticebotShakerDriveTrain extends AbstractShakerDriveTrain {
    public PracticebotShakerDriveTrain() {
        super();
        // instanciate the four talons for the drive train
        Talon leftTalonA = new Talon(PracticebotPorts.DRIVE_TALON_LEFT_PORT_FRONT);
        Talon leftTalonB = new Talon(PracticebotPorts.DRIVE_TALON_LEFT_PORT_BACK);
        Talon leftTalonC = new Talon(0);
        Talon rightTalonA = new Talon(PracticebotPorts.DRIVE_TALON_RIGHT_PORT_FRONT);
        Talon rightTalonB = new Talon(PracticebotPorts.DRIVE_TALON_RIGHT_PORT_BACK);
        Talon rightTalonC = new Talon(0);
        // use the talons to create a roboDrive (it has methods that allow for
        // easier control)
        this.robotDrive = new RobotDrive(new TalonSet(leftTalonA, leftTalonB, leftTalonC),
                new TalonSet(rightTalonA, rightTalonB, rightTalonC));
        // stop all motors right away just in case
        robotDrive.stopMotor();
        //distance senesors(Greyhill quadrature encoders 128 tick)
        this.leftDriveEncoder = new Encoder(PracticebotPorts.LEFT_DRIVE_ENCODER_PORT_A,
                PracticebotPorts.LEFT_DRIVE_ENCODER_PORT_B);
        this.rightDriveEncoder = new Encoder(PracticebotPorts.RIGHT_DRIVE_ENCOODER_PORT_A,
                PracticebotPorts.RIGHT_DRIVE_ENCODER_PORT_B);

        init();
    }

}