// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import com.ctre.phoenix6.hardware.TalonFX;
import com.ctre.phoenix6.signals.InvertedValue;
import com.ctre.phoenix6.swerve.SwerveModule.DriveRequestType;
import com.ctre.phoenix6.swerve.SwerveRequest;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
import edu.wpi.first.wpilibj2.command.button.CommandXboxController;
import edu.wpi.first.wpilibj2.command.button.RobotModeTriggers;
import edu.wpi.first.wpilibj2.command.sysid.SysIdRoutine.Direction;
import frc.robot.generated.TunerConstants;
import frc.robot.motorcontrollers.TalonFXMotorController;
import frc.robot.subsystems.CommandSwerveDrivetrain;
import frc.robot.subsystems.DualMotorSubsystem;
import frc.robot.subsystems.MotorSubsystem;

import static edu.wpi.first.units.Units.*;

public class RobotContainer {
    private double MaxSpeed = 1.0 * TunerConstants.kSpeedAt12Volts.in(MetersPerSecond); // kSpeedAt12Volts desired top speed
    private double MaxAngularRate = RotationsPerSecond.of(0.75).in(RadiansPerSecond); // 3/4 of a rotation per second max angular velocity

    /* Setting up bindings for necessary control of the swerve drive platform */
    private final SwerveRequest.FieldCentric drive = new SwerveRequest.FieldCentric()
        .withDeadband(MaxSpeed * 0.1).withRotationalDeadband(MaxAngularRate * 0.1) // Add a 10% deadband
        .withDriveRequestType(DriveRequestType.OpenLoopVoltage); // Use open-loop control for drive motors
    private final SwerveRequest.SwerveDriveBrake brake = new SwerveRequest.SwerveDriveBrake();
    private final SwerveRequest.PointWheelsAt point = new SwerveRequest.PointWheelsAt();

    private final Telemetry logger = new Telemetry(MaxSpeed);

    private final CommandXboxController joystick = new CommandXboxController(0);

    public final CommandSwerveDrivetrain drivetrain = TunerConstants.createDrivetrain();

    private final MotorSubsystem pickup = new MotorSubsystem(
        new TalonFXMotorController(new TalonFX(17)));

    private final MotorSubsystem lifter = new MotorSubsystem(
        new TalonFXMotorController(new TalonFX(15))
            .withMotionMagic(40, 1)
            .withFollower(new TalonFX(16), false));

    private final DualMotorSubsystem shooter = new DualMotorSubsystem(
        // Front motors
        new TalonFXMotorController(new TalonFX(10), InvertedValue.CounterClockwise_Positive)
            .withFollower(new TalonFX(11), false),
        // Back motor
        new TalonFXMotorController(new TalonFX(12), InvertedValue.Clockwise_Positive));

    private final DualMotorSubsystem feeder = new DualMotorSubsystem(
        // Front motor
        new TalonFXMotorController(new TalonFX(13), InvertedValue.Clockwise_Positive),
        // Back motor
        new TalonFXMotorController(new TalonFX(14), InvertedValue.Clockwise_Positive));

    //Leds leds = new Leds(9, 47);

    public RobotContainer() {
        configureBindings();
        //new LedTimings(leds);
    }

