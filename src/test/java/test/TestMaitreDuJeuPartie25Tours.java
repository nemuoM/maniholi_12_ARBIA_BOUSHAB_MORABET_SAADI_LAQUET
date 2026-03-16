package test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Point;
import java.lang.reflect.Field;
import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jeu.Joueur;
import jeu.MaitreDuJeu;
import jeu.MaitreDuJeuListener;
import jeu.Plateau;
import jeu.Joueur.Action;

/**
 * Test de la classe MaitreDuJeu (sans affichage graphique).
 * 
 * Le test est effectué sans affichage graphique.
 * Pour voir la représentation graphique du plateau utilisé dans les tests
 * ci-dessous, il suffit de lancer au préalable 
 * {@link MainTestPlusCourtCheminEtRecherche}. En faisant avancer pas à pas 
 * à chaque tour sur la fenêtre graphique on peut voir le déroulement de la partie
 * de {@link #testJoueSuivantPourPartieEn25Coups()}.
 *
 * @author lucile
 */
class TestMaitreDuJeuPartie25Tours {
  Plateau plateau;
  MaitreDuJeu jeu;
  Joueur j1;
  Joueur j2;
  Joueur j3;
  Joueur j4;
  boolean informeSpectateurs;    
  boolean affichePlateau;
  
  @BeforeEach
  void setUp() {
	  plateau = new Plateau(MaitreDuJeu.NB_TOUR_PAR_DEFAUT, Plateau.TEXTE_PLATEAU_ANORMAL_8X8_2);
	  jeu = new MaitreDuJeu(plateau);
  }
  
  @Test
  void testDonneGainRessourcageParTour() {
      int[] gains = config.ConfigurationJeu.GAIN_RESSOURCES_PAR_TOUR_PENDANT_ACQUISITION;
      int nbPeriodes = gains.length;
      int nbTours = plateau.donneNombreCoupsTotal() / 4;
      assertEquals( MaitreDuJeu.NB_TOUR_PAR_DEFAUT, nbTours); 

      int taillePeriode = nbTours / nbPeriodes;

      for (int i = 0; i < nbPeriodes; i++) {
          int tourTest = i * taillePeriode; // début de la période
          int gain = jeu.donneGainRessourcageParTour(tourTest);
          assertEquals(gains[i], gain, "Gain incorrect pour la période " + i);
      }
  }
  
  @Test
  void testBornesGainRessourcage() {
      int[] gains = config.ConfigurationJeu.GAIN_RESSOURCES_PAR_TOUR_PENDANT_ACQUISITION;
      int nbPeriodes = gains.length;
      int nbTours = plateau.donneNombreCoupsTotal() / 4;
      assertEquals( MaitreDuJeu.NB_TOUR_PAR_DEFAUT, nbTours); 

      int nbMinimumTours = nbTours / nbPeriodes;
      int nbToursSup = nbTours % nbPeriodes;

      int tour = 0;

      for (int i = 0; i < nbPeriodes; i++) {
          int duree = nbMinimumTours + (i < nbToursSup ? 1 : 0);

          // début de période
          assertEquals(gains[i], jeu.donneGainRessourcageParTour(tour));

          // fin de période
          assertEquals(gains[i], jeu.donneGainRessourcageParTour(tour + duree - 1));

          tour += duree;
      }
  }
  
  @Test
  void testBornesGainRessourcageRempliesParConstructeur() throws Exception {
      // Accès au champ privé par réflexion
      Field champBornes = MaitreDuJeu.class.getDeclaredField("bornesGainRessourcage");
      champBornes.setAccessible(true);

      int[] bornes = (int[]) champBornes.get(jeu);

      // Vérifications simples
      assertNotNull(bornes);

      int nbPeriodes = config.ConfigurationJeu.GAIN_RESSOURCES_PAR_TOUR_PENDANT_ACQUISITION.length;
      assertEquals(nbPeriodes, bornes.length);

      // Vérifie que les bornes sont croissantes
      for (int i = 1; i < bornes.length; i++) {
          assertTrue(bornes[i] > bornes[i-1]);
      }

      // Vérifie que la dernière borne correspond au nombre total de tours
      int nbToursTotal = plateau.donneNombreCoupsTotal() / 4;
      assertEquals(nbToursTotal, bornes[bornes.length - 1]);
      
      champBornes.setAccessible(false);
  }

