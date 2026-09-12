package com.drey.eventsSports.shared.constants;

public class SecurityConstants {
    public static final String JWT_SECRET_DEFAULT = "3cfa76ef14937c1c0ea519f8fc057a80fcd04a74ac2b9bd91000223f108f3e58";
    public static final long JWT_EXPIRATION = 86400000; // 1 day
    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String HEADER_STRING = "Authorization";
    public static final String CLAIM_SCOPE_ID = "scope_id";
    public static final String CLAIM_ROLE = "role";
}
