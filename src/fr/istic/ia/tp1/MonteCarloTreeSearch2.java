package fr.istic.ia.tp1;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import fr.istic.ia.tp1.Game.Move;
import fr.istic.ia.tp1.Game.PlayerId;

/**
 * A class implementing a Monte-Carlo Tree Search method (MCTS) for playing two-player games ({@link Game}).
 * @author vdrevell
 *
 */
public class MonteCarloTreeSearch2 {

    /**
     * A class to represent an evaluation node in the MCTS tree.
     * This is a member class so that each node can access the global statistics of the owning MCTS.
     * @author vdrevell
     *
     */
    class EvalNode {
        /** The number of simulations run through this node */
        int n;

        /** The number of winning runs */
        double w;

        /** The game state corresponding to this node */
        Game game;

        /** The children of the node: the games states accessible by playing a move from this node state */
        ArrayList<EvalNode> children;


        /**
         * The only constructor of EvalNode.
         * @param game The game state corresponding to this node.
         */
        EvalNode(Game game) {
            this.game = game;
            children = new ArrayList<EvalNode>();
            w = 0.0;
            n = 0;
        }

        /**
         * Compute the Upper Confidence Bound for Trees (UCT) value for the node.
         * @return UCT value for the node
         */
        double uct() {
            if (n==0) return Integer.MAX_VALUE;
            else return (w / (double) n) + 1.4142 * Math.sqrt(Math.log(nTotal) / (double) n);
        }

        /**
         * "Score" of the node, i.e estimated probability of winning when moving to this node
         * @return Estimated probability of win for the node
         */
        double score() {
            if (n==0) return Integer.MAX_VALUE;
            return (w/n);
        }

        /**
         * Update the stats (n and w) of the node with the provided rollout results
         * @param res
         */
        void updateStats(RolloutResults res) {
            this.n = res.nbSimulations();
            //TODO A verifier
            this.w = res.nbWins(game.player());
        }
    }

    /**
     * A class to hold the results of the rollout phase
     * Keeps the number of wins for each player and the number of simulations.
     * @author vdrevell
     *
     */
    static class RolloutResults {
        /** The number of wins for player 1 {@link PlayerId#ONE}*/
        double win1;

        /** The number of wins for player 2 {@link PlayerId#TWO}*/
        double win2;

        /** The number of playouts */
        int n;

        /**
         * The constructor
         */
        public RolloutResults() {
            reset();
        }

        /**
         * Reset results
         */
        public void reset() {
            n = 0;
            win1 = 0.0;
            win2 = 0.0;
        }

        /**
         * Add other results to this
         * @param res The results to add
         */
        public void add(RolloutResults res) {
            win1 += res.win1;
            win2 += res.win2;
            n += res.n;
        }

        /**
         * Update playout statistics with a win of the player <code>winner</code>
         * Also handles equality if <code>winner</code>={@link PlayerId#NONE}, adding 0.5 wins to each player
         * @param winner
         */
        public void update(PlayerId winner) {
            if(winner.equals(PlayerId.ONE)) this.win1++;
            if(winner.equals(PlayerId.TWO)) this.win2++;
            if(winner.equals(PlayerId.NONE)) {
                this.win1 = this.win1 + 0.5;
                this.win2 = this.win2 + 0.5;
            }
            this.n++;
        }

        /**
         * Getter for the number of wins of a player
         * @param playerId
         * @return The number of wins of player <code>playerId</code>
         */
        public double nbWins(PlayerId playerId) {
            switch (playerId) {
                case ONE: return win1;
                case TWO: return win2;
                default: return 0.0;
            }
        }

        /**
         * Getter for the number of simulations
         * @return The number of playouts
         */
        public int nbSimulations() {
            return n;
        }
    }

    /**
     * The root of the MCTS tree
     */
    EvalNode root;

    /**
     * The total number of performed simulations (rollouts)
     */
    int nTotal;


    /**
     * The constructor
     * @param game
     */
    public MonteCarloTreeSearch2(Game game) {
        root = new EvalNode(game.clone());
        nTotal = 0;
    }

