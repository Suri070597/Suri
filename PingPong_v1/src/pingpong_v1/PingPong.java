/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pingpong_v1;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JLabel;

/**
 *
 * @author Nguyen Thi Kim Soan - CE180197
 */
public class PingPong extends javax.swing.JFrame {

    public static int _BallSize = 20;
    public static int _LogoSize = 450;
    public static int _RacketHeight = 50;
    public static int _RacketWidth = 28;
    public static int _ScenceHeight = 400;
    public static int _ScenceWidth = 800;

    int time;
    Thread timer;
    Thread gameThread;

    int redPoint;
    int xRed, yRed;
    JLabel red;

    int bluePoint;
    int xBlue, yBlue;
    JLabel blue;

    int xBall, yBall, dxBall, dyBall;
    JLabel ball;

    int speedBall, speedRacket;

    int xLogo, yLogo;
    JLabel logo;

    public void reset() {
        redPoint = bluePoint = 0;
        xRed = 10;
        xBlue = _ScenceWidth - _RacketWidth - 10;

        yRed = yBlue = (_ScenceHeight - _RacketHeight) / 2;

        xBall = (_ScenceWidth - _BallSize) / 2;
        yBall = (_ScenceHeight - _BallSize) / 2;

        int dx = Randomizer.random(0, 1);
        dxBall = dx == 0 ? -1 : 1;

        int dy = Randomizer.random(0, 1);
        dyBall = dy == 0 ? -1 : 1;

        speedBall = 8;
        speedRacket = 5;

        xLogo = (_ScenceWidth - _LogoSize) / 2 + 70;
        yLogo = (_ScenceHeight - _LogoSize) / 2;

        time = 0;
    }

    public void initScence() {
        pnlScence.setLayout(null);
        pnlScence.removeAll();
        pnlScence.revalidate();
        pnlScence.repaint();
        pnlScence.setSize(_ScenceWidth, _ScenceHeight);

        red = new JLabel();
        red.setOpaque(true);
        red.setBounds(xRed, yRed, _RacketWidth, _RacketHeight);
        red.setIcon(new ImageIcon(getClass().getResource("/imgs/red.jpg")));

        blue = new JLabel();
        blue.setOpaque(true);
        blue.setBounds(xBlue, yBlue, _RacketWidth, _RacketHeight);
        blue.setIcon(new ImageIcon(getClass().getResource("/imgs/blue.jpg")));

        ball = new JLabel();
        ball.setBounds(xBall, yBall, _BallSize, _BallSize);
        ball.setIcon(new ImageIcon(getClass().getResource("/imgs/ball.png")));

        logo = new JLabel();
        logo.setBounds(xLogo, yLogo, _LogoSize, _LogoSize);
        logo.setIcon(new ImageIcon(getClass().getResource("/imgs/logo.png")));

        pnlScence.setBackground(Color.WHITE);
        pnlScence.add(red);
        pnlScence.add(blue);
        pnlScence.add(ball);
        pnlScence.add(logo);
    }

    private String int2time(int time) {
        return String.format("%02d:%02d:%02d", time / 3600, (time / 60) % 60, time % 60);
    }

