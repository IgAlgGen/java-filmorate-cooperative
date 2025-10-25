SELECT u.id, u.email, u.login, u.name, u.birthday
FROM friendships f
JOIN users u ON u.id = f.addressee_id
WHERE f.requester_id = :userId --AND f.status = 'CONFIRMED'
ORDER BY u.id;