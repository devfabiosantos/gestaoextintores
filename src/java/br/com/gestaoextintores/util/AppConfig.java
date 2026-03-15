package br.com.gestaoextintores.util;

public final class AppConfig {

    private AppConfig() {
    }

    public static String getAdminRemessaEmail() {
        return get("gestao.remessa.admin.email", "GESTAO_REMESSA_ADMIN_EMAIL");
    }

    public static String getMailHost() {
        return get("gestao.mail.host", "GESTAO_MAIL_HOST");
    }

    public static int getMailPort() {
        String valor = get("gestao.mail.port", "GESTAO_MAIL_PORT");
        if (valor == null || valor.trim().isEmpty()) {
            return 587;
        }
        return Integer.parseInt(valor.trim());
    }

    public static String getMailUsername() {
        return get("gestao.mail.username", "GESTAO_MAIL_USERNAME");
    }

    public static String getMailPassword() {
        return get("gestao.mail.password", "GESTAO_MAIL_PASSWORD");
    }

    public static String getMailFrom() {
        String from = get("gestao.mail.from", "GESTAO_MAIL_FROM");
        return from != null && !from.trim().isEmpty() ? from.trim() : getMailUsername();
    }

    public static boolean isMailAuthEnabled() {
        return getBoolean("gestao.mail.auth", "GESTAO_MAIL_AUTH", true);
    }

    public static boolean isMailStartTlsEnabled() {
        return getBoolean("gestao.mail.starttls", "GESTAO_MAIL_STARTTLS", true);
    }

    public static boolean isMailConfigured() {
        return notBlank(getMailHost())
                && notBlank(getMailFrom())
                && notBlank(getAdminRemessaEmail());
    }

    private static boolean getBoolean(String propertyName, String envName, boolean defaultValue) {
        String valor = get(propertyName, envName);
        if (valor == null || valor.trim().isEmpty()) {
            return defaultValue;
        }
        return Boolean.parseBoolean(valor.trim());
    }

    private static String get(String propertyName, String envName) {
        String valor = System.getProperty(propertyName);
        if (valor != null && !valor.trim().isEmpty()) {
            return valor.trim();
        }
        valor = System.getenv(envName);
        if (valor != null && !valor.trim().isEmpty()) {
            return valor.trim();
        }
        return null;
    }

    private static boolean notBlank(String valor) {
        return valor != null && !valor.trim().isEmpty();
    }
}
