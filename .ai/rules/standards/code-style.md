# Code Style Guide
**Related**: [security-rules.md](./security-rules.md) | [../architecture/hexagonal.md](../architecture/hexagonal.md) | [../architecture/folder-structure.md](../architecture/folder-structure.md)

---

## Purpose

Define mandatory code style conventions for all backend services. While examples use Go as the primary reference language, these conventions apply equally to Java, Kotlin, Dart, and any other implementation language — adjusted for each language's idiomatic style. Consistent style ensures readability, maintainability, and team scalability.

---

## Naming Conventions

### Packages / Modules

- **Use short, lowercase names**: `proxy`, `router`, `auth`, `middleware`
- **Avoid generic names**: ~~`utils`~~, ~~`common`~~, ~~`base`~~, ~~`helpers`~~
- **Package name must match its directory**: `internal/api/handlers` → `package handlers`

```go
// ✅ Correct
package proxy
package ratelimiter
package circuitbreaker

// ❌ Incorrect
package gatewayUtils
package commonFunctions
package baseComponents
```

| Language | Convention | Example |
|----------|-----------|---------|
| Go | lowercase, single word | `package proxy` |
| Java/Kotlin | reverse domain, lowercase | `com.company.gateway.proxy` |
| Dart | snake_case | `library proxy;` |

---

### Variables & Functions

- **Exported / Public**: `PascalCase` (Go), `camelCase` (Java/Kotlin/Dart)
- **Unexported / Private**: `camelCase` (Go), `_camelCase` (Dart), `private` keyword (Java/Kotlin)
- **Boolean prefixes**: `is`, `has`, `allow`, `should`, `can`

```go
// ✅ Correct
var requestCount int
func NewProxy() *Proxy { ... }
func (p *Proxy) ServeHTTP() { ... }
var isHealthy bool

// ❌ Incorrect
var Request_Count int       // snake_case — not idiomatic Go
func new_proxy() *Proxy     // unexported constructor with snake_case
var healthy_flag bool       // unclear naming
```

---

### Constants & Enumerations

- **Exported constants**: `PascalCase` (Go), `SCREAMING_SNAKE_CASE` (Java/Kotlin)
- **Unexported constants**: `camelCase`
- **Enumerations**: Use `iota` (Go), `enum` (Java/Kotlin/Dart)

```go
// ✅ Correct
const (
    MaxConnections = 1000
    DefaultTimeout = 30 * time.Second
)

type BreakerState int
const (
    StateClosed BreakerState = iota
    StateOpen
    StateHalfOpen
)

// ❌ Incorrect
const MAX_CONNECTIONS = 1000  // C-style, not idiomatic Go
const default_timeout = 30    // snake_case
```

---

### Interfaces

- **Single-method interfaces**: Use `-er` suffix — `Logger`, `Cacher`, `Validator`
- **Multi-method interfaces**: Use descriptive nouns — `AuthProvider`, `Cache`, `RouteRepository`
- **No Hungarian notation**: ~~`IAuth`~~, ~~`AuthInterface`~~

```go
// ✅ Correct
type AuthProvider interface {
    Authenticate(token string) (*User, error)
}

type Cache interface {
    Get(key string) ([]byte, error)
    Set(key string, value []byte, ttl time.Duration) error
}

// ❌ Incorrect
type IAuth interface { ... }          // Hungarian prefix
type AuthInterface interface { ... }  // redundant suffix
```

---

## File Structure

### Order Within a File

1. Package/module declaration
2. Imports (grouped)
3. Constants
4. Global/package-level variables
5. Types / Structs / Classes
6. Constructor functions
7. Methods (on the primary type)
8. Helper / utility functions (private)

```go
package proxy

import (
    "context"
    "net/http"
    "time"

    "github.com/gorilla/mux"
)

const (
    defaultTimeout = 30 * time.Second
    maxRetries     = 3
)

var (
    defaultTransport = &http.Transport{
        MaxIdleConns: 100,
    }
)

type Proxy struct {
    client  *http.Client
    timeout time.Duration
}

func NewProxy(timeout time.Duration) *Proxy {
    return &Proxy{
        client:  &http.Client{Timeout: timeout},
        timeout: timeout,
    }
}

func (p *Proxy) ServeHTTP(w http.ResponseWriter, r *http.Request) {
    // implementation
}

func (p *Proxy) buildRequest(r *http.Request) (*http.Request, error) {
    // helper function
}
```

---

### Import Grouping

Imports **must** be grouped in this order, separated by blank lines:

1. **Standard library** — language built-in packages
2. **External dependencies** — third-party libraries
3. **Internal packages** — project-internal imports

```go
// ✅ Correct
import (
    "context"
    "fmt"
    "net/http"
    "time"

    "github.com/gorilla/mux"
    "github.com/prometheus/client_golang/prometheus"

    "github.com/company/gateway/internal/config"
    "github.com/company/gateway/internal/pkg/logger"
)

// ❌ Incorrect — ungrouped, relative import, deprecated package
import (
    "fmt"
    "../config"                    // relative import
    "github.com/gorilla/mux"
    "time"
    "io/ioutil"                    // deprecated
)
```

