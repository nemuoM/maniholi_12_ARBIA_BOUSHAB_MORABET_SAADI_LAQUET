package main;

import jeu.Joueur;
import jeu.Plateau;
import jeu.aetoile.Noeud;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class UtilityEnzo {

    //Rayon de vision maximal utilisé pour scanner le plateau;
    private static final int RAYON_MAX = 45;

    public Joueur.Action cheminVersAction(Point pos, ArrayList<Noeud> chemin) {
        if (chemin == null || chemin.isEmpty()) return Joueur.Action.RIEN;

        Noeud prochain = trouverProchainNoeud(pos, chemin);
        if (prochain == null) return Joueur.Action.RIEN;

        return directionVers(pos, prochain);
    }

    public Joueur.Action allerVersOliveraie(Point pos, Plateau plateau, Joueur joueur) {
        ArrayList<Point> oliveraies = donneOliveraies(pos, plateau);
        if (oliveraies.isEmpty()) return Joueur.Action.RIEN;

        Point cible = trouverPointLePlusProcheParchemin(pos, oliveraies, plateau);
        if (cible == null) return Joueur.Action.RIEN;

        return cheminVersAction(pos, plateau.donneCheminEntre(pos, cible));
    }

    public ArrayList<Joueur> donneAutresJoueurs(Plateau plateau, Joueur joueur) {
        ArrayList<Joueur> tous = new ArrayList<>(Arrays.asList(plateau.donneJoueurs()));
        tous.removeIf(j -> j.donneNom().equals(joueur.donneNom()));
        return tous;
    }

    public ArrayList<Point> donneOliveraies(Point pos, Plateau plateau) {
        HashMap<Integer, ArrayList<Point>> scan =
                plateau.cherche(pos, RAYON_MAX, Plateau.CHERCHE_RESSOURCE);
        ArrayList<Point> oliveraies = scan.get(Plateau.CHERCHE_RESSOURCE);
        return (oliveraies != null) ? oliveraies : new ArrayList<>();
    }

    public ArrayList<Point> donneMoulins(Point pos, Plateau plateau) {
        HashMap<Integer, ArrayList<Point>> scan =
                plateau.cherche(pos, RAYON_MAX, Plateau.CHERCHE_PRODUCTION);
        ArrayList<Point> moulins = scan.get(Plateau.CHERCHE_PRODUCTION);
        return (moulins != null) ? moulins : new ArrayList<>();
    }

    public Point trouverOliveraieProche(Point pos, Plateau plateau, int rayonMax) {
        Point plusProche = null;
        double distanceMin = Double.MAX_VALUE;

        for (Point p : donneOliveraies(pos, plateau)) {
            double distance = pos.distance(p);
            if (distance <= rayonMax && distance < distanceMin) {
                distanceMin = distance;
                plusProche = p;
            }
        }
        return plusProche;
    }

    public boolean estAMoi(Plateau plateau, Point p, Joueur joueur) {
        int contenu      = plateau.donneContenuCellule(p.x, p.y);
        int proprietaire = Plateau.donneUtilisateurDeLUniteDeProduction(contenu);
        // donneUtilisateurDeLUniteDeProduction retourne rang+1 pour le propriétaire
        return proprietaire == (joueur.donneRang() + 1);
    }

    private Noeud trouverProchainNoeud(Point pos, ArrayList<Noeud> chemin) {
        for (Noeud n : chemin) {
            if (n.getX() != pos.x || n.getY() != pos.y) return n;
        }
        return null;
    }

    private Joueur.Action directionVers(Point source, Noeud cible) {
        int dx = cible.getX() - source.x;
        int dy = cible.getY() - source.y;

        if (dx < 0) return Joueur.Action.GAUCHE;
        if (dx > 0) return Joueur.Action.DROITE;
        if (dy < 0) return Joueur.Action.HAUT;
        if (dy > 0) return Joueur.Action.BAS;

        return Joueur.Action.RIEN;
    }

    private Point trouverPointLePlusProcheParchemin(Point pos,
                                                    ArrayList<Point> points,
                                                    Plateau plateau) {
        Point cible = null;
        int longueurMin = Integer.MAX_VALUE;

        for (Point p : points) {
            ArrayList<Noeud> chemin = plateau.donneCheminEntre(pos, p);
            if (chemin != null && chemin.size() < longueurMin) {
                longueurMin = chemin.size();
                cible = p;
            }
        }
        return cible;
    }

}
