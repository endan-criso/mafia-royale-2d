package Render;

import Logs.Logger;
import Networking.Client;
import Networking.Server;
import Networking.ServerState;
import Packet.LobbyPacket;
import Player.Player;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;

public class MenuSection {

    //NETWORKING
    private final int DISCOVERY_PORT = 8888;
    private int GAME_PORT = 9173;
    private Server server;
    private Client client;
    private javafx.application.HostServices hostServices;
    private final Map<String, Long> discoveredServers = new java.util.concurrent.ConcurrentHashMap<>();
    private static final long TIMEOUT_MS = 5000; // 5 seconds

    //Thread checker
    private boolean serverThreadcheck = false;
    private volatile boolean discoveryRunning = false;

    //========== MENU ==========
    private StackPane container;
    private VBox menuBox;
    private VBox topleftContainer;
    private String playerName;
    private VBox playerListArea; // To hold the names dynamically
    private Label statusLabel;
    private Label nameDisplay;// Default name
    private Stage stage;
    private boolean drawLobby = false; //Lobby running

    //Player
    private Long host = null;

    //Lobby Method
    ListView<String> lobbyList;
    Button btnStart;

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
                "https://pixabay.com/users/nickpanek-38266323/?utm_source=link-attribution&utm_medium=referral&utm_campaign=music&utm_content=318059"
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
        nameDisplay = new Label("ID: " + playerName + "  [EDIT]");
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

        //START DISCOVERY HERE
        discoveryRunning = true;
        startCatchDiscoveryThread(serverList);
        startServerCleanUp(serverList);
        Button btnConnect = createMenuButton("CONNECT");
        btnConnect.setDisable(true); // Disable until a server is picked

