import java.net.*;
import java.io.*;
import java.util.*;

class Servidor {
  ServerSocket serverSocket = null;

  public static void main(String[] args) {
    new Servidor();
  }
  
  Servidor() {
    final int porto = 8080;

    try {
      serverSocket = new ServerSocket(porto);
    } catch (IOException e) {
      System.err.println("O porto " + porto + " não pode ser usado.\n" + e);
      System.exit(1);
    }

    System.err.println("Servidor esperando cliente...\nDigite <ctrl>+C para terminar.");
      
    while (true) {
      Dados dados = new Dados();
      // Eu usei DataInputstream, mas esta classe é proibida no seu trabalho! Tente usar Scanner
      DataInputStream is[] = new DataInputStream[Dados.NUM_MAX_JOGADORES];
      // Eu usei DataOutputstream, mas esta classe é proibida no seu trabalho! Tente usar PrintStream
      DataOutputStream os[] = new DataOutputStream[Dados.NUM_MAX_JOGADORES];
      
      // conectando ao primeiro cliente
      conectaCliente(Dados.CLIENTE_UM, is, os);
      new Recebendo(Dados.CLIENTE_UM, is, dados).start();
      
      // só começa a thread de envio após um cliente chegar
      new Enviando(os, dados).start();
      
      // conectando ao segundo cliente
      conectaCliente(Dados.CLIENTE_DOIS, is, os);
      new Recebendo(Dados.CLIENTE_DOIS, is, dados).start();
    }
  }
  
  boolean conectaCliente(int id, DataInputStream is[], DataOutputStream os[]) {
    Socket clientSocket = null;
    try {
      clientSocket = serverSocket.accept();

      System.out.println("Cliente " + id + " conectou!");
      
      is[id] = new DataInputStream(clientSocket.getInputStream());
      os[id] = new DataOutputStream(clientSocket.getOutputStream());
      
    } catch (IOException e) {
      System.err.println("Não foi possível conectar com o cliente.\n" + e);
      return false;
    }
    return true;  // funcionou!
  }  
}

/** Esta classe tem os dados dos elementos do jogo, a lógica e regras 
 * do comportamento destes elementos.
 */
class Dados {
  static final int NUM_MAX_JOGADORES = 2;
  static final int CLIENTE_UM = 0;
  static final int CLIENTE_DOIS = 1;
  static final int LARG_CLIENTE = 800;
  static final int ALTU_CLIENTE = 650;
  static final int contador = 3;
  
  static final int velGancho = 40; // velocidade que o gancho anda quando eh atirado
  static final int velPlayer = 20; // velocidade que o player se move quando eh 
  
  class EstadoJogador {
    char acao; // o valor recebido da conexão com o cliente
    int pontos; // pontuacao de quantas vezes o jogador conseguiu puxar o outro para o centro
    int x, y; // posiçao em x e y do Player
    int gx, gy; // posição em x e y do gancho
    int vy; // velocidade em y do jogador
    int contador; // quantidade de frames á mais para evitar o delay do clique 
  }
  
  EstadoJogador estado[] = new EstadoJogador[NUM_MAX_JOGADORES];
  
  Dados() {
    // inicia os estados dos elementos no jogo
    for (int i = 0; i < NUM_MAX_JOGADORES; i++) {
      estado[i] = new EstadoJogador();
      estado[i].pontos = 0;
      estado[i].acao = 0;
      if(i == 0){
        estado[i].x = LARG_CLIENTE/2 - 300;
      }
      else{
        estado[i].x = LARG_CLIENTE/2 + 300;
      }
      estado[i].y = ALTU_CLIENTE / 2;
      estado[i].vy = 10;
    }
  }
  
