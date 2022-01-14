package impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import impl.Fission.Colore;
import impl.Fission.Direzioni;

public class Configurazione {

    public static final int NUM_RIGHE = 8, NUM_COLONNE = 8, NUM_PEDINE = 12;

	public static final char[] RIGHE = { 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H' };

    public final Cella[][] scacchiera = new Cella[NUM_RIGHE][NUM_COLONNE]; 

    public final List<Cella> pedineBianche = new ArrayList<Cella>(NUM_PEDINE);
    
	public final List<Cella> pedineNere = new ArrayList<Cella>(NUM_PEDINE);

    public List<Cella> pedineAlleate;
    
	public List<Cella> pedineAvversarie;

    public Colore colorePedine;

	public Mossa mossaMiglioreAlleata;

	public Mossa mossaPrecedente; // la mossa che ha portato parent a diventare this

    public Configurazione() {

        this.inizializzaScacchiera();
        
    }

    public Configurazione(Configurazione parent, Mossa mossa) {

        this.copiaConfigurazione(parent);

		System.out.println("prima\n" + this.toString());

        this.muoviPedina(mossa);

		this.mossaPrecedente = mossa;

		System.out.println("dopo\n" + this.toString());
        
    }

    public void copiaConfigurazione(Configurazione parent) {

		this.colorePedine = (parent.colorePedine == Colore.White) ? Colore.Black : Colore.White;

        for (int i = 0; i < NUM_RIGHE; i++) {

			for (int j = 0; j < NUM_COLONNE; j++) {

				if (parent.scacchiera[i][j] != null) {
					this.scacchiera[i][j] = new Cella(this,i,j);
					this.scacchiera[i][j].pedina = parent.scacchiera[i][j].pedina;
					((parent.scacchiera[i][j].pedina == Colore.White) ? this.pedineBianche : this.pedineNere).add(this.scacchiera[i][j]);
				} else {
					this.scacchiera[i][j] = null;
				}

			}
        }
        // da cambiare
        this.pedineAlleate = this.pedineBianche;
		this.pedineAvversarie = this.pedineNere;

    }

    private void inizializzaScacchiera() {

		for (int i = 0; i < NUM_RIGHE; i++)
			for (int j = 0; j < NUM_COLONNE; j++)
				this.scacchiera[i][j] = new Cella(this, i, j);

		// le coordinate iniziali delle pedine bianche
		int[] posWhite = new int[] {
				1, 3, 2, 2, 3, 3, 4, 2, 3, 1, 3, 5,
				2, 4, 4, 4, 5, 3, 5, 5, 4, 6, 6, 4
		};

		// le coordinate iniziali delle pedine nere
		int[] posBlack = new int[] {
				1, 4, 2, 3, 3, 2, 4, 1, 3, 4, 2, 5,
				4, 3, 5, 2, 3, 6, 4, 5, 5, 4, 6, 3
		};

		// assegna il colore alle pedine iniziali
		IntStream.iterate(0, n -> n + 2).limit(NUM_PEDINE).forEach(pos -> {
			pedineBianche.add(scacchiera[posWhite[pos]][posWhite[pos + 1]]);
			pedineNere.add(scacchiera[posBlack[pos]][posBlack[pos + 1]]);
			scacchiera[posWhite[pos]][posWhite[pos + 1]].pedina = Colore.White;
			scacchiera[posBlack[pos]][posBlack[pos + 1]].pedina = Colore.Black;
		});

	}

    public boolean isCellaLibera(Tupla pos) {
		return !scacchiera[pos.x][pos.y].isGiocatorePresente();
	}

	public List<Cella> getPedineCheSiPossonoMuovere(boolean alleate) {
		return (alleate ? pedineAlleate : pedineAvversarie)
			.stream().filter(c -> !c.getDirezioniPossibili().isEmpty())
			.collect(Collectors.toList());
	}

    public void assegnaColore(String colore) {
		this.colorePedine = Colore.valueOf(colore);
		if (this.colorePedine == Colore.White) {
			pedineAlleate = pedineBianche;
			pedineAvversarie = pedineNere;
		} else {
			pedineAlleate = pedineNere;
			pedineAvversarie = pedineBianche;
		}
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
			bordoRaggiunto = posSuccessiva.x == -1 || posSuccessiva.y == -1 || posSuccessiva.x >= NUM_RIGHE
					|| posSuccessiva.y >= NUM_COLONNE;
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
			System.out.println("impatto con pedina");
			rimuoviPedinaSingola(posIniziale);
			rimuoviPedineAdiacenti(posCorrente);

			// in questo caso la pedina impatterà il bordo della scacchiera;
			// nessuna pedina verrà rimossa ma la sua posizione dovrà essere aggiornata.
		} else {
			System.out.println("sposta con pedina");
			spostaPedina(posIniziale, posCorrente);
		}

		mossa.clear();

	}

