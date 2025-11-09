module marketflow {
	requires transitive javafx.controls;
	requires javafx.fxml;
	requires javafx.graphics;
	requires javafx.media;

    opens dev.luggers to javafx.controls, javafx.fxml, javafx.graphics, javafx.media;
}