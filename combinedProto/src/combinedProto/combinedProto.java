package combinedProto;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.File;
import java.nio.file.attribute.PosixFilePermission;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Random;
import java.io.BufferedReader;
import java.io.FileReader;
public class combinedProto extends Application {
	private final Map<String, User> userCredentials = new HashMap<>();
	private BorderPane mainLayout;
	private TabPane tabPane;
	private CheckBox readPermissions;
	private CheckBox writePermissions;
	private Label messageLabel;
	private File uploadedFile; // To keep track of the uploaded file
	private TextField uploadedFileNameField; // To display the name of the uploaded file
	private Label lengthRequirementLabel;
	private Label uppercaseRequirementLabel;
	private Label specialCharRequirementLabel;
	private Label passwordMatchLabel;
	public static void main(String[] args) {
		launch(args);
	}
	private String currentMfaCode;
	public void start(Stage primaryStage) {
		mainLayout = new BorderPane();
		setupTabPane();
		currentMfaCode = generateCode();
		System.out.println("The MFA Code is :: " + currentMfaCode);
		Scene scene = new Scene(mainLayout, 400, 400);
		primaryStage.setTitle("Effort Logger Interface");
		primaryStage.setScene(scene);
		primaryStage.show();
		
		 User admin = new User("Lynn", "Carter", "LynnRobertCarter", "CSE3602023!", "Admin");
		    userCredentials.put("LynnRobertCarter", admin);
	}
	private void setupTabPane() {
		tabPane = new TabPane();
		Tab homeTab = new Tab("Home");
		homeTab.setContent(createLoginUI());
		tabPane.getTabs().add(homeTab);
		tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
		mainLayout.setCenter(tabPane);
	}
	private VBox createLoginUI() {
		VBox loginLayout = new VBox(10);
		setupLoginLayout(loginLayout);
		GridPane grid = setupLoginGrid();
		TextField userTextField = new TextField();
		PasswordField hiddenPasswordField = new PasswordField();
		TextField visiblePasswordField = new TextField(); // Use this for visible password
		TextField mfaField = new TextField(); // Adding MFA field
		Label errorMsg = setupLoginFieldsAndActions(grid, userTextField, hiddenPasswordField, mfaField);
		Button signUpBtn = setupSignUpButton();
		Button loginButton = new Button("Log In");
		loginButton.setOnAction(event -> loginUser(userTextField, hiddenPasswordField, mfaField, errorMsg));
		// Button to show/hide password
		Button showPasswordBtn = new Button("Show Password");
		HBox usernameBox = new HBox(10);
		usernameBox.getChildren().addAll(new Label("Username:"), userTextField);
		usernameBox.setAlignment(Pos.CENTER_LEFT);
		HBox passwordBox = new HBox(13);
		passwordBox.getChildren().addAll(new Label("Password:"), hiddenPasswordField, showPasswordBtn);
		passwordBox.setAlignment(Pos.CENTER_LEFT);
		visiblePasswordField.setVisible(false);
		showPasswordBtn.setOnAction(event -> toggleFieldVisibility(passwordBox, visiblePasswordField,
				hiddenPasswordField, showPasswordBtn));
		HBox mfaBox = new HBox(8);
		mfaBox.getChildren().addAll(new Label("MFA Code:"), mfaField);
		mfaBox.setAlignment(Pos.CENTER_LEFT);
		HBox buttonBox = new HBox(10);
		buttonBox.setAlignment(Pos.CENTER);
		Button forgotPasswordBtn = new Button("Forgot Password?");
		forgotPasswordBtn.setOnAction(event -> openPasswordRecoveryTab());
		HBox forgotPasswordBox = new HBox(forgotPasswordBtn);
		forgotPasswordBox.setAlignment(Pos.CENTER);
		buttonBox.getChildren().addAll(loginButton, signUpBtn);
		loginLayout.getChildren().addAll(new Label("EffortLogger Login"),
				new Label("Please sign in with your credentials"), usernameBox, passwordBox, mfaBox, buttonBox,
				errorMsg, forgotPasswordBox);
		return loginLayout;
	}
	private void toggleFieldVisibility(HBox passwordBox, TextField visiblePasswordField,
			PasswordField hiddenPasswordField, Button showPasswordBtn) {
		if (hiddenPasswordField.isVisible()) {
			passwordBox.getChildren().add(1, visiblePasswordField);
			passwordBox.getChildren().remove(hiddenPasswordField);
			visiblePasswordField.setVisible(true);
			visiblePasswordField.setText(hiddenPasswordField.getText());
			hiddenPasswordField.setVisible(false);
			showPasswordBtn.setText("Hide Password");
			return;
		} else if (visiblePasswordField.isVisible()) {
			passwordBox.getChildren().add(1, hiddenPasswordField);
			passwordBox.getChildren().remove(visiblePasswordField);
			hiddenPasswordField.setVisible(true);
			visiblePasswordField.setText(hiddenPasswordField.getText());
			showPasswordBtn.setText("Show Password");
			return;
		}
	}
	private void setupLoginLayout(VBox loginLayout) {
		loginLayout.setAlignment(Pos.CENTER);
		loginLayout.setPadding(new Insets(10, 10, 10, 10));
	}
	private GridPane setupLoginGrid() {
		GridPane grid = new GridPane();
		grid.setAlignment(Pos.CENTER);
		grid.setHgap(10);
		grid.setVgap(10);
		grid.setPadding(new Insets(25, 25, 25, 25));
		return grid;
	}
	private Label setupLoginFieldsAndActions(GridPane grid, TextField userTextField, PasswordField pwBox,
			TextField mfaField) {
		grid.addRow(0, new Label("Username:"), userTextField);
		grid.addRow(1, new Label("Password:"), pwBox);
		grid.addRow(2, new Label("Enter MFA Code:"), mfaField); // MFA input row
		Label errorMsg = new Label();
		errorMsg.setStyle("-fx-text-fill: red;");
		return errorMsg;
	}
	private Button setupSignUpButton() {
		Button signUpBtn = new Button("Sign Up");
		signUpBtn.setOnAction(event -> openSignUpTab());
		return signUpBtn;
	}
	public String generateCode() {
		Random random = new Random();
		int mfaCode = 1000 + random.nextInt(9000);
		return String.valueOf(mfaCode);
	}
	public void loginUser(TextField userTextField, PasswordField pwBox, TextField mfaField, Label errorMsg) {
		String username = userTextField.getText().trim();
		String password = pwBox.getText().trim();
//	        String enteredMFA = mfaField.getText().trim();
		if (userCredentials.containsKey(username)) {
			User user = userCredentials.get(username);
			if (user.getPassword().equals(password)) {
				if (currentMfaCode.equals(mfaField.getText().trim())) {
					displayLoggedInUI(user.getFirstName(), user.getLastName(), user.getRole());
				} else {
					errorMsg.setText("Invalid MFA code. Try again.");
				}
			} else {
				errorMsg.setText("Invalid password. Try again.");
			}
		} else {
			errorMsg.setText("Username not found. Try again or sign up.");
		}
	}
	private VBox createSignUpUI() {
		VBox signUpLayout = new VBox(10);
		signUpLayout.setAlignment(Pos.CENTER);
		signUpLayout.setPadding(new Insets(10, 10, 10, 10));
		GridPane grid = new GridPane();
		grid.setAlignment(Pos.CENTER);
		grid.setHgap(10);
		grid.setVgap(10);
		grid.setPadding(new Insets(25, 25, 25, 25));
		TextField firstNameTextField = new TextField();
		TextField lastNameTextField = new TextField();
		TextField userNameTextField = new TextField();
		PasswordField hiddenPasswordField = new PasswordField();
		TextField visiblePasswordField = new TextField(); // Use this for visible password
		PasswordField passwordField = new PasswordField();
		PasswordField confirmPasswordField = new PasswordField();
		Button showPasswordBtn = new Button("Show Password");
		showPasswordBtn.setOnAction(
				event -> toggleFieldVisibility(grid, visiblePasswordField, hiddenPasswordField, showPasswordBtn));
		ChoiceBox<String> roleChoice = new ChoiceBox<>();
		roleChoice.getItems().addAll("-", "Admin", "Employee");
		roleChoice.getSelectionModel().selectFirst();
		grid.addRow(0, new Label("Role:"), roleChoice);
		grid.addRow(1, new Label("First Name:"), firstNameTextField);
		grid.addRow(2, new Label("Last Name:"), lastNameTextField);
		grid.addRow(3, new Label("Username:"), userNameTextField);
		grid.addRow(4, new Label("Password:"), hiddenPasswordField, showPasswordBtn);
		grid.addRow(5, new Label("Confirm Password:"), confirmPasswordField);
		
		// Initialize password requirement labels
      lengthRequirementLabel = new Label("Minimum Length: 7 ✘");
      uppercaseRequirementLabel = new Label("Uppercase Letter ✘");
      specialCharRequirementLabel = new Label("Special Character ✘");
      // Initialize the password match label and set it to be initially invisible
      passwordMatchLabel = new Label("Passwords Match ✘");
      passwordMatchLabel.setStyle("-fx-text-fill: red;");
      passwordMatchLabel.setVisible(false);
      // Add the password requirement labels to the grid
      grid.add(lengthRequirementLabel, 2, 6);
      grid.add(uppercaseRequirementLabel, 2, 7);
      grid.add(specialCharRequirementLabel, 2, 8);
      grid.add(passwordMatchLabel, 2, 9);
      // Set listeners to the password and confirm password fields
      hiddenPasswordField.setOnKeyReleased(event -> updatePasswordCriteriaLabels(hiddenPasswordField.getText(), confirmPasswordField.getText()));
      confirmPasswordField.setOnKeyReleased(event -> updatePasswordCriteriaLabels(hiddenPasswordField.getText(), confirmPasswordField.getText()));
      confirmPasswordField.focusedProperty().addListener((observable, oldValue, newValue) -> {
          passwordMatchLabel.setVisible(newValue); // Toggle visibility based on focus
      });
    
		Button signUpButton = new Button("Sign Up");
		signUpButton.setOnAction(event -> signUpUser(firstNameTextField, lastNameTextField, userNameTextField,
				hiddenPasswordField, confirmPasswordField, roleChoice));
		signUpLayout.getChildren().addAll(new Label("EffortLogger Sign Up"),
				new Label("Please sign up with your credentials"), grid, signUpButton);
		return signUpLayout;
	}
	private void toggleFieldVisibility(GridPane grid, TextField visiblePasswordField, PasswordField hiddenPasswordField,
			Button showPasswordBtn) {
		if (hiddenPasswordField.isVisible()) {
			grid.add(visiblePasswordField, 1, 3);
			grid.getChildren().remove(hiddenPasswordField);
			visiblePasswordField.setVisible(true);
			visiblePasswordField.setText(hiddenPasswordField.getText());
			hiddenPasswordField.setVisible(false);
			showPasswordBtn.setText("Hide Password");
			return;
		} else if (visiblePasswordField.isVisible()) {
			grid.add(hiddenPasswordField, 1, 3);
			grid.getChildren().remove(visiblePasswordField);
			hiddenPasswordField.setVisible(true);
			visiblePasswordField.setText(hiddenPasswordField.getText());
			showPasswordBtn.setText("Show Password");
			return;
		}
	}
	
