import java.awt.image.BufferedImage;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;

class Program {
    public static void main(String[] args) {
        int height = 300; 
        int width = 500; 
        int alpha = 255; 
        int red = 0; 
        int green = 0; 
        int blue = 0; 
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = (alpha << 24) | (red << 16) | (green << 8) | blue;
                image.setRGB(x, y, rgb);  // Set pixel color
            }
        }

        int x = (int) Math.round(width*0.5);
        int y = (int) Math.round(height*0.5);
        image.setRGB(x, y, (255 << 24) | (255 << 16) | (255 << 8) | 255);

        JFrame frame = new JFrame("my cool window!");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(new JTextField("first window!"));// equivalent to: `frame.add(new JTextField("first window!"));`
        frame.add(new JLabel(new ImageIcon(image)));
        frame.pack();
        frame.setVisible(true);
    }
}
