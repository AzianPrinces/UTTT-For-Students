package dk.easv.bll.bot;

import dk.easv.bll.field.IField;
import dk.easv.bll.game.GameState;
import dk.easv.bll.game.IGameState;
import dk.easv.bll.move.IMove;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DummyBot implements IBot{
    private static final String BOTNAME="DummyBot";
    private int botPlayer;
    private Random random = new Random();


    /*public DummyBot(int botPlayer) {
        this.botPlayer = botPlayer;
    }*/

    @Override
    public IMove doMove(IGameState state) {

        //gets available moves in the List avail
        List<IMove> avail = state.getField().getAvailableMoves();
        //IF there are no moves avail return null
        if(avail.isEmpty()) {
            return null;
        }

        /** Instead of always choosing the first move, collect all moves that
            share the highest evaluation and then choose one at random*/

        List<IMove> bestMoves = new ArrayList<>();
        int bestValue = Integer.MIN_VALUE;
        int depth = 3;

        // Loop through all available moves and use minimax to evaluate each
        for(IMove move : avail) {

            IGameState newState = simulateMove(state, move);
            int eval = minimax(newState, depth -1, false);
            if(eval > bestValue) {
                bestValue = eval;
                bestMoves.clear();
                bestMoves.add(move);
            } else if(eval == bestValue) {
                bestMoves.add(move);
            }

        }
        // Randomly select among moves with equal best score
        IMove bestMove = bestMoves.get(0);
        System.out.println("Chosen move: " + bestMove + "with best score "  + bestValue);
        return bestMove;

    }

    /**
     * Simulates a move by cloning the state, applying the move,
     * and updating the move and round numbers.
     */
    public IGameState simulateMove(IGameState state, IMove move) {

        IGameState newState = new GameState(state);

        int currentPlayer = state.getMoveNumber() % 2;

        String[][] board = newState.getField().getBoard();

        board[move.getX()][move.getY()] = String.valueOf(currentPlayer);

        newState.setMoveNumber(newState.getMoveNumber() + 1);
        if (newState.getMoveNumber() % 2 == 0) {
            newState.setRoundNumber(newState.getRoundNumber() + 1);
        }


        return newState;
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



    private boolean isGameOver(IGameState state) {

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
            }

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
     *   +100 if bot wins,
     *   -100 if opponent wins,
     *   0 if tie,
     *   or a heuristic value for non-terminal states.
     *
     * Here, we count tokens on the main board (9x9) for a finer evaluation.
     */
        private int evaluate(IGameState state) {

            if(isGameOver(state)) {
                Boolean winner = isGameOver(state);
                if(winner == null)
                    return 0; //tie
                else if(winner.equals(String.valueOf(botPlayer))) {
                    return 100; //bot won
                } else {
                    return -100; //opponent won
                }

            }

            // Use the main board for heuristic evaluation
            int botCount = 0, oppCount = 0;
            String[][] board = state.getField().getBoard(); // 9x9 main board
            String botMarker = String.valueOf(botPlayer);
            String oppMarker = String.valueOf((botPlayer == 0) ? 1 : 0);

            for (int i = 0; i < board.length; i++) {
                for (int j = 0; j < board[i].length; j++) {
                    if (board[i][j].equals(botMarker)) {
                        botCount++;

                        botMarker += (4 - Math.abs(i - 4)) + (4 - Math.abs(j - 4));
                    }else if (board[i][j].equals(oppMarker)) {
                        oppCount++;

                        // Bonus for center positions
                        if(i == 4 && j == 4){
                            botCount+= 2;
                        }

                    }
                }
            }
            // A simple heuristic: difference between bot's and opponent's tokens
            int heuristic = botCount - oppCount;
            heuristic += random.nextInt(3);

            return heuristic;
        }

    /**
     * Basic minimax implementation.
     */
    private int minimax(IGameState state, int depth, boolean isMaximizing) {

        if(depth == 0 || isGameOver(state)) {
            return evaluate(state);
        }

        // For simplicity, we use simulateMove without passing extra player info
        if(isMaximizing) {
            int maxEval = Integer.MIN_VALUE;
            for (IMove move : state.getField().getAvailableMoves()) {
                IGameState newState = simulateMove(state, move);
                int eval = minimax(newState, depth - 1, false);
                maxEval = Math.max(maxEval, eval);
            }
            return maxEval;
        }else{
            int minEval = Integer.MAX_VALUE;
            for (IMove move : state.getField().getAvailableMoves()) {
                IGameState newState = simulateMove(state, move);
                int eval = minimax(newState, depth - 1, true);
                minEval = Math.min(minEval, eval);
            }
            return minEval;
        }
    }





    @Override
    public String getBotName() {
        return BOTNAME; //To change body of generated methods, choose Tools | Templates.
    }

}
