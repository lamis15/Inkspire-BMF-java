package Controllers.user;

import entities.User;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import service.UserService;

import java.io.File;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

public class ShowUsers {
    private final UserService service = new UserService();
    private List<User> allUsers; // Store all users for filtering

    @FXML
    private FlowPane usersFlowPane;
    @FXML
    private TextField SearchBar;

    @FXML
    public void initialize() throws SQLException {
        usersFlowPane.setHgap(20);
        usersFlowPane.setVgap(20);

        // Load all users initially
        allUsers = service.recuperer();
        displayUsers(allUsers);

        // Set up search listener for real-time filtering
        SearchBar.textProperty().addListener((observable, oldValue, newValue) -> {
            try {
                filterUsers(newValue);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    private void filterUsers(String searchText) throws SQLException {
        List<User> filteredUsers = service.searchUsers(searchText);
        displayUsers(filteredUsers);
    }

    private void displayUsers(List<User> users) throws SQLException {
        usersFlowPane.getChildren().clear(); // Clear current display

        for (User user : users) {
            String imagepath = user.getPicture() != null
                    ? "C:/xampp/htdocs/images/ProfilePicture/" + user.getPicture()
                    : "C:/xampp/htdocs/images/ProfilePicture/user.png";

            VBox userCard = createUserCard(
                    user.getFirstName(),
                    user.getEmail(),
                    imagepath,
                    user.getStatus() == 0
            );
            usersFlowPane.getChildren().add(userCard);
        }
    }

    private VBox createUserCard(String name, String email, String imagepath, boolean isBanned) {
        VBox card = new VBox(10);
        card.setPrefWidth(200);
        card.setMaxWidth(200);
        card.setMinWidth(200);
        card.setStyle(
                "-fx-padding: 20; " +
                        "-fx-background-color: white; " +
                        "-fx-border-color: grey; " +
                        "-fx-border-radius: 5; " +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 0);"
        );

        // Profile Image
        Circle profileIcon = new Circle(30, Color.LIGHTGRAY);
        Label initial = new Label(String.valueOf(name.charAt(0)));
        initial.setStyle("-fx-font-size: 20; -fx-text-fill: white;");

        try {
            File file = new File(imagepath);
            if (file.exists()) {
                Image image = new Image(file.toURI().toString());
                profileIcon.setFill(new ImagePattern(image));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // User Info
        Label nameLabel = new Label(name);
        nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16;");

        Label emailLabel = new Label(email);
        emailLabel.setStyle("-fx-text-fill: #666;");

        // Ban/Unban Buttons
        Button banButton = new Button("Ban");
        banButton.setStyle(
                "-fx-background-color: #ff4444; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-weight: bold; " +
                        "-fx-padding: 5 10; " +
                        "-fx-background-radius: 3;"
        );
        banButton.setOnAction(event -> {
            try {
                service.banUser(email);
                displayUsers(allUsers); // Refresh the list
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });

        Button unbanButton = new Button("Unban");
        unbanButton.setStyle(
                "-fx-background-color: #4CAF50; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-weight: bold; " +
                        "-fx-padding: 5 10; " +
                        "-fx-background-radius: 3;"
        );
        unbanButton.setOnAction(event -> {
            try {
                service.unbanUser(email);
                displayUsers(allUsers); // Refresh the list
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });



        card.getChildren().addAll(
                new StackPane(profileIcon, initial),
                nameLabel,
                emailLabel,
                banButton,
                unbanButton
        );

        return card;
    }
}