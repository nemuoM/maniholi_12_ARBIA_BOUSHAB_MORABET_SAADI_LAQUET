package main;

import jeu.Joueur;
import jeu.Plateau;
import jeu.aetoile.Noeud;
import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * IA Agressive V2 (Blindée Anti-Crash) :
 * - Maximise la capture des moulins du Leader.
 * - Esquive les joueurs au millimètre (bouclier 3x3).
 * - Totalement protégée contre les plantages Java et les blocages infinis.
 */
public class MonJoueurKemil extends Joueur {

    public MonJoueurKemil(String sonNom) {
        super(sonNom);
    }

    @Override
    public Action faitUneAction(Plateau plateau) {
        // =========================================================
        // BOUCLIER ANTI-CRASH GLOBAL
        // Si la moindre erreur survient, on intercepte et on passe le tour.
        // Cela empêche le jeu de nous disqualifier pour "Plantage".
        // =========================================================
        try {
            Point maPosition = this.donnePosition();
            if (maPosition == null) return Action.RIEN; // Sécurité de base

            int monEnergie = this.donneRessources();
            int maCaseCourante = plateau.donneContenuCellule(maPosition);

            // --- 0. ANALYSE DU CLASSEMENT ---
            int pointsDuLeader = 0;
            Joueur[] tousLesJoueurs = plateau.donneJoueurs();
            if (tousLesJoueurs != null) {
                for (Joueur j : tousLesJoueurs) {
                    if (j != null && j.donneRang() != this.donneRang()) {
                        if (j.donnePoints() > pointsDuLeader) {
                            pointsDuLeader = j.donnePoints();
                        }
                    }
                }
            }

            // --- 1. GESTION DU REPOS OPTIMISÉ ---
            if (Plateau.contientUneUniteDeRessourcage(maCaseCourante) && monEnergie < 85) {
                return Action.RIEN;
            }

            // --- 2. GESTION DE L'ÉNERGIE (Flux Tendu) ---
            Point oliveraieProche = trouverOliveraieLaPlusProche(plateau, maPosition);
            int distanceOliveraie = (oliveraieProche != null) ? calculerDistance(maPosition, oliveraieProche) : 0;

            boolean urgenceEnergie = monEnergie <= (distanceOliveraie + 10);
            int rechercheCible = urgenceEnergie ? Plateau.CHERCHE_RESSOURCE : Plateau.CHERCHE_PRODUCTION;

            // --- 3. SCAN RADAR ---
            HashMap<Integer, ArrayList<Point>> radar = plateau.cherche(maPosition, 40, rechercheCible);
            if (radar == null) return mouvementAleatoireDeSurvie(plateau, maPosition); // Sécurité anti-null

            ArrayList<Point> ciblesPotentielles = radar.get(rechercheCible);
            if (ciblesPotentielles == null || ciblesPotentielles.isEmpty()) {
                return Action.RIEN;
            }

            // --- 4. ÉVITEMENT CHIRURGICAL (Anti-Manille) ---
            List<Noeud> obstaclesAntiManille = genererBlocageChirurgical(plateau, maPosition);

            ArrayList<Noeud> meilleurChemin = null;
            double meilleurScore = -1.0;

            for (Point cible : ciblesPotentielles) {
                if (cible == null) continue;

                double scoreCible = 0;

                if (rechercheCible == Plateau.CHERCHE_PRODUCTION) {
                    int contenuCase = plateau.donneContenuCellule(cible);

                    if (!Plateau.contientUneUniteDeProductionLibre(contenuCase) &&
                            !Plateau.contientUneUniteDeProductionQuiNeLuiAppartientPas(this, contenuCase)) {
                        continue;
                    }

                    int distance = calculerDistance(maPosition, cible);
                    if (distance == 0) distance = 1;

                    scoreCible = 1000.0 / distance;

                    if (Plateau.contientUneUniteDeProductionQuiNeLuiAppartientPas(this, contenuCase)) {
                        int tourActuel = plateau.donneTourCourant();

                        if (tourActuel < 100) {
                            scoreCible *= 0.8;
                        }
                        else {
                            int utilisateur = Plateau.donneUtilisateurDeLUniteDeProduction(contenuCase);
                            if (utilisateur > 0) {
                                Joueur proprietaire = plateau.donneJoueur(utilisateur - 1);
                                if (proprietaire != null) {
                                    if (proprietaire.donnePoints() >= pointsDuLeader && pointsDuLeader > 0) {
                                        scoreCible *= 3.0;
                                    } else {
                                        scoreCible *= 1.2;
                                    }
                                }
                            }
                        }
                    }
                } else {
                    int distance = calculerDistance(maPosition, cible);
                    scoreCible = 1000.0 / (distance == 0 ? 1 : distance);
                }

                ArrayList<Noeud> cheminTest = plateau.donneCheminAvecObstaclesSupplementaires(maPosition, cible, obstaclesAntiManille);

                if (cheminTest != null && !cheminTest.isEmpty() && scoreCible > meilleurScore) {
                    meilleurScore = scoreCible;
                    meilleurChemin = cheminTest;
                }
            }

            // --- 5. PLAN B (Survie Énergétique) ---
            if (meilleurChemin == null && rechercheCible == Plateau.CHERCHE_RESSOURCE) {
                int distMin = Integer.MAX_VALUE;
                for (Point cible : ciblesPotentielles) {
                    ArrayList<Noeud> cheminTest = plateau.donneCheminEntre(maPosition, cible);
                    if (cheminTest != null && !cheminTest.isEmpty() && cheminTest.size() < distMin) {
                        distMin = cheminTest.size();
                        meilleurChemin = cheminTest;
                    }
                }
            }

            // --- 6. EXÉCUTION DU MOUVEMENT ---
            if (meilleurChemin != null && !meilleurChemin.isEmpty()) {
                Noeud noeudSuivant = null;
                for (Noeud n : meilleurChemin) {
                    if (n.getX() != maPosition.x || n.getY() != maPosition.y) {
                        noeudSuivant = n;
                        break;
                    }
                }
                if (noeudSuivant != null) {
                    return determinerActionVers(maPosition, noeudSuivant);
                }
            }

            // --- 7. PLAN C (Anti-SoftLock Absolu) ---
            // Si on arrive ici, c'est qu'on a aucun chemin (bloqué de partout).
            // Au lieu de retourner RIEN à l'infini et de bloquer la partie, on tente de se dégager.
            return mouvementAleatoireDeSurvie(plateau, maPosition);

        } catch (Exception e) {
            // L'erreur est interceptée, l'IA survit pour jouer le tour suivant !
            return Action.RIEN;
        }
    }

