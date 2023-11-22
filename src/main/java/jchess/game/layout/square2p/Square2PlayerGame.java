package jchess.game.layout.square2p;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.JFrame;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.io.File;
import java.net.URISyntaxException;

public class Square2PlayerGame {
    private static final Logger logger = LoggerFactory.getLogger(Square2PlayerGame.class);
    public static void main(String[] args) throws URISyntaxException {
        String themePath = "/jchess/theme/v2/default";
        Theme theme1 = new Theme(new File(Square2PlayerGame.class.getResource(themePath).toURI()));
        Square2PlayerGame game = new Square2PlayerGame(theme1);

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
    private final Square2pGameState gameState;
    public final EntityManager entityManager;
    public final EcsEvent<Void> renderEvent;
    public final EcsEvent<MoveInfo> pieceMoveEvent;
    private final BoardCanvas boardCanvas;
    private final BoardClickedListener boardClickListener;
    private Entity[][] tiles;

    private static final int numTiles = 8;
    private static final int tileWidth = 50;
    private static final int tileHeight = 50;
    private static final double tileFraction = 1d / numTiles;

    public Square2PlayerGame(Theme theme) {
        this.theme = theme;
        gameState = new Square2pGameState();
        entityManager = new EntityManager();
        renderEvent = new EcsEvent<>(entityManager);
        pieceMoveEvent = new EcsEvent<>(entityManager);
        boardCanvas = new BoardCanvas(renderEvent, Square2PlayerGame::boardTransform);
        boardClickListener = new BoardClickedListener(
                gameState, entityManager, renderEvent,
                (fromTile, toTile) -> pieceMoveEvent.fire(new MoveInfo(fromTile, toTile))
        );

        BoardMouseListener boardMouseListener = new BoardMouseListener();
        boardMouseListener.addClickListener(this::onBoardClicked);
        boardCanvas.setPreferredSize(new Dimension(tileWidth * numTiles, tileHeight * numTiles));
        boardCanvas.addMouseListener(boardMouseListener);

        RenderContext renderContext = new RenderContext(entityManager, renderEvent, boardCanvas);
        renderEvent.registerSystem(new TileRenderSystem(renderContext), 99);
        renderEvent.registerSystem(new PieceRenderSystem(renderContext), 98);
        MarkerRenderSystem markerRenderSystem = new MarkerRenderSystem(renderContext);
        markerRenderSystem.setMarkerImages(
                theme.board.tileMarker_noAction,
                theme.board.tileMarker_yesAction,
                theme.board.tileMarker_selected
        );
        renderEvent.registerSystem(markerRenderSystem, 97);

        // TODO erja, register system for tracking the move history (and etc.) here
        pieceMoveEvent.addPostEventListener(move -> gameState.nextPlayer());
    }

    public void start() {
        generateBoard();

        // draw initial board state
        renderEvent.fire(null);
    }

    private static BoardCanvas.TransformInfo boardTransform(Image icon, Point pos, int canvasWidth, int canvasHeight) {
        return new BoardCanvas.TransformInfo(
                (int) (pos.x * canvasWidth * tileFraction),
                (int) (pos.y * canvasHeight * tileFraction),
                (int) (canvasWidth * tileFraction),
                (int) (canvasHeight * tileFraction)
        );
    }

    private void onBoardClicked(MouseEvent e) {
        if (e.getButton() != MouseEvent.BUTTON1) return;

        Point clickPos = e.getPoint();
        int x = clickPos.x * numTiles / boardCanvas.getWidth();
        int y = clickPos.y * numTiles / boardCanvas.getHeight();

        Entity tile = getTile(x, y);
        if (tile != null) {
            boardClickListener.onClick(tile);
        }
    }

    private Entity getTile(int x, int y) {
        if (x < 0 || x >= numTiles) return null;
        if (y < 0 || y >= numTiles) return null;

        return tiles[y][x];
    }

    private void generateBoard() {
        tiles = new Entity[numTiles][numTiles];
        // first pass: create entities
        for (int y = 0; y < numTiles; y++) {
            Entity[] tileRow = tiles[y];
            for (int x = 0; x < numTiles; x++) {
                tileRow[x] = entityManager.createEntity();
            }
        }

        // second pass: fill entities with components
        for (int y = 0; y < numTiles; y++) {
            Entity[] tileRow = tiles[y];
            for (int x = 0; x < numTiles; x++) {
                TileComponent tile = new TileComponent();
                tile.icon = ((x + y) % 2 == 0) ? theme.board.tileLight : theme.board.tileDark;
                tile.position = new Point(x, y);

                tile.neighborsByDirection.put(0, getTile(x, y - 1));
                tile.neighborsByDirection.put(45, getTile(x + 1, y - 1));
                tile.neighborsByDirection.put(90, getTile(x + 1, y));
                tile.neighborsByDirection.put(135, getTile(x + 1, y + 1));
                tile.neighborsByDirection.put(180, getTile(x, y + 1));
                tile.neighborsByDirection.put(225, getTile(x - 1, y + 1));
                tile.neighborsByDirection.put(270, getTile(x - 1, y));
                tile.neighborsByDirection.put(315, getTile(x - 1, y - 1));
                tileRow[x].tile = tile;
            }
        }

        placeRook(0, 0, false);
        placeRook(7, 0, false);
        placeRook(0, 7, true);
        placeRook(7, 7, true);

        placeKnight(1, 0, false);
        placeKnight(6, 0, false);
        placeKnight(1, 7, true);
        placeKnight(6, 7, true);

        placeBishop(2, 0, false);
        placeBishop(5, 0, false);
        placeBishop(2, 7, true);
        placeBishop(5, 7, true);

        placeQueen(3, 0, false);
        placeQueen(3, 7, true);

        placeKing(4, 0, false);
        placeKing(4, 7, true);

        for (int x = 0; x < numTiles; x++) {
            placePawn(x, 1, false);
            placePawn(x, 6, true);
        }
    }

    public void placeRook(int x, int y, boolean isWhite) {
        Image icon = isWhite ? theme.piece.rookWhite : theme.piece.rookBlack;
        placePiece(x, y, isWhite, PieceMoveRules.PieceType.Rook, icon);
    }

    public void placeKnight(int x, int y, boolean isWhite) {
        Image icon = isWhite ? theme.piece.knightWhite : theme.piece.knightBlack;
        placePiece(x, y, isWhite, PieceMoveRules.PieceType.Knight, icon);
    }

    public void placeBishop(int x, int y, boolean isWhite) {
        Image icon = isWhite ? theme.piece.bishopWhite : theme.piece.bishopBlack;
        placePiece(x, y, isWhite, PieceMoveRules.PieceType.Bishop, icon);
    }

    public void placeQueen(int x, int y, boolean isWhite) {
        Image icon = isWhite ? theme.piece.queenWhite : theme.piece.queenBlack;
        placePiece(x, y, isWhite, PieceMoveRules.PieceType.Queen, icon);
    }

    public void placeKing(int x, int y, boolean isWhite) {
        Image icon = isWhite ? theme.piece.kingWhite : theme.piece.kingBlack;
        placePiece(x, y, isWhite, PieceMoveRules.PieceType.King, icon);
    }

    public void placePawn(int x, int y, boolean isWhite) {
        Image icon = isWhite ? theme.piece.pawnWhite : theme.piece.pawnBlack;
        placePiece(x, y, isWhite, PieceMoveRules.PieceType.Pawn, icon);
    }

    private void placePiece(int x, int y, boolean isWhite, PieceMoveRules.PieceType pieceType, Image icon) {
        Entity tile = getTile(x, y);
        if (tile == null) {
            logger.error("cannot place piece on tile ({}, {}). No tile found.", x, y);
            return;
        }

        PieceIdentifier pieceIdentifier = new PieceIdentifier(
                pieceType.getId(),
                pieceType.getShortName(),
                icon,
                isWhite ? 0 : 1,
                isWhite ? 0 : 180
        );

        PieceComponent piece = new PieceComponent();
        piece.identifier = pieceIdentifier;
        piece.moveSet = new PieceMoveRules(pieceType, pieceIdentifier);
        tile.piece = piece;
    }

}
