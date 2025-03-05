package dk.easv.bll.bot;

import dk.easv.bll.field.IField;
import dk.easv.bll.game.IGameState;
import dk.easv.bll.move.IMove;
import dk.easv.bll.move.Move;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * ДЕМОНСТРАЦІЙНИЙ бот, що використовує мінімаксову логіку для локальної 3x3 дошки,
 * а також перетворює внутрішнє представлення "0"/"1"/"." на "X"/"O"/"".
 */
public class MyBot implements IBot {

    @Override
    public String getBotName() {
        // Назва вашого бота
        return "MyBot";
    }

    /**
     * Головний метод бота: з IGameState ми витягуємо 9x9 дошку, 3x3 макро,
     * список доступних ходів і запускаємо мінімакс. Повертаємо хід.
     */
    @Override
    public IMove doMove(IGameState state) {
        // 1) Отримуємо "сирі" дані з рушія:
        String[][] board9x9 = getFullBoard9x9(state);
        String[][] macro3x3 = getMacroBoard3x3(state);
        List<int[]> validCoords = getValidMoves(state);

        // 2) Вибираємо, у яку 3x3 (subBoard) треба грати:
        //    Якщо всі validCoords лежать в одному блоці 3x3, беремо його.
        //    Інакше (кілька блоків) — просто беремо випадково чи складніше.
        SubBoardChoice sbChoice = computeSubBoard(macro3x3, board9x9, validCoords);

        // 3) Виконуємо мінімакс (або випадковість) на цій 3x3:
        int[] localMove = computeLocalMove(sbChoice.subBoard);

        // 4) Перераховуємо локальний хід [0..2,0..2] у глобальні координати [0..8,0..8].
        int globalX = sbChoice.minCoord[0] + localMove[0];
        int globalY = sbChoice.minCoord[1] + localMove[1];

        // 5) Повертаємо хід у форматі IMove (наприклад, new Move(x, y)).
        return new Move(globalX, globalY);
    }

    //============================================================================
    // ДАЛІ — приватні методи, що допомагають боту отримати дані і порахувати хід
    //============================================================================

