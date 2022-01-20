package impl;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.swing.text.StyledEditorKit;

import impl.Fission.Colore;

import java.util.Deque;

public class AlberoDiRicerca {

    public static final float TIME_LIMIT_CREAZIONE = 0.2f /* 1.2f */, TIME_LIMIT_INCREMENTO = 0.5f;

    static class Nodo implements Comparable<Nodo> {

        Configurazione conf;

        Nodo parent;

        Tipo tipo;

        List<Nodo> figli;

        boolean isEtichettato;

        int iterazione;

        float etichetta;

        public Nodo(Configurazione conf, Nodo parent, Tipo tipo, int length, int iterazione) {

            this.conf = conf;

            this.parent = parent;

            this.tipo = tipo;

            this.figli = new ArrayList<Nodo>(100);

            this.iterazione = iterazione;

        }

        public void setEtichetta(float e) {
            this.isEtichettato = true;
            this.etichetta = e;
        }

        public boolean isMinimizzatore() {
            return this.tipo == Tipo.Min;
        }

        public boolean isFoglia() {
            return figli.isEmpty();
        }

        public List<Nodo> getAntenatiMinimizzatori(int profondita) {
            List<Nodo> antenati = new ArrayList<Nodo>(profondita / 2);
            Nodo parent = this.parent;
            while (parent != null) {
                if (parent.isMinimizzatore())
                    antenati.add(parent);
                parent = parent.parent;
            }
            return antenati;
        }

        public List<Nodo> getAntenatiMassimizzatori(int profondita) {
            List<Nodo> antenati = new ArrayList<Nodo>(profondita / 2);
            Nodo parent = this.parent;
            while (parent != null) {
                if (!parent.isMinimizzatore())
                    antenati.add(parent);
                parent = parent.parent;
            }
            return antenati;
        }

        @Override
        public int compareTo(Nodo other) {
            return Float.compare(this.etichetta, other.etichetta);
        }

        public List<Nodo> getFigli() {

            if (!this.figli.isEmpty())
                return this.figli;

            // TODO
            this.conf.getMossePossibili().stream().forEach(mossa -> {
                int[] pos = Mossa.posInizialeToInt(mossa);
                if (this.conf.scacchiera[pos[0]][pos[1]].isAlleata()) mossa.alleata = true; else mossa.alleata = false;
                this.figli.add(new Nodo(
                        new Configurazione(this.conf, mossa,
                                (this.conf.colorePedine == Colore.White) ? Colore.Black : Colore.White),
                        this,
                        (this.tipo == Tipo.Max) ? Tipo.Min : Tipo.Max, 50, this.iterazione));

            });

            return this.figli;

        }

        public List<Nodo> generaFigli() {

            List<Nodo> temp = new ArrayList<>();

            // TODO
            this.conf.getMossePossibili().stream().forEach(mossa -> {
                int[] pos = Mossa.posInizialeToInt(mossa);
                if (this.conf.scacchiera[pos[0]][pos[1]].isAlleata()) mossa.alleata = true; else mossa.alleata = false;
                temp.add(new Nodo(
                        new Configurazione(this.conf, mossa,
                                (this.conf.colorePedine == Colore.White) ? Colore.Black : Colore.White),
                        this,
                        (this.tipo == Tipo.Max) ? Tipo.Min : Tipo.Max, 50, this.iterazione));

            });

            return temp;

        }

        public List<Nodo> generaFigliAlleati() {

            List<Nodo> temp = new ArrayList<>();

            // TODO
            this.conf.getMossePossibili(this.tipo == Tipo.Max).stream().forEach(mossa -> {
                int[] pos = Mossa.posInizialeToInt(mossa);
                if (this.conf.scacchiera[pos[0]][pos[1]].isAlleata()) mossa.alleata = true; else mossa.alleata = false;
                temp.add(new Nodo(
                        new Configurazione(this.conf, mossa,
                                (this.conf.colorePedine == Colore.White) ? Colore.Black : Colore.White),
                        this,
                        (this.tipo == Tipo.Max) ? Tipo.Min : Tipo.Max, 50, this.iterazione));

            });

            return temp;

        }

    }

    public Nodo root;

    public boolean isMassimizzatore;

    public int profondita;

    public int maxProfondita;

    public Deque<Nodo> list = new ArrayDeque<Nodo>(60000);

    public List<Nodo> foglieCorr = new ArrayList<Nodo>();

