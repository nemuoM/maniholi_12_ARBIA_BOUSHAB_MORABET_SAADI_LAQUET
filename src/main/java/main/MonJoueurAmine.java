package main;

import jeu.Joueur;
import jeu.Plateau;
import jeu.aetoile.Noeud;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

/**
 * Bot Maniholi — V3 PREDATOR CORRIGÉ
 * Plus d'abandon ! On joue les objectifs (les moulins) à fond.
 */
public class MonJoueurAmine extends Joueur {

    private static final int COUT_CAPTURE_MOULIN = 20;
    private static final int SEUIL_RECHARGE_MAX = 85;

    public MonJoueurAmine(String sonNom) {
        super(sonNom);
    }

    @Override
    public Action faitUneAction(Plateau plateau) {
        try {
            return cerveauPredictif(plateau);
        } catch (Exception e) {
            return Action.RIEN;
        }
    }

    private Action cerveauPredictif(Plateau plateau) {
        Point maPos = donnePosition();
        int monEnergie = donneRessources();
        int monRang = donneRang();
        int[] echanges = plateau.donneToursRestantEchange();

        // 1. STATUT PARALYSÉ
        if (echanges != null && monRang < echanges.length && echanges[monRang] > 0) {
            return Action.RIEN;
        }

        EtatPlateau etat = scanner(plateau, maPos, monRang);

        // 2. RECHARGE EN COURS (On ne bouge pas si on est sur l'oliveraie et pas plein)
        if (estSurOliveraie(plateau, maPos) && monEnergie < SEUIL_RECHARGE_MAX) {
            return Action.RIEN;
        }

        Point meilleureOliveraie = trouverOliveraieLaPlusProche(plateau, maPos, etat.oliveraies);
        int distanceOliveraie = (meilleureOliveraie != null) ? distanceManhattan(maPos, meilleureOliveraie) : 99;

        // 3. SURVIE ABSOLUE : Si on va tomber en panne d'énergie
        // On garde juste de quoi rentrer à l'oliveraie + 5 de marge
        if (meilleureOliveraie != null && monEnergie <= (distanceOliveraie + 5)) {
            return allerVers(plateau, maPos, meilleureOliveraie);
        }

        // 4. CHERCHER LE MEILLEUR MOULIN (L'OBJECTIF PRINCIPAL !)
        Point meilleurMoulin = choisirMoulin(plateau, maPos, monEnergie, etat, monRang);

        if (meilleurMoulin != null) {
            Action goMoulin = allerVers(plateau, maPos, meilleurMoulin);
            if (goMoulin != null && goMoulin != Action.RIEN) {
                return goMoulin;
            }
        }

        // 5. MANILLE D'OPPORTUNITÉ (Si on n'a vraiment aucun moulin accessible)
        Action braquage = chercherBraquage(plateau, maPos, monEnergie, etat.adversaires, echanges);
        if (braquage != null) return braquage;

        // 6. FALLBACK : On retourne se recharger en attendant que ça se libère
        if (meilleureOliveraie != null) {
            return allerVers(plateau, maPos, meilleureOliveraie);
        }

        return Action.RIEN;
    }

    private Point choisirMoulin(Plateau plateau, Point maPos, int monEnergie, EtatPlateau etat, int monRang) {
        Point meilleur = null;
        double meilleurScore = Double.NEGATIVE_INFINITY;
        int toursRestants = plateau.donneNombreDeTours() - plateau.donneTourCourant();

        List<Point> tousMoulins = new ArrayList<>();
        tousMoulins.addAll(etat.moulinsLibres);
        tousMoulins.addAll(etat.moulinsAdverses);

        for (Point moulin : tousMoulins) {
            ArrayList<Noeud> chemin = plateau.donneCheminEntre(maPos, moulin);
            if (chemin == null) continue;

            int maDistanceReelle = Math.max(0, chemin.size() - 1);

            // Si on n'a même pas l'énergie physique d'y aller et de le capturer, on ignore
            if (monEnergie < maDistanceReelle + COUT_CAPTURE_MOULIN) continue;

            int tempsAdversaire = Integer.MAX_VALUE;
            for (Joueur j : plateau.donneJoueurs()) {
                if (j == null || j.donneRang() == monRang) continue;
                int distAdv = distanceManhattan(j.donnePosition(), moulin);
                if (distAdv < tempsAdversaire) tempsAdversaire = distAdv;
            }

            // Calcul du Score de base
            double score = (toursRestants - maDistanceReelle) * 2.0;

            // Gestion de la concurrence : ON N'ABANDONNE PLUS, on met juste un malus
            if (tempsAdversaire < maDistanceReelle) {
                score -= 100; // Un adversaire y sera avant, grosse pénalité mais pas d'abandon total
            } else if (tempsAdversaire == maDistanceReelle) {
                score -= 30;  // Arrivée en même temps, risqué
            } else {
                score += 50;  // La voie est libre !
            }

            if (etat.moulinsAdverses.contains(moulin)) score += 20; // Bonus pour voler
            score -= maDistanceReelle * 3.0; // Préférer ce qui est près

            if (score > meilleurScore) {
                meilleurScore = score;
                meilleur = moulin;
            }
        }
        return meilleur;
    }

