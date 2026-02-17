package tucil1.aufar.views;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Dialog for configuring image-based board input.
 * Allows user to:
 * - Set board size via slider
 * - Adjust grid overlay position and size to match the board in image
 * - Visualize sampling points (dots) in each cell
 */
public class ImageConfigDialog extends Stage {

    private Image image;
    private ImageView imageView;
    private Pane overlayPane;
    private Rectangle gridRect;
    private int boardSize = 8;
    private char[][] resultBoard;
    private int regionCount;
    
    // Grid overlay properties
    private double gridX, gridY, gridWidth, gridHeight;
    
    // For dragging/resizing
    private double dragStartX, dragStartY;
    private double initialGridX, initialGridY, initialGridW, initialGridH;
    private boolean isDragging = false;
    private boolean isResizing = false;
    
    // Grid lines and dots
    private Line[] horizontalLines;
    private Line[] verticalLines;
    private Circle[][] cellDots;
    
    // Resize handles
    private Rectangle resizeHandleCorner;  // Bottom-right corner (both)
    private Rectangle resizeHandleRight;   // Right edge (width only)
    private Rectangle resizeHandleBottom;  // Bottom edge (height only)
    private String resizeMode = "";  // "corner", "right", "bottom"
    
    public ImageConfigDialog(File imageFile) throws IOException {
        setTitle("Konfigurasi Grid - Image Input");
        initModality(Modality.APPLICATION_MODAL);
        
        // Load image
        try (FileInputStream fis = new FileInputStream(imageFile)) {
            image = new Image(fis);
        }
        
        // Scale image if too large
        double maxSize = 600;
        double scale = Math.min(1.0, Math.min(maxSize / image.getWidth(), maxSize / image.getHeight()));
        double displayWidth = image.getWidth() * scale;
        double displayHeight = image.getHeight() * scale;
        
        // Image view
        imageView = new ImageView(image);
        imageView.setFitWidth(displayWidth);
        imageView.setFitHeight(displayHeight);
        imageView.setPreserveRatio(true);
        
        // Overlay pane for grid
        overlayPane = new Pane();
        overlayPane.setPrefSize(displayWidth, displayHeight);
        overlayPane.setMouseTransparent(false);
        
        // Initialize grid position (centered, 80% of image size)
        gridWidth = displayWidth * 0.8;
        gridHeight = displayHeight * 0.8;
        gridX = (displayWidth - gridWidth) / 2;
        gridY = (displayHeight - gridHeight) / 2;
        
        // Create grid rectangle (border)
        gridRect = new Rectangle(gridX, gridY, gridWidth, gridHeight);
        gridRect.setFill(Color.TRANSPARENT);
        gridRect.setStroke(Color.LIME);
        gridRect.setStrokeWidth(2);
        gridRect.setCursor(Cursor.MOVE);
        
        // Resize handle - bottom-right corner (both width and height)
        resizeHandleCorner = new Rectangle(12, 12);
        resizeHandleCorner.setFill(Color.LIME);
        resizeHandleCorner.setCursor(Cursor.SE_RESIZE);
        
        // Resize handle - right edge (width only)
        resizeHandleRight = new Rectangle(8, 30);
        resizeHandleRight.setFill(Color.CYAN);
        resizeHandleRight.setCursor(Cursor.E_RESIZE);
        
        // Resize handle - bottom edge (height only)
        resizeHandleBottom = new Rectangle(30, 8);
        resizeHandleBottom.setFill(Color.CYAN);
        resizeHandleBottom.setCursor(Cursor.S_RESIZE);
        
        updateResizeHandlePositions();
        
        overlayPane.getChildren().addAll(gridRect, resizeHandleCorner, resizeHandleRight, resizeHandleBottom);
        
        // Initialize grid lines and dots
        createGridElements();
        updateGridElements();
        
        // Setup drag events for grid rectangle
        setupDragEvents();
        setupResizeEvents();
        
        // Stack image and overlay
        StackPane imageStack = new StackPane();
        imageStack.getChildren().addAll(imageView, overlayPane);
        imageStack.setAlignment(Pos.TOP_LEFT);
        
        // Controls
        VBox controls = new VBox(15);
        controls.setPadding(new Insets(15));
        controls.setAlignment(Pos.CENTER);
        controls.setStyle("-fx-background-color: #2C3E50;");
        
        // Board size slider
        Label sizeLabel = new Label("Ukuran Board: " + boardSize);
        sizeLabel.setTextFill(Color.WHITE);
        sizeLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        
        Slider sizeSlider = new Slider(3, 15, boardSize);
        sizeSlider.setMajorTickUnit(1);
        sizeSlider.setMinorTickCount(0);
        sizeSlider.setSnapToTicks(true);
        sizeSlider.setShowTickLabels(true);
        sizeSlider.setShowTickMarks(true);
        sizeSlider.setPrefWidth(300);
        sizeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            boardSize = newVal.intValue();
            sizeLabel.setText("Ukuran Board: " + boardSize);
            createGridElements();
            updateGridElements();
        });
        
        // Instructions
        Label instructions = new Label("Drag grid untuk memindahkan. Hijau=resize proporsional, Cyan=width/height.");
        instructions.setTextFill(Color.web("#BDC3C7"));
        instructions.setStyle("-fx-font-size: 12px;");
        
        // Buttons
        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER);
        
        Button confirmButton = new Button("✓ Konfirmasi");
        confirmButton.setStyle("-fx-background-color: #27AE60; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10 25;");
        confirmButton.setOnAction(e -> {
            extractBoard();
            close();
        });
        
        Button cancelButton = new Button("✗ Batal");
        cancelButton.setStyle("-fx-background-color: #E74C3C; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10 25;");
        cancelButton.setOnAction(e -> {
            resultBoard = null;
            close();
        });
        
        buttonBox.getChildren().addAll(confirmButton, cancelButton);
        controls.getChildren().addAll(sizeLabel, sizeSlider, instructions, buttonBox);
        
        // Main layout
        VBox mainLayout = new VBox(0);
        mainLayout.getChildren().addAll(imageStack, controls);
        mainLayout.setStyle("-fx-background-color: #1A252F;");
        
        Scene scene = new Scene(mainLayout);
        setScene(scene);
        setResizable(false);
    }
    
    private void createGridElements() {
        // Remove old elements (keep resize handles)
        overlayPane.getChildren().removeIf(node -> 
            node instanceof Line || (node instanceof Circle && 
            node != resizeHandleCorner && node != resizeHandleRight && node != resizeHandleBottom));
        
        // Create new lines
        horizontalLines = new Line[boardSize + 1];
        verticalLines = new Line[boardSize + 1];
        cellDots = new Circle[boardSize][boardSize];
        
        for (int i = 0; i <= boardSize; i++) {
            horizontalLines[i] = new Line();
            horizontalLines[i].setStroke(Color.LIME);
            horizontalLines[i].setStrokeWidth(1);
            horizontalLines[i].setMouseTransparent(true);
            
            verticalLines[i] = new Line();
            verticalLines[i].setStroke(Color.LIME);
            verticalLines[i].setStrokeWidth(1);
            verticalLines[i].setMouseTransparent(true);
            
            overlayPane.getChildren().addAll(horizontalLines[i], verticalLines[i]);
        }
        
        // Create dots for cell centers
        for (int row = 0; row < boardSize; row++) {
            for (int col = 0; col < boardSize; col++) {
                Circle dot = new Circle(4);
                dot.setFill(Color.RED);
                dot.setStroke(Color.WHITE);
                dot.setStrokeWidth(1);
                dot.setMouseTransparent(true);
                cellDots[row][col] = dot;
                overlayPane.getChildren().add(dot);
            }
        }
        
        // Make sure resize handles are on top
        resizeHandleCorner.toFront();
        resizeHandleRight.toFront();
        resizeHandleBottom.toFront();
    }
    
    private void updateGridElements() {
        double cellW = gridWidth / boardSize;
        double cellH = gridHeight / boardSize;
        
        // Update grid rectangle
        gridRect.setX(gridX);
        gridRect.setY(gridY);
        gridRect.setWidth(gridWidth);
        gridRect.setHeight(gridHeight);
        
        // Update horizontal lines
        for (int i = 0; i <= boardSize; i++) {
            double y = gridY + i * cellH;
            horizontalLines[i].setStartX(gridX);
            horizontalLines[i].setEndX(gridX + gridWidth);
            horizontalLines[i].setStartY(y);
            horizontalLines[i].setEndY(y);
        }
        
        // Update vertical lines
        for (int i = 0; i <= boardSize; i++) {
            double x = gridX + i * cellW;
            verticalLines[i].setStartX(x);
            verticalLines[i].setEndX(x);
            verticalLines[i].setStartY(gridY);
            verticalLines[i].setEndY(gridY + gridHeight);
        }
        
        // Update dots at cell centers
        for (int row = 0; row < boardSize; row++) {
            for (int col = 0; col < boardSize; col++) {
                double centerX = gridX + col * cellW + cellW / 2;
                double centerY = gridY + row * cellH + cellH / 2;
                cellDots[row][col].setCenterX(centerX);
                cellDots[row][col].setCenterY(centerY);
            }
        }
        
        updateResizeHandlePositions();
    }
    
    private void updateResizeHandlePositions() {
        // Corner handle (bottom-right)
        resizeHandleCorner.setX(gridX + gridWidth - 6);
        resizeHandleCorner.setY(gridY + gridHeight - 6);
        
        // Right edge handle (middle right)
        resizeHandleRight.setX(gridX + gridWidth - 4);
        resizeHandleRight.setY(gridY + gridHeight / 2 - 15);
        
        // Bottom edge handle (middle bottom)
        resizeHandleBottom.setX(gridX + gridWidth / 2 - 15);
        resizeHandleBottom.setY(gridY + gridHeight - 4);
    }
    
    private void setupDragEvents() {
        gridRect.setOnMousePressed(e -> {
            if (!isResizing) {
                isDragging = true;
                dragStartX = e.getSceneX();
                dragStartY = e.getSceneY();
                initialGridX = gridX;
                initialGridY = gridY;
            }
        });
        
        gridRect.setOnMouseDragged(e -> {
            if (isDragging) {
                double deltaX = e.getSceneX() - dragStartX;
                double deltaY = e.getSceneY() - dragStartY;
                
                gridX = Math.max(0, Math.min(initialGridX + deltaX, imageView.getFitWidth() - gridWidth));
                gridY = Math.max(0, Math.min(initialGridY + deltaY, imageView.getFitHeight() - gridHeight));
                
                updateGridElements();
            }
        });
        
        gridRect.setOnMouseReleased(e -> {
            isDragging = false;
        });
    }
    
    private void setupResizeEvents() {
        // Corner handle - proportional resize (both width and height)
        resizeHandleCorner.setOnMousePressed(e -> {
            isResizing = true;
            resizeMode = "corner";
            dragStartX = e.getSceneX();
            dragStartY = e.getSceneY();
            initialGridW = gridWidth;
            initialGridH = gridHeight;
            e.consume();
        });
        
        resizeHandleCorner.setOnMouseDragged(e -> {
            if (isResizing && "corner".equals(resizeMode)) {
                double deltaX = e.getSceneX() - dragStartX;
                double deltaY = e.getSceneY() - dragStartY;
                
                double newWidth = Math.max(50, initialGridW + deltaX);
                double newHeight = Math.max(50, initialGridH + deltaY);
                
                newWidth = Math.min(newWidth, imageView.getFitWidth() - gridX);
                newHeight = Math.min(newHeight, imageView.getFitHeight() - gridY);
                
                gridWidth = newWidth;
                gridHeight = newHeight;
                
                updateGridElements();
            }
            e.consume();
        });
        
        resizeHandleCorner.setOnMouseReleased(e -> {
            isResizing = false;
            resizeMode = "";
            e.consume();
        });
        
        // Right handle - width only
        resizeHandleRight.setOnMousePressed(e -> {
            isResizing = true;
            resizeMode = "right";
            dragStartX = e.getSceneX();
            initialGridW = gridWidth;
            e.consume();
        });
        
        resizeHandleRight.setOnMouseDragged(e -> {
            if (isResizing && "right".equals(resizeMode)) {
                double deltaX = e.getSceneX() - dragStartX;
                double newWidth = Math.max(50, initialGridW + deltaX);
                newWidth = Math.min(newWidth, imageView.getFitWidth() - gridX);
                gridWidth = newWidth;
                updateGridElements();
            }
            e.consume();
        });
        
        resizeHandleRight.setOnMouseReleased(e -> {
            isResizing = false;
            resizeMode = "";
            e.consume();
        });
        
        // Bottom handle - height only
        resizeHandleBottom.setOnMousePressed(e -> {
            isResizing = true;
            resizeMode = "bottom";
            dragStartY = e.getSceneY();
            initialGridH = gridHeight;
            e.consume();
        });
        
        resizeHandleBottom.setOnMouseDragged(e -> {
            if (isResizing && "bottom".equals(resizeMode)) {
                double deltaY = e.getSceneY() - dragStartY;
                double newHeight = Math.max(50, initialGridH + deltaY);
                newHeight = Math.min(newHeight, imageView.getFitHeight() - gridY);
                gridHeight = newHeight;
                updateGridElements();
            }
            e.consume();
        });
        
        resizeHandleBottom.setOnMouseReleased(e -> {
            isResizing = false;
            resizeMode = "";
            e.consume();
        });
    }
    
    private void extractBoard() {
        PixelReader reader = image.getPixelReader();
        
        // Calculate actual image coordinates (accounting for scaling)
        double scaleX = image.getWidth() / imageView.getFitWidth();
        double scaleY = image.getHeight() / imageView.getFitHeight();
        
        int actualGridX = (int) (gridX * scaleX);
        int actualGridY = (int) (gridY * scaleY);
        int actualGridW = (int) (gridWidth * scaleX);
        int actualGridH = (int) (gridHeight * scaleY);
        
        int cellW = actualGridW / boardSize;
        int cellH = actualGridH / boardSize;
        
        resultBoard = new char[boardSize][boardSize];
        List<double[]> knownColors = new ArrayList<>();  // Store HSB values
        char nextRegion = 'A';
        
        for (int row = 0; row < boardSize; row++) {
            for (int col = 0; col < boardSize; col++) {
                // Multi-point sampling for robustness
                Color cellColor = sampleCellColor(reader, 
                    actualGridX + col * cellW, actualGridY + row * cellH, 
                    cellW, cellH, (int) image.getWidth(), (int) image.getHeight());
                
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
    }
    
    /**
     * Sample color from multiple points in a cell and return dominant color
     */
    private Color sampleCellColor(PixelReader reader, int cellX, int cellY, 
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
    private double[] colorToHSB(Color color) {
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
     * Returns index if found, -1 if no match
     */
    private int findMatchingColor(List<double[]> knownColors, double[] hsb) {
        // Base thresholds
        double satThreshold = 0.20;      // Saturation difference (0-1)
        double brightThreshold = 0.18;   // Brightness difference (0-1)
        
        for (int i = 0; i < knownColors.size(); i++) {
            double[] known = knownColors.get(i);
            
            // Calculate hue distance (circular)
            double hueDiff = Math.abs(hsb[0] - known[0]);
            if (hueDiff > 180) hueDiff = 360 - hueDiff;
            
            double satDiff = Math.abs(hsb[1] - known[1]);
            double brightDiff = Math.abs(hsb[2] - known[2]);
            
            // Dynamic hue threshold based on color range
            // Red-Orange-Yellow (0-60°) needs tighter threshold
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
    private double getHueThreshold(double hue1, double hue2) {
        // Check if both colors are in the critical red-orange-yellow range (0-60° or 330-360°)
        boolean inCriticalRange1 = (hue1 >= 0 && hue1 <= 60) || (hue1 >= 330 && hue1 <= 360);
        boolean inCriticalRange2 = (hue2 >= 0 && hue2 <= 60) || (hue2 >= 330 && hue2 <= 360);
        
        if (inCriticalRange1 && inCriticalRange2) {
            // Both in red-orange-yellow range - use tight threshold
            return 12;
        }
        
        // Check green-cyan range (90-180°) - also needs some precision
        boolean inGreenRange1 = (hue1 >= 90 && hue1 <= 180);
        boolean inGreenRange2 = (hue2 >= 90 && hue2 <= 180);
        
        if (inGreenRange1 && inGreenRange2) {
            return 15;
        }
        
        // Default threshold for other color ranges
        return 20;
    }
    
    /**
     * Get the extracted board after dialog is closed
     * @return char[][] board or null if cancelled
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
    
    /**
     * Get the configured board size
     */
    public int getBoardSize() {
        return boardSize;
    }
}
