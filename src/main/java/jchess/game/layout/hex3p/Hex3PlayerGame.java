package jchess.game.layout.hex3p;

import jchess.ecs.EcsEvent;
import jchess.ecs.Entity;
import jchess.ecs.EntityManager;
import jchess.game.common.BoardCanvas;
import jchess.game.common.BoardClickedListener;
import jchess.game.common.BoardMouseListener;
import jchess.game.common.RenderContext;
import jchess.game.common.marker.MarkerRenderSystem;
import jchess.game.common.piece.PieceComponent;
import jchess.game.common.piece.PieceIdentifier;
import jchess.game.common.piece.PieceRenderSystem;
import jchess.game.common.tile.TileComponent;
import jchess.game.common.tile.TileRenderSystem;

import javax.swing.JFrame;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.io.File;
import java.net.URISyntaxException;

public class Hex3PlayerGame {
    public static void main(String[] args) throws URISyntaxException {
        String themePath = "/jchess/theme/v2/default";
        Theme theme1 = new Theme(new File(Hex3PlayerGame.class.getResource(themePath).toURI()));
        Hex3PlayerGame game = new Hex3PlayerGame(theme1);

        JFrame mainFrame = new JFrame("Test");
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Container frameContentPane = mainFrame.getContentPane();
        frameContentPane.setLayout(new FlowLayout(FlowLayout.CENTER));

        game.start();

        frameContentPane.add(game.boardCanvas);
        mainFrame.pack();
        mainFrame.setVisible(true);
    }

    public final Theme theme;
    private final Hex3pGameState gameState;
    public final EntityManager entityManager;
    public final EcsEvent<Void> renderEvent;
    public final EcsEvent<MoveInfo> pieceMoveEvent;
    private final BoardCanvas boardCanvas;
    private final BoardClickedListener boardClickListener;
    private Entity[][] tiles;
    private static final int numTilesHorizontal = 17 + 16;
    private static final int numTilesVertical = 17;
    private static final int PLAYER_LIGHT = 0;
    private static final int PLAYER_MEDIUM = 1;
    private static final int PLAYER_DARK = 2;

    public Hex3PlayerGame(Theme theme) {
        this.theme = theme;
        gameState = new Hex3pGameState();
        entityManager = new EntityManager();
        renderEvent = new EcsEvent<>(entityManager);
        pieceMoveEvent = new EcsEvent<>(entityManager);
        boardCanvas = new BoardCanvas(renderEvent, this::boardTransform);
        boardClickListener = new BoardClickedListener(
                gameState, entityManager, renderEvent,
                (fromTile, toTile) -> pieceMoveEvent.fire(new MoveInfo(fromTile, toTile))
        );

        BoardMouseListener boardMouseListener = new BoardMouseListener();
        boardMouseListener.addClickListener(this::onBoardClicked);
        boardCanvas.setPreferredSize(new Dimension(510, 416));
        boardCanvas.addMouseListener(boardMouseListener);

        RenderContext renderContext = new RenderContext(entityManager, renderEvent, boardCanvas);
        renderEvent.registerSystem(new TileRenderSystem(renderContext), 99);
        renderEvent.registerSystem(new PieceRenderSystem(renderContext), 98);
        MarkerRenderSystem markerRenderSystem = new MarkerRenderSystem(renderContext);
        markerRenderSystem.setMarkerImages(
                theme.board.hexMarker_noAction,
                theme.board.hexMarker_yesAction,
                theme.board.hexMarker_selected
        );
        renderEvent.registerSystem(markerRenderSystem, 97);

        // TODO erja, register system for tracking the move history (and etc.) here
        pieceMoveEvent.addPostEventListener(move -> gameState.nextPlayer());
    }

    public void start() {
        generateBoard();
        renderEvent.fire(null);
    }

