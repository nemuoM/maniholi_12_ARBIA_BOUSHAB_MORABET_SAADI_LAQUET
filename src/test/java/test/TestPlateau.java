package test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Point;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import jeu.Plateau;
import jeu.aetoile.Noeud;
import java.security.SecureRandom;
import java.util.Collections;

/**
 * Tests de la classe {@link jeu.Plateau}.
 * 
 * Ces tests également sont utiles pour comprendre le fonctionnement des méthodes de {@link jeu.Plateau} à utiliser
 * pour élaborer la stratégie de déplacement d'un Joueur (méthode {@link jeu.Joueur#faitUneAction(Plateau)} ) 
 * en fonction des règles du jeu. 
 * 
 * Remarque : La classe Plateau dépend de la classe Joueur.
 * Toutefois, on peut considérer que ces tests sont réalisés en isoltaion car les tests n'utilisent 
 * que les "getters" simples de Joueur. 
 *
 * @author lucile
 *
 */
class TestPlateau {  
  private static String description1;
  private static String description2;
  private static String joueurs1;
  private static String joueurs2;
  private static String joueursPetitPlateau;
  private static String joueursPlateauParDefaut;
  private static Random hasard;
  
  private Plateau plateau1;
  private Plateau plateau2;
  private Plateau plateauPetit;
  private Plateau plateauGrand1;

  /**
   * Initialisation des représentations textuelles des plateaux. 
   */
  @BeforeAll
  static void init() {
    hasard = new SecureRandom();
    
    joueursPetitPlateau = ",Joueur0:0:1:1:100:0,Joueur1:1:4:0:100:0,"
        + "Joueur2:2:6:1:100:0,Joueur3:3:1:5:100:0,0,16";
    
    joueursPlateauParDefaut = ",Joueur0:0:6:5:100:0,Joueur1:1:6:14:100:0,"
        + "Joueur2:2:13:14:100:0,Joueur3:3:13:5:100:0,0,32";
    
    /* Description d'un plateau 8x8 non symetrique avec :
     * 7 ruchers (P-)
     * 3 séminaires ($$)
     * 12 arbres (##)
     * joueur de rang 0 (@1) en position (1,1) 
     * joueur de rang 1 (@2) en position (4,0) 
     * joueur de rang 2 (@3) en position (6,1) 
     * joueur de rang 3 (@4) en position (1,5) 
     */
    description1 = """
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
				""";
    
    joueurs1 = ",Joueur0:0:1:1:100:0,Joueur1:1:4:0:100:0,"
        + "Joueur2:2:6:1:100:0,Joueur3:3:1:5:100:0,0,24";
    
    /* Description d'un plateau de taille 9x9 non symetrique */
    description2 = """
				+------------------+
				|              $$  |
				|                  |
				|##  P-  ##        |
				|  ##  P1          |
				|      @2          |
				|                  |
				|##        ##  P3  |
				|  ##              |
				|  ##              |
				+------------------+\
				""";
    
    joueurs2 = ",Joueur0:0:-1:-1:100:0,Joueur1:1:3:4:100:0,"
        + "Joueur2:2:-1:-1:100:0,Joueur3:3:-1:-1:100:0,0,40";
  }

  /**
   * Initialisation de 4 plateaux.
   */
  @BeforeEach
  void setUp() {
    plateauPetit = new Plateau(4, Plateau.TEXTE_PLATEAU_ANORMAL_8X8_1);
    plateauGrand1 = new Plateau(8, Plateau.TEXTE_PLATEAU_NORMAL_20X20_28P_4R_142I);
    plateau1 = new Plateau(6, description1); 
    plateau2 = new Plateau(10, description2);
  }
  
  @Nested
  class EtatDuPlateau {
	  private int entierAleatoire(int max) {
		    // renvoie un nb aleatoire entre 0 et max-1
		    return hasard.nextInt(max);
	  }
		  

	  
    /*
     ******************************************************* 
     *                ETAT DU PLATEAU                      *
     *******************************************************
     */
    /**
     * Test method for {@link jeu.Plateau#Plateau(int, int)}.
     */
    @Test
    void testPlateauIntInt() {
      final String attendu = """
			+----------+
			|          |
			|          |
			|          |
			|          |
			|          |
			+----------+,Joueur0:0:-1:-1:100:0,Joueur1:1:-1:-1:100:0,Joueur2:2:-1:-1:100:0,Joueur3:3:-1:-1:100:0,0,120\
			""";
      assertEquals(attendu, new Plateau(30, 5).toString());// 30 tours <=> 120 coups
      assertEquals(5, new Plateau(1, 5).donneTaille());
      assertEquals(0, new Plateau(2, 4).donneTourCourant());
      assertEquals(42, new Plateau(42, 4).donneNombreDeTours());
      assertEquals(8, new Plateau(8, 4).donneNombreDeTours());
      assertEquals(44, new Plateau(44, 4).donneNombreDeTours());
      assertEquals(0, new Plateau(10, 15).donneJoueurCourant());
    }

    /**
     * Test method for {@link jeu.Plateau#Plateau(int, java.lang.String)}.
     */
    @Test
    void testPlateauIntString() {
      /* plateau1 */
      assertEquals(description1 + joueurs1, plateau1.toString());    
      assertEquals(6, plateau1.donneNombreDeTours());
      assertEquals(8, plateau1.donneTaille());
      assertEquals(0, plateau1.donneTourCourant());
      assertEquals(0, plateau1.donneJoueurCourant());
      
      /* plateau2 */
      assertEquals(9, plateau2.donneTaille());
      assertEquals(0, plateau2.donneTourCourant());
      assertEquals(10, plateau2.donneNombreDeTours());
      assertEquals(0, plateau2.donneJoueurCourant());
      assertEquals(description2 + joueurs2, plateau2.toString());
  
      /* plateauPetit */
      assertEquals(8, plateauPetit.donneTaille());
      assertEquals(0, plateauPetit.donneTourCourant());
      assertEquals(4, plateauPetit.donneNombreDeTours());
      assertEquals(0, plateauPetit.donneJoueurCourant());
      assertEquals(Plateau.TEXTE_PLATEAU_ANORMAL_8X8_1 + joueursPetitPlateau, plateauPetit.toString());
  
      /* plateauGrand1 */
      assertEquals(20, plateauGrand1.donneTaille());
      assertEquals(0, plateauGrand1.donneTourCourant());
      assertEquals(8, plateauGrand1.donneNombreDeTours());
      assertEquals(0, plateauGrand1.donneJoueurCourant());
      assertEquals(Plateau.TEXTE_PLATEAU_NORMAL_20X20_28P_4R_142I + joueursPlateauParDefaut, plateauGrand1.toString());
    }
    
    /**
     * Test method for {@link jeu.Plateau#donneJoueur(int)}.
     */
    @Test
    void testDonneJoueur() {
      assertEquals("Joueur Joueur0:0:1:1:100:0", plateau1.donneJoueur(0).toString());
      assertEquals("Joueur Joueur1:1:4:0:100:0", plateau1.donneJoueur(1).toString());
      assertEquals("Joueur Joueur2:2:6:1:100:0", plateau1.donneJoueur(2).toString());
      assertEquals("Joueur Joueur3:3:1:5:100:0", plateau1.donneJoueur(3).toString());
      assertEquals(new Point(1, 1), plateau1.donneJoueur(0).donnePosition());
      assertEquals(new Point(4, 0), plateau1.donneJoueur(1).donnePosition());
      assertEquals(new Point(6, 1), plateau1.donneJoueur(2).donnePosition());
      assertEquals(new Point(1, 5), plateau1.donneJoueur(3).donnePosition());
      final int indice = entierAleatoire(4);
      assertEquals("Joueur" + indice, plateau1.donneJoueur(indice).donneNom());
      assertEquals(100, plateau1.donneJoueur(indice).donneRessources());
      assertEquals(0, plateau1.donneJoueur(indice).donnePoints());
      assertEquals(indice, plateau1.donneJoueur(indice).donneRang());
      final String [] couleurJoueurs = { "bleu", "vert", "rouge", "jaune" };
      assertEquals(couleurJoueurs[indice], plateau1.donneJoueur(indice).donneCouleur());
    }
    
