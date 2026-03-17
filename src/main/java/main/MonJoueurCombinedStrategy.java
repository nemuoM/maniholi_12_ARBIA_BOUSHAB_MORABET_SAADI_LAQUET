package main;
import jeu.Joueur;
import jeu.Plateau;
import jeu.aetoile.Noeud;

import java.awt.*;
import java.util.ArrayList;

import static jeu.Plateau.*;
import static jeu.Plateau.ENDROIT_PRODUCTION_J4;

public class MonJoueurCombinedStrategy extends Joueur {

    /** Seuil d'énergie déclenchant une recharge d'urgence. */
    private static final int SEUIL_ENERGIE_CRITIQUE = 25;

    /** Nombre de tours restants en dessous duquel on bascule en mode greedy. */
    private static final int SEUIL_FIN_PARTIE = 50;

    /** Coût en énergie nécessaire pour capturer un moulin. */
    private static final int COUT_MOULIN = 20;

    /** Seuil d'énergie déclenchant une recharge préventive. */
    private static final int SEUIL_ENERGIE_BAS = 45;

    /**
     * Rayon (distance euclidienne) pour considérer une oliveraie « proche »
     * lors de la recharge préventive.
     */
    private static final int RAYON_OLIVERAIE_PROCHE = 6;

    /**
     * Rayon (distance euclidienne) pour considérer un moulin possédé comme
     * « à portée » et éviter la recharge préventive.
     */
    private static final int RAYON_MOULIN_PROCHE = 8;

    private final UtilityEnzo routeManager = new UtilityEnzo();

    public MonJoueurCombinedStrategy(String nom) {
        super(nom);
    }

