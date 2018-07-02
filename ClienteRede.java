import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.*;
import java.net.*;
import java.util.*;

class ClienteRede extends JFrame {
  Desenho des = new Desenho();
  PrintStream os = null;
  Scanner is = null;
  Socket socket = null;
  int alturaJanela = 650, larguraJanela = 800;
  int raioPlayer = 50; // na verdade eh o diametro do jogador
  int raio = 250;

  //BufferedImage hook, hookAdv;

  int posX = 0, posY = 0;
  int posXAdversario = 0, posYAdversario = 0;
  int posXGancho = 0, posYGancho = 0;
  int posXGanchoAdv = 0, posYGanchoAdv = 0;

  class Desenho extends JPanel {
    Desenho() {
      /*
      hook = LoadImage("Hooky.jpg");
      hookAdv = LoadImage("Hooky.jpg");
      */
      setPreferredSize(new Dimension(alturaJanela, larguraJanela));
    }

    public void paintComponent(Graphics g) {
      Graphics2D g2d = (Graphics2D) g;
      super.paintComponent(g2d);
      g2d.setBackground(new Color(101,93,95));
      g2d.clearRect(0,0,larguraJanela, alturaJanela); // valores adicionados para não existir espaços em branco do reajuste

      // desenhando o circulo dos jogadores
      g2d.setColor(new Color(0, 0, 0));
      g2d.setStroke(new BasicStroke(3));
      g2d.drawOval(larguraJanela/2 - raio, alturaJanela/2 - raio, raio*2, raio*2);
      g2d.setColor(new Color(101,93,95));
      g2d.fillRect(larguraJanela/2 - 25, alturaJanela/2 - raio - 15, 50, raio*2 + 30);


      if(posX <= posXAdversario){ // para manter as cores consistentes
        g2d.setColor(new Color(27, 62, 222));
        g2d.fillOval(posX - raioPlayer/2,posY - raioPlayer/2, raioPlayer, raioPlayer);
        g2d.setColor(new Color(240, 37, 98));
        g2d.fillOval(posXAdversario - raioPlayer/2,posYAdversario - raioPlayer/2, raioPlayer, raioPlayer);

        /*
        AffineTransform at1 = AffineTransform.getTranslateInstance(P1.gX - P1.hook.getWidth()/2, P1.gY - P1.hook.getHeight()/2);
        AffineTransform at2 = AffineTransform.getTranslateInstance(P2.gX - P2.hook.getWidth()/2, P2.gY - P2.hook.getHeight()/2);
  
        at1.rotate(Math.toRadians(P1.angulo), P1.hook.getWidth()/2, P1.hook.getHeight()/2);
        at2.rotate(Math.toRadians(P2.angulo), P2.hook.getWidth()/2, P2.hook.getHeight()/2);
        */
      }
      else{ // a unica coisa que acontece e trocar as cores
        g2d.setColor(new Color(240, 37, 98));
        g2d.fillOval(posX - raioPlayer/2, posY - raioPlayer/2, raioPlayer, raioPlayer);
        g2d.setColor(new Color(27, 62, 222));
        g2d.fillOval(posXAdversario - raioPlayer/2,posYAdversario - raioPlayer/2, raioPlayer, raioPlayer);
      }

      Toolkit.getDefaultToolkit().sync();
    }
  }

  ClienteRede() {
    super("Cliente");
    setDefaultCloseOperation(EXIT_ON_CLOSE);
    add(des);
    
    try {
      socket = new Socket("127.0.0.1", 8080);
      // Eu usei DataOutputstream, mas esta classe é proibida no seu 
      // trabalho! Tente usar PrintStream
      os = new PrintStream(socket.getOutputStream());
      // Eu usei DataInputstream, mas esta classe é proibida no seu
      // trabalho! Tente usar Scanner
      is = new Scanner(socket.getInputStream());
    } catch (UnknownHostException e) {
      // coloque um JOptionPane para mostrar esta mensagem de erro
      System.err.println("Servidor desconhecido.");
      System.exit(1);
    } catch (IOException e) {
      // coloque um JOptionPane para mostrar esta mensagem de erro
      System.err.println("Não pode se conectar ao servidor.");
      System.exit(1);
    }
    
    // Thread que recebe os dados vindos do servidor, prepara as 
    // variáveis de estados dos elementos do jogo e pede o repaint()
    new Thread() {
      public void run() {
        while (true) {
          // um caracter extra pode ser usado para indicar o tipo de
          // dados está sendo recebido.

          // dados deste Player
          posX = is.nextInt();
          posY = is.nextInt();
          posXGancho = is.nextInt();
          posYGancho = is.nextInt();
          //dados do Player adversario
          posXAdversario = is.nextInt();
          posYAdversario = is.nextInt();
          posXGanchoAdv = is.nextInt();
          posYGanchoAdv= is.nextInt();
          repaint();
        }
      }
    }.start();
    
    addKeyListener(new KeyAdapter() {
      public void keyPressed(KeyEvent e) {
        int inputTeclado;

        // apenas a letra está sendo enviada, mas um comando com 
        // coordenadas ou um caracter indicador de mudança de
        // comportamento do jogador poderia ser enviado dependendo da
        // dinâmica do jogo
        inputTeclado = e.getKeyCode();
        switch(inputTeclado){ 
          case KeyEvent.VK_UP: // jogador pressionou seta para cima
            inputTeclado = 1;
            break;
          case KeyEvent.VK_DOWN: // jogador pressionou seta para baixo
            inputTeclado = 2;
            break;
          case KeyEvent.VK_SPACE: // jogador pressionou espaco
            inputTeclado = 3;
            break;
          default:
            inputTeclado = 0;
            break;

        }
        os.println(inputTeclado);
      }

      public void keyReleased(KeyEvent e){
        // se o jogador soltou a tecla ele envia o comando de parar de movimentar o jogador
        os.println(0);
      }
    });

    
    this.setSize(larguraJanela,alturaJanela);
    //pack();
    this.setResizable(false);
    this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    this.setVisible(true);
  }

  static public void main(String[] args) {
    ClienteRede f = new ClienteRede();
  }
}
