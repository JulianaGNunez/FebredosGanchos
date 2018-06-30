import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.*;
import java.net.*;

class ClienteRede extends JFrame {
  Desenho des = new Desenho();
  DataOutputStream os = null;
  DataInputStream is = null;
  Socket socket = null;

  int posX = 0, posY = 0;
  int posXAdversario = 0, posYAdversario = 0;
  int posXGancho = 0, posYGancho = 0;
  int posXGanchoAdv = 0, posYGanchoAdv = 0;

  class Desenho extends JPanel {
    int alturaJanela = 700, larguraJanela = 1500;
    Desenho() {
      setPreferredSize(new Dimension(alturaJanela, larguraJanela));
    }

    public void paintComponent(Graphics g) {
      Graphics2D g2d = (Graphics2D) g;
      super.paintComponent(g2d);
      g2d.setBackground(new Color(207,140,225));
      g2d.clearRect(0,0,larguraJanela,alturaJanela);
      g2d.setColor(new Color(50, 250, 80));
      g2d.fillOval(posX,posY, 12, 12);
      g2d.setColor(new Color(240, 100, 10));
      g2d.fillOval(posXAdversario,posYAdversario, 12, 12);
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
      os = new DataOutputStream(socket.getOutputStream());
      // Eu usei DataInputstream, mas esta classe é proibida no seu
      // trabalho! Tente usar Scanner
      is = new DataInputStream(socket.getInputStream());
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
        try {
          while (true) {
            // um caracter extra pode ser usado para indicar o tipo de
            // dados está sendo recebido.

            // dados deste Player
            posX = is.readInt();
            posY = is.readInt();
            posXGancho = is.readInt();
            posYGancho = is.readInt();
            //dados do Player adversario
            posXAdversario = is.readInt();
            posYAdversario = is.readInt();
            posXGanchoAdv = is.readInt();
            posYGanchoAdv= is.readInt();
            repaint();
          }
        } catch (IOException ex) {
          // coloque um JOptionPane para mostrar esta mensagem de erro
          System.err.println("O servidor interrompeu a comunicação");
          System.exit(1);
        }
      }
    }.start();
    
    addKeyListener(new KeyAdapter() {
      public void keyPressed(KeyEvent e) {
        int inputTeclado;
        char outputChar;
        try {
          // apenas a letra está sendo enviada, mas um comando com 
          // coordenadas ou um caracter indicador de mudança de
          // comportamento do jogador poderia ser enviado dependendo da
          // dinâmica do jogo
          inputTeclado = e.getKeyCode();
          switch(inputTeclado){ 
            case KeyEvent.VK_UP: // jogador pressionou seta para cima
              outputChar = 1;
              break;
            case KeyEvent.VK_DOWN: // jogador pressionou seta para baixo
              outputChar = 2;
              break;
            case KeyEvent.VK_SPACE: // jogador pressionou espaco
              outputChar = 3;
              break;
            default:
              outputChar = 0;
              break;

          }
          os.writeChar(outputChar);
        } catch (IOException ex) {
          // coloque um JOptionPane para mostrar esta mensagem de erro
          System.err.println("O servidor interrompeu a comunicação");
          System.exit(1);
        }
      }

      public void keyReleased(KeyEvent e){
        try {
          // se o jogador soltou a tecla ele envia o comando de parar de movimentar o jogador
          os.writeChar(0);
        } catch (IOException ex) {
          // coloque um JOptionPane para mostrar esta mensagem de erro
          System.err.println("O servidor interrompeu a comunicação");
          System.exit(1);
        }
      }
    });
    
    pack();
    setVisible(true);
  }

  static public void main(String[] args) {
    ClienteRede f = new ClienteRede();
  }
}
