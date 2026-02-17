package tucil1.aufar.controllers;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.paint.Color;

/**
 * ImageProcessor - Processes board images to extract region information
 * Converts a screenshot of the Queens game to a char[][] board representation
 */
public class ImageProcessor {

    private int regionCount;
    private Map<Color, Character> colorToRegion;
    
    public ImageProcessor() {
        colorToRegion = new HashMap<>();
        regionCount = 0;
    }

    /**
     * Process an image file and extract the board configuration
     * @param imageFile The image file to process
     * @return char[][] representing the board with regions
     * @throws IOException if image cannot be read
     */
    public char[][] processImage(File imageFile) throws IOException {
        Image image;
        try (FileInputStream fis = new FileInputStream(imageFile)) {
            image = new Image(fis);
        }
        
        int imgWidth = (int) image.getWidth();
        int imgHeight = (int) image.getHeight();
        
        PixelReader reader = image.getPixelReader();
        
        // Detect board boundaries
        int[] bounds = detectBoardBounds(reader, imgWidth, imgHeight);
        int startX = bounds[0];
        int startY = bounds[1];
        int endX = bounds[2];
        int endY = bounds[3];
        
        int boardWidth = endX - startX;
        int boardHeight = endY - startY;
        
        // Detect grid size by analyzing the image
        int gridSize = detectGridSize(reader, startX, startY, boardWidth, boardHeight);
        
        if (gridSize < 1 || gridSize > 15) {
            // Fallback: try to estimate from image size
            gridSize = estimateGridSize(boardWidth, boardHeight);
        }
        
        // Sample colors from each cell
        char[][] board = new char[gridSize][gridSize];
        colorToRegion.clear();
        regionCount = 0;
        
        int cellWidth = boardWidth / gridSize;
        int cellHeight = boardHeight / gridSize;
        
        for (int row = 0; row < gridSize; row++) {
            for (int col = 0; col < gridSize; col++) {
                // Sample color from center of cell
                int centerX = startX + col * cellWidth + cellWidth / 2;
                int centerY = startY + row * cellHeight + cellHeight / 2;
                
                Color cellColor = reader.getColor(
                    Math.min(centerX, imgWidth - 1), 
                    Math.min(centerY, imgHeight - 1)
                );
                
                // Normalize color to reduce noise
                Color normalizedColor = normalizeColor(cellColor);
                
                // Map color to region character
                board[row][col] = getRegionChar(normalizedColor);
            }
        }
        
        regionCount = colorToRegion.size();
        return board;
    }
    
    /**
     * Detect the boundaries of the game board in the image
     */
    private int[] detectBoardBounds(PixelReader reader, int width, int height) {
        int startX = 0, startY = 0, endX = width, endY = height;
        
        // Look for the board region (typically has colored cells)
        // Skip UI elements by finding the main grid area
        
        // Find top boundary
        for (int y = 0; y < height; y++) {
            int colorfulPixels = 0;
            for (int x = 0; x < width; x++) {
                Color c = reader.getColor(x, y);
                if (isColorful(c)) colorfulPixels++;
            }
            if (colorfulPixels > width * 0.3) {
                startY = Math.max(0, y - 5);
                break;
            }
        }
        
        // Find bottom boundary
        for (int y = height - 1; y >= 0; y--) {
            int colorfulPixels = 0;
            for (int x = 0; x < width; x++) {
                Color c = reader.getColor(x, y);
                if (isColorful(c)) colorfulPixels++;
            }
            if (colorfulPixels > width * 0.3) {
                endY = Math.min(height, y + 5);
                break;
            }
        }
        
        // Find left boundary
        for (int x = 0; x < width; x++) {
            int colorfulPixels = 0;
            for (int y = startY; y < endY; y++) {
                Color c = reader.getColor(x, y);
                if (isColorful(c)) colorfulPixels++;
            }
            if (colorfulPixels > (endY - startY) * 0.3) {
                startX = Math.max(0, x - 5);
                break;
            }
        }
        
        // Find right boundary
        for (int x = width - 1; x >= 0; x--) {
            int colorfulPixels = 0;
            for (int y = startY; y < endY; y++) {
                Color c = reader.getColor(x, y);
                if (isColorful(c)) colorfulPixels++;
            }
            if (colorfulPixels > (endY - startY) * 0.3) {
                endX = Math.min(width, x + 5);
                break;
            }
        }
        
        return new int[] {startX, startY, endX, endY};
    }
    
