package frc.robot.subsystems;

import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.motorcontrollers.MotorController;

public class DualMotorSubsystem extends SubsystemBase {
    private final MotorController motor1;
    private final MotorController motor2;

    public DualMotorSubsystem(MotorController motor1, MotorController motor2) {
        this.motor1 = motor1;
        this.motor2 = motor2;
    }

    public Command run(double output1, double output2) {
        return runEnd(() -> setMotors(output1, output2), this::stopMotors);
    }

    private void setMotors(double motor1Output, double motor2Output) {
        motor1.setOutput(motor1Output);
        motor2.setOutput(motor2Output);
    }

    private void stopMotors() {
        motor1.stop();
        motor2.stop();
    }
}