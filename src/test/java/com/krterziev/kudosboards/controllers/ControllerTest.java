package com.krterziev.kudosboards.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.krterziev.kudosboards.security.WebSecurityConfig;
import com.krterziev.kudosboards.security.jwt.AuthEntryPointJwt;
import com.krterziev.kudosboards.security.jwt.JwtUtils;
import com.krterziev.kudosboards.security.services.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

@Import(WebSecurityConfig.class)
public class ControllerTest {

  @Autowired
  MockMvc mvc;

  @Autowired
  ObjectMapper objectMapper;

  @MockBean
  private UserDetailsServiceImpl userDetailsService;

  @MockBean
  private AuthEntryPointJwt unauthorizedHandler;

  @MockBean
  private JwtUtils jwtUtils;
}
