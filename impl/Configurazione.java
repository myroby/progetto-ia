package impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import impl.AlberoDiRicerca.Nodo;
import impl.Fission.Colore;
import impl.Fission.Direzioni;

public class Configurazione {

	public static final int NUM_RIGHE = 8, NUM_COLONNE = 8, NUM_PEDINE = 12;

	public static final char[] RIGHE = { 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H' };

	public final Cella[][] scacchiera = new Cella[NUM_RIGHE][NUM_COLONNE];

	public final List<Cella> pedineBianche = new ArrayList<Cella>(NUM_PEDINE);

	public final List<Cella> pedineNere = new ArrayList<Cella>(NUM_PEDINE);

	public Colore colorePedine;

	public Mossa mossaMiglioreAlleata;

	public Mossa mossaPrecedente;

	public Configurazione(Colore colorePedine) {

		this.inizializzaScacchiera();

	}

	public Configurazione(Configurazione parent, Mossa mossa, Colore colorePedine) {

		this.copiaConfigurazione(parent, colorePedine);

		this.mossaPrecedente = new Mossa(mossa.posIniziale, mossa.dir, mossa.alleata);

		this.muoviPedina(mossa, colorePedine);

	}

	public void copiaConfigurazione(Configurazione parent, Colore colorePedine) {

		this.pedineBianche.clear();
		this.pedineNere.clear();

		this.colorePedine = (parent.colorePedine == Colore.White) ? Colore.Black : Colore.White;

		for (int i = 0; i < NUM_RIGHE; i++) {

			for (int j = 0; j < NUM_COLONNE; j++) {

				if (parent.scacchiera[i][j].pedina != null) {
					this.scacchiera[i][j] = new Cella(this, i, j);
					this.scacchiera[i][j].pedina = (parent.scacchiera[i][j].pedina == Colore.White) ? Colore.White : Colore.Black;
					((parent.scacchiera[i][j].pedina == Colore.White) ? this.pedineBianche : this.pedineNere).add(this.scacchiera[i][j]);
				} else {
					this.scacchiera[i][j] = new Cella(this, i, j);
					this.scacchiera[i][j].pedina = null;
				}

			}
		}

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
		if (pos.x < 0 || pos.y < 0)
			return false;
		if (pos.x >= 8 || pos.y >= 8)
			return false;
		return !scacchiera[pos.x][pos.y].isGiocatorePresente();
	}

	public void muoviPedina(Mossa mossa, Colore colorePedina) {

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
			// System.out.println("impatto con pedina");
			rimuoviPedinaSingola(posIniziale);
			rimuoviPedineAdiacenti(posCorrente);

			// in questo caso la pedina impatterà il bordo della scacchiera;
			// nessuna pedina verrà rimossa ma la sua posizione dovrà essere aggiornata.
		} else {
			// System.out.println("sposta con pedina");
			spostaPedina(posIniziale, posCorrente, colorePedina);
		}

	}

	public static Mossa muoviPedinaSimulato(Configurazione conf, Mossa mossa, boolean isMassimizzatore, List<Nodo> figli, boolean noiSiamo) {

		//if (mossa.index != 0) return mossa;

		//Mossa newMossa = new Mossa(new String(mossa.posIniziale), mossa.dir, mossa.alleata);

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
			impattoConPedina = conf.scacchiera[posSuccessiva.x][posSuccessiva.y].isGiocatorePresente();
			if (impattoConPedina)
				break;
		}

		// contiene la differenza alleatiUccisi - nemiciUccisi
		mossa.index = (impattoConPedina) ? numPedineEliminate(conf, posCorrente, isMassimizzatore, noiSiamo) : 0;

		if (figli == null)
			return mossa;

		Nodo figlio = getFiglioByMossa(mossa, figli);

		if (figlio == null)
			return mossa;

		float maxNemico = 0f;

