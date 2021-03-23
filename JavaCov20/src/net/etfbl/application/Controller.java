package net.etfbl.application;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;
import java.util.Date;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Shape;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import net.etfbl.mapa.Grad;
import net.etfbl.mapa.Pozicija;
import net.etfbl.stanovnici.Kuca;
import net.etfbl.stanovnici.Osoba;

public class Controller implements  Listener,BrojacZarazenih {
	
	public static final int FIELDSIZE=50;
	public static Listener app;
	public static List<Thread> threadList;
	int year = Calendar.getInstance().get(Calendar.YEAR);
	int dim;
	Grad map;
	FileWatcher fileWatcher;
	HashMap<Integer, Shape> shapes;
	
	@FXML
	private Button omoguciKretanje;
	@FXML
	private Button zaustavi;
	@FXML
	private Button pokreni;
	@FXML
	private Button zavrsi;
	@FXML
	private Button stanjeAmbulanti;
	@FXML
	private Button statistika;
	@FXML
	private Button posaljiVozilo;
	@FXML
	private BorderPane root;
	@FXML
	private ScrollPane tabela;
	@FXML
	private Label brojZarazenih;
	@FXML
	private Label brojOporavljenih;
	@FXML
	private VBox stanovnici;
	@FXML
	private VBox odabrani;
	@FXML
	private VBox zarazeni;
	private VBox ambulante;
	HashMap<Integer, Label> pozicije;
	HashMap<Integer, Label> imena;
	Stack<Label> alarmi = new Stack<Label>();
	Polje[] polja;
	GridPane panes;
	private long start;
	private long finish;

	@Override
	public void promjeniBrojaZarazenih(String zarazeni, String oporavljeni) {
		Platform.runLater(new Runnable() {

			@Override
			public void run() {

				brojZarazenih.setText(zarazeni);
				brojOporavljenih.setText(oporavljeni);

			}
		});
	}

	@Override
	public void azurirajPoziciju(Osoba osoba, Pozicija staraPozicija, Pozicija novaPozicija, int smijer) {

		Platform.runLater(new Runnable() {

			@Override
			public void run() {
				String c = "-";
				switch (smijer) {
				case 0:
					c = "N";
					break;
				case 1:
					c = "S";
					break;
				case 2:
					c = "W";
					break;
				case 3:
					c = "E";
					break;
				case 4:
					c = "NE";
					break;
				case 5:
					c = "NW";
					break;
				case 6:
					c = "SE";
					break;
				case 7:
					c = "SW";
					break;
				}

				polja[staraPozicija.getX() * dim + staraPozicija.getY()].removePosition(osoba.getIDBroj());
				polja[novaPozicija.getX() * dim + novaPozicija.getY()].setPosition(osoba.getIDBroj());

				Label label = pozicije.get(osoba.getIDBroj());
				label.setText(" (" + novaPozicija.getX() + "," + novaPozicija.getY() + ")" + " " + c);

				tabela.snapshot(new SnapshotParameters(), new WritableImage(1, 1));
			}
		});

	}

	@Override
	public void dodajKucu(Pozicija pozicija, Color color) {
		polja[pozicija.getX() * dim + pozicija.getY()].setBorder(
				new Border(new BorderStroke(color, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(3))));
	}

	@Override
	public void dodajPunkt(Pozicija pozicija) {
		polja[pozicija.getX() * dim + pozicija.getY()].setBorder(new Border(
				new BorderStroke(Color.RED, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(3))));
		polja[pozicija.getX() * dim + pozicija.getY()].setStyle("-fx-background-color:Transparent");
	}

