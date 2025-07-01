package chess.protocol;

import chess.data.Game;
import chess.data.GameState;
import chess.data.Move;

import java.text.SimpleDateFormat;
import java.util.Date;

import static chess.utilities.squareUtilities.toAlgebraic;

public class PGNExporter {
    public static String toPGN(Game game, String site) {
        StringBuilder builder = new StringBuilder();

        // PGN headers
        String dateString = new SimpleDateFormat("yyyy.MM.dd").format(new Date());
        builder.append("[Event \"Live Chess\"]\n");
        builder.append("[Site \"").append(site).append("\"]\n");
        builder.append("[Date \"").append(dateString).append("\"]\n");
        builder.append("[Round \"-\"]\n");
        builder.append("[White \"White\"]\n");
        builder.append("[Black \"Black\"]\n");

        String result = switch (game.state()) {
            case GameState.Draw Draw     -> "1/2-1/2";
            case GameState.WhiteWins WhiteWins -> "1-0";
            case GameState.BlackWins BlackWins -> "0-1";
            case GameState.Stalemate Stalemate -> "1/2-1/2";
            default                    -> "*";
        };
        builder.append("[Result \"").append(result).append("\"]\n\n");

        // Move list
        int moveNumber = 1;
        for (int i = 0; i < game.moveHistory().size(); i++) {
            if (i % 2 == 0) {
                builder.append(moveNumber).append(". ");
            }
            Move move = game.moveHistory().get(i);
            builder.append(toAlgebraic(move)).append(" ");
            if (i % 2 == 1) moveNumber++;
        }

        return builder.toString();
    }
}