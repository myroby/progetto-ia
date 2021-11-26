package impl;

import impl.Fission.Direzioni;

public class Mossa {

    public String posIniziale;

    public Direzioni dir;

    public boolean alleata;

    public Mossa(boolean alleata) {
        super();
        this.alleata = alleata;
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

    public String toMessage() {
        return "MOVE " + posIniziale + "," + dir;
    }

    public Tupla posInizialeToTupla() {
        int riga = 0, colonna = 0;
        for (int i = 0; i < Fission.RIGHE.length; i++) {
            if (posIniziale.charAt(0) == Fission.RIGHE[i]) riga = i;
        }
        colonna = Character.getNumericValue(posIniziale.charAt(1)) - 1;
        return new Tupla(riga,colonna);
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
    
}
