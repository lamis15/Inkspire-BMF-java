package Controllers.user;

import entities.SceneManager;
import entities.Session;
import entities.User;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import service.UserService;

import java.io.IOException;

public class UserSignin {

    private final UserService service = new UserService();

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label emailError;

    @FXML
    private Label passwordError;

    @FXML
    private Button signInButton;

    @FXML
    public void initialize() {
        // Listener for email validation only
        emailField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("[\\w.%+-]+@[\\w.-]+\\.[a-zA-Z]{2,6}")) {
                emailError.setText("Invalid email format");
            } else {
                emailError.setText("");
            }
        });
    }

    @FXML
    private void signIn(ActionEvent event) {
        String email = emailField.getText().trim();
        String password = passwordField.getText().trim();

        if (email.isEmpty() || password.isEmpty()) {

            emailError.setStyle("-fx-text-fill: red;");
            emailError.setText("Email is required * ");

            passwordError.setStyle("-fx-text-fill: red;");
            passwordError.setText("Password is required * ");


        } else if (service.checkUser(email, password) != null) {
            User loggedInUser = service.checkUser(email, password);
            Session.setCurrentUser(loggedInUser);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            SceneManager.switchTo(stage,"/Base_Window.fxml");
        } else {
            // Handle invalid login
            emailError.setStyle("-fx-text-fill: red ; ") ;
            emailError.setText("Invalid email or password");
            passwordError.setText("");
        }
    }

    @FXML
    private void switchToSignUp(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        SceneManager.switchTo(stage,"/UserUtils/SignupUser.fxml");
    }

}