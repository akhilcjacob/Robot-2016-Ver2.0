package org.usfirst.frc.team2791.robot;

import edu.wpi.first.wpilibj.Compressor;
import edu.wpi.first.wpilibj.IterativeRobot;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import org.usfirst.frc.team2791.abstractSubsystems.AbstractShakerDriveTrain;
import org.usfirst.frc.team2791.abstractSubsystems.AbstractShakerIntake;
import org.usfirst.frc.team2791.abstractSubsystems.AbstractShakerShooterArm;
import org.usfirst.frc.team2791.abstractSubsystems.AbstractShakerShooterWheels;
import org.usfirst.frc.team2791.commands.AutoLineUpShot;
import org.usfirst.frc.team2791.competitionSubsystems.ShakerDriveTrain;
import org.usfirst.frc.team2791.competitionSubsystems.ShakerIntake;
import org.usfirst.frc.team2791.competitionSubsystems.ShakerShooterArm;
import org.usfirst.frc.team2791.competitionSubsystems.ShakerShooterWheels;
import org.usfirst.frc.team2791.helpers.AutonHelper;
import org.usfirst.frc.team2791.helpers.TeleopHelper;
import org.usfirst.frc.team2791.practicebotSubsystems.PracticeShakerShooterWheels;
import org.usfirst.frc.team2791.practicebotSubsystems.PracticebotShakerDriveTrain;
import org.usfirst.frc.team2791.practicebotSubsystems.PracticebotShakerIntake;
import org.usfirst.frc.team2791.shakerJoystick.Driver;
import org.usfirst.frc.team2791.shakerJoystick.Operator;
import org.usfirst.frc.team2791.util.Constants;
import org.usfirst.frc.team2791.util.ShakerCamera;
import org.usfirst.frc.team2791.util.ShakerLights;

public class Robot extends IterativeRobot {
    public static final boolean COMPETITION_ROBOT = true;

    public static boolean debuggingMode = false;
    // Modes
    public static GamePeriod gamePeriod;
    // Joysticks
    public static Driver driverJoystick;
    public static Operator operatorJoystick;

    public static AbstractShakerIntake intake;
    public static AbstractShakerDriveTrain driveTrain;
    public static AbstractShakerShooterArm shooterArm;
    public static AbstractShakerShooterWheels shooterWheels;
    //command
    public static AutoLineUpShot visionShot;
    // camera
    public static ShakerCamera camera;
    // other
    public static Compressor compressor;
    public static Thread shooterThread;
    public static Thread cameraThread;
    public static Thread driveTrainThread;
    public static Thread AutoLineUpThread;
    // helpers
    private TeleopHelper teleopHelper;
    private AutonHelper autonHelper;
    //LIGHTS!!!!!!!!!
    public static ShakerLights lights;

    // MAIN ROBOT CODE
    public void robotInit() {
        // game period changed when ever game mode changes
        // (TELOP,AUTON,DISABLED,ETC.)
        System.out.println("Starting to init my systems.");
        gamePeriod = GamePeriod.DISABLED;

        // Singletons - only one instance of them is created
        // Shaker joysticks
        driverJoystick = Driver.getInstance();
        operatorJoystick = Operator.getInstance();
        if (COMPETITION_ROBOT) {
            // competition robot
            driveTrain = new ShakerDriveTrain();
            intake = new ShakerIntake();
            shooterWheels = new ShakerShooterWheels();
            shooterArm = new ShakerShooterArm();
        } else {
            // practicebot
            driveTrain = new PracticebotShakerDriveTrain();
            intake = new PracticebotShakerIntake();
            shooterWheels = new PracticeShakerShooterWheels();
            shooterArm = new ShakerShooterArm();
        }
        // Camera and shooter and drivetrain are put on their own thread to
        // prevent interference with main robot code
        shooterThread = new Thread(shooterWheels);
        shooterThread.start();

        driveTrainThread = new Thread(driveTrain);
        driveTrainThread.start();

        camera = new ShakerCamera();
        cameraThread = new Thread(camera);
        cameraThread.start();
        camera.setCameraMode(true);

        visionShot = new AutoLineUpShot();
        AutoLineUpThread = new Thread(visionShot);

        lights = new ShakerLights();
        new Thread(lights);

        autonHelper = AutonHelper.getInstance();
        teleopHelper = TeleopHelper.getInstance();

        compressor = new Compressor(Constants.PCM_MODULE);

        SmartDashboard.putNumber("shooter offset", AutoLineUpShot.shootOffset);
        SmartDashboard.putBoolean("DEBUGGING MODE", debuggingMode);
    }

    public void autonomousInit() {
        gamePeriod = GamePeriod.AUTONOMOUS;

    }

    public void teleopInit() {
        gamePeriod = GamePeriod.TELEOP;
    }

    public void disabledInit() {
        gamePeriod = GamePeriod.DISABLED;
    }

    public void autonomousPeriodic() {
        super.autonomousPeriodic();
        autonHelper.run();
        autonHelper.updateSmartDash();
        alwaysUpdatedSmartDashValues();
    }

    public void teleopPeriodic() {
        teleopHelper.run();
        teleopHelper.updateSmartDash();
        alwaysUpdatedSmartDashValues();
    }

    public void disabledPeriodic() {
        teleopHelper.disableRun();
        autonHelper.disableRun();
        alwaysUpdatedSmartDashValues();
        //reset the encoders if need be, this is rarely used(usually debugging)
        if (operatorJoystick.getButtonSt()) {
            driveTrain.resetEncoders();
        }
        visionShot.reset();
        shooterWheels.resetShooterFlags();
        driveTrain.forceBreakPID();
        compressor.stop();
    }

    private void alwaysUpdatedSmartDashValues() {
        //These are values that are always updated even in disabled
        SmartDashboard.putNumber("Gyro Rate", driveTrain.getEncoderAngleRate());
        SmartDashboard.putNumber("Gyro angle", driveTrain.getAngle());
        debuggingMode = SmartDashboard.getBoolean("DEBUGGING MODE");
        AutoLineUpShot.shootOffset = SmartDashboard.getNumber("shooter offset");
        if (debuggingMode) {
            driveTrain.debug();
            intake.debug();
            shooterWheels.debug();
            shooterArm.debug();
        }
    }

    // ENUMS
    public enum GamePeriod {
        AUTONOMOUS, TELEOP, DISABLED
    }

}