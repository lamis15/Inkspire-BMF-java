package Controllers.Chat;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;

import java.io.IOException;

/**
 * Utility class to launch the Gemini AI Art Assistant dialog
 */
public class GeminiChatLauncher {

    /**
     * Opens the Gemini AI Art Assistant dialog
     *
     * @param parentStage The parent stage
     * @throws IOException If the FXML file cannot be loaded
     */
    public static void openChatDialog(Stage parentStage) throws IOException {
        // Load the FXML file
        FXMLLoader loader = new FXMLLoader(GeminiChatLauncher.class.getResource("/chat/GeminiChat.fxml"));
        Scene scene = new Scene(loader.load());
        
        // Get the controller for resource cleanup
        GeminiChatController controller = loader.getController();
        
        // Create a new stage
        Stage dialogStage = new Stage();
        dialogStage.setScene(scene);
        dialogStage.setTitle("Inkspire Art Assistant");
        dialogStage.getIcons().add(new Image(GeminiChatLauncher.class.getResourceAsStream("/assets/icons/chat.png")));
        
        // Set modality and owner
        dialogStage.initModality(Modality.WINDOW_MODAL);
        dialogStage.initOwner(parentStage);
        dialogStage.initStyle(StageStyle.DECORATED);
        
        // Set size and position
        dialogStage.setWidth(500);
        dialogStage.setHeight(600);
        dialogStage.setResizable(false);
        
        // Center the dialog on the parent stage
        dialogStage.setX(parentStage.getX() + (parentStage.getWidth() - dialogStage.getWidth()) / 2);
        dialogStage.setY(parentStage.getY() + (parentStage.getHeight() - dialogStage.getHeight()) / 2);
        
        // Add close handler to clean up resources
        dialogStage.setOnCloseRequest((WindowEvent event) -> {
            controller.shutdown();
        });
        
        // Show the dialog
        dialogStage.show();
    }
}
