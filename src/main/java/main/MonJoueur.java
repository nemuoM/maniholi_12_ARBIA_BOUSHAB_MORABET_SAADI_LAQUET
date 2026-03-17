package main;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;
import java.util.List;

import jeu.Joueur;
import jeu.Joueur.Action;
import jeu.Plateau;
import jeu.aetoile.Noeud;

/**
 * Joueur Manihòli — Nouvelle stratégie.
 * 
 * Principes de la V2 :
 * - Utilisation des API natives de Plateau pour scanner et faire du pathfinding
 * - Calcul de distance réelle (BFS) au lieu de Manhattan
 * - Tracking du cycle des oliveraies
 * - Manille offensive ciblée
 */
public class MonJoueur extends Joueur {

    // --- État interne de la partie ---
    private Point dernierePosition = null;
    private int toursBloque = 0;
    
    // Tracking des Oliveraies
    // Clé: Point (x,y), Valeur: dernier tour où on y est passé/resté 
    // ou tout autre marqueur temporel pour déduire le rendement.
    // (Simplifié pour l'instant: on piste juste le nombre de tours de présence continue)
    private HashMap<Point, Integer> toursPasseDansOliveraie = new HashMap<>();
    
    // --- Constantes Stratégiques ---
    private static final int ENERGIE_CRITIQUE = 25;
    private static final int ENERGIE_MANILLE_URGENCE = 12;
    private static final int ENERGIE_MAX = 100;

    public MonJoueur(String nom) {
        super(nom);
    }

    @Override
    public void debutDePartie(int rang) {
        this.dernierePosition = null;
        this.toursBloque = 0;
        this.toursPasseDansOliveraie.clear();
    }

