package impl;

import java.util.function.Function;

import impl.Fission.Direzioni;

public final class Utils {

    // ritorna una funzione che incrementa una o più coordinate; es:
	// se la direzione è NE allora ritorna una funzione f. applicando f su una
	// tupla ritorna un'altra tupla con le coordinate aggiornate:
	// f(5,8) -> (4,9)
	public static Function<Tupla, Tupla> muoviPedinaHelper(Direzioni dir) {
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
			case W:
				return func = in -> {
					return new Tupla(in.x, in.y - 1);
				};
			case NE:
				return func = in -> {
					return new Tupla(in.x - 1, in.y + 1);
				};
			case NW:
				return func = in -> {
					return new Tupla(in.x - 1, in.y - 1);
				};
			case SE:
				return func = in -> {
					return new Tupla(in.x + 1, in.y + 1);
				};
			case SW:
				return func = in -> {
					return new Tupla(in.x + 1, in.y - 1);
				};
		}
		return func;
	}
    
}
