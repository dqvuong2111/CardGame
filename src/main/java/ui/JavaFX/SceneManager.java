package ui.JavaFX;

import core.Game;
import core.ai.tienlenai.TienLenAI;
import core.ai.tienlenai.TienLenAIStrategy;
import core.ai.tienlenai.strategies.GreedyStrategy;
import core.ai.tienlenai.strategies.RandomStrategy;
import core.ai.tienlenai.strategies.SmartStrategy;
import core.games.tienlen.tienlenmiennam.TienLenMienNamGame;
import core.games.tienlen.tienlenmiennam.TienLenMienNamRule;
import core.games.tienlen.tienlenplayer.TienLenPlayer;
import javafx.application.Platform;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane; // Thêm import này
import javafx.scene.layout.HBox; // Thêm import này
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.ChoiceBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SceneManager {
	private Stage primaryStage;
	private GraphicUIJavaFX gameGUI;
	private TienLenMienNamGame currentGame;

	private static final int FIXED_TOTAL_PLAYERS = 4; // Tổng số người chơi cố định
	private int numberOfHumanPlayers = 1; // Số người chơi thật, mặc định là 1
	private int numberOfAIPlayers = FIXED_TOTAL_PLAYERS - numberOfHumanPlayers; // Số AI, tự động tính

	// private Label totalPlayersLabel; // Không cần nữa
	// private ChoiceBox<Integer> totalPlayersChoiceBox; // Không cần nữa

	private Label humanPlayersLabel; // Sẽ đổi tên thành "Số người chơi thật"
	private ChoiceBox<Integer> numberOfHumansChoiceBox; // Đổi tên từ humanPlayersChoiceBox

	private Label aiPlayersDisplayLabel; // Label để hiển thị số AI (không phải để chọn)
	// private ChoiceBox<Integer> aiPlayersChoiceBox; // Không cần nữa

	private ChoiceBox<TienLenAI.StrategyType> aiStrategyChoiceBox; // Giữ nguyên
	private TienLenAI.StrategyType aiStrategy = TienLenAI.StrategyType.SMART; // Giá trị mặc định cho chiến lược AI

	// Labels để hiển thị giá trị trên playerCustomizationScene
	private Label currentHumanPlayersDisplay;
	private Label currentAIPlayersDisplay;
	private Label currentAIStrategyDisplay;

	// Biến tạm để lưu trữ lựa chọn trong các scene con (khi người dùng chưa "Xác
	// nhận")
	private int tempSelectedHumanPlayers;
	private TienLenAI.StrategyType tempSelectedAIStrategy;

	// Controls cho các scene con (có thể khai báo cục bộ trong từng phương thức
	// scene)
	// Nhưng nếu muốn style phức tạp hoặc truy cập nhiều lần, có thể làm biến thành
	// viên
	private Label largeHumanCountDisplay_InSubScene;
	private Label largeAIStrategyDisplay_InSubScene;

	public SceneManager(Stage primaryStage) {
	    this.primaryStage = primaryStage;
	    // Khởi tạo các biến tạm và giá trị mặc định
	    this.tempSelectedHumanPlayers = this.numberOfHumanPlayers;
	    this.tempSelectedAIStrategy = this.aiStrategy;

	    this.primaryStage.setTitle("Tiến Lên Miền Nam");

	    // 1. THIẾT LẬP CÁC LISTENERS TRƯỚC
	    // Listener cho sceneProperty để tự động gọi forceMaximize khi scene thay đổi
	    this.primaryStage.sceneProperty().addListener((obs, oldScene, newScene) -> {
	        if (newScene != null) {
	            System.out.println("Listener: Scene đã thay đổi. Gọi forceMaximize().");
	            Platform.runLater(this::forceMaximize);
	        }
	    });

	    // Các listeners debug khác (maximizedProperty, widthProperty, heightProperty)
	    this.primaryStage.maximizedProperty().addListener((obs, oldVal, newVal) -> {
	        System.out.println(">>> primaryStage.maximizedProperty changed: " + oldVal + " -> " + newVal +
	                           " Current W=" + primaryStage.getWidth() + " H=" + primaryStage.getHeight());
	        if (!newVal && oldVal) {
	            // new Exception("DEBUG: STAGE UNMAXIMIZED! Stack trace:").printStackTrace();
	        }
	        Screen screen = Screen.getPrimary();
	        if (newVal && (Math.abs(primaryStage.getWidth() - screen.getVisualBounds().getWidth()) > 5 ||
	                       Math.abs(primaryStage.getHeight() - screen.getVisualBounds().getHeight()) > 5)) {
	            // new Exception("DEBUG: STAGE MAXIMIZED WITH WRONG DIMENSIONS! Stack trace:").printStackTrace();
	        }
	    });
	    this.primaryStage.widthProperty().addListener((obs, o, n) -> System.out.println(">>> primaryStage.width: " + n));
	    this.primaryStage.heightProperty().addListener((obs, o, n) -> System.out.println(">>> primaryStage.height: " + n));

	    // 2. CHUẨN BỊ SCENE ĐẦU TIÊN (MAIN MENU)
	    // Tách logic tạo layout của MainMenu ra một phương thức riêng để có thể gọi ở đây
	    VBox mainMenuLayout = createMainMenuLayout(); // Xem bước 2a
	    Scene firstScene = new Scene(mainMenuLayout);
	    this.primaryStage.setScene(firstScene); // Đặt scene đầu tiên

	    // 3. SET MAXIMIZED TRƯỚC KHI SHOW
	    this.primaryStage.setMaximized(true);
	    System.out.println("Constructor: Đã gọi setMaximized(true) TRƯỚC KHI show().");

	    // 4. HIỂN THỊ STAGE LẦN ĐẦU TIÊN
	    this.primaryStage.show();
	    System.out.println("Constructor: Đã gọi show(). Trạng thái: isMaximized=" + primaryStage.isMaximized() +
	                       ", W=" + primaryStage.getWidth() + ", H=" + primaryStage.getHeight());

	    // Listener sceneProperty có thể đã kích hoạt forceMaximize() cho scene đầu tiên.
	    // Để chắc chắn, bạn có thể gọi forceMaximize() một cách tường minh ở đây nếu muốn,
	    // nhưng lý tưởng nhất là listener đã xử lý.
	    // Platform.runLater(this::forceMaximize); // Cân nhắc nếu vẫn thấy "khựng"
	}
	
	private VBox createMainMenuLayout() {
	    VBox menuLayout = new VBox(30);
	    menuLayout.setAlignment(Pos.CENTER);
	    menuLayout.setPadding(new Insets(50));
	    menuLayout.setStyle("-fx-background-color: #e0f0ff;");

	    Label titleLabel = new Label("TIẾN LÊN MIỀN NAM");
	    titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 52));
	    titleLabel.setTextFill(javafx.scene.paint.Color.web("#1a237e"));

	    Button newGameButton = new Button("Tùy Chỉnh Ván Chơi");
	    styleMenuButton(newGameButton, "#2ecc71", "#27ae60");
	    // Quan trọng: các setOnAction giờ sẽ gọi các phương thức show...Scene tương ứng
	    newGameButton.setOnAction(e -> showPlayerCustomizationScene());

	    Button exitButton = new Button("Thoát Game");
	    styleMenuButton(exitButton, "#e74c3c", "#c0392b");
	    exitButton.setOnAction(e -> {
	        Platform.exit();
	        System.exit(0);
	    });

	    menuLayout.getChildren().addAll(titleLabel, newGameButton, exitButton);
	    return menuLayout;
	}
	
	public void showMainMenu() {
	    VBox menuLayout = createMainMenuLayout(); // Tạo lại layout
	    Scene menuScene = new Scene(menuLayout);
	    primaryStage.setScene(menuScene); // Listener sẽ tự động gọi forceMaximize
	    primaryStage.setTitle("Tiến Lên Miền Nam - Menu Chính");
	    // Không cần gọi forceMaximize() hay setMaximized(true) ở đây nữa
	}
	
	private void forceMaximize() {
	    System.out.println("forceMaximize() được gọi. Trạng thái ban đầu: isMaximized=" + primaryStage.isMaximized() +
	                       ", W=" + primaryStage.getWidth() + ", H=" + primaryStage.getHeight());

	    Screen screen = Screen.getPrimary();
	    Rectangle2D bounds = screen.getVisualBounds();

	    boolean flagIsMaximized = primaryStage.isMaximized();
	    boolean dimensionsAreCorrect = Math.abs(primaryStage.getWidth() - bounds.getWidth()) < 5 && // Cho phép sai số nhỏ
	                                   Math.abs(primaryStage.getHeight() - bounds.getHeight()) < 5;

	    if (flagIsMaximized && dimensionsAreCorrect) {
	        System.out.println("   Đã maximized và kích thước đúng. Không cần hành động.");
	        return;
	    }

	    // Nếu flag isMaximized=true nhưng kích thước sai
	    if (flagIsMaximized && !dimensionsAreCorrect) {
	        System.out.println("   isMaximized=true NHƯNG kích thước SAI. Thử đặt lại W/H thủ công...");
	        // Tạm thời tắt listener của maximizedProperty để tránh nó phản ứng với các thay đổi bên dưới nếu có
	        // (Cần cẩn thận với việc này, có thể không cần thiết)

	        primaryStage.setWidth(bounds.getWidth());
	        primaryStage.setHeight(bounds.getHeight());
	        primaryStage.setX(bounds.getMinX());
	        primaryStage.setY(bounds.getMinY());

	        // Kiểm tra lại ngay sau khi đặt thủ công
	        boolean manualSetWorked = Math.abs(primaryStage.getWidth() - bounds.getWidth()) < 5 &&
	                                  Math.abs(primaryStage.getHeight() - bounds.getHeight()) < 5;

	        System.out.println("   Sau khi đặt W/H thủ công: isMaximized=" + primaryStage.isMaximized() + // Flag có thể bị thay đổi
	                           ", W=" + primaryStage.getWidth() + ", H=" + primaryStage.getHeight() +
	                           ". Manual set worked: " + manualSetWorked);

	        if (manualSetWorked) {
	            // Nếu đặt thủ công đã đúng kích thước, thử đảm bảo flag isMaximized là true
	            // mà không cần toggle mạnh.
	            if (!primaryStage.isMaximized()) { // Nếu flag bị clear do set W/H
	                Platform.runLater(() -> { // Chạy ở pulse tiếp theo
	                    primaryStage.setMaximized(true); // Cố gắng đặt lại flag
	                    System.out.println("   Đặt lại isMaximized=true sau khi manual W/H thành công.");
	                });
	            }
	            System.out.println("   Đặt W/H thủ công có vẻ đã khắc phục.");
	            return; // Kết thúc, hy vọng nó mượt hơn
	        } else {
	            // Nếu đặt thủ công không ăn thua, phải dùng đến toggle
	            System.out.println("   Đặt W/H thủ công không hiệu quả. Buộc phải toggle maximized state...");
	            primaryStage.setMaximized(false);
	            Platform.runLater(() -> {
	                primaryStage.setMaximized(true);
	                System.out.println("   Đã toggle xong: isMaximized=" + primaryStage.isMaximized() +
	                                   ", W=" + primaryStage.getWidth() + ", H=" + primaryStage.getHeight());
	            });
	            return;
	        }
	    }

	    // Nếu flag isMaximized=false
	    if (!flagIsMaximized) {
	        System.out.println("   isMaximized=false. Đang đặt setMaximized(true)...");
	        primaryStage.setMaximized(true);
	        // Kiểm tra lại sau 1 pulse xem lệnh có thực sự hiệu quả về kích thước không
	        Platform.runLater(() -> {
	            boolean newDimsCorrect = primaryStage.isMaximized() &&
	                                     Math.abs(primaryStage.getWidth() - bounds.getWidth()) < 5 &&
	                                     Math.abs(primaryStage.getHeight() - bounds.getHeight()) < 5;
	            System.out.println("   Sau khi setMaximized(true) (từ false): isMaximized=" + primaryStage.isMaximized() +
	                               ", W=" + primaryStage.getWidth() + ", H=" + primaryStage.getHeight() +
	                               ". Dims correct: " + newDimsCorrect);
	            if (primaryStage.isMaximized() && !newDimsCorrect) {
	                 System.err.println("   CẢNH BÁO: setMaximized(true) không làm kích thước đúng! Có thể cần toggle.");
	                 // Lúc này, nếu vẫn sai, có thể phải gọi lại forceMaximize một lần nữa, 
	                 // hoặc chấp nhận rằng có vấn đề sâu hơn.
	                 // primaryStage.setMaximized(false);
	                 // Platform.runLater(() -> primaryStage.setMaximized(true));
	            }
	        });
	    }
	}

	public void showPlayerCustomizationScene() {
	    VBox rootLayout = new VBox(20);
	    rootLayout.setAlignment(Pos.CENTER);
	    rootLayout.setPadding(new Insets(20));
	    rootLayout.setStyle("-fx-background-color: linear-gradient(from 0% 0% to 100% 100%, #e0f2f7, #b3e0ff);");

	    VBox mainPanel = new VBox(20);
	    mainPanel.setAlignment(Pos.CENTER_LEFT);
	    mainPanel.setPadding(new Insets(30, 40, 30, 40));
	    mainPanel.setStyle("-fx-background-color: white;" + "-fx-background-radius: 15;"
	            + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.12), 15, 0.3, 0, 0);" + "-fx-border-color: #cccccc;"
	            + "-fx-border-width: 1px;" + "-fx-border-radius: 15;");
	    mainPanel.setMaxWidth(600);

	    Label sceneTitleLabel = new Label("Tùy Chỉnh Ván Chơi");
	    sceneTitleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 28));
	    sceneTitleLabel.setTextFill(javafx.scene.paint.Color.web("#333333"));
	    sceneTitleLabel.setMaxWidth(Double.MAX_VALUE);
	    sceneTitleLabel.setAlignment(Pos.CENTER);
	    VBox.setMargin(sceneTitleLabel, new Insets(0, 0, 20, 0));

	    Label fixedTotalDisplay = new Label("Tổng người chơi: " + FIXED_TOTAL_PLAYERS + " (cố định)");
	    fixedTotalDisplay.setFont(Font.font("Arial", FontWeight.NORMAL, 16));
	    fixedTotalDisplay.setTextFill(javafx.scene.paint.Color.web("#555555"));

	    GridPane settingsGrid = new GridPane();
	    settingsGrid.setHgap(15);
	    settingsGrid.setVgap(15);
	    ColumnConstraints labelCol = new ColumnConstraints();
	    labelCol.setPrefWidth(200);
	    labelCol.setHalignment(HPos.LEFT);
	    ColumnConstraints valueCol = new ColumnConstraints();
	    valueCol.setPrefWidth(100);
	    valueCol.setHalignment(HPos.LEFT);
	    ColumnConstraints buttonCol = new ColumnConstraints();
	    buttonCol.setPrefWidth(150);
	    buttonCol.setHalignment(HPos.RIGHT);
	    settingsGrid.getColumnConstraints().addAll(labelCol, valueCol, buttonCol);

	    Label humanTitleLabel = new Label("Người chơi thật:");
	    styleSettingsLabel(humanTitleLabel);
	    currentHumanPlayersDisplay = new Label(String.valueOf(this.numberOfHumanPlayers));
	    styleValueDisplayLabel(currentHumanPlayersDisplay);
	    Button changeHumansButton = new Button("Thay đổi");
	    styleMiniButton(changeHumansButton);
	    changeHumansButton.setOnAction(e -> showSelectNumberOfHumansScene());
	    settingsGrid.add(humanTitleLabel, 0, 0);
	    settingsGrid.add(currentHumanPlayersDisplay, 1, 0);
	    settingsGrid.add(changeHumansButton, 2, 0);

	    Label aiTitleLabel = new Label("Người chơi AI:");
	    styleSettingsLabel(aiTitleLabel);
	    currentAIPlayersDisplay = new Label(String.valueOf(this.numberOfAIPlayers));
	    styleValueDisplayLabel(currentAIPlayersDisplay);
	    settingsGrid.add(aiTitleLabel, 0, 1);
	    settingsGrid.add(currentAIPlayersDisplay, 1, 1);

	    Label strategyTitleLabel = new Label("Chiến lược AI:");
	    styleSettingsLabel(strategyTitleLabel);
	    currentAIStrategyDisplay = new Label(this.aiStrategy.toString());
	    styleValueDisplayLabel(currentAIStrategyDisplay);
	    Button changeStrategyButton = new Button("Thay đổi");
	    styleMiniButton(changeStrategyButton);
	    changeStrategyButton.setOnAction(e -> showSelectAIStrategyScene());
	    settingsGrid.add(strategyTitleLabel, 0, 2);
	    settingsGrid.add(currentAIStrategyDisplay, 1, 2);
	    settingsGrid.add(changeStrategyButton, 2, 2);

	    HBox bottomButtonBar = new HBox(20);
	    bottomButtonBar.setAlignment(Pos.CENTER);
	    VBox.setMargin(bottomButtonBar, new Insets(30, 0, 0, 0));

	    Button backToMainMenuBtn = new Button("Về Menu Chính");
	    styleSelectionSceneButton(backToMainMenuBtn, "#7f8c8d", "#606f70", 180);
	    backToMainMenuBtn.setOnAction(e -> showMainMenu());

	    Button startGameCustomBtn = new Button("Bắt Đầu Chơi");
	    styleSelectionSceneButton(startGameCustomBtn, "#27AE60", "#229954", 180);
	    startGameCustomBtn.setOnAction(e -> startGame());

	    bottomButtonBar.getChildren().addAll(backToMainMenuBtn, startGameCustomBtn);

	    mainPanel.getChildren().addAll(sceneTitleLabel, fixedTotalDisplay, settingsGrid, bottomButtonBar);
	    rootLayout.getChildren().add(mainPanel);

	    Scene scene = new Scene(rootLayout);
	    primaryStage.setScene(scene);
	    // Listener sceneProperty sẽ tự động gọi forceMaximize()
	    primaryStage.setTitle("Tùy Chỉnh Ván Chơi");

	    // CẬP NHẬT HIỂN THỊ CHO SCENE NÀY SAU KHI ĐÃ SET SCENE
	    updateDisplayedValuesOnCustomizationScene(); // <<<<< THÊM DÒNG NÀY
	}

	// Helper style cho các label hiển thị giá trị trên scene tùy chỉnh
	private void styleValueDisplayLabel(Label label) {
		label.setFont(Font.font("Arial", FontWeight.BOLD, 16));
		label.setTextFill(javafx.scene.paint.Color.web("#2980b9"));
	}

	// Helper style cho các nút "Thay đổi" nhỏ
	private void styleMiniButton(Button button) {
		button.setFont(Font.font("Arial", FontWeight.NORMAL, 14));
		button.setPrefHeight(30);
		button.setPrefWidth(120);
		// Thêm style nếu muốn
	}

	// Hàm này sẽ cập nhật các Label trên PlayerCustomizationScene
	private void updateDisplayedValuesOnCustomizationScene() {
		if (currentHumanPlayersDisplay != null) {
			currentHumanPlayersDisplay.setText(String.valueOf(this.numberOfHumanPlayers));
		}
		this.numberOfAIPlayers = FIXED_TOTAL_PLAYERS - this.numberOfHumanPlayers; // Tính lại AI
		if (this.numberOfAIPlayers < 0)
			this.numberOfAIPlayers = 0;

		if (currentAIPlayersDisplay != null) {
			currentAIPlayersDisplay.setText(String.valueOf(this.numberOfAIPlayers));
		}
		if (currentAIStrategyDisplay != null) {
			currentAIStrategyDisplay.setText(this.aiStrategy.toString());
		}
	}

