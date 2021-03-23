package net.etfbl.stanovnici;

import java.io.Serializable;
import java.time.Year;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.scene.paint.Color;
import net.etfbl.mapa.Pozicija;
import net.etfbl.mapa.PravilaGrada;

public abstract class Osoba implements Serializable, Runnable {

	private PravilaGrada grad;

	private String ime;
	private String prezime;
	private final Integer ID;
	private final int godinaRodjenja;
	private boolean pol;
	private Integer brojKuce;
	private Kuca kuca;

	private double temperatura;
	private int smijerKretanja;
	private volatile boolean zarazen = false;

	private Pozicija trenutnaPozicija;
	Pozicija referentnaPozicija;

	private volatile boolean izvrsavaj = true;
	private volatile boolean pauziran = false;
	private volatile boolean idiKuci = false;
	private volatile boolean idiUBolnicu = false;

	private final ReentrantLock pauseLock = new ReentrantLock();

	int max;
	private Pozicija privremenaPozicija, zadanaPozicija;

	Osoba(String ime, String prezime, int godinaRodjenja, boolean pol, PravilaGrada grad, Kuca kuca) {
		this.ime = ime;
		this.prezime = prezime;
		this.godinaRodjenja = godinaRodjenja;
		this.pol = pol;
		this.grad = grad;
		this.kuca = kuca;
		this.ID = hashCode();
		this.brojKuce = kuca.getBrojKuce();
		this.trenutnaPozicija = new Pozicija(kuca.getPosition());
		privremenaPozicija = new Pozicija(kuca.getPosition());
		izmjeriTemperaturu();
	}

	public String getIme() {
		return ime;

	}

	public String getPrezime() {
		return prezime;
	}

	public int getRodjendan() {
		return godinaRodjenja;
	}

	public boolean getPol() {
		return pol;

	}

	public Color getBojaKuce() {
		return kuca.getColor();
	}

	public double getTemperatura() {
		return temperatura;
	}

	public int getIDBroj() {
		return ID;
	}

	public int getSmijerKretanja() {
		return smijerKretanja;
	}

	public Pozicija getPozicija() {
		return trenutnaPozicija;
	}

	public int getKucniID() {
		return brojKuce;
	}

	public boolean moramProci() {
		return idiUBolnicu || idiKuci;
	}

	public boolean isUkucanin(int id) {
		List<Integer> family = kuca.getMembers();
		return family.stream().anyMatch(e -> e == id);
	}

	public void stop() {
		izvrsavaj = false;
	}

	public void start() {
		izvrsavaj = true;
	}

	public int dob() {
		return Year.now().getValue() - this.godinaRodjenja;
	}

	public void zaustaviSe() {
		pauziran = true;
	}

	public void pokreniSe() {

		synchronized (pauseLock) {
			pauziran = false;
			pauseLock.notifyAll();
		}

	}

	public void slobodanSi() {
		idiKuci = false;
		idiUBolnicu = false;

		privremenaPozicija = kuca.getPosition();
		grad.novaPozicija(privremenaPozicija, ID, smijerKretanja);
		trenutnaPozicija = kuca.getPosition();

		if (!zarazen) {
			synchronized (pauseLock) {
				pauziran = false;
				pauseLock.notifyAll();
			}

		}
		zarazen = false;
	}

	public void idiKuci() {
		pokreniSe();
		idiKuci = true;
	}

	public void idiDoktoru(Pozicija pozicija) {
		zadanaPozicija = pozicija;
		idiUBolnicu = true;
		pokreniSe();

	}

	private void izmjeriTemperaturu() {
		Random random = new Random();
		this.temperatura = 36.1 + random.nextDouble();
		if (this.temperatura > 37)
			this.temperatura = this.temperatura + 3 * random.nextDouble();
	}

	public void setZarazen() {
		zarazen = true;
	}

	public boolean zarazenSi() {
		return zarazen;
	}

	@Override
	public String toString() {
		String data = getIme() + " " + getPrezime() + " :" + getIDBroj() + ": " + getRodjendan();
		return data;
	}

