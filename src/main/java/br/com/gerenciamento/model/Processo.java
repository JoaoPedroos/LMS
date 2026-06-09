package br.com.gerenciamento.model;


import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "processos")
public class Processo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero_cnj", nullable = false)
    private String numeroCnj;

    private String tribunal;
    private String status; // Ex: "Em Tramitação", "Concluso para Sentença"

    @Column(name = "ultima_movimentacao", length = 500)
    private String ultimaMovimentacao = "Processo Distribuído / Inicializado";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Empresa empresa;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime dataCadastro;

    @Column(nullable = false)
    private String numero;

    @Column(length = 500)
    private String descricao;

    public String getNumero() {
        return
                numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNumeroCnj() { return numeroCnj; }
    public void setNumeroCnj(String numeroCnj) { this.numeroCnj = numeroCnj; }
    public String getTribunal() { return tribunal; }
    public void setTribunal(String tribunal) { this.tribunal = tribunal; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getUltimaMovimentacao() { return ultimaMovimentacao; }
    public void setUltimaMovimentacao(String ultimaMovimentacao) { this.ultimaMovimentacao = ultimaMovimentacao; }
    public Cliente getCliente() { return cliente; }
    public void setCliente(Cliente cliente) { this.cliente = cliente; }
    public Empresa getEmpresa() { return empresa; }
    public void setEmpresa(Empresa empresa) { this.empresa = empresa; }
    public LocalDateTime getDataCadastro() { return dataCadastro; }
    public void setDataCadastro(LocalDateTime dataCadastro) { this.dataCadastro = dataCadastro; }
}