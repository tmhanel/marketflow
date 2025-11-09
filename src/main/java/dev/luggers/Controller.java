package dev.luggers;

import java.util.ArrayList;
import java.util.List;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.chart.LineChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.util.Duration;

public class Controller {
	int currentTab = 0;
	int prevTimeMult = 0;

	private Simulation simulation;
	private UIHelper uiHelper;
	private List<TabPane> paneList;

	@FXML
	private AnchorPane buttonPane;
	@FXML
	private StackPane stackPane;
	@FXML
	private ImageView background;
	@FXML
	private AnchorPane mainPane;
	@FXML
	private StackPane bottomPane;
	@FXML
	private VBox elementField;
	@FXML
	private HBox chartBox;
	@FXML
	private TextField moneyField;
	@FXML
	private TextField powerField;
	@FXML
	private TextField inflowField;
	@FXML
	private TextField timeLabel;
	@FXML
	private HBox timeMultBox;
	@FXML
	private LineChart<Number, Number> inflowChart;
	@FXML
	private LineChart<Number, Number> priceChart;
	@FXML
	private Spinner<Integer> timeMultSpinner;
	@FXML
	private Button leftButton;
	@FXML
	private Button rightButton;
	@FXML
	private Button pauseButton;
	@FXML
	private Button startButton;

	@FXML
	private void onLeftClicked() {
		tabLeft();
	}

	@FXML
	private void onPauseClicked() {
		prevTimeMult = timeMultSpinner.getValue();
		timeMultSpinner.getValueFactory().setValue(0);
		startButton.setVisible(true);
		pauseButton.setVisible(false);
	}

	@FXML
	private void onStartClicked() {
		timeMultSpinner.getValueFactory().setValue(prevTimeMult);
		startButton.setVisible(false);
		pauseButton.setVisible(true);
	}

	@FXML
	private void onRightClicked() {
		tabRight();
	}

	@FXML
	public void initialize() {

		simulation = new Simulation();
		uiHelper = new UIHelper(simulation, this);

		paneFormatting();
	}

	public void tabLeft() {
		paneList.get(currentTab).setVisible(false);
		paneList.get(loopTabs(-1)).setVisible(true);
	}

	public void tabRight() {
		paneList.get(currentTab).setVisible(false);
		paneList.get(loopTabs(1)).setVisible(true);
	}

	public int loopTabs(int change) {
		if (currentTab + change >= 0) {
			currentTab += change;
		} else {
			currentTab = paneList.size() - 1;
		}
		return currentTab %= paneList.size();
	}

	public void startUp() {
		Powerplant startPlant = PowerplantFactory.getInstance().getFirstPowerplant();
		simulation.setStart(startPlant);

		simulation.startUp();

		powerPlantTabPaneSetup();

		timeMultBox.toFront();
		elementFieldConfig(simulation);

		spinnerConfig();
		paneList.get(currentTab).setVisible(true);
	}

