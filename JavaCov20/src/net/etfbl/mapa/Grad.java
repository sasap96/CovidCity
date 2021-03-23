package net.etfbl.mapa;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javafx.scene.paint.Color;
import net.etfbl.application.Controller;
import net.etfbl.application.FileWatcher;
import net.etfbl.stanovnici.Dijete;
import net.etfbl.stanovnici.Kuca;
import net.etfbl.stanovnici.OdraslaOsoba;
import net.etfbl.stanovnici.Osoba;
import net.etfbl.stanovnici.StarijaOsoba;

public class Grad implements PravilaGrada, Serializable {

	private List<Set<Integer>> pozicijeStanovnika;
	private ArrayList<Punkt> punktovi = new ArrayList<Punkt>();
	private HashMap<Integer, Osoba> stanovnici = new HashMap<Integer, Osoba>();
	private HashMap<Pozicija, Ambulanta> ambulante = new HashMap<Pozicija, Ambulanta>();
	private HashMap<Integer, Kuca> kuce = new HashMap<Integer, Kuca>();
	private Stack<Alarm> alarmi = new Stack<Alarm>();
	
	private int dimenzija;
	private int brojStanovnika;
	private int brojVozila;
	
	private ReentrantLock lock = new ReentrantLock();
	private ReentrantLock lock1 = new ReentrantLock();
	
	private int trenutnoZarazenih = 0;
	public int getBrojStanovnika() {
		return brojStanovnika;
	}

	public int getBrojVozila() {
		return brojVozila;
	}

	public int getBrojKuca() {
		return kuce.size();
	}
	public int getBrojAmbulanti() {
		return ambulante.size();
	}
	public int getBrojPunktova() {
		return punktovi.size();
	}

	private int brojOporavljenih = 0;
	private int ukupanoZarazenih = 0;

	public Grad(int dimenzija) {
		this.dimenzija = dimenzija;
		pozicijeStanovnika = Stream.generate(HashSet<Integer>::new).limit(dimenzija * dimenzija)
																   .collect(Collectors.toList());

	}

	@Override
	public boolean novaPozicija(Pozicija pozicija, Integer ID, int smijer) {

		Osoba citizen = stanovnici.get(ID);
		Pozicija staraPozicija = citizen.getPozicija();

		lock.lock();
		if (!imaLiNekog(pozicija, ID) || citizen.moramProci()) {
			pozicijeStanovnika.get(staraPozicija.getX() * dimenzija + staraPozicija.getY()).removeIf(e -> ID.equals(e));
			pozicijeStanovnika.get(pozicija.getX() * dimenzija + pozicija.getY()).add(ID);
			Controller.app.azurirajPoziciju(citizen, staraPozicija, pozicija, smijer);

		} else {
			lock.unlock();
			return false;

		}
		lock.unlock();
		Punkt check = punktovi.stream().filter(e -> e.getPozicija().distance(pozicija) <= 1).findAny().orElse(null);
		if (!citizen.zarazenSi()) {
			if (check != null) {
				if (citizen.getTemperatura() > 37.0) {
					
					citizen.zaustaviSe();
					citizen.setZarazen();

					lock1.lock();
					
					alarmi.push(new Alarm(pozicija, ID, citizen.getKucniID()));
					Controller.app.upozori(citizen, pozicija);
					trenutnoZarazenih++;
					ukupanoZarazenih++;

					try {
						BufferedWriter writer = new BufferedWriter(new FileWriter("Zarazeni" + File.separator + FileWatcher.file));
						writer.write("" + trenutnoZarazenih);
						writer.newLine();
						writer.write("" + brojOporavljenih);
						writer.newLine();
						writer.write("" + ukupanoZarazenih);
						writer.close();
					} catch (IOException e) {

						Logger.getLogger(Logger.GLOBAL_LOGGER_NAME).log(Level.WARNING, e.fillInStackTrace().toString());
					}


					lock1.unlock();
				}
			}
		}

		return true;
	}

	@Override
	public int getDimenzija() {
		return dimenzija;
	}
	private boolean informacijePozicija(Integer pozicija, Osoba osoba,Predicate<Integer> pravilo) {
		
		
		boolean uslov=pozicijeStanovnika.get(pozicija).stream()
		    		   						   .anyMatch(pravilo);
		return uslov;
	}
	
