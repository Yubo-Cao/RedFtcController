package org.firstinspires.ftc.teamcode;

import android.util.Log;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.util.ElapsedTime;

public class DriveTrain {
    private final DcMotor backLeft;
    private final DcMotor backRight;
    private final DcMotor frontLeft;
    private final DcMotor frontRight;

    private final ElapsedTime time = new ElapsedTime();

    public DriveTrain(HardwareMap map) {
        backLeft = map.get(DcMotor.class, "backLeft");
        backRight = map.get(DcMotor.class, "backRight");
        frontLeft = map.get(DcMotor.class, "frontLeft");
        frontRight = map.get(DcMotor.class, "frontRight");
        backLeft.setDirection(DcMotor.Direction.FORWARD);
        backRight.setDirection(DcMotor.Direction.REVERSE);
        frontLeft.setDirection(DcMotor.Direction.FORWARD);
        frontRight.setDirection(DcMotor.Direction.REVERSE);
    }

    /**
     * Waits for a certain amount of time
     *
     * @param ms the amount of time to wait in milliseconds
     */
    public void wait(double ms) {
        time.reset();
        while (time.milliseconds() < ms) {
            Log.d("DriveTrain", "Waiting for " + (ms - time.milliseconds()) + "ms");
        }
    }

    /**
     * Sets the power of all motors
     *
     * @param power the power to set the motors to (between 0 and 1)
     */
    public void powers(double power) {
        backLeft.setPower(power);
        backRight.setPower(power);
        frontLeft.setPower(power);
        frontRight.setPower(power);
    }

    /**
     * Moves the robot
     *
     * @param x the x component of the vector
     * @param y the y component of the vector
     * @param rotate the rotation component of the vector
     */
    public void move(double x, double y, double rotate) {
        double r = Math.hypot(x, -y);
        double robotAngle = Math.atan2(-y, x) - Math.PI / 4;
        double rightX = rotate / 1.25;
        final double v1 = r * Math.cos(robotAngle) + rightX;
        final double v2 = r * Math.sin(robotAngle) - rightX;
        final double v3 = r * Math.sin(robotAngle) + rightX;
        final double v4 = r * Math.cos(robotAngle) - rightX;

        frontRight.setPower(v1);
        frontLeft.setPower(v4);
        backRight.setPower(v3);
        backLeft.setPower(v2);
    }

    /**
     * Moves the robot. Helper method for move(double, double, double)
     *
     * @param x the x component of the vector
     * @param y the y component of the vector
     */
    public void move(double x, double y) {
        move(x, y, 0);
    }

    /** Moves the robot forward for a block */
    public void blockForward() {
        move(0, 0.75);
        wait(1000.0);
        stop();
    }

    /** Moves the robot backward for a block */
    public void blockBackward() {
        move(0, -0.75);
        wait(1000.0);
        stop();
    }

    /** Moves the robot left for a block */
    public void blockLeft() {
        move(-0.75, 0);
        wait(1000.0);
        stop();
    }

    /** Moves the robot right for a block */
    public void blockRight() {
        move(0.75, 0);
        wait(1000.0);
        stop();
    }

    /** Stops the robot */
    public void stop() {
        backLeft.setPower(0);
        backRight.setPower(0);
        frontLeft.setPower(0);
        frontRight.setPower(0);
        this.time.reset();
    }
}
