package dk.easv.bll.bot;

import dk.easv.bll.bot.IBot;
import dk.easv.bll.field.IField;
import dk.easv.bll.game.IGameState;
import dk.easv.bll.move.IMove;
import dk.easv.bll.move.Move;

import java.util.ArrayList;
import java.util.List;


public class LocalPrioritisedListBot implements IBot {

    private static final String BOTNAME = "Local Prio ListBot";
    // Moves {row, col} in order of preferences. {0, 0} at top-left corner
    protected int[][] preferredMoves = {
            {1, 1}, //Center
            {0, 0}, {2, 2}, {0, 2}, {2, 0},  //Corners ordered across
            {0, 1}, {2, 1}, {1, 0}, {1, 2}}; //Outer Middles ordered across

    /**
     * Makes a turn. Edit this method to make your bot smarter.
     * A bot that uses a local prioritised list algorithm, in order to win any local board,
     * and if all boards are available for play, it'll run a on the macroboard,
     * to select which board to play in.
     *
     * @return The selected move we want to make.
     */
    @Override
    public IMove doMove(IGameState state) {

        List<IMove> winMoves = getWinningMoves(state);
        if (!winMoves.isEmpty()) {
            return winMoves.get(0);
        }

        //Find macroboard to play in
        for (int[] move : preferredMoves) {
            if (state.getField().getMacroboard()[move[0]][move[1]].equals(IField.AVAILABLE_FIELD)) {
                //find move to play

                for (int[] selectedMove : preferredMoves) {
                    int x = move[0] * 3 + selectedMove[0];
                    int y = move[1] * 3 + selectedMove[1];
                    if (state.getField().getBoard()[x][y].equals(IField.EMPTY_FIELD)) {
                        return new Move(x, y);
                    }
                }
            }
        }

        //NOTE: Something failed, just take the first available move I guess!
        return state.getField().getAvailableMoves().get(0);
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

    //compile a list of all available winning moves
    private List<IMove> getWinningMoves(IGameState state) {
        String player = "1";
        if (state.getMoveNumber() % 2 == 0)
            player = "0";


        List<IMove> avail = state.getField().getAvailableMoves();

        List<IMove> winningMoves = new ArrayList<>();
        for (IMove move : avail) {
            if(isWinningMove(state, move, player))
                winningMoves.add(move);

            }
            return winningMoves;

        }



        @Override
        public String getBotName () {
            return BOTNAME;
        }
    }
