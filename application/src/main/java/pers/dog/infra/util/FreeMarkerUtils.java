package pers.dog.infra.util;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Map;

import freemarker.cache.FileTemplateLoader;
import freemarker.cache.StringTemplateLoader;
import freemarker.cache.TemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.TemplateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FreeMarkerUtils {
    private static final String SINGLE_TEMPLATE = "singleTemplate";
    private static final Logger logger = LoggerFactory.getLogger(FreeMarkerUtils.class);
    private FreeMarkerUtils(){
    }

    public static String templateProcess(String templateContent, Map<String, Object> parameterMap) {
        return templateProcess(new StringTemplateLoader(), SINGLE_TEMPLATE, parameterMap);
    }

    public static String templateProcess(Path templateDir, String templateName, Map<String, Object> parameterMap) throws IOException {
        return templateProcess(new FileTemplateLoader(templateDir.toFile()), templateName, parameterMap);
    }

    private static String templateProcess(TemplateLoader templateLoader, String name, Map<String, Object> parameterMap) {
        Configuration configuration = new Configuration(Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS);
        configuration.setTemplateLoader(templateLoader);
        try {
            StringWriter stringWriter = new StringWriter();
            configuration.getTemplate(name, StandardCharsets.UTF_8.name()).process(parameterMap, stringWriter);
            return stringWriter.toString();
        } catch (IOException | TemplateException e) {
            logger.error("Error get or resolve template.", e);
            throw new IllegalStateException("FreeMarker Error.", e);
        }
    }
}
