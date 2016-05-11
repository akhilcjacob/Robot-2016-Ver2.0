package org.usfirst.frc.team2791.practicebotSubsystems;

import edu.wpi.first.wpilibj.AnalogInput;
import edu.wpi.first.wpilibj.CANTalon;
import edu.wpi.first.wpilibj.Servo;
import org.usfirst.frc.team2791.abstractSubsystems.AbstractShakerShooterWheels;

/**
 * Created by Akhil on 4/10/2016.
 * This class is used for the practice robot which utilizes different ports
 * electronics(solenoids) and ports than the competiton robot
 */
public class PracticeShakerShooterWheels extends AbstractShakerShooterWheels {
    public PracticeShakerShooterWheels() {
        super();
        leftShooterTalon = new CANTalon(PracticebotPorts.SHOOTER_TALON_LEFT_PORT);
        rightShooterTalon = new CANTalon(PracticebotPorts.SHOOTER_TALON_RIGHT_PORT);
        // servo
        servo = new Servo(PracticebotPorts.BALL_AID_SERVO_PORT);
        // analog sensor
        distanceSensor = new AnalogInput(PracticebotPorts.BALL_DISTANCE_SENSOR_PORT);
        init();
    }
}
