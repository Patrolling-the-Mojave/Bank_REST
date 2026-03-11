# Сценарии использования (User Scenarios) Bank_Rest

## Общие положения
- **Акторы**: Гость (неаутентифицированный), Пользователь (ROLE_USER), Администратор (ROLE_ADMIN).
- Все защищённые endpoints требуют JWT в заголовке `Authorization: Bearer <token>`.
- Пагинация: параметры `from` (номер страницы, начиная с 0) и `size` (размер страницы).

---

## Гость

| Действие | Метод | Эндпоинт |
|----------|-------|----------|
| Регистрация | POST | `/api/auth/register` |
| Вход в систему | POST | `/api/auth/login` |
| Проверка существования username | GET | `/api/auth/check-username?username={username}` |
| Обновление токенов | POST | `/api/auth/refresh` |

---

## Пользователь (ROLE_USER)

| Действие | Метод | Эндпоинт |
|----------|-------|----------|
| Просмотр своего профиля | GET | `/api/private/users` |
| Обновление профиля | PUT | `/api/private/users` |
| Просмотр своих карт (с пагинацией) | GET | `/api/private/cards?from={from}&size={size}` |
| Просмотр баланса карты | GET | `/api/private/cards/{cardId}/balance` |
| Блокировка своей карты | POST | `/api/private/cards/block` |
| Перевод между своими картами | POST | `/api/private/cards/transfer` |

---

## Администратор (ROLE_ADMIN)

### Управление пользователями

| Действие | Метод | Эндпоинт |
|----------|-------|----------|
| Создание пользователя | POST | `/api/admin/users` |
| Просмотр всех пользователей | GET | `/api/admin/users?from={from}&size={size}` |
| Просмотр пользователя по ID | GET | `/api/admin/users/{userId}` |
| Поиск пользователя по username | GET | `/api/admin/users/search?username={username}` |
| Обновление пользователя | PUT | `/api/admin/users/{userId}` |
| Блокировка пользователя | POST | `/api/admin/users/{userId}/block` |
| Активация пользователя | POST | `/api/admin/users/{userId}/activate` |
| Удаление пользователя | DELETE | `/api/admin/users/{userId}` |

### Управление картами

| Действие | Метод | Эндпоинт |
|----------|-------|----------|
| Создание карты для пользователя | POST | `/api/admin/cards` |
| Просмотр всех карт | GET | `/api/admin/cards?from={from}&size={size}` |
| Просмотр карт конкретного пользователя | GET | `/api/admin/cards/user/{userId}?from={from}&size={size}` |
| Просмотр карты по ID | GET | `/api/admin/cards/{cardId}` |
| Блокировка карты | POST | `/api/admin/cards/{cardId}/block` |
| Активация карты | POST | `/api/admin/cards/{cardId}/activate` |
| Удаление карты | DELETE | `/api/admin/cards/{cardId}` |

### Просмотр транзакций

| Действие | Метод | Эндпоинт |
|----------|-------|----------|
| Просмотр всех транзакций | GET | `/api/admin/transactions?from={from}&size={size}` |
| Просмотр транзакции по ID | GET | `/api/admin/transactions/{transactionId}` |