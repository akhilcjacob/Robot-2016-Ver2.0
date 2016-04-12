package org.usfirst.frc.team2791.competitionSubsystems;

import edu.wpi.first.wpilibj.AnalogInput;
import edu.wpi.first.wpilibj.CANTalon;
import edu.wpi.first.wpilibj.Servo;
import org.usfirst.frc.team2791.abstractSubsystems.AbstractShakerShooterWheels;
import org.usfirst.frc.team2791.util.Constants;

/**
 * Created by Akhil on 4/10/2016.
 * This class extends off of AbstractShakerShooterWheels, it is just meant
 * to initalize certain sensors and other misc. for the competition robot
 */
public class ShakerShooterWheels extends AbstractShakerShooterWheels {
    public ShakerShooterWheels() {
        super();
        leftShooterTalon = new CANTalon(Constants.SHOOTER_TALON_LEFT_PORT);
        rightShooterTalon = new CANTalon(Constants.SHOOTER_TALON_RIGHT_PORT);
        servo = new Servo(Constants.BALL_AID_SERVO_PORT);
        distanceSensor = new AnalogInput(Constants.BALL_DISTANCE_SENSOR_PORT);
        init();
    }
}
