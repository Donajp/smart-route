// Ativa os cliques de interdição nas linhas assim que a página carrega
document.addEventListener("DOMContentLoaded", () => {
    document.querySelectorAll('.linha').forEach(linha => {
        linha.addEventListener('click', function() {
            this.classList.toggle('linha-bloqueada');
            this.classList.remove('linha-brilhante');
            atualizarContadorBloqueadas();
            mostrarCaminho(); // Recalcula a rota dinamicamente ao interditar!
        });
    });
});

function liberarTodas() {
    document.querySelectorAll('.linha-bloqueada').forEach(linha => {
        linha.classList.remove('linha-bloqueada');
    });
    atualizarContadorBloqueadas();
    mostrarCaminho(); // Recalcula a rota limpa
}

function atualizarContadorBloqueadas() {
    const qtd = document.querySelectorAll('.linha-bloqueada').length;
    document.getElementById('contagem-bloqueadas').innerText = qtd;
}

// Dicionário de tradução exata para os nós da interface
const nomesNos = {
    "A": "A - Praça Central", "B": "B - Mercado", "C": "C - Estação",
    "D": "D - Hospital", "E": "E - Escola", "F": "F - Casa", "G": "G - Pet Shop",
    "H": "H - Universidade", "I": "I - Shopping", "J": "J - Banco", "K": "K - Parque"
};

function mostrarCaminho() {
    const inicio = document.getElementById('origem').value; 
    const destino = document.getElementById('destino').value; 
    const painelResultado = document.getElementById('texto-resultado');

    // Limpa os brilhos verdes anteriores
    document.querySelectorAll('.linha-brilhante').forEach(linha => {
        linha.classList.remove('linha-brilhante');
    });

    if (inicio === destino) {
        painelResultado.innerHTML = `<div style="color: #f87171;">⚠️ Você já está no destino!</div>`;
        return; 
    }

    // Varre o SVG procurando as ruas interditadas
    const ruasBloqueadas = [];
    document.querySelectorAll('.linha-bloqueada').forEach(linha => {
        const partes = lineId = linha.id.split('-');
        if (partes.length >= 3) {
            ruasBloqueadas.push(partes[1] + "-" + partes[2]);
        }
    });
    const paramBloqueadas = ruasBloqueadas.join(','); 

    // Envia o pedido para o servidor Java (Porta 8080)
    fetch(`http://localhost:8080/api/rota?origem=${inicio}&destino=${destino}&bloqueadas=${paramBloqueadas}`)
        .then(response => response.json())
        .then(resultado => {
            if (resultado.distanciaTotal === 2147483647 || resultado.distanciaTotal === -1 || resultado.caminho.length === 0) {
                painelResultado.innerHTML = `<div style="color: #f87171;">❌ Rota totalmente bloqueada ou inexistente.</div>`;
            } else {
                // 1. Cálculos de Distância e Tempo (Média urbana baseada na referência do projeto)
                const distanciaKm = resultado.distanciaTotal / 1000;
                const tempoTotalMinutos = distanciaKm * 2.5; // Estimativa de 2.5 minutos por quilómetro
                
                const minutos = Math.floor(tempoTotalMinutos);
                const segundos = Math.round((tempoTotalMinutos - minutos) * 60);
                const textoTempo = `${minutos}m ${segundos}s`;

                // 2. Formatação do Caminho Compacto (A -> I -> D)
                const caminhoFormatado = resultado.caminho.map(letra => {
                    return nomesNos[letra] ? nomesNos[letra].split(' - ')[0] : letra;
                }).join(' <span style="color: #38bdf8;">➔</span> ');
                
                // 3. Atualização do Painel de Resultados Lateral (Mais compacto e elegante)
                painelResultado.innerHTML = `
                    <div style="display: flex; justify-content: space-between; margin-bottom: 4px; font-size: 13px;">
                        <span style="color: #9ca3af;">Distância:</span>
                        <span style="color: #00e676; font-weight: bold;">${distanciaKm.toFixed(2)} km</span>
                    </div>
                    <div style="display: flex; justify-content: space-between; margin-bottom: 8px; font-size: 13px;">
                        <span style="color: #9ca3af;">Tempo Est.:</span>
                        <span style="color: #38bdf8; font-weight: bold;">  ${textoTempo}</span>
                    </div>
                    <div style="background-color: #111827; padding: 6px 10px; border-radius: 4px; font-family: monospace; font-size: 12px; color: #e2e8f0; text-align: center; border: 1px solid rgba(255,255,255,0.02);">
                        ${caminhoFormatado}
                    </div>
                `;

                // 4. Acende dinamicamente as linhas que pertencem ao caminho ativo
                const svgContainer = document.querySelector('.linhas-svg');
                for (let i = 0; i < resultado.caminho.length - 1; i++) {
                    const cidadeA = resultado.caminho[i];
                    const cidadeB = resultado.caminho[i + 1];

                    let idLinha1 = `linha-${cidadeA}-${cidadeB}`;
                    let idLinha2 = `linha-${cidadeB}-${cidadeA}`;

                    let linhaSvg = document.getElementById(idLinha1) || document.getElementById(idLinha2);
                    
                    if (linhaSvg) {
                        linhaSvg.classList.add('linha-brilhante');
                        svgContainer.appendChild(linhaSvg); // Traz a linha ativa para o primeiro plano visual
                    }
                }
            }
        })
        .catch(error => {
            console.error("Erro na comunicação:", error);
            painelResultado.innerHTML = `<div style="color: #f87171; font-size: 11px;">❌ Erro ao conectar ao servidor Java.</div>`;
        });
}