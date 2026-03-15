package br.com.gestaoextintores.util;

import br.com.gestaoextintores.model.Remessa;
import br.com.gestaoextintores.model.Usuario;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;

public class EmailService {

    private static final Logger LOGGER = Logger.getLogger(EmailService.class.getName());
    private static final SimpleDateFormat DATE_TIME_FORMAT = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

    public ResultadoEnvio enviarNovaRemessa(Remessa remessa, Usuario tecnicoSolicitante, byte[] pdfConteudo, String nomeArquivo)
            throws MessagingException {

        if (!AppConfig.isMailConfigured()) {
            return ResultadoEnvio.naoConfigurado("Envio de e-mail não configurado.");
        }

        Set<String> destinatarios = new LinkedHashSet<>();
        destinatarios.add(AppConfig.getAdminRemessaEmail());

        if (tecnicoSolicitante != null && tecnicoSolicitante.getEmail() != null && !tecnicoSolicitante.getEmail().trim().isEmpty()) {
            destinatarios.add(tecnicoSolicitante.getEmail().trim());
        }

        List<String> destinatariosValidos = new ArrayList<>();
        for (String destinatario : destinatarios) {
            if (destinatario != null && !destinatario.trim().isEmpty()) {
                destinatariosValidos.add(destinatario.trim());
            }
        }

        if (destinatariosValidos.isEmpty()) {
            return ResultadoEnvio.naoConfigurado("Nenhum destinatário configurado para envio.");
        }

        Session session = criarSession();
        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress(AppConfig.getMailFrom()));
        for (String destinatario : destinatariosValidos) {
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(destinatario));
        }
        message.setSubject("Nova remessa registrada - ID " + remessa.getIdRemessa(), StandardCharsets.UTF_8.name());

        MimeBodyPart texto = new MimeBodyPart();
        texto.setText(montarCorpo(remessa, tecnicoSolicitante), StandardCharsets.UTF_8.name());

        MimeBodyPart anexo = new MimeBodyPart();
        ByteArrayDataSource dataSource = new ByteArrayDataSource(pdfConteudo, "application/pdf");
        anexo.setDataHandler(new javax.activation.DataHandler(dataSource));
        anexo.setFileName(nomeArquivo);

        MimeMultipart multipart = new MimeMultipart();
        multipart.addBodyPart(texto);
        multipart.addBodyPart(anexo);
        message.setContent(multipart);

        Transport.send(message);
        LOGGER.log(Level.INFO, "E-mail da remessa ID {0} enviado para {1}.",
                new Object[]{remessa.getIdRemessa(), destinatariosValidos});
        return ResultadoEnvio.enviado(destinatariosValidos);
    }

    private Session criarSession() {
        Properties props = new Properties();
        props.put("mail.smtp.host", AppConfig.getMailHost());
        props.put("mail.smtp.port", String.valueOf(AppConfig.getMailPort()));
        props.put("mail.smtp.auth", String.valueOf(AppConfig.isMailAuthEnabled()));
        props.put("mail.smtp.starttls.enable", String.valueOf(AppConfig.isMailStartTlsEnabled()));

        if (AppConfig.isMailAuthEnabled()) {
            return Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(AppConfig.getMailUsername(), AppConfig.getMailPassword());
                }
            });
        }
        return Session.getInstance(props);
    }

    private String montarCorpo(Remessa remessa, Usuario tecnicoSolicitante) {
        StringBuilder sb = new StringBuilder();
        sb.append("Uma nova remessa foi registrada no sistema.\n\n");
        sb.append("ID da remessa: ").append(remessa.getIdRemessa()).append('\n');
        sb.append("Filial: ").append(remessa.getFilial() != null ? remessa.getFilial().getNome() : "ID " + remessa.getIdFilial()).append('\n');
        sb.append("Solicitante: ").append(tecnicoSolicitante != null ? tecnicoSolicitante.getNome() : "ID " + remessa.getIdUsuarioTecnico()).append('\n');
        sb.append("Data de criação: ").append(remessa.getDataCriacao() != null ? DATE_TIME_FORMAT.format(remessa.getDataCriacao()) : "-").append('\n');
        sb.append("Status: ").append(remessa.getStatusRemessa()).append("\n\n");
        sb.append("O PDF da remessa segue em anexo.");
        return sb.toString();
    }

    public static final class ResultadoEnvio {
        private final boolean enviado;
        private final String mensagem;
        private final List<String> destinatarios;

        private ResultadoEnvio(boolean enviado, String mensagem, List<String> destinatarios) {
            this.enviado = enviado;
            this.mensagem = mensagem;
            this.destinatarios = destinatarios;
        }

        public static ResultadoEnvio enviado(List<String> destinatarios) {
            return new ResultadoEnvio(true, null, destinatarios);
        }

        public static ResultadoEnvio naoConfigurado(String mensagem) {
            return new ResultadoEnvio(false, mensagem, new ArrayList<String>());
        }

        public boolean isEnviado() {
            return enviado;
        }

        public String getMensagem() {
            return mensagem;
        }

        public List<String> getDestinatarios() {
            return destinatarios;
        }
    }
}