	private void powerPlantTabPaneSetup() {
		final int length = simulation.getStart().getLength();
		paneList = new ArrayList<>();
		for (int i = 0; i < length; i++) {
			Powerplant plant = simulation.getPowerplant(i);

			Button button = buttonConfig(plant.getRelXPos(), plant.getRelYPos(), i);
			warnIconConfig(button, plant);

			TabPane tabPane = new TabPane();
			tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

			Tab controlTab = new Tab("Controls");
			Tab infoTab = new Tab("Information");

			Slider slider = new Slider(plant.getMinWaterflow(), plant.getMaxWaterflow(), 0);
			sliderConfig(slider);
			slider.valueProperty().bindBidirectional(plant.turbineFlow);
			slider.setValue(simulation.getInflow());

			Label throughFlow = new Label();
			Label inFlow = new Label();
			Label height = new Label();
			Label power = new Label();
			Label nameLabel = new Label(String.format("Kraftwerk: %s    ", plant.getName()));

			TextFlow textFlow = new TextFlow();
			Text deltaInFlow = new Text();
			Text deltaHeight = new Text();

			throughFlow.textProperty().bind(slider.valueProperty().asString("Durchfluss: %.02f m³/s "));

			inFlow.textProperty().bind(plant.specificInflow.asString("Zufluss: %.2f m³/s "));
			deltaInFlow.textProperty()
					.bind(Bindings.subtract(plant.specificInflow, plant.turbineFlow).asString("[%+.2f] "));
			plant.turbineFlow.addListener((_, _, newValue) -> {
				double diff = plant.specificInflow.doubleValue() - newValue.doubleValue();
				deltaInFlow.setFill(diff >= 0 ? Color.GREEN : Color.RED);
			});
			plant.specificInflow.addListener((_, _, newValue) -> {
				double diff = newValue.doubleValue() - plant.turbineFlow.doubleValue();
				deltaInFlow.setFill(diff >= 0 ? Color.GREEN : Color.RED);
			});

			final double normalHeight = plant.getPool().getNormalHeight();
			
			height.textProperty().bind((plant.getPool().height.asString("Stauhöhe: %.2f m ")));
			deltaHeight.textProperty()
					.bind(Bindings.subtract(plant.getPool().height, normalHeight).asString("[%+.2f]  "));
			plant.getPool().height.addListener((_, _, newValue) -> {
				double diff = newValue.doubleValue() - normalHeight;
				deltaHeight.setFill(diff >= 0 ? Color.GREEN : Color.RED);
			});
			textFlow.getChildren().addAll(height, deltaHeight, inFlow, deltaInFlow);

			power.textProperty().bind(plant.powerMW.asString("Leistung: %.2f Megawatt "));

			HBox controlBox = new HBox();
			HBox variableBox = new HBox();
			HBox informationBox = new HBox();
			VBox boxCointainer = new VBox();

			controlBox.setAlignment(Pos.CENTER_LEFT);
			controlBox.getChildren().addAll(slider, throughFlow);

			variableBox.setAlignment(Pos.CENTER_LEFT);
			variableBox.getChildren().addAll(textFlow);

			informationBox.getChildren().addAll(nameLabel, power);
			boxCointainer.setStyle("-fx-background-color: white;");
			boxCointainer.getChildren().addAll(informationBox, controlBox, variableBox);

			AnchorPane anchorPane = new AnchorPane();
			anchorPane.getChildren().add(boxCointainer);
			AnchorPane.setTopAnchor(boxCointainer, 10.0);
			AnchorPane.setLeftAnchor(boxCointainer, 10.0);

			controlTab.setContent(anchorPane);
			tabPane.getTabs().add(controlTab);
			tabPane.getTabs().add(infoTab);

			tabPane.setStyle("-fx-background-color: white;");

			paneList.add(tabPane);
			bottomPane.getChildren().add(tabPane);
			tabPane.setVisible(false);
		}
	}