    // -------------------------------------------------------------------------
    // Point d'entrée
    // -------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     *
     * <p>Délègue à {@link #stratCombined(Plateau)}. Retourne {@code RIEN}
     * en cas d'exception pour ne jamais planter le jeu.</p>
     */
    @Override
    public Action faitUneAction(Plateau etatDuJeu) {
        try {
            return stratCombined(etatDuJeu);
        } catch (Exception e) {
            return Action.RIEN;
        }
    }

    // -------------------------------------------------------------------------
    // Stratégie principale
    // -------------------------------------------------------------------------

    /**
     * Stratégie combinée à quatre phases (voir Javadoc de classe).
     *
     * @param etatDuJeu état courant du plateau
     * @return l'action à effectuer ce tour
     */
    public Action stratCombined(Plateau etatDuJeu) {
        Point maPos         = this.donnePosition();
        int   energie       = this.donneRessources();
        int   toursRestants = etatDuJeu.donneNombreDeTours() - etatDuJeu.donneTourCourant();

        // Phase 1 : énergie critique → recharge d'urgence
        if (energie <= SEUIL_ENERGIE_CRITIQUE) {
            Action action = routeManager.allerVersOliveraie(maPos, etatDuJeu, this);
            if (action != Action.RIEN) return action;
        }

        // Phase 2 : fin de partie → stratégie greedy
        if (toursRestants <= SEUIL_FIN_PARTIE) {
            return stratGreedy(maPos, etatDuJeu);
        }

        // Phase 3 : énergie basse sans moulin possédé proche → recharge préventive
        if (energie <= SEUIL_ENERGIE_BAS) {
            Action action = rechargePreventiveSiNecessaire(maPos, etatDuJeu);
            if (action != Action.RIEN) return action;
        }

        // Phase 4 : phase normale → moulin le plus rentable
        return allerVersMoulinLePlusRentable(maPos, toursRestants, etatDuJeu);
    }

    // -------------------------------------------------------------------------
    // Phases de la stratégie
    // -------------------------------------------------------------------------

    /**
     * Stratégie greedy de fin de partie : se dirige vers le moulin non possédé
     * dont le chemin A* est le plus court.
     *
     * <p>Si l'énergie est insuffisante, se rend d'abord à l'oliveraie.</p>
     *
     * @param maPos     position actuelle du joueur
     * @param etatDuJeu état courant du plateau
     * @return action vers le moulin cible, ou {@code RIEN}
     */
    private Action stratGreedy(Point maPos, Plateau etatDuJeu) {
        if (this.donneRessources() < COUT_MOULIN) {
            Action action = routeManager.allerVersOliveraie(maPos, etatDuJeu, this);
            if (action != Action.RIEN) return action;
        }

        ArrayList<Point> moulins = routeManager.donneMoulins(maPos, etatDuJeu);
        if (moulins.isEmpty()) return Action.RIEN;

        int   masqueCible  = getMasqueProduction();
        Point cible        = null;
        int   longueurMin  = Integer.MAX_VALUE;

        for (Point p : moulins) {
            int     contenu    = etatDuJeu.donneContenuCellule(p.x, p.y);
            boolean dejaPossede = (contenu & masqueCible) != 0;
            if (dejaPossede) continue;

            ArrayList<Noeud> chemin = etatDuJeu.donneCheminEntre(maPos, p);
            if (chemin != null && chemin.size() < longueurMin) {
                longueurMin = chemin.size();
                cible = p;
            }
        }

        if (cible == null) return Action.RIEN;
        return routeManager.cheminVersAction(maPos, etatDuJeu.donneCheminEntre(maPos, cible));
    }

    /**
     * Déclenche une recharge préventive si une oliveraie est proche et qu'aucun
     * moulin possédé ne se trouve dans {@link #RAYON_MOULIN_PROCHE} cases.
     *
     * @param maPos     position actuelle du joueur
     * @param etatDuJeu état courant du plateau
     * @return action vers l'oliveraie, ou {@code RIEN} si la recharge n'est pas requise
     */
    private Action rechargePreventiveSiNecessaire(Point maPos, Plateau etatDuJeu) {
        Point oliveraieProche = routeManager.trouverOliveraieProche(
                maPos, etatDuJeu, RAYON_OLIVERAIE_PROCHE);

        if (oliveraieProche == null) return Action.RIEN;
        if (moulinPossedeProcheExiste(maPos, etatDuJeu)) return Action.RIEN;

        return routeManager.cheminVersAction(
                maPos, etatDuJeu.donneCheminEntre(maPos, oliveraieProche));
    }

    /**
     * Se dirige vers le moulin non possédé dont le score de rentabilité est maximal.
     *
     * <p>Formule : {@code score = max(0, toursRestants - dist) / (dist + 1)}</p>
     *
     * @param maPos         position actuelle du joueur
     * @param toursRestants nombre de tours restants dans la partie
     * @param etatDuJeu     état courant du plateau
     * @return action vers le moulin cible, ou {@code RIEN}
     */
    private Action allerVersMoulinLePlusRentable(Point maPos,
                                                 int toursRestants,
                                                 Plateau etatDuJeu) {
        ArrayList<Point> moulins = routeManager.donneMoulins(maPos, etatDuJeu);
        if (moulins.isEmpty()) return Action.RIEN;

        Point  cible         = null;
        double meilleurScore = Double.NEGATIVE_INFINITY;

        for (Point p : moulins) {
            if (routeManager.estAMoi(etatDuJeu, p, this)) continue;

            ArrayList<Noeud> chemin = etatDuJeu.donneCheminEntre(maPos, p);
            if (chemin == null || chemin.isEmpty()) continue;

            double score = calculerScore(chemin.size(), toursRestants);
            if (score > meilleurScore) {
                meilleurScore = score;
                cible = p;
            }
        }

        if (cible == null) return Action.RIEN;
        return routeManager.cheminVersAction(maPos, etatDuJeu.donneCheminEntre(maPos, cible));
    }

    // -------------------------------------------------------------------------
    // Méthodes privées utilitaires
    // -------------------------------------------------------------------------

    /**
     * Retourne {@code true} si au moins un moulin possédé par ce joueur se trouve
     * à moins de {@link #RAYON_MOULIN_PROCHE} cases (distance euclidienne).
     */
    private boolean moulinPossedeProcheExiste(Point maPos, Plateau etatDuJeu) {
        for (Point p : routeManager.donneMoulins(maPos, etatDuJeu)) {
            if (routeManager.estAMoi(etatDuJeu, p, this)
                    && maPos.distance(p) <= RAYON_MOULIN_PROCHE) {
                return true;
            }
        }
        return false;
    }

    /**
     * Calcule le score de rentabilité d'un moulin.
     *
     * @param distanceChemin nombre de pas pour atteindre le moulin
     * @param toursRestants  nombre de tours restants dans la partie
     * @return score de rentabilité
     */
    private double calculerScore(int distanceChemin, int toursRestants) {
        int bouteillesGagnees = Math.max(0, toursRestants - distanceChemin);
        return (double) bouteillesGagnees / (distanceChemin + 1);
    }

    /**
     * Retourne le masque binaire correspondant aux moulins déjà possédés
     * par ce joueur.
     */
    private int getMasqueProduction() {
        int[] masquesParRang = {
                ENDROIT_PRODUCTION_J1,
                ENDROIT_PRODUCTION_J2,
                ENDROIT_PRODUCTION_J3,
                ENDROIT_PRODUCTION_J4
        };
        return masquesParRang[this.donneRang()];
    }

}