    /**
     * Test method for {@link jeu.Plateau#donneJoueurEnPosition(Point)}.
     */
    @Test
    void testDonneJoueurEnPositionPoint() {   
      assertEquals("Joueur0", plateau1.donneJoueurEnPosition(new Point(1, 1)).donneNom());
      assertEquals("Joueur1", plateau1.donneJoueurEnPosition(new Point(4, 0)).donneNom());
      assertEquals("Joueur2", plateau1.donneJoueurEnPosition(new Point(6, 1)).donneNom());
      assertEquals("Joueur3", plateau1.donneJoueurEnPosition(new Point(1, 5)).donneNom());
      assertNull(plateau1.donneJoueurEnPosition(new Point(0, 0))); // $$
      assertNull(plateau1.donneJoueurEnPosition(new Point(0, 2))); // ##
      assertNull(plateau1.donneJoueurEnPosition(new Point(2, 0))); // P-
      assertNull(plateau1.donneJoueurEnPosition(new Point(4, 1))); // P-
      assertNull(plateau1.donneJoueurEnPosition(new Point(7, 6))); // P3
      assertNull(plateau1.donneJoueurEnPosition(new Point(1, 0))); // vide
    }
    
    /**
     * Test method for {@link jeu.Plateau#donneJoueurEnPosition(int,int)}.
     */
   @Test
    void testDonneJoueurEnPositionIntInt() {   
      assertEquals("Joueur0", plateau1.donneJoueurEnPosition(1, 1).donneNom());
      assertEquals("Joueur1", plateau1.donneJoueurEnPosition(4, 0).donneNom());
      assertEquals("Joueur2", plateau1.donneJoueurEnPosition(6, 1).donneNom());
      assertEquals("Joueur3", plateau1.donneJoueurEnPosition(1, 5).donneNom());
      assertNull(plateau1.donneJoueurEnPosition(0, 0)); // $$
      assertNull(plateau1.donneJoueurEnPosition(0, 2)); // ##
      assertNull(plateau1.donneJoueurEnPosition(2, 0)); // P-
      assertNull(plateau1.donneJoueurEnPosition(4, 1)); // P-
      assertNull(plateau1.donneJoueurEnPosition(7, 6)); // P3
      assertNull(plateau1.donneJoueurEnPosition(1, 0)); // vide
    }
    
   /**
    * Test method for {@link jeu.Plateau#nombreDUnitesDeProductionJoueur(int)}.
    */
    @Test
    void testNombreDeruchersJoueurInt() {     	
        /* plateau1 
         * +----------------+ 
         * |$$  P-  @2  $$  | 
         * |  @1    P-  @3$$| 
         * |##  ##  ##  ##  | 
         * |  ##  ##  ##  ##|
         * |              P1| 
         * |  @4  P1        | 
         * |              P3| 
         * |  ##P1######P1  | 
         * +----------------+
         */     
      assertEquals(4, plateau1.nombreDUnitesDeProductionJoueur(0));       
      assertEquals(0, plateau1.nombreDUnitesDeProductionJoueur(1));     
      assertEquals(1, plateau1.nombreDUnitesDeProductionJoueur(2));     
      assertEquals(0, plateau1.nombreDUnitesDeProductionJoueur(3));
     }
    
    /**
     * Test method for {@link jeu.Plateau#coordonneeValide(int, int)}.
     */
    @Test
    void testCoordonneeValide() {
      assertFalse(plateau1.coordonneeValide(8, 0));
      assertFalse(plateau1.coordonneeValide(3, 8));
      assertFalse(plateau1.coordonneeValide(-1, -1));
      assertTrue(plateau1.coordonneeValide(0, 0));
      assertTrue(plateau1.coordonneeValide(7, 7));
      assertTrue(plateau1.coordonneeValide(5, 2));
    }
     
    /**
     * Test method for
     * {@link jeu.Plateau#joueurPeutAllerIci(int, int, boolean, boolean)}.
     */
    @Test
    void testJoueurPeutAllerIci() { 
      // sur le séminaire $$ en (0,0) => toujours
      assertTrue(plateau1.joueurPeutAllerIci(0, 0, false, false));
      assertTrue(plateau1.joueurPeutAllerIci(0, 0, true, true));
      assertTrue(plateau1.joueurPeutAllerIci(0, 0, false, true));
      assertTrue(plateau1.joueurPeutAllerIci(0, 0, true, false));
  
      // sur la case vide en (2,6) => toujours
      assertTrue(plateau1.joueurPeutAllerIci(2, 6, false, false));
      assertTrue(plateau1.joueurPeutAllerIci(2, 6, true, true));
      assertTrue(plateau1.joueurPeutAllerIci(2, 6, false, true));
      assertTrue(plateau1.joueurPeutAllerIci(2, 6, true, false));
  
      // sur la zone infranchissable ## en (0,2) => jamais
      assertFalse(plateau1.joueurPeutAllerIci(0, 2, false, false));
      assertFalse(plateau1.joueurPeutAllerIci(0, 2, true, true));
      assertFalse(plateau1.joueurPeutAllerIci(0, 2, false, true));
      assertFalse(plateau1.joueurPeutAllerIci(0, 2, true, false));
  
      // sur le joueur @1 en (1,1)
      assertTrue(plateau1.joueurPeutAllerIci(1, 1, false, false));
      assertFalse(plateau1.joueurPeutAllerIci(1, 1, true, true)); // deja un joueur
      assertTrue(plateau1.joueurPeutAllerIci(1, 1, false, true));
      assertFalse(plateau1.joueurPeutAllerIci(1, 1, true, false)); // deja un joueur
  
      // sur la rucher P- en (2,0)
      assertTrue(plateau1.joueurPeutAllerIci(2, 0, false, false));
      assertFalse(plateau1.joueurPeutAllerIci(2, 0, true, true)); // deja un rucher
      assertFalse(plateau1.joueurPeutAllerIci(2, 0, false, true)); // deja un rucher
      assertTrue(plateau1.joueurPeutAllerIci(2, 0, true, false));
  
      // sur la rucher P3 en (7,6)
      assertTrue(plateau1.joueurPeutAllerIci(7, 6, false, false));
      assertFalse(plateau1.joueurPeutAllerIci(7, 6, true, true)); // deja un rucher
      assertFalse(plateau1.joueurPeutAllerIci(7, 6, false, true)); // deja un rucher
      assertTrue(plateau1.joueurPeutAllerIci(7, 6, true, false));
      
      // sur le rucher P- en (4, 1)
      assertTrue(plateau1.joueurPeutAllerIci(4, 1, false, false));
      assertFalse(plateau1.joueurPeutAllerIci(4, 1, true, true)); // deja un rucher
      assertFalse(plateau1.joueurPeutAllerIci(4, 1, false, true)); // deja un rucher
      assertTrue(plateau1.joueurPeutAllerIci(4, 1, true, false));
  
      // sur le rucher P1 en (7, 4)
      assertTrue(plateau1.joueurPeutAllerIci(7, 4, false, false));
      assertFalse(plateau1.joueurPeutAllerIci(7, 4, true, true)); // deja un rucher
      assertFalse(plateau1.joueurPeutAllerIci(7, 4, false, true)); // deja un rucher
      assertTrue(plateau1.joueurPeutAllerIci(7, 4, true, false));
  
      // sur une case en dehors du plateau => jamais
      assertFalse(plateau1.joueurPeutAllerIci(8, 0, false, false));
      assertFalse(plateau1.joueurPeutAllerIci(3, 8, false, false));
      assertFalse(plateau1.joueurPeutAllerIci(-1, -1, false, false));
    }
    
