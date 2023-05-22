import javax.swing.JPanel;
import javax.imageio.ImageIO;
import javax.sound.sampled.*;
import java.util.*;
import java.util.Timer;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.BufferedInputStream;
import java.io.InputStream;

public class Game extends JPanel {
    private enum PLAYER {
        GREEN, RED;
    }

    private Timer tid;
    private Snake snake;
    private Snake snake2;
    private Point mouse;
    private int points1 = 0;
    private int points2 = 0;
    private BufferedImage image;
    private GameStatus status;
    private boolean musLaddad = true;
    private PLAYER winner;
    private AudioInputStream audioInputStream;
    private Clip chomp;
    private boolean aiXGood;
    private boolean useAi;


    private static Font FONT_M = new Font("ArcadeClassic", Font.PLAIN, 24);
    private static Font FONT_M_ITALIC = new Font("ArcadeClassic", Font.ITALIC, 24);
    private static Font FONT_L = new Font("ArcadeClassic", Font.PLAIN, 84);
    private static Font FONT_XL = new Font("ArcadeClassic", Font.PLAIN, 110);
    private static int WIDTH = 760;
    private static int HEIGHT = 520;
    private static int DELAY = 50;

    public Game() {
        try {
            image = ImageIO.read(getClass().getResourceAsStream("res/mouse.png"));
            InputStream in = getClass().getResourceAsStream("res/chomp.wav");
            InputStream bufferedIn = new BufferedInputStream(in);
            audioInputStream = AudioSystem.getAudioInputStream(bufferedIn);
            chomp = AudioSystem.getClip();
            chomp.open(audioInputStream);
            audioInputStream.close();
        } catch (Exception e) {
            musLaddad = false;
        }

        addKeyListener(new KeyListener());
        setFocusable(true);
        setBackground(Color.black);
        setDoubleBuffered(true);

        snake = new Snake(WIDTH - 50, HEIGHT / 2, Direction.LEFT);
        snake2 = new Snake(50, HEIGHT / 2, Direction.RIGHT);
        status = GameStatus.NOT_STARTED;
        repaint();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        render(g);

        Toolkit.getDefaultToolkit().sync();
    }

    private void setStatus(GameStatus newStatus) {
        switch (newStatus) {
            case RUNNING:
                tid = new Timer();
                tid.schedule(new GameLoop(), 0, DELAY);
                break;
            case PAUSED:
                tid.cancel();
            case GAME_OVER:
                tid.cancel();
                break;
        }

        status = newStatus;
    }

    private void update() {
        snake.move();
        snake2.move();

        if (mouse != null && useAi) {
            Point aiHead = snake.getHead();
            int x = aiHead.getX() - mouse.getX();
            int y = aiHead.getY() - mouse.getY();

            if (Math.abs(x) <= 10) {
                aiXGood = true;
            } else {
                aiXGood = false;
            }

            if (aiXGood) {
                if (y > 0) {
                    snake.turn(Direction.UP);
                } else {
                    snake.turn(Direction.DOWN);
                }
            } else {
                if (x > 0) {
                    snake.turn(Direction.LEFT);
                } else {
                    snake.turn(Direction.RIGHT);
                }
            }

            if (aiHead.getX() < 35 && snake.direction == Direction.LEFT) {
                snake.turn(Direction.UP);
            }
            if (aiHead.getY() < 55 && snake.direction == Direction.UP) {
                snake.turn(Direction.RIGHT);
            }
            if (aiHead.getY() > HEIGHT && snake.direction == Direction.DOWN) {
                snake.turn(Direction.LEFT);
            }
            if (aiHead.getX() > WIDTH - 5 && snake.direction == Direction.RIGHT) {
                snake.turn(Direction.DOWN);
            }

            for (Point t : snake2.getTail()) {
                if (aiHead.intersects(t, 30)) {
                    Point ahead = new Point(aiHead.getX(), aiHead.getY());
                    ahead.move(snake.direction, 10);
                    if (ahead.equals(t)) break;
                    ahead.move(snake.direction, 10);
                    if (ahead.equals(t)) break;
                    if (snake.direction == Direction.LEFT) {
                        snake.turn(Direction.UP);
                        break;
                    } else if (snake.direction == Direction.RIGHT) {
                        snake.turn(Direction.DOWN);
                        break;
                    } else if (snake.direction == Direction.UP) {
                        snake.turn(Direction.RIGHT);
                        break;
                    } else if (snake.direction == Direction.DOWN) {
                        snake.turn(Direction.LEFT);
                        break;
                    }
                }
            }
        }

        if (mouse != null && snake.getHead().intersects(mouse, 30)) {
            snake.addTail();
            mouse = null;
            points1++;
            chomp.setFramePosition(0);
            chomp.start();
        } else if (mouse != null && snake2.getHead().intersects(mouse, 30)) {
            snake2.addTail();
            mouse = null;
            points2++;
            chomp.setFramePosition(0);
            chomp.start();
        }

        if (mouse == null) {
            skapaMus();
        }

        checkForGameOver();
    }

