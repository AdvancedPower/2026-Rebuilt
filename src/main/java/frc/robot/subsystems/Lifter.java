package frc.robot.subsystems;

import edu.wpi.first.wpilibj2.command.Command;

public class Lifter {
    private final MotorSubsystem motor;

    public Lifter(MotorSubsystem motor) {
        this.motor = motor;
    }

    public Command lift() {
        return motor.go_to_position(0);
    }

    public Command lower() {
        return motor.go_to_position(-35);
    }
}
