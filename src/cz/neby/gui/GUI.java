package cz.neby.gui;

import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import cz.neby.GPIO.Signals;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import cz.neby.listeners.GpioGuiListener;

/**
 * Class contains all GUI components for StopWatch
 *
 * @author Neby
 */
public class GUI extends JFrame {

    private final Logger LOGGER;
    private JLabel left, right, leftBulb, rightBulb;
    private ImageIcon bulbOn, bulbOff;
    private boolean startStopWatch, stopStopWatch;
    private StopWatch stopWatch;
    private ExecutorService exec;
    private Signals signals;
    private GpioPinDigitalInput leftTarget, rightTarget;
    protected long leftTime, rightTime;
    protected boolean leftActive, rightActive;

    /**
     * Constructor sets up all GUI components and action listeners for targets
     */
    public GUI() {
        setTitle("Stopky");
        LOGGER = LoggerFactory.getLogger(GUI.class.getName());
        exec = Executors.newFixedThreadPool(1);
        stopWatch = new StopWatch();
        setLayout(new GridLayout(1, 2));
        setMinimumSize(new Dimension(800, 400));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        GpioGuiListener leftListener = new GpioGuiListener() {
            @Override
            public void stateChange(boolean isHigh) {
                if (isHigh) {
                    leftActive = true;
                    leftBulb.setIcon(bulbOn);
                } else {
                    leftActive = false;
                    leftBulb.setIcon(bulbOff);
                }

                if (leftActive && rightActive) {
                    stopStopWatch = true;
                }
            }
        };

        GpioGuiListener rightListener = new GpioGuiListener() {
            @Override
            public void stateChange(boolean isHigh) {
                if (isHigh) {
                    rightActive = false;
                    rightBulb.setIcon(bulbOn);
                } else {
                    rightActive = false;
                    rightBulb.setIcon(bulbOff);
                }
                if (leftActive && rightActive) {
                    stopStopWatch = true;
                }
            }
        };

        GpioGuiListener gunListener = new GpioGuiListener() {
            @Override
            public void stateChange(boolean isHigh) {
                if (isHigh) {
                    startStopWatch();
                }
            }
        };

        signals = new Signals();
        leftTarget = signals.registerPin(RaspiPin.GPIO_00, leftListener);
        rightTarget = signals.registerPin(RaspiPin.GPIO_02, rightListener);

        bulbOn = new ImageIcon("image/bulbon.png");
        bulbOff = new ImageIcon("image/bulboff.png");

        JPanel timePanel = new JPanel(new GridBagLayout());
        JPanel buttonPanel = new JPanel(new GridBagLayout());

        add(timePanel);

        add(buttonPanel);

        GridBagConstraints cons = new GridBagConstraints();
        cons.fill = GridBagConstraints.BOTH;
        cons.weightx = 1.0;

        cons.gridx = 0;
        cons.gridy = 0;
        JLabel label = new JLabel("Levý:");

        label.setFont(new Font("Sans Serif", Font.BOLD, 30));
        timePanel.add(label, cons);

        cons.gridx = 1;
        left = new JLabel("00:000");

        left.setFont(new Font("Sans Serif", Font.BOLD, 30));
        timePanel.add(left, cons);

        cons.gridx = 0;
        cons.gridy = 1;
        label = new JLabel("Pravý:");

        label.setFont(new Font("Sans Serif", Font.BOLD, 30));
        timePanel.add(label, cons);

        cons.gridx = 1;
        right = new JLabel("00:000");

        right.setFont(new Font("Sans Serif", Font.BOLD, 30));
        timePanel.add(right, cons);

        cons.gridx = 0;
        cons.gridy = 3;
        label = new JLabel("Levý:");

        label.setFont(new Font("Sans Serif", Font.BOLD, 30));
        timePanel.add(label, cons);

        cons.gridy = 4;
        leftBulb = new JLabel();
        leftBulb.setText("");
        LOGGER.debug(leftTarget.getState().toString());
        if (leftTarget.getState() == PinState.HIGH) {
            leftBulb.setIcon(bulbOn);
        } else {
            leftBulb.setIcon(bulbOff);
        }

        timePanel.add(leftBulb, cons);

        cons.gridx = 1;
        cons.gridy = 3;
        label = new JLabel("Pravý:");

        label.setFont(new Font("Sans Serif", Font.BOLD, 30));
        timePanel.add(label, cons);

        cons.ipady = 0;
        cons.gridy = 4;
        cons.ipady = 0;
        cons.gridx = 1;
        rightBulb = new JLabel();
        rightBulb.setText("");

        if (rightTarget.getState() == PinState.HIGH) {
            rightBulb.setIcon(bulbOn);
        } else {
            rightBulb.setIcon(bulbOff);
        }

        timePanel.add(rightBulb, cons);

        cons.gridx = 0;
        cons.gridy = 0;
        JButton b = new JButton(new ImageIcon("image/start.png"));
        b.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                startStopWatch();
            }
        });
        b.setFocusPainted(false);
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        buttonPanel.add(b, cons);

        cons.gridx = 1;
        b = new JButton(new ImageIcon("image/stop.png"));
        b.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                stopStopWatch = true;
            }
        });
        b.setFocusPainted(false);
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        buttonPanel.add(b, cons);

        cons.gridx = 0;
        cons.gridy = 1;
        b = new JButton(new ImageIcon("image/reset.png"));

        b.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e
            ) {
                if (leftActive || rightActive) {
                    JOptionPane.showMessageDialog(new JFrame(), "Terče jsou dole!!", "Chyba", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                stopStopWatch = true;
                left.setText("00:000");
                right.setText("00:000");
            }
        });
        b.setFocusPainted(false);
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        buttonPanel.add(b, cons);

//        cons.gridx = 1;
//        cons.gridy = 1;
//        b = new JButton(new ImageIcon("image/timer.png"));
//        b.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//
//            }
//        });
//        b.setFocusPainted(false);
//        b.setContentAreaFilled(false);
//        b.setBorderPainted(false);
//        buttonPanel.add(b, cons);
        pack();
        setVisible(true);
    }

    /**
     * Method starts stopwatch
     */
    public void startStopWatch() {
        startStopWatch = true;
        exec.execute(stopWatch);
    }

    /**
     * Class for stopwatch and implemets Runnable interface.
     */
    public class StopWatch implements Runnable {

        /**
         * Method implements stopwatch count
         */
        @Override
        public void run() {
            long currentTime, timeDifference;
            long start = System.currentTimeMillis();
            timeDifference = 0;
            while (true) {
                try {
                    if (startStopWatch) {
                        start = System.currentTimeMillis();
                        startStopWatch = false;
                        stopStopWatch = false;
                    }
                    if (!stopStopWatch) {
                        currentTime = System.currentTimeMillis();
                        timeDifference = currentTime - start;
                        if (!leftActive) {
                            leftTime = timeDifference;
                            left.setText(getTime(timeDifference));
                        }

                        if (!rightActive) {
                            rightTime = timeDifference;
                            right.setText(getTime(timeDifference));
                        }
                    }
                    Thread.sleep(50);
                } catch (InterruptedException ex) {
                    LOGGER.error(ex.toString(), ex);
                }
            }
        }

        /**
         * Method converts time from milis to seconds and minutes
         *
         * @param time time in milis
         * @return mm:ss
         */
        private String getTime(long time) {
            String timeString = "";
            timeString += (int) (time / 1000) + ":";
            timeString += (int) (time % 1000);
            return timeString;
        }
    }
}
