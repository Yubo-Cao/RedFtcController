package org.firstinspires.ftc.teamcode.components;

import android.util.Log;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.util.ElapsedTime;

public final class DriveTrain {
    private final DcMotor backLeft;
    private final DcMotor backRight;
    private final DcMotor frontLeft;
    private final DcMotor frontRight;
    private final ElapsedTime time;

    public DriveTrain(HardwareMap map) {
        this.time = new ElapsedTime();
        this.backLeft = map.get(DcMotor.class, "backLeft");
        this.backRight = map.get(DcMotor.class, "backRight");
        this.frontLeft = map.get(DcMotor.class, "frontLeft");
        this.frontRight = map.get(DcMotor.class, "frontRight");
        this.backLeft.setDirection(DcMotorSimple.Direction.FORWARD);
        this.backRight.setDirection(DcMotorSimple.Direction.REVERSE);
        this.frontLeft.setDirection(DcMotorSimple.Direction.FORWARD);
        this.frontRight.setDirection(DcMotorSimple.Direction.REVERSE);
    }

    public void wait(double ms) {
        this.time.reset();

        while (this.time.milliseconds() < ms) {
            Log.d("DriveTrain", "Waiting for " + (ms - this.time.milliseconds()) + "ms");
        }
    }

    public void powers(double pfr, double pfl, double pbr, double pbl) {
        this.backRight.setPower(pbr);
        this.backLeft.setPower(pbl);
        this.frontRight.setPower(pfr);
        this.frontLeft.setPower(pfl);
    }

    public void move(double x, double y, double rotate) {
        double robotAngle = -y;
        double r = Math.hypot(x, robotAngle);
        double rightX = -y;
        robotAngle = Math.atan2(rightX, x) - 0.7853981633974483;
        rightX = rotate / 1.25;
        double v1 = r * Math.cos(robotAngle) + rightX;
        double v2 = r * Math.sin(robotAngle) - rightX;
        double v3 = r * Math.sin(robotAngle) + rightX;
        double v4 = r * Math.cos(robotAngle) - rightX;
        this.powers(v1, v4, v3, v2);
    }

    public void move(double x, double y) {
        move(x, y, 0.0);
    }

    public void blockForward() {
        move(0, 0.75);
        this.wait(1000.0);
        this.stop();
    }

    public void blockBackward() {
        move(0, -0.75);
        this.wait(1000.0);
        this.stop();
    }

    public void blockLeft() {
        move(-0.75, 0.0);
        this.wait(1000.0);
        this.stop();
    }

    public void blockRight() {
        move(0.75, 0.0);
        this.wait(1000.0);
        this.stop();
    }

    public void stop() {
        this.backLeft.setPower(0.0);
        this.backRight.setPower(0.0);
        this.frontLeft.setPower(0.0);
        this.frontRight.setPower(0.0);
        this.time.reset();
    }
}