    @Override
    public Action faitUneAction(Plateau plateau) {
        if (plateau == null) return Action.RIEN;

        Point maPos = donnePosition();
        int monEnergie = donneRessources();
        int monRang = donneRang();

        // 1. Mise à jour de l'état de blocage
        if (dernierePosition != null && dernierePosition.equals(maPos)) {
            toursBloque++;
        } else {
            toursBloque = 0;
        }
        
        Point posPrec = dernierePosition;
        dernierePosition = new Point(maPos);

        // 2. Gestion si on est DANS une oliveraie
        int contenuIci = plateau.donneContenuCelluleSansJoueur(maPos.x, maPos.y);
        if (Plateau.contientUneUniteDeRessourcage(contenuIci)) {
            if (monEnergie < ENERGIE_MAX) {
                // Tracking (vrai gain serait 10, 20, 60, 20, 10, optionnel ici car on y reste)
                Integer recolt = toursPasseDansOliveraie.getOrDefault(maPos, 0);
                toursPasseDansOliveraie.put(maPos, recolt + 1);
                return Action.RIEN; // On récolte
            }
            // Plein, on sort au prochain tour (si pas de manille, on va chercher un moulin)
        } else {
            // Nettoyage de la mémoire de cette case si on en sort
            if (toursPasseDansOliveraie.containsKey(maPos)) {
                toursPasseDansOliveraie.remove(maPos);
            }
        }

        // 3. Identification des obstacles dynamiques (Joueurs dangereux)
        List<Noeud> obstaclesSupplementaires = trouverCasesDangereuses(plateau);

        // 4. Urgence extrême : On n'a presque plus d'énergie. 
        // -> Provoquer une manille ou foncer sur l'oliveraie la plus proche
        if (monEnergie <= ENERGIE_MANILLE_URGENCE) {
            Action urgenceManille = chercherManille(plateau, true);
            if (urgenceManille != Action.RIEN) return urgenceManille;
        }

        // 5. Cas de blocage persistant (ex: coincé par des joueurs statiques)
        if (toursBloque >= 4) {
            return tenterDeblocage(plateau);
        }

        // 6. Choix de la meilleure Cible globale via BFS des distances réelles
        Point cible = choisirMeilleureCible(plateau, monEnergie, obstaclesSupplementaires);

        // 7. Mouvement vers la cible avec l'A* de maniholi.jar
        if (cible != null) {
            // Pour ne pas inclure la position où on est si c'est déjà l'arrivée
            if (cible.equals(maPos)) return Action.RIEN; 
            
            ArrayList<Noeud> chemin = plateau.donneCheminAvecObstaclesSupplementaires(maPos, cible, obstaclesSupplementaires);
            
            if (chemin != null && !chemin.isEmpty()) {
                // Determine the correct next step by finding our position or the adjacent node
                // Maniholi's A* may return path [Start ... End] or [End ... Start]
                boolean cheminInverse = chemin.get(0).equals(new Noeud(cible.x, cible.y));
                
                int monIndex = -1;
                for (int i=0; i<chemin.size(); i++) {
                    if (chemin.get(i).getX() == maPos.x && chemin.get(i).getY() == maPos.y) {
                        monIndex = i; break;
                    }
                }
                
                Noeud prochainPas = null;
                if (monIndex != -1) {
                    if (cheminInverse && monIndex - 1 >= 0) prochainPas = chemin.get(monIndex - 1);
                    else if (!cheminInverse && monIndex + 1 < chemin.size()) prochainPas = chemin.get(monIndex + 1);
                } else {
                    // Si notre position n'est pas dedans (parfois l'A* l'omet)
                    if (cheminInverse) {
                        // Le chemin va de Fin à Début. On veut le noeud à la fin de la liste
                        prochainPas = chemin.get(chemin.size() - 1);
                    } else {
                        // Le chemin va de Début à Fin. On veut le noeud au début de la liste
                        prochainPas = chemin.get(0);
                    }
                }
                
                if (prochainPas != null && (Math.abs(prochainPas.getX() - maPos.x) + Math.abs(prochainPas.getY() - maPos.y) == 1)) {
                    Action a = convertirEnAction(maPos, new Point(prochainPas.getX(), prochainPas.getY()));
                    if (a != Action.RIEN) return a;
                }
            }
            // Si l'A* a échoué mais qu'on est adjacent à la cible, on y va
            if (Math.abs(cible.x - maPos.x) + Math.abs(cible.y - maPos.y) == 1) {
                Action a = convertirEnAction(maPos, cible);
                if (a != Action.RIEN) return a;
            }
        }
        
        // 8. Fallback : mouvement d'exploration sûr s'il n'y a pas de cible ou que l'A* a échoué
        return explorationSecurisee(plateau, obstaclesSupplementaires, posPrec);
    }

    /**
     * Enumère les cases adjacentes aux autres joueurs comme "obstacles dynamiques"
     * pour éviter d'entrer en manille accidentellement.
     */
    private List<Noeud> trouverCasesDangereuses(Plateau plateau) {
        List<Noeud> dangers = new ArrayList<>();
        Joueur[] joueurs = plateau.donneJoueurs();
        int[] cooldowns = plateau.donneToursRestantEchange();
        Point maPos = donnePosition();

        if (joueurs != null) {
            for (Joueur j : joueurs) {
                if (j == null || j.donneRang() == donneRang()) continue;
                
                int rg = j.donneRang();
                boolean peutManille = (cooldowns == null || cooldowns[rg] == 0);
                
                // Si l'adversaire est dans une oliveraie, il ne peut pas maniller
                int contenuAdv = plateau.donneContenuCelluleSansJoueur(j.donnePosition().x, j.donnePosition().y);
                if (Plateau.contientUneUniteDeRessourcage(contenuAdv)) peutManille = false;

                if (peutManille) {
                    Point p = j.donnePosition();
                    // On ajoute les 4 cases adjacentes
                    int[][] dirs = {{0,1},{0,-1},{1,0},{-1,0}};
                    for(int[] d : dirs) {
                        int nx = p.x + d[0];
                        int ny = p.y + d[1];
                        if (plateau.coordonneeValide(nx, ny) && !(nx == maPos.x && ny == maPos.y)) {
                            dangers.add(new Noeud(nx, ny)); 
                        }
                    }
                    // On n'ajoute PLUS la case du joueur lui-même comme infranchissable,
                    // car cela bloque l'algorithme d'exploration (il se considère sur un danger).
                    // dangers.add(new Noeud(p.x, p.y));
                }
            }
        }
        return dangers;
    }

