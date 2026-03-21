package frc.robot.subsystems;

import edu.wpi.first.math.MathUtil;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;

public class Shooter {
    private final DualMotorSubsystem shootMotors;
    private final DualMotorSubsystem feederMotors;
    private double shooterOutput1;
    private double shooterOutput2;
    private final double feederOutput1;
    private final double feederOutput2;

    public Shooter(
        DualMotorSubsystem shootMotors,
        DualMotorSubsystem feederMotors,
        double shooterOutput1,
        double shooterOutput2,
        double feederOutput1,
        double feederOutput2) {
        this.shootMotors = shootMotors;
        this.feederMotors = feederMotors;
        this.shooterOutput1 = shooterOutput1;
        this.shooterOutput2 = shooterOutput2;
        this.feederOutput1 = feederOutput1;
        this.feederOutput2 = feederOutput2;
    }

    public Command shootTimed(double seconds) {
        return shoot().withTimeout(seconds);
    }

    public Command shoot() {
        return shootMotors.defer(() -> shoot(shooterOutput1, shooterOutput2));
    }

    public Command shoot(double shooterOutput1, double shooterOutput2) {
        return shootMotors.run(shooterOutput1, shooterOutput2)
            .alongWith(Commands.waitSeconds(1)
                .andThen(feederMotors.run(feederOutput1, feederOutput2)));
    }

    public Command reverseFeeder() {
        return feederMotors.run(-0.5, -0.5);
    }

    public double getOutput1() {
        return shooterOutput1;
    }

    public double getOutput2() {
        return shooterOutput2;
    }

    public void setOutput1(double speed) {
        shooterOutput1 = clampOutput(speed);
        printOutputs();
    }

    public void setOutput2(double speed) {
        shooterOutput2 = clampOutput(speed);
        printOutputs();
    }

    private static double clampOutput(double speed) {
        return MathUtil.clamp(speed, -1, 1);
    }

    private void printOutputs() {
        System.out.printf("front output: %.3f%n", shooterOutput1);
        System.out.printf("back output: %.3f%n%n", shooterOutput2);
    }
}
