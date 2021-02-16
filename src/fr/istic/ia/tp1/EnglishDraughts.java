package fr.istic.ia.tp1;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Implementation of the English Draughts game.
 * @author vdrevell
 *
 */
public class EnglishDraughts extends Game {
	/** 
	 * The checker board
	 */
	CheckerBoard board;
	
	/** 
	 * The {@link PlayerId} of the current player
	 * {@link PlayerId#ONE} corresponds to the whites
	 * {@link PlayerId#TWO} corresponds to the blacks
	 */
	PlayerId playerId;
	
	/**
	 * The current game turn (incremented each time the whites play)
	 */
	int nbTurn;
	
	/**
	 * The number of consecutive moves played only with kings and without capture
	 * (used to decide equality)
	 */
	int nbKingMovesWithoutCapture;
	
	/**
	 * Class representing a move in the English draughts game
	 * A move is an ArrayList of Integers, corresponding to the successive tile numbers (Manouri notation)
	 * toString is overrided to provide Manouri notation output.
	 * @author vdrevell
	 *
	 */
	class DraughtsMove extends ArrayList<Integer> implements Game.Move {

		private static final long serialVersionUID = -8215846964873293714L;

		@Override
		public String toString() {
			Iterator<Integer> it = this.iterator();
			Integer from = it.next();
			StringBuffer sb = new StringBuffer();
			sb.append(from);
			while (it.hasNext()) {
				Integer to = it.next();
				if (board.neighborDownLeft(from)==to || board.neighborUpLeft(from)==to
						|| board.neighborDownRight(from)==to || board.neighborUpRight(from)==to) {
					sb.append('-');
				}
				else {
					sb.append('x');
				}
				sb.append(to);
				from = to;
			}
			return sb.toString();
		}
	}
	
	/**
	 * The default constructor: initializes a game on the standard 8x8 board.
	 */
	public EnglishDraughts() {
		this(8);
	}
	
	/**
	 * Constructor with custom boardSize (to play on a boardSize x boardSize checkerBoard).
	 * @param boardSize See {@link CheckerBoard#CheckerBoard(int)} for valid board sizes. 
	 */
	public EnglishDraughts(int boardSize) {
		this.board = new CheckerBoard(boardSize);
		this.playerId = PlayerId.ONE;
		this.nbTurn = 1;
		this.nbKingMovesWithoutCapture = 0;
	}
	
	/**
	 * Copy constructor
	 * @param d The game to copy
	 */
	EnglishDraughts(EnglishDraughts d) {
		this.board = d.board.clone();
		this.playerId = d.playerId;
		this.nbTurn = d.nbTurn;
		this.nbKingMovesWithoutCapture = d.nbKingMovesWithoutCapture;
	}
	
