import java.awt.Point;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
 * WIP: add pixels that need to be dimmed into an array, then dim only the pixels that need to be dimmed, not everything. then remove items from array when they reach black
 * WIP: one thread per operaton: one for moving the entity, other for dimming the screen.
 * TODO: why did i make an entity class? there already exists a point class that i'm using...
 * TODO: multiple entity support
 *
*/

class Program {

    //size of the canvas
    private static final int height = 1000; 
    private static final int width = 1500; 

    //how likely the entity is to step away from nearby color (smaller value = more likely). Negative values supported, makes it step twoard color instead
    private static final int colorAvoidance = 300;

    //the size of the square in which the entity looks for colors, to move away from them (in fraction of canvas size, where 10 on a 1000x1000 window is 100 pixels)
    private static final int rangeFactor = 4;

    //how many pixels are sampled in each direction to look for colors (performance intensive!)
    private static final int rangeResulution = 3;

    //how many times per sec the background brigtnes gets reduced (performance intensive!)
    private static final int fadeUpdateRate = 60;

    //how many pixels thiccc the line is that the entity makes (performance intensive!)
    private static final int thickness = 1;
    
    //the color that the moving entity leaves behind
    private static final int headColor = convertARGB(255,255,230,180);

    private static int prevMovementDirX = 0;
    private static int prevMovementDirY = 0;
    private static int dt = 1000000000/1000;//default update rate
    private static BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    private static Entity entity;
    private static JFrame frame = new JFrame("my cool window!");
    private static Map<Point, Integer> pixelsToUpdate = new LinkedHashMap<>();//contains all non-black pixels, so that they can be decayed

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
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();//does this just magically allow me to make stuff multithreaded? (i replaced it with Executors.newSingleThreadSceduledExecutor)
        executor.scheduleAtFixedRate(Program::entityUpdate, 0, dt, TimeUnit.NANOSECONDS);
        executor.scheduleAtFixedRate(Program::imageProcessing, 0, 1000000/fadeUpdateRate, TimeUnit.MICROSECONDS);
        //executor.scheduleAtFixedRate(Program::render, 0, 1000/60, TimeUnit.MILLISECONDS);// use when multithreading