	private boolean imaLiNekog(Pozicija pozicija, Integer ID) {
		boolean check;
		Osoba osoba = stanovnici.get(ID);
		List<Integer> positions;
		
		Predicate<Integer> praviloOdrasli=iden -> ((!osoba.isUkucanin(iden)) &&
		   			  							  (!(stanovnici.get(iden).dob() < 18) ))&&
		   			  							  (!stanovnici.get(iden).moramProci());
		
		Predicate<Integer> praviloDjeca=iden -> ((!osoba.isUkucanin(iden)) && 
												stanovnici.get(iden).dob() >= 65) && 
		  			 							(!stanovnici.get(iden).moramProci());
		
		Predicate<Integer> praviloStari=iden -> ((!osoba.isUkucanin(iden)) ) &&
		                                         (!stanovnici.get(iden).moramProci());

		positions = Stream.iterate(0, c -> c + 1)
						  .limit(dimenzija * dimenzija)
						  .filter(e -> pozicija.distance(new Pozicija(e / dimenzija, e % dimenzija)) == 2)
						  .collect(Collectors.toList());

		
		if (osoba.dob() < 65 && osoba.dob() > 18) {
			
			check = positions.stream()
							 .anyMatch(e->informacijePozicija(e,osoba,praviloOdrasli));
		
		} else if (osoba.dob() < 18) {
			
			check = positions.stream()
					         .anyMatch(e->informacijePozicija(e,osoba,praviloDjeca));

		} else {
			
			check = positions.stream()
					         .anyMatch(e->informacijePozicija(e,osoba,praviloStari));
		}
		return check;
	
		
	}

	public void postaviPunkt(Pozicija position) {
		punktovi.add(new Punkt(position));
	}

	public void dodajOsobu(Osoba osoba, Pozicija pozicija) {

		if (stanovnici.putIfAbsent(osoba.getIDBroj(), osoba) == null) {
			pozicijeStanovnika.get(pozicija.getX() * dimenzija + pozicija.getY()).add(osoba.getIDBroj());
		}
	}

	public void postaviKucu(Kuca kuca) {

		kuce.putIfAbsent(kuca.getBrojKuce(), kuca);
		
	}

	public void pokreni() {
		stanovnici.forEach((i, c) -> c.start());
		start();

	}

	public void zavrsi() {
		stanovnici.forEach((i, c) -> c.stop());
		Controller.threadList.forEach(c -> c.interrupt());
	}

	public void zaustavi() {
		zavrsi();
		Controller.threadList.removeAll(Controller.threadList);

	}

	public void start() {

		stanovnici.forEach((i, c) -> {
			Controller.threadList.add(new Thread(c));
		});
		Controller.threadList.forEach(c -> c.start());

	}

	public Osoba getStanovnik(Integer id) {
		return stanovnici.get(id);
	}

