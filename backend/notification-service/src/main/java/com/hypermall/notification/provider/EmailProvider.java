package com.hypermall.notification.provider;

import com.hypermall.notification.entity.Notification;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Component
@RequiredArgsConstructor
@Slf4j
public class EmailProvider implements NotificationProvider {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${spring.mail.username:noreply@hypermall.com}")
    private String fromEmail;

    @Value("${notification.email.enabled:false}")
    private boolean emailEnabled;

    @Override
    public boolean send(Notification notification) {
        if (!emailEnabled) {
            log.info("Email notifications disabled. Would send to: {}", notification.getRecipient());
            return true;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(notification.getRecipient());
            helper.setSubject(notification.getTitle());

            String htmlContent = buildHtmlContent(notification);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Email sent successfully to: {}", notification.getRecipient());
            return true;

        } catch (MessagingException e) {
            log.error("Failed to send email to {}: {}", notification.getRecipient(), e.getMessage());
            return false;
        }
    }

    private String buildHtmlContent(Notification notification) {
        Context context = new Context();
        context.setVariable("title", notification.getTitle());
        context.setVariable("content", notification.getContent());
        context.setVariable("type", notification.getType().name());

        try {
            return templateEngine.process("email/notification", context);
        } catch (Exception e) {
            log.warn("Email template not found, using plain content");
            return String.format("""
                <html>
                <body>
                    <h2>%s</h2>
                    <p>%s</p>
                    <hr>
                    <p style="color: gray; font-size: 12px;">HyperMall - Your Shopping Destination</p>
                </body>
                </html>
                """, notification.getTitle(), notification.getContent());
        }
    }

    @Override
    public String getProviderName() {
        return "EMAIL";
    }
}
