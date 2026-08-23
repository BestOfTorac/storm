package it.uniroma2.isw2.storm.testing.defaulthttpcredentialsplugin.bb;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import org.apache.storm.security.auth.DefaultHttpCredentialsPlugin;
import org.apache.storm.security.auth.ReqContext;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DefaultHttpCredentialsPluginBBTest {

    /*
     * DHCP-BB-01
     * prepare is documented as a no-op.
     */
    @Test
    void bb01PreparePreservesOrdinaryConfiguration() {
        DefaultHttpCredentialsPlugin plugin =
                new DefaultHttpCredentialsPlugin();

        Map<String, Object> config = new HashMap<>();
        config.put("worker.port", 6700);
        config.put("environment", "test");

        Map<String, Object> before =
                new HashMap<>(config);

        plugin.prepare(config);

        assertEquals(before, config);
    }

    /*
     * DHCP-BB-02
     * A usable request principal has precedence over remoteUser.
     */
    @Test
    void bb02PrincipalHasPrecedenceOverRemoteUser() {
        DefaultHttpCredentialsPlugin plugin =
                new DefaultHttpCredentialsPlugin();

        HttpServletRequest request =
                mock(HttpServletRequest.class);

        Principal principal =
                () -> "alice";

        when(request.getUserPrincipal())
                .thenReturn(principal);

        when(request.getRemoteUser())
                .thenReturn("bob");

        assertEquals(
                "alice",
                plugin.getUserName(request)
        );
    }

    /*
     * DHCP-BB-03
     * remoteUser supplies the authenticated identity when the
     * request principal is absent.
     */
    @Test
    void bb03RemoteUserIsUsedWhenPrincipalIsAbsent() {
        DefaultHttpCredentialsPlugin plugin =
                new DefaultHttpCredentialsPlugin();

        HttpServletRequest request =
                mock(HttpServletRequest.class);

        when(request.getUserPrincipal())
                .thenReturn(null);

        when(request.getRemoteUser())
                .thenReturn("bob");

        assertEquals(
                "bob",
                plugin.getUserName(request)
        );
    }

    /*
     * DHCP-BB-04
     * Empty principal name is the selected boundary between
     * usable and unusable principal identity.
     */
    @Test
    void bb04EmptyPrincipalNameFallsBackToRemoteUser() {
        DefaultHttpCredentialsPlugin plugin =
                new DefaultHttpCredentialsPlugin();

        HttpServletRequest request =
                mock(HttpServletRequest.class);

        Principal emptyPrincipal =
                () -> "";

        when(request.getUserPrincipal())
                .thenReturn(emptyPrincipal);

        when(request.getRemoteUser())
                .thenReturn("bob");

        assertEquals(
                "bob",
                plugin.getUserName(request)
        );
    }

    /*
     * DHCP-BB-05
     * No usable identity is available from either source.
     */
    @Test
    void bb05NoAuthenticatedIdentityReturnsNull() {
        DefaultHttpCredentialsPlugin plugin =
                new DefaultHttpCredentialsPlugin();

        HttpServletRequest request =
                mock(HttpServletRequest.class);

        when(request.getUserPrincipal())
                .thenReturn(null);

        when(request.getRemoteUser())
                .thenReturn(null);

        assertNull(
                plugin.getUserName(request)
        );
    }

    /*
     * DHCP-BB-06
     * An authenticated user without doAsUser becomes the effective
     * subject principal and no impersonation is established.
     */
    @Test
    void bb06AuthenticatedUserPopulatesSubjectWithoutImpersonation() {
        DefaultHttpCredentialsPlugin plugin =
                new DefaultHttpCredentialsPlugin();

        HttpServletRequest request =
                mock(HttpServletRequest.class);

        Principal principal =
                () -> "alice";

        when(request.getUserPrincipal())
                .thenReturn(principal);

        when(request.getHeader("doAsUser"))
                .thenReturn(null);

        when(request.getParameter("doAsUser"))
                .thenReturn(null);

        ReqContext context =
                new ReqContext();

        ReqContext returned =
                plugin.populateContext(
                        context,
                        request
                );

        assertSame(context, returned);
        assertEquals(
                "alice",
                context.principal().getName()
        );
        assertNull(context.realPrincipal());
        assertFalse(context.isImpersonating());
    }

    /*
     * DHCP-BB-07
     * An unauthenticated request without impersonation produces a
     * subject with no effective principal.
     */
    @Test
    void bb07UnauthenticatedRequestProducesNoEffectivePrincipal() {
        DefaultHttpCredentialsPlugin plugin =
                new DefaultHttpCredentialsPlugin();

        HttpServletRequest request =
                mock(HttpServletRequest.class);

        when(request.getUserPrincipal())
                .thenReturn(null);

        when(request.getRemoteUser())
                .thenReturn(null);

        when(request.getHeader("doAsUser"))
                .thenReturn(null);

        when(request.getParameter("doAsUser"))
                .thenReturn(null);

        ReqContext context =
                new ReqContext();

        ReqContext returned =
                plugin.populateContext(
                        context,
                        request
                );

        assertSame(context, returned);
        assertTrue(context.subject() != null);
        assertNull(context.principal());
        assertNull(context.realPrincipal());
        assertFalse(context.isImpersonating());
    }

    /*
     * DHCP-BB-08
     * doAsUser supplied through the header changes the effective
     * principal while preserving the authenticated real principal.
     */
    @Test
    void bb08DoAsUserHeaderEstablishesImpersonation() {
        DefaultHttpCredentialsPlugin plugin =
                new DefaultHttpCredentialsPlugin();

        HttpServletRequest request =
                mock(HttpServletRequest.class);

        Principal principal =
                () -> "alice";

        when(request.getUserPrincipal())
                .thenReturn(principal);

        when(request.getHeader("doAsUser"))
                .thenReturn("bob");

        ReqContext context =
                new ReqContext();

        ReqContext returned =
                plugin.populateContext(
                        context,
                        request
                );

        assertSame(context, returned);
        assertEquals(
                "bob",
                context.principal().getName()
        );
        assertEquals(
                "alice",
                context.realPrincipal().getName()
        );
        assertTrue(context.isImpersonating());
    }

    /*
     * DHCP-BB-09
     * When the header is absent, the doAsUser request parameter
     * establishes impersonation.
     */
    @Test
    void bb09DoAsUserParameterEstablishesImpersonation() {
        DefaultHttpCredentialsPlugin plugin =
                new DefaultHttpCredentialsPlugin();

        HttpServletRequest request =
                mock(HttpServletRequest.class);

        Principal principal =
                () -> "alice";

        when(request.getUserPrincipal())
                .thenReturn(principal);

        when(request.getHeader("doAsUser"))
                .thenReturn(null);

        when(request.getParameter("doAsUser"))
                .thenReturn("carol");

        ReqContext context =
                new ReqContext();

        ReqContext returned =
                plugin.populateContext(
                        context,
                        request
                );

        assertSame(context, returned);
        assertEquals(
                "carol",
                context.principal().getName()
        );
        assertEquals(
                "alice",
                context.realPrincipal().getName()
        );
        assertTrue(context.isImpersonating());
    }

    /*
     * DHCP-BB-10
     * Header and parameter are both populated with different values.
     * The externally observable effective user is the header value.
     */
    @Test
    void bb10DoAsUserHeaderHasPrecedenceOverParameter() {
        DefaultHttpCredentialsPlugin plugin =
                new DefaultHttpCredentialsPlugin();

        HttpServletRequest request =
                mock(HttpServletRequest.class);

        Principal principal =
                () -> "alice";

        when(request.getUserPrincipal())
                .thenReturn(principal);

        when(request.getHeader("doAsUser"))
                .thenReturn("bob");

        when(request.getParameter("doAsUser"))
                .thenReturn("carol");

        ReqContext context =
                new ReqContext();

        ReqContext returned =
                plugin.populateContext(
                        context,
                        request
                );

        assertSame(context, returned);
        assertEquals(
                "bob",
                context.principal().getName()
        );
        assertEquals(
                "alice",
                context.realPrincipal().getName()
        );
        assertTrue(context.isImpersonating());
    }
}
