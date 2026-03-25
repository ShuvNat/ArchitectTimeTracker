package tracker;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

/**
 * Таймер учета рабочего времени для архитектурного бюро
 * @author Дмитрий Ковыженко
 */

public class ArchitectTimeTracker extends JFrame {
 
    private static ArchitectTimeTracker app_instance;
    private static long last_frame_time;
    private static long work_start_time = 0;
    private static long work_seconds = 0;
    private static boolean is_working = false;
    private static boolean is_finished = false;
    private static boolean show_stamp = false;
    
    // Изображения
    private static Image logo;
    private static Image blueprint;
    private static Image blueprint_light;
    private static Image finished_stamp;
    
    // Параметры анимации
    private static class FallingBlueprint {
        float x, y;
        float speed;
        boolean active;
    }
    private static FallingBlueprint[] blueprints = new FallingBlueprint[8];
    
    public static void main(String[] args) throws IOException {
        logo = ImageIO.read(ArchitectTimeTracker.class.getResourceAsStream("architect_logo.png"));
        blueprint = ImageIO.read(ArchitectTimeTracker.class.getResourceAsStream("blueprint.png"));
        blueprint_light = ImageIO.read(ArchitectTimeTracker.class.getResourceAsStream("blueprint_light.png"));
        finished_stamp = ImageIO.read(ArchitectTimeTracker.class.getResourceAsStream("finished_stamp.png"));
        
        for (int i = 0; i < blueprints.length; i++) {
            blueprints[i] = new FallingBlueprint();
            blueprints[i].active = false;
        }
        
        app_instance = new ArchitectTimeTracker();
        app_instance.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        app_instance.setLocation(200, 50);
        app_instance.setSize(900, 700);
        app_instance.setResizable(false);
        app_instance.setTitle("Архитектурное бюро - Учет рабочего времени");
        
        last_frame_time = System.nanoTime();
        
        GameField game_field = new GameField();
        
        game_field.addMouseListener(new MouseAdapter() { 
            @Override
            public void mousePressed(MouseEvent e) {
                int x = e.getX();
                int y = e.getY();
                
                boolean start_click = x >= 50 && x <= 250 && y >= 550 && y <= 620;
                boolean finish_click = x >= 600 && x <= 850 && y >= 550 && y <= 620;
                
                if (start_click && !is_working) {
                    startWork();
                }
                
                if (finish_click && is_working) {
                    finishWork();
                }
            }
        });
        
        app_instance.add(game_field);
        app_instance.setVisible(true);
        
        runAnimationLoop();
    }
    
    private static void startWork() {
        work_start_time = System.nanoTime();
        is_working = true;
        is_finished = false;
        show_stamp = false;
        app_instance.setTitle("Архитектурное бюро - Работаем над проектом...");
        
        for (int i = 0; i < blueprints.length; i++) {
            blueprints[i].active = true;
            blueprints[i].x = (float) (Math.random() * (app_instance.getWidth() - 100));
            blueprints[i].y = (float) (Math.random() * app_instance.getHeight());
            blueprints[i].speed = 30 + (float) Math.random() * 50;
        }
    }
    
    private static void finishWork() {
        is_working = false;
        is_finished = true;
        show_stamp = true;
        work_seconds = (System.nanoTime() - work_start_time) / 1_000_000_000;
        
        for (int i = 0; i < blueprints.length; i++) {
            blueprints[i].active = false;
        }
        
        app_instance.setTitle("Архитектурное бюро - Работа завершена! ✓");
    }
    