    /**
     * Random test method for {@link jeu.Plateau#donneContenuCellule(Point)}
     * et {@link jeu.Plateau#donneContenuCellule(int, int)}.
     */
    @RepeatedTest(10)
    void testDonneContenuCellulePoint() {
      final int x = entierAleatoire(8);
      final int y = entierAleatoire(8);
      assertEquals(plateau1.donneContenuCellule(x, y), 
          plateau1.donneContenuCellule(new Point(x, y)));
    }

  }
  
  
  @Nested
  class TypesDeCellules {
    /*
    ******************************************************* 
    *                TYPES DE CELLULE                     *
    *******************************************************
    */
    /**
     * Test des cases infranchissables.
     */
    @Test
    public void testZoneInfranchissablePlateau() {
      /* plateau1 
       * +----------------+ 
       * |$$  P-  @2  $$  | 
       * |  @1    P-  @3$$| 
       * |##  ##  ##  ##  | 
       * |  ##  ##  ##  ##|
       * |              P1| 
       * |  @4  P1        | 
       * |              P3| 
       * |  ##P1######P1  | 
       * +----------------+
       */     
      assertTrue(Plateau.contientUneZoneInfranchissable(
          plateau1.donneContenuCellule(2, 0))); //rucher
      assertTrue(Plateau.contientUneZoneInfranchissable(
          plateau1.donneContenuCellule(4, 1))); //rucher
      assertTrue(Plateau.contientUneZoneInfranchissable(
          plateau1.donneContenuCellule(0, 2))); //arbre
      
      assertFalse(Plateau.contientUneZoneInfranchissable(
          plateau1.donneContenuCellule(0, 0))); //séminaire
      assertFalse(Plateau.contientUneZoneInfranchissable(
          plateau1.donneContenuCellule(1, 0))); //vide
      assertFalse(Plateau.contientUneZoneInfranchissable(
          plateau1.donneContenuCellule(4, 0))); //joueur 2
      
      final int contenuCellule = plateau1.donneContenuCellule(0, 2);
      assertEquals(Plateau.ENDROIT_INFRANCHISSABLE, contenuCellule & Plateau.MASQUE_ENDROITS);
      assertNotEquals(0, contenuCellule & Plateau.ENDROIT_INFRANCHISSABLE);  
    }
    
    /**
     * Test des cases de départ de joueur.
     */
    @Test
    public void testZoneDepart() {
      /* Le plateau de jeu : @2 en (4,0)
       * +----------------+ 
       * |$$  P-  @2  $$  | 
       * |  @1    P-  @3$$| 
       * |##  ##  ##  ##  | 
       * |  ##  ##  ##  ##|
       * |              P1| 
       * |  @4  P1        | 
       * |              P3| 
       * |  ##P1######P1  | 
       * +----------------+
       */     
      assertTrue(Plateau.contientUnDepart(plateau1.donneContenuCellule(1, 1))); //depart joueur 1
      assertFalse(Plateau.contientUnDepart(plateau1.donneContenuCellule(2, 0))); //rucher
      assertFalse(Plateau.contientUnDepart(plateau1.donneContenuCellule(4, 1))); //rucher
      assertFalse(Plateau.contientUnDepart(plateau1.donneContenuCellule(0, 2))); //arbre
      assertFalse(Plateau.contientUnDepart(plateau1.donneContenuCellule(0, 0))); //séminaire
      assertFalse(Plateau.contientUnDepart(plateau1.donneContenuCellule(1, 0))); //vide 
      
      assertEquals(1, Plateau.donneProprietaireDuPointDeDepart(
          plateau1.donneContenuCellule(1, 1))); //depart joueur 1
      assertEquals(2, Plateau.donneProprietaireDuPointDeDepart(
          plateau1.donneContenuCellule(4, 0))); //depart joueur 1
      assertEquals(3, Plateau.donneProprietaireDuPointDeDepart(
          plateau1.donneContenuCellule(6, 1))); //depart joueur 1
      assertEquals(4, Plateau.donneProprietaireDuPointDeDepart(
          plateau1.donneContenuCellule(1, 5))); //depart joueur 1
      assertEquals(0, Plateau.donneProprietaireDuPointDeDepart(
          plateau1.donneContenuCellule(0, 0))); //séminaire
      assertEquals(0, Plateau.donneProprietaireDuPointDeDepart(
          plateau1.donneContenuCellule(1, 0))); //vide
    }
    
    /**
     * Test des cases séminaire.
     */
    @Test
    public void testZoneSeminaire() {
      /* Le plateau de jeu : @2 en (4,0)
       * +----------------+ 
       * |$$  P-  @2  $$  | 
       * |  @1    P-  @3$$| 
       * |##  ##  ##  ##  | 
       * |  ##  ##  ##  ##|
       * |              P1| 
       * |  @4  P1        | 
       * |              P3| 
       * |  ##P1######P1  | 
       * +----------------+
       */     
      assertTrue(Plateau.contientUneUniteDeRessourcage(plateau1.donneContenuCellule(0, 0))); //séminaire
      assertFalse(Plateau.contientUneUniteDeRessourcage(plateau1.donneContenuCellule(1, 1))); //depart joueur 1
      assertFalse(Plateau.contientUneUniteDeRessourcage(plateau1.donneContenuCellule(2, 0))); //rucher
      assertFalse(Plateau.contientUneUniteDeRessourcage(plateau1.donneContenuCellule(4, 1))); //rucher
      assertFalse(Plateau.contientUneUniteDeRessourcage(plateau1.donneContenuCellule(0, 2))); //arbre
      assertFalse(Plateau.contientUneUniteDeRessourcage(plateau1.donneContenuCellule(1, 0))); //vide
      
      final int contenuCellule = plateau1.donneContenuCellule(0, 0); // séminaire
      assertEquals(Plateau.ENDROIT_RESSOURCE, contenuCellule & Plateau.MASQUE_ENDROITS);
      assertNotEquals(0, contenuCellule & Plateau.ENDROIT_RESSOURCE);
    }
  
    /**
     * Test des cases vides.
     */
    @Test
    public void testZoneVide() {
      /* Le plateau de jeu : @2 en (4,0)
       * +----------------+ 
       * |$$  P-  @2  $$  | 
       * |  @1    P-  @3$$| 
       * |##  ##  ##  ##  | 
       * |  ##  ##  ##  ##|
       * |              P1| 
       * |  @4  P1        | 
       * |              P3| 
       * |  ##P1######P1  | 
       * +----------------+
       */        
      assertTrue(Plateau.contientUneZoneVide(plateau1.donneContenuCellule(1, 0))); //vide
      assertFalse(Plateau.contientUneZoneVide(plateau1.donneContenuCellule(0, 0))); //séminaire
      assertFalse(Plateau.contientUneZoneVide(plateau1.donneContenuCellule(1, 1))); //depart joueur 1
      assertFalse(Plateau.contientUneZoneVide(plateau1.donneContenuCellule(2, 0))); //rucher
      assertFalse(Plateau.contientUneZoneVide(plateau1.donneContenuCellule(4, 1))); //rucher
      assertFalse(Plateau.contientUneZoneVide(plateau1.donneContenuCellule(0, 2))); //arbre
      
      final int contenuCellule = plateau1.donneContenuCellule(1, 0); // vide
      assertEquals(Plateau.ENDROIT_VIDE, contenuCellule & Plateau.MASQUE_ENDROITS);
    }
  