    /**
     * Detect grid size by looking for grid lines or color transitions
     */
    private int detectGridSize(PixelReader reader, int startX, int startY, int boardWidth, int boardHeight) {
        // Count vertical transitions across the middle row
        int middleY = startY + boardHeight / 2;
        int transitions = 0;
        Color prevColor = null;
        
        for (int x = startX; x < startX + boardWidth; x++) {
            Color c = normalizeColor(reader.getColor(x, middleY));
            if (prevColor != null && !colorsAreSimilar(c, prevColor)) {
                transitions++;
            }
            prevColor = c;
        }
        
        // Grid size is approximately transitions / 2 + 1
        int estimatedSize = (transitions / 2) + 1;
        
        // Common grid sizes for Queens game
        if (estimatedSize >= 4 && estimatedSize <= 12) {
            return estimatedSize;
        }
        
        return estimateGridSize(boardWidth, boardHeight);
    }
    
    /**
     * Estimate grid size based on image dimensions
     */
    private int estimateGridSize(int width, int height) {
        // Assume cells are roughly square and between 30-60 pixels
        int avgDimension = (width + height) / 2;
        int estimatedCellSize = 45;
        int gridSize = avgDimension / estimatedCellSize;
        
        // Clamp to reasonable range
        return Math.max(4, Math.min(12, gridSize));
    }
    
    /**
     * Check if a color is colorful (not grayscale)
     */
    private boolean isColorful(Color c) {
        double r = c.getRed();
        double g = c.getGreen();
        double b = c.getBlue();
        
        // Check if any channel differs significantly
        double max = Math.max(Math.max(r, g), b);
        double min = Math.min(Math.min(r, g), b);
        double saturation = (max > 0) ? (max - min) / max : 0;
        
        return saturation > 0.1 && c.getBrightness() > 0.3 && c.getBrightness() < 0.95;
    }
    
    /**
     * Normalize color to reduce noise and group similar colors
     */
    private Color normalizeColor(Color c) {
        // Quantize to reduce color variations
        double r = Math.round(c.getRed() * 8) / 8.0;
        double g = Math.round(c.getGreen() * 8) / 8.0;
        double b = Math.round(c.getBlue() * 8) / 8.0;
        
        return Color.color(r, g, b);
    }
    
    /**
     * Check if two colors are similar enough to be the same region
     */
    private boolean colorsAreSimilar(Color c1, Color c2) {
        double threshold = 0.15;
        return Math.abs(c1.getRed() - c2.getRed()) < threshold &&
               Math.abs(c1.getGreen() - c2.getGreen()) < threshold &&
               Math.abs(c1.getBlue() - c2.getBlue()) < threshold;
    }
    
    /**
     * Get or assign a region character for a color
     */
    private char getRegionChar(Color normalizedColor) {
        // Check if we've seen this color before
        for (Map.Entry<Color, Character> entry : colorToRegion.entrySet()) {
            if (colorsAreSimilar(entry.getKey(), normalizedColor)) {
                return entry.getValue();
            }
        }
        
        // Assign new region character
        char newChar = (char) ('A' + colorToRegion.size());
        if (newChar > 'Z') {
            newChar = (char) ('a' + (colorToRegion.size() - 26));
        }
        colorToRegion.put(normalizedColor, newChar);
        return newChar;
    }
    
    /**
     * Get the number of detected regions
     */
    public int getRegionCount() {
        return regionCount;
    }
    
    /**
     * Get the color-to-region mapping
     */
    public Map<Color, Character> getColorMapping() {
        return new HashMap<>(colorToRegion);
    }
}
