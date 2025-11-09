package dev.luggers;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class Marketflow extends Application {

	private long lastUpdate = 0;
	private Scene gameScene;
	private Scene tutorialScene;
	FXMLLoader gameLoader;
	FXMLLoader tutorialLoader;

	@Override
	public void start(Stage primaryStage) throws Exception {
		gameLoader = new FXMLLoader(getClass().getResource("firstDraft.fxml"));
		gameScene = new Scene(gameLoader.load());
		tutorialLoader = new FXMLLoader(getClass().getResource("tutorial.fxml"));
		tutorialScene = new Scene(tutorialLoader.load());
		Button button = new Button("Simulation Starten");
		button.getStyleClass().add("start-button");
		button.setOnAction(event -> {
			startGame();
			primaryStage.setScene(gameScene);
		});
		Parent root = tutorialLoader.getRoot();
		ImageView backgroundImage = (ImageView) root.lookup("#tutorialImage");
		backgroundImage.fitWidthProperty().bind(((Pane) root).widthProperty());
		backgroundImage.fitHeightProperty().bind(((Pane) root).heightProperty());
		((Pane) root).getChildren().add(button);
		primaryStage.setTitle("MarketFlow");
		primaryStage.setScene(tutorialScene);
		primaryStage.show();
	}

	private void startGame() {
		final Controller controller = gameLoader.getController();
		controller.startUp();
		gameScene.setOnKeyPressed(event ->
		{
			if (event.getCode() == KeyCode.LEFT) {
				controller.tabLeft();
			}
			if (event.getCode() == KeyCode.RIGHT) {
				controller.tabRight();
			}
		});

		final AnimationTimer timer = new AnimationTimer() {
			@Override
			public void handle(long now) {
				if (lastUpdate > 0) {
					double delta = (now - lastUpdate) / 1000000000.0;
					if (delta < 0.25) {
						return;
					}
					if (delta >= 0.25) {
						delta = 0.25;
					}
					controller.nextTick(delta);
				}
				lastUpdate = now;
			}
		};
		timer.start();
	}

}