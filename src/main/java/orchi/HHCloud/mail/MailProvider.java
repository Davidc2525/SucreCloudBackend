package orchi.HHCloud.mail;

import orchi.HHCloud.mail.Exceptions.SendEmailException;

public interface MailProvider {

    public void init();

    /**
     * Enviar mensaje por correo electronico
     *
     * @param from    Quien envia el mensaje, esta espesificado en el archivos de propiedades mail.mailmanager.admin
     * @param to      A quien va dirijido el mensaje
     * @param subject Asunto del mensaje
     * @param body    cuerpo del mensaje, contenido a enviar en el mensaje
     */
    public void sendEmail(String from, String to, String subject, String body) throws SendEmailException;
}
