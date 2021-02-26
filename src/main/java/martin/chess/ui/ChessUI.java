package martin.chess.ui;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import martin.chess.engine.Color;
import martin.chess.engine.GameResultData;
import martin.chess.ui.BoardDrawer.GameListener;

public class ChessUI extends Application implements GameListener {
    
    private static final String START_GAME = "Start game";
	private static final String RESET_GAME = "Reset game";
	private Scene playingScene;
	private ComboBox<PlayerType> whitePlayer;
	private ComboBox<PlayerType> blackPlayer;
	private Canvas boardCanvas;
	private BoardDrawer boardDrawer;
	private Button startGameButton;
	private Label resultLabel;
	private Label resultValueLabel;
	
	public static void main(String[] args) {
        launch(args);
    }

	@Override
    public void start(Stage primaryStage) {
        
        createPlayingScene();
        
    	boardDrawer = new BoardDrawer(boardCanvas, this);
    	
        primaryStage.setTitle("Martin's Crappy Chess");
        primaryStage.setScene(playingScene);
        primaryStage.show();
    }
	
    private void createPlayingScene() {
		boardCanvas = new Canvas();
		boardCanvas.setWidth(720);
		boardCanvas.setHeight(720);
		
		GridPane grid = new GridPane();
		grid.setAlignment(Pos.TOP_LEFT);
		grid.setHgap(10);
		grid.setVgap(10);
		grid.setPadding(new Insets(25, 25, 25, 25));
		
		grid.add(new Label("White: "), 0, 0);
		
		whitePlayer = new ComboBox<PlayerType>();
		whitePlayer.getItems().addAll(PlayerType.values());
		whitePlayer.getSelectionModel().select(PlayerType.Human);
		grid.add(whitePlayer, 1, 0);
		
		grid.add(new Label("Black: "), 0, 1);
		
		blackPlayer = new ComboBox<PlayerType>();
		blackPlayer.getItems().addAll(PlayerType.values());
		blackPlayer.getSelectionModel().select(PlayerType.Trait1);
		grid.add(blackPlayer, 1, 1);
		
		startGameButton = new Button(START_GAME);
		startGameButton.setOnAction(new EventHandler<ActionEvent>() {
			 
		    @Override
		    public void handle(ActionEvent e) {
		    	if (startGameButton.getText().equals(RESET_GAME)) {
		    		onGameStopped();
		    		boardDrawer.resetGame();
		    	} else {
		    		startGame();
		    	}
		    }
		});
		
		grid.add(startGameButton, 1, 2);

		resultLabel = new Label("Result");
		resultValueLabel = new Label("");

		grid.add(resultLabel, 0, 3);
		grid.add(resultValueLabel, 1, 3);
		
		VBox vbox = new VBox();
		
		vbox.getChildren().add(grid);
		
		BorderPane border = new BorderPane();
		 
		border.setLeft(vbox);
		border.setCenter(boardCanvas);
		
		playingScene = new Scene(border);  

	}

    private void startGame() {
    	boardDrawer.startGame(whitePlayer.getValue(), blackPlayer.getValue());

    	whitePlayer.setDisable(true);
		blackPlayer.setDisable(true);
		startGameButton.setText(RESET_GAME);
		
	}

	private void onGameStopped() {
		whitePlayer.setDisable(false);
		blackPlayer.setDisable(false);
		startGameButton.setText(START_GAME);
	}

	@Override
	public void onGameEnded(GameResultData result) {
		resultValueLabel.setText(getResultText(result));
		onGameStopped();
	}

	private String getResultText(GameResultData result) {
		switch (result.getOutcome()) {
		case CHECKMATE: 					return (result.getWinner() == Color.BLACK ? "Black" : "White") + " wins"; 
		case DRAW_FIFTY_MOVE_RULE:			return "Draw - 50-move rule";
		case DRAW_INSUFFICIENT_MATERIAL: 	return "Draw - Insufficient material";
		case DRAW_THREEFOLD_REPETITION:		return "Draw - Threefold repetition";
		case STALEMATE:						return "Stalemate";
		default:
			return "?";
		}
	}
}