    /**
     * BFS (Breadth-First Search) pour trouver la vraie cible la plus intéressante
     * en tenant compte des vraies distances (murs) et obstacles.
     */
    private Point choisirMeilleureCible(Plateau plateau, int monEnergie, List<Noeud> obstaclesSupplementaires) {
        int taille = plateau.donneTaille();
        Point depart = donnePosition();
        
        boolean[][] visite = new boolean[taille][taille];
        // Ne MARQUONS PAS les obstacles comme visités s'ils bloquent TOUS les chemins. 
        // L'A* s'occupera d'ajouter le surcoût. On veut juste trouver LA cible la plus proche.
        
        Queue<PointDist> file = new LinkedList<>();
        file.add(new PointDist(depart, 0));
        visite[depart.x][depart.y] = true;

        Point meilleureCible = null;
        double meilleurScore = -999999;
        
        // Critères globaux
        int nbToursRestants = plateau.donneNombreDeTours() - plateau.donneTourCourant();
        boolean rechercheUrgenteOliveraie = (monEnergie <= ENERGIE_CRITIQUE);

        while (!file.isEmpty()) {
            PointDist cur = file.poll();
            Point p = cur.p;
            int dist = cur.dist;
            
            // Ignorer si trop loin pour le temps restant
            if (dist > nbToursRestants && cur.dist > 0) continue;

            int contenuCible = plateau.donneContenuCellule(p.x, p.y);
            double score = -999999;
            
            // Calcul du temps minimum pour un adversaire pour atteindre cette case
            int tempsAdversaire = Integer.MAX_VALUE;
            Joueur[] joueurs = plateau.donneJoueurs();
            if (joueurs != null) {
                for (Joueur j : joueurs) {
                    if (j != null && j.donneRang() != donneRang()) {
                        int distAdv = distanceManhattan(j.donnePosition(), p);
                        if (distAdv < tempsAdversaire) {
                            tempsAdversaire = distAdv;
                        }
                    }
                }
            }
            
            // Un moulin coûte 20 d'énergie à capturer. dist coûte 1 / case. + 2 marge de survie.
            int coutEnergieRequis = dist + 22;
            
            if (Plateau.contientUneUniteDeProductionLibre(contenuCible)) {
                if (monEnergie < coutEnergieRequis) continue; // Pas assez d'énergie
                
                if (!rechercheUrgenteOliveraie) {
                    score = 1000.0 - dist * 5.0; 
                    if (tempsAdversaire < dist) {
                        score -= 250.0; // Trop risqué, un adversaire y sera avant !
                    } else if (tempsAdversaire == dist) {
                        score -= 50.0; // Ex aequo
                    } else {
                        score += 150.0; // Voie libre
                    }
                } else {
                    score = 200.0 - dist * 10.0; 
                }
            }
            else if (Plateau.contientUneUniteDeProductionQuiNeLuiAppartientPas(this, contenuCible)) {
                if (monEnergie < coutEnergieRequis) continue; // Pas assez d'énergie
                
                // Moulin adverse : très lucratif car on prive l'adversaire de ses revenus (+2 swing)
                // MAIS ne doit pas être préféré à un moulin libre juste à côté !
                if (!rechercheUrgenteOliveraie) {
                    score = 900.0 - dist * 6.0; // Plus bas que moulin libre pour dist égale
                    if (tempsAdversaire < dist) {
                        score -= 400.0; // Ne pas y aller si qqn de plus proche
                    } else if (tempsAdversaire <= 2) {
                        score -= 200.0; // Quelqu'un est juste à côté et va le reprendre tout de suite !
                    } else if (tempsAdversaire == dist) {
                        score -= 50.0;
                    } else {
                        score += 50.0; // Bonus sécurisé de vol !
                    }
                } else {
                    score = 150.0 - dist * 10.0;
                }
            } else if (Plateau.contientUneUniteDeRessourcage(contenuCible)) {
                // Oliveraie
                if (rechercheUrgenteOliveraie) {
                    score = 5000.0 - dist * 2.0; // Urgence absolue
                } else if (monEnergie < ENERGIE_MAX - 20) {
                    score = 300.0 - dist * 8.0; // Confort
                }
                
                // Si l'oliveraie est occupée, on s'y dirige quand même si on a pas le choix (en urgence)
                Joueur occupant = plateau.donneJoueurEnPosition(p);
                if (occupant != null && occupant.donneRang() != donneRang() && !rechercheUrgenteOliveraie) {
                    score = -999999; 
                }
            }

            // Mise à jour de la meilleure cible trouvée
            if (score > meilleurScore) {
                meilleurScore = score;
                meilleureCible = p;
            }

            // Parcourir les voisins
            int[][] dirs = {{0,1},{0,-1},{1,0},{-1,0}};
            for (int[] d : dirs) {
                int nx = p.x + d[0];
                int ny = p.y + d[1];
                
                if (plateau.coordonneeValide(nx, ny) && !visite[nx][ny]) {
                    int c = plateau.donneContenuCellule(nx, ny);
                    
                    boolean estObstacleSup = false;
                    for (Noeud n : obstaclesSupplementaires) {
                        if (n.getX() == nx && n.getY() == ny) {
                            estObstacleSup = true;
                            break;
                        }
                    }
                    
                    boolean estFranchissable = !Plateau.contientUneZoneInfranchissable(c) 
                                            || Plateau.contientUneUniteDeProduction(c) 
                                            || Plateau.contientUneUniteDeRessourcage(c);
                                            
                    if (estFranchissable && !estObstacleSup) {
                        visite[nx][ny] = true;
                        file.add(new PointDist(new Point(nx, ny), dist + 1));
                    }
                }
            }
        }

        return meilleureCible;
    }