    public void runTimer() {
        timer = new Thread() {
            public void run() {
                while (true) {
                    ++time;
                    lblTime.setText(int2time(time));
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(PingPong.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        };
        timer.start();
    }

    public boolean isGameOver() {
        return xBall <= 0 || xBall >= _ScenceWidth - 15;
    }

    public boolean isHit() {
        int ballR = _BallSize / 2;
        int bx = xBall + ballR;
        int by = yBall + ballR;

        if (bx + ballR >= xRed && bx - ballR <= xRed + _RacketWidth) {
            if (by + ballR >= yRed && by - ballR <= yRed + _RacketHeight) {
                return true;
            }
        }

        if (bx + ballR >= xBlue && bx - ballR <= xBlue + _RacketWidth) {
            if (by + ballR >= yBlue && by - ballR <= yBlue + _RacketHeight) {
                return true;
            }
        }
        return false;
    }

    public void GameOver() {

        if (xBall <= 0) {
            lblMessage.setText("Blue player is the winner. Press SPACE BAR to continue!");
            lblMessage.setForeground(Color.BLUE);
            bluePoint++;
        }
        if (xBall >= _ScenceWidth - _RacketWidth) {
            lblMessage.setText("Red player is the winner. Press SPACE BAR to continue!");
            lblMessage.setForeground(Color.RED);
            redPoint++;
        }
        lblBlue.setText(bluePoint + "");
        lblRed.setText(redPoint + "");
        timer.stop();
        gameThread.stop();
    }

    public void replay() {
        xBall = (_ScenceWidth - _BallSize) / 2;
        yBall = (_ScenceHeight - _BallSize) / 2;
        Random random = new Random();
        int rand = random.nextInt(2);

        dxBall = rand == 0 ? -1 : 1;
        dyBall = rand == 0 ? -1 : 1;
    }

    public void runGame() {
        gameThread = new Thread() {
            public void run() {
                while (true) {
                    try {
                        //Di chuyen vot do
                        red.setBounds(xRed, yRed, _RacketWidth, _RacketHeight);

                        //Di chuyen vot xanh
                        blue.setBounds(xBlue, yBlue, _RacketWidth, _RacketHeight);

                        //Di chuyen banh
                        xBall += speedBall * dxBall;
                        yBall += speedBall * dyBall;
//                        if (xBall < 0) {
//                            xBall = 0;
//                            dxBall = -dxBall; //Doi huong di chuyen
//                        } else if (xBall > _ScenceWidth - _BallSize) {
//                            xBall = _ScenceWidth - _BallSize;
//                            dxBall = -dxBall; //Doi huong di chuyen
//                        }

                        if (isGameOver()) {
                            GameOver();
                        } else if (isHit()) {
                            dxBall = -dxBall;
                        }
                        if (yBall < 0) {
                            yBall = 0;
                            dyBall = -dyBall; //Doi huong di chuyen
                        } else if (yBall > _ScenceHeight - _BallSize) {
                            yBall = _ScenceHeight - _BallSize;
                            dyBall = -dyBall; //Doi huong di chuyen
                        }
                        ball.setBounds(xBall, yBall, _BallSize, _BallSize);
                        Thread.sleep(50);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(PingPong.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        };
        gameThread.start();
    }

    /**
     * Creates new form PingPong
     */
    public PingPong() {
        initComponents();
        this.setLocationRelativeTo(null); //Put this frame into center of screen

        reset();
        initScence();
        runTimer();
        runGame();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel2 = new javax.swing.JLabel();
        pnlGameInfo = new javax.swing.JPanel();
        lblRedLabel = new javax.swing.JLabel();
        lblRed = new javax.swing.JLabel();
        lblBlueLabel = new javax.swing.JLabel();
        lblBlue = new javax.swing.JLabel();
        lblTimeLabel = new javax.swing.JLabel();
        lblTime = new javax.swing.JLabel();
        lblMessage = new javax.swing.JLabel();
        pnlScence = new javax.swing.JPanel();

        jLabel2.setText("jLabel2");

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                formKeyPressed(evt);
            }
        });

        pnlGameInfo.setBackground(new java.awt.Color(255, 255, 255));
        pnlGameInfo.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Game Infomation", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Dialog", 0, 13))); // NOI18N

        lblRedLabel.setFont(new java.awt.Font("Arial", 0, 13)); // NOI18N
        lblRedLabel.setForeground(new java.awt.Color(255, 0, 0));
        lblRedLabel.setText("Red:");

        lblRed.setForeground(new java.awt.Color(255, 0, 0));
        lblRed.setText("0");

        lblBlueLabel.setFont(new java.awt.Font("Arial", 0, 13)); // NOI18N
        lblBlueLabel.setForeground(new java.awt.Color(0, 51, 255));
        lblBlueLabel.setText("Blue:");

        lblBlue.setForeground(new java.awt.Color(0, 51, 255));
        lblBlue.setText("0");

        lblTimeLabel.setText("Time:");

        lblTime.setText("00:00:00");

        lblMessage.setText("                                                    ");

        javax.swing.GroupLayout pnlGameInfoLayout = new javax.swing.GroupLayout(pnlGameInfo);
        pnlGameInfo.setLayout(pnlGameInfoLayout);
        pnlGameInfoLayout.setHorizontalGroup(
            pnlGameInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlGameInfoLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblRedLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblRed)
                .addGap(93, 93, 93)
                .addComponent(lblBlueLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblBlue)
                .addGap(71, 71, 71)
                .addComponent(lblTimeLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblTime)
                .addGap(58, 58, 58)
                .addComponent(lblMessage, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        pnlGameInfoLayout.setVerticalGroup(
            pnlGameInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlGameInfoLayout.createSequentialGroup()
                .addGap(21, 21, 21)
                .addGroup(pnlGameInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblRedLabel)
                    .addComponent(lblRed)
                    .addComponent(lblBlueLabel)
                    .addComponent(lblBlue)
                    .addComponent(lblTimeLabel)
                    .addComponent(lblTime)
                    .addComponent(lblMessage))
                .addContainerGap(34, Short.MAX_VALUE))
        );

        pnlScence.setBackground(new java.awt.Color(255, 255, 255));
        pnlScence.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        pnlScence.setPreferredSize(new java.awt.Dimension(800, 400));
        pnlScence.setLayout(null);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(pnlGameInfo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(pnlScence, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(pnlGameInfo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlScence, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void formKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_formKeyPressed
        // TODO add your handling code here:
        int key = evt.getKeyCode();
        if (key == KeyEvent.VK_UP) {
            yBlue -= speedRacket;
        }
        if (key == KeyEvent.VK_DOWN) {
            yBlue += speedRacket;
        }
        if (key == KeyEvent.VK_W) {
            yRed -= speedRacket;
        }
        if (key == KeyEvent.VK_S) {
            yRed += speedRacket;
        }

        if (key == KeyEvent.VK_SPACE) {
            replay();
            runTimer();
            runGame();
        }
    }//GEN-LAST:event_formKeyPressed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(PingPong.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(PingPong.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(PingPong.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(PingPong.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new PingPong().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel lblBlue;
    private javax.swing.JLabel lblBlueLabel;
    private javax.swing.JLabel lblMessage;
    private javax.swing.JLabel lblRed;
    private javax.swing.JLabel lblRedLabel;
    private javax.swing.JLabel lblTime;
    private javax.swing.JLabel lblTimeLabel;
    private javax.swing.JPanel pnlGameInfo;
    private javax.swing.JPanel pnlScence;
    // End of variables declaration//GEN-END:variables
}
