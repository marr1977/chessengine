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
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import martin.chess.engine.Board;

public class ChessUI extends Application {
    
    private Scene playingScene;
    private Scene introScene;
	private ComboBox<PlayerType> whitePlayer;
	private ComboBox<PlayerType> blackPlayer;
	private Stage primaryStage;
	private Board board;
	private Canvas boardCanvas;
	private BoardDrawer boardDrawer;

	@Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        
        createPlayingScene();
        
    	board = new Board();
    	boardDrawer = new BoardDrawer(board, boardCanvas, true);
    	
        primaryStage.setTitle("Martin's Crappy Chess");
        primaryStage.setScene(playingScene);
        primaryStage.show();
    }

	private enum PlayerType {
		Human("Human"),
		RandomRobby("Random Robby");
		
		private String name;

		private PlayerType(String name) {
			this.name = name;
		}
		@Override
		public String toString() {
			return name;
		}
	}
	
    private void createPlayingScene() {
		boardCanvas = new Canvas();
		boardCanvas.setWidth(720);
		boardCanvas.setHeight(720);
		
		GridPane grid = new GridPane();
		grid.setAlignment(Pos.CENTER);
		grid.setHgap(10);
		grid.setVgap(10);
		grid.setPadding(new Insets(25, 25, 25, 25));
		
		grid.add(new Label("White: "), 0, 0);
		
		whitePlayer = new ComboBox<PlayerType>();
		whitePlayer.getItems().addAll(PlayerType.values());
		grid.add(whitePlayer, 1, 0);
		
		grid.add(new Label("Black: "), 0, 1);
		
		blackPlayer = new ComboBox<PlayerType>();
		blackPlayer.getItems().addAll(PlayerType.values());
		grid.add(blackPlayer, 1, 1);

		Button btn = new Button("Start game");
		btn.setOnAction(new EventHandler<ActionEvent>() {
			 
		    @Override
		    public void handle(ActionEvent e) {
		    	startGame();
		    }
		});
		
		HBox hbBtn = new HBox(10);
		hbBtn.setAlignment(Pos.BOTTOM_RIGHT);
		hbBtn.getChildren().add(btn);
		grid.add(hbBtn, 1, 2);
		
		VBox vbox = new VBox();
		
		vbox.getChildren().add(grid);
		
		BorderPane border = new BorderPane();
		 
		border.setLeft(vbox);
		border.setCenter(boardCanvas);
		
		playingScene = new Scene(border);  

	}

    private void startGame() {

	}

	public static void main(String[] args) {
        launch(args);
    }
}
