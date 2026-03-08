package frc.robot.subsystems;

import edu.wpi.first.math.MathUtil;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.motorcontrollers.MotorController;

public class DualMotorSubsystem extends SubsystemBase {
    private final MotorController motor1;
    private final MotorController motor2;
    private double output1 = 0.2;
    private double output2 = 0.2;

    public DualMotorSubsystem(MotorController motor1, MotorController motor2) {
        this.motor1 = motor1;
        this.motor2 = motor2;
    }

    public Command run() {
        return Commands.runEnd(() -> setMotors(output1, output2), this::stopMotors);
    }

    public double getOutput1() {
        return output1;
    }

    public double getOutput2() {
        return output2;
    }

    public void setOutput1(double speed) {
        output1 = clampOutput(speed);
        printOuputs();
    }

    public void setOutput2(double speed) {
        output2 = clampOutput(speed);
        printOuputs();
    }

    private void setMotors(double motor1Output, double motor2Output) {
        motor1.setOutput(motor1Output);
        motor2.setOutput(motor2Output);
    }

    private void stopMotors() {
        motor1.stop();
        motor2.stop();
    }

    private static double clampOutput(double speed) {
        return MathUtil.clamp(speed, -1, 1);
    }

    private void printOuputs() {
        System.out.printf("front output: %.3f%n", output1);
        System.out.printf("back output: %.3f%n%n", output2);
    }
}