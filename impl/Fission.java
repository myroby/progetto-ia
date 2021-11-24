package impl;

import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

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
    public final List<Cella> pedineAlleate = new ArrayList<Cella>(NUM_PEDINE);
    public final List<Cella> pedineAvversarie = new ArrayList<Cella>(NUM_PEDINE);
    public Socket socket;
	public PrintWriter invia;
	public BufferedReader ricevi;

	public Fission() {
		for (int i = 0; i < NUM_RIGHE; i++)
			for (int j = 0; j < NUM_COLONNE; j++)
				this.scacchiera[i][j] = new Cella(this,i,j);
	}

    // assegna le posizioni iniziali alle pedine e inizializza le due 
	// liste: pedineAlleate & pedineAvversarie
    public void assegnaPedine(Colore colore) {

        List<Tupla> posizioniPedineBianche = new ArrayList<Tupla>(NUM_PEDINE);

        List<Tupla> posizioniPedineNere = new ArrayList<Tupla>(NUM_PEDINE);
    
        posizioniPedineBianche.add(new Tupla(1,3)); posizioniPedineBianche.add(new Tupla(3,3));
        posizioniPedineBianche.add(new Tupla(2,2)); posizioniPedineBianche.add(new Tupla(4,2)); 
        posizioniPedineBianche.add(new Tupla(3,1)); posizioniPedineBianche.add(new Tupla(3,5)); 
        posizioniPedineBianche.add(new Tupla(2,4)); posizioniPedineBianche.add(new Tupla(4,4)); 
        posizioniPedineBianche.add(new Tupla(5,3)); posizioniPedineBianche.add(new Tupla(5,5)); 
        posizioniPedineBianche.add(new Tupla(4,6)); posizioniPedineBianche.add(new Tupla(6,4)); 
    
        posizioniPedineNere.add(new Tupla(1,4)); posizioniPedineNere.add(new Tupla(2,3));
        posizioniPedineNere.add(new Tupla(3,2)); posizioniPedineNere.add(new Tupla(4,1)); 
        posizioniPedineNere.add(new Tupla(2,5)); posizioniPedineNere.add(new Tupla(3,4)); 
        posizioniPedineNere.add(new Tupla(4,3)); posizioniPedineNere.add(new Tupla(5,2)); 
        posizioniPedineNere.add(new Tupla(3,6)); posizioniPedineNere.add(new Tupla(4,5)); 
        posizioniPedineNere.add(new Tupla(5,4)); posizioniPedineNere.add(new Tupla(6,3));

        if (colore == Colore.white) {

            posizioniPedineBianche.stream().forEach(pos -> {
                scacchiera[pos.x][pos.y].pedina = Colore.white;
                pedineAlleate.add(scacchiera[pos.x][pos.y]);
            });

            posizioniPedineNere.stream().forEach(pos -> {
                scacchiera[pos.x][pos.y].pedina = Colore.black;
                pedineAvversarie.add(scacchiera[pos.x][pos.y]);
            });

        } else {

            posizioniPedineBianche.stream().forEach(pos -> {
                scacchiera[pos.x][pos.y].pedina = Colore.white;
                pedineAvversarie.add(scacchiera[pos.x][pos.y]);
            });

            posizioniPedineNere.stream().forEach(pos -> {
                scacchiera[pos.x][pos.y].pedina = Colore.black;
                pedineAlleate.add(scacchiera[pos.x][pos.y]);
            });

        }
    
    }

	public void connetti(String indirizzoIp, int numeroPorta) throws UnknownHostException, IOException {
		this.socket = new Socket(indirizzoIp,numeroPorta);
		this.invia = new PrintWriter(socket.getOutputStream(), true);
		this.ricevi = new BufferedReader(new InputStreamReader(socket.getInputStream()));
	}

	// muove la pedina e gestisce eventuali uccisioni di pedine
	public void muoviPedina(Mossa mossa) {

		if (mossa.isAlleata()) {



		} else {



		}

        // TODO

    }

	// aggiorna i valori posIniziale e dir del parametro mossa
    public void scegliMossa(Mossa mossa) {

        String posIniziale = "A0";
		
		Direzioni dir = Direzioni.NE;

        // TODO

		mossa.setPosIniziale(posIniziale); mossa.setDir(dir);

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

    @Override
	public String toString() {
		StringBuilder sb = new StringBuilder(NUM_COLONNE * NUM_RIGHE * 3);
		sb.append("   1  2  3  4  5  6  7  8\n");
		for (int i = 0; i < NUM_RIGHE; i++) {
			sb.append(RIGHE[i]);
			for (int j = 0; j < NUM_COLONNE; j++) {
				sb.append("  " + scacchiera[i][j].print(true));
			}
			sb.append("\n");
		}
		return sb.toString();
	}
	
	//utility di test
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

	public static void main(String[] args) throws NumberFormatException, UnknownHostException, IOException {
		
		Fission fission = new Fission();

		String messaggio; 

		Mossa mossaAlleata = new Mossa(true);

		Mossa mossaAvversaria = new Mossa(false);

		Scanner sc = new Scanner(System.in); 				// fission.connetti(args[0], Integer.parseInt(args[1]));

		while (!(messaggio = sc.nextLine()).equals(".")) { 	// while ((messaggio = fission.leggi.readLine()) != null)

			// assegnamento colore pedine alleate
			if (messaggio.startsWith("WELCOME")) {
	
				fission.assegnaPedine(Colore.valueOf(messaggio.substring(8)));

				System.out.println("Il colore è " + Colore.valueOf(messaggio.substring(8)));
				
			// si sceglie la mossa da inviare al server
			} else if (messaggio.startsWith("YOUR_TURN")) {

				fission.scegliMossa(mossaAlleata);

				fission.invia.print(mossaAlleata.toMessage());

				// meglio aggiornare la scacchiera DOPO l'invio del messaggio per non perdere tempo
				fission.muoviPedina(mossaAlleata);
			
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

		fission.scacchiera[0][7].getDirezioniAdiacenti().stream().forEach(System.out::println);
		
		Fission.printScacchiera(fission, false);
		
	}

}
