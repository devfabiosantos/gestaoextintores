package br.com.gestaoextintores.util;

import br.com.gestaoextintores.model.Extintor;
import br.com.gestaoextintores.model.Remessa;
import br.com.gestaoextintores.model.Setor;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class PdfRemessaGenerator {

    private static final SimpleDateFormat DATE_TIME_FORMAT = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
    private static final int MAX_LINES_PER_PAGE = 42;

    private PdfRemessaGenerator() {
    }

    public static byte[] gerarPdfRemessa(Remessa remessa, List<Extintor> extintores) {
        List<String> linhas = montarLinhas(remessa, extintores);
        List<byte[]> conteudos = new ArrayList<>();

        int inicio = 0;
        while (inicio < linhas.size()) {
            int fim = Math.min(inicio + MAX_LINES_PER_PAGE, linhas.size());
            List<String> pagina = linhas.subList(inicio, fim);
            conteudos.add(gerarConteudoPagina(pagina));
            inicio = fim;
        }

        return montarPdf(conteudos);
    }

    private static List<String> montarLinhas(Remessa remessa, List<Extintor> extintores) {
        List<String> linhas = new ArrayList<>();
        linhas.add("REMESSA PARA ORCAMENTO / RECARGA");
        linhas.add("");
        linhas.add("Remessa ID: " + remessa.getIdRemessa());
        linhas.add("Status: " + valorOuPadrao(remessa.getStatusRemessa()));
        linhas.add("Gerado em: " + (remessa.getDataCriacao() != null ? DATE_TIME_FORMAT.format(remessa.getDataCriacao()) : "-"));
        linhas.add("Filial: " + (remessa.getFilial() != null ? valorOuPadrao(remessa.getFilial().getNome()) : "ID " + remessa.getIdFilial()));
        linhas.add("Solicitante: " + (remessa.getTecnico() != null ? valorOuPadrao(remessa.getTecnico().getNome()) : "ID " + remessa.getIdUsuarioTecnico()));
        linhas.add("");
        linhas.add("RESUMO AGRUPADO");
        linhas.add("QTD | EQUIPAMENTO | CLASSE | CARGA");

        Map<String, Integer> agrupado = agruparExtintores(extintores);
        if (agrupado.isEmpty()) {
            linhas.add("0 | SEM ITENS | - | -");
        } else {
            for (Map.Entry<String, Integer> entry : agrupado.entrySet()) {
                linhas.add(entry.getValue() + " | " + entry.getKey());
            }
        }

        linhas.add("");
        linhas.add("LISTA DETALHADA");
        linhas.add("ID | CONTROLE | SETOR | REFERENCIA");
        if (extintores == null || extintores.isEmpty()) {
            linhas.add("- | - | - | -");
        } else {
            for (Extintor extintor : extintores) {
                Setor setor = extintor.getSetor();
                linhas.add(extintor.getIdExtintor()
                        + " | " + valorOuPadrao(extintor.getNumeroControle())
                        + " | " + (setor != null ? valorOuPadrao(setor.getNome()) : "-")
                        + " | " + valorOuPadrao(extintor.getReferenciaLocalizacao()));
            }
        }
        return linhas;
    }

    private static Map<String, Integer> agruparExtintores(List<Extintor> extintores) {
        Map<String, Integer> agrupado = new LinkedHashMap<>();
        if (extintores == null) {
            return agrupado;
        }

        for (Extintor extintor : extintores) {
            String chave = valorOuPadrao(extintor.getTipoEquipamento())
                    + " | " + valorOuPadrao(extintor.getClasseExtintora())
                    + " | " + valorOuPadrao(extintor.getCargaNominal());
            agrupado.put(chave, agrupado.getOrDefault(chave, 0) + 1);
        }
        return agrupado;
    }

    private static byte[] gerarConteudoPagina(List<String> linhas) {
        StringBuilder sb = new StringBuilder();
        sb.append("BT\n");
        sb.append("/F1 10 Tf\n");
        sb.append("50 790 Td\n");
        sb.append("14 TL\n");

        boolean primeiraLinha = true;
        for (String linha : linhas) {
            if (!primeiraLinha) {
                sb.append("T*\n");
            }
            primeiraLinha = false;
            sb.append("(").append(escaparPdf(normalizarAscii(linha))).append(") Tj\n");
        }
        sb.append("ET\n");
        return sb.toString().getBytes(StandardCharsets.US_ASCII);
    }

    private static byte[] montarPdf(List<byte[]> conteudosPaginas) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        List<Integer> offsets = new ArrayList<>();

        escreverLinha(out, "%PDF-1.4");

        int totalPaginas = conteudosPaginas.size();
        int pagesObjectId = 2 + (totalPaginas * 2);
        int fontObjectId = pagesObjectId + 1;
        int catalogObjectId = fontObjectId + 1;
        List<Integer> pageObjectIds = new ArrayList<>();

        for (int i = 0; i < totalPaginas; i++) {
            pageObjectIds.add(2 + (i * 2));
        }

        offsets.add(out.size());
        escreverObjeto(out, 1, "<< /Type /Catalog /Pages " + pagesObjectId + " 0 R >>");

        for (int i = 0; i < totalPaginas; i++) {
            int pageObjectId = pageObjectIds.get(i);
            int contentObjectId = pageObjectId + 1;
            offsets.add(out.size());
            escreverObjeto(out, pageObjectId,
                    "<< /Type /Page /Parent " + pagesObjectId + " 0 R /MediaBox [0 0 595 842] "
                    + "/Resources << /Font << /F1 " + fontObjectId + " 0 R >> >> "
                    + "/Contents " + contentObjectId + " 0 R >>");

            byte[] conteudo = conteudosPaginas.get(i);
            offsets.add(out.size());
            escreverCabecalhoObjeto(out, contentObjectId);
            escreverLinha(out, "<< /Length " + conteudo.length + " >>");
            escreverLinha(out, "stream");
            out.write(conteudo, 0, conteudo.length);
            escreverLinha(out, "");
            escreverLinha(out, "endstream");
            escreverLinha(out, "endobj");
        }

        offsets.add(out.size());
        StringBuilder kids = new StringBuilder();
        for (Integer pageObjectId : pageObjectIds) {
            kids.append(pageObjectId).append(" 0 R ");
        }
        escreverObjeto(out, pagesObjectId, "<< /Type /Pages /Kids [" + kids.toString().trim() + "] /Count " + totalPaginas + " >>");

        offsets.add(out.size());
        escreverObjeto(out, fontObjectId, "<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>");

        int xrefOffset = out.size();
        int totalObjetos = fontObjectId;
        escreverLinha(out, "xref");
        escreverLinha(out, "0 " + (totalObjetos + 1));
        escreverLinha(out, "0000000000 65535 f ");
        for (Integer offset : offsets) {
            escreverLinha(out, String.format(Locale.US, "%010d 00000 n ", offset));
        }
        escreverLinha(out, "trailer");
        escreverLinha(out, "<< /Size " + (totalObjetos + 1) + " /Root 1 0 R >>");
        escreverLinha(out, "startxref");
        escreverLinha(out, String.valueOf(xrefOffset));
        escreverLinha(out, "%%EOF");

        return out.toByteArray();
    }

    private static void escreverObjeto(ByteArrayOutputStream out, int objectId, String corpo) {
        escreverCabecalhoObjeto(out, objectId);
        escreverLinha(out, corpo);
        escreverLinha(out, "endobj");
    }

    private static void escreverCabecalhoObjeto(ByteArrayOutputStream out, int objectId) {
        escreverLinha(out, objectId + " 0 obj");
    }

    private static void escreverLinha(ByteArrayOutputStream out, String texto) {
        byte[] bytes = (texto + "\n").getBytes(StandardCharsets.US_ASCII);
        out.write(bytes, 0, bytes.length);
    }

    private static String valorOuPadrao(String valor) {
        return valor == null || valor.trim().isEmpty() ? "-" : valor.trim();
    }

    private static String normalizarAscii(String valor) {
        String semAcento = Normalizer.normalize(valor, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        StringBuilder resultado = new StringBuilder();
        for (char c : semAcento.toCharArray()) {
            if (c >= 32 && c <= 126) {
                resultado.append(c);
            } else {
                resultado.append('?');
            }
        }
        return resultado.toString();
    }

    private static String escaparPdf(String valor) {
        return valor.replace("\\", "\\\\").replace("(", "\\(").replace(")", "\\)");
    }
}