    private Action chercherBraquage(Plateau plateau, Point maPos, int monEnergie, List<Point> adversaires, int[] echanges) {
        for (Point advPos : adversaires) {
            Joueur adv = plateau.donneJoueurEnPosition(advPos);
            if (adv == null) continue;
            int rangAdv = adv.donneRang();
            if (echanges != null && rangAdv < echanges.length && echanges[rangAdv] > 0) continue;
            if (estSurOliveraie(plateau, advPos)) continue;

            // On ne braque que s'il est JUSTE À CÔTÉ (distance 1) pour être sûr de l'avoir
            if (distanceManhattan(maPos, advPos) == 1 && adv.donneRessources() > monEnergie + 20) {
                return allerVers(plateau, maPos, advPos);
            }
        }
        return null;
    }

    private Action allerVers(Plateau plateau, Point pos, Point cible) {
        ArrayList<Noeud> chemin = plateau.donneCheminEntre(pos, cible);
        if (chemin != null && chemin.size() > 0) {
            for (Noeud n : chemin) {
                int dx = n.getX() - pos.x;
                int dy = n.getY() - pos.y;
                if (Math.abs(dx) + Math.abs(dy) == 1) { // Recherche de la case adjacente
                    return versAction(dx, dy);
                }
            }
        }
        return prochainPasGreedy(plateau, pos, cible);
    }

    private Action prochainPasGreedy(Plateau plateau, Point pos, Point cible) {
        int[][] dirs = {{0, -1}, {0, 1}, {-1, 0}, {1, 0}};
        Action[] actions = {Action.HAUT, Action.BAS, Action.GAUCHE, Action.DROITE};
        Action meilleure = Action.RIEN;
        int minDist = distanceManhattan(pos, cible);

        for (int i = 0; i < 4; i++) {
            int nx = pos.x + dirs[i][0];
            int ny = pos.y + dirs[i][1];

            if (plateau.coordonneeValide(nx, ny) &&
                    !Plateau.contientUneZoneInfranchissable(plateau.donneContenuCelluleSansJoueur(nx, ny))) {

                int dist = distanceManhattan(new Point(nx, ny), cible);
                if (dist < minDist) {
                    minDist = dist;
                    meilleure = actions[i];
                }
            }
        }
        return meilleure;
    }

    private Action versAction(int dx, int dy) {
        if (dx > 0) return Action.DROITE;
        if (dx < 0) return Action.GAUCHE;
        if (dy > 0) return Action.BAS;
        if (dy < 0) return Action.HAUT;
        return Action.RIEN;
    }

    private Point trouverOliveraieLaPlusProche(Plateau plateau, Point maPos, List<Point> oliveraies) {
        Point meilleure = null;
        int distMin = Integer.MAX_VALUE;
        for (Point o : oliveraies) {
            int dist = distanceManhattan(maPos, o);
            if (dist < distMin) {
                distMin = dist;
                meilleure = o;
            }
        }
        return meilleure;
    }

    private int distanceManhattan(Point p1, Point p2) {
        return Math.abs(p1.x - p2.x) + Math.abs(p1.y - p2.y);
    }

    private boolean estSurOliveraie(Plateau plateau, Point pos) {
        if (!plateau.coordonneeValide(pos.x, pos.y)) return false;
        return Plateau.contientUneUniteDeRessourcage(plateau.donneContenuCelluleSansJoueur(pos.x, pos.y));
    }

    private EtatPlateau scanner(Plateau plateau, Point maPos, int monRang) {
        EtatPlateau etat = new EtatPlateau();
        int taille = plateau.donneTaille();
        for (int x = 0; x < taille; x++) {
            for (int y = 0; y < taille; y++) {
                int cSJ = plateau.donneContenuCelluleSansJoueur(x, y);
                Point p = new Point(x, y);

                if (Plateau.contientUneUniteDeProductionLibre(cSJ)) etat.moulinsLibres.add(p);
                else if (Plateau.contientUneUniteDeProduction(cSJ) &&
                        Plateau.contientUneUniteDeProductionQuiNeLuiAppartientPas(this, cSJ))
                    etat.moulinsAdverses.add(p);

                if (Plateau.contientUneUniteDeRessourcage(cSJ)) etat.oliveraies.add(p);

                if (Plateau.contientUnJoueur(plateau.donneContenuCellule(x, y)) && !p.equals(maPos)) {
                    etat.adversaires.add(p);
                }
            }
        }
        return etat;
    }

    private static class EtatPlateau {
        List<Point> moulinsLibres = new ArrayList<>();
        List<Point> moulinsAdverses = new ArrayList<>();
        List<Point> oliveraies = new ArrayList<>();
        List<Point> adversaires = new ArrayList<>();
    }
}