package org.usfirst.frc.team2791.helpers.autonModes;

import org.usfirst.frc.team2791.abstractSubsystems.OldAbstractShakerShooter.ShooterHeight;
import org.usfirst.frc.team2791.commands.AutoLineUpShot;

import static org.usfirst.frc.team2791.robot.Robot.driveTrain;
import static org.usfirst.frc.team2791.robot.Robot.intake;

public class DriveStraightAutomaticLineup extends AutonMode {
	private double firstDistance;
	private double turnToAngle;

	public DriveStraightAutomaticLineup(double distance, double angle) {
		firstDistance = distance;
		turnToAngle = angle;
	}

	public void run() {
		switch (state) {
		case 0:
			driveTrain.disable();
			shooter.stopMotors();
			break;
		case 1:
			System.out.println("Starting the drive straight autoLinup ");
			driveTrain.resetEncoders();
			intake.internalExtendIntake();
			shooter.delayedShooterPosition(ShooterHeight.MID);
			state++;
			break;
		case 2:
			if (driveTrain.setDistance(firstDistance, 0, 0.65, false)) {
				// intake.setArmAttachmentDown();
				System.out.println("Drove the first distance");
				driveTrain.resetEncoders();
				state++;
			}
			break;
		case 3:
			if (driveTrain.setAngle(turnToAngle, 0.6)) {
				driveTrain.resetEncoders();
				state++;
			}
			break;

		case 4:
			AutoLineUpShot.setShootAfterAligned(true);
			AutoLineUpShot.setUseMultipleFrames(true);
			AutoLineUpShot.inAuton(true);
			AutoLineUpShot.run();
			System.out.println("Starting autoLineup");
			state++;
			break;
		case 5:
			if (!AutoLineUpShot.isRunning()) {
				state++;
				System.out.println("Auto lineup is no longer running and finishing up");
			} else
				AutoLineUpShot.run();
			break;
		case 6:
			AutoLineUpShot.reset();
			System.out.println("I am done with the drive striaght auto");
			driveTrain.resetEncoders();
			AutoLineUpShot.inAuton(false);
			state = 0;
			break;
		}
	}
}