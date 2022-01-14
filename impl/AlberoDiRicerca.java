package impl;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Deque;

public class AlberoDiRicerca {

    public enum Tipo {
        Max, Min
    }

    private static class Nodo implements Comparable<Nodo> {

        Configurazione conf;

        Nodo parent;

        Tipo tipo;

        List<Nodo> figli;

        boolean isEtichettato;

        float etichetta;

        public Nodo(Configurazione conf, Nodo parent, Tipo tipo, int length) {

            this.conf = conf;

            this.parent = parent;

            this.tipo = tipo;

            this.figli = new ArrayList<Nodo>(length);

        }

        public void setEtichetta(float e) {
            this.isEtichettato = true;
            this.etichetta = e;
        }

        public boolean isMinimizzatore() {
            return this.tipo == Tipo.Min;
        }

        // TODO !!!!!!!!!!! CONTROLLARE
        public boolean isFoglia() {
            return figli.isEmpty();
        }

        // TODO !!!!!!!!!!!!!CONTROLLARE
        public List<Nodo> getAntenatiMinimizzatori() {
            List<Nodo> antenati = new ArrayList<Nodo>(100);
            getAntenatiMinimizzatori(this, antenati);
            return antenati;
        }

        private void getAntenatiMinimizzatori(Nodo nodo, List<Nodo> antenati) {
            if (nodo.parent == null)
                return;
            if (nodo.parent.isMinimizzatore())
                antenati.add(nodo.parent);
            getAntenatiMassimizzatori(nodo.parent, antenati);
        }

        // TODO !!!!!!!!!!!!!!CONTROLLARE
        public List<Nodo> getAntenatiMassimizzatori() {
            List<Nodo> antenati = new ArrayList<Nodo>(100);
            getAntenatiMassimizzatori(this, antenati);
            return antenati;
        }

        private void getAntenatiMassimizzatori(Nodo nodo, List<Nodo> antenati) {
            if (nodo.parent == null)
                return;
            if (!nodo.parent.isMinimizzatore())
                antenati.add(nodo.parent);
            getAntenatiMassimizzatori(nodo.parent, antenati);
        }

        @Override
        public int compareTo(Nodo other) {
            return Float.compare(this.etichetta, other.etichetta);
        }

        public List<Nodo> getFigli() {

            if (!this.figli.isEmpty())
                return this.figli;

            this.conf.getMossePossibili(this.tipo == Tipo.Max).stream().forEach(mossa -> {

                this.figli.add(new Nodo(new Configurazione(this.conf, mossa), this,
                        (this.tipo == Tipo.Max) ? Tipo.Min : Tipo.Max, 50));

            });

            return this.figli;

        }

    }

    public Nodo root;

    public int profondita;

    public Deque<Nodo> list = new ArrayDeque<Nodo>(50); // 50 ?????????????????????

    public float calcolaAlpha(Tipo tipo, List<Nodo> fratelliDiP, List<Nodo> antenatiDiP) {

        float alpha;

        if (tipo == Tipo.Min) {

            // se alpha non esiste
            if (fratelliDiP.size() == 1 || antenatiDiP.isEmpty()) { // DA CONTROLLARE

                alpha = Float.NEGATIVE_INFINITY;

                // se alpha esiste (esiste davvero ?????)
            } else {

                float maxFratelliDiP = fratelliDiP.stream().max((a, b) -> a.compareTo(b)).get().etichetta;

                float maxAntenatiDiP = antenatiDiP.stream().max((a, b) -> a.compareTo(b)).get().etichetta;

                alpha = Math.max(maxFratelliDiP, maxAntenatiDiP);

            }

        } else {

            // se alpha non esiste
            if (fratelliDiP.size() == 1 || antenatiDiP.isEmpty()) { // DA CONTROLLARE

                alpha = Float.POSITIVE_INFINITY;

                // se alpha esiste (esiste davvero ?????)
            } else {

                float minFratelliDiP = fratelliDiP.stream().min((a, b) -> a.compareTo(b)).get().etichetta;

                float minAntenatiDiP = antenatiDiP.stream().min((a, b) -> a.compareTo(b)).get().etichetta;

                alpha = Math.min(minFratelliDiP, minAntenatiDiP);

            }

        }

        return alpha;

    }

    // TODO vanno ricalcolate le etichette
    public AlberoDiRicerca eseguiMossa(Mossa mossa) {

        for (Nodo figlio : this.root.figli) {

            if (figlio.conf.mossaPrecedente.equals(mossa)) {

                this.root = figlio;

                break;

            }

        }

        return this;

    }

    // generiamo l'albero. Il prossimo avrà come radice un figlio di questo albero.
    // questo costruttore verrà eseguito solo una volta
    public AlberoDiRicerca(Configurazione radice, boolean isMassimizzatore, int maxProfondita) {

        // step n° 1
        this.list.push(new Nodo(radice, null, (isMassimizzatore) ? Tipo.Max : Tipo.Min, 10));

        // step n° 2
        while (!this.root.isEtichettato) {

            Nodo x = this.list.pop(), p = x.parent;

            // serve per sapere se entrare nello step n° 4 oppure no
            boolean hoRimossoP = false; // VA QUI ???????

            // step n° 3
            if (x.isEtichettato) {

                List<Nodo> fratelliDiP = p.parent.figli, antenatiDiP;

                float alpha;

                if (p.isMinimizzatore()) {

                    antenatiDiP = p.getAntenatiMinimizzatori();

                    alpha = calcolaAlpha(p.tipo, fratelliDiP, antenatiDiP);

                    if (x.etichetta <= alpha) { // FORSE SI PUO' TOGLIERE NEL CASO IN CUI FRATELLI O ANTENATI NON
                                                // ESISTONO ??????

                        hoRimossoP = true;

                        this.list.remove(p);

                        this.list.removeAll(p.figli);

                    }

                } else {

                    antenatiDiP = p.getAntenatiMassimizzatori();

                    alpha = calcolaAlpha(p.tipo, fratelliDiP, antenatiDiP);

                    if (x.etichetta >= alpha) { // FORSE SI PUO' TOGLIERE NEL CASO IN CUI FRATELLI O ANTENATI NON
                                                // ESISTONO ??????

                        hoRimossoP = true;

                        this.list.remove(p);

                        this.list.removeAll(p.figli);

                    }

                }

            } // end step n° 3

            // step n° 4
            if (!x.isEtichettato && !hoRimossoP) {

                float valoreEtichettaParent = (p.isMinimizzatore()) ? Math.min(p.etichetta, x.etichetta)
                        : Math.max(p.etichetta, x.etichetta);

                p.setEtichetta(valoreEtichettaParent);

            }

            // step n° 5 (BISOGNA CONTROLLARE)
            if (!x.isEtichettato && (x.isFoglia() || profondita == maxProfondita)) {

                x.setEtichetta(euristica(x.conf));

                this.list.push(x);

                // step n° 6
            } else {

                x.setEtichetta(x.isMinimizzatore() ? Float.POSITIVE_INFINITY : Float.NEGATIVE_INFINITY);

                x.getFigli().forEach(figlio -> this.list.push(figlio));

                this.list.push(x);

            }

        } // end while

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
        sb.append(root.conf.toString() + "\n\n\n");
        for (Nodo n : root.figli)
            sb.append(n.conf.toString() + "\n\n\n");
        return sb.toString();
    }

    // TODO deve aggiornare l'albero (la radice)
    public void getMossaMigliore(Mossa mossa) {

        

    }

}
