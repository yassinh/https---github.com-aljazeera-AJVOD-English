package com.brightcove.consulting;

import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

import com.brightcove.consulting.alj.models.Channel;
import com.brightcove.consulting.alj.models.ModelConstants;
import com.brightcove.consulting.alj.models.NavRepository;

@Component
public class ExceptionResolver implements HandlerExceptionResolver, Ordered {

    @Autowired
    protected String webRoot;

    @Autowired
    protected String webResourceRoot;   

    @Autowired
    protected NavRepository navRepository;
    
    @Override
    public int getOrder() {
        return LOWEST_PRECEDENCE;
    }

    @Override
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ModelAndView resolveException(HttpServletRequest request,
            HttpServletResponse response, Object handler, Exception ex) {
        ModelAndView mv = new ModelAndView();
        Map<String, Object> model = mv.getModel();

        model.put("webRoot", webRoot );

        model.put("webResourceRoot", webResourceRoot );
        
        model.put("channels", navRepository.getChannels() );
        
        model.put("pageName", "error" );

        model.put("exception", ex);
        
        response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());

        if (ex instanceof HttpClientErrorException) {
            HttpClientErrorException hcee = (HttpClientErrorException)ex;
            HttpStatus status = hcee.getStatusCode();
            if (status == HttpStatus.NOT_FOUND) {
                mv.setViewName("404");
                return mv;
            }
        }

        mv.setViewName("500");
        return mv;
    }

}
