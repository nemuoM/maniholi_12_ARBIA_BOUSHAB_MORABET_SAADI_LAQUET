import jeu.Plateau;
import jeu.Joueur;
public class TestPlateauHelper {
    public static void main(String[] args) {
        System.out.println("Libre: " + Plateau.ENDROIT_PRODUCTION_LIBRE);
        System.out.println("J1: " + Plateau.ENDROIT_PRODUCTION_J1);
        System.out.println("Utilisateur de J1: " + Plateau.donneUtilisateurDeLUniteDeProduction(Plateau.ENDROIT_PRODUCTION_J1));
    }
}
