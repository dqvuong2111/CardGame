package ui.JavaFX;

import core.Game;
import core.ai.tienlenai.TienLenAI;
import core.ai.tienlenai.TienLenAIStrategy;
import core.ai.tienlenai.strategies.GreedyStrategy;
import core.ai.tienlenai.strategies.RandomStrategy;
import core.ai.tienlenai.strategies.SmartStrategy;
import core.games.tienlen.AbstractTienLenGame;
import core.games.tienlen.TienLenVariantRuleSet;
import core.games.tienlen.tienlenmienbac.TienLenMienBacRule;
import core.games.tienlen.tienlenmiennam.TienLenMienNamGame;
import core.games.tienlen.tienlenmiennam.TienLenMienNamRule;
import core.games.tienlen.tienlenplayer.TienLenPlayer;
import core.games.tienlen.tienlenmienbac.TienLenMienBacGame;
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
	private AbstractTienLenGame<?> currentGame;

	public enum GameVariant { // Đặt enum này ở nơi phù hợp, có thể là class riêng hoặc trong SceneManager
	    TIEN_LEN_MIEN_NAM("Tiến Lên Miền Nam"),
	    TIEN_LEN_MIEN_BAC("Tiến Lên Miền Bắc");

	    private final String displayName;
	    GameVariant(String displayName) {
	        this.displayName = displayName;
	    }
	    @Override public String toString() { return displayName; }
	}
	private GameVariant selectedGameVariant = GameVariant.TIEN_LEN_MIEN_NAM; // Mặc định

	// Có thể cần một ChoiceBox cho việc này trên UI
	private ChoiceBox<GameVariant> gameVariantChoiceBox_inCustomScene;
	
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
//	    menuLayout.setStyle("-fx-background-color: #e0f0ff;");
	    String imagePath = "/background/mainmenu.jpg";
	    
	    try {
	        String imageUrl = getClass().getResource(imagePath).toExternalForm();
	        if (imageUrl != null) {
	            menuLayout.setStyle(
	                "-fx-background-image: url('" + imageUrl + "'); " +
	                "-fx-background-repeat: no-repeat; " +      // Không lặp lại ảnh
	                "-fx-background-position: center center; " + // Căn ảnh ở giữa
	                "-fx-background-size: cover;"              // Phủ kín toàn bộ VBox, có thể cắt ảnh nếu tỉ lệ khác
	                                                            // Các lựa chọn khác cho -fx-background-size:
	                                                            //   contain: Hiển thị toàn bộ ảnh, có thể thừa khoảng trắng
	                                                            //   100% 100%: Kéo dãn ảnh vừa khít VBox (có thể méo ảnh)
	                                                            //   auto: Kích thước gốc của ảnh
	            );
	        } else {
	            System.err.println("Lỗi: Không tìm thấy file ảnh nền tại: " + imagePath);
	            menuLayout.setStyle("-fx-background-color: #D3D3D3;"); // Màu nền dự phòng
	        }
	    } catch (NullPointerException e) {
	        System.err.println("Lỗi NullPointerException khi lấy URL ảnh nền: " + imagePath + ". Ảnh có tồn tại không?");
	        menuLayout.setStyle("-fx-background-color: #D3D3D3;"); // Màu nền dự phòng
	    }

	 // Trong phương thức createMainMenuLayout()
	    Label titleLabel = new Label("TIẾN LÊN MIỀN NAM");

	    // Font đậm, có thể là Arial Bold như cũ hoặc Arial Black nếu muốn nổi bật hơn
	    titleLabel.setFont(Font.font("Arial", FontWeight.BLACK, 54)); // EXTRA_BOLD nếu muốn đậm hơn BOLD
	    // Hoặc titleLabel.setFont(Font.font("Impact", FontWeight.NORMAL, 58)); // Impact thường cần size lớn hơn

	    // Màu chữ Đen
	    titleLabel.setTextFill(javafx.scene.paint.Color.WHITE);

	    // Có thể thêm một viền mỏng hoặc bóng sáng nhẹ nếu muốn
	    // Ví dụ, viền trắng mỏng (stroke):
	    // titleLabel.setStyle("-fx-stroke: white; -fx-stroke-width: 1;");
	    // Hoặc bóng sáng:
	    javafx.scene.effect.DropShadow ds = new javafx.scene.effect.DropShadow();
	    ds.setOffsetY(2.0);
	    ds.setOffsetX(2.0);
	    ds.setColor(javafx.scene.paint.Color.rgb(255, 255, 200, 0.3)); // Bóng màu vàng nhạt rất mờ
	    titleLabel.setEffect(ds);
	 
	 
	    Button newGameButton = new Button("Bắt đầu");
	    styleMenuButton(newGameButton, "#FF8C00", "#FFA500");
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
	    // rootLayout.setStyle("-fx-background-color: linear-gradient(from 0% 0% to 100% 100%, #e0f2f7, #b3e0ff);");

	    String imagePathForCustomization = "/background/mainmenu.jpg"; // << THAY BẰNG TÊN FILE CỦA BẠN

	    try {
	        String imageUrl = getClass().getResource(imagePathForCustomization).toExternalForm();
	        if (imageUrl != null) {
	            rootLayout.setStyle(
	                "-fx-background-image: url('" + imageUrl + "'); " +
	                "-fx-background-repeat: no-repeat; " +
	                "-fx-background-position: center center; " +
	                "-fx-background-size: cover;" // Phủ kín
	            );
	        } else {
	            System.err.println("Lỗi: Không tìm thấy file ảnh nền cho Customization Scene: " + imagePathForCustomization);
	            rootLayout.setStyle("-fx-background-color: #ECEFF1;"); // Màu nền dự phòng
	        }
	    } catch (Exception e) { // Bắt NullPointerException nếu getResource là null hoặc lỗi khác
	        System.err.println("Ngoại lệ khi lấy URL ảnh nền Customization Scene: " + imagePathForCustomization + ". " + e.getMessage());
	        rootLayout.setStyle("-fx-background-color: #ECEFF1;"); // Màu nền dự phòng
	    }

	    
	    VBox mainPanel = new VBox(20);
	    mainPanel.setAlignment(Pos.CENTER_LEFT);
	    mainPanel.setPadding(new Insets(30, 40, 30, 40));
	    mainPanel.setStyle(
	        "-fx-background-color: rgba(255, 255, 255, 0.5);" + // << THAY ĐỔI Ở ĐÂY: Nền trắng với độ mờ 85%
	        "-fx-background-radius: 15;" +
	        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.12), 15, 0.3, 0, 0);" +
	        "-fx-border-color: rgba(204, 204, 204, 0.5);" + // Viền cũng có thể làm mờ đi một chút
	        "-fx-border-width: 1px;" +
	        "-fx-border-radius: 15;"
	    );
	    mainPanel.setMaxWidth(600); 

	    Label sceneTitleLabel = new Label("Tùy Chỉnh Ván Chơi");
	    sceneTitleLabel.setFont(Font.font("Arial", FontWeight.BLACK, 30));
	    sceneTitleLabel.setTextFill(javafx.scene.paint.Color.WHITE); // << MÀU TRẮNG
	    sceneTitleLabel.setMaxWidth(Double.MAX_VALUE);
	    sceneTitleLabel.setAlignment(Pos.CENTER);
	    javafx.scene.effect.DropShadow ds = new javafx.scene.effect.DropShadow();
	    ds.setOffsetY(1.0); // Bóng đổ xuống dưới một chút
	    ds.setOffsetX(1.0); // Bóng đổ sang phải một chút
	    ds.setColor(javafx.scene.paint.Color.rgb(0, 0, 0, 0.2));
	    sceneTitleLabel.setEffect(ds);
	    
	    VBox.setMargin(sceneTitleLabel, new Insets(0, 0, 20, 0));

	    Label fixedTotalDisplay = new Label("Tổng người chơi: " + FIXED_TOTAL_PLAYERS + " (cố định)");
	    // Cải tiến:
	    fixedTotalDisplay.setFont(Font.font("Arial", FontWeight.BOLD, 18)); // Có thể dùng BOLD thay vì BLACK nếu muốn nhẹ hơn title
	    fixedTotalDisplay.setTextFill(javafx.scene.paint.Color.WHITE);      
	    // Các thuộc tính khác giữ nguyên:
	     fixedTotalDisplay.setMaxWidth(Double.MAX_VALUE);
	     fixedTotalDisplay.setAlignment(Pos.CENTER);
	     VBox.setMargin(fixedTotalDisplay, new Insets(0, 0, 20, 0));
	     
	     
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
	    
	    Label gameVariantLabel = new Label("Chọn loại game:");
	    styleSettingsLabel(gameVariantLabel); // Dùng hàm style đã có

	    gameVariantChoiceBox_inCustomScene = new ChoiceBox<>();
	    gameVariantChoiceBox_inCustomScene.getItems().addAll(GameVariant.TIEN_LEN_MIEN_NAM, GameVariant.TIEN_LEN_MIEN_BAC);
	    gameVariantChoiceBox_inCustomScene.setValue(this.selectedGameVariant);
	    styleChoiceBox(gameVariantChoiceBox_inCustomScene); // Dùng hàm style đã có
	    gameVariantChoiceBox_inCustomScene.setOnAction(e -> {
	        this.selectedGameVariant = gameVariantChoiceBox_inCustomScene.getValue();
	        // Có thể cần cập nhật lại một số thứ khác trên UI nếu luật game ảnh hưởng đến các lựa chọn khác
	        // Ví dụ: updateDisplayedValuesOnCustomizationScene(); // Nếu có thông tin nào phụ thuộc loại game
	    });

	    settingsGrid.add(gameVariantLabel, 0, 3); // Thêm vào hàng tiếp theo trong GridPane
	    settingsGrid.add(gameVariantChoiceBox_inCustomScene, 1, 3, 2, 1); // Cho ChoiceBox chiếm 2 cột nếu cần

	    HBox bottomButtonBar = new HBox(20);
	    bottomButtonBar.setAlignment(Pos.CENTER);
	    VBox.setMargin(bottomButtonBar, new Insets(30, 0, 0, 0));

	    Button backToMainMenuBtn = new Button("Menu chính");
	    styleSelectionSceneButton(backToMainMenuBtn, "#7f8c8d", "#606f70", 180);
	    backToMainMenuBtn.setOnAction(e -> showMainMenu());

	    Button startGameCustomBtn = new Button("Bắt Đầu Chơi");
	    styleSelectionSceneButton(startGameCustomBtn, "#FF8C00", "#FFA500", 180);
	    startGameCustomBtn.setOnAction(e -> startGame());

	    bottomButtonBar.getChildren().addAll(backToMainMenuBtn, startGameCustomBtn);

	    mainPanel.getChildren().addAll(sceneTitleLabel, fixedTotalDisplay, settingsGrid, bottomButtonBar);
	    rootLayout.getChildren().add(mainPanel);

	    Scene scene = new Scene(rootLayout);
	    primaryStage.setScene(scene);
	    // Listener sceneProperty sẽ tự động gọi forceMaximize()
	    primaryStage.setTitle("Tiến Lên Miền Nam");

	    // CẬP NHẬT HIỂN THỊ CHO SCENE NÀY SAU KHI ĐÃ SET SCENE
	    updateDisplayedValuesOnCustomizationScene(); // <<<<< THÊM DÒNG NÀY
	}

	// Helper style cho các label hiển thị giá trị trên scene tùy chỉnh
	private void styleValueDisplayLabel(Label label) {
		 label.setFont(Font.font("Arial", FontWeight.BOLD, 18)); // Tăng size lên 18, giữ đậm
		 label.setTextFill(javafx.scene.paint.Color.WHITE);
	}

	// Helper style cho các nút "Thay đổi" nhỏ
	private void styleMiniButton(Button button) {
	    button.setFont(Font.font("Arial", FontWeight.BLACK, 14));
	    button.setPrefHeight(32);
	    button.setPrefWidth(120);
	    
	    // Hiện tại đang sử dụng màu nền trắng rất mờ và chữ trắng (cần điều chỉnh nếu muốn chữ nổi bật)
	    String baseColor = "rgba(255, 255, 255, 0.2)"; 
	    String hoverColor = "rgba(255, 255, 255, 0.4)"; 
	    String textFill = "white"; 

	    // Nếu muốn nút có nền tối để chữ trắng nổi bật hơn (ví dụ)
	    // baseColor = "rgba(0, 0, 0, 0.4)";
	    // hoverColor = "rgba(0, 0, 0, 0.6)";
	    // textFill = "white";

	    String baseStyle = String.format(
	        "-fx-background-color: %s; " +
	        "-fx-text-fill: %s; " +
	        "-fx-background-radius: 5; " + 
	        "-fx-border-radius: 5; " +
	        "-fx-border-color: rgba(255,255,255,0.5);" + // Viền trắng mờ
	        "-fx-border-width: 1px;" +
	        "-fx-padding: 5 15 5 15;",
	        baseColor, textFill
	    );
	    String hoverStyle = String.format(
	        "-fx-background-color: %s; " +
	        "-fx-text-fill: %s; " +
	        "-fx-background-radius: 5; " +
	        "-fx-border-radius: 5; " +
	        "-fx-border-color: rgba(255,255,255,0.8);" +
	        "-fx-border-width: 1px;" +
	        "-fx-padding: 5 15 5 15;",
	        hoverColor, textFill
	    );

	    button.setStyle(baseStyle);
	    button.setOnMouseEntered(e -> button.setStyle(hoverStyle));
	    button.setOnMouseExited(e -> button.setStyle(baseStyle));
	    button.setCursor(javafx.scene.Cursor.HAND);
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
	public void styleMenuButton(Button button, String baseColor, String hoverColor) {
	    button.setPrefWidth(300);
	    button.setPrefHeight(70);

	    // Nếu bạn muốn tất cả các nút menu có font chữ và màu chữ cố định (ví dụ, chữ trắng nét đậm)
	    // bạn có thể đặt Font ở đây thay vì chỉ định màu nền và hover.
	    // Ví dụ: Chữ trắng, nét đậm cho TẤT CẢ các nút gọi hàm này
	    button.setFont(Font.font("Arial", FontWeight.BOLD, 26)); // Giữ nguyên hoặc đổi sang FontWeight.BLACK nếu muốn đậm hơn
	    // button.setTextFill(Color.WHITE); // Đã có trong setStyle bên dưới rồi

	    String baseStyle = String.format(
	            "-fx-background-color: %s; -fx-text-fill: white; " +
	            "-fx-background-radius: 10; -fx-border-radius: 10; " + // Tăng bo góc
	            "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 8, 0.0, 0, 3);", // Thêm bóng đổ cho nút
	            baseColor);
	    String hoverStyle = String.format(
	            "-fx-background-color: %s; -fx-text-fill: white; " +
	            "-fx-background-radius: 10; -fx-border-radius: 10; " +
	            "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 10, 0.0, 0, 4);", // Bóng đổ đậm hơn khi hover
	            hoverColor);

	    button.setStyle(baseStyle);
	    button.setOnMouseEntered(e -> button.setStyle(hoverStyle));
	    button.setOnMouseExited(e -> button.setStyle(baseStyle));
	    button.setCursor(javafx.scene.Cursor.HAND); // Thêm con trỏ tay
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
		// rootPane.setStyle("-fx-background-color: #f4f6f7;");
		
		 // --- THÊM ẢNH NỀN CHO ROOTPANE ---
	    String imagePathForSelectHumans = "/background/mainmenu.jpg"; // << THAY BẰNG TÊN FILE VÀ ĐƯỜNG DẪN CỦA BẠN
	    try {
	        String imageUrl = getClass().getResource(imagePathForSelectHumans).toExternalForm();
	        if (imageUrl != null) {
	            rootPane.setStyle(
	                "-fx-background-image: url('" + imageUrl + "'); " +
	                "-fx-background-repeat: no-repeat; " +
	                "-fx-background-position: center center; " +
	                "-fx-background-size: cover;" // Phủ kín
	            );
	        } else {
	            System.err.println("Lỗi: Không tìm thấy file ảnh nền cho Select Humans Scene: " + imagePathForSelectHumans);
	            rootPane.setStyle("-fx-background-color: #ECEFF1;"); // Màu nền dự phòng
	        }
	    } catch (Exception e) {
	        System.err.println("Ngoại lệ khi lấy URL ảnh nền Select Humans Scene: " + imagePathForSelectHumans + ". " + e.getMessage());
	        rootPane.setStyle("-fx-background-color: #ECEFF1;"); // Màu nền dự phòng
	    }

	    Label title = new Label("Chọn Số Người Chơi Thật");
	    title.setFont(Font.font("Arial", FontWeight.BLACK, 30)); // Font đậm hơn, màu sắc tùy chỉnh cho phù hợp nền
	    title.setTextFill(javafx.scene.paint.Color.WHITE); // Ví dụ chữ trắng nếu nền tối
	    // Thêm hiệu ứng đổ bóng cho chữ nếu cần để dễ đọc hơn trên nền ảnh
	    javafx.scene.effect.DropShadow dsText = new javafx.scene.effect.DropShadow();
	    dsText.setRadius(3);
	    dsText.setOffsetX(1);
	    dsText.setOffsetY(1);
	    dsText.setColor(javafx.scene.paint.Color.rgb(0, 0, 0, 0.7));
	    title.setEffect(dsText);
	    
		// -- Khu vực hiển thị và thay đổi số lượng --
	    VBox contentPanel = new VBox(20); // Panel chứa các control chính
	    contentPanel.setAlignment(Pos.CENTER);
	    contentPanel.setPadding(new Insets(20));
	    contentPanel.setStyle("-fx-background-color: rgba(0, 0, 0, 0.3); -fx-background-radius: 10;"); // Nền đen mờ ví dụ

	    HBox selectorBox = new HBox(20);
	    selectorBox.setAlignment(Pos.CENTER);

	    Button decrementButton = new Button("-");
	    decrementButton.setFont(Font.font("Arial", FontWeight.BOLD, 24));
	    decrementButton.setPrefSize(60, 60);
	    styleSubSceneNavButton(decrementButton, "#bdc3c7", "#95a5a6");

	    largeHumanCountDisplay_InSubScene = new Label(String.valueOf(this.tempSelectedHumanPlayers));
	    largeHumanCountDisplay_InSubScene.setFont(Font.font("Arial", FontWeight.BOLD, 72));
	    largeHumanCountDisplay_InSubScene.setTextFill(javafx.scene.paint.Color.WHITE); // Chữ trắng
	    largeHumanCountDisplay_InSubScene.setEffect(dsText); // Áp dụng bóng cho dễ đọc
	    largeHumanCountDisplay_InSubScene.setMinWidth(100);
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
		    styleSubSceneNavButton(confirmButton, "#FF8C00", "#FFA500");
		confirmButton.setOnAction(e -> {
			this.numberOfHumanPlayers = this.tempSelectedHumanPlayers; // Cập nhật giá trị chính
			// this.numberOfAIPlayers = FIXED_TOTAL_PLAYERS - this.numberOfHumanPlayers; //
			// Sẽ được tính lại ở scene kia
			showPlayerCustomizationScene(); // Quay lại scene tùy chỉnh (nó sẽ tự cập nhật label AI)
		});
		VBox.setMargin(confirmButton, new Insets(20, 0, 0, 0));

	    contentPanel.getChildren().addAll(selectorBox, confirmButton); // Thêm vào contentPanel
	    rootPane.getChildren().addAll(title, contentPanel); // Thêm title và contentPanel vào rootPane

	    Scene scene = new Scene(rootPane);
	    primaryStage.setScene(scene);
	    // forceMaximize(); // Sẽ được gọi bởi listener
	    primaryStage.setTitle("Chọn Số Người Chơi");
	}

	public void showSelectAIStrategyScene() {
	    // Đảm bảo tempSelectedAIStrategy được cập nhật từ giá trị hiện tại khi vào scene
	    this.tempSelectedAIStrategy = this.aiStrategy;

	    VBox rootPane = new VBox(25); // Giảm khoảng cách một chút nếu cần
	    rootPane.setAlignment(Pos.CENTER);
	    rootPane.setPadding(new Insets(40)); // Tăng padding
	    // rootPane.setStyle("-fx-background-color: #f8f9fa;"); // Màu nền sáng hơn một chút
	    
	    String imagePathForSelectAI = "/background/mainmenu.jpg"; // << THAY BẰNG TÊN FILE VÀ ĐƯỜNG DẪN CỦA BẠN
	    try {
	        String imageUrl = getClass().getResource(imagePathForSelectAI).toExternalForm();
	        if (imageUrl != null) {
	            rootPane.setStyle(
	                "-fx-background-image: url('" + imageUrl + "'); " +
	                "-fx-background-repeat: no-repeat; " +
	                "-fx-background-position: center center; " +
	                "-fx-background-size: cover;" // Phủ kín
	            );
	        } else {
	            System.err.println("Lỗi: Không tìm thấy file ảnh nền cho Select AI Scene: " + imagePathForSelectAI);
	            rootPane.setStyle("-fx-background-color: #ECEFF1;"); // Màu nền dự phòng
	        }
	    } catch (Exception e) {
	        System.err.println("Ngoại lệ khi lấy URL ảnh nền Select AI Scene: " + imagePathForSelectAI + ". " + e.getMessage());
	        rootPane.setStyle("-fx-background-color: #ECEFF1;"); // Màu nền dự phòng
	    }

	    Label title = new Label("Chọn Chiến Lược Cho AI");
	    title.setFont(Font.font("Arial", FontWeight.BOLD, 32));
	    title.setTextFill(javafx.scene.paint.Color.WHITE); // Ví dụ chữ trắng
	    // Thêm hiệu ứng đổ bóng cho chữ nếu cần
	    javafx.scene.effect.DropShadow dsText = new javafx.scene.effect.DropShadow();
	    dsText.setRadius(3); dsText.setOffsetX(1); dsText.setOffsetY(1);
	    dsText.setColor(javafx.scene.paint.Color.rgb(0,0,0,0.7));
	    title.setEffect(dsText);
	    VBox.setMargin(title, new Insets(0, 0, 30, 0));

	    // --- Container cho các Label chọn chiến lược ---
	    VBox strategyLabelsContainer = new VBox(18); // Khoảng cách giữa các label
	    strategyLabelsContainer.setAlignment(Pos.CENTER);
	    strategyLabelsContainer.setMaxWidth(350); // Giới hạn chiều rộng container

	    // --- Tạo các Label chọn chiến lược ---
	    Label smartLabel = new Label("Thông Minh (Smart)");
	    Label greedyLabel = new Label("Tham Lam (Greedy)");
	    Label randomLabel = new Label("Ngẫu Nhiên (Random)");

	    // Lưu trữ các label và kiểu chiến lược tương ứng để dễ quản lý
	    // Cách 1: Dùng Map (nếu cần truy cập label bằng enum)
	    // Map<TienLenAI.StrategyType, Label> strategyLabelMap = new HashMap<>();
	    // strategyLabelMap.put(TienLenAI.StrategyType.SMART, smartLabel);
	    // strategyLabelMap.put(TienLenAI.StrategyType.GREEDY, greedyLabel);
	    // strategyLabelMap.put(TienLenAI.StrategyType.RANDOM, randomLabel);

	    // Cách 2: Dùng List và UserData (khuyến nghị)
	    List<Label> allStrategyLabels = List.of(smartLabel, greedyLabel, randomLabel);
	    smartLabel.setUserData(TienLenAI.StrategyType.SMART);
	    greedyLabel.setUserData(TienLenAI.StrategyType.GREEDY);
	    randomLabel.setUserData(TienLenAI.StrategyType.RANDOM);

	    // --- Định nghĩa Styles ---
	    // Có thể chuyển các style này vào file CSS riêng nếu muốn
	    final String normalStyle = "-fx-font-family: 'Arial'; -fx-font-size: 22px; " +
                "-fx-padding: 12px 25px; -fx-border-color: #FFD700; -fx-border-width: 1px; " + // Viền vàng mỏng
                "-fx-border-radius: 8; -fx-background-radius: 8; -fx-background-color: #2C3E50; " + // Nền xám than/xanh navy đậm
                "-fx-text-fill: white; " + // Chữ màu trắng
                "-fx-alignment: center; -fx-cursor: hand; -fx-pref-width: 320px;"; // Tăng chiều rộng nếu cần

	    final String selectedStyle = "-fx-font-family: 'Arial'; -fx-font-size: 24px; " +
                  "-fx-padding: 12px 25px; -fx-border-color: #FFFFFF; -fx-border-width: 2.5px; " + // Viền trắng nổi bật
                  "-fx-border-radius: 8; -fx-background-radius: 8; -fx-background-color: #B8860B; " + // Nền vàng đồng (DarkGoldenrod) khi được chọn
                  "-fx-text-fill: white; " + // Chữ màu trắng
                  "-fx-font-weight: bold; -fx-alignment: center; -fx-cursor: hand; -fx-pref-width: 320px;" +
                  "-fx-effect: dropshadow(gaussian, #FFD700, 15, 0.4, 0, 0);"; // Bóng đổ màu vàng gold


	    // --- Hàm cập nhật style cho các Label ---
	    Runnable updateLabelStyles = () -> {
	        for (Label label : allStrategyLabels) {
	            TienLenAI.StrategyType labelStrategy = (TienLenAI.StrategyType) label.getUserData();
	            if (labelStrategy == this.tempSelectedAIStrategy) {
	                label.setStyle(selectedStyle);
	            } else {
	                label.setStyle(normalStyle);
	            }
	        }
	    };

	    // --- Gán sự kiện Click và UserData cho từng Label ---
	    for (Label label : allStrategyLabels) {
	        label.setOnMouseClicked(event -> {
	            this.tempSelectedAIStrategy = (TienLenAI.StrategyType) label.getUserData();
	            updateLabelStyles.run(); // Cập nhật style của tất cả các label
	        });
	    }
	    
	    // Áp dụng style ban đầu dựa trên tempSelectedAIStrategy
	    updateLabelStyles.run();

	    strategyLabelsContainer.getChildren().addAll(smartLabel, greedyLabel, randomLabel);

	    // --- Nút Xác Nhận ---
	    Button confirmButton = new Button("Xác Nhận");
	    styleSubSceneNavButton(confirmButton, "#FF8C00", "#FFA500");
	    confirmButton.setPrefWidth(200); // Đồng bộ chiều rộng
	    VBox.setMargin(confirmButton, new Insets(35, 0, 0, 0)); // Tăng margin trên

	    confirmButton.setOnAction(e -> {
	        this.aiStrategy = this.tempSelectedAIStrategy; // Cập nhật chiến lược AI chính thức
	        showPlayerCustomizationScene(); // Quay lại scene tùy chỉnh (hub-scene)
	    });

	    rootPane.getChildren().addAll(title, strategyLabelsContainer, confirmButton);

	    Scene scene = new Scene(rootPane);
	    primaryStage.setScene(scene);
	    // forceMaximize() sẽ được gọi bởi sceneProperty listener nếu bạn đã thiết lập
	    primaryStage.setTitle("Chọn Chiến Lược Cho AI");
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

		Label fixedTotalPlayersDisplayLabel = new Label("Tổng người chơi: " + FIXED_TOTAL_PLAYERS + " (cố định)");
		fixedTotalPlayersDisplayLabel.setFont(Font.font("Arial", FontWeight.BLACK, 17)); // Font Arial, rất đậm, size 17
		fixedTotalPlayersDisplayLabel.setTextFill(javafx.scene.paint.Color.WHITE);      // Chữ màu trắng

	    // Thêm hiệu ứng đổ bóng để chữ dễ đọc hơn trên nền phức tạp
	    javafx.scene.effect.DropShadow dsFixed = new javafx.scene.effect.DropShadow();
	    dsFixed.setRadius(3);
	    dsFixed.setOffsetX(1.0);
	    dsFixed.setOffsetY(1.0);
	    dsFixed.setColor(javafx.scene.paint.Color.rgb(0, 0, 0, 0.75)); // Bóng đen mờ
	    fixedTotalPlayersDisplayLabel.setEffect(dsFixed);

	    // Các thuộc tính khác giữ nguyên
	    fixedTotalPlayersDisplayLabel.setMaxWidth(Double.MAX_VALUE);
	    fixedTotalPlayersDisplayLabel.setAlignment(Pos.CENTER);
	    VBox.setMargin(fixedTotalPlayersDisplayLabel, new Insets(0, 0, 20, 0));

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
		aiPlayersDisplayLabel.setTextFill(javafx.scene.paint.Color.web("#FFFFFF")); // Màu khác cho số AI
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
		label.setFont(Font.font("Arial", FontWeight.BOLD, 17)); // Giữ đậm và size 17
	    label.setTextFill(javafx.scene.paint.Color.WHITE);
	}

	// Hàm helper để style ChoiceBox
	private void styleChoiceBox(ChoiceBox<?> choiceBox) {
		choiceBox.setPrefWidth(200); // Chiều rộng đồng nhất
		choiceBox.setStyle("-fx-font-size: 14px; " + "-fx-background-color: #f8f9fa; " + // Màu nền sáng hơn
				"-fx-border-color: #ced4da; " + "-fx-border-radius: 5; " + "-fx-background-radius: 5;"
				+ "-fx-mark-color: #FFFFFF;" // Màu mũi tên dropdown
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
	    stopCurrentGame(); // Dừng game hiện tại (nếu có) để dọn dẹp

	    // In ra các lựa chọn hiện tại để debug
	    System.out.println("Bắt đầu startGame():");
	    System.out.println("  - Loại game đã chọn: " + selectedGameVariant);
	    System.out.println("  - Số người chơi thật: " + numberOfHumanPlayers);
	    System.out.println("  - Chiến lược AI: " + aiStrategy);

	    // 1. Tính toán lại số lượng AI Players một cách chắc chắn dựa trên numberOfHumanPlayers
	    // Mặc dù việc này có thể đã được thực hiện ở những nơi khác, việc tính lại ở đây đảm bảo
	    // giá trị được sử dụng để tạo game là mới nhất.
	    this.numberOfAIPlayers = FIXED_TOTAL_PLAYERS - this.numberOfHumanPlayers;
	    if (this.numberOfAIPlayers < 0) {
	        this.numberOfAIPlayers = 0; // Đảm bảo không có số AI âm
	    }
	    System.out.println("  - Số người chơi AI (đã tính toán): " + this.numberOfAIPlayers);

	    // 2. Tạo danh sách người chơi (players)
	    List<TienLenPlayer> players = new ArrayList<>();

	    // Thêm người chơi Human
	    for (int i = 0; i < this.numberOfHumanPlayers; i++) {
	        players.add(new TienLenPlayer("Người " + (i + 1), false));
	    }

	    // 3. Tạo RuleSet phù hợp cho ván game này (chỉ một lần)
	    TienLenVariantRuleSet ruleSetForThisGame;
	    if (selectedGameVariant == GameVariant.TIEN_LEN_MIEN_BAC) {
	        ruleSetForThisGame = new TienLenMienBacRule();
	    } else { // Mặc định hoặc TIEN_LEN_MIEN_NAM
	        ruleSetForThisGame = new TienLenMienNamRule();
	    }
	    System.out.println("  - RuleSet được sử dụng: " + ruleSetForThisGame.getClass().getSimpleName());

	    // Thêm người chơi AI, truyền RuleSet đã tạo ở trên cho chúng
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
	            default: // Mặc định là SMART
	                strategyImplementation = new SmartStrategy();
	                break;
	        }
	        // Quan trọng: Truyền đúng ruleSetForThisGame vào constructor của TienLenAI
	        players.add(
	            new TienLenAI("AI " + (this.numberOfHumanPlayers + i + 1), strategyImplementation, ruleSetForThisGame)
	        );
	    }
	    System.out.println("  - Tổng số người chơi thực tế trong danh sách: " + players.size());

	    // 4. Khởi tạo đối tượng game (currentGame) với danh sách người chơi và RuleSet đã tạo
	    if (selectedGameVariant == GameVariant.TIEN_LEN_MIEN_BAC) {
	        // Ép kiểu ruleSetForThisGame về TienLenMienBacRule nếu constructor của TienLenMienBacGame yêu cầu
	        currentGame = new TienLenMienBacGame(players, (TienLenMienBacRule) ruleSetForThisGame);
	    } else {
	        // Ép kiểu ruleSetForThisGame về TienLenMienNamRule nếu constructor của TienLenMienNamGame yêu cầu
	        currentGame = new TienLenMienNamGame(players, (TienLenMienNamRule) ruleSetForThisGame);
	    }
	    System.out.println("  - Đã tạo currentGame: " + currentGame.getName());


	    // 5. Khởi tạo giao diện chơi game (GraphicUIJavaFX)
	    // GraphicUIJavaFX được thiết kế để dùng chung, nó nhận vào AbstractTienLenGame<?>
	    // và SceneManager instance.
	    gameGUI = new GraphicUIJavaFX(currentGame, primaryStage, this);

	    // 6. Thiết lập tiêu đề cửa sổ và các hành động chuẩn bị cho game
	    primaryStage.setTitle(currentGame.getName() + " - Đang Chơi");
	    // Listener sceneProperty trong constructor của SceneManager sẽ tự động gọi forceMaximize()

	    // Đăng ký GraphicUIJavaFX làm listener cho các sự kiện game
	    // Dòng game.addGameEventListener(gameGUI) đã có trong constructor của CardGameGUIJavaFX,
	    // và GraphicUIJavaFX kế thừa từ CardGameGUIJavaFX, nên không cần gọi lại ở đây.
	    // Bạn có thể kiểm tra lại constructor của CardGameGUIJavaFX để chắc chắn.

	    // 7. Bắt đầu các bước của ván bài
	    currentGame.dealCards(); // Chia bài
	    if (gameGUI != null) {
	       gameGUI.updateGameState(); // Cập nhật UI lần đầu sau khi chia bài để hiển thị tay bài, v.v.
	    }
	    currentGame.setGeneralGameState(Game.GeneralGameState.RUNNING); // Đặt trạng thái game là đang chạy
	    currentGame.startGameLoop(); // Bắt đầu vòng lặp chính của game (trong một thread riêng)
	    
	    System.out.println("startGame() hoàn tất, game loop đã bắt đầu.");
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