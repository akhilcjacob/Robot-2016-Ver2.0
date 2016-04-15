package org.usfirst.frc.team2791.AutomaticPIDTuner;

import static org.usfirst.frc.team2791.robot.Robot.driveTrain;

/**
 * Created by Akhil on 4/14/2016.
 * This class extends off of the AbstractPIDTuner and sets the set
 * method to control the angle, It will then tune the pid and find
 * optimum values.
 * -----
 * This code was possible because of Sebastian Thrun, for more information on how
 * this works here's a link to the explanation: https://www.youtube.com/watch?v=2uQ2BSzDvXs
 */
public class StationaryAnglePIDTuner extends AbstractPIDTuner {


    private StationaryAnglePIDTuner() {
        super();
    }

    AbstractPIDTuner getInstance() {
        //Singleton class
        if (tuner == null)
            tuner = new StationaryAnglePIDTuner();
        if (tunerThread == null) {
            tunerThread = new Thread();
        }
        return tuner;
    }

    void set(double setPoint) {
        driveTrain.setAngle(setPoint, 0.6, true, false);
    }

    @Override
    double getError() {
        return driveTrain.getStationaryPIDError();
    }

    @Override
    void setGains(double p, double i, double d) {

    }

}
