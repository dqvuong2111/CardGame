// Main.java
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import ui.JavaFX.SceneManager; // Import SceneManager

public class Main extends Application {

    private SceneManager sceneManager; // Sử dụng SceneManager để quản lý các scene

    public static volatile boolean isShuttingDown = false;

    @Override
    public void start(Stage primaryStage) {
        // Khởi tạo SceneManager
        sceneManager = new SceneManager(primaryStage);

        // Đặt xử lý khi đóng cửa sổ
        primaryStage.setOnCloseRequest(event -> {
            System.out.println("Window closing request received. Terminating application.");
            event.consume(); // Ngăn chặn việc đóng cửa sổ mặc định
            Main.isShuttingDown = true;

            // Yêu cầu SceneManager dừng game nếu có game đang chạy
            if (sceneManager != null) {
                sceneManager.stopCurrentGame(); // Thêm phương thức này vào SceneManager
            }

            Platform.exit(); // Thoát ứng dụng JavaFX
            System.exit(0); // Đảm bảo tất cả các thread đều dừng
        });

        // SceneManager sẽ tự động hiển thị Main Menu khi được khởi tạo
        primaryStage.show();
    }

    @Override
    public void stop() throws Exception {
        System.out.println("Main application stop method called.");
        // Đảm bảo game loop được yêu cầu dừng lại một lần nữa khi ứng dụng dừng
        if (sceneManager != null) {
            sceneManager.stopCurrentGame();
        }
        super.stop();
    }

    public static void main(String[] args) {
        launch(args);
    }
}