    /**
     * Cherche un adverse pour déclencher une manille si urgence énergie
     * ou stratégie offensive.
     */
    private Action chercherManille(Plateau plateau, boolean urgence) {
        Joueur ciblesAdverses[] = plateau.donneJoueurs();
        int[] cooldowns = plateau.donneToursRestantEchange();
        if (ciblesAdverses == null || cooldowns == null) return Action.RIEN;
        
        Point maPos = donnePosition();
        
        for (Joueur j : ciblesAdverses) {
            if (j == null || j.donneRang() == donneRang()) continue;
            int rg = j.donneRang();
            
            // L'adversaire doit être dispo (pas de cooldown)
            if (cooldowns[rg] == 0) {
                Point jp = j.donnePosition();
                // Pas en oliveraie
                int cont = plateau.donneContenuCelluleSansJoueur(jp.x, jp.y);
                if (!Plateau.contientUneUniteDeRessourcage(cont)) {
                    // Trouver une case adjacente libre à cet adversaire
                    int[][] dirs = {{0,1},{0,-1},{1,0},{-1,0}};
                    for(int[] d : dirs) {
                        int nx = jp.x + d[0];
                        int ny = jp.y + d[1];
                        if (plateau.coordonneeValide(nx, ny)) {
                            int cnx = plateau.donneContenuCellule(nx, ny);
                            if (!Plateau.contientUneZoneInfranchissable(cnx) && !Plateau.contientUnJoueur(cnx)) {
                                // Mouvement direct si adjacente
                                if (Math.abs(nx - maPos.x) + Math.abs(ny - maPos.y) == 1) {
                                    return convertirEnAction(maPos, new Point(nx, ny));
                                }
                                // Mouvement A* sinon
                                ArrayList<Noeud> chemin = plateau.donneCheminEntre(maPos, new Point(nx, ny));
                                if (chemin != null && chemin.size() > 1) {
                                    return convertirEnAction(maPos, new Point(chemin.get(1).getX(), chemin.get(1).getY()));
                                }
                            }
                        }
                    }
                }
            }
        }
        return Action.RIEN;
    }