	@Override
	public void run() {

		Timer timer = new Timer();
		timer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				izmjeriTemperaturu();
				if (pauziran == true && idiUBolnicu == true) {
					grad.vizita(trenutnaPozicija, ID, temperatura);
				}

			};
		}, 0, 30000);

		Random random = new Random();

		try {
			Thread.sleep((long) ((1 + (Math.random() * (1))) * 1000));
		} catch (InterruptedException e) {
			Logger.getLogger(Logger.GLOBAL_LOGGER_NAME).log(Level.INFO, e.fillInStackTrace().toString());
		}

		while (izvrsavaj) {

			synchronized (pauseLock) {
				if (!izvrsavaj) {

					break;
				}
				if (pauziran) {

					try {
						synchronized (pauseLock) {
							pauseLock.wait();
						}
					} catch (InterruptedException e) {
						Logger.getLogger(Logger.GLOBAL_LOGGER_NAME).log(Level.INFO, e.fillInStackTrace().toString());

						break;
					}

					if (!izvrsavaj) {
						break;
					}
				}
			}

			if (!idiKuci && !idiUBolnicu) {

				smijerKretanja = random.nextInt(8);
				privremenaPozicija = new Pozicija(trenutnaPozicija);

				switch (smijerKretanja) {
				case 0:
					privremenaPozicija.setX(privremenaPozicija.getX() - 1);
					break;
				case 1:
					privremenaPozicija.setX(privremenaPozicija.getX() + 1);
					break;
				case 2:
					privremenaPozicija.setY(privremenaPozicija.getY() + 1);
					break;
				case 3:
					privremenaPozicija.setY(privremenaPozicija.getY() - 1);
					break;
				case 4:
					privremenaPozicija.setX(privremenaPozicija.getX() - 1);
					privremenaPozicija.setY(privremenaPozicija.getY() - 1);
					break;
				case 5:
					privremenaPozicija.setX(privremenaPozicija.getX() - 1);
					privremenaPozicija.setY(privremenaPozicija.getY() + 1);
					break;
				case 6:
					privremenaPozicija.setX(privremenaPozicija.getX() + 1);
					privremenaPozicija.setY(privremenaPozicija.getY() - 1);
					break;
				case 7:
					privremenaPozicija.setX(privremenaPozicija.getX() + 1);
					privremenaPozicija.setY(privremenaPozicija.getY() + 1);
					break;
				}

				if (privremenaPozicija.distance(referentnaPozicija) <= max) {

					smijerKretanja = izracunajSmijerKretanja(trenutnaPozicija, privremenaPozicija);

					if (grad.novaPozicija(privremenaPozicija, ID, smijerKretanja)) {
						trenutnaPozicija = privremenaPozicija;
					}

					try {
						Thread.sleep((long) ((1 + (Math.random() * (1))) * 1000));
					} catch (InterruptedException e) {
						Logger.getLogger(Logger.GLOBAL_LOGGER_NAME).log(Level.INFO, e.fillInStackTrace().toString());

					}
				}

			} else {

				if (idiUBolnicu) {
					privremenaPozicija = Pozicija.closestPosition(trenutnaPozicija, zadanaPozicija,
							grad.getDimenzija());
					smijerKretanja = izracunajSmijerKretanja(trenutnaPozicija, privremenaPozicija);

					if (grad.novaPozicija(privremenaPozicija, ID, smijerKretanja))
						trenutnaPozicija = privremenaPozicija;

					if (trenutnaPozicija.equals(zadanaPozicija)) {
						pauziran = true;
						grad.oslobodiAmbulantnoVozilo();
					}

					try {
						Thread.sleep((long) 200);
					} catch (InterruptedException e) {
						Logger.getLogger(Logger.GLOBAL_LOGGER_NAME).log(Level.INFO, e.fillInStackTrace().toString());
					}
				} else {
					privremenaPozicija = Pozicija.closestPosition(trenutnaPozicija, kuca.getPosition(),
							grad.getDimenzija());
					smijerKretanja = izracunajSmijerKretanja(trenutnaPozicija, privremenaPozicija);

					if (grad.novaPozicija(privremenaPozicija, ID, smijerKretanja))
						trenutnaPozicija = privremenaPozicija;

					if (trenutnaPozicija.equals(kuca.getPosition())) {
						pauziran = true;
					}

					try {
						Thread.sleep((long) ((1 + (Math.random() * (1))) * 1000));
					} catch (InterruptedException e) {
						Logger.getLogger(Logger.GLOBAL_LOGGER_NAME).log(Level.INFO, e.fillInStackTrace().toString());

					}
				}
			}

		}

		timer.cancel();
	}

	private int izracunajSmijerKretanja(Pozicija trenutnaPozicija, Pozicija zeljenaPozicija) {
		int x = trenutnaPozicija.getX() - zeljenaPozicija.getX();
		int y = trenutnaPozicija.getY() - zeljenaPozicija.getY();

		if (x > 0) {

			if (y > 0) {
				return 4;
			} else if (y < 0)
				return 5;
			else
				return 0;
		} else if (x < 0) {
			if (y > 0) {
				return 6;
			} else if (y < 0)
				return 7;
			else
				return 1;

		} else {
			if (y > 0)
				return 3;
			else
				return 2;
		}

	}
}
