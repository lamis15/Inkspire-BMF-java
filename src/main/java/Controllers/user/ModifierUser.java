package Controllers.user;

import com.sun.javafx.stage.EmbeddedWindow;
import entities.Session;
import entities.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import service.UserService;
import utils.SceneSwitch;

import java.util.EventObject;

public class ModifierUser {

    @FXML
    private TextField firstNameField;

    @FXML
    private TextField lastNameField;

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button modifyButton;

    @FXML
    private Button deleteButton;

    private final UserService service = new UserService();
    private User currentUser;

    @FXML
    public void initialize() {
        // Retrieve the current user from the session or wherever it is stored
        currentUser = Session.getCurrentUser();  // Assuming you have a session management system in place

        // Populate the fields with the current user's information
        if (currentUser != null) {
            firstNameField.setText(currentUser.getFirstName());
            lastNameField.setText(currentUser.getLastName());
            emailField.setText(currentUser.getEmail());
            passwordField.setText(currentUser.getPassword()); // Be cautious with displaying password
        }
    }

    // Modify the user details
    @FXML
    private void modifyUser() {
        // Validate the input fields, make changes, and update the user
        String firstName = firstNameField.getText();
        String lastName = lastNameField.getText();
        String email = emailField.getText();
        String password = passwordField.getText();

        // Update the user details
        currentUser.setFirstName(firstName);
        currentUser.setLastName(lastName);
        currentUser.setEmail(email);
        currentUser.setPassword(password);  // Encrypt the password as needed

        // Call the service to save the updated user
        boolean success = service.modifier(currentUser);
        }

    @FXML
    public void deleteUser(ActionEvent actionEvent) {
        boolean success = service.supprimer(currentUser.getId());

        if (success) {
            System.out.println("User deleted successfully.");
            try {
                // Get the current stage (primaryStage)
                Stage primaryStage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();

                // Load the new FXML file
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/UserUtils/SigninUser.fxml"));
                Pane pane = loader.load();

                // Create a new scene with the loaded pane and set it on the stage
                Scene scene = new Scene(pane);
                primaryStage.setScene(scene);
                primaryStage.show();

                // Optional: Close the current window if you don't want to keep it open
                // primaryStage.close();

            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            // Show an error message
            System.out.println("Failed to delete user.");
        }
    }

}
