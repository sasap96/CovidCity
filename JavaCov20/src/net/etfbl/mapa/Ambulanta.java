package net.etfbl.mapa;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.Random;


public class Ambulanta implements Serializable {

	private final Pozicija pozicija;
	private int kapacitet;

	LinkedList<ZdravstveniKarton> zarazeni = new LinkedList<ZdravstveniKarton>();

	public Ambulanta(Pozicija pozicija, int brojStanovnika) {
		this.pozicija = new Pozicija(pozicija);
		kapacitet = brojStanovnika / 10 + new Random().nextInt(brojStanovnika / 5);
	}

	public synchronized boolean dodajPacijenta(Integer pacijent) {
		if (kapacitet > 0) {

			zarazeni.add(new ZdravstveniKarton(pacijent));
			kapacitet--;
			return true;
		} else {
			return false;
		}
	}

	public synchronized boolean stanjePacijenta(Integer pacijent, double temperatura) {

		if (temperatura < 37.0) {
			boolean flag = zarazeni.stream().anyMatch(e -> e.idPacijenta.equals(pacijent) && e.stanje == 2);
			if (flag) {
				zarazeni.removeIf(e -> e.idPacijenta.equals(pacijent));
				kapacitet++;
				return true;
			}
			zarazeni.stream().filter(e -> e.idPacijenta.equals(pacijent)).forEach(e -> e.inc());
		} else {
			zarazeni.stream().filter(e -> e.idPacijenta.equals(pacijent)).findAny().ifPresent(e -> e.stanje = 0);
		}
		return false;

	}

	public Pozicija getPozicija() {
		return pozicija;
	}

	public synchronized int getKapacitet() {
		return kapacitet;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + pozicija.hashCode();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Ambulanta other = (Ambulanta) obj;
		if (!pozicija.equals(other.pozicija))
			return false;
		return true;
	}
}

class ZdravstveniKarton implements Serializable {

	Integer idPacijenta;
	int stanje = 0;

	ZdravstveniKarton(Integer id) {
		this.idPacijenta = id;
	}

	void inc() {
		stanje = stanje + 1;
	}
}
