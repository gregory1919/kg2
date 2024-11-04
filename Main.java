import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import java.io.IOException;

class ImageProcessingApp extends JFrame {
    private JLabel imageLabel;
    private BufferedImage originalImage;
    private BufferedImage processedImage;

    public ImageProcessingApp() {
        setTitle("Image Processing Application");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLayout(new BorderLayout());

        imageLabel = new JLabel();
        imageLabel.setHorizontalAlignment(JLabel.CENTER);
        add(imageLabel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        JButton loadButton = new JButton("Load Image");
        JButton sharpenButton = new JButton("Sharpen (High-Frequency Filter)");
        JButton thresholdButton1 = new JButton("Threshold Method 1");
        JButton thresholdButton2 = new JButton("Threshold Method 2");
        buttonPanel.add(loadButton);
        buttonPanel.add(sharpenButton);
        buttonPanel.add(thresholdButton1);
        buttonPanel.add(thresholdButton2);
        add(buttonPanel, BorderLayout.SOUTH);

        loadButton.addActionListener(e -> loadImage());
        sharpenButton.addActionListener(e -> applySharpenFilter());
        thresholdButton1.addActionListener(e -> applyThresholdMethod1());
        thresholdButton2.addActionListener(e -> applyThresholdMethod2());
    }

    private void loadImage() {
        JFileChooser fileChooser = new JFileChooser(System.getProperty("user.dir"));
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try {
                originalImage = ImageIO.read(file);
                processedImage = originalImage;
                imageLabel.setIcon(new ImageIcon(processedImage));
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Failed to load image");
            }
        }
    }

    private void applySharpenFilter() {
        if (originalImage == null) return;

        float[] sharpenKernel = {
                0, -1, 0,
                -1, 5, -1,
                0, -1, 0
        };

        processedImage = applyConvolution(originalImage, sharpenKernel);
        imageLabel.setIcon(new ImageIcon(processedImage));
    }

    private void applyThresholdMethod1() {
        if (originalImage == null) return;

        processedImage = localThreshold(originalImage, 128);
        imageLabel.setIcon(new ImageIcon(processedImage));
    }

    private void applyThresholdMethod2() {
        if (originalImage == null) return;

        processedImage = adaptiveThreshold(originalImage, 16);
        imageLabel.setIcon(new ImageIcon(processedImage));
    }

    private BufferedImage applyConvolution(BufferedImage image, float[] kernel) {
        BufferedImage result = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());

        for (int y = 1; y < image.getHeight() - 1; y++) {
            for (int x = 1; x < image.getWidth() - 1; x++) {
                int pixel = applyKernel(x, y, kernel, image);
                result.setRGB(x, y, pixel);
            }
        }
        return result;
    }

    private int applyKernel(int x, int y, float[] kernel, BufferedImage image) {
        int rgb = 0;
        int kernelIndex = 0;

        for (int ky = -1; ky <= 1; ky++) {
            for (int kx = -1; kx <= 1; kx++) {
                int pixelColor = image.getRGB(x + kx, y + ky);
                int gray = (pixelColor >> 16) & 0xFF;
                rgb += gray * kernel[kernelIndex++];
            }
        }
        return new Color(clamp(rgb), clamp(rgb), clamp(rgb)).getRGB();
    }

    private int clamp(int value) {
        return Math.max(0, Math.min(255, value));
    }

    private BufferedImage localThreshold(BufferedImage image, int threshold) {
        BufferedImage result = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int pixelColor = image.getRGB(x, y);
                int gray = (pixelColor >> 16) & 0xFF;
                int newColor = gray < threshold ? 0 : 255;
                result.setRGB(x, y, new Color(newColor, newColor, newColor).getRGB());
            }
        }
        return result;
    }

    private BufferedImage adaptiveThreshold(BufferedImage image, int blockSize) {
        BufferedImage result = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());

        for (int y = 0; y < image.getHeight(); y += blockSize) {
            for (int x = 0; x < image.getWidth(); x += blockSize) {
                int sum = 0, count = 0;

                for (int by = 0; by < blockSize && (y + by) < image.getHeight(); by++) {
                    for (int bx = 0; bx < blockSize && (x + bx) < image.getWidth(); bx++) {
                        int pixelColor = image.getRGB(x + bx, y + by);
                        int gray = (pixelColor >> 16) & 0xFF;
                        sum += gray;
                        count++;
                    }
                }

                int average = sum / count;

                for (int by = 0; by < blockSize && (y + by) < image.getHeight(); by++) {
                    for (int bx = 0; bx < blockSize && (x + bx) < image.getWidth(); bx++) {
                        int pixelColor = image.getRGB(x + bx, y + by);
                        int gray = (pixelColor >> 16) & 0xFF;
                        int newColor = gray < average ? 0 : 255;
                        result.setRGB(x + bx, y + by, new Color(newColor, newColor, newColor).getRGB());
                    }
                }
            }
        }
        return result;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ImageProcessingApp app = new ImageProcessingApp();
            app.setVisible(true);
        });
    }
}
