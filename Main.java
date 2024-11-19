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
    private static final int dt = 1000/600; 
    private static BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    private static Entity entity;
    private static JFrame frame = new JFrame("my cool window!");

    public static void main(String[] args) {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                image.setRGB(x, y, convertARGB(0));  // Set pixel color
            }
        }
        entity = new Entity(width/2, height/2);

        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleAtFixedRate(Program::run, 0, dt, TimeUnit.MILLISECONDS);

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(new JTextField("first window!"));// equivalent to: `frame.add(new JTextField("first window!"));`
        frame.add(new JLabel(new ImageIcon(image)));
        frame.pack();
        frame.setVisible(true);
    }

    public static void run() {
        image.setRGB(entity.x, entity.y, convertARGB(255,170,170,170));
        int rnd = (int) Math.floor(Math.random()*4);
        switch (rnd) {
            case 0:
                System.out.println("entity at - x: " + entity.x + " y: " + entity.y);
                entity.x += 1;
                if (entity.x > width) entity.x = 0;
                break;
            case 1:
                System.out.println("entity at - x: " + entity.x + " y: " + entity.y);
                entity.x -= 1;
                if (entity.x < width) entity.x = width;
                break;
            case 2:
                System.out.println("entity at - x: " + entity.x + " y: " + entity.y);
                entity.y += 1;
                if (entity.y > width) entity.y = 0;
                break;
            case 3:
                System.out.println("entity at - x: " + entity.x + " y: " + entity.y);
                entity.y -= 1;
                if (entity.y < width) entity.y = height;
                break;
            default:
                break;
        }
        image.setRGB(entity.x, entity.y, convertARGB(150));
        frame.repaint();
    }


    private static int convertARGB(int a, int r, int g, int b) {
        return (255 << 24) | (255 << 16) | (255 << 8) | 255;
    }
    private static int convertARGB(int lightness) {
        return (255 << 24) | (lightness << 16) | (lightness << 8) | lightness;
    }
}
