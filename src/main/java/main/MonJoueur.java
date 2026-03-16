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

    private Point dernierePosition = null; // Derniere position observee.
    private int toursBloque = 0; // Nombre de tours consecutifs sans mouvement.

    private static final int ENERGIE_CRITIQUE = 30; // Seuil critique pour prioriser une oliveraie.
    private static final int ENERGIE_BASSE = 30; // Seuil bas pour booster les oliveraies proches.
    private static final int ENERGIE_MAX = 100; // Energie maximale possible.
    private static final int SCORE_PRODUCTION_LIBRE = 1000; // Score de base pour un moulin libre.
    private static final int SCORE_PRODUCTION_ADVERSE = 850; // Score de base pour un moulin adverse.
    private static final int SCORE_RESSOURCE_CRITIQUE = 950; // Score de base pour une oliveraie en urgence.
    private static final int PENALITE_DISTANCE = 3; // Penalite distance en phase normale.
    private static final int PENALITE_DISTANCE_OUVERTURE = 2; // Penalite distance en ouverture.
    private static final int PENALITE_FIN_DE_PARTIE = 10; // Penalite si cible trop loin en fin de partie.
    private static final int PENALITE_RISQUE_CIBLE = 200; // Penalite par voisin dangereux autour de la cible.
    private static final int PENALITE_RISQUE_CIBLE_OUVERTURE = 120; // Penalite risque reduite en ouverture.
    private static final int BONUS_RATTRAPAGE = 150; // Bonus si on est en retard pour voler un moulin.
    private static final int SEUIL_RETARD = 3; // Ecart de points definissant un retard.
    private static final int SEUIL_AVANCE = 3; // Ecart de points definissant une avance.
    private static final int BONUS_AVANCE = 120; // Bonus si on arrive avant les autres.
    private static final int PENALITE_CONTESTE = 200; // Penalite si un adversaire est nettement plus proche.
    private static final int MARGE_AVANCE = 2; // Marge de distance pour statuer "plus proche".
    private static final int TOURS_OUVERTURE = 200; // Duree de la phase offensive initiale.
    private static final int BONUS_OUVERTURE_MOULIN = 250; // Bonus moulin durant l'ouverture.
    private static final int BONUS_CENTRE_OUVERTURE = 60; // Bonus de centralite si carte symetrique.
    private static final int PENALITE_CENTRE_OUVERTURE = 2; // Penalite par case au centre en ouverture.
    private static final int RAYON_CLUSTER = 2; // Rayon pour detecter un cluster de moulins.
    private static final int BONUS_CLUSTER_MOULIN = 30; // Bonus par moulin voisin dans un cluster.
    private static final int BONUS_PRUDENCE = 120; // Bonus de prudence quand on est en avance.
    private static final int SEUIL_MANILLE_URGENCE = 5; // Energie critique pour chercher une manille.
    private static final int BONUS_OLIVERAIE_URGENCE = 300; // Bonus si energie tres basse.
    private static final int MALUS_COLLISION = 1000; // Malus fort pour eviter collisions.
    private static final int MALUS_MANILLE = 25; // Malus de base pour eviter une manille.
    private static final int MALUS_MANILLE_OUVERTURE = 15; // Malus manille reduit en ouverture.
    private static final int MALUS_MANILLE_PAR_MOULIN = 8; // Malus additionnel par moulin possede.

    private static final Action[] ACTIONS = { // Liste des actions possibles.
            Action.HAUT, Action.BAS, Action.GAUCHE, Action.DROITE, Action.RIEN
    };
    private static final int[] DX = {0, 0, -1, 1, 0}; // Deplacements en X, alignes sur ACTIONS.
    private static final int[] DY = {-1, 1, 0, 0, 0}; // Deplacements en Y, alignes sur ACTIONS.

    public MonJoueur(String sonNom) {
        super(sonNom);
    }

    @Override
    public Action faitUneAction(Plateau etatDuJeu) {
        try {
            if (etatDuJeu == null) {
                return Action.RIEN;
            }

            Point position = donnePosition(); // Position actuelle du joueur.
            int selfX = position.x; // X actuel.
            int selfY = position.y; // Y actuel.
            if (dernierePosition != null
                    && dernierePosition.x == selfX
                    && dernierePosition.y == selfY) {
                toursBloque++; // Compte les tours sans mouvement.
            } else {
                toursBloque = 0; // Reset si on a bouge.
                dernierePosition = new Point(selfX, selfY); // Met a jour la position.
            }
            int energie = donneRessources(); // Energie actuelle.
            int contenuIci = etatDuJeu.donneContenuCelluleSansJoueur(selfX, selfY); // Contenu de la case actuelle.
            if (Plateau.contientUneUniteDeRessourcage(contenuIci) && energie < ENERGIE_MAX) {
                return Action.RIEN; // on reste pour recharger quand on est sur une oliveraie
            }

            if (energie <= SEUIL_MANILLE_URGENCE) {
                long cibleManille = trouverCibleManille(etatDuJeu, selfX, selfY);
                if (cibleManille != Long.MIN_VALUE) {
                    int cibleXManille = (int) (cibleManille >> 32);
                    int cibleYManille = (int) cibleManille;
                    Action actionManille = choisirMeilleurMouvement(etatDuJeu, selfX, selfY, cibleXManille, cibleYManille);
                    if (actionManille != Action.RIEN) {
                        return actionManille;
                    }
                }
            }

            long cible = choisirMeilleureCible(etatDuJeu, selfX, selfY, energie); // Cible encodee.
            if (cible == Long.MIN_VALUE) {
                return choisirMouvementExploration(etatDuJeu, selfX, selfY);
            }
            int cibleX = (int) (cible >> 32); // X cible.
            int cibleY = (int) cible; // Y cible.

            Action action = choisirMeilleurMouvement(etatDuJeu, selfX, selfY, cibleX, cibleY);
            if (toursBloque >= 3) {
                Action deblocage = choisirMouvementDeblocage(etatDuJeu, selfX, selfY);
                if (deblocage != Action.RIEN) {
                    return deblocage;
                }
            }
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
        int taille = plateau.donneTaille(); // Taille du plateau.
        int meilleurScore = Integer.MIN_VALUE; // Meilleur score trouve.
        int meilleureDistance = Integer.MAX_VALUE; // Distance associee au meilleur score.
        int meilleurX = -1; // X de la meilleure cible.
        int meilleurY = -1; // Y de la meilleure cible.
        Joueur[] joueurs = plateau.donneJoueurs(); // Liste des joueurs.
        int[] toursRestantEchange = plateau.donneToursRestantEchange(); // Cooldown/manille par joueur.
        int selfRang = donneRang(); // Rang du joueur.
        int toursRestants = plateau.donneNombreDeTours() - plateau.donneTourCourant(); // Tours restants.
        int myPoints = donnePoints(); // Points du joueur.
        int bestPoints = myPoints; // Meilleur score observe.
        int bestOtherPoints = Integer.MIN_VALUE; // Meilleur score d'un adversaire.
        if (joueurs != null) {
            for (Joueur joueur : joueurs) {
                if (joueur == null) {
                    continue;
                }
                bestPoints = Math.max(bestPoints, joueur.donnePoints());
                if (joueur.donneRang() != selfRang) {
                    bestOtherPoints = Math.max(bestOtherPoints, joueur.donnePoints());
                }
            }
        }
        if (bestOtherPoints == Integer.MIN_VALUE) {
            bestOtherPoints = myPoints;
        }
        boolean enRetard = (bestPoints - myPoints) >= SEUIL_RETARD; // Vrai si on est en retard.
        boolean enAvance = (myPoints - bestOtherPoints) >= SEUIL_AVANCE; // Vrai si on est en avance.
        boolean phaseOffensive = plateau.donneTourCourant() < TOURS_OUVERTURE; // Phase d'ouverture.
        int penaliteDistance = phaseOffensive ? PENALITE_DISTANCE_OUVERTURE : PENALITE_DISTANCE; // Penalite distance.
        if (energie < ENERGIE_CRITIQUE) {
            penaliteDistance += Math.max(0, (ENERGIE_CRITIQUE - energie) / 5); // Penalite accrue si energie basse.
        }
        int penaliteRisque = phaseOffensive ? PENALITE_RISQUE_CIBLE_OUVERTURE : PENALITE_RISQUE_CIBLE; // Penalite risque.
        if (!phaseOffensive && enAvance) {
            penaliteRisque += BONUS_PRUDENCE;
        }
        boolean carteSymetrique = phaseOffensive && estCarteSymetrique(plateau); // Symetrie du terrain.
        int centre = taille / 2; // Indice du centre.

        for (int y = 0; y < taille; y++) {
            for (int x = 0; x < taille; x++) {
                int contenu = plateau.donneContenuCellule(x, y);
                if (Plateau.contientUnJoueur(contenu) && !(x == selfX && y == selfY)) {
                    continue; // on évite les autres joueurs, mais on autorise la case courante
                }
                int contenuSansJoueur = plateau.donneContenuCelluleSansJoueur(x, y);

                int base = 0;
                boolean adversaire = false;
                boolean estMoulin = false;
                boolean candidat = false;
                if (Plateau.contientUneUniteDeProductionLibre(contenuSansJoueur)) {
                    base = SCORE_PRODUCTION_LIBRE;
                    candidat = true;
                    estMoulin = true;
                } else if (Plateau.contientUneUniteDeProductionQuiNeLuiAppartientPas(this, contenuSansJoueur)) {
                    base = SCORE_PRODUCTION_ADVERSE;
                    candidat = true;
                    adversaire = true;
                    estMoulin = true;
                } else if (Plateau.contientUneUniteDeRessourcage(contenuSansJoueur)) {
                    if (energie <= ENERGIE_CRITIQUE) {
                        base = SCORE_RESSOURCE_CRITIQUE;
                        candidat = true;
                        if (energie <= SEUIL_MANILLE_URGENCE) {
                            base += BONUS_OLIVERAIE_URGENCE;
                        }
                    } else {
                        continue; // on ignore les oliveraies si l'energie est suffisante
                    }
                }

                if (!candidat) {
                    continue;
                }

                int distance = distanceParChemin(plateau, selfX, selfY, x, y);
                if (distance < 0) {
                    continue;
                }
                int score = base - (penaliteDistance * distance);
                if (phaseOffensive && estMoulin) {
                    score += BONUS_OUVERTURE_MOULIN;
                    int centralite = manhattan(x, y, centre, centre);
                    if (carteSymetrique) {
                        score += Math.max(0, BONUS_CENTRE_OUVERTURE - (centralite * PENALITE_CENTRE_OUVERTURE));
                    }
                    int cluster = compteMoulinsAutour(plateau, x, y, RAYON_CLUSTER);
                    score += cluster * BONUS_CLUSTER_MOULIN;
                }
                if (enRetard && adversaire) {
                    score += BONUS_RATTRAPAGE;
                }
                if (estMoulin) {
                    int nearestOppDist = Integer.MAX_VALUE;
                    if (joueurs != null) {
                        for (Joueur joueur : joueurs) {
                            if (joueur == null) {
                                continue;
                            }
                            if (joueur.donneRang() == selfRang) {
                                continue;
                            }
                            Point pos = joueur.donnePosition();
                            if (!plateau.coordonneeValide(pos.x, pos.y)) {
                                continue;
                            }
                            int d = manhattan(pos.x, pos.y, x, y);
                            int rang = joueur.donneRang();
                            if (toursRestantEchange != null && rang >= 0 && rang < toursRestantEchange.length) {
                                int etat = toursRestantEchange[rang];
                                if (etat > 0) {
                                    d += etat; // joueur bloque par la manille
                                }
                            }
                            nearestOppDist = Math.min(nearestOppDist, d);
                        }
                    }
                    if (nearestOppDist != Integer.MAX_VALUE) {
                        if (distance + MARGE_AVANCE < nearestOppDist) {
                            score += BONUS_AVANCE;
                        } else if (nearestOppDist + MARGE_AVANCE < distance) {
                            score -= PENALITE_CONTESTE;
                        }
                    }
                }
                int dangerAdj = compteAdjacentsDangereux(plateau, joueurs, toursRestantEchange, x, y, selfRang);
                if (dangerAdj > 0) {
                    score -= dangerAdj * penaliteRisque;
                }
                if (toursRestants > 0 && distance > toursRestants) {
                    score -= (distance - toursRestants) * PENALITE_FIN_DE_PARTIE;
                }
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
        List<Noeud> obstacles = new ArrayList<>(); // Obstacles supplementaires (joueurs).
        Joueur[] joueurs = plateau.donneJoueurs(); // Liste des joueurs.
        int selfRang = donneRang(); // Rang du joueur.
        if (joueurs != null) {
            for (Joueur joueur : joueurs) {
                if (joueur == null) {
                    continue;
                }
                if (joueur.donneRang() == selfRang) {
                    continue;
                }
                Point pos = joueur.donnePosition(); // Position d'un joueur adverse.
                obstacles.add(new Noeud(pos.x, pos.y));
            }
        }

        ArrayList<Noeud> chemin = plateau.donneCheminAvecObstaclesSupplementaires(
                new Point(selfX, selfY),
                new Point(cibleX, cibleY),
                obstacles
        ); // Chemin A* en evitant les obstacles.
        if (chemin == null || chemin.isEmpty()) {
            return null;
        }

        int index = 0; // Index du prochain noeud.
        if (chemin.get(0).getX() == selfX && chemin.get(0).getY() == selfY) {
            index = 1;
        }
        if (index >= chemin.size()) {
            return Action.RIEN;
        }

        Noeud next = chemin.get(index); // Prochaine etape.
        return actionPourDeplacement(selfX, selfY, next.getX(), next.getY());
    }

    private Action choisirMeilleurMouvementGreedy(Plateau plateau, int selfX, int selfY, int cibleX, int cibleY) {
        int meilleurScore = Integer.MIN_VALUE; // Meilleur score d'action.
        Action meilleureAction = Action.RIEN; // Action retenue.
        Joueur[] joueurs = plateau.donneJoueurs(); // Liste des joueurs.
        int selfRang = donneRang(); // Rang du joueur.
        int[] toursRestantEchange = plateau.donneToursRestantEchange(); // Cooldown/manille par joueur.
        int nbMoulins = plateau.nombreDUnitesDeProductionJoueur(selfRang); // Moulins possedes.
        boolean phaseOffensive = plateau.donneTourCourant() < TOURS_OUVERTURE; // Phase d'ouverture.
        int bestOtherPoints = Integer.MIN_VALUE; // Meilleur score adverse.
        if (joueurs != null) {
            for (Joueur joueur : joueurs) {
                if (joueur == null || joueur.donneRang() == selfRang) {
                    continue;
                }
                bestOtherPoints = Math.max(bestOtherPoints, joueur.donnePoints());
            }
        }
        if (bestOtherPoints == Integer.MIN_VALUE) {
            bestOtherPoints = donnePoints();
        }
        boolean enAvance = (donnePoints() - bestOtherPoints) >= SEUIL_AVANCE;
        int malusBase = phaseOffensive ? MALUS_MANILLE_OUVERTURE : MALUS_MANILLE;
        if (!phaseOffensive && enAvance) {
            malusBase += (BONUS_PRUDENCE / 3);
        }
        int malusManilleDynamique = malusBase + (nbMoulins * MALUS_MANILLE_PAR_MOULIN);

        for (int i = 0; i < ACTIONS.length; i++) {
            Action action = ACTIONS[i]; // Action candidate.
            int destX = selfX + DX[i]; // X destination.
            int destY = selfY + DY[i]; // Y destination.

            int score = -manhattan(destX, destY, cibleX, cibleY);
            boolean collision = false;

            if (!plateau.coordonneeValide(destX, destY)) {
                collision = true;
            } else {
                int contenu = plateau.donneContenuCellule(destX, destY); // Contenu destination.
                if (Plateau.contientUneZoneInfranchissable(contenu)) {
                    collision = true;
                } else {
                    Joueur joueurEnFace = plateau.donneJoueurEnPosition(destX, destY); // Joueur en face.
                    if (joueurEnFace != null && joueurEnFace.donneRang() != selfRang) {
                        collision = true;
                    }
                }
            }

            if (collision) {
                score -= MALUS_COLLISION;
            } else if (estAdjacentAUnJoueurDangereux(plateau, joueurs, toursRestantEchange, destX, destY, selfRang)) {
                score -= malusManilleDynamique;
            }

            if (score > meilleurScore) {
                meilleurScore = score;
                meilleureAction = action;
            }
        }

        return meilleureAction;
    }

    private long trouverCibleManille(Plateau plateau, int selfX, int selfY) {
        Joueur[] joueurs = plateau.donneJoueurs(); // Liste des joueurs.
        int[] toursRestantEchange = plateau.donneToursRestantEchange(); // Cooldown/manille par joueur.
        if (joueurs == null) {
            return Long.MIN_VALUE;
        }
        int meilleurDistance = Integer.MAX_VALUE;
        int meilleurX = -1;
        int meilleurY = -1;
        for (Joueur joueur : joueurs) {
            if (joueur == null || joueur.donneRang() == donneRang()) {
                continue;
            }
            if (!joueurPeutLancerManille(plateau, joueur, toursRestantEchange)) {
                continue;
            }
            Point pos = joueur.donnePosition(); // Position de l'adversaire.
            for (int i = 0; i < 4; i++) {
                int tx = pos.x + DX[i];
                int ty = pos.y + DY[i];
                if (!plateau.coordonneeValide(tx, ty)) {
                    continue;
                }
                int contenu = plateau.donneContenuCellule(tx, ty);
                if (Plateau.contientUneZoneInfranchissable(contenu)) {
                    continue;
                }
                Joueur occupe = plateau.donneJoueurEnPosition(tx, ty);
                if (occupe != null) {
                    continue;
                }
                int distance = distanceParChemin(plateau, selfX, selfY, tx, ty);
                if (distance >= 0 && distance < meilleurDistance) {
                    meilleurDistance = distance;
                    meilleurX = tx;
                    meilleurY = ty;
                }
            }
        }
        if (meilleurX < 0) {
            return Long.MIN_VALUE;
        }
        return (((long) meilleurX) << 32) | (meilleurY & 0xffffffffL);
    }

    private Action choisirMouvementExploration(Plateau plateau, int selfX, int selfY) {
        for (int i = 0; i < ACTIONS.length; i++) {
            Action action = ACTIONS[i]; // Action candidate.
            int destX = selfX + DX[i]; // X destination.
            int destY = selfY + DY[i]; // Y destination.
            if (!plateau.coordonneeValide(destX, destY)) {
                continue;
            }
            int contenu = plateau.donneContenuCellule(destX, destY); // Contenu destination.
            if (Plateau.contientUneZoneInfranchissable(contenu)) {
                continue;
            }
            Joueur joueurEnFace = plateau.donneJoueurEnPosition(destX, destY); // Joueur en face.
            if (joueurEnFace != null && joueurEnFace.donneRang() != donneRang()) {
                continue;
            }
            return action;
        }
        return Action.RIEN;
    }

    private Action choisirMouvementDeblocage(Plateau plateau, int selfX, int selfY) {
        for (int i = 0; i < ACTIONS.length; i++) {
            Action action = ACTIONS[i]; // Action candidate.
            int destX = selfX + DX[i]; // X destination.
            int destY = selfY + DY[i]; // Y destination.
            if (!plateau.coordonneeValide(destX, destY)) {
                continue;
            }
            int contenu = plateau.donneContenuCellule(destX, destY); // Contenu destination.
            if (Plateau.contientUneZoneInfranchissable(contenu)) {
                continue;
            }
            Joueur joueurEnFace = plateau.donneJoueurEnPosition(destX, destY); // Joueur en face.
            if (joueurEnFace != null && joueurEnFace.donneRang() != donneRang()) {
                continue;
            }
            return action; // Autorise l'adjacence pour sortir d'un blocage.
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
        ArrayList<Noeud> chemin = plateau.donneCheminEntre(new Point(selfX, selfY), new Point(x, y)); // Chemin A*.
        if (chemin == null || chemin.isEmpty()) {
            return -1;
        }
        int size = chemin.size(); // Taille du chemin.
        if (chemin.get(0).getX() == selfX && chemin.get(0).getY() == selfY) {
            return Math.max(0, size - 1);
        }
        return size;
    }

    private static int manhattan(int x1, int y1, int x2, int y2) {
        return Math.abs(x1 - x2) + Math.abs(y1 - y2);
    }

    private static int compteAdjacentsDangereux(Plateau plateau, Joueur[] joueurs, int[] toursRestantEchange,
                                                int x, int y, int selfRang) {
        int count = 0; // Nombre de voisins dangereux.
        if (joueurs == null) {
            return 0;
        }
        for (Joueur joueur : joueurs) {
            if (joueur == null) {
                continue;
            }
            if (joueur.donneRang() == selfRang) {
                continue;
            }
            Point pos = joueur.donnePosition();
            if (manhattan(pos.x, pos.y, x, y) == 1
                    && joueurPeutLancerManille(plateau, joueur, toursRestantEchange)) {
                count++;
            }
        }
        return count;
    }

    private static boolean estAdjacentAUnJoueurDangereux(Plateau plateau, Joueur[] joueurs, int[] toursRestantEchange,
                                                         int x, int y, int selfRang) {
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
            if (manhattan(pos.x, pos.y, x, y) == 1
                    && joueurPeutLancerManille(plateau, joueur, toursRestantEchange)) {
                return true;
            }
        }
        return false;
    }

    private static boolean joueurPeutLancerManille(Plateau plateau, Joueur joueur, int[] toursRestantEchange) {
        if (joueur == null) {
            return false;
        }
        int rang = joueur.donneRang(); // Rang du joueur.
        int etat = 0; // Etat manille/cooldown.
        if (toursRestantEchange != null && rang >= 0 && rang < toursRestantEchange.length) {
            etat = toursRestantEchange[rang];
        }
        if (etat != 0) {
            return false;
        }
        Point pos = joueur.donnePosition(); // Position du joueur.
        if (!plateau.coordonneeValide(pos.x, pos.y)) {
            return false;
        }
        int contenuSansJoueur = plateau.donneContenuCelluleSansJoueur(pos.x, pos.y); // Contenu de la case du joueur.
        return !Plateau.contientUneUniteDeRessourcage(contenuSansJoueur);
    }

    private static int compteMoulinsAutour(Plateau plateau, int x, int y, int rayon) {
        int taille = plateau.donneTaille(); // Taille du plateau.
        int count = 0; // Nombre de moulins dans le rayon.
        for (int dy = -rayon; dy <= rayon; dy++) {
            int yy = y + dy;
            if (yy < 0 || yy >= taille) {
                continue;
            }
            for (int dx = -rayon; dx <= rayon; dx++) {
                int xx = x + dx;
                if (xx < 0 || xx >= taille) {
                    continue;
                }
                if (Math.abs(dx) + Math.abs(dy) > rayon) {
                    continue;
                }
                int contenu = plateau.donneContenuCelluleSansJoueur(xx, yy);
                if (Plateau.donneUtilisateurDeLUniteDeProduction(contenu) >= 0) {
                    count++;
                }
            }
        }
        return count;
    }

    private static boolean estCarteSymetrique(Plateau plateau) {
        int taille = plateau.donneTaille(); // Taille du plateau.
        boolean symLR = true; // Symetrie gauche-droite.
        boolean symUD = true; // Symetrie haut-bas.
        boolean symRot = true; // Symetrie rotation 180.
        for (int y = 0; y < taille; y++) {
            for (int x = 0; x < taille; x++) {
                int type = typeCelluleNormalisee(plateau, x, y); // Type normalise.
                if (symLR && type != typeCelluleNormalisee(plateau, taille - 1 - x, y)) {
                    symLR = false;
                }
                if (symUD && type != typeCelluleNormalisee(plateau, x, taille - 1 - y)) {
                    symUD = false;
                }
                if (symRot && type != typeCelluleNormalisee(plateau, taille - 1 - x, taille - 1 - y)) {
                    symRot = false;
                }
                if (!symLR && !symUD && !symRot) {
                    return false;
                }
            }
        }
        return symLR || symUD || symRot;
    }

    private static int typeCelluleNormalisee(Plateau plateau, int x, int y) {
        int contenu = plateau.donneContenuCelluleSansJoueur(x, y); // Contenu normalise de la case.
        if (Plateau.contientUneZoneInfranchissable(contenu)) {
            return 1;
        }
        if (Plateau.contientUneUniteDeRessourcage(contenu)) {
            return 2;
        }
        if (Plateau.donneUtilisateurDeLUniteDeProduction(contenu) >= 0) {
            return 3;
        }
        return 0;
    }

    private void log(String message) {
        // Intentionnellement vide : remplacer si un système de log est disponible.
    }
}