		List<Mossa> mosseNemiche = figlio.conf.getMossePossibili().stream().filter(f -> {
            int[] pos = Mossa.posInizialeToInt(f);
            return !conf.scacchiera[pos[0]][pos[1]].isAlleata();
        }).collect(Collectors.toList());

		Mossa a = new Mossa();

		for (Mossa m : mosseNemiche) {
			Mossa temp = new Mossa(m.posIniziale,m.dir,m.alleata);
			float indexNemico = Configurazione.muoviPedinaSimulato(figlio.conf, temp, !isMassimizzatore, null, noiSiamo).index;
			if (indexNemico < -maxNemico) {
				a = temp;
				maxNemico = indexNemico;
			}
		}

		//if (a.dir != null && mossa.posIniziale.charAt(0) == 'C' && mossa.posIniziale.charAt(1) == '5' && mossa.dir == Direzioni.N
			//&& mossa.index == 2) System.out.println("C5,N" + mossa.index + " " + maxNemico + " | " + a.toMessage() + "- " + a.index);

		//if (a.dir == null && mossa.posIniziale.charAt(0) == 'B' && mossa.posIniziale.charAt(1) == '4' && mossa.dir == Direzioni.E
			//) System.out.println(conf.toString());//"B4,E" + mossa.index + " " + maxNemico + " | " + a.toMessage() + " - " +a.index);
		
		/*if (mossa.index < maxNemico) {
			mossa.index -= maxNemico;
			return mossa;
		};*/


