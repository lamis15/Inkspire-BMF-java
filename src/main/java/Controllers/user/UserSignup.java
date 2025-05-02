package Controllers.user;

import entities.SceneManager;
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
import org.mindrot.jbcrypt.BCrypt;

import java.sql.SQLException;

public class UserSignup {

    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;

    @FXML private Label emailError;
    @FXML private Label firstnameError;
    @FXML private Label LastnameError;
    @FXML private Label passwordError;
    @FXML private Label confirmError;
    @FXML private Label confirmPasswordError;

    @FXML private Button signUpButton;

    private final UserService service = new UserService();

    @FXML
    public void initialize() {
        // First name validation
        firstNameField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("[a-zA-Z]+")) {
                firstNameField.setStyle("-fx-border-color: red;");
            } else {
                firstNameField.setStyle(null);
            }
        });

        // Last name validation
        lastNameField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("[a-zA-Z]+")) {
                lastNameField.setStyle("-fx-border-color: red;");
            } else {
                lastNameField.setStyle(null);
            }
        });

        // Email format + uniqueness validation
        emailField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("[\\w.%+-]+@[\\w.-]+\\.[a-zA-Z]{2,6}")) {
                emailError.setText("Invalid email format");
            } else {
                try {
                    if (service.emailExists(newVal)) {
                        emailError.setText("Email already in use");
                    } else {
                        emailError.setText("");
                    }
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        // Password length check
        passwordField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.length() < 6) {
                passwordError.setText("Password too short (min 6 chars)");
            } else {
                passwordError.setText("");
            }

            // Match check after typing
            if (!newVal.equals(confirmPasswordField.getText()) || newVal.isEmpty()) {
                confirmPasswordError.setText("Passwords do not match");
            } else {
                confirmPasswordError.setText("");
            }
        });

        // Confirm password check
        confirmPasswordField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.equals(passwordField.getText()) || newVal.isEmpty()) {
                confirmPasswordError.setText("Passwords do not match");
            } else {
                confirmPasswordError.setText("");
            }
        });
    }

    @FXML
    private void signUp(ActionEvent event) {
        boolean hasError = false;

        // Clear previous styles
        firstNameField.setStyle(null);
        lastNameField.setStyle(null);
        emailField.setStyle(null);
        passwordField.setStyle(null);
        confirmPasswordField.setStyle(null);

        //numeric values for first and last
        if (firstNameField.getText().trim().matches(".*\\d.*")) {
            firstnameError.setStyle("-fx-text-fill: red;");
            firstnameError.setText("First name cannot contain numbers");
            hasError = true;
        }

        if (lastNameField.getText().trim().matches(".*\\d.*")) {
            LastnameError.setStyle("-fx-text-fill: red;");
            LastnameError.setText("Last name cannot contain numbers");
            hasError = true;
        }


        // Check if fields are empty
        if (firstNameField.getText().trim().isEmpty()) {
            firstnameError.setStyle("-fx-text-fill: red;");
            firstnameError.setText("First name is required * ");
            hasError = true;
        }
        if (lastNameField.getText().trim().isEmpty()) {
            LastnameError.setStyle("-fx-text-fill: red;");
            LastnameError.setText("Last name is required * ");
            hasError = true;
        }
        if (emailField.getText().trim().isEmpty()) {

            emailError.setStyle("-fx-text-fill: red;");
            emailError.setText("Email is required * ");
            hasError = true;
        }
        if (passwordField.getText().trim().isEmpty()) {
            passwordError.setStyle("-fx-text-fill: red;");
            passwordError.setText("Password is required * ");
            hasError = true;
        }
        if (confirmPasswordField.getText().trim().isEmpty()) {
            confirmError.setStyle("-fx-text-fill: red;");
            confirmPasswordError.setText("Please confirm your password");
            hasError = true;
        }

        if (hasError) {
            System.out.println("Please fill all fields correctly.");
            return;
        }

        // Proceed with user creation
        User newUser = new User();
        newUser.setFirstName(firstNameField.getText().trim());
        newUser.setLastName(lastNameField.getText().trim());
        newUser.setEmail(emailField.getText().trim());

        String rawPassword = passwordField.getText().trim();
        String hashedPassword = BCrypt.hashpw(rawPassword, BCrypt.gensalt());
        newUser.setPassword(hashedPassword);

        newUser.setRole(0);
        newUser.setStatus(1);
        service.ajouter(newUser);


        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        SceneManager.switchTo(stage,"/UserUtils/SigninUser.fxml");

        System.out.println("User created successfully");
    }

    @FXML
    private void switchToSignIn(ActionEvent event) {
        Stage primaryStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        SceneManager.switchTo(primaryStage, "/UserUtils/SigninUser.fxml");
    }

}