	public void generisiObjekte(int brojPunktova, int brojKuca, int brojStanovnika, int brojVozila) {

		this.brojVozila = brojVozila;
		this.brojStanovnika = brojStanovnika;

		List<Pozicija> positions = new LinkedList<Pozicija>();

		ambulante.put(new Pozicija(0, 0), new Ambulanta(new Pozicija(0, 0), brojStanovnika));
		ambulante.put(new Pozicija(0, dimenzija - 1), new Ambulanta(new Pozicija(0, dimenzija - 1), brojStanovnika));
		ambulante.put(new Pozicija(dimenzija - 1, 0), new Ambulanta(new Pozicija(dimenzija - 1, 0), brojStanovnika));
		ambulante.put(new Pozicija(dimenzija - 1, dimenzija - 1),new Ambulanta(new Pozicija(dimenzija - 1, dimenzija - 1), brojStanovnika));
		
		Controller.app.dodajBolnicu(new Pozicija(0, 0));
		Controller.app.dodajBolnicu(new Pozicija(0, dimenzija - 1));
		Controller.app.dodajBolnicu(new Pozicija(dimenzija - 1, 0));
		Controller.app.dodajBolnicu(new Pozicija(dimenzija - 1, dimenzija - 1));
		
		for (int i = 1; i < (dimenzija - 1); i++)
			for (int j = 1; j < (dimenzija - 1); j++)
				positions.add(new Pozicija(i, j));

		Random random = new Random();

		for (int i = 0; i < brojPunktova; i++) {
			if (positions.size() > 0) {
				Pozicija position = positions.remove(random.nextInt(positions.size()));
				postaviPunkt(position);
				Controller.app.dodajPunkt(position);
			} else {
				break;
			}
		}

		for (int i = 0; i < brojKuca; i++) {

			double r = 0.8 + (random.nextDouble() - 0.8);
			double g = 0.8 + (random.nextDouble() - 0.8);
			double b = 0.8 + (random.nextDouble() - 0.8);
			
			if (positions.size() > 0) {
				
				int j = random.nextInt(positions.size());
				Pozicija position = positions.remove(j);
				positions.removeIf(e -> e.distance(position) <= 3);
				Color color = Color.color(r, g, b);

				Kuca house = new Kuca(position, r, g, b);
				postaviKucu(house);
				Controller.app.dodajKucu(position, color);
			
			} else {
				break;
			}
		}
		
	}


	public void postaviStanovnike(int brojOdraslih, int brojStarih, int brojDjece) {

		File muskaImena = new File("files" + File.separator + "muskaimena");
		File zenskaImena = new File("files" + File.separator + "zenskaimena");
		File prezimena = new File("files" + File.separator + "prezimena");

		int year = Calendar.getInstance().get(Calendar.YEAR);

		Random random = new Random();

		String firstName;
		String lastName;
		boolean pol;
		int godinaRodjenja;

		List<String> prezimenaList = new ArrayList<String>(kuce.size());

		for (Entry<Integer, Kuca> pair : kuce.entrySet()) {
			Kuca home = pair.getValue();
			lastName = choose(prezimena);
			prezimenaList.add(lastName);
			
			pol = random.nextBoolean();
			boolean isosoba = random.nextBoolean();

			if (pol) {
				firstName = choose(zenskaImena);
			} else {
				firstName = choose(muskaImena);
			}
			
			if (isosoba && brojOdraslih > 0) {
				
				int godine = 18 + random.nextInt(65 - 18);
				godinaRodjenja = year - godine;
				OdraslaOsoba osoba = new OdraslaOsoba(firstName, lastName, godinaRodjenja, pol, this, home);
				pair.getValue().addMember(osoba.getIDBroj());
				this.dodajOsobu(osoba, home.getPosition());
				Controller.app.dodajOsobu(osoba, home);
				brojOdraslih--;
			
			} else {
				
				int godine = 65 + random.nextInt(100 - 65);
				godinaRodjenja = year - godine;
				StarijaOsoba star = new StarijaOsoba(firstName, lastName, godinaRodjenja, pol, this, home);
				pair.getValue().addMember(star.getIDBroj());
				this.dodajOsobu(star, home.getPosition());
				Controller.app.dodajOsobu(star, home);
				brojStarih--;
			}
		}
		
		int counter = 0;
		while (brojStarih > 0 || brojOdraslih > 0 || brojDjece > 0) {
			if (counter == kuce.size())
				
				counter = 0;
			for (Entry<Integer, Kuca> pair : kuce.entrySet()) {
				
				Kuca home = pair.getValue();
				lastName = prezimenaList.get(counter);
				int godine;
				pol = random.nextBoolean();

				if (pol) {
				
					firstName = choose(zenskaImena);
				} else {
				
					firstName = choose(muskaImena);
				}

				godine = random.nextInt(100);
				godinaRodjenja = year - godine;
				pol = random.nextBoolean();
			
				if (pol) {
					
					firstName = choose(zenskaImena);
				} else {
					
					firstName = choose(muskaImena);
				}
				if ((godine) >= 65) {
					if (brojStarih > 0) {
						
						StarijaOsoba star = new StarijaOsoba(firstName, lastName, godinaRodjenja, pol, this, home);
						pair.getValue().addMember(star.getIDBroj());
						this.dodajOsobu(star, home.getPosition());
						Controller.app.dodajOsobu(star, home);
						brojStarih--;
					}
				} else if ((godine) < 18) {
					if (brojDjece > 0) {
						
						Dijete dijete = new Dijete(firstName, lastName, godinaRodjenja, pol, this, home);
						pair.getValue().addMember(dijete.getIDBroj());

						this.dodajOsobu(dijete, home.getPosition());
						Controller.app.dodajOsobu(dijete, home);
						brojDjece--;
					}
				} else {
					if (brojOdraslih > 0) {
						OdraslaOsoba osoba = new OdraslaOsoba(firstName, lastName, godinaRodjenja, pol, this,
								pair.getValue());
						pair.getValue().addMember(osoba.getIDBroj());
						this.dodajOsobu(osoba, home.getPosition());
						Controller.app.dodajOsobu(osoba, home);
						brojOdraslih--;
					}
				}

				counter++;
			}
		}
	}

