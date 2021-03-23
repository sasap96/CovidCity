package net.etfbl.mapa;

import java.io.Serializable;
import java.util.stream.Stream;

public class Pozicija implements Serializable {

	private int x;
	private int y;

	public Pozicija(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public Pozicija(Pozicija position) {
		this(position.getX(), position.getY());
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public void setX(int x) {
		this.x = x;
	}

	public void setY(int y) {
		this.y = y;

	}

	public int distance(Pozicija pos) {
		int x = this.x - pos.x;
		int y = this.y - pos.y;
		return (int) Math.max(Math.abs(x), Math.abs(y));
	}

	public static Pozicija calculateRefPoint(Pozicija pos, int dim, int max) {

		Pozicija referencePoint = new Pozicija(pos);
		int d;
		if ((d = pos.getX() - max) < 0)
			referencePoint.setX(pos.getX() - d);
		if ((pos.getY() - max) < 0)
			referencePoint.setY(pos.getX() - d);
		if ((d = pos.getX() + max) >= dim)
			referencePoint.setX(dim - max - 1);
		if ((d = pos.getY() + max) >= dim)
			referencePoint.setY(dim - max - 1);

		return referencePoint;

	}

	@Override
	public String toString() {
		return " (" + getX() + "," + getY() + ") ";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + x;
		result = prime * result + y;
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
		Pozicija other = (Pozicija) obj;
		if (x != other.x)
			return false;
		if (y != other.y)
			return false;
		return true;
	}

	static public Pozicija closestPosition(Pozicija current, Pozicija toPosition, int dim) {

		int position = Stream.iterate(0, c -> c + 1).limit(dim * dim)
				.filter(e -> current.distance(new Pozicija(e / dim, e % dim)) <= 1)
				.min((e1, e2) -> Integer.compare(toPosition.distance(new Pozicija(e1 / dim, e1 % dim)),
						toPosition.distance(new Pozicija(e2 / dim, e2 % dim))))
				.get();

		return new Pozicija(position / dim, position % dim);
	}

}
