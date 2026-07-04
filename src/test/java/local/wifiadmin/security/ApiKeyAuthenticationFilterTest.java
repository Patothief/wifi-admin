package local.wifiadmin.security;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

class ApiKeyAuthenticationFilterTest {

    @Test
    void skipsAuthenticationWhenSecurityIsDisabled() throws Exception {
        ApiKeyAuthenticationFilter filter = new ApiKeyAuthenticationFilter(
                new WifiAdminSecurityProperties(false, "X-API-Key", null)
        );
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/wifi-parameter/CPE_001");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
    }

    @Test
    void acceptsMatchingApiKey() throws Exception {
        ApiKeyAuthenticationFilter filter = new ApiKeyAuthenticationFilter(
                new WifiAdminSecurityProperties(true, "X-API-Key", "test-secret")
        );
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/wifi-parameter/CPE_001");
        request.addHeader("X-API-Key", "test-secret");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
    }

    @Test
    void rejectsMissingApiKey() throws Exception {
        ApiKeyAuthenticationFilter filter = new ApiKeyAuthenticationFilter(
                new WifiAdminSecurityProperties(true, "X-API-Key", "test-secret")
        );
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/wifi-parameter/CPE_001");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getContentType()).startsWith("application/json");
        assertThat(response.getContentAsString())
                .isEqualTo("{\"code\":\"UNAUTHORIZED\",\"message\":\"Missing or invalid API key\"}");
        verifyNoInteractions(chain);
    }
}