	private void spinnerConfig() {
		timeMultSpinner.setEditable(true);
		timeMultSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 10000, 20, 2));

		simulation.timeMult.bind(timeMultSpinner.getValueFactory().valueProperty());
		timeMultSpinner.getEditor().setPrefColumnCount(5);
	}

	private void sliderConfig(Slider slider) {
		slider.setMajorTickUnit(50);
		slider.setMinorTickCount(24);
		slider.setSnapToTicks(true);
		slider.setStyle("-fx-background-color: white;");
		enableSliderColor(slider);
	}

	private void elementFieldConfig(Simulation sim) {
		elementField.getStyleClass().add("elementField");
		moneyField.setEditable(false);
		powerField.setEditable(false);
		inflowField.setEditable(false);
		timeLabel.setEditable(false);
		timeLabel.setPrefColumnCount(17);
		moneyField.textProperty().bind(simulation.money.asString("Kontostand: %,.0f €"));
		powerField.textProperty().bind(simulation.totalPowerMW.asString("Totaleistung: %.2f MW"));
		inflowField.textProperty().bind(simulation.getInflowRepository().inflow.asString("Zufluss: %.0f m³/s"));
	}

	private Button buttonConfig(double relativeX, double relativeY, int i) {
		Button button = new Button();
		button.translateXProperty().bind(Bindings.createDoubleBinding(() -> getXCoordinate(relativeX, button),
				background.boundsInParentProperty(), background.imageProperty(), button.widthProperty()));
		button.translateYProperty().bind(Bindings.createDoubleBinding(() -> getYCoordinate(relativeY, button),
				background.boundsInParentProperty(), background.imageProperty(), button.heightProperty()));

		button.getStyleClass().add("circle-button");
		mainPane.getChildren().add(button);
		button.setOnAction(event -> {
			paneList.get(currentTab).setVisible(false);
			currentTab = i;
			paneList.get(currentTab).setVisible(true);
		});
		return button;
	}

	private Double getYCoordinate(double relativeY, Button button) {
		double viewWidth = background.getBoundsInParent().getWidth();
		double viewHeight = background.getBoundsInParent().getHeight();
		double imageRatio = background.getImage().getWidth() / background.getImage().getHeight();
		double viewRatio = viewWidth / viewHeight;

		double imageHeight, offsetY;
		if (viewRatio > imageRatio) {
			// Bild ist höhenbeschränkt
			imageHeight = viewHeight;
			offsetY = 0;
		} else {
			// Bild ist breitenbeschränkt
			imageHeight = viewWidth / imageRatio;
			offsetY = (viewHeight - imageHeight) / 2;
		}

		return background.getBoundsInParent().getMinY() + offsetY + imageHeight * relativeY - button.getHeight() / 2;
	}

	private Double getXCoordinate(double relativeX, Button button) {
		double viewWidth = background.getBoundsInParent().getWidth();
		double viewHeight = background.getBoundsInParent().getHeight();
		double imageRatio = background.getImage().getWidth() / background.getImage().getHeight();
		double viewRatio = viewWidth / viewHeight;

		double imageWidth, offsetX;
		if (viewRatio > imageRatio) {
			imageWidth = viewHeight * imageRatio;
			offsetX = (viewWidth - imageWidth) / 2;
		} else {
			imageWidth = viewWidth;
			offsetX = 0;
		}

		return background.getBoundsInParent().getMinX() + offsetX + imageWidth * relativeX - button.getWidth() / 2;
	}

	public void warnIconConfig(Button button, Powerplant powerplant) {
	    /*
		FontAwesomeIconView warnIcon = new FontAwesomeIconView(FontAwesomeIcon.EXCLAMATION_TRIANGLE);
		warnIcon.setVisible(false);
		warnIcon.translateXProperty().bind(button.translateXProperty().multiply(0.99));
		warnIcon.translateYProperty().bind(button.translateYProperty().multiply(0.98));
		Timeline pulse = iconPulseConfig(warnIcon);
		warnIcon.setGlyphSize(38);
		powerplant.getPool().height.addListener((_, _, newVal) -> {
			double minVolume = powerplant.getPool().getMinVolume();
			double maxVolume = powerplant.getPool().getMaxVolume();
			double volume = newVal.doubleValue() * powerplant.width() * powerplant.length();
			if (volume >= maxVolume || volume <= minVolume) {
				if (pulse.getStatus() != Animation.Status.RUNNING) {
					pulse.play();
				}
				warnIcon.setVisible(true);
			} else {
				warnIcon.setVisible(false);
			}
		});
		mainPane.getChildren().add(warnIcon);
		*/
	}
