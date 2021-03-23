package net.etfbl.stanovnici;

import net.etfbl.mapa.Pozicija;
import net.etfbl.mapa.PravilaGrada;

public class OdraslaOsoba extends Osoba {

	public OdraslaOsoba(String name, String surname, int birthYear, boolean gender, PravilaGrada city, Kuca home) {
		super(name, surname, birthYear, gender, city, home);
		max = (int) Math.ceil(city.getDimenzija() * 0.25);
		referentnaPozicija = Pozicija.calculateRefPoint(home.getPosition(), city.getDimenzija(), max);
	}

}