package Controllers.user;

import entities.SceneManager;
import entities.Session;
import entities.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.mindrot.jbcrypt.BCrypt;
import service.UserService;



import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;

public class ModifierUser {

    private String ImageUrl;

    @FXML
    private Label FirstnameError;
    @FXML
    private Label LastnameError;
    @FXML
    private Label EmailError;
    @FXML
    private Label passwordError;
    @FXML
    private Label confirmpassword;

    @FXML
    private Button uploadImage;

    @FXML
    private ImageView usericon2;

    @FXML
    private TextField firstNameField;

    @FXML
    private TextField lastNameField;

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField passwordField1;

    @FXML
    private Button modifyButton;

    @FXML
    private Button deleteButton;

    @FXML
    private Circle profilepic;


    private final UserService service = new UserService();
    private User currentUser;

    @FXML
    public void initialize() {
        currentUser = Session.getCurrentUser();

        if (currentUser != null && currentUser.getStatus() == 1) {
            String imagePath;

            if (currentUser.getPicture() == null || currentUser.getPicture().isEmpty() || currentUser.getPicture().equals("null")) {
                imagePath = "C:/xampp/htdocs/images/profilePictures/user.png";
            } else {
                imagePath = "C:/xampp/htdocs/images/profilePictures/" + currentUser.getPicture();
            }

            try {
                File file = new File(imagePath);
                if (file.exists()) {
                    Image image = new Image(file.toURI().toString());
                    profilepic.setFill(new ImagePattern(image));
                } else {
                    System.err.println("Image not found at: " + imagePath);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        if (currentUser != null) {
            firstNameField.setText(currentUser.getFirstName());
            lastNameField.setText(currentUser.getLastName());
            emailField.setText(currentUser.getEmail());
        }

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
                EmailError.setText("Invalid email format");
            } else {
                try {
                    if (service.emailExists(newVal)) {
                        EmailError.setText("Email already in use");
                    } else {
                        EmailError.setText("");
                    }
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        });


        passwordField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.length() < 6) {
                passwordError.setText("Password too short (min 6 chars)");
            } else {
                passwordError.setText("");
            }

            // Match check after typing
            if (!newVal.equals(passwordField1.getText()) || newVal.isEmpty()) {
                confirmpassword.setText("Passwords do not match");
            } else {
                confirmpassword.setText("");
            }
        });

        passwordField1.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.equals(passwordField.getText()) || newVal.isEmpty()) {
                confirmpassword.setText("Passwords do not match");
            } else {
                confirmpassword.setText("");
            }
        });

    }

    @FXML
    private void modifyUser(ActionEvent event) {
        boolean hasError = false ;

        String firstName = firstNameField.getText();
        String lastName = lastNameField.getText();
        String email = emailField.getText();
        String password = passwordField.getText();
        String confirmPassword = passwordField1.getText();

        // Validate first name
        if (firstName.isEmpty()) {
            FirstnameError.setText("First name is required *");
            FirstnameError.setStyle("-fx-text-fill: red;");
            hasError = true;
        } else if (firstName.matches(".*\\d.*")) {
            FirstnameError.setText("First name cannot contain numbers");
            FirstnameError.setStyle("-fx-text-fill: red;");
            hasError = true;
        }

        // Validate last name
        if (lastName.isEmpty()) {
            LastnameError.setText("Last name is required *");
            LastnameError.setStyle("-fx-text-fill: red;");
            hasError = true;
        } else if (lastName.matches(".*\\d.*")) {
            LastnameError.setText("Last name cannot contain numbers");
            LastnameError.setStyle("-fx-text-fill: red;");
            hasError = true;
        }

        // Validate email
        if (email.isEmpty()) {
            EmailError.setText("Email is required *");
            EmailError.setStyle("-fx-text-fill: red;");
            hasError = true;
        } else if (!email.matches("[\\w.%+-]+@[\\w.-]+\\.[a-zA-Z]{2,6}")) {
            EmailError.setText("Invalid email format");
            EmailError.setStyle("-fx-text-fill: red;");
            hasError = true;
        } else {
            try {
                if (!email.equals(currentUser.getEmail()) && service.emailExists(email)) {
                    EmailError.setText("Email already in use");
                    EmailError.setStyle("-fx-text-fill: red;");
                    hasError = true;
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        if (password.isEmpty() || confirmPassword.isEmpty()) {
            passwordError.setText("Password required * ");
            confirmpassword.setText("Password required * ");
            passwordError.setStyle("-fx-text-fill: red;");
            confirmpassword.setStyle("-fx-text-fill: red;");
            hasError = true;
        }
        if (!password.isEmpty() || !confirmPassword.isEmpty()) {
            if (password.length() < 6) {
                passwordError.setText("Password too short (min 6 chars)");
                passwordError.setStyle("-fx-text-fill: red;");
                hasError = true;
            }
            if (!password.equals(confirmPassword)) {
                confirmpassword.setText("Passwords do not match");
                confirmpassword.setStyle("-fx-text-fill: red;");
                hasError = true;
            }
        }

        if (hasError) {
            System.out.println("Please fix the errors before submitting.");
            return;
        }


        // Update user details
        currentUser.setFirstName(firstName);
        currentUser.setLastName(lastName);
        currentUser.setEmail(email);

        if(ImageUrl != null){
            currentUser.setPicture(ImageUrl) ;
        }

        //pass encryption
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
        currentUser.setPassword(hashedPassword);


        boolean success = service.modifier(currentUser);

        Stage primaryStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        SceneManager.switchTo(primaryStage,"/Base_Window.fxml");
        if (success) {
                System.out.println("User has been modified");
            }
        }

    @FXML
    public void deleteUser(ActionEvent actionEvent) {
        boolean success = service.supprimer(currentUser.getId());
        Session.clearSession();
        if (success) {
            System.out.println("User deleted successfully.");
            Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            SceneManager.switchTo(stage,"/UserUtils/SigninUser.fxml");
        } else {
            System.out.println("Failed to delete user.");
        }
    }


    public void logout(ActionEvent actionEvent) {
        Session.clearSession();
        Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
        SceneManager.switchTo(stage,"/UserUtils/SigninUser.fxml");
    }

    public void upload(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Profile Picture");

        // Set image file filters
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );

        File selectedFile = fileChooser.showOpenDialog(uploadImage.getScene().getWindow());

        if (selectedFile != null) {
            try {
                // Define a location to save the profile picture
                File dir = new File("C:/xampp/htdocs/images/profilePictures/");

                String fileName = "profile_" + System.currentTimeMillis() + "_" + selectedFile.getName();
                File dest = new File(dir, fileName);
                ImageUrl = fileName ;
                System.out.println(ImageUrl);

                // Copy the selected file to the new location
                Files.copy(selectedFile.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);

                // Store path to save later in DB or user object
                String profileImagePath = dest.getAbsolutePath();


                System.out.println("Image saved at: " + profileImagePath);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
