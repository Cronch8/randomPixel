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

/*
 *
 * WIP: make the entity more likely to move away from nearby white cells
 *      - X/Y weight is done, now just revamp the randomization algorythm
 * TODO: one thread per operaton: one for moving the entity, other for dimming the screen.
 *
*/

class Program {
    private static final int height = 1000; 
    private static final int width = 1500; 
    private static final int rangeFactor = 20;//the size of the square around which the entity looks, to steer away from newly coloured pixels
    private static int dt = 1000000000/200; //default update rate
    private static BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    private static Entity entity;
    private static JFrame frame = new JFrame("my cool window!");

    public static void main(String[] args) {
        //argumetn parsing
        if (args.length == 1) {
            try {
                dt = 1000000000/Integer.valueOf(args[0]);
            } catch (Exception e) {
                System.out.println("argument is not an integer, defaulting to " + 1000000000/dt + " updates per sec");
            }
        } else {
            System.out.println("update rate argument not provided, defaulting to " + 1000000000/dt + " updates per sec");
        }

        //make initial image
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                //image.setRGB(x, y, convertARGB((int) Math.round(Math.random()*200)));//start screen with random brightness
                image.setRGB(x, y, convertARGB(0));//all black
            }
        }
        entity = new Entity(width/2, height/2);

        //update loops
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleAtFixedRate(Program::entityUpdate, 0, dt, TimeUnit.NANOSECONDS);
        executor.scheduleAtFixedRate(Program::imageProcessing, 0, 1000000/15, TimeUnit.MICROSECONDS);
        //executor.scheduleAtFixedRate(Program::render, 0, 1000/60, TimeUnit.MILLISECONDS);// use when multithreading

        //make the window
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(new JTextField("first window!"));// equivalent to: `frame.add(new JTextField("first window!"));`
        frame.add(new JLabel(new ImageIcon(image)));
        frame.pack();
        frame.setVisible(true);
    }


    //rendering
    private static void render() {
        frame.repaint();
    }


    //entity movement and coloring loop
    private static void entityUpdate() {
        //Add more weight to directions where there's less brighness by counting 
        //the total brightness in each 4 sectors, and determining the direction of least color.
        //Positive cordinates go twoard up-right
        int weightX = 0;
        int weightY = 0;
        for (int i = 0; i < width/rangeFactor; i+=8) {//top right
            for (int j = 0; j < width/rangeFactor; j+=8) {
                int val = splitARGB(getColorLoopsafe(entity.x + i, entity.y + j)).get(3);
                weightX -= val;
                weightY -= val;
            }
        }
        for (int i = 0; i > -width/rangeFactor; i-=8) {//top left
            for (int j = 0; j < width/rangeFactor; j+=8) {
                int val = splitARGB(getColorLoopsafe(entity.x + i, entity.y + j)).get(3);
                weightX += val;
                weightY -= val;
            }
        }
        for (int i = 0; i < width/rangeFactor; i+=8) {//bottom right
            for (int j = 0; j > -width/rangeFactor; j-=8) {
                int val = splitARGB(getColorLoopsafe(entity.x + i, entity.y + j)).get(3);
                weightX -= val;
                weightY += val;
            }
        }
        for (int i = 0; i > -width/rangeFactor; i-=8) {//bottom left
            for (int j = 0; j > -width/rangeFactor; j-=8) {
                int val = splitARGB(getColorLoopsafe(entity.x + i, entity.y + j)).get(3);
                weightX += val;
                weightY += val;
            }
        }

        //calculate movement directions
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
        frame.repaint();// remove this and use the scheduler with render() instead when multithreading
    }


    //dim all pixels
    private static void imageProcessing() {
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                List<Integer> l = splitARGB(image.getRGB(x, y));
                int r = Math.max(l.get(1)-5, 0);
                int g = Math.max(l.get(2)-3, 0);
                int b = Math.max(l.get(3)-2, 0);
                image.setRGB(x, y, convertARGB(255,r,g,b));
            }
        }
    }



    //utility funcs
    private static int getColorLoopsafe(int x, int y) {
        return image.getRGB((x % width + width) % width, (y % height + height) % height);
    }

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

    private static List<Integer> splitARGB(int ARGB) {//lmao the bitshifting got me into such a C++ mindset that at first i used a Vector instead of a List
        List<Integer> l = new ArrayList<>();
        l.add((ARGB >>> 24) & 0xFF);// `& 0xFF` zeroes the negative indicator bit
        l.add((ARGB >>> 16) & 0xFF);
        l.add((ARGB >>> 8 ) & 0xFF);
        l.add((ARGB >>> 0 ) & 0xFF);
        return l;
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
    
}
