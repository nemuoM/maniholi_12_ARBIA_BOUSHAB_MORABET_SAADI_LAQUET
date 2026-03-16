package test;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jeu.Joueur;
import jeu.MaitreDuJeu;
import jeu.Plateau;

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
class TestSimpleMaitreDuJeuRessourcageVariable1 {
  Plateau plateau;
  MaitreDuJeu jeu;
  boolean avecLog;
  int nbTours;
  
  @BeforeEach
  void init() {
	  nbTours = 13;
	  final String description = """
			+----------+
			|P1  ####  |
			|    $$$$  |
			|    @1    |
			|##########|
			|@2  @3  @4|
			+----------+\
			""";
	  plateau = new Plateau(nbTours, description);
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
  public void testRessourcage() 
		  throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
	  if (avecLog)
		  System.out.println( "\n**************** Partie pour tester le ressourçage  **********************");
	  assertEquals( nbTours * 4, jeu.donneNombreDeCoupsTotal());
	  assertEquals( nbTours, plateau.donneNombreDeTours());
	  
	  // avec config.ConfigurationJeu.GAIN_RESSOURCES_PAR_TOUR_PENDANT_ACQUISITION = {10,20,60,20,10} et 13 tours
	  assertArrayEquals( new int[] {10,20,60,20,10}, config.ConfigurationJeu.GAIN_RESSOURCES_PAR_TOUR_PENDANT_ACQUISITION);
	  assertEquals(10, jeu.donneGainRessourcageParTour(0));
	  assertEquals(10, jeu.donneGainRessourcageParTour(1));
	  assertEquals(10, jeu.donneGainRessourcageParTour(2));
	  assertEquals(20, jeu.donneGainRessourcageParTour(3));
	  assertEquals(20, jeu.donneGainRessourcageParTour(4));
	  assertEquals(20, jeu.donneGainRessourcageParTour(5));
	  assertEquals(60, jeu.donneGainRessourcageParTour(6));
	  assertEquals(60, jeu.donneGainRessourcageParTour(7));
	  assertEquals(60, jeu.donneGainRessourcageParTour(8));
	  assertEquals(20, jeu.donneGainRessourcageParTour(9));
	  assertEquals(20, jeu.donneGainRessourcageParTour(10));
	  assertEquals(10, jeu.donneGainRessourcageParTour(11));
	  assertEquals(10, jeu.donneGainRessourcageParTour(12));

	  jeu.metJoueurEnPosition(0, new Automate( "Joueur 0", "BBHHDHHHGH.H."));// j1 va jouer HAUT puis RIEN
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
	  
	  assertEquals(50, j1.donneRessources());
	  for (int coup=0; coup < 4; coup++) jeu.joueSuivant(false, avecLog); // tour 0
	  assertEquals(40, j1.donneRessources()); // car collision -10 
	  for (int coup=0; coup < 4; coup++) jeu.joueSuivant(false, avecLog); // tour 1
	  assertEquals(30, j1.donneRessources()); // car collision -10
	  for (int coup=0; coup < 4; coup++) jeu.joueSuivant(false, avecLog); // tour 2
	  assertEquals(40, j1.donneRessources()); // ressourçage +10 pour tours 0 à 2
	  for (int coup=0; coup < 4; coup++) jeu.joueSuivant(false, avecLog); // tour 3
	  assertEquals(30, j1.donneRessources()); // car collision -10
	  for (int coup=0; coup < 4; coup++) jeu.joueSuivant(false, avecLog); // tour 4
	  assertEquals(50, j1.donneRessources()); // ressourçage +20 pour tours 3 à 5
	  for (int coup=0; coup < 4; coup++) jeu.joueSuivant(false, avecLog); // tour 5
	  assertEquals(40, j1.donneRessources()); // car collision -10
	  for (int coup=0; coup < 4; coup++) jeu.joueSuivant(false, avecLog); // tour 6
	  assertEquals(30, j1.donneRessources()); // car collision -10
	  for (int coup=0; coup < 4; coup++) jeu.joueSuivant(false, avecLog); // tour 7
	  assertEquals(20, j1.donneRessources()); // car collision -10
	  for (int coup=0; coup < 4; coup++) jeu.joueSuivant(false, avecLog); // tour 8
	  assertEquals(80, j1.donneRessources()); // ressourçage +60 pour tours 6 à 8
	  for (int coup=0; coup < 4; coup++) jeu.joueSuivant(false, avecLog); // tour 9
	  assertEquals(70, j1.donneRessources()); // car collision -10
	  for (int coup=0; coup < 4; coup++) jeu.joueSuivant(false, avecLog); // tour 10
	  assertEquals(90, j1.donneRessources()); // ressourçage +20 pour tours 9 et 10
	  for (int coup=0; coup < 4; coup++) jeu.joueSuivant(false, avecLog); // tour 11
	  assertEquals(80, j1.donneRessources()); // car collision -10
	  for (int coup=0; coup < 4; coup++) jeu.joueSuivant(false, avecLog); // tour 12
	  assertEquals(90, j1.donneRessources()); // ressourçage +20 pour tours 11 et 12
  }
}
