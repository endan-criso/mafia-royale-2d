package Render;

import Logs.Logger;
import Networking.Client;
import Networking.Server;
import Player.Player;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Optional;

public class MenuSection {

    private javafx.application.HostServices hostServices;
    private int GAME_PORT = 9893;
    private Server server;

    //Thread checker
    private boolean serverThreadcheck;

    //========== MENU ==========
    private StackPane container;
    private VBox menuBox;
    private VBox topleftContainer;
    private String playerName;
    private Label nameDisplay;// Default name
    private Stage stage;

    public MenuSection(Stage stage, javafx.application.HostServices hostServices) {
        this.stage = stage;
        this.hostServices = hostServices;
        playerName = "Guest";
        createLayout();
    }

    private void createLayout(){
        container = new StackPane();

        //Create a "Background"
        Region background = new Region();
        background.setStyle("-fx-background-color: #1a1a1a;");

        //Top-Left Profile (Clickable Label)
        setUpProfileData();
        topleftContainer = new VBox(nameDisplay);
        topleftContainer.setPadding(new Insets(20));
        StackPane.setAlignment(topleftContainer, Pos.TOP_LEFT);


        menuBox = new VBox(15);
        menuBox.setAlignment(Pos.CENTER);
        menuBox.prefWidthProperty().bind(container.widthProperty().multiply(0.3));

        topleftContainer.setPickOnBounds(false);
        menuBox.setPickOnBounds(false);
        showMainMenu();

        // Add everything to the StackPane
        container.getChildren().addAll(background, topleftContainer, menuBox);

        //Ensure the container StackPane allows clicks to pass through to children
        container.setPickOnBounds(false);
    }

    private void showMainMenu(){
        menuBox.getChildren().clear();

        //Create the Title
        Label title = new Label("BATTLE ROYALE");
        title.setFont(Font.font("Verdana", FontWeight.BOLD, 40));
        title.setTextFill(Color.WHITE);
        title.setStyle("-fx-padding: 0 0 50 0;");

        //Create Styled Buttons
        Button btnStart = createMenuButton("START MATCH");
        Button btnCredits = createMenuButton("CREDITS");
        Button btnExit = createMenuButton("EXIT GAME");

        btnStart.setOnAction(e -> showStartOptions());
        btnCredits.setOnAction(e -> showCredits());
        btnExit.setOnAction(e -> stage.close());

        menuBox.getChildren().addAll(title, btnStart, btnCredits, btnExit);
    }

    private void showCredits() {
        menuBox.getChildren().clear();
        menuBox.setAlignment(Pos.CENTER);

        // Title
        Label creditsTitle = new Label("CREDITS");
        creditsTitle.setFont(Font.font("Verdana", FontWeight.BOLD, 32));
        creditsTitle.setTextFill(Color.web("#2ecc71"));

        // Card container
        VBox card = new VBox(15);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(25));
        card.setMaxWidth(420);
        card.setStyle(
                "-fx-background-color: rgba(255,255,255,0.08);" +
                        "-fx-background-radius: 12;"
        );

        // ===== Music Section =====
        Label musicTitle = sectionTitle("MUSIC");

        HBox musicWrap = new HBox();
        Label musicLabel = sectionText("Intro by Nicholas Panek  ");
        Hyperlink pixabayLink = createLink(
                "visit Profile",
                "pixabay.com/users/nickpanek-38266323/?utm_source=link-attribution&utm_medium=referral&utm_campaign=music&utm_content=318059"
        );
        musicWrap.getChildren().addAll(musicLabel, pixabayLink);

        // ===== Developer Section =====
        Label devTitle = sectionTitle("DEVELOPER");

        Hyperlink githubLink = createLink(
                "Made with ❤ by endan-criso",
                "https://github.com/endan-criso"
        );

        // Back Button
        Button btnBack = createMenuButton("BACK");
        btnBack.setOnAction(e -> resetToMainMenu());

        card.getChildren().addAll(
                new Separator(),
                musicTitle, musicWrap,
                new Separator(),
                devTitle, githubLink
        );

