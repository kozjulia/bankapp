#!/bin/sh

# Параметры Keycloak
KEYCLOAK_URL="http://keycloak:8080"
KEYCLOAK_REALM="master"
ADMIN_USERNAME="admin"
ADMIN_PASSWORD="admin"

# Получаем токен администратора
get_admin_token() {
    curl -s \
        -d "client_id=admin-cli" \
        -d "username=$ADMIN_USERNAME" \
        -d "password=$ADMIN_PASSWORD" \
        -d "grant_type=password" \
        "${KEYCLOAK_URL}/realms/${KEYCLOAK_REALM}/protocol/openid-connect/token" | jq -r .access_token
}

# Получаем секрет для конкретного клиента
get_client_secret() {
    client_id=$1
    admin_token=$2

    # Получаем UUID клиента
    client_uuid=$(curl -s \
        -H "Authorization: Bearer $admin_token" \
        "${KEYCLOAK_URL}/admin/realms/${KEYCLOAK_REALM}/clients" | \
        jq -r ".[] | select(.clientId==\"${client_id}\") | .id")

    # Получаем секрет клиента
    if [ ! -z "$client_uuid" ]; then
        curl -s \
            -H "Authorization: Bearer $admin_token" \
            "${KEYCLOAK_URL}/admin/realms/${KEYCLOAK_REALM}/clients/${client_uuid}/client-secret" | \
            jq -r .value
    fi
}

# Получаем токен администратора
echo "Получение токена администратора..."
ADMIN_TOKEN=$(get_admin_token)

if [ -z "$ADMIN_TOKEN" ]; then
    echo "Ошибка: Не удалось получить токен администратора"
    exit 1
fi

# Обработка каждого клиента
process_client() {
    client_id=$1
    prefix=$2
    echo "Получение секрета для клиента $client_id..."

    secret=$(get_client_secret "$client_id" "$ADMIN_TOKEN")

    if [ ! -z "$secret" ]; then
        # Сохраняем секрет в Consul
        curl -X PUT -d "$secret" "http://consul-server:8500/v1/kv/secrets/${prefix}/client-secret"
        echo "Секрет для $client_id успешно сохранен в Consul"
    else
        echo "Ошибка: Не удалось получить секрет для клиента $client_id"
    fi
}

# Обработка всех клиентов
process_client "accounts-service-client" "ACCOUNTS_SERVICE"
process_client "cash-service-client" "CASH_SERVICE"
process_client "exchange-generator-service-client" "EXCHANGE_GENERATOR_SERVICE"
process_client "front-ui-client" "FRONT_UI_SERVICE"
process_client "front-ui" "FRONT_UI"
process_client "transfer-service-client" "TRANSFER_SERVICE"