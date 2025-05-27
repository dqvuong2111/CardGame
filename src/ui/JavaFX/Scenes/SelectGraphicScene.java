// File: ui/JavaFX/Scenes/SelectGameVariantSceneView.java
package ui.JavaFX.Scenes; // Đặt đúng package của bạn

import ui.JavaFX.SceneManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.List;

public class SelectGraphicScene {

    private SceneManager sceneManager;
    private Label graphicDisplayLabelToUpdate; // Label từ PlayerCustomizationSceneView
    private SceneManager.Graphic tempSelectedGraphic;

    public SelectGraphicScene(SceneManager sceneManager, Label graphicDisplayLabel) {
        this.sceneManager = sceneManager;
        this.graphicDisplayLabelToUpdate = graphicDisplayLabel;
        this.tempSelectedGraphic = sceneManager.getGraphic(); // Lấy giá trị hiện tại
    }

    public Parent createContent() {
        VBox rootPane = new VBox(25);
        rootPane.setAlignment(Pos.CENTER);
        rootPane.setPadding(new Insets(40));

        String imagePath = "/background/mainmenu.jpg"; // Hoặc ảnh nền bạn muốn
        try {
            String imageUrl = getClass().getResource(imagePath).toExternalForm();
            rootPane.setStyle(
                "-fx-background-image: url('" + imageUrl + "'); " +
                "-fx-background-repeat: no-repeat; " +
                "-fx-background-position: center center; " +
                "-fx-background-size: cover;"
            );
        } catch (Exception e) {
            System.err.println("Lỗi tải ảnh nền cho SelectGameVariantScene: " + imagePath + " - " + e.getMessage());
            rootPane.setStyle("-fx-background-color: #ECEFF1;");
        }

        Label title = new Label("Chọn Chế Độ Chơi");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 32));
        title.setTextFill(Color.WHITE);
        DropShadow dsText = new DropShadow();
        dsText.setRadius(3); dsText.setOffsetX(1); dsText.setOffsetY(1); dsText.setColor(Color.rgb(0, 0, 0, 0.7));
        title.setEffect(dsText);
        VBox.setMargin(title, new Insets(0, 0, 30, 0));

        VBox graphicvariantsContainer = new VBox(18);
        graphicvariantsContainer.setAlignment(Pos.CENTER);
        graphicvariantsContainer.setMaxWidth(380); // Điều chỉnh nếu cần

        Label basicLabel = createGraphicLabel("Basic", SceneManager.Graphic.BASIC);
        Label graphicLabel = createGraphicLabel("Graphic", SceneManager.Graphic.GRAPHIC);

        List<Label> allVariantLabels = List.of(basicLabel, graphicLabel);

        Runnable updateLabelStyles = () -> {
            for (Label label : allVariantLabels) {
                SceneManager.Graphic labelVariant = (SceneManager.Graphic) label.getUserData();
                if (labelVariant == this.tempSelectedGraphic) {
                    label.setStyle(getSelectedVariantLabelStyle());
                } else {
                    label.setStyle(getNormalVariantLabelStyle());
                }
            }
        };

        for (Label label : allVariantLabels) {
            label.setOnMouseClicked(event -> {
                this.tempSelectedGraphic = (SceneManager.Graphic) label.getUserData();
                updateLabelStyles.run();
            });
        }
        
        updateLabelStyles.run(); // Áp dụng style ban đầu

        graphicvariantsContainer.getChildren().addAll(basicLabel, graphicLabel);

        Button confirmButton = new Button("Xác Nhận");
        styleActionButton(confirmButton, "#FF8C00", "#FFA500"); // Màu xanh lá
        VBox.setMargin(confirmButton, new Insets(35, 0, 0, 0));

        confirmButton.setOnAction(e -> {
            sceneManager.setSelectedGraphic(this.tempSelectedGraphic);
            if (graphicDisplayLabelToUpdate != null) {
                graphicDisplayLabelToUpdate.setText(this.tempSelectedGraphic.toString());
            }
            sceneManager.showPlayerCustomizationScene();
        });

        rootPane.getChildren().addAll(title, graphicvariantsContainer, confirmButton);
        return rootPane;
    }

    private Label createGraphicLabel(String text, SceneManager.Graphic variant) {
        Label label = new Label(text);
        label.setUserData(variant);
        label.setCursor(Cursor.HAND);
        return label;
    }

    private String getNormalVariantLabelStyle() {
        return "-fx-font-family: 'Arial'; -fx-font-size: 24px; " + // Cỡ chữ to hơn chút
               "-fx-padding: 15px 30px; -fx-border-color: #f39c12; -fx-border-width: 1.5px; " + // Viền cam
               "-fx-border-radius: 10; -fx-background-radius: 10; -fx-background-color: rgba(52, 73, 94, 0.75); " + // Nền xanh đậm hơn, mờ
               "-fx-text-fill: white; " +
               "-fx-alignment: center; -fx-pref-width: 350px;"; // Chiều rộng cố định
    }

    private String getSelectedVariantLabelStyle() {
        return "-fx-font-family: 'Arial'; -fx-font-size: 26px; " + // Cỡ chữ to hơn khi chọn
               "-fx-padding: 15px 30px; -fx-border-color: #FFFFFF; -fx-border-width: 3px; " + // Viền trắng nổi bật
               "-fx-border-radius: 10; -fx-background-radius: 10; -fx-background-color: rgba(230, 126, 34, 0.9); " + // Nền cam đậm hơn, mờ
               "-fx-text-fill: white; " +
               "-fx-font-weight: bold; -fx-alignment: center; -fx-pref-width: 350px;" +
               "-fx-effect: dropshadow(gaussian, #e67e22, 18, 0.5, 0, 0);"; // Bóng đổ màu cam
    }
    
    private void styleActionButton(Button button, String baseColor, String hoverColor) {
        button.setPrefWidth(220); // To hơn chút
        button.setPrefHeight(55); // Cao hơn chút
        button.setFont(Font.font("Arial", FontWeight.BOLD, 20)); // Chữ to hơn
        String baseStyle = String.format(
                "-fx-background-color: %s; -fx-text-fill: white; -fx-background-radius: 8; -fx-border-radius: 8; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.15), 6, 0.0, 0, 2);",
                baseColor);
        String hoverStyle = String.format(
                "-fx-background-color: %s; -fx-text-fill: white; -fx-background-radius: 8; -fx-border-radius: 8; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.25), 9, 0.0, 0, 3);",
                hoverColor);
        button.setStyle(baseStyle);
        button.setOnMouseEntered(e -> button.setStyle(hoverStyle));
        button.setOnMouseExited(e -> button.setStyle(baseStyle));
        button.setCursor(Cursor.HAND);
    }
}