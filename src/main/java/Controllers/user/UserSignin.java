package Controllers.user;

import com.google.api.client.auth.oauth2.Credential;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import entities.SceneManager;
import entities.Session;
import entities.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.stage.Stage;
import service.UserService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.prefs.Preferences;


public class UserSignin {

    private final UserService service = new UserService();
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label emailError;
    @FXML private Label passwordError;
    @FXML private CheckBox rememberme ;
    @FXML private FontAwesomeIconView visibilityicon ;
    @FXML private TextField visibleText ;
    private boolean isPasswordVisible = false;

    @FXML
    public void initialize() {
        if(emailField != null) {

            //loading your account
            loadSavedCredentials() ;

            // Listener for email validation only
            emailField.textProperty().addListener((obs, oldVal, newVal) -> {
                if (!newVal.matches("[\\w.%+-]+@[\\w.-]+\\.[a-zA-Z]{2,6}")) {
                    emailError.setText("Invalid email format * ");
                    emailError.setStyle("-fx-text-fill: red;");
                } else {
                    emailError.setText("");
                }
            });
        }
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


        } else if (service.checkUser(email, password) != null ) {
            User loggedInUser = service.checkUser(email, password);
            if(loggedInUser == null ){
                System.out.println("user not found ! ");
            }
            Session.setCurrentUser(loggedInUser);

            //saving the user for the remember me
            saveCredentials(email, password, rememberme.isSelected());

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

    private void loadSavedCredentials() {
        Preferences prefs = Preferences.userRoot().node("MyAppRememberMe");
        boolean rememberMe = prefs.getBoolean("rememberMe", false);

        if (rememberMe) {
            String savedEmail = prefs.get("email", null);
            String savedPassword = prefs.get("password", null);

            if (savedEmail != null && savedPassword != null) {
                emailField.setText(savedEmail);
                passwordField.setText(savedPassword);
                rememberme.setSelected(true);
            }
        }
    }

    private void saveCredentials(String email, String password, boolean rememberMe) {
        Preferences prefs = Preferences.userRoot().node("MyAppRememberMe");

        if (rememberMe) {
            prefs.put("email", email);
            prefs.put("password", password);
        } else {
            prefs.remove("email");
            prefs.remove("password");
        }
        prefs.putBoolean("rememberMe", rememberMe);
    }


    @FXML
    private void TogglePassVisibility(ActionEvent event) {
        isPasswordVisible = !isPasswordVisible;

        boolean currentlyVisible = visibleText.isVisible();
        visibleText.setVisible(!currentlyVisible);
        visibleText.setManaged(!currentlyVisible);

        if (isPasswordVisible) {
            visibleText.setText(passwordField.getText());
            visibilityicon.setGlyphName("EYE_SLASH");
        } else {
            passwordField.setText(visibleText.getText());
            visibilityicon.setGlyphName("EYE");
        }

    }

    @FXML
    private void redirectToPassReset(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        SceneManager.switchTo(stage,"/UserUtils/RestPassword.fxml");
    }

    @FXML
    private void GoogleSignin(ActionEvent event) throws IOException, SQLException {
        GoogleSignIn googleSignIn = new GoogleSignIn();
        Credential userToken = googleSignIn.signIn() ;
        String User_email = googleSignIn.getUserEmail(userToken) ;
        User current_user = service.getUserByEmail(User_email) ;
        if(current_user != null && current_user.getStatus() == 1) {
            Session.setCurrentUser(current_user);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            SceneManager.switchTo(stage,"/Base_Window.fxml");
        }
    }

    @FXML
    private void GithubSignin(ActionEvent event) throws IOException, SQLException {
        GithubSignin githubSignin = new GithubSignin();
        Credential user_token = githubSignin.signIn();
        String User_email = githubSignin.getUserEmail(user_token) ;
        User current_user = service.getUserByEmail(User_email) ;
        if(current_user != null && current_user.getStatus() == 1) {
            Session.setCurrentUser(current_user);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            SceneManager.switchTo(stage,"/Base_Window.fxml");
        }
    }




}