  /**
   * Lancement d'une partie en 25 tours sans fenêtre graphique.
   */
  private void lancementPartie25ToursSansFenetreGraphique() {
    jeu.addEcouteurDuJeu(new MaitreDuJeuListener() {
      @Override
      public void unJeuAChange(MaitreDuJeu arg0) {
      }
      
      @Override
      public void nouveauMessage(MaitreDuJeu arg0, String arg1) {
         System.out.println("\n_____ " + arg0 + "_____" + arg1 + "\n");
         if (affichePlateau) {
           	 System.out.println(jeu.donnePlateau());
          }
      }

      @Override
      public void afficheSymbole(MaitreDuJeu arg0, Symboles arg1, Point arg2, int arg3, int arg4) {
      }
    });
    informeSpectateurs = true; // true pour afficher les infos de déroulement de la partie   
    affichePlateau = false; // true pour afficher, en plus, l'état du plateau à chaque tour
    if (informeSpectateurs) {
    	System.out.println(jeu.donnePlateau());
    }
  }

    
  /**
   * Test d'une partie en 25 coups par joueur où chaque joueur joue la séquence
   * d'actions suivante :
   *  _______________________________________________________________________________
   * |   action |   1    |   2   |    3   |    4   |   5    |    6   |    7  | ...   | 
   * |1er joueur| Droite | Haut  | Droite | Gauche | Gauche | Rien   | Rien  | Rien  | 
   * |2e joueur | Rien   | Haut  | Bas    | Droite | Bas    | Rien   | Rien  | Rien  |  
   * |3e joueur | Rien   | Rien  | Droite | Gauche | Haut   | Gauche | Bas   | Rien  | 
   * |4e joueur | Rien   | Rien  | Droite | Droite | Droite | Rien   | Rien  | Rien  | 
   *  _______________________________________________________________________________ .
   *  
   *  Attention : aucun test d'augmentation de ressources effectif en site de ressourçage.
   */
  @Test
  void testJoueSuivantPourPartieEn25Coups() {

	lancementPartie25ToursSansFenetreGraphique();   
    jeu.metJoueurEnPosition(0, new Automate("A", "DHDGG"));
    jeu.metJoueurEnPosition(1, new Automate("B", ".HBDB"));
    jeu.metJoueurEnPosition(2, new Automate("C", "..DGHGB"));
    jeu.metJoueurEnPosition(3, new Automate("D", "..DDD"));
    j1 = plateau.donneJoueur(0);
    j2 = plateau.donneJoueur(1);
    j3 = plateau.donneJoueur(2);
    j4 = plateau.donneJoueur(3);
    assertEquals("Joueur A:0:1:1:100:0", j1.toString()); 
    assertEquals("Joueur B:1:4:0:100:0", j2.toString()); 
    assertEquals("Joueur C:2:6:1:100:0", j3.toString()); 
    assertEquals("Joueur D:3:1:5:100:0", j4.toString());

 
     /* Premier tour du jeu : j1=D autres=R */
    for (int i = 0; i < 4; i++) {
      assertEquals(i, plateau.donneJoueurCourant());
      assertEquals(0, plateau.donneTourCourant());
      jeu.joueSuivant(informeSpectateurs, false);
    }
    assertEquals("Joueur A:0:2:1:99:4", j1.toString());// A est allé sur vide (-1E +4P)
    assertEquals("Joueur B:1:4:0:100:0", j2.toString());// rien 
    assertEquals("Joueur C:2:6:1:100:1", j3.toString());// rien (+1P) 
    assertEquals("Joueur D:3:1:5:100:0", j4.toString());// rien
    assertEquals(4, plateau.nombreDUnitesDeProductionJoueur(0));
    assertEquals(0, plateau.nombreDUnitesDeProductionJoueur(1));
    assertEquals(1, plateau.nombreDUnitesDeProductionJoueur(2));
    assertEquals(0, plateau.nombreDUnitesDeProductionJoueur(3));
    assertEquals(1, plateau.donneTourCourant());
    assertEquals(0, plateau.donneJoueurCourant());
    assertEquals( "[0, 0, 0, 0]", Arrays.toString( plateau.donneToursRestantEchange()));

    /* Deuxieme tour du jeu : j1=H j2=H autres=R */
    for (int i = 0; i < 4; i++) {
      jeu.joueSuivant(informeSpectateurs, false);
    }
    assertEquals("Joueur A:0:2:1:79:9", j1.toString());// A a pris Uprod libre (-20E +5P)
    assertEquals("Joueur B:1:4:0:90:0", j2.toString());// B s'est cogné (-10E)
    assertEquals("Joueur C:2:6:1:100:2", j3.toString());// rien (+1P) 
    assertEquals("Joueur D:3:1:5:100:0", j4.toString());// rien
    assertEquals(5, plateau.nombreDUnitesDeProductionJoueur(0));
    assertEquals(0, plateau.nombreDUnitesDeProductionJoueur(1));
    assertEquals(1, plateau.nombreDUnitesDeProductionJoueur(2));
    assertEquals(0, plateau.nombreDUnitesDeProductionJoueur(3));
    assertEquals(2, plateau.donneTourCourant());
    assertEquals(0, plateau.donneJoueurCourant());
    assertEquals( "[0, 0, 0, 0]", Arrays.toString( plateau.donneToursRestantEchange()));

    /* Troisieme tour du jeu : j2=B autres=D */
    for (int i = 0; i < 4; i++) {
      jeu.joueSuivant(informeSpectateurs, false);
    }
    assertEquals("Joueur A:0:3:1:78:14", j1.toString());// A est allé sur vide (-1E +5P)
    assertEquals("Joueur B:1:4:0:70:1", j2.toString());// B a pris Uprod libre (-20E +1P)
    assertEquals("Joueur C:2:7:1:100:2", j3.toString());// C est allé sur Uressource (+40E)
    assertEquals("Joueur D:3:2:5:99:0", j4.toString());// D est allé sur vide (-1E)
    assertEquals(5, plateau.nombreDUnitesDeProductionJoueur(0));
    assertEquals(1, plateau.nombreDUnitesDeProductionJoueur(1));
    assertEquals(1, plateau.nombreDUnitesDeProductionJoueur(2));
    assertEquals(0, plateau.nombreDUnitesDeProductionJoueur(3));
    assertEquals(3, plateau.donneTourCourant());
    assertEquals(0, plateau.donneJoueurCourant());
    assertEquals( "[0, 0, 0, 0]", Arrays.toString( plateau.donneToursRestantEchange()));

    /* Quatrieme tour du jeu : j1=G j2=D j3=G j4=D */
    for (int i = 0; i < 4; i++) {
      jeu.joueSuivant(informeSpectateurs, false);
    }
    assertEquals("Joueur A:0:2:1:77:19", j1.toString());// A est allé sur vide (-1E +5P) 
    assertEquals("Joueur B:1:5:0:69:2", j2.toString()); // B est allé sur vide (-1E +1P) 
    assertEquals("Joueur C:2:6:1:99:3", j3.toString());// C est allé sur vide (-1E +1P) 
    assertEquals("Joueur D:3:3:5:98:0", j4.toString()); // D est allé sur vide (-1E)
    assertEquals(5, plateau.nombreDUnitesDeProductionJoueur(0));
    assertEquals(1, plateau.nombreDUnitesDeProductionJoueur(1));
    assertEquals(1, plateau.nombreDUnitesDeProductionJoueur(2));
    assertEquals(0, plateau.nombreDUnitesDeProductionJoueur(3));
    assertEquals(4, plateau.donneTourCourant());
    assertEquals(0, plateau.donneJoueurCourant());
    assertEquals( "[0, 0, 0, 0]", Arrays.toString( plateau.donneToursRestantEchange()));

    /* Cinquieme tour du jeu : j1=G j2=B (j3 echange) j4=D */
    for (int i = 0; i < 4; i++) {
      jeu.joueSuivant(informeSpectateurs, false);
    }
    assertEquals("Joueur A:0:1:1:76:24", j1.toString()); // A est allé sur vide (-1E +5P) 
    assertEquals("Joueur B:1:5:1:68:3", j2.toString()); // B est allé sur vide à côté de C (-1E +1P) => bientôt échange (B,C)
    assertEquals("Joueur C:2:6:1:100:3", j3.toString()); // C tour 1 de manille (+2E)
    assertEquals("Joueur D:3:3:5:78:1", j4.toString());// D a pris Uprod libre (-20E +1P)
    assertEquals(4, plateau.nombreDUnitesDeProductionJoueur(0));
    assertEquals(1, plateau.nombreDUnitesDeProductionJoueur(1));
    assertEquals(1, plateau.nombreDUnitesDeProductionJoueur(2));
    assertEquals(1, plateau.nombreDUnitesDeProductionJoueur(3));
    assertEquals(5, plateau.donneTourCourant());
    assertEquals(0, plateau.donneJoueurCourant());
    assertEquals( "[0, 10, 9, 0]", Arrays.toString( plateau.donneToursRestantEchange()));

    /* Pendant 8 tours, du 6e tour au 13e tour du jeu : (j2&j3 échange) autres=R */
    for (int tour = 5; tour < 13; tour++) {
    	for (int i = 0; i < 4; i++) {
    		jeu.joueSuivant(informeSpectateurs, false);
    	}
    }
    assertEquals("Joueur A:0:1:1:76:56", j1.toString()); // rien (+4P)x8tours
    assertEquals("Joueur B:1:5:1:84:3", j2.toString()); // B 8tours d'échange (+2E)x8tours
    assertEquals("Joueur C:2:6:1:100:3", j3.toString());// C 8tours d'échange (+2E)x8tours
    assertEquals("Joueur D:3:3:5:78:9", j4.toString()); // rien (+1P)x8tours
    assertEquals(4, plateau.nombreDUnitesDeProductionJoueur(0));
    assertEquals(1, plateau.nombreDUnitesDeProductionJoueur(1));
    assertEquals(1, plateau.nombreDUnitesDeProductionJoueur(2));
    assertEquals(1, plateau.nombreDUnitesDeProductionJoueur(3));
    assertEquals(13, plateau.donneTourCourant());
    assertEquals( "[0, 2, 1, 0]", Arrays.toString( plateau.donneToursRestantEchange()));

    /* 14e tour du jeu : (j2&j3 échange) autres=R */
    for (int i = 0; i < 4; i++) {
      jeu.joueSuivant(informeSpectateurs, false);
    }
    assertEquals("Joueur A:0:1:1:76:60", j1.toString());// rien (+4P) 
    assertEquals("Joueur B:1:5:1:86:3", j2.toString()); // B 9ème tour d'échange (+2E)
    assertEquals("Joueur C:2:6:1:100:3", j3.toString());// C 10ème tour d'échange (+2E)
    assertEquals("Joueur D:3:3:5:78:10", j4.toString()); // rien (+1P)
    assertEquals(4, plateau.nombreDUnitesDeProductionJoueur(0));
    assertEquals(1, plateau.nombreDUnitesDeProductionJoueur(1));
    assertEquals(1, plateau.nombreDUnitesDeProductionJoueur(2));
    assertEquals(1, plateau.nombreDUnitesDeProductionJoueur(3));
    assertEquals(14, plateau.donneTourCourant());
    assertEquals( "[0, 1, -5, 0]", Arrays.toString( plateau.donneToursRestantEchange()));
 
    /* 15e tour du jeu : j3=H autres=R */
    for (int i = 0; i < 4; i++) {
      jeu.joueSuivant(informeSpectateurs, false);
    }
    assertEquals("Joueur A:0:1:1:76:64", j1.toString());// rien (+4P) 
    assertEquals("Joueur B:1:5:1:88:3", j2.toString()); // B 10ème tour d'échange (+2E)
    assertEquals("Joueur C:2:6:0:100:3", j3.toString());// C est allé sur Uressource (+40E) 
    assertEquals("Joueur D:3:3:5:78:11", j4.toString()); // rien (+1P)
    assertEquals(4, plateau.nombreDUnitesDeProductionJoueur(0));
    assertEquals(1, plateau.nombreDUnitesDeProductionJoueur(1));
    assertEquals(1, plateau.nombreDUnitesDeProductionJoueur(2));
    assertEquals(1, plateau.nombreDUnitesDeProductionJoueur(3));
    assertEquals(15, plateau.donneTourCourant());
    assertEquals( "[0, -5, -4, 0]", Arrays.toString( plateau.donneToursRestantEchange()));
    
    /* 16e tour du jeu : j3=G autres=R */
    for (int i = 0; i < 4; i++) {
      jeu.joueSuivant(informeSpectateurs, false);
    }
    assertEquals("Joueur A:0:1:1:76:68", j1.toString());// rien (+4P) 
    assertEquals("Joueur B:1:5:1:88:4", j2.toString()); // rien (+1P) 
    assertEquals("Joueur C:2:5:0:99:4", j3.toString());// C est allé case vide à côté de B => pas d'échange
    assertEquals("Joueur D:3:3:5:78:12", j4.toString()); // rien (+1P)
    assertEquals(4, plateau.nombreDUnitesDeProductionJoueur(0));
    assertEquals(1, plateau.nombreDUnitesDeProductionJoueur(1));
    assertEquals(1, plateau.nombreDUnitesDeProductionJoueur(2));
    assertEquals(1, plateau.nombreDUnitesDeProductionJoueur(3));
    assertEquals(16, plateau.donneTourCourant());
    assertEquals( "[0, -4, -3, 0]", Arrays.toString( plateau.donneToursRestantEchange()));
    
    /* 17e tour du jeu : j3=B autres=R */
    for (int i = 0; i < 4; i++) {
      jeu.joueSuivant(informeSpectateurs, false);
    }
    assertEquals("Joueur A:0:1:1:76:72", j1.toString());// rien (+4P) 
    assertEquals("Joueur B:1:5:1:88:5", j2.toString()); // rien (+1P) 
    assertEquals("Joueur C:2:5:0:89:5", j3.toString());// C se cogne (-10E +1P)
    assertEquals("Joueur D:3:3:5:78:13", j4.toString()); // rien (+1P)
    assertEquals(4, plateau.nombreDUnitesDeProductionJoueur(0));
    assertEquals(1, plateau.nombreDUnitesDeProductionJoueur(1));
    assertEquals(1, plateau.nombreDUnitesDeProductionJoueur(2));
    assertEquals(1, plateau.nombreDUnitesDeProductionJoueur(3));
    assertEquals(17, plateau.donneTourCourant());
    assertEquals( "[0, -3, -2, 0]", Arrays.toString( plateau.donneToursRestantEchange()));   
    
    /* Pendant 2 tours, du 18e tour au 19e tour du jeu : tous=R */
    for (int tour = 18; tour < 20; tour++) {
    	for (int i = 0; i < 4; i++) {
    		assertEquals( Action.RIEN, jeu.joueSuivant(informeSpectateurs, false));    	
    	}
    }
    assertEquals("Joueur A:0:1:1:76:80", j1.toString()); // rien (+4P)x2tours
    assertEquals("Joueur B:1:5:1:88:7", j2.toString()); // rien (+1P)x2tours
    assertEquals("Joueur C:2:5:0:89:7", j3.toString()); // rien (+1P)x2tours
    assertEquals("Joueur D:3:3:5:78:15", j4.toString()); // rien (+1P)x2tours
    assertEquals(4, plateau.nombreDUnitesDeProductionJoueur(0));
    assertEquals(1, plateau.nombreDUnitesDeProductionJoueur(1));
    assertEquals(1, plateau.nombreDUnitesDeProductionJoueur(2));
    assertEquals(1, plateau.nombreDUnitesDeProductionJoueur(3));
    assertEquals(19, plateau.donneTourCourant());
    assertEquals( "[0, -1, 0, 0]", Arrays.toString( plateau.donneToursRestantEchange()));

    /* 20e tour du jeu : (j3 échange) autres=R */
   	for (int i = 0; i < 4; i++) {
		assertEquals( Action.RIEN, jeu.joueSuivant(informeSpectateurs, false));    	
   	}
    assertEquals("Joueur A:0:1:1:76:84", j1.toString()); // rien (+4P)
    assertEquals("Joueur B:1:5:1:88:8", j2.toString()); // B est à côté de C (+1P) => bientôt échange (B,C)
    assertEquals("Joueur C:2:5:0:91:7", j3.toString()); // C 1er tour d'échange (+2E)
    assertEquals("Joueur D:3:3:5:78:16", j4.toString()); // rien (+1P)
    assertEquals(4, plateau.nombreDUnitesDeProductionJoueur(0));
    assertEquals(1, plateau.nombreDUnitesDeProductionJoueur(1));
    assertEquals(1, plateau.nombreDUnitesDeProductionJoueur(2));
    assertEquals(1, plateau.nombreDUnitesDeProductionJoueur(3));
    assertEquals(20, plateau.donneTourCourant());
    assertEquals( "[0, 10, 9, 0]", Arrays.toString( plateau.donneToursRestantEchange()));

    /* Pendant 5 tours, du 21e tour au 25e tour du jeu : (j2&j3 échange) autres=R */
    for (int tour = 21; tour < 26; tour++) {
    	for (int i = 0; i < 4; i++) {
    		assertEquals( Action.RIEN, jeu.joueSuivant(informeSpectateurs, false));    	
    	}
    }
    assertEquals("Joueur A:0:1:1:76:104", j1.toString()); // rien (+4P)x5tours
    assertEquals("Joueur B:1:5:1:98:8", j2.toString()); // B 5tours d'échange (+2E)x5tours
    assertEquals("Joueur C:2:5:0:100:7", j3.toString()); // C 5tours d'échange (+2E)x5tours
    assertEquals("Joueur D:3:3:5:78:21", j4.toString()); // rien (+1P)x5tours
    assertEquals(4, plateau.nombreDUnitesDeProductionJoueur(0));
    assertEquals(1, plateau.nombreDUnitesDeProductionJoueur(1));
    assertEquals(1, plateau.nombreDUnitesDeProductionJoueur(2));
    assertEquals(1, plateau.nombreDUnitesDeProductionJoueur(3));
    assertEquals(25, plateau.donneTourCourant());
    assertEquals( "[0, 5, 4, 0]", Arrays.toString( plateau.donneToursRestantEchange()));
  }
}
