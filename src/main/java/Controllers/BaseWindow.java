package Controllers;

import Controllers.Chat.GeminiChatLauncher;
import entities.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import utils.SceneSwitch;

import javafx.scene.image.Image;
import javafx.scene.control.Label;

import java.io.File;
import java.io.IOException;

public class BaseWindow {

    @FXML
    private Button buttonhome1;
    @FXML
    private Label displayName;

    @FXML
    private AnchorPane mainRouter;
    
    @FXML
    private ImageView userIcon;
    
    @FXML
    private ImageView messagesIcon;

    @FXML
    void goCollections(ActionEvent event) {
        SceneSwitch.switchScene(mainRouter, "/AfficherCollections.fxml");
    }

    @FXML
    void goDonations(ActionEvent event) {
        User currentUser = entities.Session.getCurrentUser();
        if (currentUser != null && currentUser.getRole() == 1) {
            SceneSwitch.switchScene(mainRouter, "/AfficherAllDonations.fxml");
        } else {
            SceneSwitch.switchScene(mainRouter, "/AfficherDonations.fxml");
        }
    }

    @FXML
    void goEvents(ActionEvent event) {
        User currentUser = entities.Session.getCurrentUser();
        if (currentUser != null && currentUser.getRole() == 1) {
            SceneSwitch.switchScene(mainRouter, "/EventUtils/AfficherEventBack.fxml");
        } else {
            SceneSwitch.switchScene(mainRouter, "/EventUtils/AfficherEvent.fxml");
        }
    }
    @FXML
    void goAuction(ActionEvent event) {
        User currentUser = entities.Session.getCurrentUser();
        if (currentUser != null && currentUser.getRole() == 1) {
            SceneSwitch.switchScene(mainRouter, "/AuctionUtils/Auction/AfficherAuctionAdmin.fxml");
        } else {
            SceneSwitch.switchScene(mainRouter, "/AuctionUtils/Auction/AfficherAuction.fxml");
        }

    }
    
    @FXML
    void goUser(MouseEvent event) {
        SceneSwitch.switchScene(mainRouter, "/UserUtils/ModifierUser.fxml");
    }
    
    @FXML
    void goMessages(MouseEvent event) {
        // Instead of directly switching to Messages.fxml, show the dropdown menu
        try {
            // Get the current stage from the event source
            Stage stage = (Stage) ((ImageView) event.getSource()).getScene().getWindow();
            
            // Create a context menu at the position of the messages icon
            javafx.scene.control.ContextMenu contextMenu = new javafx.scene.control.ContextMenu();
            
            // Create menu items
            javafx.scene.control.MenuItem messagesItem = new javafx.scene.control.MenuItem("Chat (Messages)");
            messagesItem.setOnAction(e -> {
                SceneSwitch.switchScene(mainRouter, "/messages/Messages.fxml");
            });
            
            javafx.scene.control.MenuItem chatbotItem = new javafx.scene.control.MenuItem("Art Assistant");
            chatbotItem.setOnAction(e -> {
                try {
                    GeminiChatLauncher.openChatDialog(stage);
                } catch (IOException ex) {
                    System.err.println("Failed to open chatbot dialog: " + ex.getMessage());
                    ex.printStackTrace();
                }
            });
            
            // Add items to context menu
            contextMenu.getItems().addAll(messagesItem, chatbotItem);
            
            // Show the context menu below the messages icon
            contextMenu.show(messagesIcon, javafx.geometry.Side.BOTTOM, 0, 0);
            
        } catch (Exception e) {
            System.err.println("Error showing chat options: " + e.getMessage());
            e.printStackTrace();
            
            // Fallback to original behavior if there's an error
            SceneSwitch.switchScene(mainRouter, "/messages/Messages.fxml");
        }
    }

    @FXML
    void goArtwork(ActionEvent actionEvent) {



        User currentUser = entities.Session.getCurrentUser();
        if (currentUser != null && currentUser.getRole() == 1) {
            SceneSwitch.switchScene(mainRouter, "/ArtworkAdmin.fxml");
        } else {
            SceneSwitch.switchScene(mainRouter, "/ArtworkDisplay.fxml");

        }
    }

    @FXML
    void gohome(ActionEvent actionEvent) {
        SceneSwitch.switchScene(mainRouter, "/home.fxml");
    }

    @FXML
    public void initialize() {
        User currentUser = entities.Session.getCurrentUser();

        if (currentUser != null && currentUser.getRole() == 0) {
            buttonhome1.setVisible(true);
            displayName.setText(currentUser.getFirstName() + " " + currentUser.getLastName());

            String imagePath = "C:/xampp/htdocs/images/profilePictures/" + currentUser.getPicture();

            if (imagePath != null && !imagePath.isEmpty()) {
                try {
                    File file = new File(imagePath);
                    if (file.exists()) {
                        Image image = new Image(file.toURI().toString());
                        userIcon.setImage(image);
                    } else {
                        System.out.println("Image file not found at: " + imagePath);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }else {
            buttonhome1.setVisible(false);
        }


    }

}
