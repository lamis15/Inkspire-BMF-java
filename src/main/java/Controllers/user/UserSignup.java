package Controllers.user;

import entities.User;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
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

import java.sql.SQLException;

public class UserSignup {

    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;

    @FXML private Label emailError;
    @FXML private Label passwordError;
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
            if (!newVal.equals(confirmPasswordField.getText())) {
                confirmPasswordError.setText("Passwords do not match");
            } else {
                confirmPasswordError.setText("");
            }
        });

        // Confirm password check
        confirmPasswordField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.equals(passwordField.getText())) {
                confirmPasswordError.setText("Passwords do not match");
            } else {
                confirmPasswordError.setText("");
            }
        });

        // Enable button only when everything is valid
        BooleanBinding formValid = Bindings.createBooleanBinding(() ->
                        !firstNameField.getText().trim().isEmpty() &&
                                firstNameField.getText().matches("[a-zA-Z]+") &&
                                !lastNameField.getText().trim().isEmpty() &&
                                lastNameField.getText().matches("[a-zA-Z]+") &&
                                !emailField.getText().trim().isEmpty() &&
                                emailError.getText().isEmpty() &&
                                !passwordField.getText().trim().isEmpty() &&
                                passwordError.getText().isEmpty() &&
                                confirmPasswordError.getText().isEmpty() &&
                                passwordField.getText().equals(confirmPasswordField.getText()),
                firstNameField.textProperty(),
                lastNameField.textProperty(),
                emailField.textProperty(),
                emailError.textProperty(),
                passwordField.textProperty(),
                passwordError.textProperty(),
                confirmPasswordField.textProperty(),
                confirmPasswordError.textProperty()
        );

        signUpButton.disableProperty().bind(formValid.not());
    }

    @FXML
    private void signUp(ActionEvent event) {
        User newUser = new User();
        newUser.setFirstName(firstNameField.getText().trim()) ;
        newUser.setLastName(lastNameField.getText().trim());
        newUser.setEmail(emailField.getText().trim());
        newUser.setPassword(passwordField.getText().trim());
        newUser.setRole(0);
        newUser.setStatus(1);

        service.ajouter(newUser);

        // Optional: Redirect or show confirmation label
        System.out.println("User created successfully");
    }

    @FXML
    private void switchToSignIn(ActionEvent event) {
        try {
            // Get the current stage (primaryStage)
            Stage primaryStage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // Load the new FXML file
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/UserUtils/SigninUser.fxml"));
            Pane pane = loader.load();

            // Create a new scene with the loaded pane and set it on the stage
            Scene scene = new Scene(pane);
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onBackClick(ActionEvent event) {
        // Handle back navigation
    }
}
