package test;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import jeu.Joueur;
import jeu.MaitreDuJeu;
import jeu.Plateau;

/**
 * Test de la classe MaitreDuJeu qui teste les situations
 * de déplacement des joueurs sans créer d'échanges entre
 * joueurs.
 * 
 * Les tests sont réalisés sans affichage graphique.
 * 
 * Les joueurs sont des instances de {@link Automate} qui jouent 
 * des séquences d'actions prédéfinies ou des joueurs dont
 * le déplacement est aléatoire.
 *
 * @author lucile
 */
class TestSimpleMaitreDuJeuRessourcageVariable2 {
  Plateau plateau;
  MaitreDuJeu jeu;
  
  @BeforeEach
  void init() {
	  final String description = """
			+----------------+
			|  $$    @2@3$$  |
			|  @1    ##P2  $$|
			|##  ##      ##  |
			|  ##  ##  ##  ##|
			|              P2|
			|  @4    P2      |
			|              P3|
			|  ##P1######P-  |
			+----------------+\
			""";
	  plateau = new Plateau(10, description);
	  assertEquals(description, plateau.toString().split(",")[0]);
	  jeu = new MaitreDuJeu(plateau);
  }
  
  private void metRessources( Joueur j, int valeur) 
		  throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
	  final int ressources = j.donneRessources();
	  
	  //j1.ajouteRessources(valeur-ressources);	  
	  final Method method = Joueur.class.getDeclaredMethod("ajouteRessources", int.class);
	  method.setAccessible(true); 
	  method.invoke(j, valeur-ressources);
	  
	  assertEquals(valeur, j.donneRessources()); 
	  method.setAccessible(false); 
 }
  
  private void metNombreCoupsJoues( int valeur) 
		  throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
	  final Field field = Plateau.class.getDeclaredField("nombreCoupsJoues");
	  field.setAccessible(true);
	  field.setInt(plateau, valeur);
	  field.setAccessible(false);
  }
   
  @ParameterizedTest
  @CsvSource({"0,10","1,10","2,20","3,20","4,59","5,59","6,20","7,20","8,10","9,10"})
  void testDeplacementSurUniteDeRessourcage2(int tourCourant, int gainRessources) 
		  throws IllegalAccessException, InvocationTargetException, 
		  	NoSuchMethodException, NoSuchFieldException, IllegalArgumentException {
	  jeu.metJoueurEnPosition(0, new Automate( "J1", "H"));// j1 va jouer HAUT pour aller dans l'oliveraie
	  final Joueur j1 = plateau.donneJoueur(0);   
      
	  metNombreCoupsJoues(tourCourant*4);
	  assertEquals( tourCourant, plateau.donneTourCourant());
	  assertEquals( tourCourant, plateau.donneNombreCoupsJoues()/4);
	  metRessources(j1,41); 
	  assertEquals(41, j1.donneRessources());
	  jeu.joueSuivant(false,false); 
	  //System.out.println(tourCourant+":"+j1.donneRessources());
	  // au tour 0 j1 n'est pas dans une oliveraie donc le gain en ressources vaut -1
	  assertEquals(41+gainRessources, j1.donneRessources()); 
  }

}