    // =========================================================
    //         MÉTHODES UTILITAIRES
    // =========================================================

    /**
     * Tente de faire un petit pas vers une case vide si l'IA est coincée (Soft-Lock)
     */
    private Action mouvementAleatoireDeSurvie(Plateau plateau, Point maPosition) {
        int[][] directions = {{0, -1}, {0, 1}, {-1, 0}, {1, 0}}; // HAUT, BAS, GAUCHE, DROITE
        Action[] actions = {Action.HAUT, Action.BAS, Action.GAUCHE, Action.DROITE};

        for (int i = 0; i < directions.length; i++) {
            int testX = maPosition.x + directions[i][0];
            int testY = maPosition.y + directions[i][1];

            if (plateau.coordonneeValide(testX, testY)) {
                int contenuCase = plateau.donneContenuCellule(testX, testY);
                // Si la case est vide ou contient un moulin libre, on s'y décale
                if (Plateau.contientUneZoneVide(contenuCase) || Plateau.contientUneUniteDeProductionLibre(contenuCase)) {
                    return actions[i];
                }
            }
        }
        return Action.RIEN; // Vraiment coincé à 100%, on attend.
    }

    private List<Noeud> genererBlocageChirurgical(Plateau plateau, Point maPosition) {
        List<Noeud> obstacles = new ArrayList<>();
        Joueur[] tousLesJoueurs = plateau.donneJoueurs();

        if (tousLesJoueurs == null) return obstacles;

        for (Joueur j : tousLesJoueurs) {
            if (j != null && j.donneRang() != this.donneRang()) {
                Point posAdversaire = j.donnePosition();
                if (posAdversaire == null) continue;

                for (int dx = -1; dx <= 1; dx++) {
                    for (int dy = -1; dy <= 1; dy++) {
                        int zoneX = posAdversaire.x + dx;
                        int zoneY = posAdversaire.y + dy;

                        if (plateau.coordonneeValide(zoneX, zoneY) &&
                                (zoneX != maPosition.x || zoneY != maPosition.y)) {
                            obstacles.add(new Noeud(zoneX, zoneY));
                        }
                    }
                }
            }
        }
        return obstacles;
    }

    private int calculerDistance(Point a, Point b) {
        if (a == null || b == null) return Integer.MAX_VALUE;
        return Math.abs(a.x - b.x) + Math.abs(a.y - b.y);
    }

    private Point trouverOliveraieLaPlusProche(Plateau plateau, Point maPosition) {
        HashMap<Integer, ArrayList<Point>> radar = plateau.cherche(maPosition, 40, Plateau.CHERCHE_RESSOURCE);
        if (radar == null) return null;

        ArrayList<Point> oliveraies = radar.get(Plateau.CHERCHE_RESSOURCE);
        if (oliveraies == null || oliveraies.isEmpty()) return null;

        Point plusProche = null;
        int distanceMin = Integer.MAX_VALUE;

        for (Point o : oliveraies) {
            if (o == null) continue;
            int d = calculerDistance(maPosition, o);
            if (d < distanceMin) {
                distanceMin = d;
                plusProche = o;
            }
        }
        return plusProche;
    }

    private Action determinerActionVers(Point depart, Noeud suivant) {
        if (depart == null || suivant == null) return Action.RIEN;

        int nextX = suivant.getX();
        int nextY = suivant.getY();

        if (nextX > depart.x) return Action.DROITE;
        if (nextX < depart.x) return Action.GAUCHE;
        if (nextY > depart.y) return Action.BAS;
        if (nextY < depart.y) return Action.HAUT;

        return Action.RIEN;
    }
}