# Debugging Error 401 en Login

## Problema
Estás recibiendo un error 401 (Unauthorized) al intentar hacer login con credenciales correctas.

## Cambios aplicados

### 1. NetworkModule.kt
- ✅ Agregado `headersInterceptor` que fuerza los headers correctos:
  - `Content-Type: application/json`
  - `Accept: application/json`
- El interceptor se ejecuta ANTES del logging para asegurar que se vean en los logs

### 2. AuthRepository.kt
- ✅ Mejorado el manejo del error 401 para capturar y mostrar el mensaje del servidor
- Ahora verás "Credenciales incorrectas. Detalle: [mensaje del servidor]" si el servidor envía un body en la respuesta

## ¿Qué revisar ahora?

### Paso 1: Verifica los logs en Logcat
En Android Studio, filtra por `OkHttp` y busca:

```
--> POST http://10.0.2.2:8080/api/users/login
Content-Type: application/json
Accept: application/json

{"email":"tu@email.com","password":"tuPassword"}

<-- 401 http://10.0.2.2:8080/api/users/login
[mensaje del servidor]
```

### Paso 2: Verifica que el servidor espera estos campos
Tu app envía:
```json
{
  "email": "johndoe@email.com",
  "password": "1234567890"
}
```

El servidor podría estar esperando:
- `username` en lugar de `email`
- `user` en lugar de `email`
- Campos adicionales como `grant_type`, `client_id`, etc.

### Paso 3: Prueba el endpoint manualmente
Usa curl o Postman para probar el endpoint:

```bash
curl -X POST http://localhost:8080/api/users/login \
  -H "Content-Type: application/json" \
  -H "Accept: application/json" \
  -d '{"email":"johndoe@email.com","password":"1234567890"}'
```

Si esto devuelve 401, el problema está en el servidor o las credenciales.
Si devuelve 200, el problema está en cómo la app envía la petición.

### Paso 4: Revisa la configuración del servidor
Verifica que el servidor:
- ✅ Esté corriendo en el puerto 8080
- ✅ Tenga el endpoint `/api/users/login` (con `/api` en el path)
- ✅ Acepte POST requests
- ✅ Acepte `Content-Type: application/json`
- ✅ Las credenciales sean las correctas en la base de datos

## Posibles causas del 401

### 1. El servidor espera un campo diferente
**Solución**: Modifica `LoginRequest.kt` para que coincida con lo que espera el servidor.

Ejemplo si espera `username`:
```kotlin
@Serializable
data class LoginRequest(
    @SerialName("username")  // El servidor espera "username"
    val email: String,       // Pero en la app lo llamamos email
    val password: String
)
```

### 2. El endpoint está mal configurado
**Verifica**:
- Base URL: `http://10.0.2.2:8080/api/`
- Endpoint: `users/login`
- URL completa: `http://10.0.2.2:8080/api/users/login`

### 3. CORS o middleware de autenticación
El servidor podría tener:
- Middleware que requiere headers adicionales
- CORS mal configurado (aunque esto daría otro error)
- Requiere un token de API key en los headers

### 4. El usuario no existe o no está verificado
Algunos servidores devuelven 401 si:
- El usuario no existe en la BD
- El usuario existe pero no está verificado
- La contraseña está hasheada de forma diferente

## Próximos pasos

1. **Ejecuta la app** y observa los logs de OkHttp
2. **Copia el mensaje de error completo** que aparezca en el Logcat
3. **Prueba el mismo request con curl/Postman** para verificar si es problema del servidor o de la app
4. Si el mensaje de error del servidor da más pistas, compártelo para ajustar la implementación

