import javax.swing.*;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.Random;
import marvin.video.MarvinJavaCVAdapter;
import marvin.video.MarvinVideoInterface;

public class Main {
    public static void main(String[] args) {
        new BackgroundSnap().start();
    }
}

class BackgroundSnap {
    private final Random random = new Random();
    private final int minDelay = 5;
    private final int maxDelay = 10;
    private final int displayTime = 3;
    private final MarvinVideoInterface videoAdapter;

    public BackgroundSnap() {
        videoAdapter = new MarvinJavaCVAdapter();
        videoAdapter.connect(0);
    }

    public void start() {
        while (true) {
            try {
                int delay = minDelay + random.nextInt(maxDelay - minDelay + 1);
                Thread.sleep(delay * 1000L);
                BufferedImage screenshot = takeScreenshot();
                BufferedImage photo = takeWebcamPhoto();
                showImages(screenshot, photo);
            } catch (Exception e) {
                // Silent fail
            }
        }
    }

    private BufferedImage takeScreenshot() throws Exception {
        Robot robot = new Robot();
        Rectangle screenRect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
        return robot.createScreenCapture(screenRect);
    }

    private BufferedImage takeWebcamPhoto() {
        try {
            return videoAdapter.getFrame();
        } catch (Exception e) {
            // Silent fail
        }
        return null;
    }

    private void showImages(BufferedImage img1, BufferedImage img2) {
        JFrame frame = new JFrame("Snap!");
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.setAlwaysOnTop(true);
        frame.setUndecorated(true);
        frame.setLayout(new GridLayout(1, 2));
        frame.add(new JLabel(new ImageIcon(img1)));
        if (img2 != null) {
            frame.add(new JLabel(new ImageIcon(img2)));
        } else {
            frame.add(new JLabel("No webcam image"));
        }
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        new Timer(displayTime * 1000, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                frame.setVisible(false);
                frame.dispose();
            }
        }).start();
    }
}
