import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.JPanel;

public class Draw extends JPanel implements KeyListener {

    private static final int WIN_SCORE = 5;
    private static final long TIME_LIMIT_MS = 10L * 60 * 1000; // 10 minutes

    private final boolean timed;      // false = Normal (first to 5), true = Time (10 min)
    private final Runnable onQuit;    // called when the player quits to the main menu

    private volatile boolean running = true;
    private volatile boolean paused = false;
    private volatile int menuIndex = 0; // 0 = Resume, 1 = Quit

    private long remainingMs = TIME_LIMIT_MS;
    private long lastNanos = System.nanoTime();

    private final Rectangle resumeRect = new Rectangle();
    private final Rectangle quitRect = new Rectangle();

    private Set<Integer> keysPressed = java.util.concurrent.ConcurrentHashMap.newKeySet();
    public int playerSpeed = 7;

    private int player1Y = 0;
    private int player2Y = 0;
    private static final int PlayerB = 20;

    private int ballX = 0;
    private int ballY = 0;
    private int ballSpeedX = 10;
    private int ballSpeedY = 10;
    private static final int ballSize = 20;

    private int player1Score = 0;
    private int player2Score = 0;

    private boolean gameOver = false;
    private String winner = "";

    public Draw(boolean timed, Runnable onQuit) {
        this.timed = timed;
        this.onQuit = onQuit;
        setFocusable(true);
        addKeyListener(this);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                requestFocusInWindow();
                if (!paused) return;
                if (resumeRect.contains(e.getPoint())) chooseOption(0);
                else if (quitRect.contains(e.getPoint())) chooseOption(1);
            }
        });
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                if (!paused) return;
                if (resumeRect.contains(e.getPoint())) menuIndex = 0;
                else if (quitRect.contains(e.getPoint())) menuIndex = 1;
            }
        });
    }

    public boolean isRunning() {
        return running;
    }

    private void chooseOption(int index) {
        if (index == 0) {
            paused = false;
        } else {
            quit();
        }
    }

    private void quit() {
        running = false;
        if (onQuit != null) onQuit.run();
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int k = e.getKeyCode();

        if (k == KeyEvent.VK_ESCAPE) {
            if (gameOver) {
                quit();
            } else {
                paused = !paused;
                menuIndex = 0;
            }
            return;
        }

        if (paused) {
            if (k == KeyEvent.VK_UP || k == KeyEvent.VK_W) menuIndex = 0;
            else if (k == KeyEvent.VK_DOWN || k == KeyEvent.VK_S) menuIndex = 1;
            else if (k == KeyEvent.VK_ENTER || k == KeyEvent.VK_SPACE) chooseOption(menuIndex);
            return;
        }

        keysPressed.add(k);
        if (gameOver && k == KeyEvent.VK_ENTER) {
            restart();
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        keysPressed.remove(e.getKeyCode());
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    private void restart() {
        player1Score = 0;
        player2Score = 0;
        winner = "";
        remainingMs = TIME_LIMIT_MS;
        lastNanos = System.nanoTime();
        resetBall(getWidth(), getHeight());
        gameOver = false;
    }

    private void endGame(String w) {
        gameOver = true;
        winner = w;
        final int s1 = player1Score, s2 = player2Score;
        // save on a separate thread so the game never freezes
        new Thread(() -> ApiClient.saveMatch(s1, s2, w)).start();
    }

    public void update() {
        long now = System.nanoTime();
        long delta = (now - lastNanos) / 1_000_000;
        lastNanos = now;

        if (gameOver || paused) return;

        if (timed) {
            remainingMs -= delta;
            if (remainingMs <= 0) {
                remainingMs = 0;
                if (player1Score > player2Score) endGame("Player 1");
                else if (player2Score > player1Score) endGame("Player 2");
                else endGame("Draw");
                return;
            }
        }

        int Width = getWidth();
        int Height = getHeight();
        int PlayerH = Height / 6;

        if (keysPressed.contains(KeyEvent.VK_W)) player1Y -= playerSpeed;
        if (keysPressed.contains(KeyEvent.VK_S)) player1Y += playerSpeed;
        if (keysPressed.contains(KeyEvent.VK_UP)) player2Y -= playerSpeed;
        if (keysPressed.contains(KeyEvent.VK_DOWN)) player2Y += playerSpeed;

        if (player1Y < 0) player1Y = 0;
        if (player1Y + PlayerH > Height) player1Y = Height - PlayerH;
        if (player2Y < 0) player2Y = 0;
        if (player2Y + PlayerH > Height) player2Y = Height - PlayerH;

        ballX += ballSpeedX;
        ballY += ballSpeedY;

        if (ballY <= 0 || ballY + ballSize >= Height) {
            ballSpeedY = -ballSpeedY;
        }

        if (ballX <= PlayerB &&
            ballY >= player1Y &&
            ballY <= player1Y + PlayerH) {
            ballSpeedX = -ballSpeedX;
            ballX = PlayerB;
        }

        if (ballX + ballSize >= Width - PlayerB &&
            ballY >= player2Y &&
            ballY <= player2Y + PlayerH) {
            ballSpeedX = -ballSpeedX;
            ballX = Width - PlayerB - ballSize;
        }

        if (ballX < 0) {
            player2Score++;
            if (!timed && player2Score >= WIN_SCORE) endGame("Player 2");
            else resetBall(Width, Height);
        } else if (ballX > Width) {
            player1Score++;
            if (!timed && player1Score >= WIN_SCORE) endGame("Player 1");
            else resetBall(Width, Height);
        }
    }

    private void resetBall(int Width, int Height) {
        ballX = Width / 2 - ballSize / 2;
        ballY = Height / 2 - ballSize / 2;
        ballSpeedX = 10;
        ballSpeedY = 10;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int Width = getWidth();
        int Height = getHeight();
        int PlayerH = Height / 6;

        // paddles (black)
        g2d.setColor(Color.black);
        g2d.fillRect(0, player1Y, PlayerB, PlayerH);
        g2d.fillRect(Width - PlayerB, player2Y, PlayerB, PlayerH);

        // scores
        g2d.setFont(new Font("Arial", Font.BOLD, 40));
        g2d.drawString(String.valueOf(player1Score), (Width / 2) - 40, 40);
        g2d.drawString("-", (Width / 2) - 6, 40);
        g2d.drawString(String.valueOf(player2Score), (Width / 2) + 24, 40);

        g2d.setFont(new Font("Arial", Font.BOLD, 16));
        g2d.drawString("Player 1", 40, 30);
        g2d.drawString("Player 2", Width - 110, 30);

        // mode info: countdown (time mode) or target (normal mode)
        g2d.setFont(new Font("Arial", Font.BOLD, 22));
        String info;
        if (timed) {
            long secs = remainingMs / 1000;
            info = String.format("%02d:%02d", secs / 60, secs % 60);
        } else {
            info = "First to " + WIN_SCORE;
        }
        int iw = g2d.getFontMetrics().stringWidth(info);
        g2d.drawString(info, (Width - iw) / 2, 75);

        // ball (black)
        g2d.setColor(Color.black);
        g2d.fillOval(ballX, ballY, ballSize, ballSize);

        if (gameOver) {
            g2d.setColor(Color.black);
            g2d.setFont(new Font("Arial", Font.BOLD, 60));
            String msg = winner.equals("Draw") ? "It's a Draw!" : winner + " Wins!";
            int w = g2d.getFontMetrics().stringWidth(msg);
            g2d.drawString(msg, (Width - w) / 2, Height / 2);
            g2d.setFont(new Font("Arial", Font.PLAIN, 22));
            String sub = "Press ENTER to play again  |  ESC for main menu";
            int w2 = g2d.getFontMetrics().stringWidth(sub);
            g2d.drawString(sub, (Width - w2) / 2, Height / 2 + 40);
        }

        if (paused) {
            paintPauseMenu(g2d, Width, Height);
        }
    }

    private void paintPauseMenu(Graphics2D g2d, int Width, int Height) {
        // dim the game
        g2d.setColor(new Color(0, 0, 0, 150));
        g2d.fillRect(0, 0, Width, Height);

        int boxW = 360, boxH = 300;
        int boxX = (Width - boxW) / 2, boxY = (Height - boxH) / 2;
        g2d.setColor(Color.darkGray);
        g2d.fillRoundRect(boxX, boxY, boxW, boxH, 20, 20);

        g2d.setColor(Color.white);
        g2d.setFont(new Font("Arial", Font.BOLD, 36));
        String title = "PAUSED";
        int tw = g2d.getFontMetrics().stringWidth(title);
        g2d.drawString(title, (Width - tw) / 2, boxY + 65);

        int optW = 260, optH = 50;
        int optX = (Width - optW) / 2;
        resumeRect.setBounds(optX, boxY + 100, optW, optH);
        quitRect.setBounds(optX, boxY + 180, optW, optH);

        drawOption(g2d, resumeRect, "Resume", menuIndex == 0);
        drawOption(g2d, quitRect, "Quit", menuIndex == 1);
    }

    private void drawOption(Graphics2D g2d, Rectangle r, String text, boolean selected) {
        g2d.setFont(new Font("Arial", Font.BOLD, 24));
        if (selected) {
            g2d.setColor(Color.white);
            g2d.fillRoundRect(r.x, r.y, r.width, r.height, 12, 12);
            g2d.setColor(Color.black);
        } else {
            g2d.setColor(Color.white);
            g2d.setStroke(new BasicStroke(2));
            g2d.drawRoundRect(r.x, r.y, r.width, r.height, 12, 12);
        }
        FontMetrics fm = g2d.getFontMetrics();
        int tx = r.x + (r.width - fm.stringWidth(text)) / 2;
        int ty = r.y + (r.height + fm.getAscent() - fm.getDescent()) / 2;
        g2d.drawString(text, tx, ty);
    }
}