	private String choose(File f) {
		String result = "";
		Random rand = new Random();
		int n = 0;
		try (Scanner sc = new Scanner(f)) {
			while (sc.hasNext()) {
				++n;
				String line = sc.nextLine();
				if (rand.nextInt(n) == 0)
					result = line;
			}
		} catch (FileNotFoundException e) {
			Logger.getLogger(Logger.GLOBAL_LOGGER_NAME).log(Level.WARNING, e.fillInStackTrace().toString());
		}
		return result;
	}

	public void pozovi124() {

		lock1.lock();
		Alarm alarm = alarmi.pop();
		brojVozila--;
		lock1.unlock();
		
		Kuca house = kuce.get(alarm.getIdKuce());
		Integer ID = alarm.getId();
		house.getMembers().stream().filter(e -> e != alarm.getId()).filter(e -> !stanovnici.get(e).zarazenSi())
				.forEach(e -> stanovnici.get(e).idiKuci());

		try {
			Ambulanta hospital = ambulante.entrySet().stream().filter(e -> e.getValue().getKapacitet() > 0)
															  .findAny()
					                                          .orElse(null)
					                                          .getValue();
			hospital.dodajPacijenta(ID);
			stanovnici.get(ID).idiDoktoru(hospital.getPozicija());
		
		} catch (Exception e) {
			Logger.getLogger(Logger.GLOBAL_LOGGER_NAME).log(Level.INFO, e.fillInStackTrace().toString());
		}
		lock1.lock();
		if (brojVozila == 0)
			Controller.app.slobodnoVozilo(true);
		lock1.unlock();

	}

	public void vizita(Pozicija position, Integer ID, double temp) {
		Ambulanta hospital = ambulante.get(position);

		if (hospital.stanjePacijenta(ID, temp)) {

			lock1.lock();
			brojOporavljenih++;
			trenutnoZarazenih--;
			try {
				BufferedWriter writer = new BufferedWriter(new FileWriter("Zarazeni" + File.separator + FileWatcher.file));
				writer.write("" + trenutnoZarazenih);
				writer.newLine();
				writer.write("" + brojOporavljenih);
				writer.newLine();
				writer.write("" + ukupanoZarazenih);
				writer.close();
			} catch (IOException e) {

				Logger.getLogger(Logger.GLOBAL_LOGGER_NAME).log(Level.WARNING, e.fillInStackTrace().toString());
			}
			lock1.unlock();
			Kuca home = kuce.get(stanovnici.get(ID).getKucniID());
			stanovnici.get(ID).slobodanSi();

			boolean flag = true;
			for (Integer ids : home.getMembers()) {
				if (stanovnici.get(ids).zarazenSi())
					flag = false;

			}
			if (flag) {
				home.getMembers().forEach(e -> stanovnici.get(e).slobodanSi());
			}
		}

	}

	public void oslobodiAmbulantnoVozilo() {
		lock1.lock();
		brojVozila++;
		Controller.app.slobodnoVozilo(false);
		lock1.unlock();
	}

