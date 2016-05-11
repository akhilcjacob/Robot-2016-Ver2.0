package org.usfirst.frc.team2791.practicebotSubsystems;

/**
 * Created by Akhil on 4/10/2016.
 * This class holds all the important port numbers for quick and easy changes
 */

class PracticebotPorts {

    // ANALOG
    static final int BALL_DISTANCE_SENSOR_PORT = 3;

    // DIO
    static final int LEFT_DRIVE_ENCODER_PORT_A = 2;
    static final int LEFT_DRIVE_ENCODER_PORT_B = 3;
    static final int RIGHT_DRIVE_ENCOODER_PORT_A = 0;
    static final int RIGHT_DRIVE_ENCODER_PORT_B = 1;

    // PWM PORTS
    static final int DRIVE_TALON_LEFT_PORT_FRONT = 1;
    static final int DRIVE_TALON_LEFT_PORT_BACK = 2;
    static final int DRIVE_TALON_RIGHT_PORT_FRONT = 0;
    static final int DRIVE_TALON_RIGHT_PORT_BACK = 3;
    static final int INTAKE_TALON_LEFT_PORT = 8;
    static final int INTAKE_TALON_RIGHT_PORT = 9;
    static final int BALL_AID_SERVO_PORT = 4;

    // PCM PORTS
    // First Pcm module
    static final int DRIVE_PISTON_FORWARD = 1;
    static final int DRIVE_PISTON_REVERSE = 0;
    static final int INTAKE_PISTON_CHANNEL_FORWARD = 2;
    static final int INTAKE_PISTON_CHANNEL_REVERSE = 3;
    static final int LONG_PISTON_FORWARD = 4;
    static final int LONG_PISTON_REVERSE = 5;
    static final int INTAKE_ARM_CHANNEL_FORWARD = 6;
    static final int INTAKE_ARM_CHANNEL_REVERSE = 7;
    //  static final int SHOOTER_PISTON_CHANNEL_SECOND_LEVEL_CHANNEL = 0;
    // second pcm module
    static final int SHORT_PISTON_FORWARD = 0;
    static final int SHORT_PISTON_REVERSE = 1;
    // CAN PracticePorts
    static final int PCM_MODULE = 20;
    static final int SECOND_PCM_MODULE = 21;
    static final int SHOOTER_TALON_RIGHT_PORT = 10;
    static final int SHOOTER_TALON_LEFT_PORT = 11;
}
