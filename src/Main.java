import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Arrays;
import java.util.stream.Collectors;

public class Main {

    public static void main(String[] args) throws IOException {
        
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        
        
        server.createContext("/api/rota", new RotaHandler());
        
        server.setExecutor(null); 
        System.out.println("Servidor Java rodando com sucesso na porta 8080...");
        server.start();
    }

    static class RotaHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            // CABEÇALHOS CORS: Permite que o front-end (HTML/JS) faça requisições ao servidor Java
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, OPTIONS");
            exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");

            
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                
                String query = exchange.getRequestURI().getQuery();
                Map<String, String> parametros = extrairParametros(query);

                String origem = parametros.getOrDefault("origem", "A");
                String destino = parametros.getOrDefault("destino", "D");
                
                
                String bloqueadasStr = parametros.getOrDefault("bloqueadas", "");
                List<String> ruasBloqueadas = Arrays.asList(bloqueadasStr.split(","));

                
                Grafo cidade = new Grafo();
                
                
                adicionarRuaSeNaoBloqueada(cidade, "A", "L", 400, ruasBloqueadas);
                adicionarRuaSeNaoBloqueada(cidade, "A", "I", 500, ruasBloqueadas);
                adicionarRuaSeNaoBloqueada(cidade, "A", "F", 700, ruasBloqueadas);
                adicionarRuaSeNaoBloqueada(cidade, "A", "G", 1400, ruasBloqueadas);
                adicionarRuaSeNaoBloqueada(cidade, "B", "L", 600, ruasBloqueadas);
                adicionarRuaSeNaoBloqueada(cidade, "B", "C", 800, ruasBloqueadas);
                adicionarRuaSeNaoBloqueada(cidade, "B", "E", 600, ruasBloqueadas);
                adicionarRuaSeNaoBloqueada(cidade, "C", "I", 700, ruasBloqueadas);
                adicionarRuaSeNaoBloqueada(cidade, "C", "D", 1200, ruasBloqueadas);
                adicionarRuaSeNaoBloqueada(cidade, "D", "I", 900, ruasBloqueadas);
                adicionarRuaSeNaoBloqueada(cidade, "D", "H", 1100, ruasBloqueadas);
                adicionarRuaSeNaoBloqueada(cidade, "E", "L", 600, ruasBloqueadas);
                adicionarRuaSeNaoBloqueada(cidade, "E", "F", 900, ruasBloqueadas);
                adicionarRuaSeNaoBloqueada(cidade, "E", "J", 800, ruasBloqueadas);
                adicionarRuaSeNaoBloqueada(cidade, "H", "I", 1000, ruasBloqueadas);
                adicionarRuaSeNaoBloqueada(cidade, "H", "G", 900, ruasBloqueadas);
                adicionarRuaSeNaoBloqueada(cidade, "H", "K", 1000, ruasBloqueadas);
                adicionarRuaSeNaoBloqueada(cidade, "F", "G", 1300, ruasBloqueadas);

                
                ResultadoDijkstra resultado = Dijkstra.menorCaminho(cidade, origem, destino);

                List<String> listaCaminho = resultado.caminho; 
                int distanciaTotal = resultado.distanciaTotal;

                
                String caminhoJson = listaCaminho.stream()
                        .map(no -> "\"" + no + "\"")
                        .collect(Collectors.joining(", "));

                String jsonResposta = String.format(
                        "{\"caminho\": [%s], \"distanciaTotal\": %d}",
                        caminhoJson,
                        distanciaTotal
                );

                
                byte[] respostaBytes = jsonResposta.getBytes("UTF-8");

                
                exchange.getResponseHeaders().add("Content-Type", "application/json; charset=UTF-8");
                exchange.sendResponseHeaders(200, respostaBytes.length);
                
                OutputStream os = exchange.getResponseBody();
                os.write(respostaBytes);
                os.close();
            }
        }

        
        private Map<String, String> extrairParametros(String query) {
            Map<String, String> resultado = new HashMap<>();
            if (query == null || query.isEmpty()) return resultado;

            for (String par : query.split("&")) {
                String[] dados = par.split("=");
                if (dados.length > 1) {
                    resultado.put(dados[0], dados[1]);
                }
            }
            return resultado;
        }

        
        private void adicionarRuaSeNaoBloqueada(Grafo grafo, String origem, String destino, int peso, List<String> bloqueadas) {
            String rota1 = origem + "-" + destino;
            String rota2 = destino + "-" + origem;
            
            
            if (!bloqueadas.contains(rota1) && !bloqueadas.contains(rota2)) {
                grafo.adicionarRua(origem, destino, peso);
            }
        }
    }
}