package net.etfbl.stanovnici;

import net.etfbl.mapa.Pozicija;
import net.etfbl.mapa.PravilaGrada;

public class Dijete extends Osoba {

	public Dijete(String name, String surname, int birthYear, boolean gender, PravilaGrada city, Kuca home) {
		super(name, surname, birthYear, gender, city, home);
		max = (city.getDimenzija() )/ 2;
		referentnaPozicija = new Pozicija((city.getDimenzija()) / 2, (city.getDimenzija() )/ 2);
	}

}