	public void dodajAmbulantu() {

		Set<Pozicija> keySet = ambulante.keySet();
		Pozicija pozicija;
		for (int i = 0; i < dimenzija; i++) {
			for (int j = 0; j < dimenzija; j++) {
				if (i == 0 || j == 0 || i == dimenzija - 1 || j == dimenzija - 1) {
					pozicija = new Pozicija(i, j);
					if (!keySet.contains(pozicija)) {
						ambulante.put(pozicija, new Ambulanta(pozicija, brojStanovnika));
						Controller.app.dodajBolnicu(pozicija);
						Controller.app.setAmbulanta(pozicija, ambulante.get(pozicija).getKapacitet());
						i = dimenzija;
						break;
					}
				}
			}

		}

	}

	public int stanjeAmbulanti() {
		Set<Pozicija> pozicije = ambulante.keySet();
		int kapacitet = 0;
		for (Pozicija pozicija : pozicije) {
			Controller.app.setAmbulanta(pozicija, ambulante.get(pozicija).getKapacitet());
			kapacitet = kapacitet + ambulante.get(pozicija).getKapacitet();
		}
		return kapacitet;
	}

	public int bolesnoDjece() {
		int counter = 0;
		int year = Calendar.getInstance().get(Calendar.YEAR);
		Set<Integer> set = stanovnici.keySet();
		
		for (Integer i : set) {
			Osoba osoba = stanovnici.get(i);
			if (osoba.zarazenSi() && ((year - osoba.getRodjendan()) < 18))
				counter++;
		}
		return counter;
	}

	public int bolesnoStarih() {
		int counter = 0;
		int year = Calendar.getInstance().get(Calendar.YEAR);
		Set<Integer> set = stanovnici.keySet();
		
		for (Integer i : set) {
			Osoba osoba = stanovnici.get(i);
			if (osoba.zarazenSi() && ((year - osoba.getRodjendan()) >= 65))
				counter++;
		}
		return counter;
	}

	public int bolesnoOdraslih() {
		int counter = 0;
		int year = Calendar.getInstance().get(Calendar.YEAR);
		Set<Integer> set = stanovnici.keySet();
		for (Integer i : set) {
		
			Osoba osoba = stanovnici.get(i);
			if (osoba.zarazenSi() && ((year - osoba.getRodjendan()) >= 18) && ((year - osoba.getRodjendan()) < 65))
				counter++;
		}
		return counter;
	}
	
	public void updateAll() {
		
		
		Set<Integer> kucniBrojevi = kuce.keySet();
		
		for (Integer broj : kucniBrojevi) {
			
			Controller.app.dodajKucu(kuce.get(broj).getPosition(), kuce.get(broj).getColor());
		}
		
	
		
		
		
		
		Set<Pozicija> pozicije = ambulante.keySet();
		
		for (Pozicija pozicija : pozicije) {
			Controller.app.dodajBolnicu(pozicija);
		
		}
		Set<Integer> IDs = stanovnici.keySet();
		
		for (Integer ID : IDs) {
			Osoba osoba=stanovnici.get(ID);
			int godine= Calendar.getInstance().get(Calendar.YEAR)-osoba.getRodjendan();
			if ((godine) >= 65) {
					Controller.app.dodajOsobu(osoba, kuce.get(osoba.getKucniID()));	
				}
			else if ((godine) < 18) {
					Controller.app.dodajOsobu(osoba,  kuce.get(osoba.getKucniID()));
					}
			 else {
				
					Controller.app.dodajOsobu(osoba,  kuce.get(osoba.getKucniID()));
				
				}
			
		}
		
		alarmi.forEach(e->Controller.app.upozori(stanovnici.get(e.getId()), e.getPozicija()));
		punktovi.forEach(e->Controller.app.dodajPunkt(e.getPozicija()));
		
		try {
			
			BufferedWriter writer = new BufferedWriter(new FileWriter("Zarazeni" + File.separator + FileWatcher.file));
			writer.write("" + trenutnoZarazenih);
			writer.newLine();
			writer.write("" + brojOporavljenih);
			writer.newLine();
			writer.write("" + ukupanoZarazenih);
			writer.close();
		
		} catch (IOException e) {
			Logger.getLogger(Logger.GLOBAL_LOGGER_NAME).log(Level.WARNING, e.fillInStackTrace().toString());
		}
	}

}

