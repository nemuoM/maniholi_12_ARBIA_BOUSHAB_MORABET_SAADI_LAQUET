package test;

import static org.junit.Assert.assertEquals;

import java.awt.Point;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import jeu.Joueur;
import jeu.MaitreDuJeu;
import jeu.Plateau;
import jeu.Joueur.Action;

import static config.ConfigurationJeu.PERTE_RESSOURCES_DEBUT_PRODUCTION;
import static config.ConfigurationJeu.RESSOURCES_MAX;
import static config.ConfigurationJeu.PERTE_RESSOURCES_COLLISION;
import static config.ConfigurationJeu.PERTE_RESSOURCES_DEPLACEMENT_CASE_VIDE;

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
class TestSimpleMaitreDuJeuDeplacementsSansEchanges {
  Plateau plateau;
  MaitreDuJeu jeu;
  
  @BeforeEach
  void init() {
	  final String description = """
			+----------------+
			|$$P-    @2@3$$  |
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
  @ValueSource( ints= {20, 50, 100})
  public void testDeplacementVersUniteDeProductionAvecRessourcesSuffisantes(int energie) 
		  throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
	  assertEquals( 100, RESSOURCES_MAX);
	  jeu.metJoueurEnPosition(0, new Automate( "J1", "H"));// j1 va jouer HAUT
	  final Joueur j1 = plateau.donneJoueur(0);
	  assertEquals(RESSOURCES_MAX, j1.donneRessources()); 
	  assertEquals(0, j1.donnePoints()); 
	  final Point position = j1.donnePosition();

	  metRessources(j1,energie);
	  
	  assertEquals(1, plateau.nombreDUnitesDeProductionJoueur(0));
	  assertEquals(Action.HAUT, jeu.joueSuivant(false,false));
	  assertEquals(energie - PERTE_RESSOURCES_DEBUT_PRODUCTION, j1.donneRessources()); // ressources-20
	  assertEquals(2, plateau.nombreDUnitesDeProductionJoueur(0)); // nbUprod+1
	  if ( j1.donneRessources() != 0) {
		assertEquals(2, j1.donnePoints()); // points+2
	  }
	  else {
		assertEquals(0, j1.donnePoints()); // points inchangé		  
	  }
	  assertEquals( position, j1.donnePosition()); // position inchangée
  }
  
  @ParameterizedTest
  @ValueSource( ints= {10, 0})
  public void testDeplacementVersUniteDeProductionAvecRessourcesInsuffisantes(int energie) 
		  throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
	  jeu.metJoueurEnPosition(0, new Automate( "J1", "H"));// j1 va jouer HAUT
	  final Joueur j1 = plateau.donneJoueur(0);
	  assertEquals(RESSOURCES_MAX, j1.donneRessources()); 
	  assertEquals(0, j1.donnePoints()); 
	  final Point position = j1.donnePosition();

	  metRessources(j1, energie);	  
	    
	  assertEquals(1, plateau.nombreDUnitesDeProductionJoueur(0));
	  assertEquals(Action.HAUT, jeu.joueSuivant(false,false));
	  assertEquals(0, j1.donneRessources()); // ressources=0
	  assertEquals(1, plateau.nombreDUnitesDeProductionJoueur(0)); // nbUprod inchangé
	  assertEquals( position, j1.donnePosition()); // position inchangée
	  assertEquals(0, j1.donnePoints()); // points inchangé
  }

  
  @ParameterizedTest
  @ValueSource( strings= {"H", "D", "B"})
  public void testSeCogner(String direction) {
	  jeu.metJoueurEnPosition(1, new Automate( "J2", direction));// j2 va jouer HAUT ou DROITE ou BAS
	  final Joueur j2 = plateau.donneJoueur(1);
	  assertEquals(0, j2.donnePoints()); 
	  assertEquals(RESSOURCES_MAX, j2.donneRessources()); 
	  final Point position = j2.donnePosition();
	  assertEquals(3, plateau.nombreDUnitesDeProductionJoueur(1));
	  
	  jeu.joueSuivant(false,false); // fait jouer aleatoirement le joueur 1
	  final Action action = direction.equals("H")? Action.HAUT : direction.equals("D")? Action.DROITE : Action.BAS;
	  assertEquals( action, jeu.joueSuivant(false,false)); // fait se cogner le joueur 2
	  assertEquals(3, j2.donnePoints()); // points+3	  
	  assertEquals(RESSOURCES_MAX - PERTE_RESSOURCES_COLLISION, j2.donneRessources()); // ressources-10
	  assertEquals( position, j2.donnePosition()); // position inchangée
	  assertEquals(3, plateau.nombreDUnitesDeProductionJoueur(1)); // nbUprod inchangé
  }

  @ParameterizedTest
  @ValueSource( strings= {"G", "D"})
  public void testDeplacementSurCaseVide(String direction) {
	  jeu.metJoueurEnPosition(0, new Automate( "J1", direction));// j1 va jouer DROITE ou GAUCHE
	  final Joueur j1 = plateau.donneJoueur(0);
	  assertEquals(RESSOURCES_MAX, j1.donneRessources()); 
	  assertEquals(0, j1.donnePoints()); 
	  assertEquals(1, plateau.nombreDUnitesDeProductionJoueur(0));
	  final Point position = j1.donnePosition();
	  
	  final Action action = "D".equals(direction)? Action.DROITE : Action.GAUCHE;
	  assertEquals( action, jeu.joueSuivant(false,false)); // fait se déplacer le joueur 1 vers une case vide
	  assertEquals(RESSOURCES_MAX - PERTE_RESSOURCES_DEPLACEMENT_CASE_VIDE, j1.donneRessources()); // ressources-1
	  final Point nouvellePosition = "D".equals(direction)? new Point(position.x+1, position.y) : new Point(position.x-1, position.y);
	  assertEquals( nouvellePosition, j1.donnePosition()); // position changée
	  assertEquals(1, j1.donnePoints()); //points+1	  
	  assertEquals(1, plateau.nombreDUnitesDeProductionJoueur(0));// nbUprod inchangé
 }
  
  @ParameterizedTest
  @ValueSource( ints= {20, 50, 70, 100})
  void testDeplacementSurUniteDeRessourcage(int energie) 
		  throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
	  jeu.metJoueurEnPosition(1, new Automate( "J2", "G"));// j2 va jouer GAUCHE pour s'éloigner de j3
	  jeu.metJoueurEnPosition(2, new Automate( "J3", "D"));// j3 va jouer DROITE pour aller sur seminaire
	  final Joueur j3 = plateau.donneJoueur(2);
	  assertEquals(0, j3.donnePoints()); 
	  assertEquals(RESSOURCES_MAX, j3.donneRessources()); 
	  final Point position = j3.donnePosition();
	  assertEquals(1, plateau.nombreDUnitesDeProductionJoueur(2));
	  
	  metRessources(j3,energie);
	  
	  jeu.joueSuivant(false,false); // j1 joue
	  assertEquals( Action.GAUCHE, jeu.joueSuivant(false,false)); // j2 joue 
	  assertEquals( Action.DROITE, jeu.joueSuivant(false,false)); // j3 joue 
	  final Point nouvellePosition = new Point(position.x+1, position.y);
	  assertEquals( nouvellePosition, j3.donnePosition()); // position changée
	  assertEquals(0, j3.donnePoints()); // points inchangé  
	  assertEquals(1, plateau.nombreDUnitesDeProductionJoueur(2)); // nbUprod inchangé
	  assertEquals(Math.min(energie + jeu.donneGainRessourcageParTour(plateau.donneTourCourant()), RESSOURCES_MAX), j3.donneRessources()); // ressources+10ou20ou40 mais <=100
  }  
}
