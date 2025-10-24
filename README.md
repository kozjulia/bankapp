## Микросервисное приложение «Банк»

_______

Банк **умеет** делать следующее:

1. Регистрироваться в системе по логину и паролю (заводить аккаунт);
2. Добавлять счета в различных валютах;
3. Класть виртуальные деньги на счёт и снимать их;
4. Переводить деньги между своими счетами с учётом конвертации в различные валюты;
5. Переводить деньги на другой счёт с учётом конвертации в различные валюты.

-------

Приложение состоит из следующих микросервисов:

- Front (фронт);
- Accounts (аккаунты со счетами);
- Cash (обналичивание денег);
- Transfer (перевод между счетами одного или двух аккаунтов);
- Exchange (конвертация валют);
- Exchange Generator (генератор курсов валют);
- Blocker (блокировщик подозрительных операций);
- Notifications (уведомления).

-------

Приложение написано на **Java 21**, использует **Spring Boot 3**, **Gradle**, **Thymeleaf**, **WebFlux**, **Flyway**,
**Spring Security**,
**JUnit 5**, **Mockito**, **Testcontainers**, **Docker**, API соответствует **REST**, данные хранятся в БД **PostgreSQL
**, тесты выполняются в **PostgreSQL**.  
Тестовое покрытие кода - 24% строк кода.

-------

Для запуска приложения:

```gradle
gradle clean build
```

```command
docker-compose up
```

В директории build/libs проекта появится jar-архив сервиса

-------

### Как запускать в Minikube

1. Установите Minikube:
   https://kubernetes.io/ru/docs/tasks/tools/install-minikube/

2. Инициализируйте

```bush
minikube start --driver=docker
```

3. Добавьте ингресс, чтобы была маршрутизация запросов к нашим сервисам

```bush
minikube addons enable ingress
```

Проверьте, что появился ingress-controller

```bush
kubectl get pods -n ingress-nginx
```

4. Соберите docker образы

```bush
eval $(minikube docker-env)
docker-compose build accounts blocker cash exchange-generator exchange front notification transfer
```

5. Обновите зависимости helm чарта

```bush
cd .deployment
helm dependency update .
```

6. Установите keycloak

```bush
cd ..
kubectl apply -f keycloak-deployment.yaml
## для удаления
kubectl delete -f keycloak-deployment.yaml
```

7. Добавьте записи в `/etc/hosts`

```bash
sudo nano /etc/hosts
```

Добавьте:

```text
127.0.0.1 keycloak.test.local
127.0.0.1 front.test.local
```

7. Используйте `minikube tunnel`

8. Импортируйте скоупы и клиенты для keycloak

```bush
cd .docker/keycloak-config-loader/scripts
sh upload-scopes-to-keycloak.sh
sh upload-clients-to-keycloak.sh
```

9. Задеплойте сервисы с помощью helm чартов

```bush
cd .deployment
# for install
helm install bankapp .
# for update
helm upgrade bankapp .
# for delete
helm uninstall bankapp
```

10. Перейдите по ссылке `http://front.test.local`

-------

### Как запускать helm test

1. перейти в папку `.deployment` и выполнить комманды:

```bush
helm lint . 

helm install --dry-run bankapp . 

helm test bankapp 
```

### Как запускать Jenkins

1. Установите зависимости

Для запуска проекта вам понадобятся:

- [Docker Desktop](https://www.docker.com/products/docker-desktop/)
- Включённый Kubernetes в Docker Desktop (настройка → Kubernetes → Enable Kubernetes)
- Установленный [Git](https://git-scm.com/)

> Устанавливать Helm или kubectl локально не нужно — они уже установлены в Jenkins-контейнере.

2. Создайте файл `jenkins_kubeconfig.yaml`

Jenkins будет использовать этот файл для доступа к Kubernetes.

Выполните в терминале:

```bash
cp ~/.kube/config jenkins_kubeconfig.yaml
```

Затем отредактируйте файл:

**Замените `server` на:**

```yaml
server: https://host.docker.internal:6443
```

**Добавьте:**

```yaml
insecure-skip-tls-verify: true
```

Это нужно, чтобы Jenkins внутри контейнера смог обратиться к вашему локальному кластеру и проигнорировал самоподписанные
сертификаты.

3. Установите Ingress Controller в кластер

**Ingress Controller** — это компонент, который позволяет обращаться к сервисам Kubernetes через удобные HTTP-домены (
например, `http://order.test.local`).

Мы используем `ingress-nginx`. Установите его в кластер:

```bash
helm repo add ingress-nginx https://kubernetes.github.io/ingress-nginx
helm repo update

helm upgrade --install ingress-nginx ingress-nginx/ingress-nginx   --namespace ingress-nginx --create-namespace
```

4. Создайте `.env` файл

Создайте файл `.env` в корне проекта:

```env
# Путь до локального kubeconfig-файла
KUBECONFIG_PATH=/Users/username/.kube/jenkins_kubeconfig.yaml

# Параметры для GHCR
GITHUB_USERNAME=your-username
GITHUB_TOKEN=ghp_...
GHCR_TOKEN=ghp_...

# Docker registry (в данном случае GHCR)
DOCKER_REGISTRY=ghcr.io/your-username
GITHUB_REPOSITORY=your-username/<проект>

# Пароль к базе данных PostgreSQL
DB_PASSWORD=your-db-password
```

> Убедитесь, что ваш GitHub Token имеет права `write:packages`, `read:packages` и `repo`.

5. Запустите Jenkins

```bash
cd jenkins
docker compose up -d --build
```

Jenkins будет доступен по адресу: [http://localhost:8080](http://localhost:8080)

---

## Как использовать

1. Откройте Jenkins: [http://localhost:8080](http://localhost:8080)
2. Перейдите в задачу `YandexHelmApp` → `Build Now`
3. Jenkins выполнит:
    - сборку и тесты
    - сборку Docker-образов
    - публикацию образов в GHCR
    - деплой в Kubernetes в два namespace: `test` и `prod`

---

## Проверка успешного деплоя

### 1. Добавьте записи в `/etc/hosts`

```bash
sudo nano /etc/hosts
```

Добавьте:

```text
127.0.0.1 keycloak.test.local
127.0.0.1 front.test.local
```

### 2. Отправьте запросы на `/actuator/health`

```bash
curl -s http://front.test.local/actuator/health
```

**Ожидаемый ответ:**

```json
{
  "status": "UP",
  "groups": [
    "liveness",
    "readiness"
  ]
}
```

---

## Завершение работы и очистка

Если вы хотите полностью остановить Jenkins, удалить namespace'ы `test` и `prod`, а также все установленные ресурсы,
используйте скрипт `nuke-all.sh`.

Он находится в папке `jenkins`:

```bash
cd jenkins
./nuke-all.sh
```