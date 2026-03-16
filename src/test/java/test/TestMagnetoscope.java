package test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.awt.Point;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Random;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;

import jeu.MaitreDuJeu;
import jeu.MaitreDuJeuListener;
import jeu.Plateau;
import jeu.Joueur.Action;

import java.security.SecureRandom;

/**
 * Test de la classe {@link jeu.Magnetoscope}.
 * 
 * On peut lancer ces tests soit avec un plateau prédéfini, soit
 * avec un plateau aléatoire. Il suffit pour cela de modifier
 * la première instruction de la méthode {@link #demarrer()}.
 * 
 * Le nombre de tours de la partie est défini dans la variable
 * nbTours.
 *
 * @author Lucile
 */
public class TestMagnetoscope {
	private static final int NB_TOURS = 10;
	private static final String LOG_INIT = "/tmp/partie.log";
	private static final String LOG_MAGNETO = "/tmp/magneto.log";
	private static final String LOG_TEMPORAIRE = "/tmp/toto.log";
	private Plateau plateau;
	private MaitreDuJeu jeu;
	private File logFile;
	private final Random random = new SecureRandom(); 
	
	/**
	 * Lancement d'une partie sans affichage graphique où les 4 joueurs 
	 * sont des joueurs qui se déplacent aléatoirement
	 * (c'est-à-dire des instances de la classe {@link jeu.Joueur}).
	 * @throws SecurityException 
	 * @throws NoSuchMethodException 
	 */
	@BeforeEach
	void setUp() {
		// Création du plateau
	    //plateau = creerPlateauPredefini(); // pour utiliser le plateau prédéfini
	    plateau = creerPlateauAleatoire(); // pour utiliser un plateau aléatoire
	    //System.out.println(plateau);
    
	    // Création du maitre de jeu
	    jeu = new MaitreDuJeu(plateau);
	    
	    // Écouteur pour les messages du jeu (partie.log) 
	    jeu.addEcouteurDuJeu(new MaitreDuJeuListener() {
	        @Override
	        public void unJeuAChange(MaitreDuJeu arg0) {
	        }
	        
	        /*@Override
	        public void nouveauMessage(MaitreDuJeu arg0, String arg1) {
				try (java.io.FileWriter fw = new java.io.FileWriter(logFile, true);  // true = append
	                    java.io.PrintWriter pw = new java.io.PrintWriter(fw)) {
	                   pw.println(arg1);
	               } catch (java.io.IOException e) {
	                   fail(e);
	               }
	        }*/
	        @Override
	        public void nouveauMessage(MaitreDuJeu arg0, String arg1) {
	            try {
	                // Ecrit en append avec UTF-8
	                Files.writeString(
	                    logFile.toPath(), 
	                    arg1 + System.lineSeparator(), 
	                    java.nio.file.StandardOpenOption.CREATE, 
	                    java.nio.file.StandardOpenOption.APPEND
	                );
	            } catch (IOException e) {
	                fail(e);
	            }
	        }

	        @Override
	        public void afficheSymbole(MaitreDuJeu arg0, Symboles arg1, Point arg2, int arg3, int arg4) {
	        }
	     }); 
    }
	
	@AfterEach
    void cleanFiles() throws IOException {
        Files.deleteIfExists(Path.of(LOG_TEMPORAIRE));
        Files.deleteIfExists(Path.of(LOG_MAGNETO));
        Files.deleteIfExists(Path.of(LOG_INIT));
    }
	
