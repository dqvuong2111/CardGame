import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import ui.SceneManager; 

public class Main extends Application {

    private SceneManager sceneManager; 

    public static volatile boolean isShuttingDown = false;

    @Override
    public void start(Stage primaryStage) {
        // Khởi tạo SceneManager
        sceneManager = new SceneManager(primaryStage);

        primaryStage.setOnCloseRequest(event -> {
            System.out.println("Window closing request received. Terminating application.");
            event.consume(); 
            Main.isShuttingDown = true;

            // Yêu cầu SceneManager dừng game nếu có game đang chạy
            if (sceneManager != null) {
                sceneManager.stopCurrentGame(); 
            }

            Platform.exit(); 
            System.exit(0); // Đảm bảo tất cả các thread đều dừng
        });

        // SceneManager sẽ tự động hiển thị Main Menu khi được khởi tạo
        primaryStage.show();
    }

    @Override
    public void stop() throws Exception {
        if (sceneManager != null) {
            sceneManager.stopCurrentGame();
        }
        super.stop();
    }

    public static void main(String[] args) {
        launch(args);
    }
}