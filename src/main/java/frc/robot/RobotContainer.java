// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import com.ctre.phoenix6.hardware.TalonFX;
import com.ctre.phoenix6.signals.InvertedValue;
import com.ctre.phoenix6.swerve.SwerveModule.DriveRequestType;
import com.ctre.phoenix6.swerve.SwerveRequest;
import com.pathplanner.lib.auto.AutoBuilder;
import com.pathplanner.lib.auto.NamedCommands;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
import edu.wpi.first.wpilibj2.command.button.CommandXboxController;
import edu.wpi.first.wpilibj2.command.button.RobotModeTriggers;
import frc.robot.generated.TunerConstants;
import frc.robot.motorcontrollers.TalonFXMotorController;
import frc.robot.subsystems.*;

import static edu.wpi.first.units.Units.*;

public class RobotContainer {
    private final double MaxSpeed = TunerConstants.kSpeedAt12Volts.in(MetersPerSecond); // kSpeedAt12Volts desired top speed
    private final double MaxAngularRate = RotationsPerSecond.of(0.75).in(RadiansPerSecond); // 3/4 of a rotation per second max angular velocity

    /* Setting up bindings for necessary control of the swerve drive platform */
    private final SwerveRequest.FieldCentric controllerDrive = new SwerveRequest.FieldCentric()
        .withDeadband(MaxSpeed * 0.1).withRotationalDeadband(MaxAngularRate * 0.1) // Add a 10% deadband
        .withDriveRequestType(DriveRequestType.OpenLoopVoltage); // Use open-loop control for drive motors
    private final SwerveRequest.FieldCentric autoCenterDrive = new SwerveRequest.FieldCentric()
        .withDriveRequestType(DriveRequestType.OpenLoopVoltage); // Use open-loop control for drive motors
    private final SwerveRequest.SwerveDriveBrake brake = new SwerveRequest.SwerveDriveBrake();
    private final SwerveRequest.PointWheelsAt point = new SwerveRequest.PointWheelsAt();

    private final Telemetry logger = new Telemetry(MaxSpeed);

    private final CommandXboxController joystick = new CommandXboxController(0);

    public final CommandSwerveDrivetrain drivetrain = TunerConstants.createDrivetrain();

    private final Intake intake = new Intake(new MotorSubsystem(
        new TalonFXMotorController(new TalonFX(17))));

    private final Lifter lifter = new Lifter(new MotorSubsystem(
        new TalonFXMotorController(new TalonFX(15))
            .withMotionMagic(40, 1)
            .withFollower(new TalonFX(16), false)));

    private final Shooter shooter = new Shooter(
        new DualMotorSubsystem(
            // Front motors
            new TalonFXMotorController(new TalonFX(9), InvertedValue.CounterClockwise_Positive)
                .withFollower(new TalonFX(10), false),
            // Back motor
            new TalonFXMotorController(new TalonFX(12), InvertedValue.Clockwise_Positive)),
        new DualMotorSubsystem(
            // Front motor
            new TalonFXMotorController(new TalonFX(13), InvertedValue.Clockwise_Positive),
            // Back motor
            new TalonFXMotorController(new TalonFX(14), InvertedValue.Clockwise_Positive)),
        0.2,
        0.55,
        0.8,
        0.8
    );

    private final Leds leds = new Leds(0, 142);

    private final SendableChooser<Command> autoChooser;

    public RobotContainer() {
        configureBindings();
        new LedTimings(leds);
        NamedCommands.registerCommand("Shoot Fuel", shooter.shootTimed(5));
        NamedCommands.registerCommand("Lower Intake", lifter.lower());
        NamedCommands.registerCommand("Pickup Fuel", intake.intake());

        autoChooser = AutoBuilder.buildAutoChooser();

        // Another option that allows you to specify the default auto by its name
        // autoChooser = AutoBuilder.buildAutoChooser("My Default Auto");

        SmartDashboard.putData("Auto Chooser", autoChooser);
    }

    private void configureBindings() {
        // Note that X is defined as forward according to WPILib convention,
        // and Y is defined as to the left according to WPILib convention.
        drivetrain.setDefaultCommand(
            // Drivetrain will execute this command periodically
            drivetrain.applyRequest(() ->
                controllerDrive.withVelocityX(-joystick.getLeftY() * MaxSpeed) // Drive forward with negative Y (forward)
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
        joystick.back().onTrue(drivetrain.runOnce(drivetrain::seedFieldCentric));

        joystick.leftTrigger().and(joystick.rightBumper().negate()).whileTrue(intake.intake());
        joystick.leftTrigger().and(joystick.rightBumper()).whileTrue(intake.eject());
        joystick.rightTrigger().and(joystick.leftBumper().negate()).whileTrue(shooter.shoot());
        joystick.rightTrigger().and(joystick.leftBumper()).whileTrue(shooter.reverseFeeder());
        joystick.b().whileTrue(shooter.shoot(0.6, 0.6));
        joystick.x().whileTrue(getCenterOnLimelightTargetCommand(0));
        joystick.y().onTrue(lifter.lift());
        joystick.a().onTrue(lifter.lower());
        joystick.povUp().and(joystick.rightBumper().negate()).onTrue(Commands.runOnce(() -> shooter.setOutput1(shooter.getOutput1() + 0.05)));
        joystick.povDown().and(joystick.rightBumper().negate()).onTrue(Commands.runOnce(() -> shooter.setOutput1(shooter.getOutput1() - 0.05)));
        joystick.povRight().and(joystick.rightBumper().negate()).onTrue(Commands.runOnce(() -> shooter.setOutput2(shooter.getOutput2() + 0.05)));
        joystick.povLeft().and(joystick.rightBumper().negate()).onTrue(Commands.runOnce(() -> shooter.setOutput2(shooter.getOutput2() - 0.05)));
        joystick.povUp().and(joystick.rightBumper()).onTrue(Commands.runOnce(() -> shooter.setOutput1(shooter.getOutput1() + 0.01)));
        joystick.povDown().and(joystick.rightBumper()).onTrue(Commands.runOnce(() -> shooter.setOutput1(shooter.getOutput1() - 0.01)));
        joystick.povRight().and(joystick.rightBumper()).onTrue(Commands.runOnce(() -> shooter.setOutput2(shooter.getOutput2() + 0.01)));
        joystick.povLeft().and(joystick.rightBumper()).onTrue(Commands.runOnce(() -> shooter.setOutput2(shooter.getOutput2() - 0.01)));

//        joystick.leftBumper().whileTrue(getCenterOnLimelightTargetCommand(0));
//        joystick.rightBumper().whileTrue(getCenterOnLimelightTargetCommand(1));

        drivetrain.registerTelemetry(logger::telemeterize);
    }

    public Command getAutonomousCommand() {
        return autoChooser.getSelected();
    }

    private Command getCenterOnLimelightTargetCommand(int pipelineIndex) {
        return Commands
            .runOnce(() -> LimelightHelpers.setPipelineIndex("limelight", pipelineIndex))
            .andThen(drivetrain.applyRequest(() -> autoCenterDrive
                .withVelocityX(LimelightHelpers.getTargetPose_CameraSpace("limelight")[2] - 1.5) // Drive forward with negative Y (forward)
                .withVelocityY(-joystick.getLeftX() * MaxSpeed) // Drive left with negative X (left)
                .withRotationalRate(LimelightHelpers.getTX("limelight") * -0.1) // Drive counterclockwise with negative X (left)
            ));
    }

    private double getDynamicShooterOutput() {
        var ta = LimelightHelpers.getTA("limelight-hub");
        return ta == 0 ? 0 : 1 - ta * .01;
    }
}
