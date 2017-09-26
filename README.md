# Robot_2016
Team 2791 2016 Robot Code
-----------------------------------------------------------
This is the rework code for FRC Team 2791, Shaker Robotics
Created by Akhil Jacob(akhilcjacob.public@gmail.com)
------------------------------------------------------------
CONTROLS:
Driver:
   
    GTA control(Default, Recommended): RT - forward, LT - Backward, LS(x axis) - spin
    Arcade control(bug in code, no one uses so no one ever fixed it): LS - forward, RS - spin
    Single Arcade control(bug in code, no one uses so no one ever fixed it): LS - forward(Y Axis)/spin(X axis)

    Cheval Arm: Button Y
    Dpad Left: Use one frame to lineup but no shooting
    Dpad Right: Full vision shot(multiple frames, and fire, high accuracy)

    Notes: vision shot will be canceled if the driver touches trys to move the driveTrain
Operator:
   
    Start Button: cancel shooter pids, and vision shots
    Select Button: switch between vision tracking mode and regular mode on the camera(defaults on vision tracking)

    Dpad Left: force extend intake
    Dpad Up/Right/Down: Move arm up/middle/down

    RS Button: Prep the shot(runs the shooter wheels but doesn't shoot)
    LS Button: Full vision shot but uses a higher amount of error tolerance(use LB, seen lower)

    Button A: Shoot, automatically switches speeds depending on arm angle
    Button Y: Cheval Arm actuation
    Button X: Outake from intake using shooter wheels and intake wheels
    Button B: Intake from intake using shooter wheels and intake wheels

    Button LB: Full vision shot but lower vision error to force higher accuracy
    Button RB: Moves the servo arm, and if shooting it skips on waiting for pid

    Axis RT: Run shooter wheels outward
    Axis LT: Run shooter wheels inward
-------------------------------------------------------------------------------------
Side Notes:
    
    -After a shot the shooter arm will drop this is used when in competition to be ready for the next shot right
        away
    -Once again, an attempt to move the drive train while vision shot is running will cause the vision shot to end
    -intake should automatically move out of the way for the shooter




