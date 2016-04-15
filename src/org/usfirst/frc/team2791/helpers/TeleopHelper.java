package org.usfirst.frc.team2791.helpers;

import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import org.usfirst.frc.team2791.commands.IntakeAndShooterSynergy;
import org.usfirst.frc.team2791.util.Latch;

import static org.usfirst.frc.team2791.robot.Robot.*;

/**
 * Created by Akhil on 4/14/2016.
 * This class runs everything needed in teleop
 */
public class TeleopHelper extends ShakerHelper {
    //Singleton class
    private static TeleopHelper teleop;

    private SendableChooser driveTypeChooser;
    private Latch chevalArmLatch;

    private TeleopHelper() {
        // init
        // Smartdashboard dropdown menu that lets you select the input mode
        driveTypeChooser = new SendableChooser();
        SmartDashboard.putData("Drive Chooser", driveTypeChooser);
        driveTypeChooser.addObject("Tank Drive", "TANK");
        driveTypeChooser.addObject("Arcade Drive", "ARCADE");
        driveTypeChooser.addDefault("GTA Drive", "GTA");
        driveTypeChooser.addObject("Single Arcade", "SINGLE_ARCADE");
        //Latch - this sort of works like a light switch
        chevalArmLatch = new Latch(false);

    }

    public static TeleopHelper getInstance() {
        if (teleop == null) {
            teleop = new TeleopHelper();
        }
        return teleop;
    }

    public void run() {
        //decides when to turn the compressor on and off
        compressorController();
        //This mostly deals with drive train
        driverControls();
        //deals with basically everything else
        operatorControls();
        //deals with things that are dealt by both the operator and the driver
        sharedControls();
        //class deals with prevention of collision between intake and shooter
        IntakeAndShooterSynergy.run();
    }

    private void driverControls() {
        //This reads the smart dashboard to get the selected mode and control when the driveTrain isn't busy
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
        //if the driver suddenly moves past some thresh speed then break out of control loops and visionlineup
        //and also cancel shooter stuff
        if (driveTrain.getIfBusy() && Math.abs(driverJoystick.getGtaDriveLeft()) > 0.3) {
            System.out.println("Driver exiting pid b/c moving too fast");
            driveTrain.forceBreakPID();
            visionShot.reset();
            //TODO before running this we need lights to show that we are firing to let driver know
            //shooterWheels.resetShooterFlags();
        }
    }

    private void operatorControls() {
        //This is all dealing with intaking and out-taking
        if (operatorJoystick.getButtonB()) {
            // Run intake inward with assistance of the shooter wheel
            shooterWheels.setToggledShooterSpeeds(-0.6, false);
            intake.pullBall();
        } else if (operatorJoystick.getButtonX()) {
            // Run reverse if button pressed
            shooterWheels.setToggledShooterSpeeds(0.6, false);
            intake.pushBall();
        } else {
            //this is meant for manual control of the shooter wheels
            shooterWheels.setToggledShooterSpeeds(operatorJoystick.getAxisRT() - operatorJoystick.getAxisLT(), false);
            intake.stopMotors();
        }

        //This switches between automatic and manual mode on the camera
        if (operatorJoystick.getButtonLS()) {
            camera.switchMode();
        }
        //This is for the acutuation of servo arm, if shooter is firing right now
        //it will force override the shot
        if (operatorJoystick.getButtonRB()) {
            if (shooterWheels.getIfCompleteShot())
                // if is currently doing a complete shot will override the auto fire
                shooterWheels.overrideAutoShot();
            else
                shooterWheels.extendServoArm();
        } else if (!shooterWheels.getIfCompleteShot())// this just brings the servo back
            // to its place if none of the previous cases apply
            shooterWheels.retractServoArm();
    }

    private void sharedControls() {
        //This latch controls the cheval arm
        chevalArmLatch.giveToggleInput(driverJoystick.getButtonY() || operatorJoystick.getButtonY());
        shooterArm.setChevalArm(chevalArmLatch.get());
        //constantly checks if either driver/op hit vision shot
        configureVisionShot();

    }

    private void compressorController() {
        if (shooterWheels.getIfBusy())
            compressor.stop();
        else
            compressor.start();
    }

    private void configureVisionShot() {
        //Configure the vision shot depending on what buttons get pressed
        //dpad left is for the driver and will do a lineup with a single frame
        if (driverJoystick.getDpadLeft()) {
            visionShot.setUseMultipleFrames(false);
            visionShot.setShootAfterAligned(false);
            visionShot.start();
        }
        //OpJoy lb and driveJoy dpad right uses multiple frames to lineup and fire
        if (operatorJoystick.getButtonLB() || driverJoystick.getDpadRight()) {
            visionShot.setUseMultipleFrames(true);
            visionShot.setShootAfterAligned(true);
            visionShot.start();
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
    }

    public void reset() {
        shooterWheels.reset();
        intake.reset();
    }

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