	@Override
	public EnglishDraughts clone() {
		return new EnglishDraughts(this);
	}
	
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(nbTurn);
		sb.append(". ");
		sb.append(this.playerId==PlayerId.ONE?"W":"B");
		sb.append(":");
		sb.append(board.toString());
		return sb.toString();
	}
	
	@Override
	public String playerName(PlayerId playerId) {
		switch (playerId) {
		case ONE:
			return "Player with the whites";
		case TWO:
			return "Player with the blacks";
		case NONE:
		default:
			return "Nobody";
		}
	}
	
	@Override
	public String view() {
		return board.boardView() + "Turn #" + nbTurn + ". " + playerName(playerId) + " plays.\n";
	}
	
	/**
	 * Check if a tile is empty
	 * @param square Tile number
	 * @return
	 */
	boolean isEmpty(int square) {
		return board.isEmpty(square);
	}
	/**
	 * Check if a tile is owned by adversary
	 * @param square Tile number
	 * @return
	 */
	boolean isAdversary(int square) {
		if (playerId.equals(PlayerId.ONE)) {
			return board.isBlack(square);
		} else {
			return board.isWhite(square);
		}
	}
	/**
	 * Check if a tile is owned by the current player
	 * @param square Tile number
	 * @return
	 */
	boolean isMine(int square) {
		if (playerId.equals(PlayerId.ONE)) {
			return board.isWhite(square);
		} else {
			return board.isBlack(square);
		}
	}
	
	/** 
	 * Retrieve the list of positions of the pawns owned by the current player
	 * @return The list of current player pawn positions
	 */
	ArrayList<Integer> myPawns() {
		if (playerId.equals(PlayerId.ONE)) {
			return board.getWhitePawns();
		} else {
			return board.getBlackPawns();
		}
	}

	/**
	 * Liste les sauts possibles depuis une case. Prends en compte le fait que ce soit un roi ou non afin de savoir
	 * si il peut capturer des deux sens. Il prends aussi en paramètres la dernière destination afin d'éviter toute
	 * répétition.
	 * @param pawn
	 * @param isKing
	 * @param lastDestination
	 * @return
	 */
	List<Integer> listeDestinationsSautsPossiblesDepuisCase(int pawn, boolean isKing, int lastDestination) {
		ArrayList<Integer> dest = new ArrayList<>();
		// check if the player is white so that it goes to the right direction
		if (playerId.equals(PlayerId.ONE)) { //White
			int upLeft = board.neighborUpLeft(pawn);
			int upRight = board.neighborUpRight(pawn);
			//check if the upleft neighbour is black and that the pawn can take it
			//System.out.println(upLeft);
			//System.out.println(upRight);
			if (upLeft > 0 && board.neighborUpLeft(upLeft) > 0 && board.isBlack(upLeft) && board.isEmpty(board.neighborUpLeft(upLeft))) {
				if (board.neighborUpLeft(upLeft) != lastDestination)
					dest.add(board.neighborUpLeft(upLeft));
			}
			//check if the upright neighbour is black and that the pawn can take it
			if (upRight > 0 && board.neighborUpRight(upRight) > 0 && board.isBlack(upRight) && board.isEmpty(board.neighborUpRight(upRight))) {
				if (board.neighborUpRight(upRight) != lastDestination)
					dest.add(board.neighborUpRight(upRight));
			}
			// if the pawn is a king, it can also take from the other direction
			if (pawn > 0 && isKing) {
				int downLeft = board.neighborDownLeft(pawn);
				int downRight = board.neighborDownRight(pawn);

				//System.out.println(downRight);
				//System.out.println(downLeft);

				if (downLeft > 0 && board.neighborDownLeft(downLeft) > 0 && board.isBlack(downLeft) && board.isEmpty(board.neighborDownLeft(downLeft))) {
					if (board.neighborDownLeft(downLeft) != lastDestination)
						dest.add(board.neighborDownLeft(downLeft));
				}
				if (downRight > 0 && board.neighborDownRight(downRight) > 0 && board.isBlack(downRight) && board.isEmpty(board.neighborDownRight(downRight))) {
					if (board.neighborDownRight(downRight) != lastDestination)
						dest.add(board.neighborDownRight(downRight));
				}
			}
		}
		// check if the player is black so that it goes to the right direction
		if (playerId.equals(PlayerId.TWO)) {
			int downLeft = board.neighborDownLeft(pawn);
			int downRight = board.neighborDownRight(pawn);

			//System.out.println(downRight);
			//System.out.println(downLeft);

			if (downLeft > 0 && board.neighborDownLeft(downLeft) > 0 && board.isWhite(downLeft) && board.isEmpty(board.neighborDownLeft(downLeft))) {
				if (board.neighborDownLeft(downLeft) != lastDestination)
					dest.add(board.neighborDownLeft(downLeft));
			}
			if (downRight > 0 && board.neighborDownRight(downRight) > 0 &&  board.isWhite(downRight) && board.isEmpty(board.neighborDownRight(downRight))) {
				if (board.neighborDownRight(downRight) != lastDestination)
					dest.add(board.neighborDownRight(downRight));
			}

			if (pawn > 0 && isKing) {
				int upLeft = board.neighborUpLeft(pawn);
				int upRight = board.neighborUpRight(pawn);
				if (upLeft > 0 && board.neighborUpLeft(upLeft) > 0 && board.isWhite(upLeft) && board.isEmpty(board.neighborUpLeft(upLeft))) {
					if (board.neighborUpLeft(upLeft) != lastDestination)
						dest.add(board.neighborUpLeft(upLeft));
				}
				if (upRight > 0 && board.neighborUpRight(upRight) > 0 && board.isWhite(upRight) && board.isEmpty(board.neighborUpRight(upRight))) {
					if (board.neighborUpRight(upRight) != lastDestination)
						dest.add(board.neighborUpRight(upRight));
				}
			}
		}
		return dest;
	}

	/**
	 * Liste les prisees possibles depuis une case en prenant en compte les prises multiples
	 * @param square
	 * @param isKing
	 * @param lastDestination
	 * @return
	 */
	List<DraughtsMove> prisesPossiblesDepuisCase(int square, boolean isKing, int lastDestination, int iteration) {
		List<DraughtsMove> moves = new ArrayList<>();
		List<Integer> destSauts = listeDestinationsSautsPossiblesDepuisCase(square, isKing, lastDestination);
		//System.out.println(destSauts.size());
		for (int dest : destSauts) {
			List<DraughtsMove> movesPrisesDest = new ArrayList<>();
			if (iteration <10)
				movesPrisesDest = prisesPossiblesDepuisCase(dest, isKing, square, iteration+1);
			if (movesPrisesDest.isEmpty()) {
				DraughtsMove move = new DraughtsMove();
				move.add(square);
				move.add(dest);
				moves.add(move);
			} else {
				for (DraughtsMove moveDest : movesPrisesDest) {
					DraughtsMove move = new DraughtsMove();
					move.add(square);
					move.add(dest);
					for (int i = 1; i<moveDest.size(); ++i)
						move.add(moveDest.get(i));
					moves.add(move);
				}
			}
		}
		return moves;
	}

	/**
	 * Generate the list of possible moves
	 * - first check moves with captures
	 * - if no capture possible, return displacement moves
	 */
	@Override
	public List<Move> possibleMoves() {
		//
		// TODO generate the list of possible moves
		//
		// Advice: 
		// create two auxiliary functions :
		// - one for jump moves from a given position, with capture (and multi-capture).
		//    Use recursive calls to explore all multiple capture possibilities
		// - one function that returns the displacement moves from a given position (without capture)
		//
		ArrayList<Move> moves = new ArrayList<>();

		ArrayList<Move> possibleCaptureMoves = new ArrayList<>();
		for(int i = 0; i< myPawns().size(); i++) {
			int current = myPawns().get(i);
			List<DraughtsMove> possibleCaptures = prisesPossiblesDepuisCase(current, board.isKing(current), -1, 0);
			if (!possibleCaptures.isEmpty()) {
				//System.out.println();
				possibleCaptureMoves.addAll(possibleCaptures);
			}
		}

		moves.addAll(possibleCaptureMoves);
		if(moves.isEmpty()) {
			moves.addAll(possibleMovesWithoutCapture());
		}
		return moves;
	}


	public ArrayList<Move> possibleMovesWithoutCapture(){
		ArrayList<Move> moves = new ArrayList<>();
		ArrayList<Integer> myPawns = myPawns();
		//Les pions sont blancs
		if(playerId.equals(PlayerId.ONE)){ //WHITE
			for(int i = 0; i< myPawns.size(); i++) {
				int current = myPawns.get(i);
					int upLeft = board.neighborUpLeft(current);
					int upRight = board.neighborUpRight(current);
					if (upLeft != 0 && board.isEmpty(upLeft)) {
						DraughtsMove move1 = new DraughtsMove();
						move1.add(current);
						move1.add(upLeft);
						moves.add(move1);
					}
					if (upRight != 0 && board.isEmpty(upRight)) {
						DraughtsMove move1 = new DraughtsMove();
						move1.add(current);
						move1.add(upRight);
						moves.add(move1);
					}
					if (board.isKing(current)) {//WhiteKing
						int downLeft = board.neighborDownLeft(current);
						int downRight = board.neighborDownRight(current);
						if (downLeft != 0 && board.isEmpty(downLeft)) {
							DraughtsMove move1 = new DraughtsMove();
							move1.add(current);
							move1.add(downLeft);
							moves.add( move1);
						}
						if (downRight != 0 && board.isEmpty(downRight)) {
							DraughtsMove move1 = new DraughtsMove();
							move1.add(current);
							move1.add(downRight);
							moves.add( move1);
						}
				}
			}
		}
		else { //BLACK
			for(int i = 0; i< myPawns.size(); i++) {
				int current = myPawns.get(i);
				int downLeft = board.neighborDownLeft(current);
				int downRight = board.neighborDownRight(current);
				if (downLeft != 0 && board.isEmpty(downLeft)) {
					DraughtsMove move1 = new DraughtsMove();
					move1.add(current);
					move1.add(downLeft);
					moves.add( move1);
				}
				if (downRight != 0 && board.isEmpty(downRight)) {
					DraughtsMove move1 = new DraughtsMove();
					move1.add(current);
					move1.add(downRight);
					moves.add(move1);
				}
				if (board.isKing(current)) {//WhiteKing
					int upLeft = board.neighborUpLeft(current);
					int upRight = board.neighborUpRight(current);
					if (upLeft != 0 && board.isEmpty(upLeft)) {
						DraughtsMove move1 = new DraughtsMove();
						move1.add(current);
						move1.add(upLeft);
						moves.add(move1);
					}
					if (upRight != 0 && board.isEmpty(upRight)) {
						DraughtsMove move1 = new DraughtsMove();
						move1.add(current);
						move1.add(upRight);
						moves.add(move1);
					}
				}
			}

		}


		return moves;
	}

	@Override
	public void play(Move aMove) {
		// Player should be valid
		if (playerId == PlayerId.NONE)
			return;
		// We will cast Move to DraughtsMove (kind of ArrayList<Integer>
		if (!(aMove instanceof DraughtsMove))
			return;
		// Cast and apply the move
		DraughtsMove move = (DraughtsMove) aMove;

		// Move pawn and capture opponents
		List<Move> possibleMoves = possibleMoves();

		boolean captured = false;
		int current = board.get(move.get(0));
		boolean isKing = board.isKing(move.get(0));

		if (possibleMoves.contains(move)) {
			board.set(move.get(0), CheckerBoard.EMPTY);
			if (isKing && playerId == PlayerId.ONE)
				board.set(move.get(move.size()-1), CheckerBoard.WHITE_KING);
			else if (isKing && playerId == PlayerId.TWO)
				board.set(move.get(move.size()-1), CheckerBoard.BLACK_KING);
			else board.set(move.get(move.size()-1), (byte)current);
			//capture
			for (int i = 1; i<move.size(); ++i) {
				if (board.neighborDownRight(board.neighborDownRight(move.get(i-1))) == move.get(i)) {
					board.set(board.neighborDownRight(move.get(i-1)), CheckerBoard.EMPTY);
					captured = true;
				}
				if (board.neighborDownLeft(board.neighborDownLeft(move.get(i-1))) == move.get(i)) {
					board.set(board.neighborDownLeft(move.get(i-1)), CheckerBoard.EMPTY);
					captured = true;
				}
				if (board.neighborUpRight(board.neighborUpRight(move.get(i-1))) == move.get(i)) {
					board.set(board.neighborUpRight(move.get(i-1)), CheckerBoard.EMPTY);
					captured = true;
				}
				if (board.neighborUpLeft(board.neighborUpLeft(move.get(i-1))) == move.get(i)) {
					board.set(board.neighborUpLeft(move.get(i-1)), CheckerBoard.EMPTY);
					captured = true;
				}
			}

		}

		if (!captured && isKing) {
			nbKingMovesWithoutCapture++;
		}
		else nbKingMovesWithoutCapture = 0;

		// Promote to king if the pawn ends on the opposite of the board
		int finMovement = move.get(move.size() - 1);
		if (board.inTopRow(finMovement) && playerId.equals(playerId.ONE)) board.set(finMovement,CheckerBoard.WHITE_KING);
		if (board.inBottomRow(finMovement) && playerId.equals(playerId.TWO)) board.set(finMovement,CheckerBoard.BLACK_KING);
		
		// Next player
		if(player().equals(playerId.ONE)) playerId = playerId.TWO;
		else playerId = playerId.ONE;
		
		// Update nbTurn
		nbTurn++;
		
		// Keep track of successive moves with kings wthout capture
	}

	@Override
	public PlayerId player() {
		return playerId;
	}

	/**
	 * Get the winner (or null if the game is still going)
	 * Victory conditions are :
	 * - adversary with no more pawns or no move possibilities
	 * Null game condition (return PlayerId.NONE) is
	 * - more than 25 successive moves of only kings and without any capture
	 */
	@Override
	public PlayerId winner() {
		// return the winner ID if possible
		if (possibleMoves().isEmpty()) {
			if (playerId == PlayerId.ONE)
				return PlayerId.TWO;
			return PlayerId.ONE;
		} else if (nbKingMovesWithoutCapture >= 25)
			return PlayerId.NONE;
		
		// return PlayerId.NONE if the game is null

		// Return null is the game has not ended yet
		//System.out.println(nbKingMovesWithoutCapture);
		return null;
	}
}
