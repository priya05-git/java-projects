import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.*;
import java.util.List;

/**
 * Chess.java — A complete two-player Chess game with Swing GUI
 *
 * Features:
 *  - Full chess rules: castling, en passant, pawn promotion
 *  - Check / Checkmate / Stalemate detection
 *  - Move highlighting (legal-move dots)
 *  - Last-move highlight
 *  - Captured-piece panel
 *  - Turn indicator + game-status bar
 *
 * Run:  javac Chess.java && java Chess
 */
public class Chess {

    // ─────────────────────────── Constants ───────────────────────────
    static final int TILE  = 80;
    static final int BOARD = TILE * 8;

    // Piece codes  (sign = colour: + white, - black)
    static final int EMPTY  = 0;
    static final int PAWN   = 1;
    static final int KNIGHT = 2;
    static final int BISHOP = 3;
    static final int ROOK   = 4;
    static final int QUEEN  = 5;
    static final int KING   = 6;

    // Colors
    static final Color C_LIGHT      = new Color(0xF0D9B5);
    static final Color C_DARK       = new Color(0xB58863);
    static final Color C_SELECT     = new Color(0x829769);
    static final Color C_MOVE_DOT   = new Color(0x20, 0x20, 0x20, 70);
    static final Color C_LAST_MOVE  = new Color(0xCD, 0xD1, 0x6E, 180);
    static final Color C_CHECK      = new Color(0xE7, 0x4C, 0x3C, 160);
    static final Color C_PANEL_BG   = new Color(0x2C2C2C);
    static final Color C_TEXT       = new Color(0xEEEEEE);
    static final Color C_ACCENT     = new Color(0xE8C97A);

    // ─────────────────────────── Entry Point ───────────────────────────
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("♟ Chess");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setResizable(false);

