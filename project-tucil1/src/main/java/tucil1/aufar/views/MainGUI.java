package tucil1.aufar.views;

import java.io.File;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import tucil1.aufar.controllers.IOHandler;
import tucil1.aufar.controllers.ImageProcessor;
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

    @Override
    public void start(Stage primaryStage) {
        ioHandler = new IOHandler();
        
        primaryStage.setTitle("Queens Game Solver - Tucil 1");

        // Main layout
        BorderPane mainLayout = new BorderPane();
        mainLayout.setPadding(new Insets(10));

        // Top: Controls
        HBox controlsBox = createControlsBox();
        mainLayout.setTop(controlsBox);

        // Center: Board visualization
        VBox centerBox = new VBox(10);
        centerBox.setAlignment(Pos.CENTER);
        
        Label boardLabel = new Label("Papan Permainan");
        boardLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        
        boardGrid = new GridPane();
        boardGrid.setAlignment(Pos.CENTER);
        boardGrid.setHgap(2);
        boardGrid.setVgap(2);
        boardGrid.setPadding(new Insets(10));
        boardGrid.setStyle("-fx-background-color: #333333; -fx-padding: 5;");
        
        ScrollPane boardScroll = new ScrollPane(boardGrid);
        boardScroll.setFitToWidth(true);
        boardScroll.setFitToHeight(true);
        boardScroll.setPrefViewportHeight(400);
        boardScroll.setPrefViewportWidth(400);
        
        centerBox.getChildren().addAll(boardLabel, boardScroll);
        mainLayout.setCenter(centerBox);

        // Right: Log area
        VBox logBox = new VBox(10);
        logBox.setPadding(new Insets(10));
        
        Label logLabel = new Label("Log Pencarian");
        logLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        
        logArea = new TextArea();
        logArea.setEditable(false);
        logArea.setPrefWidth(300);
        logArea.setPrefHeight(400);
        logArea.setFont(Font.font("Consolas", 12));
        
        logBox.getChildren().addAll(logLabel, logArea);
        mainLayout.setRight(logBox);

        // Bottom: Status and stats
        VBox bottomBox = new VBox(5);
        bottomBox.setPadding(new Insets(10));
        
        statusLabel = new Label("Status: Menunggu input...");
        statusLabel.setFont(Font.font("Arial", 12));
        
        statsLabel = new Label("");
        statsLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        
        // Speed control
        HBox speedBox = new HBox(10);
        speedBox.setAlignment(Pos.CENTER_LEFT);
        Label speedLabel = new Label("Kecepatan Visualisasi:");
        speedSlider = new Slider(1, 100, 50);
        speedSlider.setShowTickLabels(true);
        speedSlider.setShowTickMarks(true);
        speedSlider.setPrefWidth(200);
        Label speedValueLabel = new Label("50 ms");
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

        loadTxtButton = new Button("📂 Load TXT File");
        loadTxtButton.setStyle("-fx-font-size: 14px; -fx-padding: 10 20;");
        loadTxtButton.setOnAction(e -> loadTxtFile());

        loadImageButton = new Button("🖼️ Load Image");
        loadImageButton.setStyle("-fx-font-size: 14px; -fx-padding: 10 20;");
        loadImageButton.setOnAction(e -> loadImageFile());

        solveButton = new Button("▶️ Solve");
        solveButton.setStyle("-fx-font-size: 14px; -fx-padding: 10 20; -fx-background-color: #4CAF50; -fx-text-fill: white;");
        solveButton.setDisable(true);
        solveButton.setOnAction(e -> startSolving());

        Button clearButton = new Button("🗑️ Clear");
        clearButton.setStyle("-fx-font-size: 14px; -fx-padding: 10 20;");
        clearButton.setOnAction(e -> clearBoard());

        controlsBox.getChildren().addAll(loadTxtButton, loadImageButton, solveButton, clearButton);
        return controlsBox;
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
                ImageProcessor processor = new ImageProcessor();
                currentBoard = processor.processImage(file);
                boardSize = currentBoard.length;
                displayBoard(currentBoard, null);
                statusLabel.setText("Status: Papan berhasil dimuat dari gambar " + file.getName());
                logArea.appendText("Loaded image: " + file.getName() + "\n");
                logArea.appendText("Detected board size: " + boardSize + "x" + boardSize + "\n");
                logArea.appendText("Detected regions: " + processor.getRegionCount() + "\n\n");
                solveButton.setDisable(false);
            } catch (Exception e) {
                showAlert("Error", "Gagal memproses gambar: " + e.getMessage());
            }
        }
    }

    private void displayBoard(char[][] board, int[] queens) {
        Platform.runLater(() -> {
            boardGrid.getChildren().clear();
            
            int cellSize = Math.max(30, Math.min(50, 400 / boardSize));
            
            for (int row = 0; row < boardSize; row++) {
                for (int col = 0; col < boardSize; col++) {
                    StackPane cell = new StackPane();
                    cell.setPrefSize(cellSize, cellSize);
                    
                    Rectangle rect = new Rectangle(cellSize - 2, cellSize - 2);
                    Color cellColor = getColorForRegion(board[row][col]);
                    rect.setFill(cellColor);
                    rect.setStroke(Color.DARKGRAY);
                    rect.setStrokeWidth(1);
                    
                    cell.getChildren().add(rect);
                    
                    // Show queen if present
                    if (queens != null && queens[col] == row) {
                        Text queenText = new Text("♛");
                        queenText.setFont(Font.font("Arial", FontWeight.BOLD, cellSize - 10));
                        queenText.setFill(Color.BLACK);
                        cell.getChildren().add(queenText);
                    }
                    
                    boardGrid.add(cell, col, row);
                }
            }
        });
    }

    private Color getColorForRegion(char region) {
        // Map region characters to colors
        int index = Character.toUpperCase(region) - 'A';
        Color[] colors = {
            Color.CORAL,           // A
            Color.LIGHTBLUE,       // B
            Color.LIGHTGREEN,      // C
            Color.KHAKI,           // D
            Color.PLUM,            // E
            Color.LIGHTSALMON,     // F
            Color.LIGHTCYAN,       // G
            Color.LAVENDER,        // H
            Color.PEACHPUFF,       // I
            Color.PALEGREEN,       // J
            Color.LIGHTPINK,       // K
            Color.LIGHTYELLOW,     // L
            Color.LIGHTGRAY,       // M
            Color.BISQUE,          // N
            Color.HONEYDEW,        // O
            Color.MISTYROSE,       // P
            Color.AZURE,           // Q
            Color.LEMONCHIFFON,    // R
            Color.LAVENDERBLUSH,   // S
            Color.MINTCREAM,       // T
            Color.ALICEBLUE,       // U
            Color.SEASHELL,        // V
            Color.FLORALWHITE,     // W
            Color.GHOSTWHITE,      // X
            Color.OLDLACE,         // Y
            Color.LINEN            // Z
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
                    logArea.appendText("=== Solusi " + solutionNum + " ===\n");
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
                statsLabel.setText("Total solusi: " + bf.getSolutionCount() + 
                    " | Waktu: " + bf.getSearchTimeMs() + " ms | Kasus ditinjau: " + bf.getCasesChecked());
                
                // Display first solution if exists
                if (bf.getSolutionCount() > 0) {
                    displayBoard(currentBoard, bf.getFirstSolutionQueens());
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