/*
	private Timeline iconPulseConfig(FontAwesomeIconView icon) {
		icon.setFill(Color.ORANGERED);
		icon.setOpacity(0.0);
		icon.setScaleX(1.0);
		icon.setScaleY(1.0);

		final var pulse = new Timeline(
				new KeyFrame(Duration.ZERO, new KeyValue(icon.opacityProperty(), 0.5),
						new KeyValue(icon.scaleXProperty(), 1.0), new KeyValue(icon.scaleYProperty(), 1.0),
						new KeyValue(icon.fillProperty(), Color.ORANGERED)),
				new KeyFrame(Duration.seconds(0.9), new KeyValue(icon.opacityProperty(), 1.0),
						new KeyValue(icon.scaleXProperty(), 1.2), new KeyValue(icon.scaleYProperty(), 1.2),
						new KeyValue(icon.fillProperty(), Color.RED)),
				new KeyFrame(Duration.seconds(1.8), new KeyValue(icon.opacityProperty(), 0.5),
						new KeyValue(icon.scaleXProperty(), 1.0), new KeyValue(icon.scaleYProperty(), 1.0),
						new KeyValue(icon.fillProperty(), Color.ORANGERED)));
		pulse.setCycleCount(Animation.INDEFINITE);
		return pulse;
	}
	*/

	private void enableSliderColor(Slider slider) {
		slider.skinProperty().addListener((_, _, newSkin) -> {
			if (newSkin == null)
				return;

			Platform.runLater(() -> {
				StackPane track = (StackPane) slider.lookup(".track");
				if (track == null)
					return;

				track.setStyle("-fx-background-color: linear-gradient(to right, #005b96 0%, #b3cde0 0%);");

				slider.valueProperty().addListener((_, _, newVal) -> {
					double percent = (newVal.doubleValue() - slider.getMin()) / (slider.getMax() - slider.getMin())
							* 100.0;
					String style = String.format(java.util.Locale.US,
							"-fx-background-color: linear-gradient(to right, #005b96 %.2f%%, #b3cde0 %.2f%%);", percent,
							percent);
					track.setStyle(style);
				});
			});
		});
	}

	private void paneFormatting() {
		background.fitWidthProperty().bind(Bindings.createDoubleBinding(() -> stackPane.getWidth() * 1.01, // 1%
																											// overscale
				stackPane.widthProperty()));
		background.fitHeightProperty().bind(stackPane.heightProperty());
		background.setPreserveRatio(true);
		background.setSmooth(true);
		buttonPane.maxWidthProperty().bind(background.fitWidthProperty());
		buttonPane.maxHeightProperty().bind(background.fitHeightProperty());
		buttonPane.prefWidthProperty().bind(background.fitWidthProperty());
		buttonPane.prefHeightProperty().bind(background.fitHeightProperty());
		allBind(mainPane, stackPane, 1, 1);
		allBind(bottomPane, stackPane, 1, 0.14);

		chartBox.prefHeightProperty().bind(mainPane.heightProperty().multiply(0.2));
		priceChart.prefWidthProperty().bind(chartBox.heightProperty());
		priceChart.minWidthProperty().bind(priceChart.prefWidthProperty());
		priceChart.maxWidthProperty().bind(priceChart.prefWidthProperty());
		inflowChart.prefWidthProperty().bind(chartBox.heightProperty());
		inflowChart.minWidthProperty().bind(inflowChart.prefWidthProperty());
		inflowChart.maxWidthProperty().bind(inflowChart.prefWidthProperty());
	}

	@FXML
	public void heightBind(Pane paneRoot, Pane paneParent, double multiplier) {
		paneRoot.minHeightProperty().bind(paneParent.heightProperty().multiply(multiplier));
		paneRoot.maxHeightProperty().bind(paneParent.heightProperty().multiply(multiplier));
	}

	@FXML
	public void widthBind(Pane paneRoot, Pane paneParent, double multiplier) {
		paneRoot.minWidthProperty().bind(paneParent.widthProperty().multiply(multiplier));
		paneRoot.maxWidthProperty().bind(paneParent.widthProperty().multiply(multiplier));
	}

	@FXML
	public void allBind(Pane paneRoot, Pane paneParent, double multiplierWidth, double multiplierHeight) {
		widthBind(paneRoot, paneParent, multiplierWidth);
		heightBind(paneRoot, paneParent, multiplierHeight);
	}

	public LineChart getInflowChart() {
		return inflowChart;
	}

	public LineChart getPriceChart() {
		return priceChart;
	}

	public TextField getTimeLabel() {
		return timeLabel;
	}

	public void nextTick(double delta) {
		simulation.nextTick(delta);
	}

	public ImageView getBackground() {
		return background;
	}

}