    /**
     * Витягує з state.getField().getBoard() (9x9) масив рядків
     * і конвертує "0" → "X", "1" → "O", "." → "" тощо.
     */
    private String[][] getFullBoard9x9(IGameState state) {
        String[][] source = state.getField().getBoard(); // 9x9
        String[][] result = new String[9][9];
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                String cell = source[i][j];
                if (cell.equals("0")) result[i][j] = "X";
                else if (cell.equals("1")) result[i][j] = "O";
                else if (cell.equals(IField.EMPTY_FIELD) || cell.equals(IField.AVAILABLE_FIELD) || cell.equals(".")) {
                    result[i][j] = "";
                } else {
                    // Можливо "TIE" чи інше - просто ставимо так само:
                    result[i][j] = cell;
                }
            }
        }
        return result;
    }

    /**
     * Те саме, але для макро-дошки 3x3 (отримуємо state.getField().getMacroboard()).
     */
    private String[][] getMacroBoard3x3(IGameState state) {
        String[][] source = state.getField().getMacroboard(); // 3x3
        String[][] result = new String[3][3];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                String cell = source[i][j];
                if (cell.equals("0")) result[i][j] = "X";
                else if (cell.equals("1")) result[i][j] = "O";
                else if (cell.equals(IField.EMPTY_FIELD) || cell.equals(IField.AVAILABLE_FIELD) || cell.equals(".")) {
                    result[i][j] = "";
                } else {
                    result[i][j] = cell;
                }
            }
        }
        return result;
    }

    /**
     * Отримує список доступних ходів (IMove) і перетворює його в List<int[]>.
     */
    private List<int[]> getValidMoves(IGameState state) {
        List<int[]> valid = new ArrayList<>();
        var moves = state.getField().getAvailableMoves(); // List<IMove>
        for (IMove m : moves) {
            valid.add(new int[]{ m.getX(), m.getY() });
        }
        return valid;
    }

    /**
     * Визначає, в яку 3x3 піддошку нам треба ходити (за validMoves).
     * Якщо всі validMoves в одному блоці 3x3 → беремо його.
     * Інакше вибираємо блок випадково або (як тут) теж через computeLocalMove.
     */
    private SubBoardChoice computeSubBoard(String[][] macro3x3,
                                           String[][] fullBoard,
                                           List<int[]> validCoords) {
        // Перевіримо, чи всі validCoords в одному блоці.
        int blockX = validCoords.get(0)[0] / 3; // від 0 до 2
        int blockY = validCoords.get(0)[1] / 3;
        boolean singleBlock = true;
        for (int[] c : validCoords) {
            int bx = c[0] / 3;
            int by = c[1] / 3;
            if (bx != blockX || by != blockY) {
                singleBlock = false;
                break;
            }
        }

        if (singleBlock) {
            // Усі ходи — в одній 3x3, отже граємо там:
            int minX = blockX * 3;
            int minY = blockY * 3;
            String[][] sub = extractSubBoard(fullBoard, minX, minY);
            return new SubBoardChoice(sub, new int[]{minX, minY});
        } else {
            // Є кілька доступних блоків — для прикладу, виберемо "макро-хід"
            // випадково або теж через локальний мінімакс:
            int[] macroMove = computeLocalMove(macro3x3); // [0..2,0..2]
            int minX = macroMove[0] * 3;
            int minY = macroMove[1] * 3;
            String[][] sub = extractSubBoard(fullBoard, minX, minY);
            return new SubBoardChoice(sub, new int[]{minX, minY});
        }
    }

    /**
     * "Вирізає" 3x3 піддошку (sub-board) з повного 9x9, починаючи з (startX, startY).
     */
    private String[][] extractSubBoard(String[][] fullBoard, int startX, int startY) {
        String[][] sub = new String[3][3];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                sub[i][j] = fullBoard[startX + i][startY + j];
            }
        }
        return sub;
    }

    /**
     * Обчислює хід у межах 3x3 дошки: якщо багато вільних клітин,
     * ходимо випадково, інакше запускаємо мінімакс.
     */
    private int[] computeLocalMove(String[][] local3x3) {
        List<int[]> freeCells = getFreeCells(local3x3);
        // Якщо це «початок гри» (багато порожніх клітин),
        // зробимо рандомний хід або просту евристику.
        if (freeCells.size() >= 8) {
            return freeCells.get(new Random().nextInt(freeCells.size()));
        }
        // Інакше запускаємо мінімакс
        Object result = minimax(/*maximizing=*/true, copyOf(local3x3), 0, Integer.MIN_VALUE, Integer.MAX_VALUE);

        if (result instanceof int[]) {
            return (int[]) result; // (row,col)
        } else {
            // fallback, якщо щось пішло не так
            return freeCells.get(0);
        }
    }

    /**
     * Класичний мінімакс із альфа-бета. Для 3x3 локальної дошки.
     * Повертає або координати (int[]) на рівні depth=0, або оціночне значення (Integer).
     */
    private Object minimax(boolean maxTurn, String[][] board, int depth, int alpha, int beta) {
        // Перевіряємо базові умови: виграш X, виграш O, нічия
        if (checkTris("X", board)) {
            return 10 - depth; // чим менше depth, тим краще для X
        }
        if (checkTris("O", board)) {
            return depth - 10; // чим більше depth, тим краще для O
        }
        if (isTie(board)) {
            return 0;
        }

        List<Object> children = new ArrayList<>();

        // Перебір порожніх клітин (9 максимум)
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (board[i][j].equals("")) {
                    // Ставимо X чи O
                    board[i][j] = maxTurn ? "X" : "O";
                    Object scoreObj = minimax(!maxTurn, board, depth + 1, alpha, beta);
                    int score = (scoreObj instanceof Integer) ? (int) scoreObj : 0;

                    // Відкочуємо
                    board[i][j] = "";

                    // Альфа-бета
                    if (maxTurn) {
                        alpha = Math.max(alpha, score);
                    } else {
                        beta = Math.min(beta, score);
                    }
                    if (alpha >= beta) {
                        break; // pruning
                    }

                    // На рівні depth=0 зберігаємо (coords, score)
                    // Інакше – лише score
                    if (depth == 0) {
                        children.add(new MoveScore(new int[]{i, j}, score));
                    } else {
                        children.add(score);
                    }
                }
            }
        }
        if (children.isEmpty()) {
            return 0; // не знайшли ходів
        }

        if (depth == 0) {
            // Повертаємо кращий чи гірший MoveScore
            MoveScore bestMS = null;
            if (maxTurn) {
                // max
                int bestVal = Integer.MIN_VALUE;
                for (Object o : children) {
                    MoveScore ms = (MoveScore) o;
                    if (ms.score > bestVal) {
                        bestVal = ms.score;
                        bestMS = ms;
                    }
                }
            } else {
                // min
                int bestVal = Integer.MAX_VALUE;
                for (Object o : children) {
                    MoveScore ms = (MoveScore) o;
                    if (ms.score < bestVal) {
                        bestVal = ms.score;
                        bestMS = ms;
                    }
                }
            }
            return (bestMS != null) ? bestMS.coords : new int[]{1,1};
        } else {
            // Усередині дерева повертаємо одне число (мін / макс).
            if (maxTurn) {
                int best = Integer.MIN_VALUE;
                for (Object o : children) {
                    int val = (int) o;
                    best = Math.max(best, val);
                }
                return best;
            } else {
                int best = Integer.MAX_VALUE;
                for (Object o : children) {
                    int val = (int) o;
                    best = Math.min(best, val);
                }
                return best;
            }
        }
    }

    //==============================
    // Допоміжні методи мінімаксу
    //==============================

    /** Перевіряє, чи немає порожніх клітин (тобто нічия для 3x3). */
    private boolean isTie(String[][] b) {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (b[i][j].equals("")) return false;
            }
        }
        return true;
    }

    /** Перевіряє, чи гравець (X чи O) має 3 в ряд (гориз, верт, діагоналі) в 3x3. */
    private boolean checkTris(String player, String[][] b) {
        for (int i = 0; i < 3; i++) {
            // рядок
            if (b[i][0].equals(player) && b[i][1].equals(player) && b[i][2].equals(player))
                return true;
            // стовпець
            if (b[0][i].equals(player) && b[1][i].equals(player) && b[2][i].equals(player))
                return true;
        }
        // діагоналі
        if (b[0][0].equals(player) && b[1][1].equals(player) && b[2][2].equals(player))
            return true;
        if (b[0][2].equals(player) && b[1][1].equals(player) && b[2][0].equals(player))
            return true;

        return false;
    }

    /** Збирає список порожніх (вільних) клітин (row,col) у 3x3. */
    private List<int[]> getFreeCells(String[][] b) {
        List<int[]> list = new ArrayList<>();
        for (int i = 0; i < b.length; i++) {
            for (int j = 0; j < b[i].length; j++) {
                if (b[i][j].equals("")) {
                    list.add(new int[]{i, j});
                }
            }
        }
        return list;
    }

    /** Створює копію 3x3 масиву рядків. */
    private String[][] copyOf(String[][] src) {
        String[][] copy = new String[3][3];
        for (int i = 0; i < 3; i++) {
            System.arraycopy(src[i], 0, copy[i], 0, 3);
        }
        return copy;
    }

    //================================================
    // Внутрішні допоміжні класи
    //================================================

    /** Зберігаємо пару: координати та оціночне значення для кореневої глибини. */
    private static class MoveScore {
        int[] coords; // (row, col)
        int score;
        MoveScore(int[] c, int s) {
            coords = c;
            score = s;
        }
    }

    /** Зберігає (subBoard 3x3) та (minCoord) — де саме ця 3x3 лежить у 9x9. */
    private static class SubBoardChoice {
        String[][] subBoard;
        int[] minCoord;
        public SubBoardChoice(String[][] sb, int[] mc) {
            this.subBoard = sb;
            this.minCoord = mc;
        }
    }
}
