package net.etfbl.stanovnici;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javafx.scene.paint.Color;
import net.etfbl.mapa.Pozicija;


public class Kuca implements Serializable {

	private List<Integer> family;
	private final Pozicija position;
	private final Integer brojKuce;

	double r, g, b;

	public Kuca(Pozicija position, double r, double g, double b) {

		this.position = new Pozicija(position);
		this.family = new ArrayList<Integer>();
		this.r = r;
		this.g = g;
		this.b = b;
		this.brojKuce = hashCode();
	}

	public void addMember(int id) {
		family.add(id);
	}

	public int getBrojKuce() {
		return brojKuce;
	}

	public Pozicija getPosition() {
		return new Pozicija(position);
	}

	public List<Integer> getMembers() {
		return Collections.unmodifiableList(family);
	}

	public Color getColor() {
		return Color.color(r, g, b);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + position.hashCode();
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
		Kuca other = (Kuca) obj;
		if (!position.equals(other.position))
			return false;
		return true;
	}

}
