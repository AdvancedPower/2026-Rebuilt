package frc.robot.subsystems;

import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.motorcontrollers.MotorController;

import java.util.function.DoubleSupplier;

public class MotorSubsystem extends SubsystemBase {
    private final MotorController motor;


    public MotorSubsystem(MotorController motor) {
        this.motor = motor;
    }

    public Command run(DoubleSupplier outputSupplier) {
        return runEnd(() -> motor.setOutput(outputSupplier.getAsDouble()), motor::stop);
    }

    public Command run(double speed) {
        return startEnd(() -> motor.setOutput(speed), motor::stop);
    }

    public Command go_to_position(double position) {
        return runOnce(() -> motor.setPosition(position));
    }
}
