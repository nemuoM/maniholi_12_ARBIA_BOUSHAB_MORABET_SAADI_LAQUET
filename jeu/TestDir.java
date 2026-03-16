package jeu;
import java.awt.Point;
public class TestDir extends Joueur {
    public static void main(String[] args) {
        TestDir j = new TestDir();
        Plateau p = Plateau.generePlateauTournoi();
        j.changePosition(5, 5);
        System.out.println("HAUT: " + j.donnePositionAvec(Action.HAUT, p));
        System.out.println("BAS: " + j.donnePositionAvec(Action.BAS, p));
        System.out.println("GAUCHE: " + j.donnePositionAvec(Action.GAUCHE, p));
        System.out.println("DROITE: " + j.donnePositionAvec(Action.DROITE, p));
        j.changePosition(0, 0);
        System.out.println("WRAP GAUCHE: " + j.donnePositionAvec(Action.GAUCHE, p));
        System.out.println("WRAP HAUT: " + j.donnePositionAvec(Action.HAUT, p));
    }
}
