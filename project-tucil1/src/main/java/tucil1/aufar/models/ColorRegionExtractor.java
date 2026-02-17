package tucil1.aufar.models;

import java.util.ArrayList;
import java.util.List;

import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.paint.Color;

/**
 * Model class for extracting board regions from images based on color analysis.
 * Uses HSB color space for more accurate color matching.
 */
public class ColorRegionExtractor {

    private char[][] resultBoard;
    private int regionCount;
    
    // Thresholds (can be adjusted)
    private double satThreshold = 0.20;
    private double brightThreshold = 0.18;
    
    /**
     * Extract board configuration from an image
     * @param image The source image
     * @param gridX Grid X position (in image coordinates)
     * @param gridY Grid Y position (in image coordinates)
     * @param gridWidth Grid width (in image coordinates)
     * @param gridHeight Grid height (in image coordinates)
     * @param boardSize Number of rows/columns
     * @return char[][] board representation
     */
    public char[][] extractBoard(Image image, int gridX, int gridY, 
                                  int gridWidth, int gridHeight, int boardSize) {
        PixelReader reader = image.getPixelReader();
        
        int cellW = gridWidth / boardSize;
        int cellH = gridHeight / boardSize;
        
        resultBoard = new char[boardSize][boardSize];
        List<double[]> knownColors = new ArrayList<>();
        char nextRegion = 'A';
        
        int imgW = (int) image.getWidth();
        int imgH = (int) image.getHeight();
        
        for (int row = 0; row < boardSize; row++) {
            for (int col = 0; col < boardSize; col++) {
                // Multi-point sampling for robustness
                Color cellColor = sampleCellColor(reader, 
                    gridX + col * cellW, gridY + row * cellH, 
                    cellW, cellH, imgW, imgH);
                
                // Convert to HSB for better color matching
                double[] hsb = colorToHSB(cellColor);
                
                // Find matching region or create new one
                int matchedRegion = findMatchingColor(knownColors, hsb);
                
                if (matchedRegion == -1) {
                    // New color region
                    knownColors.add(hsb);
                    resultBoard[row][col] = nextRegion++;
                } else {
                    resultBoard[row][col] = (char) ('A' + matchedRegion);
                }
            }
        }
        
        regionCount = knownColors.size();
        return resultBoard;
    }
    
    /**
     * Sample color from multiple points in a cell and return average color
     */
    public Color sampleCellColor(PixelReader reader, int cellX, int cellY, 
                                  int cellW, int cellH, int imgW, int imgH) {
        // Sample 5 points: center and 4 around it
        int[][] offsets = {
            {cellW / 2, cellH / 2},         // center
            {cellW / 3, cellH / 3},         // top-left area
            {2 * cellW / 3, cellH / 3},     // top-right area
            {cellW / 3, 2 * cellH / 3},     // bottom-left area
            {2 * cellW / 3, 2 * cellH / 3}  // bottom-right area
        };
        
        double totalR = 0, totalG = 0, totalB = 0;
        int count = 0;
        
        for (int[] offset : offsets) {
            int x = Math.min(cellX + offset[0], imgW - 1);
            int y = Math.min(cellY + offset[1], imgH - 1);
            x = Math.max(0, x);
            y = Math.max(0, y);
            
            Color c = reader.getColor(x, y);
            totalR += c.getRed();
            totalG += c.getGreen();
            totalB += c.getBlue();
            count++;
        }
        
        return Color.color(totalR / count, totalG / count, totalB / count);
    }
    
    /**
     * Convert Color to HSB array [hue, saturation, brightness]
     */
    public double[] colorToHSB(Color color) {
        double r = color.getRed();
        double g = color.getGreen();
        double b = color.getBlue();
        
        double max = Math.max(r, Math.max(g, b));
        double min = Math.min(r, Math.min(g, b));
        double delta = max - min;
        
        double hue = 0;
        double saturation = (max == 0) ? 0 : delta / max;
        double brightness = max;
        
        if (delta != 0) {
            if (max == r) {
                hue = 60 * (((g - b) / delta) % 6);
            } else if (max == g) {
                hue = 60 * (((b - r) / delta) + 2);
            } else {
                hue = 60 * (((r - g) / delta) + 4);
            }
        }
        if (hue < 0) hue += 360;
        
        return new double[]{hue, saturation, brightness};
    }
    
    /**
     * Find a matching color in the known colors list using HSB distance
     * @return index if found, -1 if no match
     */
    public int findMatchingColor(List<double[]> knownColors, double[] hsb) {
        for (int i = 0; i < knownColors.size(); i++) {
            double[] known = knownColors.get(i);
            
            // Calculate hue distance (circular)
            double hueDiff = Math.abs(hsb[0] - known[0]);
            if (hueDiff > 180) hueDiff = 360 - hueDiff;
            
            double satDiff = Math.abs(hsb[1] - known[1]);
            double brightDiff = Math.abs(hsb[2] - known[2]);
            
            // Dynamic hue threshold based on color range
            double hueThreshold = getHueThreshold(hsb[0], known[0]);
            
            // For low saturation (grayish) colors, rely more on brightness
            if (hsb[1] < 0.15 && known[1] < 0.15) {
                // Both are grayish - compare brightness only
                if (brightDiff < 0.12) {
                    return i;
                }
            } else if (hueDiff < hueThreshold && satDiff < satThreshold && brightDiff < brightThreshold) {
                return i;
            }
        }
        
        return -1;
    }
    
    /**
     * Get dynamic hue threshold based on color range
     * Red-Orange-Yellow range needs tighter threshold to distinguish similar colors
     */
    public double getHueThreshold(double hue1, double hue2) {
        // Check if both colors are in the critical red-orange-yellow range (0-60° or 330-360°)
        boolean inCriticalRange1 = (hue1 >= 0 && hue1 <= 60) || (hue1 >= 330 && hue1 <= 360);
        boolean inCriticalRange2 = (hue2 >= 0 && hue2 <= 60) || (hue2 >= 330 && hue2 <= 360);
        
        if (inCriticalRange1 && inCriticalRange2) {
            // Both in red-orange-yellow range - use tight threshold
            return 12;
        }
        
        // Check green-cyan range (90-180°)
        boolean inGreenRange1 = (hue1 >= 90 && hue1 <= 180);
        boolean inGreenRange2 = (hue2 >= 90 && hue2 <= 180);
        
        if (inGreenRange1 && inGreenRange2) {
            return 15;
        }
        
        // Default threshold for other color ranges
        return 20;
    }
    
    /**
     * Get the extracted board
     */
    public char[][] getResultBoard() {
        return resultBoard;
    }
    
    /**
     * Get the number of detected regions
     */
    public int getRegionCount() {
        return regionCount;
    }
    
    // Setters for fine-tuning thresholds
    public void setSaturationThreshold(double threshold) {
        this.satThreshold = threshold;
    }
    
    public void setBrightnessThreshold(double threshold) {
        this.brightThreshold = threshold;
    }
}
