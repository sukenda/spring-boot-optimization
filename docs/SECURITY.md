# Security Guide

## Security Best Practices

### JWT Security

#### Secret Key

**IMPORTANT**: Use a strong secret key in production:

```bash
# Generate secure secret (32+ characters)
openssl rand -base64 32
```

Set via environment variable:

```bash
export JWT_SECRET=your-very-secure-secret-key-minimum-256-bits
```

#### Token Expiration

Set appropriate expiration:

```bash
export JWT_EXPIRATION=86400000  # 24 hours
```

For sensitive applications, use shorter expiration.

### Password Security

#### Hashing

Passwords are hashed using BCrypt:

- Automatic salt generation
- Configurable cost factor
- One-way hashing

#### Password Policy

Enforce strong passwords:

- Minimum 6 characters (configurable)
- Consider requiring uppercase, lowercase, numbers, symbols
- Implement password history (optional)

### Database Security

#### Connection Security

Use SSL in production:

```yaml
url: r2dbc:mysql://localhost:3306/proddb?useSSL=true
```

#### User Permissions

Create dedicated database user:

```sql
CREATE
USER 'prod_user'@'localhost' IDENTIFIED BY 'secure_password';
GRANT
SELECT,
INSERT
,
UPDATE,
DELETE
ON proddb.* TO 'prod_user'@'localhost';
FLUSH
PRIVILEGES;
```

### Application Security

#### Environment Variables

Never commit secrets:

- Use environment variables
- Use secrets management (AWS Secrets Manager, HashiCorp Vault)
- Never log sensitive data

#### Input Validation

All inputs are validated:

- DTO validation with Jakarta Validation
- SQL injection prevention (R2DBC parameterized queries)
- XSS prevention (Spring default)

#### Error Handling

Don't expose sensitive information:

- Generic error messages in production
- Detailed errors only in development
- Log errors securely

### Network Security

#### Firewall

Configure firewall:

```bash
sudo ufw allow 22/tcp    # SSH
sudo ufw allow 8080/tcp  # Application
sudo ufw enable
```

#### Reverse Proxy

Use Nginx/Apache with SSL:

- Terminate SSL at reverse proxy
- Hide application behind proxy
- Rate limiting at proxy level

### Security Headers

Consider adding security headers:

- X-Content-Type-Options
- X-Frame-Options
- X-XSS-Protection
- Strict-Transport-Security

## Security Checklist

### Production Deployment

- [ ] Change JWT secret
- [ ] Use SSL/TLS
- [ ] Configure firewall
- [ ] Use dedicated database user
- [ ] Enable database SSL
- [ ] Set strong passwords
- [ ] Disable debug endpoints
- [ ] Configure proper logging
- [ ] Set up monitoring
- [ ] Regular security updates
- [ ] Backup strategy
- [ ] Access control

## Security Monitoring

### Log Monitoring

Monitor logs for:

- Failed login attempts
- Unauthorized access attempts
- Unusual patterns
- Error spikes

### Health Checks

Regular health checks:

```bash
curl http://localhost:8080/actuator/health
```

## Compliance

### Data Protection

- Implement soft delete for data recovery
- Regular backups
- Data encryption at rest (database level)
- Data encryption in transit (SSL/TLS)

### Audit Logging

Consider implementing:

- User action logging
- Access logging
- Change tracking
- Audit trail

## Resources

- [OWASP Top 10](https://owasp.org/www-project-top-ten/)
- [Spring Security Documentation](https://docs.spring.io/spring-security/reference/index.html)
- [JWT Best Practices](https://datatracker.ietf.org/doc/html/rfc8725)

