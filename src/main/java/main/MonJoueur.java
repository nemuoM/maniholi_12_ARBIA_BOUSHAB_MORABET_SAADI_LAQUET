package main;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import jeu.Joueur;
import jeu.Plateau;
import jeu.aetoile.Noeud;

/**
 * Le joueur de l'Ã©quipe ???.
 *
 * Un joueur dont la stratÃ©gie de jeu est dÃ©finie par
 * {@link #faitUneAction(Plateau) }, Ã  tester dans le {@link Lanceur} du jeu.
 *
 * @author ???
 */
public class MonJoueur extends Joueur {

    private static final int ENERGIE_CRITIQUE = 30;
    private static final int ENERGIE_BASSE = 50;
    private static final int SCORE_PRODUCTION_LIBRE = 1000;
    private static final int SCORE_PRODUCTION_ADVERSE = 850;
    private static final int SCORE_RESSOURCE_CRITIQUE = 950;
    private static final int SCORE_RESSOURCE_NORMAL = 350;
    private static final int PENALITE_DISTANCE = 3;
    private static final int MALUS_COLLISION = 1000;
    private static final int MALUS_MANILLE = 25;

    private static final Action[] ACTIONS = {
            Action.HAUT, Action.BAS, Action.GAUCHE, Action.DROITE, Action.RIEN
    };
    private static final int[] DX = {0, 0, -1, 1, 0};
    private static final int[] DY = {-1, 1, 0, 0, 0};

    public MonJoueur(String sonNom) {
        super(sonNom);
    }

    @Override
    public Action faitUneAction(Plateau etatDuJeu) {
        try {
            if (etatDuJeu == null) {
                return Action.RIEN;
            }

            Point position = donnePosition();
            int selfX = position.x;
            int selfY = position.y;
            int energie = donneRessources();

            long cible = choisirMeilleureCible(etatDuJeu, selfX, selfY, energie);
            if (cible == Long.MIN_VALUE) {
                return choisirMouvementExploration(etatDuJeu, selfX, selfY);
            }
            int cibleX = (int) (cible >> 32);
            int cibleY = (int) cible;

            Action action = choisirMeilleurMouvement(etatDuJeu, selfX, selfY, cibleX, cibleY);
            if (action == Action.RIEN) {
                return choisirMouvementExploration(etatDuJeu, selfX, selfY);
            }
            return action;
        } catch (Exception e) {
            log("Erreur dans faitUneAction: " + e.getMessage());
            return Action.RIEN;
        }
    }

    private long choisirMeilleureCible(Plateau plateau, int selfX, int selfY, int energie) {
        int taille = plateau.donneTaille();
        int meilleurScore = Integer.MIN_VALUE;
        int meilleureDistance = Integer.MAX_VALUE;
        int meilleurX = -1;
        int meilleurY = -1;

        for (int y = 0; y < taille; y++) {
            for (int x = 0; x < taille; x++) {
                int contenu = plateau.donneContenuCellule(x, y);
                if (Plateau.contientUnJoueur(contenu) && !(x == selfX && y == selfY)) {
                    continue; // on évite les autres joueurs, mais on autorise la case courante
                }
                int contenuSansJoueur = plateau.donneContenuCelluleSansJoueur(x, y);

                int base = 0;
                boolean candidat = false;
                if (Plateau.contientUneUniteDeProductionLibre(contenuSansJoueur)) {
                    base = SCORE_PRODUCTION_LIBRE;
                    candidat = true;
                } else if (Plateau.contientUneUniteDeProductionQuiNeLuiAppartientPas(this, contenuSansJoueur)) {
                    base = SCORE_PRODUCTION_ADVERSE;
                    candidat = true;
                } else if (Plateau.contientUneUniteDeRessourcage(contenuSansJoueur)) {
                    base = (energie <= ENERGIE_CRITIQUE) ? SCORE_RESSOURCE_CRITIQUE : SCORE_RESSOURCE_NORMAL;
                    candidat = true;
                }

                if (!candidat) {
                    continue;
                }

                int distance = distanceParChemin(plateau, selfX, selfY, x, y);
                if (distance < 0) {
                    continue;
                }
                int score = base - (PENALITE_DISTANCE * distance);
                if (energie <= ENERGIE_BASSE && Plateau.contientUneUniteDeRessourcage(contenuSansJoueur)) {
                    score += 200;
                }
                if (score > meilleurScore || (score == meilleurScore && distance < meilleureDistance)) {
                    meilleurScore = score;
                    meilleureDistance = distance;
                    meilleurX = x;
                    meilleurY = y;
                }
            }
        }

        if (meilleurX < 0) {
            return Long.MIN_VALUE;
        }
        return (((long) meilleurX) << 32) | (meilleurY & 0xffffffffL);
    }

    private Action choisirMeilleurMouvement(Plateau plateau, int selfX, int selfY, int cibleX, int cibleY) {
        Action actionParChemin = choisirActionParChemin(plateau, selfX, selfY, cibleX, cibleY);
        if (actionParChemin != null) {
            return actionParChemin;
        }
        return choisirMeilleurMouvementGreedy(plateau, selfX, selfY, cibleX, cibleY);
    }

