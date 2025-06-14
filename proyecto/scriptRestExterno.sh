#!/bin/bash

USERNAME="usuario1"
EMAIL="usuario1@gmail.es"
PASSWORD="usuario1"
HOST="http://localhost:8180/Service/u"
CONTENT_TYPE="Content-Type: application/json"
USER="User: $USERNAME"

# Obtener el token desde el backend interno (donde se guarda el real)
get_real_token() {
    curl -s "http://localhost:8080/Service/u/$USERNAME" | jq -r '.token'
}

# Obtener el token real una vez registrado
USER_TOKEN=$(get_real_token)

# Función para generar tokens MD5 válidos según el enunciado
generate_token() {
    local url=$1
    local date=$2
    echo -n "$url$date$USER_TOKEN" | md5sum | awk '{print $1}'
}

# Función para hacer peticiones autenticadas
make_request() {
    local method=$1
    local endpoint=$2
    local data=$3

    local url="$HOST/$endpoint"
    local date=$(date -u +'%Y-%m-%dT%H:%M:%S.%3NZ')
    local token=$(generate_token "$url" "$date")
    local auth_token="Auth-Token: $token"

    curl -s -X $method "$url" \
        -H "$USER" \
        -H "Date: $date" \
        -H "$auth_token" \
        -H "$CONTENT_TYPE" \
        -d "$data"
}

# Crear usuario en backend interno (registro)
curl -s -X POST http://localhost:8080/Service/u/register \
     -H "Content-Type: application/json" \
     -d "{\"id\": \"$USERNAME\", \"name\": \"$USERNAME\", \"password\": \"$PASSWORD\", \"email\": \"$EMAIL\"}"

# Recuperar token
USER_TOKEN=$(get_real_token)

# Login externo
echo -e "\n\n Login usuario (REST externo)\n"
curl -s -X POST http://localhost:8180/Service/checkLogin \
     -H "Content-Type: application/json" \
     -d "{\"email\": \"$EMAIL\", \"password\": \"$PASSWORD\"}"

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
url="$HOST/$USERNAME/dialogue/$diag"
date=$(date -u +'%Y-%m-%dT%H:%M:%S.%3NZ')
token=$(generate_token "$url" "$date")
auth_token="Auth-Token: $token"
next_token=$(curl -s "$url" -H "$USER" -H "Date: $date" -H "$auth_token" | grep -oP 'nextUrl":".*?/next/\K[^"]+')

timestamp=$(date +%s)
make_request "POST" "$USERNAME/dialogue/$diag/next/$next_token" "{\"timestamp\": $timestamp, \"prompt\": \"Hola, ¿cómo estás?\"}"

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
