package impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import impl.AlberoDiRicerca.Nodo;
import impl.Fission.Colore;

public class Configurazione {

	public static final int NUM_RIGHE = 8, NUM_COLONNE = 8, NUM_PEDINE = 12;

	public static final char[] RIGHE = { 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H' };

	public final Cella[][] scacchiera = new Cella[NUM_RIGHE][NUM_COLONNE];

	public final List<Cella> pedineBianche = new ArrayList<Cella>(NUM_PEDINE);

	public final List<Cella> pedineNere = new ArrayList<Cella>(NUM_PEDINE);

	public Colore colorePedine;

	public Fission fission;

	public Mossa mossaMiglioreAlleata;
	
	public Mossa mossaPrecedente;

	public Configurazione(Fission fission, Colore colorePedine) {

		this.fission = fission;

		this.inizializzaScacchiera();

	}

	public Configurazione(Configurazione parent, Mossa mossa, Colore colorePedine) {

		this.fission = parent.fission;

		this.copiaConfigurazione(parent, colorePedine);

		this.mossaPrecedente = new Mossa(mossa.posIniziale, mossa.dir, mossa.alleata);

		this.muoviPedina(mossa, colorePedine);

	}

	public void copiaConfigurazione(Configurazione parent, Colore colorePedine) {

		this.pedineBianche.clear(); this.pedineNere.clear();

		this.colorePedine = (parent.colorePedine == Colore.White) ? Colore.Black : Colore.White;

		for (int i = 0; i < NUM_RIGHE; i++) {

			for (int j = 0; j < NUM_COLONNE; j++) {

				if (parent.scacchiera[i][j].pedina != null) {
					this.scacchiera[i][j] = new Cella(this, i, j);
					this.scacchiera[i][j].pedina = parent.scacchiera[i][j].pedina;
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

		int[] posWhite, posBlack;

		int[] first = new int[] {
			1, 3, 2, 2, 3, 3, 4, 2, 3, 1, 3, 5,
			2, 4, 4, 4, 5, 3, 5, 5, 4, 6, 6, 4
		}, second = new int[] {
			1, 4, 2, 3, 3, 2, 4, 1, 3, 4, 2, 5,
			4, 3, 5, 2, 3, 6, 4, 5, 5, 4, 6, 3
		}; 

		if (this.fission.colorePedine.equals(Colore.White)) {
			posWhite = first;
			posBlack = second;
		} else {
			posWhite = second;
			posBlack = first;
		}

		// assegna il colore alle pedine iniziali
		IntStream.iterate(0, n -> n + 2).limit(NUM_PEDINE).forEach(pos -> {
			pedineBianche.add(scacchiera[posWhite[pos]][posWhite[pos + 1]]);
			pedineNere.add(scacchiera[posBlack[pos]][posBlack[pos + 1]]);
			scacchiera[posWhite[pos]][posWhite[pos + 1]].pedina = Colore.White;
			scacchiera[posBlack[pos]][posBlack[pos + 1]].pedina = Colore.Black;
		});

	}

	public boolean isCellaLibera(Tupla pos) {
		if (pos.x < 0 || pos.y < 0 || pos.x >= 8 || pos.y >= 8) return false;
		return !scacchiera[pos.x][pos.y].isGiocatorePresente();
	}

	public void muoviPedina(Mossa mossa, Colore colorePedina) {

		Function<Tupla, Tupla> operation = Utils.muoviPedinaHelper(mossa.dir);

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

	public static Mossa muoviPedinaSimulato(Configurazione conf, Mossa mossa, boolean isMassimizzatore, List<Nodo> figli, boolean noiSiamo, boolean stampa) {

		// if (mossa.index != 0) return mossa;

		// Mossa newMossa = new Mossa(new String(mossa.posIniziale), mossa.dir,
		// mossa.alleata);

		Function<Tupla, Tupla> operation = Utils.muoviPedinaHelper(mossa.dir);

		Tupla posCorrente = mossa.posInizialeToTupla(), posSuccessiva;

		boolean bordoRaggiunto = false, impattoConPedina = false;

		// muove la pedina fino a quando non raggiunge il bordo della scacchiera o un
		// altra pedina
		while (true) {
			posCorrente = operation.apply(posCorrente);
			posSuccessiva = operation.apply(posCorrente);
			bordoRaggiunto = posSuccessiva.x == -1 || posSuccessiva.y == -1 || posSuccessiva.x >= NUM_RIGHE || posSuccessiva.y >= NUM_COLONNE;
			if (bordoRaggiunto) break;
			impattoConPedina = conf.scacchiera[posSuccessiva.x][posSuccessiva.y].isGiocatorePresente();
			if (impattoConPedina) break;
		}

		// contiene la differenza alleatiUccisi - nemiciUccisi
		mossa.index = (impattoConPedina) ? numPedineEliminate(conf, posCorrente, isMassimizzatore, noiSiamo, stampa, mossa) : 0;

		if (figli == null)
			return mossa;

		Nodo figlio = getFiglioByMossa(mossa, figli);

		if (figlio == null)
			return mossa;

		float maxNemico = 0f;

		final Tupla tempo = posCorrente;

		Mossa a = new Mossa();

		for (Mossa m : figlio.conf.getMossePossibili(false).stream().filter(f -> {
			Tupla t = f.posInizialeToTupla();
			if (t.y == tempo.y && t.x == tempo.x) return false;

			if (t.y == tempo.y && t.x == tempo.x - 1) return false;
			if (t.y - 1 == tempo.y && t.x == tempo.x) return false;

			if (t.y - 1 == tempo.y && t.x == tempo.x - 1) return false;
			if (t.y + 1 == tempo.y && t.x == tempo.x + 1) return false;

			if (t.y == tempo.y && t.x == tempo.x + 1) return false;
			if (t.y + 1 == tempo.y && t.x == tempo.x) return false;

			if (t.y - 1 == tempo.y && t.x == tempo.x + 1) return false;
			if (t.y + 1 == tempo.y && t.x == tempo.x - 1) return false;

			return true;
		}).distinct().collect(Collectors.toList())) {
			Mossa temp = new Mossa(m.posIniziale, m.dir, m.alleata);
			float indexNemico = Configurazione.muoviPedinaSimulato(figlio.conf, temp, !isMassimizzatore, null, noiSiamo, false).index;
			if (indexNemico < maxNemico) {
				a = temp;
				maxNemico = indexNemico;
			}
		}

		// if (a.dir != null && mossa.posIniziale.charAt(0) == 'C' &&
		// mossa.posIniziale.charAt(1) == '5' && mossa.dir == Direzioni.N
		// && mossa.index == 2) System.out.println("C5,N" + mossa.index + " " +
		// maxNemico + " | " + a.toMessage() + "- " + a.index);

		// if (a.dir != null && mossa.posIniziale.charAt(0) == 'B' &&
		// mossa.posIniziale.charAt(1) == '4' && mossa.dir == Direzioni.W
		// ) System.out.println("B4,E " + mossa.index + " " + maxNemico + " | " +
		// a.toMessage() + " - " +a.index);

		mossa.index -= Math.abs(a.index);

		return mossa;

	}

	public static float numPedineEliminate(Configurazione conf, Tupla posCorrente, boolean isMassimizzatore,
			boolean noiSiamoMassimizzatori, boolean stampa, Mossa mossa) {
		int alleateUccise = 1, nemicheUccise = 0;

		for (int k = posCorrente.x - 1; k <= posCorrente.x + 1; k++) {
			for (int h = posCorrente.y - 1; h <= posCorrente.y + 1; h++) {
				if (h >= 0 && h < NUM_RIGHE && k >= 0 && k < NUM_COLONNE) {
					if (conf.scacchiera[k][h].isGiocatorePresente()) {
						if (conf.scacchiera[k][h].pedina.equals(Colore.White)) {
							 //if (stampa) System.out.println("x = " + posCorrente.x + ", y = " +
							 //posCorrente.y + " h = " + h + ", k = " + k + ", pedina bianca trovata");
								alleateUccise++;
						} else if (conf.scacchiera[k][h].pedina.equals(Colore.Black)) {
							 //if (stampa) System.out.println("x = " + posCorrente.x + ", y = " +
							 //posCorrente.y + " h = " + h + ", k = " + k + ", pedina nera trovata");+
								nemicheUccise++;
						}
					}
				}
			}
		}

		if (mossa.isAlleata())
			alleateUccise++;
		else
			nemicheUccise++;

		// if (stampa) System.out.println(alleateUccise + " - " + nemicheUccise);

		int differenza = nemicheUccise - alleateUccise;

		//if (mossa.posIniziale.charAt(0) == 'E' && mossa.posIniziale.charAt(1) == '6' && mossa.dir == Direzioni.SW) {
		//	System.out.println(nemicheUccise + " | " + alleateUccise + " | " + conf.pedineBianche.size());
		//}

		if (alleateUccise >= conf.pedineBianche.size()) return -10f;

		/*
		 * if (isMassimizzatore) {
		 * pedineAlleate = conf.pedineBianche;
		 * pedineAvversarie = conf.pedineNere;
		 * } else {
		 * pedineAlleate = conf.pedineNere;
		 * pedineAvversarie = conf.pedineBianche;
		 * }
		 * 
		 * if (differenza == 0) {
		 * 
		 * // in questo caso la nostra unica pedina si sta suicidando
		 * if (pedineAlleate.size() == alleateUccise)
		 * return -Float.MAX_VALUE;
		 * 
		 * // in questo caso stiamo uccidendo le ultime pedine rimaste all'aversario
		 * if (pedineAvversarie.size() == nemicheUccise)
		 * return Float.MAX_VALUE;
		 * 
		 * // in questo modo uccidere 3 pedine e perderne 3 risulta essere una mossa non
		 * // conveniente
		 * return -0.1f;
		 * 
		 * }
		 */
		return differenza;

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

		return union.stream().map(pedina -> pedina.getMossePossibili()).collect(ArrayList::new, List::addAll, List::addAll);

	}

	public List<Mossa> getMossePossibili() {

		List<Cella> union = Stream.of(pedineBianche, pedineNere).flatMap(Collection::stream)
				.collect(Collectors.toList());

		return union.stream().map(pedina -> pedina.getMossePossibili()).collect(ArrayList::new, List::addAll,
				List::addAll);

	}

	public static Mossa getMossaMigliore(Configurazione conf, boolean isMassimizzatore, List<Nodo> figli,
			boolean noiSiamo, boolean stampa) {

		Optional<Mossa> mossaMigliore = null;

		if (isMassimizzatore) {
			mossaMigliore = (conf.pedineBianche)
					.stream()
					.map(pedina -> pedina.getMossePossibili())
					.flatMap(Collection::stream)
					.map(mossa -> mossa = Configurazione.muoviPedinaSimulato(conf, mossa, isMassimizzatore, figli, noiSiamo, stampa))
					.max(new Mossa()::compare);
		} else {
			mossaMigliore = (conf.pedineNere)
					.stream()
					.map(pedina -> pedina.getMossePossibili())
					.flatMap(Collection::stream)
					.map(mossa -> mossa = Configurazione.muoviPedinaSimulato(conf, mossa, !isMassimizzatore, figli, noiSiamo, stampa))
					// .map(mossa -> { mossa.index *= -1f; return mossa; })
					.min(new Mossa()::compare);
		}

		if (mossaMigliore.isPresent())
			return mossaMigliore.get();

		return null;
	}

	public static Nodo getFiglioByMossa(Mossa mossa, List<Nodo> figli) {
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