    public int iterazione = 0;

    public float calcolaAlpha(Tipo tipo, List<Nodo> fratelliDiP, List<Nodo> antenatiDiP) {

        float alpha;

        if (tipo == Tipo.Min) {

            // se alpha non esiste
            if (fratelliDiP.size() == 1 && antenatiDiP.isEmpty()) { // DA CONTROLLARE

                alpha = Float.NEGATIVE_INFINITY;

                // se alpha esiste (esiste davvero ?????)
            } else {

                Optional<Nodo> maxFratelliDiP = fratelliDiP.stream().max((a, b) -> a.compareTo(b));

                Optional<Nodo> maxAntenatiDiP = antenatiDiP.stream().max((a, b) -> a.compareTo(b));

                if (maxFratelliDiP.isPresent()) {

                    if (maxAntenatiDiP.isPresent()) {

                        alpha = Float.NEGATIVE_INFINITY;

                    }

                    alpha = maxFratelliDiP.get().etichetta;

                } else {

                    if (maxAntenatiDiP.isPresent()) {

                        alpha = maxFratelliDiP.get().etichetta;

                    }

                    alpha = Math.max(maxFratelliDiP.get().etichetta, maxAntenatiDiP.get().etichetta);

                }

            }

        } else {

            // se alpha non esiste
            if (fratelliDiP.size() == 1 && antenatiDiP.isEmpty()) { // DA CONTROLLARE

                alpha = Float.POSITIVE_INFINITY;

                // se alpha esiste (esiste davvero ?????)
            } else {

                Optional<Nodo> minFratelliDiP = fratelliDiP.stream().min((a, b) -> a.compareTo(b));

                Optional<Nodo> minAntenatiDiP = antenatiDiP.stream().min((a, b) -> a.compareTo(b));

                if (minFratelliDiP.isPresent()) {

                    if (minAntenatiDiP.isPresent()) {

                        alpha = Float.NEGATIVE_INFINITY;

                    }

                    alpha = minFratelliDiP.get().etichetta;

                } else {

                    if (minAntenatiDiP.isPresent()) {

                        alpha = minFratelliDiP.get().etichetta;

                    }

                    alpha = Math.min(minFratelliDiP.get().etichetta, minAntenatiDiP.get().etichetta);

                }

            }

        }

        return alpha;

    }

    public enum Tipo {
        Max, Min
    }

