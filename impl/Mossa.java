package impl;

import java.util.Comparator;

import impl.Fission.Direzioni;

public class Mossa implements Comparator<Mossa> {

    public String posIniziale;

    public Direzioni dir;

    public boolean alleata;

    public float index; // pedineAvversarieUccise - pedineAlleate

    public Mossa() {
        super();
    }

    public Mossa(boolean alleata) {
        super();
        this.alleata = alleata;
    }

    public Mossa(int x, int y, Direzioni dir, boolean alleata) {
        super();
        this.posIniziale = Configurazione.RIGHE[x] + "" + y;
        this.dir = dir;
    }

    public Mossa(String posIniziale, Direzioni dir, boolean alleata) {
        super();
        this.posIniziale = posIniziale;
        this.dir = dir;
    }

    public Mossa(String posIniziale, String dir, boolean alleata) {
        super();
        this.posIniziale = posIniziale;
        this.dir = Direzioni.valueOf(dir);
    }

    public static int[] posInizialeToInt(Mossa m) {
        char riga = m.posIniziale.charAt(0), colonna = m.posIniziale.charAt(1);
        if (riga == 'A') return new int[] { 0, Integer.parseInt(colonna + "")-1 };
        if (riga == 'B') return new int[] { 1, Integer.parseInt(colonna + "")-1 };
        if (riga == 'C') return new int[] { 2, Integer.parseInt(colonna + "")-1 };
        if (riga == 'D') return new int[] { 3, Integer.parseInt(colonna + "") -1};
        if (riga == 'E') return new int[] { 4, Integer.parseInt(colonna + "") -1};
        if (riga == 'F') return new int[] { 5, Integer.parseInt(colonna + "") -1};
        if (riga == 'G') return new int[] { 6, Integer.parseInt(colonna + "") -1};
        return new int[] { 7, Integer.parseInt(colonna + "") -1};
    }

    public String toMessage() {
        return "MOVE " + posIniziale + "," + dir;
    }

    public Tupla posInizialeToTupla() {
        int riga = 0, colonna = 0;
        for (int i = 0; i < Configurazione.RIGHE.length; i++) {
            if (posIniziale.charAt(0) == Configurazione.RIGHE[i]) riga = i;
        }
        colonna = Character.getNumericValue(posIniziale.charAt(1)) - 1;
        return new Tupla(riga,colonna);
    }

    public void setMossa(String messaggio) {
        this.setPosIniziale(messaggio.substring(14, 16));
        this.setDir(messaggio.substring(17));
    }

    @Override
    public boolean equals(Object x) {
        if (x == null) return false;
        if (!(x instanceof Mossa)) return false;
        Mossa m = (Mossa) x;
        if (m.posIniziale == null || m.dir == null) return false;
        return m.posIniziale.equals(this.posIniziale) && m.dir.equals(this.dir);
    }

    /****************** Setters & Getters ******************/
    public String getPosIniziale() {
        return posIniziale;
    }

    public void setPosIniziale(String posIniziale) {
        this.posIniziale = posIniziale;
    }

    public Direzioni getDir() {
        return dir;
    }

    public void setDir(String dir) {
        this.dir = Direzioni.valueOf(dir);
    }

    public void setDir(Direzioni dir) {
        this.dir = dir;
    }

    public boolean isAlleata() {
        return alleata;
    }

    public void setAlleata(boolean alleata) {
        this.alleata = alleata;
    }

    @Override
    public int compare(Mossa m1, Mossa m2) {
        return Float.compare(m1.index, m2.index);
    }
    
}