	private void updatePasswordCriteriaLabels(String hiddenPasswordField, String confirmPasswordField) {
	        // Length requirement
	        if (hiddenPasswordField.length() >= 7) {
	            lengthRequirementLabel.setText("Minimum Length: 7 ✔");
	            lengthRequirementLabel.setStyle("-fx-text-fill: green;");
	        } else {
	            lengthRequirementLabel.setText("Minimum Length: 7 ✘");
	            lengthRequirementLabel.setStyle("-fx-text-fill: red;");
	        }
	        // Uppercase letter requirement
	        if (hiddenPasswordField.matches(".*[A-Z].*")) {
	            uppercaseRequirementLabel.setText("Uppercase Letter ✔");
	            uppercaseRequirementLabel.setStyle("-fx-text-fill: green;");
	        } else {
	            uppercaseRequirementLabel.setText("Uppercase Letter ✘");
	            uppercaseRequirementLabel.setStyle("-fx-text-fill: red;");
	        }
	        // Special character requirement
	        if (hiddenPasswordField.matches(".*[^A-Za-z0-9].*")) {
	            specialCharRequirementLabel.setText("Special Character ✔");
	            specialCharRequirementLabel.setStyle("-fx-text-fill: green;");
	        } else {
	            specialCharRequirementLabel.setText("Special Character ✘");
	            specialCharRequirementLabel.setStyle("-fx-text-fill: red;");
	        }
	        // Password match requirement
	        if (hiddenPasswordField.equals(confirmPasswordField)) {
	            passwordMatchLabel.setText("Passwords Match ✔");
	            passwordMatchLabel.setStyle("-fx-text-fill: green;");
	        } else {
	            passwordMatchLabel.setText("Passwords Match ✘");
	            passwordMatchLabel.setStyle("-fx-text-fill: red;");
	        }
	    
	}
	public class User {
		private String firstName;
		private String lastName;
		private String username;
		private String password;
		private String role;
		public User(String firstName, String lastName, String username, String password, String role) {
			this.firstName = firstName;
			this.lastName = lastName;
			this.username = username;
			this.password = password;
			this.role = role;
		}
		// Getters
		public String getFirstName() {
			return firstName;
		}
		public String getLastName() {
			return lastName;
		}
		public String getUsername() {
			return username;
		}
		public String getPassword() {
			return password;
		}
		public String getRole() {
			return role;
		}
		public void setPassword(String password) {
			this.password = password;
		}
	}
	private void signUpUser(TextField firstNameTextField, TextField lastNameTextField, TextField userNameTextField,
	        PasswordField passwordField, PasswordField confirmPasswordField, ChoiceBox<String> roleChoice) {
	    String firstName = firstNameTextField.getText().trim();
	    String lastName = lastNameTextField.getText().trim();
	    String username = userNameTextField.getText().trim();
	    String password = passwordField.getText().trim();
	    String confirmPassword = confirmPasswordField.getText().trim();
	    String role = roleChoice.getValue();

	    // Check if the role is not selected
	    if ("-".equals(role)) {
	        showAlert(Alert.AlertType.ERROR, "Signup Error", "Please pick a role.");
	        return;
	    }

	    // Check if the username or password is empty
	    if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || firstName.isEmpty()
	            || lastName.isEmpty()) {
	        showAlert(Alert.AlertType.ERROR, "Signup Error", "All fields must be filled out.");
	        return;
	    }

