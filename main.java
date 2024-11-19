import java.awt.image.BufferedImage;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;

class Program {
    private static final int height = 300; 
    private static final int width = 500; 
    private static BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    private static Entity entity;

    public static void main(String[] args) {
        int alpha = 255; 
        int red = 0; 
        int green = 0; 
        int blue = 0; 
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = (alpha << 24) | (red << 16) | (green << 8) | blue;
                image.setRGB(x, y, rgb);  // Set pixel color
            }
        }
        entity = new Entity(width/2, height/2);

        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleAtFixedRate(Program::run, 1, 500, TimeUnit.MILLISECONDS);

        JFrame frame = new JFrame("my cool window!");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(new JTextField("first window!"));// equivalent to: `frame.add(new JTextField("first window!"));`
        frame.add(new JLabel(new ImageIcon(image)));
        frame.pack();
        frame.setVisible(true);
    }

    public static void run() {
        image.setRGB(x, y, (255 << 24) | (255 << 16) | (255 << 8) | 255);
    }
}