    /**
     * Perform a single random playing rollout from the given game state
     * @param game Initial game state. {@code game} will contain an ended game state when the function returns.
     * @return The PlayerId of the winner (or NONE if equality or timeout).
     */
    static PlayerId playRandomlyToEnd(Game game) {
        while(game.winner() == null){
            Player bot = new PlayerRandom();
            game.play(bot.play(game));
        }
        return game.winner();
    }

    /**
     * Perform nbRuns rollouts from a game state, and returns the winning statistics for both players.
     * @param game The initial game state to start with (not modified by the function)
     * @param nbRuns The number of playouts to perform
     * @return A RolloutResults object containing the number of wins for each player and the number of simulations
     */
    static RolloutResults rollOut(final Game game, int nbRuns) {
        RolloutResults roll = new RolloutResults();
        for(int i = 0; i < nbRuns; i++){
            roll.update(playRandomlyToEnd(game));
        }
        return roll;
    }

    /**
     * Apply the MCTS algorithm during at most <code>timeLimitMillis</code> milliseconds to compute
     * the MCTS tree statistics.
     * @param timeLimitMillis Computation time limit in milliseconds
     */
    public void evaluateTreeWithTimeLimit(int timeLimitMillis) {
        // Record function entry time
        long startTime = System.nanoTime();

        // Evaluate the tree until timeout
        while (TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime) < timeLimitMillis) {
            // Perform one MCTS step
            boolean canStop = evaluateTreeOnce();
            // Stop evaluating the tree if there is nothing more to explore
            if (canStop) {
                System.out.println("backpropagate pas");
                break;
            }
        }

