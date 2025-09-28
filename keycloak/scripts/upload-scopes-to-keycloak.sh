#!/bin/bash

KEYCLOAK_URL="http://keycloak:8080"
REALM="master"
CLIENT_ID="admin-cli"
USERNAME="admin"
PASSWORD="admin"
SCOPES_DIR="keycloak/scopes"

if [ ! -d "$SCOPES_DIR" ]; then
    echo "Ошибка: Директория $SCOPES_DIR не существует"
    exit 1
fi

get_token() {
    curl -s \
        -d "client_id=$CLIENT_ID" \
        -d "username=$USERNAME" \
        -d "password=$PASSWORD" \
        -d "grant_type=password" \
        "${KEYCLOAK_URL}/realms/master/protocol/openid-connect/token" | jq -r '.access_token'
}

TOKEN=$(get_token)

if [ -z "$TOKEN" ]; then
    echo "Ошибка: Не удалось получить токен доступа"
    exit 1
fi

create_scope() {
    local scope_file="$1"
    echo "Загрузка скоупа из файла: $scope_file"

    local response=$(curl -s \
        -X POST \
        -H "Authorization: Bearer $TOKEN" \
        -H "Content-Type: application/json" \
        -d "@$scope_file" \
        "${KEYCLOAK_URL}/admin/realms/${REALM}/client-scopes")

    if [ $? -eq 0 ]; then
        echo "✓ Скоуп успешно загружен"
    else
        echo "✗ Ошибка при загрузке скоупа"
        echo "$response"
    fi
}

echo "Начинаем импорт скоупов из директории $SCOPES_DIR"
for file in "$SCOPES_DIR"/*.json; do
    if [ -f "$file" ]; then
        if grep -q "\"protocol\": \"openid-connect\"" "$file"; then
            echo "----------------------------------------"
            echo "Обработка файла: $file"
            create_scope "$file"
        else
            echo "Пропуск файла $file (не является OpenID Connect скоупом)"
        fi
    fi
done

echo "----------------------------------------"
echo "Импорт завершен"