package org.usfirst.frc.team2791.commands;

import static org.usfirst.frc.team2791.abstractSubsystems.AbstractShakerShooterArm.ShooterHeight;
import static org.usfirst.frc.team2791.robot.Robot.*;

/**
 * Created by Akhil on 4/14/2016.
 * This class is built to deal with the intake and shooter arm collision issues
 */
public class IntakeAndShooterSynergy {
    //if it isnt null then shooter will try to get there when possible
    private static ShooterHeight position = null;

    public static void run() {
        if (intake.getIfExtended()) {
            //if the intake is already down then just move the arm without waiting
            //if the operator hits the correspoing dpad or the position variable is set
            //then act accordingly becasue intake isn't  in the way.
            if (operatorJoystick.getDpadUp() || position == ShooterHeight.HIGH) {
                shooterWheels.shooterArmMoveAndPrepShot();
                shooterArm.setHigh();
            }
            if (operatorJoystick.getDpadRight() || position == ShooterHeight.MID) {
                shooterWheels.shooterArmMoveAndPrepShot();
                shooterArm.setMiddle();
            }
            if (operatorJoystick.getDpadDown() || position == ShooterHeight.LOW) {
                shooterArm.setLow();
                shooterWheels.resetShooterFlags();
            }
        } else if (intake.getIfRetracted()) {
            //if the intake is retracted and shooter arm is not moving the run other controls
            //if the operator press the corresponding button to the arm then set the position var
            //that when the intake is extended will cause the arm to move to
            if (operatorJoystick.getDpadUp()) {
                intake.extendIntake(true);
                position = ShooterHeight.HIGH;
            }
            if (operatorJoystick.getDpadRight()) {
                intake.extendIntake(true);
                position = ShooterHeight.MID;
            }
            if (operatorJoystick.getDpadDown()) {
                intake.extendIntake(true);
                position = ShooterHeight.LOW;
            }
        }


        if (position != null && shooterArm.getShooterHeight().equals(position)) {
            if (shooterArm.getIfShooterMiddle()) {
                //do nothing because u dont want the intake to go up
            } else {
                //resets the postion variable
                position = null;
                //This breaks out of any holds it is in and then retracts the intake
                intake.retractIntake(false);
            }
        }

        if (operatorJoystick.getDpadLeft()) {
            /*override to release the intake and to extend the intake however it
            will not work if the shooter arm is moving
            */
            if (!shooterArm.getIfShooterMoving()) {
                intake.breakIntakeHold();
                intake.extendIntake();
            }
        } else if (operatorJoystick.getButtonB()) {
            //this will force hold the intake for intaking
            intake.extendIntake(true);
        } else
            //if the intake isnt busy with anything else then bring it back up
            intake.toggledRetractIntake();

    }


}
