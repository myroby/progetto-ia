package impl;

import java.util.Scanner;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class Fission {
	public static enum Colore { White, Black }
	public static enum Direzioni { N, E, S, O, NE, SE, SO, NO }
	public static enum TipoMessaggio { Welcome, YourTurn, Continue, OpponentMove, End  }
	public Configurazione configurazioneCorrente;
	public AlberoDiRicerca albero;
	public Colore colorePedine = Colore.White;
	public Socket socket;
	public BufferedWriter invia;
	public BufferedReader ricevi;
	public Scanner sc;

	public Fission(String[] args) throws UnknownHostException, IOException {
		this.connetti(args);
		this.configurazioneCorrente = new Configurazione();
		this.sc = new Scanner(System.in);
	}

	public void connetti(String[] args) throws UnknownHostException, IOException {
		this.socket = new Socket(args[0], Integer.parseInt(args[1]));
		this.invia = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
		this.ricevi = new BufferedReader(new InputStreamReader(socket.getInputStream()));
	}

	public void warmup() {
		this.albero = new AlberoDiRicerca(configurazioneCorrente, colorePedine == Colore.White, 20);
		System.out.println(this.albero.toString());
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

	public static TipoMessaggio getTipoMessaggio(String messaggio) {
		if (messaggio.startsWith("WELCOME")) 			return TipoMessaggio.Welcome;
		else if (messaggio.startsWith("YOUR_TURN")) 	return TipoMessaggio.YourTurn;
		else if (messaggio.startsWith("OPPONENT_MOVE")) return TipoMessaggio.OpponentMove;
		else if (messaggio.startsWith("VALID_MOVE") || messaggio.startsWith("MESSAGE")) 	return TipoMessaggio.Continue;
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

	public void printInfo() {
		System.out.println("** INFO *******************");
		System.out.println(this.configurazioneCorrente.toString());
		this.configurazioneCorrente.printStatoPedine();
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
				
					fission.configurazioneCorrente.assegnaColore(messaggio.substring(8)); 

					fission.warmup();
					
					break;

				case YourTurn : 

					fission.scegliMossa(mossaAlleata);

					fission.inviaMossa(mossaAlleata);

					fission.configurazioneCorrente.muoviPedina(mossaAlleata);
					
					break;
				
				case OpponentMove : 

					mossaAvversaria.setMossa(messaggio);

					fission.configurazioneCorrente.muoviPedina(mossaAvversaria);

					break;

				case Continue : 
				
					break;
				
				case End :

					fission.termina();

			}

			fission.printInfo();

		}

	}

}
