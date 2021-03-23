package net.etfbl.mapa;

import java.io.Serializable;


public class Punkt implements Serializable {

	private final Pozicija pozicija;

	public Punkt(Pozicija pozicija) {
		this.pozicija = new Pozicija(pozicija);
	}

	public Pozicija getPozicija() {
		return pozicija;
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
		Punkt other = (Punkt) obj;
		if (!pozicija.equals(other.pozicija))
			return false;
		return true;
	}
}
