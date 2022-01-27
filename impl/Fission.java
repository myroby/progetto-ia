package impl;

import java.util.Scanner;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class Fission {
	public static enum Colore { White, Black }
	public static enum Direzioni { N, E, S, W, NE, SE, SW, NW }
	public static enum TipoMessaggio { Welcome, YourTurn, Continue, OpponentMove, End  }
	public Configurazione configurazioneCorrente;
	public AlberoDiRicerca albero;
	public Colore colorePedine;
	public Socket socket;
	public BufferedWriter invia;
	public BufferedReader ricevi;

	public Fission(String[] args) throws UnknownHostException, IOException {
		//this.connetti(args);
	}

	public void connetti(String[] args) throws UnknownHostException, IOException {
		this.socket = new Socket(args[0], Integer.parseInt(args[1]));
		this.invia = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
		this.ricevi = new BufferedReader(new InputStreamReader(socket.getInputStream()));
	}

	public void warmup() {
		this.configurazioneCorrente = new Configurazione(this, colorePedine);
		this.configurazioneCorrente.colorePedine = colorePedine;
		long start = System.currentTimeMillis();
		System.out.println("Sto creando l'albero...");
		this.albero = new AlberoDiRicerca(this.configurazioneCorrente, colorePedine == Colore.White, 250);
		System.out.println(this.albero.root.conf.toString());
		System.out.println("Albero creato in " + (System.currentTimeMillis() - start) + " ms");/*
		if (colorePedine == Colore.White) {
		try {
			File f = new File("Test.txt");
			FileWriter fl = new FileWriter(f);
			fl.write(this.albero.toString());
			fl.close();
		} catch (IOException e) {
			e.printStackTrace();
		}}*/
	}

	// aggiorna i valori posIniziale e dir del parametro mossa
	public Mossa scegliMossa() {

		return this.albero.getMossaMigliore();

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
		System.exit(0);
	}

	/************************ utility di test ************************/
/*
	public void printInfo() {
		System.out.println("** INFO *******************");
		System.out.println(this.configurazioneCorrente.toString());
		this.configurazioneCorrente.printStatoPedine();
	}
*/
	/**************************** main *********************************/

	public static void main(String[] args) throws NumberFormatException, UnknownHostException, IOException {

		System.out.println();

		System.out.println("PC | Bianco\n\nTU | Nero\n");

		Fission fission = new Fission(args); 

		String messaggio;

		Mossa mossaAlleata, mossaAvversaria;

		Scanner sc = new Scanner(System.in);

		fission.assegnaColore("White");

		fission.warmup();

		System.out.println();

		while (true) {

			System.out.println("Sto scegliendo la mossa...\n");

			Mossa mossa = fission.scegliMossa();

			System.out.println("Ho scelto la mossa: " + mossa.toMessage() + "\n");

			System.out.println("Sto aggiornando l'albero...\n");

			fission.albero = fission.albero.eseguiMossa(mossa);

			System.out.println(fission.albero.root.conf.toString());

			System.out.print("Inserisci la mossa nel formato: <RigaColonna,Direzione>, ad esempio: A2,S > ");

			String mex = sc.nextLine();

			if (mex.equals("STOP")) sc.close();

			mossaAvversaria = Mossa.setMossa("OPPONENT_MOVE " + mex);

			System.out.println("\nHai scelto la mossa: " + mossaAvversaria.toMessage() + "\n");

			fission.albero = fission.albero.eseguiMossa(mossaAvversaria);

			System.out.println("Sto aggiornando l'albero...\n");

			System.out.println(fission.albero.root.conf.toString());

		}

		/*

		while ((messaggio = fission.ricevi.readLine()) != null) {

			switch (Fission.getTipoMessaggio(messaggio)) {

				case Welcome : 
				
					fission.assegnaColore(messaggio.substring(8)); 

					fission.warmup();
					
					break;

				case YourTurn : 

					long start = System.currentTimeMillis();

					mossaAlleata = fission.scegliMossa();

					System.out.println("Scelta mossa = " + (System.currentTimeMillis() - start) + " ms");

					fission.inviaMossa(mossaAlleata);

					start = System.currentTimeMillis();

					fission.albero = fission.albero.eseguiMossa(mossaAlleata);

					System.out.println("Aggiornamento albero = " + (System.currentTimeMillis() - start) + " ms");
					
					break;
				
				case OpponentMove : 

					mossaAvversaria = Mossa.setMossa(messaggio);

					start = System.currentTimeMillis();

					fission.albero = fission.albero.eseguiMossa(mossaAvversaria);

					System.out.println("eseguo mossa " + mossaAvversaria.toMessage() + "\n"+fission.albero.root.conf.toString());

					System.out.println("Aggiornamneto albero = " + (System.currentTimeMillis() - start) + " ms");					

					break;

				case Continue : 
				
					break;
				
				case End :

					fission.termina();

			}
			try {
				File f = new File("Test.txt");
				FileWriter fl = new FileWriter(f);
				fl.write(fission.albero.toString());
				fl.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			//fission.printInfo();

		}*/

	}

	private void assegnaColore(String colore) {

		this.colorePedine = Colore.valueOf(colore);

	}

}