    private void reset() {
        points1 = 0;
        points2 = 0;
        mouse = null;
        snake = new Snake(WIDTH - 50, HEIGHT / 2, Direction.LEFT);
        snake2 = new Snake(50, HEIGHT / 2, Direction.RIGHT);
        setStatus(GameStatus.RUNNING);
    }

    private void togglePause() {
        setStatus(status == GameStatus.PAUSED ? GameStatus.RUNNING : GameStatus.PAUSED);
    }

    private void checkForGameOver() {
        // snake green
        Point head = snake.getHead();
        Point head2 = snake2.getHead();
        boolean hitBoundary = head.getX() <= 20
                || head.getX() >= WIDTH + 10
                || head.getY() <= 40
                || head.getY() >= HEIGHT + 30;

        boolean ateItself = false;
        for (Point t : snake.getTail()) {
            ateItself = ateItself || head.equals(t);
            if (head2.intersects(t)) {
                winner = PLAYER.GREEN;
                setStatus(GameStatus.GAME_OVER);
                return;
            }
        }
        if (hitBoundary || ateItself) {
            setStatus(GameStatus.GAME_OVER);
            winner = PLAYER.RED;
            return;
        }

        // snake red
        hitBoundary = head2.getX() <= 20
                || head2.getX() >= WIDTH + 10
                || head2.getY() <= 40
                || head2.getY() >= HEIGHT + 30;

        ateItself = false;
        for (Point t : snake2.getTail()) {
            ateItself = ateItself || head2.equals(t);
            if (head.intersects(t)) {
                winner = PLAYER.RED;
                setStatus(GameStatus.GAME_OVER);
                return;
            }
        }
        if (hitBoundary || ateItself) {
            setStatus(GameStatus.GAME_OVER);
            winner = PLAYER.GREEN;
        }
    }

    public void drawCenteredString(Graphics g, String text, Font font, int y) {
        FontMetrics metrics = g.getFontMetrics(font);
        int x = (WIDTH - metrics.stringWidth(text)) / 2;

        g.setFont(font);
        g.drawString(text, x + 20, y);
    }

    public void skapaMus() {
        mouse = new Point((new Random()).nextInt(WIDTH - 60) + 20,
                (new Random()).nextInt(HEIGHT - 60) + 40);
    }

