package org.usfirst.frc.team2791.AutomaticPIDTuner;

import edu.wpi.first.wpilibj.Timer;

import java.util.Arrays;

/**
 * Created by Akhil on 4/13/2016.
 * This class uses the twiddle algorithm to calculate PID gains
 * This is based on theory and has yet to be tested
 * ---------------------------
 * This code was possible because of Sebastian Thrun, for more information on how
 * this works here's a link to the explanation: https://www.youtube.com/watch?v=2uQ2BSzDvXs
 */
public abstract class AbstractPIDTuner implements Runnable {
    protected Thread tunerThread;
    protected AbstractPIDTuner tuner;
    protected boolean running = true;
    //error tolerance
    private double tolerance;
    private double bestError = 1000;
    //calculated pid values
    private double[] pid = {0, 0, 0};

    private double[] pidIncrementer = {.1, .1, .1};


    protected AbstractPIDTuner() {
        tolerance = 0.0001;
    }

    abstract AbstractPIDTuner getInstance();


    public void setTolerance(double newTolerence) {
        tolerance = newTolerence;
    }

    public void destroy() {
        running = false;
        tuner = null;
        tunerThread = null;
    }

    abstract void set(double setPoint);

    abstract double getError();

    abstract void setGains(double p, double i, double d);

    /**
     * This method is the main tuner, the idea behind is basically a little guess and check in
     * a sense. First we start with one of constants(p,i,d) then increment it with the correspoinding
     * incrementer, then set and see if the error decreases or increases. if the accumulated error increases then
     * undo the most recent change and then decrease the incrementer because we are closer to a good
     * parameter, if the error increases the increase the corresponding incrementer because that means
     * we are really far away from the a good value.
     *
     * @param firstSetpoint starting firstSetpoint, this code will oscilate between
     */
    public void calculateAndUpdateGains(double firstSetpoint, double secondSetpoint) {
        double currentError = 1000;//make it some arbitrary number that error could never be
        int passNumber = 0;//how many attempts it takes
        //keep running until the imcrementers are near 0
        while (pidIncrementer[0] + pidIncrementer[1] + pidIncrementer[2] > tolerance) {
            //iterate through each gain
            for (int x = 0; x < 3; x++) {
                //add increment value to current gain
                pid[x] += pidIncrementer[x];
                //modify the gains for the pid we are currently tuning
                setGains(pid[0], pid[1], pid[2]);
                //set a setpoint
                set(firstSetpoint);
                //get the total amount of error in 3 seconds
                currentError = errorAccumulator();
                //if the new error is lower then the best one we are too far away
                //from a good gain constant
                if (currentError < bestError) {
                    //save the error
                    bestError = currentError;
                    //increase it because we were still able to fix the error
                    pidIncrementer[x] *= 1.1;
                } else {
                    //if increasing the gain didn't help we are getting close
                    //reset the gain value
                    pid[x] -= 2.0 * pidIncrementer[x];
                    //modify the gains again
                    setGains(pid[0], pid[1], pid[2]);
                    //change the setpoint
                    set(secondSetpoint);
                    //accumulate the error again
                    currentError = errorAccumulator();
                    //check if the error is better
                    if (currentError < bestError) {
                        //if better save the error
                        bestError = currentError;
                        //increase the pid gain b/c we were able to correct
                        pidIncrementer[x] *= 1.1;
                    } else {
                        //if not increase the value
                        pid[x] += pidIncrementer[x];
                        //then decrease the incrementer because we are closer
                        pidIncrementer[x] *= 0.9;
                    }
                }

            }
            System.out.println("Automatic Tuning Attempt #" + passNumber + ": PID values: " + Arrays.toString(pid) + " Current error: "
                    + currentError + " Best error " + bestError);
            passNumber++;
        }
        running = false;
        System.out.println("The best pid values are " + Arrays.toString(pid));
    }

    /**
     * This class runs for three seconds and totals the amount of error, including
     * oscillation error.
     *
     * @return The total amount of error in the three seconds
     */
    private double errorAccumulator() {
        Timer tempTimer = new Timer();
        double totalError = 0;
        tempTimer.reset();
        tempTimer.start();
        //This runs for 3 seconds and totals the error
        while (tempTimer.get() < 3)
            totalError += Math.abs(getError());
        if (totalError == 0)
            //If this runs that means there was no error at all which is impossible
            System.out.println("There is something wrong, error wasn't accumulated properly");
        return totalError;
    }

    public void start() {
        tunerThread.start();
        running = true;
    }

    public void run() {
        while (running) {
            calculateAndUpdateGains(10, 0);
        }
    }
}