    private void configureBindings() {
        // Note that X is defined as forward according to WPILib convention,
        // and Y is defined as to the left according to WPILib convention.
        drivetrain.setDefaultCommand(
            // Drivetrain will execute this command periodically
            drivetrain.applyRequest(() ->
                drive.withVelocityX(-joystick.getLeftY() * MaxSpeed) // Drive forward with negative Y (forward)
                    .withVelocityY(-joystick.getLeftX() * MaxSpeed) // Drive left with negative X (left)
                    .withRotationalRate(-joystick.getRightX() * MaxAngularRate) // Drive counterclockwise with negative X (left)
            )
        );

        // Idle while the robot is disabled. This ensures the configured
        // neutral mode is applied to the drive motors while disabled.
        final var idle = new SwerveRequest.Idle();
        RobotModeTriggers.disabled().whileTrue(
            drivetrain.applyRequest(() -> idle).ignoringDisable(true)
        );

//        joystick.a().whileTrue(drivetrain.applyRequest(() -> brake));
//        joystick.b().whileTrue(drivetrain.applyRequest(() ->
//            point.withModuleDirection(new Rotation2d(-joystick.getLeftY(), -joystick.getLeftX()))
//        ));

        // Run SysId routines when holding back/start and X/Y.
        // Note that each routine should be run exactly once in a single log.
        joystick.back().and(joystick.y()).whileTrue(drivetrain.sysIdDynamic(Direction.kForward));
        joystick.back().and(joystick.x()).whileTrue(drivetrain.sysIdDynamic(Direction.kReverse));
        joystick.start().and(joystick.y()).whileTrue(drivetrain.sysIdQuasistatic(Direction.kForward));
        joystick.start().and(joystick.x()).whileTrue(drivetrain.sysIdQuasistatic(Direction.kReverse));

        // Reset the field-centric heading on left bumper press.
        joystick.back().onTrue(drivetrain.runOnce(drivetrain::seedFieldCentric));

        joystick.x().whileTrue(pickup.run(() -> 0.7));
        joystick.y().whileTrue(getShootCommand());
        joystick.b().onTrue(lifter.go_to_position(0));
        joystick.a().onTrue(lifter.go_to_position(-35));
        joystick.povUp().and(joystick.rightBumper().negate()).onTrue(Commands.runOnce(() -> shooter.setOutput1(shooter.getOutput1() + 0.05)));
        joystick.povDown().and(joystick.rightBumper().negate()).onTrue(Commands.runOnce(() -> shooter.setOutput1(shooter.getOutput1() - 0.05)));
        joystick.povRight().and(joystick.rightBumper().negate()).onTrue(Commands.runOnce(() -> shooter.setOutput2(shooter.getOutput2() + 0.05)));
        joystick.povLeft().and(joystick.rightBumper().negate()).onTrue(Commands.runOnce(() -> shooter.setOutput2(shooter.getOutput2() - 0.05)));
        joystick.povUp().and(joystick.rightBumper()).onTrue(Commands.runOnce(() -> shooter.setOutput1(shooter.getOutput1() + 0.002)));
        joystick.povDown().and(joystick.rightBumper()).onTrue(Commands.runOnce(() -> shooter.setOutput1(shooter.getOutput1() - 0.002)));
        joystick.povRight().and(joystick.rightBumper()).onTrue(Commands.runOnce(() -> shooter.setOutput2(shooter.getOutput2() + 0.01)));
        joystick.povLeft().and(joystick.rightBumper()).onTrue(Commands.runOnce(() -> shooter.setOutput2(shooter.getOutput2() - 0.01)));

        joystick.leftTrigger().whileTrue(getCenterOnLimelightTargetCommand(0));
        joystick.rightTrigger().whileTrue(getCenterOnLimelightTargetCommand(1));

        drivetrain.registerTelemetry(logger::telemeterize);
    }

    private Command getShootCommand() {
        return shooter.run().alongWith(Commands.waitSeconds(1).andThen(feeder.run())
        );
    }

    public Command getAutonomousCommand() {
        // Simple drive forward auton
        final var idle = new SwerveRequest.Idle();
        return Commands.sequence(
            // Reset our field centric heading to match the robot
            // facing away from our alliance station wall (0 deg).
            drivetrain.runOnce(() -> drivetrain.seedFieldCentric(Rotation2d.kZero)),
            // Then slowly drive forward (away from us) for 5 seconds.
            drivetrain.applyRequest(() ->
                    drive.withVelocityX(0.5)
                        .withVelocityY(0)
                        .withRotationalRate(0)
                )
                .withTimeout(5.0),
            // Finally idle for the rest of auton
            drivetrain.applyRequest(() -> idle)
        );
    }

    private Command getCenterOnLimelightTargetCommand(int pipelineIndex) {
        return Commands
            .runOnce(() -> LimelightHelpers.setPipelineIndex("limelight", pipelineIndex))
            .andThen(drivetrain.applyRequest(() ->
                drive.withVelocityX(-joystick.getLeftY() * MaxSpeed) // Drive forward with negative Y (forward)
                    .withVelocityY(-joystick.getLeftX() * MaxSpeed) // Drive left with negative X (left)
                    .withRotationalRate(LimelightHelpers.getTX("limelight") * -0.1) // Drive counterclockwise with negative X (left)
                    .withRotationalDeadband(0)
            ));
    }

    private double getDynamicShooterOutput() {
        var ta = LimelightHelpers.getTA("limelight-hub");
        return ta == 0 ? 0 : 1 - ta * .01;
    }
}
