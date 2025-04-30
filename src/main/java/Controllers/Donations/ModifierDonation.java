//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package Controllers.Donations;

import entities.Collections;
import entities.Donation;
import entities.Session;
import entities.User;
import enums.CollectionStatus;
import java.net.URL;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import service.CollectionsService;
import service.DonationService;
import service.UserService;
import utils.SceneSwitch;

public class ModifierDonation implements Initializable {
    @FXML
    private TextField amountField;
    @FXML
    private Label selectedCollectionLabel;
    @FXML
    private Label titleLabel;
    @FXML
    private Label amountErrorLabel;
    @FXML
    private Button saveButton;
    @FXML
    private Button backButton;
    @FXML
    private VBox rootVBox;
    @FXML
    private VBox collectionDetailsContainer;
    @FXML
    private Label collectionImageLabel;
    @FXML
    private Label collectionTitleLabel;
    @FXML
    private Label collectionDescriptionLabel;
    @FXML
    private Label collectionGoalLabel;
    @FXML
    private Label collectionCurrentAmountLabel;
    @FXML
    private Label collectionStatusLabel;
    @FXML
    private Label donationIdLabel;
    @FXML
    private Label donationDateLabel;
    @FXML
    private Label userTokensLabel;
    private Donation donation;
    private DonationService donationService;
    private CollectionsService collectionsService;
    private User currentUser;
    private int userTokenBalance = 0;
    private Collections selectedCollection;

    public ModifierDonation() {
    }

    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.donationService = new DonationService();
        this.collectionsService = new CollectionsService();
        if (this.rootVBox != null) {
            this.rootVBox.sceneProperty().addListener((observable, oldScene, newScene) -> {
                if (newScene != null) {
                    Parent parent;
                    for(parent = this.rootVBox.getParent(); parent != null && !(parent instanceof ScrollPane); parent = parent.getParent()) {
                    }

                    if (parent instanceof ScrollPane) {
                        ScrollPane mainScrollPane = (ScrollPane)parent;
                        mainScrollPane.setFitToWidth(true);
                        mainScrollPane.setVbarPolicy(ScrollBarPolicy.AS_NEEDED);
                        mainScrollPane.setHbarPolicy(ScrollBarPolicy.NEVER);
                        mainScrollPane.setPannable(true);
                    }
                }

            });
        }

