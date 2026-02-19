package frc.robot.subsystems;

import edu.wpi.first.math.MathUtil;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.motorcontrollers.MotorController;

public class Shooter extends SubsystemBase {
    private final MotorController frontMotor;
    private final MotorController backMotor;
    private double frontSpeed = 0.2;
    private double backSpeed = 0.2;

    public Shooter(MotorController frontMotor, MotorController backMotor) {
        this.frontMotor = frontMotor;
        this.backMotor = backMotor;
    }

    public Command run() {
        return Commands.runEnd(() -> setMotors(frontSpeed, backSpeed), () -> setMotors(0, 0));
    }

    public double getFrontSpeed() {
        return frontSpeed;
    }

    public double getBackSpeed() {
        return backSpeed;
    }

    public void setFrontSpeed(double speed) {
        frontSpeed = clampSpeed(speed);
        printSpeeds();
    }

    public void setBackSpeed(double speed) {
        backSpeed = clampSpeed(speed);
        printSpeeds();
    }

    private void setMotors(double frontSpeed, double backSpeed) {
        frontMotor.set(frontSpeed);
        backMotor.set(backSpeed);
    }

    private static double clampSpeed(double speed) {
        return MathUtil.clamp(speed, 0, 1);
    }

    private void printSpeeds(){
        System.out.printf("front speed: %.2f%n", frontSpeed);
        System.out.printf("back speed: %.2f%n%n", backSpeed);
    }
}