        serverList.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) btnConnect.setDisable(false);
        });

        btnConnect.setOnAction(e -> {
            String selected = serverList.getSelectionModel().getSelectedItem();
            if (selected != null && selected.contains(":")) {
                String[] parts = selected.split(":");
                String host = parts[0];
                GAME_PORT = Integer.parseInt(parts[1]);
                // TRIGGER CLIENT LOGIC HERE
                try{
                    client = new Client();
                    client.connect(host, GAME_PORT, playerName);
                    // Hide menu and show game
                    discoveryRunning = false;
                    client.setMenu(this);
                } catch (IOException ex) {
                    Logger.get().error("Failed to create a client");
                    showErrorDialog("Connection Failed", "Could not connect to server: " + ex.getMessage());
                }
            }
        });

        Button btnBack = createMenuButton("BACK");
        btnBack.setOnAction(e -> {
            discoveryRunning = false;
            showStartOptions();
        });

        menuBox.getChildren().addAll(browserTitle, serverList, btnConnect, btnBack);
    }

    private void startCatchDiscoveryThread(ListView<String> serverList){
        Thread discoverThread = new Thread(() -> {
            Logger.get().info("SUCCESSFULLY STARTED: " + Thread.currentThread().getName());

            try(DatagramSocket socket1 = new DatagramSocket(DISCOVERY_PORT)){
                socket1.setBroadcast(true); // allow receiving broadcast
                byte[] buffer = new byte[256];

                while (discoveryRunning && !Thread.currentThread().isInterrupted())
                {
                    DatagramPacket pck = new DatagramPacket(buffer, buffer.length);
                    socket1.receive(pck);

                    String msg = new String(
                            pck.getData(),
                            0,
                            pck.getLength()
                    );

                    if(msg.startsWith("BATTLE_MAFIA_SERVER:")){
                        String host = pck.getAddress().getHostAddress();
                        int port = Integer.parseInt(msg.split(":")[1]);
                        String entry = host + ":" + port;

                        long currentTime = System.currentTimeMillis();
                        discoveredServers.put(entry, currentTime);
                        javafx.application.Platform.runLater(() -> {
                            if(!serverList.getItems().contains(entry)) serverList.getItems().add(entry);
                        });
                    }

                }

            } catch (Exception e) {
                Logger.get().error("FAILED TO START: " + Thread.currentThread().getName());
            }
        });
        discoverThread.setDaemon(true);
        discoverThread.setName("discoverThread");
        discoverThread.start();
    }

    private void startServerCleanUp(ListView<String> serverList){
        Thread cleanUp = new Thread(() -> {
            try {
                Logger.get().info(Thread.currentThread().getName() + " has started");
                while (discoveryRunning) {
                    long current = System.currentTimeMillis();
                    Iterator<Map.Entry<String, Long>> it = discoveredServers.entrySet().iterator();
                    while(it.hasNext()){
                        Map.Entry<String, Long> entry = it.next();
                        if (current - entry.getValue() > TIMEOUT_MS) {
                            String server = entry.getKey();
                            it.remove();

                            javafx.application.Platform.runLater(() -> {
                                serverList.getItems().remove(server);
                            });
                        }
                    }
                    try{Thread.sleep(3000);}
                    catch (InterruptedException e) {break;}
                }
            } catch (Exception e) {
                Logger.get().error(Thread.currentThread().getName() + " Force stopped");
            }
        });
        cleanUp.setDaemon(true);
        cleanUp.setName("CLEAN_LISTING_SERVER");
        cleanUp.start();
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
        playerSlider.setSnapToTicks(true);
        playerSlider.setMajorTickUnit(10);
        playerSlider.setMinorTickCount(5);
        playerSlider.valueProperty().addListener((obs, oldVal, newVal) ->
                playerLabel.setText("Max Players: " + newVal.intValue()));

        Button btnLaunch = createMenuButton("LAUNCH SERVER");
        btnLaunch.setOnAction(e -> {
            if(serverThreadcheck) {
                showErrorDialog("Server Running", "A server is already running!");
                return;
            }
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
                        serverThreadcheck = false;
                    }
                });
                serverThread.setDaemon(true);
                serverThread.start();
                Thread.sleep(100);
                joinLocalServer();

            } catch (InterruptedException ex) {
                serverThreadcheck = false;
                Thread.currentThread().interrupt();
                Logger.get().error("Server launch interrupted");
            } catch (Exception ex) {
                serverThreadcheck = false;
                Logger.get().error("Failed to launch server: " + ex.getMessage());
                showErrorDialog("Server Error", "Failed to launch server: " + ex.getMessage());
            }
        });

        Button btnBack = createMenuButton("BACK");
        btnBack.setOnAction(e -> {
            if(server != null)
            {
                serverThreadcheck = false;
                server.closeServer();
            }
            showStartOptions();
        });

        menuBox.getChildren().addAll(title, playerLabel, playerSlider, btnLaunch, btnBack);

    }


    private void joinLocalServer(){
        try{
            client = new Client();
            client.connect("127.0.0.1", GAME_PORT, playerName);
            client.setMenu(this);
            Logger.get().info("SERVER USER SELF JOIN");
        } catch (Exception e) {
            Logger.get().warn("FAILED TO CONNECT THE SERVER AND HOST TOGETHER");
            showErrorDialog("Connection Failed", "Could not connect to local server");
        }
    }

    public void updateLobbyUI(LobbyPacket lp) {

        if (client == null || client.getId() == null) {
            return;
        }


        if(!drawLobby)
        {
            menuBox.getChildren().clear();

            Label browserTitle = new Label("LOBBY: Waiting for players...");
            browserTitle.setFont(Font.font("Verdana", FontWeight.BOLD, 30));
            browserTitle.setTextFill(Color.WHITE);

            lobbyList = new ListView<>();
            lobbyList.setMaxWidth(300);
            lobbyList.setMaxHeight(200);

            btnStart = createMenuButton("START");
            Button btnBack = createMenuButton("BACK");

            if(host == null)
            {
                this.host = lp.host;
            }

            boolean isHost = client != null && client.getId() != null && client.getId().equals(lp.host);
            btnStart.setVisible(isHost);
            btnStart.setOnAction(e -> {

            });
            btnBack.setOnAction(e -> {
                drawLobby = false;
                if(server != null)
                {
                    serverThreadcheck = false;
                    server.closeServer();
                }
                if(client != null)
                {
                    client.stop();
                }
                showStartOptions();
            });

            menuBox.getChildren().addAll(browserTitle, lobbyList, btnStart, btnBack);
            drawLobby = true;
        }

        ServerState state = lp.state;
        if(state == ServerState.LOBBY || state == ServerState.READY)
        {
            javafx.application.Platform.runLater(() -> {
                lobbyList.getItems().setAll(lp.getPlayerNames());
                if (btnStart != null && server != null) {
                    btnStart.setDisable(lp.getPlayerNames().size() < lp.minRequired);
                }
            });
        }

    }

    public void onDisconnect(){
        drawLobby = false;
        if(!client.getId().equals(host)) showErrorDialog("Match Closed", "Host left the match.");
        resetToMainMenu();
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
        dialog.setHeaderText(null);
        dialog.setContentText("Enter Name: ");

        // Add custom styling
        dialog.getDialogPane().setStyle("-fx-font-family: 'Verdana';");

        Optional<String> result = dialog.showAndWait();

        result.ifPresent(name -> {
            if(!name.trim().isEmpty())
            {
                this.playerName = name;
                this.nameDisplay.setText("ID: " + playerName + "  [EDIT]");
            }
        });

    }

    private void showErrorDialog(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
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
        btn.setOnMouseEntered(e -> btn.setStyle(
                "-fx-background-color: #2ecc71; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-size: 16px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-background-radius: 5; " +
                        "-fx-cursor: hand;"
        ));
        btn.setOnMouseExited(e -> btn.setStyle(
                "-fx-background-color: #34495e; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-size: 16px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-background-radius: 5; " +
                        "-fx-cursor: hand;"
        ));

        return btn;
    }
}