    private void render(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;

        g2d.setColor(new Color(53, 220, 8));
        g2d.setFont(FONT_M);

        if (status == GameStatus.NOT_STARTED) {
            drawCenteredString(g2d, "LOKES", FONT_L, 100);
            drawCenteredString(g2d, "ANAKONDAKRIG", FONT_L, 170);
            drawCenteredString(g2d, "1 eller 2 SPELARE!", FONT_M, 225);
            drawCenteredString(g2d, "Röd använder W,A,S,D", FONT_M, 250);
            drawCenteredString(g2d, "Grön använder piltangenterna", FONT_M, 270);
            drawCenteredString(g2d, "För en spelare tryck [1], för två tryck [2]", FONT_M_ITALIC, 500);
            return;
        }

        g2d.setColor(new Color(30, 255, 30));
        g2d.drawString(String.format("%04d", points1), 730, 30);
        g2d.setColor(new Color(255, 30, 30));
        g2d.drawString(String.format("%04d", points2), 20, 30);

        if (mouse != null) {
            if (musLaddad) {
                g2d.drawImage(image, mouse.getX(), mouse.getY(), 60, 60, null);
            } else {
                g2d.setColor(Color.GRAY);
                g2d.fillOval(mouse.getX(), mouse.getY(), 10, 10);
                g2d.setColor(new Color(53, 220, 8));
            }
        }

        if (status == GameStatus.GAME_OVER) {
            if (winner == PLAYER.GREEN) {
                g2d.setColor(new Color(30, 255, 30));
                drawCenteredString(g2d, "GRÖN VANN", FONT_XL, 200);
            } else {
                g2d.setColor(new Color(255, 30, 30));
                drawCenteredString(g2d, "RÖD VANN", FONT_XL, 200);
            }
            drawCenteredString(g2d, "Tryck [R] för att starta om", FONT_M_ITALIC, 330);
        }

        // snake 1
        Point p = snake.getHead();
        g2d.setColor(new Color(30, 255, 30));
        g2d.fillRect(p.getX(), p.getY(), 10, 10);

        for (int i = 0, size = snake.getTail().size(); i < size; i++) {
            Point t = snake.getTail().get(i);
            g2d.setColor(new Color(100, 200, 100));
            g2d.fillRect(t.getX(), t.getY(), 10, 10);
        }

        // snake 2
        p = snake2.getHead();
        g2d.setColor(new Color(255, 30, 30));
        g2d.fillRect(p.getX(), p.getY(), 10, 10);

        for (int i = 0, size = snake2.getTail().size(); i < size; i++) {
            Point t = snake2.getTail().get(i);
            g2d.setColor(new Color(200, 100, 100));
            g2d.fillRect(t.getX(), t.getY(), 10, 10);
        }

        g2d.setColor(new Color(71, 0, 128));
        g2d.setStroke(new BasicStroke(4));
        g2d.drawRect(20, 40, WIDTH, HEIGHT);
    }

    private class KeyListener extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            int key = e.getKeyCode();

            if (status == GameStatus.RUNNING) {
                switch (key) {
                    // snake 1
                    case KeyEvent.VK_DOWN:
                        if (!useAi) snake.turn(Direction.DOWN);
                        break;
                    case KeyEvent.VK_LEFT:
                        if (!useAi) snake.turn(Direction.LEFT);
                        break;
                    case KeyEvent.VK_UP:
                        if (!useAi) snake.turn(Direction.UP);
                        break;
                    case KeyEvent.VK_RIGHT:
                        if (!useAi) snake.turn(Direction.RIGHT);
                        break;

                    // snake 2
                    case KeyEvent.VK_S:
                        snake2.turn(Direction.DOWN);
                        break;
                    case KeyEvent.VK_A:
                        snake2.turn(Direction.LEFT);
                        break;
                    case KeyEvent.VK_W:
                        snake2.turn(Direction.UP);
                        break;
                    case KeyEvent.VK_D:
                        snake2.turn(Direction.RIGHT);
                        break;
                }
            }

            if (status == GameStatus.NOT_STARTED) {
                if (key == KeyEvent.VK_1) {
                    useAi = true;
                    setStatus(GameStatus.RUNNING);
                } else if (key == KeyEvent.VK_2) {
                    useAi = false;
                    setStatus(GameStatus.RUNNING);
                }
            }

            if (key == KeyEvent.VK_P) {
                togglePause();
            }

            if (status == GameStatus.GAME_OVER && key == KeyEvent.VK_R) {
                reset();
            }
        }
    }

    private class GameLoop extends java.util.TimerTask {
        public void run() {
            update();
            repaint();
        }
    }
}
