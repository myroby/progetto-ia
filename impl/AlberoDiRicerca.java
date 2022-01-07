package impl;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class AlberoDiRicerca {

    public enum Tipo { Max, Min }

    private static class Nodo {

        Configurazione conf;

        Tipo tipo;

        List<Nodo> figli;

        public Nodo(Configurazione conf, Tipo tipo, int length) {

            this.conf = conf;

            this.tipo = tipo;

            this.figli = new ArrayList<Nodo>(length);

        }

    }

    public Nodo radice;

    public int profondita;

    public Queue<Nodo> nodiDaEspandere = new LinkedList<Nodo>();

    // generiamo l'albero. Il prossimo avrà come radice un figlio di questo albero
    // questo costruttore verrà eseguito solo una volta
    public AlberoDiRicerca(Configurazione radice, boolean isMassimizzatore, int maxProfondita) {

        StringBuilder sb = new StringBuilder(500);

        this.radice = new Nodo(radice, (isMassimizzatore) ? Tipo.Max : Tipo.Min, 10);

        this.profondita = 1;

        this.nodiDaEspandere.add(this.radice);

        while (this.profondita < maxProfondita && !nodiDaEspandere.isEmpty()) {

            Nodo nodoCorrente = nodiDaEspandere.poll();

            boolean isNodoCorrenteMax = nodoCorrente.tipo == Tipo.Max;

            Tipo tipoDeiFigli = (isNodoCorrenteMax) ? Tipo.Min : Tipo.Max;

            List<Mossa> mossePossibili = nodoCorrente.conf.getMossePossibili(isNodoCorrenteMax);

            for (Mossa m : mossePossibili) System.out.println("mossa = " + m.toMessage());

            mossePossibili.stream().forEach(mossa -> {

                String text = "Mossa analizzata = " + mossa.toMessage() + "\n Nodo parent \n" + 
                    nodoCorrente.conf.toString();

                Configurazione confFiglia = new Configurazione(nodoCorrente.conf, mossa);

                Nodo nodoFiglia = new Nodo(confFiglia, tipoDeiFigli , 10);
                
                nodoCorrente.figli.add(nodoFiglia);

                addNodoDaEspandere(nodoFiglia, profondita);

                text += "\nNodo figlia\n" + nodoFiglia.conf.toString() + "\n\n";

                sb.append(text);

            });

            this.profondita++;

        }
/*********************serve solo per test ***************************************
        File file = new File("data.txt");

        try {
            FileWriter bf = new FileWriter(file);
            bf.append(sb.toString());
            bf.flush();
            bf.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

*/
    }

    public void addNodoDaEspandere(Nodo nodo, int profondita) {

        //if (profondita == 3) return;

        // if (foglia) non espandere ....

        //this.nodiDaEspandere.add(nodo);
    }

    public float euristica(Configurazione configurazione) {
        // prendo l'attacco migliore del nemico
        Mossa mossaAvversario = configurazione.getMossaMigliore(false);
        int numPedine = configurazione.pedineAlleate.size() - configurazione.pedineAvversarie.size();
        if ((mossaAvversario == null && configurazione.mossaMiglioreAlleata.index == 0) || 
                mossaAvversario.index == configurazione.mossaMiglioreAlleata.index)
            return numPedine / 12;
        else if (mossaAvversario == null)
            return configurazione.mossaMiglioreAlleata.index / 6;
        else if (configurazione.mossaMiglioreAlleata.index == 0)
            return mossaAvversario.index / 6;
        return (configurazione.mossaMiglioreAlleata.index - mossaAvversario.index) / 6;

    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(100);
        sb.append(radice.conf.toString() + "\n\n\n");
        for (Nodo n : radice.figli) sb.append(n.conf.toString() + "\n\n\n");
        return sb.toString();
    }
    
}