    /**
     * Test des cases contenant un joueur.
     */
    @Test
    public void testZoneJoueur() {
      /* Le plateau de jeu : @2 en (4,0)
       * +----------------+ 
       * |$$  P-  @2  $$  | 
       * |  @1    P-  @3$$| 
       * |##  ##  ##  ##  | 
       * |  ##  ##  ##  ##|
       * |              P1| 
       * |  @4  P1        | 
       * |              P3| 
       * |  ##P1######P1  | 
       * +----------------+
       */        
      assertTrue(Plateau.contientUnJoueur(plateau1.donneContenuCellule(1, 1))); // joueur @1
      assertTrue(Plateau.contientUnJoueur(plateau1.donneContenuCellule(4, 0))); // joueur @2
      assertTrue(Plateau.contientUnJoueur(plateau1.donneContenuCellule(6, 1))); // joueur @2
      assertTrue(Plateau.contientUnJoueur(plateau1.donneContenuCellule(1, 5))); // joueur @4
      assertFalse(Plateau.contientUnJoueur(plateau1.donneContenuCellule(1, 0))); //vide
      assertFalse(Plateau.contientUnJoueur(plateau1.donneContenuCellule(0, 0))); //séminaire
      assertFalse(Plateau.contientUnJoueur(plateau1.donneContenuCellule(2, 0))); //rucher
      assertFalse(Plateau.contientUnJoueur(plateau1.donneContenuCellule(4, 1))); //rucher
      assertFalse(Plateau.contientUnJoueur(plateau1.donneContenuCellule(0, 2))); //arbre
  
      assertTrue(Plateau.contientLeJoueur(plateau1.donneContenuCellule(4, 0), 1));
      assertFalse(Plateau.contientLeJoueur(plateau1.donneContenuCellule(4, 0), 3));
      assertFalse(Plateau.contientLeJoueur(plateau1.donneContenuCellule(1, 0), 0)); //vide
      assertFalse(Plateau.contientLeJoueur(plateau1.donneContenuCellule(0, 0), 1)); //séminaire
      assertFalse(Plateau.contientLeJoueur(plateau1.donneContenuCellule(2, 0), 2)); //rucher
      assertFalse(Plateau.contientLeJoueur(plateau1.donneContenuCellule(4, 1), 3)); //rucher
      assertFalse(Plateau.contientLeJoueur(plateau1.donneContenuCellule(0, 2), 0)); //arbre
      
      final int contenuCellule = plateau1.donneContenuCellule(1, 1); // joueur @1
      assertNotEquals(0, contenuCellule & Plateau.MASQUE_PRESENCE_JOUEUR); // joueur
      assertEquals(Plateau.PRESENCE_JOUEUR1, contenuCellule & Plateau.MASQUE_PRESENCE_JOUEUR); // joueur @1
      assertNotEquals(Plateau.PRESENCE_JOUEUR2, contenuCellule & Plateau.MASQUE_PRESENCE_JOUEUR);
      assertNotEquals(Plateau.PRESENCE_JOUEUR3, contenuCellule & Plateau.MASQUE_PRESENCE_JOUEUR);
      assertNotEquals(Plateau.PRESENCE_JOUEUR4, contenuCellule & Plateau.MASQUE_PRESENCE_JOUEUR);
      assertEquals(Plateau.ENDROIT_DEPART_J1, contenuCellule & Plateau.MASQUE_ENDROITS);
      assertNotEquals(Plateau.ENDROIT_DEPART_J2, contenuCellule & Plateau.MASQUE_ENDROITS);

    }
    
    /**
     * Tests des cases de type rucher.
     */
    @Test
    public void testZoneRucher() {
      /* plateau1 
       * +----------------+ 
       * |$$  P-  @2  $$  | 
       * |  @1    P-  @3$$| 
       * |##  ##  ##  ##  | 
       * |  ##  ##  ##  ##|
       * |              P1| 
       * |  @4  P1        | 
       * |              P3| 
       * |  ##P1######P1  | 
       * +----------------+
       */        
      assertTrue(Plateau.contientUneUniteDeProduction(plateau1.donneContenuCellule(2, 0))); // fabrqiue
      assertTrue(Plateau.contientUneUniteDeProduction(plateau1.donneContenuCellule(4, 1))); // rucher
      assertFalse(Plateau.contientUneUniteDeProduction(plateau1.donneContenuCellule(1, 0))); // vide
      assertFalse(Plateau.contientUneUniteDeProduction(plateau1.donneContenuCellule(0, 0))); // séminaire
      assertFalse(Plateau.contientUneUniteDeProduction(plateau1.donneContenuCellule(1, 1))); //depart joueur 1
      assertFalse(Plateau.contientUneUniteDeProduction(plateau1.donneContenuCellule(0, 2))); //arbre
         
      assertTrue(Plateau.contientUneUniteDeProductionQuiNeLuiAppartientPas(plateau1.donneJoueur(0), 
          plateau1.donneContenuCellule(2, 0))); // P-
      assertTrue(Plateau.contientUneUniteDeProductionQuiNeLuiAppartientPas(plateau1.donneJoueur(1), 
          plateau1.donneContenuCellule(4, 1))); // P-
      assertTrue(Plateau.contientUneUniteDeProductionQuiNeLuiAppartientPas(plateau1.donneJoueur(2), 
          plateau1.donneContenuCellule(7, 4))); // P1
      assertFalse(Plateau.contientUneUniteDeProductionQuiNeLuiAppartientPas(plateau1.donneJoueur(0), 
          plateau1.donneContenuCellule(7, 4))); // P1
      assertTrue(Plateau.contientUneUniteDeProductionQuiNeLuiAppartientPas(plateau1.donneJoueur(3), 
          plateau1.donneContenuCellule(7, 6))); // P3
      assertFalse(Plateau.contientUneUniteDeProductionQuiNeLuiAppartientPas(plateau1.donneJoueur(2), 
          plateau1.donneContenuCellule(7, 6))); // P3
      
      assertEquals(plateau1.donneJoueur(2).donneRang() + 1, 
          Plateau.donneUtilisateurDeLUniteDeProduction(plateau1.donneContenuCellule(7, 6))); //P3
      assertEquals(plateau1.donneJoueur(0).donneRang() + 1, 
          Plateau.donneUtilisateurDeLUniteDeProduction(plateau1.donneContenuCellule(7, 4))); //P1
      assertEquals(0, Plateau.donneUtilisateurDeLUniteDeProduction(plateau1.donneContenuCellule(4, 1))); //P-
      assertEquals(0, Plateau.donneUtilisateurDeLUniteDeProduction(plateau1.donneContenuCellule(2, 0))); //P-
      assertEquals(-1, Plateau.donneUtilisateurDeLUniteDeProduction(plateau1.donneContenuCellule(2, 2))); //arbre
      assertEquals(-1, Plateau.donneUtilisateurDeLUniteDeProduction(plateau1.donneContenuCellule(0, 0))); //vide
      assertEquals(-1, Plateau.donneUtilisateurDeLUniteDeProduction(
          plateau1.donneContenuCellule(1, 1))); //joueur @1   
      assertEquals(-1, Plateau.donneUtilisateurDeLUniteDeProduction(
          plateau1.donneContenuCellule(0, 0))); //séminaire
           
      assertEquals(Plateau.ENDROIT_PRODUCTION_J1, Plateau.donneCelluleDeLUniteDeProductionDuJoueur(0));
      assertEquals(Plateau.ENDROIT_PRODUCTION_J2, Plateau.donneCelluleDeLUniteDeProductionDuJoueur(1));
      assertEquals(Plateau.ENDROIT_PRODUCTION_J3, Plateau.donneCelluleDeLUniteDeProductionDuJoueur(2));
      assertEquals(Plateau.ENDROIT_PRODUCTION_J4, Plateau.donneCelluleDeLUniteDeProductionDuJoueur(3));
      assertEquals(Plateau.ENDROIT_PRODUCTION_LIBRE, Plateau.donneCelluleDeLUniteDeProductionDuJoueur(-1));
 
      int contenuCellule = plateau1.donneContenuCellule(2, 0); // P-
      assertNotEquals(0, contenuCellule & Plateau.MASQUE_ENDROIT_PRODUCTION);
      assertEquals(Plateau.ENDROIT_PRODUCTION_LIBRE, contenuCellule & Plateau.MASQUE_ENDROITS);
      assertNotEquals(Plateau.ENDROIT_PRODUCTION_J1, contenuCellule & Plateau.MASQUE_ENDROITS);
      assertNotEquals(Plateau.ENDROIT_PRODUCTION_J2, contenuCellule & Plateau.MASQUE_ENDROITS);
      assertNotEquals(Plateau.ENDROIT_PRODUCTION_J3, contenuCellule & Plateau.MASQUE_ENDROITS);
      assertNotEquals(Plateau.ENDROIT_PRODUCTION_J4, contenuCellule & Plateau.MASQUE_ENDROITS);

      contenuCellule = plateau1.donneContenuCellule(4, 1); // P-
      assertNotEquals(0, contenuCellule & Plateau.MASQUE_ENDROIT_PRODUCTION);
      assertEquals(Plateau.ENDROIT_PRODUCTION_LIBRE, contenuCellule & Plateau.MASQUE_ENDROITS);
      assertNotEquals(Plateau.ENDROIT_PRODUCTION_J1, contenuCellule & Plateau.MASQUE_ENDROITS);
      assertNotEquals(Plateau.ENDROIT_PRODUCTION_J2, contenuCellule & Plateau.MASQUE_ENDROITS);
      assertNotEquals(Plateau.ENDROIT_PRODUCTION_J3, contenuCellule & Plateau.MASQUE_ENDROITS);
      assertNotEquals(Plateau.ENDROIT_PRODUCTION_J4, contenuCellule & Plateau.MASQUE_ENDROITS);

      contenuCellule = plateau1.donneContenuCellule(7, 4); // P1
      assertNotEquals(0, contenuCellule & Plateau.MASQUE_ENDROIT_PRODUCTION);
      assertNotEquals(Plateau.ENDROIT_PRODUCTION_LIBRE, contenuCellule & Plateau.MASQUE_ENDROITS);
      assertEquals(Plateau.ENDROIT_PRODUCTION_J1, contenuCellule & Plateau.MASQUE_ENDROITS);
      assertNotEquals(Plateau.ENDROIT_PRODUCTION_J2, contenuCellule & Plateau.MASQUE_ENDROITS);
      assertNotEquals(Plateau.ENDROIT_PRODUCTION_J3, contenuCellule & Plateau.MASQUE_ENDROITS);
      assertNotEquals(Plateau.ENDROIT_PRODUCTION_J4, contenuCellule & Plateau.MASQUE_ENDROITS);

      contenuCellule = plateau1.donneContenuCellule(7, 6); // P3
      assertNotEquals(0, contenuCellule & Plateau.MASQUE_ENDROIT_PRODUCTION);
      assertNotEquals(Plateau.ENDROIT_PRODUCTION_LIBRE, contenuCellule & Plateau.MASQUE_ENDROITS);
      assertNotEquals(Plateau.ENDROIT_PRODUCTION_J1, contenuCellule & Plateau.MASQUE_ENDROITS);
      assertNotEquals(Plateau.ENDROIT_PRODUCTION_J2, contenuCellule & Plateau.MASQUE_ENDROITS);
      assertEquals(Plateau.ENDROIT_PRODUCTION_J3, contenuCellule & Plateau.MASQUE_ENDROITS);
      assertNotEquals(Plateau.ENDROIT_PRODUCTION_J4, contenuCellule & Plateau.MASQUE_ENDROITS);
      
      contenuCellule = plateau1.donneContenuCellule(0, 0); // séminaire
      assertEquals(0, contenuCellule & Plateau.MASQUE_ENDROIT_PRODUCTION);
      assertNotEquals(Plateau.ENDROIT_PRODUCTION_LIBRE, contenuCellule & Plateau.MASQUE_ENDROITS);
      assertNotEquals(Plateau.ENDROIT_PRODUCTION_J1, contenuCellule & Plateau.MASQUE_ENDROITS);
      assertNotEquals(Plateau.ENDROIT_PRODUCTION_J2, contenuCellule & Plateau.MASQUE_ENDROITS);
      assertNotEquals(Plateau.ENDROIT_PRODUCTION_J3, contenuCellule & Plateau.MASQUE_ENDROITS);
      assertNotEquals(Plateau.ENDROIT_PRODUCTION_J4, contenuCellule & Plateau.MASQUE_ENDROITS);
   }
  }
  
