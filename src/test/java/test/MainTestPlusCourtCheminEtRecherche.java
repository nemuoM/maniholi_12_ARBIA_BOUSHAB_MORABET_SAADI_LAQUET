/*
 * Exemple d'utilisation du JAR AreneGoodFarm.jar
 * Version Rois MIAGE - 0.1
 */

package test;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;

import gui.FenetreDeJeu;
import jeu.Joueur;
import jeu.MaitreDuJeu;
import jeu.Plateau;
import jeu.aetoile.Noeud;

/**
 * Main de test manuel (avec affichage graphique) du calcul du plus court chemin entre 
 * deux cases de la carte (méthode {@link jeu.Plateau#donneCheminEntre(Point, Point)})
 * et de la visualisation du chemin sur la carte (méthode 
 * {@link gui.FenetreDeJeu#afficheAstarPath(java.util.ArrayList<jeu.astar.Node>)}).
 * 
 * La classe utilise aussi la méthode {@link jeu.Plateau#cherche(Point,int,int)} pour 
 * rechercher des cases spécifiques autour d'une case donnée. 
 * Elle permet ainsi de tester manuellement la méthode jeu.Plateau.cherche avec un 
 * rayon de 2 et le masque {@link Plateau#CHERCHE_TOUT}.
 *
 * La classe permet aussi de simuler les actions des 4 joueurs selon des séquences 
 * d'actions prédéfinies par un automate, 
 * définis par la classe {@link Automate}. La carte utilisée et ces séquences 
 * d'actions correspondent exactement à 
 * la suite de tests de la classe {@link TestMaitreDuJeuPartie25Tours},
 * ce qui permet également de visualiser l'exécution de cette suite de tests 
 * graphiquement sur la carte (pour aider
 * à la compréhension des tests de {@link TestMaitreDuJeuPartie25Tours}).
 * 
 * Le nombre de tours de chaque partie est 300. 
 *
 * @author Clément et Lucile
 */
public class MainTestPlusCourtCheminEtRecherche {

  public static void main(String [] args) {
    /*final String description = """
			+----------------+
			|$$  P-  @2  $$  |
			|  @1    P-  @3$$|
			|##  ##  ##  ##  |
			|  ##  ##  ##  ##|
			|              P1|
			|  @4    P1      |
			|              P3|
			|  ##P1######P1  |
			+----------------+\
			""";*/

    // Génération du plateau
    final Plateau p = new Plateau(300, Plateau.TEXTE_PLATEAU_ANORMAL_8X8_2);

    // Création du maitre de jeu
    final MaitreDuJeu jeu = new MaitreDuJeu(p);


    // Création de la fenêtre de jeu
    final FenetreDeJeu f = new FenetreDeJeu(jeu, true, false);
    
 
    // Ajout des 4 joueurs dans le jeu.
    // Les joueurs jouent les séquences d'actions programmées dans MaitreDuJeuTest
    jeu.metJoueurEnPosition(0, new Automate("A", "DHDGG"));
    jeu.metJoueurEnPosition(1, new Automate("B", ".HBDB"));
    jeu.metJoueurEnPosition(2, new Automate("C", "..DGHGB"));
    jeu.metJoueurEnPosition(3, new Automate("D", "..DDD"));

    // Envoi des logs de la partie dans un fichier texte
    f.log = new java.io.File("/tmp/titi.log");

    // Ajout d'un listenner des clics souris sur le plateau
    f.setMouseClickListener((int x, int y, int bt) -> {
      System.out.println("\n>>>>>>>>>>>>>>On a cliqué la cellule " + x + "," + y);

    // Ne fonctionne que pour une partie en cours
    // La case départ est celle du joueur courant
    // La case arrivée est celle sur laquelle on clique sur la carte
    final Joueur j = p.donneJoueur(p.donneJoueurCourant());
    System.out.println("*CHEMIN\n   Depart=" + j.donnePosition());
    System.out.println("   Arrivée=" + new Point(x, y));

    // affiche sur la console les cases SEMINAIRE, RUCHER et JOUEUR dans un rayon de 7 cases autour de la case arrivée
    System.out.println("*AUTOUR DE " + new Point(x, y) + " dans un rayon de 2 :"); 
    afficheResultatRecherche(p.cherche(new Point(x, y), 2, Plateau.CHERCHE_TOUT));
    
    // affiche sur la carte le chemin entre la case départ et la case arrivée
    final ArrayList<Noeud> a = p.donneCheminEntre(j.donnePosition(), new Point(x, y));
    f.afficheCheminAEtoile(a);
    });

    // Affichage de la fenêtre
    java.awt.EventQueue.invokeLater(() -> f.setVisible(true));
  }

  // Va avec la fonction clic souris.
  private static void afficheResultatRecherche(HashMap<Integer, ArrayList<Point>> cherche) {
    cherche.keySet().stream().map((k) -> {
        System.out.print("   ");
        System.out.print(k==1?config.ConfigurationLog.UNITES_DE_RESSOURCAGE:
        					k==2?config.ConfigurationLog.UNITES_DE_PRODUCTION:"JOUEURS");
        System.out.print(" : ");
      return k;
    }).map((k) -> {
      cherche.get(k).forEach((p) -> System.out.print("(" + p.x + "," + p.y + ") "));
      return k;
    }).forEachOrdered((inutilise) -> System.out.println());
  }
}