---

## Formatting & Style

### Line Length

- **Maximum 100 characters** (prefer 80–100)
- **Break long lines** after parentheses, commas, or dot-chains

```go
// ✅ Correct — chained calls broken for readability
err := db.
    WithContext(ctx).
    Where("status = ?", "active").
    Order("created_at DESC").
    Limit(10).
    Find(&users).Error

// ❌ Incorrect — single line exceeding limit
err := db.WithContext(ctx).Where("status = ?", "active").Order("created_at DESC").Limit(10).Find(&users).Error
```

---

### Comments & Documentation

- **Document all exported types and functions** with full-sentence comments
- **Use `TODO`, `FIXME`, `NOTE`** tags consistently
- **Avoid redundant comments** that restate the obvious

```go
// ✅ Correct
// Proxy handles forwarding requests to backend services.
// It implements http.Handler for direct use in route registration.
type Proxy struct {
    // client is a reusable HTTP client with configured timeouts
    client *http.Client
}

// NewProxy creates a new Proxy instance with the given configuration.
// Returns an error if the configuration is invalid.
func NewProxy(cfg Config) (*Proxy, error) {
    // TODO: validate full configuration
    // FIXME: default timeout too low for some use cases
    // NOTE: consider connection pooling for high load
}

// ❌ Incorrect — redundant, adds no value
type Proxy struct {  // proxy struct
    client *http.Client  // http client
}
```

---

### Error Handling

- **Use errors, not panics** (except in `init`/`main` for unrecoverable failures)
- **Error messages**: lowercase, no trailing punctuation
- **Wrap errors with context** using `fmt.Errorf("...: %w", err)`

```go
// ✅ Correct
if err != nil {
    return fmt.Errorf("failed to connect to backend: %w", err)
}

// ❌ Incorrect
if err != nil {
    panic(err)                                                // panic in library code
    return errors.New("Failed to connect to backend.")        // uppercase + period
}
```

| Language | Error Convention |
|----------|-----------------|
| Go | Return `error` as last return value; wrap with `%w` |
| Java/Kotlin | Throw typed exceptions; catch at boundaries |
| Dart | Return `Either<Failure, T>`; no raw throws crossing layers |

---

## Testing Conventions

### Test Naming

- **Pattern**: `Test{Type}_{Method}_{Scenario}`
- **Description**: Clear, readable scenario names

```go
// ✅ Correct
func TestProxy_ServeHTTP_Success(t *testing.T)
func TestProxy_ServeHTTP_Timeout(t *testing.T)
func TestRateLimiter_Allow_ExceededLimit(t *testing.T)

// ❌ Incorrect
func TestProxy(t *testing.T)     // too vague
func Test1(t *testing.T)         // meaningless name
```

---

### Table-Driven Tests

Use table-driven tests for multiple scenarios against the same function:

```go
func TestValidateToken(t *testing.T) {
    tests := []struct {
        name     string
        token    string
        wantErr  bool
        expected *User
    }{
        {"valid token", "valid.jwt.token", false, &User{ID: "123"}},
        {"expired token", "expired.jwt.token", true, nil},
        {"invalid signature", "invalid.jwt.token", true, nil},
    }

    for _, tt := range tests {
        t.Run(tt.name, func(t *testing.T) {
            // test implementation
        })
    }
}
```

| Language | Table Test Pattern |
|----------|-------------------|
| Go | `[]struct` + `t.Run()` loop |
| Java/Kotlin | `@ParameterizedTest` + `@MethodSource` |
| Dart | `group()` + multiple `test()` calls |

---

## Language-Specific Tooling

| Language | Formatter | Linter | Style Guide |
|----------|-----------|--------|-------------|
| Go | `gofmt` / `goimports` | `golangci-lint` | Effective Go |
| Java | Google Java Format | Checkstyle, SpotBugs | Google Java Style |
| Kotlin | `ktlint` | `detekt` | Kotlin Coding Conventions |
| Dart | `dart format` | `dart analyze` | Effective Dart |

> **Rule**: Auto-formatting must run on every save and as a CI gate. No unformatted code may be merged.

---

## Agent Verification Checklist

- [ ] All package/module names are short, lowercase, and avoid generic names (`utils`, `common`).
- [ ] All exported types and functions have documentation comments.
- [ ] Imports are grouped by: standard → external → internal, separated by blank lines.
- [ ] No line exceeds 100 characters without proper line-breaking.
- [ ] Error messages are lowercase with no trailing punctuation.
- [ ] No `panic()` in library code — only in `init()`/`main()` for unrecoverable failures.
- [ ] Boolean variables use prefixes: `is`, `has`, `allow`, `should`, `can`.
- [ ] Interfaces use `-er` suffix for single-method, descriptive noun for multi-method. No `I` prefix.
- [ ] Test functions follow `Test{Type}_{Method}_{Scenario}` naming pattern.
- [ ] Table-driven tests are used for multiple scenarios against the same function.
- [ ] Auto-formatter runs in CI pipeline and fails on unformatted code.
