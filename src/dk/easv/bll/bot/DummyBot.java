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
    //private Random random = new Random();


    /**
     * Chooses a random available move.
     *
     * @param state The current game state.
     * @return A randomly selected move from the available moves, or null if none.
     */

    @Override
    public IMove doMove(IGameState state) {




        //get the whole field
        IField field = state.getField();
        //gets available moves in the List avail
        List<IMove> avail = field.getAvailableMoves();

        //IF there are no moves avail return null
        if(avail.isEmpty()) {
            return null;
        }
        //pick a random index from avail list so it can make a move
        //int index = random.nextInt(avail.size());
        IMove bestMove = null;
        int bestValue = Integer.MIN_VALUE;
        int depth = 3;
        //int currentPlayer = 0;
        int currentPlayer = state.getMoveNumber() % 2;


        for(IMove move : avail){

            IGameState newState = simulateMove(state, move, currentPlayer);

            int moveValue = minimax(newState, depth - 1, false, currentPlayer);
            System.out.println("Evaluated move: " + move + " with score: " + moveValue);

            if(moveValue > bestValue) {
                bestValue = moveValue;
                bestMove = move;
            }
        }


        System.out.println("Chosen move: " + bestMove + "with best score "  + bestValue);
        return bestMove;

    }

    public IGameState simulateMove(IGameState state, IMove move, int currentPlayer) {

        IGameState newState = new GameState(state);

        String[][] board = newState.getField().getBoard();

        board[move.getX()][move.getY()] = currentPlayer + " ";

        newState.setMoveNumber(newState.getMoveNumber() + 1);
        if (newState.getMoveNumber() % 2 == 0) {
            newState.setRoundNumber(newState.getRoundNumber() + 1);
        }


        return newState;
    }

    private boolean isGameOver(IGameState state) {

        //get the macroboard 3x3
        String[][] macro = state.getField().getMacroboard();

        //Check rows for a win
        for (int r = 0; r < 3; r++) {
            if (!macro[r][0].equals(IField.EMPTY_FIELD) &&
                    !macro[r][0].equals(IField.AVAILABLE_FIELD) &&
                    macro[r][0].equals(macro[r][1]) &&
                    macro[r][1].equals(macro[r][2])) {
                return true;
            }
        }


            //Check column for a win
            for (int c = 0; c < 3; c++) {
                if (!macro[0][c].equals(IField.EMPTY_FIELD) &&
                        !macro[0][c].equals(IField.AVAILABLE_FIELD) &&
                        macro[0][c].equals(macro[1][c]) &&
                        macro[1][c].equals(macro[2][c])) {
                    return true;
                }
            }

            //Check diag for a win
                if (!macro[0][0].equals(IField.EMPTY_FIELD) &&
                        !macro[0][0].equals(IField.AVAILABLE_FIELD) &&
                        macro[0][0].equals(macro[1][1]) &&
                        macro[1][1].equals(macro[2][2])) {
                    return true;

            }

            //Check opDiag for a win
                if (!macro[0][2].equals(IField.EMPTY_FIELD) &&
                        !macro[0][2].equals(IField.AVAILABLE_FIELD) &&
                        macro[0][2].equals(macro[1][1]) &&
                        macro[1][1].equals(macro[2][0])) {
                    return true;
                }

                //also if there are no available moves the game is tie
                if(state.getField().getAvailableMoves().isEmpty()){
                    return true;
                }
                return false;
            }




        private int evaluate(IGameState state, int currentPlayer) {
            // Assume that if the game is over, state.getGameOver() will return a non-active state
            if(isGameOver(state)) {

                boolean botWon = true;
                if (botWon) {
                    return 100;
                }else{
                    return -100;
                }
            }
            return 0;
        }

    private int minimax(IGameState state, int depth, boolean isMaximizing, int currentPlayer) {
        if(depth == 0 || isGameOver(state)) {
            return evaluate(state, currentPlayer);
        }

        if(isMaximizing) {
            int maxEval = Integer.MIN_VALUE;
            for (IMove move : state.getField().getAvailableMoves()) {
                IGameState newState = simulateMove(state, move, currentPlayer);
                int eval = minimax(newState, depth - 1, false, currentPlayer);
                maxEval = Math.max(maxEval, eval);
            }
            return maxEval;
        }else{
            int minEval = Integer.MAX_VALUE;
            int opponent = (currentPlayer == 0) ? 1 : 0;
            for (IMove move : state.getField().getAvailableMoves()) {
                IGameState newState = simulateMove(state, move, opponent);
                int eval = minimax(newState, depth - 1, true, currentPlayer);
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
