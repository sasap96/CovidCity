package net.etfbl.mapa;

public interface PravilaGrada {

	boolean novaPozicija(Pozicija pozicija, Integer id, int smijer);

	void oslobodiAmbulantnoVozilo();

	void vizita(Pozicija pozicija, Integer id, double temperatura);

	int getDimenzija();

}