            GamePanel gp = new GamePanel();
            frame.add(gp);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }

    // ═══════════════════════════════════════════════════════════════════
    //  Move record
    // ═══════════════════════════════════════════════════════════════════
    static class Move {
        int fromR, fromC, toR, toC;
        int piece, captured;
        boolean isEnPassant, isCastle, isPromotion;
        int promotionPiece;
        int[] rookFrom, rookTo;          // for castling
        int enPassantCapR, enPassantCapC; // square where captured pawn stood

        Move(int fr, int fc, int tr, int tc, int piece, int captured) {
            this.fromR = fr; this.fromC = fc;
            this.toR   = tr; this.toC   = tc;
            this.piece = piece; this.captured = captured;
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    //  Board logic
    // ═══════════════════════════════════════════════════════════════════
    static class Board {
        int[][] grid = new int[8][8];

        // Castling rights  [white king-side, white queen-side, black king-side, black queen-side]
        boolean[] castleRights = {true, true, true, true};

        // En-passant target square (-1 = none)
        int epRow = -1, epCol = -1;

        // Last move (for highlight)
        Move lastMove = null;

        // Captured pieces
        List<Integer> capturedByWhite = new ArrayList<>();
        List<Integer> capturedByBlack = new ArrayList<>();

        Board() { setup(); }

        void setup() {
            // White pieces  (positive)
            int[] backRow = {ROOK, KNIGHT, BISHOP, QUEEN, KING, BISHOP, KNIGHT, ROOK};
            for (int c = 0; c < 8; c++) {
                grid[7][c] =  backRow[c];
                grid[6][c] =  PAWN;
                grid[1][c] = -PAWN;
                grid[0][c] = -backRow[c];
            }
        }

        boolean inBounds(int r, int c) { return r >= 0 && r < 8 && c >= 0 && c < 8; }

        int colorOf(int piece) {
            if (piece > 0) return  1;
            if (piece < 0) return -1;
            return 0;
        }

        // ── Generate pseudo-legal moves for piece at (r,c) ──────────────
        List<Move> pseudoMoves(int r, int c) {
            List<Move> moves = new ArrayList<>();
            int piece = grid[r][c];
            if (piece == EMPTY) return moves;
            int color = colorOf(piece);
            int type  = Math.abs(piece);

            switch (type) {
                case PAWN:   addPawnMoves(moves, r, c, color); break;
                case KNIGHT: addKnightMoves(moves, r, c, color); break;
                case BISHOP: addSlidingMoves(moves, r, c, color, new int[][]{{1,1},{1,-1},{-1,1},{-1,-1}}); break;
                case ROOK:   addSlidingMoves(moves, r, c, color, new int[][]{{1,0},{-1,0},{0,1},{0,-1}}); break;
                case QUEEN:
                    addSlidingMoves(moves, r, c, color, new int[][]{{1,1},{1,-1},{-1,1},{-1,-1}});
                    addSlidingMoves(moves, r, c, color, new int[][]{{1,0},{-1,0},{0,1},{0,-1}});
                    break;
                case KING:   addKingMoves(moves, r, c, color); break;
            }
            return moves;
        }

        void addPawnMoves(List<Move> moves, int r, int c, int color) {
            int dir  = (color == 1) ? -1 : 1;
            int start = (color == 1) ? 6 : 1;
            int promRow = (color == 1) ? 0 : 7;

            // One step forward
            int nr = r + dir;
            if (inBounds(nr, c) && grid[nr][c] == EMPTY) {
                addPawnMove(moves, r, c, nr, c, EMPTY, promRow, color);
                // Two steps from start
                if (r == start && grid[r + 2*dir][c] == EMPTY) {
                    moves.add(new Move(r, c, r + 2*dir, c, grid[r][c], EMPTY));
                }
            }
            // Captures
            for (int dc : new int[]{-1, 1}) {
                int nc = c + dc;
                if (!inBounds(nr, nc)) continue;
                if (colorOf(grid[nr][nc]) == -color) {
                    addPawnMove(moves, r, c, nr, nc, grid[nr][nc], promRow, color);
                }
                // En passant
                if (nr == epRow && nc == epCol) {
                    Move m = new Move(r, c, nr, nc, grid[r][c], grid[r][nc]);
                    m.isEnPassant = true;
                    m.enPassantCapR = r; m.enPassantCapC = nc;
                    moves.add(m);
                }
            }
        }

        void addPawnMove(List<Move> moves, int fr, int fc, int tr, int tc, int cap, int promRow, int color) {
            if (tr == promRow) {
                for (int pp : new int[]{QUEEN, ROOK, BISHOP, KNIGHT}) {
                    Move m = new Move(fr, fc, tr, tc, grid[fr][fc], cap);
                    m.isPromotion = true;
                    m.promotionPiece = color * pp;
                    moves.add(m);
                }
            } else {
                moves.add(new Move(fr, fc, tr, tc, grid[fr][fc], cap));
            }
        }

        void addKnightMoves(List<Move> moves, int r, int c, int color) {
            int[][] deltas = {{-2,-1},{-2,1},{-1,-2},{-1,2},{1,-2},{1,2},{2,-1},{2,1}};
            for (int[] d : deltas) {
                int nr = r + d[0], nc = c + d[1];
                if (inBounds(nr, nc) && colorOf(grid[nr][nc]) != color)
                    moves.add(new Move(r, c, nr, nc, grid[r][c], grid[nr][nc]));
            }
        }

        void addSlidingMoves(List<Move> moves, int r, int c, int color, int[][] dirs) {
            for (int[] d : dirs) {
                int nr = r + d[0], nc = c + d[1];
                while (inBounds(nr, nc)) {
                    if (colorOf(grid[nr][nc]) == color) break;
                    moves.add(new Move(r, c, nr, nc, grid[r][c], grid[nr][nc]));
                    if (grid[nr][nc] != EMPTY) break;
                    nr += d[0]; nc += d[1];
                }
            }
        }

        void addKingMoves(List<Move> moves, int r, int c, int color) {
            int[][] deltas = {{-1,-1},{-1,0},{-1,1},{0,-1},{0,1},{1,-1},{1,0},{1,1}};
            for (int[] d : deltas) {
                int nr = r + d[0], nc = c + d[1];
                if (inBounds(nr, nc) && colorOf(grid[nr][nc]) != color)
                    moves.add(new Move(r, c, nr, nc, grid[r][c], grid[nr][nc]));
            }
            // Castling
            if (color == 1) { // White
                if (castleRights[0] && grid[7][5]==EMPTY && grid[7][6]==EMPTY
                        && !isAttacked(7,4,-1) && !isAttacked(7,5,-1) && !isAttacked(7,6,-1)) {
                    Move m = new Move(7, 4, 7, 6, KING, EMPTY);
                    m.isCastle = true; m.rookFrom = new int[]{7,7}; m.rookTo = new int[]{7,5};
                    moves.add(m);
                }
                if (castleRights[1] && grid[7][3]==EMPTY && grid[7][2]==EMPTY && grid[7][1]==EMPTY
                        && !isAttacked(7,4,-1) && !isAttacked(7,3,-1) && !isAttacked(7,2,-1)) {
                    Move m = new Move(7, 4, 7, 2, KING, EMPTY);
                    m.isCastle = true; m.rookFrom = new int[]{7,0}; m.rookTo = new int[]{7,3};
                    moves.add(m);
                }
            } else { // Black
                if (castleRights[2] && grid[0][5]==EMPTY && grid[0][6]==EMPTY
                        && !isAttacked(0,4,1) && !isAttacked(0,5,1) && !isAttacked(0,6,1)) {
                    Move m = new Move(0, 4, 0, 6, -KING, EMPTY);
                    m.isCastle = true; m.rookFrom = new int[]{0,7}; m.rookTo = new int[]{0,5};
                    moves.add(m);
                }
                if (castleRights[3] && grid[0][3]==EMPTY && grid[0][2]==EMPTY && grid[0][1]==EMPTY
                        && !isAttacked(0,4,1) && !isAttacked(0,3,1) && !isAttacked(0,2,1)) {
                    Move m = new Move(0, 4, 0, 2, -KING, EMPTY);
                    m.isCastle = true; m.rookFrom = new int[]{0,0}; m.rookTo = new int[]{0,3};
                    moves.add(m);
                }
            }
        }

        // ── Is square (r,c) attacked by 'attacker' side? ───────────────
        boolean isAttacked(int r, int c, int attackerColor) {
            // Knights
            for (int[] d : new int[][]{{-2,-1},{-2,1},{-1,-2},{-1,2},{1,-2},{1,2},{2,-1},{2,1}}) {
                int nr = r+d[0], nc = c+d[1];
                if (inBounds(nr,nc) && grid[nr][nc] == attackerColor*KNIGHT) return true;
            }
            // Pawns
            int pawnDir = (attackerColor == 1) ? 1 : -1;
            for (int dc : new int[]{-1,1}) {
                int nr = r+pawnDir, nc = c+dc;
                if (inBounds(nr,nc) && grid[nr][nc] == attackerColor*PAWN) return true;
            }
            // Sliding (rook/queen)
            for (int[] d : new int[][]{{1,0},{-1,0},{0,1},{0,-1}}) {
                int nr=r+d[0], nc=c+d[1];
                while (inBounds(nr,nc)) {
                    int p = grid[nr][nc];
                    if (p != EMPTY) {
                        if (p == attackerColor*ROOK || p == attackerColor*QUEEN) return true;
                        break;
                    }
                    nr+=d[0]; nc+=d[1];
                }
            }
            // Sliding (bishop/queen)
            for (int[] d : new int[][]{{1,1},{1,-1},{-1,1},{-1,-1}}) {
                int nr=r+d[0], nc=c+d[1];
                while (inBounds(nr,nc)) {
                    int p = grid[nr][nc];
                    if (p != EMPTY) {
                        if (p == attackerColor*BISHOP || p == attackerColor*QUEEN) return true;
                        break;
                    }
                    nr+=d[0]; nc+=d[1];
                }
            }
            // King
            for (int[] d : new int[][]{{-1,-1},{-1,0},{-1,1},{0,-1},{0,1},{1,-1},{1,0},{1,1}}) {
                int nr=r+d[0], nc=c+d[1];
                if (inBounds(nr,nc) && grid[nr][nc] == attackerColor*KING) return true;
            }
            return false;
        }

        boolean isInCheck(int color) {
            // Find king
            for (int r=0;r<8;r++) for (int c=0;c<8;c++)
                if (grid[r][c] == color*KING) return isAttacked(r, c, -color);
            return false;
        }

        // ── Apply/undo move ─────────────────────────────────────────────
        void apply(Move m) {
            // En passant clear
            if (m.isEnPassant) grid[m.enPassantCapR][m.enPassantCapC] = EMPTY;

            // Castling
            if (m.isCastle) {
                grid[m.rookTo[0]][m.rookTo[1]] = grid[m.rookFrom[0]][m.rookFrom[1]];
                grid[m.rookFrom[0]][m.rookFrom[1]] = EMPTY;
            }

            // Move piece
            grid[m.toR][m.toC] = m.isPromotion ? m.promotionPiece : m.piece;
            grid[m.fromR][m.fromC] = EMPTY;

            // Update castling rights
            if (Math.abs(m.piece) == KING) {
                if (colorOf(m.piece) == 1) { castleRights[0] = false; castleRights[1] = false; }
                else                       { castleRights[2] = false; castleRights[3] = false; }
            }
            if (m.fromR==7 && m.fromC==7) castleRights[0] = false;
            if (m.fromR==7 && m.fromC==0) castleRights[1] = false;
            if (m.fromR==0 && m.fromC==7) castleRights[2] = false;
            if (m.fromR==0 && m.fromC==0) castleRights[3] = false;

            // En passant target
            if (Math.abs(m.piece)==PAWN && Math.abs(m.fromR-m.toR)==2) {
                epRow = (m.fromR+m.toR)/2; epCol = m.fromC;
            } else { epRow=-1; epCol=-1; }
        }

        void undo(Move m, boolean[] savedCastle, int savedEpR, int savedEpC) {
            grid[m.fromR][m.fromC] = m.piece;
            grid[m.toR][m.toC] = m.isPromotion ? EMPTY : m.captured;
            if (!m.isPromotion) grid[m.toR][m.toC] = m.captured;
            else grid[m.toR][m.toC] = EMPTY;

            if (m.isEnPassant) {
                grid[m.toR][m.toC] = EMPTY;
                grid[m.enPassantCapR][m.enPassantCapC] = m.captured;
            }
            if (m.isCastle) {
                grid[m.rookFrom[0]][m.rookFrom[1]] = grid[m.rookTo[0]][m.rookTo[1]];
                grid[m.rookTo[0]][m.rookTo[1]] = EMPTY;
            }
            System.arraycopy(savedCastle, 0, castleRights, 0, 4);
            epRow=savedEpR; epCol=savedEpC;
        }

        // ── Legal moves (filter pseudo-moves that leave own king in check) ──
        List<Move> legalMoves(int r, int c) {
            List<Move> pseudo = pseudoMoves(r, c);
            List<Move> legal  = new ArrayList<>();
            int color = colorOf(grid[r][c]);
            boolean[] savedCastle = Arrays.copyOf(castleRights, 4);
            int savedEpR=epRow, savedEpC=epCol;
            for (Move m : pseudo) {
                apply(m);
                if (!isInCheck(color)) legal.add(m);
                undo(m, Arrays.copyOf(savedCastle, 4), savedEpR, savedEpC);
            }
            return legal;
        }

        List<Move> allLegalMoves(int color) {
            List<Move> all = new ArrayList<>();
            for (int r=0;r<8;r++) for (int c=0;c<8;c++)
                if (colorOf(grid[r][c]) == color) all.addAll(legalMoves(r,c));
            return all;
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    //  Game Panel (GUI + Controller)
    // ═══════════════════════════════════════════════════════════════════
    static class GamePanel extends JPanel implements MouseListener {

        Board board = new Board();
        int turn = 1;  // 1=white, -1=black
        int selR=-1, selC=-1;
        List<Move> legalForSel = new ArrayList<>();
        String status = "White's turn";
        boolean gameOver = false;

        // Unicode pieces
        static final String[][] SYMBOLS = {
            {"♜","♞","♝","♛","♚","♟"},  // black
            {"♖","♘","♗","♕","♔","♙"}   // white
        };

        GamePanel() {
            setPreferredSize(new Dimension(BOARD + 200, BOARD + 60));
            setBackground(C_PANEL_BG);
            addMouseListener(this);
        }

        // ── Paint ─────────────────────────────────────────────────────
        @Override
        protected void paintComponent(Graphics g0) {
            super.paintComponent(g0);
            Graphics2D g = (Graphics2D) g0;
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            drawBoard(g);
            drawHighlights(g);
            drawPieces(g);
            drawLegalDots(g);
            drawSidePanel(g);
            drawStatusBar(g);
        }

        void drawBoard(Graphics2D g) {
            for (int r=0;r<8;r++) for (int c=0;c<8;c++) {
                g.setColor((r+c)%2==0 ? C_LIGHT : C_DARK);
                g.fillRect(c*TILE, r*TILE, TILE, TILE);
            }
            // Coordinates
            g.setFont(new Font("SansSerif", Font.BOLD, 11));
            for (int i=0;i<8;i++) {
                g.setColor((i%2==0)?C_DARK:C_LIGHT);
                g.drawString(String.valueOf((char)('a'+i)), i*TILE+3, BOARD-3);
                g.setColor(((i)%2==0)?C_DARK:C_LIGHT);
                g.drawString(String.valueOf(8-i), 2, i*TILE+13);
            }
        }

        void drawHighlights(Graphics2D g) {
            // Last move
            if (board.lastMove != null) {
                Move m = board.lastMove;
                g.setColor(C_LAST_MOVE);
                g.fillRect(m.fromC*TILE, m.fromR*TILE, TILE, TILE);
                g.fillRect(m.toC*TILE,   m.toR*TILE,   TILE, TILE);
            }
            // Selection
            if (selR >= 0) {
                g.setColor(C_SELECT);
                g.fillRect(selC*TILE, selR*TILE, TILE, TILE);
            }
            // Check highlight
            if (!gameOver && board.isInCheck(turn)) {
                for (int r=0;r<8;r++) for (int c=0;c<8;c++) {
                    if (board.grid[r][c] == turn*KING) {
                        g.setColor(C_CHECK);
                        g.fillRect(c*TILE, r*TILE, TILE, TILE);
                    }
                }
            }
        }

        void drawPieces(Graphics2D g) {
            g.setFont(new Font("Serif", Font.PLAIN, 56));
            FontMetrics fm = g.getFontMetrics();
            for (int r=0;r<8;r++) for (int c=0;c<8;c++) {
                int p = board.grid[r][c];
                if (p == EMPTY) continue;
                String sym = symbol(p);
                int x = c*TILE + (TILE - fm.stringWidth(sym))/2;
                int y = r*TILE + (TILE + fm.getAscent() - fm.getDescent())/2 - 2;
                // Shadow
                g.setColor(new Color(0,0,0,60));
                g.drawString(sym, x+2, y+2);
                // Piece color
                g.setColor(p > 0 ? new Color(0xFFFAF0) : new Color(0x1A1A1A));
                g.drawString(sym, x, y);
            }
        }

        void drawLegalDots(Graphics2D g) {
            for (Move m : legalForSel) {
                int r=m.toR, c=m.toC;
                if (board.grid[r][c] != EMPTY) {
                    // Capture ring
                    g.setColor(C_MOVE_DOT);
                    g.setStroke(new BasicStroke(6));
                    g.drawOval(c*TILE+4, r*TILE+4, TILE-8, TILE-8);
                    g.setStroke(new BasicStroke(1));
                } else {
                    // Move dot
                    g.setColor(C_MOVE_DOT);
                    int d=22;
                    g.fillOval(c*TILE+(TILE-d)/2, r*TILE+(TILE-d)/2, d, d);
                }
            }
        }

        void drawSidePanel(Graphics2D g) {
            int px = BOARD + 10, pw = 180;
            // Panel background
            g.setColor(new Color(0x1E1E1E));
            g.fillRoundRect(BOARD, 0, 200, BOARD+60, 0, 0);

            // Title
            g.setFont(new Font("Serif", Font.BOLD, 22));
            g.setColor(C_ACCENT);
            g.drawString("♟ Chess", px, 35);

            // Turn indicator
            g.setFont(new Font("SansSerif", Font.BOLD, 13));
            g.setColor(C_TEXT);
            g.drawString("Turn:", px, 65);
            String turnStr = turn==1 ? "● White" : "● Black";
            g.setColor(turn==1 ? new Color(0xFFFAF0) : new Color(0xAAAAAA));
            g.setFont(new Font("SansSerif", Font.BOLD, 14));
            g.drawString(turnStr, px, 82);

            // Divider
            g.setColor(new Color(0x444444));
            g.fillRect(px, 92, pw, 1);

            // Captured pieces
            g.setFont(new Font("SansSerif", Font.BOLD, 12));
            g.setColor(C_ACCENT);
            g.drawString("Captured by White:", px, 112);
            drawCaptures(g, board.capturedByWhite, px, 130, 1);

            g.drawString("Captured by Black:", px, 190);
            drawCaptures(g, board.capturedByBlack, px, 208, -1);

            // Divider
            g.setColor(new Color(0x444444));
            g.fillRect(px, 270, pw, 1);

            // New game button
            g.setColor(new Color(0x3A3A3A));
            g.fillRoundRect(px, 280, pw, 36, 10, 10);
            g.setColor(C_ACCENT);
            g.setFont(new Font("SansSerif", Font.BOLD, 13));
            g.drawString("New Game", px+45, 303);
            g.setColor(new Color(0x555555));
            g.drawRoundRect(px, 280, pw, 36, 10, 10);
        }

        void drawCaptures(Graphics2D g, List<Integer> caps, int px, int py, int side) {
            g.setFont(new Font("Serif", Font.PLAIN, 20));
            int x=px, y=py, count=0;
            for (int p : caps) {
                g.setColor(p>0 ? new Color(0xFFFAF0) : new Color(0x222222));
                // show as shadow
                g.setColor(new Color(0,0,0,80));
                g.drawString(symbol(p), x+1, y+1);
                g.setColor(p>0 ? new Color(0xFFFAF0) : new Color(0xAAAAAA));
                g.drawString(symbol(p), x, y);
                x += 22;
                count++;
                if (count % 8 == 0) { x=px; y+=24; }
            }
        }

        void drawStatusBar(Graphics2D g) {
            g.setColor(new Color(0x141414));
            g.fillRect(0, BOARD, BOARD+200, 60);
            
            // Status text
            g.setFont(new Font("SansSerif", Font.BOLD, 16));
            g.setColor(C_ACCENT);
            FontMetrics fm = g.getFontMetrics();
            g.drawString(status, (BOARD - fm.stringWidth(status))/2, BOARD + 36);
            
            // Footer text
            g.setFont(new Font("SansSerif", Font.ITALIC, 12));
            g.setColor(new Color(0x888888));
            String footer = "Made by Priya";
            g.drawString(footer, BOARD + 200 - fm.stringWidth(footer) - 10, BOARD + 50);
        }

        // ── Mouse ──────────────────────────────────────────────────────
        @Override public void mousePressed(MouseEvent e) {
            int mx = e.getX(), my = e.getY();

            // New game button
            if (mx >= BOARD+10 && mx <= BOARD+190 && my>=280 && my<=316) {
                resetGame(); return;
            }

            if (gameOver || mx >= BOARD || my >= BOARD) return;
            int c = mx/TILE, r = my/TILE;

            if (selR >= 0) {
                // Try to make a move
                Move chosen = null;
                for (Move m : legalForSel) {
                    if (m.toR==r && m.toC==c) {
                        // Prefer promotion to queen by default (auto-queen)
                        if (m.isPromotion && Math.abs(m.promotionPiece) != QUEEN) continue;
                        chosen = m; break;
                    }
                    // fallback non-promotion move
                    if (m.toR==r && m.toC==c && chosen==null) chosen=m;
                }
                if (chosen != null) {
                    executeMove(chosen);
                    return;
                }
                selR=-1; selC=-1; legalForSel.clear();
            }

            // Select piece
            if (board.colorOf(board.grid[r][c]) == turn) {
                selR=r; selC=c;
                legalForSel = board.legalMoves(r, c);
            }
            repaint();
        }

        void executeMove(Move m) {
            boolean[] savedCastle = Arrays.copyOf(board.castleRights, 4);
            // Track captures
            if (m.captured != EMPTY) {
                if (turn == 1) board.capturedByWhite.add(m.captured);
                else           board.capturedByBlack.add(m.captured);
            }
            if (m.isEnPassant) {
                if (turn == 1) board.capturedByWhite.add(m.captured);
                else           board.capturedByBlack.add(m.captured);
            }
            board.apply(m);
            board.lastMove = m;
            turn = -turn;
            selR=-1; selC=-1; legalForSel.clear();

            // Check game state
            List<Move> next = board.allLegalMoves(turn);
            if (next.isEmpty()) {
                if (board.isInCheck(turn)) {
                    String winner = (turn==-1)?"White":"Black";
                    status = "Checkmate! " + winner + " wins! 🏆";
                } else {
                    status = "Stalemate! Draw.";
                }
                gameOver = true;
            } else if (board.isInCheck(turn)) {
                status = (turn==1?"White":"Black") + " is in Check!";
            } else {
                status = (turn==1?"White":"Black") + "'s turn";
            }
            repaint();
        }

        void resetGame() {
            board = new Board();
            turn = 1;
            selR=-1; selC=-1;
            legalForSel.clear();
            status = "White's turn";
            gameOver = false;
            repaint();
        }

        // ── Helpers ───────────────────────────────────────────────────
        String symbol(int p) {
            int idx = Math.abs(p) - 1;
            return p > 0 ? SYMBOLS[1][idx] : SYMBOLS[0][idx];
        }

        @Override public void mouseClicked(MouseEvent e) {}
        @Override public void mouseReleased(MouseEvent e) {}
        @Override public void mouseEntered(MouseEvent e) {}
        @Override public void mouseExited(MouseEvent e) {}
    }
}
