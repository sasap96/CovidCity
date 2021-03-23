package net.etfbl.application;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;

import javafx.geometry.Insets;
import javafx.scene.Scene;

import javafx.scene.control.Button;
import javafx.scene.control.Label;

import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;

import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;

import javafx.scene.layout.BorderPane;

import javafx.scene.layout.CornerRadii;

import javafx.scene.paint.Color;

import javafx.stage.Stage;

public class InputController {

	Controller controller;

	@FXML
	Button start;
	@FXML
	TextField odrasli;
	@FXML
	TextField stari;
	@FXML
	TextField djeca;
	@FXML
	TextField kuce;
	@FXML
	TextField punktovi;
	@FXML
	TextField vozila;
	@FXML
	Label upozorenje;
	@FXML
	Button nastavi;
	@FXML
	Label dim;
	
	BorderPane root;
	@FXML
	public void nastaviSimulaciju(){

		try {
			controller.nastaviSimulaciju();
			Scene secondScene = new Scene(root);
			Stage newWindow = new Stage();
			newWindow.setScene(secondScene);
			newWindow.setTitle("JavaKov20");
			newWindow.show();
			newWindow.setOnCloseRequest(e -> controller.zavrsiSimulaciju());
			JavaCov20.stage.close();
		} catch (Exception e) {
			Logger.getLogger(Logger.GLOBAL_LOGGER_NAME).log(Level.INFO, e.fillInStackTrace().toString());
			nastavi.setTooltip(new Tooltip("Nije moguce nastaviti simulaciju"));
			
		}
	
		
	}
	
	public void prikaziMapu()  {
		
		controller.inicijalizacija();
		Scene secondScene = new Scene(root);
		Stage newWindow = new Stage();
		newWindow.setScene(secondScene);
		newWindow.setTitle("JavaKov20");
		newWindow.show();
		newWindow.setOnCloseRequest(e -> controller.zavrsiSimulaciju());
		JavaCov20.stage.close();

	}

	public void quit() {
		if (controller != null)
			controller.zavrsiSimulaciju();
	}

	@FXML
	public void unos() {
		Integer brojKuca, brojPunktova, brojOdraslih, brojStarih, brojDjece, brojVozila;
		brojKuca = brojPunktova = brojOdraslih = brojStarih = brojDjece = brojVozila = 0;

		brojOdraslih = getUnos(odrasli);
		brojStarih = getUnos(stari);
		brojDjece = getUnos(djeca);
		brojKuca = getUnos(kuce);
		brojPunktova = getUnos(punktovi);
		brojVozila = getUnos(vozila);

		if (brojKuca == null || brojPunktova == null || brojOdraslih == null || brojStarih == null || brojDjece == null
				|| brojVozila == null)
			return;
		if (brojKuca > brojStarih + brojOdraslih) {
			upozorenje.setText(
					"Zbir odraslih i starijih osoba mora biti veci od broja kuca \n (zbog praznih kuca i djece same u kuci)");
			return;
		}

		prikaziMapu();
		controller.setMap(brojPunktova, brojKuca, brojOdraslih, brojStarih, brojDjece, brojVozila);
	}
	

	private Integer getUnos(TextField text) {
		try {

			Integer num = Integer.parseInt(text.getText());
			text.setBackground(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));
			return num;
		} catch (Exception e) {

			start.setText("Nedozvoljen unos, pokusajte ponovo");
			start.setTooltip(new Tooltip("Nedozvoljen unos, pokusajte ponovo"));
			text.setBackground(new Background(new BackgroundFill(Color.RED, CornerRadii.EMPTY, Insets.EMPTY)));
			Logger.getLogger(Logger.GLOBAL_LOGGER_NAME).log(Level.INFO, e.fillInStackTrace().toString());
			return null;
		}
	}
	
	public void setDimenzija() throws IOException {
		FXMLLoader fxmlLoader = new FXMLLoader();
	    root = fxmlLoader.load(getClass().getResource("map.fxml").openStream());
		controller = fxmlLoader.getController();
		dim.setText("Dimenzija: "+controller.getDimenzija());
		
	}

}