        menuBox.getChildren().addAll(creditsTitle, card, btnBack);
    }
    private Label sectionTitle(String text) {
        Label label = new Label(text);
        label.setFont(Font.font("Verdana", FontWeight.BOLD, 14));
        label.setTextFill(Color.web("#95a5a6"));
        return label;
    }

    private Label sectionText(String text) {
        Label label = new Label(text);
        label.setFont(Font.font("Verdana", 15));
        label.setTextFill(Color.WHITE);
        return label;
    }

    private Hyperlink createLink(String text, String url) {
        Hyperlink link = new Hyperlink(text);
        link.setFont(Font.font("Verdana", 15));
        link.setTextFill(Color.web("#3498db"));
        link.setOnAction(e -> hostServices.showDocument(url));
        return link;
    }



    private void setUpProfileData(){
        nameDisplay = new Label("ID "+ playerName + "  [EDIT]");
        nameDisplay.setTextFill(Color.web("#2ecc71"));
        nameDisplay.setFont(Font.font("Verdana", FontWeight.BOLD, 14));
        nameDisplay.setStyle("-fx-cursor: hand; -fx-background-color: rgba(255,255,255,0.1); -fx-padding: 10; -fx-background-radius: 5;");

        // Trigger the Pop-up when clicked
        nameDisplay.setOnMouseClicked(e -> showNamepop());

    }

    private void showStartOptions(){

        menuBox.getChildren().clear();
        topleftContainer.setVisible(true);

        // Create Title
        Label subTitle = new Label("CHOOSE MODE");
        subTitle.setFont(Font.font("Verdana", FontWeight.BOLD, 30));
        subTitle.setTextFill(Color.WHITE);

        Button btnJoin = createMenuButton("JOIN SERVER");
        Button btnCreate = createMenuButton("CREATE SERVER");
        Button btnBack = createMenuButton("BACK");

        btnJoin.setOnAction(e -> searchServer());// Go to the list of servers
        btnCreate.setOnAction(e -> createServer());
        btnBack.setOnAction(e -> resetToMainMenu());   // Go back to Start/Exit

        menuBox.getChildren().addAll(subTitle, btnJoin, btnCreate, btnBack);
    }

    private void searchServer(){

        menuBox.getChildren().clear();

        Label browserTitle = new Label("SEARCHING FOR SERVER");
        browserTitle.setFont(Font.font("Verdana", FontWeight.BOLD, 30));
        browserTitle.setTextFill(Color.WHITE);

        ListView<String> serverList = new ListView<>();
        serverList.setMaxWidth(300);
        serverList.setMaxHeight(200);

        // Add some fake servers for now so you can see it working
        serverList.getItems().addAll("Localhost: 127.0.0.1", "Public Server: 192.168.1.5", "Jarvis Dev Room");

        Button btnConnect = createMenuButton("CONNECT");
        btnConnect.setDisable(true); // Disable until a server is picked

        serverList.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) btnConnect.setDisable(false);
        });

        btnConnect.setOnAction(e -> {
            String selected = serverList.getSelectionModel().getSelectedItem();
            String host = selected.split(":")[1].trim();
            // TRIGGER CLIENT LOGIC HERE
            try{
                Client client = new Client();
                client.connect(host, GAME_PORT, playerName);
                // Hide menu and show game
                this.container.setVisible(false);
            } catch (IOException ex) {
                Logger.get().error("Failed to create a client");
                throw new RuntimeException(ex);
            }
        });

        Button btnBack = createMenuButton("BACK");
        btnBack.setOnAction(e -> showStartOptions());

        menuBox.getChildren().addAll(browserTitle, serverList, btnConnect, btnBack);
    }

    private void createServer(){

        menuBox.getChildren().clear();
        topleftContainer.setVisible(false);

        Label title = new Label("HOST NEW MATCH");
        title.setFont(Font.font("Verdana", FontWeight.BOLD, 25));
        title.setTextFill(Color.WHITE);

        // Player Count Slider (3 to 50)
        Label playerLabel = new Label("Max Players: 20");
        playerLabel.setTextFill(Color.GRAY);
        playerLabel.setFont(Font.font("Verdana", FontWeight.BOLD, 15));
        Slider playerSlider = new Slider(3, 50, 20);
        playerSlider.setShowTickLabels(true);
        playerSlider.valueProperty().addListener((obs, oldVal, newVal) ->
                playerLabel.setText("Max Players: " + newVal.intValue()));

        Button btnLaunch = createMenuButton("LAUNCH SERVER");
        btnLaunch.setOnAction(e -> {
            if(serverThreadcheck) return;
            try {
                server = new Server();
                server.configure((int)playerSlider.getValue(), 1);

                Thread serverThread = new Thread(() -> {
                   try {
                       serverThreadcheck = true;
                       server.start(GAME_PORT);
                       Logger.get().info("Server Thread Started");
                   } catch (IOException ex) {
                       Logger.get().error("FAILED TO CREATE A SERVER PORT");
                       ex.printStackTrace();
                   }
                });
                serverThread.setDaemon(true);
                serverThread.start();
                Thread.sleep(100);
                joinLocalServer();

            } catch (Exception ex) {
                serverThreadcheck = false;
                throw new RuntimeException(ex);
            }
        });

        Button btnBack = createMenuButton("BACK");
        btnBack.setOnAction(e -> {
                if(server != null)
                {
                    serverThreadcheck = false;
                    server.closeServer();
                }
                showStartOptions();});

        menuBox.getChildren().addAll(title, playerLabel, playerSlider, btnLaunch, btnBack);

    }

    private void joinLocalServer(){
        try{
            Client client = new Client();
            client.connect("127.0.0.1", GAME_PORT, playerName);
            Logger.get().info("SERVER USER SELF JOIN");
        } catch (Exception e) {
            Logger.get().warn("FAILED TO CONNECT THE SERVER AND HOST TOGETHER");
            e.printStackTrace();
        }
    }

    private void resetToMainMenu(){
        menuBox.getChildren().clear();
        topleftContainer.setVisible(true);
        showMainMenu();
    }

    public Pane getRoot() {
        return container;
    }

    private void showNamepop(){
        // Create the JavaFX Pop-up Dialog
        TextInputDialog dialog = new TextInputDialog(playerName);
        dialog.setTitle("Player Profile");
        dialog.setContentText("Enter Name: ");

        // Add custom styling
        dialog.getDialogPane().setStyle("-fx-font-family: 'Verdana';");

        Optional<String> result = dialog.showAndWait();

        result.ifPresent(name -> {
            if(!name.trim().isEmpty())
            {
                this.playerName = name;
                this.nameDisplay.setText("ID: " + playerName + " [EDIT]");
            }
        });

    }


    private Button createMenuButton(String text)
    {
        Button btn = new Button(text);
        btn.setPrefWidth(250);
        btn.setStyle(
                "-fx-background-color: #34495e; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-size: 16px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-background-radius: 5; " +
                        "-fx-cursor: hand;"
        );
        // Hover Effect
        btn.setOnMouseEntered(e -> btn.setStyle(btn.getStyle() + "-fx-background-color: #2ecc71;"));
        btn.setOnMouseExited(e -> btn.setStyle(btn.getStyle().replace("-fx-background-color: #2ecc71;", "-fx-background-color: #34495e;")));

        return btn;
    }
}
