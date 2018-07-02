import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.lang.Object.*;
import java.awt.Image;
import java.awt.image.BufferedImage;

import java.awt.geom.AffineTransform;
import java.awt.Graphics;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;



class ClienteRede extends JFrame {
  Desenho des = new Desenho();
  PrintStream os = null;
  Scanner is = null;
  Socket socket = null;
  BufferedImage hook, hookAdv;
  int alturaJanela = 650, larguraJanela = 800;
  int raioPlayer = 50; // na verdade eh o diametro do jogador
  int raio = 250;

  int posX = 0, posY = 0;
  int posXAdversario = 0, posYAdversario = 0;
  int posXGancho = 0, posYGancho = 0;
  int posXGanchoAdv = 0, posYGanchoAdv = 0;
  int angulo = 0, anguloAdv = 180;

  class Desenho extends JPanel {
    Desenho() {
      setPreferredSize(new Dimension(alturaJanela, larguraJanela));
      hook = LoadImage("Hooky.png");
      hookAdv = LoadImage("Hooky.png");
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
      
      g2d.setColor(new Color(60,60,60));
      g2d.fillOval(larguraJanela/2 - 65, alturaJanela/2 - 65, 130, 130);

      if(posX <= posXAdversario){ // para manter as cores consistentes
        g2d.setColor(new Color(27, 62, 222));
        g2d.fillOval(posX - raioPlayer/2,posY - raioPlayer/2, raioPlayer, raioPlayer);
        g2d.setColor(new Color(240, 37, 98));
        g2d.fillOval(posXAdversario - raioPlayer/2,posYAdversario - raioPlayer/2, raioPlayer, raioPlayer);
      }
      else{ // a unica coisa que acontece e trocar as cores
        g2d.setColor(new Color(240, 37, 98));
        g2d.fillOval(posX - raioPlayer/2, posY - raioPlayer/2, raioPlayer, raioPlayer);
        g2d.setColor(new Color(27, 62, 222));
        g2d.fillOval(posXAdversario - raioPlayer/2,posYAdversario - raioPlayer/2, raioPlayer, raioPlayer);
      }
      
      AffineTransform at1 = AffineTransform.getTranslateInstance(posXGancho - hook.getWidth()/2, posYGancho - hook.getHeight()/2);
      AffineTransform at2 = AffineTransform.getTranslateInstance(posXGanchoAdv - hookAdv.getWidth()/2, posYGanchoAdv - hookAdv.getHeight()/2);
      
      at1.rotate(Math.toRadians(-angulo + 180), hook.getWidth()/2, hook.getHeight()/2);
      at2.rotate(Math.toRadians(-anguloAdv+180), hook.getWidth()/2, hookAdv.getHeight()/2);
      
      g2d.drawImage(hook, at1, null);
      g2d.drawImage(hookAdv, at2, null);

      /*
      int xis = (int)(Math.cos(Math.toRadians(-angulo + 180))*80);
      int yis = (int)(Math.sin(Math.toRadians(-angulo + 180))*80);

      System.out.println("Angulo: " + (-angulo + 180) + " X: " + xis + " Y: " + yis);

      g2d.setColor(new Color(240, 240, 240));
      g2d.fillOval(posX +xis - 5, posY + yis - 5, 10, 10);
      */
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
          angulo = is.nextInt();
          //dados do Player adversario
          posXAdversario = is.nextInt();
          posYAdversario = is.nextInt();
          posXGanchoAdv = is.nextInt();
          posYGanchoAdv = is.nextInt();
          anguloAdv = is.nextInt();
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

  public BufferedImage LoadImage(String FileName){ //Carrega a imagem no modo BufferedImage, que é trabalhado pelo AffineTransform
    BufferedImage img = null;
    try{
    img = ImageIO.read(new File(FileName));
  }catch(IOException e){}
    return img;
  }
}
