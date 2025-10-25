SELECT u.id, u.email, u.login, u.name, u.birthday
FROM friendships f1
JOIN friendships f2 ON f1.addressee_id = f2.addressee_id
JOIN users u ON u.id = f1.addressee_id
WHERE f1.requester_id = :userId AND f2.requester_id = :friendId --AND f1.status = 'CONFIRMED' AND f2.status = 'CONFIRMED'
ORDER BY u.id;