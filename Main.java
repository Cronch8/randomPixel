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
    private static int dt = 1000000/200; 
    private static BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    private static Entity entity;
    private static JFrame frame = new JFrame("my cool window!");

    public static void main(String[] args) {
        //argumetn parsing
        if (args.length == 1) {
            try {
                dt = 1000000/Integer.valueOf(args[0]);
            } catch (Exception e) {
                System.out.println("argument is not an integer, defaulting to " + 1000000/dt + " updates per sec");
            }
        } else {
            System.out.println("update rate argument not provided, defaulting to " + 1000000/dt + " updates per sec");
        }

        //make black image
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                image.setRGB(x, y, convertARGB(0));
            }
        }
        entity = new Entity(width/2, height/2);

        //image update loop
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleAtFixedRate(Program::run, 0, dt, TimeUnit.MICROSECONDS);

        //make the window
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(new JTextField("first window!"));// equivalent to: `frame.add(new JTextField("first window!"));`
        frame.add(new JLabel(new ImageIcon(image)));
        frame.pack();
        frame.setVisible(true);
    }

    //update loop
    public static void run() {
        image.setRGB(entity.x, entity.y, convertARGB(255,170,170,170));
        int rnd = (int) Math.floor(Math.random()*4);
        switch (rnd) {
            case 0:
                entity.x += 1;
                if (entity.x > width) entity.x = 1;
                break;
            case 1:
                entity.x -= 1;
                if (entity.x < 0) entity.x = width-1;
                break;
            case 2:
                entity.y += 1;
                if (entity.y > height) entity.y = 1;
                break;
            case 3:
                entity.y -= 1;
                if (entity.y < 0) entity.y = height-1;
                break;
            default:
                break;
        }
        image.setRGB(entity.x, entity.y, convertARGB(150));
        frame.repaint();
    }


    //utility funcs
    private static int convertARGB(int a, int r, int g, int b) {
        return (255 << 24) | (255 << 16) | (255 << 8) | 255;
    }
    private static int convertARGB(int lightness) {
        return (255 << 24) | (lightness << 16) | (lightness << 8) | lightness;
    }
}
