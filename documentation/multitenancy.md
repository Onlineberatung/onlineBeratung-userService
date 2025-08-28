#  Multitenancy Tenant Resolution

This module ensures that each incoming request is linked to the correct **tenant** before it is processed.  
It provides the foundation for running a **multi-tenant application**, where different customers (tenants) can securely share the same system.

---

## What It Does

- Every HTTP request is checked to figure out **which tenant it belongs to**.
- The tenant id is then stored for the duration of the request so services and repositories can use it automatically.
- After the request is complete, the tenant id is cleared to avoid any cross-request leaks.

---

## How Tenant Resolution Works

The system can figure out the tenant from different places, depending on the request type:

- **Headers** → some requests carry the tenant id in a custom HTTP header (when this microservice is called from another microservice).
- **Subdomains** → the tenant can also be part of the URL (e.g. `tenant1.example.com`).
- **Access Token** → authenticated users usually have the tenant encoded in their token.
- **Technical/Super Admin Users** → some special users belong to a technical tenant (`0L`) or have super admin privileges.

If a request is **authenticated**, we first try to resolve the tenant from the user’s access token or technical user configuration.  
If a request is **not authenticated**, we fall back to headers, subdomain, or single-domain setup.

---

## Security Checks

- If no tenant can be resolved → access is denied.
- If tenant information from different sources (e.g., token vs. subdomain) doesn’t match → access is denied.
- Technical or super admin tenants are treated specially and bypass some checks.

This ensures that a user from **one tenant cannot access another tenant’s data**.

---

## Configuration

```yaml
multitenancy:
  enabled: true  # turn multitenancy on or off

feature:
  multitenancy:
    with:
      single:
        domain:
          enabled: false # when true, only one domain is used and tenant checks are simplified
```

- You can fully disable multitenancy if not needed.
- In **single domain mode**, tenant resolution is simplified and only uses authentication.

---

## Request Flow

1. Request comes in.
2. The system decides if the user is authenticated.
3. Based on authentication, the correct resolver is used:
    - Authenticated → access token / technical user
    - Non-authenticated → header / subdomain
4. The tenant id is set for the request.
5. Application logic runs with tenant isolation.
6. Tenant id is cleared after the request finishes.

---

## Example Usage

In your service classes you don’t need to manually pass tenant ids around.  
You can always access the **current tenant** like this:

```java
Long tenantId = TenantContext.getCurrentTenant();
```

---

## Summary

- Automatically detects the correct tenant for each request.
- Supports multiple strategies (header, subdomain, token, technical user).
- Prevents unauthorized access across tenants.
- Simple configuration for single-domain or multi-domain setups.
- Keeps tenant id available throughout request handling.