  @Nested
  class RepresentationTextuelleDuTableau {
    /*
    ******************************************************* 
    *     REPRESENTATION TEXTUELLE DU PLATEAU             *
    *******************************************************
    */
     /**
     * Test method for {@link jeu.Plateau#toJavaCode()}.
     */
    @Test
    void testToJavaCode() {
        final String attendu = """
				String tableau_ascii = "+----------------+\\n"+
				"|$$  P-  @2  $$  |\\n"+
				"|  @1    P-  @3$$|\\n"+
				"|##  ##  ##  ##  |\\n"+
				"|  ##  ##  ##  ##|\\n"+
				"|              P1|\\n"+
				"|  @4    P1      |\\n"+
				"|              P3|\\n"+
				"|  ##P1######P1  |\\n"+
				"+----------------+\\n"+
				""";
       assertEquals(attendu, plateau1.toJavaCode());
     }
  
    /**
     * Test method for {@link jeu.Plateau#encode()}.
     */
    @Test
    void testEncode() {
      final String description = description1.replace('\n', 'X');
      assertEquals(description1 + joueurs1, plateau1.encode('\n'));
      assertEquals(description + joueurs1, plateau1.encode('X'));
    }
  
    /**
     * Test method for
     * {@link jeu.Plateau#decode(java.lang.String, java.lang.String)}.
     */
    @Test
    void testDecodeStringString() {
      Plateau nouveau = Plateau.decode(plateau1.encode('X'), "X");
      assertEquals(plateau1, nouveau);
      assertEquals(0, nouveau.donneJoueurCourant());
  
      nouveau = Plateau.decode(plateau2.encode('Q'), "Q");
      assertEquals(plateau2, nouveau);
      assertEquals(0, nouveau.donneJoueurCourant());
    }
  
    /**
     * Test method for {@link jeu.Plateau#equals()}.
     */
    @Test
    void testEquals() {
      assertEquals(new Plateau(6, description1), plateau1);
      assertNotEquals(new Plateau(4, description1), plateau1);
    }
  }

  @Nested
  class RechercheDeCheminsOuDeZones {
    /*
     ********************************************************* 
     *     RECHERCHE DE CHEMIN OU DE ZONE ENVIRONNANTE       *
     *********************************************************
     **/
  
