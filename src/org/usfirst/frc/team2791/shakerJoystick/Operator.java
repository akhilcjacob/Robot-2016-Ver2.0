package org.usfirst.frc.team2791.shakerJoystick;

import org.usfirst.frc.team2791.overridenClasses.Joystick;
import org.usfirst.frc.team2791.util.Constants;

public class Operator extends Joystick {
    private static Operator operatorJoystickInstance;

    private Operator() {
        super(Constants.JOYSTICK_OPERATOR_PORT);
    }

    public static Operator getInstance() {
        if (operatorJoystickInstance == null)
            operatorJoystickInstance = new Operator();
        return operatorJoystickInstance;

    }
    // place any special controls here


}
