package org.apereo.cas.oidc.claims.mapping;

import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.util.CollectionUtils;

import lombok.val;
import org.apache.commons.lang3.BooleanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This is {@link OidcAttributeToScopeClaimMapper}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public interface OidcAttributeToScopeClaimMapper {
    Logger LOGGER = LoggerFactory.getLogger(OidcAttributeToScopeClaimMapper.class);

    /**
     * The bean name of the default implementation.
     */
    String DEFAULT_BEAN_NAME = "oidcAttributeToScopeClaimMapper";

    /**
     * Gets mapped attribute.
     *
     * @param claim the claim
     * @return the mapped attribute
     */
    String getMappedAttribute(String claim);

    /**
     * Contains mapped attribute boolean.
     *
     * @param claim the claim
     * @return true/false
     */
    boolean containsMappedAttribute(String claim);

    /**
     * Map the claim to the mapped-name, or itself.
     *
     * @param claimName the claim name
     * @return the string
     */
    default String toMappedClaimName(final String claimName) {
        return containsMappedAttribute(claimName)
            ? getMappedAttribute(claimName)
            : claimName;
    }

    /**
     * Map claim and return values.
     *
     * @param claimName    the claim name
     * @param principal    the principal
     * @param defaultValue the default value
     * @return the list of values
     */
    default List<Object> mapClaim(final String claimName,
                                  final Principal principal,
                                  final Object defaultValue) {
        val attribute = toMappedClaimName(claimName);
        val attributeValues = principal.getAttributes().containsKey(attribute)
            ? principal.getAttributes().get(attribute)
            : defaultValue;

        LOGGER.trace("Handling claim [{}] with value(s) [{}]", attribute, attributeValues);
        return CollectionUtils.toCollection(attributeValues)
            .stream()
            .map(value -> {
                val valueContent = value.toString();
                if (value instanceof Boolean
                    || valueContent.equalsIgnoreCase(Boolean.FALSE.toString())
                    || valueContent.equalsIgnoreCase(Boolean.TRUE.toString())) {
                    return BooleanUtils.toBoolean(valueContent);
                }
                return value;
            })
            .collect(Collectors.toCollection(ArrayList::new));
    }
}
