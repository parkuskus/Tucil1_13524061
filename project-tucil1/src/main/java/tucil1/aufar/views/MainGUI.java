package tucil1.aufar.views;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.imageio.ImageIO;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.control.TextArea;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.InnerShadow;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import tucil1.aufar.controllers.IOHandler;
import tucil1.aufar.models.BruteForce;

public class MainGUI extends Application {

    private GridPane boardGrid;
    private TextArea logArea;
    private Label statusLabel;
    private Label statsLabel;
    private Button solveButton;
    private Button loadTxtButton;
    private Button loadImageButton;
    private Slider speedSlider;
    
    private char[][] currentBoard;
    private int boardSize;
    private IOHandler ioHandler;
    private volatile boolean isSolving = false;
    
    // Pre-created cell components for stable rendering
    private StackPane[][] cellPanes;
    private Circle[][] queenCircles;
    private int currentCellSize;

    @Override
    public void start(Stage primaryStage) {
        ioHandler = new IOHandler();
        
        primaryStage.setTitle("Queens Game Solver - Tucil 1");

        // Main layout with modern dark theme
        BorderPane mainLayout = new BorderPane();
        mainLayout.setPadding(new Insets(15));
        mainLayout.setStyle("-fx-background-color: linear-gradient(to bottom, #2C3E50, #1A252F);");

        // Top: Controls
        HBox controlsBox = createControlsBox();
        mainLayout.setTop(controlsBox);

        // Center: Board visualization
        VBox centerBox = new VBox(15);
        centerBox.setAlignment(Pos.CENTER);
        
        Label boardLabel = new Label("Papan Permainan");
        boardLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        boardLabel.setTextFill(Color.WHITE);
        
        boardGrid = new GridPane();
        boardGrid.setAlignment(Pos.CENTER);
        boardGrid.setHgap(3);
        boardGrid.setVgap(3);
        boardGrid.setPadding(new Insets(15));
        boardGrid.setStyle("-fx-background-color: #1E272E; -fx-background-radius: 10;");
        
        ScrollPane boardScroll = new ScrollPane(boardGrid);
        boardScroll.setFitToWidth(true);
        boardScroll.setFitToHeight(true);
        boardScroll.setPrefViewportHeight(420);
        boardScroll.setPrefViewportWidth(420);
        boardScroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        
        centerBox.getChildren().addAll(boardLabel, boardScroll);
        mainLayout.setCenter(centerBox);

        // Right: Log area
        VBox logBox = new VBox(10);
        logBox.setPadding(new Insets(10));
        
        Label logLabel = new Label("Log Pencarian");
        logLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        logLabel.setTextFill(Color.WHITE);
        
        logArea = new TextArea();
        logArea.setEditable(false);
        logArea.setPrefWidth(320);
        logArea.setPrefHeight(400);
        logArea.setFont(Font.font("Consolas", 11));
        logArea.setStyle("-fx-control-inner-background: #1E272E; -fx-text-fill: #ECF0F1; -fx-background-radius: 8;");
        
        logBox.getChildren().addAll(logLabel, logArea);
        mainLayout.setRight(logBox);

        // Bottom: Status and stats
        VBox bottomBox = new VBox(8);
        bottomBox.setPadding(new Insets(10));
        
        statusLabel = new Label("Status: Menunggu input...");
        statusLabel.setFont(Font.font("Segoe UI", 12));
        statusLabel.setTextFill(Color.web("#BDC3C7"));
        
        statsLabel = new Label("");
        statsLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        statsLabel.setTextFill(Color.web("#3498DB"));
        
        // Speed control
        HBox speedBox = new HBox(10);
        speedBox.setAlignment(Pos.CENTER_LEFT);
        Label speedLabel = new Label("Kecepatan Visualisasi:");
        speedLabel.setTextFill(Color.WHITE);
        speedSlider = new Slider(1, 100, 50);
        speedSlider.setShowTickLabels(true);
        speedSlider.setShowTickMarks(true);
        speedSlider.setPrefWidth(200);
        Label speedValueLabel = new Label("50 ms");
        speedValueLabel.setTextFill(Color.web("#2ECC71"));
        speedSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            speedValueLabel.setText(newVal.intValue() + " ms");
        });
        speedBox.getChildren().addAll(speedLabel, speedSlider, speedValueLabel);
        
        bottomBox.getChildren().addAll(speedBox, statusLabel, statsLabel);
        mainLayout.setBottom(bottomBox);

        Scene scene = new Scene(mainLayout, 900, 650);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private HBox createControlsBox() {
        HBox controlsBox = new HBox(15);
        controlsBox.setPadding(new Insets(10));
        controlsBox.setAlignment(Pos.CENTER);
        
        // Style dengan font yang mendukung emoji (Segoe UI Emoji untuk Windows)
        String buttonStyle = "-fx-font-family: 'Segoe UI Emoji', 'Apple Color Emoji', 'Noto Color Emoji', sans-serif; -fx-font-size: 14px; -fx-padding: 10 20;";

        loadTxtButton = new Button("\uD83D\uDCC2 Load TXT File");
        loadTxtButton.setStyle(buttonStyle);
        loadTxtButton.setOnAction(e -> loadTxtFile());

        loadImageButton = new Button("\uD83D\uDDBC Load Image");
        loadImageButton.setStyle(buttonStyle);
        loadImageButton.setOnAction(e -> loadImageFile());

        solveButton = new Button("\u25B6 Solve");
        solveButton.setStyle(buttonStyle + "-fx-background-color: #4CAF50; -fx-text-fill: white;");
        solveButton.setDisable(true);
        solveButton.setOnAction(e -> startSolving());

        Button clearButton = new Button("\uD83D\uDDD1 Clear");
        clearButton.setStyle(buttonStyle);
        clearButton.setOnAction(e -> clearBoard());

        Button saveTxtButton = new Button("\uD83D\uDCBE Save Log");
        saveTxtButton.setStyle(buttonStyle + "-fx-background-color: #3498DB; -fx-text-fill: white;");
        saveTxtButton.setOnAction(e -> saveTxtFile());

        Button saveImageButton = new Button("\uD83D\uDDBC Save Image");
        saveImageButton.setStyle(buttonStyle + "-fx-background-color: #9B59B6; -fx-text-fill: white;");
        saveImageButton.setOnAction(e -> saveImageFile());

        controlsBox.getChildren().addAll(loadTxtButton, loadImageButton, solveButton, clearButton, saveTxtButton, saveImageButton);
        return controlsBox;
    }

    private void saveTxtFile() {
        String logContent = logArea.getText();
        if (logContent == null || logContent.trim().isEmpty()) {
            showAlert("Error", "Log kosong! Jalankan solver terlebih dahulu.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Simpan Log Hasil");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Text Files", "*.txt")
        );
        fileChooser.setInitialFileName("hasil_" + boardSize + "x" + boardSize + ".txt");

        File file = fileChooser.showSaveDialog(null);
        if (file != null) {
            try (FileWriter writer = new FileWriter(file)) {
                writer.write(logContent);
                statusLabel.setText("Status: Log disimpan ke " + file.getName());
                logArea.appendText("\nLog saved to: " + file.getName() + "\n");
            } catch (IOException e) {
                showAlert("Error", "Gagal menyimpan file: " + e.getMessage());
            }
        }
    }

    private void saveImageFile() {
        if (currentBoard == null || boardGrid.getChildren().isEmpty()) {
            showAlert("Error", "Tidak ada papan untuk disimpan!");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Simpan sebagai Gambar");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("PNG Image", "*.png"),
            new FileChooser.ExtensionFilter("JPEG Image", "*.jpg", "*.jpeg")
        );
        fileChooser.setInitialFileName("board_" + boardSize + "x" + boardSize + ".png");

        File file = fileChooser.showSaveDialog(null);
        if (file != null) {
            try {
                // Take snapshot of the board grid
                SnapshotParameters params = new SnapshotParameters();
                params.setFill(Color.web("#1E272E"));
                WritableImage snapshot = boardGrid.snapshot(params, null);
                
                // Convert to BufferedImage and save
                BufferedImage bufferedImage = SwingFXUtils.fromFXImage(snapshot, null);
                
                String format = file.getName().toLowerCase().endsWith(".png") ? "png" : "jpg";
                ImageIO.write(bufferedImage, format, file);
                
                statusLabel.setText("Status: Gambar disimpan ke " + file.getName());
                logArea.appendText("Image saved to: " + file.getName() + "\n");
            } catch (IOException e) {
                showAlert("Error", "Gagal menyimpan gambar: " + e.getMessage());
            }
        }
    }

    private void loadTxtFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Pilih File Test Case");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Text Files", "*.txt")
        );
        
        // Set initial directory
        File initialDir = new File("../test");
        if (initialDir.exists()) {
            fileChooser.setInitialDirectory(initialDir);
        }

        File file = fileChooser.showOpenDialog(null);
        if (file != null) {
            if (ioHandler.readFile(file.getAbsolutePath())) {
                currentBoard = ioHandler.getBoard();
                boardSize = ioHandler.getSize();
                displayBoard(currentBoard, null);
                statusLabel.setText("Status: Papan berhasil dimuat dari " + file.getName());
                logArea.appendText("Loaded: " + file.getName() + "\n");
                logArea.appendText("Ukuran papan: " + boardSize + "x" + boardSize + "\n\n");
                solveButton.setDisable(false);
            } else {
                showAlert("Error", ioHandler.getErrorMessage());
            }
        }
    }

    private void loadImageFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Pilih File Gambar");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.webp", "*.bmp")
        );

        File file = fileChooser.showOpenDialog(null);
        if (file != null) {
            try {
                // Open configuration dialog for manual grid adjustment
                ImageConfigDialog configDialog = new ImageConfigDialog(file);
                configDialog.showAndWait();
                
                // Get result from dialog
                currentBoard = configDialog.getResultBoard();
                if (currentBoard != null) {
                    boardSize = currentBoard.length;
                    displayBoard(currentBoard, null);
                    statusLabel.setText("Status: Papan berhasil dimuat dari gambar " + file.getName());
                    logArea.appendText("Loaded image: " + file.getName() + "\n");
                    logArea.appendText("Board size: " + boardSize + "x" + boardSize + "\n");
                    logArea.appendText("Detected regions: " + configDialog.getRegionCount() + "\n\n");
                    solveButton.setDisable(false);
                }
            } catch (Exception e) {
                showAlert("Error", "Gagal memproses gambar: " + e.getMessage());
            }
        }
    }

    private void displayBoard(char[][] board, int[] queens) {
        Platform.runLater(() -> {
            // Only rebuild grid if board size changed or first time
            if (cellPanes == null || cellPanes.length != boardSize) {
                initializeBoardGrid(board);
            }
            
            // Update queen positions only (no rebuild)
            updateQueenPositions(queens);
        });
    }
    
    /**
     * Initialize the board grid once - creates all cells and queen overlays
     */
    private void initializeBoardGrid(char[][] board) {
        boardGrid.getChildren().clear();
        
        currentCellSize = Math.max(35, Math.min(55, 450 / boardSize));
        cellPanes = new StackPane[boardSize][boardSize];
        queenCircles = new Circle[boardSize][boardSize];
        
        // Drop shadow for cells
        DropShadow cellShadow = new DropShadow();
        cellShadow.setRadius(3);
        cellShadow.setOffsetX(1);
        cellShadow.setOffsetY(1);
        cellShadow.setColor(Color.color(0, 0, 0, 0.3));
        
        for (int row = 0; row < boardSize; row++) {
            for (int col = 0; col < boardSize; col++) {
                StackPane cell = new StackPane();
                cell.setPrefSize(currentCellSize, currentCellSize);
                cell.setMinSize(currentCellSize, currentCellSize);
                cell.setMaxSize(currentCellSize, currentCellSize);
                
                // Modern rounded rectangle for cell
                Rectangle rect = new Rectangle(currentCellSize - 3, currentCellSize - 3);
                rect.setArcWidth(8);
                rect.setArcHeight(8);
                Color cellColor = getColorForRegion(board[row][col]);
                rect.setFill(cellColor);
                rect.setStroke(cellColor.darker());
                rect.setStrokeWidth(1.5);
                rect.setEffect(cellShadow);
                
                cell.getChildren().add(rect);
                
                // Pre-create queen circle (hidden by default)
                Circle queenCircle = createQueenCircle();
                queenCircle.setVisible(false);
                cell.getChildren().add(queenCircle);
                
                cellPanes[row][col] = cell;
                queenCircles[row][col] = queenCircle;
                
                boardGrid.add(cell, col, row);
            }
        }
    }
    
    /**
     * Create a modern-looking queen piece using shapes
     */
    private Circle createQueenCircle() {
        int queenSize = (int) (currentCellSize * 0.55);
        
        // Gradient fill for 3D effect
        RadialGradient gradient = new RadialGradient(
            0, 0, 0.3, 0.3, 0.8, true, CycleMethod.NO_CYCLE,
            new Stop(0, Color.web("#FF6B6B")),
            new Stop(0.7, Color.web("#C0392B")),
            new Stop(1, Color.web("#8B0000"))
        );
        
        Circle queen = new Circle(queenSize / 2.0);
        queen.setFill(gradient);
        queen.setStroke(Color.web("#2C3E50"));
        queen.setStrokeWidth(2);
        
        // Inner shadow for depth
        InnerShadow innerShadow = new InnerShadow();
        innerShadow.setRadius(5);
        innerShadow.setColor(Color.color(0, 0, 0, 0.4));
        
        // Drop shadow
        DropShadow dropShadow = new DropShadow();
        dropShadow.setRadius(4);
        dropShadow.setOffsetX(2);
        dropShadow.setOffsetY(2);
        dropShadow.setColor(Color.color(0, 0, 0, 0.5));
        dropShadow.setInput(innerShadow);
        
        queen.setEffect(dropShadow);
        
        return queen;
    }
    
    /**
     * Update only queen positions without rebuilding the grid
     */
    private void updateQueenPositions(int[] queens) {
        if (queenCircles == null) return;
        
        for (int row = 0; row < boardSize; row++) {
            for (int col = 0; col < boardSize; col++) {
                boolean hasQueen = queens != null && queens[col] == row;
                queenCircles[row][col].setVisible(hasQueen);
            }
        }
    }

    private Color getColorForRegion(char region) {
        // Map region characters to colors
        int index = Character.toUpperCase(region) - 'A';
        // Modern flat design colors with good contrast
        Color[] colors = {
            Color.web("#FF6B6B"),  // A - Coral Red
            Color.web("#74B9FF"),  // B - Light Blue
            Color.web("#55EFC4"),  // C - Mint Green
            Color.web("#FFEAA7"),  // D - Light Yellow
            Color.web("#DDA0DD"),  // E - Plum
            Color.web("#FAB1A0"),  // F - Peach
            Color.web("#81ECEC"),  // G - Cyan
            Color.web("#E8DAEF"),  // H - Lavender
            Color.web("#FDCB6E"),  // I - Orange Yellow
            Color.web("#00B894"),  // J - Green
            Color.web("#FD79A8"),  // K - Pink
            Color.web("#F8E71C"),  // L - Yellow
            Color.web("#B2BEC3"),  // M - Gray
            Color.web("#E17055"),  // N - Orange
            Color.web("#A29BFE"),  // O - Purple
            Color.web("#FF7675"),  // P - Red
            Color.web("#0984E3"),  // Q - Blue
            Color.web("#F39C12"),  // R - Gold
            Color.web("#9B59B6"),  // S - Violet
            Color.web("#1ABC9C"),  // T - Teal
            Color.web("#3498DB"),  // U - Sky Blue
            Color.web("#E74C3C"),  // V - Red
            Color.web("#2ECC71"),  // W - Emerald
            Color.web("#F1C40F"),  // X - Sunflower
            Color.web("#9980FA"),  // Y - Light Purple
            Color.web("#22A6B3")   // Z - Blue Green
        };
        
        if (index >= 0 && index < colors.length) {
            return colors[index];
        }
        return Color.WHITE;
    }

    private void startSolving() {
        if (currentBoard == null || isSolving) return;
        
        isSolving = true;
        solveButton.setDisable(true);
        loadTxtButton.setDisable(true);
        loadImageButton.setDisable(true);
        logArea.clear();
        logArea.appendText("Memulai pencarian solusi...\n\n");
        
        Thread solverThread = new Thread(() -> {
            BruteForce bf = new BruteForce(boardSize);
            
            // Set callback for live visualization
            bf.setVisualizationCallback((queens, casesChecked, progress) -> {
                Platform.runLater(() -> {
                    displayBoard(currentBoard, queens);
                    statusLabel.setText(String.format("Progress: %.1f%% | Kasus: %d", progress, casesChecked));
                });
                
                // Delay based on slider
                try {
                    Thread.sleep((long) speedSlider.getValue());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
            
            // Set callback for found solutions
            bf.setSolutionCallback((solution, solutionNum) -> {
                Platform.runLater(() -> {
                    logArea.appendText("=== Solusi ===\n");
                    logArea.appendText(solution + "\n");
                });
            });
            
            String result = bf.solve(currentBoard);
            
            Platform.runLater(() -> {
                isSolving = false;
                solveButton.setDisable(false);
                loadTxtButton.setDisable(false);
                loadImageButton.setDisable(false);
                statusLabel.setText("Status: Pencarian selesai!");
                statsLabel.setText("Waktu: " + bf.getSearchTimeMs() + " ms | Kasus ditinjau: " + bf.getCasesChecked());
                
                // Display first solution if exists
                if (bf.getSolutionCount() > 0) {
                    displayBoard(currentBoard, bf.getFirstSolutionQueens());
                } else {
                    logArea.appendText("\n=== Tidak Ada Solusi! ===\n");
                }
            });
        });
        
        solverThread.setDaemon(true);
        solverThread.start();
    }

    private void clearBoard() {
        boardGrid.getChildren().clear();
        logArea.clear();
        currentBoard = null;
        boardSize = 0;
        cellPanes = null;
        queenCircles = null;
        solveButton.setDisable(true);
        statusLabel.setText("Status: Menunggu input...");
        statsLabel.setText("");
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