		return mossa;

	}

	private static float numPedineEliminate(Configurazione conf, Tupla posCorrente, boolean isMassimizzatore, boolean noiSiamoMassimizzatori) {
		int alleateUccise = 0, nemicheUccise = 0;
		for (int h = posCorrente.x - 1; h <= posCorrente.x + 1; h++) {
			for (int k = posCorrente.y - 1; k <= posCorrente.y + 1; k++) {
				if (h >= 0 && h < NUM_RIGHE && k >= 0 && k < NUM_COLONNE) {
					if (conf.scacchiera[h][k].isGiocatorePresente()) {
						if (conf.scacchiera[h][k].pedina == Colore.White) {
							if (isMassimizzatore) alleateUccise++;
							else nemicheUccise++;
						} else if (conf.scacchiera[h][k].pedina == Colore.Black) {
							if (isMassimizzatore) nemicheUccise++;
							else alleateUccise++;
						}
					}
				}
			}
		}

		// se il numero delle pedine nemiche uccise è pari a quelle alleate non deve
		// essere 0
		// perchè se a noi rimane una pedina allora perdiamo il gioco

		List<Cella> pedineAlleate, pedineAvversarie;

		int differenza = nemicheUccise - alleateUccise;
/*
		if (isMassimizzatore) {
			pedineAlleate = conf.pedineBianche;
			pedineAvversarie = conf.pedineNere;
		} else {
			pedineAlleate = conf.pedineNere;
			pedineAvversarie = conf.pedineBianche;
		}

		if (differenza == 0) {

			// in questo caso la nostra unica pedina si sta suicidando
			if (pedineAlleate.size() == alleateUccise)
				return -Float.MAX_VALUE;

			// in questo caso stiamo uccidendo le ultime pedine rimaste all'aversario
			if (pedineAvversarie.size() == nemicheUccise)
				return Float.MAX_VALUE;

			// in questo modo uccidere 3 pedine e perderne 3 risulta essere una mossa non
			// conveniente
			return -0.1f;

		}
*/
		return differenza;

	}

	// ritorna una funzione che incrementa una o più coordinate; es:
	// se la direzione è NE allora ritorna una funzione f. applicando f su una
	// tupla ritorna un'altra tupla con le coordinate aggiornate:
	// f(5,8) -> (4,9)
	private static Function<Tupla, Tupla> muoviPedinaHelper(Direzioni dir) {
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
			case W:
				return func = in -> {
					return new Tupla(in.x, in.y - 1);
				};
			case NE:
				return func = in -> {
					return new Tupla(in.x - 1, in.y + 1);
				};
			case NW:
				return func = in -> {
					return new Tupla(in.x - 1, in.y - 1);
				};
			case SE:
				return func = in -> {
					return new Tupla(in.x + 1, in.y + 1);
				};
			case SW:
				return func = in -> {
					return new Tupla(in.x + 1, in.y - 1);
				};
		}
		return func;
	}

	// rimuove la pedina in posizione (x,y) dalla scacchiera e dalla relativa lista
	private void rimuoviPedinaSingola(int x, int y) {
		if (x >= 0 && x < NUM_RIGHE && y >= 0 && y < NUM_COLONNE) {
			((scacchiera[x][y].pedina == Colore.White) ? pedineBianche : pedineNere).remove(scacchiera[x][y]);
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
	private void spostaPedina(Tupla start, Tupla end, Colore colorePedina) {
		List<Cella> temp = (scacchiera[start.x][start.y].pedina == Colore.White) ? pedineBianche : pedineNere;
		temp.remove(scacchiera[start.x][start.y]);
		temp.add(scacchiera[end.x][end.y]);
		scacchiera[end.x][end.y].pedina = scacchiera[start.x][start.y].pedina;
		scacchiera[start.x][start.y].removePedina();
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

	public static String print(Cella[][] scacchiera) {
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

		List<Cella> union = (isMassimizzatore) ? pedineBianche : pedineNere;

		return union.stream().map(pedina -> pedina.getMossePossibili()).collect(ArrayList::new, List::addAll,
				List::addAll);

	}

	public List<Mossa> getMossePossibili() {

		List<Cella> union = Stream.of(pedineBianche, pedineNere).flatMap(Collection::stream)
				.collect(Collectors.toList());

		return union.stream().map(pedina -> pedina.getMossePossibili()).collect(ArrayList::new, List::addAll,
				List::addAll);

	}

	public static Mossa getMossaMigliore(Configurazione conf, boolean isMassimizzatore, List<Nodo> figli, boolean noiSiamo) {

		Optional<Mossa> mossaMigliore = null;

		if (isMassimizzatore) {
			mossaMigliore = ((isMassimizzatore) ? conf.pedineBianche : conf.pedineNere)
				.stream()
				.map(pedina -> pedina.getMossePossibili())
				.flatMap(Collection::stream)
				.map(mossa -> mossa = Configurazione.muoviPedinaSimulato(conf, mossa, isMassimizzatore, null, noiSiamo))
				.max(new Mossa()::compare);
		} else {
			mossaMigliore = ((isMassimizzatore) ? conf.pedineBianche : conf.pedineNere)
				.stream()
				.map(pedina -> pedina.getMossePossibili())
				.flatMap(Collection::stream)
				.map(mossa -> mossa = Configurazione.muoviPedinaSimulato(conf, mossa, isMassimizzatore, null, noiSiamo))
				.min(new Mossa()::compare);
		}

		if (mossaMigliore.isPresent())
			return mossaMigliore.get();

		return null;
	}
	/*
	 * private static boolean isMossaDannosa(Mossa mossa, List<Nodo> figli, boolean
	 * isMassimizzatore) {
	 * 
	 * if (figli == null) return false;
	 * 
	 * Nodo figlio = getFiglioByMossa(mossa, figli);
	 * 
	 * if (figlio == null) return false;
	 * 
	 * for (Mossa m : figlio.conf.getMossePossibili()) {
	 * if (Configurazione.muoviPedinaSimulato(figlio.conf, m,
	 * !isMassimizzatore).index > 0) return true;
	 * }
	 * 
	 * return false;
	 * 
	 * }
	 */

	public static Nodo getFiglioByMossa(Mossa mossa, List<Nodo> figli) {
		// figli.stream().forEach(f -> System.out.println(f.conf.toString()));
		Nodo figlio = null;
		for (Nodo f : figli) {
			if (f.conf.mossaPrecedente.equals(mossa)) {
				figlio = f;
				break;
			}
		}
		return figlio;
	}

}