        //make the window
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(new JTextField("moving thingy"));// equivalent to: `frame.add(new JTextField(""));`
        frame.add(new JLabel(new ImageIcon(image)));
        frame.pack();
        //frame.setSize(1500, 1000);
        frame.setVisible(true);
    }


    //rendering (use when implemting multithreading)
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
        for (int i = 0; i < width/rangeFactor; i+=width/rangeFactor/rangeResulution) {//top right
            for (int j = 0; j < width/rangeFactor; j+=width/rangeFactor/rangeResulution) {
                int val = splitARGB(getColorLoopsafe(entity.x + i, entity.y + j))[3];
                weightX -= val;
                weightY -= val;
            }
        }
        for (int i = 0; i > -width/rangeFactor; i-=width/rangeFactor/rangeResulution) {//top left
            for (int j = 0; j < width/rangeFactor; j+=width/rangeFactor/rangeResulution) {
                int val = splitARGB(getColorLoopsafe(entity.x + i, entity.y + j))[3];
                weightX += val;
                weightY -= val;
            }
        }
        for (int i = 0; i < width/rangeFactor; i+=width/rangeFactor/rangeResulution) {//bottom right
            for (int j = 0; j > -width/rangeFactor; j-=width/rangeFactor/rangeResulution) {
                int val = splitARGB(getColorLoopsafe(entity.x + i, entity.y + j))[3];
                weightX -= val;
                weightY += val;
            }
        }
        for (int i = 0; i > -width/rangeFactor; i-=width/rangeFactor/rangeResulution) {//bottom left
            for (int j = 0; j > -width/rangeFactor; j-=width/rangeFactor/rangeResulution) {
                int val = splitARGB(getColorLoopsafe(entity.x + i, entity.y + j))[3];
                weightX += val;
                weightY += val;
            }
        }

        //calculate movement directions
        int moveRight = (int)Math.round(Math.random()*3000-1500) + weightX/colorAvoidance + prevMovementDirX*300;
        int moveUp = (int)Math.round(Math.random()*3000-1500) + weightY/colorAvoidance + prevMovementDirY*300;
        if (moveRight > 1000) {
            entity.x += 1;
            prevMovementDirX = 1;
            if (entity.x >= width) entity.x = 0;
        } else if (moveRight < -1000) {
            entity.x -= 1;
            prevMovementDirX = -1;
            if (entity.x <= 0) entity.x = width-1;
        }
        if (moveUp > 1000) {
            entity.y += 1;
            prevMovementDirY = 1;
            if (entity.y >= height) entity.y = 0;
        } else if (moveUp < -1000) {
            entity.y -= 1;
            prevMovementDirY = -1;
            if (entity.y <= 0) entity.y = height-1;
        }

        //make the line thiccc 
        for (int y = 0; y < thickness; y++) {//bottom left
            for (int x = 0; x < thickness; x++) {
                setColorLoopsafe(entity.x+x, entity.y+y, headColor);
                pixelsToUpdate.put(new Point(entity.x+x, entity.y+y), image.getRGB(entity.x+x, entity.y+y));
            }
        }
        frame.repaint();// remove this and use the scheduler with render() func instead when multithreading
    }


    //dim all pixels
    private static void imageProcessing() {
        //loops through each non-black pixel
        int[] colors = new int[4];
        List<Map.Entry<Point,Integer>> entriesToRemove = new ArrayList<>(pixelsToUpdate.size());
        for (Map.Entry<Point, Integer> entry : pixelsToUpdate.entrySet()) {
            colors = splitARGB(entry.getValue());
            //System.out.print(" || r:" + colors[1] + " g:" + colors[2] + " b:" + colors[3]);
            int r = Math.max(colors[1]-5, 0);
            int g = Math.max(colors[2]-3, 0);
            int b = Math.max(colors[3]-2, 0);
            image.setRGB(entry.getKey().x, entry.getKey().y, convertARGB(255,r,g,b));
            pixelsToUpdate.replace(entry.getKey(), convertARGB(255,r,g,b));
            if (r+g+b == 0) {
                entriesToRemove.add(entry);
            }
        }
        //if pixel is black, stop updating it
        for (Map.Entry<Point,Integer> entry : entriesToRemove) {
            pixelsToUpdate.remove(entry.getKey(), entry.getValue());
        }
        //System.out.println("size: " + pixelsToUpdate.size());
    }



    //utility funcs
    private static int getColorLoopsafe(int x, int y) {
        return image.getRGB((x % width + width) % width, (y % height + height) % height);
    }

    private static void setColorLoopsafe(int x, int y, int color) {
        image.setRGB((x % width + width) % width, (y % height + height) % height, color);
        return;
    }


    private static int convertARGB(int a, int r, int g, int b) {
        return (clamp(a, 0, 255) << 24) | 
               (clamp(r, 0, 255) << 16) | 
               (clamp(g, 0, 255) << 8 ) | 
                clamp(b, 0, 255);
    }

    private static int convertARGB(int lightness) {
        return (255 << 24) |
            (clamp(lightness, 0, 255) << 16) | 
            (clamp(lightness, 0, 255) << 8 ) | 
             clamp(lightness, 0, 255);
    }

    private static int[] splitARGB(int ARGB) {//lmao the bitshifting got me into such a C++ mindset that at first i used a Vector instead of a List
        int[] i = new int[4];
        i[0] = ((ARGB >>> 24) & 0xFF);// `& 0xFF` zeroes the negative indicator bit
        i[1] = ((ARGB >>> 16) & 0xFF);
        i[2] = ((ARGB >>> 8 ) & 0xFF);
        i[3] = ((ARGB >>> 0 ) & 0xFF);
        return i;
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
    
}
