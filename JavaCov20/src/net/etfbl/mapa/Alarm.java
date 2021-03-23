package net.etfbl.mapa;

import java.io.Serializable;

public class Alarm implements Serializable {

	final private Pozicija pozicija;
	final private Integer id;
	final private Integer idKuce;

	public Alarm(Pozicija pozicija, int id, int idKuce) {
		this.pozicija = new Pozicija(pozicija);
		this.id = id;
		this.idKuce = idKuce;

	}

	public Pozicija getPozicija() {
		return new Pozicija(pozicija);
	}

	public int getId() {
		return id;
	}

	public int getIdKuce() {
		return idKuce;
	}
}
