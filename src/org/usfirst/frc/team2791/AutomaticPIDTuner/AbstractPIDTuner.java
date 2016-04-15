package org.usfirst.frc.team2791.AutomaticPIDTuner;

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
     * incrementer, then set and see if the error decreases or increases. if the error increases then
     * undo the most recent change and then decrease the incrementer because we are closer to a good
     * parameter, if the error increases the increase the corresponding incrementer because that means
     * we are really far away from the a good value.
     *
     * @param firstSetpoint starting firstSetpoint, this code will oscilate between
     */
    public void calculateAndUpdateGains(double firstSetpoint, double secondSetpoint) {
        //keep running until the imcrementers are near 0
        double currentError = 100;
        int passNumber = 0;//how many attempts it takes
        while (pidIncrementer[0] + pidIncrementer[1] + pidIncrementer[2] > tolerance) {
            for (int x = 0; x < 3; x++) {
                pid[x] += pidIncrementer[x];
                setGains(pid[0], pid[1], pid[2]);
                set(firstSetpoint);
                sleep(3000);
                currentError = getError();
                if (currentError < bestError) {
                    bestError = currentError;
                    pidIncrementer[x] *= 1.1;
                } else {
                    pid[x] -= 2.0 * pidIncrementer[x];
                    setGains(pid[0], pid[1], pid[2]);
                    set(secondSetpoint);
                    sleep(3000);
                    currentError = getError();
                    if (currentError < bestError) {
                        bestError = currentError;
                        pidIncrementer[x] *= 1.1;
                    } else {
                        pid[x] += pidIncrementer[x];
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

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
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



