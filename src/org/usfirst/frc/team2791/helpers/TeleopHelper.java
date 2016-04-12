package org.usfirst.frc.team2791.helpers;

import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import org.usfirst.frc.team2791.abstractSubsystems.OldAbstractShakershooterWheels.ShooterHeight;
import org.usfirst.frc.team2791.commands.visionShot;
import org.usfirst.frc.team2791.util.Toggle;

import static org.usfirst.frc.team2791.robot.Robot.*;

//import org.usfirst.frc.team2791.subsystems.ShakerIntake;

/**
 * Created by Akhil on 2/14/2016.
 */
public class TeleopHelper extends ShakerHelper {
	private static TeleopHelper teleop;
	private static boolean cameraLineUp = false;
	private SendableChooser driveTypeChooser;
	private Toggle useArmAttachmentToggle;
	private boolean holdIntakeDown = false;

	private TeleopHelper() {
		// init
		// smartdashboard drop down menu
		driveTypeChooser = new SendableChooser();
		SmartDashboard.putData("Drive Chooser", driveTypeChooser);
		driveTypeChooser.addObject("Tank Drive", "TANK");
		driveTypeChooser.addObject("Arcade Drive", "ARCADE");
		driveTypeChooser.addDefault("GTA Drive", "GTA");
		driveTypeChooser.addObject("Single Arcade", "SINGLE_ARCADE");
		SmartDashboard.putNumber("Shooter Speeds Setpoint range table", 0);

		// toggles, to prevent sending a subsystem a value too many times
		// this is sort of like a light switch
		useArmAttachmentToggle = new Toggle(false);
	}

	public static TeleopHelper getInstance() {
		if (teleop == null)
			teleop = new TeleopHelper();
		return teleop;
	}

	public void run() {
		// just in case something weird happen in auto
		shooterWheels.setAutonShotMode(false);
		configureAutoShot();
		operatorRun();// runs the operator controls
		driverRun();// runs the driver controls
		sharedRun();
	}

	private void driverRun() {
		// Read a value from the smart dashboard and chose what control scheme
		// to use for the
		// drive train
		switch (getDriveType()) {
		case TANK:
			driveTrain.setToggledLeftRight(driverJoystick.getAxisLeftY(), -driverJoystick.getAxisRightY());
			break;
		default:
		case GTA:
			driveTrain.setToggledLeftRight(driverJoystick.getGtaDriveLeft(), driverJoystick.getGtaDriveRight());
			break;
		case ARCADE:
			driveTrain.setToggledLeftRight(-driverJoystick.getAxisLeftY(), -driverJoystick.getAxisRightX());
			break;
		case SINGLE_ARCADE:
			driveTrain.setToggledLeftRight(-driverJoystick.getAxisLeftY(), -driverJoystick.getAxisLeftX());
			break;
		}

		if (driveTrain.isUsingPID() && Math.abs(driverJoystick.getGtaDriveLeft()) > 0.2) {
			System.out.println("driver exiting PID");
			driveTrain.doneUsingPID();
			visionShot.reset();
		}

		// TODO: rework this method and the drive train methods to do PID in the
		// run loop

		// Let the driver busy B to set high gear, busy X to set low gear and
		// othewise auto shit
		if (driverJoystick.getButtonB())
			driveTrain.setHighGear();
		else if (driverJoystick.getButtonX())
			driveTrain.setLowGear();
		else
			driveTrain.autoShift(shooterWheels.equals(ShooterHeight.LOW));
	}

	private void operatorRun() {
		// Operator button layout
		if (operatorJoystick.getButtonB()) {
			// Run intake inward with assistance of the shooter wheel
			shooterWheels.setShooterSpeeds(-0.6, false);
			intake.pullBall();
			holdIntakeDown = true;
		} else if (operatorJoystick.getButtonX()) {
			// Run reverse if button pressed
			shooterWheels.setShooterSpeeds(0.6, false);
			intake.pushBall();

		} else if (!visionShot.isRunning() && !shooterWheels.getIfAutoFire()) {
			shooterWheels.setShooterSpeeds(operatorJoystick.getAxisRT() - operatorJoystick.getAxisLT(), false);
			intake.stopMotors();
		}
		if (operatorJoystick.getDpadLeft())
			holdIntakeDown = false;
		if (operatorJoystick.getButtonRS()) {
			shooterWheels.prepShot();
		}
		if (operatorJoystick.getButtonA()) {
			shooterWheels.autoFire();
		}

		if (operatorJoystick.getButtonLS()) {
			if (camera.isCameraManual())
				camera.setCameraValuesAutomatic();
			else
				camera.setCameraValues(1, 1);
		}
		if (operatorJoystick.getDpadUp()) {
			intake.internalExtendIntake();
			shooterWheels.delayedShooterPosition(ShooterHeight.HIGH);
			holdIntakeDown = false;
//			camera.setCameraValues(1, 1);
		}
		if (operatorJoystick.getDpadRight()) {
			intake.internalExtendIntake();
			shooterWheels.delayedShooterPosition(ShooterHeight.MID);
//			camera.setCameraValues(1, 1);
			holdIntakeDown = true;
		}
		if (operatorJoystick.getDpadDown()) {
			intake.internalExtendIntake();
//			camera.setCameraValuesAutomatic();
			shooterWheels.delayedShooterPosition(ShooterHeight.LOW);
			holdIntakeDown = false;
		}

		if (operatorJoystick.getButtonRB()) {
			if (shooterWheels.getIfAutoFire())// if is currently autofiring will
				// override the auto fire
				shooterWheels.overrideAutoShot();
			else
				shooterWheels.pushBall();
		} else if (!shooterWheels.getIfAutoFire())// this just brings the servo back
			// to its place if none of the
			// previous cases apply
			shooterWheels.resetServoAngle();

		if (shooterWheels.getIfAutoFire() || visionShot.isRunning())
			compressor.stop();
		else
			compressor.start();

		if ((operatorJoystick.getButtonLB() || driverJoystick.getDpadRight() || visionShot.isRunning())
				&& !cameraLineUp) {
			visionShot.run();}

		if (operatorJoystick.getButtonSt()||operatorJoystick.getDpadDown()||driverJoystick.getButtonSel()) {
			shooterWheels.resetShooterAutoStuff();
			visionShot.reset();
		}

	}

