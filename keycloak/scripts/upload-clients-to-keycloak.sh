#!/bin/bash

# Конфигурация
KEYCLOAK_URL="http://keycloak:8080"
REALM="master"
CLIENT_ID="admin-cli"
USERNAME="admin"
PASSWORD="admin"  # Изменено на admin согласно docker-compose.yml
CLIENTS_DIR="keycloak/clients"

# Проверка существования директории
if [ ! -d "$CLIENTS_DIR" ]; then
    echo "Ошибка: Директория $CLIENTS_DIR не существует"
    exit 1
fi

# Получение токена доступа
get_token() {
    curl -s \
        -d "client_id=$CLIENT_ID" \
        -d "username=$USERNAME" \
        -d "password=$PASSWORD" \
        -d "grant_type=password" \
        "${KEYCLOAK_URL}/realms/master/protocol/openid-connect/token" | jq -r '.access_token'
}

# Получаем токен
TOKEN=$(get_token)

if [ -z "$TOKEN" ]; then
    echo "Ошибка: Не удалось получить токен доступа"
    exit 1
fi

# Функция для создания клиента
create_client() {
    local client_file="$1"
    echo "Загрузка клиента из файла: $client_file"

    # Получаем clientId более безопасным способом
    local client_data=$(cat "$client_file")
    local client_id_value=$(echo "$client_data" | jq -r 'select(.clientId != null) | .clientId')

    if [ -z "$client_id_value" ]; then
        echo "⚠️ Предупреждение: Не удалось получить clientId из файла"
        return 1
    fi

    echo "Обработка клиента: $client_id_value"

    # Проверяем, существует ли клиент
    local existing_client=$(curl -s \
        -H "Authorization: Bearer $TOKEN" \
        "${KEYCLOAK_URL}/admin/realms/${REALM}/clients?clientId=$client_id_value")

    if echo "$existing_client" | jq -e '.[0]' >/dev/null; then
        local id=$(echo "$existing_client" | jq -r '.[0].id')
        echo "Обновление существующего клиента: $client_id_value (ID: $id)"

        # Обновляем существующего клиента
        local response=$(curl -s \
            -X PUT \
            -H "Authorization: Bearer $TOKEN" \
            -H "Content-Type: application/json" \
            -d "@$client_file" \
            "${KEYCLOAK_URL}/admin/realms/${REALM}/clients/$id")

        if [ $? -eq 0 ]; then
            echo "✓ Клиент успешно обновлен"
        else
            echo "✗ Ошибка при обновлении клиента"
            echo "$response"
        fi
    else
        # Создаем нового клиента
        local response=$(curl -s \
            -X POST \
            -H "Authorization: Bearer $TOKEN" \
            -H "Content-Type: application/json" \
            -d "@$client_file" \
            "${KEYCLOAK_URL}/admin/realms/${REALM}/clients")

        if [ $? -eq 0 ]; then
            echo "✓ Клиент успешно создан"
        else
            echo "✗ Ошибка при создании клиента"
            echo "$response"
        fi
    fi
}

# Обработка всех JSON файлов в указанной директории
echo "Начинаем импорт клиентов из директории $CLIENTS_DIR"
for file in "$CLIENTS_DIR"/*.json; do
    if [ -f "$file" ]; then
        echo "----------------------------------------"
        create_client "$file"
    fi
done

echo "----------------------------------------"
echo "Импорт клиентов завершен"