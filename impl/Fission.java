package impl;

import java.util.List;
import java.util.Scanner;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class Fission {
	public static final int NUM_RIGHE = 8, NUM_COLONNE = 8, NUM_PEDINE = 12;
	public static final char[] RIGHE = { 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H' };
	public static enum Colore { white, black } 
    public static enum Direzioni { N, E, S, O, NE, SE, SO, NO }
	public final Cella[][] scacchiera = new Cella[NUM_RIGHE][NUM_COLONNE];
	public final List<Cella> pedineBianche = new ArrayList<Cella>(NUM_PEDINE);
    public final List<Cella> pedineNere = new ArrayList<Cella>(NUM_PEDINE);
	public Colore colorePedine;
    public List<Cella> pedineAlleate;
    public List<Cella> pedineAvversarie;
    public Socket socket;
	public PrintWriter invia;
	public BufferedReader ricevi;

	public Fission() {

		for (int i = 0; i < NUM_RIGHE; i++)
			for (int j = 0; j < NUM_COLONNE; j++)
				this.scacchiera[i][j] = new Cella(this,i,j);
		
		// le coordinate iniziali delle pedine bianche
		int[] posWhite = new int[] {
			1, 3,	2, 2,	3, 3,	4, 2,	3, 1,	3, 5,	
			2, 4,	4, 4,	5, 3,	5, 5,	4, 6,	6, 4
		};

		// le coordinate iniziali delle pedine nere
		int[] posBlack = new int[] {
			1, 4,	2, 3,	3, 2,	4, 1,	3, 4,	2, 5,	
			4, 3,	5, 2,	3, 6,	4, 5,	5, 4,	6, 3
		};

		// assegna il colore alle pedine iniziali
		IntStream.iterate(0, n -> n + 2).limit(NUM_PEDINE).forEach(pos -> {
			pedineBianche.add(scacchiera[posWhite[pos]][posWhite[pos + 1]]);
			pedineNere.add(scacchiera[posBlack[pos]][posBlack[pos + 1]]);
			scacchiera[posWhite[pos]][posWhite[pos + 1]].pedina = Colore.white;
			scacchiera[posBlack[pos]][posBlack[pos + 1]].pedina = Colore.black;
		});

	}

    public void assegnaColore(Colore colore) {
		this.colorePedine = colore;
        if (colore == Colore.white) {
			pedineAlleate = pedineBianche;
			pedineAvversarie = pedineNere;
        } else {
			pedineAlleate = pedineNere;
			pedineAvversarie = pedineBianche;
        }
    }

	public void connetti(String indirizzoIp, int numeroPorta) throws UnknownHostException, IOException {
		this.socket = new Socket(indirizzoIp,numeroPorta);
		this.invia = new PrintWriter(socket.getOutputStream(), true);
		this.ricevi = new BufferedReader(new InputStreamReader(socket.getInputStream()));
	}

	// muove la pedina e gestisce eventuali rimozioni di pedine
	public void muoviPedina(Mossa mossa) {

		Function<Tupla,Tupla> operation = muoviPedinaHelper(mossa.dir);

		Tupla posCorrente = mossa.posInizialeToTupla(), posSuccessiva;

		boolean bordoRaggiunto = false, impattoConPedina = false;

		// muove la pedina fino a quando non raggiunge il bordo della scacchiera o un altra pedina
		while (true) {
			posCorrente = operation.apply(posCorrente);
			posSuccessiva = operation.apply(posCorrente);
			bordoRaggiunto = posSuccessiva.x == -1 || posSuccessiva.y == -1 || posSuccessiva.x >= NUM_RIGHE || posSuccessiva.y >= NUM_COLONNE;
			if (bordoRaggiunto) break;
			impattoConPedina = scacchiera[posSuccessiva.x][posSuccessiva.y].isGiocatorePresente();
			if (impattoConPedina) break;
		}

		Tupla posIniziale = mossa.posInizialeToTupla();

		// se la pedina impatterà un'altra pedina andranno rimosse sia la pedina da spostare
		// sia le pedine adiacenti
		if (impattoConPedina) {

			rimuoviPedinaSingola(posIniziale);

			rimuoviPedineAdiacenti(posCorrente);

		// in questo caso la pedina impatterà il bordo della scacchiera;
		// nessuna pedina verrà rimossa ma la sua posizione dovrà essere aggiornata.
		} else {

			spostaPedina(posIniziale, posCorrente);

		}
    }

	// rimuove le pedine adiacenti dalla scacchiera servendosi di rimuoviPedinaSingola()
	private void rimuoviPedineAdiacenti(Tupla posCorrente) {
		for (int h = posCorrente.x-1; h <= posCorrente.x+1; h++) {
			for (int k = posCorrente.y-1; k <= posCorrente.y+1; k++) {
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

	// rimuove una singola pedina dalla scacchiera e dalla relativa lista
	private void rimuoviPedinaSingola(int x, int y) {
		if (x >= 0 && x < NUM_RIGHE && y >= 0 && y < NUM_COLONNE) {
			Cella pedinaDaRimuovere = scacchiera[x][y];
			List<Cella> temp = (pedinaDaRimuovere.isAlleata()) ? pedineAlleate : pedineAvversarie;
			temp.remove(scacchiera[x][y]);
			scacchiera[x][y].removePedina();
		}
	}

	private void rimuoviPedinaSingola(Tupla tupla) {
		rimuoviPedinaSingola(tupla.x,tupla.y);
	}

	// ritorna una funzione che incrementa una o più coordinate; es:
	// se la direzione è NE allora ritorna una funzione f. applicando f su una 
	// tupla ritorna un'altra tupla con le coordinate aggiornate:
	// f(5,8) -> (4,9)
	private Function<Tupla,Tupla> muoviPedinaHelper(Direzioni dir) {
		Function<Tupla, Tupla> func = null;
		switch (dir) {
            case N	: return func = in -> { return new Tupla(in.x-1,in.y); };
            case S	: return func = in -> { return new Tupla(in.x+1,in.y); };
            case E	: return func = in -> { return new Tupla(in.x,in.y+1); };
            case O	: return func = in -> { return new Tupla(in.x,in.y-1); };
            case NE	: return func = in -> { return new Tupla(in.x-1,in.y+1); };
            case NO : return func = in -> { return new Tupla(in.x-1,in.y-1); };
            case SE	: return func = in -> { return new Tupla(in.x+1,in.y+1); };
            case SO	: return func = in -> { return new Tupla(in.x+1,in.y-1); };
        }
		return func;
	}

	// aggiorna i valori posIniziale e dir del parametro mossa
    public void scegliMossa(Mossa mossa) {
		Fission.printScacchiera(this, false);
		/*
        String posIniziale = "A0";
		Direzioni dir = Direzioni.NE;

        // TODO

		mossa.setPosIniziale(posIniziale); 
		mossa.setDir(dir);*/
    }

    public boolean isCellaLibera(Tupla pos) {
        return !scacchiera[pos.x][pos.y].isGiocatorePresente();
    }
	
    public List<Cella> getPedineAlleateCheSipossonoMuovere() {
        return pedineAlleate.stream()
            .filter(c -> !c.getDirezioniPossibili().isEmpty())
            .collect(Collectors.toList());
    }

    public List<Cella> getPedineAvversarieCheSipossonoMuovere() {
        return pedineAvversarie.stream()
            .filter(c -> !c.getDirezioniPossibili().isEmpty())
            .collect(Collectors.toList());
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

	public static void printScacchiera(Fission s, boolean withColors) {
		if (withColors) {
			char[] st = s.toString().toCharArray();
			for (int i = 0; i < st.length; i++) {
				if (st[i] == 'W') System.err.print(st[i]);
				else System.out.print(st[i]);
			}
			System.out.println();
		} else {
			System.out.println(s.toString());
		}
	}

	public void printStatoPedine() {
		System.out.println("Le pedine alleate sono: " + pedineAlleate.size());
		this.pedineAlleate.stream().forEach(pedina -> System.out.print(pedina.getCoordinate()));
		System.out.println("\n");
		System.out.println("Le pedine avversarie sono: " + pedineAvversarie.size());
		this.pedineAvversarie.stream().forEach(pedina -> System.out.print(pedina.getCoordinate()));
		System.out.println();
	}

	/*****************************************************************/

	public static void main(String[] args) throws NumberFormatException, UnknownHostException, IOException {
		
		Fission fission = new Fission();

		String messaggio; 

		Mossa mossaAlleata = new Mossa(true);

		Mossa mossaAvversaria = new Mossa(false);

		Scanner sc = new Scanner(System.in); 				// fission.connetti(args[0], Integer.parseInt(args[1]));

		while (!(messaggio = sc.nextLine()).equals(".")) { 	// while ((messaggio = fission.leggi.readLine()) != null)

			// assegnamento colore pedine alleate
			if (messaggio.startsWith("WELCOME")) {
	
				fission.assegnaColore(Colore.valueOf(messaggio.substring(8)));

				Fission.printScacchiera(fission, false);
				
			// si sceglie la mossa da inviare al server
			} else if (messaggio.startsWith("YOUR_TURN")) {

				//fission.scegliMossa(mossaAlleata);

				//fission.invia.print(mossaAlleata.toMessage());

				// meglio aggiornare la scacchiera DOPO l'invio del messaggio per non perdere tempo
				//fission.muoviPedina(mossaAlleata);

				mossaAlleata.setPosIniziale(messaggio.substring(10,12));

				mossaAlleata.setDir(messaggio.substring(13));

				fission.muoviPedina(mossaAlleata);

				Fission.printScacchiera(fission, false);

				fission.printStatoPedine();

				System.out.println();
			
			// si riceve la mossa dell'avversario; bisogna aggiornare la scacchiera
			} else if (messaggio.startsWith("OPPONENT_MOVE")) {

				mossaAvversaria.setPosIniziale(messaggio.substring(14,16));

				mossaAvversaria.setDir(messaggio.substring(17));

				fission.muoviPedina(mossaAvversaria);

			// la mossa è valida. che si fa?
			} else if (messaggio.startsWith("VALID_MOVE")) {

				System.out.println("Mossa Valida");
				
			// forse forse abbiamo vinto ma sicuramente abbiamo perso. 
			// il programma viene chiuso
			} else if (messaggio.startsWith("DEFEAT") || messaggio.startsWith("ILLEGAL_MOVE") || 
						messaggio.startsWith("VICTORY") || messaggio.startsWith("TIMEOUT") || 
						messaggio.startsWith("TIE")) {

				System.exit(0);

			// è arrivato un messaggio generico
			} else {

				System.out.println(messaggio);

			}

		}

		sc.close();
		
		Fission.printScacchiera(fission, false);
		
	}

}
