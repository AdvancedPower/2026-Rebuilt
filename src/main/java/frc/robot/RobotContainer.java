// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
import edu.wpi.first.wpilibj2.command.button.CommandXboxController;
import frc.robot.subsystems.MotorSubsystem;

public class RobotContainer {
    CommandXboxController controller = new CommandXboxController(0);
    MotorController shooterMotor = new MockMotorController();
    MotorController pickupMotor = new MockMotorController();
    MotorSubsystem pickup = new MotorSubsystem(pickupMotor);
    MotorSubsystem shooter = new MotorSubsystem(shooterMotor);

    public RobotContainer() {
        configureBindings();
    }

    private void configureBindings() {
        controller.a().whileTrue(pickup.run(0.2));
        controller.b().whileTrue(shooter.run(0.4));
    }

    public Command getAutonomousCommand() {
        return Commands.print("No autonomous command configured");
    }
}