  /** Envia os dados dos elementos do jogo aos clientes
   */
  synchronized boolean enviaClientes(DataOutputStream os[]) {
    try {
      // um caracter extra pode ser usado para indicar o tipo de dados
      // está sendo enviado.
      if (os[CLIENTE_UM] != null) {
        // para enviar ao cliente um inverte o lado do cliente dois
        os[CLIENTE_UM].writeInt(estado[CLIENTE_UM].x);
        os[CLIENTE_UM].writeInt(estado[CLIENTE_UM].y);
        os[CLIENTE_UM].writeInt(estado[CLIENTE_UM].gx);
        os[CLIENTE_UM].writeInt(estado[CLIENTE_UM].gy);
        os[CLIENTE_UM].writeInt(estado[CLIENTE_DOIS].x);
        os[CLIENTE_UM].writeInt(estado[CLIENTE_DOIS].y);
        os[CLIENTE_UM].writeInt(0);
        os[CLIENTE_UM].writeInt(0);
      }
      
      if (os[CLIENTE_DOIS] != null) {
        // para enviar ao cliente dois inverte o lado do cliente um
        os[CLIENTE_DOIS].writeInt(estado[CLIENTE_DOIS].x);
        os[CLIENTE_DOIS].writeInt(estado[CLIENTE_DOIS].y);
        os[CLIENTE_DOIS].writeInt(estado[CLIENTE_DOIS].gx);
        os[CLIENTE_DOIS].writeInt(estado[CLIENTE_DOIS].gy);
        os[CLIENTE_DOIS].writeInt(estado[CLIENTE_UM].x);
        os[CLIENTE_DOIS].writeInt(estado[CLIENTE_UM].y);
        os[CLIENTE_DOIS].writeInt(0);
        os[CLIENTE_DOIS].writeInt(0);
      }
      
      if (os[CLIENTE_UM] != null)
        os[CLIENTE_UM].flush();
      if (os[CLIENTE_DOIS] != null)
        os[CLIENTE_DOIS].flush();
    } catch (IOException ex) {
      System.err.println("O servidor interrompeu a comunicação.");
      return false;
    }
    return true;
  }
  
  synchronized void alteraDadosInput(char c, int id) {
    estado[id].acao = c;
  }
  
  synchronized void alteraDadosPlayer(int x, int y, int id) {
    estado[id].x = x;
    estado[id].y = y;
  }

  synchronized void alteraDadosGancho(int x, int y, int id) {
    estado[id].gx = x;
    estado[id].gy = y;
  }
  
  synchronized void alteraDadosVelocidade(int dx, int dy, int id) {
    estado[id].vy = dy;
  }
  
  /** Logica do jogo. Os testes das jogadas e das movimentações dos 
   * elementos na arena do jogo são atualizados aqui.
   */
  synchronized void logicaDoJogo() {
    for (int i = 0; i < NUM_MAX_JOGADORES; i++) {
      if(estado[i].acao == 1)
        estado[i].y += estado[i].vy;

      if (estado[i].y <= 0 || estado[i].y > ALTU_CLIENTE) {
          estado[i].vy = -estado[i].vy;
      }

      //estado[i].acao = 0;
    }
  }
}

/** Esta classe é responsável por receber os dados de cada cliente.
 * Uma instância para cada cliente deve ser executada.
 */
class Recebendo extends Thread {
  DataInputStream is[];
  Dados dados;
  int idCliente;

  Recebendo(int id, DataInputStream is[], Dados d) {
    idCliente = id;
    dados = d;
    this.is = is;
  }
  
  public void run() {
    try {
      while (true) {
        char c = is[idCliente].readChar();
        dados.alteraDadosInput(c, idCliente);
      }

    } catch (IOException e) {
      System.err.println("Conexacao terminada pelo cliente");
    } catch (NoSuchElementException e) {
      System.err.println("Conexacao terminada pelo cliente");
    }
  }
};

/** Esta classe é responsável por enviar os dados dos elementos para os 
 * cliente. Uma única instância envia os dados para os dois clientes.
 */
class Enviando extends Thread {
  DataOutputStream os[];
  Dados dados;

  Enviando(DataOutputStream os[], Dados d) {
    dados = d;
    this.os = os;
  }

  public void run() {
    while (true) {
      dados.logicaDoJogo();
      if (!dados.enviaClientes(os)) {
        break;
      }
      try {
        sleep(33);   // o cliente receberá 30 vezes por segundo
      } catch (InterruptedException ex) {}
    }
  }
};
