package com.network.shopping.service.utils;

import com.network.shopping.dto.MailRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
public class MailContentBuilder {
    public static final String ACTIVATION_URL = "activationUrl";
    private final TemplateEngine templateEngine;

    @Autowired
    public MailContentBuilder(final TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    public String build(final MailRequest request, final String template) {
        final Context context = new Context();
        context.setVariables(request.getProps());
        return this.templateEngine.process(template, context);
    }

    public String build(final MailRequest request, final String template, final String withUrl) {
        final Context context = new Context();
        context.setVariables(request.getProps());
        context.setVariable(ACTIVATION_URL, withUrl);
        return this.templateEngine.process(template, context);
    }

}

