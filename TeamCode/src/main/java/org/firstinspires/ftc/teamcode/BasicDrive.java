package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.CRServo;

@TeleOp(name = "Basic Drive X", group = "Iterative Opmode")
public class BasicDrive extends OpMode {
    // drive train
    private DcMotor backLeft = null;
    private DcMotor backRight = null;
    private DcMotor frontLeft = null;
    private DcMotor frontRight = null;

    // intake servo
    private DcMotor baseArm = null;
    private Servo gripServo1 = null;
    private Servo gripServo2 = null;
    private DcMotor linearSlide = null;

    @Override
    public void init() {
        // ---- drive train ----
        backLeft = hardwareMap.get(DcMotor.class, "backLeft");
        backLeft.setDirection(DcMotor.Direction.FORWARD);
        backRight = hardwareMap.get(DcMotor.class, "backRight");
        backRight.setDirection(DcMotor.Direction.REVERSE);
        frontLeft = hardwareMap.get(DcMotor.class, "frontLeft");
        frontLeft.setDirection(DcMotor.Direction.FORWARD);
        frontRight = hardwareMap.get(DcMotor.class, "frontRight");
        frontRight.setDirection(DcMotor.Direction.REVERSE);
        linearSlide = hardwareMap.get(DcMotor.class, "linearSlide");
        linearSlide.setDirection(DcMotor.Direction.FORWARD);
        // ---- intake ----
        // base arm
        baseArm = hardwareMap.get(DcMotor.class, "baseArm");
        // left and right servo
        gripServo1 = hardwareMap.get(Servo.class, "gripServo1");
        gripServo2 = hardwareMap.get(Servo.class, "gripServo2");
        gripServo2.setDirection(Servo.Direction.REVERSE);
    }

    /*
     * Code to run REPEATEDLY after the driver hits PLAY but before they hit STOP
     */
    @Override
    public void loop() {
        if (gamepad1.right_trigger == 1) {
            baseArm.setPower(.5);
        } else {
            baseArm.setPower(.05);
        }

        if (gamepad1.b) {
            gripServo1.setPosition(0.25);
            gripServo2.setPosition(0.25);
        }
        if (gamepad1.a) {
            gripServo1.setPosition(0.75);
            gripServo2.setPosition(0.75);
        }

        if (gamepad1.left_trigger >= 0.05)
            linearSlide.setPower(-0.9);
        else if (gamepad1.right_trigger >= 0.05)
            linearSlide.setPower(0.9);
        else
            linearSlide.setPower(0.0);

        // Mecanum wheel drive calculations
        double dx = Math.abs(gamepad1.left_stick_x) < 0.05 ? 0 : -gamepad1.left_stick_x;
        double dy = Math.abs(gamepad1.right_stick_x) < 0.05 ? 0 : -gamepad1.right_stick_x;
        double dr = Math.abs(gamepad1.left_stick_y) < 0.05 ? 0 : gamepad1.left_stick_y;

        double r = Math.hypot(dx, -dy); // deadzones are incorporated into these values
        double robotAngle = Math.atan2(-dy, dx) - Math.PI / 4;
        double rightX = dr / 1.25;
        final double v1 = r * Math.cos(robotAngle) + rightX;
        final double v2 = r * Math.sin(robotAngle) - rightX;
        final double v3 = r * Math.sin(robotAngle) + rightX;
        final double v4 = r * Math.cos(robotAngle) - rightX;

        // Power set to each motor based on these calculations
        frontRight.setPower(v1 * .75);
        frontLeft.setPower(v4 * .75);
        backRight.setPower(v3 * .75);
        backLeft.setPower(v2 * .75);
    }

    @Override
    public void stop() {
    }
}
