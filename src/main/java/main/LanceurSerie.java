package main;

import gui.FenetreDeJeu;
import jeu.Joueur;
import jeu.MaitreDuJeu;
import jeu.MaitreDuJeuListener;
import jeu.Plateau;

import java.awt.Point;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Lance une serie de parties en sequence, sans demarrer la suivante
 * tant que la precedente n'est pas terminee et quittee.
 * Enregistre les résultats dans un fichier CSV.
 */
public class LanceurSerie {

    private static final int NOMBRE_DE_PARTIES = 100;
    private static final boolean AVEC_INTERFACE = false;
    private static final String CSV_FILE = "resultats_parties.csv";

    public static void main(String[] args) throws InterruptedException {
        File f = new File(CSV_FILE);
        boolean fileExists = f.exists();

        try (PrintWriter writer = new PrintWriter(new FileWriter(f, true))) {
            if (!fileExists) {
                writer.println("taille_grille\tnombre_arbres\tnombre_moulins\tnombre_rochers\tnom_joueur\tscore_joueur\tclassement_joueur\tmanilles_joueur\tdepart_joueur");
            }

            for (int i = 1; i <= NOMBRE_DE_PARTIES; i++) {
                System.out.println("Démarrage de la partie " + i + "/" + NOMBRE_DE_PARTIES);
                Plateau plateau = Plateau.generePlateauTournoi();
                MaitreDuJeu jeu = new MaitreDuJeu(plateau);

                jeu.metJoueurEnPosition(0, new MonJoueur("Moumen"));
                jeu.metJoueurEnPosition(1, new MonJoueurCombinedStrategy("Enzo"));
                jeu.metJoueurEnPosition(2, new MonJoueurAmine("Amine"));
                jeu.metJoueurEnPosition(3, new MonJoueurKemil("Kemil"));

                // Pour traquer les manilles par joueur
                final Map<Integer, Integer> manillesCount = new HashMap<>();
                for (int r = 0; r < 4; r++) manillesCount.put(r, 0);

                jeu.addEcouteurDuJeu(new MaitreDuJeuListener() {
                    @Override
                    public void afficheSymbole(MaitreDuJeu mdj, Symboles s, Point p, int r, int val) {
                        if (s == Symboles.DEBUT_ECHANGE || s == Symboles.COLLISION) {
                            if (manillesCount.containsKey(r)) {
                                manillesCount.put(r, manillesCount.get(r) + 1);
                            }
                        }
                    }
                    @Override public void unJeuAChange(MaitreDuJeu mdj) {}
                    @Override public void nouveauMessage(MaitreDuJeu mdj, String msg) {}
                });

                FenetreDeJeu fenetre = null;
                if (AVEC_INTERFACE) {
                    fenetre = new FenetreDeJeu(jeu, true, Lanceur.LOGO_ACTIF);
                }

                jeu.continueLaPartie(true);

                while (jeu.partieEnCours() || !jeu.partieTerminee()) {
                    Thread.sleep(100);
                }

                // Collecte des statistiques de la carte et points de départ
                int taille = plateau.donneTaille();
                int arbres = 0;
                int moulins = 0;
                int rochers = 0;
                Point[] departs = new Point[4];
                for (int y = 0; y < taille; y++) {
                    for (int x = 0; x < taille; x++) {
                        int contenu = plateau.donneContenuCelluleSansJoueur(x, y);
                        if (Plateau.contientUneUniteDeProduction(contenu)) moulins++;
                        if (Plateau.contientUneZoneInfranchissable(contenu)) {
                            // On met tout dans arbres car l'API ne distingue qu'une seule zone infranchissable
                            arbres++;
                        }
                        if (contenu == Plateau.ENDROIT_DEPART_J1) departs[0] = new Point(x, y);
                        else if (contenu == Plateau.ENDROIT_DEPART_J2) departs[1] = new Point(x, y);
                        else if (contenu == Plateau.ENDROIT_DEPART_J3) departs[2] = new Point(x, y);
                        else if (contenu == Plateau.ENDROIT_DEPART_J4) departs[3] = new Point(x, y);
                    }
                }
                
                // Calcul du classement
                Joueur[] joueurs = plateau.donneJoueurs();
                Integer[] indices = {0, 1, 2, 3};
                Arrays.sort(indices, (a, b) -> Integer.compare(joueurs[b].donnePoints(), joueurs[a].donnePoints()));
                
                int[] classement = new int[4];
                for (int rank = 0; rank < 4; rank++) {
                    classement[indices[rank]] = rank + 1;
                }

                // Écriture pour chaque joueur
                for (int j = 0; j < 4; j++) {
                    Joueur player = joueurs[j];
                    if (player == null) continue;
                    
                    Point ptDepart = departs[player.donneRang()];
                    String departStr = (ptDepart != null) ? (ptDepart.x + "," + ptDepart.y) : "?,?";
                    
                    writer.printf("%d\t%d\t%d\t%d\t%s\t%d\t%d\t%d\t%s%n",
                            taille, arbres, moulins, rochers,
                            player.donneNom(), player.donnePoints(), classement[j],
                            manillesCount.get(j), departStr);
                }
                writer.flush(); // On s'assure que c'est bien écrit sur le disque

                if (fenetre != null) {
                    fenetre.dispose();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
