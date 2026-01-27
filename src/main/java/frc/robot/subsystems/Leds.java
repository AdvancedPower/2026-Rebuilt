package frc.robot.subsystems;

import edu.wpi.first.wpilibj.AddressableLED;
import edu.wpi.first.wpilibj.AddressableLEDBuffer;
import edu.wpi.first.wpilibj.LEDPattern;
import edu.wpi.first.wpilibj.util.Color;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.SubsystemBase;

import static edu.wpi.first.units.Units.*;

public class Leds extends SubsystemBase {
    private final AddressableLED m_led;
    private final AddressableLEDBuffer m_buffer;

    public Leds(int port, int length) {
        m_led = new AddressableLED(port);
        m_buffer = new AddressableLEDBuffer(length);
        m_led.setLength(length);
        m_led.start();
        setDefaultCommand(runPattern(LEDPattern.solid(Color.kBlack)));
    }

    @Override
    public void periodic() {
        m_led.setData(m_buffer);
    }

    public Command runPattern(LEDPattern pattern) {
        return run(() -> pattern.applyTo(m_buffer));
    }

    public static LEDPattern scroll(Color color, double speed) {
        return LEDPattern
            .gradient(LEDPattern.GradientType.kDiscontinuous, Color.kBlack, color)
            .scrollAtRelativeSpeed(Percent.per(Second).of(speed));
    }

    public static LEDPattern blink(Color color, double speed) {
        return LEDPattern.solid(color).blink(Seconds.of(speed));
    }
}