    /**
     * Test method for {@link jeu.Plateau#cherche(java.awt.Point, int, int)}.
     */
    @Test
    void testCherche1() {
      /* plateau1 
       * +----------------+ 
       * |$$  P-  @2  $$  | 
       * |  @1    P-  @3$$| 
       * |##  ##  ##  ##  | 
       * |  ##  ##  ##  ##|
       * |              P1| 
       * |  @4  P1        | 
       * |              P3| 
       * |  ##P1######P1  | 
       * +----------------+
       */     
      final Point positionJoueur4 = new Point(1, 5);
      assertTrue(Plateau.contientLeJoueur(plateau1.donneContenuCellule(positionJoueur4), 3));
      int rayon;
      /* recherches autour de la case @4 avec un rayon 0 => le joueur */
      rayon = 0;
      assertEquals("{1=[], 2=[], 4=[java.awt.Point[x=1,y=5]]}",
          plateau1.cherche(positionJoueur4, rayon, Plateau.CHERCHE_TOUT).toString());
      assertEquals("{1=[], 2=[], 4=[java.awt.Point[x=1,y=5]]}",
          plateau1.cherche(positionJoueur4, rayon, Plateau.CHERCHE_JOUEUR).toString());
      assertEquals("{1=[], 2=[], 4=[]}", 
          plateau1.cherche(positionJoueur4, rayon, Plateau.CHERCHE_PRODUCTION).toString());
      assertEquals("{1=[], 2=[], 4=[]}", 
          plateau1.cherche(positionJoueur4, rayon, Plateau.CHERCHE_RESSOURCE).toString());
  
      /* recherches autour de la case @4 avec un rayon 1 => le joueur */
      rayon = 1;
      assertEquals("{1=[], 2=[], 4=[java.awt.Point[x=1,y=5]]}",
          plateau1.cherche(positionJoueur4, rayon, Plateau.CHERCHE_TOUT).toString());
      assertEquals("{1=[], 2=[], 4=[java.awt.Point[x=1,y=5]]}",
          plateau1.cherche(positionJoueur4, rayon, Plateau.CHERCHE_JOUEUR).toString());
      assertEquals("{1=[], 2=[], 4=[]}", 
          plateau1.cherche(positionJoueur4, rayon, Plateau.CHERCHE_PRODUCTION).toString());
      assertEquals("{1=[], 2=[], 4=[]}", 
          plateau1.cherche(positionJoueur4, rayon, Plateau.CHERCHE_RESSOURCE).toString());
  
      /* recherches autour de la case @4 avec un rayon 2 => le joueur & P1 */
      rayon = 2;
      assertEquals("{1=[], 2=[java.awt.Point[x=2,y=7]], 4=[java.awt.Point[x=1,y=5]]}",
          plateau1.cherche(positionJoueur4, rayon, Plateau.CHERCHE_TOUT).toString());
      assertEquals("{1=[], 2=[], 4=[java.awt.Point[x=1,y=5]]}",
          plateau1.cherche(positionJoueur4, rayon, Plateau.CHERCHE_JOUEUR).toString());
      assertEquals("{1=[], 2=[java.awt.Point[x=2,y=7]], 4=[]}",
          plateau1.cherche(positionJoueur4, rayon, Plateau.CHERCHE_PRODUCTION).toString());
      assertEquals("{1=[], 2=[], 4=[]}", 
          plateau1.cherche(positionJoueur4, rayon, Plateau.CHERCHE_RESSOURCE).toString());
  
      /* recherches autour de la case @4 avec un rayon 3 => le joueur & 2P1 */
      rayon = 3;
      assertEquals("{1=[], 2=[java.awt.Point[x=4,y=5], java.awt.Point[x=2,y=7]], "
          + "4=[java.awt.Point[x=1,y=5]]}",
          plateau1.cherche(positionJoueur4, rayon, Plateau.CHERCHE_TOUT).toString());
      assertEquals("{1=[], 2=[], 4=[java.awt.Point[x=1,y=5]]}",
          plateau1.cherche(positionJoueur4, rayon, Plateau.CHERCHE_JOUEUR).toString());
      assertEquals("{1=[], 2=[java.awt.Point[x=4,y=5], java.awt.Point[x=2,y=7]], 4=[]}",
          plateau1.cherche(positionJoueur4, rayon, Plateau.CHERCHE_PRODUCTION).toString());
      assertEquals("{1=[], 2=[], 4=[]}", 
          plateau1.cherche(positionJoueur4, rayon, Plateau.CHERCHE_RESSOURCE).toString());
   
      /* recherches autour de la case @4 avec un rayon 5 => 4 joueurs & 2 séminaires & 5 ruchers */
      rayon = 5;
      assertEquals(
          "{1=[java.awt.Point[x=0,y=0], java.awt.Point[x=6,y=0]], "
              + "2=[java.awt.Point[x=2,y=0], java.awt.Point[x=4,y=1], java.awt.Point[x=4,y=5], "
              + "java.awt.Point[x=2,y=7], java.awt.Point[x=6,y=7]], "
              + "4=[java.awt.Point[x=4,y=0], java.awt.Point[x=1,y=1], java.awt.Point[x=6,y=1], "
              + "java.awt.Point[x=1,y=5]]}",
          plateau1.cherche(positionJoueur4, rayon, Plateau.CHERCHE_TOUT).toString());
      assertEquals("{1=[java.awt.Point[x=0,y=0], java.awt.Point[x=6,y=0]], 2=[], 4=[]}",
          plateau1.cherche(positionJoueur4, rayon, Plateau.CHERCHE_RESSOURCE).toString());
      assertEquals(2, plateau1.cherche(positionJoueur4, rayon, 
          Plateau.CHERCHE_TOUT).get(Plateau.CHERCHE_RESSOURCE).size());
      assertEquals(5, plateau1.cherche(positionJoueur4, rayon, 
          Plateau.CHERCHE_TOUT).get(Plateau.CHERCHE_PRODUCTION).size());
      assertEquals(4, plateau1.cherche(positionJoueur4, rayon, 
          Plateau.CHERCHE_TOUT).get(Plateau.CHERCHE_JOUEUR).size());
      
      /* recherches autour de la case @4 avec un rayon 6 => 4 joueurs & 3 séminaires & 7 ruchers */
      rayon = 6;
      assertEquals(3, plateau1.cherche(positionJoueur4, rayon, 
          Plateau.CHERCHE_TOUT).get(Plateau.CHERCHE_RESSOURCE).size());
      assertEquals(7, plateau1.cherche(positionJoueur4, rayon, 
          Plateau.CHERCHE_TOUT).get(Plateau.CHERCHE_PRODUCTION).size());
      assertEquals(4, plateau1.cherche(positionJoueur4, rayon, 
          Plateau.CHERCHE_TOUT).get(Plateau.CHERCHE_JOUEUR).size());
    }
  
    /**
     * Test method for {@link jeu.Plateau#cherche(java.awt.Point, int, int)}.
     */
    @Test
    void testCherche2() {
      final Point position = new Point(4, 1);
      assertTrue(Plateau.contientUneUniteDeProduction(plateau1.donneContenuCellule(position)));
      final int rayon;
  
      /* recherches autour de la case P- en (2,0) avec un rayon 0 */
      rayon = 0;
      assertEquals("{1=[], 2=[java.awt.Point[x=4,y=1]], 4=[]}",
          plateau1.cherche(position, rayon, Plateau.CHERCHE_TOUT).toString());
      assertEquals("{1=[], 2=[], 4=[]}", 
          plateau1.cherche(position, rayon, Plateau.CHERCHE_JOUEUR).toString());
      assertEquals("{1=[], 2=[java.awt.Point[x=4,y=1]], 4=[]}",
          plateau1.cherche(position, rayon, Plateau.CHERCHE_PRODUCTION).toString());
      assertEquals("{1=[], 2=[], 4=[]}", 
          plateau1.cherche(position, rayon, Plateau.CHERCHE_RESSOURCE).toString());
  
      /* recherches autour de la case P- en (2,0) avec d'autres rayons */
      assertEquals("{1=[], 2=[], 4=[java.awt.Point[x=4,y=0]]}",
          plateau1.cherche(position, 1, Plateau.CHERCHE_JOUEUR).toString());
      assertEquals(2, plateau1.cherche(position, 2, 
          Plateau.CHERCHE_JOUEUR).get(Plateau.CHERCHE_JOUEUR).size());
      assertTrue(plateau1.cherche(position, 2, 
          Plateau.CHERCHE_JOUEUR).get(Plateau.CHERCHE_JOUEUR).contains(new Point(4, 0)));
      assertTrue(plateau1.cherche(position, 2, 
          Plateau.CHERCHE_JOUEUR).get(Plateau.CHERCHE_JOUEUR).contains(new Point(6, 1)));
      assertEquals("{1=[], 2=[], 4=[]}", 
          plateau1.cherche(position, 1, Plateau.CHERCHE_RESSOURCE).toString());
      assertEquals("{1=[java.awt.Point[x=6,y=0]], 2=[], 4=[]}",
          plateau1.cherche(position, 2, Plateau.CHERCHE_RESSOURCE).toString());
      assertEquals(2, plateau1.cherche(position, 3, 
          Plateau.CHERCHE_RESSOURCE).get(Plateau.CHERCHE_RESSOURCE).size());
      assertTrue(plateau1.cherche(position, 3, 
          Plateau.CHERCHE_RESSOURCE).get(Plateau.CHERCHE_RESSOURCE).contains(new Point(6, 0)));
      assertTrue(plateau1.cherche(position, 3, 
          Plateau.CHERCHE_RESSOURCE).get(Plateau.CHERCHE_RESSOURCE).contains(new Point(7, 1)));
      assertEquals(2, plateau1.cherche(position, 2, 
          Plateau.CHERCHE_PRODUCTION).get(Plateau.CHERCHE_PRODUCTION).size());
      assertTrue(plateau1.cherche(position, 2, 
          Plateau.CHERCHE_PRODUCTION).get(Plateau.CHERCHE_PRODUCTION).contains(new Point(2, 0)));
      assertTrue(plateau1.cherche(position, 2, 
          Plateau.CHERCHE_PRODUCTION).get(Plateau.CHERCHE_PRODUCTION).contains(new Point(4, 1)));
    }
  
