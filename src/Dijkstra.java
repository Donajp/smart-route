import java.util.*;

class ResultadoDijkstra {
    public List<String> caminho;
    public int distanciaTotal;

    public ResultadoDijkstra(List<String> caminho, int distanciaTotal) {
        this.caminho = caminho;
        this.distanciaTotal = distanciaTotal;
    }
}

class Dijkstra {

    public static ResultadoDijkstra menorCaminho(Grafo grafo, String inicio, String destinoFinal) {

        Map<String, Integer> distancias = new HashMap<>();
        Map<String, String> anteriores = new HashMap<>();

        for (String cidade : grafo.map.keySet()) {
            distancias.put(cidade, Integer.MAX_VALUE);
        }

        distancias.put(inicio, 0);

        PriorityQueue<String> fila =
                new PriorityQueue<>(Comparator.comparingInt(distancias::get));

        fila.add(inicio);

        while (!fila.isEmpty()) {

            String atual = fila.poll();

            for (Aresta aresta : grafo.map.get(atual)) {

                int novaDistancia = distancias.get(atual) + aresta.peso;

                if (novaDistancia < distancias.get(aresta.destino)) {

                    distancias.put(aresta.destino, novaDistancia);

                    
                    anteriores.put(aresta.destino, atual);

                    fila.add(aresta.destino);
                }
            }
        }

        
        List<String> caminho = new ArrayList<>();
        String passoAtual = destinoFinal;

        
        if (anteriores.containsKey(passoAtual) || passoAtual.equals(inicio)) {
            while (passoAtual != null) {
                caminho.add(passoAtual);
                passoAtual = anteriores.get(passoAtual);
            }
            Collections.reverse(caminho);
        }

        
        int distanciaFinal = distancias.getOrDefault(destinoFinal, Integer.MAX_VALUE);

        
        return new ResultadoDijkstra(caminho, distanciaFinal);
    }
}