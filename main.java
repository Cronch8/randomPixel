import java.awt.image.BufferedImage;

import javax.swing.JFrame;
import javax.swing.JTextField;

class Program {
    public static void main(String[] args) {
        int height = 50; 
        int width = 50; 
        int alpha = 117; 
        int red = 159; 
        int green = 50; 
        int blue = 50; 
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = (alpha << 24) | (red << 16) | (green << 8) | blue;
                image.setRGB(x, y, rgb);  // Set pixel color
            }
        }

        JFrame frame = new JFrame("my cool window!");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(new JTextField("first window!"));// equivalent to: `frame.add(new JTextField("first window!"));`
        frame.pack();
        frame.setVisible(true);
    }
}
