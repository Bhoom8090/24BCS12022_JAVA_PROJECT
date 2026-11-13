import java.awt.*;
import javax.swing.*;

public class Main {

    private static JButton styledButton(String text) {
        JButton b = new JButton(text);
        b.setBackground(Color.BLACK);
        b.setForeground(Color.WHITE);
        b.setOpaque(true);
        return b;
    }

    public void MainScreen(){
        JFrame frame = new JFrame("Start");

        frame.setSize(800, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);

        JPanel panel = new JPanel();
        panel.setBackground(Color.lightGray);
        panel.setLayout(null);

        JButton start = styledButton("Start");
        panel.add(start);

        JButton Quit = styledButton("Exit");
        panel.add(Quit);

        JButton history = styledButton("History");
        panel.add(history);

        JLabel Name = new JLabel("PONG");
        Name.setFont(new Font("Math Sans",Font.BOLD,50));
        panel.add(Name);

        panel.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                int Width = panel.getWidth();
                int Height = panel.getHeight();
                start.setBounds(Width/2 - 100, Height/2 - 10, 200, 30);

                history.setBounds(Width/2 - 100, Height/2 + 50, 200, 30);

                Quit.setBounds(Width/2 - 100, Height/2 + 110, 200, 30);

                Name.setBounds(Width/2-75 , 200, 200, 50);
            }
        });

        start.addActionListener(e -> showModeMenu(frame, panel));

        history.addActionListener(e ->{
            JTextArea area = new JTextArea(ApiClient.getHistory());
            area.setEditable(false);
            JOptionPane.showMessageDialog(frame, new JScrollPane(area), "Match History", JOptionPane.PLAIN_MESSAGE);
        });

        Quit.addActionListener(e ->{
            System.exit(0);
        });

        frame.setContentPane(panel);
        frame.setVisible(true);
    }

    // Second menu: choose Normal (first to 5) or Time (10 minutes)
    private void showModeMenu(JFrame frame, JPanel mainPanel) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(Color.lightGray);

        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0;
        gc.gridy = GridBagConstraints.RELATIVE;
        gc.insets = new Insets(10, 0, 10, 0);

        JLabel title = new JLabel("SELECT MODE");
        title.setFont(new Font("Math Sans", Font.BOLD, 40));
        p.add(title, gc);

        JButton normal = styledButton("Normal  (5 point match)");
        JButton time = styledButton("Time  (10 min match)");
        JButton back = styledButton("Back");
        Dimension size = new Dimension(280, 40);
        normal.setPreferredSize(size);
        time.setPreferredSize(size);
        back.setPreferredSize(size);
        p.add(normal, gc);
        p.add(time, gc);
        p.add(back, gc);

        normal.addActionListener(e -> {
            new Main().MainGame(false);
            frame.dispose();
        });
        time.addActionListener(e -> {
            new Main().MainGame(true);
            frame.dispose();
        });
        back.addActionListener(e -> {
            frame.setContentPane(mainPanel);
            frame.revalidate();
            frame.repaint();
        });

        frame.setContentPane(p);
        frame.revalidate();
        frame.repaint();
    }

    public void MainGame(boolean timed){
        JFrame frame = new JFrame("Pong Game");
        // Quit from the pause menu goes back to the main menu
        Draw draw = new Draw(timed, () -> SwingUtilities.invokeLater(() -> {
            frame.dispose();
            new Main().MainScreen();
        }));

        draw.setBackground(Color.lightGray);
        frame.add(draw);
        frame.setSize(800, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setVisible(true);
        draw.requestFocusInWindow();

        startGameLoop(draw);
    }

    public static void main(String[] args) {
        Main m = new Main();
        m.MainScreen();
    }

    private static void startGameLoop(Draw draw) {
        Thread gameThread = new Thread(() -> {
            int fps = 60;
            int targetFrameTime = 1000 / fps;

            while (draw.isRunning()) {
                long frameStartTime = System.currentTimeMillis();

                draw.update();
                draw.repaint();

                long elapsedTime = System.currentTimeMillis() - frameStartTime;
                long sleepTime = targetFrameTime - elapsedTime;

                if (sleepTime > 0) {
                    try {
                        Thread.sleep(sleepTime);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        gameThread.setDaemon(true);
        gameThread.start();
    }
}
