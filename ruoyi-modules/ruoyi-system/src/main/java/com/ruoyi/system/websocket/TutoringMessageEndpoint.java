package com.ruoyi.system.websocket;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import jakarta.websocket.CloseReason;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import com.ruoyi.common.security.service.TokenService;
import com.ruoyi.system.api.model.LoginUser;
import com.ruoyi.system.domain.TutoringMessage;
import com.ruoyi.system.service.TutoringService;

@ServerEndpoint("/tutoring/ws/messages/{matchId}/{token}")
public class TutoringMessageEndpoint
{
    private static final ConcurrentHashMap<Long, Set<TutoringMessageEndpoint>> CLIENTS = new ConcurrentHashMap<>();
    private static TutoringService tutoringService;
    private static TokenService tokenService;

    private Session session;
    private Long matchId;
    private Long userId;
    private String username;

    public static void configure(TutoringService service, TokenService tokens)
    {
        tutoringService = service;
        tokenService = tokens;
    }

    @OnOpen
    public void onOpen(Session session, @PathParam("matchId") Long matchId, @PathParam("token") String token)
    {
        this.session = session;
        try
        {
            LoginUser user = tokenService.getLoginUser(URLDecoder.decode(token, StandardCharsets.UTF_8));
            if (user == null)
            {
                close(CloseReason.CloseCodes.CANNOT_ACCEPT, "unauthorized");
                return;
            }
            tutoringService.getMessages(matchId, user.getUserid());
            this.matchId = matchId;
            this.userId = user.getUserid();
            this.username = user.getUsername();
            CLIENTS.computeIfAbsent(matchId, key -> new CopyOnWriteArraySet<>()).add(this);
        }
        catch (Exception e)
        {
            close(CloseReason.CloseCodes.CANNOT_ACCEPT, "forbidden");
        }
    }

    @OnMessage
    public void onMessage(String content)
    {
        if (content == null || content.trim().isEmpty() || matchId == null || userId == null)
        {
            return;
        }
        TutoringMessage message = new TutoringMessage();
        message.setContent(content.length() > 500 ? content.substring(0, 500) : content);
        tutoringService.addMessage(matchId, message, userId, username);
        broadcastMatch(matchId);
    }

    @OnClose
    public void onClose()
    {
        Set<TutoringMessageEndpoint> endpoints = CLIENTS.get(matchId);
        if (endpoints != null)
        {
            endpoints.remove(this);
        }
    }

    @OnError
    public void onError(Throwable error)
    {
        onClose();
    }

    public static void broadcastMatch(Long matchId)
    {
        Set<TutoringMessageEndpoint> endpoints = CLIENTS.get(matchId);
        if (endpoints == null)
        {
            return;
        }
        // ponytail: push is only an invalidation signal; clients reload history, switch to payloads if chat volume grows.
        String payload = "{\"type\":\"message\",\"matchId\":" + matchId + "}";
        for (TutoringMessageEndpoint endpoint : endpoints)
        {
            if (endpoint.session != null && endpoint.session.isOpen())
            {
                endpoint.session.getAsyncRemote().sendText(payload);
            }
        }
    }

    private void close(CloseReason.CloseCode code, String reason)
    {
        try
        {
            session.close(new CloseReason(code, reason));
        }
        catch (IOException ignored)
        {
        }
    }
}
