package impl;

import java.util.List;
import java.util.Scanner;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class Fission {
	public static final int NUM_RIGHE = 8, NUM_COLONNE = 8, NUM_PEDINE = 12;
	public static final char[] RIGHE = { 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H' };
	public static enum Colore { White, Black }
	public static enum Direzioni { N, E, S, O, NE, SE, SO, NO }
	public static enum TipoMessaggio { Welcome, YourTurn, ValidMove, OpponentMove, End }
	public final Cella[][] scacchiera = new Cella[NUM_RIGHE][NUM_COLONNE];
	public final List<Cella> pedineBianche = new ArrayList<Cella>(NUM_PEDINE);
	public final List<Cella> pedineNere = new ArrayList<Cella>(NUM_PEDINE);
	public Colore colorePedine;
	public List<Cella> pedineAlleate;
	public List<Cella> pedineAvversarie;
	public Socket socket;
	public BufferedWriter invia;
	public BufferedReader ricevi;
	private Scanner sc;

	public Fission(String[] args) throws UnknownHostException, IOException {
		this.connetti(args);
		this.inizializzaScacchiera();
		this.sc = new Scanner(System.in);
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

	public void assegnaColore(Colore colore) {
		this.colorePedine = colore;
		if (colore == Colore.White) {
			pedineAlleate = pedineBianche;
			pedineAvversarie = pedineNere;
		} else {
			pedineAlleate = pedineNere;
			pedineAvversarie = pedineBianche;
		}
	}

	public void connetti(String[] args) throws UnknownHostException, IOException {
		String indirizzoIp = args[0];
		int numeroPorta = Integer.parseInt(args[1]);
		this.socket = new Socket(indirizzoIp, numeroPorta);
		this.invia = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
		this.ricevi = new BufferedReader(new InputStreamReader(socket.getInputStream()));
	}

	// muove la pedina e gestisce eventuali rimozioni di pedine
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
			rimuoviPedinaSingola(posIniziale);
			rimuoviPedineAdiacenti(posCorrente);

			// in questo caso la pedina impatterà il bordo della scacchiera;
			// nessuna pedina verrà rimossa ma la sua posizione dovrà essere aggiornata.
		} else {
			spostaPedina(posIniziale, posCorrente);
		}

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

	// aggiorna i valori posIniziale e dir del parametro mossa
	public void scegliMossa(Mossa mossa) {
		/*
		 * String posIniziale = "A0";
		 * Direzioni dir = Direzioni.NE;
		 * 
		 * // TODO
		 * 
		 * mossa.setPosIniziale(posIniziale);
		 * mossa.setDir(dir);
		 */
		
		System.out.println("scegli mossa");
		String s = sc.nextLine();
		System.out.println("mossa scelta = " + s);
		mossa.setPosIniziale(s.charAt(0) + "" + s.charAt(1));
		mossa.setDir(s.substring(3));
		System.out.println("La mossa scelta è " + mossa.posIniziale + "," + mossa.dir);
	}

	public boolean isCellaLibera(Tupla pos) {
		return !scacchiera[pos.x][pos.y].isGiocatorePresente();
	}

	public List<Cella> getPedineCheSiPossonoMuovere(boolean alleate) {
		return (alleate ? pedineAlleate : pedineAvversarie)
			.stream().filter(c -> !c.getDirezioniPossibili().isEmpty())
			.collect(Collectors.toList());
	}

	public static TipoMessaggio getTipoMessaggio(String messaggio) {
		if (messaggio.startsWith("WELCOME")) return TipoMessaggio.Welcome;
		else if (messaggio.startsWith("YOUR_TURN")) return TipoMessaggio.YourTurn;
		else if (messaggio.startsWith("OPPONENT_MOVE")) return TipoMessaggio.OpponentMove;
		else if (messaggio.startsWith("VALID_MOVE")) return TipoMessaggio.ValidMove;
		return TipoMessaggio.End;
	}

	public void inviaMossa(Mossa mossa) {
		try {
			this.invia.write(mossa.toMessage() + "\n");
			this.invia.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void termina() {
		this.sc.close();
		System.exit(0);
	}

	/************************ utility di test ************************/

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

	public void printStatoPedine() {
		System.out.println("Le pedine alleate sono: " + pedineAlleate.size());
		this.pedineAlleate.stream().forEach(pedina -> System.out.print(pedina.getCoordinate()));
		System.out.println("\n");
		System.out.println("Le pedine avversarie sono: " + pedineAvversarie.size());
		this.pedineAvversarie.stream().forEach(pedina -> System.out.print(pedina.getCoordinate()));
		System.out.println("\n");
	}

	public void printInfo() {
		System.out.println("** INFO *******************");
		System.out.println(this.toString());
		printStatoPedine();
	}

	/**************************** main *********************************/

	public static void main(String[] args) throws NumberFormatException, UnknownHostException, IOException {

		Fission fission = new Fission(args); 

		String messaggio;

		Mossa mossaAlleata = new Mossa(true);

		Mossa mossaAvversaria = new Mossa(false);

		while ((messaggio = fission.ricevi.readLine()) != null) {

			switch (Fission.getTipoMessaggio(messaggio)) {

				case Welcome : 
				
					fission.assegnaColore(Colore.valueOf(messaggio.substring(8))); 
					
					break;

				case YourTurn : 

					fission.scegliMossa(mossaAlleata);

					fission.inviaMossa(mossaAlleata);

					fission.muoviPedina(mossaAlleata);

					mossaAlleata.clear();
					
					break;
				
				case OpponentMove : 

					mossaAvversaria.setInfo(messaggio.substring(14, 16), messaggio.substring(17));

					fission.muoviPedina(mossaAvversaria);

					mossaAvversaria.clear();

					break;

				case ValidMove : 
				
					break;
				
				case End : default :

					fission.termina();

			}

			fission.printInfo();

		}

	}

}
