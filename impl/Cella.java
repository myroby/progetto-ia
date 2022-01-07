package impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import impl.Fission.Colore;
import impl.Fission.Direzioni;

public class Cella {
	public final Configurazione configurazione;
	public final int riga;
	public final int colonna;
	public Colore pedina;

	public Cella(Configurazione configurazione, int riga, int colonna) {
		super();
		this.configurazione = configurazione;
		this.riga = riga;
		this.colonna = colonna;
	}
	
	public boolean isGiocatorePresente() {
		return pedina != null;
	}
	
	public void removePedina() {
		this.pedina = null;
	}

	public List<Mossa> getMossePossibili() {
		return getDirezioniPossibili().stream().map(dir -> 
			new Mossa(riga, colonna + 1, dir, configurazione.colorePedine == pedina)
		).collect(Collectors.toList());
	}

	// da invocare solo se nella cella è presente una pedina. ritorna le
	// direzioni possibili che la pedina può intraprendere
	public List<Direzioni> getDirezioniPossibili() {
		return getDirezioniAdiacenti().stream()
			.filter(direzione -> configurazione.isCellaLibera(valutaDirezione(direzione)))
			.collect(Collectors.toList());
	}

	// ritorna le direzioni adiacenti della cella.
	public List<Direzioni> getDirezioniAdiacenti() {
		
		// ret contiene tutte le direzioni
		List<Direzioni> ret = new ArrayList<Direzioni>(Arrays.asList(Direzioni.values()));

		// rimuovo da ret le direzioni non consentite
		if (riga == 0) {
			ret.remove(Direzioni.N);
			ret.remove(Direzioni.NE);
			ret.remove(Direzioni.NO);
		} else if (riga == Configurazione.NUM_RIGHE - 1) {
			ret.remove(Direzioni.S);
			ret.remove(Direzioni.SE);
			ret.remove(Direzioni.SO);
		}

		if (colonna == 0) {
			ret.remove(Direzioni.O);
			ret.remove(Direzioni.NO);
			ret.remove(Direzioni.SO);
		} else if (colonna == Configurazione.NUM_COLONNE - 1) {
			ret.remove(Direzioni.E);
			ret.remove(Direzioni.NE);
			ret.remove(Direzioni.SE);
		}

		return ret;
	}

	private Tupla valutaDirezione(Direzioni direzione) {
        switch (direzione) {
            case N	: return new Tupla(riga - 1, colonna);
            case S	: return new Tupla(riga + 1, colonna);
            case E	: return new Tupla(riga, colonna - 1);
            case O	: return new Tupla(riga, colonna + 1);
            case NE	: return new Tupla(riga - 1, colonna + 1);
            case NO : return new Tupla(riga - 1, colonna - 1);
            case SE	: return new Tupla(riga + 1, colonna + 1);
            case SO	: return new Tupla(riga + 1, colonna - 1);
            default	: return null;
        }
    }

	public boolean isAlleata() {
		return this.pedina == configurazione.colorePedine;
	}

	public String print(boolean onlyColor) {
		if (onlyColor) {
			if (pedina == null) return "-";
			return "" + pedina.toString().charAt(0);
		} else {
			if (pedina == null) return "" + Configurazione.RIGHE[riga] + colonna;
			return "" + Configurazione.RIGHE[riga] + colonna + "(" + pedina.toString().charAt(0) + "(";
		}
	}

	public String getCoordinate() {
		return "(" + Configurazione.RIGHE[riga] + "," + (colonna + 1) + ")";
	}

}