// Hàm helper để style các nút trên Menu (tùy chọn)
	private void styleMenuButton(Button button, String baseColor, String hoverColor) {
		button.setPrefWidth(300);
		button.setPrefHeight(70);
		button.setFont(Font.font("Arial", FontWeight.BOLD, 26));
		String baseStyle = String.format(
				"-fx-background-color: %s; -fx-text-fill: white; -fx-background-radius: 8; -fx-border-radius: 8;",
				baseColor);
		String hoverStyle = String.format(
				"-fx-background-color: %s; -fx-text-fill: white; -fx-background-radius: 8; -fx-border-radius: 8;",
				hoverColor);
		button.setStyle(baseStyle);
		button.setOnMouseEntered(e -> button.setStyle(hoverStyle));
		button.setOnMouseExited(e -> button.setStyle(baseStyle));
	}

	private void updatePlayerCountsAndLabels() {
		// numberOfHumanPlayers đã được cập nhật bởi listener của
		// numberOfHumansChoiceBox

		// Tính toán số lượng AI
		this.numberOfAIPlayers = FIXED_TOTAL_PLAYERS - this.numberOfHumanPlayers;
		if (this.numberOfAIPlayers < 0) { // Đề phòng trường hợp numberOfHumanPlayers > FIXED_TOTAL_PLAYERS (dù
											// ChoiceBox đã giới hạn)
			this.numberOfAIPlayers = 0;
		}

		// Cập nhật các Label
		// humanPlayersLabel không cần cập nhật ở đây vì nó là label tĩnh "Chọn số người
		// chơi thật:"
		// Chỉ cần cập nhật label hiển thị số AI
		if (aiPlayersDisplayLabel != null) { // Kiểm tra null phòng trường hợp gọi trước khi khởi tạo xong
			aiPlayersDisplayLabel.setText("Số lượng AI (tự động): " + this.numberOfAIPlayers);
		}
	}

	public void showSelectNumberOfHumansScene() {
		this.tempSelectedHumanPlayers = this.numberOfHumanPlayers; // Reset biến tạm mỗi khi vào scene

		VBox rootPane = new VBox(30);
		rootPane.setAlignment(Pos.CENTER);
		rootPane.setPadding(new Insets(30));
		rootPane.setStyle("-fx-background-color: #f4f6f7;");

		Label title = new Label("Chọn Số Người Chơi Thật");
		title.setFont(Font.font("Arial", FontWeight.BOLD, 28));
		title.setTextFill(javafx.scene.paint.Color.web("#333"));

		// -- Khu vực hiển thị và thay đổi số lượng --
		HBox selectorBox = new HBox(20);
		selectorBox.setAlignment(Pos.CENTER);

		Button decrementButton = new Button("-");
		decrementButton.setFont(Font.font("Arial", FontWeight.BOLD, 24));
		decrementButton.setPrefSize(60, 60);
		styleSubSceneNavButton(decrementButton, "#bdc3c7", "#95a5a6");

		largeHumanCountDisplay_InSubScene = new Label(String.valueOf(this.tempSelectedHumanPlayers));
		largeHumanCountDisplay_InSubScene.setFont(Font.font("Arial", FontWeight.BOLD, 72)); // Label to
		largeHumanCountDisplay_InSubScene.setTextFill(javafx.scene.paint.Color.web("#2980b9"));
		largeHumanCountDisplay_InSubScene.setMinWidth(100); // Đảm bảo có không gian
		largeHumanCountDisplay_InSubScene.setAlignment(Pos.CENTER);

		Button incrementButton = new Button("+");
		incrementButton.setFont(Font.font("Arial", FontWeight.BOLD, 24));
		incrementButton.setPrefSize(60, 60);
		styleSubSceneNavButton(incrementButton, "#bdc3c7", "#95a5a6");

		// Logic cho nút +/-
		decrementButton.setOnAction(e -> {
			if (tempSelectedHumanPlayers > 1) {
				tempSelectedHumanPlayers--;
				largeHumanCountDisplay_InSubScene.setText(String.valueOf(tempSelectedHumanPlayers));
			}
		});
		incrementButton.setOnAction(e -> {
			if (tempSelectedHumanPlayers < FIXED_TOTAL_PLAYERS) { // Giới hạn max là tổng số người chơi
				tempSelectedHumanPlayers++;
				largeHumanCountDisplay_InSubScene.setText(String.valueOf(tempSelectedHumanPlayers));
			}
		});

		selectorBox.getChildren().addAll(decrementButton, largeHumanCountDisplay_InSubScene, incrementButton);

		// -- Nút xác nhận --
		Button confirmButton = new Button("Xác Nhận");
		styleSubSceneNavButton(confirmButton, "#2ecc71", "#27ae60");
		confirmButton.setOnAction(e -> {
			this.numberOfHumanPlayers = this.tempSelectedHumanPlayers; // Cập nhật giá trị chính
			// this.numberOfAIPlayers = FIXED_TOTAL_PLAYERS - this.numberOfHumanPlayers; //
			// Sẽ được tính lại ở scene kia
			showPlayerCustomizationScene(); // Quay lại scene tùy chỉnh (nó sẽ tự cập nhật label AI)
		});
		VBox.setMargin(confirmButton, new Insets(20, 0, 0, 0));

		rootPane.getChildren().addAll(title, selectorBox, confirmButton);
		Scene scene = new Scene(rootPane);
		primaryStage.setScene(scene);
		  forceMaximize();
		primaryStage.setTitle("Chọn Số Người Chơi");
	}

	public void showSelectAIStrategyScene() {
		this.tempSelectedAIStrategy = this.aiStrategy; // Reset biến tạm

		VBox rootPane = new VBox(30);
		rootPane.setAlignment(Pos.CENTER);
		rootPane.setPadding(new Insets(30));
		rootPane.setStyle("-fx-background-color: #f4f6f7;");

		Label title = new Label("Chọn Chiến Lược AI");
		title.setFont(Font.font("Arial", FontWeight.BOLD, 28));
		title.setTextFill(javafx.scene.paint.Color.web("#333"));

		largeAIStrategyDisplay_InSubScene = new Label(this.tempSelectedAIStrategy.toString());
		largeAIStrategyDisplay_InSubScene.setFont(Font.font("Arial", FontWeight.BOLD, 48)); // Label to
		largeAIStrategyDisplay_InSubScene.setTextFill(javafx.scene.paint.Color.web("#2980b9"));
		largeAIStrategyDisplay_InSubScene.setWrapText(true);
		largeAIStrategyDisplay_InSubScene.setAlignment(Pos.CENTER);
		VBox.setMargin(largeAIStrategyDisplay_InSubScene, new Insets(10, 0, 20, 0));

		ChoiceBox<TienLenAI.StrategyType> strategyChoiceBoxInSubScene = new ChoiceBox<>();
		strategyChoiceBoxInSubScene.getItems().addAll(TienLenAI.StrategyType.SMART, TienLenAI.StrategyType.GREEDY,
				TienLenAI.StrategyType.RANDOM);
		strategyChoiceBoxInSubScene.setValue(this.tempSelectedAIStrategy);
		styleChoiceBox(strategyChoiceBoxInSubScene); // Dùng lại helper style nếu có
		strategyChoiceBoxInSubScene.setPrefWidth(250);
		strategyChoiceBoxInSubScene.valueProperty().addListener((obs, oldVal, newVal) -> {
			if (newVal != null) {
				this.tempSelectedAIStrategy = newVal;
				largeAIStrategyDisplay_InSubScene.setText(this.tempSelectedAIStrategy.toString());
			}
		});

		Button confirmButton = new Button("Xác Nhận");
		styleSubSceneNavButton(confirmButton, "#2ecc71", "#27ae60");
		confirmButton.setOnAction(e -> {
			this.aiStrategy = this.tempSelectedAIStrategy; // Cập nhật giá trị chính
			showPlayerCustomizationScene(); // Quay lại scene tùy chỉnh
		});
		VBox.setMargin(confirmButton, new Insets(20, 0, 0, 0));

		rootPane.getChildren().addAll(title, largeAIStrategyDisplay_InSubScene, strategyChoiceBoxInSubScene,
				confirmButton);
		Scene scene = new Scene(rootPane);
		primaryStage.setScene(scene);
		  forceMaximize();
		primaryStage.setTitle("Chọn Chiến Lược AI");
	}

	// Helper cho các nút trong sub-scene (Xác nhận, +/-)
	private void styleSubSceneNavButton(Button button, String baseColor, String hoverColor) {
		// Kế thừa từ styleSelectionSceneButton hoặc tạo style riêng
		button.setPrefHeight(50);
		if (button.getText().equals("+") || button.getText().equals("-")) {
			button.setPrefWidth(60);
		} else {
			button.setPrefWidth(200); // Nút Xác nhận
		}
		button.setFont(Font.font("Arial", FontWeight.BOLD, 18));
		// (Copy phần còn lại của logic style từ styleSelectionSceneButton nếu cần)
		String baseStyle = String.format(
				"-fx-background-color: %s; -fx-text-fill: white; -fx-background-radius: 8; -fx-border-radius: 8; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0.0, 0, 1);",
				baseColor);
		String hoverStyle = String.format(
				"-fx-background-color: %s; -fx-text-fill: white; -fx-background-radius: 8; -fx-border-radius: 8; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 8, 0.0, 0, 2);",
				hoverColor);
		button.setStyle(baseStyle);
		button.setOnMouseEntered(e -> button.setStyle(hoverStyle));
		button.setOnMouseExited(e -> button.setStyle(baseStyle));
	}

	public void showPlayerSelectionScene() {
		// --- Root Layout ---
		VBox rootLayout = new VBox(20);
		rootLayout.setAlignment(Pos.CENTER);
		rootLayout.setPadding(new Insets(20));
		// Thêm một gradient nền nhẹ nhàng
		rootLayout.setStyle("-fx-background-color: linear-gradient(from 0% 0% to 100% 100%, #e0f2f7, #b3e0ff);");

		// --- Main Content Panel ---
		VBox mainPanel = new VBox(25); // Khoảng cách giữa các mục trong panel
		mainPanel.setAlignment(Pos.CENTER_LEFT); // Căn lề trái cho các mục trong panel
		mainPanel.setPadding(new Insets(35, 45, 35, 45)); // Padding bên trong panel
		mainPanel.setStyle("-fx-background-color: white;" + "-fx-background-radius: 20;" + // Bo góc nhiều hơn
				"-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.12), 15, 0.3, 0, 0);" + // Đổ bóng mềm hơn
				"-fx-border-color: #cccccc;" + "-fx-border-width: 1px;" + "-fx-border-radius: 20;");
		mainPanel.setMaxWidth(580); // Giới hạn chiều rộng của panel

		// --- Scene Title ---
		Label sceneTitleLabel = new Label("Tùy Chỉnh Ván Chơi"); // Có thể đổi tiêu đề
		sceneTitleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 30));
		sceneTitleLabel.setTextFill(javafx.scene.paint.Color.web("#333333"));
		VBox.setMargin(sceneTitleLabel, new Insets(0, 0, 25, 0)); // Canh giữa và thêm margin dưới
		sceneTitleLabel.setAlignment(Pos.CENTER);
		sceneTitleLabel.setMaxWidth(Double.MAX_VALUE);

		// --- Fixed Total Players Display ---
		Label fixedTotalPlayersDisplayLabel = new Label(
				"Tổng số người chơi trong ván: " + FIXED_TOTAL_PLAYERS + " (cố định)");
		fixedTotalPlayersDisplayLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 16));
		fixedTotalPlayersDisplayLabel.setTextFill(javafx.scene.paint.Color.web("#555555"));
		VBox.setMargin(fixedTotalPlayersDisplayLabel, new Insets(0, 0, 20, 0));
		fixedTotalPlayersDisplayLabel.setAlignment(Pos.CENTER);
		fixedTotalPlayersDisplayLabel.setMaxWidth(Double.MAX_VALUE);

		// --- GridPane for Settings ---
		GridPane settingsGrid = new GridPane();
		// settingsGrid.setAlignment(Pos.CENTER); // Sẽ căn giữa trong VBox mainPanel
		settingsGrid.setHgap(20); // Khoảng cách ngang giữa các cột
		settingsGrid.setVgap(18); // Khoảng cách dọc giữa các hàng

		// Thiết lập cột cho GridPane (2 cột, cột label và cột control)
		ColumnConstraints col1 = new ColumnConstraints();
		col1.setHalignment(javafx.geometry.HPos.RIGHT); // Căn phải cho label
		col1.setPrefWidth(220); // Độ rộng cố định cho cột label
		ColumnConstraints col2 = new ColumnConstraints();
		col2.setPrefWidth(200); // Độ rộng cho cột control
		settingsGrid.getColumnConstraints().addAll(col1, col2);

		// Human Players Selection
		Label humanPlayersSetupLabel = new Label("Số người chơi thật:"); // Label mới cho GridPane
		styleSettingsLabel(humanPlayersSetupLabel);

		numberOfHumansChoiceBox = new ChoiceBox<>();
		numberOfHumansChoiceBox.getItems().addAll(1, 2, 3, 4);
		numberOfHumansChoiceBox.setValue(this.numberOfHumanPlayers);
		styleChoiceBox(numberOfHumansChoiceBox);
		numberOfHumansChoiceBox.valueProperty().addListener((obs, oldVal, newVal) -> {
			if (newVal != null) {
				this.numberOfHumanPlayers = newVal;
				updatePlayerCountsAndLabels();
			}
		});
		settingsGrid.add(humanPlayersSetupLabel, 0, 0);
		settingsGrid.add(numberOfHumansChoiceBox, 1, 0);

		// AI Players Display
		Label aiCountTextLabel = new Label("Số lượng AI (tự động):");
		styleSettingsLabel(aiCountTextLabel);

		aiPlayersDisplayLabel = new Label(); // Label này chỉ hiển thị số, đã khai báo là biến thành viên
		aiPlayersDisplayLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16)); // Làm nổi bật số AI
		aiPlayersDisplayLabel.setTextFill(javafx.scene.paint.Color.web("#2980b9")); // Màu khác cho số AI
		settingsGrid.add(aiCountTextLabel, 0, 1);
		settingsGrid.add(aiPlayersDisplayLabel, 1, 1);

		// AI Strategy Selection
		Label aiStrategySetupLabel = new Label("Chiến lược AI:");
		styleSettingsLabel(aiStrategySetupLabel);

		aiStrategyChoiceBox = new ChoiceBox<>();
		aiStrategyChoiceBox.getItems().addAll(TienLenAI.StrategyType.SMART, TienLenAI.StrategyType.GREEDY,
				TienLenAI.StrategyType.RANDOM);
		aiStrategyChoiceBox.setValue(this.aiStrategy);
		styleChoiceBox(aiStrategyChoiceBox);
		aiStrategyChoiceBox.setOnAction(e -> this.aiStrategy = aiStrategyChoiceBox.getValue());
		settingsGrid.add(aiStrategySetupLabel, 0, 2);
		settingsGrid.add(aiStrategyChoiceBox, 1, 2);

		// --- Button Bar ---
		HBox buttonBar = new HBox(20);
		buttonBar.setAlignment(Pos.CENTER_RIGHT); // Căn phải cho cụm nút
		VBox.setMargin(buttonBar, new Insets(35, 0, 0, 0)); // Margin trên cho cụm nút

		Button backButton = new Button("Quay Lại"); // Text ngắn hơn
		styleSelectionSceneButton(backButton, "#95a5a6", "#7f8c8d", 150); // Nút màu xám
		backButton.setOnAction(e -> showMainMenu());

		Button startGameButton = new Button("Vào Chơi"); // Text ngắn hơn
		styleSelectionSceneButton(startGameButton, "#2ecc71", "#27ae60", 150); // Nút màu xanh lá
		startGameButton.setOnAction(e -> startGame());

		buttonBar.getChildren().addAll(backButton, startGameButton);

		// --- Assemble Main Panel ---
		mainPanel.getChildren().addAll(sceneTitleLabel, fixedTotalPlayersDisplayLabel, settingsGrid, buttonBar);

		// --- Assemble Root Layout ---
		rootLayout.getChildren().add(mainPanel);

		// --- Scene Creation ---
		Scene playerSelectionScene = new Scene(rootLayout);
		primaryStage.setScene(playerSelectionScene);
		  forceMaximize();
		primaryStage.setTitle("Tiến Lên Miền Nam - Chọn Chế Độ Chơi");

		updatePlayerCountsAndLabels(); // Cập nhật label số AI khi scene được hiển thị lần đầu
	}

	// Hàm helper để style Label trong GridPane
	private void styleSettingsLabel(Label label) {
		label.setFont(Font.font("Arial", FontWeight.SEMI_BOLD, 16));
		label.setTextFill(javafx.scene.paint.Color.web("#444444"));
	}

	// Hàm helper để style ChoiceBox
	private void styleChoiceBox(ChoiceBox<?> choiceBox) {
		choiceBox.setPrefWidth(200); // Chiều rộng đồng nhất
		choiceBox.setStyle("-fx-font-size: 14px; " + "-fx-background-color: #f8f9fa; " + // Màu nền sáng hơn
				"-fx-border-color: #ced4da; " + "-fx-border-radius: 5; " + "-fx-background-radius: 5;"
				+ "-fx-mark-color: #2980b9;" // Màu mũi tên dropdown
		);
	}

	// Hàm helper để style Button (có thể thay thế hàm styleMenuButton cũ nếu muốn)
	private void styleSelectionSceneButton(Button button, String baseColor, String hoverColor, double width) {
		button.setPrefWidth(width);
		button.setPrefHeight(45);
		button.setFont(Font.font("Arial", FontWeight.BOLD, 16));
		String baseStyle = String.format(
				"-fx-background-color: %s; -fx-text-fill: white; -fx-background-radius: 8; -fx-border-radius: 8; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0.0, 0, 1);",
				baseColor);
		String hoverStyle = String.format(
				"-fx-background-color: %s; -fx-text-fill: white; -fx-background-radius: 8; -fx-border-radius: 8; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 8, 0.0, 0, 2);",
				hoverColor);
		button.setStyle(baseStyle);
		button.setOnMouseEntered(e -> button.setStyle(hoverStyle));
		button.setOnMouseExited(e -> button.setStyle(baseStyle));
	}

	private void startGame() {
		stopCurrentGame();

		TienLenMienNamRule tienLenRule = new TienLenMienNamRule();
		List<TienLenPlayer> players = new ArrayList<>();

		// Add human players
		for (int i = 0; i < this.numberOfHumanPlayers; i++) {
			players.add(new TienLenPlayer("Người " + (i + 1), false)); // Đổi tên để phân biệt rõ hơn
		}

		// Add AI Players (sử dụng this.numberOfAIPlayers đã được tính)
		for (int i = 0; i < this.numberOfAIPlayers; i++) {
			TienLenAIStrategy strategyImplementation;
			switch (this.aiStrategy) {
			case RANDOM:
				strategyImplementation = new RandomStrategy();
				break;
			case GREEDY:
				strategyImplementation = new GreedyStrategy();
				break;
			case SMART:
			default:
				strategyImplementation = new SmartStrategy();
				break;
			}
			// Đặt tên AI dựa trên tổng số người đã có
			players.add(
					new TienLenAI("AI " + (this.numberOfHumanPlayers + i + 1), strategyImplementation, tienLenRule));
		}

		// Không cần vòng lặp while để thêm/bớt player nữa vì số lượng đã cố định và
		// tính toán chính xác
		// if (players.size() != FIXED_TOTAL_PLAYERS) {
		// System.err.println("Lỗi logic: Số lượng người chơi cuối cùng (" +
		// players.size() + ") không bằng " + FIXED_TOTAL_PLAYERS);
		// // Có thể thêm xử lý ở đây nếu muốn, ví dụ quay lại menu hoặc báo lỗi
		// return;
		// }

		currentGame = new TienLenMienNamGame(players, tienLenRule);

		// Quan trọng: Nếu GraphicUIJavaFX của bạn đang dùng FXML và Controller,
		// việc tạo mới gameGUI mỗi lần startGame có thể cần xem xét lại.
		// Nếu GraphicUIJavaFX là UI cố định, bạn chỉ cần truyền game mới vào controller
		// của nó.
		// Tuy nhiên, với code hiện tại của GraphicUIJavaFX (phiên bản programmatic),
		// việc tạo mới gameGUI có thể là cách bạn đang làm.
		gameGUI = new GraphicUIJavaFX(currentGame, primaryStage); // Giả sử GraphicUIJavaFX vẫn là phiên bản
																	// programmatic

		// primaryStage.setTitle("Tiến Lên Miền Nam - Đang chơi"); // Có thể đổi title
		// primaryStage.setMaximized(true); // Đã được set khi Scene thay đổi
		// primaryStage.show(); // Không cần nếu stage đã show và chỉ đổi scene

		// Khởi động game
		currentGame.dealCards();
		if (gameGUI != null) { // Luôn kiểm tra null
			gameGUI.updateGameState(); // Cập nhật UI trước khi game loop chạy
		}
		currentGame.setGeneralGameState(Game.GeneralGameState.RUNNING);
		currentGame.startGameLoop();
	}

	public void stopCurrentGame() {
		if (currentGame != null) {
			System.out.println("Yêu cầu dừng game loop hiện tại...");
			currentGame.removeGameEventListener(gameGUI);
			currentGame.stopGameLoop();
			try {
				if (currentGame.gameThread != null && currentGame.gameThread.isAlive()) {
					currentGame.gameThread.join(2000);
					if (currentGame.gameThread.isAlive()) {
						System.err.println("Game thread did not terminate gracefully. Interrupting.");
						currentGame.gameThread.interrupt();
					}
				}
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				System.err.println("Main thread interrupted while waiting for game thread to stop.");
			}
			currentGame = null;
			gameGUI = null;
		}
	}
}