    public AlberoDiRicerca eseguiMossa(Mossa mossa) {

        long start = System.currentTimeMillis();

        boolean stop = false;

        this.iterazione++;

        for (Nodo figlio : this.root.figli) {

            if (figlio.conf.mossaPrecedente.equals(mossa)) {

                this.root = figlio;

                this.root.parent = null;

                break;

            }

        }
        /*
         * List<Nodo> foglieF = new ArrayList<Nodo>();
         * 
         * for (Nodo foglia : this.foglieCorr) {
         * 
         * foglieF.addAll(foglia.getFigli());
         * 
         * }
         * 
         * foglieCorr = foglieF;
         */
        this.list.clear();

        // MODIFICA ETICHETTE

        profondita = 0;

        // step n° 1
        this.list.push(this.root);

        while ((!this.root.isEtichettato && this.root.iterazione != this.iterazione)
                || this.root.etichetta != Float.POSITIVE_INFINITY || this.root.etichetta != Float.NEGATIVE_INFINITY) {

            if (System.currentTimeMillis() - start > TIME_LIMIT_INCREMENTO * 1000)
                stop = true;

            if (this.list.isEmpty())
                break;

            // System.out.println("WHILE");

            Nodo x = this.list.pop(), p = x.parent;

            if (!stop)
                x.getFigli();

            // serve per sapere se entrare nello step n° 4 oppure no
            boolean hoRimossoP = false; // VA QUI ???????

            // step n° 3 DA CONTROLLARE
            if (x.isEtichettato && x.iterazione == this.iterazione && p != null) {

                // System.out.println("STEP 3");

                List<Nodo> fratelliDiP, antenatiDiP;

                float alpha;

                // TODO DA CONTROLLARE
                if (p.parent != null) {

                    fratelliDiP = p.parent.figli;

                    if (p.isMinimizzatore()) {

                        antenatiDiP = p.getAntenatiMinimizzatori(this.profondita);

                        alpha = calcolaAlpha(p.tipo, fratelliDiP, antenatiDiP);

                        if (x.etichetta <= alpha) { // FORSE SI PUO' TOGLIERE NEL CASO IN CUI FRATELLI O ANTENATI NON
                                                    // ESISTONO ??????

                            hoRimossoP = true;

                            this.list.remove(p);

                            this.list.removeAll(p.figli);

                        }

                    } else {

                        antenatiDiP = p.getAntenatiMassimizzatori(this.profondita);

                        alpha = calcolaAlpha(p.tipo, fratelliDiP, antenatiDiP);

                        if (x.etichetta >= alpha) { // FORSE SI PUO' TOGLIERE NEL CASO IN CUI FRATELLI O ANTENATI NON
                                                    // ESISTONO ??????

                            hoRimossoP = true;

                            this.list.remove(p);

                            this.list.removeAll(p.figli);

                        }

                    }

                }

            } // end step n° 3

            // step n° 4
            if ((!x.isEtichettato || (x.isEtichettato && x.iterazione != this.iterazione)) && x.parent != null
                    && !hoRimossoP) {

                // System.out.println("STEP 4 " + profondita);

                float valoreEtichettaParent = (p.isMinimizzatore()) ? Math.min(p.etichetta, x.etichetta)
                        : Math.max(p.etichetta, x.etichetta);

                p.setEtichetta(valoreEtichettaParent);

                p.iterazione = this.iterazione;

            }

            // step n° 5 (BISOGNA CONTROLLARE)
            if ((!x.isEtichettato || (x.isEtichettato && x.iterazione != this.iterazione))
                    && (x.isFoglia() || profondita >= maxProfondita)) {

                // System.out.println("STEP 5");

                x.setEtichetta(euristica(x, x.conf));

                x.iterazione = this.iterazione;

                this.list.push(x);

                // step n° 6
            } else {

                if (x.isEtichettato && x.iterazione == this.iterazione)
                    continue;

                // System.out.println("STEP 6");

                x.setEtichetta(x.isMinimizzatore() ? Float.POSITIVE_INFINITY : Float.NEGATIVE_INFINITY);

                x.iterazione = this.iterazione;

                x.figli.forEach(figlio -> this.list.push(figlio));

                this.list.addLast(x);

                profondita++;

            }

        } // end while

        return this;

    }

    // generiamo l'albero. Il prossimo avrà come radice un figlio di questo albero.
    // questo costruttore verrà eseguito solo una volta
    public AlberoDiRicerca(Configurazione radice, boolean isMassimizzatore, int maxProfondita) {

        long start = System.currentTimeMillis();

        boolean stop = false;

        this.isMassimizzatore = isMassimizzatore;

        this.maxProfondita = maxProfondita;

        this.root = new Nodo(radice, null, (isMassimizzatore) ? Tipo.Max : Tipo.Min, 10, this.iterazione);

        // step n° 1
        this.list.push(this.root);

        // System.out.println("STEP 1");

        // step n° 2
        while (!this.root.isEtichettato || this.root.etichetta != Float.POSITIVE_INFINITY
                || this.root.etichetta != Float.NEGATIVE_INFINITY) {

            if (System.currentTimeMillis() - start > TIME_LIMIT_CREAZIONE * 1000)
                stop = true;

            if (this.list.isEmpty())
                break;

            // System.out.println("STEP 2");

            // System.out.println(this.list.size());

            Nodo x = this.list.pop(), p = x.parent;

            if (!stop)
                x.getFigli();

            // serve per sapere se entrare nello step n° 4 oppure no
            boolean hoRimossoP = false; // VA QUI ???????

            // step n° 3 DA CONTROLLARE
            if (x.isEtichettato && p != null) {

                // System.out.println("STEP 3");

                List<Nodo> fratelliDiP, antenatiDiP;

                float alpha;

                // TODO DA CONTROLLARE
                if (p.parent != null) {

                    fratelliDiP = p.parent.figli;

                    if (p.isMinimizzatore()) {

                        antenatiDiP = p.getAntenatiMinimizzatori(this.profondita);

                        alpha = calcolaAlpha(p.tipo, fratelliDiP, antenatiDiP);

                        if (x.etichetta <= alpha) { // FORSE SI PUO' TOGLIERE NEL CASO IN CUI FRATELLI O ANTENATI NON
                                                    // ESISTONO ??????

                            hoRimossoP = true;

                            this.list.remove(p);

                            this.list.removeAll(p.figli);

                        }

                    } else {

                        antenatiDiP = p.getAntenatiMassimizzatori(this.profondita);

                        alpha = calcolaAlpha(p.tipo, fratelliDiP, antenatiDiP);

                        if (x.etichetta >= alpha) { // FORSE SI PUO' TOGLIERE NEL CASO IN CUI FRATELLI O ANTENATI NON
                                                    // ESISTONO ??????

                            hoRimossoP = true;

                            this.list.remove(p);

                            this.list.removeAll(p.figli);

                        }

                    }

                }

            } // end step n° 3

            // step n° 4
            if (!x.isEtichettato && x.parent != null && !hoRimossoP) {

                // System.out.println("STEP 4");

                float valoreEtichettaParent = (p.isMinimizzatore()) ? Math.min(p.etichetta, x.etichetta)
                        : Math.max(p.etichetta, x.etichetta);

                p.setEtichetta(valoreEtichettaParent);

            }

            // step n° 5 (BISOGNA CONTROLLARE)
            if (!x.isEtichettato && x.isFoglia()) {

                // System.out.println("STEP 5");

                x.setEtichetta(euristica(x, x.conf));

                this.list.push(x);
                foglieCorr.add(x);
                // step n° 6
            } else {

                if (x.isEtichettato)
                    continue;

                // System.out.println("STEP 6");

                x.setEtichetta(x.isMinimizzatore() ? Float.POSITIVE_INFINITY : Float.NEGATIVE_INFINITY);

                x.figli.forEach(figlio -> this.list.push(figlio));

                this.list.addLast(x);

                profondita++;

            }

        } // end while

    }

