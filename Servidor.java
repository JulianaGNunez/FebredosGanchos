import java.net.*;
import java.io.*;
import java.util.*;

//para calcular seno e cosseno
import java.lang.Object;
import java.lang.Math; 

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
      // nao usar DataInputstream
      Scanner is[] = new Scanner[Dados.NUM_MAX_JOGADORES];
      // nao usar DataOutputstream
      PrintStream os[] = new PrintStream[Dados.NUM_MAX_JOGADORES];
      
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
  
  boolean conectaCliente(int id, Scanner is[], PrintStream os[]) {
    Socket clientSocket = null;
    try {
      clientSocket = serverSocket.accept();

      System.out.println("Cliente " + id + " conectou!");
      
      is[id] = new Scanner(clientSocket.getInputStream());
      os[id] = new PrintStream(clientSocket.getOutputStream());
      
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
  static final int RAIO = 250;
  static final int angInviavel = 8;
  
  static final int velGancho = 40; // velocidade que o gancho anda quando eh atirado
  static final int velPlayer = 3; // velocidade que o player se move quando eh 
  
  class EstadoJogador {
    int acao; // o valor recebido da conexão com o cliente
    int pontos; // pontuacao de quantas vezes o jogador conseguiu puxar o outro para o centro
    int x, y; // posiçao em x e y do Player
    int gx, gy; // posição em x e y do gancho
    int vy; // velocidade em y do jogador
    int contador; // quantidade de frames á mais para evitar o delay do clique 
    int angulo; // para o jogador e seu próprio gancho
    int velGancho;
    boolean imovel; // se o jogador esta atirando ou foi agarrado
  }
  
  EstadoJogador estado[] = new EstadoJogador[NUM_MAX_JOGADORES];
  
  Dados() {
    // inicia os estados dos elementos no jogo
    for (int i = 0; i < NUM_MAX_JOGADORES; i++) {
      estado[i] = new EstadoJogador();
      estado[i].pontos = 0;
      estado[i].acao = 0;
      if(i == 0){
        estado[i].x = LARG_CLIENTE/2 - RAIO;
        estado[i].angulo = 180;
        estado[i].velGancho = velGancho;
      }
      else{
        estado[i].x = LARG_CLIENTE/2 + RAIO;
        estado[i].angulo = 0;
        estado[i].velGancho = -velGancho;
      }
      estado[i].y = ALTU_CLIENTE / 2;
      estado[i].vy = 10;
      estado[i].imovel = false;
    }
  }
  
  /** Envia os dados dos elementos do jogo aos clientes
   */
  synchronized boolean enviaClientes(PrintStream os[]) {
    // um caracter extra pode ser usado para indicar o tipo de dados
    // está sendo enviado.
    if (os[CLIENTE_UM] != null) {
      // para enviar ao cliente um inverte o lado do cliente dois
      os[CLIENTE_UM].println(estado[CLIENTE_UM].x);
      os[CLIENTE_UM].println(estado[CLIENTE_UM].y);
      os[CLIENTE_UM].println(estado[CLIENTE_UM].gx);
      os[CLIENTE_UM].println(estado[CLIENTE_UM].gy);
      os[CLIENTE_UM].println(estado[CLIENTE_DOIS].x);
      os[CLIENTE_UM].println(estado[CLIENTE_DOIS].y);
      os[CLIENTE_UM].println(0);
      os[CLIENTE_UM].println(0);
    }
    
    if (os[CLIENTE_DOIS] != null) {
      // para enviar ao cliente dois inverte o lado do cliente um
      os[CLIENTE_DOIS].println(estado[CLIENTE_DOIS].x);
      os[CLIENTE_DOIS].println(estado[CLIENTE_DOIS].y);
      os[CLIENTE_DOIS].println(estado[CLIENTE_DOIS].gx);
      os[CLIENTE_DOIS].println(estado[CLIENTE_DOIS].gy);
      os[CLIENTE_DOIS].println(estado[CLIENTE_UM].x);
      os[CLIENTE_DOIS].println(estado[CLIENTE_UM].y);
      os[CLIENTE_DOIS].println(0);
      os[CLIENTE_DOIS].println(0);
    }
    
    if (os[CLIENTE_UM] != null)
      os[CLIENTE_UM].flush();
    if (os[CLIENTE_DOIS] != null)
      os[CLIENTE_DOIS].flush();
    return true;
  }
  
  synchronized void alteraDadosInput(int c, int id) {
    if(c == 1 || c == 2){
      if(estado[id].imovel == false)
        estado[id].acao = c;
    }
    else
      estado[id].acao = c;
  }
  
  synchronized void logicaDoJogo() {
    boolean moveu;
    for (int i = 0; i < NUM_MAX_JOGADORES; i++) {
      moveu = false;
      if(estado[i].imovel == false){
        if(estado[i].acao == 1){
          if(i == 0)
            estado[i].angulo -= velPlayer;
          else
            estado[i].angulo += velPlayer;
          moveu = true;
          if(estado[i].angulo < 0)
            estado[i].angulo = 360 + estado[i].angulo;
          if(estado[i].angulo >= 360)
           estado[i].angulo = estado[i].angulo % 360;
        }
        if(estado[i].acao == 2){
          if(i == 0)
            estado[i].angulo += velPlayer;
          else
            estado[i].angulo -= velPlayer;
          moveu = true;
          if(estado[i].angulo < 0)
            estado[i].angulo = 360 + estado[i].angulo;
          if(estado[i].angulo >= 360)
            estado[i].angulo = estado[i].angulo % 360;
        }
      }

      if(i == 0){
        if(estado[0].angulo < 90 + angInviavel){
          estado[0].angulo = 90 + angInviavel;
        }
        else{
          if(estado[0].angulo > 270 - angInviavel)
            estado[0].angulo = 270 - angInviavel;
        }
      }
      else{
        if(estado[1].angulo > 90 - angInviavel && estado[1].angulo < 270){
          estado[1].angulo = 90 - angInviavel;
        }
        else{
          if(estado[1].angulo > 270 && estado[1].angulo < 270 + angInviavel)
            estado[1].angulo = 270 + angInviavel;
        }
      }
   
      if(moveu == true){
        int x, y;

        x = (int)((float)Math.cos(Math.toRadians((double)estado[i].angulo)) * (float)RAIO);
        y = (int)((float)Math.sin(Math.toRadians((double)estado[i].angulo)) * (float)RAIO);
        /*
        if(i == 0)
          x = - x;
        */
        estado[i].x = LARG_CLIENTE/2 + x;
        estado[i].y = ALTU_CLIENTE/2 - y;
      }
    }
  }
}

/** Esta classe é responsável por receber os dados de cada cliente.
 * Uma instância para cada cliente deve ser executada.
 */
class Recebendo extends Thread {
  Scanner is[];
  Dados dados;
  int idCliente;

  Recebendo(int id, Scanner is[], Dados d) {
    idCliente = id;
    dados = d;
    this.is = is;
  }
  
  public void run() {
      while (true) {
        int c = is[idCliente].nextInt();
        dados.alteraDadosInput(c, idCliente);
      }
  }
};

/** Esta classe é responsável por enviar os dados dos elementos para os 
 * cliente. Uma única instância envia os dados para os dois clientes.
 */
class Enviando extends Thread {
  PrintStream os[];
  Dados dados;

  Enviando(PrintStream os[], Dados d) {
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