    /**
     * Test method for
     * {@link jeu.Plateau#donneCheminEntre(java.awt.Point, java.awt.Point)}.
     */
    @Test
    void testDonneCheminEntre() {
      /*depart ou arrivee null */
      assertNull(plateau1.donneCheminEntre(null, new Point(6, 1)));
      assertNull(plateau1.donneCheminEntre(new Point(0, 5), null));
      assertNull(plateau1.donneCheminEntre(null, null));
  
      /*depart ou arrivee hors limites */
      assertNull(plateau1.donneCheminEntre(new Point(0, 5), new Point(8, 2)));
      assertNull(plateau1.donneCheminEntre(new Point(0, -1), new Point(0, 1)));
  
      /*debut ok et arrivee infranchissable */
      assertTrue(Plateau.contientUneZoneInfranchissable(plateau1.donneContenuCellule(0, 2)));
      assertEquals("[(1,1), (1,2), (0,2)]", 
          plateau1.donneCheminEntre(new Point(1, 0), new Point(0, 2)).toString());
      assertTrue(Plateau.contientUneZoneInfranchissable(plateau1.donneContenuCellule(1, 3)));
      assertEquals("[(1,1), (1,2), (1,3)]", 
          plateau1.donneCheminEntre(new Point(1, 0), new Point(1, 3)).toString());
  
      /*depart ou arrivee ok */
      assertTrue(Plateau.contientUneZoneVide(plateau1.donneContenuCellule(1, 0)));
      assertTrue(Plateau.contientLeJoueur(plateau1.donneContenuCellule(6, 1), 2));
      assertEquals("[(1,1), (2,1), (3,1), (3,0), (4,0), (5,0), (5,1), (6,1)]",
          plateau1.donneCheminEntre(new Point(1, 0), new Point(6, 1)).toString());
      assertEquals("[(5,1), (5,0), (4,0), (3,0), (3,1), (2,1), (1,1), (1,0)]",
          plateau1.donneCheminEntre(new Point(6, 1), new Point(1, 0)).toString());
  
      assertTrue(Plateau.contientUneZoneVide(plateau1.donneContenuCellule(1, 2)));
      assertTrue(Plateau.contientUneZoneVide(plateau1.donneContenuCellule(3, 2)));
      assertEquals("[(1,1), (2,1), (3,1), (3,2)]",
          plateau1.donneCheminEntre(new Point(1, 2), new Point(3, 2)).toString());
      assertEquals("[(3,1), (2,1), (1,1), (1,2)]",
          plateau1.donneCheminEntre(new Point(3, 2), new Point(1, 2)).toString());
  
      /*debut infranchissable et arrivee ok */
      assertTrue(Plateau.contientUneZoneInfranchissable(plateau1.donneContenuCellule(0, 2)));
      assertEquals("[(1,2), (1,1), (2,1), (3,1), (3,2)]",
          plateau1.donneCheminEntre(new Point(0, 2), new Point(3, 2)).toString());
  
      assertTrue(Plateau.contientUneZoneInfranchissable(plateau1.donneContenuCellule(1, 3)));
      assertEquals("[(1,2), (1,1), (1,0)]", 
          plateau1.donneCheminEntre(new Point(1, 3), new Point(1, 0)).toString());
  
      /* chemin impossible */
      assertNull(plateau1.donneCheminEntre(new Point(1, 0), new Point(1, 5)));
      assertNull(plateau1.donneCheminEntre(new Point(1, 1), new Point(1, 4)));
      
      /* chemin qui doit éviter des ruchers */
      assertEquals( "[(2,1), (3,1), (3,0), (4,0), (5,0), (6,0), (6,1), (7,1)]", 
          plateau1.donneCheminEntre(new Point(1,1), new Point(7,1)).toString());
      assertEquals( "[(2,1), (3,1), (3,0), (4,0), (5,0), (6,0)]", 
          plateau1.donneCheminEntre(new Point(1,1), new Point(6,0)).toString());
      assertNull( plateau1.donneCheminEntre(new Point(1,1), new Point(6,3)));
     
      /* chemin qui traverse les séminaires */
      assertNotNull( plateau1.donneCheminEntre(new Point(1,1), new Point(7,2)));
      assertNotNull( plateau1.donneCheminEntre(new Point(4,0), new Point(7,0)));
     }
  
