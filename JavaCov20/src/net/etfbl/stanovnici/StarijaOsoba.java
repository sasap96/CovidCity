package net.etfbl.stanovnici;

import net.etfbl.mapa.Pozicija;
import net.etfbl.mapa.PravilaGrada;

public class StarijaOsoba extends Osoba {

	public StarijaOsoba(String name, String surname, int birthYear, boolean gender, PravilaGrada city, Kuca home) {
		super(name, surname, birthYear, gender, city, home);
		max = 3;
		referentnaPozicija = Pozicija.calculateRefPoint(home.getPosition(), city.getDimenzija(), max);

	}

}