    private double getCanvasTileScaleFactor() {
        int canvasWidth = boardCanvas.getWidth();
        int canvasHeight = boardCanvas.getHeight();

        int totalWidth = 17 * 30; // 17 = num tiles in center row | 30 = tile width in pixels
        int totalHeight = (9 * 32) + (8 * 16); // 32 = full tile height | 16 = tile edge length

        double scaleFactor = ((double) canvasWidth) / totalWidth;
        if (scaleFactor * totalHeight > canvasHeight) {
            scaleFactor = ((double) canvasHeight) / totalHeight;
        }

        return scaleFactor;
    }

    private BoardCanvas.TransformInfo boardTransform(Image icon, Point pos, int canvasWidth, int canvasHeight) {
        double scaleFactor = getCanvasTileScaleFactor();
        return new BoardCanvas.TransformInfo(
                (int) (pos.x * 15 * scaleFactor),
                (int) (pos.y * 24 * scaleFactor),
                (int) (30 * scaleFactor),
                (int) (32 * scaleFactor)
        );
    }

    private void onBoardClicked(MouseEvent e) {
        if (e.getButton() != MouseEvent.BUTTON1) return;

        // this is only loosely approximating the actual hexagon hitBox
        double scaleFactor = getCanvasTileScaleFactor();
        Point clickPos = e.getPoint();
        int x = (int) (clickPos.x / (15 * scaleFactor));
        int y = (int) (clickPos.y / (24 * scaleFactor));

        if(y % 2 == 0) {
            x = x - (x % 2);
        }else{
            x = x - ((x + 1) % 2);
        }

        Entity tile = getTile(x, y);
        if (tile != null) {
            boardClickListener.onClick(tile);
        }
    }

    private Entity getTile(int x, int y) {
        if (x < 0 || x >= numTilesHorizontal) return null;
        if (y < 0 || y >= numTilesVertical) return null;

        return tiles[y][x];
    }

    private void generateBoard() {
        tiles = new Entity[numTilesVertical][numTilesHorizontal];
        // first pass: create entities
        for (int y = 0; y < numTilesVertical; y++) {
            Entity[] tileRow = tiles[y];
            int x0 = Math.abs(8 - y);
            int x1 = 32 - x0;
            for (int x = x0; x <= x1; x += 2) {
                tileRow[x] = entityManager.createEntity();
            }
        }

        // second pass: fill entities with components
        for (int y = 0; y < numTilesVertical; y++) {
            Entity[] tileRow = tiles[y];
            int x0 = Math.abs(8 - y);
            int x1 = 32 - x0;
            for (int x = x0; x <= x1; x += 2) {
                TileComponent tile = new TileComponent();
                tile.icon = (x % 3 == 0) ? theme.board.hexLight
                        : ((x % 3 == 1) ? theme.board.hexMedium
                        : theme.board.hexDark);
                tile.position = new Point(x, y);

                tile.neighborsByDirection.put(0, getTile(x, y - 2));
                tile.neighborsByDirection.put(30, getTile(x + 1, y - 1));
                tile.neighborsByDirection.put(60, getTile(x + 3, y - 1));
                tile.neighborsByDirection.put(90, getTile(x + 2, y));
                tile.neighborsByDirection.put(120, getTile(x + 3, y + 1));
                tile.neighborsByDirection.put(150, getTile(x + 1, y + 1));
                tile.neighborsByDirection.put(180, getTile(x, y + 2));
                tile.neighborsByDirection.put(210, getTile(x - 1, y + 1));
                tile.neighborsByDirection.put(240, getTile(x - 3, y + 1));
                tile.neighborsByDirection.put(270, getTile(x - 2, y));
                tile.neighborsByDirection.put(300, getTile(x - 3, y - 1));
                tile.neighborsByDirection.put(330, getTile(x - 1, y - 1));

                tileRow[x].tile = tile;
            }
        }

        placeRook(8, 16, PLAYER_LIGHT);
        placeRook(24, 16, PLAYER_LIGHT);
        placeRook(32, 8, PLAYER_MEDIUM);
        placeRook(24, 0, PLAYER_MEDIUM);
        placeRook(8, 0, PLAYER_DARK);
        placeRook(0, 8, PLAYER_DARK);

        placeKnight(10, 16, PLAYER_LIGHT);
        placeKnight(22, 16, PLAYER_LIGHT);
        placeKnight(31, 7, PLAYER_MEDIUM);
        placeKnight(25, 1, PLAYER_MEDIUM);
        placeKnight(7, 1, PLAYER_DARK);
        placeKnight(1, 7, PLAYER_DARK);

        placeBishop(12, 16, PLAYER_LIGHT);
        placeBishop(16, 16, PLAYER_LIGHT);
        placeBishop(20, 16, PLAYER_LIGHT);
        placeBishop(30, 6, PLAYER_MEDIUM);
        placeBishop(28, 4, PLAYER_MEDIUM);
        placeBishop(26, 2, PLAYER_MEDIUM);
        placeBishop(6, 2, PLAYER_DARK);
        placeBishop(4, 4, PLAYER_DARK);
        placeBishop(2, 6, PLAYER_DARK);

        placeQueen(14, 16, PLAYER_LIGHT);
        placeQueen(29, 5, PLAYER_MEDIUM);
        placeQueen(5, 3, PLAYER_DARK);

        placeKing(18, 16, PLAYER_LIGHT);
        placeKing(27, 3, PLAYER_MEDIUM);
        placeKing(3, 5, PLAYER_DARK);

        for (int i = 0; i < 10; i++) {
            placePawn(7 + (i * 2), 15, PLAYER_LIGHT);
            placePawn(31 - i, 9 - i, PLAYER_MEDIUM);
            placePawn(10 - i, i, PLAYER_DARK);
        }
        for (int i = 0; i < 9; i++) {
            placePawn(8 + (i * 2), 14, PLAYER_LIGHT);
            placePawn(29 - i, 9 - i, PLAYER_MEDIUM);
            placePawn(11 - i, 1 + i, PLAYER_DARK);
        }
    }

