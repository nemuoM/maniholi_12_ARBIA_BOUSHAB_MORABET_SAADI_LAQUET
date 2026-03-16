package main;

import jeu.Joueur;
import jeu.Plateau;

/**
 * Le joueur de l'Ã©quipe ???.
 *
 * Un joueur dont la stratÃ©gie de jeu est dÃ©finie par
 * {@link #faitUneAction(Plateau) }, Ã  tester dans le {@link Lanceur} du jeu.
 *
 * @author ???
 */
public class MonJoueur extends Joueur {

    public MonJoueur(String sonNom) {
        super(sonNom);
    }

    @Override
    public Action faitUneAction(Plateau etatDuJeu) {
        return super.faitUneAction(etatDuJeu); // Ã  modifier
    }

}