        this.currentUser = Session.getCurrentUser();
        if (this.currentUser == null) {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Authentication Error");
            alert.setHeaderText((String)null);
            alert.setContentText("You must be logged in to perform this action.");
            alert.showAndWait();
            this.amountField.setDisable(true);
            this.saveButton.setDisable(true);
        } else {
            this.loadUserTokenBalance();
            this.amountField.textProperty().addListener((observable, oldValue, newValue) -> this.validateAmount((KeyEvent)null));
        }
    }

    private void loadUserTokenBalance() {
        try {
            UserService userService = new UserService();
            User user = userService.getById(this.currentUser.getId());
            if (user != null) {
                this.userTokenBalance = user.getTokens() != null ? user.getTokens() : 0;
                this.userTokensLabel.setText(String.valueOf(this.userTokenBalance));
            } else {
                this.userTokensLabel.setText("0");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            this.userTokensLabel.setText("0");
        }

    }

    public void setDonation(Donation donation) {
        this.donation = donation;

        try {
            if (donation.getCollections() != null && donation.getCollections().getId() != null) {
                this.selectedCollection = this.collectionsService.recupererById(donation.getCollections().getId());
                if (this.selectedCollection == null) {
                    this.selectedCollection = donation.getCollections();
                    if (this.selectedCollection.getUser() == null) {
                        this.selectedCollection.setUser(this.currentUser);
                    }
                }
            } else {
                this.selectedCollection = donation.getCollections();
            }

            donation.setCollections(this.selectedCollection);
            this.populateFields();
            this.updateCollectionDetails(this.selectedCollection);
        } catch (SQLException e) {
            this.showError("Error loading collection details: " + e.getMessage());
            this.selectedCollection = donation.getCollections();
            this.populateFields();
            this.updateCollectionDetails(this.selectedCollection);
        }

    }

    private void populateFields() {
        if (this.donation != null) {
            this.donationIdLabel.setText(String.valueOf(this.donation.getId()));
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            this.donationDateLabel.setText(this.donation.getDate().format(formatter));
            this.amountField.setText(String.valueOf(this.donation.getAmount()));
            this.selectedCollectionLabel.setText(this.donation.getCollections().getTitle());
        }

    }

    private void updateCollectionDetails(Collections collection) {
        if (collection != null) {
            this.collectionTitleLabel.setText(collection.getTitle() != null ? collection.getTitle() : "No Title");
            this.collectionDescriptionLabel.setText(collection.getDescription() != null ? collection.getDescription() : "No Description");
            if (collection.getGoal() != null) {
                this.collectionGoalLabel.setText(String.format("%.2f TND", collection.getGoal()));
            } else {
                this.collectionGoalLabel.setText("No goal set");
            }

            if (collection.getCurrentAmount() != null) {
                this.collectionCurrentAmountLabel.setText(String.format("%.2f TND", collection.getCurrentAmount()));
            } else {
                this.collectionCurrentAmountLabel.setText("0.00 TND");
            }

            if (collection.getStatus() != null) {
                this.collectionStatusLabel.setText(collection.getStatus().toString());
            } else {
                this.collectionStatusLabel.setText("Unknown");
            }

            this.collectionDetailsContainer.setVisible(true);
            if (collection.getImage() != null && !collection.getImage().isEmpty()) {
                try {
                    ImageView imageView = new ImageView(new Image(collection.getImage()));
                    imageView.setFitWidth((double)150.0F);
                    imageView.setFitHeight((double)150.0F);
                    imageView.setPreserveRatio(true);
                    this.collectionImageLabel.setGraphic(imageView);
                    this.collectionImageLabel.setText("");
                } catch (Exception var3) {
                    this.collectionImageLabel.setText("Image not available");
                    this.collectionImageLabel.setGraphic((Node)null);
                }
            } else {
                this.collectionImageLabel.setText("No Image");
                this.collectionImageLabel.setGraphic((Node)null);
            }
        } else {
            this.collectionDetailsContainer.setVisible(false);
        }

    }

    @FXML
    public void validateAmount(KeyEvent event) {
        String amountText = this.amountField.getText().trim();
        if (amountText.isEmpty()) {
            this.amountErrorLabel.setText("Please enter an amount");
            this.amountErrorLabel.setVisible(true);
            this.saveButton.setDisable(true);
        } else {
            try {
                double amount;
                if (amountText.matches("\\d+")) {
                    amount = (double)Integer.parseInt(amountText);
                } else {
                    amount = Double.parseDouble(amountText);
                }

                if (amount <= (double)0.0F) {
                    this.amountErrorLabel.setText("Amount must be greater than zero");
                    this.amountErrorLabel.setVisible(true);
                    this.saveButton.setDisable(true);
                    return;
                }

                double originalAmount = this.donation.getAmount();
                double amountDifference = amount - originalAmount;
                if (amountDifference > (double)0.0F) {
                    int tokensNeeded = (int)Math.ceil(amountDifference);
                    if (tokensNeeded > this.userTokenBalance) {
                        this.amountErrorLabel.setText("Not enough tokens. You need " + tokensNeeded + " tokens, but have only " + this.userTokenBalance);
                        this.amountErrorLabel.setVisible(true);
                        this.saveButton.setDisable(true);
                        return;
                    }
                }

                this.amountErrorLabel.setVisible(false);
                this.saveButton.setDisable(false);
            } catch (NumberFormatException var10) {
                this.amountErrorLabel.setText("Please enter a valid number");
                this.amountErrorLabel.setVisible(true);
                this.saveButton.setDisable(true);
            }

        }
    }

    @FXML
    public void saveDonation(ActionEvent event) {
        String amountText = this.amountField.getText().trim();
        if (amountText.isEmpty()) {
            this.amountErrorLabel.setText("Please enter an amount");
            this.amountErrorLabel.setVisible(true);
        } else {
            try {
                double newAmount;
                if (amountText.matches("\\d+")) {
                    newAmount = (double)Integer.parseInt(amountText);
                } else {
                    newAmount = Double.parseDouble(amountText);
                }

                if (newAmount <= (double)0.0F) {
                    this.amountErrorLabel.setText("Amount must be greater than zero");
                    this.amountErrorLabel.setVisible(true);
                    return;
                }

                double originalAmount = this.donation.getAmount();
                double amountDifference = newAmount - originalAmount;
                if (amountDifference == (double)0.0F) {
                    this.showSuccessAlert();
                    this.onBackClick((ActionEvent)null);
                    return;
                }

                Collections originalCollection = this.donation.getCollections();
                if (originalCollection == null) {
                    this.showError("Collection not found. Cannot update donation.");
                    return;
                }

                User collectionOwner = originalCollection.getUser();
                if (collectionOwner == null) {
                    this.showError("Collection owner not found. Cannot update donation.");
                    return;
                }

                UserService userService = new UserService();
                boolean tokenTransferSuccess = false;
                int tokenAmount = 0;

                try {
                    tokenAmount = (int)Math.ceil(Math.abs(amountDifference));
                    if (tokenAmount < 1) {
                        tokenAmount = 1;
                    }

                    if (amountDifference > (double)0.0F) {
                        User latestUser = userService.getById(this.currentUser.getId());
                        int latestTokenBalance = latestUser.getTokens() != null ? latestUser.getTokens() : 0;
                        if (latestTokenBalance < tokenAmount) {
                            this.showError("You don't have enough tokens. You need " + tokenAmount + " tokens.");
                            return;
                        }

                        tokenTransferSuccess = userService.transferTokens(this.currentUser.getId(), collectionOwner.getId(), tokenAmount);
                    } else {
                        User latestOwner = userService.getById(collectionOwner.getId());
                        int latestOwnerBalance = latestOwner.getTokens() != null ? latestOwner.getTokens() : 0;
                        if (latestOwnerBalance < tokenAmount) {
                            this.showError("The collection owner doesn't have enough tokens to process this refund.");
                            return;
                        }

                        tokenTransferSuccess = userService.transferTokens(collectionOwner.getId(), this.currentUser.getId(), tokenAmount);
                    }

                    if (!tokenTransferSuccess) {
                        this.showError("Failed to transfer tokens. Please try again.");
                        return;
                    }

                    this.donation.setAmount(newAmount);
                    boolean donationUpdateSuccess = this.donationService.modifier(this.donation);
                    if (!donationUpdateSuccess) {
                        this.showError("Failed to update donation. Please try again.");

                        try {
                            if (amountDifference > (double)0.0F) {
                                userService.transferTokens(collectionOwner.getId(), this.currentUser.getId(), tokenAmount);
                            } else {
                                userService.transferTokens(this.currentUser.getId(), collectionOwner.getId(), tokenAmount);
                            }
                        } catch (SQLException ex) {
                            System.err.println("Failed to revert token transfer: " + ex.getMessage());
                        }

                        return;
                    }

                    double newCollectionAmount = originalCollection.getCurrentAmount() + amountDifference;
                    originalCollection.setCurrentAmount(Math.max((double)0.0F, newCollectionAmount));
                    if (originalCollection.getGoal() != null && newCollectionAmount >= originalCollection.getGoal()) {
                        originalCollection.setStatus(CollectionStatus.REACHED);
                    } else {
                        originalCollection.setStatus(CollectionStatus.IN_PROGRESS);
                    }

                    boolean collectionUpdateSuccess = this.collectionsService.modifier(originalCollection);
                    if (!collectionUpdateSuccess) {
                        this.showError("Donation updated but failed to update collection details.");
                        return;
                    }

                    this.loadUserTokenBalance();
                    this.showSuccessAlert();
                    this.onBackClick((ActionEvent)null);
                } catch (SQLException e) {
                    this.showError("Database error: " + e.getMessage());
                }
            } catch (NumberFormatException var20) {
                this.amountErrorLabel.setText("Please enter a valid number");
                this.amountErrorLabel.setVisible(true);
            } catch (Exception e) {
                this.showError("Error: " + e.getMessage());
            }

        }
    }

    private void showError(String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText((String)null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showSuccessAlert() {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText((String)null);
        alert.setContentText("Donation #" + this.donation.getId() + " has been successfully updated.");
        alert.showAndWait();
    }

    @FXML
    void onBackClick(ActionEvent event) {
        try {
            Node mainRouter = this.backButton.getScene().getRoot().lookup("#mainRouter");
            if (mainRouter == null) {
                throw new Exception("mainRouter not found in scene graph");
            }

            SceneSwitch.switchScene((Pane)mainRouter, "/AfficherDonations.fxml");
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText((String)null);
            alert.setContentText("Failed to navigate back: " + e.getMessage());
            alert.showAndWait();
        }

    }
}
