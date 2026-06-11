import java.util.*;

public class Grafo {
    Map<String, List<Aresta>> map = new HashMap<>();
    
    public void adicionarRua(String origem, String destino, int distancia) {

        map.putIfAbsent(origem, new ArrayList<>());
        map.putIfAbsent(destino, new ArrayList<>());

        map.get(origem).add(new Aresta(destino, distancia));
        map.get(destino).add(new Aresta(origem, distancia));

    }
}