    private Action explorationSecurisee(Plateau plateau, List<Noeud> dangers, Point posPrec) {
        Point p = donnePosition();
        int[][] dirs = {{0,-1},{0,1},{-1,0},{1,0}}; // HAUT, BAS, GAUCHE, DROITE
        Action[] acts = {Action.HAUT, Action.BAS, Action.GAUCHE, Action.DROITE};
        
        Action antiOscillation = Action.RIEN;
        Action nimporteLequel = Action.RIEN;

        for (int i=0; i<4; i++) {
            int nx = p.x + dirs[i][0];
            int ny = p.y + dirs[i][1];
            
            if (plateau.coordonneeValide(nx, ny)) {
                // Verifier si (nx, ny) est dans la liste des dangers
                boolean estDanger = false;
                for (Noeud n : dangers) {
                    if (n.getX() == nx && n.getY() == ny) {
                        estDanger = true; break;
                    }
                }
                
                if (!estDanger) {
                    int c = plateau.donneContenuCellule(nx, ny);
                    boolean estFranchissable = !Plateau.contientUneZoneInfranchissable(c) 
                                            || Plateau.contientUneUniteDeProduction(c) 
                                            || Plateau.contientUneUniteDeRessourcage(c);
                                            
                    if (estFranchissable && !Plateau.contientUnJoueur(c)) {
                        nimporteLequel = acts[i];
                        // Anti-oscillation basique : ne pas retourner là où on vient d'être
                        if (posPrec == null || (posPrec.x != nx || posPrec.y != ny)) {
                            antiOscillation = acts[i];
                        }
                    }
                }
            }
        }
        
        if (antiOscillation != Action.RIEN) return antiOscillation;
        if (nimporteLequel != Action.RIEN) return nimporteLequel;
        return Action.RIEN;
    }

    private Action tenterDeblocage(Plateau plateau) {
        // Mouvement forcé même dans les zones "dangereuses" pour sortir
        Point p = donnePosition();
        int[][] dirs = {{0,-1},{0,1},{-1,0},{1,0}};
        Action[] acts = {Action.HAUT, Action.BAS, Action.GAUCHE, Action.DROITE};
        
        for (int i=0; i<4; i++) {
            int nx = p.x + dirs[i][0];
            int ny = p.y + dirs[i][1];
            if (plateau.coordonneeValide(nx, ny)) {
                int c = plateau.donneContenuCellule(nx, ny);
                if (!Plateau.contientUneZoneInfranchissable(c) && !Plateau.contientUnJoueur(c)) {
                    return acts[i];
                }
            }
        }
        return Action.RIEN;
    }

    private Action convertirEnAction(Point de, Point vers) {
        int dx = vers.x - de.x;
        int dy = vers.y - de.y;
        if (dx == 1 && dy == 0) return Action.DROITE;
        if (dx == -1 && dy == 0) return Action.GAUCHE;
        if (dx == 0 && dy == 1) return Action.BAS;
        if (dx == 0 && dy == -1) return Action.HAUT;
        return Action.RIEN;
    }

    // Helper pour le BFS
    class PointDist {
        Point p;
        int dist;
        PointDist(Point p, int dist) {
            this.p = p;
            this.dist = dist;
        }
    }
    
    private int distanceManhattan(Point p1, Point p2) {
        return Math.abs(p1.x - p2.x) + Math.abs(p1.y - p2.y);
    }
}