	    // Check if the passwords match
	    if (!password.equals(confirmPassword)) {
	        showAlert(Alert.AlertType.ERROR, "Signup Error", "Passwords do not match.");
	        return;
	    }

	    // Check if the password meets the criteria
	    if (!password.matches("^(?=.*[A-Z])(?=.*[^A-Za-z0-9]).{7,}$")) {
	        showAlert(Alert.AlertType.ERROR, "Signup Error", "Password does not meet the criteria.");
	        return;
	    }

	    // Check if the username already exists
	    if (userCredentials.containsKey(username)) {
	        showAlert(Alert.AlertType.ERROR, "Signup Error", "Username already exists. Choose a different one.");
	        return;
	    }

	    // Check if the selected role is Admin and an Admin already exists
	    if ("Admin".equals(role)) {
	        showAlert(Alert.AlertType.ERROR, "Signup Error", "An Admin already exists in the system. Please go to the Log In page.");
	        return;
	    }

	    // Create a new user and add it to the userCredentials map
	    User newUser = new User(firstName, lastName, username, password, role);
	    userCredentials.put(username, newUser);

	    // Save user details to file
	    saveUserDetailsToFile(username, password, role);

	    // Show success message
	    showAlert(Alert.AlertType.INFORMATION, "Signup Success", "Account created successfully. You can now log in.");

