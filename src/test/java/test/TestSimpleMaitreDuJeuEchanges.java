package test;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jeu.Joueur;
import jeu.MaitreDuJeu;
import jeu.Plateau;
import jeu.Joueur.Action;

/**
 * Test de la classe MaitreDuJeu qui teste les situations
 * de déplacement des joueurs qui créent des échanges.
 * 
 * Les tests sont réalisés sans affichage graphique.
 * 
 * Les joueurs sont des instances de {@link Automate} qui jouent 
 * des séquences d'actions prédéfinies.
 *
 * @author lucile
 */
class TestSimpleMaitreDuJeuEchanges {
  Plateau plateau;
  MaitreDuJeu jeu;
  boolean avecLog;
  
  @BeforeEach
  void init() {
	  final String description = """
			+----------+
			|    @4    |
			|  @2  @3  |
			|    @1    |
			|          |
			|P1P2P3P4  |
			+----------+\
			""";
	  plateau = new Plateau(30, description);
	  assertEquals(description, plateau.toString().split(",")[0]);
	  jeu = new MaitreDuJeu(plateau);
	  avecLog = true; // true pour afficher les infos de déroulement des parties
  }
  
  private void metRessources( Joueur j, int valeur) 
		  throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
	  final int ressources = j.donneRessources();
	  
	  //j1.ajouteRessources(valeur-ressources);	  
	  final Method method = Joueur.class.getDeclaredMethod("ajouteRessources", int.class);
	  method.setAccessible(true); 
	  method.invoke(j, valeur-ressources);
	  
