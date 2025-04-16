package Controllers.user;

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

        // Binding: Sign In enabled only when email is valid & password not empty
        BooleanBinding formValid = Bindings.createBooleanBinding(() ->
                        !emailField.getText().trim().isEmpty() &&
                                emailError.getText().isEmpty() &&
                                !passwordField.getText().trim().isEmpty(),
                emailField.textProperty(),
                emailError.textProperty(),
                passwordField.textProperty()
        );

        signInButton.disableProperty().bind(formValid.not());
    }

    @FXML
    private void signIn(ActionEvent event) {
        String email = emailField.getText().trim();
        String password = passwordField.getText().trim();

        if (email.isEmpty() || password.isEmpty()) {
            // Optional: Show error messages for empty fields
            emailError.setText("Email is required");
            passwordError.setText("Password is required");
        } else if (service.checkUser(email, password) != null) {
            User loggedInUser = service.checkUser(email, password);
            Session.setCurrentUser(loggedInUser);

            try {
                Parent root = FXMLLoader.load(getClass().getResource("/Base_Window.fxml"));
                Scene scene = new Scene(root);
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.setScene(scene);
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            // Handle invalid login
            emailError.setText("Invalid email or password");
            passwordError.setText("");
        }
    }

    @FXML
    private void switchToSignUp(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/UserUtils/SignupUser.fxml"));
            Scene scene = new Scene(root);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onBackClick(ActionEvent event) {
        try {
            // Replace with appropriate back navigation
            Parent root = FXMLLoader.load(getClass().getResource("/MainMenu.fxml"));
            Scene scene = new Scene(root);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
