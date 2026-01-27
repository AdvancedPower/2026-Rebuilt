package frc.robot;

import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.util.Color;
import edu.wpi.first.wpilibj2.command.Commands;
import edu.wpi.first.wpilibj2.command.button.Trigger;
import frc.robot.subsystems.Leds;

public class LedTimings {
    private class PhaseData {
        public final Color phaseColor;
        public final Color warningColor;
        public final double warningTime;

        public PhaseData(Color phaseColor, Color warningColor, double warningTime) {
            this.phaseColor = phaseColor;
            this.warningColor = warningColor;
            this.warningTime = warningTime;

        }
    }

    private PhaseData phase1And3Data;
    private PhaseData phase2And4Data;

    public LedTimings(Leds leds) {
        final double phaseLength = 25;
        final double endGameStart = 30;
        final double phase4Start = endGameStart + phaseLength;
        final double phase3Start = phase4Start + phaseLength;
        final double phase2Start = phase3Start + phaseLength;
        final double phase1Start = phase2Start + phaseLength;
        final double transitionStart = phase1Start + 10;

        final double inactiveWarningTime = 3;
        final double activeWarningTime = 5;

        final Color activeColor = Color.kGreen;
        final Color inactiveColor = Color.kRed;
        final Color inactiveWarningColor = Color.kOrange;
        final Color activeWarningColor = Color.kBlue;

        final PhaseData activeData = new PhaseData(activeColor, activeWarningColor, activeWarningTime);
        final PhaseData inactiveData = new PhaseData(inactiveColor, inactiveWarningColor, inactiveWarningTime);

        new Trigger(DriverStation::isAutonomousEnabled).onTrue(leds.runPattern(Leds.scroll(activeColor, 50)));

        new Trigger(() -> DriverStation.isTeleopEnabled() && DriverStation.getMatchTime() <= transitionStart)
            .onTrue(leds.runPattern(Leds.scroll(activeColor, 50)));

        new Trigger(() -> DriverStation.getGameSpecificMessage().isEmpty()).onFalse(Commands.runOnce(() -> {
            var inactiveFirstChar = DriverStation.getGameSpecificMessage(); // 'R' (red) or 'B' (blue)
            var myAlliance = DriverStation.getAlliance();
            var inactiveFirst = myAlliance.get().name().substring(0, 1).equals(inactiveFirstChar);
            phase1And3Data = inactiveFirst ? inactiveData : activeData;
            phase2And4Data = inactiveFirst ? activeData : inactiveData;
        }));

        new Trigger(() -> phase1And3Data != null && DriverStation.getMatchTime() < phase1Start + phase1And3Data.warningTime && phase1And3Data.phaseColor == inactiveColor)
            .onTrue(leds.defer(() -> leds.runPattern(Leds.blink(phase1And3Data.warningColor, 0.4))));

        new Trigger(() -> phase1And3Data != null && DriverStation.getMatchTime() < phase1Start)
            .onTrue(leds.defer(() -> leds.runPattern(Leds.scroll(phase1And3Data.phaseColor, 50))));

        new Trigger(() -> phase2And4Data != null && DriverStation.getMatchTime() < phase2Start + phase2And4Data.warningTime)
            .onTrue(leds.defer(() -> leds.runPattern(Leds.blink(phase2And4Data.warningColor, 0.4))));

        new Trigger(() -> phase2And4Data != null && DriverStation.getMatchTime() < phase2Start)
            .onTrue(leds.defer(() -> leds.runPattern(Leds.scroll(phase2And4Data.phaseColor, 50))));

        new Trigger(() -> phase1And3Data != null && DriverStation.getMatchTime() < phase3Start + phase1And3Data.warningTime)
            .onTrue(leds.defer(() -> leds.runPattern(Leds.blink(phase1And3Data.warningColor, 0.4))));

        new Trigger(() -> phase1And3Data != null && DriverStation.getMatchTime() < phase3Start)
            .onTrue(leds.defer(() -> leds.runPattern(Leds.scroll(phase1And3Data.phaseColor, 50))));

        new Trigger(() -> phase2And4Data != null && DriverStation.getMatchTime() < phase4Start + phase2And4Data.warningTime)
            .onTrue(leds.defer(() -> leds.runPattern(Leds.blink(phase2And4Data.warningColor, 0.4))));

        new Trigger(() -> phase2And4Data != null && DriverStation.getMatchTime() < phase4Start)
            .onTrue(leds.defer(() -> leds.runPattern(Leds.scroll(phase2And4Data.phaseColor, 50))));

        new Trigger(() -> DriverStation.isTeleopEnabled() && DriverStation.getMatchTime() < endGameStart)
            .onTrue(leds.runPattern(Leds.scroll(activeColor, 50)));
    }
}