        // Print some statistics
        System.out.println("Stopped search after "
                + TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime) + " ms. "
                + "Root stats is " + root.w + "/" + root.n + String.format(" (%.2f%% loss)", 100.0*root.w/root.n));
    }

    /**
     * Perform one MCTS step (selection, expansion(s), simulation(s), backpropagation
     * @return <code>true</code> if there is no need for further exploration (to speed up end of games).
     */
    public boolean evaluateTreeOnce() {
        //
        // TODO implement MCTS evaluateTreeOnce
        //

        //Liste des noeuds visités - NOTE : ils forment une ligné "père-fils"
        List<EvalNode> visitedNodes = new ArrayList<>();
        //On part du noeud root
        //On prend un noeud courant = noeud root = de manière générale : le dernier éléments de la liste "visites"
        EvalNode currentNode = root;
        //On ajoute root aux neouds visités
        visitedNodes.add(root);
        //Tant que le noeud courant n'est pas une feuille
        while (!currentNode.children.isEmpty()) {
            //Si ses enfants ne couvrent pas toutes les possibilités
            if (currentNode.children.size() < currentNode.game.possibleMoves().size()) {
                //On ajoute un enfant à noeud courant qui couvre une nouvelle possibilité
                EvalNode child = new EvalNode(currentNode.game.clone());
                int i = 0;
                List<Move> possibleMoves = currentNode.game.possibleMoves();
                child.game.play(possibleMoves.get(0));
                while (i<currentNode.children.size() && currentNode.children.contains(child)) {
                    ++i;
                    child = new EvalNode(currentNode.game.clone());
                    child.game.play(possibleMoves.get(i));
                }
                currentNode.children.add(child);
                //On ajoute cet enfant aux noeuds visités
                visitedNodes.add(child);
                //Il devient le nouveau noeud courant
                currentNode = child;
            }
            //Sinon
            else {
                //On calcule l'uct de tous les enfants du noeud courant et on prend le max
                EvalNode selectedChild = currentNode.children.get(0);
                double uctMax = selectedChild.uct();
                for (int i = 1; i<currentNode.children.size();++i) {
                    if (currentNode.children.get(i).uct() > uctMax) {
                        uctMax = currentNode.children.get(i).uct();
                        selectedChild = currentNode.children.get(i);
                    }
                }
                //On ajoute le max aux noeuds visités
                visitedNodes.add(selectedChild);
                //Ce noeud devient le nouveau noeud courant
                currentNode = selectedChild;
            }
        }
        //À la fin de cette boucle, on a un noeud qui est une feuille
        //Si il n'a plus de move disponible
        if (currentNode.game.possibleMoves().size() == 0) {
            //On récupère l'issue du match
            PlayerId winner = currentNode.game.winner();
            //On fait parvenir aux enfants l'issue du match en remontant la liste des neouds visités
            for (int i = visitedNodes.size()-1; i>0; --i) {
                EvalNode node = visitedNodes.get(i);
                node.n++;
                if (winner.equals(node.game.player()))
                    node.w++;
                else if (winner == PlayerId.NONE)
                    node.w+=0.5;
                visitedNodes.set(i,node);
            }
        }
        //Sinon
        else {
            //On lui fait jouer le premier move parmi la liste des moves possible
            List<Move> possibleMoves = currentNode.game.possibleMoves();
            EvalNode child = new EvalNode(currentNode.game.clone());
            child.game.play(possibleMoves.get(0));
            currentNode.children.add(child);
            currentNode = child;
            //On génère à partir de cet enfant une série de match aléatoires
            double wins = 0;
            double losses = 0;
            double iterations = 10;
            for (int i = 0; i<iterations; ++i) {
                PlayerId winner = playRandomlyToEnd(currentNode.game.clone());
                if (winner.equals(currentNode.game.player()))
                    wins++;
                else if (winner == PlayerId.NONE) {
                    wins += 0.5;
                    losses += 0.5;
                } else
                    losses++;
            }
            //On fait remonter à tous les éléments de la liste visites l'issue de ces matchs et le nombre de match au total
            for (int i = visitedNodes.size()-1; i>0; --i) {
                EvalNode node = visitedNodes.get(i);
                node.n+=iterations;
                if (currentNode.game.player().equals(node.game.player()))
                    node.w+=wins;
                else
                    node.w+=losses;
                visitedNodes.set(i,node);
            }
        }
        //On crée le boolean noMoreMoves = true
        boolean noMoreMoves = true;
        //On reparcours la liste des noeuds visites
        for (int i = visitedNodes.size()-1; i>=0; --i) {
            //si un des noeuds a sa liste de movespossibles non vide, on met noMoreMoves à false et on break
            EvalNode node = visitedNodes.get(i);
            if (node.game.possibleMoves().size() > 0) {
                noMoreMoves = false;
                break;
            }
        }
        //Si noMoreMoves
        //return true
        nTotal++;
        return noMoreMoves;
        //Sinon return false
    }

    /**
     * Select the best move to play, given the current MCTS tree playout statistics
     * @return The best move to play from the current MCTS tree state.
     */
    public Move getBestMove() {
        System.out.println("Recherche du meilleur coups possible ..");
        List<EvalNode> children = root.children;
        List<Move> possibleMoves = root.game.possibleMoves();
        int indexOfBestChildren = -1;
        double bestScore = Integer.MAX_VALUE;
        System.out.println("ROOT POSSEDE " + root.children.size() + " FILS");
        for(int i = 0; i < children.size() ; i++){
            if(children.get(i).w <= bestScore) {
                bestScore = children.get(i).w;
                indexOfBestChildren = i;
            }
        }
        System.out.println("Le meilleur move est :" + bestScore + " fois gagné" );
        if (indexOfBestChildren < 0) {
            System.out.println("Erreur getBestMove");
        }
        return possibleMoves.get(indexOfBestChildren);
    }


    /**
     * Get a few stats about the MTS tree and the possible moves scores
     * @return A string containing MCTS stats
     */
    public String stats() {
        String str = "MCTS with " + nTotal + " evals\n";
        Iterator<Move> itMove = root.game.possibleMoves().iterator();
        for (EvalNode node : root.children) {
            Move move = itMove.next();
            double score = node.score();
            str += move + " : " + score + " (" + node.w + "/" + node.n + ")\n";
        }
        return str;
    }
}