	    // Close the current SignUp tab
	    tabPane.getTabs().remove(tabPane.getSelectionModel().getSelectedItem());

	    // Go back to the Login tab
	    tabPane.getSelectionModel().selectFirst();
	}
	private void openPasswordRecoveryTab() {
		Tab recoveryTab = new Tab("Password Recovery");
		recoveryTab.setContent(createPasswordRecoveryUI());
		tabPane.getTabs().add(recoveryTab);
		tabPane.getSelectionModel().select(recoveryTab);
	}
	private VBox createPasswordRecoveryUI() {
		VBox recoveryLayout = new VBox(10);
		recoveryLayout.setAlignment(Pos.CENTER);
		recoveryLayout.setPadding(new Insets(10, 10, 10, 10));
		TextField usernameOrEmailField = new TextField();
		Button recoverPasswordBtn = new Button("Recover Password");
		recoverPasswordBtn.setOnAction(event -> recoverPassword(usernameOrEmailField.getText()));
		recoveryLayout.getChildren().addAll(new Label("Password Recovery"), new Label("Enter your username"),
				usernameOrEmailField, recoverPasswordBtn);
		return recoveryLayout;
	}
	private void recoverPassword(String username) { // Check if the username exists
		if (userCredentials.containsKey(username)) {
			User user = userCredentials.get(username);
			// Generate a temporary password (this is a simple example, you should use a
			// more secure method)
			String tempPassword = generateTemporaryPassword();
			
			user.setPassword(tempPassword);
			
			showAlert(Alert.AlertType.INFORMATION, "Password Recovery", "Your temporary password is: " + tempPassword);
		} else {
			showAlert(Alert.AlertType.ERROR, "Error", "User not found.");
		}
	}
	private String generateTemporaryPassword() {
		// Simple password generator - you should replace this with a more secure method
		int length = 8;
		String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
		StringBuilder tempPassword = new StringBuilder();
		for (int i = 0; i < length; i++) {
			int index = (int) (Math.random() * chars.length());
			tempPassword.append(chars.charAt(index));
		}
		return tempPassword.toString();
	}
	private void displayLoggedInUI(String firstName, String lastName, String role) {
		// Home tab layout
		VBox homeLayout = new VBox(10);
		homeLayout.setAlignment(Pos.CENTER);
		Label welcomeLabel = new Label("Welcome, " + lastName + ", " + firstName + ": " + role);
		homeLayout.getChildren().add(welcomeLabel);
		// Home Tab
		Tab homeTab = new Tab("Home");
		homeTab.setContent(homeLayout);
		// EffortLogger tab layout
		GridPane effortLoggerLayout = new GridPane();
		effortLoggerLayout.setAlignment(Pos.CENTER);
		effortLoggerLayout.setHgap(10);
		effortLoggerLayout.setVgap(10);
		effortLoggerLayout.setPadding(new Insets(10, 10, 10, 10));
		// Add components to the effortLoggerLayout
		addEffortLoggerComponents(effortLoggerLayout);
		// Effort Logger Tab
		Tab effortLoggerTab = new Tab("Effort Logger");
		effortLoggerTab.setContent(new ScrollPane(effortLoggerLayout)); // Wrap in a ScrollPane for larger content
		// Clear any existing tabs and add the new tabs
		tabPane.getTabs().clear();
		tabPane.getTabs().addAll(homeTab, effortLoggerTab); // Add both tabs
		// Select the Home tab by default
		tabPane.getSelectionModel().select(homeTab);
		setupLogoutButton();
		
		 if ("Admin".equals(role)) {
		        displayAllEmployees();
		    }
	}
	
	private void displayAllEmployees() {
	    // Create a new stage to display employee details
	    Stage employeeStage = new Stage();
	    employeeStage.setTitle("Employee Details");

	    // Create a TableView to display employee details
	    TableView<User> tableView = new TableView<>();
	    TableColumn<User, String> firstNameCol = new TableColumn<>("First Name");
	    TableColumn<User, String> lastNameCol = new TableColumn<>("Last Name");
	    TableColumn<User, String> usernameCol = new TableColumn<>("Username");
	    TableColumn<User, String> roleCol = new TableColumn<>("Role");

	    // Set cell value factories for each column
	    firstNameCol.setCellValueFactory(new PropertyValueFactory<>("firstName"));
	    lastNameCol.setCellValueFactory(new PropertyValueFactory<>("lastName"));
	    usernameCol.setCellValueFactory(new PropertyValueFactory<>("username"));
	    roleCol.setCellValueFactory(new PropertyValueFactory<>("role"));

	    // Add columns to the TableView
	    tableView.getColumns().addAll(firstNameCol, lastNameCol, usernameCol, roleCol);

	    // Create an ObservableList to hold the user data
	    ObservableList<User> userData = FXCollections.observableArrayList(userCredentials.values());
	    User newUser1 = new User("Meghana", "EdigaChoutukur", "medigachoutukur", "CSE3602023!", "Employee");
	    userData.add(newUser1);
	    User newUser2 = new User("Rudresh", "Bhandari", "rbhandari", "CSE3602023!", "Employee");
	    userData.add(newUser2);
	    User newUser3 = new User("Suyong", "Choi", "schoi", "CSE3602023!", "Employee");
	    userData.add(newUser3);
	    User newUser4 = new User("Dalton", "Berg", "dberg", "CSE3602023!", "Employee");
	    userData.add(newUser4);
	    User newUser5 = new User("Brendan", "Hoover", "bhoover", "CSE3602023!", "Employee");
	    userData.add(newUser5);
	  

	    // Set the user data to the TableView
	    tableView.setItems(userData);

	    // Create a scene and set it in the stage
	    Scene scene = new Scene(new StackPane(tableView), 400, 400);
	    employeeStage.setScene(scene);

	    // Show the stage
	    employeeStage.show();
	}

	private void addEffortLoggerComponents(GridPane grid) {
		// TextFields for the name and ID
		TextField firstNameField = new TextField();
		TextField lastNameField = new TextField();
		TextField employeeIdField = new TextField();
		ComboBox<String> projectListComboBox = new ComboBox<>();
		projectListComboBox.getItems().addAll("Project 1", "Project 2");
		DatePicker startDatePicker = new DatePicker();
		DatePicker endDatePicker = new DatePicker();
		TextField daysField = new TextField();
		TextField hoursField = new TextField();
		Button uploadFileButton = new Button("Upload File");
		uploadFileButton.setOnAction(event -> uploadFile(grid)); // Existing upload file method
		Button submitButton = new Button("Submit");
		// submitButton.setOnAction(event -> submitEffort()); // Implement this method
		// for submitting the effort
		// Arrange components in the grid
		grid.add(new Label("First Name"), 0, 0);
		grid.add(firstNameField, 1, 0);
		grid.add(new Label("Last Name"), 2, 0);
		grid.add(lastNameField, 3, 0);
		grid.add(new Label("Employee ID#"), 0, 1);
		grid.add(employeeIdField, 1, 1);
		grid.add(new Label("Project List"), 0, 2);
		grid.add(projectListComboBox, 1, 2);
		grid.add(new Label("Date Started"), 0, 3);
		grid.add(startDatePicker, 1, 3);
		grid.add(new Label("Date Ended"), 2, 3);
		grid.add(endDatePicker, 3, 3);
		grid.add(new Label("Days"), 0, 4);
		grid.add(daysField, 1, 4);
		grid.add(new Label("Hours"), 2, 4);
		grid.add(hoursField, 3, 4);
		grid.add(uploadFileButton, 0, 5, 2, 1);
		grid.add(submitButton, 2, 5, 2, 1);
	}
	private void saveUserDetailsToFile(String username, String password, String role) {
		String userDetail = username + " " + password + " " + role + System.lineSeparator();
		File file = new File("userDetails.txt");
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, true))) {
			writer.write(userDetail);
			writer.flush(); // Make sure to flush the stream.
		} catch (IOException e) {
			System.out.println("An error occurred while writing to the file.");
			e.printStackTrace();
			return;
		}
		// Read the file contents back to ensure the data was written.
		try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
			String line;
			while ((line = reader.readLine()) != null) {
				System.out.println("File contains: " + line);
			}
		} catch (IOException e) {
			System.out.println("An error occurred while reading from the file.");
			e.printStackTrace();
		}
	}
	private void showAlert(Alert.AlertType alertType, String title, String content) {
		Alert alert = new Alert(alertType);
		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.setContentText(content);
		alert.showAndWait();
	}
	private void openSignUpTab() {
		Tab signUpTab = new Tab("Sign Up");
		signUpTab.setContent(createSignUpUI());
		tabPane.getTabs().add(signUpTab);
		tabPane.getSelectionModel().select(signUpTab);
	}
	private void setupLogoutButton() {
		Button logoutButton = new Button("Log Out");
		logoutButton.setOnAction(event -> {
			tabPane.getTabs().clear();
			Tab homeTab = new Tab("Home");
			homeTab.setContent(createLoginUI());
			tabPane.getTabs().add(homeTab);
		});
		mainLayout.setTop(logoutButton);
	}
	private void uploadFile(GridPane effortLoggerLayout) {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Open Resource File");
		// Set extension filter for PDF files
		FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("PDF files (*.pdf)", "*.pdf");
		fileChooser.getExtensionFilters().add(extFilter);
		File selectedFile = fileChooser.showOpenDialog(null);
		if (selectedFile != null) {
			// Create a new row in the GridPane for the uploaded file
			int nextRow = getNextEmptyRow(effortLoggerLayout); // This method needs to find the next empty row in the
																// GridPane
			Label fileNameLabel = new Label(selectedFile.getName());
			Button cpButton = new Button("Change Permissions ");
			cpButton.setOnAction(event -> displayFilePermissionsUI(mainLayout));
			// Add the new row with the file name label and the CP button
			effortLoggerLayout.add(fileNameLabel, 0, nextRow);
			effortLoggerLayout.add(cpButton, 1, nextRow);
			System.out.println("File uploaded: " + selectedFile.getAbsolutePath());
		} else {
			System.out.println("File selection cancelled.");
		}
	}
	private void displayFilePermissionsUI(BorderPane layout) {
		// Clear the previous UI components
		layout.setTop(null);
		layout.setBottom(null);
		layout.setLeft(null);
		layout.setRight(null);
		layout.setCenter(null);
		readPermissions = new CheckBox("Read Permissions");
		writePermissions = new CheckBox("Write Permissions");
		Button setPermissionsButton = new Button("Set the Permissions");
		setPermissionsButton.setOnAction(e -> setFilePermissions());
		Button auditButton = new Button("Audit File Permissions");
		auditButton.setOnAction(e -> auditPermissions());
		messageLabel = new Label("");
		Button goBackBtn = new Button("Go Back");
		goBackBtn.setOnAction(event -> {
			layout.setCenter(createLoginUI());
		});
		// Create a layout for the file permissions UI components
		VBox permissionsLayout = new VBox(10, readPermissions, writePermissions, setPermissionsButton, auditButton,
				messageLabel, goBackBtn);
		permissionsLayout.setAlignment(Pos.CENTER);
		permissionsLayout.setPadding(new Insets(10));
		// Set the file permissions UI to the center of the BorderPane
		layout.setCenter(permissionsLayout);
	}
	// New method to set file permissions
	private void setFilePermissions() {
		Set<PosixFilePermission> permissions = new HashSet<>();
		if (readPermissions.isSelected())
			permissions.add(PosixFilePermission.OWNER_READ);
		if (writePermissions.isSelected())
			permissions.add(PosixFilePermission.OWNER_WRITE);
		// if (executePermissions.isSelected())
		// permissions.add(PosixFilePermission.OWNER_EXECUTE);
		setMessage("Permissions set to: " + permissions);
	}
	// New method to audit file permissions
	private void auditPermissions() {
		if (writePermissions.isSelected()) {
			setMessage("Security risk! Write permissions are enabled.");
		} else {
			setMessage("No security risks detected.");
		}
	}
	private void setMessage(String message) {
		messageLabel.setText(message);
	}
	// Utility method to find the next empty row in the GridPane
	private int getNextEmptyRow(GridPane grid) {
		int rowNum = 0;
		for (Node node : grid.getChildren()) {
			if (GridPane.getRowIndex(node) != null) {
				rowNum = Math.max(rowNum, GridPane.getRowIndex(node) + 1);
			}
		}
		return rowNum;
	}
}


