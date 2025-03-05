package dk.easv.bll.bot;

import dk.easv.bll.game.GameState;
import dk.easv.bll.game.IGameState;
import dk.easv.bll.move.IMove;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DumpMinimaxBot implements IBot{
    private static final String BOTNAME="Dump minimax bot";
    private int botPlayer; // 0 or 1 representing which player the bot is
    private Random random = new Random();


    @Override
    public IMove doMove(IGameState state) {

        // Get all possible legal moves in current game state
        List<IMove> avail = state.getField().getAvailableMoves();
        //IF there are no moves avail return null
        if(avail.isEmpty()) {
            return null;
        }

        /** Instead of always choosing the first move, collect all moves that
            share the highest evaluation and then choose one at random*/

        List<IMove> bestMoves = new ArrayList<>();
        int bestValue = Integer.MIN_VALUE;
        int depth = 1; // Search depth (higher = deeper lookahead, but slower)
        // Determine the current player (as a String marker)
        String currentPlayer = String.valueOf(state.getMoveNumber() % 2);

        // Loop through all available moves and use minimax to evaluate each
        for(IMove move : avail) {
            // Simulate making this move in a copy of the game state
            IGameState newState = simulateMove(state, move);

            // Recursively evaluate subsequent moves using minimax with alpha-beta pruning
            int eval = minimax(newState, depth -1, false, move, currentPlayer, Integer.MIN_VALUE, Integer.MAX_VALUE);

            // Track best moves found
            if(eval > bestValue) {
                bestValue = eval;
                bestMoves.clear();
                bestMoves.add(move);
            } else if(eval == bestValue) {
                bestMoves.add(move);
            }

        }
        // Random selection between equally good moves to avoid predictability
        IMove bestMove = bestMoves.get(random.nextInt(bestMoves.size()));
        System.out.println("Chosen move: " + bestMove + " with best score: " + bestValue);
        return bestMove;

    }

    /**
     * Simulates a move by cloning the state, applying the move,
     * and updating the move and round numbers
     */
    // Creates a copy of the game state with a move applied
    private IGameState simulateMove(IGameState state, IMove move) {

        IGameState newState = new GameState(state); // Clone original state
        String[][] board = newState.getField().getBoard();

        // Determine current player (0 or 1) based on move number
        int currentPlayer = state.getMoveNumber() % 2;
        board[move.getX()][move.getY()] = String.valueOf(currentPlayer);

        // Update game state counters
        newState.setMoveNumber(newState.getMoveNumber() + 1);
        if (newState.getMoveNumber() % 2 == 0) {
            newState.setRoundNumber(newState.getRoundNumber() + 1);
        }


        return newState;
    }

    // Win condition checker for 3x3 sub-boards
    private boolean isWinningMove(IGameState state, IMove move, String player) {
        String[][] board = state.getField().getBoard();

        boolean isRowWin = true;
        //Row checking
        int startX = move.getX() - (move.getX() % 3);
        int endX = startX + 2;
        for (int x = startX; x < endX; x++) {
            if (x != move.getX())
                if (!board[x][move.getY()].equals(player))
                    isRowWin = false;
        }


        boolean isColumnWin = true;
        //Column checking
        int startY = move.getY() - (move.getY() % 3);
        int endY = startY + 2;
        for (int y = startY; y < endY; y++) {
            if (y != move.getY())
                if (!board[move.getX()][y].equals(player))
                    isColumnWin = false;
        }



        boolean isDiagonalWin = true;

        //Diagonal checking left-top to right-bottom
        if (!(move.getX()==startX && move.getY()==startY))
            if (!board[startX][startY].equals(player))
                isDiagonalWin = false;
        if (!(move.getX()==startX+1 && move.getY()==startY+1))
            if (!board[startX + 1][startY + 1].equals(player))
                isDiagonalWin = false;
        if (!(move.getX()==startX+2 && move.getY()==startY+2))
            if (!board[startX + 2][startY + 2].equals(player))
                isDiagonalWin = false;



        boolean isOppositeDiagonalWin = true;
        //Diagonal checking left-bottom to right-top
        if (!(move.getX()==startX && move.getY()==startY+2))
            if (!board[startX][startY + 2].equals(player))
                isOppositeDiagonalWin = false;
        if (!(move.getX()==startX+1 && move.getY()==startY+1))
            if (!board[startX + 1][startY + 1].equals(player))
                isOppositeDiagonalWin = false;
        if (!(move.getX()==startX+2 && move.getY()==startY))
            if (!board[startX + 2][startY].equals(player))
                isOppositeDiagonalWin = false;


        return isColumnWin || isRowWin || isDiagonalWin || isOppositeDiagonalWin;
    }




    /**
     * Evaluation function that returns:
     *   +1 if bot wins,
     *   +2 if opponent wins,
     *   -1 if tie,
     *   or a heuristic value for non-terminal states.
     *
     * Here, we count tokens on the main board (9x9) for a finer evaluation.
     */
        // Heuristic evaluation function (critical for minimax performance)
        private int evaluate(IGameState state, IMove lastMove, String player) {

            if(lastMove != null && isWinningMove(state, lastMove, player)) {

                Boolean winner = isWinningMove(state, lastMove, player); ///I don't know what it does, need to improve....
                if(winner == null)
                    return -1; //tie

               if(player.equals(String.valueOf(botPlayer))) {
                    return 1; //bot won
                } else {
                    return 2; //opponent won
                }

            }

            // Position-based heuristic: counts pieces with center bonus
            int botCount = 0, oppCount = 0;
            String[][] board = state.getField().getBoard(); // 9x9 main board
            String botMarker = String.valueOf(botPlayer);
            String oppMarker = String.valueOf(1 - botPlayer);

            for (int i = 0; i < board.length; i++) {
                for (int j = 0; j < board[i].length; j++) {
                    if (board[i][j].equals(botMarker)) {

                        botCount++;
                        botMarker += (4 - Math.abs(i - 4)) + (4 - Math.abs(j - 4));

                    }else if (board[i][j].equals(oppMarker)) {

                        oppCount++;
                        botMarker += (4 - Math.abs(i - 4)) + (4 - Math.abs(j - 4));

                    }
                }
            }

            return botCount - oppCount;
        }

    /**
     * Basic minimax implementation.
     */
    // Minimax core with alpha-beta pruning
    private int minimax(IGameState state, int depth, boolean isMaximizing, IMove lastMove, String player, int alpha, int beta) {

        if(depth == 0 || (lastMove != null && isWinningMove(state, lastMove, player))) {
            return evaluate(state, lastMove, player);
        }

        if(isMaximizing) {
            int maxEval = Integer.MIN_VALUE;

            // When maximizing, the current move is by our bot
            for (IMove move : state.getField().getAvailableMoves()) {
                IGameState newState = simulateMove(state, move);

                // The new last move is "move" and the player is our bot
                int eval = minimax(newState, depth - 1, false, move, String.valueOf(botPlayer), alpha, beta);
                maxEval = Math.max(maxEval, eval);
                alpha = Math.max(alpha, eval);
                if(beta <= alpha) {
                    break; // Alpha cutoff
                }
            }
            return maxEval;
        }else{
            int minEval = Integer.MAX_VALUE;

            // When minimizing, the move is by the opponent
            String opponentMarker = String.valueOf((botPlayer == 0) ? 1 : 0);
            for (IMove move : state.getField().getAvailableMoves()) {
                IGameState newState = simulateMove(state, move);
                int eval = minimax(newState, depth - 1, true, move, opponentMarker, alpha, beta);
                minEval = Math.min(minEval, eval);
                beta = Math.min(beta, eval);
                if(beta <= alpha) {
                    break; // Beta cutoff
                }
            }
            return minEval;
        }
    }





    @Override
    public String getBotName() {
        return BOTNAME; //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * Returns the winning marker ("0" or "1") if there's a winner on the macroboard,
     * or null if there is no winner.
     */
    /*private String getWinner(IGameState state) {
        String[][] macro = state.getField().getMacroboard();

        //Check rows for a win
        for (int r = 0; r < 3; r++) {
            if (!macro[r][0].equals(IField.EMPTY_FIELD) &&
                    !macro[r][0].equals(IField.AVAILABLE_FIELD) &&

                    macro[r][0].equals(macro[r][1]) && macro[r][1].equals(macro[r][2])) {
                return macro[r][0];
            }
        }


        //Check column for a win
        for (int c = 0; c < 3; c++) {
            if (!macro[0][c].equals(IField.EMPTY_FIELD) &&
                    !macro[0][c].equals(IField.AVAILABLE_FIELD) &&

                    macro[0][c].equals(macro[1][c]) && macro[1][c].equals(macro[2][c])) {
                return macro[0][c];
            }
        }

        //Check diag for a win
        if (!macro[0][0].equals(IField.EMPTY_FIELD) &&
                !macro[0][0].equals(IField.AVAILABLE_FIELD) &&

                macro[0][0].equals(macro[1][1]) && macro[1][1].equals(macro[2][2])) {
            return macro[0][0];

        }

        //Check opDiag for a win
        if (!macro[0][2].equals(IField.EMPTY_FIELD) &&
                !macro[0][2].equals(IField.AVAILABLE_FIELD) &&

                macro[0][2].equals(macro[1][1]) && macro[1][1].equals(macro[2][0])) {
            return macro[0][2];
        }


        return null;

    }*/



    /*private boolean isGameOver(IGameState state) {

        //get the macroboard 3x3
        String[][] macro = state.getField().getMacroboard();

        //Check rows for a win
        for (int r = 0; r < 3; r++) {
            if (!macro[r][0].equals(IField.EMPTY_FIELD) &&
                    !macro[r][0].equals(IField.AVAILABLE_FIELD) &&

                    macro[r][0].equals(macro[r][1]) && macro[r][1].equals(macro[r][2])) {
                return true;
            }
        }


            //Check column for a win
            for (int c = 0; c < 3; c++) {
                if (!macro[0][c].equals(IField.EMPTY_FIELD) &&
                        !macro[0][c].equals(IField.AVAILABLE_FIELD) &&

                        macro[0][c].equals(macro[1][c]) && macro[1][c].equals(macro[2][c])) {
                    return true;
                }
            }

            //Check diag for a win
                if (!macro[0][0].equals(IField.EMPTY_FIELD) &&
                        !macro[0][0].equals(IField.AVAILABLE_FIELD) &&

                        macro[0][0].equals(macro[1][1]) && macro[1][1].equals(macro[2][2])) {
                    return true;

            }

            //Check opDiag for a win
                if (!macro[0][2].equals(IField.EMPTY_FIELD) &&
                        !macro[0][2].equals(IField.AVAILABLE_FIELD) &&

                        macro[0][2].equals(macro[1][1]) && macro[1][1].equals(macro[2][0])) {
                    return true;
                }

        // If no available moves remain the game over (tie)
                if(state.getField().getAvailableMoves().isEmpty()){
                    return true;
                }
                return false;
            }*/


}
