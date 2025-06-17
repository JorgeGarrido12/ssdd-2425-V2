#!/bin/bash

USERNAME="usuario12"
EMAIL="usuario12@gmail.es"
PASSWORD="usuario12"
HOST="http://localhost:8180/u"
CONTENT_TYPE="Content-Type: application/json"
USER="User: $USERNAME"

# Función para generar tokens MD5 válidos según el enunciado
generate_token() {
    local full_url=$1
    local date=$2

    # Extraer solo el path (quita el protocolo, host y puerto)
    local path=$(echo "$full_url" | sed -E 's|^https?://[^/]+||')

    echo -n "$path$date$USER_TOKEN" | md5sum | awk '{print $1}'
}

RESPONSE=$(curl -s -X POST http://localhost:8080/Service/register \
     -H "Content-Type: application/json" \
     -d "{\"id\": \"$USERNAME\", \"name\": \"$USERNAME\", \"password\": \"$PASSWORD\", \"email\": \"$EMAIL\"}")

# Mostrar por si viene con error (útil para depuración)
echo ">>> Respuesta al registro:"
echo "$RESPONSE"

# Extraer token solo si contiene el campo .token
if echo "$RESPONSE" | jq -e '.token' > /dev/null 2>&1; then
    USER_TOKEN=$(echo "$RESPONSE" | jq -r '.token')
else
    echo "Error: no se pudo extraer el token. Respuesta inesperada del backend."
    exit 1
fi


# Función para hacer peticiones autenticadas
make_request() {
    local method=$1
    local endpoint=$2
    local data=$3

    local url="$HOST/$endpoint"
    local date=$(date -u +'%Y-%m-%dT%H:%M:%S.%3NZ')
    local token=$(generate_token "$url" "$date")
    local auth_token="Auth-Token: $token"

    curl -s -w "\nHTTP CODE: %{http_code}\n" -X $method "$url" \
        -H "$USER" \
        -H "Date: $date" \
        -H "$auth_token" \
        -H "$CONTENT_TYPE" \
        -d "$data"
}

# Ver perfil completo
echo -e "\n\n Perfil completo del usuario:\n"
make_request "GET" "$USERNAME" ""

# Crear diálogo
echo -e "\n\n Crear diálogo:\n"
make_request "POST" "$USERNAME/dialogue" '{"dialogueId": "test"}'

# Consultar diálogo
echo -e "\n\n Consultar diálogo:\n"
make_request "GET" "$USERNAME/dialogue/test" ""

# Enviar prompt
echo -e "\n\n Enviar prompt:\n"
diag="test"
timestamp=$(date +%s)
make_request "POST" "$USERNAME/dialogue/$diag/next" "{\"prompt\": \"¿Cuál es la capital de Italia?\"}"


# Finalizar diálogo
echo -e "\n\n Terminar diálogo:\n"
make_request "POST" "$USERNAME/dialogue/$diag/end" ""

# Ver logs
echo -e "\n\n Consultar logs:\n"
make_request "GET" "$USERNAME/logs" ""

# Borrar log
echo -e "\n\n Borrar log del diálogo:\n"
make_request "DELETE" "$USERNAME/logs/test" ""

# Confirmar borrado
echo -e "\n\n Consultar logs después del borrado:\n"
make_request "GET" "$USERNAME/logs" ""