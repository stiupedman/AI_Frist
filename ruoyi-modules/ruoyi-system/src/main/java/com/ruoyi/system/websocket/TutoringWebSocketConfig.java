package com.ruoyi.system.websocket;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.websocket.DeploymentException;
import jakarta.websocket.server.ServerContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.context.annotation.Configuration;
import com.ruoyi.common.security.service.TokenService;
import com.ruoyi.system.service.TutoringService;

@Configuration
public class TutoringWebSocketConfig implements ServletContextInitializer
{
    @Autowired
    private TutoringService tutoringService;

    @Autowired
    private TokenService tokenService;

    @Override
    public void onStartup(ServletContext servletContext) throws ServletException
    {
        TutoringMessageEndpoint.configure(tutoringService, tokenService);
        ServerContainer container = (ServerContainer) servletContext.getAttribute(ServerContainer.class.getName());
        if (container == null)
        {
            return;
        }
        try
        {
            container.addEndpoint(TutoringMessageEndpoint.class);
        }
        catch (DeploymentException e)
        {
            throw new ServletException(e);
        }
    }
}