    /**
     * Test method for
     * {@link jeu.Plateau#donneCheminAvecObstaclesSupplementaires(java.awt.Point, 
     * java.awt.Point, java.util.ArrayList<jeu.astar.Node>)}.
     */
    @Test
    void testDonneCheminAvecObstaclesSupplementaires() {
      List<Noeud> obstacles;
  
      /*depart ou arrivee null */
      assertNull(plateau1.donneCheminAvecObstaclesSupplementaires(null, new Point(6, 1), null));
      assertNull(plateau1.donneCheminAvecObstaclesSupplementaires(new Point(0, 5), null, null));
      assertNull(plateau1.donneCheminAvecObstaclesSupplementaires(null, null, null));
  
      /*depart ou arrivee hors limites */
      assertNull(plateau1.donneCheminAvecObstaclesSupplementaires(new Point(0, 5), 
          new Point(8, 2), null));
      assertNull(plateau1.donneCheminAvecObstaclesSupplementaires(new Point(0, -1), 
          new Point(0, 1), null));
  
      /*debut ok et arrivee infranchissable */
      assertTrue(Plateau.contientUneZoneInfranchissable(plateau1.donneContenuCellule(0, 2)));
      assertEquals("[(1,1), (1,2), (0,2)]",
          plateau1.donneCheminAvecObstaclesSupplementaires(new Point(1, 0), 
              new Point(0, 2), null).toString());
      assertNotEquals("[(1,1), (1,2), (0,2)]",
          plateau1
              .donneCheminAvecObstaclesSupplementaires(new Point(1, 0), new Point(0, 2), 
            		  Collections.singletonList(new Noeud(1, 2)))
              .toString());
      assertEquals("[(1,1), (1,2), (0,2)]",
          plateau1
              .donneCheminAvecObstaclesSupplementaires(new Point(1, 0), new Point(0, 2), 
            		  Collections.singletonList(new Noeud(0, 2)))
              .toString());
  
      assertTrue(Plateau.contientUneZoneInfranchissable(plateau1.donneContenuCellule(1, 3)));
      assertEquals("[(1,1), (1,2), (1,3)]",
          plateau1.donneCheminAvecObstaclesSupplementaires(new Point(1, 0), 
              new Point(1, 3), null).toString());
      assertEquals("[(1,1), (1,2), (1,3)]", 
          plateau1.donneCheminAvecObstaclesSupplementaires(new Point(1, 0),
              new Point(1, 3), Arrays.asList(new Noeud(4, 4), new Noeud(5, 2))).toString());
  
      /*depart ou arrivee ok */
      assertTrue(Plateau.contientUneZoneVide(plateau1.donneContenuCellule(1, 0)));
      assertTrue(Plateau.contientLeJoueur(plateau1.donneContenuCellule(6, 1), 2));
      assertEquals("[(1,1), (2,1), (3,1), (3,0), (4,0), (5,0), (5,1), (6,1)]",
          plateau1.donneCheminAvecObstaclesSupplementaires(new Point(1, 0), 
              new Point(6, 1), null).toString());
      assertEquals("[(5,1), (5,0), (4,0), (3,0), (3,1), (2,1), (1,1), (1,0)]",
          plateau1.donneCheminAvecObstaclesSupplementaires(new Point(6, 1), 
              new Point(1, 0), null).toString());
      obstacles = Collections.singletonList(new Noeud(3, 1));
      assertNull(plateau1.donneCheminAvecObstaclesSupplementaires(new Point(1, 0), 
          new Point(6, 1), obstacles));
      assertNull(plateau1.donneCheminAvecObstaclesSupplementaires(new Point(6, 1), 
          new Point(1, 0), obstacles));
  
      assertTrue(Plateau.contientUneZoneVide(plateau1.donneContenuCellule(1, 2)));
      assertTrue(Plateau.contientUneZoneVide(plateau1.donneContenuCellule(3, 2)));
      assertEquals("[(1,1), (2,1), (3,1), (3,2)]",
          plateau1.donneCheminAvecObstaclesSupplementaires(new Point(1, 2), 
              new Point(3, 2), null).toString());
  
      assertEquals("[(3,1), (2,1), (1,1), (1,2)]",
          plateau1.donneCheminAvecObstaclesSupplementaires(new Point(3, 2), 
              new Point(1, 2), null).toString());
  
      assertEquals("[(3,5), (3,6), (4,6), (5,6)]",
          plateau1.donneCheminAvecObstaclesSupplementaires(new Point(3, 4), 
              new Point(5, 6), null).toString());
      obstacles = Arrays.asList(new Noeud(4, 4), new Noeud(6, 4), new Noeud(7, 4));
      assertEquals("[(3,5), (3,6), (4,6), (5,6)]",
          plateau1.donneCheminAvecObstaclesSupplementaires(new Point(3, 4), 
              new Point(5, 6), obstacles).toString());
      obstacles = Arrays.asList(new Noeud(3, 6), new Noeud(3, 1), new Noeud(3, 7));
      assertEquals("[(4,4), (5,4), (5,5), (5,6)]",
          plateau1.donneCheminAvecObstaclesSupplementaires(new Point(3, 4), 
              new Point(5, 6), obstacles).toString());
      obstacles = Arrays.asList(new Noeud(3, 6), new Noeud(5, 4));
      assertNull(plateau1.donneCheminAvecObstaclesSupplementaires(new Point(3, 4), 
          new Point(5, 6), obstacles));
  
      /*debut infranchissable et arrivee ok */
      assertTrue(Plateau.contientUneZoneInfranchissable(plateau1.donneContenuCellule(0, 2)));
      assertEquals("[(1,2), (1,1), (2,1), (3,1), (3,2)]",
          plateau1.donneCheminAvecObstaclesSupplementaires(new Point(0, 2), 
              new Point(3, 2), null).toString());
      obstacles = Arrays.asList(new Noeud(5, 5), new Noeud(2, 1));
      assertNull(plateau1.donneCheminAvecObstaclesSupplementaires(new Point(0, 2), 
          new Point(3, 2), obstacles));
  
      assertTrue(Plateau.contientUneZoneInfranchissable(plateau1.donneContenuCellule(1, 3)));
      assertEquals("[(1,2), (1,1), (1,0)]",
          plateau1.donneCheminAvecObstaclesSupplementaires(new Point(1, 3), 
              new Point(1, 0), null).toString());
      obstacles = Arrays.asList(new Noeud(1, 2), new Noeud(1, 1), new Noeud(1, 0));
      assertNull(plateau1.donneCheminAvecObstaclesSupplementaires(new Point(1, 3), 
          new Point(1, 0), obstacles));
  
      /*chemin impossible */
      assertNull(plateau1.donneCheminAvecObstaclesSupplementaires(new Point(1, 0), 
          new Point(1, 5), null));
    }
  }
  
  @Nested
  class GenerationDePlateauAleatoire {
    /*
     ******************************************************** 
     *            GENERATION DE PLATEAU ALEATOIRE           *
     ********************************************************
     */
  
    /**
     * Test method for
     * {@link jeu.Plateau#generePlateauAleatoire(int, int, int, int, int)}.
     */
    @RepeatedTest(10)
    void testGenerePlateauAleatoire() {
      Plateau nouveau = null;
      while (nouveau == null) {
        nouveau = Plateau.generePlateauAleatoire(100, 5, 2, 5, 5);
      }
      assertNotNull( nouveau);
      assertEquals(100, nouveau.donneNombreDeTours());
      assertEquals(10, nouveau.donneTaille());
      int nbRuchers = 0;
      int nbArbres = 0;
      int nbSeminaires = 0;
      int nbVides = 0;
      int nbDeparts = 0;
      for (int i = 0; i < 10; i++) {
        for (int j = 0; j < 10; j++) {
          final int n = nouveau.donneContenuCellule(i, j);
          if (Plateau.contientUneUniteDeProduction(n)) {
               nbRuchers++; 
           } else if (Plateau.contientUneZoneInfranchissable(n)) {
            nbArbres++;
          }
          if (Plateau.contientUneUniteDeRessourcage(n)) {
            nbSeminaires++;
          }
          if (Plateau.contientUneZoneVide(n)) {
            nbVides++;
          }
          if (Plateau.contientUnDepart(n)) {
            nbDeparts++;
          }
        }
      }
      assertEquals(20, nbArbres);
      assertEquals(20, nbRuchers);
      assertEquals(8, nbSeminaires);
      assertEquals(4, nbDeparts);
      assertEquals(nouveau.donneTaille() * nouveau.donneTaille(), 
          nbArbres + nbRuchers + nbSeminaires + nbDeparts + nbVides);
    }
  }
  
  @Nested
  class GenerationDePlateauDeTournoi {
    /*
     ******************************************************** 
     *    GENERATION DE PLATEAU ALEATOIRE POUR LE TOURNOI   *
     ********************************************************
     */ 
    /**
     * Test method for
     * {@link jeu.Plateau#generePlateauTournoi()}.
     */
    @RepeatedTest(10)
    void testGenerePlateauAleatoireDeTournoi() {
      final Plateau nouveau = Plateau.generePlateauTournoi();
      assertNotNull( nouveau);
      assertEquals(1000, nouveau.donneNombreDeTours());
      final int taille = nouveau.donneTaille();
      assertTrue( taille >= 20 && taille <= 30);
      int nbRuchers = 0;
      int nbArbres = 0;
      int nbSeminaires = 0;
      int nbVides = 0;
      int nbDeparts = 0;
      for (int i = 0; i < taille; i++) {
        for (int j = 0; j < taille; j++) {
          final int n = nouveau.donneContenuCellule(i, j);
          if (Plateau.contientUneUniteDeProduction(n)) {
               nbRuchers++; 
           } else if (Plateau.contientUneZoneInfranchissable(n)) {
            nbArbres++;
          } else if (Plateau.contientUneUniteDeRessourcage(n)) {
            nbSeminaires++;
          } else if (Plateau.contientUneZoneVide(n)) {
            nbVides++;
          } else if (Plateau.contientUnDepart(n)) {
            nbDeparts++;
          }
        }
      }
      assertTrue(nbArbres >= 15*4 && nbArbres <= 49*4, Integer.toString(nbArbres));
      assertTrue(nbRuchers >= 3*4 && nbRuchers <= 17*4, Integer.toString(nbRuchers));
      assertTrue(nbSeminaires >= 1*4 && nbSeminaires <= 3*4, Integer.toString(nbSeminaires));
      assertEquals(4, nbDeparts);
      assertEquals(nouveau.donneTaille() * nouveau.donneTaille(), 
          nbArbres + nbRuchers + nbSeminaires + nbDeparts + nbVides);
    }
  }

}