	  assertEquals(valeur, j.donneRessources()); 
 }
  
  private void metPoints( Joueur j, int valeur) 
		  throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
	  int points = j.donnePoints();

	  //j1.ajoutePoints(valeur-points);	  
	  Method method = Joueur.class.getDeclaredMethod("ajoutePoints", int.class);
	  method.setAccessible(true); 
	  method.invoke(j, valeur-points);
	  
	  assertEquals(valeur, j.donnePoints()); 
 }

   
  @Test
  public void testDeplacementVers3Joueurs() 
		  throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
	  if (avecLog)
		  System.out.println( "\n**************** Partie pour tester un échange à 2 joueurs  **********************");
	  jeu.metJoueurEnPosition(0, new Automate( "Joueur 0", "H"));// j1 va jouer HAUT puis RIEN
	  jeu.metJoueurEnPosition(1, new Automate( "Joueur 1", "."));// j2 toujours RIEN
	  jeu.metJoueurEnPosition(2, new Automate( "Joueur 2", "."));// j3 toujours RIEN
	  jeu.metJoueurEnPosition(3, new Automate( "Joueur 3", "."));// j4 toujours RIEN
	  final Joueur j1 = plateau.donneJoueur(0);
	  final Joueur j2 = plateau.donneJoueur(1);
	  final Joueur j3 = plateau.donneJoueur(2);
	  final Joueur j4 = plateau.donneJoueur(3);
	  metRessources( j1, 50);
	  metRessources( j2, 50);
	  metRessources( j3, 50);
	  metRessources( j4, 50);
	  metPoints( j1, 50);
	  metPoints( j2, 50);
	  metPoints( j3, 50);
	  metPoints( j4, 50);
	  
	  /*** Tour 1 ***/	  
	  jeu.joueSuivant(false, avecLog); // j1 joue HAUT vers j2 j3 et j4
	  assertEquals( "[10, 10, 10, 10]", Arrays.toString( plateau.donneToursRestantEchange()));
	  assertEquals(49, j1.donneRessources());
	  assertEquals(51, j1.donnePoints());
	  
	  assertEquals( Action.RIEN, jeu.joueSuivant(false, avecLog)); // j2 échange
	  assertEquals( "[10, 9, 10, 10]", Arrays.toString( plateau.donneToursRestantEchange()));
	  assertEquals(52, j2.donneRessources());
	  assertEquals(50, j2.donnePoints());

	  assertEquals( Action.RIEN, jeu.joueSuivant(false, avecLog)); // j3 échange
	  assertEquals( "[10, 9, 9, 10]", Arrays.toString( plateau.donneToursRestantEchange()));
	  assertEquals(52, j3.donneRessources());
	  assertEquals(50, j3.donnePoints());

	  assertEquals( Action.RIEN, jeu.joueSuivant(false, avecLog)); // j4 échange
	  assertEquals( "[10, 9, 9, 9]", Arrays.toString( plateau.donneToursRestantEchange()));
	  assertEquals(52, j4.donneRessources());
	  assertEquals(50, j4.donnePoints());
	  
	  /*** Tours 2 à 10 ***/
	  for( int tours = 0; tours < 9; tours++) {
		  for( int i = 0 ; i < 4 ; i++) {
			assertEquals( Action.RIEN, jeu.joueSuivant(false, avecLog)); // tous en échange
		}
	  }
	  assertEquals( "[1, -5, -5, -5]", Arrays.toString( plateau.donneToursRestantEchange()));
	  assertEquals(67, j1.donneRessources());
	  assertEquals(51, j1.donnePoints());
	  assertEquals(70, j2.donneRessources());
	  assertEquals(50, j2.donnePoints());
	  assertEquals(70, j3.donneRessources());
	  assertEquals(50, j3.donnePoints());
	  assertEquals(70, j4.donneRessources());
	  assertEquals(50, j4.donnePoints());
	  
	  /*** Tour 11 ***/
	  assertEquals( Action.RIEN, jeu.joueSuivant(false, avecLog)); // j1 échange
	  assertEquals( "[-5, -5, -5, -5]", Arrays.toString( plateau.donneToursRestantEchange()));
	  assertEquals(69, j1.donneRessources());
	  assertEquals(51, j1.donnePoints());

	  assertEquals( Action.RIEN, jeu.joueSuivant(false, avecLog)); // j2 joue
	  assertEquals( "[-5, -4, -5, -5]", Arrays.toString( plateau.donneToursRestantEchange()));
	  assertEquals(70, j2.donneRessources());
	  assertEquals(51, j2.donnePoints());

	  assertEquals( Action.RIEN, jeu.joueSuivant(false, avecLog)); // j3 joue
	  assertEquals( "[-5, -4, -4, -5]", Arrays.toString( plateau.donneToursRestantEchange()));
	  assertEquals(70, j3.donneRessources());
	  assertEquals(51, j3.donnePoints());

	  assertEquals( Action.RIEN, jeu.joueSuivant(false, avecLog)); // j4 joue
	  assertEquals( "[-5, -4, -4, -4]", Arrays.toString( plateau.donneToursRestantEchange()));
	  assertEquals(70, j4.donneRessources());
	  assertEquals(51, j4.donnePoints());

	  /*** Tours 12 à 14 ***/
	  for( int tours = 0; tours < 3; tours++) {
		  for( int i = 0 ; i < 4 ; i++) {
			assertEquals( Action.RIEN, jeu.joueSuivant(false, avecLog)); // tous jouent RIEN
		}
	  }
	  assertEquals( "[-2, -1, -1, -1]", Arrays.toString( plateau.donneToursRestantEchange()));
	  assertEquals(69, j1.donneRessources());
	  assertEquals(54, j1.donnePoints());
	  assertEquals(70, j2.donneRessources());
	  assertEquals(54, j2.donnePoints());
	  assertEquals(70, j3.donneRessources());
	  assertEquals(54, j3.donnePoints());
	  assertEquals(70, j4.donneRessources());
	  assertEquals(54, j4.donnePoints());

	  /*** Tour 15 ***/
	  assertEquals( Action.RIEN, jeu.joueSuivant(false, avecLog)); // j1 joue
	  assertEquals( "[-1, -1, -1, -1]", Arrays.toString( plateau.donneToursRestantEchange()));
	  assertEquals(69, j1.donneRessources());
	  assertEquals(55, j1.donnePoints());

	  assertEquals( Action.RIEN, jeu.joueSuivant(false, avecLog)); // j2 joue
	  assertEquals( "[-1, 0, -1, -1]", Arrays.toString( plateau.donneToursRestantEchange()));
	  assertEquals(70, j2.donneRessources());
	  assertEquals(55, j2.donnePoints());

	  assertEquals( Action.RIEN, jeu.joueSuivant(false, avecLog)); // j3 joue
	  assertEquals( "[-1, 0, 0, -1]", Arrays.toString( plateau.donneToursRestantEchange()));
	  assertEquals(70, j3.donneRessources());
	  assertEquals(55, j3.donnePoints());

	  assertEquals( Action.RIEN, jeu.joueSuivant(false, avecLog)); // j4 joue
	  assertEquals( "[-1, 0, 0, 0]", Arrays.toString( plateau.donneToursRestantEchange()));
	  assertEquals(70, j4.donneRessources());
	  assertEquals(55, j4.donnePoints());
	  
	  /*** Tour 16 ***/
	  assertEquals( Action.RIEN, jeu.joueSuivant(false, avecLog)); // j1 joue
	  assertEquals( "[10, 10, 10, 10]", Arrays.toString( plateau.donneToursRestantEchange()));
	  assertEquals(69, j1.donneRessources());
	  assertEquals(56, j1.donnePoints());

	  assertEquals( Action.RIEN, jeu.joueSuivant(false, avecLog)); // j2 échange
	  assertEquals( "[10, 9, 10, 10]", Arrays.toString( plateau.donneToursRestantEchange()));
	  assertEquals(72, j2.donneRessources());
	  assertEquals(55, j2.donnePoints());

	  assertEquals( Action.RIEN, jeu.joueSuivant(false, avecLog)); // j3 échange
	  assertEquals( "[10, 9, 9, 10]", Arrays.toString( plateau.donneToursRestantEchange()));
	  assertEquals(72, j3.donneRessources());
	  assertEquals(55, j3.donnePoints());

	  assertEquals( Action.RIEN, jeu.joueSuivant(false, avecLog)); // j4 échange
	  assertEquals( "[10, 9, 9, 9]", Arrays.toString( plateau.donneToursRestantEchange()));
	  assertEquals(72, j4.donneRessources());
	  assertEquals(55, j4.donnePoints());
  }

  @Test
  public void testDeplacementVers2Joueurs() 
		  throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
	  if (avecLog) {
		System.out.println( "\n**************** Partie pour tester un échange à 2 joueurs  **********************");
	  }
	  jeu.metJoueurEnPosition(0, new Automate( "Joueur 0", "B"));// j1 va jouer BAS puis RIEN
	  jeu.metJoueurEnPosition(1, new Automate( "Joueur 1", "."));// j2 toujours RIEN
	  jeu.metJoueurEnPosition(2, new Automate( "Joueur 2", "G"));// j3 va jouer GAUCHE puis RIEN
	  jeu.metJoueurEnPosition(3, new Automate( "Joueur 3", "."));// j4 toujours RIEN
	  final Joueur j1 = plateau.donneJoueur(0);
	  final Joueur j2 = plateau.donneJoueur(1);
	  final Joueur j3 = plateau.donneJoueur(2);
	  final Joueur j4 = plateau.donneJoueur(3);
	  metRessources( j1, 50);
	  metRessources( j2, 50);
	  metRessources( j3, 50);
	  metRessources( j4, 50);
	  metPoints( j1, 50);
	  metPoints( j2, 50);
	  metPoints( j3, 50);
	  metPoints( j4, 50);

	  /*** Tour 1 ***/	  
	  assertEquals(Action.BAS,jeu.joueSuivant(false, avecLog)); // j1 joue BAS
	  assertEquals( "[0, 0, 0, 0]", Arrays.toString( plateau.donneToursRestantEchange()));
	  assertEquals(49, j1.donneRessources());
	  assertEquals(51, j1.donnePoints());
	  
	  assertEquals(Action.RIEN,jeu.joueSuivant(false, avecLog)); // j2 joue RIEN
	  assertEquals( "[0, 0, 0, 0]", Arrays.toString( plateau.donneToursRestantEchange()));
	  assertEquals(50, j2.donneRessources());
	  assertEquals(51, j2.donnePoints());

	  assertEquals(Action.GAUCHE,jeu.joueSuivant(false, avecLog)); // j3 joue GAUCHE
	  assertEquals( "[0, 10, 10, 10]", Arrays.toString( plateau.donneToursRestantEchange()));
	  assertEquals(49, j3.donneRessources());
	  assertEquals(51, j3.donnePoints());

	  assertEquals( Action.RIEN, jeu.joueSuivant(false, avecLog)); // j4 échange
	  assertEquals( "[0, 10, 10, 9]", Arrays.toString( plateau.donneToursRestantEchange()));
	  assertEquals(52, j4.donneRessources());
	  assertEquals(50, j4.donnePoints());
	  
	  /*** Tour 2 ***/	  
	  assertEquals(Action.RIEN,jeu.joueSuivant(false, avecLog)); // j1 joue RIEN
	  assertEquals( "[0, 10, 10, 9]", Arrays.toString( plateau.donneToursRestantEchange()));
	  assertEquals(49, j1.donneRessources());
	  assertEquals(52, j1.donnePoints());
	  
	  assertEquals( Action.RIEN, jeu.joueSuivant(false, avecLog)); // j2 échange
	  assertEquals( "[0, 9, 10, 9]", Arrays.toString( plateau.donneToursRestantEchange()));
	  assertEquals(52, j2.donneRessources());
	  assertEquals(51, j2.donnePoints());

	  jeu.joueSuivant(false, avecLog); // j3 échange
	  assertEquals( "[0, 9, 9, 9]", Arrays.toString( plateau.donneToursRestantEchange()));
	  assertEquals(51, j3.donneRessources());
	  assertEquals(51, j3.donnePoints());

	  jeu.joueSuivant(false, avecLog); // j4 échange
	  assertEquals( "[0, 9, 9, 8]", Arrays.toString( plateau.donneToursRestantEchange()));
	  assertEquals(54, j4.donneRessources());
	  assertEquals(50, j4.donnePoints());
  } 
}