    private Action choisirActionParChemin(Plateau plateau, int selfX, int selfY, int cibleX, int cibleY) {
        List<Noeud> obstacles = new ArrayList<>();
        Joueur[] joueurs = plateau.donneJoueurs();
        int selfRang = donneRang();
        if (joueurs != null) {
            for (Joueur joueur : joueurs) {
                if (joueur == null) {
                    continue;
                }
                if (joueur.donneRang() == selfRang) {
                    continue;
                }
                Point pos = joueur.donnePosition();
                obstacles.add(new Noeud(pos.x, pos.y));
            }
        }

        ArrayList<Noeud> chemin = plateau.donneCheminAvecObstaclesSupplementaires(
                new Point(selfX, selfY),
                new Point(cibleX, cibleY),
                obstacles
        );
        if (chemin == null || chemin.isEmpty()) {
            return null;
        }

        int index = 0;
        if (chemin.get(0).getX() == selfX && chemin.get(0).getY() == selfY) {
            index = 1;
        }
        if (index >= chemin.size()) {
            return Action.RIEN;
        }

        Noeud next = chemin.get(index);
        return actionPourDeplacement(selfX, selfY, next.getX(), next.getY());
    }

    private Action choisirMeilleurMouvementGreedy(Plateau plateau, int selfX, int selfY, int cibleX, int cibleY) {
        int meilleurScore = Integer.MIN_VALUE;
        Action meilleureAction = Action.RIEN;
        Joueur[] joueurs = plateau.donneJoueurs();
        int selfRang = donneRang();

        for (int i = 0; i < ACTIONS.length; i++) {
            Action action = ACTIONS[i];
            int destX = selfX + DX[i];
            int destY = selfY + DY[i];

            int score = -manhattan(destX, destY, cibleX, cibleY);
            boolean collision = false;

            if (!plateau.coordonneeValide(destX, destY)) {
                collision = true;
            } else {
                int contenu = plateau.donneContenuCellule(destX, destY);
                if (Plateau.contientUneZoneInfranchissable(contenu)) {
                    collision = true;
                } else {
                    Joueur joueurEnFace = plateau.donneJoueurEnPosition(destX, destY);
                    if (joueurEnFace != null && joueurEnFace.donneRang() != selfRang) {
                        collision = true;
                    }
                }
            }

            if (collision) {
                score -= MALUS_COLLISION;
            } else if (estAdjacentAUnAutreJoueur(joueurs, destX, destY, selfRang)) {
                score -= MALUS_MANILLE;
            }

            if (score > meilleurScore) {
                meilleurScore = score;
                meilleureAction = action;
            }
        }

        return meilleureAction;
    }

    private Action choisirMouvementExploration(Plateau plateau, int selfX, int selfY) {
        for (int i = 0; i < ACTIONS.length; i++) {
            Action action = ACTIONS[i];
            int destX = selfX + DX[i];
            int destY = selfY + DY[i];
            if (!plateau.coordonneeValide(destX, destY)) {
                continue;
            }
            int contenu = plateau.donneContenuCellule(destX, destY);
            if (Plateau.contientUneZoneInfranchissable(contenu)) {
                continue;
            }
            Joueur joueurEnFace = plateau.donneJoueurEnPosition(destX, destY);
            if (joueurEnFace != null && joueurEnFace.donneRang() != donneRang()) {
                continue;
            }
            return action;
        }
        return Action.RIEN;
    }

    private static Action actionPourDeplacement(int selfX, int selfY, int destX, int destY) {
        int dx = destX - selfX;
        int dy = destY - selfY;
        if (dx == 1 && dy == 0) {
            return Action.DROITE;
        }
        if (dx == -1 && dy == 0) {
            return Action.GAUCHE;
        }
        if (dx == 0 && dy == 1) {
            return Action.BAS;
        }
        if (dx == 0 && dy == -1) {
            return Action.HAUT;
        }
        return Action.RIEN;
    }

    private static int distanceParChemin(Plateau plateau, int selfX, int selfY, int x, int y) {
        ArrayList<Noeud> chemin = plateau.donneCheminEntre(new Point(selfX, selfY), new Point(x, y));
        if (chemin == null || chemin.isEmpty()) {
            return -1;
        }
        int size = chemin.size();
        if (chemin.get(0).getX() == selfX && chemin.get(0).getY() == selfY) {
            return Math.max(0, size - 1);
        }
        return size;
    }

    private static int manhattan(int x1, int y1, int x2, int y2) {
        return Math.abs(x1 - x2) + Math.abs(y1 - y2);
    }

    private static boolean estAdjacentAUnAutreJoueur(Joueur[] joueurs, int x, int y, int selfRang) {
        if (joueurs == null) {
            return false;
        }
        for (Joueur joueur : joueurs) {
            if (joueur == null) {
                continue;
            }
            if (joueur.donneRang() == selfRang) {
                continue;
            }
            Point pos = joueur.donnePosition();
            if (manhattan(pos.x, pos.y, x, y) == 1) {
                return true;
            }
        }
        return false;
    }

    private void log(String message) {
        // Intentionnellement vide : remplacer si un système de log est disponible.
    }
}
