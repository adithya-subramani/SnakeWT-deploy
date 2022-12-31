/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ss.snake;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.WindowConstants;

/**
 *
 * @author adithya
 */
class Snake implements Runnable {
    public static final int SCALE = 10;
    public static final int BOUND = 52;
    public static final int[] levelsArray = {
            50, 200, 400, 600, 800, 1000, 1200
    };
    private static Random random;

    private ArrayList<Point> snakeParts;
    private SnakePanel snakePanel;
    private boolean statusPlay;
    private int snakeLength;
    private Point head;
    private Point apple;
    private int score;
    private int speed;
    private long timeStart;
    private long timeStop;
    private int level;

    public Snake(SnakePanel snakePanel) {
        timeStart = Instant.now().toEpochMilli() / 1000;

        snakeParts = new ArrayList<>();
        snakeLength = 10;
        speed = 50;
        score = 0;
        level = 0;

        head = new Point(SCALE, SCALE);
        this.snakePanel = snakePanel;
        random = new Random();
        statusPlay = true;

        apple = new Point(getRandomCoordinate(BOUND), getRandomCoordinate(BOUND));
    }

    public void move(int direction) {
        timeStop = Instant.now().toEpochMilli() / 1000;
        int x = (int) head.getX();
        int y = (int) head.getY();
        int prevX = x;
        int prevY = y;

        if (head.equals(apple)) {
            apple.setLocation(getRandomCoordinate(BOUND), getRandomCoordinate(BOUND));
            snakeLength++;
            score += 15;
            if (score >= levelsArray[level]) {
                level++;
                speed -= 5;
            }
        }

        if (direction == SnakeFrame.MOVE_FORWARD) {
            checkStatus(x, y);
            y -= SCALE;
            head.setLocation(x, y);
        }
        if (direction == SnakeFrame.MOVE_LEFT) {
            checkStatus(x, y);
            x -= SCALE;
            head.setLocation(x, y);
        }
        if (direction == SnakeFrame.MOVE_RIGHT) {
            checkStatus(x, y);
            x += SCALE;
            head.setLocation(x, y);
        }
        if (direction == SnakeFrame.MOVE_BACK) {
            checkStatus(x, y);
            y += SCALE;
            head.setLocation(x, y);
        }
        snakeParts.add(new Point(prevX, prevY));
        checkCannibal();

        if (snakeParts.size() > snakeLength)
            snakeParts.remove(snakeParts.size() - (snakeLength + 1));
    }

    @Override
    public void run() {
        while (statusPlay) {
            int direction = SnakeFrame.getDirection();
            move(direction);
            snakePanel.repaint();
            try {
                Thread.sleep(speed);
            } catch (InterruptedException e) {
                System.out.println("Interrupted Exception");
            }
        }
    }

    public void checkStatus(int x, int y) {
        //I have no idea, why this Panel is smaller than 600 x 600 (on my computer 594 x 571)
        if (x <= -2 || x >= 592 - SCALE) {
            statusPlay = false;
        }
        if (y <= -2 || y >= 572 - SCALE) {
            statusPlay = false;
        }
    }

    //If u want, u can put getRandomX() and getRandomY() to common "getRandom(int bound) + min"
    public static int getRandomCoordinate(int bound) {
        int a = random.nextInt(bound) + 1;
        int b = 10;
        return a * b;
    }

    private void checkCannibal() {
        snakeParts.stream().filter(head::equals).forEach(parts -> statusPlay = false);
    }

    public String getScoreString() {
        StringBuilder builder = new StringBuilder();
        builder.append("SCORE: ").append(score).append("   ");
        return builder.toString();
    }

    public int getHeadX() {
        return (int) head.getX();
    }

    public int getHeadY() {
        return (int) head.getY();
    }

    public boolean isStatusPlay() {
        return statusPlay;
    }

    public ArrayList<Point> getSnakeParts() {
        return snakeParts;
    }

    public Point getApple() {
        return apple;
    }

}

class SnakeFrame extends JFrame implements KeyListener {
    public static final int SIZE_X = 600;
    public static final int SIZE_Y = 600;

    public static final int MOVE_FORWARD = 0;
    public static final int MOVE_LEFT = 1;
    public static final int MOVE_RIGHT = 3;
    public static final int MOVE_BACK = 4;

    private static SnakePanel snakePanel;
    private static Snake snakeRunnable;
    private static int direction;

    public SnakeFrame() {
        setSize(SIZE_X, SIZE_Y);
        addKeyListener(this);
        startGame();
    }

    public void startGame() {
        direction = MOVE_BACK;
        snakePanel = new SnakePanel();
        snakePanel.setBackground(Color.BLACK);
        snakeRunnable = new Snake(snakePanel);
        snakePanel.setSnake();
        snakePanel.setSnakeParts();
        snakePanel.setApple();
        add(snakePanel);

        Thread thread = new Thread((Runnable) snakeRunnable);
        thread.start();
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_UP && direction != MOVE_BACK)
            direction = MOVE_FORWARD;
        if (e.getKeyCode() == KeyEvent.VK_LEFT && direction != MOVE_RIGHT)
            direction = MOVE_LEFT;
        if (e.getKeyCode() == KeyEvent.VK_DOWN && direction != MOVE_FORWARD)
            direction = MOVE_BACK;
        if (e.getKeyCode() == KeyEvent.VK_RIGHT && direction != MOVE_LEFT)
            direction = MOVE_RIGHT;
        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            if (!snakeRunnable.isStatusPlay()) {
                remove(snakePanel);
                startGame();
                snakePanel.revalidate();
            }
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyReleased(KeyEvent e) {

    }

    public static int getDirection() {
        return direction;
    }

    public static Snake getSnakeRunnable() {
        return snakeRunnable;
    }
}
class SnakePanel extends JPanel {
    private ArrayList<Point> snakeParts;
    private Snake snake;
    private Point apple;

    public SnakePanel() {
        snake = SnakeFrame.getSnakeRunnable();
    }

    @Override
    public void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D graphics2D = (Graphics2D) graphics;

        graphics2D.setColor(Color.GREEN);
        graphics2D.fillOval(snake.getHeadX(), snake.getHeadY(), Snake.SCALE, Snake.SCALE);

        if (snakeParts != null)
            for (Point part : snakeParts)
                graphics2D.drawOval(part.x, part.y, Snake.SCALE, Snake.SCALE);

        graphics2D.setColor(Color.RED);
        graphics2D.fillOval(apple.x, apple.y, Snake.SCALE, Snake.SCALE);

        graphics2D.setColor(Color.WHITE);
        graphics2D.setFont(new Font("Roboto", Font.PLAIN, 18));
        graphics2D.drawString(snake.getScoreString(), 250, 20);

        if (!snake.isStatusPlay())
            graphics2D.drawString("GAME OVER - PRESS SPACE", 5, 40);
    }

    public void setSnake() {
        snake = SnakeFrame.getSnakeRunnable();
    }

    public void setSnakeParts() {
        snakeParts = snake.getSnakeParts();
    }

    public void setApple() {
        apple = snake.getApple();
    }
}

public class FullGame {
        public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
        }

        EventQueue.invokeLater(() -> {
            JFrame frame = new SnakeFrame();
            frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            frame.setTitle("SnakeWT");
            frame.setResizable(false);
            frame.setVisible(true);
        });
    }
}
