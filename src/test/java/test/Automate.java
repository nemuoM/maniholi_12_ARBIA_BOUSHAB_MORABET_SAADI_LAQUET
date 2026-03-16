package test;

import jeu.Joueur;
import jeu.Plateau;

/**
 * Joueur qui joue une séquence prédéterminée d'actions.
 * 
 * Joueur utilisé par les classes de test {@link MainTestPlusCourtCheminEtRecherche}
 * {@link TestMaitreDuJeuPartie25Tours}, {@link TestSimpleMaitreDuJeuEchanges} et 
 * {@link TestSimpleMaitreDuJeuDeplacementsSansEchanges}
 * 
 * @author Lucile
 *
 */
public class Automate extends Joueur {

  /** Séquence d'actions de déplacement du joueur. */
  private final String deplacements;
  
  /** Numéro de l'action dans la séquence d'actions. */
  private int numero;

  /**
   * Crée un joueur de nom donné et de séquence d'actions donnée.
   * 
   * La séquence d'actions est une chaîne formée des caractères D (pour DROITE), G
   * (pour GAUCHE), H (pour HAUT), B (pour BAS) et . (pour RIEN). Par exemple, la
   * chaîne "D.BBH" définit la séquence d'actions : DROITE, RIEN, BAS, BAS, HAUT.
   *
   * @param nom du joueur
   * @param dep la séquence des déplacements du joueur
   */
  public Automate(String nom, String dep) {
    super(nom);
    deplacements = dep;
    numero = 0;
  }


  /**
   *  Redéfinit la méthode {@link jeu.Joueur#faitUneAction(jeu.Plateau)}.
   *  
   *  La méthode renvoie les actions dans l'ordre de la séquence deplacements.
   *  Une fois la séquence entièrement parcourue, la méthode renvoie l'action RIEN.
   *
   *  @param p le plateau de jeu.
   */
  @Override
  public Action faitUneAction(Plateau p) {
    if (numero >= deplacements.length()) {
      return Action.RIEN;
    }
    return switch (deplacements.charAt(numero++)) {
			case 'H' -> Action.HAUT;
			case 'B' -> Action.BAS;
			case 'D' -> Action.DROITE;
			case 'G' -> Action.GAUCHE;
			case '.' -> Action.RIEN;
			default -> throw new Error("Action inconnue");
	};
  }  
}