	@Override
	public void dodajBolnicu(Pozicija pozicija) {
		polja[pozicija.getX() * dim + pozicija.getY()].setBorder(new Border(
				new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(3))));
		polja[pozicija.getX() * dim + pozicija.getY()].setStyle("-fx-background-color:Transparent");
	}

	public void upozori(Osoba osoba, Pozicija novaPozicija) {

		Platform.runLater(new Runnable() {

			@Override
			public void run() {
				Label label = new Label();
				label.setText(osoba.getKucniID() + " " + novaPozicija + " " + osoba);
				label.setTextFill(osoba.getBojaKuce());
				zarazeni.getChildren().add(label);
				alarmi.push(label);
				tabela.snapshot(new SnapshotParameters(), new WritableImage(1, 1));
			}
		});

	}

	public void slobodnoVozilo(boolean flag) {
		posaljiVozilo.setDisable(flag);
	}

	@FXML
	public void omoguciKretanje() {
		try {
			BufferedWriter writer = new BufferedWriter(
					new FileWriter("./Zarazeni" + File.separator + FileWatcher.file));
			writer.write("0");
			writer.newLine();
			writer.write("0");
			writer.newLine();
			writer.write("0");
			statistika.setDisable(false);
			writer.close();
		} catch (IOException e) {
			Logger.getLogger(Logger.GLOBAL_LOGGER_NAME).log(Level.WARNING, e.fillInStackTrace().toString());
		}

		fileWatcher = new FileWatcher(this);
		fileWatcher.start();
		start=System.nanoTime();
		map.start();
		
		omoguciKretanje.setDisable(true);

	}
	
	private void deserijalizuj() throws Exception {
		
		FileInputStream fis = new FileInputStream("files" + File.separator + "serialization");
		ObjectInputStream in = new ObjectInputStream(fis);

		map = (Grad) in.readObject();
		in.close();
		fis.close();
		threadList = new ArrayList<Thread>(map.getDimenzija());
		
		pokreni.setDisable(true);
		zaustavi.setDisable(false);	
}

	@FXML
	public void pokreniSimulaciju() {
		try {
			deserijalizuj();
			map.pokreni();
		} catch (Exception e) {
			Logger.getLogger(Logger.GLOBAL_LOGGER_NAME).log(Level.WARNING, e.fillInStackTrace().toString());
		}
		

	}

	@FXML
	void zaustaviSimulaciju() {
		try {

			FileOutputStream fos = new FileOutputStream("files" + File.separator + "serialization");
			ObjectOutputStream out = new ObjectOutputStream(fos);
			
			map.zaustavi();
			zaustavi.setDisable(true);
			pokreni.setDisable(false);
			
			out.writeObject(map);
			
			out.close();
			fos.close();
			

		} catch (Exception e) {
			Logger.getLogger(Logger.GLOBAL_LOGGER_NAME).log(Level.WARNING, e.fillInStackTrace().toString());
		}

	}

	@FXML
	public void zavrsiSimulaciju() {
		if(map!=null) {
		this.zaustaviSimulaciju();
		finish=System.nanoTime();
		SimpleDateFormat formatter = new SimpleDateFormat("dd_MM_yyyy_HH_mm_ss");
		Date date = new Date();
		
		try {
			File myObj = new File("Zarazeni" + File.separator + FileWatcher.file);
			Scanner myReader = new Scanner(myObj);
			String trenutnoZarazenih = myReader.nextLine();
		    String	brojOporavljenih = myReader.nextLine();
			String ukupnoZarazenih = myReader.nextLine();
			myReader.close();
			
			PrintWriter writer = new PrintWriter(new FileWriter("IzlazneInformacije"+File.separator+"SIM-JavaKov-20-"+formatter.format(date) +".txt"));
			StringBuilder sb = new StringBuilder();
						
			sb.append("Trenutno zarazenih: "+trenutnoZarazenih);
			sb.append('\n');
			sb.append("Ukupno Zarazenih: "+ukupnoZarazenih);
			sb.append("\n");
			sb.append("Broj oporavljenih: "+brojOporavljenih);
			sb.append("\n");
			sb.append("Broj stanovnika: "+map.getBrojStanovnika());
			sb.append("\n");
			sb.append("Broj ambulantnih vozila: "+map.getBrojAmbulanti());
			sb.append("\n");
			sb.append("Broj kuca: "+map.getBrojKuca());
			sb.append("\n");
			sb.append("Broj ambulantni: "+map.getBrojAmbulanti());
			sb.append("\n");
			sb.append("Broj punktova: "+map.getBrojPunktova());
			sb.append("\n");
			sb.append("Vrijeme trajanja simulacije: "+(double)(finish-start)/1_000_000_000.00);
			sb.append("\n");
			
			writer.write(sb.toString());
			writer.close();
		} catch (IOException e) {
			Logger.getLogger(Logger.GLOBAL_LOGGER_NAME).log(Level.WARNING, e.fillInStackTrace().toString());
		}
		for(Thread thread:threadList) {
			try {
				thread.join();
			} catch (InterruptedException e) {
				Logger.getLogger(Logger.GLOBAL_LOGGER_NAME).log(Level.WARNING, e.fillInStackTrace().toString());
			}
		}
		}
		MyLogger.close();
		Platform.exit();
		System.exit(0);

	}

	@FXML
	public void posaljiAmbulantnoVozilo() {
		if (alarmi.size() > 0) {
			Label label = alarmi.pop();
			zarazeni.getChildren().remove(label);
			map.pozovi124();
		}
	}

	@FXML
	public void pregledStanjaAmbulanti() {

		stanjeAmbulanti.setDisable(true);
		
		Stage newWindow = new Stage();
		ScrollPane scrollPane=new ScrollPane();
		StackPane stackAmbulante = new StackPane();
		
		ambulante = new VBox();
		scrollPane.setContent(ambulante);
		scrollPane.setPannable(false);
		ambulante.setPrefWidth(150);
		
		Button kreirajAmbulantu = new Button("Kreiraj\nambulantu");
		kreirajAmbulantu.setWrapText(true);
		kreirajAmbulantu.setAlignment(Pos.CENTER);

		stackAmbulante.getChildren().add(kreirajAmbulantu);
		ambulante.getChildren().add(stackAmbulante);

		Scene secondScene = new Scene(scrollPane);

		Label label = new Label("POZICIJA | KAPACITET");
		StackPane stack = new StackPane();
		stack.getChildren().add(label);
		ambulante.getChildren().add(stack);
		
		map.stanjeAmbulanti();
		
		kreirajAmbulantu.setOnAction(e -> {
			map.dodajAmbulantu();
		
		});
		newWindow.setHeight(500);
		newWindow.setTitle("STANJE AMBULANTI");
		newWindow.setScene(secondScene);
		newWindow.setOnCloseRequest(e -> stanjeAmbulanti.setDisable(false));
		newWindow.initStyle(StageStyle.UTILITY);
		newWindow.setResizable(false);
		newWindow.initModality(Modality.APPLICATION_MODAL);
		newWindow.showAndWait();

	}

	@FXML
	public void pregledajStatistickePodatke() {
		try {
			this.prikaziStatistiku();
		} catch (IOException e) {
			Logger.getLogger(Logger.GLOBAL_LOGGER_NAME).log(Level.WARNING, e.fillInStackTrace().toString());
		}

	}

	private class Polje extends StackPane {

		Set<Integer> IDNum = new HashSet<Integer>();

		Polje(int x, int y) {
			super();
			setMinWidth(1);
			setMinHeight(1);
			setStyle("-fx-background-color:WHITE");
			relocate(x * FIELDSIZE, y * FIELDSIZE);
			setBorder(new Border(new BorderStroke(Color.ANTIQUEWHITE, BorderStrokeStyle.SOLID, CornerRadii.EMPTY,
					BorderWidths.DEFAULT)));
			this.setOnMousePressed(e -> mouseClicked());

		}

		public void setOsoba(Integer ID, Color color, int size) {

			Ellipse circle = new Ellipse();

			circle.setFill(Color.TRANSPARENT);
			circle.setStroke(color);
			circle.setRadiusX(this.getWidth() / size);
			circle.setRadiusY(this.getHeight() / size);

			this.heightProperty().addListener(e -> circle.setRadiusY(this.getHeight() / size));
			this.widthProperty().addListener(e -> circle.setRadiusX(this.getWidth() / size));

			this.getChildren().add(circle);

			shapes.put(ID, circle);
			IDNum.add(ID);

		}

		public void setPosition(Integer ID) {
			if(!this.getChildren().contains(shapes.get(ID)))
			{this.getChildren().add(shapes.get(ID));
			IDNum.add(ID);
			}
		}

		public void removePosition(Integer ID) {
			Shape shape = shapes.get(ID);
			this.getChildren().remove(shape);
			IDNum.remove(ID);

		}

		public void mouseClicked() {

			for (Integer ID : IDNum) {
				Label t = imena.get(ID);
				Label label = new Label();
				label.setTextFill(t.getTextFill());
				label.textProperty().bind(t.textProperty());
				label.setOnMouseClicked(e -> odabrani.getChildren().remove(label));
				odabrani.getChildren().add(label);
			}
		}

	}

	private void markPosition(Label label) {

		String text = label.getText();
		int i = text.indexOf(':');
		int j = text.lastIndexOf(':');
		int ID = Integer.parseInt(text.substring(i + 1, j));
		Shape shape = shapes.get(ID);
		shape.setFill(Color.BLACK);

	}

	private void unmarkPosition(Label label) {
		String text = label.getText();
		int i = text.indexOf(':');
		int j = text.lastIndexOf(':');
		int ID = Integer.parseInt(text.substring(i + 1, j));
		Shape shape = shapes.get(ID);
		shape.setFill(Color.TRANSPARENT);

	}

	private void generisiMatricu() {
		panes = new GridPane();
		panes.setGridLinesVisible(false);
		panes.setHgap(2);
		panes.setVgap(2);
		polja = new Polje[dim * dim];
		shapes = new HashMap<Integer, Shape>();
		pozicije = new HashMap<Integer, Label>();
		imena = new HashMap<Integer, Label>();

		for (int i = 0; i < dim; i++)
			for (int j = 0; j < dim; j++) {
				polja[i * dim + j] = new Polje(i, j);
				polja[i * dim + j].prefHeightProperty().bind(root.heightProperty());
				polja[i * dim + j].prefWidthProperty().bind(root.widthProperty());
				panes.add(polja[i * dim + j], j, i);
			}

		root.setCenter(panes);

		stanovnici.prefWidthProperty().bind(tabela.widthProperty().multiply(0.33));
		odabrani.prefWidthProperty().bind(tabela.widthProperty().multiply(0.33));
		zarazeni.prefWidthProperty().bind(tabela.widthProperty().multiply(0.33));

		posaljiVozilo.setTooltip(new Tooltip("Posalji ambulantno vozilo"));
		stanjeAmbulanti.setTooltip(new Tooltip("Pregledaj stanje ambulanti"));
		statistika.setTooltip(new Tooltip("Pregledaj statisticke podatke"));
		pokreni.setDisable(true);
	}
	public void inicijalizacija(){
		app = this;
		
		generisiMatricu();
		statistika.setDisable(true);
	}
	

	
	public void nastaviSimulaciju() throws Exception {
		app = this;
		deserijalizuj();
		if(map!=null) {
		dim=map.getDimenzija();
		generisiMatricu();
		omoguciKretanje.setDisable(true);
		fileWatcher = new FileWatcher(this);
		fileWatcher.start();
		start=System.nanoTime();
		
		map.updateAll();
		map.pokreni();
		}
		
	}
	@Override
	public void setAmbulanta(Pozicija pozicija, int kapacitet) {
		Label label = new Label(pozicija.toString() + "  " + kapacitet);
		StackPane stack = new StackPane();
		stack.getChildren().add(label);
		ambulante.getChildren().add(stack);
	}

	@Override
	public void dodajOsobu(Osoba osoba, Kuca kuca) {
		Pozicija pozicija=osoba.getPozicija();
		if ((year - osoba.getRodjendan()) >= 65) {
			polja[pozicija.getX() * dim + pozicija.getY()].setOsoba(osoba.getIDBroj(),
					kuca.getColor(), 2);
		} else if ((year - osoba.getRodjendan()) < 18) {
			polja[pozicija.getX() * dim + pozicija.getY()].setOsoba(osoba.getIDBroj(),
					kuca.getColor(), 8);
		} else {
			polja[pozicija.getX() * dim + pozicija.getY()].setOsoba(osoba.getIDBroj(),
					kuca.getColor(), 4);
		}

		Label labelOsoba = new Label();
		Label labelPozicija = new Label();
		
		HBox box = new HBox();
		
		stanovnici.getChildren().add(box);
		
		box.getChildren().add(labelOsoba);
		box.getChildren().add(labelPozicija);
		
		labelOsoba.setTextFill(kuca.getColor());
		labelOsoba.setOnMousePressed(e -> this.markPosition(labelOsoba));
		labelOsoba.setOnMouseReleased(e -> this.unmarkPosition(labelOsoba));
		labelOsoba.setText(osoba + " ");
		
		pozicije.put(osoba.getIDBroj(), labelPozicija);
		imena.put(osoba.getIDBroj(), labelOsoba);
		
		labelPozicija.setText("" + pozicija);
		labelPozicija.setTextFill(kuca.getColor());
		
		tabela.snapshot(new SnapshotParameters(), new WritableImage(1, 1));
	}


	private void prikaziStatistiku() throws IOException {
		
		
		VBox vbox = new VBox();
		
		Button preuzmiPodatke = new Button("Preuzmi podatke");
		Button statistikaZarazenih = new Button("Oporavljeni-Zarazeni");
		Button statistikaPoStarosti = new Button("Zarazeni po starosti");
			
		String trenutnoZarazenih;
		String brojOporavljenih;
		String ukupnoZarazenih;
		
		Scene secondScene = new Scene(vbox);
		
		vbox.getChildren().add(statistikaZarazenih);
		vbox.getChildren().add(statistikaPoStarosti);
		vbox.getChildren().add(preuzmiPodatke);
		
		
		ObservableList<PieChart.Data> pieChartData;
		try {
			
			File myObj = new File("Zarazeni" + File.separator + FileWatcher.file);
			Scanner myReader = new Scanner(myObj);
			trenutnoZarazenih = myReader.nextLine();
			brojOporavljenih = myReader.nextLine();
			ukupnoZarazenih = myReader.nextLine();
			
			pieChartData = FXCollections.observableArrayList(
					new PieChart.Data("Trenutno zarazenih", Integer.parseInt(trenutnoZarazenih)),
					new PieChart.Data("Ukupno zarazenih", Integer.parseInt(trenutnoZarazenih)),
					new PieChart.Data("Oporavljenih", Integer.parseInt(brojOporavljenih)));
			
			pieChartData.forEach(e -> e.setName(e.getName() + " " + (int) e.getPieValue()));
			PieChart zarazeni = new PieChart(pieChartData);

			ObservableList<PieChart.Data> pieChartData2 = FXCollections.observableArrayList(
					new PieChart.Data("Starih", map.bolesnoStarih()), new PieChart.Data("Djece", map.bolesnoDjece()),
					new PieChart.Data("Odraslih", map.bolesnoOdraslih()));
			
			pieChartData2.forEach(e -> e.setName(e.getName() + " " + (int) e.getPieValue()));
			PieChart ozdravili = new PieChart(pieChartData2);

			Stage newWindow = new Stage();

			statistikaZarazenih.setOnAction(e -> {
				StackPane stackZarazeni = new StackPane();
				stackZarazeni.setPrefSize(400, 400);
				stackZarazeni.setMaxSize(400, 400);
				stackZarazeni.setMinSize(400, 400);
				stackZarazeni.getChildren().add(zarazeni);
				Stage stage = new Stage();
				stage.setScene(new Scene(stackZarazeni));
				stage.initStyle(StageStyle.UTILITY);
				stage.setResizable(false);
				stage.initModality(Modality.APPLICATION_MODAL);
				stage.showAndWait();
			});

			statistikaPoStarosti.setOnAction(e -> {
				StackPane stackOzdravili = new StackPane();
				stackOzdravili.setPrefSize(400, 400);
				stackOzdravili.setMaxSize(400, 400);
				stackOzdravili.getChildren().add(ozdravili);
				Stage stage = new Stage();
				stage.setScene(new Scene(stackOzdravili));
				stage.initStyle(StageStyle.UTILITY);
				stage.setResizable(false);
				stage.initModality(Modality.APPLICATION_MODAL);
				stage.showAndWait();
			});
			preuzmiPodatke.setOnAction(e -> {

				PrintWriter writer;
				try {

					SimpleDateFormat formatter = new SimpleDateFormat("dd_MM_yyyy_HH_mm_ss");
					Date date = new Date();
					writer = new PrintWriter(new FileWriter(formatter.format(date) + "_statistika_po_starosti.csv"));
					StringBuilder sb = new StringBuilder();
					sb.append("Kategorija");
					sb.append(',');
					sb.append("Broj Zarazenih");
					sb.append("\r\n");
					sb.append("Odrasli");
					sb.append(',');
					sb.append("" + map.bolesnoOdraslih());
					sb.append("\r\n");
					sb.append("Djeca");
					sb.append(',');
					sb.append("" + map.bolesnoDjece());
					sb.append("\r\n");
					sb.append("stari");
					sb.append(',');
					sb.append("" + map.bolesnoStarih());
					sb.append("\r\n");

					writer.write(sb.toString());
					writer.close();

					writer = new PrintWriter(new FileWriter(formatter.format(date) + "_statistika.csv"));
					sb = new StringBuilder();
					sb.append("Trenutno zarazenih");
					sb.append(',');
					sb.append("Ukupno Zarazenih");
					sb.append(",");
					sb.append("Broj oporavljenih");
					sb.append("\r\n");
					sb.append(trenutnoZarazenih);
					sb.append(',');
					sb.append(ukupnoZarazenih);
					sb.append(",");
					sb.append(brojOporavljenih);
					sb.append("\r\n");

					writer.write(sb.toString());
					writer.close();

				} catch (IOException e1) {
					Logger.getLogger(Logger.GLOBAL_LOGGER_NAME).log(Level.WARNING, e1.fillInStackTrace().toString());
				}

			});

			map.zaustavi();

			newWindow.setTitle("Statistika");
			newWindow.setScene(secondScene);
			newWindow.initStyle(StageStyle.UTILITY);
			newWindow.setResizable(false);
			newWindow.initModality(Modality.APPLICATION_MODAL);
			newWindow.setOnCloseRequest(e -> map.pokreni());
			newWindow.showAndWait();


			myReader.close();
		} catch (FileNotFoundException e) {
			Logger.getLogger(Logger.GLOBAL_LOGGER_NAME).log(Level.WARNING, e.fillInStackTrace().toString());
		}

	}

	public void setMap(int brojPunktova, int brojKuca, int brojOdraslih, int brojStarih, int brojDjece,int brojVozila) {
		threadList = new ArrayList<Thread>(dim);
		map = new Grad(dim);
		int brojCitizens = brojOdraslih + brojStarih + brojDjece;
		map.generisiObjekte(brojPunktova, brojKuca, brojCitizens, brojVozila);
		map.postaviStanovnike(brojOdraslih, brojStarih, brojDjece);

	}
	public int getDimenzija() {
		dim = 15 + new Random().nextInt(7)*2;
		return dim;
	}

}
