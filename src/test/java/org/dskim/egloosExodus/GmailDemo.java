package org.dskim.egloosExodus;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

public class GmailDemo {
    public static void main(String[] args) {

        final String username = "egloos.exodus@gmail.com";
        final String password = "?#H1xkUc6V";

        Properties prop = new Properties();
        prop.put("mail.smtp.host", "smtp.gmail.com");
        prop.put("mail.smtp.port", "587");
        prop.put("mail.smtp.auth", "true");
        prop.put("mail.smtp.starttls.enable", "true"); //TLS

        Session session = Session.getInstance(prop,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(username, password);
                    }
                });

        try {

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress("from@gmail.com"));
            message.setRecipients(
                    Message.RecipientType.TO,
                    InternetAddress.parse("bluedskim@gmail.com")
            );
            message.setSubject("Testing Gmail TLS");
            message.setContent("<a href='http://google.com'>google.com</a>", "text/html; charset=utf-8");
            //message.setText("gmail demo" + "<br/> <a href='http://google.com'>google.com</a>");

            Transport.send(message);

            System.out.println("Done");

        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
}
