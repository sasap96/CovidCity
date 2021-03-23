package net.etfbl.application;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class JavaCov20 extends Application {

	Scene scene;
	static Stage stage;

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		
		MyLogger.setup();

		try {
			FXMLLoader fxmlLoader = new FXMLLoader();
			BorderPane pane = fxmlLoader.load(getClass().getResource("input.fxml").openStream());
			InputController c = fxmlLoader.getController();
			c.setDimenzija();
			scene = new Scene(pane);
			stage = primaryStage;
			stage.setScene(scene);
			stage.setTitle("Unos Podataka");
			stage.initStyle(StageStyle.UTILITY);
			stage.setResizable(false);
			stage.show();
			stage.setOnCloseRequest(e -> c.quit());

		} catch (IOException e) {
			Logger.getLogger(Logger.GLOBAL_LOGGER_NAME).log(Level.WARNING, e.fillInStackTrace().toString());
		}

	}
}
