package org.apereo.cas.oidc.claims.mapping;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.oidc.claims.OidcEmailScopeAttributeReleasePolicy;
import org.apereo.cas.services.RegisteredServiceAttributeReleasePolicyContext;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.util.CollectionUtils;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OidcDefaultAttributeToScopeClaimMapperTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@TestPropertySource(properties = {
    "cas.authn.oidc.core.claims-map.email=mail",
    "cas.authn.oidc.core.claims-map.email_verified=mail_confirmed"
})
@Tag("OIDC")
public class OidcDefaultAttributeToScopeClaimMapperTests extends AbstractOidcTests {

    @Test
    public void verifyValueTypes() {
        val mapper = new OidcDefaultAttributeToScopeClaimMapper(
            CollectionUtils.wrap("active1", "status1", "active2", "status2",
                "active3", "status3"));
        val principal = RegisteredServiceTestUtils.getPrincipal("casuser",
            CollectionUtils.wrap("status1", "true",
                "status2", false, "status3", 1));
        var value = mapper.mapClaim("active1", principal, null).get(0);
        assertTrue(value instanceof Boolean);
        value = mapper.mapClaim("active2", principal, null).get(0);
        assertTrue(value instanceof Boolean);
        value = mapper.mapClaim("active3", principal, null).get(0);
        assertTrue(value instanceof Number);
    }

    @Test
    public void verifyOperation() {
        val mapper = new OidcDefaultAttributeToScopeClaimMapper(CollectionUtils.wrap("name", "givenName"));
        assertTrue(mapper.containsMappedAttribute("name"));
        assertEquals("givenName", mapper.getMappedAttribute("name"));
    }

    @Test
    public void verifyClaimMapOperation() {
        val policy = new OidcEmailScopeAttributeReleasePolicy();
        assertEquals(OidcConstants.StandardScopes.EMAIL.getScope(), policy.getScopeType());
        assertNotNull(policy.getAllowedAttributes());
        val principal = CoreAuthenticationTestUtils.getPrincipal(CollectionUtils.wrap("mail", List.of("cas@example.org"),
            "mail_confirmed", List.of("cas@example.org")));

        val releasePolicyContext = RegisteredServiceAttributeReleasePolicyContext.builder()
            .registeredService(CoreAuthenticationTestUtils.getRegisteredService())
            .service(CoreAuthenticationTestUtils.getService())
            .principal(principal)
            .build();
        val attrs = policy.getAttributes(releasePolicyContext);
        assertTrue(policy.getAllowedAttributes().stream().allMatch(attrs::containsKey));
        assertTrue(policy.determineRequestedAttributeDefinitions(releasePolicyContext).containsAll(policy.getAllowedAttributes()));
    }
}
