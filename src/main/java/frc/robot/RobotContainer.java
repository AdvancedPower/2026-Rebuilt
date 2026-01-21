// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import com.ctre.phoenix6.hardware.TalonFX;
import com.ctre.phoenix6.swerve.SwerveModule.DriveRequestType;
import com.ctre.phoenix6.swerve.SwerveRequest;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.util.Color;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
import edu.wpi.first.wpilibj2.command.button.CommandXboxController;
import edu.wpi.first.wpilibj2.command.button.RobotModeTriggers;
import edu.wpi.first.wpilibj2.command.button.Trigger;
import edu.wpi.first.wpilibj2.command.sysid.SysIdRoutine.Direction;
import frc.robot.generated.TunerConstants;
import frc.robot.subsystems.CommandSwerveDrivetrain;
import frc.robot.subsystems.Leds;
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

    MotorController shooterMotor = new MockMotorController();
    MotorController pickupMotor = new TalonFXMotorController(new TalonFX(9));
    MotorSubsystem pickup = new MotorSubsystem(pickupMotor);
    MotorSubsystem shooter = new MotorSubsystem(shooterMotor);
    Leds leds = new Leds(9, 47);

    public RobotContainer() {
        configureBindings();
        configureLedTimings();
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

        joystick.a().whileTrue(drivetrain.applyRequest(() -> brake));
        joystick.b().whileTrue(drivetrain.applyRequest(() ->
            point.withModuleDirection(new Rotation2d(-joystick.getLeftY(), -joystick.getLeftX()))
        ));

        // Run SysId routines when holding back/start and X/Y.
        // Note that each routine should be run exactly once in a single log.
        joystick.back().and(joystick.y()).whileTrue(drivetrain.sysIdDynamic(Direction.kForward));
        joystick.back().and(joystick.x()).whileTrue(drivetrain.sysIdDynamic(Direction.kReverse));
        joystick.start().and(joystick.y()).whileTrue(drivetrain.sysIdQuasistatic(Direction.kForward));
        joystick.start().and(joystick.x()).whileTrue(drivetrain.sysIdQuasistatic(Direction.kReverse));

        // Reset the field-centric heading on left bumper press.
        joystick.leftBumper().onTrue(drivetrain.runOnce(drivetrain::seedFieldCentric));

        joystick.x().whileTrue(pickup.run(0.2));

        drivetrain.registerTelemetry(logger::telemeterize);
    }

    private void configureLedTimings() {
        final double endGameStart = 30;
        final double phase4Start = endGameStart + 25;
        final double phase3Start = phase4Start + 25;
        final double phase2Start = phase3Start + 25;
        final double phase1Start = phase2Start + 25;
        final double transitionStart = phase1Start + 10;

        final double inactiveWarningTime = 3;
        final double activeWarningTime = 5;

        final Color activeColor = Color.kGreen;
        final Color inactiveColor = Color.kRed;
        final Color inactiveWarningColor = Color.kYellow;
        final Color activeWarningColor = Color.kBlue;

        new Trigger(DriverStation::isAutonomousEnabled).onTrue(leds.runPattern(Leds.solidColor(activeColor)));
        new Trigger(() -> {
            var time = DriverStation.getMatchTime();
            return DriverStation.isTeleopEnabled() && time <= transitionStart;
        }).onTrue(leds.runPattern(Leds.solidColor(activeColor)));

        new Trigger(() -> DriverStation.getGameSpecificMessage().isEmpty()).onFalse(Commands.runOnce(() -> {
            var inactiveFirstChar = DriverStation.getGameSpecificMessage(); // 'R' (red) or 'B' (blue)
            var myAlliance = DriverStation.getAlliance();
            var inactiveFirst = myAlliance.get().name().substring(0, 1).equals(inactiveFirstChar);
            var phase1And3Color = inactiveFirst ? inactiveColor : activeColor;
            var phase2And4Color = inactiveFirst ? activeColor : inactiveColor;
            new Trigger(() -> {
                var time = DriverStation.getMatchTime();
                return DriverStation.isTeleopEnabled() && time <= phase1Start + inactiveWarningTime;
            }).onTrue(leds.runPattern(Leds.solidColor(inactiveWarningColor)));
            new Trigger(() -> {
                var time = DriverStation.getMatchTime();
                return DriverStation.isTeleopEnabled() && time <= phase1Start;
            }).onTrue(leds.runPattern(Leds.solidColor(phase1And3Color)));
            new Trigger(() -> {
                var time = DriverStation.getMatchTime();
                return DriverStation.isTeleopEnabled() && time <= phase2Start + activeWarningTime;
            }).onTrue(leds.runPattern(Leds.solidColor(activeWarningColor)));
            new Trigger(() -> {
                var time = DriverStation.getMatchTime();
                return DriverStation.isTeleopEnabled() && time <= phase2Start;
            }).onTrue(leds.runPattern(Leds.solidColor(phase2And4Color)));
            new Trigger(() -> {
                var time = DriverStation.getMatchTime();
                return DriverStation.isTeleopEnabled() && time <= phase3Start + inactiveWarningTime;
            }).onTrue(leds.runPattern(Leds.solidColor(inactiveWarningColor)));
            new Trigger(() -> {
                var time = DriverStation.getMatchTime();
                return DriverStation.isTeleopEnabled() && time <= phase3Start;
            }).onTrue(leds.runPattern(Leds.solidColor(phase1And3Color)));
            new Trigger(() -> {
                var time = DriverStation.getMatchTime();
                return DriverStation.isTeleopEnabled() && time <= phase4Start + activeWarningTime;
            }).onTrue(leds.runPattern(Leds.solidColor(activeWarningColor)));
            new Trigger(() -> {
                var time = DriverStation.getMatchTime();
                return DriverStation.isTeleopEnabled() && time <= phase4Start;
            }).onTrue(leds.runPattern(Leds.solidColor(phase2And4Color)));
            new Trigger(() -> {
                var time = DriverStation.getMatchTime();
                return DriverStation.isTeleopEnabled() && time <= endGameStart;
            }).onTrue(leds.runPattern(Leds.solidColor(activeColor)));
        }));
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
}