    public float euristica(Nodo n, Configurazione configurazione) {

        List<Nodo> figli = n.generaFigli();

        float euristica = 0.0f;

        float mul;

        Mossa mossa = null;

        if (n.isMinimizzatore()) {
            mossa = Configurazione.getMossaMigliore(configurazione, false, figli);
            mul = -1f;
        } else {
            Configurazione.getMossaMigliore(configurazione, true, figli);
            mul = 1f;
        }

        if (mossa == null || (mossa != null && mossa.index == 0)) {

            euristica = (configurazione.pedineBianche.size() - configurazione.pedineNere.size()) / 12.0f;

        } else {
            
            euristica = mossa.index / 6.0f * mul;
            
        }

        return euristica;

    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(100);
        sb.append(root.conf.toString() + "\n\n\n");
        Deque<String> indici = new ArrayDeque<String>();
        Deque<Nodo> temp = new ArrayDeque<Nodo>(50);
        temp.push(root);
        indici.add("00");
        int i = 0;
        while (!temp.isEmpty()) {
            Nodo nodoN = temp.pop();
            sb.append(nodoN.conf.toString());
            sb.append("Etichetta = " + nodoN.etichetta + "\n");
            sb.append(indici.pop());
            int j = 0;
            for (Nodo f : nodoN.figli) {
                j++;
                temp.addLast(f);
                indici.addLast(i + "" + j);
            }
            i++;
        }

        return sb.toString();
    }

    public Mossa getMossaMigliore() {
        //return Configurazione.getMossaMigliore(this.root.conf, isMassimizzatore, this.root.figli);

        Mossa m = this.root.figli.stream().filter(f -> {
            int[] pos = Mossa.posInizialeToInt(f.conf.mossaPrecedente);
            return this.root.conf.scacchiera[pos[0]][pos[1]].isAlleata();
        }).max(new Comparator<Nodo>() {

            @Override
            public int compare(Nodo o1, Nodo o2) {
                return Float.compare(o1.etichetta, o2.etichetta);
            }

        }).get().conf.mossaPrecedente;

        System.out.println("HO scelto la mossa " + m.toMessage() +  " con index = " + m.index);

        return m;

/*
        Optional<Nodo> nodoEtichettaMax = this.root.figli.stream().filter(n -> {
            Mossa mossaPrecedente = n.conf.mossaPrecedente;
            char c = mossaPrecedente.posIniziale.charAt(0);
            return true;
        }).max(new Comparator<Nodo>() {

            @Override
            public int compare(Nodo o1, Nodo o2) {
                return Float.compare(o1.etichetta, o2.etichetta);
            }
            
        });

        return nodoEtichettaMax.get().conf.mossaPrecedente;*/
    }

}
