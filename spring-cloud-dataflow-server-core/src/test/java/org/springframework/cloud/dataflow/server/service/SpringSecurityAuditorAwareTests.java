/*
 * Copyright 2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.dataflow.server.service;

import java.time.Instant;
import java.util.HashMap;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import org.springframework.cloud.common.security.support.SecurityStateBean;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthentication;
import org.springframework.security.oauth2.server.resource.introspection.OAuth2IntrospectionAuthenticatedPrincipal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for org.springframework.cloud.dataflow.server.service.SpringSecurityAuditorAware
 * @author Corneil du Plessis
 */
public class SpringSecurityAuditorAwareTests {
	/**
	 * Test the fix for the case where the authentication doesn't contain a name but isn't anonymous it resulted in an NPE.
	 */
	@Test
	public void testNullPointerOnUnnamedPrincipal() {
		// given
		HashMap<String, Object> attributes = new HashMap<>();
		attributes.put("attr1", "value1");
		final OAuth2IntrospectionAuthenticatedPrincipal principal = new OAuth2IntrospectionAuthenticatedPrincipal(
				attributes,
				AuthorityUtils.NO_AUTHORITIES
		);
		final OAuth2AccessToken token = new OAuth2AccessToken(
				OAuth2AccessToken.TokenType.BEARER,
				"test",
				Instant.now(),
				Instant.now().plusSeconds(3600)
		);
		final BearerTokenAuthentication authentication = new BearerTokenAuthentication(
				principal,
				token,
				AuthorityUtils.NO_AUTHORITIES
		);
		final SecurityStateBean securityStateBean = new SecurityStateBean();
		securityStateBean.setAuthenticationEnabled(true);
		SecurityContextHolder.setContext(new SecurityContext() {
			@Override
			public Authentication getAuthentication() {
				return authentication;
			}

			@Override
			public void setAuthentication(Authentication authentication) {
			}
		});
		SpringSecurityAuditorAware auditorAware = new SpringSecurityAuditorAware(securityStateBean);
		// then
		assertThat(principal.getName()).isNull();
		Optional<String> auditor = auditorAware.getCurrentAuditor();
		assertThat(auditor.isPresent()).isFalse();
	}
}
