import jeu.Plateau;
public class TestP {
    public static void main(String[] args) {
        int l = Plateau.ENDROIT_PRODUCTION_LIBRE;
        int p1 = Plateau.ENDROIT_PRODUCTION_J1;
        System.out.println("Libre contient prod: " + Plateau.contientUneUniteDeProduction(l));
        System.out.println("J1 contient prod: " + Plateau.contientUneUniteDeProduction(p1));
        System.out.println("Utilisateur Libre: " + Plateau.donneUtilisateurDeLUniteDeProduction(l));
    }
}