	private void sharedRun() {
		if (!shooterWheels.getIfPreppingShot())
			if (operatorJoystick.getButtonSel()) {
				intake.internalExtendIntake();
				useArmAttachmentToggle.setManual(true);
			} else if (driverJoystick.getButtonA() || operatorJoystick.getButtonB()
					|| OldAbstractShakershooterWheels.delayedArmMove || operatorJoystick.getDpadLeft() || holdIntakeDown) {
				// this runs if intaking ball too
				intake.internalExtendIntake();
			} else
				// Retract intake
				intake.internalRetractIntake();

		// arm attachment
		useArmAttachmentToggle.giveToggleInput(driverJoystick.getButtonY() || operatorJoystick.getButtonY());
		if (useArmAttachmentToggle.getToggleOutput())
			intake.setArmAttachmentDown();
		else
			intake.setArmAttachmentUp();

	}

	public void configureAutoShot() {
		// this is a driver auto line up without shooting
		if (driverJoystick.getDpadLeft()) {
			visionShot.setUseMultipleFrames(false);
			visionShot.setShootAfterAligned(false);
			visionShot.run();
		}
		if (operatorJoystick.getButtonLB() || driverJoystick.getDpadRight()) {
			visionShot.setUseMultipleFrames(true);
			visionShot.setShootAfterAligned(true);
			visionShot.run();
		}
	}

	public void disableRun() {
		// runs disable methods of subsystems that fall under the driver
		driveTrain.disable();
		shooterWheels.disable();
		intake.disable();
		visionShot.reset();
	}

	public void updateSmartDash() {
		intake.updateSmartDash();
		shooterWheels.updateSmartDash();
		driveTrain.updateSmartDash();
		SmartDashboard.putString("Current Driver Input:", getDriveType().toString());
		SmartDashboard.putNumber("turning value", driverJoystick.getAxisLeftX());
	}

	public void reset() {
		shooterWheels.reset();
		intake.reset();
	}

	@Override
	public void debug() {
		driveTrain.debug();
		intake.debug();
		shooterWheels.debug();
	}

	public DriveType getDriveType() {
		// reads data of the smart dashboard and converts to enum DriveType
		String driverInputType = (String) driveTypeChooser.getSelected();
		switch (driverInputType) {
		default:
		case "GTA":
			return DriveType.GTA;
		case "ARCADE":
			return DriveType.ARCADE;
		case "TANK":
			return DriveType.TANK;
		case "SINGLE_ARCADE":
			return DriveType.SINGLE_ARCADE;
		}
	}

	public enum DriveType {
		TANK, ARCADE, GTA, SINGLE_ARCADE
	}

}
/***************
 * old driver auto lineup code
 ***************/
// // THIS IS UNTESTED!!!!
// private static void driverAutoLineUp() {
// cameraLineUp = true;
// if (!runOnlyOnce) {
// driveTrain.resetEncoders();
// if (camera.getTarget() != null)
// target = driveTrain.getAngle()
// + camera.getTarget().ThetaDifference;
// runOnlyOnce = true;
// }
//
// double driverThrottle = driverJoystick.getAxisRT() -
// driverJoystick.getAxisLT();
// // Exit the autoline up after the
// if (driveTrain.setAngleWithDriving(target, 0.7, driverThrottle) ||
// driverJoystick.getDpadUp()) {
// SmartDashboard.putBoolean("Done Lining Up", true);
// cameraLineUp = false;
// runOnlyOnce = false;
// target = 0;
//
// } else
// SmartDashboard.putBoolean("Done Lining Up", false);
//
// }