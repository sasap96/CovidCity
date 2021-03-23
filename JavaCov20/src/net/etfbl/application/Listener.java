package net.etfbl.application;

import javafx.scene.paint.Color;
import net.etfbl.mapa.Pozicija;
import net.etfbl.stanovnici.Kuca;
import net.etfbl.stanovnici.Osoba;

public interface Listener {
	public void azurirajPoziciju(Osoba citizen, Pozicija staraPozicija, Pozicija novaPozicija, int smijer);

	public void dodajPunkt(Pozicija pozicija);

	public void dodajBolnicu(Pozicija pozicija);

	public void dodajKucu(Pozicija pozicija, Color color);

	public void dodajOsobu(Osoba osoba, Kuca kuca);

	public void upozori(Osoba osoba, Pozicija pozicija);

	public void slobodnoVozilo(boolean flag);

	public void setAmbulanta(Pozicija pozicija, int kapacitet);

}