    public void placeRook(int x, int y, int playerColor) {
        placePiece(x, y, playerColor, PieceMoveRules.PieceType.Rook, theme.piece.rook.get(playerColor));
    }

    public void placeKnight(int x, int y, int playerColor) {
        placePiece(x, y, playerColor, PieceMoveRules.PieceType.Knight, theme.piece.knight.get(playerColor));
    }

    public void placeBishop(int x, int y, int playerColor) {
        placePiece(x, y, playerColor, PieceMoveRules.PieceType.Bishop, theme.piece.bishop.get(playerColor));
    }

    public void placeQueen(int x, int y, int playerColor) {
        placePiece(x, y, playerColor, PieceMoveRules.PieceType.Queen, theme.piece.queen.get(playerColor));
    }

    public void placeKing(int x, int y, int playerColor) {
        placePiece(x, y, playerColor, PieceMoveRules.PieceType.King, theme.piece.king.get(playerColor));
    }

    public void placePawn(int x, int y, int playerColor) {
        placePiece(x, y, playerColor, PieceMoveRules.PieceType.Pawn, theme.piece.pawn.get(playerColor));
    }

    private void placePiece(int x, int y, int playerColor, PieceMoveRules.PieceType pieceType, Image icon) {
        Entity tile = getTile(x, y);
        if (tile == null) {
            System.err.println("cannot place piece on tile (" + x + ", " + y + "). No tile found.");
            return;
        }

        PieceIdentifier pieceId = new PieceIdentifier(
                pieceType.getId(),
                pieceType.getShortName(),
                icon,
                playerColor,
                ((playerColor - 3) * (-120)) % 360 // [0, 240, 120]
        );
        PieceComponent piece = new PieceComponent();
        piece.identifier = pieceId;
        piece.moveSet = new PieceMoveRules(pieceType, pieceId);
        tile.piece = piece;
    }

}
