package impl;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import impl.Fission.Direzioni;

public class Configurazione {

    public enum Tipo { Alleata, Avversaria }

    public final Cella[][] scacchiera = new Cella[Fission.NUM_RIGHE][Fission.NUM_COLONNE]; 

    public List<Cella> pedineAlleate;
    
	public List<Cella> pedineAvversarie;

    public Tipo tipo;

    public Configurazione(Configurazione parent, Mossa mossa) {

        for (int i = 0; i < scacchiera.length; i++) 
            this.scacchiera[i] = Arrays.copyOf(parent.scacchiera[i], parent.scacchiera[i].length);
        
        this.tipo = (parent.tipo == Tipo.Alleata) ? Tipo.Avversaria : Tipo.Alleata;

        this.pedineAlleate = List.copyOf(parent.pedineAlleate);

        this.pedineAvversarie = List.copyOf(parent.pedineAvversarie);

        this.muoviPedina(mossa);
        
    }

    public void muoviPedina(Mossa mossa) {

        Function<Tupla, Tupla> operation = muoviPedinaHelper(mossa.dir);

		Tupla posCorrente = mossa.posInizialeToTupla(), posSuccessiva;

		boolean bordoRaggiunto = false, impattoConPedina = false;

		// muove la pedina fino a quando non raggiunge il bordo della scacchiera o un
		// altra pedina
		while (true) {
			posCorrente = operation.apply(posCorrente);
			posSuccessiva = operation.apply(posCorrente);
			bordoRaggiunto = posSuccessiva.x == -1 || posSuccessiva.y == -1 || posSuccessiva.x >= Fission.NUM_RIGHE
					|| posSuccessiva.y >= Fission.NUM_COLONNE;
			if (bordoRaggiunto)
				break;
			impattoConPedina = scacchiera[posSuccessiva.x][posSuccessiva.y].isGiocatorePresente();
			if (impattoConPedina)
				break;
		}

		Tupla posIniziale = mossa.posInizialeToTupla();

		// se la pedina impatterà un'altra pedina andranno rimosse sia la pedina da
		// spostare sia le pedine adiacenti
		if (impattoConPedina) {
			rimuoviPedinaSingola(posIniziale);
			rimuoviPedineAdiacenti(posCorrente);

			// in questo caso la pedina impatterà il bordo della scacchiera;
			// nessuna pedina verrà rimossa ma la sua posizione dovrà essere aggiornata.
		} else {
			spostaPedina(posIniziale, posCorrente);
		}

	}

    // ritorna una funzione che incrementa una o più coordinate; es:
	// se la direzione è NE allora ritorna una funzione f. applicando f su una
	// tupla ritorna un'altra tupla con le coordinate aggiornate:
	// f(5,8) -> (4,9)
	private Function<Tupla, Tupla> muoviPedinaHelper(Direzioni dir) {
		Function<Tupla, Tupla> func = null;
		switch (dir) {
			case N:
				return func = in -> {
					return new Tupla(in.x - 1, in.y);
				};
			case S:
				return func = in -> {
					return new Tupla(in.x + 1, in.y);
				};
			case E:
				return func = in -> {
					return new Tupla(in.x, in.y + 1);
				};
			case O:
				return func = in -> {
					return new Tupla(in.x, in.y - 1);
				};
			case NE:
				return func = in -> {
					return new Tupla(in.x - 1, in.y + 1);
				};
			case NO:
				return func = in -> {
					return new Tupla(in.x - 1, in.y - 1);
				};
			case SE:
				return func = in -> {
					return new Tupla(in.x + 1, in.y + 1);
				};
			case SO:
				return func = in -> {
					return new Tupla(in.x + 1, in.y - 1);
				};
		}
		return func;
	}

    // rimuove la pedina in posizione (x,y) dalla scacchiera e dalla relativa lista
	private void rimuoviPedinaSingola(int x, int y) {
		if (x >= 0 && x < Fission.NUM_RIGHE && y >= 0 && y < Fission.NUM_COLONNE) {
			((scacchiera[x][y].isAlleata()) ? pedineAlleate : pedineAvversarie).remove(scacchiera[x][y]);
			scacchiera[x][y].removePedina();
		}
	}

	private void rimuoviPedinaSingola(Tupla tupla) {
		rimuoviPedinaSingola(tupla.x, tupla.y);
	}

    // rimuove le pedine adiacenti dalla scacchiera servendosi di
	// rimuoviPedinaSingola()
	private void rimuoviPedineAdiacenti(Tupla posCorrente) {
		for (int h = posCorrente.x - 1; h <= posCorrente.x + 1; h++) {
			for (int k = posCorrente.y - 1; k <= posCorrente.y + 1; k++) {
				if (h >= 0 && h < Fission.NUM_RIGHE && k >= 0 && k < Fission.NUM_COLONNE) {
					rimuoviPedinaSingola(h, k);
				}
			}
		}
	}

	// sposta una pedina senza rimuovere alcuna pedina
	private void spostaPedina(Tupla start, Tupla end) {
		Cella pedinaDaSpostare = scacchiera[start.x][start.y];
		List<Cella> temp = (pedinaDaSpostare.isAlleata()) ? pedineAlleate : pedineAvversarie;
		temp.remove(scacchiera[start.x][start.y]);
		temp.add(scacchiera[end.x][end.y]);
		scacchiera[end.x][end.y].pedina = scacchiera[start.x][start.y].pedina;
		scacchiera[start.x][start.y].pedina = null;
	}
    
}
