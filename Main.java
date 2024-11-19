import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;

class Program {
    private static final int height = 1000; 
    private static final int width = 1500; 
    private static int dt = 1000000/200; //default update rate
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

        //make initial black image
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                image.setRGB(x, y, convertARGB(0));
            }
        }
        entity = new Entity(width/2, height/2);

        //update loops
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleAtFixedRate(Program::entityUpdate, 0, dt, TimeUnit.MICROSECONDS);
        executor.scheduleAtFixedRate(Program::imageProcessing, 0, 1000/30, TimeUnit.MILLISECONDS);

        //make the window
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(new JTextField("first window!"));// equivalent to: `frame.add(new JTextField("first window!"));`
        frame.add(new JLabel(new ImageIcon(image)));
        frame.pack();
        frame.setVisible(true);
    }

    //entity update loop
    private static void entityUpdate() {
        image.setRGB(entity.x, entity.y, convertARGB(255));
        int rnd = (int) Math.floor(Math.random()*4);
        switch (rnd) {
            case 0:
                entity.x += 1;
                if (entity.x >= width) entity.x = 0;
                break;
            case 1:
                entity.x -= 1;
                if (entity.x <= 0) entity.x = width-1;
                break;
            case 2:
                entity.y += 1;
                if (entity.y >= height) entity.y = 0;
                break;
            case 3:
                entity.y -= 1;
                if (entity.y <= 0) entity.y = height-1;
                break;
            default:
                break;
        }
        image.setRGB(entity.x, entity.y, convertARGB(150));
        frame.repaint();
    }


    //dims all pixels by 1 value
    private static void imageProcessing() {
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                List<Integer> l = splitARGB(image.getRGB(x, y));
                int newBrightness = Math.max(l.get(1)-1, 0);
                image.setRGB(x, y, convertARGB(newBrightness));
            }
        }
        //System.out.println("--------------------------");
    }



    //utility funcs
    private static int convertARGB(int a, int r, int g, int b) {
        return (clamp(a, 0, 255) << 24) | 
               (clamp(r, 0, 255) << 16) | 
               (clamp(g, 0, 255) << 8) | 
                clamp(b, 0, 255);
    }

    private static int convertARGB(int lightness) {
        return (255 << 24) |
            (clamp(lightness, 0, 255) << 16) | 
            (clamp(lightness, 0, 255) << 8 ) | 
             clamp(lightness, 0, 255);
    }

    private static List<Integer> splitARGB(int ARGB) {//lmao the bitshifting got me into such a C++ mindset that i used a Vector instead of a List
        List<Integer> l = new ArrayList<>();
        l.add((ARGB >>> 24) & 0xFF);
        l.add((ARGB >>> 16) & 0xFF);
        l.add((ARGB >>> 8 ) & 0xFF);
        l.add((ARGB >>> 0 ) & 0xFF);
        return l;
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
    
}
