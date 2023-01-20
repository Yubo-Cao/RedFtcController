package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.util.ElapsedTime;

public class DriveTrain {
    private final DcMotor backLeft;
    private final DcMotor backRight;
    private final DcMotor frontLeft;
    private final DcMotor frontRight;

    private final ElapsedTime time = new ElapsedTime();

    public void wait(double ms) {
        time.reset();
        while (time.milliseconds() < ms) {
        }
    }

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

    public void move(double x, double y) {
        move(x, y, 0);
    }

    public void stop() {
        backLeft.setPower(0);
        backRight.setPower(0);
        frontLeft.setPower(0);
        frontRight.setPower(0);
        this.time.reset();
    }
}