	/**
	 * Méthode de test du {@link jeu.Magnetoscope}.
	 * 
	 * Cette méthode utilise la méthode protégée {@link Plateau#clone()}.
	 * 
	 * Joue une partie entière en nbTours tours et vérifie que le magnétoscope peut rejouer
	 * la séquence d'actions jouées pendant la partie. Les 4 joueurs sont des joueurs à 
	 * déplacement aléatoire.  
	 */
	@RepeatedTest(1000)
    void testRelectureMagnetoscope() throws Exception {
		final Plateau[] plateaux = new Plateau[NB_TOURS+1];
		final Method methodeClone = Plateau.class.getDeclaredMethod("clone");
		methodeClone.setAccessible(true); 
		
		final ArrayList<Action> actions = new ArrayList<>();
			    
        // ----------- PARTIE INITIALE -----------
		logFile = new File(LOG_INIT); 
		// Les appels à joueSuivant(true,false) en appelant nouveauMessage 
		// vont écrire dans partie.log
		
		for (int i = 0 ; i < NB_TOURS; i++) {
			for ( int j = 0; j < 4; j++) {
				actions.add(jeu.joueSuivant(true, false)); 
			}
			// le plateau est enregistré après chaque tour (et pas après chaque coup de chaque joueur)
			// plateaux [0] contient le plateau après le 1er tour (soit après 4 coups)
			plateaux[i]= (Plateau) methodeClone.invoke(jeu.donnePlateau()); 
		}
		assertTrue( jeu.partieTerminee());
		final Plateau plateauFinalPartie = jeu.donnePlateau();
		
        final String actionsMagneto = jeu.donneMagnetoscope().encode().split("£")[0];
		final String actionsJouees = actions.toString();
		final int nbActionsMagneto = 1 + actionsMagneto.length() - actionsMagneto.replace(",", "").length();
		final int nbActionsJouees = 1 + actionsJouees.length() - actionsJouees.replace(",", "").length();
		/*if ( ! actionsJouees.equals(actionsMagneto)) {
			System.out.println( "Initiale :"+nbActionsJouees+actionsJouees);
			System.err.println( "Magneto :"+nbActionsMagneto+actionsMagneto);
		}*/
		assertEquals( nbActionsJouees, nbActionsMagneto);
		assertEquals( actionsJouees.length(), actionsMagneto.length());
		assertEquals( actionsJouees, actionsMagneto.replace("null", "RIEN"));
		
		// ----------- RELECTURE COMPLETE -----------
		// On rejoue tous les coups mais les messages sont écrits dans magneto.log
		logFile = new File(LOG_MAGNETO); 
		Plateau plateauCourantMagneto= jeu.donneMagnetoscope().voirCoup(40, true).donnePlateau(); // 40 coups pour 10 tours
		assertEquals(plateauFinalPartie.toString(),plateauCourantMagneto.toString());
		
		// On compare les log de la partie initiale et de la partie rejouée
		final String contenuPartie = Files.readString(Path.of(LOG_INIT));
		final String contenuMagneto = Files.readString(Path.of(LOG_MAGNETO));
		assertEquals(contenuPartie, contenuMagneto);
		
		// ----------- VERIFICATION TOUR PAR TOUR -----------
		logFile = new File(LOG_TEMPORAIRE); 
		plateauCourantMagneto = jeu.donneMagnetoscope().voirCoup(4, false).donnePlateau(); // 4 coups pour 1 tour
		assertEquals(plateaux[0].toString(),plateauCourantMagneto.toString());
				
		for (int i = 0 ; i < NB_TOURS; i++) { 
			// les coups sont numérotés à partir de 1
			plateauCourantMagneto = jeu.donneMagnetoscope().voirCoup((i+1)*4, false).donnePlateau();
//			if ( ! plateaux[i].equals(plateauCourantMagneto)) {
//				final int k = plateaux[i].toString().indexOf(",");
//				final int j = plateauCourantMagneto.toString().indexOf(",");
//				System.err.println( plateaux[i].toString()/*.substring(k+1)*/);
//				System.err.println( plateauCourantMagneto.toString()/*.substring(j+1)*/);
//				System.err.println();
//			}
			assertEquals(plateaux[i].toString(),plateauCourantMagneto.toString());		
		}		
		methodeClone.setAccessible(false); 	
	}
	
	 // ================== SUPPORT ==================

	/**
	 * 
	 * Création d'un plateau aléatoire pour les tests avec un nombre de tours
	 * égal à nbTours.
	 * 
	 * Le plateau est créé avec une taille de 20, un nombre d'unités de ressourçage
	 * compris entre 4 et 12, un nombre d'unités de production compris entre 12 et 28
	 * et un nombre de zones infranchissables compris entre 60 et 96.
	 * 
	 * @return le plateau créé.
	 */
	private Plateau creerPlateauAleatoire() {
		final int demiLargeur = 10;
		final int nbUnitesRessourcageParQuart = 1 + random.nextInt(3); // 1 à 3
		final int nbUnitesProductionParQuart = 3 + random.nextInt(5); // 3 à 7
		final int nbZonesInfranchissablesParQuart = 15 + random.nextInt(10); // 15 à 24
		Plateau p;     
		do {        
          p = Plateau.generePlateauAleatoire(NB_TOURS,demiLargeur,nbUnitesRessourcageParQuart, 
        		  nbUnitesProductionParQuart, nbZonesInfranchissablesParQuart); 
        }  while ( p == null);
		return p;
	}
	
	/**
	 * 
	 * Création d'un plateau prédéfini pour les tests avec un nombre de tours
	 * égal à nbTours.
	 * 
	 * Le plateau est créé à partir d'une description textuelle de la carte. 
	 * 
	 * @return le plateau créé.
	 */
	private Plateau creerPlateauPredefini() {
		final String description = """
				+----------------------------------------+
				|      ##                        ##      |
				|                  P-P-                  |
				|    ##    P-      $$$$      P-    ##    |
				|    ####          ####          ####    |
				|P-    ####    ##        ##    ####    P-|
				|  ##    ####                ####    ##  |
				|      ##P-  P-            P-  P-##      |
				|            P-P-        P-P-            |
				|        ####                ####        |
				|  ####@1        ########        @2####  |
				|  ####@3        ########        @4####  |
				|        ####                ####        |
				|            P-P-        P-P-            |
				|      ##P-  P-            P-  P-##      |
				|  ##    ####                ####    ##  |
				|P-    ####    ##        ##    ####    P-|
				|    ####          ####          ####    |
				|    ##    P-      $$$$      P-    ##    |
				|                  P-P-                  |
				|      ##                        ##      |
				+----------------------------------------+\
				""";
		return new Plateau(NB_TOURS, description);
	}


}