    private static void runAnimationLoop() {
        while (true) {
            app_instance.repaint();
            try {
                Thread.sleep(16);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    
    private static void updateAnimation(float delta_time) {
        if (!is_working) return;
        
        for (FallingBlueprint bp : blueprints) {
            if (bp.active) {
                bp.y += bp.speed * delta_time;
                
                if (bp.y > app_instance.getHeight()) {
                    bp.y = -100;
                    bp.x = (float) (Math.random() * (app_instance.getWidth() - 100));
                }
            }
        }
    }
    
    private static void onRepaint(Graphics g) {
        long current_time = System.nanoTime();
        float delta_time = (current_time - last_frame_time) * 0.000000001f;
        last_frame_time = current_time;
        
        updateAnimation(delta_time);
        
        // Фон
        g.setColor(new java.awt.Color(240, 240, 250));
        g.fillRect(0, 0, app_instance.getWidth(), app_instance.getHeight());
        
        // Сетка
        g.setColor(new java.awt.Color(200, 200, 220));
        for (int i = 0; i < app_instance.getWidth(); i += 50) {
            g.drawLine(i, 0, i, app_instance.getHeight());
            g.drawLine(0, i, app_instance.getWidth(), i);
        }
        
        // Логотип
        if (logo != null) {
            g.drawImage(logo, 30, 120, 120, 120, null);
        } else {
            g.setColor(new java.awt.Color(150, 120, 80));
            g.fillRect(30, 120, 120, 120);
            g.setColor(java.awt.Color.WHITE);
            g.drawString("Logo", 70, 190);
        }
        
        // Падающие чертежи
        for (FallingBlueprint bp : blueprints) {
            if (bp.active) {
                if (blueprint != null) {
                    Image img = (work_seconds % 2 == 0) ? blueprint : blueprint_light;
                    if (img != null) {
                        g.drawImage(img, (int) bp.x, (int) bp.y, 70, 90, null);
                    } else {
                        g.setColor(new java.awt.Color(200, 180, 120));
                        g.fillRect((int) bp.x, (int) bp.y, 70, 90);
                    }
                }
            }
        }
        
        // Верхняя панель
        g.setColor(new java.awt.Color(50, 50, 80));
        g.fillRect(0, 0, app_instance.getWidth(), 100);
        
        g.setColor(java.awt.Color.WHITE);
        g.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 28));
        
        long current_seconds = 0;
        if (is_working) {
            current_seconds = (System.nanoTime() - work_start_time) / 1_000_000_000;
        } else if (is_finished) {
            current_seconds = work_seconds;
        }
        
        long hours = current_seconds / 3600;
        long minutes = (current_seconds % 3600) / 60;
        long seconds = current_seconds % 60;
        String time_str = String.format("%02d:%02d:%02d", hours, minutes, seconds);
        
        g.drawString("Время работы: " + time_str, 200, 60);
        
        String status;
        if (is_working) {
            status = "СТАТУС: РАБОТА НАД ПРОЕКТОМ";
            g.setColor(new java.awt.Color(100, 200, 100));
        } else if (is_finished) {
            status = "СТАТУС: РАБОТА ЗАВЕРШЕНА";
            g.setColor(new java.awt.Color(200, 200, 100));
        } else {
            status = "СТАТУС: ОЖИДАНИЕ НАЧАЛА РАБОТЫ";
            g.setColor(new java.awt.Color(200, 200, 100));
        }
        g.drawString(status, 200, 100);
        
        // ============ КНОПКИ (исправлено) ============
        
        // Кнопка "Начать работу"
        g.setColor(new java.awt.Color(60, 120, 60));
        g.fillRoundRect(50, 550, 200, 70, 20, 20);
        g.setColor(java.awt.Color.BLACK);
        g.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 18));
        g.drawString("НАЧАТЬ РАБОТУ", 70, 600);
        
        // Кнопка "Завершить работу"
        if (is_working) {
            g.setColor(new java.awt.Color(180, 60, 60));  // красная — активна
        } else {
            g.setColor(new java.awt.Color(160, 160, 160)); // светло-серая — неактивна
        }
        g.fillRoundRect(600, 550, 250, 70, 20, 20);
        g.setColor(java.awt.Color.BLACK);  // текст всегда черный
        g.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 18));
        g.drawString("ЗАВЕРШИТЬ РАБОТУ", 620, 600);
        
        // =============================================
        
        // Штамп
        if (show_stamp && finished_stamp != null) {
            int stampWidth = 280;
            int stampHeight = 160;
            int stampX = (app_instance.getWidth() - stampWidth) / 2;
            int stampY = (app_instance.getHeight() - stampHeight) / 2 - 50;
            g.drawImage(finished_stamp, stampX, stampY, stampWidth, stampHeight, null);
            
            g.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 18));
            g.setColor(new java.awt.Color(50, 150, 50));
            String message = "✓ Работа сдана на проверку";
            int msgWidth = g.getFontMetrics().stringWidth(message);
            g.drawString(message, (app_instance.getWidth() - msgWidth) / 2, stampY + stampHeight + 30);
        }
        
        // Подсказка
        g.setFont(new java.awt.Font("Arial", java.awt.Font.PLAIN, 12));
        g.setColor(new java.awt.Color(100, 100, 120));
        g.drawString("Архитектурное бюро | Учет рабочего времени", 30, app_instance.getHeight() - 20);
    }
    
    private static class GameField extends JPanel {
        @Override 
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            onRepaint(g);
        }
    }
}