	public int muoviPedinaSimulato(Mossa mossa) {

        Function<Tupla, Tupla> operation = muoviPedinaHelper(mossa.dir);

        Tupla posCorrente = mossa.posInizialeToTupla(), posSuccessiva;

        boolean bordoRaggiunto = false, impattoConPedina = false;

        // muove la pedina fino a quando non raggiunge il bordo della scacchiera o un
        // altra pedina
        while (true) {
            posCorrente = operation.apply(posCorrente);
            posSuccessiva = operation.apply(posCorrente);
            bordoRaggiunto = posSuccessiva.x == -1 || posSuccessiva.y == -1 || posSuccessiva.x >= NUM_RIGHE || posSuccessiva.y >= NUM_COLONNE;
            if (bordoRaggiunto)
                break;
            impattoConPedina = scacchiera[posSuccessiva.x][posSuccessiva.y].isGiocatorePresente();
            if (impattoConPedina)
                break;
        }

		return (impattoConPedina) ? numPedineEliminate(posCorrente) - 1 : 0;

    }

    private int numPedineEliminate(Tupla posCorrente) {
        int c = 0;
        for (int h = posCorrente.x - 1; h <= posCorrente.x + 1; h++) {
            for (int k = posCorrente.y - 1; k <= posCorrente.y + 1; k++) {
                if (h >= 0 && h < NUM_RIGHE && k >= 0 && k < NUM_COLONNE) {
                    if (scacchiera[h][k].isAlleata())
                        c--;
                    else if (!scacchiera[h][k].isAlleata())
                        c++;
                }
            }
        }
        return c;
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
		if (x >= 0 && x < NUM_RIGHE && y >= 0 && y < NUM_COLONNE) {
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
				if (h >= 0 && h < NUM_RIGHE && k >= 0 && k < NUM_COLONNE) {
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

    public void printStatoPedine() {
		System.out.println("Le pedine alleate sono: " + pedineAlleate.size());
		this.pedineAlleate.stream().forEach(pedina -> System.out.print(pedina.getCoordinate()));
		System.out.println("\n");
		System.out.println("Le pedine avversarie sono: " + pedineAvversarie.size());
		this.pedineAvversarie.stream().forEach(pedina -> System.out.print(pedina.getCoordinate()));
		System.out.println("\n");
	}

    @Override
	public String toString() {
		StringBuilder sb = new StringBuilder(NUM_COLONNE * NUM_RIGHE * 3);
		sb.append("\n   1  2  3  4  5  6  7  8\n");
		for (int i = 0; i < NUM_RIGHE; i++) {
			sb.append(RIGHE[i]);
			for (int j = 0; j < NUM_COLONNE; j++) {
				sb.append("  " + scacchiera[i][j].print(true));
			}
			sb.append("\n");
		}
		sb.append("\n");
		return sb.toString();
	}

    public List<Mossa> getMossePossibili(boolean isMassimizzatore) {
        return ((isMassimizzatore) ? pedineAlleate : pedineAvversarie)
            .stream()
            .map(pedina -> pedina.getMossePossibili())
            .collect(ArrayList::new, List::addAll, List::addAll);
    }

	public Mossa getMossaMigliore(boolean isMassimizzatore) {

		Comparator<Mossa> comparator = new Comparator<Mossa>() {
			@Override
			public int compare(Mossa m1, Mossa m2) {
				return Integer.compare(m1.index, m2.index);
			}
		};

        return ((isMassimizzatore) ? pedineAlleate : pedineAvversarie)
            .stream()
            .map(pedina -> pedina.getMossePossibili())
			.flatMap(Collection::stream)
			.max(comparator)
			.get();
    }
    
}
